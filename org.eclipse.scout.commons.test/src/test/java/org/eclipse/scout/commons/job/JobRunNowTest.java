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
package org.eclipse.scout.commons.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.internal.JobManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JobRunNowTest {

  private IJobManager<IJobInput> m_jobManager;

  @Before
  public void before() {
    m_jobManager = new JobManager<IJobInput>("scout");
  }

  @After
  public void after() {
    m_jobManager.shutdown();
  }

  @Test
  public void testWithCallable() throws ProcessingException {
    String result = m_jobManager.runNow(new ICallable<String>() {

      @Override
      public String call() throws Exception {
        return "running";
      }
    });

    assertEquals("running", result);
  }

  @Test
  public void testVoidResult() throws ProcessingException {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.
    m_jobManager.runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("running");
      }
    });

    assertEquals(CollectionUtility.hashSet("running"), protocol);
  }

  @Test
  public void testProcessingExceptionWithRunnable() throws ProcessingException {
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

  @Test
  public void testProcessingExceptionWithCallable() throws ProcessingException {
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

  @Test
  public void testRuntimeExceptionWithRunnable() throws ProcessingException {
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

  @Test
  public void testRuntimeExceptionWithCallable() throws ProcessingException {
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

  @Test
  public void testExceptionExceptionWithRunnable() throws ProcessingException {
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

  @Test
  public void testExceptionExceptionWithCallable() throws ProcessingException {
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

  @Test
  public void testSameThread() throws ProcessingException {
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

  @Test
  public void testThreadName() throws ProcessingException {
    Thread.currentThread().setName("main");

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
        }, JobInput.defaults().name("XYZ"));
      }
    }, JobInput.defaults().name("ABC"));

    assertEquals("main;job:ABC", actualThreadName1.getValue());
    assertEquals("main;job:XYZ", actualThreadName2.getValue());
    assertEquals("main", Thread.currentThread().getName());
  }

  @Test
  public void testCurrentFuture1() throws ProcessingException {
    final AtomicReference<Future<?>> actualFuture1 = new AtomicReference<>();
    final AtomicReference<Future<?>> actualFuture2 = new AtomicReference<>();

    IFuture.CURRENT.remove();

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

    assertEquals("same future expected", 1, CollectionUtility.hashSet(actualFuture1.get(), actualFuture2.get()).size());

    assertNull(IFuture.CURRENT.get());
  }

  @Test
  public void testCurrentFuture() throws ProcessingException {
    final AtomicReference<Future<?>> actualFuture1 = new AtomicReference<>();
    final AtomicReference<Future<?>> actualFuture2 = new AtomicReference<>();

    IFuture.CURRENT.remove();

    // Schedule outermost job.
    IFuture<Void> future = m_jobManager.schedule(new IRunnable() {

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

    future.get();

    assertEquals("same future expected", CollectionUtility.hashSet(future.getDelegate()), CollectionUtility.hashSet(actualFuture1.get(), actualFuture2.get()));

    assertNull(IFuture.CURRENT.get());
  }

  @Test
  public void testBlocking() throws ProcessingException {
    final List<Integer> protocol = Collections.synchronizedList(new ArrayList<Integer>()); // synchronized because modified/read by different threads.

    m_jobManager.runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(1);
      }
    });
    protocol.add(2);

    assertEquals(Arrays.asList(1, 2), protocol);
  }

  @Test
  public void testJobContext() throws ProcessingException {
    JobContext.CURRENT.remove();

    final Holder<JobContext> actualJobContext1 = new Holder<>();
    final Holder<JobContext> actualJobContext2 = new Holder<>();

    m_jobManager.runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        JobContext ctx1 = JobContext.CURRENT.get();
        ctx1.set("PROP_JOB1", "J1");
        ctx1.set("PROP_JOB1+JOB2", "SHARED-1");
        actualJobContext1.setValue(ctx1);

        m_jobManager.runNow(new IRunnable() {

          @Override
          public void run() throws Exception {
            JobContext ctx2 = JobContext.CURRENT.get();
            ctx2.set("PROP_JOB2", "J2");
            ctx2.set("PROP_JOB1+JOB2", "SHARED-2");
            actualJobContext2.setValue(ctx2);
          }
        });
      }
    });

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
}
