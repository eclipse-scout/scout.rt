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

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class PostConstructTest {

  private static IBean<Bean01> m_bean01;
  private static IBean<Bean02> m_bean02;
  private static IBean<Bean04> m_bean04;

  @BeforeClass
  public static void registerBeans() {
    m_bean01 = Platform.get().getBeanManager().registerClass(Bean01.class);
    m_bean02 = Platform.get().getBeanManager().registerClass(Bean02.class);
    m_bean04 = Platform.get().getBeanManager().registerClass(Bean04.class);

  }

  /**
   * Tests if an initialize method (annotated with @PostConstruct) is called when a bean gets instanciated. An singleton
   * (annotated with @ApplicationScoped) is only created once therefore the initialze method is also only called once on
   * several lookups.
   */
  @Test
  public void testInitialize() {
    Assert.assertEquals(0, Bean01.m_initializedCounter.get());
    BEANS.get(Bean01.class);
    Assert.assertEquals(1, Bean01.m_initializedCounter.get());

    BEANS.get(Bean01.class);
    Assert.assertEquals(1, Bean01.m_initializedCounter.get());
  }

  @Test
  public void testInitialzeInHierarchy() {
    Bean02 new1 = BEANS.get(Bean02.class);
    Assert.assertEquals(1, new1.getSuperInitCount());
    Assert.assertEquals(1, new1.getInitCount());
    new1 = BEANS.get(Bean02.class);
    Assert.assertEquals(1, new1.getSuperInitCount());
    Assert.assertEquals(1, new1.getInitCount());

  }

  @Test
  public void testInitializeInHierarchyWithSameMethodNames() {
    Bean04 new1 = BEANS.get(Bean04.class);
    Assert.assertEquals(1, new1.getBean04InitCount());
    Assert.assertEquals(1, new1.getAbstractBean03InitCount());
    new1 = BEANS.get(Bean04.class);
    Assert.assertEquals(1, new1.getBean04InitCount());
    Assert.assertEquals(1, new1.getAbstractBean03InitCount());

  }

  @AfterClass
  public static void removeBeans() {
    Platform.get().getBeanManager().unregisterBean(m_bean01);
    Platform.get().getBeanManager().unregisterBean(m_bean02);
    Platform.get().getBeanManager().unregisterBean(m_bean04);
  }

  @ApplicationScoped
  public static class Bean01 {
    private static AtomicInteger m_initializedCounter = new AtomicInteger(0);

    @PostConstruct
    private void initialize() {
      m_initializedCounter.incrementAndGet();
    }
  }

  @ApplicationScoped
  private static abstract class AbstractBean02 {
    private AtomicInteger m_superInitCount = new AtomicInteger();

    @PostConstruct
    public void post() {
      m_superInitCount.incrementAndGet();
    }

    public int getSuperInitCount() {
      return m_superInitCount.get();
    }

  }

  private static class Bean02 extends AbstractBean02 {
    private AtomicInteger m_InitCount = new AtomicInteger();

    @PostConstruct
    private void localPost() {
      m_InitCount.incrementAndGet();
    }

    public int getInitCount() {
      return m_InitCount.get();
    }
  }

  @ApplicationScoped
  private static abstract class AbstractBean03 {
    private AtomicInteger m_AbstractBean03Init = new AtomicInteger();

    @PostConstruct
    private void localPost() {
      m_AbstractBean03Init.incrementAndGet();
    }

    public int getAbstractBean03InitCount() {
      return m_AbstractBean03Init.get();
    }
  }

  private static class Bean04 extends AbstractBean03 {
    private AtomicInteger m_Bean04Init = new AtomicInteger();

    @PostConstruct
    private void localPost() {
      m_Bean04Init.incrementAndGet();
    }

    public int getBean04InitCount() {
      return m_Bean04Init.get();
    }
  }

}
