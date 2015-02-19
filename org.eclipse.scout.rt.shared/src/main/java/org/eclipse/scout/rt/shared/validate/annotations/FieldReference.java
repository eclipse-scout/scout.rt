/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.validate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * In order to annotate fields that are not accessible (for example in subclassed types) this annotation can be placed
 * on a phantom field
 * together with validations. These validations are then applied to the referenced field instead.
 * <p>
 * Example:
 * 
 * <pre>
 * class A {
 *   private String m_name;
 * }
 * 
 * class B extends A {
 *   &#064;MaxLength(5000000)
 *   &#064;FieldReference(&quot;m_name&quot;)
 *   private static final int M_NAME_REF = 0;
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface FieldReference {
  String value();
}
