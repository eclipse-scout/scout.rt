/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;
import org.eclipse.scout.rt.platform.internal.PlatformImplementor;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 * <h3>{@link PreDestroyTest}</h3>
 */
public class PreDestroyTest {

  private static Set<String> events = new HashSet<>();

  @AfterClass
  public static void cleanup() {
    events = null;
  }

  @Before
  public void prepareTest() {
    events.clear(); // reset for next test
  }

  @Test
  public void testPreDestroyOnUnusedBean() {
    IPlatform platform = privatePlatformWith(
        new BeanMetaData(P_FixtureWithSingleValidPreDestroy.class)
            .withApplicationScoped(true));
    platform.stop();
    assertTrue(events.isEmpty());
  }

  @Test
  public void testPreDestroyOnNonApplicationScopedBean() {
    IPlatform platform = privatePlatformWith(
        new BeanMetaData(P_FixtureWithSingleValidPreDestroy.class));
    P_FixtureWithSingleValidPreDestroy instance = platform.getBeanManager().getBean(P_FixtureWithSingleValidPreDestroy.class).getInstance();
    assertNotNull(instance);
    platform.stop();
    assertTrue(events.isEmpty());
  }

  @Test
  public void testPreDestroyOnSimpleBean() {
    IPlatform platform = privatePlatformWith(
        new BeanMetaData(P_FixtureWithSingleValidPreDestroy.class)
            .withApplicationScoped(true));
    P_FixtureWithSingleValidPreDestroy instance = platform.getBeanManager().getBean(P_FixtureWithSingleValidPreDestroy.class).getInstance();
    assertNotNull(instance);
    platform.stop();
    assertEquals(CollectionUtility.hashSet("cleanup"), events);
  }

  @Test
  public void testPreDestroyThrowingException() {
    IPlatform platform = privatePlatformWith(new BeanMetaData(P_FixtureWithPreDestroyThrowingException.class).withApplicationScoped(true));
    assertNotNull(platform.getBeanManager().getBean(P_FixtureWithPreDestroyThrowingException.class).getInstance());
    platform.stop();

    assertEquals(CollectionUtility.hashSet("cleanupOne", "cleanupTwo"), events);
  }

  @Test
  public void testPreDestroyWithInheritedMethods() {
    IPlatform platform = privatePlatformWith(new BeanMetaData(P_FixtureWithInherited.class).withApplicationScoped(true));
    assertNotNull(platform.getBeanManager().getBean(P_FixtureWithInherited.class).getInstance());
    platform.stop();

    assertEquals(CollectionUtility.hashSet("cleanupOne", "cleanupTwoInherited"), events);
  }

  @Test
  public void testPreDestroyWithInheritedPrivateMethods() {
    IPlatform platform = privatePlatformWith(new BeanMetaData(P_FixtureWithInheritedPrivate.class).withApplicationScoped(true));
    assertNotNull(platform.getBeanManager().getBean(P_FixtureWithInheritedPrivate.class).getInstance()); // create the instance
    platform.stop();

    assertEquals(CollectionUtility.hashSet("cleanupChild", "cleanup"), events);
  }

  private static class P_FixtureWithSingleValidPreDestroy {

    @PreDestroy
    private final void cleanup() {
      events.add("cleanup");
    }

    @PreDestroy
    private static void cleanupStatic() {
      events.add("cleanupStatic");
    }

    @PreDestroy
    private void cleanupWithArgs(int param) {
      events.add("cleanupWithArgs");
    }
  }

  private static class P_FixtureWithPreDestroyThrowingException {
    @PreDestroy
    public void cleanupOne() throws Exception {
      events.add("cleanupOne");
      throw new Exception("test");
    }

    @PreDestroy
    public void cleanupTwo() {
      events.add("cleanupTwo");
    }
  }

  private static final class P_FixtureWithInherited extends P_FixtureWithPreDestroyThrowingException {
    @Override
    public void cleanupTwo() {
      events.add("cleanupTwoInherited");
    }
  }

  private static final class P_FixtureWithInheritedPrivate extends P_FixtureWithSingleValidPreDestroy {
    @PreDestroy
    private final void cleanup() {
      events.add("cleanupChild");
    }
  }

  private IPlatform privatePlatformWith(final BeanMetaData bean) {
    PlatformImplementor platform = new PlatformImplementor() {

      @Override
      protected void validateConfiguration() {
        // nop
      }

      @Override
      protected void initBeanDecorationFactory() {
        // nop
      }

      @Override
      protected void validateHeadless() {
        // nop
      }

      @Override
      protected BeanManagerImplementor createBeanManager() {
        BeanManagerImplementor beanManager = new BeanManagerImplementor();
        beanManager.registerBean(bean);
        return beanManager;
      }
    };
    platform.start();
    return platform;
  }
}
