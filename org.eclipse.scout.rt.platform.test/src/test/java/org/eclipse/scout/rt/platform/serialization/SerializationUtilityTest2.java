/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.serialization;

import java.io.Serializable;
import java.nio.ByteBuffer;

import org.eclipse.scout.rt.platform.util.HexUtility;
import org.junit.Assert;
import org.junit.Test;

public class SerializationUtilityTest2 {

  private interface IAnimal0 {
  }

  private interface IAnimal1 extends Serializable, Cloneable {
    Object clone();
  }

  @SuppressWarnings("unused")
  private interface IAnimal2 extends Serializable, Cloneable {
    //long serialVersionUID = 0x0123456789012345L;

    String __fixtureField = "F";

    default void __fixtureMethod() {
    }

    Object clone();
  }

  private static final class Cat0 implements IAnimal0, Serializable {
    private static final long serialVersionUID = 0x0123456789012345L;
  }

  private static final class Cat1 implements IAnimal1 {
    private static final long serialVersionUID = 0x0123456789012345L;

    @Override
    public Object clone() {
      return null;
    }
  }

  private static final class Cat2 implements IAnimal2 {
    private static final long serialVersionUID = 0x0123456789012345L;

    @Override
    public Object clone() {
      return null;
    }
  }

  @Test
  public void testInterfaceImplementationsWithClone() throws Exception {
    IObjectSerializer ser = new BasicObjectSerializer(null);
    byte[] a0 = ser.serialize(new Cat0());
    byte[] a1 = ser.serialize(new Cat1());
    byte[] a2 = ser.serialize(new Cat2());
    dump("0", a0);
    dump("1", a1);
    dump("2", a2);
    Assert.assertArrayEquals(mockSerialData(Cat0.class, 0x0123456789012345L), a0);
    Assert.assertArrayEquals(mockSerialData(Cat1.class, 0x0123456789012345L), a1);
    Assert.assertArrayEquals(mockSerialData(Cat2.class, 0x0123456789012345L), a2);

    //serialize Cat0, deserialize Cat2
    byte[] a = ser.serialize(new Cat0());
    Assert.assertEquals('0', a[a.length - 14]);
    a[a.length - 14] = '2';
    Cat2 cat2 = ser.deserialize(a, Cat2.class);
    Assert.assertNotNull(cat2);

    //serialize Cat2, deserialize Cat0
    a = ser.serialize(new Cat2());
    Assert.assertEquals('2', a[a.length - 14]);
    a[a.length - 14] = '0';
    Cat0 cat0 = ser.deserialize(a, Cat0.class);
    Assert.assertNotNull(cat0);
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
