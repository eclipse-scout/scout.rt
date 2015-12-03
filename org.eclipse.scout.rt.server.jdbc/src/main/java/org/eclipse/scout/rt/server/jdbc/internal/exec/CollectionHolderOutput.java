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
import java.util.Set;

import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueOutputToken;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;

class CollectionHolderOutput implements IBindOutput {
  private final IHolder<?> m_holder;
  private final ValueOutputToken m_source;
  private final List<Object> m_accumulator;

  private int m_batchIndex;
  private int m_jdbcBindIndex;

  public CollectionHolderOutput(IHolder<?> holder, ValueOutputToken source) {
    m_holder = holder;
    m_source = source;
    m_accumulator = new ArrayList<Object>();
    m_batchIndex = -1;
    m_jdbcBindIndex = -1;
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

  @SuppressWarnings("unchecked")
  private static <T> void setValueToHolder(IHolder<T> holder, List<Object> list) {
    Class<T> toType = holder.getHolderType();
    if (Set.class.isAssignableFrom(toType)) {
      T s = (T) CollectionUtility.hashSet(list);
      holder.setValue(s);
    }
    else {
      holder.setValue((T) list);
    }
  }

  @Override
  public void finishBatch() {
    setValueToHolder(m_holder, m_accumulator);
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
