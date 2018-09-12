/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.serialization;

import java.io.Serializable;
import java.nio.ByteBuffer;

import org.eclipse.scout.rt.platform.util.HexUtility;
import org.junit.Assert;
import org.junit.Test;

public class SerializationUtilityTest3 {

  private interface IAnimal0 extends Serializable {
  }

  @SuppressWarnings("unused")
  private interface IAnimal1 extends Serializable, Cloneable {

    String __fixtureField = "F";

    default void __fixtureMethod() {
    }
  }

  @SuppressWarnings("unused")
  private interface IAnimal2 extends Serializable, Cloneable {
    long serialVersionUID = 0x0123456789012345L;

    String __fixtureField = "F";

    default void __fixtureMethod() {
    }
  }

  @Test
  public void testInterfaceLiteralsWithClone() throws Exception {
    IObjectSerializer ser = new BasicObjectSerializer(null);
    byte[] a0 = ser.serialize(IAnimal0.class);
    byte[] a1 = ser.serialize(IAnimal1.class);
    byte[] a2 = ser.serialize(IAnimal2.class);
    dump("0", a0);
    dump("1", a1);
    dump("2", a2);
    Assert.assertArrayEquals(mockSerialData(IAnimal0.class, 0x61df14aa141f7c9eL), a0);
    Assert.assertArrayEquals(mockSerialData(IAnimal1.class, 0x8d207f4e15d110a8L), a1);
    Assert.assertArrayEquals(mockSerialData(IAnimal2.class, 0x0123456789012345L), a2);

    //serialize IAnimal0, deserialize IAnimal2
    byte[] a = ser.serialize(IAnimal0.class);
    Assert.assertEquals('0', a[a.length - 14]);
    a[a.length - 14] = '2';

    Class<?> c2 = ser.deserialize(a, Class.class);
    Assert.assertSame(IAnimal2.class, c2);
  }

  private static void dump(String name, byte[] a) {
    System.out.print(name + ": ");
    for (int i = 0; i < a.length; i++) {
      String h = HexUtility.encode(new byte[]{a[i]});
      System.out.print(h + " ");
    }
    System.out.println();

    System.out.print(name + ": ");
    for (int i = 0; i < a.length; i++) {
      System.out.print(" " + (a[i] >= 32 ? (char) a[i] : ' ') + " ");
    }
    System.out.println();
  }

  private static byte[] mockSerialData(Class<?> clazz, long serialVersionUID) {
    String className = clazz.getName();
    //big endian
    ByteBuffer buf = ByteBuffer.allocate(21 + className.length());
    buf.putShort((short) 0xaced);//STREAM_MAGIC
    buf.putShort((short) 0x0005);//STREAM_VERSION
    buf.put((byte) 0x76);//TC_CLASS
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
