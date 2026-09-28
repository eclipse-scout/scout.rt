/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.bench.layout;

/**
 * The layout data for a FlexboxLayout (JS).
 */
public class FlexboxLayoutData {

  private double m_initial = 100.0 / 3.0;
  private boolean m_relative = true;
  private double m_grow = 1;
  private double m_shrink = 1;
  private double m_min = -1;
  private double m_max = -1;


  /**
   * If the part has a relative layout data (see {@link FlexboxLayoutData#isRelative()}) the initial value describes the
   * size in relation to relative sibling parts. If the part is not relative the initial value is an absolute pixel
   * value.
   *
   * @param initial
   *     the initial size for the part.
   * @see FlexboxLayoutData#withRelative(boolean)
   */
  public FlexboxLayoutData withInitial(double initial) {
    m_initial = initial;
    return this;
  }

  /**
   * @see FlexboxLayoutData#withInitial(double)
   */
  public double getInitial() {
    return m_initial;
  }

  /**
   * Use relative to evaluate the size (initial) against other relative datas. All absolute parts are subtracted from the
   * total space to distribute. The rest is distributed to all relative parts relative to their initial sizes.<br>
   * E.g.<br>
   *
   * <pre>
   * Total Size: 600px
   * Part 1: relative= true, initial=2; => calculated size: 200px
   * Part 2: relative= false, initial = 300; => calculated size: 300px
   * Part 3: relative = true, initial = 1; => calculated size: 100px
   * </pre>
   *
   * @param relative
   *     true for relative size false for absolute (pixel) size
   * @return this fluent api
   */
  public FlexboxLayoutData withRelative(boolean relative) {
    m_relative = relative;
    return this;
  }

  /**
   * @see FlexboxLayoutData#withRelative(boolean)
   */
  public boolean isRelative() {
    return m_relative;
  }

  /**
   * The weight for growing. If the initial size is reached and there is still some space to distribute it will be
   * distributed to all parts considering their grow values.
   *
   * @param grow
   *     0 for not growing, > 0 for growing in relative to other parts grow values.
   * @return this fluent api
   */
  public FlexboxLayoutData withGrow(double grow) {
    m_grow = grow;
    return this;
  }

  /**
   * @see FlexboxLayoutData#withGrow(double)
   */
  public double getGrow() {
    return m_grow;
  }

  /**
   * The weight for shrinking. If the part should shrink below its initial size the shrink value says if and with what
   * relation it should shrink in relative to other parts.
   *
   * @param shrink
   *     0 for not shrinking, > 0 for shrinking in relative to other parts shrink values.
   * @return this fluent api
   */
  public FlexboxLayoutData withShrink(double shrink) {
    m_shrink = shrink;
    return this;
  }

  /**
   * @see FlexboxLayoutData#withShrink(double)
   */
  public double getShrink() {
    return m_shrink;
  }

  /**
   * The minimal size in pixels for this part. Should be smaller than the initial and max size.
   * By default, the minimum is unlimited ({@code -1}).
   *
   * @param min
   *     minimum size, -1 for unlimited
   * @return this fluent api
   */
  public FlexboxLayoutData withMin(double min) {
    m_min = min;
    return this;
  }

  /**
   * @see #withMin(double)
   */
  public double getMin() {
    return m_min;
  }

  /**
   * The maximal size in pixels for this part. Should be larger than the initial and min size.
   * By default, the maximum is unlimited ({@code -1}).
   *
   * @param max
   *     maximum size, -1 for unlimited
   * @return this fluent api
   */
  public FlexboxLayoutData withMax(double max) {
    m_max = max;
    return this;
  }

  /**
   * @see #withMax(double)
   */
  public double getMax() {
    return m_max;
  }

  public FlexboxLayoutData copy() {
    return copyValues(new FlexboxLayoutData());
  }

  protected FlexboxLayoutData copyValues(FlexboxLayoutData copy) {
    copy.withInitial(getInitial())
        .withRelative(isRelative())
        .withGrow(getGrow())
        .withShrink(getShrink())
        .withMin(getMin())
        .withMax(getMax());
    return copy;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Double.hashCode(m_initial);
    result = prime * result + (m_relative ? 1231 : 1237);
    result = prime * result + Double.hashCode(m_grow);
    result = prime * result + Double.hashCode(m_shrink);
    result = prime * result + Double.hashCode(m_min);
    result = prime * result + Double.hashCode(m_max);
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
    FlexboxLayoutData other = (FlexboxLayoutData) obj;
    if (Double.doubleToLongBits(m_initial) != Double.doubleToLongBits(other.m_initial)) {
      return false;
    }
    if (m_relative != other.m_relative) {
      return false;
    }
    if (Double.doubleToLongBits(m_grow) != Double.doubleToLongBits(other.m_grow)) {
      return false;
    }
    if (Double.doubleToLongBits(m_shrink) != Double.doubleToLongBits(other.m_shrink)) {
      return false;
    }
    if (Double.doubleToLongBits(m_min) != Double.doubleToLongBits(other.m_min)) {
      return false;
    }
    if (Double.doubleToLongBits(m_max) != Double.doubleToLongBits(other.m_max)) {
      return false;
    }
    return true;
  }
}
