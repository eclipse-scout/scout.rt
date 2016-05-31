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
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IBiFunction;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HierachycalContentAssistDataFetcher<LOOKUP_KEY> extends AbstractContentAssistFieldLookupRowFetcher<LOOKUP_KEY> {
  private static final Logger LOG = LoggerFactory.getLogger(HierachycalContentAssistDataFetcher.class);

  public HierachycalContentAssistDataFetcher(IContentAssistField<?, LOOKUP_KEY> contentAssistField) {
    super(contentAssistField);
  }

  @Override
  public void update(IContentAssistSearchParam<LOOKUP_KEY> query, boolean blocking) {
    IFuture<Void> fRes =
        scheduleLookup(query)
            .whenDoneSchedule(createResult(query), newJobInput())
            .whenDoneSchedule(updateResult(), newModelJobInput());
    if (blocking) {
      LookupJobHelper.awaitDone(fRes);
    }
  }

  private IBiFunction<List<ILookupRow<LOOKUP_KEY>>, Throwable, ContentAssistFieldDataFetchResult<LOOKUP_KEY>> createResult(final IContentAssistSearchParam<LOOKUP_KEY> fetchInfo) {
    return new IBiFunction<List<ILookupRow<LOOKUP_KEY>>, Throwable, ContentAssistFieldDataFetchResult<LOOKUP_KEY>>() {

      @Override
      public ContentAssistFieldDataFetchResult<LOOKUP_KEY> apply(List<ILookupRow<LOOKUP_KEY>> rows, Throwable error) {
        return new ContentAssistFieldDataFetchResult<>(rows, error, fetchInfo);
      }
    };
  }

  private IBiFunction<ContentAssistFieldDataFetchResult<LOOKUP_KEY>, Throwable, Void> updateResult() {
    return new IBiFunction<ContentAssistFieldDataFetchResult<LOOKUP_KEY>, Throwable, Void>() {

      @Override
      public Void apply(ContentAssistFieldDataFetchResult<LOOKUP_KEY> result, Throwable error) {
        if (result.getException() != null) {
          logException(result.getException());
        }
        setResult(result);
        return null;
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
        .withExceptionHandling(null, true);
  }

  private JobInput newJobInput() {
    return Jobs.newInput()
        .withRunContext(ClientRunContexts.copyCurrent())
        .withExceptionHandling(null, true);
  }

  protected IFuture<List<ILookupRow<LOOKUP_KEY>>> scheduleLookup(IContentAssistSearchParam<LOOKUP_KEY> query) {
    if (query.isByParentSearch()) {
      return getContentAssistField().callSubTreeLookupInBackground(query.getParentKey(), false);
    }
    else if (isTextLookup(query.getSearchText()) && !query.isSelectCurrentValue()) {
      return getContentAssistField().callTextLookupInBackground(query.getSearchText(), true);
    }
    else {
      return getContentAssistField().callBrowseLookupInBackground(null, true);
    }
  }

  protected boolean isTextLookup(String searchText) {
    return !StringUtility.isNullOrEmpty(searchText) && !getContentAssistField().getWildcard().equals(searchText);
  }

}
