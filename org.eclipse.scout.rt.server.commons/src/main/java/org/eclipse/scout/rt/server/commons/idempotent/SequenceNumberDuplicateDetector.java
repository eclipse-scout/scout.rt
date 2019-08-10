/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.commons.idempotent;

import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.date.IDateProvider;

/**
 * Data structure that detects duplicates on a series of sequence numbers as it is used with non-idempotent http post
 * communications. The request sequence numbers arrive in <em>almost</em> ascending order. This means that the incoming
 * numbers must not be perfectly ascending but more or less; some numbers bypassing others within the network latency
 * time. Such bypassing can happen when multiple parallel http requests rush the server.
 * <p>
 * Therefore the {@link SequenceNumberDuplicateDetector} keeps in mind the sequence numbers of the last few minutes
 * (DT). That way it can safely decide if an incoming sequence number is a duplicate or not.
 * <p>
 * The decision if a new incoming sequence number S is a duplicate is as follows
 * <ol>
 * <li>if the {@link SequenceNumberDuplicateDetector} contains S in its cache of former N numbers, then S is rejected as
 * duplicate</li>
 * <li>else if S is lower than the lowest number in the cache of former N numbers, then S is rejected, since it is
 * definitely out of sequence</li>
 * <li>else S is added to the cache. If the cache is larger than cacheSize, then cache elements older than DT are
 * dropped until the cache has size cacheSize. S is finally accepted</li>
 * </ol>
 * <p>
 * This implementation is thread-safe.
 *
 * @since 9.0
 */
public class SequenceNumberDuplicateDetector {
  private final TreeMap<Long/*sequenceNumber*/, Long/*timestamp*/> m_cache = new TreeMap<>();
  private final int m_cacheSizeGuide;
  private final long m_maxAgeMillis;

  /**
   * Default constructor with cacheSize of 25 and maxAge of 1 minute
   */
  public SequenceNumberDuplicateDetector() {
    this(25, 1, TimeUnit.MINUTES);
  }

  /**
   * The cache inside {@link SequenceNumberDuplicateDetector} guarantees to hold at any time all request sequence
   * numbers of the last maxAge timeframe, but at least cacheSize elements.
   *
   * @param cacheSizeGuide
   *          is the number of entries that the cache tries to maintain. The cache may become larger upon high request
   *          load but will later try to resize to this size. Typical values are 25-100.
   * @param maxAge
   *          is the time while sequence numbers remain cached. This value must be larger than the expected network
   *          latency. Typical latency time is around 10-200ms thus a maxAge of 1 minute is safe.
   * @param maxAgeUnit
   */
  public SequenceNumberDuplicateDetector(int cacheSizeGuide, long maxAge, TimeUnit maxAgeUnit) {
    Assertions.assertTrue(cacheSizeGuide >= 1, "cacheSizeGuide ({}) must be at least 1");
    Assertions.assertTrue(cacheSizeGuide < Integer.MAX_VALUE / 3, "cacheSizeGuide ({}) must be at most {}" + (Integer.MAX_VALUE / 3));
    m_cacheSizeGuide = cacheSizeGuide;
    m_maxAgeMillis = maxAgeUnit.toMillis(maxAge);
  }

  /**
   * Put a new sequence number N into the detector
   *
   * @return true if N is accepted and was added to the cache, false if N was rejected as a duplicate number
   */
  public synchronized boolean accept(long n) {
    if (m_cache.containsKey(n)) {
      return false;
    }
    if (m_cache.size() >= m_cacheSizeGuide && n < m_cache.firstKey()) {
      return false;
    }
    long now = BEANS.get(IDateProvider.class).currentUTCMillis();
    m_cache.put(n, now);
    //housekeeping
    if (m_cache.size() > m_cacheSizeGuide * 2) {
      while (m_cache.size() > m_cacheSizeGuide) {
        long age = now - m_cache.firstEntry().getValue();
        if (age > m_maxAgeMillis) {
          m_cache.pollFirstEntry();
        }
        else {
          break;
        }
      }
    }
    return true;
  }

  /**
   * used for unit testing
   */
  protected TreeMap<Long, Long> getCache() {
    return m_cache;
  }
}
