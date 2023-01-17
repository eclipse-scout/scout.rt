/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

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
  private final AtomicInteger m_lockCount = new AtomicInteger(0);

  /**
   * @return true if lock was acquired as first monitor
   */
  public synchronized boolean acquire() {
    int count = m_lockCount.incrementAndGet();
    if (count == 1) {
      // this is the first
      return true;
    }
    else {
      if (count > 10) {
        LOG.warn("potential programming problem; lock was 10 times acquired and not released", new Exception("origin"));
      }
      return false;
    }
  }

  public void release() {
    m_lockCount.decrementAndGet();
  }

  public boolean isAcquired() {
    return m_lockCount.get() > 0;
  }

  public boolean isReleased() {
    return !isAcquired();
  }

}
