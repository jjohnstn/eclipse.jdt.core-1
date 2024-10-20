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
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jdt.internal.core.util.Util;

import java.io.*;
import java.util.*;


/**
 * The abstract superclass of Java builders.
 * Provides the building and compilation mechanism
 * in common with the batch and incremental builders.
 */
public abstract class AbstractImageBuilder implements ICompilerRequestor {

protected JavaBuilder javaBuilder;
protected State newState;

// local copies
protected NameEnvironment nameEnvironment;
protected ClasspathMultiDirectory[] sourceLocations;
protected BuildNotifier notifier;

protected Compiler compiler;
protected WorkQueue workQueue;
protected ArrayList problemSourceFiles;
protected boolean compiledAllAtOnce;

private boolean inCompiler;

/** 
 * this is a Map<IFile, Set<String>> where each String is a fully-qualified name
 * of an extra dependency introduced by a compilation participant.
 */
private Map extraDependencyMap;

public static int MAX_AT_ONCE = 1000;
public final static String[] JAVA_PROBLEM_MARKER_ATTRIBUTE_NAMES = {
					IMarker.MESSAGE, 
					IMarker.SEVERITY, 
					IJavaModelMarker.ID, 
					IMarker.CHAR_START, 
					IMarker.CHAR_END, 
					IMarker.LINE_NUMBER, 
					IJavaModelMarker.ARGUMENTS};
public final static String[] JAVA_TASK_MARKER_ATTRIBUTE_NAMES = {
	IMarker.MESSAGE, 
	IMarker.PRIORITY, 
	IJavaModelMarker.ID, 
	IMarker.CHAR_START, 
	IMarker.CHAR_END, 
	IMarker.LINE_NUMBER, 
	IJavaModelMarker.ARGUMENTS};
public final static Integer S_ERROR = new Integer(IMarker.SEVERITY_ERROR);
public final static Integer S_WARNING = new Integer(IMarker.SEVERITY_WARNING);
public final static Integer P_HIGH = new Integer(IMarker.PRIORITY_HIGH);
public final static Integer P_NORMAL = new Integer(IMarker.PRIORITY_NORMAL);
public final static Integer P_LOW = new Integer(IMarker.PRIORITY_LOW);

protected AbstractImageBuilder(JavaBuilder javaBuilder) {
	this.javaBuilder = javaBuilder;
	this.newState = new State(javaBuilder);

	// local copies
	this.nameEnvironment = javaBuilder.nameEnvironment;
	this.sourceLocations = this.nameEnvironment.sourceLocations;
	this.notifier = javaBuilder.notifier;

	this.compiler = newCompiler();
	this.workQueue = new WorkQueue();
	this.problemSourceFiles = new ArrayList(3);
}

public void acceptResult(CompilationResult result) {
	// In Batch mode, we write out the class files, hold onto the dependency info
	// & additional types and report problems.

	// In Incremental mode, when writing out a class file we need to compare it
	// against the previous file, remembering if structural changes occured.
	// Before reporting the new problems, we need to update the problem count &
	// remove the old problems. Plus delete additional class files that no longer exist.

	SourceFile compilationUnit = (SourceFile) result.getCompilationUnit(); // go directly back to the sourceFile
	if (!workQueue.isCompiled(compilationUnit)) {
		workQueue.finished(compilationUnit);

		try {
			updateProblemsFor(compilationUnit, result); // record compilation problems before potentially adding duplicate errors
			updateTasksFor(compilationUnit, result); // record tasks
		} catch (CoreException e) {
			throw internalException(e);
		}

		if (result.hasInconsistentToplevelHierarchies)
			// ensure that this file is always retrieved from source for the rest of the build
			if (!problemSourceFiles.contains(compilationUnit))
				problemSourceFiles.add(compilationUnit);

		IType mainType = null;
		String mainTypeName = null;
		String typeLocator = compilationUnit.typeLocator();
		ClassFile[] classFiles = result.getClassFiles();
		int length = classFiles.length;
		ArrayList duplicateTypeNames = null;
		ArrayList definedTypeNames = new ArrayList(length);
		for (int i = 0; i < length; i++) {
			ClassFile classFile = classFiles[i];

			char[][] compoundName = classFile.getCompoundName();
			char[] typeName = compoundName[compoundName.length - 1];
			boolean isNestedType = classFile.enclosingClassFile != null;

			// Look for a possible collision, if one exists, report an error but do not write the class file
			if (isNestedType) {
				String qualifiedTypeName = new String(classFile.outerMostEnclosingClassFile().fileName());
				if (newState.isDuplicateLocator(qualifiedTypeName, typeLocator))
					continue;
			} else {
				String qualifiedTypeName = new String(classFile.fileName()); // the qualified type name "p1/p2/A"
				if (newState.isDuplicateLocator(qualifiedTypeName, typeLocator)) {
					if (duplicateTypeNames == null)
						duplicateTypeNames = new ArrayList();
					duplicateTypeNames.add(compoundName);
					if (mainType == null)
						try {
							mainTypeName = compilationUnit.initialTypeName; // slash separated qualified name "p1/p1/A"
							mainType = javaBuilder.javaProject.findType(mainTypeName.replace('/', '.'));
						} catch (JavaModelException e) {
							// ignore
						}
					IType type;
					if (qualifiedTypeName.equals(mainTypeName))
						type = mainType;
					else {
						String simpleName = qualifiedTypeName.substring(qualifiedTypeName.lastIndexOf('/')+1);
						type = mainType == null ? null : mainType.getCompilationUnit().getType(simpleName);
					}
					createProblemFor(compilationUnit.resource, type, Messages.bind(Messages.build_duplicateClassFile, new String(typeName)), JavaCore.ERROR); 
					continue;
				}
				newState.recordLocatorForType(qualifiedTypeName, typeLocator);
			}
			try {
				definedTypeNames.add(writeClassFile(classFile, compilationUnit, !isNestedType));
			} catch (CoreException e) {
				Util.log(e, "JavaBuilder handling CoreException"); //$NON-NLS-1$
				if (e.getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS)
					createProblemFor(compilationUnit.resource, null, Messages.bind(Messages.build_classFileCollision, e.getMessage()), JavaCore.ERROR); 
				else
					createProblemFor(compilationUnit.resource, null, Messages.build_inconsistentClassFile, JavaCore.ERROR); 
			}
		}
		finishedWith(typeLocator, result, compilationUnit.getMainTypeName(), definedTypeNames, duplicateTypeNames);
		notifier.compiled(compilationUnit);
	}
}

protected void cleanUp() {
	this.nameEnvironment.cleanup();

	this.javaBuilder = null;
	this.nameEnvironment = null;
	this.sourceLocations = null;
	this.notifier = null;
	this.compiler = null;
	this.workQueue = null;
	this.problemSourceFiles = null;
}

/* Compile the given elements, adding more elements to the work queue 
* if they are affected by the changes.
*/
protected void compile(SourceFile[] units) {
	int unitsLength = units.length;

	this.compiledAllAtOnce = unitsLength <= MAX_AT_ONCE;
	if (this.compiledAllAtOnce) {
		// do them all now
		if (JavaBuilder.DEBUG)
			for (int i = 0; i < unitsLength; i++)
				System.out.println("About to compile " + units[i].typeLocator()); //$NON-NLS-1$
		compile(units, null);
	} else {
		int i = 0;
		boolean compilingFirstGroup = true;
		while (i < unitsLength) {
			int doNow = unitsLength < MAX_AT_ONCE ? unitsLength : MAX_AT_ONCE;
			int index = 0;
			SourceFile[] toCompile = new SourceFile[doNow];
			while (i < unitsLength && index < doNow) {
				// Although it needed compiling when this method was called, it may have
				// already been compiled when it was referenced by another unit.
				SourceFile unit = units[i++];
				if (compilingFirstGroup || workQueue.isWaiting(unit)) {
					if (JavaBuilder.DEBUG)
						System.out.println("About to compile " + unit.typeLocator()); //$NON-NLS-1$
					toCompile[index++] = unit;
				}
			}
			if (index < doNow)
				System.arraycopy(toCompile, 0, toCompile = new SourceFile[index], 0, index);
			SourceFile[] additionalUnits = new SourceFile[unitsLength - i];
			System.arraycopy(units, i, additionalUnits, 0, additionalUnits.length);
			compilingFirstGroup = false;
			compile(toCompile, additionalUnits);
		}	
	}
}


/**
 *  notify the CompilationParticipants of the pre-build event 
 *  @return a map that maps source units to problems encountered during the prebuild process.
 */
private  Map notifyCompilationParticipants(
		ICompilationUnit[] sourceUnits, 
		Set newFiles, 
		Set deletedFiles, 
		Map extraDependencies,
		int round) {
	List cps = JavaCore
			.getCompilationParticipants( CompilationParticipant.PRE_BUILD_EVENT,
					javaBuilder.javaProject );
	if ( cps.isEmpty() ) {
		return null;
	}

	IFile[] files = new IFile[sourceUnits.length];
	for ( int i = 0; i < files.length; i++ ) {
		if ( sourceUnits[i] instanceof SourceFile ) {
			files[i] = ( ( SourceFile ) sourceUnits[i] ).getFile();
		} else {
			String fname = new String( sourceUnits[i].getFileName() );
			files[i] = javaBuilder.javaProject.getProject().getFile( fname );
		}
	}
	PreBuildCompilationEvent pbce = new PreBuildCompilationEvent( files, 
			javaBuilder.javaProject,
			!javaBuilder.nameEnvironment.isIncrementalBuild, 
			round);

	java.util.Iterator it = cps.iterator();
	Map ifiles2problems = new HashMap();
	while ( it.hasNext() ) {
		CompilationParticipant p = ( CompilationParticipant ) it.next();

		CompilationParticipantResult cpr = p.notify( pbce );
		if ( cpr instanceof PreBuildCompilationResult ) {
			PreBuildCompilationResult pbcr = ( PreBuildCompilationResult ) cpr;

			IFile[] f = pbcr.getNewFiles();
			if ( f != null ) {
				for ( int i = 0; i < f.length; i++ )
					newFiles.add( f[i] );
			}
			
			f = pbcr.getDeletedFiles();
			if ( f != null ) { 
				for ( int i = 0; i < f.length; i++ ) 
					deletedFiles.add( f[i] );
			}
			
			mergeMaps( pbcr.getNewDependencies(), extraDependencies );
			mergeMaps( pbcr.getProblems(), ifiles2problems );
		}
	}
	
	if ( newFiles.size() > 0 ) {
		Set newFiles_2 = new HashSet();
		Set deletedFiles_2 = new HashSet();
		ICompilationUnit[] newFileArray = ifileSet2SourceFileArray( newFiles );
		final Map newFiles2Problems = notifyCompilationParticipants( newFileArray, newFiles_2, deletedFiles_2, extraDependencies, round+1 );
		newFiles.addAll( newFiles_2 );
		deletedFiles.addAll( deletedFiles_2 );
		
		mergeMaps( newFiles2Problems, ifiles2problems);
	}
	
	return convertKey(files, sourceUnits, ifiles2problems);
}	

/**
 * Convert the key of the map from <code>IFile</code> to the corresponding <code>ICompilationUnit</code>
 * @param files parallel to <code>units</code>. The <code>IFile</code> of the corresponding <code>ICompilationUnit</code> 
 * @param units parallel to <code>files</code>. The <code>ICompilationUnit</code> of the corresponding <code>IFile</code>
 * @param problems Map between <code>IFile</code> and its list of <code>IProblem</code>
 *                        Content of this map will be destructively modified.
 * @return a map between <code>ICompilationUnit</code> and its list of <code>IProblem</code>
 */
private Map convertKey(final IFile[] files, final ICompilationUnit[] units, Map problems)
{	
	for( int i=0, len=files.length; i<len; i++ ){				
		Object val = problems.remove(files[i]);
		if( val != null )
			problems.put(units[i], val);
	}
	
	return problems;
}

/** 
 *   Given a Map which maps from a key to a value, where key is an arbitrary 
 *   type, and where value is a Collection, mergeMaps will ensure that for a key 
 *   k with value v in source, all of the elements in the Collection v will be 
 *   moved into the Collection v' corresponding to key k in the destination Map. 
 * 
 * @param source - The source map from some key to a Collection.
 * @param destination - The destination map from some key to a Collection
 */
private static void mergeMaps( Map source, Map destination ) {
	Iterator keys = source.keySet().iterator();
	while( keys.hasNext() ) {
		Object key = keys.next();
		Object val = destination.get( key );
		if ( val != null ) {
			Collection c = (Collection) val;
			c.addAll( (Collection)source.get( key ) );
		}
		else {
			destination.put( key, source.get( key ) );
		}
	}
}



/** 
 * given a source file, determine which of the project's source folders the file lives 
 */
protected ClasspathMultiDirectory getSourceLocationForFile(IFile file) {
	ClasspathMultiDirectory md = null;
	md = sourceLocations[0];
	if ( sourceLocations.length > 1 ) {
		IPath sourceFileFullPath = file.getFullPath();
		for ( int j = 0, m = sourceLocations.length; j < m; j++ ) {
			if ( sourceLocations[j].sourceFolder.getFullPath()
					.isPrefixOf( sourceFileFullPath ) ) {
				md = sourceLocations[j];
				if ( md.exclusionPatterns == null
						&& md.inclusionPatterns == null )
					break;
				if ( !Util.isExcluded( file, md.inclusionPatterns,
						md.exclusionPatterns ) )
					break;
			}
		}
	}
	return md;
}

/**
 * copies IFile entries in a Set<IFile> into SourceFile entries into a SourceFile[]. 
 * Copying starts at the specified start position.
 */
private void ifileSet2SourceFileArray( Set ifiles, SourceFile[] sourceFiles, int start ) {
	Iterator it = ifiles.iterator();
	while ( it.hasNext() ) {
		IFile f = ( IFile ) it.next();
		sourceFiles[start++] = new SourceFile( f, getSourceLocationForFile( f ) );
	}	
}

/**
 *  Given a Set<IFile>, this method returns a SourceFile[] where each entry 
 *  in the array corresponds to an entry in the set.
 */
private SourceFile[] ifileSet2SourceFileArray( Set ifiles ) {
	SourceFile[] sf = new SourceFile[ ifiles.size() ];
	ifileSet2SourceFileArray( ifiles, sf, 0 );
	return sf;
}

private SourceFile[] updateSourceUnits( SourceFile[] units, Set newFiles, Set deletedFiles ) {
	
	if ( newFiles.size() == 0 && deletedFiles.size() == 0 )
		return units;
	else if ( deletedFiles.size() == 0 ) {
		// files have only been added
		SourceFile[] newUnits = new SourceFile[ units.length + newFiles.size() ];
		System.arraycopy( units, 0, newUnits, 0, units.length );
		ifileSet2SourceFileArray( newFiles, newUnits, units.length );
		return newUnits;
	}
	else {
		// files have been added & deleted.  Deal with deleted files first.  If 
		// someone reports that a file has been added and deleted, then it will be 
		// added.
		HashSet unitSet = new HashSet();
		for ( int i=0; i<units.length; i++ )
			unitSet.add( units[i].getFile() );
		Iterator it = deletedFiles.iterator();
		while ( it.hasNext() )	{
			IFile f = (IFile) it.next();
			if ( unitSet.contains( f ) )
				unitSet.remove( f );
			handleFileDeletedByCompilationParticipant( f );
		}
		unitSet.addAll( newFiles );
		return ifileSet2SourceFileArray( unitSet );
	}
}

protected void handleFileDeletedByCompilationParticipant( IFile f )
{
	// noop
}

void compile(SourceFile[] units, SourceFile[] additionalUnits) {
	if (units.length == 0) return;
	notifier.aboutToCompile(units[0]); // just to change the message

	// extend additionalFilenames with all hierarchical problem types found during this entire build
	if (!problemSourceFiles.isEmpty()) {
		int toAdd = problemSourceFiles.size();
		int length = additionalUnits == null ? 0 : additionalUnits.length;
		if (length == 0)
			additionalUnits = new SourceFile[toAdd];
		else
			System.arraycopy(additionalUnits, 0, additionalUnits = new SourceFile[length + toAdd], 0, length);
		for (int i = 0; i < toAdd; i++)
			additionalUnits[length + i] = (SourceFile) problemSourceFiles.get(i);
	}
	String[] initialTypeNames = new String[units.length];
	for (int i = 0, l = units.length; i < l; i++)
		initialTypeNames[i] = units[i].initialTypeName;
	nameEnvironment.setNames(initialTypeNames, additionalUnits);
	notifier.checkCancel();
	try {
		inCompiler = true;
		
		// notify compilation participants, 
		Set newFiles = new HashSet();
		Set deletedFiles = new HashSet();
		extraDependencyMap = new HashMap();
		Map units2Problems = notifyCompilationParticipants( units, newFiles, deletedFiles, extraDependencyMap, 0 );

		// update units array with the new & deleted files
		units = updateSourceUnits( units, newFiles, deletedFiles );
		
		compiler.compile(units, units2Problems);
		
		// we should be done with this map here, so null it out for GC
		extraDependencyMap = null;
		
	} catch (AbortCompilation ignored) {
		// ignore the AbortCompilcation coming from BuildNotifier.checkCancelWithinCompiler()
		// the Compiler failed after the user has chose to cancel... likely due to an OutOfMemory error
	} finally {
		inCompiler = false;
	}
	// Check for cancel immediately after a compile, because the compiler may
	// have been cancelled but without propagating the correct exception
	notifier.checkCancel();
}

protected void createProblemFor(IResource resource, IMember javaElement, String message, String problemSeverity) {
	try {
		IMarker marker = resource.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
		int severity = problemSeverity.equals(JavaCore.WARNING) ? IMarker.SEVERITY_WARNING : IMarker.SEVERITY_ERROR;

		ISourceRange range = javaElement == null ? null : javaElement.getNameRange();
		int start = range == null ? 0 : range.getOffset();
		int end = range == null ? 1 : start + range.getLength();
		marker.setAttributes(
			new String[] {IMarker.MESSAGE, IMarker.SEVERITY, IMarker.CHAR_START, IMarker.CHAR_END},
			new Object[] {message, new Integer(severity), new Integer(start), new Integer(end)});
	} catch (CoreException e) {
		throw internalException(e);
	}
}

protected void finishedWith(String sourceLocator, CompilationResult result, char[] mainTypeName, ArrayList definedTypeNames, ArrayList duplicateTypeNames) {
		
	char[][][] qualifiedRefs = result.qualifiedReferences;
	char[][] simpleRefs = result.simpleNameReferences;
	
	if ( extraDependencyMap != null && extraDependencyMap.size() > 0 ) {
		IFile f = ResourcesPlugin.getWorkspace().getRoot().getFile( new Path( new String( result.fileName ) ));
		Set s  = (Set)extraDependencyMap.get( f );
		if ( s != null && s.size() > 0 ) {
			NameSet simpleNameSet = new NameSet( simpleRefs.length + s.size() );
			QualifiedNameSet qualifiedNameSet = new QualifiedNameSet( qualifiedRefs.length + s.size() );

			//
			// add in all of the existing refs to the sets to filter out duplicates
			//
			for ( int i = 0; i< simpleRefs.length; i++ ) 
				simpleNameSet.add( simpleRefs[i] );

			for ( int i = 0; i< qualifiedRefs.length; i++ ) 
				qualifiedNameSet.add( qualifiedRefs[i] );

			//
			// get all of the the parts of the new dependencies into sets
			// for  a dependency "a.b.c.d", we want the qualifiedNameSet to include
			// "a.b.c.d", "a.b.c" & "a.b" and we want the simple name set to include
			// "a", "b", "c" & "d".
			//
			Iterator it = s.iterator();
			while ( it.hasNext() ) {
				char[] array = ((String) it.next() ).toCharArray();
				char[][] parts = CharOperation.splitOn('.', array );
				for ( int i = 0; i<parts.length; i++ )
					simpleNameSet.add( parts[i] );
				
				for( int i = parts.length - 1; i > 0; --i ) {
					qualifiedNameSet.add( parts );
					parts = CharOperation.subarray( parts, 0, i );
				}
			}
	
			//
			// strip out any null entries in the arrays retrieved from the sets.
			//
			simpleRefs = new char[ simpleNameSet.elementSize][];
			char[][] names = simpleNameSet.names;
			int j = 0;
			for ( int i = 0; i<names.length; i++ )
				if ( names[i] != null)
					simpleRefs[j++] = names[i];
			
			qualifiedRefs = new char[ qualifiedNameSet.elementSize ][][];
			j = 0;
			char[][][] qnames = qualifiedNameSet.qualifiedNames;
			for ( int i = 0; i< qnames.length; i++ )
				if ( qnames[i] != null )
					qualifiedRefs[j++] = qnames[i];
			
		}
	}
		
	if (duplicateTypeNames == null) {
		newState.record(sourceLocator, qualifiedRefs, simpleRefs, mainTypeName, definedTypeNames);
		return;
	}
	
	// for each duplicate type p1.p2.A, add the type name A (package was already added)
	next : for (int i = 0, l = duplicateTypeNames.size(); i < l; i++) {
		char[][] compoundName = (char[][]) duplicateTypeNames.get(i);
		char[] typeName = compoundName[compoundName.length - 1];
		int sLength = simpleRefs.length;
		for (int j = 0; j < sLength; j++)
			if (CharOperation.equals(simpleRefs[j], typeName))
				continue next;
		System.arraycopy(simpleRefs, 0, simpleRefs = new char[sLength + 1][], 0, sLength);
		simpleRefs[sLength] = typeName;
	}
	newState.record(sourceLocator, qualifiedRefs, simpleRefs, mainTypeName, definedTypeNames);
}

protected IContainer createFolder(IPath packagePath, IContainer outputFolder) throws CoreException {
	if (packagePath.isEmpty()) return outputFolder;
	IFolder folder = outputFolder.getFolder(packagePath);
	if (!folder.exists()) {
		createFolder(packagePath.removeLastSegments(1), outputFolder);
		folder.create(true, true, null);
		folder.setDerived(true);
	}
	return folder;
}

protected RuntimeException internalException(CoreException t) {
	ImageBuilderInternalException imageBuilderException = new ImageBuilderInternalException(t);
	if (inCompiler)
		return new AbortCompilation(true, imageBuilderException);
	return imageBuilderException;
}

protected Compiler newCompiler() {
	// disable entire javadoc support if not interested in diagnostics
	Map projectOptions = javaBuilder.javaProject.getOptions(true);
	String option = (String) projectOptions.get(JavaCore.COMPILER_PB_INVALID_JAVADOC);
	if (option == null || option.equals(JavaCore.IGNORE)) { // TODO (frederic) see why option is null sometimes while running model tests!?
		option = (String) projectOptions.get(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS);
		if (option == null || option.equals(JavaCore.IGNORE)) {
			option = (String) projectOptions.get(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS);
			if (option == null || option.equals(JavaCore.IGNORE)) {
				option = (String) projectOptions.get(JavaCore.COMPILER_PB_UNUSED_IMPORT);
				if (option == null || option.equals(JavaCore.IGNORE)) { // Unused import need also to look inside javadoc comment
					projectOptions.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.DISABLED);
				}
			}
		}
	}
	
