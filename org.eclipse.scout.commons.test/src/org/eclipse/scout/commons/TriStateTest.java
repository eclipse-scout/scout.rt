package org.eclipse.scout.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.commons.serialization.IObjectSerializer;
import org.eclipse.scout.commons.serialization.SerializationUtility;
import org.junit.Test;

/**
 * Tests for {@link TriState}
 */
public class TriStateTest {

  /**
   * see {@link TriState#parseTriState(Object)}
   */
  @Test
  public void testSingleton() {
    assertSame(TriState.TRUE, TriState.parseTriState(true));
    assertSame(TriState.TRUE, TriState.parseTriState(1));
    assertSame(TriState.TRUE, TriState.parseTriState("true"));
    assertSame(TriState.TRUE, TriState.parseTriState("1"));
    assertSame(TriState.TRUE, TriState.parseTriState(1L));
    assertSame(TriState.TRUE, TriState.parseTriState(TriState.TRUE));

    assertSame(TriState.FALSE, TriState.parseTriState(false));
    assertSame(TriState.FALSE, TriState.parseTriState(0));
    assertSame(TriState.FALSE, TriState.parseTriState("false"));
    assertSame(TriState.FALSE, TriState.parseTriState("0"));

    assertSame(TriState.UNDEFINED, TriState.parseTriState((Boolean) null));
    assertSame(TriState.UNDEFINED, TriState.parseTriState((Integer) null));
    assertSame(TriState.UNDEFINED, TriState.parseTriState(""));
    assertSame(TriState.UNDEFINED, TriState.parseTriState((String) null));

    assertSame(TriState.UNDEFINED, TriState.parseTriState(-3));
    assertSame(TriState.UNDEFINED, TriState.parseTriState("hello"));
  }

  @Test
  public void testSerializedSingleton() throws Exception {
    assertSame(TriState.TRUE, ser(TriState.TRUE));
    assertSame(TriState.FALSE, ser(TriState.FALSE));
    assertSame(TriState.UNDEFINED, ser(TriState.UNDEFINED));

    assertSame(TriState.TRUE, ser(TriState.parseTriState(true)));
    assertSame(TriState.TRUE, ser(TriState.parseTriState(1)));
    assertSame(TriState.TRUE, ser(TriState.parseTriState("true")));
    assertSame(TriState.TRUE, ser(TriState.parseTriState("1")));
    assertSame(TriState.TRUE, ser(TriState.parseTriState(1L)));

    assertSame(TriState.FALSE, ser(TriState.parseTriState(false)));
    assertSame(TriState.FALSE, ser(TriState.parseTriState(0)));
    assertSame(TriState.FALSE, ser(TriState.parseTriState("false")));
    assertSame(TriState.FALSE, ser(TriState.parseTriState("0")));

    assertSame(TriState.UNDEFINED, ser(TriState.parseTriState((Boolean) null)));
    assertSame(TriState.UNDEFINED, ser(TriState.parseTriState((Integer) null)));
    assertSame(TriState.UNDEFINED, ser(TriState.parseTriState("")));
    assertSame(TriState.UNDEFINED, ser(TriState.parseTriState((String) null)));

    assertSame(TriState.UNDEFINED, ser(TriState.parseTriState(-3)));
    assertSame(TriState.UNDEFINED, ser(TriState.parseTriState("hello")));
  }

  private TriState ser(TriState t) throws Exception {
    IObjectSerializer ser = SerializationUtility.createObjectSerializer();
    return ser.deserialize(ser.serialize(t), TriState.class);
  }

  @Test
  public void testBooleanConversion() {
    assertTrue(TriState.TRUE.getBooleanValue());
    assertFalse(TriState.FALSE.getBooleanValue());
    assertNull(TriState.UNDEFINED.getBooleanValue());
  }

  @Test
  public void testIntegerConversion() {
    assertEquals(Integer.valueOf(1), TriState.TRUE.getIntegerValue());
    assertEquals(Integer.valueOf(0), TriState.FALSE.getIntegerValue());
    assertNull(TriState.UNDEFINED.getIntegerValue());
  }

  @Test
  public void testProperties() {
    assertFalse(TriState.TRUE.isUndefined());
    assertFalse(TriState.FALSE.isUndefined());
    assertTrue(TriState.UNDEFINED.isUndefined());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testparseTriStateAnyObject() throws Exception {
    TriState.parseTriState(new Object());
  }

  @Test
  public void testOrder() throws Exception {
    assertTrue(TriState.TRUE.compareTo(TriState.FALSE) > 0);
    assertTrue(TriState.UNDEFINED.compareTo(TriState.FALSE) > 0);
    assertTrue(TriState.UNDEFINED.compareTo(TriState.TRUE) > 0);
    assertEquals(0, TriState.UNDEFINED.compareTo(TriState.UNDEFINED));
  }

}
