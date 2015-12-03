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
package org.eclipse.scout.rt.server.jdbc.parsers.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
  public void testBlankString() throws Exception {
    assertEquals("", SqlFormatter.wellform(""));
    assertEquals("", SqlFormatter.wellform(" "));
    assertEquals("", SqlFormatter.wellform("     "));
    assertEquals("", SqlFormatter.wellform("\t"));
  }

  protected void check(String resourceName) throws Exception {
    String s = IOUtility.getContentUtf8(SqlFormatterTest.class.getResourceAsStream(resourceName));
    String w = SqlFormatter.wellform(s);
    int i = w.toLowerCase().indexOf("unparsed");
    if (i != -1) {
      fail("** " + resourceName + " **\n" + w);
    }
    assertEquals(-1, i);
  }
}
