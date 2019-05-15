/*******************************************************************************
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.dataobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
