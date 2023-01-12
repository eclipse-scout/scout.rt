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

import java.io.Serializable;

/**
 * A simple abstract pair class, having two properties <code>left</code> and <code>right</code> with different generic
 * types. See {@link MutablePair} and {@link ImmutablePair} for implementations.
 *
 * @since 6.0
 */
@SuppressWarnings("squid:S00118")
public abstract class Pair<L, R> implements Serializable {

  private static final long serialVersionUID = 1L;

  public abstract L getLeft();

  public abstract R getRight();

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getLeft() == null) ? 0 : getLeft().hashCode());
    result = prime * result + ((getRight() == null) ? 0 : getRight().hashCode());
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
    if (getLeft() == null) {
      if (other.getLeft() != null) {
        return false;
      }
    }
    else if (!getLeft().equals(other.getLeft())) {
      return false;
    }
    if (getRight() == null) {
      if (other.getRight() != null) {
        return false;
      }
    }
    else if (!getRight().equals(other.getRight())) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attr("left", getLeft())
        .attr("right", getRight())
        .toString();
  }
}
