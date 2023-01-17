/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.healthcheck;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

/**
 * Test for {@link AbstractHealthChecker}
 */
@RunWith(PlatformTestRunner.class)
public class HealthCheckerTest {

  @Rule
  public Timeout testTimeout = Timeout.seconds(30);

  protected AbstractHealthChecker createDummyHealthChecker(final boolean value, final long ttl, final long timeout, final IRunnable exec) {
    final String name = UUID.randomUUID().toString();

    AbstractHealthChecker checker = new AbstractHealthChecker() {
      @Override
      protected String getConfiguredName() {
        return name;
      }

      @Override
      protected long getConfiguredTimeToLiveMillis() {
        return ttl;
      }

      @Override
      protected long getConfiguredTimeoutMillis() {
        return timeout;
      }

      @Override
      protected boolean execCheckHealth(HealthCheckCategoryId category) throws Exception {
        exec.run();
        return value;
      }
    };

    assertEquals("Name", name, checker.m_name);
    assertEquals("TimeToLive", ttl, checker.m_timeToLive);
    assertEquals("Timeout", timeout, checker.m_timeout);

    return checker;
  }

  protected void awaitDone(IFuture future) {
    if (future != null) {
      future.awaitDoneAndGet();
    }
  }

  protected Stubber doSleep(final long millis) {
    return Mockito.doAnswer((Answer<Object>) invocation -> {
      TimeUnit.MILLISECONDS.sleep(millis);
      return null;
    });
  }

  @Test
  public void testAsync() throws Exception {
    final AtomicBoolean flag = new AtomicBoolean(false);
    final Object sync = new Object();

    IHealthChecker checker = createDummyHealthChecker(true, TimeUnit.DAYS.toMillis(1), 1, () -> {
      synchronized (sync) {
        flag.set(true);
        sync.notify();
        sync.wait();
        flag.set(false);
        sync.notify();
      }
    });

    // ping-pong synchronization: main(checkHealth) -> checker(flag=true) -> main(assertEquals) -> checker(flag=false) -> main(assertEquals)
    synchronized (sync) {
      checker.checkHealth(RunContexts.empty(), null);
      sync.wait();
      assertTrue("flag check 1", flag.get());
      sync.notify();
      sync.wait();
      assertFalse("flag check 2", flag.get());
    }
  }

  @Test
  public void testTimeout() throws Exception {
    final AtomicBoolean sleep = new AtomicBoolean(false);

    AbstractHealthChecker checker = createDummyHealthChecker(true, 0, TimeUnit.SECONDS.toMillis(1), () -> {
      if (sleep.get()) {
        TimeUnit.SECONDS.sleep(10);
      }
    });

    // run without sleep
    assertFalse("resultInitial", checker.checkHealth(RunContexts.empty(), null));
    awaitDone(checker.getFuture());

    // run with sleep
    sleep.set(true);
    assertTrue("resultT", checker.checkHealth(RunContexts.empty(), null));

    // wait for timeout
    TimeUnit.SECONDS.sleep(2);

    // fails due to timeout
    assertFalse("resultF", checker.checkHealth(RunContexts.empty(), null));
  }

  @Test
  public void testTimeToLive() throws Exception {
    IRunnable test = mock(IRunnable.class);

    AbstractHealthChecker checker = createDummyHealthChecker(true, TimeUnit.SECONDS.toMillis(1), 0, test);

    // start & validate first run
    checker.checkHealth(RunContexts.empty(), null);
    awaitDone(checker.getFuture());
    verify(test, times(1)).run();
    assertTrue("result", checker.checkHealth(RunContexts.empty(), null));

    // spam checking
    for (int i = 0; i < 10; i++) {
      assertTrue("resultX" + i, checker.checkHealth(RunContexts.empty(), null));
    }

    // validate executions again
    verify(test, times(1)).run();

    // wait for TTL to expire
    // Note: Unit tests with timing is always problematic. Waiting just the TTL of 1 second is not enough in case the unit test system is under high load.
    TimeUnit.SECONDS.sleep(1 + 2);

    // start and validate again
    checker.checkHealth(RunContexts.empty(), null);
    awaitDone(checker.getFuture());
    verify(test, times(2)).run();
    assertTrue("resultZ", checker.checkHealth(RunContexts.empty(), null));
  }

  @Test
  public void testNoTimeToLive() throws Exception {
    IRunnable test = mock(IRunnable.class);
    doSleep(TimeUnit.SECONDS.toMillis(1)).when(test).run();

    AbstractHealthChecker checker = createDummyHealthChecker(true, 0, 0, test);

    // spam checking
    for (int i = 0; i < 10; i++) {
      assertFalse("resultX" + i, checker.checkHealth(RunContexts.empty(), null));
    }

    // validate
    awaitDone(checker.getFuture());
    verify(test, times(1)).run();
    assertTrue("result", checker.checkHealth(RunContexts.empty(), null));
  }

  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void testErrors() throws Exception {
    final AtomicBoolean throwException = new AtomicBoolean(false);

    AbstractHealthChecker checker = createDummyHealthChecker(true, 0, 0, new IRunnable() {
      int counter = 0;

      @Override
      public void run() {
        counter++;
        if (throwException.get()) {
          throw new ArrayIndexOutOfBoundsException(counter);
        }
      }
    });

    // start and validate success
    checker.checkHealth(RunContexts.empty(), null);

    // flip-flap between OK and exception throwing
    for (int i = 0; i < 10; i++) {
      // flip switch before checking the result as the checking of the result triggers a new check (TTL = 0)
      awaitDone(checker.getFuture());
      throwException.set(true);

      assertTrue("resultT:" + i, checker.checkHealth(RunContexts.empty(), null));

      // validate exception and flip switch back
      awaitDone(checker.getFuture());
      throwException.set(false);

      assertFalse("resultF:" + i, checker.checkHealth(RunContexts.empty(), null));
    }

    // verify that the checker now stopped failing as there was no exception thrown
    awaitDone(checker.getFuture());
    assertTrue("resultZ", checker.checkHealth(RunContexts.empty(), null));
  }

}
