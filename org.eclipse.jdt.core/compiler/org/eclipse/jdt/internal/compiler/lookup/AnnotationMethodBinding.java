/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

/**
 * Annotation method that came from binary or source
 * @author tyeung
 *
 */
public class AnnotationMethodBinding extends MethodBinding 
{
	private static Object NO_RESOLVED_VALUE = new Object();
	private Object defaultValue = null;
	/**
	 * 
	 * @param modifiers
	 * @param selector
	 * @param returnType
	 * @param declaringClass	
	 * @param defaultValue <code>null</code> for source method. 
	 */
	public AnnotationMethodBinding(int modifiers,
								   char[] selector, 
								   TypeBinding returnType, 
								   ReferenceBinding declaringClass,								  
								   Object defaultValue)
	{
		super(modifiers, selector, returnType, NoParameters, NoExceptions, declaringClass );
		this.defaultValue = defaultValue;
	}
	
	public void setDefaultValue()
	{			
		if (this.declaringClass instanceof SourceTypeBinding) {
			final SourceTypeBinding srcType = (SourceTypeBinding)this.declaringClass;
			TypeDeclaration typeDecl = srcType.scope.referenceContext;
			final AbstractMethodDeclaration methodDecl = typeDecl.declarationOf(this);
			if( methodDecl instanceof AnnotationMethodDeclaration){
				final AnnotationMethodDeclaration annotationMethodDecl = (AnnotationMethodDeclaration)methodDecl;				
				this.defaultValue = SourceElementValuePair.getValue(annotationMethodDecl.defaultValue);
				if( this.defaultValue == null )
					this.defaultValue = NO_RESOLVED_VALUE;
			}
		}
	}
	
	/**
	 * @return the default value for this annotation method.
	 *         Return <code>null</code> if there is no default value 
	 */
	public Object getDefaultValue()
	{
		if(this.defaultValue == NO_RESOLVED_VALUE )
			return null;
		else if(this.defaultValue != null)
			return this.defaultValue;
		else{
			if (this.declaringClass instanceof SourceTypeBinding) {			
				final SourceTypeBinding srcType = (SourceTypeBinding)this.declaringClass;
				// we have already cut the AST. 
				// default value is either already resolved or it is null.
				if( srcType.scope == null ){
					this.defaultValue = NO_RESOLVED_VALUE;
					return null;
				}
				TypeDeclaration typeDecl = ((SourceTypeBinding)this.declaringClass).scope.referenceContext;
				final AbstractMethodDeclaration methodDecl = typeDecl.declarationOf(this);
				if( methodDecl instanceof AnnotationMethodDeclaration){				
					final AnnotationMethodDeclaration annotationMethodDecl = (AnnotationMethodDeclaration)methodDecl;
					annotationMethodDecl.resolveStatements();
				}
			}
		}
		return this.defaultValue;
	}	
}
