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
package org.eclipse.jdt.core.search;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;

/**
 * A search participant describes a particular extension to a generic search
 * mechanism, permitting combined search actions which will involve all required
 * participants.
 * <p>
 * A search participant is involved in the indexing phase and in the search phase. 
 * The indexing phase consists in taking one or more search documents, parse them, and
 * add index entries in an index chosen by the participant. An index is identified by a
 * path on disk.
 * The search phase consists in selecting the indexes corresponding to a search pattern
 * and a search scope, from these indexes the search infrastructure extracts the document paths
 * that match the search pattern asking the search participant for the corresponding document,
 * finally the search participant is asked to locate the matches precisely in these search documents.
 * </p>
 * <p>
 * This class is intended to be subclassed by clients. During the indexing phase, 
 * a subclass will be called with the following requests in order:
 * <ul>
 * <li>{@link #scheduleDocumentIndexing(SearchDocument, IPath)}</li>
 * <li>{@link #indexDocument(SearchDocument, IPath)}</li>
 * </ul>
 * During the search phase, a subclass will be called with the following requests in order:
 * <ul>
 * <li>{@link #selectIndexes(SearchPattern, IJavaSearchScope)}</li>
 * <li>one or more {@link #getDocument(String)}</li>
 * <li>{@link #locateMatches(SearchDocument[], SearchPattern, IJavaSearchScope, SearchRequestor, IProgressMonitor)}</li>
 * </ul>
 * </p>
 * 
 * @since 3.0
 */
public abstract class SearchParticipant {

	/**
	 * Creates a new search participant.
	 */
	protected SearchParticipant() {
		// do nothing
	}
	
	/**
	 * Notification that this participant's help is needed in a search.
	 * <p>
	 * This method should be re-implemented in subclasses that need to do something
	 * when the participant is needed in a search.
	 * </p>
	 */
	public void beginSearching() {
		// do nothing
	}

	/**
	 * Notification that this participant's help is no longer needed.
	 * <p>
	 * This method should be re-implemented in subclasses that need to do something
	 * when the participant is no longer needed in a search.
	 * </p>
	 */
	public void doneSearching() {
		// do nothing
	}

	/**
	 * Returns a displayable name of this search participant.
	 * <p>
	 * This method should be re-implemented in subclasses that need to 
	 * display a meaningfull name.
	 * </p>
	 * 
	 * @return the displayable name of this search participant
	 */
	public String getDescription() {
		return "Search participant"; //$NON-NLS-1$
	}

	/**
	 * Returns a search document for the given path.
	 * The given document path is a string that uniquely identifies the document.
	 * Most of the time it is a workspace-relative path, but it can also be a file system path, or a path inside a zip file.
	 * <p>
	 * Implementors of this method can either create an instance of their own subclass of 
	 * {@link SearchDocument} or return an existing instance of such a subclass.
	 * </p>
	 * 
	 * @param documentPath the path of the document.
	 * @return a search document
	 */
	public abstract SearchDocument getDocument(String documentPath);

	/**
	 * Indexes the given document in the given index. A search participant
	 * asked to index a document should parse it and call 
	 * {@link SearchDocument#addIndexEntry(char[], char[])} as many times as
	 * needed to add index entries to the index. If delegating to another
	 * participant, it should use the original index location (and not the
	 * delegatee's one). In the particular case of delegating to the default
	 * search participant (see {@link SearchEngine#getDefaultSearchParticipant()}),
	 * the provided document's path must be a path ending with '.java' or a '.class'.
	 * <p>
	 * The given index location must represent a path in the file system to a file that
	 * either already exists or is going to be created. If it exists, it must be an index file,
	 * otherwise its data might be overwritten.
	 * </p><p>
	 * Clients are not expected to call this method.
	 * </p>
	 * 
	 * @param document the document to index
	 * @param indexLocation the location in the file system to the index
	 */
	public abstract void indexDocument(SearchDocument document, IPath indexLocation);

	/**
	 * Locates the matches in the given documents using the given search pattern
	 * and search scope, and reports them to the givenn search requestor. This
	 * method is called by the search engine once it has search documents
	 * matching the given pattern in the given search scope.
	 * <p>
	 * Note that a participant (e.g. a JSP participant) can pre-process the contents of the given documents, 
	 * create its own documents whose contents are Java compilation units and delegate the match location 
	 * to the default participant (see {@link SearchEngine#getDefaultSearchParticipant()}). Passing its own
	 * {@link SearchRequestor} this particpant can then map the match positions back to the original
	 * contents, create its own matches and report them to the original requestor.
	 * </p><p>
	 * Implementors of this method should check the progress monitor
	 * for cancelation when it is safe and appropriate to do so.  The cancelation
	 * request should be propagated to the caller by throwing 
	 * <code>OperationCanceledException</code>.
	 * </p>
	 * 
	 * @param documents the documents to locate matches in
	 * @param pattern the search pattern to use when locating matches
	 * @param scope the scope to limit the search to
	 * @param requestor the requestor to report matches to
	 * @param monitor the progress monitor to report progress to,
	 * or <code>null</code> if no progress should be reported
	 * @throws CoreException if the requestor had problem accepting one of the matches
	 */
	public abstract void locateMatches(SearchDocument[] documents, SearchPattern pattern, IJavaSearchScope scope, SearchRequestor requestor, IProgressMonitor monitor) throws CoreException;

	/**
	 * Schedules the indexing of the given document.
	 * Once the document is ready to be indexed, 
	 * {@link #indexDocument(SearchDocument, IPath) indexDocument(document, indexPath)}
	 * will be called in a different thread than the caller's thread.
	 * <p>
	 * The given index location must represent a path in the file system to a file that
	 * either already exists or is going to be created. If it exists, it must be an index file,
	 * otherwise its data might be overwritten.
	 * </p>
	 * 
	 * @param document the document to index
	 * @param indexLocation the location on the file system of the index
	 */
	public final void scheduleDocumentIndexing(SearchDocument document, IPath indexLocation) {
		IPath documentPath = new Path(document.getPath());
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		Object file = JavaModel.getTarget(root, documentPath, true);
		IPath containerPath = documentPath;
		if (file instanceof IResource) {
			containerPath = ((IResource)file).getProject().getFullPath();
		} else if (file == null) {
			containerPath = documentPath.removeLastSegments(documentPath.segmentCount()-1);
		}
		IndexManager manager = JavaModelManager.getJavaModelManager().getIndexManager();
		String osIndexLocation = indexLocation.toOSString();
		// TODO (jerome) should not have to create index manually, should expose API that recreates index instead
		manager.ensureIndexExists(osIndexLocation, containerPath);
		manager.scheduleDocumentIndexing(document, containerPath, osIndexLocation, this);
	}

	/**
	 * Returns the collection of index locations to consider when performing the
	 * given search query in the given scope. The search engine calls this
	 * method before locating matches.
	 * <p>
	 * An index location represents a path in the file system to a file that holds index information. 
	 * </p><p>
	 * Clients are not expected to call this method.
	 * </p>
	 * 
	 * @param query the search pattern to consider
	 * @param scope the given search scope
	 * @return the collection of index paths to consider
	 */
	public abstract IPath[] selectIndexes(SearchPattern query, IJavaSearchScope scope);
}
