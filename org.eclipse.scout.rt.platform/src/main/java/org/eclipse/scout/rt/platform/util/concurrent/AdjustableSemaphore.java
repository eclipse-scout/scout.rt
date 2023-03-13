/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util.concurrent;

import java.util.concurrent.Semaphore;

/**
 * Allows changing the maximum number of permits of a semaphore.
 * <p>
 * Be warned: The maximum number of permits is just some kind of 'virtual' value. As this class expose methods like
 * {@link #release(int)}, this virtual value can be 'corrupted'. Therefore:<br/>
 * Always acquire first, then release the same number of permits as one has acquired. Change the maximum permits only
 * through {@link #setMaximumPermits(int)}.
 */
public final class AdjustableSemaphore extends Semaphore {

  private static final long serialVersionUID = 1L;

  /**
   * access must be synchronized
   */
  private int m_maximumPermits;

  public AdjustableSemaphore(int maximumPermits) {
    this(maximumPermits, false);
  }

  public AdjustableSemaphore(int maximumPermits, boolean fair) {
    super(maximumPermits, fair);
    if (maximumPermits < 1) {
      throw new IllegalArgumentException("Maximum permits must be at least 1");
    }
    m_maximumPermits = maximumPermits;
  }

  public synchronized int getMaximumPermits() {
    return m_maximumPermits;
  }

  public synchronized void setMaximumPermits(int maximumPermits) {
    if (maximumPermits < 1) {
      throw new IllegalArgumentException("Maximum permits must be at least 1");
    }
    int delta = maximumPermits - m_maximumPermits;
    if (delta == 0) {
      return;
    }
    else if (delta > 0) {
      // new maximum permits is higher, release some in order to increase maximum
      release(delta);
    }
    else {
      // reducePermits needs a positive #, though.
      reducePermits(-delta);
    }
    m_maximumPermits = maximumPermits;
  }
}
