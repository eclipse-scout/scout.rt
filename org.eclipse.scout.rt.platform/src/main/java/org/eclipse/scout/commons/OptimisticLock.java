/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Optimistic locking with one accepted writer in critical section usage for writers with access check is as follows:
 * try{ if(lock.acquire()){ ... } } finally{ lock.release(); } usage for writers with no access check is as follows:
 * try{ lock.acquire() ... } finally{ lock.release(); } usage for tester is as follows: if(lock.isAcquired()){ ... } or
 * if(lock.isReleased()){ ... }
 */
public class OptimisticLock {
  private static final Logger LOG = LoggerFactory.getLogger(OptimisticLock.class);
  private volatile int m_lockCount = 0;

  /**
   * @return true if lock was acquired as first monitor
   */
  public synchronized boolean acquire() {
    m_lockCount++;
    if (m_lockCount == 1) {
      // this is the first
      return true;
    }
    else {
      if (m_lockCount > 10) {
        LOG.warn("potential programming problem; lock was 10 times acquired and not released", new Exception("origin"));
      }
      return false;
    }
  }

  public void release() {
    m_lockCount--;
  }

  public boolean isAcquired() {
    return m_lockCount > 0;
  }

  public boolean isReleased() {
    return m_lockCount <= 0;
  }

}
