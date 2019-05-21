/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.rest.param;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
