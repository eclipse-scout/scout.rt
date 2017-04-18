package org.eclipse.scout.rt.client.ui.desktop.bench.layout;

import java.util.Arrays;

import org.eclipse.scout.rt.platform.util.Assertions;

/**
 * <h3>{@link BenchColumnData}</h3>
 *
 * @author aho
 */
public class BenchColumnData extends FlexboxLayoutData {
  public static final int NORTH = 0;
  public static final int CENTER = 1;
  public static final int SOUTH = 2;
  private FlexboxLayoutData[] m_rows = {
      new FlexboxLayoutData(),
      new FlexboxLayoutData(),
      new FlexboxLayoutData()
  };

  public FlexboxLayoutData[] getRows() {
    return m_rows;
  }

  /**
   * To provide a configuration of the north view stack. Null for default values.
   *
   * @param data
   * @return this fluent API
   */
  public BenchColumnData withNorth(FlexboxLayoutData data) {
    m_rows[NORTH] = data;
    return this;
  }

  /**
   * @see BenchColumnData#withNorth(FlexboxLayoutData)
   */
  public FlexboxLayoutData getNorth() {
    return m_rows[NORTH];
  }

  /**
   * To provide a configuration of the Center view stack. Null for default values.
   *
   * @param data
   * @return this fluent API
   */
  public BenchColumnData withCenter(FlexboxLayoutData data) {
    m_rows[CENTER] = data;
    return this;
  }

  /**
   * @see BenchColumnData#withCenter(FlexboxLayoutData)
   */
  public FlexboxLayoutData getCenter() {
    return m_rows[CENTER];
  }

  /**
   * To provide a configuration of the south view stack. Null for default values.
   *
   * @param data
   * @return this fluent API
   */
  public BenchColumnData withSouth(FlexboxLayoutData data) {
    m_rows[SOUTH] = data;
    return this;
  }

  /**
   * @see BenchColumnData#withSouth(FlexboxLayoutData)
   */
  public FlexboxLayoutData getSouth() {
    return m_rows[SOUTH];
  }

  @Override
  public BenchColumnData withInitial(double initial) {
    return (BenchColumnData) super.withInitial(initial);
  }

  @Override
  public BenchColumnData withRelative(boolean relative) {
    return (BenchColumnData) super.withRelative(relative);
  }

  @Override
  public BenchColumnData withGrow(double rise) {
    return (BenchColumnData) super.withGrow(rise);
  }

  @Override
  public BenchColumnData withShrink(double shrink) {
    return (BenchColumnData) super.withShrink(shrink);
  }

  @Override
  public BenchColumnData copy() {
    return (BenchColumnData) copyValues(new BenchColumnData());
  }

  @Override
  protected BenchColumnData copyValues(FlexboxLayoutData copyRaw) {
    Assertions.assertInstance(copyRaw, BenchColumnData.class);
    super.copyValues(copyRaw);
    BenchColumnData copy = (BenchColumnData) copyRaw;
    if (getSouth() != null) {
      copy.withSouth(getSouth().copy());
    }
    if (getCenter() != null) {
      copy.withCenter(getCenter().copy());
    }
    if (getNorth() != null) {
      copy.withNorth(getNorth().copy());
    }
    return copy;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(m_rows);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BenchColumnData other = (BenchColumnData) obj;
    if (!Arrays.equals(m_rows, other.m_rows)) {
      return false;
    }
    return true;
  }
}
