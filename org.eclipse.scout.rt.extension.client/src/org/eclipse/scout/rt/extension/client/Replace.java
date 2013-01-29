/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation on scout configuration object used to replace other configured objects by the annotated one. By default
 * the first object is removed which type is the annotated class's super class.
 * 
 * @since 3.9.0
 * @deprecated Do not use this class any more. It has been replaced by
 *             {@link org.eclipse.scout.commons.annotations.Replace}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Deprecated
public @interface Replace {

  /**
   * @return Returns the type of the object to be replaced by an instance of the annotated class.
   */
  Class value() default Object.class;
}
