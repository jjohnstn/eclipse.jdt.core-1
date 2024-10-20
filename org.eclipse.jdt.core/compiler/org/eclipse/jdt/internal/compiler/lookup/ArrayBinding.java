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
package org.eclipse.jdt.internal.compiler.lookup;

import java.util.Map;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.impl.Constant;

public final class ArrayBinding extends TypeBinding {
	// creation and initialization of the length field
	// the declaringClass of this field is intentionally set to null so it can be distinguished.
	public static final FieldBinding ArrayLength = new FieldBinding(LENGTH, IntBinding, AccPublic | AccFinal, null, Constant.NotAConstant);

	public TypeBinding leafComponentType;
	public int dimensions;
	LookupEnvironment environment;	
	char[] constantPoolName;
	char[] genericTypeSignature;
	
public ArrayBinding(TypeBinding type, int dimensions, LookupEnvironment environment) {
	this.tagBits |= IsArrayType;
	this.leafComponentType = type;
	this.dimensions = dimensions;
	this.environment = environment;
	if (type instanceof UnresolvedReferenceBinding)
		((UnresolvedReferenceBinding) type).addWrapper(this);
	else
    	this.tagBits |= type.tagBits & (HasTypeVariable | HasDirectWildcard);
}

/**
 * Collect the substitutes into a map for certain type variables inside the receiver type
 * e.g.   Collection<T>.collectSubstitutes(Collection<List<X>>, Map), will populate Map with: T --> List<X>
 */
public void collectSubstitutes(Scope scope, TypeBinding otherType, Map substitutes, int constraint) {
	
	if ((this.tagBits & TagBits.HasTypeVariable) == 0) return;
	if (otherType == NullBinding) return;
	
	switch(otherType.kind()) {
		case Binding.ARRAY_TYPE :
	        int otherDim = otherType.dimensions();
	        if (otherDim == this.dimensions) {
			    this.leafComponentType.collectSubstitutes(scope, otherType.leafComponentType(), substitutes, constraint);
	        } else if (otherDim > this.dimensions) {
	            ArrayBinding otherReducedType = this.environment.createArrayType(otherType.leafComponentType(), otherDim - this.dimensions);
	            this.leafComponentType.collectSubstitutes(scope, otherReducedType, substitutes, constraint);
	        }
			break;
		case Binding.TYPE_PARAMETER :
			//TypeVariableBinding variable = (TypeVariableBinding) otherType;
			// TODO (philippe) should consider array bounds, and recurse
			break;
	}
}

/*
 * brakets leafUniqueKey
 * p.X[][] --> [[Lp/X;
 */
public char[] computeUniqueKey(boolean isLeaf) {
	char[] brackets = new char[dimensions];
	for (int i = dimensions - 1; i >= 0; i--) brackets[i] = '[';
	return CharOperation.concat(brackets, this.leafComponentType.computeUniqueKey(isLeaf));
 }
	
/**
 * Answer the receiver's constant pool name.
 * NOTE: This method should only be used during/after code gen.
 * e.g. '[Ljava/lang/Object;'
 */
public char[] constantPoolName() {
	if (constantPoolName != null)
		return constantPoolName;

	char[] brackets = new char[dimensions];
	for (int i = dimensions - 1; i >= 0; i--) brackets[i] = '[';
	return constantPoolName = CharOperation.concat(brackets, leafComponentType.signature());
}
public String debugName() {
	StringBuffer brackets = new StringBuffer(dimensions * 2);
	for (int i = dimensions; --i >= 0;)
		brackets.append("[]"); //$NON-NLS-1$
	return leafComponentType.debugName() + brackets.toString();
}
public int dimensions() {
	return this.dimensions;
}

/* Answer an array whose dimension size is one less than the receiver.
*
* When the receiver's dimension size is one then answer the leaf component type.
*/

public TypeBinding elementsType() {
	if (this.dimensions == 1) return this.leafComponentType;
	return this.environment.createArrayType(this.leafComponentType, this.dimensions - 1);
}
/**
 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#erasure()
 */
public TypeBinding erasure() {
    TypeBinding erasedType = this.leafComponentType.erasure();
    if (this.leafComponentType != erasedType)
        return this.environment.createArrayType(erasedType, this.dimensions);
    return this;
}
public LookupEnvironment environment() {
    return this.environment;
}

/**
 * Find supertype which erases to a given type, or null if not found
 */
public TypeBinding findSuperTypeWithSameErasure(TypeBinding otherType) {

	if (this == otherType) return this;
	int otherDim = otherType.dimensions();
	if (this.dimensions != otherDim) {
		switch(otherType.id) {
			case T_JavaLangObject :
			case T_JavaIoSerializable :
			case T_JavaLangCloneable :
				return otherType;
		}
		if (otherDim < this.dimensions & otherType.leafComponentType().id == T_JavaLangObject) {
			return otherType; // X[][] has Object[] as an implicit supertype
		}
		return null;
	}
	if (!(this.leafComponentType instanceof ReferenceBinding)) return null;
	TypeBinding leafSuperType = ((ReferenceBinding)this.leafComponentType).findSuperTypeWithSameErasure(otherType.leafComponentType());
	if (leafSuperType == null) return null;
	return environment().createArrayType(leafSuperType, this.dimensions);	
}

public char[] genericTypeSignature() {
	
    if (this.genericTypeSignature == null) {
		char[] brackets = new char[dimensions];
		for (int i = dimensions - 1; i >= 0; i--) brackets[i] = '[';
		this.genericTypeSignature = CharOperation.concat(brackets, leafComponentType.genericTypeSignature());
    }
    return this.genericTypeSignature;
}

public PackageBinding getPackage() {
	return leafComponentType.getPackage();
}

public int hashCode() {
	return this.leafComponentType == null ? super.hashCode() : this.leafComponentType.hashCode();
}

/* Answer true if the receiver type can be assigned to the argument type (right)
*/
public boolean isCompatibleWith(TypeBinding otherType) {
	if (this == otherType)
		return true;

	switch (otherType.kind()) {
		case Binding.ARRAY_TYPE :
			ArrayBinding otherArray = (ArrayBinding) otherType;
			if (otherArray.leafComponentType.isBaseType())
				return false; // relying on the fact that all equal arrays are identical
			if (dimensions == otherArray.dimensions)
				return leafComponentType.isCompatibleWith(otherArray.leafComponentType);
			if (dimensions < otherArray.dimensions)
				return false; // cannot assign 'String[]' into 'Object[][]' but can assign 'byte[][]' into 'Object[]'
			break;
		case Binding.BASE_TYPE :
			return false;
		case Binding.WILDCARD_TYPE :
		    return ((WildcardBinding) otherType).boundCheck(this);
		    
		case Binding.TYPE_PARAMETER :
			// check compatibility with capture of ? super X
			if (otherType.isCapture()) {
				CaptureBinding otherCapture = (CaptureBinding) otherType;
				TypeBinding otherLowerBound;
				if ((otherLowerBound = otherCapture.lowerBound) != null) {
					if (!otherLowerBound.isArrayType()) return false;					
					return this.isCompatibleWith(otherLowerBound);
				}
			}
			return false;

	}
	//Check dimensions - Java does not support explicitly sized dimensions for types.
	//However, if it did, the type checking support would go here.
	switch (otherType.leafComponentType().id) {
	    case T_JavaLangObject :
	    case T_JavaLangCloneable :
	    case T_JavaIoSerializable :
	        return true;
	}
	return false;
}

public int kind() {
	return ARRAY_TYPE;
}

public TypeBinding leafComponentType(){
	return leafComponentType;
}

/* API
* Answer the problem id associated with the receiver.
* NoError if the receiver is a valid binding.
*/
public int problemId() {
	return leafComponentType.problemId();
}
/**
* Answer the source name for the type.
* In the case of member types, as the qualified name from its top level type.
* For example, for a member type N defined inside M & A: "A.M.N".
*/

public char[] qualifiedSourceName() {
	char[] brackets = new char[dimensions * 2];
	for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
		brackets[i] = ']';
		brackets[i - 1] = '[';
	}
	return CharOperation.concat(leafComponentType.qualifiedSourceName(), brackets);
}
public char[] readableName() /* java.lang.Object[] */ {
	char[] brackets = new char[dimensions * 2];
	for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
		brackets[i] = ']';
		brackets[i - 1] = '[';
	}
	return CharOperation.concat(leafComponentType.readableName(), brackets);
}
public char[] shortReadableName(){
	char[] brackets = new char[dimensions * 2];
	for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
		brackets[i] = ']';
		brackets[i - 1] = '[';
	}
	return CharOperation.concat(leafComponentType.shortReadableName(), brackets);
}
public char[] sourceName() {
	char[] brackets = new char[dimensions * 2];
	for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
		brackets[i] = ']';
		brackets[i - 1] = '[';
	}
	return CharOperation.concat(leafComponentType.sourceName(), brackets);
}
public void swapUnresolved(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType, LookupEnvironment env) {
	if (this.leafComponentType == unresolvedType) {
		this.leafComponentType = env.convertToRawType(resolvedType);
		this.tagBits |= this.leafComponentType.tagBits & (HasTypeVariable | HasDirectWildcard);
	}
}
public String toString() {
	return leafComponentType != null ? debugName() : "NULL TYPE ARRAY"; //$NON-NLS-1$
}
}
