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
package org.eclipse.scout.rt.dataobject.testing;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import org.eclipse.scout.rt.dataobject.id.ILongId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests the structure of {@link ILongId} classes.
 * <p>
 * This test is parameterized. Implementing classes can use the static {@link #streamLongIdClasses(String)} method to
 * specify the parameters. Implementing classes have to add a static method, like the following one:
 *
 * <pre>
 * &#64;Parameters(name = "{0}")
 * public static Iterable<? extends Object> parameters() {
 *   return streamLongIdClasses("com.my.package.base.name")
 *       // add additional filters if required
 *       .collect(Collectors.toList());
 * }
 * </pre>
 */
@RunWith(Parameterized.class)
public abstract class AbstractLongIdStructureTest extends AbstractIdStructureTest {

  private static final Long TEST_LONG = Long.valueOf(42L);

  protected static Stream<Class<? extends ILongId>> streamLongIdClasses(String packageNameFilter) {
    return streamIdClasses(packageNameFilter)
        .filter(ILongId.class::isAssignableFrom)
        .map(c -> c.asSubclass(ILongId.class));
  }

  public AbstractLongIdStructureTest(Class<? extends ILongId> id) {
    super(id);
  }

  @Override
  protected Class<? extends ILongId> getIdClass() {
    @SuppressWarnings("unchecked")
    Class<? extends ILongId> idClass = (Class<? extends ILongId>) super.getIdClass();
    return idClass;
  }

  @Test
  public void ofLongMethod() throws ReflectiveOperationException {
    Method of = getIdClass().getDeclaredMethod("of", Long.class);
    assertNotNull("Method 'of(Long)' is missing", of);
    int mod = of.getModifiers();
    assertTrue("Method is expected public static, but it is " + Modifier.toString(mod), Modifier.isStatic(mod) && Modifier.isPublic(mod));
    assertEquals(getIdClass(), of.getReturnType());

    // invoke method
    ILongId id = (ILongId) of.invoke(null, TEST_LONG);
    assertNotNull("of must not return null", id);
    assertEquals("unwrapped Long must be equal to the one the ID was created for", TEST_LONG, id.unwrap());
  }

  @Test
  @Override
  public void ofStringMethod() throws ReflectiveOperationException {
    Method of = getIdClass().getDeclaredMethod("of", String.class);
    assertNotNull("Method 'of(String)' is missing", of);
    int mod = of.getModifiers();
    assertTrue("Method is expected public static, but it is " + Modifier.toString(mod), Modifier.isStatic(mod) && Modifier.isPublic(mod));
    assertEquals(getIdClass(), of.getReturnType());

    // invoke method
    ILongId id = (ILongId) of.invoke(null, TEST_LONG.toString());
    assertNotNull("of must not return null", id);
    assertEquals("unwrapped Long must be equal to the one the ID was created for", TEST_LONG, id.unwrap());
  }
}
