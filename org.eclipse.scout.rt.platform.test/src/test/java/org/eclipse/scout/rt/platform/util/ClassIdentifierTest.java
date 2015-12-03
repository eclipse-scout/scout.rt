/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

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
