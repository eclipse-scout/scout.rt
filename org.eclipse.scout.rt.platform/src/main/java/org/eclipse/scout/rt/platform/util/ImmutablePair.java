/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
