/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;

import org.eclipse.scout.rt.platform.util.CloneUtility;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @since 3.8.3
 */
public class CloneUtilityTest {

  private static final String CUSTOM_TYPE_CLASS_NAME = "org.eclipse.scout.james.shared.tests.commons.utility.CustomType";
  private static final int[] CUSTOM_TYPE_CLASS_CONTENTS = {
      0xca, 0xfe, 0xba, 0xbe, 0x00, 0x00, 0x00, 0x32, 0x00, 0x17, 0x07, 0x00, 0x02, 0x01, 0x00,
      0x3f, 0x6f, 0x72, 0x67, 0x2f, 0x65, 0x63, 0x6c, 0x69, 0x70, 0x73, 0x65, 0x2f, 0x73, 0x63,
      0x6f, 0x75, 0x74, 0x2f, 0x6a, 0x61, 0x6d, 0x65, 0x73, 0x2f, 0x73, 0x68, 0x61, 0x72, 0x65,
      0x64, 0x2f, 0x74, 0x65, 0x73, 0x74, 0x73, 0x2f, 0x63, 0x6f, 0x6d, 0x6d, 0x6f, 0x6e, 0x73,
      0x2f, 0x75, 0x74, 0x69, 0x6c, 0x69, 0x74, 0x79, 0x2f, 0x43, 0x75, 0x73, 0x74, 0x6f, 0x6d,
      0x54, 0x79, 0x70, 0x65, 0x07, 0x00, 0x04, 0x01, 0x00, 0x10, 0x6a, 0x61, 0x76, 0x61, 0x2f,
      0x6c, 0x61, 0x6e, 0x67, 0x2f, 0x4f, 0x62, 0x6a, 0x65, 0x63, 0x74, 0x07, 0x00, 0x06, 0x01,
      0x00, 0x14, 0x6a, 0x61, 0x76, 0x61, 0x2f, 0x69, 0x6f, 0x2f, 0x53, 0x65, 0x72, 0x69, 0x61,
      0x6c, 0x69, 0x7a, 0x61, 0x62, 0x6c, 0x65, 0x01, 0x00, 0x10, 0x73, 0x65, 0x72, 0x69, 0x61,
      0x6c, 0x56, 0x65, 0x72, 0x73, 0x69, 0x6f, 0x6e, 0x55, 0x49, 0x44, 0x01, 0x00, 0x01, 0x4a,
      0x01, 0x00, 0x0d, 0x43, 0x6f, 0x6e, 0x73, 0x74, 0x61, 0x6e, 0x74, 0x56, 0x61, 0x6c, 0x75,
      0x65, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x01, 0x00, 0x06, 0x3c, 0x69,
      0x6e, 0x69, 0x74, 0x3e, 0x01, 0x00, 0x03, 0x28, 0x29, 0x56, 0x01, 0x00, 0x04, 0x43, 0x6f,
      0x64, 0x65, 0x0a, 0x00, 0x03, 0x00, 0x10, 0x0c, 0x00, 0x0c, 0x00, 0x0d, 0x01, 0x00, 0x0f,
      0x4c, 0x69, 0x6e, 0x65, 0x4e, 0x75, 0x6d, 0x62, 0x65, 0x72, 0x54, 0x61, 0x62, 0x6c, 0x65,
      0x01, 0x00, 0x12, 0x4c, 0x6f, 0x63, 0x61, 0x6c, 0x56, 0x61, 0x72, 0x69, 0x61, 0x62, 0x6c,
      0x65, 0x54, 0x61, 0x62, 0x6c, 0x65, 0x01, 0x00, 0x04, 0x74, 0x68, 0x69, 0x73, 0x01, 0x00,
      0x41, 0x4c, 0x6f, 0x72, 0x67, 0x2f, 0x65, 0x63, 0x6c, 0x69, 0x70, 0x73, 0x65, 0x2f, 0x73,
      0x63, 0x6f, 0x75, 0x74, 0x2f, 0x6a, 0x61, 0x6d, 0x65, 0x73, 0x2f, 0x73, 0x68, 0x61, 0x72,
      0x65, 0x64, 0x2f, 0x74, 0x65, 0x73, 0x74, 0x73, 0x2f, 0x63, 0x6f, 0x6d, 0x6d, 0x6f, 0x6e,
      0x73, 0x2f, 0x75, 0x74, 0x69, 0x6c, 0x69, 0x74, 0x79, 0x2f, 0x43, 0x75, 0x73, 0x74, 0x6f,
      0x6d, 0x54, 0x79, 0x70, 0x65, 0x3b, 0x01, 0x00, 0x0a, 0x53, 0x6f, 0x75, 0x72, 0x63, 0x65,
      0x46, 0x69, 0x6c, 0x65, 0x01, 0x00, 0x0f, 0x43, 0x75, 0x73, 0x74, 0x6f, 0x6d, 0x54, 0x79,
      0x70, 0x65, 0x2e, 0x6a, 0x61, 0x76, 0x61, 0x00, 0x21, 0x00, 0x01, 0x00, 0x03, 0x00, 0x01,
      0x00, 0x05, 0x00, 0x01, 0x00, 0x1a, 0x00, 0x07, 0x00, 0x08, 0x00, 0x01, 0x00, 0x09, 0x00,
      0x00, 0x00, 0x02, 0x00, 0x0a, 0x00, 0x01, 0x00, 0x01, 0x00, 0x0c, 0x00, 0x0d, 0x00, 0x01,
      0x00, 0x0e, 0x00, 0x00, 0x00, 0x2f, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x05, 0x2a,
      0xb7, 0x00, 0x0f, 0xb1, 0x00, 0x00, 0x00, 0x02, 0x00, 0x11, 0x00, 0x00, 0x00, 0x06, 0x00,
      0x01, 0x00, 0x00, 0x00, 0x12, 0x00, 0x12, 0x00, 0x00, 0x00, 0x0c, 0x00, 0x01, 0x00, 0x00,
      0x00, 0x05, 0x00, 0x13, 0x00, 0x14, 0x00, 0x00, 0x00, 0x01, 0x00, 0x15, 0x00, 0x00, 0x00,
      0x02, 0x00, 0x16};

