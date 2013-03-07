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
package org.eclipse.scout.commons;

import org.junit.Assert;
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
    ClassIdentifier cid = new ClassIdentifier(Template.class);
    Assert.assertArrayEquals(new Class[]{Template.class}, cid.getClasses());
  }

  @Test
  public void testTemplateClassUsingFullPath() {
    ClassIdentifier aid = new ClassIdentifier(Foo.A.class, Foo.A.InnerClass.class);
    ClassIdentifier bid = new ClassIdentifier(Foo.B.class, Foo.B.InnerClass.class);
    Assert.assertFalse(aid.equals(bid));
    Assert.assertNotSame(aid.getClasses()[0], bid.getClasses()[0]);
    Assert.assertSame(aid.getLastSegment(), bid.getLastSegment());
  }

  @Test
  public void testTemplateClassUsingShortestPath() {
    ClassIdentifier aid = new ClassIdentifier(Foo.A.class, Template.InnerClass.class);
    ClassIdentifier bid = new ClassIdentifier(Foo.B.class, Template.InnerClass.class);
    Assert.assertFalse(aid.equals(bid));
    Assert.assertNotSame(aid.getClasses()[0], bid.getClasses()[0]);
    Assert.assertSame(aid.getLastSegment(), bid.getLastSegment());
  }

  @Test
  public void testConvertClassArray_nullArgument() {
    ClassIdentifier[] cids = ClassIdentifier.convertClassArrayToClassIdentifierArray((Class<?>[]) null);
    Assert.assertNotNull(cids);
    Assert.assertArrayEquals(new ClassIdentifier[0], cids);
  }

  @Test
  public void testConvertClassArray_emptyArgument() {
    ClassIdentifier[] cids = ClassIdentifier.convertClassArrayToClassIdentifierArray();
    Assert.assertNotNull(cids);
    Assert.assertArrayEquals(new ClassIdentifier[0], cids);
  }

  @Test
  public void testConvertClassArray_nullClasses() {
    ClassIdentifier[] cids = ClassIdentifier.convertClassArrayToClassIdentifierArray(String.class, null, Integer.class);
    Assert.assertNotNull(cids);
    Assert.assertEquals(2, cids.length);
    Assert.assertArrayEquals(new ClassIdentifier[]{new ClassIdentifier(String.class), new ClassIdentifier(Integer.class)}, cids);
  }

  public abstract static class Template {
    public class InnerClass {
    }
  }

  public static class Foo {
    public class A extends Template {
    }

    public class B extends Template {
    }
  }
}
