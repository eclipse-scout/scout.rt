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
package org.eclipse.scout.rt.shared.services.lookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Normalize batch lookup calls. see {@link #normalize(LookupCall[])}
 * <p>
 * Instances of this type are intended to be used on one-time per-call basis.
 * <p>
 * Typical code is
 *
 * <pre>
 * LookupCall[] callArray=...;
 * BatchLookupNormalizer normalizer=new BatchLookupNormalizer();
 * LookupCall[] normArray=normalizer.normalizeCalls(callArray);
 * LookupRow[][] normResultArray=BEANS.get(IBatchLookupService.class).getBatchByKey(normArray);
 * LookupRow[][] resultArray=normalizer.denormalizeResults(normResultArray);
 * </pre>
 */
public class BatchLookupNormalizer {
  private int m_originalLength;
  private int[] m_forwardMapping;
  private List<ILookupCall<?>> m_normalizedCalls;

  /**
   * When two {@link LookupCall}s are {@link Object#equals(Object)} then the first one iq used, subsequent ones
   * reference the first ones directly.
   *
   * @return the reduced array only containing distinct calls with respect to {@link LookupCall#equals(Object)}
   */
  public List<ILookupCall<?>> normalizeCalls(List<ILookupCall<?>> calls) {
    m_originalLength = 0;
    m_forwardMapping = null;
    m_normalizedCalls = null;
    if (calls == null || calls.isEmpty()) {
      return CollectionUtility.emptyArrayList();
    }
    m_originalLength = calls.size();
    m_forwardMapping = new int[m_originalLength];
    Map<ILookupCall<?>, Integer> normMap = new HashMap<>();
    List<ILookupCall<?>> normList = new ArrayList<>(m_originalLength / 2);
    int normIndex = 0;
    for (int i = 0; i < m_originalLength; i++) {
      ILookupCall<?> call = calls.get(i);
      if (call == null) {
        m_forwardMapping[i] = -1;
        continue;
      }
      if (!BatchLookupResultCache.isCacheable(call.getClass())) {
        m_forwardMapping[i] = normIndex;
        normList.add(call);
        normIndex++;
        continue;
      }
      Integer existingIndex = normMap.get(call);
      if (existingIndex != null) {
        m_forwardMapping[i] = existingIndex.intValue();
        continue;
      }
      m_forwardMapping[i] = normIndex;
      normList.add(call);
      normMap.put(call, normIndex);
      normIndex++;
    }
    normMap = null;//gc
    m_normalizedCalls = normList;
    return CollectionUtility.arrayList(m_normalizedCalls);
  }

  /**
   * @return the results for the original array containing direct references to the shared results.
   */
  public List<List<ILookupRow<?>>> denormalizeResults(List<List<ILookupRow<?>>> normalizedResults) {
    if (m_originalLength == 0 || normalizedResults == null) {
      return CollectionUtility.emptyArrayList();
    }
    if (normalizedResults.size() != m_normalizedCalls.size()) {
      throw new IllegalArgumentException("normalized result array must have length " + m_normalizedCalls.size() + " (" + normalizedResults.size() + ")");
    }
    List<List<ILookupRow<?>>> result = new ArrayList<>(m_originalLength);
    for (int i = 0; i < m_originalLength; i++) {
      int mapIndex = m_forwardMapping[i];
      if (mapIndex >= 0) {
        result.add(normalizedResults.get(mapIndex));
      }
      else {
        result.add(null);
      }
    }
    return result;
  }
}
