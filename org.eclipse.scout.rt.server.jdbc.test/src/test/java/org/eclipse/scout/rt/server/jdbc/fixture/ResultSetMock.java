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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLWarning;

/**
 * Bug 402901: {@link java.sql.ResultSet} is different in Java 6 and Java 7. To avoid compilation error with one or the
 * other rt.jar, this mock class does not implement this interface
 */
public class ResultSetMock extends VerboseMock implements InvocationHandler /*, java.sql.ResultSet */ {
  private final ResultSet m_rs;
  private final ResultSetMetaData m_rsMeta;
  private Object[][] m_resultData;
  private int m_row = -1;
  private boolean m_wasNull;

  public ResultSetMock(StringBuffer protocol) {
    this(protocol, null);
  }

  public ResultSetMock(StringBuffer protocol, Object[][] resultData) {
    super(protocol);
    m_resultData = resultData;
    m_rs = (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ResultSet.class}, this);
    m_rsMeta = new ResultSetMetaDataMock(resultData).getResultSetMetaData();
  }

  public ResultSet getResultSet() {
    return m_rs;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String mname = method.getName();
    Class[] ptypes = method.getParameterTypes();
    log(ResultSet.class, mname, args);
    //generic getter
    if (mname.startsWith("get") && ptypes != null && ptypes.length == 1 && ptypes[0] == int.class) {
      return getObjectInternal((Integer) args[0], method.getReturnType());
    }
    Method m = ResultSetMock.class.getMethod(mname, ptypes);
    return m.invoke(this, args);
  }

  public boolean next() {
    m_row++;
    m_wasNull = false;
    if (m_resultData != null && m_row < m_resultData.length) {
      return true;
    }
    return false;
  }

  private Object getObjectInternal(int columnIndex, Class<?> type) {
    Object o = m_resultData[m_row][columnIndex - 1];
    m_wasNull = (o == null);
    if (o == null && type.isPrimitive()) {
      if (type == int.class) {
        return (int) 0;
      }
      if (type == long.class) {
        return (long) 0;
      }
      if (type == float.class) {
        return (float) 0;
      }
      if (type == double.class) {
        return (double) 0;
      }
      if (type == boolean.class) {
        return (boolean) false;
      }
      if (type == byte.class) {
        return (byte) 0;
      }
      if (type == char.class) {
        return (char) 0;
      }
    }
    return o;
  }

  public ResultSetMetaData getMetaData() {
    return m_rsMeta;
  }

  public boolean wasNull() {
    return m_wasNull;
  }

  public void close() {
  }

  public SQLWarning getWarnings() {
    return null;
  }

  public void clearWarnings() {
  }

  public int getFetchSize() {
    if (m_resultData != null) {
      return m_resultData.length;
    }
    return 0;
  }

  public void setFetchSize(int rows) {
  }
}
