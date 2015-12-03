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
package org.eclipse.scout.rt.server.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface IStatementCache {

  PreparedStatement getPreparedStatement(Connection conn, String s) throws SQLException;

  void releasePreparedStatement(PreparedStatement ps);

  CallableStatement getCallableStatement(Connection conn, String s) throws SQLException;

  void releaseCallableStatement(CallableStatement cs);

}
