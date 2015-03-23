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
package org.eclipse.scout.commons.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a priority for a type (for example a service implementation) lowest
 * priority is negative infinity highest priority is positive infinity if a type
 * has no priority annotation, the priority value of 0 is assumed if multiple
 * types are considered, the type with the highest priority is chosen
 * recommended priority values are: -1: use only when no other candidate is
 * found 0: use as first choice (when no annotation is found, 0 is assumed) 1-9:
 * use preferrably 10-99: use explicitly
 * <p>
 * NOTE: this annotation is not sufficient to distinguish between subclassing with "REPLACE" semantics and subclassing
 * with "RE-USE CODE" semantics. Use the {@link Replace} annotation in addition when a "REPLACE" subclassing of a bean
 * is intended. Just delete the prio annotation when "RE-USE CODE" is intended.
 * <p>
 * TODO imo deprecate and remove
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Priority {
  /**
   * @return The type's priority.
   */
  double value();
}
