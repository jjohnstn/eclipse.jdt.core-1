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
import org.eclipse.jdt.core.IJavaElement;

/**
 * A Java search match that represents a local variable declaration.
 * The element is an <code>ILocalVariable</code>.
 * <p>
 * This class is intended to be instantiated and subclassed by clients.
 * </p>
 * 
 * @since 3.0
 */
public class LocalVariableDeclarationMatch extends SearchMatch {

	/**
	 * Creates a new local variable declaration match.
	 * 
	 * @param element the local variable declaration
	 * @param accuracy one of A_ACCURATE or A_INACCURATE
	 * @param offset the offset the match starts at, or -1 if unknown
	 * @param length the length of the match, or -1 if unknown
	 * @param participant the search participant that created the match
	 * @param resource the resource of the element
	 */
	public LocalVariableDeclarationMatch(IJavaElement element, int accuracy, int offset, int length, SearchParticipant participant, IResource resource) {
		super(element, accuracy, offset, length, participant, resource);
	}

}
