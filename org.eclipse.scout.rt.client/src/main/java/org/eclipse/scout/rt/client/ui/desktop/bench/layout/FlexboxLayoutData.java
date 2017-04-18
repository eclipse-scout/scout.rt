package org.eclipse.scout.rt.client.ui.desktop.bench.layout;

/**
 * <h3>{@link FlexboxLayoutData}</h3><br>
 * The layout data for a FlexboxLayout (JS).
 */
public class FlexboxLayoutData {
  private boolean m_relative = true;
  private double m_grow = 1;
  private double m_shrink = 1;
  private double m_initial = 100.0 / 3.0;

  /**
   * Use relative to evaluate the size (inital) against other relative datas. All absolute parts are subtracted from the
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
   *          true for relative size false for absolute (pixel) size
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
   *          0 for not growing, > 0 for growing in relative to other parts grow values.
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
   *          0 for not shrinking, > 0 for shrinking in relative to other parts shrink values.
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
   * If the part has a relative layout data (see {@link FlexboxLayoutData#isRelative()}) the initial value describes the
   * size in relation to relative sibling parts. If the part is not relative the initial value is a absolute pixel
   * value.
   *
   * @see FlexboxLayoutData#withRelative(boolean)
   * @param initial
   *          the initial size for the part.
   * @return
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

  public FlexboxLayoutData copy() {
    return copyValues(new FlexboxLayoutData());
  }

  protected FlexboxLayoutData copyValues(FlexboxLayoutData copy) {
    copy.withInitial(getInitial())
        .withRelative(isRelative())
        .withGrow(getGrow())
        .withShrink(getShrink());
    return copy;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(m_initial);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (m_relative ? 1231 : 1237);
    temp = Double.doubleToLongBits(m_grow);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(m_shrink);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    return true;
  }
}
