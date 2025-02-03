/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.util.collection.ConcurrentExpiringMap;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Logback message filter which filters duplicate messages based on the format of the log message.
 **/
public class ExpiringDuplicateMessageFilter extends TurboFilter {

  private static final int DEFAULT_CACHE_SIZE = 100;
  private static final long DEFAULT_TTL = 15;
  private static final int DEFAULT_ALLOWED_REPETITIONS = 5;
  private static final String DEFAULT_MARKER = "filterDuplicates";

  private ConcurrentExpiringMap<String, Integer> m_messageCache;
  private int m_allowedRepetitions = DEFAULT_ALLOWED_REPETITIONS;
  private int m_cacheSize = DEFAULT_CACHE_SIZE;
  private long m_ttl = DEFAULT_TTL;
  private Marker m_markerToMatch = MarkerFactory.getMarker(DEFAULT_MARKER);

  @Override
  public void start() {
    super.start();
    m_messageCache = new ConcurrentExpiringMap<>(new ConcurrentHashMap<>(), TimeUnit.MINUTES.toMillis(m_ttl), false, m_cacheSize);
  }

  @Override
  public void stop() {
    m_messageCache.clear();
    super.stop();
  }

  @Override
  public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
    if (format == null || !m_markerToMatch.equals(marker)) {
      return FilterReply.NEUTRAL;
    }
    int count = m_messageCache.compute(format, (k, v) -> v == null ? 1 : ++v);
    return count > m_allowedRepetitions ? FilterReply.DENY : FilterReply.NEUTRAL;
  }

  public int getAllowedRepetitions() {
    return m_allowedRepetitions;
  }

  /**
   * The allowed number of repetitions before a message is filtered.
   *
   * @param allowedRepetitions
   *     number of allowed repetitions
   */
  public void setAllowedRepetitions(int allowedRepetitions) {
    this.m_allowedRepetitions = allowedRepetitions;
  }

  public int getCacheSize() {
    return m_cacheSize;
  }

  public void setCacheSize(int cacheSize) {
    this.m_cacheSize = cacheSize;
  }

  public long getTtl() {
    return m_ttl;
  }

  /**
   * Sets the time to live of the underlying {@link ConcurrentExpiringMap}
   *
   * @param ttl
   *     Time to live in minutes
   */
  public void setTtl(long ttl) {
    m_ttl = ttl;
  }

  public void setMarker(String markerStr) {
    if (markerStr != null) {
      m_markerToMatch = MarkerFactory.getMarker(markerStr);
    }
  }
}
