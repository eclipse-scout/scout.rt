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
package org.eclipse.scout.rt.server.jdbc.internal.exec;

import java.lang.reflect.Method;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.holders.ITableBeanHolder;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueOutputToken;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;

class TableBeanHolderOutput implements IBindOutput {
  private ITableBeanHolder m_holder;
  private Method m_getterMethod;
  private Method m_setterMethod;
  private Class m_beanType;
  private ValueOutputToken m_source;
  private int m_batchIndex = -1;
  private int m_jdbcBindIndex = -1;

  public TableBeanHolderOutput(ITableBeanHolder holder, String columnName, ValueOutputToken source) {
    m_holder = holder;
    try {
      m_getterMethod = m_holder.getRowType().getMethod("get" + Character.toUpperCase(columnName.charAt(0)) + columnName.substring(1));
      m_beanType = m_getterMethod.getReturnType();
      m_setterMethod = m_holder.getRowType().getMethod("set" + Character.toUpperCase(columnName.charAt(0)) + columnName.substring(1), new Class[]{m_beanType});
    }
    catch (NoSuchMethodException | SecurityException e) {
      throw new ProcessingException("unexpected exception", e);
    }
    m_source = source;
  }

  @Override
  public IToken getToken() {
    return m_source;
  }

  @Override
  public boolean isJdbcBind() {
    return !m_source.isSelectInto();
  }

  @Override
  public int getJdbcBindIndex() {
    return m_jdbcBindIndex;
  }

  @Override
  public void setJdbcBindIndex(int index) {
    m_jdbcBindIndex = index;
  }

  @Override
  public boolean isBatch() {
    return true;
  }

  @Override
  public boolean isSelectInto() {
    return m_source.isSelectInto();
  }

  @Override
  public Class getBindType() {
    return m_getterMethod.getReturnType();
  }

  @Override
  public void setNextBatchIndex(int i) {
    m_batchIndex = i;
  }

  @Override
  public void finishBatch() {
    ensureSize(m_holder, m_batchIndex + 1);
  }

  @Override
  public void setReplaceToken(ISqlStyle style) {
    m_source.setReplaceToken("?");
  }

  @Override
  @SuppressWarnings("unchecked")
  public void consumeValue(Object value) {
    ensureSize(m_holder, m_batchIndex + 1);
    try {
      Object castValue = TypeCastUtility.castValue(value, m_beanType);
      m_setterMethod.invoke(m_holder.getRows()[m_batchIndex], new Object[]{castValue});
    }
    catch (ReflectiveOperationException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[bindType=" + getBindType() + ", source=" + m_source + "]";
  }

  private static void ensureSize(ITableBeanHolder table, int size) {
    while (table.getRowCount() < size) {
      table.addRow();
    }
    while (table.getRowCount() > size) {
      table.removeRow(table.getRowCount() - 1);
    }
  }

}
