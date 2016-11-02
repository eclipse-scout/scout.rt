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
package org.eclipse.scout.rt.platform.util;

public final class BooleanUtility {

  private BooleanUtility() {
  }

  /**
   * @return {@code b} if not {@code null} otherwise {@code false}.
   */
  public static boolean nvl(Boolean b) {
    return nvl(b, false);
  }

  /**
   * @return {@code b} if not {@code null} otherwise {@code defaultValue}.
   */
  public static boolean nvl(Boolean b, boolean defaultValue) {
    if (b == null) {
      return defaultValue;
    }
    return b.booleanValue();
  }
}
