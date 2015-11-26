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
package org.eclipse.scout.rt.server.jdbc.internal.exec;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.ITableHolder;
import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueOutputToken;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;

class TableHolderOutput implements IBindOutput {
  private ITableHolder m_holder;
  private Method m_getterMethod;
  private Method m_setterMethod;
  private Class m_beanType;
  private ValueOutputToken m_source;
  private int m_batchIndex = -1;
  private int m_jdbcBindIndex = -1;

  public TableHolderOutput(ITableHolder holder, String columnName, ValueOutputToken source) {
    m_holder = holder;
    try {
      m_getterMethod = m_holder.getClass().getMethod("get" + Character.toUpperCase(columnName.charAt(0)) + columnName.substring(1), new Class[]{int.class});
      m_beanType = m_getterMethod.getReturnType();
      m_setterMethod = m_holder.getClass().getMethod("set" + Character.toUpperCase(columnName.charAt(0)) + columnName.substring(1), new Class[]{int.class, m_beanType});
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
    m_holder.ensureSize(m_batchIndex + 1);
  }

  @Override
  public void setReplaceToken(ISqlStyle style) {
    m_source.setReplaceToken("?");
  }

  @Override
  @SuppressWarnings("unchecked")
  public void consumeValue(Object value) {
    m_holder.ensureSize(m_batchIndex + 1);
    try {
      Object castValue = TypeCastUtility.castValue(value, m_beanType);
      m_setterMethod.invoke(m_holder, new Object[]{m_batchIndex, castValue});
    }
    catch (IllegalAccessException | InvocationTargetException e) {
      throw new ProcessingException("unexpected exception", e);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[bindType=" + getBindType() + ", source=" + m_source + "]";
  }

}
