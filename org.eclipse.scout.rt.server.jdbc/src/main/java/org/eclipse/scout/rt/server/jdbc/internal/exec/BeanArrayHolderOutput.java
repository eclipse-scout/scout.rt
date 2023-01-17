/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jdbc.internal.exec;

import org.eclipse.scout.rt.platform.holders.IBeanArrayHolder;
import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueOutputToken;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;

class BeanArrayHolderOutput implements IBindOutput {
  private final IBeanArrayHolder m_holder;
  private final AbstractBeanPropertyOutput m_delegate;

  public BeanArrayHolderOutput(IBeanArrayHolder holder, String propertyName, ValueOutputToken source) {
    m_holder = holder;
    m_delegate = new AbstractBeanPropertyOutput(m_holder.getHolderType(), propertyName, source) {
      @Override
      protected Object[] getFinalBeanArray() {
        return m_holder.getBeans();
      }
    };
  }

  @Override
  public IToken getToken() {
    return m_delegate.getToken();
  }

  @Override
  public boolean isJdbcBind() {
    return m_delegate.isJdbcBind();
  }

  @Override
  public int getJdbcBindIndex() {
    return m_delegate.getJdbcBindIndex();
  }

  @Override
  public void setJdbcBindIndex(int index) {
    m_delegate.setJdbcBindIndex(index);
  }

  @Override
  public boolean isBatch() {
    return m_delegate.isBatch();
  }

  @Override
  public boolean isSelectInto() {
    return m_delegate.isSelectInto();
  }

  @Override
  public Class getBindType() {
    return m_delegate.getBindType();
  }

  @Override
  public void setNextBatchIndex(int i) {
    m_delegate.setNextBatchIndex(i);
  }

  @Override
  public void finishBatch() {
    m_holder.ensureSize(m_delegate.getBatchIndex() + 1);
    m_delegate.finishBatch();
  }

  @Override
  public void setReplaceToken(ISqlStyle style) {
    m_delegate.setReplaceToken(style);
  }

  @Override
  public void consumeValue(Object value) {
    m_delegate.consumeValue(value);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[delegate=" + m_delegate + "]";
  }

}
