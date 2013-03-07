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

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link CompareUtility}
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
    Assert.assertFalse(CompareUtility.isOneOf(TEST_STRING, (Object[]) null));
    Assert.assertFalse(CompareUtility.isOneOf(TEST_STRING, new Object[0]));
    Assert.assertFalse(CompareUtility.isOneOf(TEST_STRING, FOO_STRING, BAR_STRING));
    Assert.assertFalse(CompareUtility.isOneOf(TEST_STRING, FOO_STRING, NUMBER_12));
    Assert.assertFalse(CompareUtility.isOneOf(null, FOO_STRING, NUMBER_12));
    Assert.assertFalse(CompareUtility.isOneOf(null, (Object[]) null));
    Assert.assertTrue(CompareUtility.isOneOf(TEST_STRING, TEST_STRING, NUMBER_12));
    Assert.assertTrue(CompareUtility.isOneOf(null, TEST_STRING, NUMBER_12, null));
    Assert.assertTrue(CompareUtility.isOneOf(null, (Object) null));
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
    Assert.assertFalse(CompareUtility.equals(d, null));
    Assert.assertFalse(CompareUtility.equals(null, d));
    Assert.assertFalse(CompareUtility.equals(t, null));
    Assert.assertFalse(CompareUtility.equals(null, t));

    // Test comparison between _equal_ dates of different types
    Assert.assertTrue(CompareUtility.equals(d, d));
    Assert.assertTrue(CompareUtility.equals(t, t));
    Assert.assertTrue(CompareUtility.equals(d, t));
    Assert.assertTrue(CompareUtility.equals(t, d)); // <-- this fails with default Timestamp.equals()
    Assert.assertEquals(d.hashCode(), t.hashCode());

    // Test comparison between _unequal_ dates of different types
    Assert.assertFalse(CompareUtility.equals(d, d2));
    Assert.assertFalse(CompareUtility.equals(t, d2));
    Assert.assertFalse(CompareUtility.equals(d2, d));

    // Test comparison of objects of non-related types
    Assert.assertFalse(CompareUtility.equals(t, s));
    Assert.assertFalse(CompareUtility.equals(s, t));
    Assert.assertFalse(CompareUtility.equals(t, o));
    Assert.assertFalse(CompareUtility.equals(o, t));

    // Test comparison of non-Date/Timestamp objects
    Assert.assertTrue(CompareUtility.equals(s, s2));
    Assert.assertTrue(CompareUtility.equals(s2, s));
    Assert.assertFalse(CompareUtility.equals(s, s3));
    Assert.assertFalse(CompareUtility.equals(s3, s));

    //Test a timestamp with nanos against a date
    Assert.assertTrue(CompareUtility.equals(twithNanos, twithNanos));
    Assert.assertFalse(CompareUtility.equals(twithNanos, t));
    Assert.assertFalse(CompareUtility.equals(twithNanos, d));
    Assert.assertFalse(CompareUtility.equals(d, twithNanos));
  }

}
