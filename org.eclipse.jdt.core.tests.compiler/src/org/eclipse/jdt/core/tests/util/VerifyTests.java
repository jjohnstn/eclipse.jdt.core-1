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
package org.eclipse.jdt.core.tests.util;

import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.util.*;

/******************************************************
 * 
 * IMPORTANT NOTE: If modifying this class, copy the source to TestVerifier#getVerifyTestsCode()
 * (see this method for details)
 * 
 ******************************************************/

public class VerifyTests {
	int portNumber;
	Socket socket;

/**
 * NOTE: Code copied from junit.util.TestCaseClassLoader.
 *
 * A custom class loader which enables the reloading
 * of classes for each test run. The class loader
 * can be configured with a list of package paths that
 * should be excluded from loading. The loading
 * of these packages is delegated to the system class
 * loader. They will be shared across test runs.
 * <p>
 * The list of excluded package paths is specified in
 * a properties file "excluded.properties" that is located in 
 * the same place as the TestCaseClassLoader class.
 * <p>
 * <b>Known limitation:</b> the VerifyClassLoader cannot load classes
 * from jar files.
 */


public class VerifyClassLoader extends ClassLoader {
	/** scanned class path */
	private String[] fPathItems;
	
	/** excluded paths */
	private String[] fExcluded= {};

	/**
	 * Constructs a VerifyClassLoader. It scans the class path
	 * and the excluded package paths
	 */
	public VerifyClassLoader() {
		super();
		String classPath= System.getProperty("java.class.path");
		String separator= System.getProperty("path.separator");
		
		// first pass: count elements
		StringTokenizer st= new StringTokenizer(classPath, separator);
		int i= 0;
		while (st.hasMoreTokens()) {
			st.nextToken();
			i++;
		}
		// second pass: split
		fPathItems= new String[i];
		st= new StringTokenizer(classPath, separator);
		i= 0;
		while (st.hasMoreTokens()) {
			fPathItems[i++]= st.nextToken();
		}

	}
	public java.net.URL getResource(String name) {
		return ClassLoader.getSystemResource(name);
	}
	public InputStream getResourceAsStream(String name) {
		return ClassLoader.getSystemResourceAsStream(name);
	}
	protected boolean isExcluded(String name) {
		// exclude the "java" packages.
		// They always need to be excluded so that they are loaded by the system class loader
		if (name.startsWith("java"))
			return true;
			
		// exclude the user defined package paths
		for (int i= 0; i < fExcluded.length; i++) {
			if (name.startsWith(fExcluded[i])) {
				return true;
			}
		}
		return false;	
	}
	public synchronized Class loadClass(String name, boolean resolve)
		throws ClassNotFoundException {
			
		Class c= findLoadedClass(name);
		if (c != null)
			return c;
		//
		// Delegate the loading of excluded classes to the
		// standard class loader.
		//
		if (isExcluded(name)) {
			try {
				c= findSystemClass(name);
				return c;
			} catch (ClassNotFoundException e) {
				// keep searching
			}
		}
		File file= locate(name);
		if (file == null)
			throw new ClassNotFoundException();
		byte data[]= loadClassData(file);
		c= defineClass(name, data, 0, data.length);
		if (resolve) 
			resolveClass(c);
		return c;
	}
	private byte[] loadClassData(File f) throws ClassNotFoundException {
		try {
			//System.out.println("loading: "+f.getPath());
			FileInputStream stream= new FileInputStream(f);
			
			try {
				byte[] b= new byte[stream.available()];
				stream.read(b);
				stream.close();
				return b;
			}
			catch (IOException e) {
				throw new ClassNotFoundException();
			}
		}
		catch (FileNotFoundException e) {
			throw new ClassNotFoundException();
		}
	}
	/**
	 * Locate the given file.
	 * @return Returns null if file couldn't be found.
	 */
	private File locate(String fileName) { 
		fileName= fileName.replace('.', '/')+".class";
		File path= null;
		
		if (fileName != null) {
			for (int i= 0; i < fPathItems.length; i++) {
				path= new File(fPathItems[i], fileName);
				if (path.exists())
					return path;
			}
		}
		return null;
	}
}
	
public void loadAndRun(String className) throws Throwable {
	//System.out.println("Loading " + className + "...");
	Class testClass = new VerifyClassLoader().loadClass(className);
	//System.out.println("Loaded " + className);
	try {
		Method main = testClass.getMethod("main", new Class[] {String[].class});
		//System.out.println("Running " + className);
		main.invoke(null, new Object[] {new String[] {}});
		//System.out.println("Finished running " + className);
	} catch (NoSuchMethodException e) {
		return;
	} catch (InvocationTargetException e) {
		throw e.getTargetException();
	}
}
public static void main(String[] args) throws IOException {
	VerifyTests verify = new VerifyTests();
	verify.portNumber = Integer.parseInt(args[0]);
	verify.run();
}
public void run() throws IOException {
	ServerSocket server = new ServerSocket(this.portNumber);
	this.socket = server.accept();
	this.socket.setTcpNoDelay(true);
	server.close();

	DataInputStream in = new DataInputStream(this.socket.getInputStream());
	final DataOutputStream out = new DataOutputStream(this.socket.getOutputStream());
	while (true) {
		final String className = in.readUTF();
		Thread thread = new Thread() {
			public void run() {
				try {
					loadAndRun(className);
					out.writeBoolean(true);
					System.err.println(VerifyTests.class.getName());
					System.out.println(VerifyTests.class.getName());
				} catch (Throwable e) {
					e.printStackTrace();
					try {
						System.err.println(VerifyTests.class.getName());
						System.out.println(VerifyTests.class.getName());
						out.writeBoolean(false);
					} catch (IOException e1) {
						// ignore
					}
				}
			}
		};
		thread.start();
	}
}
}
