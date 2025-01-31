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

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class FutureFilterTest {

  @Test
  public void test() {
    IFuture<Void> future1 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput());
    IFuture<Void> future2 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput());
    IFuture<Void> future3 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput());

    FutureFilter filter = new FutureFilter(future1, future2);
    assertTrue(filter.test(future1));
    assertTrue(filter.test(future2));
    assertFalse(filter.test(future3));
  }
}
