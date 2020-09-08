/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util.date;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for {@link DateFormatProvider}
 */
public class DateFormatProviderTest {

  private static final Locale LOCALE_SWITZERLAND_DE = new Locale("de", "CH");

  private DateFormatProvider m_provider;

  @Before
  public void before() {
    m_provider = new DateFormatProvider();
  }

  @Test
  public void testGetIsolatedTimeFormatPattern() {
    assertEquals("h:mm a", m_provider.getIsolatedTimeFormatPattern(DateFormatProvider.PATTERN_STYLE_ISOLATED_TIME, Locale.US));
    assertEquals("h:mm:ss a", m_provider.getIsolatedTimeFormatPattern(DateFormatProvider.PATTERN_STYLE_ISOLATED_TIME_WITH_SECONDS, Locale.US));
    assertEquals("h:mm:ss.SSS a", m_provider.getIsolatedTimeFormatPattern(DateFormatProvider.PATTERN_STYLE_ISOLATED_TIME_WITH_MILLISECONDS, Locale.US));

    assertEquals("HH:mm", m_provider.getIsolatedTimeFormatPattern(DateFormatProvider.PATTERN_STYLE_ISOLATED_TIME, Locale.GERMANY));
    assertEquals("HH:mm:ss", m_provider.getIsolatedTimeFormatPattern(DateFormatProvider.PATTERN_STYLE_ISOLATED_TIME_WITH_SECONDS, Locale.GERMANY));
    assertEquals("HH:mm:ss,SSS", m_provider.getIsolatedTimeFormatPattern(DateFormatProvider.PATTERN_STYLE_ISOLATED_TIME_WITH_MILLISECONDS, Locale.GERMANY));

    assertEquals("HH:mm", m_provider.getIsolatedTimeFormatPattern(DateFormatProvider.PATTERN_STYLE_ISOLATED_TIME, LOCALE_SWITZERLAND_DE));
    assertEquals("HH:mm:ss", m_provider.getIsolatedTimeFormatPattern(DateFormatProvider.PATTERN_STYLE_ISOLATED_TIME_WITH_SECONDS, LOCALE_SWITZERLAND_DE));
    assertEquals("HH:mm:ss.SSS", m_provider.getIsolatedTimeFormatPattern(DateFormatProvider.PATTERN_STYLE_ISOLATED_TIME_WITH_MILLISECONDS, LOCALE_SWITZERLAND_DE));
  }
}
