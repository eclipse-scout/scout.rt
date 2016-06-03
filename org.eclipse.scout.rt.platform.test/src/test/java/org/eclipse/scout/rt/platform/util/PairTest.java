package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class PairTest {

  @Test
  public void test() throws Exception {
    Pair<String, Integer> pair = new Pair<>("Foo", 123);
    assertEquals("Foo", pair.getLeft());
    assertEquals(123, pair.getRight().intValue());
  }

  @Test
  public void testEquals() throws Exception {
    Pair<String, Integer> pairA = new Pair<>("Foo", 123);
    Pair<String, Integer> pairB = new Pair<>("Foo", 123);
    Pair<String, String> pairC = new Pair<>("Foo", "Bar");
    assertEquals(pairA, pairB);
    assertEquals(pairB, pairA);
    assertFalse(pairA.equals(pairC));
    assertFalse(pairC.equals(pairA));
  }

}
