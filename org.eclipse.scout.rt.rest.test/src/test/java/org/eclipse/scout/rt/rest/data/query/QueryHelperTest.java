/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.rest.data.query;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.data.query.QueryHelper;
import org.junit.BeforeClass;
import org.junit.Test;

public class QueryHelperTest {

  private static QueryHelper queryHelper;

  @BeforeClass
  public static void init() {
    queryHelper = BEANS.get(QueryHelper.class);
  }

  @Test
  public void testSplit() {
    List<String> split = queryHelper.split("abc");
    assertEquals(1, split.size());

    split = queryHelper.split("abc,test,aaa");
    assertEquals(3, split.size());
    assertEquals("abc", split.get(0));
    assertEquals("test", split.get(1));
    assertEquals("aaa", split.get(2));
  }

  @Test
  public void testSplit_Empty() {
    List<String> split = queryHelper.split(null);
    assertEquals(0, split.size());

    split = queryHelper.split("");
    assertEquals(0, split.size());
  }

  @Test
  public void testSplit_EdgeComma() {
    List<String> split = queryHelper.split(",abc,ccc,");
    assertEquals(4, split.size());
    assertEquals("", split.get(0));
    assertEquals("abc", split.get(1));
    assertEquals("ccc", split.get(2));
    assertEquals("", split.get(3));
  }

  @Test
  public void testSplit_Brackets() {
    List<String> split = queryHelper.split("abc(ccc,ddd),qqq,aaa(xxx(bbb,asdf)))");
    assertEquals(3, split.size());
    assertEquals("abc(ccc,ddd)", split.get(0));
    assertEquals("qqq", split.get(1));
    assertEquals("aaa(xxx(bbb,asdf)))", split.get(2));
  }

  /**
   * Brackets are invalid -> should not affect splitting
   */
  @Test
  public void testSplit_BracketsInvalid() {
    List<String> split = queryHelper.split("abc(ccc,)ddd),qqq,aaa(xxx(bbb,asdf)))");
    assertEquals(3, split.size());
    assertEquals("abc(ccc,)ddd)", split.get(0));
    assertEquals("qqq", split.get(1));
    assertEquals("aaa(xxx(bbb,asdf)))", split.get(2));
  }
}
