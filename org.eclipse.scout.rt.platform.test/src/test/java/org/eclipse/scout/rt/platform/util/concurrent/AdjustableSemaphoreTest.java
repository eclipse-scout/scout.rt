/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util.concurrent;

import static org.junit.Assert.*;

import org.junit.Test;

public class AdjustableSemaphoreTest {

  @Test
  public void testCreateInstance_invalidInputs() {
    assertThrows(IllegalArgumentException.class, () -> new AdjustableSemaphore(0));
    assertThrows(IllegalArgumentException.class, () -> new AdjustableSemaphore(-1));
    assertThrows(IllegalArgumentException.class, () -> new AdjustableSemaphore(-10));

    assertThrows(IllegalArgumentException.class, () -> new AdjustableSemaphore(0, true));
    assertThrows(IllegalArgumentException.class, () -> new AdjustableSemaphore(-1, true));
    assertThrows(IllegalArgumentException.class, () -> new AdjustableSemaphore(-10, true));
  }

  @Test
  public void testGetMaximumPermits() {
    assertEquals(1, new AdjustableSemaphore(1).getMaximumPermits());
    assertEquals(10, new AdjustableSemaphore(10).getMaximumPermits());
    assertEquals(100, new AdjustableSemaphore(100).getMaximumPermits());
  }

  @Test
  public void testSetMaximumPermits_invalidInputs() {
    final AdjustableSemaphore adjustableSemaphore = new AdjustableSemaphore(5);

    assertThrows(IllegalArgumentException.class, () -> adjustableSemaphore.setMaximumPermits(0));
    assertThrows(IllegalArgumentException.class, () -> adjustableSemaphore.setMaximumPermits(-1));
    assertThrows(IllegalArgumentException.class, () -> adjustableSemaphore.setMaximumPermits(-10));
  }

  @Test
  public void testSetMaximumPermits() {
    final AdjustableSemaphore adjustableSemaphore = new AdjustableSemaphore(5);
    assertEquals(5, adjustableSemaphore.getMaximumPermits());

    adjustableSemaphore.setMaximumPermits(10);
    assertEquals(10, adjustableSemaphore.getMaximumPermits());

    adjustableSemaphore.setMaximumPermits(100);
    assertEquals(100, adjustableSemaphore.getMaximumPermits());

    adjustableSemaphore.setMaximumPermits(50);
    assertEquals(50, adjustableSemaphore.getMaximumPermits());
  }

  @Test
  public void testSetMaximumPermits_decreasePermits() {
    final AdjustableSemaphore adjustableSemaphore = new AdjustableSemaphore(5);
    assertEquals(5, adjustableSemaphore.getMaximumPermits());
    assertTrue(adjustableSemaphore.tryAcquire(5));
    // now 5 of 5 permits are in use

    adjustableSemaphore.setMaximumPermits(3);
    assertEquals(3, adjustableSemaphore.getMaximumPermits());
    // now 5 of 3 permits are in use

    assertFalse(adjustableSemaphore.tryAcquire());
    adjustableSemaphore.release();
    // now 4 of 3 permits are in use

    assertFalse(adjustableSemaphore.tryAcquire());
    adjustableSemaphore.release();
    // now 3 of 3 permits are in use

    assertFalse(adjustableSemaphore.tryAcquire());
    adjustableSemaphore.release();
    // now 2 of 3 permits are in use

    assertTrue(adjustableSemaphore.tryAcquire());
    // now 3 of 3 permits are in use
  }

  @Test
  public void testSetMaximumPermits_increasePermits() {
    final AdjustableSemaphore adjustableSemaphore = new AdjustableSemaphore(3);
    assertEquals(3, adjustableSemaphore.getMaximumPermits());
    assertTrue(adjustableSemaphore.tryAcquire(3));
    // now 3 of 3 permits are in use

    assertFalse(adjustableSemaphore.tryAcquire());
    adjustableSemaphore.setMaximumPermits(5);
    assertEquals(5, adjustableSemaphore.getMaximumPermits());
    // now 3 of 5 permits are in use

    assertTrue(adjustableSemaphore.tryAcquire());
    // now 4 of 5 permits are in use

    assertTrue(adjustableSemaphore.tryAcquire());
    // now 5 of 5 permits are in use

    assertFalse(adjustableSemaphore.tryAcquire());
  }
}
