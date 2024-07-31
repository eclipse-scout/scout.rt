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

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link BeanManagerImplementor#isBean(Class)}
 */
@RunWith(PlatformTestRunner.class)
public class BeanManagerIsBeanTest {

  private static class TestObject {
  }

  @Test
  public void testIsBean() {
    BeanManagerImplementor context = new BeanManagerImplementor(new SimpleBeanDecorationFactory());

    // test object is a bean, as soon as it was registered within bean manager
    assertFalse(context.isBean(TestObject.class));
    IBean<?> reg = context.registerClass(TestObject.class);
    assertTrue(context.isBean(TestObject.class));
    context.unregisterBean(reg);
    assertFalse(context.isBean(TestObject.class));

    // JRE classes are no beans
    assertFalse(context.isBean(String.class));
  }


  @Test
  public void testUnregisterBean() throws InterruptedException {
    long startTime = System.nanoTime();
    long delta = 2_000_000_000L;
    BeanTestingHelper helper = BEANS.get(BeanTestingHelper.class);
    AtomicReference<Exception> occurredException = new AtomicReference<>();
    Thread t1 = new Thread(() -> {
      while (System.nanoTime() - startTime < delta) {
        try {
          BEANS.get(TestA.class);
        }
        catch (Exception e) {
          occurredException.set(e);
        }
      }
    });
    Thread t2 = new Thread(() -> {
      while (System.nanoTime() - startTime < delta) {
        IBean<Object> bean = helper.registerBean(new BeanMetaData(TestB.class).withReplace(true));
        helper.unregisterBean(bean);
      }
    });
    t1.start();
    t2.start();
    Thread.sleep(2_000);
    Assert.assertNull("Exception occurred: " + occurredException, occurredException.get());
  }

  @Bean
  public static class TestA {
  }

  @IgnoreBean
  public static class TestB extends TestA {
  }
}
