/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.dataobject;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to specify a custom name for a {@link DoEntity} attribute, used by serializer/deserializer while
 * converting the attribute name from/to a string value.
 */
@Documented
@Retention(RUNTIME)
@Target({ElementType.METHOD})
public @interface AttributeName {

  /**
   * Custom name for a {@link DoEntity} attribute, used by serializer/deserializer while converting the attribute name
   * from/to string value.
   * <p>
   * <b>The annotation value must match the string literal used to call the {@link DoEntity#doValue(String)} or
   * {@link DoEntity#doList(String)} attribute creation method.</b>
   * <p>
   * For example a {@link DoEntity} could use the following accessor method to specify an attribute with a custom name
   * for serialization.
   *
   * <pre>
   * &#64;AttributeName("customNameForAttributeAll")
   * public DoValue<BigDecimal> allAttribute() {
   *   return doValue("customNameForAttributeAll");
   * }
   * </pre>
   */
  String value();
}
