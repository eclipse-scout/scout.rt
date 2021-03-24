/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.jdbc.oracle;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.UUID;

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
  protected SqlBind createBindFor(Object o, Class c) {
    if (UUID.class.isAssignableFrom(c)) {
      return new SqlBind(Types.OTHER, o);
    }

    return super.createBindFor(o, c);
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
      case Types.OTHER: {
        if (bind.getValue() instanceof UUID) {
          ps.setBytes(jdbcBindIndex, unwrapUuid((UUID) bind.getValue()));
        }
        else {
          super.writeBind(ps, jdbcBindIndex, bind);
        }
        break;
      }
      default: {
        super.writeBind(ps, jdbcBindIndex, bind);
      }
    }
  }

  public byte[] unwrapUuid(UUID javaValue) {
    if (javaValue == null) {
      return null;
    }
    ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
    bb.putLong(javaValue.getMostSignificantBits());
    bb.putLong(javaValue.getLeastSignificantBits());
    return bb.array();
  }
}
