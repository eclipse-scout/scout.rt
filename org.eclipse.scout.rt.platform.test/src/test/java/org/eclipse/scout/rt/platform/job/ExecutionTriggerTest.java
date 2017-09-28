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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ExecutionTriggerTest {

  @Test(expected = AssertionException.class)
  public void testTemporalOverflow() {
    Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(Long.MAX_VALUE, TimeUnit.SECONDS)));
  }

  @Test(expected = AssertionException.class)
  public void testMinusStartDuration() {
    Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(-1, TimeUnit.SECONDS)));
  }

  @Test
  public void testZeroStartDuration() {
    ExecutionTrigger trigger = Jobs.newExecutionTrigger()
        .withStartIn(0, TimeUnit.SECONDS);
    Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionTrigger(trigger));
    assertEquals(trigger.getNow(), trigger.getStartTime());
  }

  @Test(expected = AssertionException.class)
  public void testMinusEndDuration() {
    Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(-1, TimeUnit.SECONDS)));
  }

  @Test
  public void testZeroEndDuration() {
    ExecutionTrigger trigger = Jobs.newExecutionTrigger()
        .withEndIn(0, TimeUnit.SECONDS);
    Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionTrigger(trigger));
    assertEquals(trigger.getNow(), trigger.getEndTime());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEndBeforeStart() {
    Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(2, TimeUnit.SECONDS)
            .withEndIn(1, TimeUnit.SECONDS)));
  }

  @Test(expected = AssertionException.class)
  public void testStartOverride() {
    Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.SECONDS)
            .withStartAt(new Date())));

    Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartAt(new Date())
            .withStartIn(1, TimeUnit.SECONDS)));
  }

  @Test(expected = AssertionException.class)
  public void testEndOverride() {
    Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withEndIn(1, TimeUnit.SECONDS)
            .withEndAt(new Date())));

    Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withEndAt(new Date())
            .withEndIn(1, TimeUnit.SECONDS)));
  }
}
