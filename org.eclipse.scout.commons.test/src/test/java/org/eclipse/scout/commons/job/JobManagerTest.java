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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.CollectionUtility;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JobManagerTest {

  private static ExecutorService s_executor;

  private IJobManager m_jobManager;

  @Before
  public void before() {
    m_jobManager = new JobManager();
  }

  @After
  public void after() {
    m_jobManager = new JobManager();
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
    IJob<Void> job1 = new _Job<Void>("job-1") {

      @Override
      protected Void call() throws Exception {
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        return null;
      }
    };
    IJob<Void> job2 = new _Job<Void>("job-2") {

      @Override
      protected Void call() throws Exception {
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        return null;
      }
    };
    IJob<Void> job3 = new _Job<Void>("job-3") {

      @Override
      protected Void call() throws Exception {
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        return null;
      }
    };

    job1.schedule();
    job2.schedule();
    job3.schedule();

    Thread.sleep(500);

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
    final Set<IJob<?>> actualInterruptedProtocol = Collections.synchronizedSet(new HashSet<IJob<?>>()); // synchronized because modified/read by different threads.

    final CountDownLatch latchBefore = new CountDownLatch(3);

    final IJob<Void> job1 = new _Job<Void>("job-1") {

      @Override
      protected Void call() throws Exception {
        latchBefore.countDown();
        try {
          Thread.sleep(TimeUnit.SECONDS.toMillis(30));
        }
        catch (InterruptedException e) {
          actualInterruptedProtocol.add(this);
        }

        return null;
      }
    };
    final IJob<Void> job2 = new _Job<Void>("job-2") {

      @Override
      protected Void call() throws Exception {
        latchBefore.countDown();
        try {
          Thread.sleep(TimeUnit.SECONDS.toMillis(30));
        }
        catch (InterruptedException e) {
          actualInterruptedProtocol.add(this);
        }

        return null;
      }
    };
    final IJob<Void> job3 = new _Job<Void>("job-3") {

      @Override
      protected Void call() throws Exception {
        latchBefore.countDown();
        try {
          Thread.sleep(TimeUnit.SECONDS.toMillis(30));
        }
        catch (InterruptedException e) {
          actualInterruptedProtocol.add(this);
        }

        return null;
      }
    };

    job1.schedule();
    job2.schedule();
    job3.schedule();

    latchBefore.await(10, TimeUnit.SECONDS); // Wait for all jobs to be ready
    m_jobManager.shutdown();

    Thread.sleep(TimeUnit.SECONDS.toMillis(5)); // Wait some time until all jobs were interrupted.
    assertEquals(CollectionUtility.hashSet(job1, job2, job3), actualInterruptedProtocol);
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
      return JobManagerTest.this.m_jobManager;
    }
  }
}
