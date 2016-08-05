package org.eclipse.scout.rt.shared.data.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @since 6.1
 */
public class MemoryOptimizedObjectTest {

  @Test
  public void testSetAndGetValue() {
    MemoryOptimizedObject moo = new MemoryOptimizedObject();
    assertTrue(moo.setValueInternal(0, "bitValue0"));
    assertTrue(moo.setValueInternal(2, "bitValue2"));
    assertTrue(moo.setValueInternal(1, "bitValue1"));

    assertEquals("bitValue0", moo.getValueInternal(0));
    assertEquals("bitValue1", moo.getValueInternal(1));
    assertEquals("bitValue2", moo.getValueInternal(2));
    assertNull(moo.getValueInternal(3));
  }

  @Test
  public void testSetAndGetNullValue() {
    MemoryOptimizedObject moo = new MemoryOptimizedObject();
    assertFalse(moo.setValueInternal(3, null));
    assertNull(moo.getValueInternal(3));
  }

  @Test
  public void testSetGetAndOverrideValue() {
    MemoryOptimizedObject moo = new MemoryOptimizedObject();
    assertTrue(moo.setValueInternal(0, "bitValue0"));
    assertTrue(moo.setValueInternal(2, "bitValue2"));
    assertTrue(moo.setValueInternal(1, "bitValue1"));

    assertTrue(moo.setValueInternal(1, null));

    assertEquals("bitValue0", moo.getValueInternal(0));
    assertNull(moo.getValueInternal(1));
    assertEquals("bitValue2", moo.getValueInternal(2));
    assertNull(moo.getValueInternal(3));
  }

  @Test
  public void testOverrideValueWithSameContent() {
    MemoryOptimizedObject moo = new MemoryOptimizedObject();
    assertTrue(moo.setValueInternal(0, "bitValue0"));
    assertFalse(moo.setValueInternal(0, "bitValue0"));
    assertEquals("bitValue0", moo.getValueInternal(0));
  }
}
