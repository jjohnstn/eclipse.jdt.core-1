/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;

public class ConstantTest extends AbstractRegressionTest {
	
public ConstantTest(String name) {
	super(name);
}
public static Test suite() {

	if (false) {
	   	TestSuite ts;
		//some of the tests depend on the order of this suite.
		ts = new TestSuite();
		ts.addTest(new ConstantTest("test221"));
		return new RegressionTestSetup(ts, COMPLIANCE_1_4);
	}
	return setupSuite(testClass());
}

public void test001() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class  X { \n" + 
		"public static void main (String args []) {\n" + 
		"  foo(); \n" + 
		"}\n" + 
		"public static void foo() {\n" + 
		"  if(55f!=00000000000000000000055F)      // HERE VA/Java detects an unexpected error\n" + 
		"  {\n" + 
		"System.out.println(\"55f!=00000000000000000000055F\");\n" + 
		"  }\n" + 
		"  else\n" + 
		"  {\n" + 
		"System.out.println(\"55f==00000000000000000000055F\");\n" + 
		"  }\n" + 
		" }      \n" + 
		"}\n",
	});
}

public void test002() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class X {\n" + 
		"  public static void main (String args []) {\n" + 
		"    foo();\n" + 
		"  }\n" + 
		"  public static void foo() {\n" + 
		"    if(55f!=00000000000000000000055F)      // HERE VA/Java detects an unexpected error\n" + 
		"      {\n" + 
		"      System.out.println(\"55f!=00000000000000000000055F\");\n" + 
		"    }\n" + 
		"    else\n" + 
		"    {\n" + 
		"      System.out.println(\"55f==00000000000000000000055F\");\n" + 
		"    }\n" + 
		"  }      \n" + 
		"}\n",
	});
}

public void test003() {
	this.runConformTest(new String[] {
		"p/Z.java",
		"package p;\n" + 
		"public class Z {\n" + 
		"  public static void main(String[] cargs) throws Exception {\n" + 
		"    System.out.println(System.getProperty(\"java.vm.info\", \"J9\"));\n" + 
		"    System.out.write((byte) 0x89);\n" + 
		"    System.out.println();\n" + 
		"    System.out.println(\"�?�\");\n" + 
		"    System.out.println(Integer.toHexString(\"�?�\".charAt(0)));\n" + 
		"  }\n" + 
		"}\n",
	});
}

