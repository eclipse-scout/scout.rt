/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
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

import java.util.UUID;

import javax.ws.rs.ext.ParamConverter;

import org.eclipse.scout.rt.dataobject.fixture.FixtureCompositeId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class IIdParamConverterProviderTest {

  private static final UUID TEST_UUID = UUID.randomUUID();

  private IIdParamConverterProvider m_provider;

  @Before
  public void before() {
    m_provider = new IIdParamConverterProvider();
  }

  @Test
  public void testGetConverterUnhandledType() {
    assertThrows(NullPointerException.class, () -> m_provider.getConverter(null, null, null));
    assertNull(m_provider.getConverter(String.class, null, null));
  }

  @Test
  public void testGetConverter() {
    ParamConverter<FixtureUuId> uuIdConverter = m_provider.getConverter(FixtureUuId.class, null, null);
    assertNotNull(uuIdConverter);
    ParamConverter<FixtureStringId> stringIdConverter = m_provider.getConverter(FixtureStringId.class, null, null);
    assertNotNull(stringIdConverter);
    //noinspection AssertBetweenInconvertibleTypes
    assertNotSame(uuIdConverter, stringIdConverter);
  }

  @Test
  public void testGetConverterSameTypeMultipleTimes() {
    ParamConverter<FixtureUuId> conv1 = m_provider.getConverter(FixtureUuId.class, null, null);
    assertNotNull(conv1);
    ParamConverter<FixtureUuId> conv2 = m_provider.getConverter(FixtureUuId.class, null, null);
    assertSame(conv1, conv2);
  }

  @Test
  public void testIdParamConverterFromString() {
    ParamConverter<FixtureUuId> conv = m_provider.getConverter(FixtureUuId.class, null, null);

    assertNull(conv.fromString(null));
    assertThrows(PlatformException.class, () -> conv.fromString("invalid UUID"));

    assertNotNull(conv);
    FixtureUuId id = conv.fromString(TEST_UUID.toString());
    assertNotNull(id);
    assertEquals(TEST_UUID, id.unwrap());
  }

  @Test
  public void testCompositeIdParamConverterFromString() {
    ParamConverter<FixtureCompositeId> conv = m_provider.getConverter(FixtureCompositeId.class, null, null);

    assertNull(conv.fromString(null));
    assertThrows(PlatformException.class, () -> conv.fromString("invalid"));

    assertNotNull(conv);
    FixtureCompositeId id = conv.fromString("abc;" + TEST_UUID);
    assertNotNull(id);
    assertEquals(FixtureCompositeId.of("abc", TEST_UUID), id);
  }

  @Test
  public void testIdParamConverterToString() {
    ParamConverter<FixtureUuId> conv = m_provider.getConverter(FixtureUuId.class, null, null);

    assertNull(conv.toString(null));

    FixtureUuId id = FixtureUuId.of(TEST_UUID);
    assertEquals(TEST_UUID.toString(), conv.toString(id));
  }

  @Test
  public void testCompositeIdParamConverterToString() {
    ParamConverter<FixtureCompositeId> conv = m_provider.getConverter(FixtureCompositeId.class, null, null);

    assertNull(conv.toString(null));

    FixtureCompositeId id = FixtureCompositeId.of("abc", TEST_UUID);
    assertEquals("abc;" + TEST_UUID, conv.toString(id));
  }
}
