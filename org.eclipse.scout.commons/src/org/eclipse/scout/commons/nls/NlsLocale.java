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
package org.eclipse.scout.commons.nls;

import java.util.Locale;

import org.eclipse.scout.commons.LocaleThreadLocal;

/**
 * Sometimes it is useful to distinguish between a nls locale and a
 * formatting/date/currency locale In Switzerland for example some people like
 * to have nls language "en" but formatting locale de_CH This class represents
 * the nls locale
 * 
 * @see Locale.getLocale() for formatting locale
 */
public final class NlsLocale {

  private Locale m_locale;

  public NlsLocale() {
    this(null);
  }

  public NlsLocale(Locale l) {
    if (l == null) {
      l = Locale.getDefault();
    }
    m_locale = l;
  }

  public Locale getLocale() {
    return m_locale;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return m_locale.hashCode();
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    return (o instanceof NlsLocale && ((NlsLocale) o).m_locale.equals(this.m_locale));
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return m_locale.toString();
  }

  private static NlsLocale nlsLocale;
  private static ThreadLocal<NlsLocale> nlsLocaleThreadLocal = new ThreadLocal<NlsLocale>();

  public static NlsLocale getDefault() {
    NlsLocale l = nlsLocaleThreadLocal.get();
    if (l == null) {
      l = nlsLocale;
    }
    if (l == null) {
      Locale formattingLocale = LocaleThreadLocal.get();
      if (formattingLocale != null) {
        l = new NlsLocale(formattingLocale);
      }
    }
    if (l == null) {
      l = new NlsLocale(Locale.getDefault());
    }
    return l;
  }

  public static void setDefault(NlsLocale l) {
    nlsLocale = l;
  }

  public static void setThreadDefault(NlsLocale l) {
    nlsLocaleThreadLocal.set(l);
  }

}
