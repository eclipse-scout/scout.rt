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
package org.eclipse.scout.rt.client.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ModelJobTest {

  private IClientSession m_clientSession1;
  private IClientSession m_clientSession2;

  @Before
  public void before() {
    m_clientSession1 = mock(IClientSession.class);
    when(m_clientSession1.getModelJobSemaphore()).thenReturn(Jobs.newExecutionSemaphore(1));

    m_clientSession2 = mock(IClientSession.class);
    when(m_clientSession2.getModelJobSemaphore()).thenReturn(Jobs.newExecutionSemaphore(1));
  }

  @After
  public void after() {
    ISession.CURRENT.remove();
  }

  @Test(expected = AssertionException.class)
  public void testNoSession() {
    ISession.CURRENT.remove();
    ModelJobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
      }
    }, ModelJobs.newInput(ClientRunContexts.copyCurrent()));
  }

  @Test
  public void testModelThread() {
    final AtomicBoolean modelThread = new AtomicBoolean();

    assertFalse(ModelJobs.isModelThread());

    ModelJobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        modelThread.set(ModelJobs.isModelThread());
      }
    }, ModelJobs.newInput(ClientRunContexts.empty().withSession(m_clientSession1, true))).awaitDoneAndGet();

    assertFalse(ModelJobs.isModelThread());
    assertTrue(modelThread.get());
  }

  @Test
  public void testThreadName() throws InterruptedException {
    ClientRunContext clientRunContext = ClientRunContexts.empty().withSession(m_clientSession1, true);
    assertEquals("scout-model-thread", ModelJobs.newInput(clientRunContext).getThreadName());
  }
}
