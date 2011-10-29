package org.eclipse.scout.rt.shared;

import java.util.Locale;

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.nls.NlsUtility;

public class TEXTS {

  /**
   * Gets the translation for the given key and the default locale.<br>
   * The default locale is defined as the locale set on the current thread or <code>Locale.getLocale()</code> if the
   * thread locale is null.
   * 
   * @param key
   *          The nls text key
   * @return The translation.
   * @see ScoutTexts
   * @see NlsUtility
   * @see LocaleThreadLocal
   * @see Locale
   */
  public static String get(String key) {
    return ScoutTexts.getInstance().getText(key);
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
   * <li>TEXTS.get("MissingFile3", fileName, dir); with MissingFile3="The File {0} in Folder {1} could not be found."</li>
   * </ul>
   * 
   * @param key
   *          The nls text key
   * @param messageArguments
   *          The arguments (can be null) to replace in the returned text.
   * @return
   * @see ScoutTexts
   * @see NlsUtility
   * @see LocaleThreadLocal
   * @see Locale
   */
  public static String get(String key, String... messageArguments) {
    return ScoutTexts.getInstance().getText(key, messageArguments);
  }

  /**
   * Gets the translation for the given key and locale.<br>
   * The translation might contain variables like {0},{1},{2},... In that case, they are replaced with the given
   * argument list.<br>
   * Examples: <br>
   * <ul>
   * <li>TEXTS.get(locale, "MissingFile1"); with MissingFile1="The File could not be found."</li>
   * <li>TEXTS.get(locale, "MissingFile2", fileName); with MissingFile2="The File {0} could not be found."</li>
   * <li>TEXTS.get(locale, "MissingFile3", fileName, dir); with
   * MissingFile3="The File {0} in Folder {1} could not be found."</li>
   * </ul>
   * 
   * @param locale
   *          The locale of the text
   * @param key
   *          The nls text key
   * @param messageArguments
   *          The arguments (can be null) to replace in the returned text.
   * @return
   * @see ScoutTexts
   * @see Locale
   */
  public static String get(Locale locale, String key, String... messageArguments) {
    return ScoutTexts.getInstance().getText(locale, key, messageArguments);
  }
}
