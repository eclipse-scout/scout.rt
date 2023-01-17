/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.common.perf.internal;

import java.util.Arrays;

public class PerformanceSampleSet {
  private final Object m_samplesLock = new Object();
  private final long[] m_samples;
  private int m_samplesLastIndex;
  private long m_samplesSum;

  public PerformanceSampleSet(int size, long initialValue) {
    m_samplesSum = 0;
    m_samples = new long[size];
    Arrays.fill(m_samples, initialValue);
    m_samplesSum = size * initialValue;
    m_samplesLastIndex = m_samples.length - 1;
  }

  public void addSample(long millis) {
    if (millis < 0) {
      millis = 0;
    }
    synchronized (m_samplesLock) {
      long l = m_samplesSum;
      int i = (m_samplesLastIndex + 1) % m_samples.length;
      // remove last
      l -= m_samples[i];
      // add new
      m_samples[i] = millis;
      l += m_samples[i];
      // next
      m_samplesLastIndex = i;
      m_samplesSum = l;
    }
  }

  /**
   * @return the value in ms measured over the last n samples
   */
  public long getValue() {
    return m_samplesSum / m_samples.length;
  }
}
