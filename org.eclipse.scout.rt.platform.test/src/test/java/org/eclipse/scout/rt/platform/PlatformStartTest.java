/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import static org.junit.Assert.assertSame;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test if Platform.get with autostart does allow for reentrant calls to Platform.get
 */
public class PlatformStartTest {
  private static final Logger LOG = LoggerFactory.getLogger(PlatformStartTest.class);

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
  public void testPerformance() throws Exception {
    ClassLoader loader = new PlatformOverrideClassLoader(getClass().getClassLoader(), FixturePlatformWithReentrantCall.class);
    Thread.currentThread().setContextClassLoader(loader);

    Platform.set(null);
    IPlatform p = Platform.get();
    p.awaitPlatformStarted();

    final AtomicLong dtSum = new AtomicLong();
    Thread[] t = new Thread[5];
    for (int k = 0; k < t.length; k++) {
      t[k] = new Thread(new Runnable() {
        @Override
        public void run() {
          int n = 100000000;
          long t0 = System.nanoTime();
          State state = null;
          for (int i = 0; i < n; i++) {
            state = Platform.get().getState();
          }
          long t1 = System.nanoTime();
          assertNotNull(state);
          dtSum.addAndGet(t1 - t0);
        }
      });
      t[k].start();
    }
    for (int i = 0; i < t.length; i++) {
      t[i].join();
    }
    long dtMillis = dtSum.longValue() / t.length / 1000000L;
    if (dtMillis > 2000L) {
      LOG.error("This test must complete within 2 seconds. It took {}ms.", dtMillis);
    }
  }

  @Test
  public void testStartStop() throws Exception {
    Platform.set(new FixturePlatformWithMinimalBeanManager());
    IPlatform p = Platform.get();
    p.start(new PlatformStateLatch());
    p.awaitPlatformStarted();

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
    static IPlatform reentrantPlatform;

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
