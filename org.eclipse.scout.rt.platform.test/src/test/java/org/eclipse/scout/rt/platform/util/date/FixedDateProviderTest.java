/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.date;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.scout.rt.testing.platform.util.date.FixedDateProvider;
import org.junit.Test;

/**
 * Testcases for {@link FixedDateProvider}
 */
public class FixedDateProviderTest {

  private static final Date NOW = new Date();

  @Test
  public void testGetDate() {
    FixedDateProvider fixedDateProvider = new FixedDateProvider();
    assertEquals(fixedDateProvider.getDate(), fixedDateProvider.currentMillis());

    fixedDateProvider = new FixedDateProvider(NOW);
    assertEquals(NOW, fixedDateProvider.getDate());
  }

  @Test
  public void testSetDate() {
    FixedDateProvider fixedDateProvider = new FixedDateProvider(NOW);
    assertEquals(NOW, fixedDateProvider.getDate());
    Date date = DateUtility.addDays(NOW, 1);
    fixedDateProvider.setDate(date);
    assertEquals(date, fixedDateProvider.getDate());
  }

  @Test
  public void testSetTimeMillis() {
    FixedDateProvider fixedDateProvider = new FixedDateProvider(NOW);
    fixedDateProvider.setTimeMillis(1000);
    assertEquals(1000, fixedDateProvider.currentUTCMillis());
  }

  @Test
  public void testCurrentCalendar() {
    FixedDateProvider fixedDateProvider = new FixedDateProvider(NOW);
    Calendar calendar = fixedDateProvider.currentCalendar();
    assertEquals(NOW, calendar.getTime());
  }
}