  private static CloneUtilityTestClassLoader s_customClassLoader;
  private static Class<?> s_customClass;
  private static Object s_customObject;

  @BeforeClass
  public static void beforeClass() throws Exception {
    s_customClassLoader = new CloneUtilityTestClassLoader();
    s_customClass = s_customClassLoader.defineClass(CUSTOM_TYPE_CLASS_NAME, toByteArray(CUSTOM_TYPE_CLASS_CONTENTS));
    s_customObject = s_customClass.newInstance();
  }

  @Test(expected = ClassNotFoundException.class)
  public void testCustomClassNotFoundByDefaultClassLoader() throws Exception {
    // if this test fails, make sure the custom type class has been deleted
    CloneUtilityTest.class.getClassLoader().loadClass(CUSTOM_TYPE_CLASS_NAME);
  }

  @Test
  public void testCustomClassFoundByCustomClassLoader() throws Exception {
    assertNotNull(s_customClass);
    assertSame(s_customClassLoader, s_customClass.getClassLoader());
    assertSame(s_customClass, s_customClassLoader.loadClass(CUSTOM_TYPE_CLASS_NAME));
    assertNotNull(s_customObject);
  }

  @Test
  public void testCloneKnownObject() throws Exception {
    MyObject orig = new MyObject();
    String s = "test";
    orig.setObject(s);
    MyObject clone = CloneUtility.createDeepCopyBySerializing(orig);
    assertNotNull(clone);
    assertNotSame(orig, clone);
    assertNull(clone.getType());
    assertNotSame(s, clone.getObject());
    assertEquals(s, clone.getObject());
  }

