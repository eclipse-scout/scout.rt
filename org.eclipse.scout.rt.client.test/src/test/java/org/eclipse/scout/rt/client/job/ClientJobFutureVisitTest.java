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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.IVisitor;
import org.eclipse.scout.commons.filter.AlwaysFilter;
import org.eclipse.scout.commons.filter.AndFilter;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.commons.job.filter.JobFilter;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.filter.ClientSessionFilter;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ClientJobFutureVisitTest {

  private IClientJobManager m_jobManager;
  private IClientSession m_session1;
  private IClientSession m_session2;
  private IClientSession m_session3;

  private Set<String> protocol;

  private BlockingCountDownLatch latch;

  @Before
  public void before() throws InterruptedException, JobExecutionException {
    m_jobManager = OBJ.one(IClientJobManager.class);
    m_jobManager.cancel(new AlwaysFilter<IFuture<?>>(), true);
    assertTrue(m_jobManager.waitUntilDone(new AlwaysFilter<IFuture<?>>(), 10, TimeUnit.SECONDS)); // TODO DWI: own job manager for this test.

    m_session1 = mock(IClientSession.class);
    m_session2 = mock(IClientSession.class);
    m_session3 = mock(IClientSession.class);

    // prepare the test-case
    protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.

    latch = new BlockingCountDownLatch(4);

    // SESSION 1 (JOB-1)
    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
      }
    }, ClientJobInput.empty().name("session1_job1").session(m_session1));

    // SESSION 1 (JOB-2)
    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        latch.countDownAndBlock();
      }
    }, ClientJobInput.empty().name("session1_job2").session(m_session1));

    // SESSION 1 (JOB-3)
    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
      }
    }, ClientJobInput.empty().name("session1_job3").session(m_session1));

    // =========
    // SESSION 2 (JOB-1)
    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        latch.countDownAndBlock();
      }
    }, ClientJobInput.empty().name("session2_job1").session(m_session2).id("ABC"));

    // SESSION 2 (JOB-2)
    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
      }
    }, ClientJobInput.empty().name("session2_job2").session(m_session2).id("ABC"));

    // SESSION 2  (JOB-3)
    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        latch.countDownAndBlock();
      }
    }, ClientJobInput.empty().name("session2_job3").session(m_session2).id("ABC"));

    // SESSION 2  (JOB-4)
    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
      }
    }, ClientJobInput.empty().name("session2_job4").session(m_session2));

    // =========
    // SESSION 3 (JOB-1)
    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        latch.countDownAndBlock();
      }
    }, ClientJobInput.empty().name("session3_job1").session(m_session3).id("ABC"));

    assertTrue(latch.await());
  }

  @Test
  public void testProtocol() throws JobExecutionException, InterruptedException {
    Set<String> expected = new HashSet<>();
    expected.add("session1_job1"); // completed
    expected.add("session1_job2"); // waiting on the latch
    expected.add("session1_job3"); // completed
    expected.add("session2_job1"); // waiting on the latch
    expected.add("session2_job2"); // completed
    expected.add("session2_job3"); // waiting on the latch
    expected.add("session2_job4"); // completed
    expected.add("session3_job1"); // waiting on the latch

    assertEquals(expected, protocol);
  }

  @Test
  public void testVisitNullFilter() throws JobExecutionException, InterruptedException {
    final Set<String> visitedFutures = new HashSet<>();
    m_jobManager.visit(null, new IVisitor<IFuture<?>>() {

      @Override
      public boolean visit(IFuture<?> future) {
        visitedFutures.add(future.getJobInput().getName());
        return true;
      }
    });
    Set<String> expected = new HashSet<>();
//    expected.add("session1_job1"); // completed
    expected.add("session1_job2"); // waiting on the latch
//    expected.add("session1_job3"); // completed
    expected.add("session2_job1"); // waiting on the latch
//    expected.add("session2_job2"); // completed
    expected.add("session2_job3"); // waiting on the latch
//    expected.add("session2_job4"); // completed
    expected.add("session3_job1"); // waiting on the latch
    assertEquals(expected, visitedFutures);
  }

  @Test
  public void testVisitAlwaysFilter() throws JobExecutionException, InterruptedException {
    final Set<String> visitedFutures = new HashSet<>();
    m_jobManager.visit(new AlwaysFilter<IFuture<?>>(), new IVisitor<IFuture<?>>() {

      @Override
      public boolean visit(IFuture<?> future) {
        visitedFutures.add(future.getJobInput().getName());
        return true;
      }
    });
    Set<String> expected = new HashSet<>();
//  expected.add("session1_job1"); // completed
    expected.add("session1_job2"); // waiting on the latch
//  expected.add("session1_job3"); // completed
    expected.add("session2_job1"); // waiting on the latch
//  expected.add("session2_job2"); // completed
    expected.add("session2_job3"); // waiting on the latch
//  expected.add("session2_job4"); // completed
    expected.add("session3_job1"); // waiting on the latch
    assertEquals(expected, visitedFutures);
  }

  @Test
  public void testVisitSession1Filter() throws JobExecutionException, InterruptedException {
    final Set<String> visitedFutures = new HashSet<>();
    m_jobManager.visit(new ClientSessionFilter(m_session1), new IVisitor<IFuture<?>>() {

      @Override
      public boolean visit(IFuture<?> future) {
        visitedFutures.add(future.getJobInput().getName());
        return true;
      }
    });
    Set<String> expected = new HashSet<>();
//  expected.add("session1_job1"); // completed
    expected.add("session1_job2"); // waiting on the latch
//  expected.add("session1_job3"); // completed
//  expected.add("session2_job1"); // waiting on the latch
//  expected.add("session2_job2"); // completed
//    expected.add("session2_job3"); // waiting on the latch
//  expected.add("session2_job4"); // completed
//    expected.add("session3_job1"); // waiting on the latch
    assertEquals(expected, visitedFutures);
  }

  @Test
  public void testVisitSession2Filter() throws JobExecutionException, InterruptedException {
    final Set<String> visitedFutures = new HashSet<>();
    m_jobManager.visit(new ClientSessionFilter(m_session2), new IVisitor<IFuture<?>>() {

      @Override
      public boolean visit(IFuture<?> future) {
        visitedFutures.add(future.getJobInput().getName());
        return true;
      }
    });
    Set<String> expected = new HashSet<>();
//  expected.add("session1_job1"); // completed
//    expected.add("session1_job2"); // waiting on the latch
//  expected.add("session1_job3"); // completed
    expected.add("session2_job1"); // waiting on the latch
//  expected.add("session2_job2"); // completed
    expected.add("session2_job3"); // waiting on the latch
//  expected.add("session2_job4"); // completed
//    expected.add("session3_job1"); // waiting on the latch
    assertEquals(expected, visitedFutures);
  }

  @Test
  public void testVisitSessionFilterAndId() throws JobExecutionException, InterruptedException {
    final Set<String> visitedFutures = new HashSet<>();
    m_jobManager.visit(new AndFilter<IFuture<?>>(new ClientSessionFilter(m_session3), new JobFilter("ABC")), new IVisitor<IFuture<?>>() {

      @Override
      public boolean visit(IFuture<?> future) {
        visitedFutures.add(future.getJobInput().getName());
        return true;
      }
    });
    Set<String> expected = new HashSet<>();
//  expected.add("session1_job1"); // completed
//    expected.add("session1_job2"); // waiting on the latch
//  expected.add("session1_job3"); // completed
//    expected.add("session2_job1"); // waiting on the latch
//  expected.add("session2_job2"); // completed
//    expected.add("session2_job3"); // waiting on the latch
//  expected.add("session2_job4"); // completed
    expected.add("session3_job1"); // waiting on the latch
    assertEquals(expected, visitedFutures);
  }
}
