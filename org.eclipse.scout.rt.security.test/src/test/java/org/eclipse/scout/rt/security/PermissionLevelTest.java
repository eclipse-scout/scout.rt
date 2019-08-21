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
package org.eclipse.scout.rt.security;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.scout.rt.platform.util.CloneUtility;
import org.eclipse.scout.rt.security.fixture.TestPermissionLevels;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class PermissionLevelTest {

  @After
  public void after() {
    PermissionLevel.registerOrOverride(TestPermissionLevels.LEVEL_DENIED, "DENIED", true, () -> "DENIED");
  }

  @Test
  public void testBasic() throws Exception {
    assertEquals("DENIED", TestPermissionLevels.DENIED.getText());
    assertEquals("DENIED", TestPermissionLevels.DENIED.getStringValue());
    assertTrue(TestPermissionLevels.DENIED.isActive());
    assertEquals(TestPermissionLevels.LEVEL_DENIED, TestPermissionLevels.DENIED.getValue());

    PermissionLevel.register(TestPermissionLevels.LEVEL_DENIED, "DENIED_X", false, () -> "DENIED_Y"); // no override
    assertEquals("DENIED", TestPermissionLevels.DENIED.getText());
    assertEquals("DENIED", TestPermissionLevels.DENIED.getStringValue());
    assertTrue(TestPermissionLevels.DENIED.isActive());
    assertEquals(TestPermissionLevels.LEVEL_DENIED, TestPermissionLevels.DENIED.getValue());

    PermissionLevel.registerOrOverride(TestPermissionLevels.LEVEL_DENIED, "DENIED_X", false, () -> "DENIED_Y");
    assertEquals("DENIED_Y", TestPermissionLevels.DENIED.getText());
    assertEquals("DENIED", TestPermissionLevels.DENIED.getStringValue());
    assertFalse(TestPermissionLevels.DENIED.isActive());
    assertEquals(TestPermissionLevels.LEVEL_DENIED, TestPermissionLevels.DENIED.getValue());

    assertSame(TestPermissionLevels.DENIED, PermissionLevel.get(TestPermissionLevels.LEVEL_DENIED));
  }

  @Test
  public void testAllLevelsStream() throws Exception {
    Set<PermissionLevel> levels = PermissionLevel.all();
    assertTrue(levels.containsAll(Arrays.asList(PermissionLevel.ALL, PermissionLevel.NONE, PermissionLevel.UNDEFINED, TestPermissionLevels.GRANTED, TestPermissionLevels.DENIED)));
  }

  @Test
  public void testSerialization() throws Exception {
    assertSame(TestPermissionLevels.DENIED, CloneUtility.createDeepCopyBySerializing(TestPermissionLevels.DENIED));
    assertSame(TestPermissionLevels.DENIED, PermissionLevel.get(TestPermissionLevels.LEVEL_DENIED));
  }

  @Test
  public void testDeserializeUnknown() throws Exception {
    Constructor<?> ctor = PermissionLevel.class.getDeclaredConstructors()[0];
    ctor.setAccessible(true);
    PermissionLevel unknownLevel = (PermissionLevel) ctor.newInstance(17, "17U", true, (Supplier<String>) () -> "17UT");

    assertNull(PermissionLevel.opt(17));

    PermissionLevel deserializedUnknownLevel = CloneUtility.createDeepCopyBySerializing(unknownLevel);
    assertSame(deserializedUnknownLevel, PermissionLevel.get(17));
    assertEquals(deserializedUnknownLevel, CloneUtility.createDeepCopyBySerializing(unknownLevel));
    assertEquals(PermissionLevel.get(17), CloneUtility.createDeepCopyBySerializing(deserializedUnknownLevel));
  }
}