	// called once when the builder is initialized... can override if needed
	Compiler newCompiler = new Compiler(
		nameEnvironment,
		DefaultErrorHandlingPolicies.proceedWithAllProblems(),
		projectOptions,
		this,
		ProblemFactory.getProblemFactory(Locale.getDefault()));
	CompilerOptions options = newCompiler.options;

	// enable the compiler reference info support
	options.produceReferenceInfo = true;
	
	org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment env = newCompiler.lookupEnvironment;
	synchronized (env) {
		// enable shared byte[]'s used by ClassFile to avoid allocating MBs during a build
		env.sharedArraysUsed = false;
		env.sharedClassFileHeader = new byte[30000];
		env.sharedClassFileContents = new byte[30000];
	}

	return newCompiler;
}

protected boolean isExcludedFromProject(IPath childPath) throws JavaModelException {
	// answer whether the folder should be ignored when walking the project as a source folder
	if (childPath.segmentCount() > 2) return false; // is a subfolder of a package

	for (int j = 0, k = sourceLocations.length; j < k; j++) {
		if (childPath.equals(sourceLocations[j].binaryFolder.getFullPath())) return true;
		if (childPath.equals(sourceLocations[j].sourceFolder.getFullPath())) return true;
	}
	// skip default output folder which may not be used by any source folder
	return childPath.equals(javaBuilder.javaProject.getOutputLocation());
}

