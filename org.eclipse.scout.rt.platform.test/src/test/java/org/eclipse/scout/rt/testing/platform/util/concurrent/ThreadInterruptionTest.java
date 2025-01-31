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

import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption.IRestorer;
import org.junit.Test;

public class ThreadInterruptionTest {

  @Test
  public void testThreadNotInterrupted() {
    Jobs.schedule(() -> {
          assertFalse(Thread.currentThread().isInterrupted());
          IRestorer interruption = ThreadInterruption.clear();
          assertFalse(Thread.currentThread().isInterrupted());
          interruption.restore();
          assertFalse(Thread.currentThread().isInterrupted());
        }, Jobs.newInput()
            .withExceptionHandling(null, false))
        .awaitDoneAndGet();
  }

  @Test
  public void testThreadInterrupted() {
    Jobs.schedule(() -> {
          Thread.currentThread().interrupt();

          assertTrue(Thread.currentThread().isInterrupted());
          IRestorer interruption = ThreadInterruption.clear();
          assertFalse(Thread.currentThread().isInterrupted());
          interruption.restore();
          assertTrue(Thread.currentThread().isInterrupted());
        }, Jobs.newInput()
            .withExceptionHandling(null, false))
        .awaitDoneAndGet();
  }

  @Test
  public void testInterruptionAfterClear() {
    Jobs.schedule(() -> {
          assertFalse(Thread.currentThread().isInterrupted());

          IRestorer interruption = ThreadInterruption.clear();
          assertFalse(Thread.currentThread().isInterrupted());

          Thread.currentThread().interrupt();

          interruption.restore();

          assertTrue(Thread.currentThread().isInterrupted());
        }, Jobs.newInput()
            .withExceptionHandling(null, false))
        .awaitDoneAndGet();
  }
}
