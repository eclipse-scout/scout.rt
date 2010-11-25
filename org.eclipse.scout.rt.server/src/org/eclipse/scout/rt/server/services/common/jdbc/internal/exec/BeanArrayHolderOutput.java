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
import org.eclipse.scout.commons.holders.IBeanArrayHolder;
import org.eclipse.scout.rt.server.services.common.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.services.common.jdbc.parsers.token.ValueOutputToken;
import org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle;

class BeanArrayHolderOutput implements IBindOutput {
  private IBeanArrayHolder m_holder;
  private AbstractBeanPropertyOutput m_delegate;

  public BeanArrayHolderOutput(IBeanArrayHolder holder, String propertyName, ValueOutputToken source) throws ProcessingException {
    m_holder = holder;
    m_delegate = new AbstractBeanPropertyOutput(m_holder.getHolderType(), propertyName, source) {
      @Override
      protected Object[] getFinalBeanArray() {
        return m_holder.getBeans();
      }
    };
  }

  public IToken getToken() {
    return m_delegate.getToken();
  }

  public boolean isJdbcBind() {
    return m_delegate.isJdbcBind();
  }

  public int getJdbcBindIndex() {
    return m_delegate.getJdbcBindIndex();
  }

  public void setJdbcBindIndex(int index) {
    m_delegate.setJdbcBindIndex(index);
  }

  public boolean isBatch() {
    return m_delegate.isBatch();
  }

  public boolean isSelectInto() {
    return m_delegate.isSelectInto();
  }

  public Class getBindType() {
    return m_delegate.getBindType();
  }

  public void setNextBatchIndex(int i) {
    m_delegate.setNextBatchIndex(i);
  }

  public void finishBatch() throws ProcessingException {
    m_holder.ensureSize(m_delegate.getBatchIndex() + 1);
    m_delegate.finishBatch();
  }

  public void setReplaceToken(ISqlStyle style) {
    m_delegate.setReplaceToken(style);
  }

  public void consumeValue(Object value) throws ProcessingException {
    m_delegate.consumeValue(value);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[delegate=" + m_delegate + "]";
  }

}
