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

import java.lang.reflect.Array;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.jdbc.SqlBind;
import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueInputToken;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;

class ArrayInput implements IBindInput {
  private Object m_array;

  private ValueInputToken m_target;
  private int m_arrayLen = 0;
  private int m_batchIndex = -1;
  private int m_jdbcBindIndex = -1;

  public ArrayInput(ISqlStyle sqlStyle, Object array, ValueInputToken target) {
    if (array != null && !array.getClass().isArray()) {
      throw new ProcessingException("array parameter must be an array type: " + array.getClass());
    }
    m_array = array;
    m_arrayLen = m_array != null ? Array.getLength(m_array) : 0;
    m_target = target;
    // initialize target
    if (isBatch()) {
    }
    else {
      if (!sqlStyle.isCreatingInListGeneratingBind(m_array)) {
        m_target.setPlainValue(true);
      }
      if (m_target.isPlainValue() || m_target.isPlainSql()) {
        // if the op is = or <> change it to IN or NOT IN
        if ("=".equals(m_target.getParsedOp())) {
          m_target.setParsedOp("IN");
        }
        else if ("!=".equals(m_target.getParsedOp()) || "<>".equals(m_target.getParsedOp())) {
          m_target.setParsedOp("NOT IN");
        }
      }
    }
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
    if (isBatch()) {
      return i < m_arrayLen;
    }
    else {
      return i <= 0;
    }
  }

  @Override
  public void setNextBatchIndex(int i) {
    m_batchIndex = i;
  }

  @Override
  public boolean isJdbcBind(ISqlStyle sqlStyle) {
    if (isBatch()) {
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
    else {
      return m_target.getParsedAttribute() != null && !m_target.isPlainSql() && !m_target.isPlainValue() && sqlStyle.isCreatingInListGeneratingBind(m_array);
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
    if (isBatch()) {
      Object value = null;
      if (m_batchIndex < m_arrayLen) {
        value = Array.get(m_array, m_batchIndex);
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
        return sqlStyle.buildBindFor(value, m_array.getClass().getComponentType());
      }
    }
    else {
      return applyMultiValued(sqlStyle);
    }
  }

  private SqlBind applyMultiValued(ISqlStyle sqlStyle) {
    if (m_target.getParsedAttribute() != null) {
      boolean plain = m_target.isPlainSql() || m_target.isPlainValue();
      String att = m_target.getParsedAttribute();
      String op = m_target.getParsedOp();
      m_target.setParsedAttribute(null);
      m_target.setParsedOp(null);
      if (op.equalsIgnoreCase("IN") || op.equalsIgnoreCase("=")) {
        m_target.setReplaceToken(sqlStyle.createInList(att, plain, m_array));
      }
      else {
        m_target.setReplaceToken(sqlStyle.createNotInList(att, plain, m_array));
      }
      if (!plain && sqlStyle.isCreatingInListGeneratingBind(m_array)) {
        return sqlStyle.buildBindFor(m_array, null);
      }
    }
    else {
      m_target.setReplaceToken(sqlStyle.toPlainText(m_array));
    }
    return null;
  }

}
