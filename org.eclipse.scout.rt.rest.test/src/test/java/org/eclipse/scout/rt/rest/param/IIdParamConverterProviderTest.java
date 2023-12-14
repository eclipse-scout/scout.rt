/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.param;

import static org.junit.Assert.*;

import jakarta.ws.rs.ext.ParamConverter;

import org.eclipse.scout.rt.dataobject.fixture.FixtureCompositeId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.IUuId;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.rest.param.IIdParamConverterProvider.QualifiedIIdParamConverter;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class IIdParamConverterProviderTest {

  private static final FixtureUuId TEST_UUID = FixtureUuId.of("d93072a6-05d3-47c8-84a6-3cca5ee286a6");

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
  public void testGetConverterUnqualified() {
    ParamConverter<FixtureUuId> uuIdConverter = m_provider.getConverter(FixtureUuId.class, null, null);
    assertNotNull(uuIdConverter);
    ParamConverter<FixtureStringId> stringIdConverter = m_provider.getConverter(FixtureStringId.class, null, null);
    assertNotNull(stringIdConverter);
    //noinspection AssertBetweenInconvertibleTypes
    assertNotSame(uuIdConverter, stringIdConverter);
  }

  @Test
  public void testGetConverterQualified() {
    ParamConverter<IUuId> uuIdConverter = m_provider.getConverter(IUuId.class, null, null);
    assertNotNull(uuIdConverter);
    ParamConverter<IId> idConverter = m_provider.getConverter(IId.class, null, null);
    assertNotNull(idConverter);
    assertSame(uuIdConverter, idConverter); // same application scoped bean is used for qualified converter
    //noinspection AssertBetweenInconvertibleTypes
    assertSame(uuIdConverter, BEANS.get(QualifiedIIdParamConverter.class));
  }

  @Test
  public void testGetConverterSameTypeMultipleTimes() {
    ParamConverter<FixtureUuId> conv1 = m_provider.getConverter(FixtureUuId.class, null, null);
    assertNotNull(conv1);
    ParamConverter<FixtureUuId> conv2 = m_provider.getConverter(FixtureUuId.class, null, null);
    assertSame(conv1, conv2);
  }

  @Test
  public void testIdParamConverterFromStringUnqualified() {
    ParamConverter<FixtureUuId> conv = m_provider.getConverter(FixtureUuId.class, null, null);

    assertNull(conv.fromString(null));
    assertThrows(PlatformException.class, () -> conv.fromString("invalid UUID"));

    assertNotNull(conv);
    FixtureUuId id = conv.fromString(TEST_UUID.unwrapAsString());
    assertNotNull(id);
    assertEquals(TEST_UUID, id);
  }

  @Test
  public void testIdParamConverterFromStringQualified() {
    ParamConverter<IUuId> conv = m_provider.getConverter(IUuId.class, null, null);

    assertNull(conv.fromString(null));
    assertThrows(PlatformException.class, () -> conv.fromString("invalid_type:invalid UUID"));

    assertNotNull(conv);
    IUuId id = conv.fromString("scout.FixtureUuId:" + TEST_UUID.unwrapAsString());
    assertNotNull(id);
    assertEquals(TEST_UUID, id);
  }

  @Test
  public void testCompositeIdParamConverterFromString() {
    ParamConverter<FixtureCompositeId> conv = m_provider.getConverter(FixtureCompositeId.class, null, null);

    assertNull(conv.fromString(null));
    assertThrows(PlatformException.class, () -> conv.fromString("invalid"));

    assertNotNull(conv);
    FixtureCompositeId id = conv.fromString("abc;" + TEST_UUID.unwrapAsString());
    assertNotNull(id);
    assertEquals(FixtureCompositeId.of(FixtureStringId.of("abc"), TEST_UUID), id);
  }

  @Test
  public void testIdParamConverterToStringUnqualified() {
    ParamConverter<FixtureUuId> conv = m_provider.getConverter(FixtureUuId.class, null, null);

    assertNull(conv.toString(null));
    assertEquals(TEST_UUID.unwrapAsString(), conv.toString(TEST_UUID));
  }

  @Test
  public void testIdParamConverterToStringQualified() {
    ParamConverter<IUuId> conv = m_provider.getConverter(IUuId.class, null, null);

    assertNull(conv.toString(null));
    assertEquals("scout.FixtureUuId:" + TEST_UUID.unwrapAsString(), conv.toString(TEST_UUID));
  }

  @Test
  public void testCompositeIdParamConverterToString() {
    ParamConverter<FixtureCompositeId> conv = m_provider.getConverter(FixtureCompositeId.class, null, null);

    assertNull(conv.toString(null));

    FixtureCompositeId id = FixtureCompositeId.of(FixtureStringId.of("abc"), TEST_UUID);
    assertEquals("abc;" + TEST_UUID.unwrapAsString(), conv.toString(id));
  }
}
