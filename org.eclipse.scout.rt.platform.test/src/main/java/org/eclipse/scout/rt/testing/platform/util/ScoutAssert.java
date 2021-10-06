/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.testing.platform.util;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.junit.ComparisonFailure;

public final class ScoutAssert {

  private ScoutAssert() {
  }

  public static <T> void assertSetEquals(T[] expected, Collection<T> actual) {
    assertSetEquals(new ArrayList<>(Arrays.asList(expected)), actual);
  }

  public static <T> void assertSetEquals(Collection<T> expected,
      Collection<T> actual) {
    if (actual == null) {
      fail(format("sets are not equal", expected, actual));
    }
    if (!new HashSet<>(expected).equals(new HashSet<>(actual))) {
      fail(format("sets are not equal", expected, actual));
    }
  }

  public static <T> void assertListEquals(T[] expected, Collection<T> actual) {
    assertListEquals(new ArrayList<>(Arrays.asList(expected)), actual);
  }

  public static <T> void assertListEquals(Collection<T> expected,
      Collection<T> actual) {
    if (actual == null) {
      fail(format("lists are not equal", expected, actual));
    }
    if (!new ArrayList<>(expected).equals(new ArrayList<>(actual))) {
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
      for (int j = actualIndex; j < actual.length; j++) { //NOSONAR
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

  /**
   * Asserts that two objects are equal. If they are not, an {@link ComparisonFailure} is thrown with a string
   * representation of the given objects. If <code>expected</code> and <code>actual</code> are <code>null</code>, they
   * are considered equal.
   * <p>
   * This method was motivated by the JUnit method org.junit.Assert.assertEquals(Object, Object) but extending its
   * functionality to throw a {@link ComparisonFailure} also for non-String objects.
   *
   * @param expected
   *          expected value
   * @param actual
   *          actual value
   */
  public static void assertEqualsWithComparisonFailure(Object expected, Object actual) {
    assertEqualsWithComparisonFailure(null, expected, actual);
  }

  /**
   * Asserts that two objects are equal. If they are not, an {@link ComparisonFailure} is thrown with the given message
   * and a string representation of the given objects. If <code>expected</code> and <code>actual</code> are
   * <code>null</code>, they are considered equal.
   * <p>
   * This method was motivated by the JUnit method org.junit.Assert.assertEquals(String, Object, Object) but extending
   * its functionality to throw a {@link ComparisonFailure} also for non-String objects.
   *
   * @param message
   *          the identifying message for the {@link ComparisonFailure} (<code>null</code> okay)
   * @param expected
   *          expected value
   * @param actual
   *          actual value
   */
  public static void assertEqualsWithComparisonFailure(String message, Object expected, Object actual) {
    if (!ObjectUtility.equals(expected, actual)) {
      String cleanMessage = message == null ? "objects not equals" : message;
      throw new ComparisonFailure(cleanMessage, Objects.toString(expected), Objects.toString(actual));
    }
  }
}
