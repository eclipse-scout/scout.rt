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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Locale;

import org.eclipse.scout.rt.platform.util.ArrayComparator;
import org.eclipse.scout.rt.platform.util.MatrixUtility;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for {@link MatrixUtility}
 */
public class MatrixUtilityTest {

  private static final Object[] E1 = new Object[]{1L, "a", 30L, null};
  private static final Object[] E2 = new Object[]{2L, "b", 29L, "a"};
  private static final Object[] E3 = new Object[]{3L, "ö", 10L, "z"};
  private static final Object[] E4 = new Object[]{4L, "c", 10L, "ñ"};
  private static final Object[] E5 = new Object[]{5L, "ä", 10L, "ë"};

  private Object[][] m_array = null;

  @Before
  public void setUp() {
    m_array = new Object[][]{E1, E2, E3, E4, E5};
  }

  @Test
  public void testSort() {
    Object[][] data = new Object[][]{
        {"Aac"},
        {"ab"},
        {"äab"},
        {"Äaa"}};
    MatrixUtility.sort(Locale.GERMAN, data, 0);
    assertEquals("Äaa", data[0][0]);
    assertEquals("äab", data[1][0]);
    assertEquals("Aac", data[2][0]);
    assertEquals("ab", data[3][0]);
  }

  @Test
  public void testSortNullMatrix() {
    MatrixUtility.sort(null, 0);
  }

  @Test
  public void testSortEmptyMatrix() {
    MatrixUtility.sort(new Object[0][], 0);
  }

  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void testSortLowerBounds() {
    MatrixUtility.sort(m_array, -1);
  }

  @Test
  public void testSortUpperBounds() {
    MatrixUtility.sort(m_array, m_array.length);
  }

  @Test
  public void testSortUsingDeChLocale() {
    MatrixUtility.sort(new Locale("de", "CH"), m_array, 1);
    assertArrayEquals(new Object[][]{E1, E5, E2, E4, E3}, m_array);
  }

  @Test
  public void testSortUsingEnUsLocale() {
    MatrixUtility.sort(new Locale("en", "US"), m_array, 1);
    assertArrayEquals(new Object[][]{E1, E5, E2, E4, E3}, m_array);
  }

  @Test
  public void testSortTwoSortColumns() {
    MatrixUtility.sort(new Locale("de", "CH"), m_array, 2, 1);
    assertArrayEquals(new Object[][]{E5, E4, E3, E2, E1}, m_array);
  }

  @Test
  public void testSortNullSortValues() {
    MatrixUtility.sort(m_array, 3);
    assertArrayEquals(new Object[][]{E1, E2, E5, E4, E3}, m_array);
  }

  @Test
  public void testSortSwedish() {
    String[][] data = {
        {"Aa"},
        {"Æ"},
        {"B"},
        {"Ö"},
        {"Ø"},
        {"Z"},
        {"Å"},
        {"Ä"},
        {"Ae"},
        {"A"},
        {"Ü"}};

    MatrixUtility.sort(new Locale("sv", "SE"), data, 0);

    String[][] expected = {
        {"A"},
        {"Aa"},
        {"Ae"},
        {"B"},
        {"Ü"},
        {"Z"},
        {"Å"},
        {"Ä"},
        {"Æ"},
        {"Ö"},
        {"Ø"}};

    assertArrayEquals(expected, data);
  }

  @Test
  public void testSortComparator() {
    MatrixUtility.sortWithComparators(m_array,
        new ArrayComparator.ColumnComparator(2, new ArrayComparator.DefaultObjectComparator(new Locale("de", "CH"))),
        new ArrayComparator.ColumnComparator(1, Collections.reverseOrder(new ArrayComparator.DefaultObjectComparator(new Locale("de", "CH")))));
    assertArrayEquals(new Object[][]{E3, E4, E5, E2, E1}, m_array);
  }

  @Test
  public void testSortComparatorNoVararg() {
    MatrixUtility.sortWithComparators(m_array);
    assertArrayEquals(m_array, m_array);
  }

  @Test
  public void testSortNoVararg() {
    MatrixUtility.sort(m_array);
    assertArrayEquals(m_array, m_array);
  }

}
