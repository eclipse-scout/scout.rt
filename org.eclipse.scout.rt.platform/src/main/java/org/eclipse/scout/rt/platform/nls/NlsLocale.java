/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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

/**
 * This class represents the <code>NLS-Locale</code> currently associated with the current thread.
 */
public class NlsLocale {

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
    return NlsLocale.get(true);
  }

  /**
   * Returns the {@link Locale} which is currently associated with the current thread.
   *
   * @param defaultIfNotSet
   *          <code>true</code> to return the default JVM {@link Locale} if no {@link Locale} is associated with the
   *          current thread.
   */
  public static Locale get(final boolean defaultIfNotSet) {
    final Locale locale = CURRENT.get();
    if (locale != null) {
      return locale;
    }
    else if (defaultIfNotSet) {
      return Locale.getDefault();
    }
    return null;
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
