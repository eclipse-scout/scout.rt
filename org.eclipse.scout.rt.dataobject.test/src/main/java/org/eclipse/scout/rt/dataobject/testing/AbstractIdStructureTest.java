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
package org.eclipse.scout.rt.dataobject.testing;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.stream.Stream;

import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.IUuId;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
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
        .filter(ci -> !ci.isAbstract() && !ci.isInterface())
        .sorted(Comparator.comparing(IClassInfo::name))
        .map(ci -> ci.resolveClass())
        .filter(c -> packageNameFilter == null || c.getName().startsWith(packageNameFilter))
        .map(c -> c.asSubclass(IId.class));
  }

  private final Class<? extends IId<?>> m_idClass;

  protected Class<? extends IId<?>> getIdClass() {
    return m_idClass;
  }

  public AbstractIdStructureTest(Class<? extends IId<?>> idClass) {
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
    Class<?> wrappedTypeClass = TypeCastUtility.getGenericsParameterClass(getIdClass(), IId.class);
    Method of = getIdClass().getDeclaredMethod("of", wrappedTypeClass);
    assertNotNull("Method 'of(" + wrappedTypeClass.getName() + ")' is missing", of);
    int mod = of.getModifiers();
    assertTrue("Method is expected public static, but it is " + Modifier.toString(mod), Modifier.isStatic(mod) && Modifier.isPublic(mod));
    assertEquals(getIdClass(), of.getReturnType());

    // invoke method with null
    IId<?> id = (IId<?>) of.invoke(null, (Object) null);
    assertNull("of(null) must return null", id);
  }

  @Test
  public void ofStringMethod() throws ReflectiveOperationException {
    Method of = getIdClass().getDeclaredMethod("of", String.class);
    assertNotNull("Method 'of(String)' is missing", of);
    int mod = of.getModifiers();
    assertTrue("Method is expected public static, but it is " + Modifier.toString(mod), Modifier.isStatic(mod) && Modifier.isPublic(mod));
    assertEquals(getIdClass(), of.getReturnType());

    // invoke method
    IId id = (IUuId) of.invoke(null, (String) null);
    assertNull("of(null) must return null", id);
  }
}
