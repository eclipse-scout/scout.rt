/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption.IRestorer;
import org.junit.Test;

public class ThreadInterruptionTest {

  @Test
  public void testThreadNotInterrupted() {
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertFalse(Thread.currentThread().isInterrupted());
        IRestorer interruption = ThreadInterruption.clear();
        assertFalse(Thread.currentThread().isInterrupted());
        interruption.restore();
        assertFalse(Thread.currentThread().isInterrupted());
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, false))
        .awaitDoneAndGet();
  }

  @Test
  public void testThreadInterrupted() {
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        Thread.currentThread().interrupt();

        assertTrue(Thread.currentThread().isInterrupted());
        IRestorer interruption = ThreadInterruption.clear();
        assertFalse(Thread.currentThread().isInterrupted());
        interruption.restore();
        assertTrue(Thread.currentThread().isInterrupted());
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, false))
        .awaitDoneAndGet();
  }

  @Test
  public void testInterruptionAfterClear() {
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertFalse(Thread.currentThread().isInterrupted());

        IRestorer interruption = ThreadInterruption.clear();
        assertFalse(Thread.currentThread().isInterrupted());

        Thread.currentThread().interrupt();

        interruption.restore();

        assertTrue(Thread.currentThread().isInterrupted());
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, false))
        .awaitDoneAndGet();
  }
}
