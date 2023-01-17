/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.nls;

import java.util.Locale;

/**
 * This class represents the <code>NLS-Locale</code> currently associated with the current thread.
 */
public final class NlsLocale {

  /**
   * The {@link Locale} which is currently associated with the current thread.
   */
  public static final ThreadLocal<Locale> CURRENT = new ThreadLocal<>();

  private NlsLocale() {
  }

  /**
   * Returns the {@link Locale} which is currently associated with the current thread or {@link Locale#getDefault()} if
   * not set.
   */
  public static Locale get() {
    return getOrElse(Locale.getDefault());
  }

  /**
   * @param defaultLocale
   *          {@link #CURRENT} locale or default, if <code>null</code>
   * @return locale of current thread or the default
   */
  public static Locale getOrElse(Locale defaultLocale) {
    final Locale locale = CURRENT.get();
    if (locale != null) {
      return locale;
    }
    return defaultLocale;
  }

  /**
   * Associates the current thread with the given {@link Locale}.
   */
  public static void set(final Locale locale) {
    if (locale == null) {
      CURRENT.remove();
    }
    else {
      CURRENT.set(locale);
    }
  }
}
