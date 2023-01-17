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

import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.server.jdbc.SqlBind;
import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueInputToken;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;

class TriStateInput implements IBindInput {
  private final IBindInput m_delegate;

  public TriStateInput(ISqlStyle sqlStyle, TriState ts, ValueInputToken target) {
    if (ts == null || ts.isUndefined()) {
      m_delegate = new ArrayInput(sqlStyle, new int[]{0, 1}, target);
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
  public boolean isJdbcBind(ISqlStyle sqlStyle) {
    return m_delegate.isJdbcBind(sqlStyle);
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
  public SqlBind produceSqlBindAndSetReplaceToken(ISqlStyle sqlStyle) {
    return m_delegate.produceSqlBindAndSetReplaceToken(sqlStyle);
  }

}
