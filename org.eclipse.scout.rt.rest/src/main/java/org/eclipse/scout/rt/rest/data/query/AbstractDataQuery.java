/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.data.query;

import java.util.Collection;

import org.eclipse.scout.rt.platform.util.ToStringBuilder;

public abstract class AbstractDataQuery extends AbstractQuery {

  public static final String PROP_CHUNK_SIZE = "chunkSize";
  public static final String PROP_CHUNK_RANGE = "chunkRange";
  public static final String PROP_IDS = "ids";
  private static final long serialVersionUID = 1L;

  private Integer m_chunkSize;
  private ChunkRange m_chunkRange;
  private Collection<String> m_ids;

  public Integer getChunkSize() {
    return m_chunkSize;
  }

  public void setChunkSize(Integer chunkSize) {
    m_chunkSize = chunkSize;
  }

  public ChunkRange getChunkRange() {
    return m_chunkRange;
  }

  public void setChunkRange(ChunkRange chunkRange) {
    m_chunkRange = chunkRange;
  }

  public Collection<String> getIds() {
    return m_ids;
  }

  public void setIds(Collection<String> ids) {
    m_ids = ids;
  }

  @Override
  public QueryBuilder addQueryParams(QueryBuilder builder) {
    return super.addQueryParams(builder)
        .queryParam(PROP_CHUNK_SIZE, m_chunkSize)
        .queryParam(PROP_CHUNK_RANGE, m_chunkRange)
        .queryParam(PROP_IDS, m_ids);
  }

  @Override
  protected void interceptToStringBuilder(ToStringBuilder builder) {
    super.interceptToStringBuilder(builder);
    builder
        .attr(PROP_CHUNK_SIZE, m_chunkSize)
        .attr(PROP_CHUNK_RANGE, m_chunkRange)
        .attr(PROP_IDS, m_ids);
  }
}
