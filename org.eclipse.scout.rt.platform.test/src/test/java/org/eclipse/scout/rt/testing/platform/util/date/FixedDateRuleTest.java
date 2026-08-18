/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.util.date;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.date.IDateProvider;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class FixedDateRuleTest {

  @Rule
  public FixedDateRule m_fixedDateRule = new FixedDateRule();

  @Test
  public void testRule() {
    assertEquals(m_fixedDateRule.getDate(), BEANS.get(IDateProvider.class).currentMillis());
  }

  @Test
  public void testRuleSetDate() {
    Date date = new Date();
    m_fixedDateRule.setDate(date);
    assertEquals(date, BEANS.get(IDateProvider.class).currentMillis());
  }

  @Test
  public void testRuleSetTimeMillis() {
    m_fixedDateRule.setTimeMillis(1000L);
    assertEquals(1000L, BEANS.get(IDateProvider.class).currentMillis().getTime());
  }
}
