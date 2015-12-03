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
import static org.mockito.Mockito.when;

import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class BlockedFutureFilterTest {

  @SuppressWarnings("unchecked")
  @Test
  public void testBlocked() {
    IFuture<Void> future1 = mock(IFuture.class);
    IFilter<IFuture<?>> filter = BlockedFutureFilter.INSTANCE_BLOCKED;

    when(future1.isBlocked()).thenReturn(false);
    assertFalse(filter.accept(future1));

    when(future1.isBlocked()).thenReturn(true);
    assertTrue(filter.accept(future1));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testNotBlocked() {
    IFuture<Void> future1 = mock(IFuture.class);
    IFilter<IFuture<?>> filter = BlockedFutureFilter.INSTANCE_NOT_BLOCKED;

    when(future1.isBlocked()).thenReturn(false);
    assertTrue(filter.accept(future1));

    when(future1.isBlocked()).thenReturn(true);
    assertFalse(filter.accept(future1));
  }
}
