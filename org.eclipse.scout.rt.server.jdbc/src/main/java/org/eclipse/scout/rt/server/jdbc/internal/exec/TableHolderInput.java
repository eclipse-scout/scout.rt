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
import org.eclipse.scout.rt.platform.holders.ITableHolder;
import org.eclipse.scout.rt.server.jdbc.SqlBind;
import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueInputToken;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;

class TableHolderInput implements IBindInput {
  private ITableHolder m_table;
  private int[] m_filteredRowIndices;
  private Method m_getterMethod;
  private ValueInputToken m_target;
  private int m_batchIndex = -1;
  private int m_jdbcBindIndex = -1;

  public TableHolderInput(ITableHolder table, int[] filteredRowIndices, String columnName, ValueInputToken target) {
    m_table = table;
    if (filteredRowIndices == null) {
      filteredRowIndices = new int[table.getRowCount()];
      for (int i = 0; i < filteredRowIndices.length; i++) {
        filteredRowIndices[i] = i;
      }
    }
    m_filteredRowIndices = filteredRowIndices;
    try {
      m_getterMethod = table.getClass().getMethod("get" + Character.toUpperCase(columnName.charAt(0)) + columnName.substring(1), new Class[]{int.class});
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
    return i < m_filteredRowIndices.length;
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
    if (m_batchIndex < m_filteredRowIndices.length) {
      try {
        value = m_getterMethod.invoke(m_table, new Object[]{new Integer(m_filteredRowIndices[m_batchIndex])});
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
