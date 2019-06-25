package org.eclipse.scout.rt.jackson.dataobject;

import java.text.DateFormatSymbols;
import java.util.Locale;

/**
 * <i>Use {@link org.eclipse.scout.rt.platform.util.date.StrictSimpleDateFormat} instead. This class will be removed in
 * Scout 10.</i>
 */
public class StrictSimpleDateFormat extends org.eclipse.scout.rt.platform.util.date.StrictSimpleDateFormat {
  private static final long serialVersionUID = 1L;

  public StrictSimpleDateFormat() {
    super();
  }

  public StrictSimpleDateFormat(String pattern) {
    super(pattern);
  }

  public StrictSimpleDateFormat(String pattern, DateFormatSymbols formatSymbols) {
    super(pattern, formatSymbols);
  }

  public StrictSimpleDateFormat(String pattern, Locale locale) {
    super(pattern, locale);
  }
}
