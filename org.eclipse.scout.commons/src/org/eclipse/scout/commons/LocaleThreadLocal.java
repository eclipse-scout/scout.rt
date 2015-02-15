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

import org.eclipse.scout.commons.nls.NlsLocale;

/**
 * @deprecated use {@link NlsLocale} instead; will be removed in 5.2.0;
 */
@Deprecated
public final class LocaleThreadLocal {

  private LocaleThreadLocal() {
  }

  /**
   * @deprecated use {@link NlsLocale} instead; will be removed in 5.2.0;
   */
  @Deprecated
  public static Locale get() {
    return NlsLocale.get();
  }

  /**
   * @deprecated use {@link NlsLocale} instead; will be removed in 5.2.0;
   */
  @Deprecated
  public static Locale get(boolean useDefaultLocale) {
    return NlsLocale.get(useDefaultLocale);
  }

  /**
   * @deprecated use {@link NlsLocale} instead; will be removed in 5.2.0;
   */
  @Deprecated
  public static void set(Locale locale) {
    NlsLocale.CURRENT.set(locale);
  }
}
