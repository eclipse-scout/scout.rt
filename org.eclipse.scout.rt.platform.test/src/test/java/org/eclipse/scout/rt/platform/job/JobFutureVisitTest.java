/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.filter.AndFilter;
import org.eclipse.scout.rt.platform.filter.NotFilter;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.visitor.IVisitor;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobFutureVisitTest {

  private static final String JOB_IDENTIFIER = UUID.randomUUID().toString();

  private IMutex m_mutex1;
  private IMutex m_mutex2;
  private IMutex m_mutex3;

  private Set<String> protocol;

  private BlockingCountDownLatch m_latch;

  private IBlockingCondition bc1;
  private IBlockingCondition bc2;

  @Before
  public void before() throws InterruptedException {
    m_mutex1 = Jobs.newMutex();
    m_mutex2 = Jobs.newMutex();
    m_mutex3 = Jobs.newMutex();

    // prepare the test-case
    protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.
    bc1 = Jobs.getJobManager().createBlockingCondition("BC1", true);
    bc2 = Jobs.getJobManager().createBlockingCondition("BC2", true);

    m_latch = new BlockingCountDownLatch(3);

    // SESSION 1 (JOB-1)
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        bc1.waitFor();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withName("mutex1_job1")
        .withMutex(m_mutex1)
        .withExecutionHint(JOB_IDENTIFIER)
        .withExceptionHandling(null, false));

    // SESSION 1 (JOB-2)
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        m_latch.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withName("mutex1_job2")
        .withMutex(m_mutex1)
        .withExecutionHint(JOB_IDENTIFIER)
        .withExceptionHandling(null, false));

    // SESSION 1 (JOB-3)
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withName("mutex1_job3")
        .withExecutionHint(JOB_IDENTIFIER)
        .withMutex(m_mutex1));

    // =========
    // SESSION 2 (JOB-1)
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withName("mutex2_job1")
        .withExecutionHint(JOB_IDENTIFIER)
        .withMutex(m_mutex2));

    // SESSION 2 (JOB-2)
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        bc2.waitFor();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withName("mutex2_job2")
        .withMutex(m_mutex2)
        .withExecutionHint(JOB_IDENTIFIER)
        .withExceptionHandling(null, false));

    // SESSION 2  (JOB-3)
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        bc2.setBlocking(false);

        JobTestUtil.waitForMutexCompetitors(m_mutex2, 3); // Wait until job 'mutex2_job2' is re-acquiring the mutex. [3=job-2, job-3, job-4]

        m_latch.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withName("mutex2_job3")
        .withMutex(m_mutex2)
        .withExecutionHint(JOB_IDENTIFIER)
        .withExceptionHandling(null, false));

    // SESSION 2  (JOB-4)
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withName("mutex2_job4")
        .withExecutionHint(JOB_IDENTIFIER)
        .withMutex(m_mutex2));

    // =========
    // SESSION 3 (JOB-1)
    Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        m_latch.countDownAndBlock();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withName("mutex3_job1")
        .withMutex(m_mutex3)
        .withExecutionHint(JOB_IDENTIFIER)
        .withExceptionHandling(null, false));

    assertTrue(m_latch.await());
  }

  @After
  public void after() {
    m_latch.unblock();
    bc1.setBlocking(false);

    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_IDENTIFIER)
        .toFilter(), 10, TimeUnit.SECONDS);
  }

  @Test
  public void testProtocol() {
    Set<String> expected = new HashSet<>();
    expected.add("mutex1_job1"); // blocked
    expected.add("mutex1_job2"); // waiting on the latch
//    expected.add("mutex1_job3"); // waiting for execution
    expected.add("mutex2_job1"); // completed
    expected.add("mutex2_job2"); // re-acquiring mutex
    expected.add("mutex2_job3"); // waiting on the latch
//    expected.add("mutex2_job4"); // waiting for execution
    expected.add("mutex3_job1"); // waiting on the latch

    assertEquals(expected, protocol);
  }

  @Test
  public void testVisitNullFilter() {
    final Set<String> visitedFutures = new HashSet<>();
    Jobs.getJobManager().visit(null, new IVisitor<IFuture<?>>() {

      @Override
      public boolean visit(IFuture<?> future) {
        visitedFutures.add(future.getJobInput().getName());
        return true;
      }
    });
    Set<String> expected = new HashSet<>();
    expected.add("mutex1_job1"); // blocked
    expected.add("mutex1_job2"); // waiting on the latch
    expected.add("mutex1_job3"); // waiting for execution
//    expected.add("mutex2_job1"); // completed
    expected.add("mutex2_job2"); // // re-acquiring mutex
    expected.add("mutex2_job3"); // waiting on the latch
    expected.add("mutex2_job4"); // waiting for execution
    expected.add("mutex3_job1"); // waiting on the latch
    assertEquals(expected, visitedFutures);
  }

  @Test
  public void testVisitFilter() {
    final Set<String> visitedFutures = new HashSet<>();
    Jobs.getJobManager().visit(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_IDENTIFIER)
        .toFilter(), new IVisitor<IFuture<?>>() {

          @Override
          public boolean visit(IFuture<?> future) {
            visitedFutures.add(future.getJobInput().getName());
            return true;
          }
        });
    Set<String> expected = new HashSet<>();
    expected.add("mutex1_job1"); // blocked
    expected.add("mutex1_job2"); // waiting on the latch
    expected.add("mutex1_job3"); // waiting for execution
