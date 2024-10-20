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
package org.eclipse.jdt.core.tests.formatter.comment;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextUtilities;

import org.eclipse.jdt.core.formatter.CodeFormatter;

public abstract class CommentTestCase extends TestCase {

	public static final String DELIMITER= TextUtilities.getDefaultLineDelimiter(new Document());

	private Map fUserOptions;

	protected CommentTestCase(String name) {
		super(name);
	}

	/*
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		fUserOptions= null;
	}
	
	protected abstract int getCommentKind();

	protected Map getUserOptions() {
		return fUserOptions;
	}
	
	protected void setUserOption(String name, String value) {
		if (fUserOptions == null)
			fUserOptions= new HashMap();
		
		fUserOptions.put(name, value);
	}
	
	protected final String testFormat(String text) {
		return testFormat(text, 0, text.length());
	}

	protected String testFormat(String text, int offset, int length) {
		return testFormat(text, offset, length, getCommentKind());
	}

	protected String testFormat(String text, int offset, int length, int kind) {
		assertNotNull(text);
		assertTrue(offset >= 0);
		assertTrue(offset < text.length());
		assertTrue(length >= 0);
		assertTrue(offset + length <= text.length());

		assertTrue(kind == CodeFormatter.K_JAVA_DOC || kind == CodeFormatter.K_MULTI_LINE_COMMENT || kind == CodeFormatter.K_SINGLE_LINE_COMMENT);

		return CommentFormatterUtil.format(kind, text, offset, length, CommentFormatterUtil.createOptions(getUserOptions()));
	}
}
