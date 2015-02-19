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
package org.eclipse.scout.commons;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;

import org.junit.Test;

public class ToStringBuilderTest {

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

  @Test
  public void testInstanceClass() {
    Testee testee = new Testee();
    assertEquals("Testee@7b[string=blubber, int=1, long=2, short=3, float=4.0, double=5.0, ref=Object@1c8, obj=OBJ]", testee.toString());
  }

  @Test
  public void testAnonymousClass1() {
    Testee testee = new Testee() {

      @Override
      public String toString() {
        return toTestString(this);
      }

      @Override
      public int hashCode() {
        return 123;
      }
    };
    assertEquals("Testee@7b[string=blubber, int=1, long=2, short=3, float=4.0, double=5.0, ref=Object@1c8, obj=OBJ]", testee.toString());
  }

  @Test
  public void testAnonymousClass2() {
    Serializable testee = new Serializable() {
      private static final long serialVersionUID = 1L;

      @Override
      public String toString() {
        return toTestString(this);
      }

      @Override
      public int hashCode() {
        return 123;
      }
    };
    assertEquals("Serializable@7b[string=blubber, int=1, long=2, short=3, float=4.0, double=5.0, ref=Object@1c8, obj=OBJ]", testee.toString());
  }

  private static class Testee {

    @Override
    public String toString() {
      return toTestString(this);
    }

    @Override
    public int hashCode() {
      return 123;
    }
  }

  private static String toTestString(Object instance) {
    ToStringBuilder builder = new ToStringBuilder(instance);
    builder.attr("string", "blubber");
    builder.attr("int", 1);
    builder.attr("long", 2L);
    builder.attr("short", (short) 3);
    builder.attr("float", (float) 4);
    builder.attr("double", (double) 5.0);
    builder.ref("ref", REF);
    builder.attr("obj", REF);

    return builder.toString();
  }
}
