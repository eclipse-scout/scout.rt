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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit-test to test {@link Job#runNow()}
 */
public class JobRunNowTest {

  private IJobManager m_jobManager;

  @Before
  public void before() {
    m_jobManager = new JobManager();
  }

  @After
  public void after() {
    m_jobManager.shutdown();
  }

  @Test
  public void testResult() throws ProcessingException {
    IJob<String> job = new _Job<String>("job") {
      @Override
      protected String call() throws ProcessingException {
        return "RUNNING_WITH_RESULT";
      }
    };
    String actualResult = job.runNow();

    assertEquals("RUNNING_WITH_RESULT", actualResult);
  }

  @Test
  public void testVoidResult() throws ProcessingException {
    final Holder<String> holder = new Holder<>();

    IJob<Void> job = new _Job<Void>("job") {

      @Override
      protected Void call() throws Exception {
        holder.setValue("RUNNING_VOID");
        return null;
      }
    };
    Void actualResult = job.runNow();

    assertNull(actualResult);
    assertEquals("RUNNING_VOID", holder.getValue());
  }

  @Test
  public void testProcessingException() throws ProcessingException {
    final ProcessingException expectedException = new ProcessingException();

    IJob<String> job = new _Job<String>("job") {
      @Override
      protected String call() throws Exception {
        throw expectedException;
      }
    };

    try {
      job.runNow();
      fail("Exception expected");
    }
    catch (Exception e) {
      assertSame(expectedException, e);
    }
  }

  @Test
  public void testRuntimeException() throws ProcessingException {
    final RuntimeException expectedException = new RuntimeException();

    IJob<String> job = new _Job<String>("job") {
      @Override
      protected String call() throws Exception {
        throw expectedException;
      }
    };

    try {
      job.runNow();
      fail("Exception expected");
    }
    catch (Exception e) {
      assertTrue(e instanceof ProcessingException);
      assertSame(expectedException, e.getCause());
    }
  }

  @Test
  public void testSameThread() throws ProcessingException {
    final Set<Thread> threads = Collections.synchronizedSet(new HashSet<Thread>()); // synchronized because modified/read by different threads.

    new _Job<Void>("job-1") {

      @Override
      protected Void call() throws Exception {
        threads.add(Thread.currentThread());

        new _Job<Void>("job-2") {

          @Override
          protected Void call() throws Exception {
            threads.add(Thread.currentThread());
            return null;
          }
        }.runNow();
        return null;
      }
    }.runNow();

    assertEquals(1, threads.size());
    assertTrue(threads.contains(Thread.currentThread()));
  }

  @Test
  public void testThreadName() throws ProcessingException {
    Thread.currentThread().setName("main");

    final Holder<String> actualThreadName1 = new Holder<>();
    final Holder<String> actualThreadName2 = new Holder<>();

    new _Job<Void>("ABC") {

      @Override
      protected Void call() throws Exception {
        actualThreadName1.setValue(Thread.currentThread().getName());

        new _Job<Void>("XYZ") {

          @Override
          protected Void call() throws Exception {
            actualThreadName2.setValue(Thread.currentThread().getName());
            return null;
          }
        }.runNow();
        return null;
      }
    }.runNow();

    assertEquals("thread:main;job:ABC", actualThreadName1.getValue());
    assertEquals("thread:main;job:XYZ", actualThreadName2.getValue());
    assertEquals("main", Thread.currentThread().getName());
  }

  @Test
  public void testCurrentJob() throws ProcessingException {
    final Holder<IJob<?>> job1 = new Holder<>();
    final Holder<IJob<?>> job2 = new Holder<>();

    final Holder<IJob<?>> actualJob1 = new Holder<>();
    final Holder<IJob<?>> actualJob2 = new Holder<>();

    IJob.CURRENT.set(null);

    new _Job<Void>("job-1") {

      @Override
      protected Void call() throws Exception {
        job1.setValue(this);
        actualJob1.setValue(IJob.CURRENT.get());

        new _Job<Void>("job-2") {

          @Override
          protected Void call() throws Exception {
            job2.setValue(this);
            actualJob2.setValue(IJob.CURRENT.get());
            return null;
          }
        }.runNow();
        return null;
      }
    }.runNow();

    assertNotNull(job1.getValue());
    assertNotNull(job2.getValue());

    assertSame(job1.getValue(), actualJob1.getValue());
    assertSame(job2.getValue(), actualJob2.getValue());

    assertNull(IJob.CURRENT.get());
  }

  @Test
  public void testBlocking() throws ProcessingException {
    final List<Integer> actualProtocol = Collections.synchronizedList(new ArrayList<Integer>()); // synchronized because modified/read by different threads.

    new _Job<Void>("job") {

      @Override
      protected Void call() throws Exception {
        actualProtocol.add(1);
        return null;
      }
    }.runNow();
    actualProtocol.add(2);

    assertEquals(Arrays.asList(1, 2), actualProtocol);
  }

  @Test
  public void testJobContext() throws ProcessingException {
    Thread.currentThread().setName("main");

    final Holder<JobContext> actualJobContext1 = new Holder<>();
    final Holder<JobContext> actualJobContext2 = new Holder<>();

    new _Job<Void>("job-1") {

      @Override
      protected Void call() throws Exception {
        JobContext ctx1 = JobContext.CURRENT.get();
        ctx1.set("PROP_JOB1", "J1");
        ctx1.set("PROP_JOB1+JOB2", "SHARED-1");
        actualJobContext1.setValue(ctx1);

        new _Job<Void>("job-2") {

          @Override
          protected Void call() throws Exception {
            JobContext ctx2 = JobContext.CURRENT.get();
            ctx2.set("PROP_JOB2", "J2");
            ctx2.set("PROP_JOB1+JOB2", "SHARED-2");
            actualJobContext2.setValue(ctx2);
            return null;
          }
        }.runNow();
        return null;
      }
    }.runNow();

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

  /**
   * Job with a dedicated {@link JobManager} per test-case.
   */
  public abstract class _Job<R> extends Job<R> {

    public _Job(String name) {
      super(name);
    }

    @Override
    protected IJobManager createJobManager() {
      return JobRunNowTest.this.m_jobManager;
    }
  }
}
