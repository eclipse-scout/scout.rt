/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.rest.data.query;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.rest.data.query.ResourceInclude;
import org.junit.Test;

public class ResourceIncludeTest {

  @Test
  public void testParse() {
    ResourceInclude include = new ResourceInclude();
    include.addValidInclude("attribute");

    include.parse("attribute");
    assertTrue(include.isIncluded("attribute"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParse_NotValid() {
    ResourceInclude include = new ResourceInclude();
    include.addValidInclude("attribute");

    include.parse("invalid");
  }

  @Test
  public void testParse_Multiple() {
    ResourceInclude include = new ResourceInclude();
    include.addValidInclude("attribute");
    include.addValidInclude("another");

    include.parse("another,attribute");

    assertTrue(include.isIncluded("another"));
    assertTrue(include.isIncluded("attribute"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParse_MultipleInvalid() {
    ResourceInclude include = new ResourceInclude();
    include.addValidInclude("attribute");
    include.addValidInclude("another");

    include.parse("another,attribute,invalid");
  }

  @Test
  public void testParse_Nested() {
    ResourceInclude include = new ResourceInclude();
    include.addValidInclude("nested", NestedInclude.class);

    include.parse("nested(attribute)");
    assertTrue(include.isIncluded("nested"));
    assertTrue(((NestedInclude) include.getInclude("nested")).isIncluded("attribute"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParse_NestedNotValid() {
    ResourceInclude include = new ResourceInclude();
    include.addValidInclude("nested", NestedInclude.class);

    include.parse("nested(notvalid)");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParse_NestedInvalidBrackets() {
    ResourceInclude include = new ResourceInclude();
    include.addValidInclude("nested", NestedInclude.class);

    include.parse("nested(notvalid");
  }

  @Test
  public void testParse_MultipleNested() {
    ResourceInclude include = new ResourceInclude();
    include.addValidInclude("nested", NestedInclude.class);

    include.parse("nested(attribute,another)");
    assertTrue(include.isIncluded("nested"));
    assertTrue(((NestedInclude) include.getInclude("nested")).isIncluded("attribute"));
    assertTrue(((NestedInclude) include.getInclude("nested")).isIncluded("another"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParse_MultipleNestedNotValid() {
    ResourceInclude include = new ResourceInclude();
    include.addValidInclude("nested", NestedInclude.class);

    include.parse("nested(attribute,another,invalid)");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParse_MultipleNestedComplex() {
    ResourceInclude include = new ResourceInclude();
    include.addValidInclude("nested", NestedInclude.class);
    include.addValidInclude("another");
    include.addValidInclude("anotherNested", NestedInclude.class);

    include.parse("nested(attribute,another,invalid),another,anotherNested(attribute)");
    assertTrue(include.isIncluded("nested"));
    assertTrue(include.isIncluded("another"));
    assertTrue(include.isIncluded("anotherNested"));
    assertTrue(((NestedInclude) include.getInclude("nested")).isIncluded("attribute"));
    assertFalse(include.getInclude("another") instanceof NestedInclude);
    assertTrue(((NestedInclude) include.getInclude("anotherNested")).isIncluded("attribute"));
  }

  @Test
  public void testInclude() {
    ResourceInclude include = new ResourceInclude();
    include.addValidInclude("attribute");

    include.include("attribute");
    assertTrue(include.isIncluded("attribute"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInclude_NotValid() {
    ResourceInclude include = new ResourceInclude();
    include.addValidInclude("attribute");

    include.include("invalid");
  }

  @Test
  public void testInclude_Multiple() {
    ResourceInclude include = new ResourceInclude();
    include.addValidInclude("attribute");
    include.addValidInclude("another");

    include.include("attribute").include("another");

    assertTrue(include.isIncluded("another"));
    assertTrue(include.isIncluded("attribute"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInclude_MultipleInvalid() {
    ResourceInclude include = new ResourceInclude();
    include.addValidInclude("attribute");
    include.addValidInclude("another");

    include.include("attribute").include("another").include("invalid");
  }

  @Test
  public void testIncludeNested() {
    ResourceInclude include = new ResourceInclude();
    include.addValidInclude("nested", NestedInclude.class);

    include.include("nested", new NestedInclude().include("attribute"));
    assertTrue(include.isIncluded("nested"));
    assertTrue(((NestedInclude) include.getInclude("nested")).isIncluded("attribute"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncludeNestedNotValid() {
    ResourceInclude include = new ResourceInclude();
    include.addValidInclude("nested", NestedInclude.class);

    include.include("nested", new NestedInclude().include("notValid"));
  }

  @Test
  public void testIncludeMultipleNested() {
    ResourceInclude include = new ResourceInclude();
    include.addValidInclude("nested", NestedInclude.class);

    include.include("nested", new NestedInclude().include("attribute").include("another"));
    assertTrue(include.isIncluded("nested"));
    assertTrue(((NestedInclude) include.getInclude("nested")).isIncluded("attribute"));
    assertTrue(((NestedInclude) include.getInclude("nested")).isIncluded("another"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncludeMultipleNestedNotValid() {
    ResourceInclude include = new ResourceInclude();
    include.addValidInclude("nested", NestedInclude.class);

    include.parse("nested(attribute,another,invalid)");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncludeMultipleNestedComplex() {
    ResourceInclude include = new ResourceInclude();
    include.addValidInclude("nested", NestedInclude.class);
    include.addValidInclude("another");
    include.addValidInclude("anotherNested", NestedInclude.class);

    include.parse("nested(attribute,another,invalid),another,anotherNested(attribute)");
    include
        .include("nested", new NestedInclude().include("attribute").include("another"))
        .include("another")
        .include("anotherNested", new NestedInclude().include("attribute"));
    assertTrue(include.isIncluded("nested"));
    assertTrue(include.isIncluded("another"));
    assertTrue(include.isIncluded("anotherNested"));
    assertTrue(((NestedInclude) include.getInclude("nested")).isIncluded("attribute"));
    assertFalse(include.getInclude("another") instanceof NestedInclude);
    assertTrue(((NestedInclude) include.getInclude("anotherNested")).isIncluded("attribute"));
  }

  public static class NestedInclude extends ResourceInclude {
    private static final long serialVersionUID = 1L;

    public NestedInclude() {
      addValidInclude("attribute");
      addValidInclude("another");
    }
  }
}
