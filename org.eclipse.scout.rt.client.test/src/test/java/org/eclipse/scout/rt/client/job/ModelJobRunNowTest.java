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
import java.util.concurrent.atomic.AtomicReference;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.ICallable;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.internal.ModelJobManager;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.ui.UiDeviceType;
import org.eclipse.scout.rt.shared.ui.UiLayer;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.platform.ScoutPlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(ScoutPlatformTestRunner.class)
public class ModelJobRunNowTest {

  private IModelJobManager m_jobManager;

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

    ISession.CURRENT.set(m_clientSession1);
  }

  @After
  public void after() {
    m_jobManager.shutdown();

    ISession.CURRENT.remove();
  }

  @Test
  public void testMissingSession() throws Throwable {
    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        String result = null;
        try {
          result = m_jobManager.runNow(new ICallable<String>() {

            @Override
            public String call() throws Exception {
              return "executed";
            }
          }, ClientJobInput.empty());
          fail();
        }
        catch (AssertionException e) {
          assertNull(result);
        }
      }
    });

    // Wait for the job to complete and propagate assertion errors.
    getAndThrowCause(future);
  }

  @Test
  public void testMissingJobInput() throws Throwable {
    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        String result = null;
        try {
          result = m_jobManager.runNow(new ICallable<String>() {

            @Override
            public String call() throws Exception {
              return "executed";
            }
          }, null);
          fail();
        }
        catch (AssertionException e) {
          assertNull(result);
        }
      }
    });

    // Wait for the job to complete and propagate assertion errors.
    getAndThrowCause(future);
  }

  @Test
  public void testRunNowNotModelThread() throws ProcessingException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    try {
      m_jobManager.runNow(new IRunnable() {

        @Override
        public void run() throws Exception {
          protocol.add("running");
        }
      });
      fail();
    }
    catch (AssertionException e) {
      assertTrue(protocol.isEmpty());
    }
  }

  @Test
  public void testWithCallable() throws Throwable {
    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        String result = m_jobManager.runNow(new ICallable<String>() {

          @Override
          public String call() throws Exception {
            return "running";
          }
        });

        assertEquals("running", result);
      }
    });

    // Wait for the job to complete and propagate assertion errors.
    getAndThrowCause(future);
  }

  @Test
  public void testVoidResult() throws Throwable {
    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.
        m_jobManager.runNow(new IRunnable() {

          @Override
          public void run() throws Exception {
            protocol.add("running");
          }
        });

        assertEquals(CollectionUtility.hashSet("running"), protocol);
      }
    });

    // Wait for the job to complete and propagate assertion errors.
    getAndThrowCause(future);
  }

  @Test
  public void testProcessingExceptionWithRunnable() throws Throwable {
    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        final ProcessingException exception = new ProcessingException();

        try {
          m_jobManager.runNow(new IRunnable() {

            @Override
            public void run() throws Exception {
              throw exception;
            }
          });
          fail("Exception expected");
        }
        catch (Exception e) {
          assertSame(exception, e);
        }
      }
    });

    // Wait for the job to complete and propagate assertion errors.
    getAndThrowCause(future);
  }

  @Test
  public void testProcessingExceptionWithCallable() throws Throwable {
    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        final ProcessingException exception = new ProcessingException();

        try {
          m_jobManager.runNow(new ICallable<Void>() {

            @Override
            public Void call() throws Exception {
              throw exception;
            }
          });
          fail("Exception expected");
        }
        catch (Exception e) {
          assertSame(exception, e);
        }
      }
    });

    // Wait for the job to complete and propagate assertion errors.
    getAndThrowCause(future);
  }

  @Test
  public void testRuntimeExceptionWithRunnable() throws Throwable {
    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        final RuntimeException exception = new RuntimeException();

        try {
          m_jobManager.runNow(new IRunnable() {

            @Override
            public void run() throws Exception {
              throw exception;
            }
          });
          fail("Exception expected");
        }
        catch (Exception e) {
          assertTrue(e instanceof ProcessingException);
          assertSame(exception, e.getCause());
        }
      }
    });

    // Wait for the job to complete and propagate assertion errors.
    getAndThrowCause(future);
  }

  @Test
  public void testRuntimeExceptionWithCallable() throws Throwable {
    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        final RuntimeException exception = new RuntimeException();

        try {
          m_jobManager.runNow(new IRunnable() {

            @Override
            public void run() throws Exception {
              throw exception;
            }
          });
          fail("Exception expected");
        }
        catch (Exception e) {
          assertTrue(e instanceof ProcessingException);
          assertSame(exception, e.getCause());
        }
      }
    });

    // Wait for the job to complete and propagate assertion errors.
    getAndThrowCause(future);
  }

  @Test
  public void testExceptionExceptionWithRunnable() throws Throwable {
    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        final Exception exception = new Exception();

        try {
          m_jobManager.runNow(new IRunnable() {

            @Override
            public void run() throws Exception {
              throw exception;
            }
          });
          fail("Exception expected");
        }
        catch (Exception e) {
          assertTrue(e instanceof ProcessingException);
          assertSame(exception, e.getCause());
        }
      }
    });

    // Wait for the job to complete and propagate assertion errors.
    getAndThrowCause(future);
  }

  @Test
  public void testExceptionExceptionWithCallable() throws Throwable {
    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        final Exception exception = new Exception();

        try {
          m_jobManager.runNow(new IRunnable() {

            @Override
            public void run() throws Exception {
              throw exception;
            }
          });
          fail("Exception expected");
        }
        catch (Exception e) {
          assertTrue(e instanceof ProcessingException);
          assertSame(exception, e.getCause());
        }
      }
    });

    // Wait for the job to complete and propagate assertion errors.
    getAndThrowCause(future);
  }

  @Test
  public void testSameThread() throws Throwable {
    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        final Set<Thread> threads = Collections.synchronizedSet(new HashSet<Thread>()); // synchronized because modified/read by different threads.

        m_jobManager.runNow(new IRunnable() {

          @Override
          public void run() throws Exception {
            threads.add(Thread.currentThread());

            m_jobManager.runNow(new IRunnable() {

              @Override
              public void run() throws Exception {
                threads.add(Thread.currentThread());
              }
            });
          }
        });

        assertEquals(1, threads.size());
        assertTrue(threads.contains(Thread.currentThread()));
      }
    });

    // Wait for the job to complete and propagate assertion errors.
    getAndThrowCause(future);
  }

  @Test
  public void testThreadName() throws Throwable {
    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        Thread.currentThread().setName("scout-model-thread");

        final Holder<String> actualThreadName1 = new Holder<>();
        final Holder<String> actualThreadName2 = new Holder<>();

        m_jobManager.runNow(new IRunnable() {

          @Override
          public void run() throws Exception {
            actualThreadName1.setValue(Thread.currentThread().getName());

            m_jobManager.runNow(new IRunnable() {

              @Override
              public void run() throws Exception {
                actualThreadName2.setValue(Thread.currentThread().getName());
              }
            }, ClientJobInput.defaults().name("XYZ"));
          }
        }, ClientJobInput.defaults().name("ABC"));

        assertEquals("scout-model-thread;job:ABC", actualThreadName1.getValue());
        assertEquals("scout-model-thread;job:XYZ", actualThreadName2.getValue());
        assertEquals("scout-model-thread", Thread.currentThread().getName());
      }
    });

    // Wait for the job to complete and propagate assertion errors.
    getAndThrowCause(future);
  }

  @Test
  public void testCurrentJob() throws Throwable {
    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        final AtomicReference<IFuture<?>> actualFuture1 = new AtomicReference<>();
        final AtomicReference<IFuture<?>> actualFuture2 = new AtomicReference<>();

        IFuture<?> currentFuture = IFuture.CURRENT.get();

        m_jobManager.runNow(new IRunnable() {

          @Override
          public void run() throws Exception {
            actualFuture1.set(IFuture.CURRENT.get());

            m_jobManager.runNow(new IRunnable() {

              @Override
              public void run() throws Exception {
                actualFuture2.set(IFuture.CURRENT.get());
              }
            });
          }
        });

        assertNotNull(actualFuture1.get());
        assertNotNull(actualFuture2.get());

        assertEquals("same future expected", 1, CollectionUtility.hashSet(currentFuture, actualFuture1.get(), actualFuture2.get()).size());

        assertEquals(currentFuture, IFuture.CURRENT.get());
      }
    });

    // Wait for the job to complete and propagate assertion errors.
    getAndThrowCause(future);
  }

  @Test
  public void testBlocking() throws Throwable {
    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        final List<Integer> actualProtocol = Collections.synchronizedList(new ArrayList<Integer>()); // synchronized because modified/read by different threads.

        m_jobManager.runNow(new IRunnable() {

          @Override
          public void run() throws Exception {
            actualProtocol.add(1);
          }
        });
        actualProtocol.add(2);

        assertEquals(Arrays.asList(1, 2), actualProtocol);
      }
    });

    // Wait for the job to complete and propagate assertion errors.
    getAndThrowCause(future);
  }

  @Test
  public void testModelJobContext() throws Throwable {
    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
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

        m_jobManager.runNow(new IRunnable() {

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

            m_jobManager.runNow(new IRunnable() {

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
      }
    });

    // Wait for the job to complete and propagate assertion errors.
    getAndThrowCause(future);

    assertNull(JobContext.CURRENT.get());
  }

  private void getAndThrowCause(IFuture<?> future) throws Throwable {
    try {
      future.get(10, TimeUnit.SECONDS);
    }
    catch (ProcessingException e) {
      throw e.getCause();
    }
  }

  private static UserAgent newUserAgent() {
    return UserAgent.create(UiLayer.UNKNOWN, UiDeviceType.UNKNOWN, "n/a");
  }
}
