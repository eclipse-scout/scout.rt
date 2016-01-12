/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
  public void testCompareNumeric() {
    String s1 = "doc9.txt";
    String s2 = "doc10.txt";
    assertTrue("'doc9.txt' < 'doc10.txt'", StringUtility.ALPHANUMERIC_COMPARATOR.compare(s1, s2) < 0);
  }

  @Test
  public void testCompareText() {
    String s1 = "doc9.txt";
    String s2 = "myfile.txt";
    assertTrue("'doc9.txt' < 'myfile.txt'", StringUtility.ALPHANUMERIC_COMPARATOR.compare(s1, s2) < 0);
  }

  private void assertConsistentWithEquals(String s1, String s2) {
    assertTrue("Comparison method violates its general contract", (StringUtility.ALPHANUMERIC_COMPARATOR.compare(s1, s2) == 0) == s1.equals(s2));
  }

}
