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
package org.eclipse.jdt.core.tests.model;

import java.io.IOException;

import junit.framework.Test;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * TO DO:
 * - source attachment on external jar.
 * - don't use assertTrue where assertEquals should be used
 * - don't hardcode positions
*/
public class AttachSourceTests extends ModifyingResourceTests {

	/** @deprecated using deprecated code */
	private static final int AST_INTERNAL_JLS2 = AST.JLS2;
	
	private IPackageFragmentRoot pkgFragmentRoot;
	private IType genericType;
	private IPackageFragment innerClasses;
	
public AttachSourceTests(String name) {
	super(name);
}
public static Test suite() {
	return new Suite(AttachSourceTests.class);
}
public ASTNode runConversion(IClassFile classFile, boolean resolveBindings) {
	ASTParser parser = ASTParser.newParser(AST_INTERNAL_JLS2);
	parser.setSource(classFile);
	parser.setResolveBindings(resolveBindings);
	parser.setWorkingCopyOwner(null);
	return parser.createAST(null);
}
protected void setUp() throws Exception {
	super.setUp();
	this.attachSource(this.pkgFragmentRoot, "/AttachSourceTests/attachsrc.zip", "");
}
/**
 * Create project and set the jar placeholder.
 */
public void setUpSuite() throws Exception {
	super.setUpSuite();
	
	IJavaProject project = setUpJavaProject("AttachSourceTests");
	this.pkgFragmentRoot = project.getPackageFragmentRoot(this.getFile("/AttachSourceTests/attach.jar"));
	setUpGenericJar();
	setUpInnerClassesJar();
}
private void setUpGenericJar() throws IOException, CoreException {
	String[] pathAndContents = new String[] {
		"generic/X.java", 
		"package generic;\n" +
		"public class X<T> {\n" + 
		"  void foo(X<T> x) {\n" +
		"  }\n" +
		"  <K, V> V foo(K key, V value) {\n" +
		"    return value;\n" +
		"  }\n" +
		"}"
	};
	IJavaProject project = getJavaProject("AttachSourceTests");
	addLibrary(project, "generic.jar", "genericsrc.zip", pathAndContents, JavaCore.VERSION_1_5);
	IFile jar = getFile("/AttachSourceTests/generic.jar");
	this.genericType = project.getPackageFragmentRoot(jar).getPackageFragment("generic").getClassFile("X.class").getType();
}
private void setUpInnerClassesJar() throws IOException, CoreException {
	String[] pathAndContents = new String[] {
		"inner/X.java", 
		"package inner;\n" +
		"public class X {\n" + 
		"  void foo() {\n" +
		"    new X() {};\n" +
		"    class Y {}\n" +
		"    new Y() {\n" +
		"      class Z {}\n" +
		"    };\n" +
		"    class W {\n" +
		"      void bar() {\n" +
		"        new W() {};\n" +
		"      }\n" +
		"    }\n" +
		"  }\n" +
		"  class V {\n" +
		"  }\n" +
		"}"
	};
	IJavaProject project = getJavaProject("AttachSourceTests");
	addLibrary(project, "innerClasses.jar", "innerClassessrc.zip", pathAndContents, JavaCore.VERSION_1_4);
	IFile jar = getFile("/AttachSourceTests/innerClasses.jar");
	this.innerClasses = project.getPackageFragmentRoot(jar).getPackageFragment("inner");
}
protected void tearDown() throws Exception {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
	for (int i = 0; i < roots.length; i++) {
		IPackageFragmentRoot root = roots[i];
		if (this.genericType != null && root.equals(this.genericType.getPackageFragment().getParent())) continue;
		if (this.innerClasses != null && root.equals(this.innerClasses.getParent())) continue;
		if (root.getKind() == IPackageFragmentRoot.K_BINARY) {
			this.attachSource(root, null, null); // detach source
		}
	}
	super.tearDown();
}

/**
 * Reset the jar placeholder and delete project.
 */
public void tearDownSuite() throws Exception {
	this.deleteProject("AttachSourceTests");
	super.tearDown();
}

/**
 * Test AST.parseCompilationUnit(IClassFile, boolean).
 */
public void testASTParsing() throws JavaModelException {
	this.attachSource(this.pkgFragmentRoot, "/AttachSourceTests/attachsrc.zip", "");	
	IClassFile classFile = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	ASTNode node = runConversion(classFile, true);
	assertNotNull("No node", node);
	this.attachSource(this.pkgFragmentRoot, null, null);
	IClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	assertTrue("source code should no longer exist for A", cf.getSource() == null);
	try {
		node = runConversion(classFile, true);
		assertTrue("Should not be here", false);
	} catch(IllegalStateException e) {
		assertTrue(true);
	}
}
/**
 * Test AST.parseCompilationUnit(IClassFile, boolean).
 * Test for http://bugs.eclipse.org/bugs/show_bug.cgi?id=30471
 */
public void testASTParsing2() throws JavaModelException {
	this.attachSource(this.pkgFragmentRoot, "/AttachSourceTests/attachsrc.zip", "");	
	IClassFile classFile = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	ASTNode node = runConversion(classFile, false);
	assertNotNull("No node", node);
	this.attachSource(this.pkgFragmentRoot, null, null);
	IClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	assertTrue("source code should no longer exist for A", cf.getSource() == null);
	try {
		node = runConversion(classFile, false);
		assertTrue("Should not be here", false);
	} catch(IllegalStateException e) {
		assertTrue(true);
	}
}
/**
 * Changing the source attachment file should update the java model.
 * (regression test for bug 23292 Must restart Eclipse after debug of source in .zip is updated)
 */
public void testChangeSourceAttachmentFile() throws CoreException {
	IClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	IMethod method = cf.getType().getMethod("foo", new String[] {});
	
	// check initial source
	assertSourceEquals(
		"unexpected initial source for foo()",
		"public void foo() {\n" +
		"	}",
		method.getSource());

	// replace source attachment file
	this.swapFiles("AttachSourceTests/attachsrc.zip", "AttachSourceTests/attachsrc.new.zip");
	assertSourceEquals(
		"unexpected source for foo() after replacement",
		"public void foo() {\n" +
		"		System.out.println(\"foo\");\n" +
		"	}",
		method.getSource());
		
	// delete source attachment file
	this.deleteFile("AttachSourceTests/attachsrc.zip");
	assertSourceEquals(
		"unexpected source for foo() after deletion",
		null,
		method.getSource());
		
	// add source attachment file back
	this.moveFile("AttachSourceTests/attachsrc.new.zip", "AttachSourceTests/attachsrc.zip");
	assertSourceEquals(
		"unexpected source for foo() after addition",
		"public void foo() {\n" +
		"	}",
		method.getSource());
}
/**
 * Ensure that a class file with an attached source can retrieve its children given a source index.
 */
public void testClassFileGetElementAt() throws JavaModelException {
	IClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	IJavaElement elt = null;
	
	elt = cf.getElementAt(15);
	assertTrue("should have found \"A\"",
		elt != null &&
		elt.getElementType() == IJavaElement.TYPE &&
		elt.getElementName().equals("A"));
	
	elt = cf.getElementAt(53);
	assertTrue("should have found \"public A()\"",
		elt != null &&
		elt.getElementType() == IJavaElement.METHOD &&
		elt.getElementName().equals("A"));

	elt = cf.getElementAt(72);
	assertTrue("should have found \"public void foo()\"",
		elt != null &&
		elt.getElementType() == IJavaElement.METHOD &&
		elt.getElementName().equals("foo"));
}
/*
 * Ensures that the source of a .class file is implicetely attached when prj=src=bin
 * (regression test for bug 41444 [navigation] error dialog on opening class file)
 */
public void testClassFileInOutput() throws CoreException {
	IClassFile classFile = getClassFile("AttachSourceTests/src/A.class");
	String source = classFile.getSource();
	assertSourceEquals(
		"Unexpected source",
		"public class A {\n" + 
		"}",
		source);
}
/**
 * Retrieves the source code for "A.class", which is
 * the entire CU for "A.java".
 */
public void testClassRetrieval() throws JavaModelException {
	IClassFile objectCF = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	assertTrue("source code does not exist for the entire attached compilation unit", objectCF.getSource() != null);
}
/**
 * Removes the source attachment from the jar.
 */
public void testDetachSource() throws JavaModelException {
	this.attachSource(this.pkgFragmentRoot, null, null);
	IClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	assertTrue("source code should no longer exist for A", cf.getSource() == null);
	assertTrue("name range should no longer exist for A", cf.getType().getNameRange().getOffset() == -1);
	assertTrue("source range should no longer exist for A", cf.getType().getSourceRange().getOffset() == -1);
	assertTrue("Source attachment path should be null", null == this.pkgFragmentRoot.getSourceAttachmentPath());
	assertTrue("Source attachment root path should be null", null ==this.pkgFragmentRoot.getSourceAttachmentRootPath());
}
/*
 * Ensures that the source of a generic method can be retrieved.
 */
public void testGeneric1() throws JavaModelException {
	IMethod method = this.genericType.getMethod("foo", new String[] {"QX<QT;>;"});
	assertSourceEquals(
		"Unexpected source",
		"void foo(X<T> x) {\n" + 
		"  }",
		method.getSource());
}
/*
 * Ensures that the source of a generic method can be retrieved.
 */
public void testGeneric2() throws JavaModelException {
	IMethod method = this.genericType.getMethod("foo", new String[] {"QK;", "QV;"});
	assertSourceEquals(
		"Unexpected source",
		"<K, V> V foo(K key, V value) {\n" + 
		"    return value;\n" + 
		"  }",
		method.getSource());
}
/**
 * Ensures that name ranges exists for BinaryMembers that have
 * mapped source.
 */
public void testGetNameRange() throws JavaModelException {
	IClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	IMethod method = cf.getType().getMethod("foo", null);
	assertTrue("method name range not correct", method.getNameRange().getOffset() != -1 && method.getNameRange().getLength() != 0);

	IClassFile objectCF = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	ISourceRange range= objectCF.getType().getNameRange();
	int start, end;
	start= range.getOffset();
	end= start + range.getLength() - 1;

	assertTrue("source code does not exist for the entire attached compilation unit", start != -1 && end != -1);
	String source= objectCF.getSource().substring(start, end + 1);
	assertSourceEquals("name should be 'A'", "A", source);
}
/**
 * Retrieves the source attachment paths for jar root.
 */
public void testGetSourceAttachmentPath() throws JavaModelException {
	IPath saPath= this.pkgFragmentRoot.getSourceAttachmentPath();
	assertEquals("Source attachment path not correct for root " + this.pkgFragmentRoot, "/AttachSourceTests/attachsrc.zip", saPath.toString());
	assertEquals("Source attachment root path should be empty", new Path(""), this.pkgFragmentRoot.getSourceAttachmentRootPath());
}
/**
 * Ensures that a source range exists for the class file that has
 * mapped source.
 */
public void testGetSourceRange() throws JavaModelException {
	IClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	ISourceRange sourceRange = cf.getSourceRange();
	assertTrue("Class file should have associated source range", sourceRange != null);
	assertEquals("Unexpected offset", 0, sourceRange.getOffset());
	assertEquals("Unexpected length", 100, sourceRange.getLength());
}
/**
 * Ensures that a source range exists for the (inner) class file that has
 * mapped source.
 */
public void testGetSourceRangeInnerClass() throws JavaModelException {
	IClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A$Inner.class");
	ISourceRange sourceRange = cf.getSourceRange();
	assertTrue("Inner class file should have associated source range", sourceRange != null);
	assertEquals("Unexpected offset", 0, sourceRange.getOffset());
	assertEquals("Unexpected length", 100, sourceRange.getLength());
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass1() throws JavaModelException {
	IType type = this.innerClasses.getClassFile("X.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"public class X {\n" + 
		"  void foo() {\n" + 
		"    new X() {};\n" + 
		"    class Y {}\n" + 
		"    new Y() {\n" + 
		"      class Z {}\n" + 
		"    };\n" + 
		"    class W {\n" + 
		"      void bar() {\n" + 
		"        new W() {};\n" + 
		"      }\n" + 
		"    }\n" + 
		"  }\n" + 
		"  class V {\n" + 
		"  }\n" + 
		"}",
		type.getSource());
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass2() throws JavaModelException {
	IType type = this.innerClasses.getClassFile("X$1.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"X() {}",
		type.getSource());
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass3() throws JavaModelException {
	IType type = this.innerClasses.getClassFile("X$2.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"Y() {\n" + 
		"      class Z {}\n" + 
		"    }",
		type.getSource());
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass4() throws JavaModelException {
	IType type = this.innerClasses.getClassFile("X$3.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"W() {}",
		type.getSource());
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass5() throws JavaModelException {
	IType type = this.innerClasses.getClassFile("X$1$Y.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"class Y {}",
		type.getSource());
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass6() throws JavaModelException {
	IType type = this.innerClasses.getClassFile("X$1$W.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"class W {\n" + 
		"      void bar() {\n" + 
		"        new W() {};\n" + 
		"      }\n" + 
		"    }",
		type.getSource());
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass7() throws JavaModelException {
	IType type = this.innerClasses.getClassFile("X$2$Z.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"class Z {}",
		type.getSource());
}
/*
 * Ensures that the source of an inner class can be retrieved.
 */
public void testInnerClass8() throws JavaModelException {
	IType type = this.innerClasses.getClassFile("X$V.class").getType();
	assertSourceEquals(
		"Unexpected source",
		"class V {\n" + 
		"  }",
		type.getSource());
}

/**
 * Ensures that a source folder can be attached to a lib folder.
 */
public void testLibFolder() throws JavaModelException {
	IPackageFragmentRoot root = this.getPackageFragmentRoot("/AttachSourceTests/lib");
	this.attachSource(root, "/AttachSourceTests/srcLib", "");
	
	IClassFile cf = root.getPackageFragment("p").getClassFile("X.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p;\n" +
		"public class X {\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}",
		cf.getSource());
}
/**
 * Retrieves the source code for methods of class A.
 */
public void testMethodRetrieval() throws JavaModelException {
	IClassFile cf = this.pkgFragmentRoot.getPackageFragment("x.y").getClassFile("A.class");
	IMethod[] methods = cf.getType().getMethods();
	for (int i = 0; i < methods.length; i++) {
		IMethod method = methods[i];
		assertTrue("source code does not exist for the method " + method, method.getSource() != null);
		assertTrue("method name range not correct", method.getNameRange().getOffset() != -1 && method.getNameRange().getLength() != 0);
	}
}
/**
 * Closes the jar, to ensure when it is re-opened the source
 * attachment still exists.
 */
public void testPersistence() throws JavaModelException {
	this.pkgFragmentRoot.close();
	testClassRetrieval();
	testMethodRetrieval();
}

/*
 * Ensures that having a project as a class folder and attaching its sources finds the source
 * of a class in a non-default package.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=65186)
 */
public void testProjectAsClassFolder1() throws CoreException {
	try {
		createJavaProject("P1");
		createFolder("/P1/p");
		createFile(
			"/P1/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"}"
		);
		IProject p1 = getProject("P1");
		p1.build(IncrementalProjectBuilder.FULL_BUILD, null);
		IJavaProject javaProject = createJavaProject("P2", new String[]{""}, new String[]{"/P1"}, "");
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(p1);
		attachSource(root, "/P1", null);
		IClassFile cf = root.getPackageFragment("p").getClassFile("X.class");
		assertSourceEquals(
			"Unexpected source for class file P1/p/X.class",
			"package p;\n" +
			"public class X {\n" +
			"}",
			cf.getSource());		
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}

/*
 * Ensures that having a project as a class folder and attaching its sources finds the source
 * of a class in the default package.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=65186)
 */
public void testProjectAsClassFolder2() throws CoreException {
	try {
		createJavaProject("P1");
		createFile(
			"/P1/X.java",
			"public class X {\n" +
			"}"
		);
		IProject p1 = getProject("P1");
		p1.build(IncrementalProjectBuilder.FULL_BUILD, null);
		IJavaProject javaProject = createJavaProject("P2", new String[]{""}, new String[]{"/P1"}, "");
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(p1);
		attachSource(root, "/P1", null);
		IClassFile cf = root.getPackageFragment("").getClassFile("X.class");
		assertSourceEquals(
			"Unexpected source for class file P1/X.class",
			"public class X {\n" +
			"}",
			cf.getSource());		
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}

/*
 * Ensures that having a project as source attachement finds the source
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=65186)
 */
public void testProjectAsSourceAttachment() throws CoreException {
	try {
		IJavaProject javaProject = createJavaProject("Test", new String[]{""}, new String[]{"/AttachSourceTests/test.jar"}, "");
		createFolder("/Test/test1");
		createFile("/Test/test1/Test.java",
			"package test1;\n" + 
			"\n" + 
			"public class Test {}");
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(getFile("/AttachSourceTests/test.jar"));
		attachSource(root, "/Test", null);
		IClassFile cf = root.getPackageFragment("test1").getClassFile("Test.class");
		assertSourceEquals(
			"Unexpected source for class file test1/Test.class",
			"package test1;\n" + 
			"\n" + 
			"public class Test {}",
			cf.getSource());		
	} finally {
		deleteProject("Test");
	}
}

/**
 * Attaches a source zip to a jar.  The source zip has
 * a nested root structure and exists as a resource.  Tests that
 * the attachment is persisted as a server property for the jar.
 */
public void testRootPath() throws JavaModelException {
	IJavaProject project = getJavaProject("AttachSourceTests");
	IFile jar = (IFile) project.getProject().findMember("attach2.jar");
	IFile srcZip=(IFile) project.getProject().findMember("attach2src.zip");
	JarPackageFragmentRoot root = (JarPackageFragmentRoot) project.getPackageFragmentRoot(jar);
	root.attachSource(srcZip.getFullPath(), new Path("src/nested"), null);

	IClassFile cf = root.getPackageFragment("x.y").getClassFile("B.class");
	assertTrue("source code does not exist for the entire attached compilation unit", cf.getSource() != null);
	root.close();
	cf = root.getPackageFragment("x.y").getClassFile("B.class");
	assertTrue("source code does not exist for the entire attached compilation unit", cf.getSource() != null);

	IPath rootSAPath= root.getSourceAttachmentRootPath();
	assertEquals("Unexpected source attachment root path for " + root.getPath(), "src/nested", rootSAPath.toString());

	IPath saPath= root.getSourceAttachmentPath();
	assertEquals("Unexpected source attachment path for " + root.getPath(), "/AttachSourceTests/attach2src.zip", saPath.toString());
	
	root.close();
}
/**
 * Attaches a source zip to a jar specifying an invalid root path.  
 * Ensures that the root path is just used as a hint, and that the source is still retrieved.
 */
public void testRootPath2() throws JavaModelException {
	IJavaProject project = getJavaProject("AttachSourceTests");
	IFile jar = (IFile) project.getProject().findMember("attach2.jar");
	IFile srcZip=(IFile) project.getProject().findMember("attach2src.zip");
	JarPackageFragmentRoot root = (JarPackageFragmentRoot) project.getPackageFragmentRoot(jar);
	root.attachSource(srcZip.getFullPath(), new Path(""), null);

	IClassFile cf = root.getPackageFragment("x.y").getClassFile("B.class");
	assertTrue("source code does not exist for the entire attached compilation unit", cf.getSource() != null);
	root.close();
}
/**
 * Attaches a sa source folder can be attached to a lib folder specifying an invalid root path.  
 * Ensures that the root path is just used as a hint, and that the source is still retrieved.
 */
public void testRootPath3() throws JavaModelException {
	IPackageFragmentRoot root = this.getPackageFragmentRoot("/AttachSourceTests/lib");
	this.attachSource(root, "/AttachSourceTests/srcLib", "invalid");
	
	IClassFile cf = root.getPackageFragment("p").getClassFile("X.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p;\n" +
		"public class X {\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}",
		cf.getSource());
	root.close();
}
/**
 * Attach a jar with a source attachement that doesn't contain the source folders
 */
public void testRootPath4() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(this.getFile("/AttachSourceTests/test.jar"));
	this.attachSource(root, "/AttachSourceTests/src.zip", "invalid");
	
	IClassFile cf = root.getPackageFragment("test1").getClassFile("Test.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package test1;\n" +
		"\n" +
		"public class Test {}",
		cf.getSource());
	root.close();
}
/**
 * Attach a jar with a source attachement that doesn't contain the source folders
 */
public void testRootPath5() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(this.getFile("/AttachSourceTests/update.jar"));
	this.attachSource(root, "/AttachSourceTests/src.zip", "invalid");
	
	IClassFile cf = root.getPackageFragment("p1.p2").getClassFile("A.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p1.p2;\n" +
		"\n" +
		"public class A {}",
		cf.getSource());
		
	cf = root.getPackageFragment("").getClassFile("B.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"public class B {}",
		cf.getSource());		
	
	this.attachSource(root, null, null); // detach source
	root.close();
}
/**
 * Attach a jar with a source attachement that doesn't contain the source folders
 */
public void testRootPath6() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(this.getFile("/AttachSourceTests/update.jar"));
	this.attachSource(root, "/AttachSourceTests/src.zip", null);
	
	IClassFile cf = root.getPackageFragment("p1.p2").getClassFile("A.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p1.p2;\n" +
		"\n" +
		"public class A {}",
		cf.getSource());
		
	cf = root.getPackageFragment("").getClassFile("B.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"public class B {}",
		cf.getSource());		

	this.attachSource(root, null, null); // detach source
	root.close();
}
/**
 * Attach a jar with a source attachement that doesn't contain the source folders
 */
public void testRootPath7() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(this.getFile("/AttachSourceTests/full.jar"));
	this.attachSource(root, "/AttachSourceTests/src.zip", null);
	
	IClassFile cf = root.getPackageFragment("p1.p2").getClassFile("A.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p1.p2;\n" +
		"\n" +
		"public class A {}",
		cf.getSource());
		
	cf = root.getPackageFragment("").getClassFile("B.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"public class B {}",
		cf.getSource());
		
	cf = root.getPackageFragment("test1").getClassFile("Test.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package test1;\n" +
		"\n" +
		"public class Test {}",
		cf.getSource());				
	
	this.attachSource(root, null, null); // detach source
	root.close();
}
/**
 * Attach a jar with a source attachement that contains the source folders
 */
public void testRootPath8() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(this.getFile("/AttachSourceTests/full.jar"));
	this.attachSource(root, "/AttachSourceTests/fullsrc.zip", null);
	
	IClassFile cf = root.getPackageFragment("p1.p2").getClassFile("A.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p1.p2;\n" +
		"\n" +
		"public class A {}",
		cf.getSource());
		
	cf = root.getPackageFragment("").getClassFile("B.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"public class B {}",
		cf.getSource());
		
	cf = root.getPackageFragment("test1").getClassFile("Test.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package test1;\n" +
		"\n" +
		"public class Test {}",
		cf.getSource());				
	
	this.attachSource(root, null, null); // detach source
	root.close();
}
/**
 * Attach a jar with a source attachement that contains the source folders
 */
public void testRootPath9() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(this.getFile("/AttachSourceTests/full.jar"));
	this.attachSource(root, "/AttachSourceTests/fullsrc.zip", "invalid");
	
	IClassFile cf = root.getPackageFragment("p1.p2").getClassFile("A.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p1.p2;\n" +
		"\n" +
		"public class A {}",
		cf.getSource());
		
	cf = root.getPackageFragment("").getClassFile("B.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"public class B {}",
		cf.getSource());
		
	cf = root.getPackageFragment("test1").getClassFile("Test.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package test1;\n" +
		"\n" +
		"public class Test {}",
		cf.getSource());				
	
	this.attachSource(root, null, null); // detach source
	root.close();
}
/**
 * Attach a jar with a source attachement that is itself
 */
public void testRootPath10() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(this.getFile("/AttachSourceTests/test2.jar"));
	this.attachSource(root, "/AttachSourceTests/test2.jar", null);
	
	IClassFile cf = root.getPackageFragment("p").getClassFile("X.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p;\n" +
		"\n" +
		"public class X {\n" +
		"\n" +
		"	public static void main(String[] args) {\n" +
		"	}\n" +
		"}",
		cf.getSource());
	this.attachSource(root, null, null); // detach source
	root.close();
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=35965
 */
public void testRootPath11() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(this.getFile("/AttachSourceTests/test4.jar"));
	this.attachSource(root, "/AttachSourceTests/test4_src.zip", null);
	
	IClassFile cf = root.getPackageFragment("P1").getClassFile("D.class");
	assertSourceEquals(
		"Unexpected source for class file P1.D",
		"package P1;\n" +
		"\n" +
		"public class D {}",
		cf.getSource());

	cf = root.getPackageFragment("P1.p2").getClassFile("A.class");
	assertSourceEquals(
		"Unexpected source for class file P1.p2.A",
		"package P1.p2;\n" +
		"\n" +
		"public class A {}",
		cf.getSource());	

	assertTrue("Not a binary root", root.getKind() == IPackageFragmentRoot.K_BINARY);
	assertEquals("wrong jdk level", ClassFileConstants.JDK1_2, Util.getJdkLevel(root.getResource()));
	this.attachSource(root, null, null); // detach source
	root.close();
}
/**
 * Attach a jar with a source attachement that is itself. The jar contains 2 root paths for the same class file.
 * (regression test for bug 74014 prefix path for source attachements - automatic detection does not seem to work)
 */
public void testRootPath12() throws JavaModelException {
	IJavaProject project = this.getJavaProject("/AttachSourceTests");
	IPackageFragmentRoot root = project.getPackageFragmentRoot(this.getFile("/AttachSourceTests/test5.jar"));
	attachSource(root, "/AttachSourceTests/test5.jar", null);
	
	IClassFile cf = root.getPackageFragment("p1.p2").getClassFile("X.class");
	assertSourceEquals(
		"Unexpected source for class file",
		"package p1.p2;\n" +
		"public class X {\n" +
		"}\n",
		cf.getSource());
	attachSource(root, null, null); // detach source
	root.close();
}

}
