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

import java.util.List;

import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.rest.data.IDataObjectWithId;

public abstract class AbstractDataResult<T extends IDataObjectWithId> extends AbstractResult {
  private static final long serialVersionUID = 1L;

  public static final int INITIAL_RESULT_CHUNK_ID = -1; // do not change the value of this constant!

  private int m_totalItemCount;
  private int m_chunkSize;
  private int m_totalChunkCount;
  private int m_chunkId;
  private ChunkRange m_chunkRange;
  private boolean m_hasMoreData;
  private List<T> m_items;

  public int getTotalItemCount() {
    return m_totalItemCount;
  }

  public void setTotalItemCount(int totalResultCount) {
    m_totalItemCount = totalResultCount;
  }

  public int getChunkSize() {
    return m_chunkSize;
  }

  public void setChunkSize(int chunkSize) {
    m_chunkSize = chunkSize;
  }

  public int getTotalChunkCount() {
    return m_totalChunkCount;
  }

  public void setTotalChunkCount(int totalChunkCount) {
    m_totalChunkCount = totalChunkCount;
  }

  public int getChunkId() {
    return m_chunkId;
  }

  public void setChunkId(int chunkId) {
    m_chunkId = chunkId;
  }

  public ChunkRange getChunkRange() {
    return m_chunkRange;
  }

  public void setChunkRange(ChunkRange chunkRange) {
    m_chunkRange = chunkRange;
  }

  public boolean isHasMoreData() {
    return m_hasMoreData;
  }

  public void setHasMoreData(boolean hasMoreData) {
    m_hasMoreData = hasMoreData;
  }

  public List<T> getItems() {
    return m_items;
  }

  public void setItems(List<T> results) {
    m_items = results;
  }

  @Override
  protected void interceptToStringBuilder(ToStringBuilder builder) {
    builder
        .attr("queryId", getQueryId())
        .attr("totalResultCount", m_totalItemCount)
        .attr("chunkSize", m_chunkSize)
        .attr("totalChunkCount", m_totalChunkCount)
        .attr("chunkId", m_chunkId)
        .attr("chunkRange", m_chunkRange)
        .attr("hasMoreData", m_hasMoreData)
        .attr("items", (m_items == null ? "null" : "List(" + m_items.size() + ")"));
  }
}
