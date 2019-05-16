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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.UUID;
import java.util.stream.Stream;

import org.eclipse.scout.rt.dataobject.id.IUuId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests the structure of {@link IUuId} classes.
 * <p>
 * This test is parameterized. Implementing classes can use the static {@link #streamStringIdClasses(String)} method to
 * specify the parameters. Implementing classes have to add a static method, like the following one:
 *
 * <pre>
 * &#64;Parameters(name = "{0}")
 * public static Iterable<? extends Object> parameters() {
 *   return streamUuIdClasses("com.my.package.base.name")
 *       // add additional filters if required
 *       .collect(Collectors.toList());
 * }
 * </pre>
 */
@RunWith(Parameterized.class)
public abstract class AbstractUuIdStructureTest extends AbstractIdStructureTest {

  private static final UUID TEST_UUID = UUID.randomUUID();

  protected static Stream<Class<? extends IUuId>> streamUuIdClasses(String packageNameFilter) {
    return streamIdClasses(packageNameFilter)
        .filter(c -> IUuId.class.isAssignableFrom(c))
        .map(c -> c.asSubclass(IUuId.class));
  }

  public AbstractUuIdStructureTest(Class<? extends IUuId> id) {
    super(id);
  }

  @Override
  protected Class<? extends IUuId> getIdClass() {
    @SuppressWarnings("unchecked")
    Class<? extends IUuId> idClass = (Class<? extends IUuId>) super.getIdClass();
    return idClass;
  }

  @Test
  public void createMethod() throws ReflectiveOperationException, SecurityException, IllegalAccessException, IllegalArgumentException {
    Method create = getIdClass().getDeclaredMethod("create");
    assertNotNull("Method 'create()' is missing", create);
    int mod = create.getModifiers();
    assertTrue("Method is expected public static, but it is " + Modifier.toString(mod), Modifier.isStatic(mod) && Modifier.isPublic(mod));
    assertEquals(getIdClass(), create.getReturnType());

    // invoke method
    IUuId id = getIdClass().cast(create.invoke(null));
    assertNotNull("create must not return null", id);
    assertNotNull("unwrapped UUID must not be null", id.unwrap());
  }

  @Test
  public void ofUuidMethod() throws ReflectiveOperationException, SecurityException, IllegalAccessException, IllegalArgumentException {
    Method of = getIdClass().getDeclaredMethod("of", UUID.class);
    assertNotNull("Method 'of(UUID)' is missing", of);
    int mod = of.getModifiers();
    assertTrue("Method is expected public static, but it is " + Modifier.toString(mod), Modifier.isStatic(mod) && Modifier.isPublic(mod));
    assertEquals(getIdClass(), of.getReturnType());

    // invoke method
    IUuId id = (IUuId) of.invoke(null, TEST_UUID);
    assertNotNull("of must not return null", id);
    assertEquals("unwrapped UUID must be equal to the one the ID was created for", TEST_UUID, id.unwrap());
  }

  @Test
  @Override
  public void ofStringMethod() throws ReflectiveOperationException, SecurityException, IllegalAccessException, IllegalArgumentException {
    Method of = getIdClass().getDeclaredMethod("of", String.class);
    assertNotNull("Method 'of(String)' is missing", of);
    int mod = of.getModifiers();
    assertTrue("Method is expected public static, but it is " + Modifier.toString(mod), Modifier.isStatic(mod) && Modifier.isPublic(mod));
    assertEquals(getIdClass(), of.getReturnType());

    // invoke method
    IUuId id = (IUuId) of.invoke(null, TEST_UUID.toString());
    assertNotNull("of must not return null", id);
    assertEquals("unwrapped UUID must be equal to the one the ID was created for", TEST_UUID, id.unwrap());
  }
}