  @Test
  public void testCloneCustomObject() throws Exception {
    MyObject orig = new MyObject();
    orig.setObject(s_customObject);
    MyObject clone = CloneUtility.createDeepCopyBySerializing(orig);
    assertNotNull(clone);
    assertNotSame(orig, clone);
    assertNull(clone.getType());
    assertNotSame(s_customObject, clone.getObject());
  }

  @Test
  public void testCloneKnownType() throws Exception {
    MyObject orig = new MyObject();
    orig.setType(String.class);
    MyObject clone = CloneUtility.createDeepCopyBySerializing(orig);
    assertNotNull(clone);
    assertNotSame(orig, clone);
    assertSame(String.class, clone.getType());
    assertNull(clone.getObject());
  }

  @Test
  public void testCloneCustomType() throws Exception {
    MyObject orig = new MyObject();
    orig.setType(s_customClass);
    MyObject clone = CloneUtility.createDeepCopyBySerializing(orig);
    assertNotNull(clone);
    assertNotSame(orig, clone);
    assertSame(s_customClass, clone.getType());
    assertNull(clone.getObject());
  }

  /* ##########################################################################
   * FIXTURE
   * ##########################################################################
   */
  public static class MyObject implements Serializable {
    private static final long serialVersionUID = 1L;

    private Class<?> m_type;
    private Object m_object;

    public Class<?> getType() {
      return m_type;
    }

    public void setType(Class<?> type) {
      m_type = type;
    }

    public Object getObject() {
      return m_object;
    }

    public void setObject(Object object) {
      m_object = object;
    }
  }

  private static class CloneUtilityTestClassLoader extends ClassLoader {
    public Class<?> defineClass(String name, byte[] b) {
      return defineClass(name, b, 0, b.length);
    }
  }

  /* ##########################################################################
   * UTILITY METHODS FOR SETTING UP THIS TEST
   * ##########################################################################
   *
   * Creating the byte array above is done by:
   * 1) create the following java source file
   *     package org.eclipse.scout.james.shared.tests.commons.utility;
   *     import java.io.Serializable;
   *     public class CustomType implements Serializable {
   *       private static final long serialVersionUID = 1L;
   *     }
   * 2) compile the code and execute the main method of this class
   * 3) copy the output and replace the byte array above
   * 4) delete the class 'CustomType' (.java and .class file)
   */
  private static final char[] HEX_ARRAY = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

  public static byte[] toByteArray(int[] intArray) {
    byte[] byteArray = new byte[intArray.length];
    for (int i = 0; i < intArray.length; i++) {
      byteArray[i] = (byte) intArray[i];
    }
    return byteArray;
  }

  public static void main(String[] args) throws Exception {
    String classFileName = CUSTOM_TYPE_CLASS_NAME.replace('.', '/') + ".class";
    URL resource = CloneUtilityTest.class.getClassLoader().getResource(classFileName);
    if (resource == null) {
      System.err.println("could not find resource '" + classFileName + "'. Aborting.");
      System.exit(1);
      return;
    }
    System.out.println("reading class file: " + resource + "\n\n");

    InputStream in = resource.openStream();
    StringBuilder sb = new StringBuilder();
    sb.append("private static final String CUSTOM_TYPE_CLASS_NAME = \"");
    sb.append(CUSTOM_TYPE_CLASS_NAME);
    sb.append("\";\n");
    sb.append("private static final int[] CUSTOM_TYPE_CLASS_CONTENTS = {");
    int counter = 0;

    int size;
    byte[] buffer = new byte[512];
    while ((size = in.read(buffer)) > -1) {
      for (int i = 0; i < size; i++) {
        int v = buffer[i] & 0xff;
        if (counter > 0) {
          sb.append(", ");
        }
        if (counter % 15 == 0) {
          sb.append("\n    ");
        }
        sb.append("0x");
        sb.append(HEX_ARRAY[v >>> 4]);
        sb.append(HEX_ARRAY[v & 0x0F]);
        counter++;
      }
    }
    sb.append("};");
    System.out.println(sb);
  }
}
