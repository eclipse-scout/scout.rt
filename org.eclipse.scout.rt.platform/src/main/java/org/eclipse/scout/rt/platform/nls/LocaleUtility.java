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
package org.eclipse.scout.rt.platform.nls;

import java.util.Locale;

public final class LocaleUtility {
  private LocaleUtility() {
  }

  /**
   * @return the parsed locale such as created by {@link Locale#toString()}
   */
  public static Locale parse(String s) {
    if (s == null || s.isEmpty()) {
      return null;
    }
    int a = s.indexOf('_');
    int b = (a >= 0 && a + 1 < s.length() ? s.indexOf('_', a + 1) : -1);
    if (a >= 0 && b >= 0) {
      return new Locale(s.substring(0, a), s.substring(a + 1, b), s.substring(b + 1));
    }
    if (a >= 0) {
      return new Locale(s.substring(0, a), s.substring(a + 1));
    }
    return new Locale(s);
  }
}
