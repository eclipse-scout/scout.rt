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
import java.sql.Connection;

public class ConnectionMock extends VerboseMock implements InvocationHandler {
  private final Connection m_conn;
  private final Object[][] m_resultData;

  public ConnectionMock(StringBuffer protocol) {
    this(protocol, null);
  }

  public ConnectionMock(StringBuffer protocol, Object[][] resultData) {
    super(protocol);
    m_resultData = resultData;
    m_conn = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Connection.class}, this);
  }

  public Connection getConnection() {
    return m_conn;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) {
    String mname = method.getName();
    if ("prepareStatement".equals(mname)) {
      log(Connection.class, mname, args);
      return new PreparedStatementMock(getProtocol(), m_resultData).getPreparedStatement();
    }
    return null;
  }
}
