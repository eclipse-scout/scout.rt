/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.filter.future;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.UUID;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SimpleScheduleBuilder;

@RunWith(PlatformTestRunner.class)
public class FutureFilterBuilderTest {

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
        .withExecutionSemaphore(mutex2)
        .withExecutionHint(JOB_IDENTIFIER));

    IFuture<?> future10 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withName("I")
        .withRunContext(new P_RunContext())
        .withExecutionSemaphore(mutex1)
        .withExecutionHint(JOB_IDENTIFIER));

    assertTrue(new FutureFilterBuilder().toFilter().test(future1));
    assertTrue(new FutureFilterBuilder().toFilter().test(future2));
    assertTrue(new FutureFilterBuilder().toFilter().test(future3));
    assertTrue(new FutureFilterBuilder().toFilter().test(future4));
    assertTrue(new FutureFilterBuilder().toFilter().test(future5));
    assertTrue(new FutureFilterBuilder().toFilter().test(future6));
    assertTrue(new FutureFilterBuilder().toFilter().test(future7));
    assertTrue(new FutureFilterBuilder().toFilter().test(future8));
    assertTrue(new FutureFilterBuilder().toFilter().test(future9));
    assertTrue(new FutureFilterBuilder().toFilter().test(future10));

    // with filtering for futures
    Predicate<IFuture<?>> filter = new FutureFilterBuilder()
        .andMatchFuture(future1, future2, future3, future4, future8, future9, future10)
        .toFilter();
    assertTrue(filter.test(future1));
    assertTrue(filter.test(future2));
    assertTrue(filter.test(future3));
    assertTrue(filter.test(future4));
    assertFalse(filter.test(future5));
    assertFalse(filter.test(future6));
    assertFalse(filter.test(future7));
    assertTrue(filter.test(future8));
    assertTrue(filter.test(future9));
    assertTrue(filter.test(future10));

    // additionally with filtering for single executing jobs
    filter = new FutureFilterBuilder()
        .andMatchFuture(future1, future2, future3, future4, future8, future9, future10)
        .andAreSingleExecuting()
        .toFilter();
    assertTrue(filter.test(future1));
    assertTrue(filter.test(future2));
    assertTrue(filter.test(future3));
    assertFalse(filter.test(future4));
    assertFalse(filter.test(future5));
    assertFalse(filter.test(future6));
    assertFalse(filter.test(future7));
    assertTrue(filter.test(future8));
    assertTrue(filter.test(future9));
    assertTrue(filter.test(future10));

    // additionally with filtering for mutex
    filter = new FutureFilterBuilder()
        .andMatchFuture(future1, future2, future3, future4, future8, future9, future10)
        .andAreSingleExecuting()
        .andMatchExecutionSemaphore(mutex1)
        .toFilter();
    assertFalse(filter.test(future1));
    assertTrue(filter.test(future2));
    assertTrue(filter.test(future3));
    assertFalse(filter.test(future4));
    assertFalse(filter.test(future5));
    assertFalse(filter.test(future6));
    assertFalse(filter.test(future7));
    assertTrue(filter.test(future8));
    assertFalse(filter.test(future9));
    assertTrue(filter.test(future10));

    // additionally with filtering for jobs running on behalf of a RunContext
    filter = new FutureFilterBuilder()
        .andMatchFuture(future1, future2, future3, future4, future8, future9, future10)
        .andAreSingleExecuting()
        .andMatchExecutionSemaphore(mutex1)
        .andMatchRunContext(RunContext.class)
        .toFilter();
    assertFalse(filter.test(future1));
    assertTrue(filter.test(future2));
    assertTrue(filter.test(future3));
    assertFalse(filter.test(future4));
    assertFalse(filter.test(future5));
    assertFalse(filter.test(future6));
    assertFalse(filter.test(future7));
    assertFalse(filter.test(future8));
    assertFalse(filter.test(future9));
    assertTrue(filter.test(future10));

    // additionally with filtering for jobs running on behalf of a specific P_RunContext
    filter = new FutureFilterBuilder()
        .andMatchFuture(future1, future2, future3, future4, future8, future9, future10)
        .andAreSingleExecuting()
        .andMatchExecutionSemaphore(mutex1)
        .andMatchRunContext(P_RunContext.class)
        .toFilter();
    assertFalse(filter.test(future1));
    assertFalse(filter.test(future2));
    assertTrue(filter.test(future3));
    assertFalse(filter.test(future4));
    assertFalse(filter.test(future5));
    assertFalse(filter.test(future6));
    assertFalse(filter.test(future7));
    assertFalse(filter.test(future8));
    assertFalse(filter.test(future9));
    assertTrue(filter.test(future10));

    // additionally with filtering for names
    filter = new FutureFilterBuilder()
        .andMatchFuture(future1, future2, future3, future4, future8, future9, future10)
        .andAreSingleExecuting()
        .andMatchExecutionSemaphore(mutex1)
        .andMatchRunContext(P_RunContext.class)
        .andMatchName("A", "B", "C")
        .toFilter();
    assertFalse(filter.test(future1));
    assertFalse(filter.test(future2));
    assertTrue(filter.test(future3));
    assertFalse(filter.test(future4));
    assertFalse(filter.test(future5));
    assertFalse(filter.test(future6));
    assertFalse(filter.test(future7));
    assertFalse(filter.test(future8));
    assertFalse(filter.test(future9));
    assertFalse(filter.test(future10));

    // additionally with filtering for other names
    filter = new FutureFilterBuilder()
        .andMatchFuture(future1, future2, future3, future4, future8, future9, future10)
        .andAreSingleExecuting()
        .andMatchExecutionSemaphore(mutex1)
        .andMatchRunContext(P_RunContext.class)
        .andMatchName("D", "E", "F")
        .toFilter();
    assertFalse(filter.test(future1));
    assertFalse(filter.test(future2));
    assertFalse(filter.test(future3));
    assertFalse(filter.test(future4));
    assertFalse(filter.test(future5));
    assertFalse(filter.test(future6));
    assertFalse(filter.test(future7));
    assertFalse(filter.test(future8));
    assertFalse(filter.test(future9));
    assertFalse(filter.test(future10));
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
        .withExecutionHint(JOB_IDENTIFIER)
        .withExecutionSemaphore(mutex));

    IFuture<?> future7 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionHint(JOB_IDENTIFIER)
        .withExecutionSemaphore(mutex));

    IFuture<?> future8 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionHint(JOB_IDENTIFIER)
        .withExecutionSemaphore(mutex));

    IFuture<?> future9 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionHint(JOB_IDENTIFIER)
        .withExecutionSemaphore(mutex));

    IFuture<?> future10 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionHint(JOB_IDENTIFIER)
        .withExecutionSemaphore(mutex));

    // One future exclusion with not other criteria
    Predicate<IFuture<?>> filter = Jobs.newFutureFilterBuilder()
        .andMatchNotFuture(future8).toFilter();
    assertTrue(filter.test(future1));
    assertTrue(filter.test(future2));
    assertTrue(filter.test(future3));
    assertTrue(filter.test(future4));
    assertTrue(filter.test(future5));
    assertTrue(filter.test(future6));
    assertTrue(filter.test(future7));
    assertFalse(filter.test(future8));
    assertTrue(filter.test(future9));
    assertTrue(filter.test(future10));

    // Multiple future exclusions with not other criteria
    filter = Jobs.newFutureFilterBuilder()
        .andMatchNotFuture(future8, future9).toFilter();
    assertTrue(filter.test(future1));
    assertTrue(filter.test(future2));
    assertTrue(filter.test(future3));
    assertTrue(filter.test(future4));
    assertTrue(filter.test(future5));
    assertTrue(filter.test(future6));
    assertTrue(filter.test(future7));
    assertFalse(filter.test(future8));
    assertFalse(filter.test(future9));
    assertTrue(filter.test(future10));

    // One future exclusion with other criterion (mutex)
    filter = Jobs.newFutureFilterBuilder()
        .andMatchExecutionSemaphore(mutex)
        .andMatchNotFuture(future8).toFilter();
    assertFalse(filter.test(future1));
    assertFalse(filter.test(future2));
    assertFalse(filter.test(future3));
    assertFalse(filter.test(future4));
    assertFalse(filter.test(future5));
    assertTrue(filter.test(future6));
    assertTrue(filter.test(future7));
    assertFalse(filter.test(future8));
    assertTrue(filter.test(future9));
    assertTrue(filter.test(future10));

    // Multiple future exclusion with other criterion (mutex)
    filter = Jobs.newFutureFilterBuilder()
        .andMatchExecutionSemaphore(mutex)
        .andMatchNotFuture(future8, future9).toFilter();
    assertFalse(filter.test(future1));
    assertFalse(filter.test(future2));
    assertFalse(filter.test(future3));
    assertFalse(filter.test(future4));
    assertFalse(filter.test(future5));
    assertTrue(filter.test(future6));
    assertTrue(filter.test(future7));
    assertFalse(filter.test(future8));
    assertFalse(filter.test(future9));
    assertTrue(filter.test(future10));
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
