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

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCallFetcher;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

public class ContentAssistFieldDataFetcher<LOOKUP_KEY> extends AbstractContentAssistFieldLookupRowFetcher<LOOKUP_KEY> {

  private IFuture<?> m_dataLoadJob;

  public ContentAssistFieldDataFetcher(IContentAssistField<?, LOOKUP_KEY> contentAssistField) {
    super(contentAssistField);

  }

  @Override
  public void update(final String searchText, boolean selectCurrentValue, boolean synchronous) {
    String text = searchText;
    if (text == null) {
      text = "";
    }
    final String textNonNull = text;
    final int maxCount = getContentAssistField().getBrowseMaxRowCount();
    if (m_dataLoadJob != null) {
      m_dataLoadJob.cancel(true);
    }
    ILookupCallFetcher<LOOKUP_KEY> fetcher = new P_LookupCallFetcher(searchText, selectCurrentValue);
    // go async/sync
    if (synchronous) {
      try {
        List<? extends ILookupRow<LOOKUP_KEY>> rows;
        if (getContentAssistField().getWildcard().equals(text)) {
          rows = getContentAssistField().callBrowseLookup(text, maxCount > 0 ? maxCount + 1 : 0);
        }
        else if (text.length() == 0) {
          rows = getContentAssistField().callBrowseLookup(text, maxCount > 0 ? maxCount + 1 : 0);
        }
        else {
          rows = getContentAssistField().callTextLookup(text, maxCount > 0 ? maxCount + 1 : 0);
        }
        fetcher.dataFetched(rows, null);
      }
      catch (RuntimeException e) {
        fetcher.dataFetched(null, e);
      }
    }
    else {
      if (getContentAssistField().getWildcard().equals(textNonNull)) {
        m_dataLoadJob = getContentAssistField().callBrowseLookupInBackground(textNonNull, maxCount > 0 ? maxCount + 1 : 0, fetcher);
      }
      else if (textNonNull.length() == 0) {
        m_dataLoadJob = getContentAssistField().callBrowseLookupInBackground(textNonNull, maxCount > 0 ? maxCount + 1 : 0, fetcher);
      }
      else {
        m_dataLoadJob = getContentAssistField().callTextLookupInBackground(textNonNull, maxCount > 0 ? maxCount + 1 : 0, fetcher);
      }
    }
  }

  private class P_LookupCallFetcher implements ILookupCallFetcher<LOOKUP_KEY> {
    private String m_searchText;
    private boolean m_selectCurrentValue;

    private P_LookupCallFetcher(String searchText, boolean selectCurrentValue) {
      m_searchText = searchText;
      m_selectCurrentValue = selectCurrentValue;

    }

    @Override
    public void dataFetched(List<? extends ILookupRow<LOOKUP_KEY>> rows, RuntimeException failed) {
      setResult(new ContentAssistFieldDataFetchResult<LOOKUP_KEY>(rows, failed, m_searchText, m_selectCurrentValue));
    }
  }
}
