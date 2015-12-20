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
package org.eclipse.scout.rt.platform.job.filter.future;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.ISchedulingSemaphore;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class MutexFutureFilterTest {

  @Test
  public void test() {
    ISchedulingSemaphore mutex1 = Jobs.newSchedulingSemaphore(1);
    ISchedulingSemaphore mutex2 = Jobs.newSchedulingSemaphore(1);

    IFuture<Void> future1 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withSchedulingSemaphore(mutex1));
    IFuture<Void> future2 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withSchedulingSemaphore(mutex1));
    IFuture<Void> future3 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withSchedulingSemaphore(mutex2));

    IFilter<IFuture<?>> filter = new SchedulingSemaphoreFutureFilter(mutex1);
    assertTrue(filter.accept(future1));
    assertTrue(filter.accept(future2));
    assertFalse(filter.accept(future3));
  }
}
