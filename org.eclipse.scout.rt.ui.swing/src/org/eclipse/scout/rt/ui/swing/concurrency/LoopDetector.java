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
package org.eclipse.scout.rt.ui.swing.concurrency;

import java.util.LinkedList;

/**
 * counts hits within a timeframe and compares count to a suspicious size
 * <p>
 * Detects based on an enter/exit hysteresis.
 */
public class LoopDetector {
  private long m_ttl;
  private int m_detectSize;
  private int m_releaseSize;
  private boolean m_armed;
  private LinkedList<Long> m_samples;

  public LoopDetector(long ttlMillis, int detectSize, int releaseSize) {
    m_ttl = ttlMillis;
    m_detectSize = detectSize;
    m_releaseSize = releaseSize;
    m_samples = new LinkedList<Long>();
  }

  /**
   * injectable timestamp creator, can be overrwritten for unit testing
   * <p>
   * default returns {@link System#currentTimeMillis()}
   */
  protected long createTimestamp() {
    return System.currentTimeMillis();
  }

  public synchronized void addSample() {
    long ts = createTimestamp();
    while (m_samples.size() > 0 && m_samples.getFirst() + m_ttl < ts) {
      m_samples.removeFirst();
    }
    m_samples.add(ts);
    if (m_armed) {
      if (m_samples.size() <= m_releaseSize) {
        m_armed = false;
      }
    }
    else {
      if (m_samples.size() > m_detectSize) {
        m_armed = true;
      }
    }
  }

  public boolean isArmed() {
    return m_armed;
  }

  public int getSampleCount() {
    return m_samples.size();
  }
}
