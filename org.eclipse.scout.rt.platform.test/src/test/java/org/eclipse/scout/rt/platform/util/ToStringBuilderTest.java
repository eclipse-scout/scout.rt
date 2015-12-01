/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.junit.Test;

public class ToStringBuilderTest {

  @Test
  public void testInstanceClass() {
    Instance instance = new Instance(123 /* HashCode */);

    ToStringBuilder builder = new ToStringBuilder(instance);
    builder.attr("string", "blubber");
    builder.attr("int", 1);
    builder.attr("long", 2L);
    builder.attr("short", (short) 3);
    builder.attr("float", (float) 4);
    builder.attr("double", (double) 5.0);
    builder.ref("ref", REF);
    builder.attr("obj", REF);

    assertEquals("Instance@7b[string=blubber, int=1, long=2, short=3, float=4.0, double=5.0, ref=Object@1c8, obj=OBJ]", builder.toString());
  }

  @Test
  public void testNullValue() {
    Instance instance = new Instance(123 /* HashCode */);

    ToStringBuilder builder = new ToStringBuilder(instance);
    builder.attr(null);

    assertEquals("Instance@7b[]", builder.toString());
  }

  @Test
  public void testNullAttribute() {
    Instance instance = new Instance(123 /* HashCode */);

    ToStringBuilder builder = new ToStringBuilder(instance);
    builder.attr("attr1", (Object) null);
    builder.attr("attr2", (Object) null, true);
    builder.attr("attr3", (Object) null, false);

    assertEquals("Instance@7b[attr1=null, attr2=null]", builder.toString());
  }

  @Test
  public void testCollectionAttribute1() {
    Instance instance = new Instance(123 /* HashCode */);

    ToStringBuilder builder = new ToStringBuilder(instance);
    builder.attr("c1", Arrays.asList(1, 2, null, 3));
    builder.attr("c2", (Collection) null);
    builder.attr("c3", new ArrayList<>(0));
    builder.attr("c4", Arrays.asList((String) null));

    assertEquals("Instance@7b[c1=[1,2,3], c2=[], c3=[], c4=[]]", builder.toString());
  }

  @Test
  public void testCollectionAttribute2() {
    Instance instance = new Instance(123 /* HashCode */);

    ToStringBuilder builder = new ToStringBuilder(instance);
    builder.attr("c1", Arrays.asList(1, 2, null, 3), false);
    builder.attr("c2", (Collection) null, false);
    builder.attr("c3", new ArrayList<>(0), false);
    builder.attr("c4", Arrays.asList((String) null), false);

    assertEquals("Instance@7b[c1=[1,2,3]]", builder.toString());
  }

  @Test
  public void testVarArgAttribute1() {
    Instance instance = new Instance(123 /* HashCode */);

    ToStringBuilder builder = new ToStringBuilder(instance);
    builder.attr("c1", 1, 2, null, 3);
    builder.attr("c2", (Object[]) null);
    builder.attr("c3", new Object[0]);

    assertEquals("Instance@7b[c1=[1,2,3], c2=[], c3=[]]", builder.toString());
  }

  @Test
  public void testNullStringAttribute() {
    Instance instance = new Instance(123 /* HashCode */);

    ToStringBuilder builder = new ToStringBuilder(instance);
    builder.attr("attr1", (String) null);
    builder.attr("attr2", (String) null, true);
    builder.attr("attr3", (String) null, false);

    assertEquals("Instance@7b[attr1=null, attr2=null]", builder.toString());
  }

  @Test
  public void testEmptyStringAttribute() {
    Instance instance = new Instance(123 /* HashCode */);

    ToStringBuilder builder = new ToStringBuilder(instance);
    builder.attr("attr1", "");
    builder.attr("attr2", "", true);
    builder.attr("attr3", "", false);

    assertEquals("Instance@7b[attr1=, attr2=]", builder.toString());
  }

  @Test
  public void testAnonymousClass1() {
    ToStringBuilder builder = new ToStringBuilder(new Instance(123) {

      @Override
      public int hashCode() {
        return 123;
      }
    });

    builder.attr("string", "blubber");
    builder.attr("int", 1);
    builder.attr("long", 2L);
    builder.attr("short", (short) 3);
    builder.attr("float", (float) 4);
    builder.attr("double", (double) 5.0);
    builder.ref("ref", REF);
    builder.attr("obj", REF);

    assertEquals("Instance@7b[string=blubber, int=1, long=2, short=3, float=4.0, double=5.0, ref=Object@1c8, obj=OBJ]", builder.toString());
  }

  @Test
  public void testAnonymousClass2() {
    ToStringBuilder builder = new ToStringBuilder(new Serializable() {
      private static final long serialVersionUID = 1L;

      @Override
      public int hashCode() {
        return 123;
      }
    });

    builder.attr("string", "blubber");
    builder.attr("int", 1);
    builder.attr("long", 2L);
    builder.attr("short", (short) 3);
    builder.attr("float", (float) 4);
    builder.attr("double", (double) 5.0);
    builder.ref("ref", REF);
    builder.attr("obj", REF);

    assertEquals("Serializable@7b[string=blubber, int=1, long=2, short=3, float=4.0, double=5.0, ref=Object@1c8, obj=OBJ]", builder.toString());
  }

  // === Test classes ===

  private static class Instance {

    private int m_hashCode;

    public Instance(int hashCode) {
      m_hashCode = hashCode;
    }

    @Override
    public int hashCode() {
      return m_hashCode;
    }
  }

  private static final Object REF = new Object() {

    @Override
    public int hashCode() {
      return 456;
    }

    @Override
    public String toString() {
      return "OBJ";
    }
  };
}
