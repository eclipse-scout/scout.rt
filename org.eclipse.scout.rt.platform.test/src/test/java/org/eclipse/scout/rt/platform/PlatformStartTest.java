/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test if Platform.get with autostart does allow for reentrant calls to Platform.get
 */
public class PlatformStartTest {

  private static ClassLoader oldContextLoader;
  private static IPlatform oldPlatform;

  @BeforeClass
  public static void beforeClass() {
    oldContextLoader = Thread.currentThread().getContextClassLoader();
    oldPlatform = Platform.peek();
  }

  @AfterClass
  public static void afterClass() {
    Platform.set(oldPlatform);
    Thread.currentThread().setContextClassLoader(oldContextLoader);
  }

  @Test
  public void testInitialize() {
    ClassLoader loader = new PlatformOverrideClassLoader(getClass().getClassLoader(), FixturePlatformWithReentrantCall.class);
    Thread.currentThread().setContextClassLoader(loader);

    Platform.set(null);
    IPlatform p = Platform.get();
    p.awaitPlatformStarted();
    assertNotNull(p);
    assertEquals(FixturePlatformWithReentrantCall.class, p.getClass());
    assertSame(p, FixturePlatformWithReentrantCall.reentrantPlatform);
  }

  @Test
  public void testStartStop() {
    Platform.set(new FixturePlatformWithMinimalBeanManager());
    IPlatform p = Platform.get();
    p.start();
    p.awaitPlatformStarted();

    p.stop();
    assertEquals(IPlatform.State.PlatformStopped, p.getState());

    p.start();
    p.awaitPlatformStarted();
    assertEquals(IPlatform.State.PlatformStarted, p.getState());

    p.stop();
    assertEquals(IPlatform.State.PlatformStopped, p.getState());
  }

  public static class FixturePlatformWithReentrantCall extends DefaultPlatform {
    static IPlatform reentrantPlatform;

    @Override
    protected BeanManagerImplementor createBeanManager() {
      BeanManagerImplementor context = new BeanManagerImplementor();
      context.registerClass(SimpleBeanDecorationFactory.class);
      //make a recursive call to the platform
      reentrantPlatform = Platform.get();
      reentrantPlatform.getBeanManager();
      return context;
    }

    @Override
    protected void validateConfiguration() {
      // nop
    }
  }

  public static class FixturePlatformWithMinimalBeanManager extends DefaultPlatform {

    @Override
    protected BeanManagerImplementor createBeanManager() {
      BeanManagerImplementor context = new BeanManagerImplementor();
      context.registerClass(SimpleBeanDecorationFactory.class);
      return context;
    }

    @Override
    protected void validateConfiguration() {
      // nop
    }
  }
}
