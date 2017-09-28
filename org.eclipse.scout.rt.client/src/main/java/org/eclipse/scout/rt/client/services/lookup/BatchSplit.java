/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.services.lookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.shared.services.lookup.BatchLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;

/**
 * Split a batch into local calls and remote calls
 */
public class BatchSplit {
  private final List<ILookupCall<?>> m_calls;
  private final boolean[] m_local;
  private int m_localCount;
  private int m_remoteCount;
  //
  private final Map<ILookupCall<?>, List<ILookupRow<?>>> m_results;

  public BatchSplit(BatchLookupCall batch) {
    this(batch.getCallBatch());
  }

  public BatchSplit(List<ILookupCall<?>> calls) {
    m_calls = calls;
    m_local = new boolean[m_calls.size()];
    for (int i = 0; i < m_calls.size(); i++) {
      ILookupCall<?> call = m_calls.get(i);
      if (call != null) {
        if (call instanceof LocalLookupCall) {
          m_local[i] = true;
          m_localCount++;
        }
        else {
          m_local[i] = false;
          m_remoteCount++;
        }
      }
    }
    m_results = new HashMap<>();
  }

  public int getLocalCallCount() {
    return m_localCount;
  }

  public List<ILookupCall<?>> getLocalCalls() {
    List<ILookupCall<?>> localResult = new ArrayList<>();
    for (int i = 0; i < m_calls.size(); i++) {
      ILookupCall<?> call = m_calls.get(i);
      if (call != null && m_local[i]) {
        localResult.add(call);
      }
    }
    return localResult;
  }

  public int getRemoteCallCount() {
    return m_remoteCount;
  }

  public List<ILookupCall<?>> getRemoteCalls() {
    List<ILookupCall<?>> remoteResult = new ArrayList<>();
    for (int i = 0; i < m_calls.size(); i++) {
      ILookupCall<?> call = m_calls.get(i);
      if (call != null && !m_local[i]) {
        remoteResult.add(call);
      }
    }
    return remoteResult;
  }

  public void setLocalResults(List<List<ILookupRow<?>>> data) {
    int k = 0;
    for (int i = 0; i < m_calls.size(); i++) {
      ILookupCall<?> call = m_calls.get(i);
      if (call != null && m_local[i]) {
        m_results.put(call, data.get(k));
        k++;
      }
    }
  }

  public void setRemoteResults(List<List<ILookupRow<?>>> data) {
    int k = 0;
    for (int i = 0; i < m_calls.size(); i++) {
      ILookupCall<?> call = m_calls.get(i);
      if (call != null && !m_local[i]) {
        m_results.put(call, data.get(k));
        k++;
      }
    }
  }

  public List<List<ILookupRow<?>>> getCombinedResults() {
    List<List<ILookupRow<?>>> result = new ArrayList<>();
    for (ILookupCall<?> call : m_calls) {
      result.add(m_results.get(call));
    }
    return result;
  }
}
