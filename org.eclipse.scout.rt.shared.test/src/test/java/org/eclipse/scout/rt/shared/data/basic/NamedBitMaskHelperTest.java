/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.basic;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class NamedBitMaskHelperTest {

  private static String[] BIT_NAMES = new String[NamedBitMaskHelper.NUM_BITS + 1];

  @BeforeClass
  public static void setup() {
    for (int i = 0; i < BIT_NAMES.length; i++) {
      BIT_NAMES[i] = "bitName" + Integer.toString(i);
    }
  }

  @AfterClass
  public static void cleanup() {
    BIT_NAMES = null;
  }

  @Test
  public void testUsedBitNames() {
    NamedBitMaskHelper bitMask = new NamedBitMaskHelper();
    Assert.assertEquals(0, bitMask.usedBits());
    Assert.assertTrue(bitMask.bitNames().isEmpty());

    bitMask = new NamedBitMaskHelper(BIT_NAMES[0], BIT_NAMES[3]);
    Assert.assertEquals(2, bitMask.usedBits());
    Assert.assertEquals(2, bitMask.bitNames().size());
    Assert.assertArrayEquals(new Object[]{BIT_NAMES[0], BIT_NAMES[3]}, bitMask.bitNames().toArray());
  }

  @Test
  public void testAllBitsEqual() {
    byte holder = 0;
    NamedBitMaskHelper bitMask = new NamedBitMaskHelper(BIT_NAMES[0], BIT_NAMES[1], BIT_NAMES[2], BIT_NAMES[3]);
    holder = bitMask.setBit(BIT_NAMES[0], holder);
    holder = bitMask.setBit(BIT_NAMES[3], holder);

    Assert.assertTrue(bitMask.allBitsEqual(holder, name -> BIT_NAMES[0].equals(name) || BIT_NAMES[3].equals(name)));
    Assert.assertFalse(bitMask.allBitsEqual(holder, name -> BIT_NAMES[0].equals(name) || BIT_NAMES[2].equals(name)));
    Assert.assertFalse(bitMask.allBitsEqual(holder, null));
  }

  @Test
  public void testValues() {
    byte holder = 0;
    NamedBitMaskHelper bitMask = new NamedBitMaskHelper();

    // initially all set to false
    for (int i = 0; i < NamedBitMaskHelper.NUM_BITS; i++) {
      Assert.assertFalse(bitMask.isBitSet(BIT_NAMES[i], holder));
    }

    // change some bits
    holder = bitMask.setBit(BIT_NAMES[3], holder);
    holder = bitMask.changeBit(BIT_NAMES[5], true, holder);
    Assert.assertFalse(bitMask.isBitSet(BIT_NAMES[0], holder));
    Assert.assertFalse(bitMask.isBitSet(BIT_NAMES[1], holder));
    Assert.assertFalse(bitMask.isBitSet(BIT_NAMES[2], holder));
    Assert.assertTrue(bitMask.isBitSet(BIT_NAMES[3], holder));
    Assert.assertFalse(bitMask.isBitSet(BIT_NAMES[4], holder));
    Assert.assertTrue(bitMask.isBitSet(BIT_NAMES[5], holder));
    Assert.assertFalse(bitMask.isBitSet(BIT_NAMES[6], holder));
    Assert.assertFalse(bitMask.isBitSet(BIT_NAMES[7], holder));

    Assert.assertTrue(bitMask.isBit(BIT_NAMES[3], holder, true));
    Assert.assertFalse(bitMask.isBit(BIT_NAMES[3], holder, false));

    // reset bits
    holder = bitMask.clearBit(BIT_NAMES[3], holder);
    holder = bitMask.changeBit(BIT_NAMES[5], false, holder);
    holder = bitMask.setBit(BIT_NAMES[0], holder);
    holder = bitMask.changeBit(BIT_NAMES[7], true, holder);
    Assert.assertTrue(bitMask.isBitSet(BIT_NAMES[0], holder));
    Assert.assertFalse(bitMask.isBitSet(BIT_NAMES[1], holder));
    Assert.assertFalse(bitMask.isBitSet(BIT_NAMES[2], holder));
    Assert.assertFalse(bitMask.isBitSet(BIT_NAMES[3], holder));
    Assert.assertFalse(bitMask.isBitSet(BIT_NAMES[4], holder));
    Assert.assertFalse(bitMask.isBitSet(BIT_NAMES[5], holder));
    Assert.assertFalse(bitMask.isBitSet(BIT_NAMES[6], holder));
    Assert.assertTrue(bitMask.isBitSet(BIT_NAMES[7], holder));
  }

  @Test
  public void testAllBitsSetAndUnusedBitName() {
    byte holder = NamedBitMaskHelper.ALL_BITS_SET;
    NamedBitMaskHelper bitMask = new NamedBitMaskHelper();
    Assert.assertTrue(bitMask.isBitSet("whatever", holder));
    Assert.assertEquals(1, bitMask.usedBits());
  }

  @Test
  public void testAllBitsSet() {
    byte holder = 0;
    NamedBitMaskHelper bitMask = new NamedBitMaskHelper();

    Assert.assertFalse(NamedBitMaskHelper.allBitsSet(holder));

    // all set to true
    for (int i = 0; i < NamedBitMaskHelper.NUM_BITS; i++) {
      holder = bitMask.setBit(BIT_NAMES[i], holder);
    }
    Assert.assertTrue(NamedBitMaskHelper.allBitsSet(holder));

    holder = bitMask.clearBit(BIT_NAMES[1], holder);
    Assert.assertFalse(NamedBitMaskHelper.allBitsSet(holder));
  }

  @Test
  public void testOverflow() {
    byte holder = 0;
    NamedBitMaskHelper bitMask = new NamedBitMaskHelper();

    for (int i = 0; i < NamedBitMaskHelper.NUM_BITS; i++) {
      holder = bitMask.setBit(BIT_NAMES[i], holder);
    }
    for (int i = 0; i < NamedBitMaskHelper.NUM_BITS; i++) {
      Assert.assertTrue(bitMask.isBitSet(BIT_NAMES[i], holder));
    }

    try {
      bitMask.setBit(BIT_NAMES[NamedBitMaskHelper.NUM_BITS], holder);
      Assert.fail("overflow check wrong");
    }
    catch (IllegalStateException e) {
      // ok
    }

    try {
      bitMask = new NamedBitMaskHelper(BIT_NAMES);
      Assert.fail("overflow check wrong");
    }
    catch (IllegalStateException e) {
      // ok
    }
  }

  @Test
  public void testNullBitName() {
    byte holder = 0;
    NamedBitMaskHelper bitMask = new NamedBitMaskHelper();
    try {
      holder = bitMask.setBit(null, holder);
      Assert.fail("null bitname is not allowed");
    }
    catch (AssertionException e) {
      // ok
    }
    Assert.assertEquals(0, holder); // ensure unchanged
  }
}