//    expected.add("mutex2_job1"); // completed
    expected.add("mutex2_job2"); // // re-acquiring mutex
    expected.add("mutex2_job3"); // waiting on the latch
    expected.add("mutex2_job4"); // waiting for execution
    expected.add("mutex3_job1"); // waiting on the latch
    assertEquals(expected, visitedFutures);
  }

  @Test
  public void testVisitBlockedFilter() {
    final Set<String> visitedFutures = new HashSet<>();
    Jobs.getJobManager().visit(Jobs.newFutureFilterBuilder()
        .andAreBlocked()
        .toFilter(), new IVisitor<IFuture<?>>() {

          @Override
          public boolean visit(IFuture<?> future) {
            visitedFutures.add(future.getJobInput().getName());
            return true;
          }
        });
    Set<String> expected = new HashSet<>();
    expected.add("mutex1_job1"); // blocked
//    expected.add("mutex1_job2"); // waiting on the latch
//    expected.add("mutex1_job3"); // waiting for execution
//    expected.add("mutex2_job1"); // completed
//    expected.add("mutex2_job2"); // // re-acquiring mutex
//    expected.add("mutex2_job3"); // waiting on the latch
//    expected.add("mutex2_job4"); // waiting for execution
//    expected.add("mutex3_job1"); // waiting on the latch
    assertEquals(expected, visitedFutures);
  }

  @Test
  public void testVisitNotBlockedFilter() {
    final Set<String> visitedFutures = new HashSet<>();
    Jobs.getJobManager().visit(new NotFilter<>(Jobs.newFutureFilterBuilder()
        .andAreBlocked()
        .toFilter()), new IVisitor<IFuture<?>>() {

          @Override
          public boolean visit(IFuture<?> future) {
            visitedFutures.add(future.getJobInput().getName());
            return true;
          }
        });
    Set<String> expected = new HashSet<>();
//    expected.add("mutex1_job1"); // blocked
    expected.add("mutex1_job2"); // waiting on the latch
    expected.add("mutex1_job3"); // waiting for execution
//    expected.add("mutex2_job1"); // completed
    expected.add("mutex2_job2"); // // re-acquiring mutex
    expected.add("mutex2_job3"); // waiting on the latch
    expected.add("mutex2_job4"); // waiting for execution
    expected.add("mutex3_job1"); // waiting on the latch
    assertEquals(expected, visitedFutures);
  }

  @Test
  public void testVisitSession1Filter() {
    final Set<String> visitedFutures = new HashSet<>();
    Jobs.getJobManager().visit(Jobs.newFutureFilterBuilder()
        .andMatchNameRegex(Pattern.compile("mutex1_.*"))
        .toFilter(), new IVisitor<IFuture<?>>() {

          @Override
          public boolean visit(IFuture<?> future) {
            visitedFutures.add(future.getJobInput().getName());
            return true;
          }
        });
    Set<String> expected = new HashSet<>();
    expected.add("mutex1_job1"); // blocked
    expected.add("mutex1_job2"); // waiting on the latch
    expected.add("mutex1_job3"); // waiting for execution
//    expected.add("mutex2_job1"); // completed
//    expected.add("mutex2_job2"); // // re-acquiring mutex
//    expected.add("mutex2_job3"); // waiting on the latch
//    expected.add("mutex2_job4"); // waiting for execution
//    expected.add("mutex3_job1"); // waiting on the latch
    assertEquals(expected, visitedFutures);
  }

  @Test
  public void testVisitSession2Filter() {
    final Set<String> visitedFutures = new HashSet<>();
    Jobs.getJobManager().visit(Jobs.newFutureFilterBuilder()
        .andMatchNameRegex(Pattern.compile("mutex2_.*"))
        .toFilter(), new IVisitor<IFuture<?>>() {

          @Override
          public boolean visit(IFuture<?> future) {
            visitedFutures.add(future.getJobInput().getName());
            return true;
          }
        });
    Set<String> expected = new HashSet<>();
//    expected.add("mutex1_job1"); // blocked
//    expected.add("mutex1_job2"); // waiting on the latch
//    expected.add("mutex1_job3"); // waiting for execution
//    expected.add("mutex2_job1"); // completed
    expected.add("mutex2_job2"); // // re-acquiring mutex
    expected.add("mutex2_job3"); // waiting on the latch
    expected.add("mutex2_job4"); // waiting for execution
//    expected.add("mutex3_job1"); // waiting on the latch
    assertEquals(expected, visitedFutures);
  }

  @Test
  public void testVisitSessionFilterAndBlocked() {
    final Set<String> visitedFutures = new HashSet<>();
    Jobs.getJobManager().visit(new AndFilter<IFuture<?>>(Jobs.newFutureFilterBuilder()
        .andMatchNameRegex(Pattern.compile("mutex1_.*"))
        .andAreBlocked()
        .toFilter()), new IVisitor<IFuture<?>>() {

          @Override
          public boolean visit(IFuture<?> future) {
            visitedFutures.add(future.getJobInput().getName());
            return true;
          }
        });
    Set<String> expected = new HashSet<>();
    expected.add("mutex1_job1"); // blocked
//    expected.add("mutex1_job2"); // waiting on the latch
//    expected.add("mutex1_job3"); // waiting for execution
//    expected.add("mutex2_job1"); // completed
//    expected.add("mutex2_job2"); // // re-acquiring mutex
//    expected.add("mutex2_job3"); // waiting on the latch
//    expected.add("mutex2_job4"); // waiting for execution
//    expected.add("mutex3_job1"); // waiting on the latch
    assertEquals(expected, visitedFutures);
  }
}
