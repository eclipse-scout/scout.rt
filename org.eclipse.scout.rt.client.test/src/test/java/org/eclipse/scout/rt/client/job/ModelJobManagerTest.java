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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IFutureVisitor;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.internal.ModelJobManager;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
import org.eclipse.scout.rt.testing.platform.ScoutPlatformTestRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ScoutPlatformTestRunner.class)
public class ModelJobManagerTest {

  private static ExecutorService s_executor;

  private IModelJobManager m_jobManager;

  @Before
  public void before() {
    m_jobManager = new ModelJobManager();

    ISession.CURRENT.set(mock(IClientSession.class));
  }

  @After
  public void after() {
    m_jobManager.shutdown();

    ISession.CURRENT.remove();
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
    final List<String> runningProtocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch latch = new BlockingCountDownLatch(1);

    IFuture<Void> future1 = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {

        m_jobManager.runNow(new IRunnable() {

          @Override
          public void run() throws Exception {
            runningProtocol.add("running-1");
            latch.countDownAndBlock();
          }
        });
      }
    });

    IFuture<Void> future2 = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        runningProtocol.add("running-2");
      }
    });

    IFuture<Void> future3 = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        runningProtocol.add("running-3");
      }
    });

    assertTrue(latch.await());

    // RUN THE TEST
    final Set<Future<?>> futureProtocol = new HashSet<>();
    m_jobManager.visit(new IFutureVisitor() {

      @Override
      public boolean visit(Future<?> future) {
        futureProtocol.add(future);
        return true;
      }
    });

    // VERIFY
    assertEquals(CollectionUtility.hashSet(future1.getDelegate(), future2.getDelegate(), future3.getDelegate()), futureProtocol);
    assertEquals(CollectionUtility.arrayList("running-1"), runningProtocol);
  }

  @Test
  public void testShutdown() throws Exception {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(3);

    IFuture<Void> future1 = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("running-1");
        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("interrupted-1");
        }
        finally {
          verifyLatch.countDown();
        }
      }
    });

    IFuture<Void> future2 = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("running-2");
      }
    });

    IFuture<Void> future3 = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add("running-3");
      }
    });

    assertTrue(setupLatch.await());

    // RUN THE TEST
    m_jobManager.shutdown();

    // VERIFY
    assertTrue(m_jobManager.waitForIdle(10, TimeUnit.SECONDS));

    assertEquals(CollectionUtility.hashSet("running-1", "interrupted-1"), protocol);

    // verify future 1
    assertTrue(future1.isCancelled());
    assertTrue(future1.isDone());
    try {
      future1.get();
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(e.isCancellation());
      assertFalse(e.isRejection());
      assertFalse(e.isInterruption());
      assertFalse(e.isTimeout());
    }

    // verify future 2
    assertTrue(future2.isCancelled());
    assertTrue(future2.isDone());
    try {
      future2.get();
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(e.isCancellation());
      assertFalse(e.isRejection());
      assertFalse(e.isInterruption());
      assertFalse(e.isTimeout());
    }

    // verify future 3
    assertTrue(future3.isCancelled());
    assertTrue(future3.isDone());
    try {
      future3.get();
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(e.isCancellation());
      assertFalse(e.isRejection());
      assertFalse(e.isInterruption());
      assertFalse(e.isTimeout());
    }
  }
}