public void test004() {
	this.runConformTest(
		new String[] {
			"TempClassFormat.java",
			"/**\n" +
			" * Insert the type's description here.\n" +
			" * Creation date: (02/28/01 2:58:07 PM)\n" +
			" * @author: Administrator\n" +
			" */\n" +
			"public class TempClassFormat {\n" +
			"		// ERROR NUMBERS\n" +
			"\n" +
			"	// Blank field error numbers\n" +
			"	private static final String PERSON_ID_BLANK = \"2\";\n" +
			"	private static final String DEMOGRAPHIC_TYPE_BLANK = \"3\";\n" +
			"	private static final String EMPLOYEE_NUMBER_BLANK = \"23\";\n" +
			"	private static final String WORK_PHONE_AREA_CODE_BLANK = \"25\";\n" +
			"	private static final String WORK_PHONE1_BLANK = \"26\";\n" +
			"	private static final String WORK_PHONE2_BLANK = \"27\";\n" +
			"	private static final String WORK_ADDRESS1_BLANK = \"28\";\n" +
			"	private static final String WORK_CITY_BLANK = \"29\";\n" +
			"	private static final String WORK_STATE_BLANK = \"30\";\n" +
			"	private static final String WORK_ZIP5_BLANK = \"31\";\n" +
			"	private static final String BENEFITS_SALARY_BLANK = \"32\";\n" +
			"	private static final String TRUE_SALARY_BLANK = \"33\";\n" +
			"	private static final String PAY_FREQUENCY_BLANK = \"34\";\n" +
			"	private static final String WORK_HOURS_BLANK = \"35\";\n" +
			"	private static final String LOCATION_ID_BLANK = \"36\";\n" +
			"	private static final String SALARY_GRADE_BLANK = \"37\";\n" +
			"	private static final String DATE_OF_HIRE_BLANK = \"38\";\n" +
			"	private static final String RETIRE_VEST_PERCENT_BLANK = \"39\";\n" +
			"	private static final String JOB_CODE_BLANK = \"40\";\n" +
			"	private static final String UNION_FLAG_BLANK = \"41\";\n" +
			"	private static final String OFFICER_FLAG_BLANK = \"42\";\n" +
			"	private static final String PIN_USER_ID_BLANK = \"43\";\n" +
			"	private static final String VENDOR_EMPLOYEE_ID_BLANK = \"44\";\n" +
			"	private static final String MODIFIED_BY_BLANK = \"8\";\n" +
			"	private static final String MODIFIED_DATE_BLANK = \"9\";\n" +
			"	\n" +
			"	\n" +
			"	// Invalid field error numbers\n" +
			"	private static final String DEMOGRAPHIC_TYPE_INVALID = \"54\";\n" +
			"	private static final String EMPLOYER_ID_INVALID = \"22\";\n" +
			"	private static final String WORK_STATE_INVALID = \"70\";\n" +
			"	private static final String PAY_FREQUENCY_INVALID = \"138\";\n" +
			"	private static final String WORK_HOURS_TOO_SMALL = \"140\";\n" +
			"	private static final String DATE_OF_HIRE_INVALID = \"75\";\n" +
			"	private static final String DATE_OF_HIRE_AFTER_TODAY = \"137\";\n" +
			"	private static final String RETIRE_VEST_PERCENT_TOO_LARGE = \"77\";\n" +
			"	private static final String RETIRE_VEST_PERCENT_TOO_SMALL = \"139\";\n" +
			"	private static final String UNION_FLAG_INVALID = \"78\";\n" +
			"	private static final String OFFICER_FLAG_INVALID = \"79\";\n" +
			"	private static final String BENEFIT_GROUP_ID_INVALID = \"45\";\n" +
			"	private static final String LAST_PERSON_SEQ_NUMBER_INVALID = \"80\";\n" +
			"\n" +
			"	// Field not numeric error numbers\n" +
			"	private static final String WORK_PHONE_AREA_CODE_NOT_NUMERIC = \"67\";\n" +
			"	private static final String WORK_PHONE1_NOT_NUMERIC = \"68\";\n" +
			"	private static final String WORK_PHONE2_NOT_NUMERIC = \"69\";\n" +
			"	private static final String WORK_PHONE_EXTENSION_NOT_NUMERIC = \"109\";\n" +
			"	private static final String WORK_ZIP5_NOT_NUMERIC = \"71\";\n" +
			"	private static final String WORK_ZIP4_NOT_NUMERIC = \"46\";\n" +
			"	private static final String BENEFITS_SALARY_NOT_NUMERIC = \"72\";\n" +
			"	private static final String TRUE_SALARY_NOT_NUMERIC = \"73\";\n" +
			"	private static final String WORK_HOURS_NOT_NUMERIC = \"74\";\n" +
			"	private static final String RETIRE_VEST_PERCENT_NOT_NUMERIC = \"76\";\n" +
			"	\n" +
			"	// Field too short error numbers\n" +
			"	private static final String WORK_PHONE_AREA_CODE_TOO_SHORT = \"110\";\n" +
			"	private static final String WORK_PHONE1_TOO_SHORT = \"111\";\n" +
			"	private static final String WORK_PHONE2_TOO_SHORT = \"112\";\n" +
			"	private static final String WORK_STATE_TOO_SHORT = \"113\";\n" +
			"	private static final String WORK_ZIP5_TOO_SHORT = \"114\";\n" +
			"	private static final String WORK_ZIP4_TOO_SHORT = \"115\";\n" +
			"\n" +
			"	// Field too long error numbers\n" +
			"	private static final String PERSON_ID_TOO_LONG = \"82\";\n" +
			"	private static final String EMPLOYEE_NUMBER_TOO_LONG = \"116\";\n" +
			"	private static final String WORK_PHONE_AREA_CODE_TOO_LONG = \"117\";\n" +
			"	private static final String WORK_PHONE1_TOO_LONG = \"118\";\n" +
			"	private static final String WORK_PHONE2_TOO_LONG = \"119\";\n" +
			"	private static final String WORK_PHONE_EXTENSION_TOO_LONG = \"120\";\n" +
			"	private static final String WORK_ADDRESS1_TOO_LONG = \"121\";\n" +
			"	private static final String WORK_ADDRESS2_TOO_LONG = \"122\";\n" +
			"	private static final String WORK_CITY_TOO_LONG = \"123\";\n" +
			"	private static final String WORK_STATE_TOO_LONG = \"124\";\n" +
			"	private static final String WORK_ZIP5_TOO_LONG = \"125\";\n" +
			"	private static final String WORK_ZIP4_TOO_LONG = \"126\";\n" +
			"	private static final String BENEFITS_SALARY_TOO_LONG = \"127\";\n" +
			"	private static final String TRUE_SALARY_TOO_LONG = \"128\";\n" +
			"	private static final String WORK_HOURS_TOO_LONG = \"129\";\n" +
			"	private static final String LOCATION_ID_TOO_LONG = \"130\";\n" +
			"	private static final String SALARY_GRADE_TOO_LONG = \"131\";\n" +
			"	private static final String RETIRE_VEST_PERCENT_TOO_LONG = \"132\";\n" +
			"	private static final String JOB_CODE_TOO_LONG = \"133\";\n" +
			"	private static final String PIN_USER_ID_TOO_LONG = \"134\";\n" +
			"	private static final String VENDOR_EMPLOYEE_ID_TOO_LONG = \"135\";\n" +
			"	private static final String MODIFIED_BY_TOO_LONG = \"86\";\n" +
			"\n" +
			"	// Administrator approval error numbers\n" +
			"	private static final String EMPLOYER_ID_REQ_APPR = \"623\";\n" +
			"	private static final String EMPLOYEE_NUMBER_REQ_APPR = \"624\";\n" +
			"	private static final String STATUS_FLAG_REQ_APPR = \"625\";\n" +
			"	private static final String WORK_PHONE_AREA_CODE_REQ_APPR = \"626\";\n" +
			"	private static final String WORK_PHONE1_REQ_APPR = \"627\";\n" +
			"	private static final String WORK_PHONE2_REQ_APPR = \"628\";\n" +
			"	private static final String WORK_PHONE_EXTENSION_REQ_APPR = \"629\";\n" +
			"	private static final String WORK_ADDRESS1_REQ_APPR = \"630\";\n" +
			"	private static final String WORK_ADDRESS2_REQ_APPR = \"631\";\n" +
			"	private static final String WORK_CITY_REQ_APPR = \"632\";\n" +
			"	private static final String WORK_STATE_REQ_APPR = \"633\";\n" +
			"	private static final String WORK_ZIP5_REQ_APPR = \"634\";\n" +
			"	private static final String WORK_ZIP4_REQ_APPR = \"635\";\n" +
			"	private static final String BENEFITS_SALARY_REQ_APPR = \"636\";\n" +
			"	private static final String TRUE_SALARY_REQ_APPR = \"637\";\n" +
			"	private static final String PAY_FREQUENCY_REQ_APPR = \"638\";\n" +
			"	private static final String WORK_HOURS_REQ_APPR = \"639\";\n" +
			"	private static final String LOCATION_ID_REQ_APPR = \"640\";\n" +
			"	private static final String SALARY_GRADE_REQ_APPR = \"641\";\n" +
			"	private static final String DATE_OF_HIRE_REQ_APPR = \"642\";\n" +
			"	private static final String RETIRE_VEST_PERCENT_REQ_APPR = \"643\";\n" +
			"	private static final String JOB_CODE_REQ_APPR = \"644\";\n" +
			"	private static final String UNION_FLAG_REQ_APPR = \"645\";\n" +
			"	private static final String OFFICER_FLAG_REQ_APPR = \"646\";\n" +
			"	private static final String PIN_USER_ID_REQ_APPR = \"647\";\n" +
			"	private static final String VENDOR_EMPLOYEE_ID_REQ_APPR = \"648\";\n" +
			"	private static final String BENEFIT_GROUP_ID_REQ_APPR = \"649\";\n" +
			"	private static final String LAST_PERSON_SEQ_NBR_REQ_APPR = \"650\";\n" +
			"	\n" +
			"public static void main(String[] args) {\n" +
			"		System.out.println(\"Success\");\n" +
			"}\n" +
			"}"
		},
		"Success");
}

