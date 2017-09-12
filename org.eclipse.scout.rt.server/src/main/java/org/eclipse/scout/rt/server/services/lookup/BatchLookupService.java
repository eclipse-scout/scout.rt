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
package org.eclipse.scout.rt.server.services.lookup;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.services.lookup.BatchLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.BatchLookupResultCache;
import org.eclipse.scout.rt.shared.services.lookup.IBatchLookupService;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.IServerBatchLookupService;

/**
 * Implementation of {@link IBatchLookupService} that can be used in a server.<br>
 * It is not implemented for client-only application, bug 447592 is not relevant anymore.<br>
 * If you want to use {@link IBatchLookupService} in a client-only application, copy this service to your client code.
 *
 * @since 4.3.0 (Mars-M5)
 */
@Order(5100)
public class BatchLookupService implements IServerBatchLookupService {

  @Override
  public List<List<ILookupRow<?>>> getBatchDataByKey(BatchLookupCall batch) {
    List<ILookupCall<?>> calls = batch.getCallBatch();
    List<List<ILookupRow<?>>> result = new ArrayList<>();
    BatchLookupResultCache cache = new BatchLookupResultCache();
    for (ILookupCall<?> call : calls) {
      result.add(new ArrayList<>(cache.getDataByKey(call)));
    }
    return result;
  }

  @Override
  public List<List<ILookupRow<?>>> getBatchDataByText(BatchLookupCall batch) {
    List<ILookupCall<?>> calls = batch.getCallBatch();
    List<List<ILookupRow<?>>> result = new ArrayList<>();
    BatchLookupResultCache cache = new BatchLookupResultCache();
    for (ILookupCall<?> call : calls) {
      result.add(new ArrayList<>(cache.getDataByText(call)));
    }
    return result;
  }

  @Override
  public List<List<ILookupRow<?>>> getBatchDataByAll(BatchLookupCall batch) {
    List<ILookupCall<?>> calls = batch.getCallBatch();
    List<List<ILookupRow<?>>> result = new ArrayList<>();
    BatchLookupResultCache cache = new BatchLookupResultCache();
    for (ILookupCall<?> call : calls) {
      result.add(new ArrayList<>(cache.getDataByAll(call)));
    }
    return result;
  }

  @Override
  public List<List<ILookupRow<?>>> getBatchDataByRec(BatchLookupCall batch) {
    List<ILookupCall<?>> calls = batch.getCallBatch();
    List<List<ILookupRow<?>>> result = new ArrayList<>();
    BatchLookupResultCache cache = new BatchLookupResultCache();
    for (ILookupCall<?> call : calls) {
      result.add(new ArrayList<>(cache.getDataByRec(call)));
    }
    return result;
  }

}
