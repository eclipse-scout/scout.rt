/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
