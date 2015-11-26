/*******************************************************************************
 * Copyright (c) 2011,2013 BSI Business Systems Integration.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jérémie Bresson
 *     Lynn Aders
 ******************************************************************************/
package org.eclipse.scout.rt.server.jdbc.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.scout.rt.server.jdbc.style.AbstractSqlStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySqlSqlStyle extends AbstractSqlStyle {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(MySqlSqlStyle.class);

  private static final String DATE_SQL_METHOD = "DATE(";
  private static final String UPPERCASE_SQL_METHOD = "upper(";
  private static final String CONCATENATE_SQL_METHOD = "concat(";
  private static final String SQL_ANY = "'%'";

  @Override
  public void testConnection(Connection conn) throws SQLException {
    Statement testStatement = null;
    try {
      testStatement = conn.createStatement();
      testStatement.execute("SELECT 1 FROM DUAL");
    }
    finally {
      if (testStatement != null) {
        try {
          testStatement.close();
        }
        catch (Exception e) {
          LOG.error("Failed to close the connection", e);
        }
      }
    }
  }

  @Override
  public boolean isBlobEnabled() {
    return false;
  }

  @Override
  public boolean isClobEnabled() {
    return false;
  }

  @Override
  public boolean isLargeString(String s) {
    return (s.length() > MAX_SQL_STRING_LENGTH);
  }

  @Override
  protected int getMaxListSize() {
    return MAX_LIST_SIZE;
  }

  @Override
  public String getNvlToken() {
    return "IFNULL";
  }

  @Override
  public String createDateGE(String attribute, String bindName) {
    return attribute + ">=" + DATE_SQL_METHOD + adaptBindName(bindName) + ")";
  }

  @Override
  public String createDateGT(String attribute, String bindName) {
    return attribute + ">" + DATE_SQL_METHOD + adaptBindName(bindName) + ")";
  }

  @Override
  public String createDateLE(String attribute, String bindName) {
    return attribute + "<" + DATE_SQL_METHOD + adaptBindName(bindName) + ") + INTERVAL 1 DAY";
  }

  @Override
  public String createDateLT(String attribute, String bindName) {
    return attribute + "<" + DATE_SQL_METHOD + adaptBindName(bindName) + ")";
  }

  @Override
  public String createStartsWith(String attribute, String bindName) {
    return UPPERCASE_SQL_METHOD + attribute + ") like " + UPPERCASE_SQL_METHOD + CONCATENATE_SQL_METHOD + adaptBindName(bindName) + "," + SQL_ANY + "))";
  }

  @Override
  public String createNotStartsWith(String attribute, String bindName) {
    return UPPERCASE_SQL_METHOD + attribute + ") not like " + UPPERCASE_SQL_METHOD + CONCATENATE_SQL_METHOD + adaptBindName(bindName) + "," + SQL_ANY + "))";
  }

  @Override
  public String createEndsWith(String attribute, String bindName) {
    return UPPERCASE_SQL_METHOD + attribute + ") like " + UPPERCASE_SQL_METHOD + CONCATENATE_SQL_METHOD + SQL_ANY + "," + adaptBindName(bindName) + "))";
  }

  @Override
  public String createNotEndsWith(String attribute, String bindName) {
    return UPPERCASE_SQL_METHOD + attribute + ") not like " + UPPERCASE_SQL_METHOD + CONCATENATE_SQL_METHOD + SQL_ANY + "," + adaptBindName(bindName) + "))";
  }

  @Override
  public String createContains(String attribute, String bindName) {
    return UPPERCASE_SQL_METHOD + attribute + ") like " + UPPERCASE_SQL_METHOD + CONCATENATE_SQL_METHOD + SQL_ANY + "," + adaptBindName(bindName) + "," + SQL_ANY + "))";
  }

  @Override
  public String createNotContains(String attribute, String bindName) {
    return UPPERCASE_SQL_METHOD + attribute + ") not like " + UPPERCASE_SQL_METHOD + CONCATENATE_SQL_METHOD + SQL_ANY + "," + adaptBindName(bindName) + "," + SQL_ANY + "))";
  }
}
