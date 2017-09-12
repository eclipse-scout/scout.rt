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
package org.eclipse.scout.rt.platform.job.filter.event;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventData;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SimpleScheduleBuilder;

@RunWith(PlatformTestRunner.class)
public class JobEventFilterBuilderTest {

  private static final String JOB_IDENTIFIER = UUID.randomUUID().toString();

  @After
  public void after() {
    Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
        .andMatchExecutionHint(JOB_IDENTIFIER)
        .toFilter(), true);
  }

  @Test
  public void test() {
    IExecutionSemaphore mutex1 = Jobs.newExecutionSemaphore(1);
    IExecutionSemaphore mutex2 = Jobs.newExecutionSemaphore(1);

    IFuture<?> future1 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withName("A")
        .withExecutionHint(JOB_IDENTIFIER));

    IFuture<?> future2 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withName("B")
        .withRunContext(RunContexts.empty())
        .withExecutionSemaphore(mutex1)
        .withExecutionHint(JOB_IDENTIFIER));

    IFuture<?> future3 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withName("C")
        .withRunContext(new P_RunContext())
        .withExecutionSemaphore(mutex1)
        .withExecutionHint(JOB_IDENTIFIER));

    IFuture<?> future4 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withName("D")
        .withExecutionHint(JOB_IDENTIFIER)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever())));

    IFuture<?> future5 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withName("E")
        .withExecutionHint(JOB_IDENTIFIER)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever())));

    IFuture<?> future6 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withName("E")
        .withRunContext(new P_RunContext())
        .withExecutionHint(JOB_IDENTIFIER)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever())));

    IFuture<?> future7 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withName("F")
        .withExecutionSemaphore(mutex1)
        .withExecutionHint(JOB_IDENTIFIER));

    IFuture<?> future8 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withName("G")
        .withExecutionSemaphore(mutex1)
        .withExecutionHint(JOB_IDENTIFIER));

    IFuture<?> future9 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withName("H")
        .withExecutionSemaphore(mutex2));

    IFuture<?> future10 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withName("I")
        .withRunContext(new P_RunContext())
        .withExecutionSemaphore(mutex1)
        .withExecutionHint(JOB_IDENTIFIER));

    // with filtering for futures
    IFilter<JobEvent> filter = new JobEventFilterBuilder()
        .andMatchFuture(future1, future2, future3, future4, future8, future9, future10)
        .toFilter();

    assertTrue(filter.accept(newJobStateChangedEvent(future1)));
    assertTrue(filter.accept(newJobStateChangedEvent(future2)));
    assertTrue(filter.accept(newJobStateChangedEvent(future3)));
    assertTrue(filter.accept(newJobStateChangedEvent(future4)));
    assertFalse(filter.accept(newJobStateChangedEvent(future5)));
    assertFalse(filter.accept(newJobStateChangedEvent(future6)));
    assertFalse(filter.accept(newJobStateChangedEvent(future7)));
    assertTrue(filter.accept(newJobStateChangedEvent(future8)));
    assertTrue(filter.accept(newJobStateChangedEvent(future9)));
    assertTrue(filter.accept(newJobStateChangedEvent(future10)));

    // additionally with filtering for single executing jobs
    filter = new JobEventFilterBuilder()
        .andMatchFuture(future1, future2, future3, future4, future8, future9, future10)
        .andAreSingleExecuting()
        .toFilter();
    assertTrue(filter.accept(newJobStateChangedEvent(future1)));
    assertTrue(filter.accept(newJobStateChangedEvent(future2)));
    assertTrue(filter.accept(newJobStateChangedEvent(future3)));
    assertFalse(filter.accept(newJobStateChangedEvent(future4)));
    assertFalse(filter.accept(newJobStateChangedEvent(future5)));
    assertFalse(filter.accept(newJobStateChangedEvent(future6)));
    assertFalse(filter.accept(newJobStateChangedEvent(future7)));
    assertTrue(filter.accept(newJobStateChangedEvent(future8)));
    assertTrue(filter.accept(newJobStateChangedEvent(future9)));
    assertTrue(filter.accept(newJobStateChangedEvent(future10)));

    // additionally with filtering for mutex
    filter = new JobEventFilterBuilder()
        .andMatchFuture(future1, future2, future3, future4, future8, future9, future10)
        .andAreSingleExecuting()
        .andMatchExecutionSemaphore(mutex1)
        .toFilter();
    assertFalse(filter.accept(newJobStateChangedEvent(future1)));
    assertTrue(filter.accept(newJobStateChangedEvent(future2)));
    assertTrue(filter.accept(newJobStateChangedEvent(future3)));
    assertFalse(filter.accept(newJobStateChangedEvent(future4)));
    assertFalse(filter.accept(newJobStateChangedEvent(future5)));
    assertFalse(filter.accept(newJobStateChangedEvent(future6)));
    assertFalse(filter.accept(newJobStateChangedEvent(future7)));
    assertTrue(filter.accept(newJobStateChangedEvent(future8)));
    assertFalse(filter.accept(newJobStateChangedEvent(future9)));
    assertTrue(filter.accept(newJobStateChangedEvent(future10)));

    // additionally with filtering for jobs running on behalf of a RunContext
    filter = new JobEventFilterBuilder()
        .andMatchFuture(future1, future2, future3, future4, future8, future9, future10)
        .andAreSingleExecuting()
        .andMatchExecutionSemaphore(mutex1)
        .andMatchRunContext(RunContext.class)
        .toFilter();
    assertFalse(filter.accept(newJobStateChangedEvent(future1)));
    assertTrue(filter.accept(newJobStateChangedEvent(future2)));
    assertTrue(filter.accept(newJobStateChangedEvent(future3)));
    assertFalse(filter.accept(newJobStateChangedEvent(future4)));
    assertFalse(filter.accept(newJobStateChangedEvent(future5)));
    assertFalse(filter.accept(newJobStateChangedEvent(future6)));
    assertFalse(filter.accept(newJobStateChangedEvent(future7)));
    assertFalse(filter.accept(newJobStateChangedEvent(future8)));
    assertFalse(filter.accept(newJobStateChangedEvent(future9)));
    assertTrue(filter.accept(newJobStateChangedEvent(future10)));

    // additionally with filtering for jobs running on behalf of a specific P_RunContext
    filter = new JobEventFilterBuilder()
        .andMatchFuture(future1, future2, future3, future4, future8, future9, future10)
        .andAreSingleExecuting()
        .andMatchExecutionSemaphore(mutex1)
        .andMatchRunContext(P_RunContext.class)
        .toFilter();
    assertFalse(filter.accept(newJobStateChangedEvent(future1)));
    assertFalse(filter.accept(newJobStateChangedEvent(future2)));
    assertTrue(filter.accept(newJobStateChangedEvent(future3)));
    assertFalse(filter.accept(newJobStateChangedEvent(future4)));
    assertFalse(filter.accept(newJobStateChangedEvent(future5)));
    assertFalse(filter.accept(newJobStateChangedEvent(future6)));
    assertFalse(filter.accept(newJobStateChangedEvent(future7)));
    assertFalse(filter.accept(newJobStateChangedEvent(future8)));
    assertFalse(filter.accept(newJobStateChangedEvent(future9)));
    assertTrue(filter.accept(newJobStateChangedEvent(future10)));

    // additionally with filtering for names
    filter = new JobEventFilterBuilder()
        .andMatchFuture(future1, future2, future3, future4, future8, future9, future10)
        .andAreSingleExecuting()
        .andMatchExecutionSemaphore(mutex1)
        .andMatchRunContext(P_RunContext.class)
        .andMatchName("A", "B", "C")
        .toFilter();
    assertFalse(filter.accept(newJobStateChangedEvent(future1)));
    assertFalse(filter.accept(newJobStateChangedEvent(future2)));
    assertTrue(filter.accept(newJobStateChangedEvent(future3)));
    assertFalse(filter.accept(newJobStateChangedEvent(future4)));
    assertFalse(filter.accept(newJobStateChangedEvent(future5)));
    assertFalse(filter.accept(newJobStateChangedEvent(future6)));
    assertFalse(filter.accept(newJobStateChangedEvent(future7)));
    assertFalse(filter.accept(newJobStateChangedEvent(future8)));
    assertFalse(filter.accept(newJobStateChangedEvent(future9)));
    assertFalse(filter.accept(newJobStateChangedEvent(future10)));

    // additionally with filtering for other names
    filter = new JobEventFilterBuilder()
        .andMatchFuture(future1, future2, future3, future4, future8, future9, future10)
        .andAreSingleExecuting()
        .andMatchExecutionSemaphore(mutex1)
        .andMatchRunContext(P_RunContext.class)
        .andMatchName("D", "E", "F")
        .toFilter();
    assertFalse(filter.accept(newJobStateChangedEvent(future1)));
    assertFalse(filter.accept(newJobStateChangedEvent(future2)));
    assertFalse(filter.accept(newJobStateChangedEvent(future3)));
    assertFalse(filter.accept(newJobStateChangedEvent(future4)));
    assertFalse(filter.accept(newJobStateChangedEvent(future5)));
    assertFalse(filter.accept(newJobStateChangedEvent(future6)));
    assertFalse(filter.accept(newJobStateChangedEvent(future7)));
    assertFalse(filter.accept(newJobStateChangedEvent(future8)));
    assertFalse(filter.accept(newJobStateChangedEvent(future9)));
    assertFalse(filter.accept(newJobStateChangedEvent(future10)));
  }

  @Test
  public void testFutureExclusion() {
    IExecutionSemaphore mutex = Jobs.newExecutionSemaphore(1);

    IFuture<?> future1 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionHint(JOB_IDENTIFIER));

    IFuture<?> future2 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionHint(JOB_IDENTIFIER));

    IFuture<?> future3 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionHint(JOB_IDENTIFIER));

    IFuture<?> future4 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionHint(JOB_IDENTIFIER));

    IFuture<?> future5 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionHint(JOB_IDENTIFIER));

    IFuture<?> future6 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionSemaphore(mutex)
        .withExecutionHint(JOB_IDENTIFIER));

    IFuture<?> future7 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionSemaphore(mutex)
        .withExecutionHint(JOB_IDENTIFIER));

    IFuture<?> future8 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionSemaphore(mutex)
        .withExecutionHint(JOB_IDENTIFIER));

    IFuture<?> future9 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionSemaphore(mutex)
        .withExecutionHint(JOB_IDENTIFIER));

    IFuture<?> future10 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionSemaphore(mutex)
        .withExecutionHint(JOB_IDENTIFIER));

    // One future exclusion with no other criteria
    IFilter<JobEvent> filter = Jobs.newEventFilterBuilder()
        .andMatchNotFuture(future8).toFilter();
    assertTrue(filter.accept(newJobStateChangedEvent(future1)));
    assertTrue(filter.accept(newJobStateChangedEvent(future2)));
    assertTrue(filter.accept(newJobStateChangedEvent(future3)));
    assertTrue(filter.accept(newJobStateChangedEvent(future4)));
    assertTrue(filter.accept(newJobStateChangedEvent(future5)));
    assertTrue(filter.accept(newJobStateChangedEvent(future6)));
    assertTrue(filter.accept(newJobStateChangedEvent(future7)));
    assertFalse(filter.accept(newJobStateChangedEvent(future8)));
    assertTrue(filter.accept(newJobStateChangedEvent(future9)));
    assertTrue(filter.accept(newJobStateChangedEvent(future10)));

    // Multiple future exclusions with no other criteria
    filter = Jobs.newEventFilterBuilder()
        .andMatchNotFuture(future8, future9).toFilter();
    assertTrue(filter.accept(newJobStateChangedEvent(future1)));
    assertTrue(filter.accept(newJobStateChangedEvent(future2)));
    assertTrue(filter.accept(newJobStateChangedEvent(future3)));
    assertTrue(filter.accept(newJobStateChangedEvent(future4)));
    assertTrue(filter.accept(newJobStateChangedEvent(future5)));
    assertTrue(filter.accept(newJobStateChangedEvent(future6)));
    assertTrue(filter.accept(newJobStateChangedEvent(future7)));
    assertFalse(filter.accept(newJobStateChangedEvent(future8)));
    assertFalse(filter.accept(newJobStateChangedEvent(future9)));
    assertTrue(filter.accept(newJobStateChangedEvent(future10)));

    // One future exclusion with other criterion (mutex)
    filter = Jobs.newEventFilterBuilder()
        .andMatchExecutionSemaphore(mutex)
        .andMatchNotFuture(future8).toFilter();
    assertFalse(filter.accept(newJobStateChangedEvent(future1)));
    assertFalse(filter.accept(newJobStateChangedEvent(future2)));
    assertFalse(filter.accept(newJobStateChangedEvent(future3)));
    assertFalse(filter.accept(newJobStateChangedEvent(future4)));
    assertFalse(filter.accept(newJobStateChangedEvent(future5)));
    assertTrue(filter.accept(newJobStateChangedEvent(future6)));
    assertTrue(filter.accept(newJobStateChangedEvent(future7)));
    assertFalse(filter.accept(newJobStateChangedEvent(future8)));
    assertTrue(filter.accept(newJobStateChangedEvent(future9)));
    assertTrue(filter.accept(newJobStateChangedEvent(future10)));

    // Multiple future exclusion with other criterion (mutex)
    filter = Jobs.newEventFilterBuilder()
        .andMatchExecutionSemaphore(mutex)
        .andMatchNotFuture(future8, future9).toFilter();
    assertFalse(filter.accept(newJobStateChangedEvent(future1)));
    assertFalse(filter.accept(newJobStateChangedEvent(future2)));
    assertFalse(filter.accept(newJobStateChangedEvent(future3)));
    assertFalse(filter.accept(newJobStateChangedEvent(future4)));
    assertFalse(filter.accept(newJobStateChangedEvent(future5)));
    assertTrue(filter.accept(newJobStateChangedEvent(future6)));
    assertTrue(filter.accept(newJobStateChangedEvent(future7)));
    assertFalse(filter.accept(newJobStateChangedEvent(future8)));
    assertFalse(filter.accept(newJobStateChangedEvent(future9)));
    assertTrue(filter.accept(newJobStateChangedEvent(future10)));
  }

  private static JobEvent newJobStateChangedEvent(IFuture<?> future) {
    return new JobEvent(mock(IJobManager.class), JobEventType.JOB_STATE_CHANGED, new JobEventData().withFuture(future));
  }

  private static class P_RunContext extends RunContext {

    @Override
    public RunContext copy() {
      final P_RunContext copy = new P_RunContext();
      copy.copyValues(this);
      return copy;
    }
  }
}
