/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import static org.eclipse.scout.rt.platform.util.CollectionUtility.*;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.reflect.ReflectionUtility;
import org.junit.Test;

public class ReflectionUtilityTest {

  @Test
  public void testGetInterfaces() {
    assertEquals(emptyHashSet(), hashSet(ReflectionUtility.getInterfaces(Object.class)));
    assertEquals(hashSet(Runnable.class), hashSet(ReflectionUtility.getInterfaces(Runnable.class)));
    assertEquals(hashSet(Callable.class), hashSet(ReflectionUtility.getInterfaces(Callable.class)));
    assertEquals(hashSet(Callable.class), hashSet(ReflectionUtility.getInterfaces(Callable.class)));
    assertEquals(emptyHashSet(), hashSet(ReflectionUtility.getInterfaces(Top.class)));
    assertEquals(hashSet(Runnable.class, Serializable.class), hashSet(ReflectionUtility.getInterfaces(Middle.class)));
    assertEquals(hashSet(Callable.class, Runnable.class, Serializable.class), hashSet(ReflectionUtility.getInterfaces(Bottom.class)));
  }

  @Test
  public void testConstructor() {
    assertNotNull(ReflectionUtility.getConstructor(Bottom.class, new Class<?>[]{String.class, Integer.class}));
    assertNull(ReflectionUtility.getConstructor(Bottom.class, new Class<?>[]{Integer.class, String.class}));
  }

  // === Test classes ===

  public static class Top {
  }

  public static class Middle extends Top implements Runnable, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public void run() {
    }
  }

  public static class Bottom extends Middle implements Callable {

    private static final long serialVersionUID = 1L;

    public Bottom(String string, Integer integer) {
    }

    @Override
    public Object call() {
      return null;
    }
  }
}
