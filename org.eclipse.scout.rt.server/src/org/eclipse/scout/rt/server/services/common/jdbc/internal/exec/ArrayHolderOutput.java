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

import java.util.ArrayList;

import org.eclipse.scout.commons.holders.HolderUtility;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.commons.parsers.token.IToken;
import org.eclipse.scout.commons.parsers.token.ValueOutputToken;
import org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle;

class ArrayHolderOutput implements IBindOutput {
  private IHolder<?> m_holder;
  private ValueOutputToken m_source;
  private ArrayList<Object> m_accumulator = new ArrayList<Object>();
  private int m_batchIndex = -1;
  private int m_jdbcBindIndex = -1;

  public ArrayHolderOutput(IHolder<?> holder, ValueOutputToken source) {
    m_holder = holder;
    m_source = source;
  }

  public IToken getToken() {
    return m_source;
  }

  public boolean isJdbcBind() {
    return !m_source.isSelectInto();
  }

  public int getJdbcBindIndex() {
    return m_jdbcBindIndex;
  }

  public void setJdbcBindIndex(int index) {
    m_jdbcBindIndex = index;
  }

  public boolean isBatch() {
    return m_source.isBatch();
  }

  public boolean isSelectInto() {
    return m_source.isSelectInto();
  }

  public Class getBindType() {
    return m_holder.getHolderType().getComponentType();
  }

  public void setNextBatchIndex(int i) {
    m_batchIndex = i;
  }

  public void finishBatch() {
    HolderUtility.setAndCastValue(m_holder, m_accumulator.toArray());
  }

  public void setReplaceToken(ISqlStyle style) {
    m_source.setReplaceToken("?");
  }

  public void consumeValue(Object value) {
    m_accumulator.add(value);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[bindType=" + getBindType() + ", source=" + m_source + "]";
  }

}
