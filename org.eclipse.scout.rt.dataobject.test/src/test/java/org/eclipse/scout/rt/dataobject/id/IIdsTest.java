/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.id;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;

import java.util.UUID;
import java.util.function.Function;

import org.eclipse.scout.rt.dataobject.fixture.FixtureCompositeId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureLongId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureWrapperCompositeId;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.DefaultPlatform;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.internal.BeanImplementor;
import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test for {@link IIds}.
 */
public class IIdsTest {

  private static final UUID TEST_UUID = UUID.fromString("67fef04a-16e8-4ba6-aabc-987f9d4a2487");

  @Test
  public void testToString() {
    assertNull(IIds.toString(null));
    assertEquals("foo", IIds.toString(FixtureStringId.of("foo")));
    assertEquals("42", IIds.toString(FixtureLongId.of(42L)));
  }

  @Test
  public void testCreate() {
    assertEquals(FixtureStringId.of("abc"), IIds.create(FixtureStringId.class, "abc"));
    assertEquals(FixtureLongId.of("42"), IIds.create(FixtureLongId.class, 42L));
    assertEquals(FixtureUuId.of(TEST_UUID), IIds.create(FixtureUuId.class, TEST_UUID));

    assertEquals(FixtureCompositeId.of("abc", TEST_UUID), IIds.create(FixtureCompositeId.class, "abc", TEST_UUID));
    assertEquals(FixtureWrapperCompositeId.of("abc", TEST_UUID, "efg"), IIds.create(FixtureWrapperCompositeId.class, "abc", TEST_UUID, "efg"));

    assertThrows("expect exception due to wrong parameter type", PlatformException.class, () -> IIds.create(FixtureUuId.class, "foo"));
    assertThrows("expect exception due to wrong parameter type", PlatformException.class, () -> IIds.create(FixtureLongId.class, "foo"));
    assertThrows("expect exception due to wrong parameter count", PlatformException.class, () -> IIds.create(FixtureCompositeId.class, "foo"));
    assertThrows("expect exception due to wrong parameter count", PlatformException.class, () -> IIds.create(FixtureCompositeId.class, (Comparable<?>) null));
    assertThrows("expect exception due to wrong parameter count", PlatformException.class, () -> IIds.create(FixtureCompositeId.class, (Object[]) null));
  }

  @Test
  public void testCreateByNullValues() {
    assertNull(IIds.create(FixtureStringId.class, (String) null));
    assertNull(IIds.create(FixtureLongId.class, (Long) null));
    assertNull(IIds.create(FixtureUuId.class, (UUID) null));

    Object[] params = new Object[]{null, null};
    assertNull(IIds.create(FixtureCompositeId.class, params));
    assertNull(IIds.create(FixtureCompositeId.class, null, null));
    assertNull(IIds.create(FixtureCompositeId.class, "abc", null));
    assertNull(IIds.create(FixtureCompositeId.class, null, TEST_UUID));
  }

  @Test
  public void testFactory() {
    Function<String, FixtureStringId> stringIdFactory = IIds.factory(FixtureStringId.class);
    assertEquals(FixtureStringId.of("abc"), stringIdFactory.apply("abc"));

    Function<UUID, FixtureUuId> uuIdFactory = IIds.factory(FixtureUuId.class);
    assertEquals(FixtureUuId.of(TEST_UUID), uuIdFactory.apply(TEST_UUID));
  }

  protected static class FixturePlatformWithMinimalBeanManager extends DefaultPlatform {

    private BeanManagerImplementor m_beanManager;

    FixturePlatformWithMinimalBeanManager(BeanManagerImplementor beanManager) {
      m_beanManager = beanManager;
    }

    @Override
    protected BeanManagerImplementor createBeanManager() {
      return m_beanManager;
    }

    @Override
    protected void validateConfiguration() {
      // nop
    }
  }

  @Test
  public void testFactoryBeanCached() {
    IPlatform oldPlatform = Platform.peek();
    try {
      BeanManagerImplementor beanManager = Mockito.mock(BeanManagerImplementor.class);
      Mockito.when(beanManager.optBean(IdFactory.class)).thenReturn(new BeanImplementor<>(new BeanMetaData(IdFactory.class)));
      Platform.set(new FixturePlatformWithMinimalBeanManager(beanManager));
      Platform.get().start();

      Function<UUID, FixtureUuId> uuIdFactory = IIds.factory(FixtureUuId.class);
      assertEquals(FixtureUuId.of(TEST_UUID), uuIdFactory.apply(TEST_UUID));
      // invoke factory for a second time
      assertEquals(FixtureUuId.of(TEST_UUID), uuIdFactory.apply(TEST_UUID));
      Mockito.verify(beanManager, times(1)).optBean(IdFactory.class);
    }
    finally {
      Platform.set(oldPlatform);
    }
  }
}