/**
 * Creates a marker from each problem and adds it to the resource.
 * The marker is as follows:
 *   - its type is T_PROBLEM
 *   - its plugin ID is the JavaBuilder's plugin ID
 *	 - its message is the problem's message
 *	 - its priority reflects the severity of the problem
 *	 - its range is the problem's range
 *	 - it has an extra attribute "ID" which holds the problem's id
 */
protected void storeProblemsFor(SourceFile sourceFile, IProblem[] problems) throws CoreException {
	if (sourceFile == null || problems == null || problems.length == 0) return;

	String missingClassFile = null;
	IResource resource = sourceFile.resource;
	for (int i = 0, l = problems.length; i < l; i++) {
		IProblem problem = problems[i];
		int id = problem.getID();
		if (id == IProblem.IsClassPathCorrect) {
			JavaBuilder.removeProblemsAndTasksFor(javaBuilder.currentProject); // make this the only problem for this project
			String[] args = problem.getArguments();
			missingClassFile = args[0];
		}

		if (id != IProblem.Task) {
			IMarker marker = resource.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
			marker.setAttributes(
				JAVA_PROBLEM_MARKER_ATTRIBUTE_NAMES,
				new Object[] { 
					problem.getMessage(),
					problem.isError() ? S_ERROR : S_WARNING, 
					new Integer(id),
					new Integer(problem.getSourceStart()),
					new Integer(problem.getSourceEnd() + 1),
					new Integer(problem.getSourceLineNumber()),
					Util.getProblemArgumentsForMarker(problem.getArguments())
				});
		}

/* Do NOT want to populate the Java Model just to find the matching Java element.
 * Also cannot query compilation units located in folders with invalid package
 * names such as 'a/b.c.d/e'.

		// compute a user-friendly location
		IJavaElement element = JavaCore.create(resource);
		if (element instanceof org.eclipse.jdt.core.ICompilationUnit) { // try to find a finer grain element
			org.eclipse.jdt.core.ICompilationUnit unit = (org.eclipse.jdt.core.ICompilationUnit) element;
			IJavaElement fragment = unit.getElementAt(problem.getSourceStart());
			if (fragment != null) element = fragment;
		}
		String location = null;
		if (element instanceof JavaElement)
			location = ((JavaElement) element).readableName();
		if (location != null)
			marker.setAttribute(IMarker.LOCATION, location);
*/

		if (missingClassFile != null)
			throw new MissingClassFileException(missingClassFile);
	}
}

