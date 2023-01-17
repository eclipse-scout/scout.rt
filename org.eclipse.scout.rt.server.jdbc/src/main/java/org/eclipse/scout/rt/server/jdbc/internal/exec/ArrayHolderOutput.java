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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.holders.HolderUtility;
import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueOutputToken;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;

class ArrayHolderOutput implements IBindOutput {
  private final IHolder<?> m_holder;
  private final ValueOutputToken m_source;
  private final List<Object> m_accumulator = new ArrayList<>();
  private int m_batchIndex = -1;
  private int m_jdbcBindIndex = -1;

  public ArrayHolderOutput(IHolder<?> holder, ValueOutputToken source) {
    m_holder = holder;
    m_source = source;
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
    return m_holder.getHolderType().getComponentType();
  }

  @Override
  public void setNextBatchIndex(int i) {
    m_batchIndex = i;
  }

  public int getBatchIndex() {
    return m_batchIndex;
  }

  @Override
  public void finishBatch() {
    HolderUtility.setAndCastValue(m_holder, m_accumulator.toArray());
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
    return getClass().getSimpleName() + "[bindType=" + getBindType() + ", source=" + m_source + "]";
  }

}
