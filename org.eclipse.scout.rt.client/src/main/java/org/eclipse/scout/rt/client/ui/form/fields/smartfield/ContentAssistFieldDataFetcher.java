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
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRowFetchedCallback;

public class ContentAssistFieldDataFetcher<LOOKUP_KEY> extends AbstractContentAssistFieldLookupRowFetcher<LOOKUP_KEY> {

  private volatile IFuture<?> m_future;

  public ContentAssistFieldDataFetcher(IContentAssistField<?, LOOKUP_KEY> contentAssistField) {
    super(contentAssistField);

  }

  @Override
  public void update(final String searchText, boolean selectCurrentValue, boolean synchronous) {
    String text = searchText;
    if (text == null) {
      text = "";
    }

    // Cancel potential running fetcher
    final IFuture<?> future = m_future;
    if (future != null) {
      future.cancel(true);
    }

    final String textNonNull = text;
    final int maxCount = getContentAssistField().getBrowseMaxRowCount();
    final ILookupRowFetchedCallback<LOOKUP_KEY> callback = new P_LookupCallDataCallback(searchText, selectCurrentValue);
    final int maxRowCount = (maxCount > 0 ? maxCount + 1 : 0);

    if (synchronous) {
      try {
        if (getContentAssistField().getWildcard().equals(text) || text.isEmpty()) {
          callback.onSuccess(getContentAssistField().callBrowseLookup(text, maxRowCount));
        }
        else {
          callback.onSuccess(getContentAssistField().callTextLookup(text, maxRowCount));
        }
      }
      catch (RuntimeException e) {
        callback.onFailure(e);
      }
    }
    else {
      if (getContentAssistField().getWildcard().equals(textNonNull) || textNonNull.isEmpty()) {
        m_future = getContentAssistField().callBrowseLookupInBackground(textNonNull, maxRowCount, callback);
      }
      else {
        m_future = getContentAssistField().callTextLookupInBackground(textNonNull, maxRowCount, callback);
      }
    }
  }

  private class P_LookupCallDataCallback implements ILookupRowFetchedCallback<LOOKUP_KEY> {
    private String m_searchText;
    private boolean m_selectCurrentValue;

    private P_LookupCallDataCallback(String searchText, boolean selectCurrentValue) {
      m_searchText = searchText;
      m_selectCurrentValue = selectCurrentValue;
    }

    @Override
    public void onSuccess(List<? extends ILookupRow<LOOKUP_KEY>> rows) {
      m_future = null;
      setResult(new ContentAssistFieldDataFetchResult<>(rows, null, m_searchText, m_selectCurrentValue));
    }

    @Override
    public void onFailure(RuntimeException exception) {
      m_future = null;
      setResult(new ContentAssistFieldDataFetchResult<LOOKUP_KEY>(null, exception, m_searchText, m_selectCurrentValue));
    }
  }
}