public void test005() {
	this.runConformTest(
		new String[] {
			"Code.java",
			"public class Code {\n" +
			"  public static final String s = \"<clinit>\";\n" +
			"  public static final String s2 = \"()V\";\n" +
			"  public Code(int i) {\n" +
			"  }\n" +
			"public static void main(String[] args) {\n" +
			"  System.out.print(s.length());\n" +
			"  System.out.println(s2.length());\n" +
			"}\n" +
			"}"
		},
		"83");
}

public void test006() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"package p1;	\n" +
			"public class X {	\n" +
			"	X otherX;	\n" +
			"	static String STR = \"SUCCESS\";	\n" +
			"	public static void main(String args[]) {	\n" +
			"		try {	\n" +
			"			System.out.println(new X().otherX.STR);	\n" +
			"		} catch(NullPointerException e){	\n" +
			"			System.out.println(\"FAILED\");	\n" +
			"		}	\n" +
			"	}	\n" +
			"}	\n",
		},
		"SUCCESS");
}

/*
 * null is not a constant
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26585
 */
public void test007() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"    public static final boolean F = false;	\n"+
			"    public static final String Str = F ? \"dummy\" : null;	\n"+
			"    public static void main(String[] args) {	\n"+
			"        if (Str == null)	\n"+
			"        	System.out.println(\"SUCCESS\");	\n"+
			"       	else	\n"+
			"        	System.out.println(\"FAILED\");	\n"+
			"    }	\n"+
			"}	\n",
		},
		"SUCCESS");
}

