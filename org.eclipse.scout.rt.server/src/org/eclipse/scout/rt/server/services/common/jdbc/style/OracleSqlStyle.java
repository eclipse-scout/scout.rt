/*******************************************************************************
 * Copyright (c) 2010,2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.jdbc.style;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.eclipse.scout.rt.server.services.common.jdbc.SqlBind;

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
        catch (Throwable t) {
        }
      }
    }
  }

  @Override
  public void writeBind(PreparedStatement ps, int jdbcBindIndex, SqlBind bind) throws SQLException {
    switch (bind.getSqlType()) {
      case Types.CLOB: {
        if (bind.getValue() instanceof Clob) {
          super.writeBind(ps, jdbcBindIndex, bind);
        }
        else {
          String s = (String) bind.getValue();
          Clob clob;
          try {
            Class<?> clobClass;
            clobClass = Class.forName("oracle.sql.CLOB", true, ps.getClass().getClassLoader());
            clob = (Clob) clobClass.getMethod("createTemporary", new Class[]{Connection.class, boolean.class, int.class}).invoke(null, new Object[]{ps.getConnection(), false, clobClass.getField("DURATION_SESSION").get(null)});
          }
          catch (Throwable t) {
            SQLException ex = new SQLException("bind clob on jdbcIndex " + jdbcBindIndex);
            ex.initCause(t);
            throw ex;
          }
          //
          try {
            clob.getClass().getMethod("putString", new Class[]{long.class, String.class}).invoke(clob, new Object[]{1, s});
          }
          catch (Throwable t) {
            clob.setString(1, s);
          }
          ps.setClob(jdbcBindIndex, clob);
        }
        break;
      }
      case Types.BLOB: {
        if (bind.getValue() instanceof Blob) {
          super.writeBind(ps, jdbcBindIndex, bind);
        }
        else {
          byte[] data = (byte[]) bind.getValue();
          Blob blob;
          try {
            Class<?> blobClass = Class.forName("oracle.sql.BLOB", true, ps.getClass().getClassLoader());
            blob = (Blob) blobClass.getMethod("createTemporary", new Class[]{Connection.class, boolean.class, int.class}).invoke(null, new Object[]{ps.getConnection(), false, blobClass.getField("DURATION_SESSION").get(null)});
          }
          catch (Throwable t) {
            SQLException ex = new SQLException("bind blob on jdbcIndex " + jdbcBindIndex);
            ex.initCause(t);
            throw ex;
          }
          //
          try {
            blob.getClass().getMethod("putBytes", new Class[]{long.class, byte[].class}).invoke(blob, new Object[]{1, data});
          }
          catch (Throwable t) {
            blob.setBytes(1, data);
          }
          ps.setBlob(jdbcBindIndex, blob);
        }
        break;
      }
      default: {
        super.writeBind(ps, jdbcBindIndex, bind);
      }
    }
  }
}
