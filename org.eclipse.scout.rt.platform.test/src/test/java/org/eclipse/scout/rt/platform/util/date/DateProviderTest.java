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
package org.eclipse.scout.rt.platform.util.date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

/**
 * Tests for {@link DateProvider}
 */
public class DateProviderTest {

  @Test
  public void testCurrentMillis() {
    DateProvider provider = new DateProvider();

    // These calls must be in exactly this order!
    long dateBefore = System.currentTimeMillis();
    Date returned = provider.currentMillis();
    long dateAfter = System.currentTimeMillis();

    assertNotNull(returned);
    assertTrue("Date returned by date provider must be greater or equal to a date obtained beforehand", dateBefore <= returned.getTime());
    assertTrue("Date returned by date provider must be smaller or equal to a date obtained afterwards", dateAfter >= returned.getTime());
  }

  @Test
  public void testCurrentSeconds() {
    DateProvider provider = new DateProvider();

    // These calls must be in exactly this order!
    Date dateBefore = new Date();
    Date returned = provider.currentSeconds();
    Date dateAfter = new Date();

    assertNotNull(returned);
    assertTrue("Truncated date returned by date provider must be smaller or equal to a date obtained afterwards", dateAfter.getTime() >= returned.getTime());
    assertTrue("Truncated date returned by date provider must be greater or equal than the truncated value of date obtained beforhand",
        returned.getTime() >= DateUtility.truncDate(dateBefore).getTime());

    GregorianCalendar returnedCalendar = new GregorianCalendar();
    returnedCalendar.setTime(returned);
    assertEquals("Milliseconds value of truncated date must be 0", returnedCalendar.get(GregorianCalendar.MILLISECOND), 0);
  }

  @Test
  public void testTimeZone() {
    TimeZone currentTimeZone = TimeZone.getDefault();

    DateProvider provider = new DateProvider();
    TimeZone tz = provider.currentTimeZone();
    assertNotNull("Provided timezone should not be null", tz);
    assertTrue("Provided timezone should have same rules as default timezone", currentTimeZone.hasSameRules(tz));
    assertEquals("Provided timezone should have same ID as default timezone", currentTimeZone.getID(), tz.getID());
  }

}
