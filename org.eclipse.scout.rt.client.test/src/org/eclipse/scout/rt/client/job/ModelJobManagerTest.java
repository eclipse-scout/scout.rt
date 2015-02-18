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
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.commons.job.IJobVisitor;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.rt.client.IClientSession;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ModelJobManagerTest {

  private static ExecutorService s_executor;

  private IClientSession m_clientSession;
  private ModelJobManager m_jobManager;

  @Before
  public void before() {
    m_jobManager = new ModelJobManager();
    m_clientSession = mock(IClientSession.class);
    when(m_clientSession.getModelJobManager()).thenReturn(m_jobManager);
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
  public void testVisit() throws JobExecutionException, InterruptedException {
    ModelJob job1 = new ModelJob("job-1", m_clientSession) {

      @Override
      protected void run() throws Exception {
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
      }
    };
    ModelJob job2 = new ModelJob("job-2", m_clientSession) {

      @Override
      protected void run() throws Exception {
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
      }
    };
    ModelJob job3 = new ModelJob("job-3", m_clientSession) {

      @Override
      protected void run() throws Exception {
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
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
  public void testShutdown() throws JobExecutionException, InterruptedException {
    final Set<IJob<?>> actualInterruptedProtocol = Collections.synchronizedSet(new HashSet<IJob<?>>()); // synchronized because modified/read by different threads.
    final CountDownLatch latchBefore = new CountDownLatch(1);

    ModelJob job1 = new ModelJob("job-1", m_clientSession) {

      @Override
      protected void run() throws Exception {
        latchBefore.countDown();
        try {
          Thread.sleep(TimeUnit.SECONDS.toMillis(30));
        }
        catch (InterruptedException e) {
          actualInterruptedProtocol.add(this);
        }
      }
    };
    ModelJob job2 = new ModelJob("job-2", m_clientSession) {

      @Override
      protected void run() throws Exception {
        latchBefore.countDown();
        try {
          Thread.sleep(TimeUnit.SECONDS.toMillis(30));
        }
        catch (InterruptedException e) {
          actualInterruptedProtocol.add(this);
        }
      }
    };
    ModelJob job3 = new ModelJob("job-3", m_clientSession) {

      @Override
      protected void run() throws Exception {
        latchBefore.countDown();
        try {
          Thread.sleep(TimeUnit.SECONDS.toMillis(30));
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
    assertFalse(m_jobManager.isIdle());
    m_jobManager.shutdown();
    assertTrue(m_jobManager.isIdle());

    Thread.sleep(TimeUnit.SECONDS.toMillis(5)); // Wait some time until all jobs were interrupted.
    assertEquals("active mutex-job interrupted", Collections.singleton(job1), actualInterruptedProtocol);
  }
}
