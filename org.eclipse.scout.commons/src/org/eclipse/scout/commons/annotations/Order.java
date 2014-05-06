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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Assigns an absolute order to a type. To remove all Order-annotations theses
 * regular expressions can be used: search for: (@Order\()(\d+)(\.0f) replace
 * with: $1$2 search for: (@Order\()(\d+)(\.\d+)f replace with: $1$2$3
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {
  /**
   * @return The type's absolute order.
   */
  double value();
}
