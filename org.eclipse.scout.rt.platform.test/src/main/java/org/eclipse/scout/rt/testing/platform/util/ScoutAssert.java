/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.platform.util;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.scout.rt.platform.util.IOUtility;

public final class ScoutAssert {

  private ScoutAssert() {
  }

  public static <T> void assertSetEquals(T[] expected, Collection<T> actual) {
    assertSetEquals(new ArrayList<T>(Arrays.asList(expected)), actual);
  }

  public static <T> void assertSetEquals(Collection<T> expected,
      Collection<T> actual) {
    if (actual == null) {
      fail(format("sets are not equal", expected, actual));
    }
    if (!new HashSet<T>(expected).equals(new HashSet<T>(actual))) {
      fail(format("sets are not equal", expected, actual));
    }
  }

  public static <T> void assertListEquals(T[] expected, Collection<T> actual) {
    assertListEquals(new ArrayList<T>(Arrays.asList(expected)), actual);
  }

  public static <T> void assertListEquals(Collection<T> expected,
      Collection<T> actual) {
    if (actual == null) {
      fail(format("lists are not equal", expected, actual));
    }
    if (!new ArrayList<T>(expected).equals(new ArrayList<T>(actual))) {
      fail(format("lists are not equal", expected, actual));
    }
  }

  public static void assertOrder(Object[] expected, Object[] actual) {
    assertOrder(null, expected, actual);
  }

  @SuppressWarnings("null")
  public static void assertOrder(String message, Object[] expected,
      Object[] actual) {
    if (expected == null && actual == null) {
      return;
    }
    if (expected == null || actual == null) {
      fail();
    }
    int actualIndex = 0;
    expectedLoop: for (Object expectedElement : expected) {
      for (int j = actualIndex; j < actual.length; j++) {
        if (expectedElement.equals(actual[j])) {
          actualIndex = j + 1;
          continue expectedLoop;
        }
      }
      fail(format(message, expected, actual));
    }
  }

  /**
   * Compare 2 comparable with {@link Comparable#compareTo(Object)} (expect to obtain 0). This can be useful when two
   * {@link java.math.BigDecimal} are compared.
   *
   * @since 3.10.0-M3
   */
  public static <T extends Comparable<T>> void assertComparableEquals(T expected, T actual) {
    assertComparableEquals(null, expected, actual);
  }

  /**
   * Compare 2 comparable with {@link Comparable#compareTo(Object)} (expect to obtain 0). This can be useful when two
   * {@link java.math.BigDecimal} are compared.
   *
   * @since 3.10.0-M3
   */
  public static <T extends Comparable<T>> void assertComparableEquals(String message, T expected, T actual) {
    if (expected == null && actual == null) {
      return;
    }
    else if (expected == null || actual == null) {
      fail(format(message, expected, actual));
    }
    else if (expected.compareTo(actual) == 0) {
      return;
    }
    fail(format(message, expected, actual));
  }

  private static String format(String message, Object expected, Object actual) {
    String s = "";
    if (message != null) {
      s = message + " ";
    }
    return s + "expected:<" + expected + "> but was:<" + actual + ">";
  }

  /**
   * compares two textfiles
   *
   * @param expectedFile
   * @param actualFile
   * @param charsetName
   *          The name of a supported {@link java.nio.charset.Charset </code>charset<code>}
   */
  public static void assertTextFileEquals(File expectedFile, File actualFile, String charsetName) {
    if (!expectedFile.exists()) {
      fail("File does not exists:" + expectedFile.getPath());
    }
    if (!actualFile.exists()) {
      fail("File does not exists:" + expectedFile.getPath());
    }
    List<String> expectedLines = IOUtility.readLines(expectedFile, charsetName);
    List<String> actualLines = IOUtility.readLines(actualFile, charsetName);
    assertListEquals(expectedLines, actualLines);
  }
}
