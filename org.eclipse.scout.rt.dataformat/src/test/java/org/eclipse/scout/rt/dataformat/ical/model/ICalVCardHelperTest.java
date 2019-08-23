/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.dataformat.ical.model;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.TimeZone;

import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Test;

public class ICalVCardHelperTest {

  @Test
  public void testCreateDateTimeGMT() {
    ICalVCardHelper helper = BEANS.get(ICalVCardHelper.class);
    Calendar cal = Calendar.getInstance();
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    cal.set(2019, 7, 23, 15, 22, 11); // month is 0 based 8 (august)
    assertEquals("20190823T152200Z", helper.createDateTime(cal.getTime()));
  }

  @Test
  public void testCreateDateTimeGMT_1() {
    ICalVCardHelper helper = BEANS.get(ICalVCardHelper.class);
    Calendar cal = Calendar.getInstance();
    cal.setTimeZone(TimeZone.getTimeZone("GMT+1"));
    cal.set(2019, 7, 23, 15, 22, 11); // month is 0 based 8 (august)
    assertEquals("20190823T142200Z", helper.createDateTime(cal.getTime()));
  }

  @Test
  public void testCreateDate() {
    ICalVCardHelper helper = BEANS.get(ICalVCardHelper.class);
    Calendar cal = Calendar.getInstance();
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    cal.set(2019, 7, 23, 15, 22, 00); // month is 0 based 8 (august)
    assertEquals("20190823", helper.createDate(cal.getTime()));
  }

  /**
   * test cases for concatenating arbitrary strings
   */
  @Test
  public void testConcatenateStrings() {
    ICalVCardHelper helper = BEANS.get(ICalVCardHelper.class);
    assertEquals("", helper.concatenateStrings(null));
    assertEquals("", helper.concatenateStrings(null, (String[]) null));
    assertEquals("", helper.concatenateStrings(".", (String[]) null));
    assertEquals(".", helper.concatenateStrings(".", null, null));
    assertEquals("", helper.concatenateStrings("", "", "", ""));
    assertEquals("..", helper.concatenateStrings(".", "", "", ""));
    assertEquals(helper.concatenateStrings("", "test1", "test2", "test3"), helper.concatenateStrings(null, "test1", "test2", "test3"));
    assertEquals("abc.def.ghi", helper.concatenateStrings(".", "abc", "def", "ghi"));
    assertEquals("abc..def..ghi.", helper.concatenateStrings(".", "abc", null, "def", null, "ghi", null));
    assertEquals("abcabcabc", helper.concatenateStrings("abc", "abc", "abc"));
    assertEquals("1234567890_§¦@#°§¬|¢)=´~_1234567890", helper.concatenateStrings("§¦@#°§¬|¢)=´~", "1234567890_", "_1234567890"));
  }
}
