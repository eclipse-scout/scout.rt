/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.testing;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

import org.eclipse.scout.rt.dataobject.id.ICompositeId;
import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.IRootId;
import org.eclipse.scout.rt.dataobject.id.IdFactory;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.platform.util.date.IDateProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests the structure of {@link IId} classes.
 * <p>
 * This test is parameterized. Implementing classes can use the static {@link #streamIdClasses(String)} method to
 * specify the parameters.
 *
 * <pre>
 * &#64;Parameters(name = "{0}")
 * public static Iterable<? extends Object> parameters() {
 *   return streamIdClasses("com.my.package.base.name")
 *       // add additional filters if required
 *       .collect(Collectors.toList());
 * }
 * </pre>
 */
@RunWith(Parameterized.class)
public abstract class AbstractIdStructureTest {

  protected static Stream<Class<? extends IId>> streamIdClasses(String packageNameFilter) {
    return ClassInventory.get().getAllKnownSubClasses(IId.class).stream()
        .filter(ci -> !ci.isAbstract() && !ci.isInterface() && !ci.hasAnnotation(IgnoreBean.class))
        .sorted(Comparator.comparing(IClassInfo::name))
        .map(IClassInfo::resolveClass)
        .filter(c -> packageNameFilter == null || c.getName().startsWith(packageNameFilter))
        .map(c -> c.asSubclass(IId.class));
  }

  private final Class<? extends IId> m_idClass;

  protected Class<? extends IId> getIdClass() {
    return m_idClass;
  }

  public AbstractIdStructureTest(Class<? extends IId> idClass) {
    m_idClass = idClass;
  }

  @Test
  public void classIsFinal() {
    assertTrue(Modifier.isFinal(getIdClass().getModifiers()));
  }

  @Test
  public void constructorsArePrivate() {
    Constructor<?>[] constructors = getIdClass().getDeclaredConstructors();
    assertTrue(constructors.length > 0);
    for (Constructor<?> ctor : constructors) {
      assertTrue("Constructors are expected to be private: " + ctor, Modifier.isPrivate(ctor.getModifiers()));
    }
  }

  @Test
  public void ofTypeMethod() throws ReflectiveOperationException {
    List<Class<?>> parameterTypes = BEANS.get(IdFactory.class).getRawTypes(getIdClass());
    Method of = getIdClass().getDeclaredMethod("of", parameterTypes.toArray(new Class[]{}));
    assertNotNull("Method 'of(" + parameterTypes + ")' is missing", of);
    int mod = of.getModifiers();
    assertTrue("Method is expected public static, but it is " + Modifier.toString(mod), Modifier.isStatic(mod) && Modifier.isPublic(mod));
    assertEquals(getIdClass(), of.getReturnType());

    // invoke method with nulls
    testOfTypeNullValues(parameterTypes, of);

    // invoke with test values
    testOfTypeMockValues(parameterTypes, of);
  }

  protected void testOfTypeNullValues(List<Class<?>> parameterTypes, Method of) throws IllegalAccessException, InvocationTargetException {
    Object[] params = new Object[parameterTypes.size()];
    Arrays.fill(params, null);
    IId id = (IId) of.invoke(null, params);
    assertNull("of(null,null...) must return null if invoked with all null values", id);

    // check empty values for various default root values
    if (parameterTypes.size() == 1 && parameterTypes.get(0) == String.class) {
      assertNull("of(\"\") must return null if invoked with empty string", of.invoke(null, ""));
    }
    else if (parameterTypes.size() == 1 && parameterTypes.get(0) == Long.class) {
      assertNull("of(0L) must return null if invoked with zero long value", of.invoke(null, 0L));
    }
    else if (parameterTypes.size() == 1 && parameterTypes.get(0) == Integer.class) {
      assertNull("of(0) must return null if invoked with zero int value", of.invoke(null, 0));
    }
  }

  protected void testOfTypeMockValues(List<Class<?>> parameterTypes, Method of) throws ReflectiveOperationException {
    if (IRootId.class.isAssignableFrom(getIdClass())) {
      Class<?> wrappedType = parameterTypes.get(0);
      assertEquals("of method parameter type must be equal to generic type of id class", wrappedType, TypeCastUtility.getGenericsParameterClass(getIdClass(), IRootId.class));

      // invoke method with test value
      Object value = getMockValue(wrappedType);
      IRootId id = (IRootId) of.invoke(null, value);
      assertNotNull("of must not return null", id);
      assertEquals("class type of class under test must be equal to class type of created id", getIdClass(), id.getClass());
      assertEquals("unwrapped value must be equal to the one the ID was created for", value, id.unwrap());
    }
    else {
      // invoke method with test values
      Object[] params = parameterTypes.stream().map(this::getMockValue).toArray();
      ICompositeId id = (ICompositeId) of.invoke(null, params);
      assertNotNull("of(" + Arrays.toString(params) + ") must not return null", id);
      assertArrayEquals(params, unwrapId(id).toArray());
    }
  }

  protected List<Object> unwrapId(IId id) {
    List<Object> rawComponents = new ArrayList<>();
    unwrapId(id, rawComponents);
    return rawComponents;
  }

  protected void unwrapId(IId id, List<Object> rawComponents) {
    if (id instanceof ICompositeId) {
      (((ICompositeId) id).unwrap()).forEach(idComponent -> unwrapId(idComponent, rawComponents));
    }
    else {
      rawComponents.add(id.unwrap());
    }
  }

  protected Object getMockValue(Class<?> clazz) {
    if (clazz == String.class) {
      return "abc";
    }
    else if (clazz == UUID.class) {
      return UUID.fromString("5860fb13-875a-43db-9204-c501edf3d2ed");
    }
    else if (clazz == Long.class) {
      return 42L;
    }
    else if (clazz == Integer.class) {
      return 42;
    }
    else if (clazz == Date.class) {
      return BEANS.get(IDateProvider.class).currentMillis();
    }
    else if (clazz == Locale.class) {
      return Locale.US;
    }
    else if (clazz == Boolean.class) {
      return Boolean.FALSE;
    }
    return getCustomMockValue(clazz);
  }

  /**
   * Implement this method to provide mock test data for additional raw types.
   */
  protected Object getCustomMockValue(Class<?> clazz) {
    throw new AssertionError("Missing mock data for IId " + getIdClass() + " using  wrapper type " + clazz);
  }
}
