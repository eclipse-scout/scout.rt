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

import org.eclipse.scout.rt.dataobject.enumeration.Enums;
import org.eclipse.scout.rt.dataobject.fixture.FixtureEnum;
import org.junit.Test;

public class EnumsTest {

  @Test
  public void testResolve() {
    assertEquals(FixtureEnum.ONE, Enums.resolve(FixtureEnum.class, "one"));
  }

  @Test
  public void testToString() {
    assertEquals("one", Enums.toStringValue(FixtureEnum.ONE));
    assertNull(Enums.toStringValue(null));
  }
}
