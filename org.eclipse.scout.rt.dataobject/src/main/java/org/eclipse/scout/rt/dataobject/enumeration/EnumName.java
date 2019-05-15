/*******************************************************************************
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.dataobject.enumeration;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to define the unique enum name for a {@link IEnum}.
 * <p>
 * Example:
 *
 * <pre>
 * &#64;EnumName("scout.ExampleStatus")
 * &#64;EnumVersion("scout-8.0.0.001")
 * public enum ExampleStatus implements IEnum {
 *   ...
 * }
 * </pre>
 *
 * @see IEnum
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE})
@Inherited
public @interface EnumName {

  /**
   * Unique enum name
   */
  String value();
}
