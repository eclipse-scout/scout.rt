package org.eclipse.scout.rt.platform.util;

/**
 * A simple pair class, having two properties <code>left</code> and <code>right</code> with different generic types.
 *
 * @since 6.0
 */
public class Pair<L, R> {

  private final L m_left;

  private final R m_right;

  public Pair(L left, R right) {
    m_left = left;
    m_right = right;
  }

  public L getLeft() {
    return m_left;
  }

  public R getRight() {
    return m_right;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_left == null) ? 0 : m_left.hashCode());
    result = prime * result + ((m_right == null) ? 0 : m_right.hashCode());
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
    Pair other = (Pair) obj;
    if (m_left == null) {
      if (other.m_left != null) {
        return false;
      }
    }
    else if (!m_left.equals(other.m_left)) {
      return false;
    }
    if (m_right == null) {
      if (other.m_right != null) {
        return false;
      }
    }
    else if (!m_right.equals(other.m_right)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attr("left", m_left)
        .attr("right", m_right)
        .toString();
  }

}
