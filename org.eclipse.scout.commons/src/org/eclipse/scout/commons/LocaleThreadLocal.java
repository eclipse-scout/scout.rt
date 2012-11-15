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
package org.eclipse.scout.commons;

import java.util.Locale;

/**
 * This class represents the nls locale for the current thread and
 * Locale.getLocale() by default.
 */
public final class LocaleThreadLocal {
  private static ThreadLocal<Locale> localeThreadLocal = new ThreadLocal<Locale>();

  private LocaleThreadLocal() {
  }

  public static Locale get() {
    Locale l = localeThreadLocal.get();
    if (l == null) {
      l = Locale.getDefault();
    }
    return l;
  }

  public static void set(Locale l) {
    localeThreadLocal.set(l);
  }

}
