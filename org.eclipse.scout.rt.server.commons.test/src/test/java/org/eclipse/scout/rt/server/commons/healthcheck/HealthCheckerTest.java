package org.eclipse.scout.rt.server.commons.healthcheck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.ThrowableCauseMatcher.hasCause;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.hamcrest.core.IsInstanceOf;
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

  @Test(expected = TimedOutError.class)
  public void testTimeout() throws Exception {
    final Object sync = new Object();

    IHealthChecker checker = createDummyHealthChecker(true, TimeUnit.DAYS.toMillis(1001), 1, new IRunnable() {
      @Override
      public void run() throws Exception {
        // synchronize execution
        synchronized (sync) {
          sync.notifyAll();
        }
        // sleep for 1001 nights
        TimeUnit.DAYS.sleep(1001);
      }
    });
    // synchronize execution
    synchronized (sync) {
      checker.checkHealth(RunContexts.empty());
      sync.wait();
    }
    // wait for timeout
    TimeUnit.MILLISECONDS.sleep(2);
    // throws TimedOutError
    checker.checkHealth(RunContexts.empty());
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
      @Override
      public void run() throws Exception {
        if (throwException.get()) {
          throw new ArrayIndexOutOfBoundsException(-1);
        }
      }
    });

    // start and validate success
    checker.checkHealth(RunContexts.empty());
    awaitDone(checker.getFuture());

    // flip switch before checking the result as the checking of the result triggers a new check (TTL = 0)
    throwException.set(true);
    assertEquals("result1", true, checker.checkHealth(RunContexts.empty()));

    // validate exception
    awaitDone(checker.getFuture());
    try {
      checker.checkHealth(RunContexts.empty());
    }
    catch (ProcessingException pe) {
      assertThat(pe, hasCause(IsInstanceOf.any(ArrayIndexOutOfBoundsException.class)));
    }
  }

}
