/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.reflect;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Test;

/**
 * Testcases for ConfigurationUtility
 */
public class ConfigurationUtilityTest {

  @Test
  public void testGetDeclaredMethod() {
    runTestGetDeclaredMethod(Fixture.class, "privateMethod", int.class);
    runTestGetDeclaredMethod(Fixture.class, "defaultMethod", long.class);
    runTestGetDeclaredMethod(Fixture.class, "protectedMethod", float.class);
    runTestGetDeclaredMethod(Fixture.class, "publicMethod", double.class);

    runTestGetDeclaredMethod(SubFixture.class, "privateSubMethod", int.class);
    runTestGetDeclaredMethod(SubFixture.class, "defaultSubMethod", long.class);
    runTestGetDeclaredMethod(SubFixture.class, "protectedSubMethod", float.class);
    runTestGetDeclaredMethod(SubFixture.class, "publicSubMethod", double.class);
    runTestGetDeclaredMethod(SubFixture.class, "publicMethod", double.class);

    assertNull(ConfigurationUtility.getDeclaredMethod(SubFixture.class, "privateMethod", new Class[]{int.class}));
    assertNull(ConfigurationUtility.getDeclaredMethod(SubFixture.class, "defaultMethod", new Class[]{long.class}));
    assertNull(ConfigurationUtility.getDeclaredMethod(SubFixture.class, "protectedMethod", new Class[]{float.class}));
  }

  @Test
  public void testGetAllDeclaredMethods() {
    Method[] methods = ConfigurationUtility.getAllDeclaredMethods(Fixture.class);
    // NOTE: do not assert method count and exact method set since test framework agents could add additional methods (e.g. $jacocoInit method used for profiling)
    assertTrue(CollectionUtility.containsAll(Arrays.stream(methods).map(Method::getName).collect(Collectors.toSet()), Set.of("privateMethod", "defaultMethod", "protectedMethod", "publicMethod")));
    assertSame(methods, ConfigurationUtility.getAllDeclaredMethods(Fixture.class));

    Method[] methodsSub = ConfigurationUtility.getAllDeclaredMethods(SubFixture.class);
    // NOTE: do not assert method count and exact method set since test framework agents could add additional methods (e.g. $jacocoInit method used for profiling)
    assertTrue(CollectionUtility.containsAll(Arrays.stream(methodsSub).map(Method::getName).collect(Collectors.toSet()), Set.of("privateSubMethod", "defaultSubMethod", "protectedSubMethod", "publicSubMethod", "publicMethod")));
    assertSame(methodsSub, ConfigurationUtility.getAllDeclaredMethods(SubFixture.class));
  }

  @Test
  public void testIsMethodOverride() {
    assertTrue(ConfigurationUtility.isMethodOverwrite(Fixture.class, "publicMethod", new Class[]{double.class}, SubFixture.class));
    assertTrue(ConfigurationUtility.isMethodOverwrite(Fixture.class, "protectedMethod", new Class[]{float.class}, SubSubFixture.class));

    // method overridden by SubFixture
    assertTrue(ConfigurationUtility.isMethodOverwrite(Fixture.class, "publicMethod", new Class[]{double.class}, SubSubFixture.class));

    // method not overridden
    assertFalse(ConfigurationUtility.isMethodOverwrite(Fixture.class, "protectedMethod", new Class[]{float.class}, SubFixture.class));

    // method on same class
    assertFalse(ConfigurationUtility.isMethodOverwrite(Fixture.class, "publicMethod", new Class[]{double.class}, Fixture.class));

    // non-existent method
    assertFalse(ConfigurationUtility.isMethodOverwrite(Fixture.class, "nonExistentMethod", new Class[]{double.class}, SubFixture.class));

    // wrong parameters
    assertFalse(ConfigurationUtility.isMethodOverwrite(Fixture.class, "publicMethod", new Class[]{int.class}, SubFixture.class));
  }

  protected void runTestGetDeclaredMethod(Class clazz, String methodName, Class paramType) {
    Method m = ConfigurationUtility.getDeclaredMethod(clazz, methodName, new Class[]{paramType});
    assertEquals(methodName, m.getName());
    assertEquals(paramType, m.getParameterTypes()[0]);
    assertEquals(void.class, m.getReturnType());
  }

  @SuppressWarnings("unused")
  private static class Fixture {
    private void privateMethod(int i) {
    }

    void defaultMethod(long l) {
    }

    protected void protectedMethod(float f) {
    }

    public void publicMethod(double d) {
    }
  }

  @SuppressWarnings("unused")
  private static class SubFixture extends Fixture {
    private void privateSubMethod(int i) {
    }

    void defaultSubMethod(long l) {
    }

    protected void protectedSubMethod(float f) {
    }

    public void publicSubMethod(double d) {
    }

    @Override
    public void publicMethod(double d) {
      // overridden method fixture
      super.publicMethod(d);
    }
  }

  @SuppressWarnings("unused")
  private static class SubSubFixture extends SubFixture {
    @Override
    protected void protectedMethod(float f) {
      // overridden method fixture
      super.protectedMethod(f);
    }
  }
}
