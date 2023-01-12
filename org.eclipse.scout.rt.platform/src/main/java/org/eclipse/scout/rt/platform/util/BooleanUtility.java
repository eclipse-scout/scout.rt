/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
