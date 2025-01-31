/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.List;
import java.util.function.BiConsumer;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.services.lookup.IQueryParam;
import org.eclipse.scout.rt.client.services.lookup.IQueryParam.QueryBy;
import org.eclipse.scout.rt.client.services.lookup.LookupCallResult;
import org.eclipse.scout.rt.platform.exception.IProcessingStatus;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HierarchicalSmartFieldDataFetcher<LOOKUP_KEY> extends AbstractSmartFieldLookupRowFetcher<LOOKUP_KEY> {
  private static final Logger LOG = LoggerFactory.getLogger(HierarchicalSmartFieldDataFetcher.class);

  public HierarchicalSmartFieldDataFetcher(ISmartField<LOOKUP_KEY> smartField) {
    super(smartField);
  }

  @Override
  public void update(IQueryParam<LOOKUP_KEY> query, boolean blocking) {
    IFuture<Void> fRes =
        scheduleLookup(query)
            .whenDoneSchedule(updateResult(query), newModelJobInput());
    if (blocking) {
      LookupJobHelper.awaitDone(fRes);
    }
  }

  private BiConsumer<List<ILookupRow<LOOKUP_KEY>>, Throwable> updateResult(final IQueryParam<LOOKUP_KEY> query) {
    return (rows, error) -> {
      LookupCallResult<LOOKUP_KEY> result = new LookupCallResult<>(rows, query, error);
      if (error != null) {
        logException(error);
      }
      setResult(result);
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

  protected IFuture<List<ILookupRow<LOOKUP_KEY>>> scheduleLookup(IQueryParam<LOOKUP_KEY> query) {
    if (query.is(QueryBy.REC)) {
      return getSmartField().callSubTreeLookupInBackground(query.getKey(), false);
    }
    else if (query.is(QueryBy.TEXT)) {
      return getSmartField().callTextLookupInBackground(query.getText(), true);
    }
    else if (query.is(QueryBy.ALL)) {
      return getSmartField().callBrowseLookupInBackground(true);
    }
    else if (query.is(QueryBy.KEY)) {
      return getSmartField().callKeyLookupInBackground(query.getKey(), true);
    }
    else {
      throw new IllegalStateException();
    }
  }
}
