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
package org.eclipse.scout.rt.shared.services.lookup;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Normalize batch lookup calls.
 * see {@link #normalize(LookupCall[])}
 * <p>
 * Instances of this type are intended to be used on one-time per-call basis.
 * <p>
 * Typical code is
 * 
 * <pre>
 * LookupCall[] callArray=...;
 * BatchLookupNormalizer normalizer=new BatchLookupNormalizer();
 * LookupCall[] normArray=normalizer.normalizeCalls(callArray);
 * LookupRow[][] normResultArray=SERVICES.getService(IBatchLookupService.class).getBatchByKey(normArray);
 * LookupRow[][] resultArray=normalizer.denormalizeResults(normResultArray);
 * </pre>
 */
public class BatchLookupNormalizer {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BatchLookupNormalizer.class);

  private int m_originalLength;
  private int[] m_forwardMapping;
  private LookupCall[] m_normalizedCalls;

  public BatchLookupNormalizer() {
  }

  /**
   * When two {@link LookupCall}s are {@link Object#equals(Object)} then the first one iq used, subsequent ones
   * reference the first ones directly.
   * 
   * @return the reduced array only containing distinct calls with respect to {@link LookupCall#equals(Object)}
   */
  public LookupCall[] normalizeCalls(LookupCall[] calls) throws ProcessingException {
    m_originalLength = 0;
    m_forwardMapping = null;
    m_normalizedCalls = null;
    if (calls == null || calls.length == 0) {
      return new LookupCall[0];
    }
    m_originalLength = calls.length;
    m_forwardMapping = new int[m_originalLength];
    HashMap<LookupCall, Integer> normMap = new HashMap<LookupCall, Integer>();
    ArrayList<LookupCall> normList = new ArrayList<LookupCall>(m_originalLength / 2);
    int normIndex = 0;
    for (int i = 0; i < m_originalLength; i++) {
      LookupCall call = calls[i];
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
    m_normalizedCalls = normList.toArray(new LookupCall[normList.size()]);
    return m_normalizedCalls;
  }

  /**
   * @return the results for the original array containing direct references to the shared results.
   */
  public LookupRow[][] denormalizeResults(LookupRow[][] normalizedResults) throws ProcessingException {
    if (m_originalLength == 0 || normalizedResults == null) {
      return new LookupRow[0][];
    }
    if (normalizedResults.length != m_normalizedCalls.length) {
      throw new IllegalArgumentException("normalized result array must have length " + m_normalizedCalls.length + " (" + normalizedResults.length + ")");
    }
    LookupRow[][] result = new LookupRow[m_originalLength][];
    for (int i = 0; i < m_originalLength; i++) {
      int mapIndex = m_forwardMapping[i];
      if (mapIndex >= 0) {
        result[i] = normalizedResults[mapIndex];
      }
    }
    return result;
  }
}
