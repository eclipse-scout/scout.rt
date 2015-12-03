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
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String mname = method.getName();
    if ("prepareStatement".equals(mname)) {
      log(Connection.class, mname, args);
      return new PreparedStatementMock(getProtocol(), m_resultData).getPreparedStatement();
    }
    return null;
  }
}
