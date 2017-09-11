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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.List;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.exception.IProcessingStatus;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IBiConsumer;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HierarchicalSmartFieldDataFetcher<LOOKUP_KEY> extends AbstractSmartFieldLookupRowFetcher<LOOKUP_KEY> {
  private static final Logger LOG = LoggerFactory.getLogger(HierarchicalSmartFieldDataFetcher.class);

  public HierarchicalSmartFieldDataFetcher(ISmartField<LOOKUP_KEY> smartField) {
    super(smartField);
  }

  @Override
  public void update(ISmartFieldSearchParam<LOOKUP_KEY> query, boolean blocking) {
    IFuture<Void> fRes =
        scheduleLookup(query)
            .whenDoneSchedule(updateResult(query), newModelJobInput());
    if (blocking) {
      LookupJobHelper.awaitDone(fRes);
    }
  }

  private IBiConsumer<List<ILookupRow<LOOKUP_KEY>>, Throwable> updateResult(final ISmartFieldSearchParam<LOOKUP_KEY> query) {
    return new IBiConsumer<List<ILookupRow<LOOKUP_KEY>>, Throwable>() {

      @Override
      public void accept(List<ILookupRow<LOOKUP_KEY>> rows, Throwable error) {
        SmartFieldDataFetchResult<LOOKUP_KEY> result = new SmartFieldDataFetchResult<>(rows, error, query);
        if (result.getException() != null) {
          logException(result.getException());
        }
        setResult(result);
      }
    };
  }

  /**
   * Exceptions are handled differently in smartfields (make sure it is logged)
   */
  protected void logException(Throwable e) {
    if (e instanceof VetoException) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("", e);
      }
      else {
        LOG.info("{}: {}", e.getClass().getSimpleName(), e.getMessage());
      }
    }
    else if (e instanceof ProcessingException) {
      switch (((ProcessingException) e).getStatus().getSeverity()) {
        case IProcessingStatus.INFO:
        case IProcessingStatus.OK:
          LOG.info("", e);
          break;
        case IProcessingStatus.WARNING:
          LOG.warn("", e);
          break;
        default:
          LOG.error("", e);
          break;
      }
    }
    else {
      LOG.error("", e);
    }
  }

  private JobInput newModelJobInput() {
    return ModelJobs.newInput(ClientRunContexts.copyCurrent())
        .withExceptionHandling(null, false);
  }

  protected IFuture<List<ILookupRow<LOOKUP_KEY>>> scheduleLookup(ISmartFieldSearchParam<LOOKUP_KEY> query) {
    if (query.isByParentSearch()) {
      return getSmartField().callSubTreeLookupInBackground(query.getParentKey(), false);
    }
    else if (isTextLookup(query.getSearchQuery()) && !query.isSelectCurrentValue()) {
      return getSmartField().callTextLookupInBackground(query.getSearchQuery(), true);
    }
    else {
      return getSmartField().callBrowseLookupInBackground(true);
    }
  }

  protected boolean isTextLookup(String searchText) {
    return !StringUtility.isNullOrEmpty(searchText) && !getSmartField().getWildcard().equals(searchText);
  }

}
