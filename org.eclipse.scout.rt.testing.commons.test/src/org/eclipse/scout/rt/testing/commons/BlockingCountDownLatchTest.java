/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG-initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.CollectionUtility;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BlockingCountDownLatchTest {

  private static ScheduledExecutorService s_executor;

  @BeforeClass
  public static void beforeClass() {
    s_executor = Executors.newScheduledThreadPool(5);
  }

  @AfterClass
  public static void afterClass() {
    s_executor.shutdown();
  }

  @Test
  public void test() throws InterruptedException {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>());

    final BlockingCountDownLatch testee = new BlockingCountDownLatch(3);
    final CountDownLatch latch = new CountDownLatch(3);

    s_executor.schedule(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        protocol.add("1-beforeCountDownAndBlock");
        try {
          testee.countDownAndBlock();
        }
        finally {
          protocol.add("1-afterCountDownAndBlock");
          latch.countDown();
        }
        return null;
      }
    }, 1, TimeUnit.SECONDS);
    s_executor.schedule(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        protocol.add("2-beforeCountDownAndBlock");
        try {
          testee.countDownAndBlock();
        }
        finally {
          protocol.add("2-afterCountDownAndBlock");
          latch.countDown();
        }
        return null;
      }
    }, 1, TimeUnit.SECONDS);
    s_executor.schedule(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        protocol.add("3-beforeCountDownAndBlock");
        try {
          testee.countDownAndBlock();
        }
        finally {
          protocol.add("3-afterCountDownAndBlock");
          latch.countDown();
        }
        return null;
      }
    }, 1, TimeUnit.SECONDS);

    final CountDownLatch latch4 = new CountDownLatch(1);
    s_executor.schedule(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        protocol.add("4-beforeCountDownAndBlock");
        try {
          testee.countDown();
        }
        finally {
          protocol.add("4-afterCountDownAndBlock");
          latch4.countDown();
        }
        return null;
      }
    }, 1, TimeUnit.SECONDS);
    assertTrue(latch4.await(30, TimeUnit.SECONDS));

    testee.await();

    Set<String> expected = new HashSet<>();
    expected.add("1-beforeCountDownAndBlock");
    expected.add("2-beforeCountDownAndBlock");
    expected.add("3-beforeCountDownAndBlock");
    expected.add("4-beforeCountDownAndBlock");
    expected.add("4-afterCountDownAndBlock");
    assertEquals(expected, CollectionUtility.hashSet(protocol));

    testee.unblock();

    assertTrue(latch.await(30, TimeUnit.SECONDS));

    expected.add("1-afterCountDownAndBlock");
    expected.add("2-afterCountDownAndBlock");
    expected.add("3-afterCountDownAndBlock");
    assertEquals(expected, CollectionUtility.hashSet(protocol));
  }
}
