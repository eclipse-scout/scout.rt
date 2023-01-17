/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

/**
 * A simple pair class, having two immutable final properties <code>left</code> and <code>right</code> with different
 * generic types.
 *
 * @since 6.1
 */
@SuppressWarnings("squid:S2160")
public class ImmutablePair<L, R> extends Pair<L, R> {

  private static final long serialVersionUID = 1L;

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

  /**
   * @return {@link ImmutablePair} instance with given values
   */
  public static <L, R> ImmutablePair<L, R> of(L left, R right) {
    return new ImmutablePair<>(left, right);
  }
}
