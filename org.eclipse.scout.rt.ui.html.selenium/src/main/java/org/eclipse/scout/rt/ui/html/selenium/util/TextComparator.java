/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
