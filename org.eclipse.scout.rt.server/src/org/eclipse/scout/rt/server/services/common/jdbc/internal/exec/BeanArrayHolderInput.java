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
import org.eclipse.scout.commons.parsers.token.IToken;
import org.eclipse.scout.commons.parsers.token.ValueInputToken;
import org.eclipse.scout.rt.server.services.common.jdbc.SqlBind;
import org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle;

class BeanArrayHolderInput implements IBindInput {
  private IBeanArrayHolder m_holder;
  private BeanPropertyInput m_delegate;

  public BeanArrayHolderInput(IBeanArrayHolder holder, Object[] beans, String propertyName, ValueInputToken target) throws ProcessingException {
    m_holder = holder;
    if (beans == null) {
      beans = m_holder.getBeans();
    }
    m_delegate = new BeanPropertyInput(propertyName, beans, target);
  }

  public IToken getToken() {
    return m_delegate.getToken();
  }

  public boolean isBatch() {
    return m_delegate.isBatch();
  }

  public boolean hasBatch(int i) {
    return m_delegate.hasBatch(i);
  }

  public void setNextBatchIndex(int i) {
    m_delegate.setNextBatchIndex(i);
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

  public SqlBind produceSqlBindAndSetReplaceToken(ISqlStyle sqlStyle) throws ProcessingException {
    return m_delegate.produceSqlBindAndSetReplaceToken(sqlStyle);
  }

}
