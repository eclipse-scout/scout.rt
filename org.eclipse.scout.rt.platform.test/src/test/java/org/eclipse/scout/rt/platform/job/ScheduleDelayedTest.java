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
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ScheduleDelayedTest {

  @Test
  public void testScheduleDelayed() {
    final List<Long> protocol = Collections.synchronizedList(new ArrayList<Long>());

    long delayNanos = TimeUnit.SECONDS.toNanos(1);
    long tStartNano = System.nanoTime();
    long assertToleranceNano = TimeUnit.MILLISECONDS.toNanos(200);

    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        protocol.add(System.nanoTime());
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.empty())
        .withSchedulingDelay(delayNanos, TimeUnit.NANOSECONDS));

    // verify
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(future)
        .toFilter(), 30, TimeUnit.SECONDS);
    assertEquals(1, protocol.size());
    Long actualExecutionTime = protocol.get(0);
    long expectedExecutionTime = tStartNano + delayNanos;
    long expectedExecutionTimeMin = expectedExecutionTime;
    long expectedExecutionTimeMax = expectedExecutionTime + assertToleranceNano;

    if (actualExecutionTime < expectedExecutionTimeMin || actualExecutionTime > expectedExecutionTimeMax) {
      fail(String.format("actualExecutionTime=%s, expectedExecutionTime=[%s;%s]", actualExecutionTime, expectedExecutionTimeMin, expectedExecutionTimeMax));
    }
  }
}
