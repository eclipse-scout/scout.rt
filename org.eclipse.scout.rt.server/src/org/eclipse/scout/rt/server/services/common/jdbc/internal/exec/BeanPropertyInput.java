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

import org.eclipse.scout.commons.BeanUtility;
import org.eclipse.scout.commons.beans.FastPropertyDescriptor;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.parsers.token.IToken;
import org.eclipse.scout.commons.parsers.token.ValueInputToken;
import org.eclipse.scout.rt.server.services.common.jdbc.SqlBind;
import org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle;

class BeanPropertyInput implements IBindInput {
  private String m_propertyName;
  private FastPropertyDescriptor m_propertyDesc;
  private Object[] m_beans;
  private ValueInputToken m_target;
  private int m_batchIndex = -1;
  private int m_jdbcBindIndex = -1;

  public BeanPropertyInput(String propertyName, Object[] beans, ValueInputToken target) throws ProcessingException {
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
    if (isBatch()) {
    }
    else if (m_beans.length >= 2) {
      m_target.setPlainValue(true);
      // if the op is = or <> change it to IN or NOT IN
      if (m_target.getParsedOp() != null) {
        if (m_target.getParsedOp().equals("=")) {
          m_target.setParsedOp("IN");
        }
        else if (m_target.getParsedOp().equals("<>")) {
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
  public boolean isJdbcBind() {
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
      return false;
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
  public SqlBind produceSqlBindAndSetReplaceToken(ISqlStyle sqlStyle) throws ProcessingException {
    if (isBatch()) {
      Object value = null;
      Class valueType = m_propertyDesc != null ? m_propertyDesc.getPropertyType() : null;
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
      Object[] values = new Object[m_beans.length];
      for (int i = 0; i < values.length; i++) {
        Object bean = m_beans[i];
        if (bean != null && m_propertyDesc != null) {
          try {
            values[i] = m_propertyDesc.getReadMethod().invoke(bean);
          }
          catch (Exception e) {
            throw new ProcessingException("property " + m_propertyName, e);
          }
        }
      }
      if (m_target.getParsedAttribute() != null) {
        String att = m_target.getParsedAttribute();
        String op = m_target.getParsedOp();
        m_target.setParsedAttribute(null);
        m_target.setParsedOp(null);
        if (op.equalsIgnoreCase("IN") || op.equalsIgnoreCase("=")) {
          m_target.setReplaceToken(sqlStyle.createInList(att, values));
        }
        else {
          m_target.setReplaceToken(sqlStyle.createNotInList(att, values));
        }
      }
      else {
        m_target.setReplaceToken(sqlStyle.toPlainText(values));
      }
      return null;
    }
  }

}
