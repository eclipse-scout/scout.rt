package org.eclipse.scout.rt.shared;

import java.util.Locale;

public class TEXTS {
  public static String get(String key, String... messageArguments) {
    return ScoutTexts.getInstance().getText(key, messageArguments);
  }

  public static String get(Locale locale, String key, String... messageArguments) {
    return ScoutTexts.getInstance().getText(locale, key, messageArguments);
  }
}
