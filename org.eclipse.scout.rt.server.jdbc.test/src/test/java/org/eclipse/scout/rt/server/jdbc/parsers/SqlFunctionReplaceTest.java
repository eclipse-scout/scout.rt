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
package org.eclipse.scout.rt.server.jdbc.parsers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.server.jdbc.parsers.token.DatabaseSpecificToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueInputToken;
import org.junit.Test;

public class SqlFunctionReplaceTest {

  /**
   * sql functions in sql style
   */
  @Test
  public void testFunctionReplacement() {

    String sql = "SELECT $$sysdate FROM DUAL";

    BindModel bindModel = new BindParser(sql).parse();
    IToken[] tokens = bindModel.getAllTokens();

    int dbSpecTokenFound = 0;
    for (IToken token : tokens) {
      if (token instanceof DatabaseSpecificToken) {
        assertEquals("sysdate", ((DatabaseSpecificToken) token).getName());
        dbSpecTokenFound++;
      }
    }
    assertTrue("no DatabaseSpecificToken found", dbSpecTokenFound > 0);

    sql = "SELECT $$sysdate FROM TABLE1 WHERE COLUMN1 != $$nvl(:val1,0)";

    bindModel = new BindParser(sql).parse();
    tokens = bindModel.getAllTokens();

    dbSpecTokenFound = 0;
    int dbSpecTokenCount = 0;
    for (IToken token : tokens) {
      if (token instanceof DatabaseSpecificToken) {
        if (dbSpecTokenCount == 0) {
          assertEquals("sysdate", ((DatabaseSpecificToken) token).getName());
        }
        if (dbSpecTokenCount == 1) {
          assertEquals("nvl", ((DatabaseSpecificToken) token).getName());
        }
        dbSpecTokenCount++;
        dbSpecTokenFound++;
      }
    }

    assertTrue("no DatabaseSpecificToken found", dbSpecTokenFound > 0);

    sql = "SELECT A, B, C "
        + "FROM   TABLE1 "
        + "WHERE  COLUMN1 != $$nvl(:val1,0) "
        + "INTO   :{a}, :{b}, {c} ";

    bindModel = new BindParser(sql).parse();
    tokens = bindModel.getAllTokens();

    dbSpecTokenFound = 0;
    int valueInputCount = 0;
    for (IToken token : tokens) {
      if (token instanceof DatabaseSpecificToken) {
        assertEquals("nvl", ((DatabaseSpecificToken) token).getName());
        dbSpecTokenFound++;
      }
      if (token instanceof ValueInputToken) {
        if (valueInputCount == 0) {
          assertEquals("val1", ((ValueInputToken) token).getName());
        }
        if (valueInputCount == 1) {
          assertEquals("a", ((ValueInputToken) token).getName());
        }
        if (valueInputCount == 2) {
          assertEquals("b", ((ValueInputToken) token).getName());
        }
        if (valueInputCount == 3) {
          assertEquals("c", ((ValueInputToken) token).getName());
        }
        valueInputCount++;
      }
    }

    assertTrue("no DatabaseSpecificToken found", dbSpecTokenFound > 0);

    sql = "INSERT INTO TABLE1 (A, B, C) "
        + "VALUES (:{a}, :{b}, :{c}) ";

    bindModel = new BindParser(sql).parse();
    tokens = bindModel.getAllTokens();

    valueInputCount = 0;
    for (IToken token : tokens) {
      if (token instanceof ValueInputToken) {
        if (valueInputCount == 0) {
          assertEquals("a", ((ValueInputToken) token).getName());
        }
        if (valueInputCount == 1) {
          assertEquals("b", ((ValueInputToken) token).getName());
        }
        if (valueInputCount == 2) {
          assertEquals("c", ((ValueInputToken) token).getName());
        }
        valueInputCount++;
      }
    }
  }
}
