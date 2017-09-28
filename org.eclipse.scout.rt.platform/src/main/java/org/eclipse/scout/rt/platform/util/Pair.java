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
 * A simple abstract pair class, having two properties <code>left</code> and <code>right</code> with different generic
 * types. See {@link MutablePair} and {@link ImmutablePair} for implementations.
 *
 * @since 6.0
 */
@SuppressWarnings("squid:S00118")
public abstract class Pair<L, R> {

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
