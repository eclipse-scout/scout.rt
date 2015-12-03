/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.services.common.perf.internal;

public class PerformanceSampleSet {
  private Object m_samplesLock = new Object();
  private long[] m_samples;
  private int m_samplesLastIndex;
  private long m_samplesSum;

  public PerformanceSampleSet(int size, long initialValue) {
    m_samplesSum = 0;
    m_samples = new long[size];
    for (int i = 0; i < m_samples.length; i++) {
      m_samples[i] = initialValue;
      m_samplesSum += m_samples[i];
    }
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
