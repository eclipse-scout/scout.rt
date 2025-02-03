/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.platform.classid.ClassIdentifier;
import org.junit.Test;

/**
 * JUnit tests for {@link ClassIdentifier}
 */
public class ClassIdentifierTest {

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyConstructor() {
    new ClassIdentifier();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullConstructor() {
    new ClassIdentifier((Class<?>[]) null);
  }

  @Test
  public void testAbstractClass() {
    ClassIdentifier cid = new ClassIdentifier(AbstractTemplate.class);
    assertArrayEquals(new Class[]{AbstractTemplate.class}, cid.getClasses());
  }

  @Test
  public void testTemplateClassUsingFullPath() {
    ClassIdentifier aid = new ClassIdentifier(Foo.A.class, Foo.A.InnerClass.class);
    ClassIdentifier bid = new ClassIdentifier(Foo.B.class, Foo.B.InnerClass.class);
    assertFalse(aid.equals(bid));
    assertNotSame(aid.getClasses()[0], bid.getClasses()[0]);
    assertSame(aid.getLastSegment(), bid.getLastSegment());
  }

  @Test
  public void testTemplateClassUsingShortestPath() {
    ClassIdentifier aid = new ClassIdentifier(Foo.A.class, AbstractTemplate.InnerClass.class);
    ClassIdentifier bid = new ClassIdentifier(Foo.B.class, AbstractTemplate.InnerClass.class);
    assertFalse(aid.equals(bid));
    assertNotSame(aid.getClasses()[0], bid.getClasses()[0]);
    assertSame(aid.getLastSegment(), bid.getLastSegment());
  }

  @Test
  public void testConvertClassArray_nullArgument() {
    ClassIdentifier[] cids = ClassIdentifier.convertClassArrayToClassIdentifierArray((Class<?>[]) null);
    assertNotNull(cids);
    assertArrayEquals(new ClassIdentifier[0], cids);
  }

  @Test
  public void testConvertClassArray_emptyArgument() {
    ClassIdentifier[] cids = ClassIdentifier.convertClassArrayToClassIdentifierArray();
    assertNotNull(cids);
    assertArrayEquals(new ClassIdentifier[0], cids);
  }

  @Test
  public void testConvertClassArray_nullClasses() {
    ClassIdentifier[] cids = ClassIdentifier.convertClassArrayToClassIdentifierArray(String.class, null, Integer.class);
    assertNotNull(cids);
    assertEquals(2, cids.length);
    assertArrayEquals(new ClassIdentifier[]{new ClassIdentifier(String.class), new ClassIdentifier(Integer.class)}, cids);
  }

  @Test
  public void testCreateWithContextOnly() {
    ClassIdentifier cid = new ClassIdentifier(String.class);
    ClassIdentifier cid2 = new ClassIdentifier(cid);
    ClassIdentifier cid3 = new ClassIdentifier(cid, (Class[]) null);
    assertNotSame(cid, cid2);
    assertNotSame(cid, cid3);
    assertEquals(cid, cid2);
    assertEquals(cid, cid3);
    assertArrayEquals(new Class[]{String.class}, cid2.getClasses());
    assertArrayEquals(new Class[]{String.class}, cid3.getClasses());
  }

  @Test
  public void testCreateWithContextAndCalss() {
    ClassIdentifier cid = new ClassIdentifier(String.class);
    ClassIdentifier cid2 = new ClassIdentifier(cid, Long.class);
    assertNotSame(cid, cid2);
    assertArrayEquals(new Class[]{String.class, Long.class}, cid2.getClasses());
  }

  @Test
  public void testCreateWithContextAndCalsses() {
    ClassIdentifier cid = new ClassIdentifier(String.class);
    ClassIdentifier cid2 = new ClassIdentifier(cid, Long.class, Boolean.class);
    assertNotSame(cid, cid2);
    assertArrayEquals(new Class[]{String.class, Long.class, Boolean.class}, cid2.getClasses());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateWithNullSegement() {
    new ClassIdentifier(String.class, null);
  }

  public abstract static class AbstractTemplate {
    public class InnerClass {
    }
  }

  public static class Foo {
    public class A extends AbstractTemplate {
    }

    public class B extends AbstractTemplate {
    }
  }
}
