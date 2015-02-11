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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.commons.job.IJobVisitor;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.JobManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ModelJobManagerTest {

  private static ExecutorService s_executor;

  private ModelJobManager m_jobManager;

  @Before
  public void before() {
    m_jobManager = new ModelJobManager();
  }

  @After
  public void after() {
    m_jobManager.shutdown();
  }

  @BeforeClass
  public static void beforeClass() {
    s_executor = Executors.newCachedThreadPool();
  }

  @AfterClass
  public static void afterClass() {
    s_executor.shutdown();
  }

  @Test
  public void testVisit() throws Exception {
    IJob<Void> job1 = new Job_<Void>("job-1") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        _sleep(1000);
      }
    };
    IJob<Void> job2 = new Job_<Void>("job-2") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        _sleep(1000);
      }
    };
    IJob<Void> job3 = new Job_<Void>("job-3") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        _sleep(1000);
      }
    };

    job1.schedule();
    job2.schedule();
    job3.schedule();

    _sleep(500);

    final Set<IJob<?>> actualVisitedJobs = new HashSet<>();
    m_jobManager.visit(new IJobVisitor() {

      @Override
      public boolean visit(IJob<?> job) {
        actualVisitedJobs.add(job);
        return true;
      }
    });
    assertEquals(CollectionUtility.hashSet(job1, job2, job3), actualVisitedJobs);
  }

  @Test
  public void testShutdown() throws Exception {
    final Set<IJob<?>> actualInterruptedProtocol = new HashSet<>();

    final CountDownLatch latchBefore = new CountDownLatch(1);

    final IJob<Void> job1 = new Job_<Void>("job-1") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        latchBefore.countDown();
        try {
          Thread.sleep(Long.MAX_VALUE);
        }
        catch (InterruptedException e) {
          actualInterruptedProtocol.add(this);
        }
      }
    };
    final IJob<Void> job2 = new Job_<Void>("job-2") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        latchBefore.countDown();
        try {
          Thread.sleep(Long.MAX_VALUE);
        }
        catch (InterruptedException e) {
          actualInterruptedProtocol.add(this);
        }
      }
    };
    final IJob<Void> job3 = new Job_<Void>("job-3") {

      @Override
      protected void onRunVoid(IProgressMonitor monitor) throws ProcessingException {
        latchBefore.countDown();
        try {
          Thread.sleep(Long.MAX_VALUE);
        }
        catch (InterruptedException e) {
          actualInterruptedProtocol.add(this);
        }
      }
    };

    job1.schedule();
    job2.schedule();
    job3.schedule();

    latchBefore.await(10, TimeUnit.SECONDS); // Wait for all jobs to be ready
    _sleep(100);
    assertFalse(m_jobManager.isIdle());
    m_jobManager.shutdown();
    assertTrue("pending queue not cleared", m_jobManager.waitForIdle(10, TimeUnit.SECONDS));
    assertEquals("active mutex-job interrupted", Collections.singleton(job1), actualInterruptedProtocol);
    assertTrue("pending queue not cleared", m_jobManager.isIdle());
  }

  private static boolean _sleep(long millis) {
    try {
      Thread.sleep(millis);
      return true;
    }
    catch (InterruptedException e) {
      return false;
    }
  }

  /**
   * Job with a dedicated {@link JobManager} per test-case.
   */
  public class Job_<R> extends ModelJob<R> {

    public Job_(String name) {
      super(name, mock(IClientSession.class));
    }

    @Override
    protected ModelJobManager createJobManager(IClientSession clientSession) {
      return ModelJobManagerTest.this.m_jobManager;
    }
  }
}
