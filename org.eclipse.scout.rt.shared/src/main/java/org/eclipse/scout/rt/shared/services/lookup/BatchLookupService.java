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
package org.eclipse.scout.rt.shared.services.lookup;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.Order;

/**
 * Implementation of {@link IBatchLookupService} that can be used in a server or in a client-only application. See Bug
 * 447592.
 *
 * @since 4.3.0 (Mars-M5)
 */
@Order(5100)
public class BatchLookupService implements IServerBatchLookupService {

  @Override
  public List<List<ILookupRow<?>>> getBatchDataByKey(BatchLookupCall batch) {
    List<ILookupCall<?>> calls = batch.getCallBatch();
    List<List<ILookupRow<?>>> result = new ArrayList<List<ILookupRow<?>>>();
    BatchLookupResultCache cache = new BatchLookupResultCache();
    for (ILookupCall<?> call : calls) {
      result.add(new ArrayList<ILookupRow<?>>(cache.getDataByKey(call)));
    }
    return result;
  }

  @Override
  public List<List<ILookupRow<?>>> getBatchDataByText(BatchLookupCall batch) {
    List<ILookupCall<?>> calls = batch.getCallBatch();
    List<List<ILookupRow<?>>> result = new ArrayList<List<ILookupRow<?>>>();
    BatchLookupResultCache cache = new BatchLookupResultCache();
    for (ILookupCall<?> call : calls) {
      result.add(new ArrayList<ILookupRow<?>>(cache.getDataByText(call)));
    }
    return result;
  }

  @Override
  public List<List<ILookupRow<?>>> getBatchDataByAll(BatchLookupCall batch) {
    List<ILookupCall<?>> calls = batch.getCallBatch();
    List<List<ILookupRow<?>>> result = new ArrayList<List<ILookupRow<?>>>();
    BatchLookupResultCache cache = new BatchLookupResultCache();
    for (ILookupCall<?> call : calls) {
      result.add(new ArrayList<ILookupRow<?>>(cache.getDataByAll(call)));
    }
    return result;
  }

  @Override
  public List<List<ILookupRow<?>>> getBatchDataByRec(BatchLookupCall batch) {
    List<ILookupCall<?>> calls = batch.getCallBatch();
    List<List<ILookupRow<?>>> result = new ArrayList<List<ILookupRow<?>>>();
    BatchLookupResultCache cache = new BatchLookupResultCache();
    for (ILookupCall<?> call : calls) {
      result.add(new ArrayList<ILookupRow<?>>(cache.getDataByRec(call)));
    }
    return result;
  }

}
