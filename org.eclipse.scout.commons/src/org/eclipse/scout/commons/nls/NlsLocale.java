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

import java.text.spi.DateFormatProvider;
import java.text.spi.NumberFormatProvider;
import java.util.Locale;

import org.eclipse.scout.commons.LocaleThreadLocal;

/**
 * @deprecated Will be removed in release 3.9.0 and is to be replaced by {@link Locale} or {@link LocaleThreadLocal}.
 *             On client side, it is typically replaced by {@link Locale#getDefault()} or
 *             {@link Locale#setDefault(Locale)}, on server side by {@link LocaleThreadLocal#get()} or
 *             {@link LocaleThreadLocal#set(Locale)}, respectively. This is because on server side, the default
 *             {@link Locale} must be bound to the current thread.
 *             <p>
 *             To support a custom locale (e.g. en_CH), do the following:
 *             <ol>
 *             <li>Create a plain Java project</li>
 *             <li>Create two classes that inherit from {@link DateFormatProvider} and {@link NumberFormatProvider} and
 *             implement the method stubs specific to your locale</li>
 *             <li>Create the folder META-INF/services with two files java.text.spi.DateFormatProvider and
 *             java.text.spi.NumberFormatProvider</li>
 *             <li>In those files, simply put the the fully qualified name to your date/number providers</li>
 *             <li>Export project as JAR file and put it into \lib\ext of your JRE</li>
 *             </ol>
 *             </p>
 */
@Deprecated
public final class NlsLocale {

  public Locale getLocale() {
    return NlsUtility.getDefaultLocale();
  }

  @Override
  public int hashCode() {
    return getLocale().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof NlsLocale && ((NlsLocale) o).getLocale().equals(this.getLocale()));
  }

  @Override
  public String toString() {
    return getLocale().toString();
  }

  /**
   * @return
   * @deprecated Will be removed in release 3.9.0, use {@link Locale#getDefault()} or {@link LocaleThreadLocal#get()}
   *             instead.<br/>
   *             Please note that {@link Locale#getDefault()} returns the locale for this instance of the Java Virtual
   *             Machine.
   *             Typically, this is only meaningful on client side. On server side, use {@link LocaleThreadLocal#get()}
   *             instead.
   */
  @Deprecated
  public static NlsLocale getDefault() {
    return new NlsLocale();
  }

  /**
   * @param l
   * @deprecated Will be removed in release 3.9.0, use {@link Locale#setDefault(Locale)} instead.<br/>
   *             Please note that this changes the default locale for this instance of the Java Virtual Machine.
   *             Typically, this is only meaningful on client side. On server side, use
   *             {@link LocaleThreadLocal#set(Locale)} instead.
   */
  @Deprecated
  public static void setDefault(NlsLocale l) {
    Locale.setDefault(l.getLocale());
  }

  /**
   * @param l
   * @deprecated Will be removed in release 3.9.0, use {@link LocaleThreadLocal#set(Locale)} instead.<br/>
   */
  @Deprecated
  public static void setThreadDefault(NlsLocale l) {
    LocaleThreadLocal.set(l.getLocale());
  }
}
