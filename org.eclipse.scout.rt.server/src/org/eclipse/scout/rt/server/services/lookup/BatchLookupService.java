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
package org.eclipse.scout.rt.server.services.lookup;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.lookup.BatchLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.IBatchLookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.service.AbstractService;

@Priority(-1)
public class BatchLookupService extends AbstractService implements IBatchLookupService {

  public BatchLookupService() {
  }

  public LookupRow[][] getBatchDataByKey(BatchLookupCall batch) throws ProcessingException {
    LookupCall[] calls = batch.getCallBatch();
    LookupRow[][] resultArray = new LookupRow[calls.length][];
    for (int i = 0; i < calls.length; i++) {
      resultArray[i] = calls[i].getDataByKey();
    }
    return resultArray;
  }

  public LookupRow[][] getBatchDataByText(BatchLookupCall batch) throws ProcessingException {
    LookupCall[] calls = batch.getCallBatch();
    LookupRow[][] resultArray = new LookupRow[calls.length][];
    for (int i = 0; i < calls.length; i++) {
      resultArray[i] = calls[i].getDataByText();
    }
    return resultArray;
  }

  public LookupRow[][] getBatchDataByAll(BatchLookupCall batch) throws ProcessingException {
    LookupCall[] calls = batch.getCallBatch();
    LookupRow[][] resultArray = new LookupRow[calls.length][];
    for (int i = 0; i < calls.length; i++) {
      resultArray[i] = calls[i].getDataByAll();
    }
    return resultArray;
  }

  public LookupRow[][] getBatchDataByRec(BatchLookupCall batch) throws ProcessingException {
    LookupCall[] calls = batch.getCallBatch();
    LookupRow[][] resultArray = new LookupRow[calls.length][];
    for (int i = 0; i < calls.length; i++) {
      resultArray[i] = calls[i].getDataByRec();
    }
    return resultArray;
  }

}
