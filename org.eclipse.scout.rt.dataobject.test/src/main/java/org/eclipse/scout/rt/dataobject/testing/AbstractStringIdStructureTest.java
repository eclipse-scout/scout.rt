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

import org.eclipse.scout.rt.dataobject.id.IStringId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests the structure of {@link IStringId} classes.
 * <p>
 * This test is parameterized. Implementing classes can use the static {@link #streamStringIdClasses(String)} method to
 * specify the parameters. Implementing classes have to add a static method, like the following one:
 *
 * <pre>
 * &#64;Parameters(name = "{0}")
 * public static Iterable<? extends Object> parameters() {
 *   return streamStringIdClasses("com.my.package.base.name")
 *       // add additional filters if required
 *       .collect(Collectors.toList());
 * }
 * </pre>
 */
@RunWith(Parameterized.class)
public abstract class AbstractStringIdStructureTest extends AbstractIdStructureTest {

  private static final String TEST_ID = "aaabbbccc";

  protected static Stream<Class<? extends IStringId>> streamStringIdClasses(String packageNameFilter) {
    return streamIdClasses(packageNameFilter)
        .filter(IStringId.class::isAssignableFrom)
        .map(c -> c.asSubclass(IStringId.class));
  }

  public AbstractStringIdStructureTest(Class<? extends IStringId> stringId) {
    super(stringId);
  }

  @Override
  protected Class<? extends IStringId> getIdClass() {
    @SuppressWarnings("unchecked")
    Class<? extends IStringId> idClass = (Class<? extends IStringId>) super.getIdClass();
    return idClass;
  }

  @Test
  public void invokeOfStringMethod() throws ReflectiveOperationException {
    Method of = getIdClass().getDeclaredMethod("of", String.class);
    assertNotNull("Method 'of(String) is missing", of);
    int mod = of.getModifiers();
    assertTrue("Method is expected public static, but it is " + Modifier.toString(mod), Modifier.isStatic(mod) && Modifier.isPublic(mod));
    assertEquals(getIdClass(), of.getReturnType());

    // invoke method
    IStringId id = (IStringId) of.invoke(null, TEST_ID);
    assertNotNull("of must not return null", id);
    assertEquals("unwrapped string must be equal to the one the ID was created for", TEST_ID, id.unwrap());
  }
}
