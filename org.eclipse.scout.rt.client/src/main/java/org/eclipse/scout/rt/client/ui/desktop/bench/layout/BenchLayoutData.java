package org.eclipse.scout.rt.client.ui.desktop.bench.layout;

import java.util.Arrays;

/**
 * <h3>{@link BenchLayoutData}</h3>
 *
 * @author aho
 */
public class BenchLayoutData {
  public static final int WEST = 0;
  public static final int CENTER = 1;
  public static final int EAST = 2;

  private String m_cacheKey;
  private BenchColumnData[] m_columns = {
      new BenchColumnData(),
      new BenchColumnData(),
      new BenchColumnData(),
  };

  public BenchLayoutData withCacheKey(String cacheKey) {
    m_cacheKey = cacheKey;
    return this;
  }

  public String getCacheKey() {
    return m_cacheKey;
  }

  public BenchColumnData[] getColumns() {
    return m_columns;
  }

  public BenchLayoutData withWest(BenchColumnData data) {
    m_columns[WEST] = data;
    return this;
  }

  public BenchColumnData getWest() {
    return m_columns[WEST];
  }

  public BenchLayoutData withCenter(BenchColumnData data) {
    m_columns[CENTER] = data;
    return this;
  }

  public BenchColumnData getCenter() {
    return m_columns[CENTER];
  }

  public BenchLayoutData withEast(BenchColumnData data) {
    m_columns[EAST] = data;
    return this;
  }

  public BenchColumnData getEast() {
    return m_columns[EAST];
  }

  public BenchLayoutData copy() {
    return copyValues(new BenchLayoutData());
  }

  protected BenchLayoutData copyValues(BenchLayoutData copy) {
    copy.withCacheKey(getCacheKey());
    if (getCenter() != null) {
      copy.withCenter(getCenter().copy());
    }
    if (getEast() != null) {
      copy.withEast(getEast().copy());
    }
    if (getWest() != null) {
      copy.withWest(getWest().copy());
    }
    return copy;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_cacheKey == null) ? 0 : m_cacheKey.hashCode());
    result = prime * result + Arrays.hashCode(m_columns);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BenchLayoutData other = (BenchLayoutData) obj;
    if (m_cacheKey == null) {
      if (other.m_cacheKey != null) {
        return false;
      }
    }
    else if (!m_cacheKey.equals(other.m_cacheKey)) {
      return false;
    }
    if (!Arrays.equals(m_columns, other.m_columns)) {
      return false;
    }
    return true;
  }

}
