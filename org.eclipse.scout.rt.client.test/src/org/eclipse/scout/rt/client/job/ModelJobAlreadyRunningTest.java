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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IAsyncFuture;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ModelJobAlreadyRunningTest {

  private IClientSession m_clientSession;

  @Before
  public void before() {
    m_clientSession = mock(IClientSession.class);
    when(m_clientSession.getLocale()).thenReturn(new Locale("de", "DE"));
    when(m_clientSession.getTexts()).thenReturn(new ScoutTexts());
    when(m_clientSession.getModelJobManager()).thenReturn(new ModelJobManager());
  }

  @After
  public void after() {
    m_clientSession.getModelJobManager().shutdown();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testAlreadyRunning() throws ProcessingException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.
    // This job runs forever.
    ModelJob job1 = new ModelJob("job-1", m_clientSession) {

      @Override
      protected void run() throws Exception {
        protocol.add("running-1");
        Thread.sleep(TimeUnit.SECONDS.toMillis(30));
      }
    };
    job1.schedule();

    // Try to schedule job again.
    try {
      job1.schedule();
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(e.isRejection());
      assertFalse(e.isCancellation());
      assertFalse(e.isInterruption());
      assertFalse(e.isTimeout());
    }

    // Try to schedule job again (with AsyncFuture)
    try {
      job1.schedule(mock(IAsyncFuture.class));
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(e.isRejection());
      assertFalse(e.isCancellation());
      assertFalse(e.isInterruption());
      assertFalse(e.isTimeout());
    }

    // Try to run job again (with AsyncFuture)
    ((ModelJobManager) m_clientSession.getModelJobManager()).m_mutexSemaphore.registerAsModelThread();
    try {
      job1.runNow();
      fail();
    }
    catch (JobExecutionException e) {
      assertTrue(e.isRejection());
      assertFalse(e.isCancellation());
      assertFalse(e.isInterruption());
      assertFalse(e.isTimeout());
    }
  }
}
