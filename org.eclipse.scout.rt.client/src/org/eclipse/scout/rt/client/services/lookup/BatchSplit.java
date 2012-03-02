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
package org.eclipse.scout.rt.client.services.lookup;

import org.eclipse.scout.rt.shared.services.lookup.BatchLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 * Split a batch into local calls and remote calls
 */
public class BatchSplit {
  private LookupCall[] m_calls;
  private boolean[] m_local;
  private int m_localCount;
  private int m_remoteCount;
  //
  private LookupRow[][] m_results;

  public BatchSplit(BatchLookupCall batch) {
    this(batch.getCallBatch());
  }

  public BatchSplit(LookupCall[] calls) {
    m_calls = calls;
    m_local = new boolean[m_calls.length];
    for (int i = 0; i < m_calls.length; i++) {
      if (m_calls[i] != null) {
        if (m_calls[i] instanceof LocalLookupCall) {
          m_local[i] = true;
          m_localCount++;
        }
        else {
          m_local[i] = false;
          m_remoteCount++;
        }
      }
    }
    m_results = new LookupRow[m_calls.length][];
  }

  public int getLocalCallCount() {
    return m_localCount;
  }

  public LookupCall[] getLocalCalls() {
    LookupCall[] a = new LookupCall[m_localCount];
    int k = 0;
    for (int i = 0; i < m_calls.length; i++) {
      if (m_calls[i] != null) {
        if (m_local[i]) {
          a[k] = m_calls[i];
          k++;
        }
      }
    }
    return a;
  }

  public int getRemoteCallCount() {
    return m_remoteCount;
  }

  public LookupCall[] getRemoteCalls() {
    LookupCall[] a = new LookupCall[m_remoteCount];
    int k = 0;
    for (int i = 0; i < m_calls.length; i++) {
      if (m_calls[i] != null) {
        if (!m_local[i]) {
          a[k] = m_calls[i];
          k++;
        }
      }
    }
    return a;
  }

  public void setLocalResults(LookupRow[][] data) {
    int k = 0;
    for (int i = 0; i < m_calls.length; i++) {
      if (m_calls[i] != null) {
        if (m_local[i]) {
          m_results[i] = data[k];
          k++;
        }
      }
    }
  }

  public void setRemoteResults(LookupRow[][] data) {
    int k = 0;
    for (int i = 0; i < m_calls.length; i++) {
      if (m_calls[i] != null) {
        if (!m_local[i]) {
          m_results[i] = data[k];
          k++;
        }
      }
    }
  }

  public LookupRow[][] getCombinedResults() {
    return m_results;
  }

}
