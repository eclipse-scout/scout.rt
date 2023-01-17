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

import java.util.Map;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueOutputToken;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;

class MapOutput implements IBindOutput {
  private final Map<Object, Object> m_map;
  private final Object m_mapKey;
  private final ValueOutputToken m_source;
  private int m_batchIndex = -1;
  private int m_jdbcBindIndex = -1;
  private Object m_accumulator;

  public MapOutput(Map<Object, Object> map, Object mapKey, ValueOutputToken source) {
    m_map = map;
    m_mapKey = mapKey;
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
  public void setNextBatchIndex(int i) {
    m_batchIndex = i;
  }

  @Override
  public Class getBindType() {
    Object o = m_map.get(m_mapKey);
    return o != null ? o.getClass() : Object.class;
  }

  @Override
  public void finishBatch() {
    m_map.put(m_mapKey, m_accumulator);
  }

  @Override
  public void setReplaceToken(ISqlStyle style) {
    m_source.setReplaceToken("?");
  }

  @Override
  public void consumeValue(Object value) {
    if (m_batchIndex == 0) {
      m_accumulator = value;
    }
    else {
      // ticket 83809
      throw new ProcessingException("expected a single value for map key \"" + m_source.getParsedToken() + "\" but got multiple values");
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[bindType=" + getBindType() + ", key=" + m_mapKey + ", token=" + m_source + "]";
  }

}
