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
import static org.junit.Assert.assertFalse;
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

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.internal.JobManager;
import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JobScheduleTest {

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
  public void testWorkerThread() throws ProcessingException {
    final Set<Thread> protocol = Collections.synchronizedSet(new HashSet<Thread>()); // synchronized because modified/read by different threads.

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(Thread.currentThread());

        m_jobManager.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            protocol.add(Thread.currentThread());
          }
        }).get();
      }
    }).get();

    assertEquals(2, protocol.size());
    assertFalse(protocol.contains(Thread.currentThread()));
  }

  @Test
  public void testThreadName() throws ProcessingException {
    Thread.currentThread().setName("main");

    final Holder<String> actualThreadName1 = new Holder<>();
    final Holder<String> actualThreadName2 = new Holder<>();

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualThreadName1.setValue(Thread.currentThread().getName());

        m_jobManager.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            actualThreadName2.setValue(Thread.currentThread().getName());
          }
        }, JobInput.defaults().name("XYZ")).get();
      }
    }, JobInput.defaults().name("ABC")).get();

    assertTrue(actualThreadName1.getValue().matches("scout-(\\d)+;job:ABC"));
    assertTrue(actualThreadName2.getValue().matches("scout-(\\d)+;job:XYZ"));
    assertEquals("main", Thread.currentThread().getName());
  }

  @Test
  public void testCurrentFuture() throws ProcessingException {
    final Holder<IFuture<?>> expectedFuture1 = new Holder<>();
    final Holder<IFuture<?>> expectedFuture2 = new Holder<>();

    final Holder<Future<?>> actualFuture1 = new Holder<>();
    final Holder<Future<?>> actualFuture2 = new Holder<>();

    IFuture.CURRENT.remove();

    expectedFuture1.setValue(m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualFuture1.setValue(IFuture.CURRENT.get());

        expectedFuture2.setValue(m_jobManager.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            actualFuture2.setValue(IFuture.CURRENT.get());
          }
        }));

        expectedFuture2.getValue().get(); // wait for the job to complete
      }
    }));

    expectedFuture1.getValue().get(); // wait for the job to complete

    assertNotNull(expectedFuture1.getValue());
    assertNotNull(expectedFuture2.getValue());

    assertSame(expectedFuture1.getValue().getDelegate(), actualFuture1.getValue());
    assertSame(expectedFuture2.getValue().getDelegate(), actualFuture2.getValue());

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
  public void testParallelExecution() throws Exception {
    final BlockingCountDownLatch barrier = new BlockingCountDownLatch(3);

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

    assertTrue(barrier.await());
    barrier.unblock();
  }

  @Test
  public void testJobContext() throws ProcessingException {
    JobContext.CURRENT.remove();

    final Holder<JobContext> actualJobContext1 = new Holder<>();
    final Holder<JobContext> actualJobContext2 = new Holder<>();

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        JobContext ctx1 = JobContext.CURRENT.get();
        ctx1.set("PROP_JOB1", "J1");
        ctx1.set("PROP_JOB1+JOB2", "SHARED-1");
        actualJobContext1.setValue(ctx1);

        m_jobManager.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            JobContext ctx2 = JobContext.CURRENT.get();
            ctx2.set("PROP_JOB2", "J2");
            ctx2.set("PROP_JOB1+JOB2", "SHARED-2");
            actualJobContext2.setValue(ctx2);
          }
        }).get();
      }
    }).get();

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
