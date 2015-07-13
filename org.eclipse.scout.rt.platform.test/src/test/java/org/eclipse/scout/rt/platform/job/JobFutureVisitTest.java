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
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.IVisitor;
import org.eclipse.scout.commons.filter.AlwaysFilter;
import org.eclipse.scout.commons.filter.AndFilter;
import org.eclipse.scout.commons.filter.NotFilter;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobFutureVisitTest {

  private P_JobManager m_jobManager;
  private Object m_mutexObject1;
  private Object m_mutexObject2;
  private Object m_mutexObject3;

  private Set<String> protocol;

  private BlockingCountDownLatch latch;

  private IBlockingCondition bc1;
  private IBlockingCondition bc2;

  @Before
  public void before() throws InterruptedException {
    m_jobManager = new P_JobManager();

    m_mutexObject1 = new Object();
    m_mutexObject2 = new Object();
    m_mutexObject3 = new Object();

    // prepare the test-case
    protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.
    bc1 = m_jobManager.createBlockingCondition("BC1", true);
    bc2 = m_jobManager.createBlockingCondition("BC2", true);

    latch = new BlockingCountDownLatch(3);

    // SESSION 1 (JOB-1)
    m_jobManager.schedule(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        bc1.waitFor();
        return null;
      }
    }, Jobs.newInput(RunContexts.empty()).withName("mutex1_job1").withMutex(m_mutexObject1).withLogOnError(false));

    // SESSION 1 (JOB-2)
    m_jobManager.schedule(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        latch.countDownAndBlock();
        return null;
      }
    }, Jobs.newInput(RunContexts.empty()).withName("mutex1_job2").withMutex(m_mutexObject1).withLogOnError(false));

    // SESSION 1 (JOB-3)
    m_jobManager.schedule(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        return null;
      }
    }, Jobs.newInput(RunContexts.empty()).withName("mutex1_job3").withMutex(m_mutexObject1));

    // =========
    // SESSION 2 (JOB-1)
    m_jobManager.schedule(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        return null;
      }
    }, Jobs.newInput(RunContexts.empty()).withName("mutex2_job1").withMutex(m_mutexObject2));

    // SESSION 2 (JOB-2)
    m_jobManager.schedule(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        bc2.waitFor();
        return null;
      }
    }, Jobs.newInput(RunContexts.empty()).withName("mutex2_job2").withMutex(m_mutexObject2).withLogOnError(false));

    // SESSION 2  (JOB-3)
    m_jobManager.schedule(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        bc2.setBlocking(false);

        m_jobManager.waitForPermitsAcquired(m_mutexObject2, 3); // Wait until job 'mutex2_job2' is re-acquiring the mutex. [3=job-2, job-3, job-4]

        latch.countDownAndBlock();
        return null;
      }
    }, Jobs.newInput(RunContexts.empty()).withName("mutex2_job3").withMutex(m_mutexObject2).withLogOnError(false));

    // SESSION 2  (JOB-4)
    m_jobManager.schedule(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        return null;
      }
    }, Jobs.newInput(RunContexts.empty()).withName("mutex2_job4").withMutex(m_mutexObject2));

    // =========
    // SESSION 3 (JOB-1)
    m_jobManager.schedule(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        protocol.add(IFuture.CURRENT.get().getJobInput().getName());
        latch.countDownAndBlock();
        return null;
      }
    }, Jobs.newInput(RunContexts.empty()).withName("mutex3_job1").withMutex(m_mutexObject3).withLogOnError(false));

    assertTrue(latch.await());
  }

  @After
  public void after() {
    m_jobManager.shutdown();
  }

  @Test
  public void testProtocol() throws InterruptedException {
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
  public void testVisitNullFilter() throws InterruptedException {
    final Set<String> visitedFutures = new HashSet<>();
    m_jobManager.visit(null, new IVisitor<IFuture<?>>() {

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
  public void testVisitAlwaysFilter() throws InterruptedException {
    final Set<String> visitedFutures = new HashSet<>();
    m_jobManager.visit(new AlwaysFilter<IFuture<?>>(), new IVisitor<IFuture<?>>() {

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
  public void testVisitBlockedFilter() throws InterruptedException {
    final Set<String> visitedFutures = new HashSet<>();
    m_jobManager.visit(Jobs.newFutureFilter().andAreBlocked(), new IVisitor<IFuture<?>>() {

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
  public void testVisitNotBlockedFilter() throws InterruptedException {
    final Set<String> visitedFutures = new HashSet<>();
    m_jobManager.visit(new NotFilter<>(Jobs.newFutureFilter().andAreBlocked()), new IVisitor<IFuture<?>>() {

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
  public void testVisitSession1Filter() throws InterruptedException {
    final Set<String> visitedFutures = new HashSet<>();
    m_jobManager.visit(Jobs.newFutureFilter().andMatchNameRegex(Pattern.compile("mutex1_.*")), new IVisitor<IFuture<?>>() {

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
  public void testVisitSession2Filter() throws InterruptedException {
    final Set<String> visitedFutures = new HashSet<>();
    m_jobManager.visit(Jobs.newFutureFilter().andMatchNameRegex(Pattern.compile("mutex2_.*")), new IVisitor<IFuture<?>>() {

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
  public void testVisitSessionFilterAndBlocked() throws InterruptedException {
    final Set<String> visitedFutures = new HashSet<>();
    m_jobManager.visit(new AndFilter<IFuture<?>>(Jobs.newFutureFilter().andMatchNameRegex(Pattern.compile("mutex1_.*")).andAreBlocked()), new IVisitor<IFuture<?>>() {

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

  private class P_JobManager extends JobManager {

    /**
     * Blocks the current thread until the expected number of mutex-permits is acquired; Waits for maximal 30s.
     */
    protected void waitForPermitsAcquired(Object mutexObject, int expectedPermitCount) throws InterruptedException {
      long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);

      // Wait until the other jobs tried to re-acquire the mutex.
      while (m_mutexSemaphores.getPermitCount(mutexObject) != expectedPermitCount) {
        if (System.currentTimeMillis() > deadline) {
          fail(String.format("Timeout elapsed while waiting for a mutex-permit count. [expectedPermitCount=%s, actualPermitCount=%s]", expectedPermitCount, m_mutexSemaphores.getPermitCount(mutexObject)));
        }
        Thread.sleep(10);
      }
    }
  }
}
