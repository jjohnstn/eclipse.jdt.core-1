/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

/**
 * This operation copies/moves/renames a collection of resources from their current
 * container to a new container, optionally renaming the
 * elements.
 * <p>Notes:<ul>
 *    <li>If there is already an resource with the same name in
 *    the new container, the operation either overwrites or aborts,
 *    depending on the collision policy setting. The default setting is
 *	  abort.
 *
 *    <li>When a compilation unit is copied to a new package, the
 *    package declaration in the compilation unit is automatically updated.
 *
 *    <li>The collection of elements being copied must all share the
 *    same type of container.
 *
 *    <li>This operation can be used to copy and rename elements within
 *    the same container. 
 *
 *    <li>This operation only copies compilation units and package fragments.
 *    It does not copy package fragment roots - a platform operation must be used for that.
 * </ul>
 *
 */
public class CopyResourceElementsOperation extends MultiOperation implements SuffixConstants {
	/**
	 * The list of new resources created during this operation.
	 */
	protected ArrayList createdElements;
	/**
	 * Table specifying deltas for elements being 
	 * copied/moved/renamed. Keyed by elements' project(s), and
	 * values are the corresponding deltas.
	 */
	protected Map deltasPerProject = new HashMap(1);
	/**
	 * The <code>ASTParser</code> used to manipulate the source code of
	 * <code>ICompilationUnit</code>.
	 */
	protected ASTParser parser;
	/**
	 * When executed, this operation will copy the given resources to the
	 * given container.
	 */
	public CopyResourceElementsOperation(IJavaElement[] resourcesToCopy, IJavaElement destContainer, boolean force) {
		this(resourcesToCopy, new IJavaElement[]{destContainer}, force);
	}
	/**
	 * When executed, this operation will copy the given resources to the
	 * given containers.  The resources and destination containers must be in
	 * the correct order. If there is > 1 destination, the number of destinations
	 * must be the same as the number of resources being copied/moved.
	 */
	public CopyResourceElementsOperation(IJavaElement[] resourcesToCopy, IJavaElement[] destContainers, boolean force) {
		super(resourcesToCopy, destContainers, force);
		initializeASTParser();
	}
	private void initializeASTParser() {
		this.parser = ASTParser.newParser(AST.JLS3);
	}
	/**
	 * Returns the children of <code>source</code> which are affected by this operation.
	 * If <code>source</code> is a <code>K_SOURCE</code>, these are the <code>.java</code>
	 * files, if it is a <code>K_BINARY</code>, they are the <code>.class</code> files.
	 */
	private IResource[] collectResourcesOfInterest(IPackageFragment source) throws JavaModelException {
		IJavaElement[] children = source.getChildren();
		int childOfInterest = IJavaElement.COMPILATION_UNIT;
		if (source.getKind() == IPackageFragmentRoot.K_BINARY) {
			childOfInterest = IJavaElement.CLASS_FILE;
		}
		ArrayList correctKindChildren = new ArrayList(children.length);
		for (int i = 0; i < children.length; i++) {
			IJavaElement child = children[i];
			if (child.getElementType() == childOfInterest) {
				correctKindChildren.add(child.getResource());
			}
		}
		// Gather non-java resources
		Object[] nonJavaResources = source.getNonJavaResources();
		int actualNonJavaResourceCount = 0;
		for (int i = 0, max = nonJavaResources.length; i < max; i++){
			if (nonJavaResources[i] instanceof IResource) actualNonJavaResourceCount++;
		}
		IResource[] actualNonJavaResources = new IResource[actualNonJavaResourceCount];
		for (int i = 0, max = nonJavaResources.length, index = 0; i < max; i++){
			if (nonJavaResources[i] instanceof IResource) actualNonJavaResources[index++] = (IResource)nonJavaResources[i];
		}
		
		if (actualNonJavaResourceCount != 0) {
			int correctKindChildrenSize = correctKindChildren.size();
			IResource[] result = new IResource[correctKindChildrenSize + actualNonJavaResourceCount];
			correctKindChildren.toArray(result);
			System.arraycopy(actualNonJavaResources, 0, result, correctKindChildrenSize, actualNonJavaResourceCount);
			return result;
		} else {
			IResource[] result = new IResource[correctKindChildren.size()];
			correctKindChildren.toArray(result);
			return result;
		}
	}
	/**
	 * Creates any destination package fragment(s) which do not exists yet.
	 * Return true if a read-only package fragment has been found among package fragments, false otherwise
	 */
	private boolean createNeededPackageFragments(IContainer sourceFolder, PackageFragmentRoot root, String[] newFragName, boolean moveFolder) throws JavaModelException {
		boolean containsReadOnlyPackageFragment = false;
		IContainer parentFolder = (IContainer) root.getResource();
		JavaElementDelta projectDelta = null;
		String[] sideEffectPackageName = null;
		char[][] inclusionPatterns = root.fullInclusionPatternChars();
		char[][] exclusionPatterns = root.fullExclusionPatternChars();
		for (int i = 0; i < newFragName.length; i++) {
			String subFolderName = newFragName[i];
			sideEffectPackageName = Util.arrayConcat(sideEffectPackageName, subFolderName);
			IResource subFolder = parentFolder.findMember(subFolderName);
			if (subFolder == null) {
				// create deepest folder only if not a move (folder will be moved in processPackageFragmentResource)
				if (!(moveFolder && i == newFragName.length-1)) {
					createFolder(parentFolder, subFolderName, force);
				}
				parentFolder = parentFolder.getFolder(new Path(subFolderName));
				sourceFolder = sourceFolder.getFolder(new Path(subFolderName));
				if (Util.isReadOnly(sourceFolder)) {
					containsReadOnlyPackageFragment = true;
				}
				IPackageFragment sideEffectPackage = root.getPackageFragment(sideEffectPackageName);
				if (i < newFragName.length - 1 // all but the last one are side effect packages
						&& !Util.isExcluded(parentFolder, inclusionPatterns, exclusionPatterns)) { 
					if (projectDelta == null) {
						projectDelta = getDeltaFor(root.getJavaProject());
					}
					projectDelta.added(sideEffectPackage);
				}
				createdElements.add(sideEffectPackage);
			} else {
				parentFolder = (IContainer) subFolder;
			}
		}
		return containsReadOnlyPackageFragment;
	}
	
