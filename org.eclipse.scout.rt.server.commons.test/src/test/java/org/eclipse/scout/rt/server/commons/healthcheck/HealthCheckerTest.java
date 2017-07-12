package org.eclipse.scout.rt.server.commons.healthcheck;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import org.mockito.invocation.InvocationOnMock;
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
      protected boolean execCheckHealth() throws Exception {
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
      future.awaitDone();
    }
  }

  protected Stubber doSleep(final long millis) {
    return Mockito.doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        TimeUnit.MILLISECONDS.sleep(millis);
        return null;
      }
    });
  }

  @Test
  public void testAsync() throws Exception {
    final AtomicBoolean flag = new AtomicBoolean(false);
    final Object sync = new Object();

    IHealthChecker checker = createDummyHealthChecker(true, TimeUnit.DAYS.toMillis(1), 1, new IRunnable() {
      @Override
      public void run() throws Exception {
        synchronized (sync) {
          flag.set(true);
          sync.notify();
          sync.wait();
          flag.set(false);
          sync.notify();
        }
      }
    });

    // ping-pong synchronization: main(checkHealth) -> checker(flag=true) -> main(assertEquals) -> checker(flag=false) -> main(assertEquals)
    synchronized (sync) {
      checker.checkHealth(RunContexts.empty());
      sync.wait();
      assertEquals("flag check 1", true, flag.get());
      sync.notify();
      sync.wait();
      assertEquals("flag check 2", false, flag.get());
    }
  }

  @Test
  public void testTimeout() throws Exception {
    final AtomicBoolean sleep = new AtomicBoolean(false);

    AbstractHealthChecker checker = createDummyHealthChecker(true, 0, TimeUnit.SECONDS.toMillis(1), new IRunnable() {
      @Override
      public void run() throws Exception {
        if (sleep.get()) {
          TimeUnit.SECONDS.sleep(10);
        }
      }
    });

    // run without sleep
    assertEquals("resultInitial", false, checker.checkHealth(RunContexts.empty()));
    awaitDone(checker.getFuture());

    // run with sleep
    sleep.set(true);
    assertEquals("resultT", true, checker.checkHealth(RunContexts.empty()));

    // wait for timeout
    TimeUnit.SECONDS.sleep(2);

    // fails due to timeout
    assertEquals("resultF", false, checker.checkHealth(RunContexts.empty()));
  }

  @Test
  public void testTimeToLive() throws Exception {
    IRunnable test = mock(IRunnable.class);

    AbstractHealthChecker checker = createDummyHealthChecker(true, TimeUnit.SECONDS.toMillis(1), 0, test);

    // start & validate first run
    checker.checkHealth(RunContexts.empty());
    awaitDone(checker.getFuture());
    verify(test, times(1)).run();
    assertEquals("result", true, checker.checkHealth(RunContexts.empty()));

    // spam checking
    for (int i = 0; i < 10; i++) {
      assertEquals("resultX" + i, true, checker.checkHealth(RunContexts.empty()));
    }

    // validate executions again
    verify(test, times(1)).run();

    // wait for TTL to expire
    TimeUnit.SECONDS.sleep(1);

    // start and validate again
    checker.checkHealth(RunContexts.empty());
    awaitDone(checker.getFuture());
    verify(test, times(2)).run();
    assertEquals("resultZ", true, checker.checkHealth(RunContexts.empty()));
  }

  @Test
  public void testNoTimeToLive() throws Exception {
    IRunnable test = mock(IRunnable.class);
    doSleep(TimeUnit.SECONDS.toMillis(1)).when(test).run();

    AbstractHealthChecker checker = createDummyHealthChecker(true, 0, 0, test);

    // spam checking
    for (int i = 0; i < 10; i++) {
      assertEquals("resultX" + i, false, checker.checkHealth(RunContexts.empty()));
    }

    // validate
    awaitDone(checker.getFuture());
    verify(test, times(1)).run();
    assertEquals("result", true, checker.checkHealth(RunContexts.empty()));
  }

  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void testErrors() throws Exception {
    final AtomicBoolean throwException = new AtomicBoolean(false);

    AbstractHealthChecker checker = createDummyHealthChecker(true, 0, 0, new IRunnable() {
      int counter = 0;

      @Override
      public void run() throws Exception {
        counter++;
        if (throwException.get()) {
          throw new ArrayIndexOutOfBoundsException(counter);
        }
      }
    });

    // start and validate success
    checker.checkHealth(RunContexts.empty());

    // flip-flap between OK and exception throwing
    for (int i = 0; i < 10; i++) {
      // flip switch before checking the result as the checking of the result triggers a new check (TTL = 0)
      awaitDone(checker.getFuture());
      throwException.set(true);

      assertEquals("resultT:" + i, true, checker.checkHealth(RunContexts.empty()));

      // validate exception and flip switch back
      awaitDone(checker.getFuture());
      throwException.set(false);

      assertEquals("resultF:" + i, false, checker.checkHealth(RunContexts.empty()));
    }

    // verify that the checker now stopped failing as there was no exception thrown
    awaitDone(checker.getFuture());
    assertEquals("resultZ", true, checker.checkHealth(RunContexts.empty()));
  }

}
