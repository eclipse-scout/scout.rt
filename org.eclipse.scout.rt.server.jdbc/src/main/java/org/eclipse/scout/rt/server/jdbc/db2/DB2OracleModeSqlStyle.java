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
import java.sql.SQLException;
import java.sql.Statement;

public class DB2OracleModeSqlStyle extends DB2SqlStyle {

  private static final long serialVersionUID = 1L;

  @Override
  public void testConnection(Connection conn) throws SQLException {
    try (Statement testStatement = conn.createStatement()) {
      testStatement.execute("SELECT 1 FROM DUAL");
    }
  }
}