/*
 * null is not a constant
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26138
 */
public void test008() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"    public static void main(String[] args) {	\n"+
			"      	System.out.println(\"SUCCESS\");	\n"+
			"	} 	\n"+
			"	void foo(){	\n"+
			"		while (null == null);	//not an inlinable constant\n"+
			"		System.out.println(\"unreachable but shouldn't be flagged\");	\n" +
			"	}	\n"+
			"}	\n",
		},
		"SUCCESS");
}

/*
 * null is not a constant
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26138
 */
public void test009() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"    public static void main(String[] args) {	\n" +
			"        if (null == null) System.out.print(\"1\");	\n" +
			"        if ((null==null ? null:null) == (null==null ? null:null))	\n" +
			"        	System.out.print(\"2\");	\n" +
			"		boolean b = (\"[\" + null + \"]\") == \"[null]\";  // cannot inline	\n" +
			"		System.out.print(\"3\");	\n" +
			"		final String s = (String) null;	\n" +
			"		if (s == null) System.out.print(\"4\");	\n" +
			"		final String s2 = (String) \"aaa\";	\n" +
			"		if (s2 == \"aaa\") System.out.println(\"5\");	\n" +
			"    }	\n" +
			"}",
		},
		"12345");
		
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String actualOutput = null;
	try {
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED); 
	} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
		assertTrue("ClassFormatException", false);
	} catch (IOException e) {
		assertTrue("IOException", false);
	}
	
	String expectedOutput = 
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 3, Locals: 4\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  getstatic java.lang.System.out : java.io.PrintStream [21]\n" + 
		"     3  ldc <String \"1\"> [23]\n" + 
		"     5  invokevirtual java.io.PrintStream.print(java.lang.String) : void  [29]\n" + 
		"     8  aconst_null\n" + 
		"     9  goto 13\n" + 
		"    12  aconst_null\n" + 
		"    13  aconst_null\n" + 
		"    14  goto 18\n" + 
		"    17  aconst_null\n" + 
		"    18  if_acmpne 29\n" + 
		"    21  getstatic java.lang.System.out : java.io.PrintStream [21]\n" + 
		"    24  ldc <String \"2\"> [31]\n" + 
		"    26  invokevirtual java.io.PrintStream.print(java.lang.String) : void  [29]\n" + 
		"    29  new java.lang.StringBuffer [33]\n" + 
		"    32  dup\n" + 
		"    33  ldc <String \"[\"> [35]\n" + 
		"    35  invokespecial java.lang.StringBuffer(java.lang.String) [37]\n" + 
		"    38  aconst_null\n" + 
		"    39  invokevirtual java.lang.StringBuffer.append(java.lang.Object) : java.lang.StringBuffer  [41]\n" + 
		"    42  ldc <String \"]\"> [43]\n" + 
		"    44  invokevirtual java.lang.StringBuffer.append(java.lang.String) : java.lang.StringBuffer  [46]\n" + 
		"    47  invokevirtual java.lang.StringBuffer.toString() : java.lang.String  [50]\n" + 
		"    50  ldc <String \"[null]\"> [52]\n" + 
		"    52  if_acmpne 59\n" + 
		"    55  iconst_1\n" + 
		"    56  goto 60\n" + 
		"    59  iconst_0\n" + 
		"    60  istore_1 [b]\n" + 
		"    61  getstatic java.lang.System.out : java.io.PrintStream [21]\n" + 
		"    64  ldc <String \"3\"> [54]\n" + 
		"    66  invokevirtual java.io.PrintStream.print(java.lang.String) : void  [29]\n" + 
		"    69  aconst_null\n" + 
		"    70  astore_2 [s]\n" + 
		"    71  aload_2 [s]\n" + 
		"    72  ifnonnull 83\n" + 
		"    75  getstatic java.lang.System.out : java.io.PrintStream [21]\n" + 
		"    78  ldc <String \"4\"> [56]\n" + 
		"    80  invokevirtual java.io.PrintStream.print(java.lang.String) : void  [29]\n" + 
		"    83  ldc <String \"aaa\"> [58]\n" + 
		"    85  astore_3 [s2]\n" + 
		"    86  getstatic java.lang.System.out : java.io.PrintStream [21]\n" + 
		"    89  ldc <String \"5\"> [60]\n" + 
		"    91  invokevirtual java.io.PrintStream.println(java.lang.String) : void  [63]\n" + 
		"    94  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 3]\n" + 
		"        [pc: 8, line: 4]\n" + 
		"        [pc: 21, line: 5]\n" + 
		"        [pc: 29, line: 6]\n" + 
		"        [pc: 61, line: 7]\n" + 
		"        [pc: 69, line: 8]\n" + 
		"        [pc: 71, line: 9]\n" + 
		"        [pc: 83, line: 10]\n" + 
		"        [pc: 86, line: 11]\n" + 
		"        [pc: 94, line: 12]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 95] local: args index: 0 type: java.lang.String[]\n" + 
		"        [pc: 61, pc: 95] local: b index: 1 type: boolean\n" + 
		"        [pc: 71, pc: 95] local: s index: 2 type: java.lang.String\n" + 
		"        [pc: 86, pc: 95] local: s2 index: 3 type: java.lang.String\n";
	
	String expectedOutput15 =
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 3, Locals: 4\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  getstatic java.lang.System.out : java.io.PrintStream [21]\n" + 
		"     3  ldc <String \"1\"> [23]\n" + 
		"     5  invokevirtual java.io.PrintStream.print(java.lang.String) : void  [29]\n" + 
		"     8  aconst_null\n" + 
		"     9  goto 13\n" + 
		"    12  aconst_null\n" + 
		"    13  aconst_null\n" + 
		"    14  goto 18\n" + 
		"    17  aconst_null\n" + 
		"    18  if_acmpne 29\n" + 
		"    21  getstatic java.lang.System.out : java.io.PrintStream [21]\n" + 
		"    24  ldc <String \"2\"> [31]\n" + 
		"    26  invokevirtual java.io.PrintStream.print(java.lang.String) : void  [29]\n" + 
		"    29  new java.lang.StringBuilder [33]\n" + 
		"    32  dup\n" + 
		"    33  ldc <String \"[\"> [35]\n" + 
		"    35  invokespecial java.lang.StringBuilder(java.lang.String) [37]\n" + 
		"    38  aconst_null\n" + 
		"    39  invokevirtual java.lang.StringBuilder.append(java.lang.Object) : java.lang.StringBuilder  [41]\n" + 
		"    42  ldc <String \"]\"> [43]\n" + 
		"    44  invokevirtual java.lang.StringBuilder.append(java.lang.String) : java.lang.StringBuilder  [46]\n" + 
		"    47  invokevirtual java.lang.StringBuilder.toString() : java.lang.String  [50]\n" + 
		"    50  ldc <String \"[null]\"> [52]\n" + 
		"    52  if_acmpne 59\n" + 
		"    55  iconst_1\n" + 
		"    56  goto 60\n" + 
		"    59  iconst_0\n" + 
		"    60  istore_1 [b]\n" + 
		"    61  getstatic java.lang.System.out : java.io.PrintStream [21]\n" + 
		"    64  ldc <String \"3\"> [54]\n" + 
		"    66  invokevirtual java.io.PrintStream.print(java.lang.String) : void  [29]\n" + 
		"    69  aconst_null\n" + 
		"    70  astore_2 [s]\n" + 
		"    71  aload_2 [s]\n" + 
		"    72  ifnonnull 83\n" + 
		"    75  getstatic java.lang.System.out : java.io.PrintStream [21]\n" + 
		"    78  ldc <String \"4\"> [56]\n" + 
		"    80  invokevirtual java.io.PrintStream.print(java.lang.String) : void  [29]\n" + 
		"    83  ldc <String \"aaa\"> [58]\n" + 
		"    85  astore_3 [s2]\n" + 
		"    86  getstatic java.lang.System.out : java.io.PrintStream [21]\n" + 
		"    89  ldc <String \"5\"> [60]\n" + 
		"    91  invokevirtual java.io.PrintStream.println(java.lang.String) : void  [63]\n" + 
		"    94  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 3]\n" + 
		"        [pc: 8, line: 4]\n" + 
		"        [pc: 21, line: 5]\n" + 
		"        [pc: 29, line: 6]\n" + 
		"        [pc: 61, line: 7]\n" + 
		"        [pc: 69, line: 8]\n" + 
		"        [pc: 71, line: 9]\n" + 
		"        [pc: 83, line: 10]\n" + 
		"        [pc: 86, line: 11]\n" + 
		"        [pc: 94, line: 12]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 95] local: args index: 0 type: java.lang.String[]\n" + 
		"        [pc: 61, pc: 95] local: b index: 1 type: boolean\n" + 
		"        [pc: 71, pc: 95] local: s index: 2 type: java.lang.String\n" + 
		"        [pc: 86, pc: 95] local: s2 index: 3 type: java.lang.String\n";
	
	if (COMPLIANCE_1_5.equals(this.complianceLevel)) {
		int index = actualOutput.indexOf(expectedOutput15);
		if (index == -1 || expectedOutput15.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput15, actualOutput);
		}
	} else {
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}
}

