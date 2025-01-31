/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.util.concurrent;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.platform.util.concurrent.OptimisticLock;
import org.junit.Test;

/**
 * Tests for {@link OptimisticLock}
 */
public class OptimisticLockTest {

  @Test
  public void testInitiallyNotAcqiured() {
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