protected void storeTasksFor(SourceFile sourceFile, IProblem[] tasks) throws CoreException {
	if (sourceFile == null || tasks == null || tasks.length == 0) return;

	IResource resource = sourceFile.resource;
	for (int i = 0, l = tasks.length; i < l; i++) {
		IProblem task = tasks[i];
		if (task.getID() == IProblem.Task) {
			IMarker marker = resource.createMarker(IJavaModelMarker.TASK_MARKER);
			Integer priority = P_NORMAL;
			String compilerPriority = task.getArguments()[2];
			if (JavaCore.COMPILER_TASK_PRIORITY_HIGH.equals(compilerPriority))
				priority = P_HIGH;
			else if (JavaCore.COMPILER_TASK_PRIORITY_LOW.equals(compilerPriority))
				priority = P_LOW;
			marker.setAttributes(
				JAVA_TASK_MARKER_ATTRIBUTE_NAMES,
				new Object[] { 
					task.getMessage(),
					priority,
					new Integer(task.getID()),
					new Integer(task.getSourceStart()),
					new Integer(task.getSourceEnd() + 1),
					new Integer(task.getSourceLineNumber()),
					Boolean.FALSE,
				});
		}
	}
}

protected void updateProblemsFor(SourceFile sourceFile, CompilationResult result) throws CoreException {
	IProblem[] problems = result.getProblems();
	if (problems == null || problems.length == 0) return;

	notifier.updateProblemCounts(problems);
	storeProblemsFor(sourceFile, problems);
}

