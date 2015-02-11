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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
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

  private JobManager m_jobManager;

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
    IJob<String> job = new Job_<String>("job") {
      @Override
      protected String onRun(IProgressMonitor monitor) throws ProcessingException {
        return "RUNNING_WITH_RESULT";
      }
    };
    String actualResult = job.runNow();

    assertEquals("RUNNING_WITH_RESULT", actualResult);
  }

  @Test
  public void testVoidResult() throws ProcessingException {
    final Holder<String> holder = new Holder<>();

    IJob<Void> job = new Job_<Void>("job") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        holder.setValue("RUNNING_VOID");
      }
    };
    Void actualResult = job.runNow();

    assertNull(actualResult);
    assertEquals("RUNNING_VOID", holder.getValue());
  }

  @Test
  public void testProcessingException() throws ProcessingException {
    final ProcessingException expectedException = new ProcessingException();

    IJob<String> job = new Job_<String>("job") {
      @Override
      protected String onRun(IProgressMonitor monitor) throws ProcessingException {
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

    IJob<String> job = new Job_<String>("job") {
      @Override
      protected String onRun(IProgressMonitor monitor) throws ProcessingException {
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
    final Set<Thread> threads = new HashSet<Thread>();

    new Job_<Void>("job-1") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor1) throws ProcessingException {
        threads.add(Thread.currentThread());

        new Job_<Void>("job-2") {

          @Override
          protected void onRunVoid(IProgressMonitor monitor2) throws ProcessingException {
            threads.add(Thread.currentThread());
          }
        }.runNow();
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

    new Job_<Void>("ABC") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor1) throws ProcessingException {
        actualThreadName1.setValue(Thread.currentThread().getName());

        new Job_<Void>("XYZ") {

          @Override
          protected void onRunVoid(IProgressMonitor monitor2) throws ProcessingException {
            actualThreadName2.setValue(Thread.currentThread().getName());
          }
        }.runNow();
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

    new Job_<Void>("job-1") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor1) throws ProcessingException {
        job1.setValue(this);
        actualJob1.setValue(Job.get());

        new Job_<Void>("job-2") {

          @Override
          protected void onRunVoid(IProgressMonitor monitor2) throws ProcessingException {
            job2.setValue(this);
            actualJob2.setValue(Job.get());
          }
        }.runNow();
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
    final List<Integer> actualProtocol = new ArrayList<>();

    new Job_<Void>("job") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        actualProtocol.add(1);
      }
    }.runNow();
    actualProtocol.add(2);

    assertEquals(Arrays.asList(1, 2), actualProtocol);
  }

  /**
   * Job with a dedicated {@link JobManager} per test-case.
   */
  public class Job_<R> extends Job<R> {

    public Job_(String name) {
      super(name);
    }

    @Override
    protected JobManager createJobManager() {
      return JobRunNowTest.this.m_jobManager;
    }
  }
}
