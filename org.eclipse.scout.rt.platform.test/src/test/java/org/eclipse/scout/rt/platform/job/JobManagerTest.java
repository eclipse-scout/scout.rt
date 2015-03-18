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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.IVisitor;
import org.eclipse.scout.commons.filter.AlwaysFilter;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobManagerTest {

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
  public void testVisit() throws Exception {
    final BlockingCountDownLatch latch = new BlockingCountDownLatch(3);

    IFuture<Void> future1 = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        latch.countDownAndBlock();
      }
    }, JobInput.defaults());

    IFuture<Void> future2 = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        latch.countDownAndBlock();
      }
    }, JobInput.defaults());

    IFuture<Void> future3 = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        latch.countDownAndBlock();
      }
    }, JobInput.defaults());

    assertTrue(latch.await());

    // RUN THE TEST
    final Set<IFuture<?>> protocol = new HashSet<>();
    m_jobManager.visit(new AlwaysFilter<IFuture<?>>(), new IVisitor<IFuture<?>>() {

      @Override
      public boolean visit(IFuture<?> future) {
        protocol.add(future);
        return true;
      }
    });

    // VERIFY
    assertEquals(CollectionUtility.hashSet(future1, future2, future3), protocol);
  }

  @Test
  public void testShutdown() throws Exception {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(3);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(3);

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
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
    }, JobInput.defaults());

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("interrupted-2");
        }
        finally {
          verifyLatch.countDown();
        }
      }
    }, JobInput.defaults());

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("interrupted-3");
        }
        finally {
          verifyLatch.countDown();
        }
      }
    }, JobInput.defaults());

    assertTrue(setupLatch.await());

    // RUN THE TEST
    m_jobManager.shutdown();

    // VERIFY
    assertTrue(verifyLatch.await());

    assertEquals(CollectionUtility.hashSet("interrupted-1", "interrupted-2", "interrupted-3"), protocol);
  }

  @Test(expected = AssertionException.class)
  public void testValidateInput() {
    new _JobManager().interceptInput(null);
  }

  private class _JobManager extends JobManager {

    @Override
    protected JobInput interceptInput(JobInput input) {
      return super.interceptInput(input);
    }

    @Override
    protected void finalize() throws Throwable {
      shutdown();
      super.finalize();
    }
  }
}