protected void updateTasksFor(SourceFile sourceFile, CompilationResult result) throws CoreException {
	IProblem[] tasks = result.getTasks();
	if (tasks == null || tasks.length == 0) return;

	storeTasksFor(sourceFile, tasks);
}

protected char[] writeClassFile(ClassFile classFile, SourceFile compilationUnit, boolean isSecondaryType) throws CoreException {
	String fileName = new String(classFile.fileName()); // the qualified type name "p1/p2/A"
	IPath filePath = new Path(fileName);
	IContainer outputFolder = compilationUnit.sourceLocation.binaryFolder; 
	IContainer container = outputFolder;
	if (filePath.segmentCount() > 1) {
		container = createFolder(filePath.removeLastSegments(1), outputFolder);
		filePath = new Path(filePath.lastSegment());
	}

	IFile file = container.getFile(filePath.addFileExtension(SuffixConstants.EXTENSION_class));
	writeClassFileBytes(classFile.getBytes(), file, fileName, isSecondaryType, compilationUnit.updateClassFile);
	if (classFile.ownSharedArrays) {
		org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment env = this.compiler.lookupEnvironment;
		synchronized (env) {
			env.sharedArraysUsed = false;
		}
	}

	// answer the name of the class file as in Y or Y$M
	return filePath.lastSegment().toCharArray();
}

protected void writeClassFileBytes(byte[] bytes, IFile file, String qualifiedFileName, boolean isSecondaryType, boolean updateClassFile) throws CoreException {
	if (file.exists()) {
		// Deal with shared output folders... last one wins... no collision cases detected
		if (JavaBuilder.DEBUG)
			System.out.println("Writing changed class file " + file.getName());//$NON-NLS-1$
		file.setContents(new ByteArrayInputStream(bytes), true, false, null);
		if (!file.isDerived())
			file.setDerived(true);
	} else {
		// Default implementation just writes out the bytes for the new class file...
		if (JavaBuilder.DEBUG)
			System.out.println("Writing new class file " + file.getName());//$NON-NLS-1$
		file.create(new ByteArrayInputStream(bytes), IResource.FORCE, null);
		file.setDerived(true);
	}
}
}
