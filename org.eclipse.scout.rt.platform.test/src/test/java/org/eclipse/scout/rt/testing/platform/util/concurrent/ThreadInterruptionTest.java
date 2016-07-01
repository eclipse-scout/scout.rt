package org.eclipse.scout.rt.testing.platform.util.concurrent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption.IRestorer;
import org.junit.Test;

public class ThreadInterruptionTest {

  @Test
  public void testThreadNotInterrupted() {
    assertFalse(Thread.currentThread().isInterrupted());
    IRestorer interruption = ThreadInterruption.clear();
    assertFalse(Thread.currentThread().isInterrupted());
    interruption.restore();
    assertFalse(Thread.currentThread().isInterrupted());
  }

  @Test
  public void testThreadInterrupted() {
    Thread.currentThread().interrupt();

    assertTrue(Thread.currentThread().isInterrupted());
    IRestorer interruption = ThreadInterruption.clear();
    assertFalse(Thread.currentThread().isInterrupted());
    interruption.restore();
    assertTrue(Thread.currentThread().isInterrupted());
  }

  @Test
  public void testInterruptionAfterClear() {
    assertFalse(Thread.currentThread().isInterrupted());

    IRestorer interruption = ThreadInterruption.clear();
    assertFalse(Thread.currentThread().isInterrupted());

    Thread.currentThread().interrupt();

    interruption.restore();

    assertTrue(Thread.currentThread().isInterrupted());
  }
}
