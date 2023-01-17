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
import java.sql.ResultSetMetaData;
import java.sql.Types;

public class ResultSetMetaDataMock implements InvocationHandler, ResultSetMetaData {
  private final ResultSetMetaData m_meta;
  private Object[][] m_resultData;

  public ResultSetMetaDataMock(Object[][] resultData) {
    m_resultData = resultData;
    m_meta = (ResultSetMetaData) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ResultSetMetaData.class}, this);
  }

  public ResultSetMetaData getResultSetMetaData() {
    return m_meta;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Method m = this.getClass().getMethod(method.getName(), method.getParameterTypes());
    return m.invoke(this, args);
  }

  @Override
  public int getColumnCount() {
    if (m_resultData != null && m_resultData.length > 0) {
      return m_resultData[0].length;
    }
    return 0;
  }

  @Override
  public int getColumnType(int column) {
    return Types.OTHER;
  }

  @Override
  public int getPrecision(int column) {
    return 0;
  }

  @Override
  public int getScale(int column) {
    return 0;
  }

  @Override
  public int getColumnDisplaySize(int column) {
    int max = 0;
    if (m_resultData != null) {
      for (int i = 0; i < m_resultData.length; i++) {
        if (column < m_resultData[i].length) {
          Object object = m_resultData[i][column];
          if (object != null && object.toString().length() > max) {
            max = object.toString().length();
          }
        }
      }
    }
    return max;
  }

  @Override
  public <T> T unwrap(Class<T> iface) {
    return null;
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) {
    return false;
  }

  @Override
  public boolean isAutoIncrement(int column) {
    return false;
  }

  @Override
  public boolean isCaseSensitive(int column) {
    return false;
  }

  @Override
  public boolean isSearchable(int column) {
    return false;
  }

  @Override
  public boolean isCurrency(int column) {
    return false;
  }

  @Override
  public int isNullable(int column) {
    return 0;
  }

  @Override
  public boolean isSigned(int column) {
    return false;
  }

  @Override
  public String getColumnLabel(int column) {
    return null;
  }

  @Override
  public String getColumnName(int column) {
    return null;
  }

  @Override
  public String getSchemaName(int column) {
    return null;
  }

  @Override
  public String getTableName(int column) {
    return null;
  }

  @Override
  public String getCatalogName(int column) {
    return null;
  }

  @Override
  public String getColumnTypeName(int column) {
    return null;
  }

  @Override
  public boolean isReadOnly(int column) {
    return false;
  }

  @Override
  public boolean isWritable(int column) {
    return false;
  }

  @Override
  public boolean isDefinitelyWritable(int column) {
    return false;
  }

  @Override
  public String getColumnClassName(int column) {
    return null;
  }
}
