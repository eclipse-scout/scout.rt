/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.serialization;

import java.io.InvalidClassException;
import java.io.Serializable;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

public class SerializationUtilityTest1 {

  private static final class FixtureClassWithSerialVersionUID1 implements Serializable {
    private static final long serialVersionUID = 0x1234567890123456L;
  }

  private static final class FixtureClassWithSerialVersionUID2 implements Serializable {
    private static final long serialVersionUID = 0x1234567890123456L;
  }

  @Test
  public void testDifferentSerialVersionUID() throws Exception {
    IObjectSerializer ser = new BasicObjectSerializer(null);
    byte[] b1 = ser.serialize(new FixtureClassWithSerialVersionUID1());

    Assert.assertArrayEquals(mockSerialData(FixtureClassWithSerialVersionUID1.class, 0x1234567890123456L), b1);

    FixtureClassWithSerialVersionUID1 deser1 = ser.deserialize(b1, FixtureClassWithSerialVersionUID1.class);
    Assert.assertSame(FixtureClassWithSerialVersionUID1.class, deser1.getClass());

    //now change the class name to FixtureClassWithSerialVersionUID2 and keep the correct serializationUID
    byte[] b2 = mockSerialData(FixtureClassWithSerialVersionUID2.class, 0x1234567890123456L);

    FixtureClassWithSerialVersionUID2 deser2 = ser.deserialize(b2, FixtureClassWithSerialVersionUID2.class);
    Assert.assertSame(FixtureClassWithSerialVersionUID2.class, deser2.getClass());

    //now change the serializationUID = 1L which is the wrong value
    byte[] b2x = mockSerialData(FixtureClassWithSerialVersionUID2.class, 0x1L);

    //deserialize fails with InvalidClassException: ... local class incompatible: stream classdesc serialVersionUID = 1, local class serialVersionUID = 1311768467284833366
    try {
      ser.deserialize(b2x, FixtureClassWithSerialVersionUID2.class);
      Assert.fail("former call must fail");
    }
    catch (InvalidClassException e) {
      //expected failure
    }
  }

  private static byte[] mockSerialData(Class<?> clazz, long serialVersionUID) {
    String className = clazz.getName();
    //big endian
    ByteBuffer buf = ByteBuffer.allocate(21 + className.length());
    buf.putShort((short) 0xaced);//STREAM_MAGIC
    buf.putShort((short) 0x0005);//STREAM_VERSION
    buf.put((byte) 0x73);//TC_OBJECT
    buf.put((byte) 0x72);//TC_CLASSDESC
    buf.putShort((short) className.length());//length of class name
    buf.put(className.getBytes());//class name
    buf.putLong(serialVersionUID);//serialVersionUID
    buf.put(new byte[]{0x02});//flags: Serializable=0x02
    buf.putShort((short) 0x0000);// number of fields in this class
    buf.put((byte) 0x78);//TC_ENDBLOCKDATA
    buf.put((byte) 0x70);//TC_NULL
    return buf.array();
  }
}
