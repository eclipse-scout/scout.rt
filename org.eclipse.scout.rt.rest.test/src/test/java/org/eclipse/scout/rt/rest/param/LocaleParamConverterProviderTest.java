/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.param;

import static org.junit.Assert.*;

import java.util.Locale;

import javax.ws.rs.ext.ParamConverter;

import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class LocaleParamConverterProviderTest {

  private LocaleParamConverterProvider m_provider;

  @Before
  public void before() {
    m_provider = new LocaleParamConverterProvider();
  }

  @Test
  public void testLocaleConverterFromString() {
    ParamConverter<Locale> conv = m_provider.getConverter(Locale.class, null, null);

    assertNull(conv.fromString(null));

    Locale expectedLocale = new Locale("de", "CH");
    Locale locale = conv.fromString(expectedLocale.toLanguageTag());
    assertNotNull(locale);
    assertEquals(expectedLocale, locale);
  }

  @Test
  public void testLocaleParamConverterToString() {
    ParamConverter<Locale> conv = m_provider.getConverter(Locale.class, null, null);

    assertNull(conv.toString(null));

    Locale expectedLocale = new Locale("de", "CH");
    assertEquals(expectedLocale.toLanguageTag(), conv.toString(expectedLocale));
  }
}
