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
package org.eclipse.scout.rt.server.jdbc.postgresql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.eclipse.scout.rt.server.jdbc.SqlBind;
import org.eclipse.scout.rt.server.jdbc.style.AbstractSqlStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgreSqlStyle extends AbstractSqlStyle {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(PostgreSqlStyle.class);

  @Override
  public void testConnection(Connection conn) throws SQLException {
    Statement testStatement = null;
    try {
      testStatement = conn.createStatement();
      testStatement.execute("SELECT 1 + 1");
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
  public Object readBind(final ResultSet rs, final ResultSetMetaData meta, final int type, final int jdbcBindIndex) throws SQLException {
    Object result = null;
    if (Types.BIT == type) {
      result = rs.getObject(jdbcBindIndex);
      if (result instanceof Boolean) {
        return result;
      }
    }
    return super.readBind(rs, meta, type, jdbcBindIndex);
  }

  @Override
  protected SqlBind createBindFor(Object o, @SuppressWarnings("rawtypes") Class c) {
    if (Boolean.class.isAssignableFrom(c)) {
      return new SqlBind(Types.BOOLEAN, o);
    }

    return super.createBindFor(o, c);
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
}