	/**
	 * Returns the <code>JavaElementDelta</code> for <code>javaProject</code>,
	 * creating it and putting it in <code>fDeltasPerProject</code> if
	 * it does not exist yet.
	 */
	private JavaElementDelta getDeltaFor(IJavaProject javaProject) {
		JavaElementDelta delta = (JavaElementDelta) deltasPerProject.get(javaProject);
		if (delta == null) {
			delta = new JavaElementDelta(javaProject);
			deltasPerProject.put(javaProject, delta);
		}
		return delta;
	}
	/**
	 * @see MultiOperation
	 */
	protected String getMainTaskName() {
		return Messages.operation_copyResourceProgress; 
	}
	/**
	 * Sets the deltas to register the changes resulting from this operation
	 * for this source element and its destination.
	 * If the operation is a cross project operation<ul>
	 * <li>On a copy, the delta should be rooted in the dest project
	 * <li>On a move, two deltas are generated<ul>
	 * 			<li>one rooted in the source project
	 *			<li>one rooted in the destination project</ul></ul>
	 * If the operation is rooted in a single project, the delta is rooted in that project
	 * 	 
	 */
	protected void prepareDeltas(IJavaElement sourceElement, IJavaElement destinationElement, boolean isMove) {
		if (Util.isExcluded(sourceElement) || Util.isExcluded(destinationElement)) return;
		IJavaProject destProject = destinationElement.getJavaProject();
		if (isMove) {
			IJavaProject sourceProject = sourceElement.getJavaProject();
			getDeltaFor(sourceProject).movedFrom(sourceElement, destinationElement);
			getDeltaFor(destProject).movedTo(destinationElement, sourceElement);
		} else {
			getDeltaFor(destProject).added(destinationElement);
		}
	}
	/**
	 * Copies/moves a compilation unit with the name <code>newCUName</code>
	 * to the destination package.<br>
	 * The package statement in the compilation unit is updated if necessary.
	 * The main type of the compilation unit is renamed if necessary.
	 *
	 * @exception JavaModelException if the operation is unable to
	 * complete
	 */
	private void processCompilationUnitResource(ICompilationUnit source, PackageFragment dest) throws JavaModelException {
		String newCUName = getNewNameFor(source);
		String destName = (newCUName != null) ? newCUName : source.getElementName();
		ASTRewrite rewrite = updateContent(source, dest, newCUName); // null if unchanged
	
		// TODO (frederic) remove when bug 67606 will be fixed (bug 67823)
		// store encoding (fix bug 66898)
		IFile sourceResource = (IFile)source.getResource();
		String sourceEncoding = null;
		try {
			sourceEncoding = sourceResource.getCharset(false);
		}
		catch (CoreException ce) {
			// no problem, use default encoding
		}
		// end todo
		// copy resource
		IContainer destFolder = (IContainer)dest.getResource(); // can be an IFolder or an IProject
		IFile destFile = destFolder.getFile(new Path(destName));
		org.eclipse.jdt.internal.core.CompilationUnit destCU = new org.eclipse.jdt.internal.core.CompilationUnit(dest, destName, DefaultWorkingCopyOwner.PRIMARY);
		if (!destFile.equals(sourceResource)) {
			try {
				if (destFile.exists()) {
					if (this.force) {
						// we can remove it
						deleteResource(destFile, IResource.KEEP_HISTORY);
						destCU.close(); // ensure the in-memory buffer for the dest CU is closed
					} else {
						// abort
						throw new JavaModelException(new JavaModelStatus(
							IJavaModelStatusConstants.NAME_COLLISION, 
							Messages.bind(Messages.status_nameCollision, destFile.getFullPath().toString()))); 
					}
				}
				int flags = this.force ? IResource.FORCE : IResource.NONE;
				if (this.isMove()) {
					flags |= IResource.KEEP_HISTORY;
					sourceResource.move(destFile.getFullPath(), flags, getSubProgressMonitor(1));
				} else {
					if (rewrite != null) flags |= IResource.KEEP_HISTORY;
					sourceResource.copy(destFile.getFullPath(), flags, getSubProgressMonitor(1));
				}
				this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE); 
			} catch (JavaModelException e) {
				throw e;
			} catch (CoreException e) {
				throw new JavaModelException(e);
			}
	
			// update new resource content
			if (rewrite != null){
				boolean wasReadOnly = destFile.isReadOnly();
				try {
					saveContent(dest, destName, rewrite, sourceEncoding, destFile);
				} catch (CoreException e) {
					if (e instanceof JavaModelException) throw (JavaModelException) e;
					throw new JavaModelException(e);
				} finally {
					Util.setReadOnly(destFile, wasReadOnly);
				}
			}
		
			// register the correct change deltas
			prepareDeltas(source, destCU, isMove());
			if (newCUName != null) {
				//the main type has been renamed
				String oldName = Util.getNameWithoutJavaLikeExtension(source.getElementName());
				String newName = Util.getNameWithoutJavaLikeExtension(newCUName);
				prepareDeltas(source.getType(oldName), destCU.getType(newName), isMove());
			}
		} else {
			if (!this.force) {
				throw new JavaModelException(new JavaModelStatus(
					IJavaModelStatusConstants.NAME_COLLISION, 
					Messages.bind(Messages.status_nameCollision, destFile.getFullPath().toString()))); 
			}
			// update new resource content
			// in case we do a saveas on the same resource we have to simply update the contents
			// see http://dev.eclipse.org/bugs/show_bug.cgi?id=9351
			try {
				if (rewrite != null){
					saveContent(dest, destName, rewrite, sourceEncoding, destFile);
				}
			} catch (CoreException e) {
				if (e instanceof JavaModelException) throw (JavaModelException) e;
				throw new JavaModelException(e);
			}
		}
	}
	/**
	 * Process all of the changed deltas generated by this operation.
	 */
	protected void processDeltas() {
		for (Iterator deltas = this.deltasPerProject.values().iterator(); deltas.hasNext();){
			addDelta((IJavaElementDelta) deltas.next());
		}
	}
	/**
	 * @see MultiOperation
	 * This method delegates to <code>processCompilationUnitResource</code> or
	 * <code>processPackageFragmentResource</code>, depending on the type of
	 * <code>element</code>.
	 */
	protected void processElement(IJavaElement element) throws JavaModelException {
		IJavaElement dest = getDestinationParent(element);
		switch (element.getElementType()) {
			case IJavaElement.COMPILATION_UNIT :
				processCompilationUnitResource((ICompilationUnit) element, (PackageFragment) dest);
				createdElements.add(((IPackageFragment) dest).getCompilationUnit(element.getElementName()));
				break;
			case IJavaElement.PACKAGE_FRAGMENT :
				processPackageFragmentResource((PackageFragment) element, (PackageFragmentRoot) dest, getNewNameFor(element));
				break;
			default :
				throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, element));
		}
	}
	/**
	 * @see MultiOperation
	 * Overridden to allow special processing of <code>JavaElementDelta</code>s
	 * and <code>fResultElements</code>.
	 */
	protected void processElements() throws JavaModelException {
		createdElements = new ArrayList(elementsToProcess.length);
		try {
			super.processElements();
		} catch (JavaModelException jme) {
			throw jme;
		} finally {
			resultElements = new IJavaElement[createdElements.size()];
			createdElements.toArray(resultElements);
			processDeltas();
		}
	}
	/**
	 * Copies/moves a package fragment with the name <code>newName</code>
	 * to the destination package.<br>
	 *
	 * @exception JavaModelException if the operation is unable to
	 * complete
	 */
	private void processPackageFragmentResource(PackageFragment source, PackageFragmentRoot root, String newName) throws JavaModelException {
		try {
			String[] newFragName = (newName == null) ? source.names : Util.getTrimmedSimpleNames(newName);
			IPackageFragment newFrag = root.getPackageFragment(newFragName);
			IResource[] resources = collectResourcesOfInterest(source);
			
			// if isMove() can we move the folder itself ? (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=22458)
			boolean shouldMoveFolder = isMove() && !newFrag.getResource().exists(); // if new pkg fragment exists, it is an override
			IFolder srcFolder = (IFolder)source.getResource();
			IPath destPath = newFrag.getPath();
			if (shouldMoveFolder) {
				// check if destination is not included in source
				if (srcFolder.getFullPath().isPrefixOf(destPath)) {
					shouldMoveFolder = false;
				} else {
					// check if there are no sub-packages
					IResource[] members = srcFolder.members();
					for (int i = 0; i < members.length; i++) {
						if ( members[i] instanceof IFolder) {
							shouldMoveFolder = false;
							break;
						}
					}
				}	
			}
			boolean containsReadOnlySubPackageFragments = createNeededPackageFragments((IContainer) source.getParent().getResource(), root, newFragName, shouldMoveFolder);
			boolean sourceIsReadOnly = Util.isReadOnly(srcFolder);
	
			// Process resources
			if (shouldMoveFolder) {
				// move underlying resource
				// TODO Revisit once bug 43044 is fixed
				if (sourceIsReadOnly) {
					Util.setReadOnly(srcFolder, false);
				}
				srcFolder.move(destPath, force, true /* keep history */, getSubProgressMonitor(1));
				if (sourceIsReadOnly) {
					Util.setReadOnly(srcFolder, true);
				}
				this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE); 
			} else {
				// process the leaf resources
				if (resources.length > 0) {
					if (isRename()) {
						if (! destPath.equals(source.getPath())) {
							moveResources(resources, destPath);
						}
					} else if (isMove()) {
						// we need to delete this resource if this operation wants to override existing resources
						for (int i = 0, max = resources.length; i < max; i++) {
							IResource destinationResource = ResourcesPlugin.getWorkspace().getRoot().findMember(destPath.append(resources[i].getName()));
							if (destinationResource != null) {
								if (force) {
									deleteResource(destinationResource, IResource.KEEP_HISTORY);
								} else {
									throw new JavaModelException(new JavaModelStatus(
										IJavaModelStatusConstants.NAME_COLLISION, 
										Messages.bind(Messages.status_nameCollision, destinationResource.getFullPath().toString()))); 
								}
							}
						}
						moveResources(resources, destPath);
					} else {
						// we need to delete this resource if this operation wants to override existing resources
						for (int i = 0, max = resources.length; i < max; i++) {
							IResource destinationResource = ResourcesPlugin.getWorkspace().getRoot().findMember(destPath.append(resources[i].getName()));
							if (destinationResource != null) {
								if (force) {
									// we need to delete this resource if this operation wants to override existing resources
									deleteResource(destinationResource, IResource.KEEP_HISTORY);
								} else {
									throw new JavaModelException(new JavaModelStatus(
										IJavaModelStatusConstants.NAME_COLLISION, 
										Messages.bind(Messages.status_nameCollision, destinationResource.getFullPath().toString()))); 
								}
							}
						}
						copyResources(resources, destPath);
					}
				}
			}
	
			// Update package statement in compilation unit if needed
			if (!Util.equalArraysOrNull(newFragName, source.names)) { // if package has been renamed, update the compilation units
				char[][] inclusionPatterns = root.fullInclusionPatternChars();
				char[][] exclusionPatterns = root.fullExclusionPatternChars();
				for (int i = 0; i < resources.length; i++) {
					String resourceName = resources[i].getName();
					if (Util.isJavaLikeFileName(resourceName)) {
						// we only consider potential compilation units
						ICompilationUnit cu = newFrag.getCompilationUnit(resourceName);
						if (Util.isExcluded(cu.getPath(), inclusionPatterns, exclusionPatterns, false/*not a folder*/)) continue;
						this.parser.setSource(cu);
						CompilationUnit astCU = (CompilationUnit) this.parser.createAST(this.progressMonitor);
						AST ast = astCU.getAST();
						ASTRewrite rewrite = ASTRewrite.create(ast);
						updatePackageStatement(astCU, newFragName, rewrite);
						IDocument document = getDocument(cu);
						TextEdit edits = rewrite.rewriteAST(document, null);
						try {
							edits.apply(document);
						} catch (BadLocationException e) {
							throw new JavaModelException(e, IJavaModelStatusConstants.INVALID_CONTENTS);
						}			
						cu.save(null, false);
					}
				}
			}
			
			// Discard empty old package (if still empty after the rename)
			boolean isEmpty = true;
			if (isMove()) {
				// delete remaining files in this package (.class file in the case where Proj=src=bin)
				// in case of a copy
				updateReadOnlyPackageFragmentsForMove((IContainer) source.getParent().getResource(), root, newFragName, sourceIsReadOnly);
				if (srcFolder.exists()) {
					IResource[] remaining = srcFolder.members();
					for (int i = 0, length = remaining.length; i < length; i++) {
						IResource file = remaining[i];
						if (file instanceof IFile) {
							if (Util.isReadOnly(file)) {
								Util.setReadOnly(file, false);
							}
							this.deleteResource(file, IResource.FORCE | IResource.KEEP_HISTORY);
						} else {
							isEmpty = false;
						}
					}
				}
				if (isEmpty) {
					IResource rootResource;
					// check if source is included in destination
					if (destPath.isPrefixOf(srcFolder.getFullPath())) {
						rootResource = newFrag.getResource();
					} else {
						rootResource =  source.getParent().getResource();
					}
					
					// delete recursively empty folders
					deleteEmptyPackageFragment(source, false, rootResource);
				}
			} else if (containsReadOnlySubPackageFragments) {
				// in case of a copy
				updateReadOnlyPackageFragmentsForCopy((IContainer) source.getParent().getResource(), root, newFragName);
			}
			// workaround for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=24505
			if (isEmpty && isMove() && !(Util.isExcluded(source) || Util.isExcluded(newFrag))) {
				IJavaProject sourceProject = source.getJavaProject();
				getDeltaFor(sourceProject).movedFrom(source, newFrag);
				IJavaProject destProject = newFrag.getJavaProject();
				getDeltaFor(destProject).movedTo(newFrag, source);
			}
		} catch (JavaModelException e) {
			throw e;
		} catch (CoreException ce) {
			throw new JavaModelException(ce);
		}
	}
	private void saveContent(PackageFragment dest, String destName, ASTRewrite rewrite, String sourceEncoding, IFile destFile) throws JavaModelException {
		try {
			// TODO (frederic) remove when bug 67606 will be fixed (bug 67823)
			// fix bug 66898
			if (sourceEncoding != null) destFile.setCharset(sourceEncoding, this.progressMonitor);
			// end todo
		}
		catch (CoreException ce) {
			// use no encoding
		}
		// when the file was copied, its read-only flag was preserved -> temporary set it to false
		// note this doesn't interfer with repository providers as this is a new resource that cannot be under
		// version control yet
		Util.setReadOnly(destFile, false);
		ICompilationUnit destCU = dest.getCompilationUnit(destName);
		IDocument document = getDocument(destCU);
		TextEdit edits = rewrite.rewriteAST(document, null);
		try {
			edits.apply(document);
		} catch (BadLocationException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.INVALID_CONTENTS);
		}
		destCU.save(getSubProgressMonitor(1), this.force);
	}
	/**
	 * Updates the content of <code>cu</code>, modifying the type name and/or package
	 * declaration as necessary.
	 *
	 * @return an AST rewrite or null if no rewrite needed
	 */
	private ASTRewrite updateContent(ICompilationUnit cu, PackageFragment dest, String newName) throws JavaModelException {
		String[] currPackageName = ((PackageFragment) cu.getParent()).names;
		String[] destPackageName = dest.names;
		if (Util.equalArraysOrNull(currPackageName, destPackageName) && newName == null) {
			return null; //nothing to change
		} else {
			// ensure cu is consistent (noop if already consistent)
			cu.makeConsistent(this.progressMonitor);
			this.parser.setSource(cu);
			CompilationUnit astCU = (CompilationUnit) this.parser.createAST(this.progressMonitor);
			AST ast = astCU.getAST();
			ASTRewrite rewrite = ASTRewrite.create(ast);
			updateTypeName(cu, astCU, cu.getElementName(), newName, rewrite);
			updatePackageStatement(astCU, destPackageName, rewrite);
			return rewrite;
		}
	}
	private void updatePackageStatement(CompilationUnit astCU, String[] pkgName, ASTRewrite rewriter) throws JavaModelException {
		boolean defaultPackage = pkgName.length == 0;
		AST ast = astCU.getAST();
		if (defaultPackage) {
			// remove existing package statement
			if (astCU.getPackage() != null)
				rewriter.set(astCU, CompilationUnit.PACKAGE_PROPERTY, null, null);
		} else {
			org.eclipse.jdt.core.dom.PackageDeclaration pkg = astCU.getPackage();
			if (pkg != null) {
				// rename package statement
				Name name = ast.newName(pkgName);
				rewriter.set(pkg, PackageDeclaration.NAME_PROPERTY, name, null);
			} else {
				// create new package statement
				pkg = ast.newPackageDeclaration();
				pkg.setName(ast.newName(pkgName));
				rewriter.set(astCU, CompilationUnit.PACKAGE_PROPERTY, pkg, null);
			}
		}
	}
	
	private void updateReadOnlyPackageFragmentsForCopy(IContainer sourceFolder, IPackageFragmentRoot root, String[] newFragName) {
		IContainer parentFolder = (IContainer) root.getResource();
		for (int i = 0, length = newFragName.length; i <length; i++) {
			String subFolderName = newFragName[i];
			parentFolder = parentFolder.getFolder(new Path(subFolderName));
			sourceFolder = sourceFolder.getFolder(new Path(subFolderName));
			if (sourceFolder.exists() && Util.isReadOnly(sourceFolder)) {
				Util.setReadOnly(parentFolder, true);
			}
		}
	}

	private void updateReadOnlyPackageFragmentsForMove(IContainer sourceFolder, IPackageFragmentRoot root, String[] newFragName, boolean sourceFolderIsReadOnly) {
		IContainer parentFolder = (IContainer) root.getResource();
		for (int i = 0, length = newFragName.length; i < length; i++) {
			String subFolderName = newFragName[i];
			parentFolder = parentFolder.getFolder(new Path(subFolderName));
			sourceFolder = sourceFolder.getFolder(new Path(subFolderName));
			if ((sourceFolder.exists() && Util.isReadOnly(sourceFolder)) || (i == length - 1 && sourceFolderIsReadOnly)) {
				Util.setReadOnly(parentFolder, true);
				// the source folder will be deleted anyway (move operation)
				Util.setReadOnly(sourceFolder, false);
			}
		}
	}
			/**
		 * Renames the main type in <code>cu</code>.
		 */
		private void updateTypeName(ICompilationUnit cu, CompilationUnit astCU, String oldName, String newName, ASTRewrite rewriter) throws JavaModelException {
			if (newName != null) {
				String oldTypeName= Util.getNameWithoutJavaLikeExtension(oldName);
				String newTypeName= Util.getNameWithoutJavaLikeExtension(newName);
				AST ast = astCU.getAST();
				// update main type name
				IType[] types = cu.getTypes();
				for (int i = 0, max = types.length; i < max; i++) {
					IType currentType = types[i];
					if (currentType.getElementName().equals(oldTypeName)) {
						AbstractTypeDeclaration typeNode = (AbstractTypeDeclaration) ((JavaElement) currentType).findNode(astCU);
						if (typeNode != null) {
							// rename type
							rewriter.replace(typeNode.getName(), ast.newSimpleName(newTypeName), null);
							// rename constructors
							Iterator bodyDeclarations = typeNode.bodyDeclarations().iterator();
							while (bodyDeclarations.hasNext()) {
								Object bodyDeclaration = bodyDeclarations.next();
								if (bodyDeclaration instanceof MethodDeclaration) {
									MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
									if (methodDeclaration.isConstructor()) {
										SimpleName methodName = methodDeclaration.getName();
										if (methodName.getIdentifier().equals(oldTypeName)) {
											rewriter.replace(methodName, ast.newSimpleName(newTypeName), null);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	/**
	 * Possible failures:
	 * <ul>
	 *  <li>NO_ELEMENTS_TO_PROCESS - no elements supplied to the operation
	 *	<li>INDEX_OUT_OF_BOUNDS - the number of renamings supplied to the operation
	 *		does not match the number of elements that were supplied.
	 * </ul>
	 */
	protected IJavaModelStatus verify() {
		IJavaModelStatus status = super.verify();
		if (!status.isOK()) {
			return status;
		}
	
		if (this.renamingsList != null && this.renamingsList.length != elementsToProcess.length) {
			return new JavaModelStatus(IJavaModelStatusConstants.INDEX_OUT_OF_BOUNDS);
		}
		return JavaModelStatus.VERIFIED_OK;
	}
	/**
	 * @see MultiOperation
	 */
	protected void verify(IJavaElement element) throws JavaModelException {
		if (element == null || !element.exists())
			error(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, element);
			
		if (element.isReadOnly() && (isRename() || isMove()))
			error(IJavaModelStatusConstants.READ_ONLY, element);

		IResource resource = element.getResource();
		if (resource instanceof IFolder) {
			if (resource.isLinked()) {
				error(IJavaModelStatusConstants.INVALID_RESOURCE, element);
			}
		}
	
		int elementType = element.getElementType();
	
		if (elementType == IJavaElement.COMPILATION_UNIT) {
			org.eclipse.jdt.internal.core.CompilationUnit compilationUnit = (org.eclipse.jdt.internal.core.CompilationUnit) element;
			if (isMove() && compilationUnit.isWorkingCopy() && !compilationUnit.isPrimary())
				error(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, element);
		} else if (elementType != IJavaElement.PACKAGE_FRAGMENT) {
			error(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, element);
		}
		
		JavaElement dest = (JavaElement) getDestinationParent(element);
		verifyDestination(element, dest);
		if (this.renamings != null) {
			verifyRenaming(element);
		}
}
}
