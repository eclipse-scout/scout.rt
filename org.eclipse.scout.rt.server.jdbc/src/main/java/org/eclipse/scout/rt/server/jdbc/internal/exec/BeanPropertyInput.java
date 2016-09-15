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

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.reflect.FastPropertyDescriptor;
import org.eclipse.scout.rt.platform.util.BeanUtility;
import org.eclipse.scout.rt.server.jdbc.SqlBind;
import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueInputToken;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;

class BeanPropertyInput implements IBindInput {
  private String m_propertyName;
  private FastPropertyDescriptor m_propertyDesc;
  private Object[] m_beans;
  private Object[] m_rawValues;
  private ValueInputToken m_target;
  private int m_batchIndex = -1;
  private int m_jdbcBindIndex = -1;

  public BeanPropertyInput(String propertyName, Object[] beans, ValueInputToken target) {
    if (beans == null) {
      beans = new Object[0];
    }
    m_propertyName = propertyName;
    m_beans = beans;
    m_target = target;
    try {
      if (m_propertyDesc == null) {
        FastPropertyDescriptor test = BeanUtility.getFastBeanInfo(m_beans.getClass().getComponentType(), null).getPropertyDescriptor(m_propertyName);
        if (test != null && test.getReadMethod() != null) {
          m_propertyDesc = test;
        }
      }
      if (m_propertyDesc == null) {
        for (Object bean : m_beans) {
          if (bean != null) {
            m_propertyDesc = BeanUtility.getFastBeanInfo(bean.getClass(), null).getPropertyDescriptor(m_propertyName);
            break;
          }
        }
      }
    }
    catch (Exception e) {
      throw new ProcessingException("property " + m_propertyName, e);
    }
    // initialize target
    if (!isBatch()
        && m_beans.length >= 2
        && m_target.getParsedOp() != null
        && (m_target.isPlainValue() || m_target.isPlainSql())) {

      if ("=".equals(m_target.getParsedOp())) {
        m_target.setParsedOp("IN");
      }
      else if ("!=".equals(m_target.getParsedOp()) || "<>".equals(m_target.getParsedOp())) {
        m_target.setParsedOp("NOT IN");
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
      return i < m_beans.length;
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
      return !m_target.isPlainSql();
    }
    else {
      return m_target.getParsedAttribute() != null && !m_target.isPlainSql() && !m_target.isPlainValue() && sqlStyle.isCreatingInListGeneratingBind(getRawValues());
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
      Class<?> valueType = m_propertyDesc != null ? m_propertyDesc.getPropertyType() : null;
      if (m_batchIndex < m_beans.length) {
        Object bean = m_beans[m_batchIndex];
        if (bean != null && m_propertyDesc != null) {
          try {
            value = m_propertyDesc.getReadMethod().invoke(bean);
          }
          catch (Exception e) {
            throw new ProcessingException("property " + m_propertyName, e);
          }
        }
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
        return sqlStyle.buildBindFor(value, valueType);
      }
    }
    else {
      return applyMultivalued(sqlStyle);
    }
  }

  private Object[] getRawValues() {
    if (m_rawValues != null) {
      return m_rawValues;
    }
    m_rawValues = new Object[m_beans.length];
    for (int i = 0; i < m_rawValues.length; i++) {
      Object bean = m_beans[i];
      if (bean != null && m_propertyDesc != null) {
        try {
          m_rawValues[i] = m_propertyDesc.getReadMethod().invoke(bean);
        }
        catch (Exception e) {
          throw new IllegalArgumentException("property " + m_propertyName, e);
        }
      }
    }
    return m_rawValues;
  }

  private SqlBind applyMultivalued(ISqlStyle sqlStyle) {
    Object[] values = getRawValues();
    if (m_target.getParsedAttribute() != null) {
      boolean plain = m_target.isPlainSql() || m_target.isPlainValue();
      String att = m_target.getParsedAttribute();
      String op = m_target.getParsedOp();
      m_target.setParsedAttribute(null);
      m_target.setParsedOp(null);
      if ("IN".equalsIgnoreCase(op) || "=".equalsIgnoreCase(op)) {
        m_target.setReplaceToken(sqlStyle.createInList(att, plain, values));
      }
      else {
        m_target.setReplaceToken(sqlStyle.createNotInList(att, plain, values));
      }
      if (!plain && sqlStyle.isCreatingInListGeneratingBind(values)) {
        return sqlStyle.buildBindFor(values, null);
      }
    }
    else {
      m_target.setReplaceToken(sqlStyle.toPlainText(values));
    }
    return null;
  }

}
