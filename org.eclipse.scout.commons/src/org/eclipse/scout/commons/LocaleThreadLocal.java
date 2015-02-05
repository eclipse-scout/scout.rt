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
  private static final ThreadLocal<Locale> THREAD_LOCAL = new ThreadLocal<Locale>();

  private LocaleThreadLocal() {
  }

  /**
   * Same as {@link #get(boolean)} with the argument <code>true</code>.
   * <p>
   * Note: If you want to backup the current value before calling {@link #set(Locale)}, you should <b>not</b> use this
   * method, but use {@link #get(boolean)} with the argument <code>false</code> instead.
   */
  public static Locale get() {
    return get(true);
  }

  /**
   * Returns the {@link Locale} that was previously stored for the current thread using {@link #set(Locale)}.
   *
   * @param useDefaultLocale
   *          If this argument is <code>true</code> and no value was found in the ThreadLocal, the result of
   *          {@link Locale#getDefault()} is returned. Otherwise, <code>null</code> is returned.
   */
  public static Locale get(boolean useDefaultLocale) {
    Locale l = THREAD_LOCAL.get();
    if (l == null && useDefaultLocale) {
      l = Locale.getDefault();
    }
    return l;
  }

  public static void set(Locale l) {
    THREAD_LOCAL.set(l);
  }
}
