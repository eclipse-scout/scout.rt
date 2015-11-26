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
package org.eclipse.scout.rt.server.jdbc.fixture;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;

public class PreparedStatementMock extends VerboseMock implements InvocationHandler/*, java.sql.PreparedStatement*/ {
  private final PreparedStatement m_ps;
  private final Object[][] m_resultData;

  public PreparedStatementMock(StringBuffer protocol) {
    this(protocol, null);
  }

  public PreparedStatementMock(StringBuffer protocol, Object[][] resultData) {
    super(protocol);
    m_resultData = resultData;
    m_ps = (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{PreparedStatement.class}, this);
  }

  public PreparedStatement getPreparedStatement() {
    return m_ps;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String mname = method.getName();
    if ("executeQuery".equals(mname) || "getResultSet".equals(mname) || mname.matches("set[a-zA-Z0-9]+")) {
      log(PreparedStatement.class, mname, args);
    }
    //generic setter
    Class[] ptypes = method.getParameterTypes();
    if (mname.startsWith("set") && ptypes != null && ptypes.length == 2 && ptypes[0] == int.class) {
      setObjectInternal((Integer) args[0], args[1]);
      return null;
    }
    return this.getClass().getMethod(mname, method.getParameterTypes()).invoke(this, args);
  }

  private void setObjectInternal(int columnIndex, Object obj) throws SQLException {
    //ok
  }

  public void setObject(int columnIndex, Object obj, int targetSqlType) throws SQLException {
    //ok
  }

  public void close() throws SQLException {
  }

  public void cancel() throws SQLException {
  }

  public SQLWarning getWarnings() throws SQLException {
    return null;
  }

  public void clearWarnings() throws SQLException {
  }

  public boolean isClosed() throws SQLException {
    return false;
  }

  public ResultSet executeQuery() throws SQLException {
    return new ResultSetMock(getProtocol(), m_resultData).getResultSet();
  }

  public int executeUpdate() throws SQLException {
    return 0;
  }
}
