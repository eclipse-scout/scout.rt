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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobsTest {

  private IBean<IJobManager> m_jobManagerBean;

  @Before
  public void before() {
    m_jobManagerBean = JobTestUtil.registerJobManager();
  }

  @After
  public void after() {
    JobTestUtil.unregisterJobManager(m_jobManagerBean);
  }

  @Test
  public void testScheduleWithoutInput() {
    NlsLocale.set(Locale.CANADA_FRENCH);

    // Test schedule
    IFuture<?> actualFuture = Jobs.schedule(new Callable<IFuture<?>>() {

      @Override
      public IFuture<?> call() throws Exception {
        return IFuture.CURRENT.get();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()))
        .awaitDoneAndGet();

    assertEquals(Locale.CANADA_FRENCH, actualFuture.getJobInput().getRunContext().getLocale());

    // schedule with delay
    actualFuture = Jobs.schedule(new Callable<IFuture<?>>() {

      @Override
      public IFuture<?> call() throws Exception {
        return IFuture.CURRENT.get();
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withSchedulingDelay(0, TimeUnit.MILLISECONDS))
        .awaitDoneAndGet();

    assertEquals(Locale.CANADA_FRENCH, actualFuture.getJobInput().getRunContext().getLocale());

    // schedule at fixed rate
    final Holder<IFuture<?>> actualFutureHolder = new Holder<IFuture<?>>();
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualFutureHolder.setValue(IFuture.CURRENT.get());
        IFuture.CURRENT.get().cancel(false); // cancel periodic action
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withPeriodicExecutionAtFixedRate(0, TimeUnit.MILLISECONDS))
        .awaitDone();

    assertEquals(Locale.CANADA_FRENCH, actualFuture.getJobInput().getRunContext().getLocale());

    // schedule with fixed delay
    actualFutureHolder.setValue(null);
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualFutureHolder.setValue(IFuture.CURRENT.get());
        IFuture.CURRENT.get().cancel(false); // cancel periodic action
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withPeriodicExecutionWithFixedDelay(0, TimeUnit.MILLISECONDS))
        .awaitDone();

    assertEquals(Locale.CANADA_FRENCH, actualFuture.getJobInput().getRunContext().getLocale());
  }

  @Test
  public void testScheduleWithoutRunContext() {
    NlsLocale.set(Locale.CANADA_FRENCH);

    // Test schedule
    IFuture<?> actualFuture = Jobs.schedule(new Callable<IFuture<?>>() {

      @Override
      public IFuture<?> call() throws Exception {
        return IFuture.CURRENT.get();
      }
    }, Jobs.newInput()).awaitDoneAndGet();

    assertNull(actualFuture.getJobInput().getRunContext());

    // schedule with delay
    actualFuture = Jobs.schedule(new Callable<IFuture<?>>() {

      @Override
      public IFuture<?> call() throws Exception {
        return IFuture.CURRENT.get();
      }
    }, Jobs.newInput()
        .withSchedulingDelay(0, TimeUnit.MILLISECONDS))
        .awaitDoneAndGet();

    assertNull(actualFuture.getJobInput().getRunContext());

    // schedule at fixed rate
    final Holder<IFuture<?>> actualFutureHolder = new Holder<IFuture<?>>();
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualFutureHolder.setValue(IFuture.CURRENT.get());
        IFuture.CURRENT.get().cancel(false); // cancel periodic action
      }
    }, Jobs.newInput()
        .withPeriodicExecutionAtFixedRate(0, TimeUnit.MILLISECONDS))
        .awaitDone();

    assertNull(actualFuture.getJobInput().getRunContext());

    // schedule with fixed delay
    actualFutureHolder.setValue(null);
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualFutureHolder.setValue(IFuture.CURRENT.get());
        IFuture.CURRENT.get().cancel(false); // cancel periodic action
      }
    }, Jobs.newInput()
        .withPeriodicExecutionWithFixedDelay(0, TimeUnit.MILLISECONDS))
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
