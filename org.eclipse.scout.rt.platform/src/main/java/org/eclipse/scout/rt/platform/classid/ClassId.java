/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.classid;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.platform.annotations.DtoRelevant;

/**
 * Assigns an id to a class that should be <b>unique</b>.
 * <p>
 * It is sometimes necessary to identify a class by a unique id other than the class name. E.g. an id for a scout model
 * entity could be used in test tools and should therefore not change when a class is moved to another package or
 * another place in the inner class hierarchy.
 * <p>
 * 
 * @see org.eclipse.scout.rt.platform.classid.ITypeWithClassId
 */
@DtoRelevant
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClassId {
  /** unique id */
  String value();
}
