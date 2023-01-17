/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.job.filter.future;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.filter.future.FutureFilter;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.job.filter.future.SessionFutureFilter;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SimpleScheduleBuilder;

@RunWith(PlatformTestRunner.class)
public class ClientRunContextFutureFilterTest {

  private IFuture<?> m_clientJobFuture;
  private IFuture<?> m_modelJobFuture;
  private IClientSession m_clientSession1;
  private IClientSession m_clientSession2;

  @Before
  public void before() {
    m_clientSession1 = mock(IClientSession.class);
    when(m_clientSession1.getModelJobSemaphore()).thenReturn(Jobs.newExecutionSemaphore(1));

    m_clientSession2 = mock(IClientSession.class);
    when(m_clientSession2.getModelJobSemaphore()).thenReturn(Jobs.newExecutionSemaphore(1));

    m_clientJobFuture = Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withRunContext(ClientRunContexts.empty().withSession(m_clientSession1, true)));
    m_modelJobFuture = ModelJobs.schedule(mock(IRunnable.class), ModelJobs.newInput(ClientRunContexts.empty().withSession(m_clientSession1, true)));
  }

  @Test
  public void testBlocked() {
    final IBlockingCondition condition = Jobs.newBlockingCondition(true);

    // Client Job
    m_clientJobFuture = Jobs.schedule(() -> condition.waitFor(10, TimeUnit.SECONDS), Jobs.newInput().withRunContext(ClientRunContexts.empty().withSession(m_clientSession1, true)));

    // Model Job
    m_modelJobFuture = ModelJobs.schedule(() -> condition.waitFor(10, TimeUnit.SECONDS), ModelJobs.newInput(ClientRunContexts.empty().withSession(m_clientSession1, true)));

    JobTestUtil.waitForState(m_clientJobFuture, JobState.WAITING_FOR_BLOCKING_CONDITION);
    JobTestUtil.waitForState(m_modelJobFuture, JobState.WAITING_FOR_BLOCKING_CONDITION);

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .toFilter()
        .test(m_clientJobFuture));

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchState(JobState.WAITING_FOR_BLOCKING_CONDITION)
        .toFilter()
        .test(m_clientJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchNotState(JobState.WAITING_FOR_BLOCKING_CONDITION)
        .toFilter()
        .test(m_clientJobFuture));

    assertTrue(ModelJobs.newFutureFilterBuilder()
        .toFilter()
        .test(m_modelJobFuture));

    assertTrue(ModelJobs.newFutureFilterBuilder()
        .andMatchState(JobState.WAITING_FOR_BLOCKING_CONDITION)
        .toFilter()
        .test(m_modelJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andMatchNotState(JobState.WAITING_FOR_BLOCKING_CONDITION)
        .toFilter()
        .test(m_modelJobFuture));

    // Release threads
    condition.setBlocking(false);
  }

  @Test
  public void testRepetitive() {
    IFuture<Void> clientJobFuture = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withRunContext(ClientRunContexts.empty().withSession(m_clientSession1, true))
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever())));

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .toFilter()
        .test(clientJobFuture));

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andAreNotSingleExecuting()
        .toFilter()
        .test(clientJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andAreSingleExecuting()
        .toFilter()
        .test(clientJobFuture));

    IFuture<Void> modelJobFuture = ModelJobs.schedule(mock(IRunnable.class), ModelJobs.newInput(ClientRunContexts.empty().withSession(m_clientSession1, true))
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever())));

    assertTrue(ModelJobs.newFutureFilterBuilder()
        .toFilter()
        .test(modelJobFuture));

    assertTrue(ModelJobs.newFutureFilterBuilder()
        .andAreNotSingleExecuting()
        .toFilter()
        .test(modelJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andAreSingleExecuting()
        .toFilter()
        .test(modelJobFuture));

    // cleanup
    Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
        .andMatchFuture(clientJobFuture, modelJobFuture)
        .toFilter(), true);
  }

  @Test
  public void testSession() {
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatch(new SessionFutureFilter(m_clientSession1))
        .toFilter()
        .test(m_clientJobFuture));

    assertTrue(ModelJobs.newFutureFilterBuilder()
        .andMatch(new SessionFutureFilter(m_clientSession1))
        .toFilter()
        .test(m_modelJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatch(new SessionFutureFilter(m_clientSession2))
        .toFilter()
        .test(m_clientJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andMatch(new SessionFutureFilter(m_clientSession2))
        .toFilter()
        .test(m_modelJobFuture));
  }

  @Test
  public void testCurrentSession() {
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class).andMatch(new SessionFutureFilter(m_clientSession1))
        .toFilter()
        .test(m_clientJobFuture));

    assertTrue(ModelJobs.newFutureFilterBuilder().andMatch(new SessionFutureFilter(m_clientSession1))
        .toFilter()
        .test(m_modelJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class).andMatch(new SessionFutureFilter(m_clientSession2))
        .toFilter()
        .test(m_clientJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andMatch(new SessionFutureFilter(m_clientSession2))
        .toFilter()
        .test(m_modelJobFuture));
  }

  @Test
  public void testNotCurrentSession() {
    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchNot(new SessionFutureFilter(m_clientSession1))
        .toFilter()
        .test(m_clientJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andMatchNot(new SessionFutureFilter(m_clientSession1))
        .toFilter()
        .test(m_modelJobFuture));

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchNot(new SessionFutureFilter(m_clientSession2))
        .toFilter()
        .test(m_clientJobFuture));

    assertTrue(ModelJobs.newFutureFilterBuilder()
        .andMatchNot(new SessionFutureFilter(m_clientSession2))
        .toFilter()
        .test(m_modelJobFuture));
  }

  @Test
  public void testFuture() {
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchFuture(m_clientJobFuture, m_modelJobFuture)
        .toFilter()
        .test(m_clientJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andMatchFuture(m_clientJobFuture, m_modelJobFuture)
        .toFilter()
        .test(m_clientJobFuture));

    assertTrue(new FutureFilter(m_clientJobFuture, m_modelJobFuture).test(m_clientJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchNot(ModelJobFutureFilter.INSTANCE)
        .andMatchFuture(m_clientJobFuture, m_modelJobFuture)
        .toFilter()
        .test(m_modelJobFuture));

    assertTrue(ModelJobs.newFutureFilterBuilder()
        .andMatchFuture(m_clientJobFuture, m_modelJobFuture)
        .toFilter()
        .test(m_modelJobFuture));

    assertTrue(new FutureFilter(m_clientJobFuture, m_modelJobFuture).test(m_modelJobFuture));

    assertTrue(ModelJobs.newFutureFilterBuilder()
        .andMatchFuture(m_modelJobFuture)
        .toFilter()
        .test(m_modelJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchNot(ModelJobFutureFilter.INSTANCE)
        .andMatchFuture(m_modelJobFuture)
        .toFilter()
        .test(m_modelJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andMatchFuture(m_clientJobFuture)
        .toFilter()
        .test(m_modelJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchFuture(m_clientJobFuture)
        .toFilter()
        .test(m_modelJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andMatchFuture(m_modelJobFuture)
        .toFilter()
        .test(m_clientJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchFuture(m_modelJobFuture)
        .toFilter()
        .test(m_clientJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andMatchFuture(m_clientJobFuture)
        .toFilter()
        .test(m_clientJobFuture));

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchFuture(m_clientJobFuture)
        .toFilter()
        .test(m_clientJobFuture));
  }

  @Test
  public void testCurrentFuture() {
    IFuture.CURRENT.set(m_clientJobFuture);

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchFuture(IFuture.CURRENT.get())
        .toFilter()
        .test(m_clientJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andMatchFuture(IFuture.CURRENT.get())
        .toFilter()
        .test(m_modelJobFuture));

    IFuture.CURRENT.set(m_modelJobFuture);

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchFuture(IFuture.CURRENT.get())
        .toFilter()
        .test(m_clientJobFuture));

    assertTrue(ModelJobs.newFutureFilterBuilder()
        .andMatchFuture(IFuture.CURRENT.get())
        .toFilter()
        .test(m_modelJobFuture));

    IFuture.CURRENT.remove();
  }

  @Test
  public void testNotCurrentFuture() {
    IFuture.CURRENT.set(m_clientJobFuture);

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter()
        .test(m_clientJobFuture));

    assertTrue(ModelJobs.newFutureFilterBuilder()
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter()
        .test(m_modelJobFuture));

    IFuture.CURRENT.set(m_modelJobFuture);

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter()
        .test(m_clientJobFuture));

    assertFalse(ModelJobs.newFutureFilterBuilder()
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter()
        .test(m_modelJobFuture));

    IFuture.CURRENT.remove();
  }

  @Test
  public void testMutualExclusion() {
    IExecutionSemaphore mutex1 = Jobs.newExecutionSemaphore(1);
    IExecutionSemaphore mutex2 = Jobs.newExecutionSemaphore(1);

    m_clientJobFuture.getJobInput().withExecutionSemaphore(mutex1);
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .toFilter()
        .test(m_clientJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchExecutionSemaphore(null)
        .toFilter()
        .test(m_clientJobFuture));

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchExecutionSemaphore(mutex1)
        .toFilter()
        .test(m_clientJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchExecutionSemaphore(mutex2)
        .toFilter()
        .test(m_clientJobFuture));

    m_clientJobFuture.getJobInput().withExecutionSemaphore(null);
    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .toFilter()
        .test(m_clientJobFuture));

    assertTrue(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchExecutionSemaphore(null)
        .toFilter()
        .test(m_clientJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchExecutionSemaphore(mutex1)
        .toFilter()
        .test(m_clientJobFuture));

    assertFalse(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchExecutionSemaphore(mutex2)
        .toFilter()
        .test(m_clientJobFuture));
  }

  @Test
  public void testCustomFilter() {
    // False Filter
    assertFalse(Jobs.newFutureFilterBuilder().andMatchRunContext(ClientRunContext.class).andMatch(future -> false).toFilter().test(m_clientJobFuture));

    // True Filter
    assertTrue(Jobs.newFutureFilterBuilder().andMatchRunContext(ClientRunContext.class).andMatch(future -> true).toFilter().test(m_clientJobFuture));

    // True/False Filter
    assertFalse(Jobs.newFutureFilterBuilder().andMatchRunContext(ClientRunContext.class).andMatch(future -> true).andMatch(future -> false).toFilter().test(m_clientJobFuture));
  }
}
