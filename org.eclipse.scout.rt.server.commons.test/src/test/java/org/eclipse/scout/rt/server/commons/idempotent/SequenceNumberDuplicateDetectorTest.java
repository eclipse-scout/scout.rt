/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.idempotent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.TuningUtility;
import org.eclipse.scout.rt.platform.util.date.IDateProvider;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithNewPlatform;
import org.eclipse.scout.rt.testing.platform.util.date.FixedDateProvider;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
@RunWithNewPlatform
public class SequenceNumberDuplicateDetectorTest {

  @BeforeClass
  public static void setupClass() {
    TestingUtility.registerBeans(new BeanMetaData(IDateProvider.class, new FixedDateProvider()));
  }

  private static void setCurrentTime(long t) {
    ((FixedDateProvider) BEANS.get(IDateProvider.class)).setTimeMillis(t);
  }

  /**
   * Test that the cache can reach unlimited size if there is a rush of requests
   */
  @Test
  public void testUnlimitedSizeOnRush() {
    setCurrentTime(0);
    SequenceNumberDuplicateDetector det = new SequenceNumberDuplicateDetector(2, 10, TimeUnit.MILLISECONDS);
    assertTrue(det.accept(1));
    assertTrue(det.accept(2));
    assertTrue(det.accept(3));
    assertTrue(det.accept(4));
    assertTrue(det.accept(5));
    assertTrue(det.accept(6));
    assertEquals(CollectionUtility.hashSet(1L, 2L, 3L, 4L, 5L, 6L), det.getCache().keySet());

    //let time flow for 11ms after maxAge of 10, expect size to shrink to 2 (but not 1!)
    setCurrentTime(11);
    assertTrue(det.accept(7));
    assertEquals(CollectionUtility.hashSet(6L, 7L), det.getCache().keySet());
  }

  @Test
  public void testHouseKeeping() {
    setCurrentTime(0);
    SequenceNumberDuplicateDetector det = new SequenceNumberDuplicateDetector(2, 10, TimeUnit.MILLISECONDS);
    setCurrentTime(11);
    assertTrue(det.accept(1));
    setCurrentTime(22);
    assertTrue(det.accept(2));
    setCurrentTime(33);
    assertTrue(det.accept(3));
    setCurrentTime(44);
    assertTrue(det.accept(4));
    assertEquals(CollectionUtility.hashSet(1L, 2L, 3L, 4L), det.getCache().keySet());

    setCurrentTime(55);
    assertTrue(det.accept(5));
    assertEquals(CollectionUtility.hashSet(4L, 5L), det.getCache().keySet());

    assertTrue(det.accept(60));
    assertTrue(det.accept(61));
    assertTrue(det.accept(62));
    setCurrentTime(60);
    assertTrue(det.accept(59));//this is a late arrival of a low number, this one stays in the cache for at least 10ms (maxAge)
    assertEquals(CollectionUtility.hashSet(5L, 59L, 60L, 61L, 62L), det.getCache().keySet());

    setCurrentTime(60 + 10);//exactly at maxAge of 59L but after maxAge of 60L, 61L, 62L
    assertTrue(det.accept(63));
    assertEquals(CollectionUtility.hashSet(59L, 60L, 61L, 62L, 63L), det.getCache().keySet());

    setCurrentTime(60 + 11);//after at maxAge of 59L, now 59L, 60L, 61L, 62L are timeout, but we keep 62 to have cacheSize 2
    assertTrue(det.accept(64));
    assertEquals(CollectionUtility.hashSet(63L, 64L), det.getCache().keySet());
  }

  @Test
  public void testAcceptReject() {
    setCurrentTime(0);
    SequenceNumberDuplicateDetector det = new SequenceNumberDuplicateDetector(2, 10, TimeUnit.MILLISECONDS);

    setCurrentTime(11);
    assertTrue(det.accept(1));
    assertTrue(det.accept(0));//cache has not yet minimum size 2
    assertFalse(det.accept(1));

    setCurrentTime(22);
    assertTrue(det.accept(2));
    assertFalse(det.accept(0));//cache has minimum size 2
    assertFalse(det.accept(1));
    assertFalse(det.accept(2));

    setCurrentTime(33);
    assertTrue(det.accept(3));
    assertFalse(det.accept(0));
    assertFalse(det.accept(1));
    assertFalse(det.accept(2));
    assertFalse(det.accept(3));
  }

  /**
   * This method is here as a matter of quality. It can be used to get an idea of how housekeeping performs.
   * <p>
   * Output: #TUNING: housekeeping of 1000000 items took 56.175160ms
   */
  @Ignore
  @Test
  public void estimatePerformaceOfHousekeeping() {
    setCurrentTime(0);
    int n = 1000000;
    SequenceNumberDuplicateDetector det = new SequenceNumberDuplicateDetector(2, 10, TimeUnit.MILLISECONDS);
    for (int i = 1; i <= n; i++) {
      assertTrue(det.accept(i));
    }

    setCurrentTime(11);
    TuningUtility.startTimer();
    assertTrue(det.accept(n + 1));
    TuningUtility.stopTimer("housekeeping of " + n + " items");
  }
}
