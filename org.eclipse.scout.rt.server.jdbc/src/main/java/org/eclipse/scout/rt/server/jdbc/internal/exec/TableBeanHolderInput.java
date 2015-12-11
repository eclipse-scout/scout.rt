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
import org.eclipse.scout.rt.platform.holders.ITableBeanRowHolder;
import org.eclipse.scout.rt.server.jdbc.SqlBind;
import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueInputToken;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;

class TableBeanHolderInput implements IBindInput {

  private ITableBeanHolder m_table;
  private ITableBeanRowHolder[] m_filteredRows;
  private Method m_getterMethod;
  private ValueInputToken m_target;
  private int m_batchIndex = -1;
  private int m_jdbcBindIndex = -1;

  public TableBeanHolderInput(ITableBeanHolder table, ITableBeanRowHolder[] filteredRows, String columnName, ValueInputToken target) {
    m_table = table;
    if (filteredRows == null) {
      m_filteredRows = m_table.getRows();
    }
    else {
      m_filteredRows = filteredRows;
    }
    try {
      m_getterMethod = table.getRowType().getMethod("get" + Character.toUpperCase(columnName.charAt(0)) + columnName.substring(1));
    }
    catch (NoSuchMethodException | SecurityException e) {
      throw new ProcessingException("unexpected exception", e);
    }
    m_target = target;
  }

  @Override
  public IToken getToken() {
    return m_target;
  }

  @Override
  public boolean isBatch() {
    return true;
  }

  @Override
  public boolean hasBatch(int i) {
    return i < m_filteredRows.length;
  }

  @Override
  public void setNextBatchIndex(int i) {
    m_batchIndex = i;
  }

  @Override
  public boolean isJdbcBind(ISqlStyle sqlStyle) {
    if (m_target.isPlainValue()) {
      return false;
    }
    else if (m_target.isPlainSql()) {
      return false;
    }
    else {
      return true;
    }
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
  public SqlBind produceSqlBindAndSetReplaceToken(ISqlStyle sqlStyle) {
    Object value = null;
    if (m_batchIndex < m_filteredRows.length) {
      try {
        value = m_getterMethod.invoke(m_filteredRows[m_batchIndex]);
      }
      catch (ReflectiveOperationException e) {
        throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
      }
    }

    if (m_target.isPlainValue()) {
      m_target.setReplaceToken(sqlStyle.toPlainText(value));
      return null;
    }
    else if (m_target.isPlainSql()) {
      m_target.setReplaceToken("" + value);
      return null;
    }
    else {
      m_target.setReplaceToken("?");
      return sqlStyle.buildBindFor(value, m_getterMethod.getReturnType());
    }
  }

}
