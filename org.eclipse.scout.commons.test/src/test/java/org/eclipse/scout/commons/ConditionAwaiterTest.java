/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConditionAwaiterTest {

  private static ExecutorService s_executor;

  private Lock m_lock;
  private Condition m_condition;

  @BeforeClass
  public static void beforeClass() {
    s_executor = Executors.newCachedThreadPool();
  }

  @AfterClass
  public static void afterClass() {
    s_executor.shutdownNow();
  }

  @Before
  public void before() {
    m_lock = new ReentrantLock();
    m_condition = m_lock.newCondition();

  }

  @Test(timeout = 10000)
  public void testAwaitTrueCondition() throws InterruptedException, TimeoutException {
    final ConditionAwaiter waiter = new ConditionAwaiter(m_lock, m_condition) {

      @Override
      protected boolean evaluateCondition() {
        return true;
      }
    };

    waiter.await();
    assertTrue(waiter.await(0, TimeUnit.NANOSECONDS) <= 0);
  }

  @Test(timeout = 10000)
  public void testAwaitCondition() throws InterruptedException {
    final AtomicBoolean ok = new AtomicBoolean();

    final ConditionAwaiter waiter = new ConditionAwaiter(m_lock, m_condition) {

      @Override
      protected boolean evaluateCondition() {
        return ok.get();
      }
    };

    final BlockingCountDownLatch latch = new BlockingCountDownLatch(1);

    s_executor.execute(new Runnable() {

      @Override
      public void run() {
        try {
          waiter.await();
          latch.countDown();
        }
        catch (InterruptedException e) {
          // NOOP
        }
      }
    });
    s_executor.execute(new Runnable() {

      @Override
      public void run() {
        sleep(2000);

        m_lock.lock();
        try {
          ok.set(true);
          m_condition.signalAll();
        }
        finally {
          m_lock.unlock();
        }
      }
    });

    assertTrue(latch.await());
  }

  @Test(timeout = 10000)
  public void testAwaitTimeout() throws InterruptedException {
    final AtomicLong remainingNanos = new AtomicLong(Long.MAX_VALUE);

    final ConditionAwaiter waiter = new ConditionAwaiter(m_lock, m_condition) {

      @Override
      protected boolean evaluateCondition() {
        return false;
      }
    };

    final BlockingCountDownLatch latch = new BlockingCountDownLatch(1);

    s_executor.execute(new Runnable() {

      @Override
      public void run() {
        try {
          remainingNanos.set(waiter.await(2, TimeUnit.SECONDS));
          latch.countDown();
        }
        catch (InterruptedException e) {
          // NOOP
        }
      }
    });

    assertTrue(latch.await());
    assertTrue(remainingNanos.get() <= 0);
  }

  @Test(timeout = 10000)
  public void testAwaitConditionWithTimeout() throws InterruptedException {
    final AtomicBoolean ok = new AtomicBoolean();

    final AtomicLong remainingNanos = new AtomicLong(Long.MAX_VALUE);

    final ConditionAwaiter waiter = new ConditionAwaiter(m_lock, m_condition) {

      @Override
      protected boolean evaluateCondition() {
        return ok.get();
      }
    };

    final BlockingCountDownLatch latch = new BlockingCountDownLatch(1);

    s_executor.execute(new Runnable() {

      @Override
      public void run() {
        try {
          remainingNanos.set(waiter.await(10, TimeUnit.SECONDS));
          latch.countDown();
        }
        catch (InterruptedException e) {
          // NOOP
        }
      }
    });
    s_executor.execute(new Runnable() {

      @Override
      public void run() {
        sleep(2000);

        m_lock.lock();
        try {
          ok.set(true);
          m_condition.signalAll();
        }
        finally {
          m_lock.unlock();
        }
      }
    });

    assertTrue(latch.await());
    assertTrue(remainingNanos.get() > 0);
  }

  private static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    }
    catch (InterruptedException e) {
      // NOOP
    }
  }
}
