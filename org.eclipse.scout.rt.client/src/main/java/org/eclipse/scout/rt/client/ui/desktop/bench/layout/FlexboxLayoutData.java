package org.eclipse.scout.rt.client.ui.desktop.bench.layout;

/**
 * <h3>{@link FlexboxLayoutData}</h3>
 *
 * @author aho
 */
public class FlexboxLayoutData {
  private boolean m_relative = true;
  private double m_grow = 1;
  private double m_shrink = 1;
  private double m_initial = 100.0 / 3.0;

  public FlexboxLayoutData withRelative(boolean relative) {
    m_relative = relative;
    return this;
  }

  public boolean isRelative() {
    return m_relative;
  }

  public FlexboxLayoutData withGrow(double grow) {
    m_grow = grow;
    return this;
  }

  public double getGrow() {
    return m_grow;
  }

  public FlexboxLayoutData withShrink(double shrink) {
    m_shrink = shrink;
    return this;
  }

  public double getShrink() {
    return m_shrink;
  }

  public FlexboxLayoutData withInitial(double initial) {
    m_initial = initial;
    return this;
  }

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
