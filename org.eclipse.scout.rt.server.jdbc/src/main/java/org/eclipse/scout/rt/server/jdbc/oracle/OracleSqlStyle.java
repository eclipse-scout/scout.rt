/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jdbc.oracle;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.eclipse.scout.rt.server.jdbc.SqlBind;
import org.eclipse.scout.rt.server.jdbc.style.AbstractSqlStyle;

public class OracleSqlStyle extends AbstractSqlStyle {
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
    try (Statement testStatement = conn.createStatement()) {
      testStatement.execute("SELECT 1 FROM DUAL");
    }
  }

  @Override
  public void writeBind(PreparedStatement ps, int jdbcBindIndex, SqlBind bind) throws SQLException {
    switch (bind.getSqlType()) {
      case Types.BLOB: {
        if (bind.getValue() instanceof Blob) {
          ps.setBlob(jdbcBindIndex, (Blob) bind.getValue());
        }
        else {
          byte[] data = (byte[]) bind.getValue();
          if (data != null) {
            ps.setBlob(jdbcBindIndex, new ByteArrayInputStream(data), data.length);
          }
          else {
            ps.setNull(jdbcBindIndex, Types.BLOB);
          }
        }
        break;
      }
      case Types.CLOB: {
        if (bind.getValue() instanceof Clob) {
          ps.setClob(jdbcBindIndex, (Clob) bind.getValue());
        }
        else {
          String s = (String) bind.getValue();
          if (s != null) {
            ps.setClob(jdbcBindIndex, new StringReader(s), s.length());
          }
          else {
            ps.setNull(jdbcBindIndex, Types.CLOB);
          }
        }
        break;
      }
      default: {
        super.writeBind(ps, jdbcBindIndex, bind);
      }
    }
  }
}
