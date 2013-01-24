/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

/**
 * Tests for {@link org.eclipse.scout.commons.CompareUtility CompareUtility}
 * 
 * @since 3.8.1
 */
public class CompareUtilityTest {

  static final String TEST_STRING = "test";
  static final String FOO_STRING = "foo";
  static final String BAR_STRING = "bar";

  static final long NUMBER_12 = 12L;

  @Test
  public void testIsOneOf() {
    assertFalse(CompareUtility.isOneOf(TEST_STRING, (Object[]) null));
    assertFalse(CompareUtility.isOneOf(TEST_STRING, new Object[0]));
    assertFalse(CompareUtility.isOneOf(TEST_STRING, FOO_STRING, BAR_STRING));
    assertFalse(CompareUtility.isOneOf(TEST_STRING, FOO_STRING, NUMBER_12));
    assertFalse(CompareUtility.isOneOf(null, FOO_STRING, NUMBER_12));
    assertFalse(CompareUtility.isOneOf(null, (Object[]) null));
    assertTrue(CompareUtility.isOneOf(TEST_STRING, TEST_STRING, NUMBER_12));
    assertTrue(CompareUtility.isOneOf(null, TEST_STRING, NUMBER_12, null));
    assertTrue(CompareUtility.isOneOf(null, (Object) null));
  }

  /**
   * Tests {@link org.eclipse.scout.commons.CompareUtility#equals(T, T) CompareUtility#equals(T, T)} with respect to
   * {@link java.util.Date} and {@link java.sql.Timestamp}.
   */
  @Test
  public void testTimestampDateEquality() {
    Calendar c = Calendar.getInstance();
    long millis = c.getTimeInMillis();
    Timestamp t = new Timestamp(millis);
    Date d = new Date(millis);
    Date d2 = new Date(millis + 1);
    String s = "test string";
    String s2 = "test string";
    String s3 = "another string";
    Object o = new Object();
    Timestamp twithNanos = new Timestamp(millis);
    twithNanos.setNanos(15);

    // Test comparison to null
    assertFalse(CompareUtility.equals(d, null));
    assertFalse(CompareUtility.equals(null, d));
    assertFalse(CompareUtility.equals(t, null));
    assertFalse(CompareUtility.equals(null, t));

    // Test comparison between _equal_ dates of different types
    assertTrue(CompareUtility.equals(d, d));
    assertTrue(CompareUtility.equals(t, t));
    assertTrue(CompareUtility.equals(d, t));
    assertTrue(CompareUtility.equals(t, d)); // <-- this fails with default Timestamp.equals()
    assertEquals(d.hashCode(), t.hashCode());

    // Test comparison between _unequal_ dates of different types
    assertFalse(CompareUtility.equals(d, d2));
    assertFalse(CompareUtility.equals(t, d2));
    assertFalse(CompareUtility.equals(d2, d));

    // Test comparison of objects of non-related types
    assertFalse(CompareUtility.equals(t, s));
    assertFalse(CompareUtility.equals(s, t));
    assertFalse(CompareUtility.equals(t, o));
    assertFalse(CompareUtility.equals(o, t));

    // Test comparison of non-Date/Timestamp objects
    assertTrue(CompareUtility.equals(s, s2));
    assertTrue(CompareUtility.equals(s2, s));
    assertFalse(CompareUtility.equals(s, s3));
    assertFalse(CompareUtility.equals(s3, s));

    //Test a timestamp with nanos against a date
    assertTrue(CompareUtility.equals(twithNanos, twithNanos));
    assertFalse(CompareUtility.equals(twithNanos, t));
    assertFalse(CompareUtility.equals(twithNanos, d));
    assertFalse(CompareUtility.equals(d, twithNanos));
  }

}
