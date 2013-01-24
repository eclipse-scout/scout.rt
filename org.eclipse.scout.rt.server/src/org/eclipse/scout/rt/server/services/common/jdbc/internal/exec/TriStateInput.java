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

import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.parsers.token.IToken;
import org.eclipse.scout.commons.parsers.token.ValueInputToken;
import org.eclipse.scout.rt.server.services.common.jdbc.SqlBind;
import org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle;

class TriStateInput implements IBindInput {
  private IBindInput m_delegate;

  public TriStateInput(TriState ts, ValueInputToken target) throws ProcessingException {
    if (ts == null || ts.isUndefined()) {
      m_delegate = new ArrayInput(new int[]{0, 1}, target);
    }
    else {
      m_delegate = new SingleInput(ts.getBooleanValue(), Boolean.class, target);
    }
  }

  @Override
  public IToken getToken() {
    return m_delegate.getToken();
  }

  @Override
  public boolean isBatch() {
    return m_delegate.isBatch();
  }

  @Override
  public boolean hasBatch(int i) {
    return m_delegate.hasBatch(i);
  }

  @Override
  public void setNextBatchIndex(int i) {
    m_delegate.setNextBatchIndex(i);
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
  public SqlBind produceSqlBindAndSetReplaceToken(ISqlStyle sqlStyle) throws ProcessingException {
    return m_delegate.produceSqlBindAndSetReplaceToken(sqlStyle);
  }

}
