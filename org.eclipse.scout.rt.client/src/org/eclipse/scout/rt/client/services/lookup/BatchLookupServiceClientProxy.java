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

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.servicetunnel.ServiceTunnelUtility;
import org.eclipse.scout.rt.shared.services.lookup.BatchLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.IBatchLookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.service.AbstractService;

@Priority(-3)
public class BatchLookupServiceClientProxy extends AbstractService implements IBatchLookupService {

  public BatchLookupServiceClientProxy() {
  }

  @Override
  public LookupRow[][] getBatchDataByKey(BatchLookupCall batch) throws ProcessingException {
    BatchSplit split = new BatchSplit(batch);
    if (split.getLocalCallCount() > 0) {
      LookupCall[] calls = split.getLocalCalls();
      LookupRow[][] resultArray = new LookupRow[calls.length][];
      for (int i = 0; i < calls.length; i++) {
        resultArray[i] = calls[i].getDataByKey();
      }
      split.setLocalResults(resultArray);
    }
    if (split.getRemoteCallCount() > 0) {
      LookupRow[][] resultArray = getTargetService().getBatchDataByKey(new BatchLookupCall(split.getRemoteCalls()));
      split.setRemoteResults(resultArray);
    }
    return split.getCombinedResults();
  }

  @Override
  public LookupRow[][] getBatchDataByText(BatchLookupCall batch) throws ProcessingException {
    BatchSplit split = new BatchSplit(batch);
    if (split.getLocalCallCount() > 0) {
      LookupCall[] calls = split.getLocalCalls();
      LookupRow[][] resultArray = new LookupRow[calls.length][];
      for (int i = 0; i < calls.length; i++) {
        resultArray[i] = calls[i].getDataByText();
      }
      split.setLocalResults(resultArray);
    }
    if (split.getRemoteCallCount() > 0) {
      LookupRow[][] resultArray = getTargetService().getBatchDataByText(new BatchLookupCall(split.getRemoteCalls()));
      split.setRemoteResults(resultArray);
    }
    return split.getCombinedResults();
  }

  @Override
  public LookupRow[][] getBatchDataByAll(BatchLookupCall batch) throws ProcessingException {
    BatchSplit split = new BatchSplit(batch);
    if (split.getLocalCallCount() > 0) {
      LookupCall[] calls = split.getLocalCalls();
      LookupRow[][] resultArray = new LookupRow[calls.length][];
      for (int i = 0; i < calls.length; i++) {
        resultArray[i] = calls[i].getDataByAll();
      }
      split.setLocalResults(resultArray);
    }
    if (split.getRemoteCallCount() > 0) {
      LookupRow[][] resultArray = getTargetService().getBatchDataByAll(new BatchLookupCall(split.getRemoteCalls()));
      split.setRemoteResults(resultArray);
    }
    return split.getCombinedResults();
  }

  @Override
  public LookupRow[][] getBatchDataByRec(BatchLookupCall batch) throws ProcessingException {
    BatchSplit split = new BatchSplit(batch);
    if (split.getLocalCallCount() > 0) {
      LookupCall[] calls = split.getLocalCalls();
      LookupRow[][] resultArray = new LookupRow[calls.length][];
      for (int i = 0; i < calls.length; i++) {
        resultArray[i] = calls[i].getDataByRec();
      }
      split.setLocalResults(resultArray);
    }
    if (split.getRemoteCallCount() > 0) {
      LookupRow[][] resultArray = getTargetService().getBatchDataByRec(new BatchLookupCall(split.getRemoteCalls()));
      split.setRemoteResults(resultArray);
    }
    return split.getCombinedResults();
  }

  private IBatchLookupService getTargetService() {
    return ServiceTunnelUtility.createProxy(IBatchLookupService.class, ClientSyncJob.getCurrentSession().getServiceTunnel());
  }

}
