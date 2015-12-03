/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.platform.util.concurrent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.platform.util.concurrent.OptimisticLock;
import org.junit.Test;

/**
 * Tests for {@link OptimisticLock}
 */
public class OptimisticLockTest {

  @Test
  public void testInitiallyNotAcqiured() throws Exception {
    OptimisticLock l = new OptimisticLock();
    assertFalse(l.isAcquired());
    assertTrue(l.isReleased());
  }

  @Test
  public void testAcquire() {
    OptimisticLock l = new OptimisticLock();
    boolean acquired = l.acquire();
    assertTrue(acquired);
    assertTrue(l.isAcquired());
    assertFalse(l.isReleased());
  }

  @Test
  public void testAcquireAndRelease() {
    OptimisticLock l = new OptimisticLock();
    l.acquire();
    l.release();
    assertFalse(l.isAcquired());
    assertTrue(l.isReleased());
  }

  @Test
  public void testMultipleAcquires() {
    OptimisticLock l = new OptimisticLock();
    l.acquire();
    l.acquire();
    l.release();
    assertTrue(l.isAcquired());
    assertFalse(l.isReleased());
  }

  @Test
  public void testMultipleAcquiresReleases() {
    OptimisticLock l = new OptimisticLock();
    l.acquire();
    l.acquire();
    l.release();
    l.release();
    assertFalse(l.isAcquired());
    assertTrue(l.isReleased());
  }

}
