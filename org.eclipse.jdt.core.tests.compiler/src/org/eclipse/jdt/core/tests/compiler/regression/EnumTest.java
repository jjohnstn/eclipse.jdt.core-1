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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class EnumTest extends AbstractComparableTest {
	
	String reportMissingJavadocComments = null;

	public EnumTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 110 };
//		TESTS_RANGE = new int[] { 21, 50 };
	}
	public static Test suite() {
		Test suite = buildTestSuite(testClass());
		TESTS_COUNTERS.put(testClass().getName(), new Integer(suite.countTestCases()));
		return suite;
	}

	public static Class testClass() {  
		return EnumTest.class;
	}

	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, CompilerOptions.PRIVATE);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsVisibility, CompilerOptions.PRIVATE);
		if (reportMissingJavadocComments != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, reportMissingJavadocComments);
		return options;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		reportMissingJavadocComments = null;
	}

	// test simple valid enum and its usage
	public void test000() {
		runConformTest(
			new String[] {
				"e/X.java",
				"package e;\n" + 
					"import e.T;\n" + 
					"import static e.T.*;\n" + 
					"\n" + 
					"public class X {\n" + 
					"    public static void main(String[] args) {\n" + 
					"    	System.out.print(\"JDTCore team:\");\n" + 
					"    	T oldest = null;\n" +
					"    	int maxAge = Integer.MIN_VALUE;\n" +
					"    	for (T t : T.values()) {\n" + 
					"            if (t == YODA) continue;// skip YODA\n" +
					"            t.setRole(t.isManager());\n" + 
					"			 if (t.age() > maxAge) {\n" +
					"               oldest = t;\n" +
					"               maxAge = t.age();\n" +
					"            }\n" +
					"            System.out.print(\" \"+ t + ':'+t.age()+':'+location(t)+':'+t.role);\n" + 
					"        }\n" + 
					"        System.out.println(\" WINNER is:\" + T.valueOf(oldest.name()));\n" +
					"    }\n" + 
					"\n" + 
					"   private enum Location { SNZ, OTT }\n" + 
					"\n" + 
					"    private static Location location(T t) {\n" + 
					"        switch(t) {\n" + 
					"          case PHILIPPE:  \n" + 
					"          case DAVID:\n" + 
					"          case JEROME:\n" + 
					"          case FREDERIC:\n" + 
					"          	return Location.SNZ;\n" + 
					"          case OLIVIER:\n" + 
					"          case KENT:\n" + 
					"            return Location.OTT;\n" + 
					"          default:\n" + 
					"            throw new AssertionError(\"Unknown team member: \" + t);\n" + 
					"        }\n" + 
					"    }\n" + 
					"}\n",
				"e/T.java",
				"package e;\n" + 
					"public enum T {\n" + 
					"	PHILIPPE(37) {\n" + 
					"		public boolean isManager() {\n" + 
					"			return true;\n" + 
					"		}\n" + 
					"	},\n" + 
					"	DAVID(27),\n" + 
					"	JEROME(33),\n" + 
					"	OLIVIER(35),\n" + 
					"	KENT(40),\n" + 
					"	YODA(41),\n" +
					"	FREDERIC;\n" + 
					"	final static int OLD = 41;\n" +
					"\n" + 
					"   enum Role { M, D }\n" + 
					"\n" + 
					"   int age;\n" + 
					"	Role role;\n" + 
					"\n" + 
					"	T() { this(OLD); }\n" +  
					"	T(int age) {\n" + 
					"		this.age = age;\n" + 
					"	}\n" + 
					"	public int age() { return this.age; }\n" + 
					"	public boolean isManager() { return false; }\n" + 
					"	void setRole(boolean mgr) {\n" + 
					"		this.role = mgr ? Role.M : Role.D;\n" + 
					"	}\n" + 
					"}\n"
			},
			"JDTCore team: PHILIPPE:37:SNZ:M DAVID:27:SNZ:D JEROME:33:SNZ:D OLIVIER:35:OTT:D KENT:40:OTT:D FREDERIC:41:SNZ:D WINNER is:FREDERIC"
		);
	}
	// check assignment to enum constant is disallowed
	public void test001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	BLEU, \n" + 
				"	BLANC, \n" + 
				"	ROUGE;\n" + 
				"	static {\n" + 
				"		BLEU = null;\n" + 
				"	}\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	BLEU = null;\n" + 
			"	^^^^\n" + 
			"The final field X.BLEU cannot be assigned\n" + 
			"----------\n");
	}
	// check diagnosis for duplicate enum constants
	public void test002() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	\n" + 
				"	BLEU, \n" + 
				"	BLANC, \n" + 
				"	ROUGE,\n" + 
				"	BLEU;\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	BLEU, \n" + 
			"	^^^^\n" + 
			"Duplicate field X.BLEU\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	BLEU;\n" + 
			"	^^^^\n" + 
			"Duplicate field X.BLEU\n" + 
			"----------\n");
	}
	// check properly rejecting enum constant modifiers
	public void test003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	\n" + 
				"	public BLEU, \n" + 
				"	transient BLANC, \n" + 
				"	ROUGE\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	public BLEU, \n" + 
			"	       ^^^^\n" + 
			"Illegal modifier for the enum constant BLEU; no modifier is allowed\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	transient BLANC, \n" + 
			"	          ^^^^^\n" + 
			"Illegal modifier for the enum constant BLANC; no modifier is allowed\n" + 
			"----------\n");
	}
	// check using an enum constant
	public void test004() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	\n" + 
				"	BLEU,\n" + 
				"	BLANC,\n" + 
				"	ROUGE;\n" + 
				"	\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(BLEU);\n" + 
				"	}\n" + 
				"	\n" + 
				"}\n"
			},
			"BLEU");
	}
	// check method override diagnosis (with no enum constants)
	public void test005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	;\n" + 
				"	protected Object clone() { return this; }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	protected Object clone() { return this; }\n" + 
			"	                 ^^^^^^^\n" + 
			"Cannot override the final method from Enum<X>\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 3)\n" + 
			"	protected Object clone() { return this; }\n" + 
			"	                 ^^^^^^^\n" + 
			"The method clone() of type X should be tagged with @Override since it actually overrides a superclass method\n" + 
			"----------\n");
	}	
	// check generated #values() method
	public void test006() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	\n" + 
				"	BLEU,\n" + 
				"	BLANC,\n" + 
				"	ROUGE;\n" + 
				"	\n" + 
				"	public static void main(String[] args) {\n" + 
				"		for(X x: X.values()) {\n" + 
				"			System.out.print(x);\n" + 
				"		}\n" + 
				"	}\n" + 
				"	\n" + 
				"}\n"
			},
			"BLEUBLANCROUGE");
	}	
	// tolerate user definition for $VALUES
	public void test007() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	\n" + 
				"	BLEU,\n" + 
				"	BLANC,\n" + 
				"	ROUGE;\n" + 
				"	\n" + 
				"   int $VALUES;\n" +
				"	public static void main(String[] args) {\n" + 
				"		for(X x: X.values()) {\n" + 
				"			System.out.print(x);\n" + 
				"		}\n" + 
				"	}\n" + 
				"	\n" + 
				"}\n"
			},
			"BLEUBLANCROUGE");
	}	
	// reject user definition for #values()
	public void test008() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	\n" + 
				"	BLEU,\n" + 
				"	BLANC,\n" + 
				"	ROUGE;\n" + 
				"	\n" + 
				"   void dup() {} \n" +
				"   void values() {} \n" +
				"   void dup() {} \n" +
				"   void values() {} \n" +
				"   Missing dup() {} \n" +
				"	public static void main(String[] args) {\n" + 
				"		for(X x: X.values()) {\n" + 
				"			System.out.print(x);\n" + 
				"		}\n" + 
				"	}\n" + 
				"	\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	void dup() {} \n" + 
			"	     ^^^^^\n" + 
			"Duplicate method dup() in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	void values() {} \n" + 
			"	     ^^^^^^^^\n" + 
			"The enum X already defines the method values() implicitly\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 9)\n" + 
			"	void dup() {} \n" + 
			"	     ^^^^^\n" + 
			"Duplicate method dup() in type X\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 10)\n" + 
			"	void values() {} \n" + 
			"	     ^^^^^^^^\n" + 
			"The enum X already defines the method values() implicitly\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 11)\n" + 
			"	Missing dup() {} \n" + 
			"	^^^^^^^\n" + 
			"Missing cannot be resolved to a type\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 11)\n" + 
			"	Missing dup() {} \n" + 
			"	        ^^^^^\n" + 
			"Duplicate method dup() in type X\n" + 
			"----------\n");
	}		
	// switch on enum
	public void test009() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	\n" + 
				"	BLEU,\n" + 
				"	BLANC,\n" + 
				"	ROUGE;\n" + 
				"	\n" + 
				"	//void values() {}\n" + 
				"	\n" + 
				"	public static void main(String[] args) {\n" + 
				"		X x = BLEU;\n" + 
				"		switch(x) {\n" + 
				"			case BLEU :\n" + 
				"				System.out.println(\"SUCCESS\");\n" + 
				"				break;\n" + 
				"			case BLANC :\n" + 
				"			case ROUGE :\n" + 
				"				System.out.println(\"FAILED\");\n" + 
				"				break;\n" + 
				"		}\n" + 
				"	}\n" + 
				"	\n" + 
				"}"
			},
			"SUCCESS");
	}		
	// duplicate switch case 
	public void test010() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	\n" + 
				"	BLEU,\n" + 
				"	BLANC,\n" + 
				"	ROUGE;\n" + 
				"	\n" + 
				"	//void values() {}\n" + 
				"	\n" + 
				"	public static void main(String[] args) {\n" + 
				"		X x = BLEU;\n" + 
				"		switch(x) {\n" + 
				"			case BLEU :\n" + 
				"				break;\n" + 
				"			case BLEU :\n" + 
				"			case BLANC :\n" + 
				"			case ROUGE :\n" + 
				"				System.out.println(\"FAILED\");\n" + 
				"				break;\n" + 
				"		}\n" + 
				"	}\n" + 
				"	\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 12)\r\n" + 
			"	case BLEU :\r\n" + 
			"	^^^^^^^^^\n" + 
			"Duplicate case\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 14)\r\n" + 
			"	case BLEU :\r\n" + 
			"	^^^^^^^^^\n" + 
			"Duplicate case\n" + 
			"----------\n");
	}
	// reject user definition for #values()
	public void test011() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	\n" + 
				"	BLEU,\n" + 
				"	BLANC,\n" + 
				"	ROUGE;\n" + 
				"	\n" + 
				"   void values() {} \n" +
				"   void values() {} \n" +
				"	public static void main(String[] args) {\n" + 
				"		for(X x: X.values()) {\n" + 
				"			System.out.print(x);\n" + 
				"		}\n" + 
				"	}\n" + 
				"	\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	void values() {} \n" + 
			"	     ^^^^^^^^\n" + 
			"The enum X already defines the method values() implicitly\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	void values() {} \n" + 
			"	     ^^^^^^^^\n" + 
			"The enum X already defines the method values() implicitly\n" + 
			"----------\n");
	}	
	// check abstract method diagnosis
	public void test012() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X implements Runnable { \n" + 
				"	\n" + 
				"	BLEU,\n" + 
				"	BLANC,\n" + 
				"	ROUGE;\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public enum X implements Runnable { \n" + 
			"	            ^\n" + 
			"The type X must implement the inherited abstract method Runnable.run()\n" + 
			"----------\n");
	}
	// check enum constants with wrong arguments
	public void test013() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	\n" + 
				"	BLEU(10),\n" + 
				"	BLANC(20),\n" + 
				"	ROUGE(30);\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	BLEU(10),\n" + 
			"	^^^^\n" + 
			"The constructor X(int) is undefined\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	BLANC(20),\n" + 
			"	^^^^^\n" + 
			"The constructor X(int) is undefined\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	ROUGE(30);\n" + 
			"	^^^^^\n" + 
			"The constructor X(int) is undefined\n" + 
			"----------\n");
	}
	// check enum constants with extra arguments
	public void test014() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	\n" + 
				"	BLEU(10),\n" + 
				"	BLANC(20),\n" + 
				"	ROUGE(30);\n" + 
				"\n" + 
				"	int val;\n" + 
				"	X(int val) {\n" + 
				"		this.val = val;\n" + 
				"	}\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"		for(X x: values()) {\n" + 
				"			System.out.print(x.val);\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n"
			},
			"102030");
	}	
	// check enum constants with wrong arguments
	public void test015() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	\n" + 
				"	BLEU(10),\n" + 
				"	BLANC(),\n" + 
				"	ROUGE(30);\n" + 
				"\n" + 
				"	int val;\n" + 
				"	X(int val) {\n" + 
				"		this.val = val;\n" + 
				"	}\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"		for(X x: values()) {\n" + 
				"			System.out.print(x.val);\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\r\n" + 
			"	BLANC(),\r\n" + 
			"	^^^^^\n" + 
			"The constructor X() is undefined\n" + 
			"----------\n");
	}		
	// check enum constants with wrong arguments
	public void test016() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	\n" + 
				"	BLEU(10) {\n" + 
				"		String foo() { // inner\n" + 
				"			return super.foo() + this.val;\n" + 
				"		}\n" + 
				"	},\n" + 
				"	BLANC(20),\n" + 
				"	ROUGE(30);\n" + 
				"\n" + 
				"	int val;\n" + 
				"	X(int val) {\n" + 
				"		this.val = val;\n" + 
				"	}\n" + 
				"	String foo() {  // outer\n" + 
				"		return this.name();\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		for(X x: values()) {\n" + 
				"			System.out.print(x.foo());\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n"
			},
			"BLEU10BLANCROUGE");
	}			
	// check enum constants with empty arguments
	public void test017() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	\n" + 
				"	BLEU()\n" + 
				"}\n"
			},
			"");
	}
	// cannot extend enums
	public void test018() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	BLEU()\n" + 
				"}\n" + 
				"\n" + 
				"class XX extends X implements X {\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	class XX extends X implements X {\n" + 
			"	                 ^\n" + 
			"The type X cannot be the superclass of XX; a superclass must be a class\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\n" + 
			"	class XX extends X implements X {\n" + 
			"	                              ^\n" + 
			"The type X cannot be a superinterface of XX; a superinterface must be an interface\n" + 
			"----------\n");
	}		
	// 74851
	public void test019() {
		this.runConformTest(
			new String[] {
				"MonthEnum.java",
				"public enum MonthEnum {\n" + 
				"    JANUARY   (30),\n" + 
				"    FEBRUARY  (28),\n" + 
				"    MARCH     (31),\n" + 
				"    APRIL     (30),\n" + 
				"    MAY       (31),\n" + 
				"    JUNE      (30),\n" + 
				"    JULY      (31),\n" + 
				"    AUGUST    (31),\n" + 
				"    SEPTEMBER (31),\n" + 
				"    OCTOBER   (31),\n" + 
				"    NOVEMBER  (30),\n" + 
				"    DECEMBER  (31);\n" + 
				"    \n" + 
				"    private final int days;\n" + 
				"    \n" + 
				"    MonthEnum(int days) {\n" + 
				"        this.days = days;\n" + 
				"    }\n" + 
				"    \n" + 
				"    public int getDays() {\n" + 
				"    	boolean leapYear = true;\n" + 
				"    	switch(this) {\n" + 
				"    		case FEBRUARY: if(leapYear) return days+1;\n" + 
				"    	}\n" + 
				"    	return days;\n" + 
				"    }\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"    	System.out.println(JANUARY.getDays());\n" + 
				"    }\n" + 
				"    \n" + 
				"}\n",
			},
			"30");
	}
	// 74226
	public void test020() {
		this.runConformTest(
			new String[] {
				"Foo.java",
				"public class Foo{\n" + 
				"    public enum Rank {FIRST,SECOND,THIRD}\n" + 
				"    public void setRank(Rank rank){}\n" + 
				"}\n",
			},
			"");
	}	
	// 74226 variation - check nested enum is implicitly static
	public void test021() {
		this.runNegativeTest(
			new String[] {
				"Foo.java",
				"public class Foo {\n" + 
				"    public static enum Rank {FIRST,SECOND,THIRD;\n" + 
				"            void bar() { foo(); } \n" + 
				"    }\n" + 
				"    public void setRank(Rank rank){}\n" + 
				"    void foo() {}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in Foo.java (at line 3)\n" + 
			"	void bar() { foo(); } \n" + 
			"	             ^^^\n" + 
			"Cannot make a static reference to the non-static method foo() from the type Foo\n" + 
			"----------\n");
	}		
	// 77151 - cannot use qualified name to denote enum constants in switch case label
	public void test022() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	\n" + 
				"	enum MX { BLEU, BLANC, ROUGE }\n" + 
				"	\n" + 
				"	void foo(MX e) {\n" + 
				"		switch(e) {\n" + 
				"			case MX.BLEU : break;\n" + 
				"			case MX.BLANC : break;\n" + 
				"			case MX.ROUGE : break;\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	case MX.BLEU : break;\n" + 
			"	     ^^^^^^^\n" + 
			"The enum constant X.MX.BLEU reference cannot be qualified in a case label\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	case MX.BLANC : break;\n" + 
			"	     ^^^^^^^^\n" + 
			"The enum constant X.MX.BLANC reference cannot be qualified in a case label\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 9)\n" + 
			"	case MX.ROUGE : break;\n" + 
			"	     ^^^^^^^^\n" + 
			"The enum constant X.MX.ROUGE reference cannot be qualified in a case label\n" + 
			"----------\n");
	}
	
	// 77212 
	public void test023() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	public enum RuleType{ SUCCESS, FAILURE }\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.print(RuleType.valueOf(RuleType.SUCCESS.name()));\n" + 
				"	}\n" + 
				"}",
			},
			"SUCCESS");
	}
	
	// 77244 - cannot declare final enum
	public void test024() {
		this.runNegativeTest(
			new String[] {
				"X.java",	
				"public final enum X {\n" +
				"	FOO() {}\n" +
				"}\n" + 
				"\n",
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public final enum X {\n" + 
		"	                  ^\n" + 
		"Illegal modifier for the enum X; only public is permitted\n" + 
		"----------\n");
	}	
	
	// values is using arraycopy instead of clone 
	public void test025() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	SUC, CESS;\n" + 
				"	public static void main(String[] args) {\n" + 
				"		for (X x : values()) {\n" + 
				"			System.out.print(x.name());\n" + 
				"		}\n" + 
				"	}\n" + 
				"}",
			},
			"SUCCESS");
	}
	
	// check enum name visibility
	public void test026() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	enum Couleur { BLEU, BLANC, ROUGE }\n" + 
				"}\n" + 
				"\n" + 
				"class Y {\n" + 
				"	void foo(Couleur c) {\n" + 
				"		switch (c) {\n" + 
				"			case BLEU :\n" + 
				"				break;\n" + 
				"			case BLANC :\n" + 
				"				break;\n" + 
				"			case ROUGE :\n" + 
				"				break;\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	void foo(Couleur c) {\n" + 
			"	         ^^^^^^^\n" + 
			"Couleur cannot be resolved to a type\n" + 
			"----------\n");
	}	
	// check enum name visibility
	public void test027() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	enum Couleur { BLEU, BLANC, ROUGE }\n" + 
				"	class Y {\n" + 
				"		void foo(Couleur c) {\n" + 
				"			switch (c) {\n" + 
				"				case BLEU :\n" + 
				"					break;\n" + 
				"				case BLANC :\n" + 
				"					break;\n" + 
				"				case ROUGE :\n" + 
				"					break;\n" + 
				"			}\n" + 
				"		}	\n" + 
				"	}\n" + 
				"}\n",
			},
			"");
	}		
	// check enum name visibility
	public void test028() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	enum Couleur { \n" + 
				"		BLEU, BLANC, ROUGE;\n" + 
				"		static int C = 0;\n" + 
				"		static void FOO() {}\n" + 
				"	}\n" + 
				"	class Y {\n" + 
				"		void foo(Couleur c) {\n" + 
				"			switch (c) {\n" + 
				"				case BLEU :\n" + 
				"					break;\n" + 
				"				case BLANC :\n" + 
				"					break;\n" + 
				"				case ROUGE :\n" + 
				"					break;\n" + 
				"			}\n" + 
				"			FOO();\n" + 
				"			C++;\n" + 
				"		}	\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 17)\n" + 
			"	FOO();\n" + 
			"	^^^\n" + 
			"The method FOO() is undefined for the type X.Y\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 18)\n" + 
			"	C++;\n" + 
			"	^\n" + 
			"C cannot be resolved\n" + 
			"----------\n");
	}		
	// check enum name visibility
	public void test029() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	enum Couleur { \n" + 
				"		BLEU, BLANC, ROUGE; // take precedence over toplevel BLEU type\n" + 
				"	}\n" + 
				"	class Y {\n" + 
				"		void foo(Couleur c) {\n" + 
				"			switch (c) {\n" + 
				"				case BLEU :\n" + 
				"					break;\n" + 
				"				case BLANC :\n" + 
				"					break;\n" + 
				"				case ROUGE :\n" + 
				"					break;\n" + 
				"			}\n" + 
				"		}	\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class BLEU {}\n",
			},
			"");
	}		
	// check enum name visibility
	public void test030() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	enum Couleur { \n" + 
				"		BLEU, BLANC, ROUGE; // take precedence over sibling constant from Color\n" + 
				"	}\n" + 
				"	enum Color { \n" + 
				"		BLEU, BLANC, ROUGE;\n" + 
				"	}\n" + 
				"	class Y {\n" + 
				"		void foo(Couleur c) {\n" + 
				"			switch (c) {\n" + 
				"				case BLEU :\n" + 
				"					break;\n" + 
				"				case BLANC :\n" + 
				"					break;\n" + 
				"				case ROUGE :\n" + 
				"					break;\n" + 
				"			}\n" + 
				"		}	\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class BLEU {}\n",
			},
			"");
	}		
	// check enum name visibility
	public void test031() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	enum Couleur { \n" + 
				"		BLEU, BLANC, ROUGE; // take precedence over toplevel BLEU type\n" + 
				"	}\n" + 
				"	class Y implements IX, JX {\n" + 
				"		void foo(Couleur c) {\n" + 
				"			switch (c) {\n" + 
				"				case BLEU :\n" + 
				"					break;\n" + 
				"				case BLANC :\n" + 
				"					break;\n" + 
				"				case ROUGE :\n" + 
				"					break;\n" + 
				"			}\n" + 
				"		}	\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"interface IX {\n" + 
				"	int BLEU = 1;\n" + 
				"}\n" + 
				"interface JX {\n" + 
				"	int BLEU = 2;\n" + 
				"}\n" + 
				"class BLEU {}\n" + 
				"\n",
			},
			"");
	}	
	
	// check Enum cannot be used as supertype (explicitly)
	public void test032() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X extends Enum {\n" + 
				"}",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X extends Enum {\n" + 
			"	                       ^^^^\n" + 
			"The type X may not subclass Enum explicitly\n" + 
			"----------\n");
	}		

	// Javadoc in enum (see bug 78018)
	public void test033() {
		this.runConformTest(
			new String[] {
				"E.java",
				"	/**\n" +
					"	 * Valid javadoc\n" +
					"	 * @author ffr\n" +
					"	 */\n" +
					"public enum E {\n" +
					"	/** Valid javadoc */\n" +
					"	TEST,\n" +
					"	/** Valid javadoc */\n" +
					"	VALID;\n" +
					"	/** Valid javadoc */\n" +
					"	public void foo() {}\n" +
					"}\n"
			}
		);
	}
	public void test034() {
		this.runNegativeTest(
			new String[] {
				"E.java",
				"	/**\n" +
					"	 * Invalid javadoc\n" +
					"	 * @exception NullPointerException Invalid tag\n" +
					"	 * @throws NullPointerException Invalid tag\n" +
					"	 * @return Invalid tag\n" +
					"	 * @param x Invalid tag\n" +
					"	 */\n" +
					"public enum E { TEST, VALID }\n"
			},
			"----------\n" +
				"1. ERROR in E.java (at line 3)\n" +
				"	* @exception NullPointerException Invalid tag\n" +
				"	   ^^^^^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n" +
				"2. ERROR in E.java (at line 4)\n" +
				"	* @throws NullPointerException Invalid tag\n" +
				"	   ^^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n" +
				"3. ERROR in E.java (at line 5)\n" +
				"	* @return Invalid tag\n" +
				"	   ^^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n" +
				"4. ERROR in E.java (at line 6)\n" +
				"	* @param x Invalid tag\n" +
				"	   ^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n"
		);
	}
	public void test035() {
		this.runConformTest(
			new String[] {
				"E.java",
				"	/**\n" +
					"	 * @see \"Valid normal string\"\n" +
					"	 * @see <a href=\"http://java.sun.com/j2se/1.4.2/docs/tooldocs/windows/javadoc.html\">Valid URL link reference</a>\n" +
					"	 * @see Object\n" +
					"	 * @see #TEST\n" +
					"	 * @see E\n" +
					"	 * @see E#TEST\n" +
					"	 */\n" +
					"public enum E { TEST, VALID }\n"
			}
		);
	}
	public void test036() {
		this.runNegativeTest(
			new String[] {
				"E.java",
				"	/**\n" +
					"	 * @see \"invalid\" no text allowed after the string\n" +
					"	 * @see <a href=\"invalid\">invalid</a> no text allowed after the href\n" +
					"	 * @see\n" +
					"	 * @see #VALIDE\n" +
					"	 */\n" +
					"public enum E { TEST, VALID }\n"
			},
			"----------\n" +
				"1. ERROR in E.java (at line 2)\n" + 
				"	* @see \"invalid\" no text allowed after the string\n" + 
				"	                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Unexpected text\n" + 
				"----------\n" + 
				"2. ERROR in E.java (at line 3)\n" + 
				"	* @see <a href=\"invalid\">invalid</a> no text allowed after the href\n" + 
				"	                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Unexpected text\n" + 
				"----------\n" + 
				"3. ERROR in E.java (at line 4)\n" + 
				"	* @see\n" + 
				"	   ^^^\n" + 
				"Javadoc: Missing reference\n" + 
				"----------\n" + 
				"4. ERROR in E.java (at line 5)\n" + 
				"	* @see #VALIDE\n" + 
				"	        ^^^^^^\n" + 
				"Javadoc: VALIDE cannot be resolved or is not a field\n" + 
				"----------\n"
		);
	}
	public void test037() {
		this.runConformTest(
			new String[] {
				"E.java",
				"	/**\n" +
					"	 * Value test: {@value #TEST}\n" +
					"	 * or: {@value E#TEST}\n" +
					"	 */\n" +
					"public enum E { TEST, VALID }\n"
			}
		);
	}
	public void test038() {
		reportMissingJavadocComments = CompilerOptions.ERROR;
		this.runNegativeTest(
			new String[] {
				"E.java",
				"public enum E { TEST, VALID;\n" +
				"	public void foo() {}\n" +
				"}"
			},
			"----------\n" + 
				"1. ERROR in E.java (at line 1)\n" + 
				"	public enum E { TEST, VALID;\n" + 
				"	            ^\n" + 
				"Javadoc: Missing comment for public declaration\n" + 
				"----------\n" + 
				"2. ERROR in E.java (at line 1)\n" + 
				"	public enum E { TEST, VALID;\n" + 
				"	                ^^^^\n" + 
				"Javadoc: Missing comment for public declaration\n" + 
				"----------\n" + 
				"3. ERROR in E.java (at line 1)\n" + 
				"	public enum E { TEST, VALID;\n" + 
				"	                      ^^^^^\n" + 
				"Javadoc: Missing comment for public declaration\n" + 
				"----------\n" + 
				"4. ERROR in E.java (at line 2)\n" + 
				"	public void foo() {}\n" + 
				"	            ^^^^^\n" + 
				"Javadoc: Missing comment for public declaration\n" + 
				"----------\n"
		);
	}
	public void test039() {
		this.runNegativeTest(
			new String[] {
				"E.java",
				"public enum E {\n" +
					"	/**\n" +
					"	 * @exception NullPointerException Invalid tag\n" +
					"	 * @throws NullPointerException Invalid tag\n" +
					"	 * @return Invalid tag\n" +
					"	 * @param x Invalid tag\n" +
					"	 */\n" +
					"	TEST,\n" +
					"	VALID;\n" +
					"}\n"
			},
			"----------\n" +
				"1. ERROR in E.java (at line 3)\n" +
				"	* @exception NullPointerException Invalid tag\n" +
				"	   ^^^^^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n" +
				"2. ERROR in E.java (at line 4)\n" +
				"	* @throws NullPointerException Invalid tag\n" +
				"	   ^^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n" +
				"3. ERROR in E.java (at line 5)\n" +
				"	* @return Invalid tag\n" +
				"	   ^^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n" +
				"4. ERROR in E.java (at line 6)\n" +
				"	* @param x Invalid tag\n" +
				"	   ^^^^^\n" +
				"Javadoc: Unexpected tag\n" +
				"----------\n"
		);
	}
	public void test040() {
		this.runConformTest(
			new String[] {
				"E.java",
				"public enum E {\n" +
					"	/**\n" +
					"	 * @see E\n" +
					"	 * @see #VALID\n" +
					"	 */\n" +
					"	TEST,\n" +
					"	/**\n" +
					"	 * @see E#TEST\n" +
					"	 * @see E\n" +
					"	 */\n" +
					"	VALID;\n" +
					"	/**\n" +
					"	 * @param x the object\n" +
					"	 * @return String\n" +
					"	 * @see Object\n" +
					"	 */\n" +
					"	public String val(Object x) { return x.toString(); }\n" +
					"}\n"
			}
		);
	}
	public void test041() {
		this.runNegativeTest(
			new String[] {
				"E.java",
				"public enum E {\n" +
					"	/**\n" +
					"	 * @see e\n" +
					"	 * @see #VALIDE\n" +
					"	 */\n" +
					"	TEST,\n" +
					"	/**\n" +
					"	 * @see E#test\n" +
					"	 * @see EUX\n" +
					"	 */\n" +
					"	VALID;\n" +
					"	/**\n" +
					"	 * @param obj the object\n" +
					"	 * @return\n" +
					"	 * @see Objet\n" +
					"	 */\n" +
					"	public String val(Object x) { return x.toString(); }\n" +
					"}\n"
			},
			"----------\n" +
				"1. ERROR in E.java (at line 3)\n" + 
				"	* @see e\n" + 
				"	       ^\n" + 
				"Javadoc: e cannot be resolved to a type\n" + 
				"----------\n" + 
				"2. ERROR in E.java (at line 4)\n" + 
				"	* @see #VALIDE\n" + 
				"	        ^^^^^^\n" + 
				"Javadoc: VALIDE cannot be resolved or is not a field\n" + 
				"----------\n" + 
				"3. ERROR in E.java (at line 8)\n" + 
				"	* @see E#test\n" + 
				"	         ^^^^\n" + 
				"Javadoc: test cannot be resolved or is not a field\n" + 
				"----------\n" + 
				"4. ERROR in E.java (at line 9)\n" + 
				"	* @see EUX\n" + 
				"	       ^^^\n" + 
				"Javadoc: EUX cannot be resolved to a type\n" + 
				"----------\n" + 
				"5. ERROR in E.java (at line 13)\n" + 
				"	* @param obj the object\n" + 
				"	         ^^^\n" + 
				"Javadoc: Parameter obj is not declared\n" + 
				"----------\n" + 
				"6. ERROR in E.java (at line 14)\n" + 
				"	* @return\n" + 
				"	   ^^^^^^\n" + 
				"Javadoc: Missing return type description\n" + 
				"----------\n" + 
				"7. ERROR in E.java (at line 15)\n" + 
				"	* @see Objet\n" + 
				"	       ^^^^^\n" + 
				"Javadoc: Objet cannot be resolved to a type\n" + 
				"----------\n" + 
				"8. ERROR in E.java (at line 17)\n" + 
				"	public String val(Object x) { return x.toString(); }\n" + 
				"	                         ^\n" + 
				"Javadoc: Missing tag for parameter x\n" + 
				"----------\n"
		);
	}
	public void test042() {
		this.runConformTest(
			new String[] {
				"E.java",
				"public enum E {\n" +
					"	/**\n" +
					"	 * Test value: {@value #TEST}\n" +
					"	 */\n" +
					"	TEST,\n" +
					"	/**\n" +
					"	 * Valid value: {@value E#VALID}\n" +
					"	 */\n" +
					"	VALID;\n" +
					"	/**\n" +
					"	 * Test value: {@value #TEST}\n" +
					"	 * Valid value: {@value E#VALID}\n" +
					"	 * @param x the object\n" +
					"	 * @return String\n" +
					"	 */\n" +
					"	public String val(Object x) { return x.toString(); }\n" +
					"}\n"
			}
		);
	}
	
	// External javadoc references to enum
	public void test043() {
		this.runConformTest(
			new String[] {
				"test/E.java",
				"package test;\n" +
					"public enum E { TEST, VALID }\n",
				"test/X.java",
				"import static test.E.TEST;\n" +
					"	/**\n" +
					"	 * @see test.E\n" +
					"	 * @see test.E#VALID\n" +
					"	 * @see #TEST\n" +
					"	 */\n" +
					"public class X {}\n"
			}
		);
	}
	public void test044() {
		this.runConformTest(
			new String[] {
				"test/E.java",
				"package test;\n" +
					"public enum E { TEST, VALID }\n",
				"test/X.java",
				"import static test.E.TEST;\n" +
					"	/**\n" +
					"	 * Valid value = {@value test.E#VALID}\n" +
					"	 * Test value = {@value #TEST}\n" +
					"	 */\n" +
					"public class X {}\n"
			}
		);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78321
	 */
	public void test045() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X\n" + 
				"{\n" + 
				"  FIRST,\n" + 
				"  SECOND,\n" + 
				"  THIRD;\n" + 
				"\n" + 
				"  static {\n" + 
				"    for (X t : values()) {\n" + 
				"      System.out.print(t.name());\n" + 
				"    }\n" + 
				"  }\n" + 
				"\n" + 
				"  X() {\n" + 
				"  }\n" + 
				"\n" + 
				"  public static void main(String[] args) {\n" + 
				"  }\n" + 
				"}"
			},
			"FIRSTSECONDTHIRD"
		);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78464
	 */
	public void test046() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"  a(1);\n" + 
				"  X(int i) {\n" + 
				"  }\n" + 
				"}"
			},
			""
		);
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78914
	 */
	public void test047() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X {\n" +
				"	;\n" +
				"	X() {\n" +
				"		super();\n" +
				"	}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	super();\n" + 
			"	^^^^^^^\n" + 
			"Cannot invoke super constructor from enum constructor X()\n" + 
			"----------\n"
		);
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77211
	 */
	public void test048() {
		this.runConformTest(
			new String[] {
				"StopLight.java",
				"public enum StopLight{\n" +
				"    RED{\n" +
				"        public StopLight next(){ return GREEN; }\n" +
				"    },\n" +
				"    GREEN{\n" +
				"        public StopLight next(){ return YELLOW; }\n" +
				"    },\n" +
				"    YELLOW{\n" +
				"        public StopLight next(){ return RED; }\n" +
				"    };\n" +
				"\n" +
				"   public abstract StopLight next();\n" +
				"}"
			},
			""
		);
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78915
	 */
	public void test049() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public abstract enum X {}"
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public abstract enum X {}\n" + 
		"	                     ^\n" + 
		"Illegal modifier for the enum X; only public is permitted\n" + 
		"----------\n"
		);
	}

	public void test050() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X {}"
			},
			""
		);
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78914 - variation
	 */
	public void test051() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	BLEU (0) {\n" + 
				"	}\n" + 
				"	;\n" + 
				"	X() {\n" + 
				"		this(0);\n" + 
				"	}\n" + 
				"	X(int i) {\n" + 
				"	}\n" + 
				"}\n"
			},
			""
		);
	}	

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78916
	 */
	public void test052() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	A\n" + 
				"	;\n" + 
				"	\n" + 
				"	public abstract void foo();\n" + 
				"}\n"
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 5)\n" + 
		"	public abstract void foo();\n" + 
		"	                     ^^^^^\n" + 
		"The enum X can only define the abstract method foo() if it also defines enum constants with corresponding implementations\n" + 
		"----------\n"
		);
	}		

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78916 - variation
	 */
	public void test053() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	A () { public void foo() {} }\n" + 
				"	;\n" + 
				"	\n" + 
				"	public abstract void foo();\n" + 
				"}\n"
			},
			""
		);
	}	
		
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78916 - variation
	 */
	public void test054() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	A() {}\n" + 
				"	;\n" + 
				"	\n" + 
				"	public abstract void foo();\n" + 
				"}\n"
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	A() {}\n" + 
		"	    ^\n" + 
		"The type new X(){} must implement the inherited abstract method X.foo()\n" + 
		"----------\n"
		);
	}			

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78916 - variation
	 */
	public void test055() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	;\n" + 
				"	\n" + 
				"	public abstract void foo();\n" + 
				"}\n"
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	public abstract void foo();\n" + 
		"	                     ^^^^^\n" + 
		"The enum X can only define the abstract method foo() if it also defines enum constants with corresponding implementations\n" + 
		"----------\n"
		);
	}		
	// TODO (philippe) enum cannot be declared as local type
	
	// TODO (philippe) check one cannot redefine Enum incorrectly
	
	// TODO (philippe) check enum syntax recovery
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78914 - variation
	 */
	public void test056() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n" +
				"    PLUS {\n" +
				"        double eval(double x, double y) { return x + y; }\n" +
				"    };\n" +
				"\n" +
				"    // Perform the arithmetic X represented by this constant\n" +
				"    abstract double eval(double x, double y);\n" +
				"}"
			},
			""
		);
		String expectedOutput = 
			"// Compiled from X.java (version 1.5 : 49.0, super bit)\n" + 
			"// Signature: Ljava/lang/Enum<LX;>;\n" + 
			"public abstract enum X extends java.lang.Enum {\n"; 

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED); 
			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 3));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77430
	 */
	public void test057() {
		this.runConformTest(
			new String[] {
				"Enum2.java",
				"public class Enum2 {\n" + 
				"    enum Color { RED, GREEN };\n" + 
				"    public static void main(String[] args) {\n" + 
				"        Color c= Color.GREEN;\n" + 
				"        switch (c) {\n" + 
				"        case RED:\n" + 
				"            System.out.println(Color.RED);\n" + 
				"            break;\n" + 
				"        case GREEN:\n" + 
				"            System.out.println(c);\n" + 
				"            break;\n" + 
				"        }\n" + 
				"    }\n" + 
				"}\n"
			},
			"GREEN"
		);
	}			

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77430 - variation
	 */
	public void test058() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"enum X { a }\n" + 
				"class A {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		test(X.a, 9);\n" + 
				"		test2(X.a, 3);\n" + 
				"	}\n" + 
				"	static void test(X x, int a) {\n" + 
				"		if (x == a) a++; // incomparable types: X and int\n" + 
				"		switch(x) {\n" + 
				"			case a : System.out.println(a); // prints \'9\'\n" + 
				"		}\n" + 
				"	}\n" + 
				"	static void test2(X x, final int aa) {\n" + 
				"		switch(x) {\n" + 
				"			case aa : // unqualified enum constant error\n" + 
				"				System.out.println(a); // cannot find a\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	if (x == a) a++; // incomparable types: X and int\n" + 
			"	    ^^^^^^\n" + 
			"Incompatible operand types X and int\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 15)\n" + 
			"	case aa : // unqualified enum constant error\n" + 
			"	     ^^\n" + 
			"aa cannot be resolved or is not a field\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 16)\n" + 
			"	System.out.println(a); // cannot find a\n" + 
			"	                   ^\n" + 
			"a cannot be resolved\n" + 
			"----------\n"
		);
	}			

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=81262
	 */
	public void test059() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	MONDAY {\n" + 
				"		public void foo() {\n" + 
				"		}\n" + 
				"	};\n" + 
				"	private X() {\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"	  System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"}\n"
			},
			"SUCCESS");
	}	
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=81589
	 */
	public void test060() {
		this.runNegativeTest(
			new String[] {
				"com/flarion/test/a/MyEnum.java",
				"package com.flarion.test.a;\n" + 
				"public enum MyEnum {\n" + 
				"\n" + 
				"    First, Second;\n" + 
				"    \n" + 
				"}\n",
				"com/flarion/test/b/MyClass.java",
				"package com.flarion.test.b;\n" + 
				"import com.flarion.test.a.MyEnum;\n" + 
				"import static com.flarion.test.a.MyEnum.First;\n" +
				"import static com.flarion.test.a.MyEnum.Second;\n" +
				"public class MyClass {\n" + 
				"\n" + 
				"    public void myMethod() {\n" + 
				"        MyEnum e = MyEnum.First;\n" + 
				"        switch (e) {\n" + 
				"        case First:\n" + 
				"            break;\n" + 
				"        case Second:\n" + 
				"            break;\n" + 
				"        }\n" + 
				"        throw new Exception();\n" + // fake error to cause dump of unused import warnings
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in com\\flarion\\test\\b\\MyClass.java (at line 3)\n" + 
			"	import static com.flarion.test.a.MyEnum.First;\n" + 
			"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The import com.flarion.test.a.MyEnum.First is never used\n" + 
			"----------\n" + 
			"2. WARNING in com\\flarion\\test\\b\\MyClass.java (at line 4)\n" + 
			"	import static com.flarion.test.a.MyEnum.Second;\n" + 
			"	              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The import com.flarion.test.a.MyEnum.Second is never used\n" + 
			"----------\n" + 
			"3. ERROR in com\\flarion\\test\\b\\MyClass.java (at line 15)\n" + 
			"	throw new Exception();\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type Exception\n" + 
			"----------\n");
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82217
	 */
	public void test061() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	A, B, C;\n" + 
				"	public static final X D = null;\n" + 
				"}\n" + 
				"\n" + 
				"class A {\n" + 
				"	private void foo(X x) {\n" + 
				"		switch (x) {\n" + 
				"			case D:\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 8)\n" + 
			"	switch (x) {\n" + 
			"	        ^\n" + 
			"The enum constant X.A has no corresponding case label\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 8)\n" + 
			"	switch (x) {\n" + 
			"	        ^\n" + 
			"The enum constant X.C has no corresponding case label\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 8)\n" + 
			"	switch (x) {\n" + 
			"	        ^\n" + 
			"The enum constant X.B has no corresponding case label\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 9)\n" + 
			"	case D:\n" + 
			"	     ^\n" + 
			"The field X.D cannot be referenced from an enum case label; only enum constants can be used in enum switch\n" + 
			"----------\n");
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82217 - variation with qualified name
	 */
	public void test062() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	A, B, C;\n" + 
				"	public static final X D = null;\n" + 
				"}\n" + 
				"\n" + 
				"class A {\n" + 
				"	private void foo(X x) {\n" + 
				"		switch (x) {\n" + 
				"			case X.D:\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 8)\n" + 
			"	switch (x) {\n" + 
			"	        ^\n" + 
			"The enum constant X.A has no corresponding case label\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 8)\n" + 
			"	switch (x) {\n" + 
			"	        ^\n" + 
			"The enum constant X.C has no corresponding case label\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 8)\n" + 
			"	switch (x) {\n" + 
			"	        ^\n" + 
			"The enum constant X.B has no corresponding case label\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 9)\n" + 
			"	case X.D:\n" + 
			"	     ^^^\n" + 
			"The field X.D cannot be referenced from an enum case label; only enum constants can be used in enum switch\n" + 
			"----------\n");
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=81945
	 */
	public void test063() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"  enum Option { ALPHA, BRAVO  };\n" + 
				"  void method1(Option item) {\n" + 
				"    switch (item) {\n" + 
				"    case ALPHA:      break;\n" + 
				"    case BRAVO:      break;\n" + 
				"    }\n" + 
				"  }\n" + 
				"}\n",
			},
			"");
	}		
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82590
	 */
	public void test064() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X implements B {\n" + 
				"\n" + 
				"	C1 {\n" + 
				"		public void test() {};\n" + 
				"	},\n" + 
				"	C2 {\n" + 
				"		public void test() {};\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"interface B {\n" + 
				"	public void test();\n" + 
				"	\n" + 
				"}\n",
			},
			"");
	}	
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83847
	 */
	public void test065() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"  A;\n" + 
				"  private void foo() {\n" + 
				"    X e= new X() {\n" + 
				"    };\n" + 
				"  }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	X e= new X() {\n" + 
			"	         ^\n" + 
			"Cannot instantiate the type X\n" + 
			"----------\n");
	}
    
    /**
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83860
     */
    public void test066() {
        this.runConformTest(
            new String[] {
                "X.java",
                "public enum X {\n" +
                "    SUCCESS (0) {};\n" +
                "    private X(int i) {}\n" +
                "    public static void main(String[] args) {\n" +
                "       for (X x : values()) {\n" +
                "           System.out.print(x);\n" +
                "       }\n" +
                "    }\n" +
                "}",
            },
            "SUCCESS");
    }

    /**
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83219
     */
    public void test067() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                "public enum X {\r\n" + 
                "    ONE, TWO, THREE;\r\n" + 
                "    abstract int getSquare();\r\n" + 
                "    abstract int getSquare();\r\n" + 
                "}",
            },
            "----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	abstract int getSquare();\n" + 
			"	             ^^^^^^^^^^^\n" + 
			"Duplicate method getSquare() in type X\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 4)\n" + 
			"	abstract int getSquare();\n" + 
			"	             ^^^^^^^^^^^\n" + 
			"Duplicate method getSquare() in type X\n" + 
			"----------\n");
    }
    
    /**
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83648
     */
    public void test068() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                "public enum X {\n" +
                "    A(1, 3), B(1, 3), C(1, 3) { }\n" +
                "   	;\n" +
                "    public X(int i, int j) { }\n" +
                "}",
            },
            "----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	public X(int i, int j) { }\n" + 
			"	       ^^^^^^^^^^^^^^^\n" + 
			"Illegal modifier for the enum constructor; only private is permitted.\n" + 
			"----------\n");
    }
    
    /**
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83648
     */
    public void test069() {
        this.runNegativeTest(
            new String[] {
                "X.java",
                "public enum X {\n" +
                "    A(1, 3), B(1, 3), C(1, 3) { }\n" +
                "   	;\n" +
                "    protected X(int i, int j) { }\n" +
                "}",
            },
            "----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	protected X(int i, int j) { }\n" + 
			"	          ^^^^^^^^^^^^^^^\n" + 
			"Illegal modifier for the enum constructor; only private is permitted.\n" + 
			"----------\n");
    }
    
	public void test070() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n" +
				"    PLUS {\n" +
				"        double eval(double x, double y) { return x + y; }\n" +
				"    };\n" +
				"\n" +
				"    // Perform the arithmetic X represented by this constant\n" +
				"    abstract double eval(double x, double y);\n" +
				"}"
			},
			""
		);
		String expectedOutput = 
			"  // Method descriptor #16 (Ljava/lang/String;I)V\n" + 
			"  // Stack: 3, Locals: 3\n" + 
			"  private X(String arg, int arg);\n" + 
			"    0  aload_0 [this]\n" + 
			"    1  aload_1\n" + 
			"    2  iload_2\n" + 
			"    3  invokespecial java.lang.Enum(java.lang.String, int) [25]\n" + 
			"    6  return\n";

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED); 
			int index = actualOutput.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(actualOutput, 3));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, actualOutput);
			}
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
	}

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
	public void test071() {
		this.runConformTest( // no methods to implement
			new String[] {
				"X1.java",
				"public enum X1 implements I {\n" +
				"	;\n" +
				"}\n" +
				"interface I {}\n"
			},
			""
		);
		this.runConformTest( // no methods to implement with constant
			new String[] {
				"X1a.java",
				"public enum X1a implements I {\n" +
				"	A;\n" +
				"}\n" +
				"interface I {}\n"
			},
			""
		);
		this.runConformTest( // no methods to implement with constant body
			new String[] {
				"X1b.java",
				"public enum X1b implements I {\n" +
				"	A() { void random() {} };\n" +
				"}\n" +
				"interface I {}\n"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
	public void test072() {
		this.runConformTest( // implement inherited method
			new String[] {
				"X2.java",
				"public enum X2 implements I {\n" +
				"	;\n" +
				"	public void test() {}\n" +
				"}\n" +
				"interface I { void test(); }\n"
			},
			""
		);
		this.runConformTest( // implement inherited method with constant
			new String[] {
				"X2a.java",
				"public enum X2a implements I {\n" +
				"	A;\n" +
				"	public void test() {}\n" +
				"}\n" +
				"interface I { void test(); }\n"
			},
			""
		);
		this.runConformTest( // implement inherited method with constant body
			new String[] {
				"X2b.java",
				"public enum X2b implements I {\n" +
				"	A() { public void test() {} };\n" +
				"	public void test() {}\n" +
				"}\n" +
				"interface I { void test(); }\n"
			},
			""
		);
		this.runConformTest( // implement inherited method with random constant body
			new String[] {
				"X2c.java",
				"public enum X2c implements I {\n" +
				"	A() { void random() {} };\n" +
				"	public void test() {}\n" +
				"}\n" +
				"interface I { void test(); }\n"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
	public void test073() {
		this.runNegativeTest( // implement inherited method but as abstract
			new String[] {
				"X3.java",
				"public enum X3 implements I {\n" +
				"	;\n" +
				"	public abstract void test();\n" +
				"}\n" +
				"interface I { void test(); }\n"
			},
			"----------\n" + 
			"1. ERROR in X3.java (at line 3)\r\n" + 
			"	public abstract void test();\r\n" + 
			"	                     ^^^^^^\n" + 
			"The enum X3 can only define the abstract method test() if it also defines enum constants with corresponding implementations\n" + 
			"----------\n"
			// X3 is not abstract and does not override abstract method test() in X3
		);
		this.runNegativeTest( // implement inherited method as abstract with constant
			new String[] {
				"X3a.java",
				"public enum X3a implements I {\n" +
				"	A;\n" +
				"	public abstract void test();\n" +
				"}\n" +
				"interface I { void test(); }\n"
			},
			"----------\n" + 
			"1. ERROR in X3a.java (at line 3)\r\n" + 
			"	public abstract void test();\r\n" + 
			"	                     ^^^^^^\n" + 
			"The enum X3a can only define the abstract method test() if it also defines enum constants with corresponding implementations\n" + 
			"----------\n"
			// X3a is not abstract and does not override abstract method test() in X3a
		);
		this.runConformTest( // implement inherited method as abstract with constant body
			new String[] {
				"X3b.java",
				"public enum X3b implements I {\n" +
				"	A() { public void test() {} };\n" +
				"	public abstract void test();\n" +
				"}\n" +
				"interface I { void test(); }\n"
			},
			""
		);
		this.runNegativeTest( // implement inherited method as abstract with random constant body
			new String[] {
				"X3c.java",
				"public enum X3c implements I {\n" +
				"	A() { void random() {} };\n" +
				"	public abstract void test();\n" +
				"}\n" +
				"interface I { void test(); }\n"
			},
			"----------\n" + 
			"1. ERROR in X3c.java (at line 2)\r\n" + 
			"	A() { void random() {} };\r\n" + 
			"	    ^\n" + 
			"The type new X3c(){} must implement the inherited abstract method X3c.test()\n" + 
			"----------\n"
			// <anonymous X3c$1> is not abstract and does not override abstract method test() in X3c
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
	public void test074() {
		this.runNegativeTest( // define abstract method
			new String[] {
				"X4.java",
				"public enum X4 {\n" +
				"	;\n" +
				"	public abstract void test();\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X4.java (at line 3)\r\n" + 
			"	public abstract void test();\r\n" + 
			"	                     ^^^^^^\n" + 
			"The enum X4 can only define the abstract method test() if it also defines enum constants with corresponding implementations\n" + 
			"----------\n"
			// X4 is not abstract and does not override abstract method test() in X4
		);
		this.runNegativeTest( // define abstract method with constant
			new String[] {
				"X4a.java",
				"public enum X4a {\n" +
				"	A;\n" +
				"	public abstract void test();\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X4a.java (at line 3)\r\n" + 
			"	public abstract void test();\r\n" + 
			"	                     ^^^^^^\n" + 
			"The enum X4a can only define the abstract method test() if it also defines enum constants with corresponding implementations\n" + 
			"----------\n"
			// X4a is not abstract and does not override abstract method test() in X4a
		);
		this.runConformTest( // define abstract method with constant body
			new String[] {
				"X4b.java",
				"public enum X4b {\n" +
				"	A() { public void test() {} };\n" +
				"	public abstract void test();\n" +
				"}\n"
			},
			""
		);
		this.runNegativeTest( // define abstract method with random constant body
			new String[] {
				"X4c.java",
				"public enum X4c {\n" +
				"	A() { void random() {} };\n" +
				"	public abstract void test();\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X4c.java (at line 2)\r\n" + 
			"	A() { void random() {} };\r\n" + 
			"	    ^\n" + 
			"The type new X4c(){} must implement the inherited abstract method X4c.test()\n" + 
			"----------\n"
			// <anonymous X4c$1> is not abstract and does not override abstract method test() in X4c
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
	public void test075() {
		this.runNegativeTest( // do not implement inherited method
			new String[] {
				"X5.java",
				"public enum X5 implements I {\n" +
				"	;\n" +
				"}\n" +
				"interface I { void test(); }\n"
			},
			"----------\n" + 
			"1. ERROR in X5.java (at line 1)\r\n" + 
			"	public enum X5 implements I {\r\n" + 
			"	            ^^\n" + 
			"The type X5 must implement the inherited abstract method I.test()\n" + 
			"----------\n"
			// X5 is not abstract and does not override abstract method test() in I
		);
		this.runNegativeTest( // do not implement inherited method & have constant with no body
			new String[] {
				"X5a.java",
				"public enum X5a implements I {\n" +
				"	A;\n" +
				"}\n" +
				"interface I { void test(); }\n"
			},
			"----------\n" + 
			"1. ERROR in X5a.java (at line 1)\r\n" + 
			"	public enum X5a implements I {\r\n" + 
			"	            ^^^\n" + 
			"The type X5a must implement the inherited abstract method I.test()\n" + 
			"----------\n"
			// X5a is not abstract and does not override abstract method test() in I
		);
		this.runConformTest( // do not implement inherited method & have constant with body
			new String[] {
				"X5b.java",
				"public enum X5b implements I {\n" +
				"	A() { public void test() {} };\n" +
				"	;\n" +
				"}\n" +
				"interface I { void test(); }\n"
			},
			""
		);
		this.runNegativeTest( // do not implement inherited method & have constant with random body
			new String[] {
				"X5c.java",
				"public enum X5c implements I {\n" +
				"	A() { void random() {} };\n" +
				"	;\n" +
				"	private X5c() {}\n" +
				"}\n" +
				"interface I { void test(); }\n"
			},
			"----------\n" + 
			"1. ERROR in X5c.java (at line 2)\r\n" + 
			"	A() { void random() {} };\r\n" + 
			"	    ^\n" + 
			"The type new X5c(){} must implement the inherited abstract method I.test()\n" + 
			"----------\n"
			// <anonymous X5c$1> is not abstract and does not override abstract method test() in I
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83902
	public void test076() { // bridge method needed
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	public static void main(String[] args) { ((I) E.A).foo(); }\n" +
				"}\n" +
				"interface I { I foo(); }\n" +
				"enum E implements I {\n" +
				"	A;\n" +
				"	public E foo() {\n" +
				"		System.out.println(\"SUCCESS\");\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n"
			},
			"SUCCESS"
		);
	}
	
	public void test077() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	\n" + 
				"	public static void main(String[] args) {\n" + 
				"		E.A.bar();\n" + 
				"	}\n" + 
				"}\n" + 
				"enum E {\n" + 
				"	A {\n" + 
				"		void bar() {\n" + 
				"			new M();\n" + 
				"		}\n" + 
				"	};\n" + 
				"	abstract void bar();\n" + 
				"	\n" + 
				"	class M {\n" + 
				"		M() {\n" + 
				"			System.out.println(\"SUCCESS\");\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n"
			},
			"SUCCESS"
		);
	}	
	
	public void test078() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	\n" + 
				"	public static void main(String[] args) {\n" + 
				"		E.A.bar();\n" + 
				"	}\n" + 
				"}\n" + 
				"enum E {\n" + 
				"	A {\n" + 
				"		void bar() {\n" + 
				"			new X(){\n" + 
				"				void baz() {\n" + 
				"					new M();\n" + 
				"				}\n" + 
				"			}.baz();\n" + 
				"		}\n" + 
				"	};\n" + 
				"	abstract void bar();\n" + 
				"	\n" + 
				"	class M {\n" + 
				"		M() {\n" + 
				"			System.out.println(\"SUCCESS\");\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n"
			},
			"SUCCESS"
		);
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85397
	public void test079() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X {\n" +
				"	A, B;\n" +
				"	private strictfp X() {}\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	private strictfp X() {}\n" + 
			"	                 ^^^\n" + 
			"Illegal modifier for the method X.X()\n" + 
			"----------\n"
		);
		this.runConformTest(
			new String[] {
				"X.java",
				"public strictfp enum X {\n" +
				"	A, B;\n" +
				"	private X() {}\n" +
				"}\n"
			},
			""
		);

		String[] expectedOutputs = new String[] {
			"  private strictfp X(String arg, int arg);\n",
			"  public static final strictfp X[] values();\n",
			"  public static final strictfp X valueOf(String arg);\n"
		};

		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
			actualOutput =
				disassembler.disassemble(
					classFileBytes,
					"\n",
					ClassFileBytesDisassembler.DETAILED);
			
			for (int i = 0, max = expectedOutputs.length; i < max; i++) {
				String expectedOutput = expectedOutputs[i];
				int index = actualOutput.indexOf(expectedOutput);
				if (index == -1 || expectedOutput.length() == 0) {
					System.out.println(Util.displayString(actualOutput, 3));
				}
				if (index == -1) {
					assertEquals("Wrong contents", expectedOutput, actualOutput);
				}
			}
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue("ClassFormatException", false);
		} catch (IOException e) {
			assertTrue("IOException", false);
		}
	}	
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=87064
	public void test080() {
		this.runConformTest(
			new String[] {
				"X.java",
				"interface TestInterface {\n" + 
				"	int test();\n" + 
				"}\n" + 
				"\n" + 
				"public enum X implements TestInterface {\n" + 
				"	TEST {\n" + 
				"		public int test() {\n" + 
				"			return 42;\n" + 
				"		}\n" + 
				"	},\n" + 
				"	ENUM {\n" + 
				"		public int test() {\n" + 
				"			return 37;\n" + 
				"		}\n" + 
				"	};\n" + 
				"} \n"
			},
			""
		);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87818
	public void test081() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void foo() {\n" +
				"		enum E {}\n" +
				"	}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	enum E {}\n" + 
			"	     ^\n" + 
			"The member enum E cannot be local\n" + 
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88223
	public void test082() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	class Y {\n" +
				"		enum E {}\n" +
				"	}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	enum E {}\n" + 
			"	     ^\n" + 
			"The member enum E must be defined inside a static member type\n" + 
			"----------\n");
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	static class Y {\n" +
				"		enum E {}\n" +
				"	}\n" +
				"}"
			},
			"");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"	void foo() {\n" +
				"		class Local {\n" +
				"			enum E {}\n" +
				"		}\n" +
				"	}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	enum E {}\n" + 
			"	     ^\n" + 
			"The member enum E cannot be local\n" + 
			"----------\n");
	}
	

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87998 - check no emulation warning
	public void test083() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	INPUT {\n" + 
				"		@Override\n" +
				"		public X getReverse() {\n" + 
				"			return OUTPUT;\n" + 
				"		}\n" + 
				"	},\n" + 
				"	OUTPUT {\n" + 
				"		@Override\n" +
				"		public X getReverse() {\n" + 
				"			return INPUT;\n" + 
				"		}\n" + 
				"	},\n" + 
				"	INOUT {\n" + 
				"		@Override\n" +
				"		public X getReverse() {\n" + 
				"			return INOUT;\n" + 
				"		}\n" + 
				"	};\n" + 
				"	X(){}\n" + 
				"  Zork z;\n" +
				"	public abstract X getReverse();\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 21)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
	}	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87998 - check private constructor generation
	public void test084() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	INPUT {\n" + 
				"		@Override\n" +
				"		public X getReverse() {\n" + 
				"			return OUTPUT;\n" + 
				"		}\n" + 
				"	},\n" + 
				"	OUTPUT {\n" + 
				"		@Override\n" +
				"		public X getReverse() {\n" + 
				"			return INPUT;\n" + 
				"		}\n" + 
				"	},\n" + 
				"	INOUT {\n" + 
				"		@Override\n" +
				"		public X getReverse() {\n" + 
				"			return INOUT;\n" + 
				"		}\n" + 
				"	};\n" + 
				"	X(){}\n" + 
				"	public abstract X getReverse();\n" + 
				"}\n",
			},
			"");
		
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
			"  // Method descriptor #18 (Ljava/lang/String;I)V\n" + 
			"  // Stack: 3, Locals: 3\n" + 
			"  private X(String arg, int arg);\n"; 
			
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 3));
		}
		if (index == -1) {
			assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
		}
	}	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88625
	public void test085() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	enum Test1 {\n" + 
				"		test11, test12\n" + 
				"	};\n" + 
				"	enum Test2 {\n" + 
				"		test21, test22\n" + 
				"	};\n" + 
				"\n" + 
				"	void foo1(Test1 t1, Test2 t2) {\n" + 
				"		boolean b = t1 == t2;\n" + 
				"	}\n" + 
				"	void foo2(Test1 t1, Object t2) {\n" + 
				"		boolean b = t1 == t2;\n" + 
				"	}\n" + 
				"	void foo3(Test1 t1, Enum t2) {\n" + 
				"		boolean b = t1 == t2;\n" + 
				"	}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		boolean booleanTest = (Test1.test11 == Test2.test22);\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	boolean b = t1 == t2;\n" + 
			"	            ^^^^^^^^\n" + 
			"Incompatible operand types X.Test1 and X.Test2\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 19)\n" + 
			"	boolean booleanTest = (Test1.test11 == Test2.test22);\n" + 
			"	                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Incompatible operand types X.Test1 and X.Test2\n" + 
			"----------\n");
	}	
	public void test086() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	enum Test1 {\n" + 
				"		V;\n" + 
				"		static int foo = 0;\n" + 
				"	}\n" + 
				"}\n",
			},
			"");
	}	
	public void test087() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"	enum Test1 {\n" + 
				"		V;\n" + 
				"		interface Foo {}\n" + 
				"	}\n" + 
				"}\n",
			},
			"");
	}	
	public void test088() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	enum Test1 {\n" + 
				"		V;\n" + 
				"	}\n" + 
				"	Object foo() {\n" + 
				"		return this;\n" + 
				"	}\n" + 
				"\n" + 
				"	static class Sub extends X {\n" + 
				"		@Override\n" + 
				"		Test1 foo() {\n" + 
				"			return Test1.V;\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"");
	}		
	public void test089() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	enum Test1 {\n" + 
				"		V;\n" + 
				"		protected final Test1 clone() { return V; }\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	protected final Test1 clone() { return V; }\n" + 
			"	                      ^^^^^^^\n" + 
			"Cannot override the final method from Enum<X.Test1>\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 5)\n" + 
			"	protected final Test1 clone() { return V; }\n" + 
			"	                      ^^^^^^^\n" + 
			"The method clone() of type X.Test1 should be tagged with @Override since it actually overrides a superclass method\n" + 
			"----------\n");
	}			
	public void test090() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	enum Test1 {\n" + 
				"		V;\n" + 
				"		public Test1 foo() { return V; }\n" + 
				"	}\n" + 
				"	Zork z;\n" +
				"}\n",
				"java/lang/Object.java",
				"package java.lang;\n" +
				"public class Object {\n" + 
				"	public Object foo() { return this; }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	public Test1 foo() { return V; }\n" + 
			"	             ^^^^^\n" + 
			"The method foo() of type X.Test1 should be tagged with @Override since it actually overrides a superclass method\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
	}
	public void test091() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	enum Test1 {\n" + 
				"		V;\n" + 
				"		void foo() {}\n" + 
				"	}\n" + 
				"	class Member<E extends Test1> {\n" + 
				"		void bar(E e) {\n" + 
				"			e.foo();\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"");
	}		
	public void test092() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	enum Test1 {\n" + 
				"		V;\n" + 
				"		void foo() {}\n" + 
				"	}\n" + 
				"	class Member<E extends Object & Test1> {\n" + 
				"		void bar(E e) {\n" + 
				"			e.foo();\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	class Member<E extends Object & Test1> {\n" + 
			"	                                ^^^^^\n" + 
			"The type X.Test1 is not an interface; it cannot be specified as a bounded parameter\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 9)\n" + 
			"	e.foo();\n" + 
			"	  ^^^\n" + 
			"The method foo() is undefined for the type E\n" + 
			"----------\n");
	}
	// check wildcard can extend Enum superclass
	public void test093() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"\n" + 
				"	enum Test1 {\n" + 
				"		V;\n" + 
				"		void foo() {}\n" + 
				"	}\n" + 
				"	class Member<E extends Test1> {\n" + 
				"		E e;\n" + 
				"		void bar(Member<? extends Test1> me) {\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n",
			},
			"");
	}		
	// check super bit is set
	public void test094() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"}\n",
			},
			"");
		
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
			"// Compiled from X.java (version 1.5 : 49.0, super bit)\n" + 
			"// Signature: Ljava/lang/Enum<LX;>;\n" + 
			"public final enum X extends java.lang.Enum {\n" + 
			"  \n" + 
			"  // Field descriptor #6 [LX;\n" + 
			"  private static final synthetic X[] ENUM$VALUES;\n" + 
			"  \n" + 
			"  // Method descriptor #8 (Ljava/lang/String;I)V\n" + 
			"  // Stack: 3, Locals: 3\n" + 
			"  private X(String arg, int arg);\n" + 
			"    0  aload_0 [this]\n" + 
			"    1  aload_1\n" + 
			"    2  iload_2\n" + 
			"    3  invokespecial java.lang.Enum(java.lang.String, int) [11]\n" + 
			"    6  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 1]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 7] local: this index: 0 type: X\n" + 
			"  \n" + 
			"  // Method descriptor #17 ()[LX;\n" + 
			"  // Stack: 5, Locals: 3\n" + 
			"  public static final X[] values();\n" + 
			"     0  getstatic X.ENUM$VALUES : X[] [19]\n" + 
			"     3  dup\n" + 
			"     4  astore_0\n" + 
			"     5  iconst_0\n" + 
			"     6  aload_0\n" + 
			"     7  arraylength\n" + 
			"     8  dup\n" + 
			"     9  istore_1\n" + 
			"    10  anewarray X [2]\n" + 
			"    13  dup\n" + 
			"    14  astore_2\n" + 
			"    15  iconst_0\n" + 
			"    16  iload_1\n" + 
			"    17  invokestatic java.lang.System.arraycopy(java.lang.Object, int, java.lang.Object, int, int) : void  [25]\n" + 
			"    20  aload_2\n" + 
			"    21  areturn\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 1]\n" + 
			"  \n" + 
			"  // Method descriptor #27 (Ljava/lang/String;)LX;\n" + 
			"  // Stack: 3, Locals: 4\n" + 
			"  public static final X valueOf(String arg);\n" + 
			"     0  getstatic X.ENUM$VALUES : X[] [19]\n" + 
			"     3  dup\n" + 
			"     4  astore_1\n" + 
			"     5  arraylength\n" + 
			"     6  istore_2\n" + 
			"     7  goto 27\n" + 
			"    10  aload_0\n" + 
			"    11  aload_1\n" + 
			"    12  iload_2\n" + 
			"    13  aaload\n" + 
			"    14  dup\n" + 
			"    15  astore_3\n" + 
			"    16  invokevirtual X.name() : java.lang.String  [31]\n" + 
			"    19  invokevirtual java.lang.String.equals(java.lang.Object) : boolean  [37]\n" + 
			"    22  ifeq 27\n" + 
			"    25  aload_3\n" + 
			"    26  areturn\n" + 
			"    27  iinc 2 -1\n" + 
			"    30  iload_2\n" + 
			"    31  ifge 10\n" + 
			"    34  new java.lang.IllegalArgumentException [39]\n" + 
			"    37  dup\n" + 
			"    38  aload_0\n" + 
			"    39  invokespecial java.lang.IllegalArgumentException(java.lang.String) [42]\n" + 
			"    42  athrow\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 1]\n" + 
			"}"; 
			
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 3));
		}
		if (index == -1) {
			assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
		}
	}
	public void test095() { // check missing abstract cases from multiple interfaces
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X implements I, J { \n" + 
				"	ROUGE;\n" + 
				"}\n" +
				"interface I { void foo(); }\n" +
				"interface J { void foo(); }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public enum X implements I, J { \n" + 
			"	            ^\n" + 
			"The type X must implement the inherited abstract method I.foo()\n" + 
			"----------\n");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X implements I, J { \n" + 
				"	ROUGE;\n" + 
				"	public void foo() {}\n" + 
				"}\n" +
				"interface I { void foo(int i); }\n" +
				"interface J { void foo(); }\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public enum X implements I, J { \n" + 
			"	            ^\n" + 
			"The type X must implement the inherited abstract method I.foo(int)\n" + 
			"----------\n");
	}
	public void test096() { // check for raw vs. parameterized parameter types
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X implements I { \n" + 
				"	ROUGE;\n" + 
				"	public void foo(A a) {}\n" + 
				"}\n" +
				"interface I { void foo(A<String> a); }\n" +
				"class A<T> {}\n"
			},
			"");
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X implements I { \n" + 
				"	ROUGE { public void foo(A a) {} }\n" +
				"	;\n" + 
				"}\n" +
				"interface I { void foo(A<String> a); }\n" +
				"class A<T> {}\n"
			},
			"");
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X implements I { \n" + 
				"	ROUGE;\n" + 
				"	public void foo(A<String> a) {}\n" + 
				"}\n" +
				"interface I { void foo(A a); }\n" +
				"class A<T> {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\r\n" + 
			"	public enum X implements I { \r\n" + 
			"	            ^\n" + 
			"The type X must implement the inherited abstract method I.foo(A)\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\r\n" + 
			"	public void foo(A<String> a) {}\r\n" + 
			"	            ^^^^^^^^^^^^^^^^\n" + 
			"Name clash: The method foo(A<String>) of type X has the same erasure as foo(A) of type I but does not override it\n" + 
			"----------\n");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89982
	public void test097() {
		this.runNegativeTest(
			new String[] {
				"E.java",
				"public class E {\n" + 
				"	enum Numbers { ONE, TWO, THREE }\n" + 
				"	static final String BLANK = \"    \";\n" + 
				"	void foo(Colors color) {\n" + 
				"		switch (color) {\n" + 
				"			case BLUE:\n" + 
				"			case RED:\n" + 
				"				break;\n" + 
				"		} \n" + 
				"	}\n" + 
				"}\n" + 
				"/**\n" + 
				" * Enumeration of some basic colors.\n" + 
				" */\n" + 
				"enum Colors {\n" + 
				"	BLACK,\n" + 
				"	WHITE,\n" + 
				"	RED  \n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in E.java (at line 6)\n" + 
			"	case BLUE:\n" + 
			"	     ^^^^\n" + 
			"BLUE cannot be resolved or is not a field\n" + 
			"----------\n");
	}			
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89982 - variation
	public void test098() {
		this.runNegativeTest(
			new String[] {
				"E.java",
				"public class E {\n" + 
				"	enum Numbers { ONE, TWO, THREE }\n" + 
				"	static final String BLANK = \"    \";\n" + 
				"	void foo(Colors color) {\n" + 
				"		switch (color) {\n" + 
				"		} \n" + 
				"	}\n" + 
				"}\n" + 
				"/**\n" + 
				" * Enumeration of some basic colors.\n" + 
				" */\n" + 
				"enum Colors {\n" + 
				"	BLACK,\n" + 
				"	WHITE,\n" + 
				"	RED;  \n" + 
				"  Zork z;\n" +
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in E.java (at line 5)\n" + 
			"	switch (color) {\n" + 
			"	        ^^^^^\n" + 
			"The enum constant Colors.BLACK has no corresponding case label\n" + 
			"----------\n" + 
			"2. WARNING in E.java (at line 5)\n" + 
			"	switch (color) {\n" + 
			"	        ^^^^^\n" + 
			"The enum constant Colors.RED has no corresponding case label\n" + 
			"----------\n" + 
			"3. WARNING in E.java (at line 5)\n" + 
			"	switch (color) {\n" + 
			"	        ^^^^^\n" + 
			"The enum constant Colors.WHITE has no corresponding case label\n" + 
			"----------\n" + 
			"4. ERROR in E.java (at line 16)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
	}			
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89274
	public void test099() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class A<T> {\n" + 
				"	enum E {\n" + 
				"		v1, v2;\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"public class X extends A<Integer> {\n" + 
				"	void a(A.E e) {\n" + 
				"		b(e); // no unchecked warning\n" + 
				"	}\n" + 
				"\n" + 
				"	void b(E e) {\n" + 
				"		A<Integer>.E e1 = e;\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 13)\n" + 
			"	A<Integer>.E e1 = e;\n" + 
			"	^^^^^^^^^^^^\n" + 
			"The member type A<Integer>.E cannot be qualified with a parameterized type, since it is static. Remove arguments from qualifying type A<Integer>\n" + 
			"----------\n");
	}				
	/* from JLS
"It is a compile-time error to reference a static field of an enum type
that is not a compile-time constant (15.28) from constructors, instance
initializer blocks, or instance variable initializer expressions of that
type.  It is a compile-time error for the constructors, instance initializer
blocks, or instance variable initializer expressions of an enum constant e1
to refer to itself or an enum constant of the same type that is declared to
the right of e1."
	*/
	public void test100() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"\n" + 
				"	anEnumValue {\n" + 
				"		private final X thisOne = anEnumValue;\n" + 
				"\n" + 
				"		@Override String getMessage() {\n" + 
				"			return \"Here is what thisOne gets assigned: \" + thisOne;\n" + 
				"		}\n" + 
				"	};\n" + 
				"\n" + 
				"	abstract String getMessage();\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(anEnumValue.getMessage());\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	private final X thisOne = anEnumValue;\n" + 
			"	                          ^^^^^^^^^^^\n" + 
			"Cannot refer to the static enum field X.anEnumValue within an initializer\n" + 
			"----------\n");
	}	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=91761
	public void test101() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"interface Foo {\n" + 
				"  public boolean bar();\n" + 
				"}\n" + 
				"enum BugDemo {\n" + 
				"  CONSTANT(new Foo() {\n" + 
				"    public boolean bar() {\n" + 
				"      Zork z;\n" + 
				"      return true;\n" + 
				"    }\n" + 
				"  });\n" + 
				"  BugDemo(Foo foo) {\n" + 
				"  }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90775
	public void test102() {
	    this.runNegativeTest(
            new String[] {
                "X.java",
				"import java.util.*;\n" + 
				"\n" + 
				"public class X <T> {\n" + 
				"	enum SomeEnum {\n" + 
				"		A, B;\n" + 
				"		static SomeEnum foo() {\n" + 
				"			return null;\n" + 
				"		}\n" + 
				"	}\n" + 
				"	Enum<SomeEnum> e = SomeEnum.A;\n" + 
				"		\n" + 
				"	Set<SomeEnum> set1 = EnumSet.of(SomeEnum.A);\n" + 
				"	Set<SomeEnum> set2 = EnumSet.of(SomeEnum.foo());\n" + 
				"	\n" + 
				"	Foo<Bar> foo = null;\n" + 
				"}\n" + 
				"class Foo <U extends Foo<U>> {\n" + 
				"}\n" + 
				"class Bar extends Foo {\n" + 
				"}\n",
	        },
			"----------\n" + 
			"1. ERROR in X.java (at line 15)\n" + 
			"	Foo<Bar> foo = null;\n" + 
			"	    ^^^\n" + 
			"Bound mismatch: The type Bar is not a valid substitute for the bounded parameter <U extends Foo<U>> of the type Foo<U>\n" + 
			"----------\n");
	}		
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=93396
    public void test103() {
        this.runNegativeTest(
            new String[] {
                "BadEnum.java",
                "public class BadEnum {\n" + 
                "  public interface EnumInterface<T extends Object> {\n" + 
                "    public T getMethod();\n" + 
                "  }\n" + 
                "  public enum EnumClass implements EnumInterface<String> {\n" + 
                "    ENUM1 { public String getMethod() { return \"ENUM1\";} },\n" + 
                "    ENUM2 { public String getMethod() { return \"ENUM2\";} };\n" + 
                "  }\n" + 
                "}\n" + 
                "}\n",
            },
            "----------\n" + 
            "1. ERROR in BadEnum.java (at line 10)\n" + 
            "	}\n" + 
            "	^\n" + 
            "Syntax error on token \"}\", delete this token\n" + 
            "----------\n");
    }
    //https://bugs.eclipse.org/bugs/show_bug.cgi?id=90215
    public void test104() {
        this.runConformTest(
            new String[] {
                "p/Placeholder.java",
				"package p;\n" + 
				"\n" + 
				"public class Placeholder {\n" + 
				"    public static void main(String... argv) {\n" + 
				"        ClassWithBadEnum.EnumClass constant = ClassWithBadEnum.EnumClass.ENUM1;\n" + // forward ref
				"        ClassWithBadEnum.main(argv);\n" + 
				"	}\n" + 
				"}    \n" + 
				"\n",
                "p/ClassWithBadEnum.java",
				"package p;\n" + 
				"\n" + 
				"public class ClassWithBadEnum {\n" + 
				"	public interface EnumInterface<T extends Object> {\n" + 
				"	    public T getMethod();\n" + 
				"	}\n" + 
				"\n" + 
				"	public enum EnumClass implements EnumInterface<String> {\n" + 
				"		ENUM1 { public String getMethod() { return \"ENUM1\";} },\n" + 
				"		ENUM2 { public String getMethod() { return \"ENUM2\";} };\n" + 
				"	}\n" + 
				"	private EnumClass enumVar; \n" + 
				"	public EnumClass getEnumVar() {\n" + 
				"		return enumVar;\n" + 
				"	}\n" + 
				"	public void setEnumVar(EnumClass enumVar) {\n" + 
				"		this.enumVar = enumVar;\n" + 
				"	}\n" + 
				"\n" + 
				"	public static void main(String... argv) {\n" + 
				"		int a = 1;\n" + 
				"		ClassWithBadEnum badEnum = new ClassWithBadEnum();\n" + 
				"		badEnum.setEnumVar(ClassWithBadEnum.EnumClass.ENUM1);\n" + 
				"		// Should fail if bug manifests itself because there will be two getInternalValue() methods\n" + 
				"		// one returning an Object instead of a String\n" + 
				"		String s3 = badEnum.getEnumVar().getMethod();\n" + 
				"		System.out.println(s3);\n" + 
				"	}\n" + 
				"}  \n",
            },
            "ENUM1");
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
	public void test105() {
		this.runConformTest(
				new String[] {
					"pack/X.java",
					"package pack;\n" +
					"import static pack.Color.*;\n" +
					"public class X {\n" +
					"    public static void main(String[] args) {\n" +
					"        Color c = BLACK;\n" +
					"        switch(c) {\n" +
					"        case BLACK:\n" +
					"            System.out.print(\"Black\");\n" +
					"            break;\n" +
					"        case WHITE:\n" +
					"            System.out.print(\"White\");\n" +
					"            break;\n" +
					"        }\n" +
					"    }\n" +
					"}",
					"pack/Color.java",
					"package pack;\n" + 
					"enum Color {WHITE, BLACK}"
				},
				"Black"
			);
		
		this.runConformTest(
			new String[] {
				"pack/Color.java",
				"package pack;\n" + 
				"enum Color {BLACK, WHITE}"
			},
			"",
			null,
			false,
			null
		);
		
		this.executeClass(
			"pack/X.java",
			"Black",
			null,
			false,
			null,
			null,
			null);	
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
	public void test106() {
		this.runConformTest(
				new String[] {
					"pack/X.java",
					"package pack;\n" +
					"import static pack.Color.*;\n" +
					"public class X {\n" +
					"    public static void main(String[] args) {\n" +
					"        Color c = BLACK;\n" +
					"        switch(c) {\n" +
					"        }\n" +
					"		 System.out.print(\"SUCCESS\");\n" +
					"    }\n" +
					"}",
					"pack/Color.java",
					"package pack;\n" + 
					"enum Color {WHITE, BLACK}"
				},
				"SUCCESS"
			);
		
		this.runConformTest(
			new String[] {
				"pack/Color.java",
				"package pack;\n" + 
				"enum Color {BLACK, WHITE}"
			},
			"",
			null,
			false,
			null
		);
		
		this.executeClass(
			"pack/X.java",
			"SUCCESS",
			null,
			false,
			null,
			null,
			null);	
	}
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
	public void test107() {
		this.runConformTest(
				new String[] {
					"pack/X.java",
					"package pack;\n" +
					"import static pack.Color.*;\n" +
					"public class X {\n" +
					"    public static void main(String[] args) {\n" +
					"        Color c = BLACK;\n" +
					"        switch(c) {\n" +
					"        case BLACK:\n" +
					"            System.out.print(\"Black\");\n" +
					"            break;\n" +
					"        case WHITE:\n" +
					"            System.out.print(\"White\");\n" +
					"            break;\n" +
					"        }\n" +
					"        switch(c) {\n" +
					"        case BLACK:\n" +
					"            System.out.print(\"Black\");\n" +
					"            break;\n" +
					"        case WHITE:\n" +
					"            System.out.print(\"White\");\n" +
					"            break;\n" +
					"        }\n" +
					"    }\n" +
					"}",
					"pack/Color.java",
					"package pack;\n" + 
					"enum Color {WHITE, BLACK}"
				},
				"BlackBlack"
			);
		
		this.runConformTest(
			new String[] {
				"pack/Color.java",
				"package pack;\n" + 
				"enum Color { BLACK }"
			},
			"",
			null,
			false,
			null
		);
		
		this.executeClass(
			"pack/X.java",
			"BlackBlack",
			null,
			false,
			null,
			null,
			null);	
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
	public void test108() {
		this.runConformTest(
				new String[] {
					"pack/X.java",
					"package pack;\n" +
					"import static pack.Color.*;\n" +
					"public class X {\n" +
					"    public static void main(String[] args) {\n" +
					"        Color c = BLACK;\n" +
					"        switch(c) {\n" +
					"        case BLACK:\n" +
					"            System.out.print(\"Black\");\n" +
					"            break;\n" +
					"        case WHITE:\n" +
					"            System.out.print(\"White\");\n" +
					"            break;\n" +
					"        default:\n" +
					"            System.out.print(\"Error\");\n" +
					"            break;\n" +
					"        }\n" +
					"    }\n" +
					"}",
					"pack/Color.java",
					"package pack;\n" + 
					"enum Color {WHITE, BLACK}"
				},
				"Black"
			);
		
		this.runConformTest(
			new String[] {
				"pack/Color.java",
				"package pack;\n" + 
				"enum Color {RED, GREEN, YELLOW, BLACK, WHITE}"
			},
			"",
			null,
			false,
			null
		);
		
		this.executeClass(
			"pack/X.java",
			"Black",
			null,
			false,
			null,
			null,
			null);	
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
	public void test109() {
		this.runConformTest(
				new String[] {
					"pack/X.java",
					"package pack;\n" +
					"import static pack.Color.*;\n" +
					"public class X {\n" +
					"    public static void main(String[] args) {\n" +
					"		Color c = null;\n" +
					"		 try {\n" +
					"        	c = BLACK;\n" +
					"		} catch(NoSuchFieldError e) {\n" +
					"			System.out.print(\"SUCCESS\");\n" +
					"			return;\n" +
					"		}\n" +
					"      	switch(c) {\n" +
					"       	case BLACK:\n" +
					"          	System.out.print(\"Black\");\n" +
					"          	break;\n" +
					"       	case WHITE:\n" +
					"          	System.out.print(\"White\");\n" +
					"          	break;\n" +
					"      	}\n" +
					"    }\n" +
					"}",
					"pack/Color.java",
					"package pack;\n" + 
					"enum Color {WHITE, BLACK}"
				},
				"Black"
			);
		
		this.runConformTest(
			new String[] {
				"pack/Color.java",
				"package pack;\n" + 
				"enum Color {RED, GREEN, YELLOW, WHITE}"
			},
			"",
			null,
			false,
			null
		);
		
		this.executeClass(
			"pack/X.java",
			"SUCCESS",
			null,
			false,
			null,
			null,
			null);	
	}
	
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
	public void test110() {
		this.runConformTest(
				new String[] {
					"pack/X.java",
					"package pack;\n" +
					"import static pack.Color.*;\n" +
					"public class X {\n" +
					"	public int[] $SWITCH_TABLE$pack$Color;\n" +
					"	public int[] $SWITCH_TABLE$pack$Color() { return null; }\n" +
					"   public static void main(String[] args) {\n" +
					"        Color c = BLACK;\n" +
					"        switch(c) {\n" +
					"        case BLACK:\n" +
					"            System.out.print(\"Black\");\n" +
					"            break;\n" +
					"        case WHITE:\n" +
					"            System.out.print(\"White\");\n" +
					"            break;\n" +
					"        }\n" +
					"    }\n" +
					"}",
					"pack/Color.java",
					"package pack;\n" + 
					"enum Color {WHITE, BLACK}"
				},
				"Black"
			);
		
		this.runConformTest(
			new String[] {
				"pack/Color.java",
				"package pack;\n" + 
				"enum Color {BLACK, WHITE}"
			},
			"",
			null,
			false,
			null
		);
		
		this.executeClass(
			"pack/X.java",
			"Black",
			null,
			false,
			null,
			null,
			null);	
	}
	
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
	public void test111() {
		this.runConformTest(
				new String[] {
					"pack/X.java",
					"package pack;\n" +
					"import static pack.Color.*;\n" +
					"public class X {\n" +
					"	public int[] $SWITCH_TABLE$pack$Color;\n" +
					"	public int[] $SWITCH_TABLE$pack$Color() { return null; }\n" +
					"   public static void main(String[] args) {\n" +
					"        Color c = BLACK;\n" +
					"        switch(c) {\n" +
					"        case BLACK:\n" +
					"            System.out.print(\"Black\");\n" +
					"            break;\n" +
					"        case WHITE:\n" +
					"            System.out.print(\"White\");\n" +
					"            break;\n" +
					"        }\n" +
					"		 foo();\n" +
					"    }\n" +
					"   public static void foo() {\n" +
					"        Color c = BLACK;\n" +
					"        switch(c) {\n" +
					"        case BLACK:\n" +
					"            System.out.print(\"Black\");\n" +
					"            break;\n" +
					"        case WHITE:\n" +
					"            System.out.print(\"White\");\n" +
					"            break;\n" +
					"        }\n" +
					"    }\n" +
					"}",
					"pack/Color.java",
					"package pack;\n" + 
					"enum Color {WHITE, BLACK}"
				},
				"BlackBlack"
			);
		
		this.runConformTest(
			new String[] {
				"pack/Color.java",
				"package pack;\n" + 
				"enum Color {BLACK, WHITE}"
			},
			"",
			null,
			false,
			null
		);
		
		this.executeClass(
			"pack/X.java",
			"BlackBlack",
			null,
			false,
			null,
			null,
			null);	
	}
	
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=97247
	public void test112() {
		this.runConformTest(
			new String[] {
				"com/annot/Foo.java",
				"package com.annot;\r\n" + 
				"\r\n" + 
				"import static com.annot.TestType.*;\n" +
				"\n" +
				"public class Foo {\n" +
				"	@Test(type=PERFORMANCE)\n" +
				"	public void testBar() throws Exception {\n" +
				"		Test annotation = this.getClass().getMethod(\"testBar\").getAnnotation(Test.class);\n" +
				"		switch (annotation.type()) {\n" +
				"			case PERFORMANCE:\n" +
				"				System.out.println(PERFORMANCE);\n" +
				"				break;\n" +
				"			case CORRECTNESS:\n" +
				"				System.out.println(CORRECTNESS);\n" +
				"				break;\n" +
				"		}		\n" +
				"	}\n" +
				"}",
				"com/annot/Test.java",
				"package com.annot;\n" +
				"\n" +
				"import static com.annot.TestType.CORRECTNESS;\n" +
				"import static java.lang.annotation.ElementType.METHOD;\n" +
				"\n" +
				"import java.lang.annotation.Target;\n" +
				"\n" +
				"@Target(METHOD)\n" +
				"public @interface Test {\n" +
				"	TestType type() default CORRECTNESS;\n" +
				"}",
				"com/annot/TestType.java",
				"package com.annot;\n" +
				"\n" +
				"public enum TestType {\n" +
				"	CORRECTNESS,\n" +
				"	PERFORMANCE\n" +
				"}"
			},
			""
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=93789
	public void test113() {
	    this.runNegativeTest(
            new String[] {
                "X.java",
				"enum BugDemo {\n" + 
				"	FOO() {\n" + 
				"		static int bar;\n" + 
				"	}\n" + 
				"}\n",
	        },
			"----------\n" + 
			"1. WARNING in X.java (at line 3)\n" + 
			"	static int bar;\n" + 
			"	           ^^^\n" + 
			"The field new BugDemo(){}.bar is never read locally\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	static int bar;\n" + 
			"	           ^^^\n" + 
			"The field bar cannot be declared static; static fields can only be declared in static or top level types\n" + 
			"----------\n");
	}	

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=99428 and https://bugs.eclipse.org/bugs/show_bug.cgi?id=99655
	public void test114() {
	    this.runConformTest(
            new String[] {
                "EnumTest.java",
				"import java.lang.reflect.*;\n" + 
				"import java.lang.annotation.*;\n" + 
				"@ExpectedModifiers(Modifier.FINAL)\n" + 
				"public enum EnumTest {\n" + 
				"	X(255);\n" + 
				"	EnumTest(int r) {}\n" + 
				"	public static void main(String argv[]) throws Exception {\n" + 
				"		test(\"EnumTest\");\n" + 
				"		test(\"EnumTest$EnumA\");\n" + 
				"		test(\"EnumTest$EnumB\");\n" + 
				"		test(\"EnumTest$EnumB2\");\n" + 
				"		test(\"EnumTest$EnumB3\");\n" + 
				// TODO (kent) need verifier to detect when an Enum should be tagged as abstract
				//"		test(\"EnumTest$EnumC\");\n" + 
				//"		test(\"EnumTest$EnumC2\");\n" + 
				"		test(\"EnumTest$EnumC3\");\n" + 
				"		test(\"EnumTest$EnumD\");\n" + 
				"	}\n" + 
				"	static void test(String className) throws Exception {\n" + 
				"		Class c = Class.forName(className);\n" + 
				"		ExpectedModifiers em = (ExpectedModifiers) c.getAnnotation(ExpectedModifiers.class);\n" + 
				"		if (em != null) {\n" + 
				"			int classModifiers = c.getModifiers();\n" + 
				"			int expected = em.value();\n" + 
				"			if (expected != (classModifiers & (Modifier.ABSTRACT|Modifier.FINAL|Modifier.STATIC))) {\n" + 
				"				if ((expected & Modifier.ABSTRACT) != (classModifiers & Modifier.ABSTRACT))\n" + 
				"					System.out.println(\"FAILED ABSTRACT: \" + className);\n" + 
				"				if ((expected & Modifier.FINAL) != (classModifiers & Modifier.FINAL))\n" + 
				"					System.out.println(\"FAILED FINAL: \" + className);\n" + 
				"				if ((expected & Modifier.STATIC) != (classModifiers & Modifier.STATIC))\n" + 
				"					System.out.println(\"FAILED STATIC: \" + className);\n" + 
				"			}\n" + 
				"		}\n" + 
				"	}\n" + 
				"	@ExpectedModifiers(Modifier.FINAL|Modifier.STATIC)\n" + 
				"	enum EnumA {\n" + 
				"		A;\n" + 
				"	}\n" + 
				"	@ExpectedModifiers(Modifier.STATIC)\n" + 
				"	enum EnumB {\n" + 
				"		B {\n" + 
				"			int value() { return 1; }\n" + 
				"		};\n" + 
				"		int value(){ return 0; }\n" + 
				"	}\n" + 
				"	@ExpectedModifiers(Modifier.STATIC)\n" + 
				"	enum EnumB2 {\n" + 
				"		B2 {};\n" + 
				"		int value(){ return 0; }\n" + 
				"	}\n" + 
				"	@ExpectedModifiers(Modifier.FINAL|Modifier.STATIC)\n" + 
				"	enum EnumB3 {\n" + 
				"		B3;\n" + 
				"		int value(){ return 0; }\n" + 
				"	}\n" + 
				"	@ExpectedModifiers(Modifier.STATIC)\n" + 
				"	enum EnumC implements I {\n" + 
				"		C {\n" + 
				"			int value() { return 1; }\n" + 
				"		};\n" + 
				"		int value(){ return 0; }\n" + 
				"		public void foo(){}\n" + 
				"	}\n" + 
				"	@ExpectedModifiers(Modifier.STATIC)\n" + 
				"	enum EnumC2 implements I {\n" + 
				"		C2 {};\n" + 
				"		int value(){ return 0; }\n" + 
				"		public void foo(){}\n" + 
				"	}\n" + 
				"	@ExpectedModifiers(Modifier.FINAL|Modifier.STATIC)\n" + 
				"	enum EnumC3 implements I {\n" + 
				"		C3;\n" + 
				"		int value(){ return 0; }\n" + 
				"		public void foo(){}\n" + 
				"	}\n" + 
				"	@ExpectedModifiers(Modifier.ABSTRACT|Modifier.STATIC)\n" + 
				"	enum EnumD {\n" + 
				"		D {\n" + 
				"			int value() { return 1; }\n" + 
				"		};\n" + 
				"		abstract int value();\n" + 
				"	}\n" + 
				"}\n" +
				"interface I {\n" +
				"	void foo();\n" + 
				"}\n" + 
				"@Retention(RetentionPolicy.RUNTIME)\n" + 
				"@interface ExpectedModifiers {\n" + 
				"	int value();\n" + 
				"}"
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101713
	public void test115() {
	    this.runNegativeTest(
            new String[] {
                "X.java",
				"public enum X {\n" + 
				"	VALUE;\n" + 
				"\n" + 
				"	static int ASD;\n" + 
				"	final static int CST = 0;\n" + 
				"	\n" + 
				"	private X() {\n" + 
				"		VALUE = null;\n" + 
				"		ASD = 5;\n" + 
				"		X.VALUE = null;\n" + 
				"		X.ASD = 5;\n" + 
				"		\n" + 
				"		System.out.println(CST);\n" + 
				"	}\n" + 
				"}\n",
	        },
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	VALUE = null;\n" + 
			"	^^^^^\n" + 
			"Cannot refer to the static enum field X.VALUE within an initializer\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	VALUE = null;\n" + 
			"	^^^^^\n" + 
			"The final field X.VALUE cannot be assigned\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 9)\n" + 
			"	ASD = 5;\n" + 
			"	^^^\n" + 
			"Cannot refer to the static enum field X.ASD within an initializer\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 10)\n" + 
			"	X.VALUE = null;\n" + 
			"	^^^^^^^\n" + 
			"The final field X.VALUE cannot be assigned\n" + 
			"----------\n");
	}	
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101713 - variation
	public void test116() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	BLEU, \n" + 
				"	BLANC, \n" + 
				"	ROUGE;\n" + 
				"	{\n" + 
				"		BLEU = null;\n" + 
				"	}\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	BLEU = null;\n" + 
			"	^^^^\n" + 
			"Cannot refer to the static enum field X.BLEU within an initializer\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	BLEU = null;\n" + 
			"	^^^^\n" + 
			"The final field X.BLEU cannot be assigned\n" + 
			"----------\n");
	}	
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101713 - variation
	public void test117() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X { \n" + 
				"	BLEU, \n" + 
				"	BLANC, \n" + 
				"	ROUGE;\n" + 
				"	{\n" + 
				"		X x = BLEU.BLANC; // ko\n" + 
				"		X x2 = BLEU; // ko\n" + 
				"	}\n" + 
				"	static {\n" + 
				"		X x = BLEU.BLANC; // ok\n" + 
				"		X x2 = BLEU; // ok\n" + 
				"	}	\n" + 
				"	X dummy = BLEU; // ko\n" + 
				"	static X DUMMY = BLANC; // ok\n" + 
				"	X() {\n" + 
				"		X x = BLEU.BLANC; // ko\n" + 
				"		X x2 = BLEU; // ko\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 6)\n" + 
			"	X x = BLEU.BLANC; // ko\n" + 
			"	      ^^^^^^^^^^\n" + 
			"The static field X.BLANC should be accessed in a static way\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 6)\n" + 
			"	X x = BLEU.BLANC; // ko\n" + 
			"	      ^^^^^^^^^^\n" + 
			"Cannot refer to the static enum field X.BLEU within an initializer\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 7)\n" + 
			"	X x2 = BLEU; // ko\n" + 
			"	       ^^^^\n" + 
			"Cannot refer to the static enum field X.BLEU within an initializer\n" + 
			"----------\n" + 
			"4. WARNING in X.java (at line 10)\n" + 
			"	X x = BLEU.BLANC; // ok\n" + 
			"	      ^^^^^^^^^^\n" + 
			"The static field X.BLANC should be accessed in a static way\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 13)\n" + 
			"	X dummy = BLEU; // ko\n" + 
			"	          ^^^^\n" + 
			"Cannot refer to the static enum field X.BLEU within an initializer\n" + 
			"----------\n" + 
			"6. WARNING in X.java (at line 16)\n" + 
			"	X x = BLEU.BLANC; // ko\n" + 
			"	      ^^^^^^^^^^\n" + 
			"The static field X.BLANC should be accessed in a static way\n" + 
			"----------\n" + 
			"7. ERROR in X.java (at line 16)\n" + 
			"	X x = BLEU.BLANC; // ko\n" + 
			"	      ^^^^^^^^^^\n" + 
			"Cannot refer to the static enum field X.BLEU within an initializer\n" + 
			"----------\n" + 
			"8. ERROR in X.java (at line 17)\n" + 
			"	X x2 = BLEU; // ko\n" + 
			"	       ^^^^\n" + 
			"Cannot refer to the static enum field X.BLEU within an initializer\n" + 
			"----------\n");
	}	
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=102265
	public void test118() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"\n" + 
				"public enum X {\n" + 
				"		 one,\n" + 
				"		 two;\n" + 
				"		 \n" + 
				"		 static ArrayList someList;\n" + 
				"		 \n" + 
				"		 private X() {\n" + 
				"		 		 if (someList == null) {\n" + 
				"		 		 		 someList = new ArrayList();\n" + 
				"		 		 }\n" + 
				"		 }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	if (someList == null) {\n" + 
			"	    ^^^^^^^^\n" + 
			"Cannot refer to the static enum field X.someList within an initializer\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 11)\n" + 
			"	someList = new ArrayList();\n" + 
			"	^^^^^^^^\n" + 
			"Cannot refer to the static enum field X.someList within an initializer\n" + 
			"----------\n");
	}		
	public void test119() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"	BLEU, BLANC, ROUGE;\n" + 
				"	final static int CST = 0;\n" + 
				"    enum Member {\n" + 
				"    	;\n" + 
				"        Object obj1 = CST;\n" + 
				"        Object obj2 = BLEU;\n" + 
				"    }\n" + 
				"}\n"
			},
			"");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=102213
	public void test120() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"\n" + 
				"	A() {\n" + 
				"		final X a = A;\n" + 
				"		final X a2 = B.A;\n" + 
				"		@Override void foo() {\n" + 
				"			System.out.println(String.valueOf(a));\n" + 
				"			System.out.println(String.valueOf(a2));\n" + 
				"		}\n" + 
				"	},\n" + 
				"	B() {\n" + 
				"		@Override void foo(){}\n" + 
				"	};\n" + 
				"	abstract void foo();\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"		A.foo();\n" + 
				"	}\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	final X a = A;\n" + 
			"	            ^\n" + 
			"Cannot refer to the static enum field X.A within an initializer\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 5)\n" + 
			"	final X a2 = B.A;\n" + 
			"	             ^^^\n" + 
			"The static field X.A should be accessed in a static way\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 5)\n" + 
			"	final X a2 = B.A;\n" + 
			"	             ^^^\n" + 
			"Cannot refer to the static enum field X.B within an initializer\n" + 
			"----------\n");
	}			
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=92165
	public void test121() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public enum X {\n" + 
				"\n" + 
				"	UNKNOWN();\n" + 
				"\n" + 
				"	private static String error;\n" + 
				"\n" + 
				"	{\n" + 
				"		error = \"error\";\n" + 
				"	}\n" + 
				"\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 5)\n" + 
			"	private static String error;\n" + 
			"	                      ^^^^^\n" + 
			"The field X.error is never read locally\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	error = \"error\";\n" + 
			"	^^^^^\n" + 
			"Cannot refer to the static enum field X.error within an initializer\n" + 
			"----------\n");
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=112231
	public void test125() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"\n" + 
				"public class X {\n" + 
				"	interface I {\n" + 
				"		int values();\n" + 
				"		enum E implements I {\n" + 
				"			A, B, C;\n" +
				"		}\n" +
				"	}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	enum E implements I {\n" + 
			"	     ^\n" + 
			"This static method cannot hide the instance method from X.I\n" + 
			"----------\n");
	}
}
