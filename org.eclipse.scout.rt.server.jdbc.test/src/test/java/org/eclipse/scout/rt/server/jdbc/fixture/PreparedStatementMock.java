/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jdbc.fixture;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

  private void setObjectInternal(int columnIndex, Object obj) {
    //ok
  }

  public void setObject(int columnIndex, Object obj, int targetSqlType) {
    //ok
  }

  public void close() {
  }

  public void cancel() {
  }

  public SQLWarning getWarnings() {
    return null;
  }

  public void clearWarnings() {
  }

  public boolean isClosed() {
    return false;
  }

  public ResultSet executeQuery() {
    return new ResultSetMock(getProtocol(), m_resultData).getResultSet();
  }

  public int executeUpdate() {
    return 0;
  }
}
