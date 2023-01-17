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

import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class MutexFutureFilterTest {

  @Test
  public void test() {
    IExecutionSemaphore mutex1 = Jobs.newExecutionSemaphore(1);
    IExecutionSemaphore mutex2 = Jobs.newExecutionSemaphore(1);

    IFuture<Void> future1 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withExecutionSemaphore(mutex1));
    IFuture<Void> future2 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withExecutionSemaphore(mutex1));
    IFuture<Void> future3 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withExecutionSemaphore(mutex2));

    Predicate<IFuture<?>> filter = new ExecutionSemaphoreFutureFilter(mutex1);
    assertTrue(filter.test(future1));
    assertTrue(filter.test(future2));
    assertFalse(filter.test(future3));
  }
}
