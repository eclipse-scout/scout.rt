/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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

  static class StartsWith extends TextComparator {

    @Override
    boolean compare(String expected, String actual) {
      return actual.startsWith(expected);
    }
  }
}