/*
 * null is not a constant
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26138
 */
public void test010() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"    public static void main(String[] args) {	\n" +
			"       if (null == null) {\n"+
			"			System.out.print(\"SUCCESS\");	\n" +
			"			return;	\n" +
			"		}	\n" +
			"		System.out.print(\"SHOULDN'T BE GENERATED\");	\n" +
			"    }	\n" +
			"}	\n",
		},
		"SUCCESS");
		
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String actualOutput = null;
	try {
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED); 
	} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
		assertTrue("ClassFormatException", false);
	} catch (IOException e) {
		assertTrue("IOException", false);
	}
	
	String expectedOutput = 
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 2, Locals: 1\n" + 
		"  public static void main(String[] args);\n" + 
		"     0  getstatic java.lang.System.out : java.io.PrintStream [21]\n" + 
		"     3  ldc <String \"SUCCESS\"> [23]\n" + 
		"     5  invokevirtual java.io.PrintStream.print(java.lang.String) : void  [29]\n" + 
		"     8  return\n" + 
		"     9  getstatic java.lang.System.out : java.io.PrintStream [21]\n" + 
		"    12  ldc <String \"SHOULDN\'T BE GENERATED\"> [31]\n" + 
		"    14  invokevirtual java.io.PrintStream.print(java.lang.String) : void  [29]\n" + 
		"    17  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 4]\n" + 
		"        [pc: 8, line: 5]\n" + 
		"        [pc: 9, line: 7]\n" + 
		"        [pc: 17, line: 8]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 18] local: args index: 0 type: java.lang.String[]\n";
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}

