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
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.filter.AlwaysFilter;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.commons.job.filter.SameFutureFilter;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.internal.ModelJobManager;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WaitUntilDoneTest {

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

  @Test
  public void testWaitAll() throws JobExecutionException, InterruptedException {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        Thread.sleep(1000);
        protocol.add("run-1");
      }
    });

    assertTrue(m_jobManager.waitUntilDone(new AlwaysFilter<IFuture<?>>(), 30, TimeUnit.SECONDS));
    assertEquals(CollectionUtility.hashSet("run-1"), protocol);
    assertTrue(m_jobManager.isDone(new AlwaysFilter<IFuture<?>>()));
  }

  @Test
  public void testWaitForFuture1() throws JobExecutionException, InterruptedException {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.

    final IFuture<Void> future1 = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        Thread.sleep(1000);
        protocol.add("run-1");
      }
    });

    assertTrue(m_jobManager.waitUntilDone(new SameFutureFilter(future1), 30, TimeUnit.SECONDS));
    assertEquals(CollectionUtility.hashSet("run-1"), protocol);
    assertTrue(m_jobManager.isDone(new AlwaysFilter<IFuture<?>>()));
    assertTrue(m_jobManager.isDone(new SameFutureFilter(future1)));
  }

  @Test
  public void testWaitForFuture2() throws JobExecutionException, InterruptedException {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>()); // synchronized because modified/read by different threads.

    final BlockingCountDownLatch latchJob2 = new BlockingCountDownLatch(1);

    final IFuture<Void> future1 = m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        Thread.sleep(1000);
        protocol.add("run-1");
      }
    });

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        latchJob2.await();
        protocol.add("run-2");
      }
    });

    assertTrue(m_jobManager.waitUntilDone(new SameFutureFilter(future1), 30, TimeUnit.SECONDS));
    assertTrue(m_jobManager.isDone(new SameFutureFilter(future1)));
    assertFalse(m_jobManager.isDone(new AlwaysFilter<IFuture<?>>()));
    assertFalse(m_jobManager.waitUntilDone(new AlwaysFilter<IFuture<?>>(), 500, TimeUnit.MILLISECONDS));
    assertEquals(CollectionUtility.hashSet("run-1"), protocol);

    latchJob2.countDown();
    assertTrue(m_jobManager.waitUntilDone(new AlwaysFilter<IFuture<?>>(), 30, TimeUnit.SECONDS));
    assertTrue(m_jobManager.isDone(new AlwaysFilter<IFuture<?>>()));
    assertEquals(CollectionUtility.hashSet("run-1", "run-2"), protocol);
  }
}
