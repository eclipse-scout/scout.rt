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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.rt.server.jdbc.AbstractSqlService;
import org.eclipse.scout.rt.server.jdbc.ISqlService;
import org.eclipse.scout.rt.server.jdbc.SqlBind;
import org.eclipse.scout.rt.server.jdbc.parsers.token.FunctionInputToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;

class FunctionInput implements IBindInput {
  private ISqlService m_callerService;
  private Object[] m_bindBases;
  //
  private int m_batchIndex = -1;
  private int m_jdbcBindIndex = -1;
  private Object m_value;
  private boolean m_valueSet;
  private FunctionInputToken m_target;

  public FunctionInput(ISqlService callerService, Object[] bindBases, FunctionInputToken target) {
    m_callerService = callerService;
    m_bindBases = bindBases;
    m_target = target;
  }

  @Override
  public IToken getToken() {
    return m_target;
  }

  @Override
  public boolean isBatch() {
    return false;
  }

  @Override
  public boolean hasBatch(int i) {
    return i <= 0;
  }

  @Override
  public void setNextBatchIndex(int i) {
    m_batchIndex = i;
  }

  @Override
  public boolean isJdbcBind(ISqlStyle sqlStyle) {
    if (m_target.isPlainToken()) {
      return false;
    }
    else if (m_target.isPlainValue()) {
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
    if (isBatch() || !m_valueSet) {
      if (m_callerService instanceof AbstractSqlService) {
        m_value = ((AbstractSqlService) m_callerService).callbackCustomBindFunction(m_target.getName(), m_target.getArgs(), m_bindBases);
      }
      else {
        throw new ProcessingException("don't know how to resolve custom bind function '" + m_target.getName() + "' on service " + m_callerService.getClass().getName());
      }
      m_valueSet = true;
    }
    Object value = null;
    if (isBatch() && m_batchIndex >= 1) {
      value = null;
    }
    else {
      value = m_value;
    }
    Class<?> nullType = null;
    if (value instanceof IHolder<?>) {
      IHolder h = (IHolder<?>) value;
      value = h.getValue();
      nullType = h.getHolderType();
    }
    //
    if (m_target.isPlainToken()) {
      m_target.setReplaceToken(m_target.getParsedToken());
      return null;
    }
    else if (m_target.isPlainValue()) {
      m_target.setReplaceToken(sqlStyle.toPlainText(value));
      return null;
    }
    else if (m_target.isPlainSql()) {
      m_target.setReplaceToken("" + value);
      return null;
    }
    else {
      m_target.setReplaceToken("?");
      return sqlStyle.buildBindFor(value, nullType);
    }
  }

}
