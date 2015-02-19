/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
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

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.service.AbstractService;

/**
 * Implementation of {@link IBatchLookupService} that can be used from in a server or in a client-only application.
 * See Bug 447592.
 * 
 * @since 4.3.0 (Mars-M5)
 */
@Priority(-1)
public class BatchLookupService extends AbstractService implements IBatchLookupService {

  public BatchLookupService() {
  }

  @Override
  public List<List<ILookupRow<?>>> getBatchDataByKey(BatchLookupCall batch) throws ProcessingException {
    List<ILookupCall<?>> calls = batch.getCallBatch();
    List<List<ILookupRow<?>>> result = new ArrayList<List<ILookupRow<?>>>();
    BatchLookupResultCache cache = new BatchLookupResultCache();
    for (ILookupCall<?> call : calls) {
      result.add(new ArrayList<ILookupRow<?>>(cache.getDataByKey(call)));
    }
    return result;
  }

  @Override
  public List<List<ILookupRow<?>>> getBatchDataByText(BatchLookupCall batch) throws ProcessingException {
    List<ILookupCall<?>> calls = batch.getCallBatch();
    List<List<ILookupRow<?>>> result = new ArrayList<List<ILookupRow<?>>>();
    BatchLookupResultCache cache = new BatchLookupResultCache();
    for (ILookupCall<?> call : calls) {
      result.add(new ArrayList<ILookupRow<?>>(cache.getDataByText(call)));
    }
    return result;
  }

  @Override
  public List<List<ILookupRow<?>>> getBatchDataByAll(BatchLookupCall batch) throws ProcessingException {
    List<ILookupCall<?>> calls = batch.getCallBatch();
    List<List<ILookupRow<?>>> result = new ArrayList<List<ILookupRow<?>>>();
    BatchLookupResultCache cache = new BatchLookupResultCache();
    for (ILookupCall<?> call : calls) {
      result.add(new ArrayList<ILookupRow<?>>(cache.getDataByAll(call)));
    }
    return result;
  }

  @Override
  public List<List<ILookupRow<?>>> getBatchDataByRec(BatchLookupCall batch) throws ProcessingException {
    List<ILookupCall<?>> calls = batch.getCallBatch();
    List<List<ILookupRow<?>>> result = new ArrayList<List<ILookupRow<?>>>();
    BatchLookupResultCache cache = new BatchLookupResultCache();
    for (ILookupCall<?> call : calls) {
      result.add(new ArrayList<ILookupRow<?>>(cache.getDataByRec(call)));
    }
    return result;
  }

}
