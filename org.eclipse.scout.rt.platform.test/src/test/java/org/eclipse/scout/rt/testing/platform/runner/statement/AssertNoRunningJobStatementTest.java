/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.runner.statement;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@Ignore // only used for development
@RunWith(PlatformTestRunner.class)
public class AssertNoRunningJobStatementTest {

  @Test
  public void testJobCompletesWithinTest() {
    assertTrue(Jobs.schedule(() -> true,
        Jobs.newInput()
            .withRunContext(RunContexts.empty()))
        .awaitDoneAndGet(500, TimeUnit.MILLISECONDS));
    // not expecting 'job did not complete' warning in log
  }

  @Test
  public void testJobStartedWithinTestTakesLongerThan500Millis() {
    Jobs.schedule(() -> SleepUtil.sleepSafe(750, TimeUnit.MILLISECONDS),
        Jobs.newInput()
            .withRunContext(RunContexts.empty()));
    // expecting 'job did not complete' warning in log
  }

  @Test
  public void testDelayedCancelledJob() {
    IFuture<Boolean> f = Jobs.schedule(() -> true,
        Jobs.newInput()
            .withRunContext(RunContexts.empty())
            .withExecutionTrigger(Jobs.newExecutionTrigger().withStartIn(750, TimeUnit.MILLISECONDS)));
    SleepUtil.sleepSafe(50, TimeUnit.MILLISECONDS);
    f.cancel(true);
    // not expecting 'job did not complete' warning in log
  }

  @Test
  public void testDelayedJob() {
    Jobs.schedule(() -> true,
        Jobs.newInput()
            .withRunContext(RunContexts.empty())
            .withExecutionTrigger(Jobs.newExecutionTrigger().withStartIn(750, TimeUnit.MILLISECONDS)));
    // expecting 'job did not complete' warning in log
  }

  @Test
  public void testUnfinishedCancelledJob() {
    IFuture<Void> f = Jobs.schedule(() -> SleepUtil.sleepSafe(70, TimeUnit.SECONDS),
        Jobs.newInput()
            .withRunContext(RunContexts.empty()));
    SleepUtil.sleepSafe(50, TimeUnit.MILLISECONDS);
    f.cancel(false); // do not interrupt, otherwise sleepSafe is pointless
    // not expecting 'job did not complete' warning in log
  }
}
