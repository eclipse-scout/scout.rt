/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.concurrency;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link LoopDetector}.
 * Not required to run in a special thread
 */
public class SwingLoopDetectionTest {

  @Test
  public void testNoTrigger() throws Exception {
    final AtomicLong ts = new AtomicLong(0);
    LoopDetector det = new LoopDetector(1000L, 100, 10) {
      @Override
      protected long createTimestamp() {
        return ts.get();
      }
    };
    //add 90/second
    for (int i = 0; i < 200; i++) {
      det.addSample();
      ts.set(ts.get() + 1000 / 90);
      Assert.assertFalse(det.isArmed());
    }
    //remove 70/second
    for (int i = 0; i < 200; i++) {
      det.addSample();
      ts.set(ts.get() + 1000 / 70);
      Assert.assertFalse(det.isArmed());
    }
    Assert.assertFalse(det.isArmed());
  }

  @Test
  public void testTrigger() throws Exception {
    final AtomicLong ts = new AtomicLong(0);
    LoopDetector det = new LoopDetector(1000L, 100, 10) {
      @Override
      protected long createTimestamp() {
        return ts.get();
      }
    };
    //add 110/second
    for (int i = 0; i < 200; i++) {
      det.addSample();
      ts.set(ts.get() + 1000 / 110);
      if (i < 100) {
        Assert.assertFalse(det.isArmed());
      }
      else {
        Assert.assertTrue(det.isArmed());
      }
    }
    //add additional at 2/second
    for (int i = 0; i < 200; i++) {
      det.addSample();
      ts.set(ts.get() + 1000 / 2);
      if (i < 2) {
        Assert.assertTrue(det.isArmed());
      }
      else {
        Assert.assertFalse(det.isArmed());
      }
    }
    Assert.assertFalse(det.isArmed());
  }

}
