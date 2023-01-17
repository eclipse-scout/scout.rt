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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ExecutionHintFutureFilterTest {

  @Test
  public void test() {
    // job1
    IFuture<Void> future1 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionHint("ui-interaction-required"));

    // job2
    IFuture<Void> future2 = Jobs.schedule(() -> {
      IFuture.CURRENT.get().removeExecutionHint("ui-interaction-required");
    }, Jobs.newInput().withExecutionHint("ui-interaction-required"));

    // job3
    IFuture<Void> future3 = Jobs.schedule(() -> {
      IFuture.CURRENT.get().addExecutionHint("ui-interaction-required");
    }, Jobs.newInput());

    // job4
    IFuture<Void> future4 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput());
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(
            future1,
            future2,
            future3,
            future4)
        .toFilter(), 10, TimeUnit.SECONDS);

    Predicate<IFuture<?>> filter = new ExecutionHintFutureFilter("ui-interaction-required");
    assertTrue(filter.test(future1)); // hint added by job input
    assertFalse(filter.test(future2)); // hint is removed while running
    assertTrue(filter.test(future3)); // hint added while running
    assertFalse(filter.test(future4));
  }
}
