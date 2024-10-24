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
package org.eclipse.jdt.internal.compiler.impl;

public class LongConstant extends Constant {
	long value;
public LongConstant(long value) {
	this.value = value;
}
public byte byteValue() {
	return (byte) value;
}
public char charValue() {
	return (char) value;
}
public double doubleValue() {
	return value; // implicit cast to return type
}
public float floatValue() {
	return value; // implicit cast to return type
}
public int intValue() {
	return (int) value;
}
public long longValue() {
	return value; 
}
public short shortValue() {
	return (short) value;
}
public String stringValue() {
	return String.valueOf(this.value);
}
public String toString(){

	return "(long)" + value ; } //$NON-NLS-1$
public int typeID() {
	return T_long;
}
}
