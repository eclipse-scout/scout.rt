package org.eclipse.scout.rt.platform.util;

/**
 * A simple pair class, having two immutable final properties <code>left</code> and <code>right</code> with different
 * generic types.
 *
 * @since 6.1
 */
@SuppressWarnings("squid:S2160")
public class ImmutablePair<L, R> extends Pair<L, R> {

  private final L m_left;
  private final R m_right;

  public ImmutablePair(L left, R right) {
    m_left = left;
    m_right = right;
  }

  @Override
  public L getLeft() {
    return m_left;
  }

  @Override
  public R getRight() {
    return m_right;
  }
}
