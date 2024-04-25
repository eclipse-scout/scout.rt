/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jdbc.parsers.sql;

import static org.junit.Assert.*;

import java.io.InputStream;

import org.eclipse.scout.rt.platform.util.IOUtility;
import org.junit.Test;

/**
 * JUnit for {@link SqlFormatter}
 */
public class SqlFormatterTest {

  @Test
  public void testInsert() throws Exception {
    check("insert1.sql");
    check("insert2.sql");
    check("insert3.sql");
  }

  @Test
  public void testSelectCase() throws Exception {
    check("case-suffix.sql");
    check("select-case1.sql");
    check("select-case2.sql");
    check("select-case3.sql");
  }

  @Test
  public void testSelectCast() throws Exception {
    check("select-cast1.sql");
    check("select-cast2.sql");
    check("select-cast3.sql");
    check("select-cast4.sql");
    check("select-cast5.sql");
  }

  @Test
  public void testFromSubSelect() throws Exception {
    check("from-sub-select1.sql");
  }

  @Test
  public void testSubSelect() throws Exception {
    check("sub-select1.sql");
    check("sub-select2.sql");
  }

  @Test
  public void testNewKeyword() throws Exception {
    check("select-newArray1.sql");
  }

  @Test
  public void testBlankString() {
    assertEquals("", SqlFormatter.wellform(""));
    assertEquals("", SqlFormatter.wellform(" "));
    assertEquals("", SqlFormatter.wellform("     "));
    assertEquals("", SqlFormatter.wellform("\t"));
  }

  protected void check(String resourceName) throws Exception {
    String s;
    try (InputStream in = SqlFormatterTest.class.getResourceAsStream(resourceName)) {
      s = IOUtility.readStringUTF8(in);
    }
    String w = SqlFormatter.wellform(s);
    int i = w.toLowerCase().indexOf("unparsed");
    if (i != -1) {
      fail("** " + resourceName + " **\n" + w);
    }
    assertEquals(-1, i);
  }
}
