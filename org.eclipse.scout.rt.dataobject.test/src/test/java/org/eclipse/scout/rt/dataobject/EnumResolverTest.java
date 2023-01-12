/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.dataobject.enumeration.EnumResolver;
import org.eclipse.scout.rt.dataobject.fixture.FixtureEnum;
import org.eclipse.scout.rt.dataobject.fixture.FixtureEnumWithCustomResolve;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Assert;
import org.junit.Test;

public class EnumResolverTest {

  @Test
  public void testFixtureEnum() {
    assertEquals(FixtureEnum.ONE, BEANS.get(EnumResolver.class).resolve(FixtureEnum.class, "one"));
    assertEquals(FixtureEnum.TWO, BEANS.get(EnumResolver.class).resolve(FixtureEnum.class, "two"));
    assertEquals(FixtureEnum.THREE, BEANS.get(EnumResolver.class).resolve(FixtureEnum.class, "three"));
    assertNull(BEANS.get(EnumResolver.class).resolve(FixtureEnum.class, null));
    Assert.assertThrows(AssertionException.class, () -> BEANS.get(EnumResolver.class).resolve(FixtureEnum.class, "foo"));
  }

  @Test
  public void testFixtureEnumWithCustomResolve() {
    assertEquals(FixtureEnumWithCustomResolve.ONE, BEANS.get(EnumResolver.class).resolve(FixtureEnumWithCustomResolve.class, "ONE"));
    assertEquals(FixtureEnumWithCustomResolve.TWO, BEANS.get(EnumResolver.class).resolve(FixtureEnumWithCustomResolve.class, "Two"));
    assertEquals(FixtureEnumWithCustomResolve.THREE, BEANS.get(EnumResolver.class).resolve(FixtureEnumWithCustomResolve.class, "thRee"));
    assertNull(BEANS.get(EnumResolver.class).resolve(FixtureEnumWithCustomResolve.class, null));
    Assert.assertThrows(PlatformException.class, () -> BEANS.get(EnumResolver.class).resolve(FixtureEnumWithCustomResolve.class, "foo"));
  }
}
