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

import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ScheduleDelayedTest {

  @Test
  public void testScheduleDelayed() {
    final AtomicReference<Long> actualExecutionTime = new AtomicReference<>();

    long tStartMillis = System.currentTimeMillis();
    long delayMillis = TimeUnit.SECONDS.toMillis(1);
    IFuture<Void> future = Jobs.getJobManager().schedule(() -> actualExecutionTime.set(System.currentTimeMillis()), Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(delayMillis, TimeUnit.MILLISECONDS)));

    // verify
    future.awaitDone(10, TimeUnit.SECONDS);

    long minExpectedExecutionTime = tStartMillis + delayMillis;
    if (actualExecutionTime.get() < minExpectedExecutionTime) {
      fail(String.format("actualExecutionTime=%s, minExpectedExecutionTime=[%s]", actualExecutionTime.get(), minExpectedExecutionTime));
    }
  }
}
