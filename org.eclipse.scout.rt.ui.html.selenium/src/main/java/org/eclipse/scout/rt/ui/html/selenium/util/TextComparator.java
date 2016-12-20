package org.eclipse.scout.rt.ui.html.selenium.util;

/**
 * A few text comparator implementations to be used with {@link SeleniumExpectedConditions}.
 */
@SuppressWarnings("squid:S00118")
abstract class TextComparator {

  abstract boolean compare(String expected, String actual);

  static class Equals extends TextComparator {

    @Override
    boolean compare(String expected, String actual) {
      return expected.equals(actual);
    }
  }

  static class EqualsIgnoreCase extends TextComparator {

    @Override
    boolean compare(String expected, String actual) {
      return expected.equalsIgnoreCase(actual);
    }
  }

  static class Contains extends TextComparator {

    @Override
    boolean compare(String expected, String actual) {
      return actual.contains(expected);
    }
  }
}
