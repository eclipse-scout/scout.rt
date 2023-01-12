/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jdbc.db2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.scout.rt.server.jdbc.style.AbstractSqlStyle;

public class DB2SqlStyle extends AbstractSqlStyle {

  private static final long serialVersionUID = 1L;

  @Override
  public String getConcatOp() {
    return "||";
  }

  @Override
  public String getLikeWildcard() {
    return "%";
  }

  @Override
  protected int getMaxListSize() {
    return MAX_LIST_SIZE;
  }

  @Override
  public boolean isLargeString(String s) {
    return (s.length() > MAX_SQL_STRING_LENGTH);
  }

  @Override
  public boolean isBlobEnabled() {
    return true;
  }

  @Override
  public boolean isClobEnabled() {
    return true;
  }

  @Override
  public void testConnection(Connection conn) throws SQLException {
    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM SYSIBM.SYSTABLES")) {
      rs.next();
    }
  }

  @Override
  protected String adaptBindNameTimeDateOp(String bindName) {
    return " TO_NUMBER(" + adaptBindName(bindName) + ") ";
  }
}
