package org.eclipse.scout.rt.platform.util;

import java.util.Map;

/**
 * A simple pair class, having two mutable properties <code>left</code> and <code>right</code> with different generic
 * types.
 * <p>
 * <b>Do not use this class as key for {@link Map} or in a cache, since the members are mutable. Use
 * {@link ImmutablePair} instead.</b>
 *
 * @since 6.1
 */
@SuppressWarnings("squid:S2160")
public class MutablePair<L, R> extends Pair<L, R> {

  private L m_left;
  private R m_right;

  public MutablePair(L left, R right) {
    m_left = left;
    m_right = right;
  }

  @Override
  public L getLeft() {
    return m_left;
  }

  public void setLeft(L left) {
    m_left = left;
  }

  @Override
  public R getRight() {
    return m_right;
  }

  public void setRight(R right) {
    m_right = right;
  }
}
