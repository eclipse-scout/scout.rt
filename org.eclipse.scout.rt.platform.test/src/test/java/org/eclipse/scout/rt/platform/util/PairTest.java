/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for {@link ImmutablePair} and {@link MutablePair}.
 */
public class PairTest {

  @Test
  public void testImmutable() {
    Pair<String, Integer> pair = new ImmutablePair<>("Foo", 123);
    assertPair(pair);
    assertPair(ImmutablePair.of("Foo", 123));
  }

  @Test
  public void testMutable() {
    Pair<String, Integer> pair = new MutablePair<>("Foo", 123);
    assertPair(pair);
    assertPair(MutablePair.of("Foo", 123));
  }

  @Test
  public void testMutatePair() {
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
  public void testEqualsImmutable() {
    Pair<String, Integer> pairA = new ImmutablePair<>("Foo", 123);
    Pair<String, Integer> pairB = new ImmutablePair<>("Foo", 123);
    Pair<String, String> pairC = new ImmutablePair<>("Foo", "Bar");
    assertEqualsPair(pairA, pairB, pairC);
    assertEquals(ImmutablePair.of("foo", "bar"), ImmutablePair.of("foo", "bar"));
    assertNotEquals(ImmutablePair.of("foo", "bar"), MutablePair.of("foo", "bar"));
  }

  @Test
  public void testEqualsMutable() {
    Pair<String, Integer> pairA = new MutablePair<>("Foo", 123);
    Pair<String, Integer> pairB = new MutablePair<>("Foo", 123);
    Pair<String, String> pairC = new MutablePair<>("Foo", "Bar");
    assertEqualsPair(pairA, pairB, pairC);
    assertEquals(MutablePair.of("foo", "bar"), MutablePair.of("foo", "bar"));
    assertNotEquals(MutablePair.of("foo", "bar"), ImmutablePair.of("foo", "bar"));
  }

  protected void assertEqualsPair(Pair<String, Integer> pairA, Pair<String, Integer> pairB, Pair<String, String> pairC) {
    assertEquals(pairA, pairB);
    assertEquals(pairB, pairA);
    assertFalse(pairA.equals(pairC));
    assertFalse(pairC.equals(pairA));
  }
}
