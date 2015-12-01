package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.platform.serialization.IObjectSerializer;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.eclipse.scout.rt.platform.util.TriState;
import org.junit.Test;

/**
 * Tests for {@link TriState}
 */
public class TriStateTest {

  /**
   * see {@link TriState#parse(Object)}
   */
  @Test
  public void testSingleton() {
    assertSame(TriState.TRUE, TriState.parse(true));
    assertSame(TriState.TRUE, TriState.parse(1));
    assertSame(TriState.TRUE, TriState.parse("true"));
    assertSame(TriState.TRUE, TriState.parse("1"));
    assertSame(TriState.TRUE, TriState.parse(1L));
    assertSame(TriState.TRUE, TriState.parse(TriState.TRUE));

    assertSame(TriState.FALSE, TriState.parse(false));
    assertSame(TriState.FALSE, TriState.parse(0));
    assertSame(TriState.FALSE, TriState.parse("false"));
    assertSame(TriState.FALSE, TriState.parse("0"));

    assertSame(TriState.UNDEFINED, TriState.parse((Boolean) null));
    assertSame(TriState.UNDEFINED, TriState.parse((Integer) null));
    assertSame(TriState.UNDEFINED, TriState.parse(""));
    assertSame(TriState.UNDEFINED, TriState.parse((String) null));

    assertSame(TriState.UNDEFINED, TriState.parse(-3));
    assertSame(TriState.UNDEFINED, TriState.parse("hello"));
  }

  @Test
  public void testSerializedSingleton() throws Exception {
    assertSame(TriState.TRUE, ser(TriState.TRUE));
    assertSame(TriState.FALSE, ser(TriState.FALSE));
    assertSame(TriState.UNDEFINED, ser(TriState.UNDEFINED));

    assertSame(TriState.TRUE, ser(TriState.parse(true)));
    assertSame(TriState.TRUE, ser(TriState.parse(1)));
    assertSame(TriState.TRUE, ser(TriState.parse("true")));
    assertSame(TriState.TRUE, ser(TriState.parse("1")));
    assertSame(TriState.TRUE, ser(TriState.parse(1L)));

    assertSame(TriState.FALSE, ser(TriState.parse(false)));
    assertSame(TriState.FALSE, ser(TriState.parse(0)));
    assertSame(TriState.FALSE, ser(TriState.parse("false")));
    assertSame(TriState.FALSE, ser(TriState.parse("0")));

    assertSame(TriState.UNDEFINED, ser(TriState.parse((Boolean) null)));
    assertSame(TriState.UNDEFINED, ser(TriState.parse((Integer) null)));
    assertSame(TriState.UNDEFINED, ser(TriState.parse("")));
    assertSame(TriState.UNDEFINED, ser(TriState.parse((String) null)));

    assertSame(TriState.UNDEFINED, ser(TriState.parse(-3)));
    assertSame(TriState.UNDEFINED, ser(TriState.parse("hello")));
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
    assertTrue(TriState.TRUE.isTrue());
    assertFalse(TriState.FALSE.isTrue());
    assertFalse(TriState.UNDEFINED.isTrue());

    assertFalse(TriState.TRUE.isFalse());
    assertTrue(TriState.FALSE.isFalse());
    assertFalse(TriState.UNDEFINED.isFalse());

    assertFalse(TriState.TRUE.isUndefined());
    assertFalse(TriState.FALSE.isUndefined());
    assertTrue(TriState.UNDEFINED.isUndefined());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseAnyObject() throws Exception {
    TriState.parse(new Object());
  }

  @Test
  public void testOrder() throws Exception {
    assertTrue(TriState.TRUE.compareTo(TriState.FALSE) > 0);
    assertTrue(TriState.UNDEFINED.compareTo(TriState.FALSE) > 0);
    assertTrue(TriState.UNDEFINED.compareTo(TriState.TRUE) > 0);
    assertEquals(0, TriState.UNDEFINED.compareTo(TriState.UNDEFINED));
  }

}
