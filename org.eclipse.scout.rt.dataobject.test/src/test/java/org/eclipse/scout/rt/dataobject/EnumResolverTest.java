/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.dataobject.enumeration.EnumResolver;
import org.eclipse.scout.rt.dataobject.fixture.FixtureEnum;
import org.eclipse.scout.rt.dataobject.fixture.FixtureEnumWithCustomResolve;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.testing.platform.util.ScoutAssert;
import org.junit.Test;

public class EnumResolverTest {

  @Test
  public void testFixtureEnum() {
    assertEquals(FixtureEnum.ONE, BEANS.get(EnumResolver.class).resolve(FixtureEnum.class, "one"));
    assertEquals(FixtureEnum.TWO, BEANS.get(EnumResolver.class).resolve(FixtureEnum.class, "two"));
    assertEquals(FixtureEnum.THREE, BEANS.get(EnumResolver.class).resolve(FixtureEnum.class, "three"));
    assertNull(BEANS.get(EnumResolver.class).resolve(FixtureEnum.class, null));
    ScoutAssert.assertThrows(AssertionException.class, () -> BEANS.get(EnumResolver.class).resolve(FixtureEnum.class, "foo"));
  }

  @Test
  public void testFixtureEnumWithCustomResolve() {
    assertEquals(FixtureEnumWithCustomResolve.ONE, BEANS.get(EnumResolver.class).resolve(FixtureEnumWithCustomResolve.class, "ONE"));
    assertEquals(FixtureEnumWithCustomResolve.TWO, BEANS.get(EnumResolver.class).resolve(FixtureEnumWithCustomResolve.class, "Two"));
    assertEquals(FixtureEnumWithCustomResolve.THREE, BEANS.get(EnumResolver.class).resolve(FixtureEnumWithCustomResolve.class, "thRee"));
    assertNull(BEANS.get(EnumResolver.class).resolve(FixtureEnumWithCustomResolve.class, null));
    ScoutAssert.assertThrows(PlatformException.class, () -> BEANS.get(EnumResolver.class).resolve(FixtureEnumWithCustomResolve.class, "foo"));
  }
}
