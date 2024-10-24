/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.*;

public class DeletePackageFragmentRootOperation extends JavaModelOperation {

	int updateResourceFlags;
	int updateModelFlags;

	public DeletePackageFragmentRootOperation(
		IPackageFragmentRoot root,
		int updateResourceFlags,
		int updateModelFlags) {
			
		super(root);
		this.updateResourceFlags = updateResourceFlags;
		this.updateModelFlags = updateModelFlags;
	}

	protected void executeOperation() throws JavaModelException {
		
		IPackageFragmentRoot root = (IPackageFragmentRoot)this.getElementToProcess();
		IClasspathEntry rootEntry = root.getRawClasspathEntry();
		
		// delete resource
		if (!root.isExternal() && (this.updateModelFlags & IPackageFragmentRoot.NO_RESOURCE_MODIFICATION) == 0) {
			deleteResource(root, rootEntry);
		}

		// update classpath if needed
		if ((this.updateModelFlags & IPackageFragmentRoot.ORIGINATING_PROJECT_CLASSPATH) != 0) {
			updateProjectClasspath(rootEntry.getPath(), root.getJavaProject());
		}
		if ((this.updateModelFlags & IPackageFragmentRoot.OTHER_REFERRING_PROJECTS_CLASSPATH) != 0) {
			updateReferringProjectClasspaths(rootEntry.getPath(), root.getJavaProject());
		}
	}

	protected void deleteResource(
		IPackageFragmentRoot root,
		IClasspathEntry rootEntry)
		throws JavaModelException {
		final char[][] exclusionPatterns = ((ClasspathEntry)rootEntry).fullExclusionPatternChars();
		IResource rootResource = root.getResource();
		if (rootEntry.getEntryKind() != IClasspathEntry.CPE_SOURCE || exclusionPatterns == null) {
			try {
				rootResource.delete(this.updateResourceFlags, progressMonitor);
			} catch (CoreException e) {
				throw new JavaModelException(e);
			}
		} else {
			final IPath[] nestedFolders = getNestedFolders(root);
			IResourceProxyVisitor visitor = new IResourceProxyVisitor() {
				public boolean visit(IResourceProxy proxy) throws CoreException {
					if (proxy.getType() == IResource.FOLDER) {
						IPath path = proxy.requestFullPath();
						if (prefixesOneOf(path, nestedFolders)) {
							// equals if nested source folder
							return !equalsOneOf(path, nestedFolders);
						} else {
							// subtree doesn't contain any nested source folders
							proxy.requestResource().delete(updateResourceFlags, progressMonitor);
							return false;
						}
					} else {
						proxy.requestResource().delete(updateResourceFlags, progressMonitor);
						return false;
					}
				}
			};
			try {
				rootResource.accept(visitor, IResource.NONE);
			} catch (CoreException e) {
				throw new JavaModelException(e);
			}
		}
		this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE); 
	}


	/*
	 * Deletes the classpath entries equals to the given rootPath from all Java projects.
	 */
	protected void updateReferringProjectClasspaths(IPath rootPath, IJavaProject projectOfRoot) throws JavaModelException {
		IJavaModel model = this.getJavaModel();
		IJavaProject[] projects = model.getJavaProjects();
		for (int i = 0, length = projects.length; i < length; i++) {
			IJavaProject project = projects[i];
			if (project.equals(projectOfRoot)) continue;
			updateProjectClasspath(rootPath, project);
		}
	}

	/*
	 * Deletes the classpath entries equals to the given rootPath from the given project.
	 */
	protected void updateProjectClasspath(IPath rootPath, IJavaProject project)
		throws JavaModelException {
		IClasspathEntry[] classpath = project.getRawClasspath();
		IClasspathEntry[] newClasspath = null;
		int cpLength = classpath.length;
		int newCPIndex = -1;
		for (int j = 0; j < cpLength; j++) {
			IClasspathEntry entry = classpath[j];
			if (rootPath.equals(entry.getPath())) {
				if (newClasspath == null) {
					newClasspath = new IClasspathEntry[cpLength-1];
					System.arraycopy(classpath, 0, newClasspath, 0, j);
					newCPIndex = j;
				}
			} else if (newClasspath != null) {
				newClasspath[newCPIndex++] = entry;
			}
		}
		if (newClasspath != null) {
			if (newCPIndex < newClasspath.length) {
				System.arraycopy(newClasspath, 0, newClasspath = new IClasspathEntry[newCPIndex], 0, newCPIndex);
			}
			project.setRawClasspath(newClasspath, progressMonitor);
		}
	}	
	protected IJavaModelStatus verify() {
		IJavaModelStatus status = super.verify();
		if (!status.isOK()) {
			return status;
		}
		IPackageFragmentRoot root = (IPackageFragmentRoot) this.getElementToProcess();
		if (root == null || !root.exists()) {
			return new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, root);
		}

		IResource resource = root.getResource();
		if (resource instanceof IFolder) {
			if (resource.isLinked()) {
				return new JavaModelStatus(IJavaModelStatusConstants.INVALID_RESOURCE, root);
			}
		}
		return JavaModelStatus.VERIFIED_OK;
	}

}
