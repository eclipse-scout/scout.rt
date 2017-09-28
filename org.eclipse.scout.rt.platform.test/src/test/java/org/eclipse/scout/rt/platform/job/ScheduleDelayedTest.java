/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
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
    IFuture<Void> future = Jobs.getJobManager().schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualExecutionTime.set(System.currentTimeMillis());
      }
    }, Jobs.newInput()
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
