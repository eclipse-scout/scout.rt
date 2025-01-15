/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.text;

import java.util.Locale;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.NlsUtility;

public final class TEXTS {

  private TEXTS() {
  }

  /**
   * Gets the translation for the given key and the default locale.<br>
   * The default locale is defined as the locale set on the current thread or <code>Locale.getLocale()</code> if the
   * thread locale is null.
   *
   * @param key
   *     The nls text key
   * @return The translation.
   * @see ScoutTexts
   * @see NlsUtility
   * @see Locale
   */
  public static String get(@NlsKey String key) {
    return BEANS.get(ScoutTexts.class).getText(key);
  }

  /**
   * Gets the translation for the given key and the default locale.<br>
   * The default locale is defined as the locale set on the current thread or <code>Locale.getLocale()</code> if the
   * thread locale is null.<br>
   * The translation might contain variables like {0},{1},{2},... In that case, they are replaced with the given
   * argument list.<br>
   * Examples: <br>
   * <ul>
   * <li>TEXTS.get("MissingFile1"); with MissingFile1="The File could not be found."</li>
   * <li>TEXTS.get("MissingFile2", fileName); with MissingFile2="The File {0} could not be found."</li>
   * <li>TEXTS.get("MissingFile3", fileName, dir); with MissingFile3="The File {0} in Folder {1} could not be found."
   * </li>
   * </ul>
   *
   * @param key
   *     The nls text key
   * @param messageArguments
   *     The arguments (can be null) to replace in the returned text.
   * @return
   * @see ScoutTexts
   * @see NlsUtility
   * @see Locale
   */
  public static String get(@NlsKey String key, String... messageArguments) {
    return BEANS.get(ScoutTexts.class).getText(key, messageArguments);
  }

  /**
   * Gets the translation for the given key and locale.<br>
   * The translation might contain variables like {0},{1},{2},... In that case, they are replaced with the given
   * argument list.<br>
   * Examples: <br>
   * <ul>
   * <li>TEXTS.get(locale, "MissingFile1"); with MissingFile1="The File could not be found."</li>
   * <li>TEXTS.get(locale, "MissingFile2", fileName); with MissingFile2="The File {0} could not be found."</li>
   * <li>TEXTS.get(locale, "MissingFile3", fileName, dir); with MissingFile3= "The File {0} in Folder {1} could not be
   * found."</li>
   * </ul>
   *
   * @param locale
   *     The locale of the text
   * @param key
   *     The nls text key
   * @param messageArguments
   *     The arguments (can be null) to replace in the returned text.
   * @return
   * @see ScoutTexts
   * @see Locale
   */
  public static String get(Locale locale, @NlsKey String key, String... messageArguments) {
    return BEANS.get(ScoutTexts.class).getText(locale, key, messageArguments);
  }

  /**
   * Gets the translation for the given key and the default locale.<br>
   * The default locale is defined as the locale set on the current thread or <code>Locale.getLocale()</code> if the
   * thread locale is null.
   *
   * @param key
   * @param fallback
   *     The fallback is returned when the text for the given key is undefinded.
   * @return
   */
  public static String getWithFallback(@NlsKey String key, String fallback, String... messageArguments) {
    return getWithFallback(null, key, fallback, messageArguments);
  }

  public static String getWithFallback(Locale locale, @NlsKey String key, String fallback, String... messageArguments) {
    return BEANS.get(ScoutTexts.class).getTextWithFallback(locale, key, fallback, messageArguments);
  }
}