//http://bugs.eclipse.org/bugs/show_bug.cgi?id=30704
public void test011() {
	this.runConformTest(
		new String[] {
			"A.java",
			"public class A {\n" +
			"    public static void main(String[] args) {\n" +
			"		System.out.print((01.f == 1) && (01e0f == 1));	\n" +
			"    }\n" +
			"}",
		},
		"true");
}

//http://bugs.eclipse.org/bugs/show_bug.cgi?id=79545
public void test012() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static String C = \"\" + +\' \';\n" +
			"    public static String I = \"\" + +32;\n" +
			"\n" +
			"    public static void main(String[] args) {\n" +
			"        System.out.print(C);\n" +
			"        System.out.print(I);\n" +
			"    }\n" +
			"}",
		},
		"3232");
}
//http://bugs.eclipse.org/bugs/show_bug.cgi?id=97190
public void test013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(-9223372036854775809L); // KO\n" +
			"		System.out.println(9223372036854775809L); // KO\n" +
			"		System.out.println(9223372036854775808L); // KO\n" +
			"		System.out.println(23092395825689123986L); // KO\n" +
			"		System.out.println(-9223372036854775808L); // OK\n" +
			"		System.out.println(9223372036854775807L); // OK\n" +
			"		System.out.println(2309239582568912398L); // OK\n" +
			"	}\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	System.out.println(-9223372036854775809L); // KO\n" + 
		"	                    ^^^^^^^^^^^^^^^^^^^^\n" + 
		"The literal 9223372036854775809L of type long is out of range \n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 4)\n" + 
		"	System.out.println(9223372036854775809L); // KO\n" + 
		"	                   ^^^^^^^^^^^^^^^^^^^^\n" + 
		"The literal 9223372036854775809L of type long is out of range \n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 5)\n" + 
		"	System.out.println(9223372036854775808L); // KO\n" + 
		"	                   ^^^^^^^^^^^^^^^^^^^^\n" + 
		"The literal 9223372036854775808L of type long is out of range \n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 6)\n" + 
		"	System.out.println(23092395825689123986L); // KO\n" + 
		"	                   ^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The literal 23092395825689123986L of type long is out of range \n" + 
		"----------\n");
}
//http://bugs.eclipse.org/bugs/show_bug.cgi?id=117495
public void test014() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"		int x = 2;\n" + 
			"       System.out.println(\"n: \"+(x > 1  ? 2 : 1.0));\n" +
			"    }\n" +
			"}",
		},
		"n: 2.0");
}
//http://bugs.eclipse.org/bugs/show_bug.cgi?id=117495
public void test015() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"		System.out.println(\"n: \"+(true ? 2 : 1.0));\n" +
			"    }\n" +
			"}",
		},
		"n: 2.0");
}
public static Class testClass() {
	return ConstantTest.class;
}
}
