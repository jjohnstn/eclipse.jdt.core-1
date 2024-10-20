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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;

import org.eclipse.jdt.internal.formatter.comment.JavaDocLine;
import org.eclipse.jdt.internal.formatter.comment.MultiCommentLine;

public class JavaDocTestCase extends CommentTestCase {

	protected static final String INFIX= MultiCommentLine.MULTI_COMMENT_CONTENT_PREFIX;

	protected static final String POSTFIX= MultiCommentLine.MULTI_COMMENT_END_PREFIX;

	protected static final String PREFIX= JavaDocLine.JAVADOC_START_PREFIX;
	
	public static Test suite() {
		if (true) {
			return new TestSuite(JavaDocTestCase.class);
		}
		TestSuite suite = new TestSuite(JavaDocTestCase.class.getName());
		suite.addTest(new JavaDocTestCase("testMultiLineComment"));
		suite.addTest(new JavaDocTestCase("testMultiLineComment2"));
		return suite;
	}

	public JavaDocTestCase(String name) {
		super(name);
	}

	protected int getCommentKind() {
		return CodeFormatter.K_JAVA_DOC;
	}

	public void testSingleLineComment1() {
		assertEquals(PREFIX + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX, testFormat(PREFIX + "\t\t" + DELIMITER + "*\t test*/")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	public void testSingleLineComment2() {
		assertEquals(PREFIX + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX, testFormat(PREFIX + "test" + DELIMITER + "\t" + POSTFIX)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	public void testSingleLineComment3() {
		assertEquals(PREFIX + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX, testFormat(PREFIX + DELIMITER + "* test\t*/")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testSingleLineComment4() {
		assertEquals(PREFIX + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX, testFormat(PREFIX + "test" + DELIMITER + POSTFIX)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testSingleLineCommentSpace1() {
		assertEquals(PREFIX + " test" + POSTFIX, testFormat(PREFIX + "test*/")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testSingleLineCommentSpace2() {
		assertEquals(PREFIX + " test" + POSTFIX, testFormat(PREFIX + "test" + POSTFIX)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testSingleLineCommentSpace3() {
		assertEquals(PREFIX + " test" + POSTFIX, testFormat(PREFIX + "test*/")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testSingleLineCommentSpace4() {
		assertEquals(PREFIX + " test test" + POSTFIX, testFormat(PREFIX + " test   test*/")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testSingleLineCommentTabs1() {
		assertEquals(PREFIX + " test test" + POSTFIX, testFormat(PREFIX + "\ttest\ttest" + POSTFIX)); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testSingleLineCommentTabs2() {
		assertEquals(PREFIX + " test test" + POSTFIX, testFormat(PREFIX + "\ttest\ttest*/")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void testMultiLineCommentBreak1() {
		String input= PREFIX + " test<br>test" + POSTFIX; //$NON-NLS-1$
		String expected= PREFIX + DELIMITER + INFIX + "test<br>" + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals(expected, testFormat(input));
	}
	
	public void testMultiLineCommentCodeSnippet1() {
		String prefix= PREFIX + DELIMITER + INFIX + "<pre>" + DELIMITER + INFIX; //$NON-NLS-1$
		String postfix= DELIMITER + INFIX + "</pre>" + DELIMITER + POSTFIX; //$NON-NLS-1$
		String input= prefix + "while (i != 0) i--;" + postfix; //$NON-NLS-1$
		String expected= prefix + "while (i != 0)" + DELIMITER + INFIX + "\ti--;" + postfix;    //$NON-NLS-1$//$NON-NLS-2$
		assertEquals(expected, testFormat(input));
	}
	
	/**
	 * [formatting] Error in formatting parts of java code snippets in comment
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44035
	 */
	public void testMultiLineCommentCodeSnippet2() {
		String prefix= PREFIX + DELIMITER + INFIX + "<pre>" + DELIMITER + INFIX; //$NON-NLS-1$
		String postfix= DELIMITER + INFIX + "</pre>" + DELIMITER + POSTFIX; //$NON-NLS-1$
		String input= prefix + "while (i != 0) { i--; }" + postfix; //$NON-NLS-1$
		String expected= prefix + "while (i != 0) {" + DELIMITER + INFIX + "\ti--;" + DELIMITER + INFIX + "}" + postfix; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals(expected, testFormat(input));
	}
	
	public void testMultiLineCommentCodeSnippet3() {
		String input= PREFIX + DELIMITER + "<pre>" + DELIMITER + "while (i != 0)" + DELIMITER + "i--;" + DELIMITER + "</pre>" + DELIMITER + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		String expected= PREFIX + DELIMITER + INFIX + "<pre>" + DELIMITER + INFIX + "while (i != 0)" + DELIMITER + INFIX + "\ti--;" + DELIMITER + INFIX + "</pre>" + DELIMITER + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		assertEquals(expected, testFormat(input));
	}
	
	public void testMultiLineCommentCodeSnippetHtmlEntities1() {
		String prefix= PREFIX + DELIMITER + INFIX + "<pre>" + DELIMITER + INFIX; //$NON-NLS-1$
		String postfix= DELIMITER + INFIX + "</pre>" + DELIMITER + POSTFIX; //$NON-NLS-1$
		String input= prefix + "System.out.println(\"test\");" + postfix; //$NON-NLS-1$
		String expected= prefix + "System.out.println(&quot;test&quot;);" + postfix; //$NON-NLS-1$
		assertEquals(expected, testFormat(input));
	}
	
	public void testMultiLineCommentIndentTabs1() {
		String prefix= "public class Test {" + DELIMITER + "\t\t"; //$NON-NLS-1$ //$NON-NLS-2$
		String content= PREFIX + DELIMITER + "\t\t\t" + INFIX + "test test" + DELIMITER + "\t\t\t\t" + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		String postfix= DELIMITER + "}"; //$NON-NLS-1$
		String expected= PREFIX + DELIMITER + "\t\t" + INFIX + "test test" + DELIMITER + "\t\t" + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
		assertEquals(prefix + expected + postfix, testFormat(prefix + content + postfix, prefix.length(), content.length()));
	}
	
	/**
	 * [formatting] Comments formatter inserts tabs when it should use spaces
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47491
	 */
	public void testMultiLineCommentIndentSpaces1() {
		String prefix= "public class Test {" + DELIMITER + "\t"; //$NON-NLS-1$ //$NON-NLS-2$
		String content= PREFIX + DELIMITER + "\t\t" + INFIX + "test test" + DELIMITER + "        " + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		String postfix= DELIMITER + "}"; //$NON-NLS-1$
		String expected= PREFIX + DELIMITER + "   " + INFIX + "test test" + DELIMITER + "   " + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "3"); //$NON-NLS-1$
		assertEquals(prefix + expected + postfix, testFormat(prefix + content + postfix, prefix.length(), content.length()));
	}
	
	public void testMultiLineCommentIndentSpaces2() {
		String prefix= "public class Test {" + DELIMITER + "    "; //$NON-NLS-1$ //$NON-NLS-2$
		String content= PREFIX + DELIMITER + "\t\t" + INFIX + "test test" + DELIMITER + "        " + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		String postfix= DELIMITER + "}"; //$NON-NLS-1$
		String expected= PREFIX + DELIMITER + "      " + INFIX + "test test" + DELIMITER + "      " + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "3"); //$NON-NLS-1$
		assertEquals(prefix + expected + postfix, testFormat(prefix + content + postfix, prefix.length(), content.length()));
	}
	
	public void testMultiLineCommentIndentSpaces3() {
		String prefix= "public class Test {" + DELIMITER + "  \t  "; //$NON-NLS-1$ //$NON-NLS-2$
		String content= PREFIX + DELIMITER + "\t\t" + INFIX + "test test" + DELIMITER + "        " + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		String postfix= DELIMITER + "}"; //$NON-NLS-1$
		String expected= PREFIX + DELIMITER + "      " + INFIX + "test test" + DELIMITER + "      " + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "3"); //$NON-NLS-1$
		assertEquals(prefix + expected + postfix, testFormat(prefix + content + postfix, prefix.length(), content.length()));
	}
	
	public void testMultiLineCommentIndentSpaces4() {
		String prefix= "public class Test {" + DELIMITER + "   \t   "; //$NON-NLS-1$ //$NON-NLS-2$
		String content= PREFIX + DELIMITER + "\t\t" + INFIX + "test test" + DELIMITER + "        " + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		String postfix= DELIMITER + "}"; //$NON-NLS-1$
		String expected= PREFIX + DELIMITER + "         " + INFIX + "test test" + DELIMITER + "         " + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "3"); //$NON-NLS-1$
		assertEquals(prefix + expected + postfix, testFormat(prefix + content + postfix, prefix.length(), content.length()));
	}
	
	/**
	 * [formatting] Repeated insertion of new line when formatting javadoc comment
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=50212
	 */
	public void testMultiLineCommentBlankLineAfterPre1() {
		String input= PREFIX + DELIMITER + INFIX + "<pre></pre>" + DELIMITER  + INFIX + "test" + DELIMITER + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$
		String expected= PREFIX + DELIMITER + INFIX + "<pre></pre>" + DELIMITER + INFIX + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$
		String result= testFormat(input);
		assertEquals(expected, result);
		result= testFormat(result);
		assertEquals(expected, result);
	}
	
	/**
	 * [formatting][implementation] comment line length not correctly applied
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46341
	 * Do not wrap.
	 */
	public void testMultiLineCommentLineBreakBeforeImmutableRegions1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "22"); //$NON-NLS-1$
		String input= PREFIX + DELIMITER + INFIX + "a <code>test</code>" + DELIMITER + POSTFIX; //$NON-NLS-1$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}
	
	/**
	 * [formatting][implementation] comment line length not correctly applied
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46341
	 * Do wrap.
	 */
	public void testMultiLineCommentLineBreakBeforeImmutableRegions2() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "21"); //$NON-NLS-1$
		String input= PREFIX + DELIMITER + INFIX + "a <code>test</code>" + DELIMITER + POSTFIX; //$NON-NLS-1$
		String expected= PREFIX + DELIMITER + INFIX + "a" + DELIMITER + INFIX + "<code>test</code>" + DELIMITER + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$
		String result= testFormat(input);
		assertEquals(expected, result);
	}
	
	/**
	 * [formatting][implementation] comment line length not correctly applied
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46341
	 * Do not wrap. (Consecutive immutable regions on multiple lines.)
	 */
	public void testMultiLineCommentLineBreakBeforeImmutableRegions3() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "20"); //$NON-NLS-1$
		String input= PREFIX + DELIMITER + INFIX + "a <code>" + DELIMITER + INFIX + "testestestestestestestestestest" + DELIMITER + INFIX + "</code>" + DELIMITER + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}
	
	/**
	 * Prefs > Java > Code Formatter > Comments: Preview incorrect
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=55204
	 * Do not insert blank line before Javadoc tags
	 */
	public void testMultiLineCommentBlankLineBeforeJavadoctags1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_INSERT_EMPTY_LINE_BEFORE_ROOT_TAGS, JavaCore.DO_NOT_INSERT); //$NON-NLS-1$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES, "false"); //$NON-NLS-1$
		String input= PREFIX + DELIMITER + INFIX + "Description" + DELIMITER + INFIX + "@param test" + DELIMITER + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}
	
	/**
	 * Prefs > Java > Code Formatter > Comments: Preview incorrect
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=55204
	 * Do insert blank line before Javadoc tags
	 */
	public void testMultiLineCommentBlankLineBeforeJavadoctags2() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_INSERT_EMPTY_LINE_BEFORE_ROOT_TAGS, JavaCore.INSERT); //$NON-NLS-1$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES, "true"); //$NON-NLS-1$
		String prefix= PREFIX + DELIMITER + INFIX + "Description"; //$NON-NLS-1$
		String postfix= DELIMITER + INFIX + "@param test" + DELIMITER + POSTFIX; //$NON-NLS-1$
		String input= prefix + postfix;
		String expected= prefix + DELIMITER + INFIX + postfix;
		String result= testFormat(input);
		assertEquals(expected, result);
	}
	
	/**
	 * Prefs > Java > Code Formatter > Comments: Preview incorrect
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=55204
	 * Do not remove blank line before Javadoc tags
	 */
	public void testMultiLineCommentBlankLineBeforeJavadoctags3() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_INSERT_EMPTY_LINE_BEFORE_ROOT_TAGS, JavaCore.INSERT); //$NON-NLS-1$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES, "true"); //$NON-NLS-1$
		String input= PREFIX + DELIMITER + INFIX + "Description" + DELIMITER + INFIX + DELIMITER + INFIX + "@param test" + DELIMITER + POSTFIX; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}
	
	/**
	 * Prefs > Java > Code Formatter > Comments: Preview incorrect
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=55204
	 * Do remove blank line before Javadoc tags
	 */
	public void testMultiLineCommentBlankLineBeforeJavadoctags4() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_INSERT_EMPTY_LINE_BEFORE_ROOT_TAGS, JavaCore.DO_NOT_INSERT); //$NON-NLS-1$
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES, "true"); //$NON-NLS-1$
		String prefix= PREFIX + DELIMITER + INFIX + "Description"; //$NON-NLS-1$
		String postfix= DELIMITER + INFIX + "@param test" + DELIMITER + POSTFIX; //$NON-NLS-1$
		String input= prefix + DELIMITER + INFIX + postfix;
		String expected= prefix + postfix;
		String result= testFormat(input);
		assertEquals(expected, result);
	}
	
	/**
	 * [formatting] javadoc formatter removes blank lines between empty javadoc tags (xdoclet fails)
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=68577
	 */
	public void testLineBreaksBetweenEmptyJavaDocTags1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES, "false"); //$NON-NLS-1$
		String input= PREFIX + DELIMITER + INFIX + "@custom1" + DELIMITER + INFIX + DELIMITER + INFIX + "@custom2" + DELIMITER + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}
	
	/**
	 * [formatting] javadoc formatter removes blank lines between empty javadoc tags (xdoclet fails)
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=68577
	 */
	public void testLineBreaksBetweenEmptyJavaDocTags2() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_CLEAR_BLANK_LINES, "false"); //$NON-NLS-1$
		String input= PREFIX + DELIMITER + INFIX + "@custom1" + DELIMITER + INFIX + "@custom2" + DELIMITER + POSTFIX;  //$NON-NLS-1$//$NON-NLS-2$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}
	
	public void testNoChange1() {
		String content= PREFIX + DELIMITER + POSTFIX;
		assertEquals(content, testFormat(content));
	}
	
	public void testNoFormat1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT, "false");
		String content= PREFIX + DELIMITER + INFIX + "test" + DELIMITER + INFIX + "test" + DELIMITER + POSTFIX;
		assertEquals(content, testFormat(content));
	}
	
	/**
	 * [formatting] Javadoc Formatter mishandles spaces in comments
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49686
	 */
	public void testInlineTag1() {
		String input= PREFIX + DELIMITER + INFIX + "{@link Object} has many methods." + DELIMITER + POSTFIX;  //$NON-NLS-1$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}
	
	/**
	 * [formatting] Javadoc Formatter mishandles spaces in comments
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49686
	 */
	public void testInlineTag2() {
		String input= PREFIX + DELIMITER + INFIX + "{@link Object}s are cool." + DELIMITER + POSTFIX;  //$NON-NLS-1$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}
	
	/**
	 * [formatting] Javadoc Formatter mishandles spaces in comments
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49686
	 */
	public void testMultilineInlineTag1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "20"); //$NON-NLS-1$
		final String prefix= PREFIX + DELIMITER + INFIX + "{@link Object}";
		final String postfix= "has many methods." + DELIMITER + POSTFIX;
		String input= prefix + " " + postfix;  //$NON-NLS-1$
		String expected= prefix + DELIMITER + INFIX + postfix;
		String result= testFormat(input);
		assertEquals(expected, result);
	}
	
	/**
	 * [formatting] Javadoc Formatter mishandles spaces in comments
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49686
	 */
	public void testMultilineInlineTag2() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "20"); //$NON-NLS-1$
		final String prefix= PREFIX + DELIMITER + INFIX + "{@link Objecterr}s";
		final String postfix= "are cool." + DELIMITER + POSTFIX;
		String input= prefix + " " + postfix;  //$NON-NLS-1$
		String expected= prefix + DELIMITER + INFIX + postfix;
		String result= testFormat(input);
		assertEquals(expected, result);
	}
	
	/**
	 * [formatting] Javadoc Formatter mishandles spaces in comments
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49686
	 */
	public void testTagWordbreaks1() {
		String input= PREFIX + DELIMITER + INFIX + "<code>Object</code> rocks." + DELIMITER + POSTFIX;  //$NON-NLS-1$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	/**
	 * [formatting] Javadoc Formatter mishandles spaces in comments
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49686
	 */
	public void testTagWordbreaks2() {
		String input= PREFIX + DELIMITER + INFIX + "<code>Object</code>s are cool." + DELIMITER + POSTFIX;  //$NON-NLS-1$
		String expected= input;
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	/**
	 * [formatting] Javadoc Formatter mishandles spaces in comments
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49686
	 */
	public void testMultilineTagWordbreaks1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "20"); //$NON-NLS-1$
		String prefix= PREFIX + DELIMITER + INFIX + "<code>Object</code>";
		String postfix=  "rocks." + DELIMITER + POSTFIX;  //$NON-NLS-1$
		String input= prefix + " " + postfix;
		String expected= prefix + DELIMITER + INFIX + postfix; 
		String result= testFormat(input);
		assertEquals(expected, result);
	}

	/**
	 * [formatting] Javadoc Formatter mishandles spaces in comments
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49686
	 */
	public void testMultilineTagWordbreaks2() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_LINE_LENGTH, "20"); //$NON-NLS-1$
		final String prefix= PREFIX + DELIMITER + INFIX + "Foo";
		final String postfix= "<code>Obj</code>s" + DELIMITER + POSTFIX;
		String input= prefix + " " + postfix;
		String expected= prefix + DELIMITER + INFIX + postfix;
		String result= testFormat(input);
		assertEquals(expected, result);
	}
	
	public void testMultiLineComment() {
		String input= PREFIX + DELIMITER + " TOTO " + POSTFIX; //$NON-NLS-1$
		String expected= PREFIX + DELIMITER + INFIX + "TOTO" + DELIMITER + POSTFIX; //$NON-NLS-1$
		final String result = testFormat(input);
		assertEquals(expected, result);
	}
	
	public void testMultiLineComment2() {
		String input= PREFIX + DELIMITER + "TOTO" + POSTFIX; //$NON-NLS-1$
		String expected= PREFIX + DELIMITER + INFIX + "TOTO" + DELIMITER + POSTFIX; //$NON-NLS-1$
		final String result = testFormat(input);
		assertEquals(expected, result);
	}
	
	/**
	 * [formatting] Javadoc formatting: extra newline with [pre]
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=52921
	 * <p>
	 * This test only formats once.
	 * </p>
	 */
	public void testNoExtraNewlineWithPre1() {
		setUserOption(DefaultCodeFormatterConstants.FORMATTER_COMMENT_FORMAT_SOURCE, DefaultCodeFormatterConstants.TRUE);
		String input= PREFIX + DELIMITER + INFIX + "<pre>wrap here</pre>" + DELIMITER + POSTFIX; //$NON-NLS-1$
		String expected= PREFIX + DELIMITER + INFIX + "<pre>" + DELIMITER + INFIX + "wrap here" + DELIMITER + INFIX + "</pre>" + DELIMITER + POSTFIX; //$NON-NLS-1$; //$NON-NLS-2$; //$NON-NLS-3$;
		String result= testFormat(input);
		assertEquals(expected, result);

		// now re-format several times
		result= testFormat(result);
		result= testFormat(result);
		result= testFormat(result);
		result= testFormat(result);
		
		// XXX: workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=99738
		String WORKAROUND_BUG_99738= "    ";  //$NON-NLS-1$
		expected= PREFIX + DELIMITER + INFIX + "<pre>" + DELIMITER + INFIX + WORKAROUND_BUG_99738 + "wrap here" + DELIMITER + INFIX + "</pre>" + DELIMITER + POSTFIX; //$NON-NLS-1$; //$NON-NLS-2$; //$NON-NLS-3$;
		assertEquals(expected, result);
	}
	
}
