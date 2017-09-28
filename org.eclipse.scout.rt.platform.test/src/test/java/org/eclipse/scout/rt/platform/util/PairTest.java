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
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * Test for {@link ImmutablePair} and {@link MutablePair}.
 */
public class PairTest {

  @Test
  public void testImmutable() throws Exception {
    Pair<String, Integer> pair = new ImmutablePair<>("Foo", 123);
    assertPair(pair);
  }

  @Test
  public void testMutable() throws Exception {
    Pair<String, Integer> pair = new MutablePair<>("Foo", 123);
    assertPair(pair);
  }

  @Test
  public void testMutatePair() throws Exception {
    MutablePair<String, Integer> pair = new MutablePair<>("Foo", 123);
    assertPair(pair);

    pair.setLeft("bar");
    assertEquals(new MutablePair<>("bar", 123), pair);

    pair.setRight(456);
    assertEquals(new MutablePair<>("bar", 456), pair);
  }

  protected void assertPair(Pair<String, Integer> pair) {
    assertEquals("Foo", pair.getLeft());
    assertEquals(123, pair.getRight().intValue());
  }

  @Test
  public void testEqualsImmutable() throws Exception {
    Pair<String, Integer> pairA = new ImmutablePair<>("Foo", 123);
    Pair<String, Integer> pairB = new ImmutablePair<>("Foo", 123);
    Pair<String, String> pairC = new ImmutablePair<>("Foo", "Bar");
    assertEqualsPair(pairA, pairB, pairC);
  }

  @Test
  public void testEqualsMutable() throws Exception {
    Pair<String, Integer> pairA = new MutablePair<>("Foo", 123);
    Pair<String, Integer> pairB = new MutablePair<>("Foo", 123);
    Pair<String, String> pairC = new MutablePair<>("Foo", "Bar");
    assertEqualsPair(pairA, pairB, pairC);
  }

  protected void assertEqualsPair(Pair<String, Integer> pairA, Pair<String, Integer> pairB, Pair<String, String> pairC) {
    assertEquals(pairA, pairB);
    assertEquals(pairB, pairA);
    assertFalse(pairA.equals(pairC));
    assertFalse(pairC.equals(pairA));
  }
}
