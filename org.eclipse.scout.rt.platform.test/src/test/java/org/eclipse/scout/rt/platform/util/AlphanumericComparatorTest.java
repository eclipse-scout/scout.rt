/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests for {@link AlphanumericComparator}
 *
 * @since 5.2
 */
public class AlphanumericComparatorTest {

  @Test
  public void testConsistentWithEquals() {
    String[] s1 = new String[]{"", new String(""), "test", new String("test")};
    String[] s2 = new String[]{"", new String(""), "test", new String("test")};

    for (int i = 0; i < s1.length; i++) {
      assertTrue("Comparison method violates its general contract", (StringUtility.ALPHANUMERIC_COMPARATOR.compare(s1[i], s2[i]) == 0) == s1[i].equals(s2[i]));
    }
  }

  @Test
  public void testConsistentWithEqualsBothEmpty() {
    String s1 = "";
    String s2 = "";
    assertConsistentWithEquals(s1, s2);
  }

  @Test
  public void testConsistentWithEqualsBothEmptyNewInstance() {
    String s1 = new String("");
    String s2 = new String("");
    assertConsistentWithEquals(s1, s2);
  }

  @Test
  public void testConsistentWithEqualsBothNonEmpty() {
    String s1 = "test";
    String s2 = "test";
    assertConsistentWithEquals(s1, s2);
  }

  @Test
  public void testConsistentWithEqualsBothNonEmptyNewInstance() {
    String s1 = new String("test");
    String s2 = new String("test");
    assertConsistentWithEquals(s1, s2);
  }

  @Test
  public void testBothNull() {
    String s1 = null;
    String s2 = null;
    assertEquals("Both arguments are 'null'", 0, StringUtility.ALPHANUMERIC_COMPARATOR.compare(s1, s2));
  }

  @Test
  public void testFirstNull() {
    String s1 = null;
    String s2 = "";
    assertTrue("First argument is 'null'", StringUtility.ALPHANUMERIC_COMPARATOR.compare(s1, s2) < 0);
  }

  @Test
  public void testSecondNull() {
    String s1 = "";
    String s2 = null;
    assertTrue("Second argument is 'null'", StringUtility.ALPHANUMERIC_COMPARATOR.compare(s1, s2) > 0);
  }

  @Test
  public void testCompare() {
    String s1 = "doc8";
    String s2 = "doc9.txt";
    assertTrue("'doc8' < 'doc9.txt'", StringUtility.ALPHANUMERIC_COMPARATOR.compare(s1, s2) < 0);
  }

  @Test
  public void testCompareFoundBoth() {
    String s1 = "doc9.doc";
    String s2 = "doc9.txt";
    assertTrue("'doc9.doc' < 'doc9.txt'", StringUtility.ALPHANUMERIC_COMPARATOR.compare(s1, s2) < 0);
  }

  @Test
  public void testCompareFoundSecondLower() {
    String s1 = "doc10";
    String s2 = "doc9.txt";
    assertTrue("'doc10' > 'doc9.txt'", StringUtility.ALPHANUMERIC_COMPARATOR.compare(s1, s2) > 0);
  }

  @Test
  public void testCompareFoundSecondGreater() {
    String s1 = "doc9";
    String s2 = "doc9.txt";
    assertTrue("'doc9' < 'doc9.txt'", StringUtility.ALPHANUMERIC_COMPARATOR.compare(s1, s2) < 0);
  }

  @Test
  public void testCompareNumeric() {
    String s1 = "doc9.txt";
    String s2 = "doc10.txt";
    assertTrue("'doc9.txt' < 'doc10.txt'", StringUtility.ALPHANUMERIC_COMPARATOR.compare(s1, s2) < 0);
  }

  @Test
  public void testCompareLargeNumbers() {
    String s1, s2;

    //18 digits
    s1 = "999999999999999999";
    s2 = "888888888888888888";
    assertTrue("'" + "s1" + "' > '" + s2 + "'", StringUtility.ALPHANUMERIC_COMPARATOR.compare(s1, s2) > 0);

    //19 digits
    s1 = "9999999999999999999";
    s2 = "8888888888888888888";
    assertTrue("'" + "s1" + "' > '" + s2 + "'", StringUtility.ALPHANUMERIC_COMPARATOR.compare(s1, s2) > 0);

    s1 = "88cfea-6578700000000477040000000473720035636.263562363265445634-32653";
    s2 = "88cfea-685485800000477040000000473720035636.263562363265445634-fec54";
    assertTrue("'" + "s1" + "' > '" + s2 + "'", StringUtility.ALPHANUMERIC_COMPARATOR.compare(s1, s2) > 0);
  }

  @Test
  public void testCompareText() {
    String s1 = "doc9.txt";
    String s2 = "myfile.txt";
    assertTrue("'doc9.txt' < 'myfile.txt'", StringUtility.ALPHANUMERIC_COMPARATOR.compare(s1, s2) < 0);
  }

  @Test
  public void testCompareUmlaute() {
    String s1 = "a";
    String s2 = "b";
    assertTrue("'a' < 'b'", StringUtility.ALPHANUMERIC_COMPARATOR.compare(s1, s2) < 0);

    s1 = "ä";
    s2 = "b";
    assertTrue("'ä' < 'b'", StringUtility.ALPHANUMERIC_COMPARATOR.compare(s1, s2) < 0);

    s1 = "Bärtschi";
    s2 = "Bucher";
    assertTrue("'Bärtschi' < 'Bucher'", StringUtility.ALPHANUMERIC_COMPARATOR.compare(s1, s2) < 0);
  }

  private void assertConsistentWithEquals(String s1, String s2) {
    assertTrue("Comparison method violates its general contract", (StringUtility.ALPHANUMERIC_COMPARATOR.compare(s1, s2) == 0) == s1.equals(s2));
  }
}
