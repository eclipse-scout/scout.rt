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
package org.eclipse.scout.rt.client.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.filter.AlwaysFilter;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.ICallable;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IJobInput;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.internal.ModelJobManager;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class ModelJobScheduleTest {

  private ModelJobManager m_jobManager;

  private Subject m_subject1 = new Subject();
  private Subject m_subject2 = new Subject();

  @Mock
  private IClientSession m_clientSession1;
  @Mock
  private IClientSession m_clientSession2;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);

    m_jobManager = new ModelJobManager();

    // initialize ClientSession1
    when(m_clientSession1.getLocale()).thenReturn(new Locale("de", "DE"));
    when(m_clientSession1.getTexts()).thenReturn(new ScoutTexts());
    when(m_clientSession1.getUserAgent()).thenReturn(newUserAgent());

    // initialize ClientSession2
    m_clientSession2 = mock(IClientSession.class);
    when(m_clientSession2.getLocale()).thenReturn(new Locale("de", "CH"));
    when(m_clientSession2.getTexts()).thenReturn(new ScoutTexts());
    when(m_clientSession2.getUserAgent()).thenReturn(newUserAgent());

    IClientSession.CURRENT.set(m_clientSession1);
  }

  @After
  public void after() {
    m_jobManager.shutdown();
    IClientSession.CURRENT.remove();
  }

  @Test
  public void testMissingSession() throws JobExecutionException {
    ISession.CURRENT.remove(); // ensure no session installed.

    final AtomicReference<Boolean> running = new AtomicReference<Boolean>(false);

    IFuture<Void> future = null;
    try {
      future = m_jobManager.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          running.set(true);
        }
      });
      fail();
    }
    catch (AssertionException e) {
      assertFalse(running.get());
      assertNull(future);
    }
  }

  @Test
  public void testMissingJobInput() throws JobExecutionException {
    final AtomicReference<Boolean> running = new AtomicReference<Boolean>(false);

    IFuture<Void> future = null;
    try {
      future = m_jobManager.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          running.set(true);
        }
      }, null);
      fail();
    }
    catch (AssertionException e) {
      assertFalse(running.get());
      assertNull(future);
    }
  }

  @Test
  public void testWithCallable() throws ProcessingException {
    IFuture<String> future = m_jobManager.schedule(new ICallable<String>() {

      @Override
      public String call() throws Exception {
        return "running";
      }
    });

    // VERIFY
    assertEquals("running", future.get());
    assertTrue(future.isDone());
  }

  @Test
  public void testWithRunnable() throws ProcessingException {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.
    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("running");
      }
    });

    // VERIFY
    assertNull(future.get());
    assertEquals(CollectionUtility.hashSet("running"), protocol);
    assertTrue(future.isDone());
  }

  @Test
  public void testProcessingExceptionWithRunnable() throws ProcessingException {
    final ProcessingException exception = new ProcessingException();

    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        throw exception;
      }
    });

    try {
      future.get();
      fail("Exception expected");
    }
    catch (Exception e) {
      assertSame(exception, e);
      assertTrue(future.isDone());
    }
  }

  @Test
  public void testProcessingExceptionWithCallable() throws ProcessingException {
    final ProcessingException exception = new ProcessingException();

    IFuture<Void> future = m_jobManager.schedule(new ICallable<Void>() {

      @Override
      public Void call() throws Exception {
        throw exception;
      }
    });

    try {
      future.get();
      fail("Exception expected");
    }
    catch (Exception e) {
      assertSame(exception, e);
      assertTrue(future.isDone());
    }
  }

  @Test
  public void testRuntimeExceptionWithRunnable() throws ProcessingException {
    final RuntimeException exception = new RuntimeException();

    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        throw exception;
      }
    });

    try {
      future.get();
      fail("Exception expected");
    }
    catch (Exception e) {
      assertTrue(e instanceof ProcessingException);
      assertSame(exception, e.getCause());
      assertTrue(future.isDone());
    }
  }

  @Test
  public void testRuntimeExceptionWithCallable() throws ProcessingException {
    final RuntimeException exception = new RuntimeException();

    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        throw exception;
      }
    });

    try {
      future.get();
      fail("Exception expected");
    }
    catch (Exception e) {
      assertTrue(e instanceof ProcessingException);
      assertSame(exception, e.getCause());
      assertTrue(future.isDone());
    }
  }

  @Test
  public void testExceptionExceptionWithRunnable() throws ProcessingException {
    final Exception exception = new Exception();

    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        throw exception;
      }
    });

    try {
      future.get();
      fail("Exception expected");
    }
    catch (Exception e) {
      assertTrue(e instanceof ProcessingException);
      assertSame(exception, e.getCause());
      assertTrue(future.isDone());
    }
  }

  @Test
  public void testExceptionExceptionWithCallable() throws ProcessingException {
    final Exception exception = new Exception();

    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        throw exception;
      }
    });

    try {
      future.get();
      fail("Exception expected");
    }
    catch (Exception e) {
      assertTrue(e instanceof ProcessingException);
      assertSame(exception, e.getCause());
      assertTrue(future.isDone());
    }
  }

  @Test
  public void testModelThread() throws ProcessingException, InterruptedException {
    final Set<Thread> protocol = Collections.synchronizedSet(new HashSet<Thread>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(2);

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(Thread.currentThread());
        setupLatch.countDown();

        m_jobManager.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            protocol.add(Thread.currentThread());
            setupLatch.countDown();
          }
        });
      }
    });

    assertTrue(setupLatch.await());

    assertEquals(2, protocol.size());
    assertFalse(protocol.contains(Thread.currentThread()));
  }

  @Test
  public void testThreadName() throws ProcessingException, InterruptedException {
    Thread.currentThread().setName("main");

    final Holder<String> actualThreadName1 = new Holder<>();
    final Holder<String> actualThreadName2 = new Holder<>();

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(2);

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualThreadName1.setValue(Thread.currentThread().getName());
        setupLatch.countDown();

        m_jobManager.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            actualThreadName2.setValue(Thread.currentThread().getName());
            setupLatch.countDown();
          }
        }, ClientJobInput.defaults().name("XYZ"));
        System.out.println("ASDF");
      }
    }, ClientJobInput.defaults().name("ABC"));

    assertTrue(setupLatch.await());

    assertTrue(actualThreadName1.getValue().matches("scout-model-thread-(\\d)+;ABC"));
    assertTrue(actualThreadName2.getValue().matches("scout-model-thread-(\\d)+;XYZ"));
    assertEquals("main", Thread.currentThread().getName());
  }

  @Test
  public void testCurrentFuture() throws Exception {
    final Holder<IFuture<?>> expectedFuture1 = new Holder<>();
    final Holder<IFuture<?>> expectedFuture2 = new Holder<>();

    final Holder<IFuture<?>> actualFuture1 = new Holder<>();
    final Holder<IFuture<?>> actualFuture2 = new Holder<>();

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(2);

    IFuture.CURRENT.remove();

    expectedFuture1.setValue(m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualFuture1.setValue(IFuture.CURRENT.get());
        setupLatch.countDown();

        expectedFuture2.setValue(m_jobManager.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            actualFuture2.setValue(IFuture.CURRENT.get());
            setupLatch.countDown();
          }
        }));
      }
    }));

    assertTrue(setupLatch.await());

    assertNotNull(expectedFuture1.getValue());
    assertNotNull(expectedFuture2.getValue());

    assertSame(expectedFuture1.getValue(), actualFuture1.getValue());
    assertSame(expectedFuture2.getValue(), actualFuture2.getValue());

    assertNull(IFuture.CURRENT.get());
  }

  @Test
  public void testScheduleAndGet() throws ProcessingException {
    final List<Integer> protocol = Collections.synchronizedList(new ArrayList<Integer>()); // synchronized because modified/read by different threads.

    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        Thread.sleep(500); // Wait some time to test that 'Future.get' blocks.
        protocol.add(1);
      }
    });
    future.get();
    protocol.add(2);

    assertEquals(Arrays.asList(1, 2), protocol);
  }

  @Test
  public void testNoParallelExecution() throws Exception {
    final BlockingCountDownLatch barrier = new BlockingCountDownLatch(3, 3, TimeUnit.SECONDS);

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        barrier.countDownAndBlock();
      }
    });

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        barrier.countDownAndBlock();
      }
    });

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        barrier.countDownAndBlock();
      }
    });

    assertFalse(barrier.await());
    barrier.unblock();
  }

  @Test
  public void testScheduleDelayed() throws ProcessingException {
    final AtomicLong tRunning = new AtomicLong();

    long tScheduled = System.nanoTime();
    String result = m_jobManager.schedule(new ICallable<String>() {

      @Override
      public String call() throws Exception {
        tRunning.set(System.nanoTime());
        return "executed";
      }
    }, 2, TimeUnit.SECONDS).get(5, TimeUnit.SECONDS);

    assertEquals("executed", result);
    long delta = tRunning.get() - tScheduled;
    assertTrue(delta >= TimeUnit.SECONDS.toNanos(2));
  }

  @Test
  public void testExpired() throws ProcessingException {
    final AtomicBoolean executed = new AtomicBoolean(false);

    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        executed.set(true);
      }
    }, 1, TimeUnit.SECONDS, ClientJobInput.defaults().expirationTime(500, TimeUnit.MILLISECONDS));

    try {
      future.get();
      fail();
    }
    catch (JobExecutionException e) {
      assertFalse(executed.get());
      assertTrue(e.isCancellation());
    }
  }

  @Test
  public void testExpireNever() throws ProcessingException {
    final AtomicBoolean executed = new AtomicBoolean(false);

    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        executed.set(true);
      }
    }, 1, TimeUnit.SECONDS, ClientJobInput.defaults().expirationTime(IJobInput.INFINITE_EXPIRATION, TimeUnit.MILLISECONDS));

    future.get();
    assertTrue(executed.get());
  }

  @Test
  public void testModelJobContext() throws Exception {
    ISession.CURRENT.remove();
    NlsLocale.CURRENT.remove();
    ScoutTexts.CURRENT.remove();
    UserAgent.CURRENT.remove();
    JobContext.CURRENT.remove();

    final Holder<JobContext> actualJobContext1 = new Holder<>();
    final Holder<JobContext> actualJobContext2 = new Holder<>();

    final Holder<ISession> actualClientSession1 = new Holder<>();
    final Holder<ISession> actualClientSession2 = new Holder<>();

    final Holder<Locale> actualLocale1 = new Holder<>();
    final Holder<Locale> actualLocale2 = new Holder<>();

    final Holder<UserAgent> actualUserAgent1 = new Holder<>();
    final Holder<UserAgent> actualUserAgent2 = new Holder<>();

    final Holder<ScoutTexts> actualTexts1 = new Holder<>();
    final Holder<ScoutTexts> actualTexts2 = new Holder<>();

    final Holder<Subject> actualSubject1 = new Holder<>();
    final Holder<Subject> actualSubject2 = new Holder<>();

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualClientSession1.setValue(IClientSession.CURRENT.get());
        actualLocale1.setValue(NlsLocale.CURRENT.get());
        actualTexts1.setValue(ScoutTexts.CURRENT.get());
        actualSubject1.setValue(Subject.getSubject(AccessController.getContext()));
        actualUserAgent1.setValue(UserAgent.CURRENT.get());

        // Job context
        JobContext ctx1 = JobContext.CURRENT.get();
        ctx1.set("PROP_JOB1", "J1");
        ctx1.set("PROP_JOB1+JOB2", "SHARED-1");
        actualJobContext1.setValue(ctx1);

        m_jobManager.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            actualClientSession2.setValue(IClientSession.CURRENT.get());
            actualLocale2.setValue(NlsLocale.CURRENT.get());
            actualTexts2.setValue(ScoutTexts.CURRENT.get());
            actualSubject2.setValue(Subject.getSubject(AccessController.getContext()));
            actualUserAgent2.setValue(UserAgent.CURRENT.get());

            // Job context
            JobContext ctx2 = JobContext.CURRENT.get();
            ctx2.set("PROP_JOB2", "J2");
            ctx2.set("PROP_JOB1+JOB2", "SHARED-2");
            actualJobContext2.setValue(ctx2);
          }
        }, ClientJobInput.defaults().session(m_clientSession2).subject(m_subject2));
      }
    }, ClientJobInput.defaults().session(m_clientSession1).subject(m_subject1));

    assertTrue(m_jobManager.waitUntilDone(new AlwaysFilter<IFuture<?>>(), 10, TimeUnit.SECONDS));

    assertSame(m_clientSession1, actualClientSession1.getValue());
    assertSame(m_clientSession2, actualClientSession2.getValue());
    assertNull(ISession.CURRENT.get());

    assertSame(m_clientSession1.getLocale(), actualLocale1.getValue());
    assertSame(m_clientSession2.getLocale(), actualLocale2.getValue());
    assertNull(NlsLocale.CURRENT.get());

    assertSame(m_clientSession1.getUserAgent(), actualUserAgent1.getValue());
    assertSame(m_clientSession2.getUserAgent(), actualUserAgent2.getValue());
    assertNull(UserAgent.CURRENT.get());

    assertSame(m_clientSession1.getTexts(), actualTexts1.getValue());
    assertSame(m_clientSession2.getTexts(), actualTexts2.getValue());
    assertNull(ScoutTexts.CURRENT.get());

    assertSame(m_subject1, actualSubject1.getValue());
    assertSame(m_subject2, actualSubject2.getValue());
    assertNull(Subject.getSubject(AccessController.getContext()));

    assertNotNull(actualJobContext1.getValue());
    assertNotNull(actualJobContext2.getValue());
    assertNotSame("JobContext should be a copy", actualJobContext1.getValue(), actualJobContext2.getValue());

    assertEquals("J1", actualJobContext1.getValue().get("PROP_JOB1"));
    assertEquals("SHARED-1", actualJobContext1.getValue().get("PROP_JOB1+JOB2"));
    assertNull(actualJobContext1.getValue().get("PROP_JOB2"));

    assertEquals("J1", actualJobContext2.getValue().get("PROP_JOB1"));
    assertEquals("J2", actualJobContext2.getValue().get("PROP_JOB2"));
    assertEquals("SHARED-2", actualJobContext2.getValue().get("PROP_JOB1+JOB2"));
    assertNull(actualJobContext1.getValue().get("JOB2"));

    assertNull(JobContext.CURRENT.get());
  }

  private static UserAgent newUserAgent() {
    return UserAgent.create(UiLayer.UNKNOWN, UiDeviceType.UNKNOWN, "n/a");
  }
}
