/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.*;

import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SimpleScheduleBuilder;

@RunWith(PlatformTestRunner.class)
public class JobsTest {

  @Test
  public void testScheduleWithoutInput() {
    NlsLocale.set(Locale.CANADA_FRENCH);

    // Test schedule
    IFuture<?> actualFuture = Jobs.schedule((Callable<IFuture<?>>) () -> IFuture.CURRENT.get(), Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()))
        .awaitDoneAndGet();

    assertEquals(Locale.CANADA_FRENCH, actualFuture.getJobInput().getRunContext().getLocale());

    // schedule with delay
    actualFuture = Jobs.schedule((Callable<IFuture<?>>) () -> IFuture.CURRENT.get(), Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.MILLISECONDS)))
        .awaitDoneAndGet();

    assertEquals(Locale.CANADA_FRENCH, actualFuture.getJobInput().getRunContext().getLocale());

    // schedule at fixed rate
    final Holder<IFuture<?>> actualFutureHolder = new Holder<>();
    Jobs.schedule(() -> {
      actualFutureHolder.setValue(IFuture.CURRENT.get());
      IFuture.CURRENT.get().cancel(false); // cancel periodic action
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(1)
                .repeatForever())))
        .awaitDone();

    assertEquals(Locale.CANADA_FRENCH, actualFuture.getJobInput().getRunContext().getLocale());

    // schedule with fixed delay
    actualFutureHolder.setValue(null);
    Jobs.schedule(() -> {
      actualFutureHolder.setValue(IFuture.CURRENT.get());
      IFuture.CURRENT.get().cancel(false); // cancel periodic action
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(FixedDelayScheduleBuilder.repeatForever(1, TimeUnit.MILLISECONDS))))
        .awaitDone();

    assertEquals(Locale.CANADA_FRENCH, actualFuture.getJobInput().getRunContext().getLocale());
  }

  @Test
  public void testScheduleWithoutRunContext() {
    NlsLocale.set(Locale.CANADA_FRENCH);

    // Test schedule
    IFuture<?> actualFuture = Jobs.schedule((Callable<IFuture<?>>) () -> IFuture.CURRENT.get(), Jobs.newInput()).awaitDoneAndGet();

    assertNull(actualFuture.getJobInput().getRunContext());

    // schedule with delay
    actualFuture = Jobs.schedule((Callable<IFuture<?>>) () -> IFuture.CURRENT.get(), Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.MILLISECONDS)))
        .awaitDoneAndGet();

    assertNull(actualFuture.getJobInput().getRunContext());

    // schedule at fixed rate
    final Holder<IFuture<?>> actualFutureHolder = new Holder<>();
    Jobs.schedule(() -> {
      actualFutureHolder.setValue(IFuture.CURRENT.get());
      IFuture.CURRENT.get().cancel(false); // cancel periodic action
    }, Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(1)
                .repeatForever())))
        .awaitDone();

    assertNull(actualFuture.getJobInput().getRunContext());

    // schedule with fixed delay
    actualFutureHolder.setValue(null);
    Jobs.schedule(() -> {
      actualFutureHolder.setValue(IFuture.CURRENT.get());
      IFuture.CURRENT.get().cancel(false); // cancel periodic action
    }, Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(FixedDelayScheduleBuilder.repeatForever(1, TimeUnit.MILLISECONDS))))
        .awaitDone();

    assertNull(actualFuture.getJobInput().getRunContext());
  }

  @Test
  public void testNewInput() {
    RunContext runContext = RunContexts.empty();
    assertSame(runContext, Jobs.newInput().withRunContext(runContext).getRunContext());
    assertEquals("scout-thread", Jobs.newInput().getThreadName());
    assertNull(Jobs.newInput().getRunContext());
  }
}
