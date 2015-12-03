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
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobNameFutureFilterTest {

  @Test
  public void test() {
    IFuture<Void> future1 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withName("A"));
    IFuture<Void> future2 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withName("B"));
    IFuture<Void> future3 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput().withName("C"));
    IFuture<Void> future4 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput());

    IFilter<IFuture<?>> filter = new JobNameFutureFilter("A", "B");
    assertTrue(filter.accept(future1));
    assertTrue(filter.accept(future2));
    assertFalse(filter.accept(future3));
    assertFalse(filter.accept(future4));
  }
}
