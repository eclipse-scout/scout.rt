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

import org.eclipse.scout.rt.server.jdbc.SqlBind;
import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueInputToken;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;

class SingleInput implements IBindInput {
  private int m_batchIndex = -1;
  private int m_jdbcBindIndex = -1;
  private final Object m_value;
  private final Class m_nullType;
  private final ValueInputToken m_target;

  public SingleInput(Object value, Class nullType, ValueInputToken target) {
    m_value = value;
    m_nullType = nullType;
    m_target = target;
  }

  @Override
  public IToken getToken() {
    return m_target;
  }

  @Override
  public boolean isBatch() {
    return m_target.isBatch();
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
    if (m_target.isPlainValue()) {
      return false;
    }
    return !m_target.isPlainSql();
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
