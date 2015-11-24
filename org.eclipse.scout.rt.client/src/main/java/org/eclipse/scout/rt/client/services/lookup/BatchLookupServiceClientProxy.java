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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.services.lookup.BatchLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.BatchLookupNormalizer;
import org.eclipse.scout.rt.shared.services.lookup.BatchLookupResultCache;
import org.eclipse.scout.rt.shared.services.lookup.IBatchLookupService;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.IServerBatchLookupService;

@Order(4900)
public class BatchLookupServiceClientProxy implements IBatchLookupService {

  public BatchLookupServiceClientProxy() {
  }

  @Override
  public List<List<ILookupRow<?>>> getBatchDataByKey(BatchLookupCall batch) {
    List<ILookupCall<?>> allCalls = batch.getCallBatch();
    List<ILookupCall<?>> cleanCalls = new ArrayList<ILookupCall<?>>(allCalls.size());
    //set calls with key==null to null
    for (ILookupCall<?> call : allCalls) {
      if (call != null && call.getKey() == null) {
        cleanCalls.add(null);
      }
      else {
        cleanCalls.add(call);
      }
    }
    BatchSplit split = new BatchSplit(cleanCalls);
    if (split.getLocalCallCount() > 0) {
      BatchLookupResultCache cache = new BatchLookupResultCache();
      List<ILookupCall<?>> calls = split.getLocalCalls();
      List<List<ILookupRow<?>>> result = new ArrayList<List<ILookupRow<?>>>();
      for (ILookupCall<?> call : calls) {
        result.add(cache.getDataByKey(call));
      }
      split.setLocalResults(result);
    }
    if (split.getRemoteCallCount() > 0) {
      BatchLookupNormalizer normalizer = new BatchLookupNormalizer();
      List<ILookupCall<?>> normCallArray = normalizer.normalizeCalls(split.getRemoteCalls());
      List<List<ILookupRow<?>>> normResultArray = getTargetService().getBatchDataByKey(new BatchLookupCall(normCallArray));
      List<List<ILookupRow<?>>> resultArray = normalizer.denormalizeResults(normResultArray);
      split.setRemoteResults(resultArray);
    }
    List<List<ILookupRow<?>>> results = split.getCombinedResults();

    //set null results to empty list
    for (int i = 0; i < results.size(); i++) {
      if (results.get(i) == null) {
        List<ILookupRow<?>> emptyList = CollectionUtility.emptyArrayList();
        results.set(i, emptyList);
      }
    }
    return results;
  }

  @Override
  public List<List<ILookupRow<?>>> getBatchDataByText(BatchLookupCall batch) {
    BatchSplit split = new BatchSplit(batch);
    if (split.getLocalCallCount() > 0) {
      BatchLookupResultCache cache = new BatchLookupResultCache();
      List<ILookupCall<?>> calls = split.getLocalCalls();
      List<List<ILookupRow<?>>> resultArray = new ArrayList<List<ILookupRow<?>>>();
      for (ILookupCall<?> call : calls) {
        resultArray.add(cache.getDataByText(call));
      }
      split.setLocalResults(resultArray);
    }
    if (split.getRemoteCallCount() > 0) {
      List<List<ILookupRow<?>>> resultArray = getTargetService().getBatchDataByText(new BatchLookupCall(split.getRemoteCalls()));
      split.setRemoteResults(resultArray);
    }
    return split.getCombinedResults();
  }

  @Override
  public List<List<ILookupRow<?>>> getBatchDataByAll(BatchLookupCall batch) {
    BatchSplit split = new BatchSplit(batch);
    if (split.getLocalCallCount() > 0) {
      BatchLookupResultCache cache = new BatchLookupResultCache();
      List<ILookupCall<?>> calls = split.getLocalCalls();
      List<List<ILookupRow<?>>> resultArray = new ArrayList<List<ILookupRow<?>>>();
      for (ILookupCall<?> call : calls) {
        resultArray.add(cache.getDataByAll(call));
      }
      split.setLocalResults(resultArray);
    }
    if (split.getRemoteCallCount() > 0) {
      List<List<ILookupRow<?>>> resultArray = getTargetService().getBatchDataByAll(new BatchLookupCall(split.getRemoteCalls()));
      split.setRemoteResults(resultArray);
    }
    return split.getCombinedResults();
  }

  @Override
  public List<List<ILookupRow<?>>> getBatchDataByRec(BatchLookupCall batch) {
    BatchSplit split = new BatchSplit(batch);
    if (split.getLocalCallCount() > 0) {
      BatchLookupResultCache cache = new BatchLookupResultCache();
      List<ILookupCall<?>> calls = split.getLocalCalls();
      List<List<ILookupRow<?>>> resultArray = new ArrayList<List<ILookupRow<?>>>();
      for (ILookupCall<?> call : calls) {
        resultArray.add(cache.getDataByRec(call));
      }
      split.setLocalResults(resultArray);
    }
    if (split.getRemoteCallCount() > 0) {
      List<List<ILookupRow<?>>> resultArray = getTargetService().getBatchDataByRec(new BatchLookupCall(split.getRemoteCalls()));
      split.setRemoteResults(resultArray);
    }
    return split.getCombinedResults();
  }

  private IBatchLookupService getTargetService() {
    return BEANS.get(IServerBatchLookupService.class);
  }

}
