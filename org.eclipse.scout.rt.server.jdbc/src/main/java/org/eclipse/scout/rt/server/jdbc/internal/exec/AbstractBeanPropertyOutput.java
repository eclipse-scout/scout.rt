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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.reflect.FastPropertyDescriptor;
import org.eclipse.scout.rt.platform.util.BeanUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueOutputToken;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;

abstract class AbstractBeanPropertyOutput implements IBindOutput {
  private final String m_propertyName;
  private final Class m_propertyType;
  private final ValueOutputToken m_source;
  private int m_batchIndex = -1;
  private int m_jdbcBindIndex = -1;
  private final List<Object> m_accumulator;

  public AbstractBeanPropertyOutput(Class beanType, String propertyName, ValueOutputToken source) {
    m_propertyName = propertyName;
    m_accumulator = new ArrayList<>();
    m_source = source;
    //
    try {
      FastPropertyDescriptor desc = BeanUtility.getFastBeanInfo(beanType, null).getPropertyDescriptor(m_propertyName);
      m_propertyType = desc.getPropertyType();
    }
    catch (Exception e) {
      throw new ProcessingException("property " + m_propertyName, e);
    }
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
    return m_source.isBatch();
  }

  @Override
  public boolean isSelectInto() {
    return m_source.isSelectInto();
  }

  @Override
  public Class getBindType() {
    return m_propertyType;
  }

  public int getBatchIndex() {
    return m_batchIndex;
  }

  @Override
  public void setNextBatchIndex(int i) {
    m_batchIndex = i;
  }

  /**
   * called from {@link AbstractBeanPropertyOutput#finishBatch()}
   */
  protected abstract Object[] getFinalBeanArray();

  @Override
  public void finishBatch() {
    FastPropertyDescriptor desc = null;
    Object[] beans = getFinalBeanArray();
    if (beans != null) {
      int accSize = m_accumulator.size();
      for (int i = 0; i < beans.length; i++) {
        try {
          Object bean = beans[i];
          if (bean != null) {
            if (desc == null) {
              desc = BeanUtility.getFastBeanInfo(bean.getClass(), null).getPropertyDescriptor(m_propertyName);
            }
            Object value = null;
            if (i < accSize) {
              value = m_accumulator.get(i);
            }
            if (IHolder.class.isAssignableFrom(desc.getPropertyType())) {
              @SuppressWarnings("unchecked")
              IHolder<Object> h = (IHolder<Object>) desc.getReadMethod().invoke(bean);
              if (h != null) {
                Object castValue = TypeCastUtility.castValue(value, h.getHolderType());
                h.setValue(castValue);
              }
            }
            else {
              Object castValue = TypeCastUtility.castValue(value, desc.getPropertyType());
              desc.getWriteMethod().invoke(bean, castValue);
            }
          }
        }
        catch (Exception e) {
          throw new ProcessingException("property " + m_propertyName, e);
        }
      }
    }
  }

  @Override
  public void setReplaceToken(ISqlStyle style) {
    m_source.setReplaceToken("?");
  }

  @Override
  public void consumeValue(Object value) {
    m_accumulator.add(value);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[bindType=" + getBindType() + ", name=" + m_propertyName + ", source=" + m_source + "]";
  }

}
