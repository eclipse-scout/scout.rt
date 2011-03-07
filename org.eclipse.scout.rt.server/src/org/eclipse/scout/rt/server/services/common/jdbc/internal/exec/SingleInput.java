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
package org.eclipse.scout.rt.server.services.common.jdbc.internal.exec;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.parsers.token.IToken;
import org.eclipse.scout.commons.parsers.token.ValueInputToken;
import org.eclipse.scout.rt.server.services.common.jdbc.SqlBind;
import org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle;

class SingleInput implements IBindInput {
  private int m_batchIndex = -1;
  private int m_jdbcBindIndex = -1;
  private Object m_value;
  private Class m_nullType;
  private ValueInputToken m_target;

  public SingleInput(Object value, Class nullType, ValueInputToken target) {
    m_value = value;
    m_nullType = nullType;
    m_target = target;
  }

  public IToken getToken() {
    return m_target;
  }

  public boolean isBatch() {
    return m_target.isBatch();
  }

  public boolean hasBatch(int i) {
    return i <= 0;
  }

  public void setNextBatchIndex(int i) {
    m_batchIndex = i;
  }

  public boolean isJdbcBind() {
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

  public int getJdbcBindIndex() {
    return m_jdbcBindIndex;
  }

  public void setJdbcBindIndex(int index) {
    m_jdbcBindIndex = index;
  }

  public SqlBind produceSqlBindAndSetReplaceToken(ISqlStyle sqlStyle) throws ProcessingException {
    Object value = null;
    if ((!isBatch()) || m_batchIndex <= 0) {
      value = m_value;
    }
    //
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
      return sqlStyle.buildBindFor(value, m_nullType);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[value=" + m_value + ", token=" + m_target + "]";
  }
}
