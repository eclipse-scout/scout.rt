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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRowFetchedCallback;

public class SmartFieldDataFetcher<LOOKUP_KEY> extends AbstractSmartFieldLookupRowFetcher<LOOKUP_KEY> {

  public SmartFieldDataFetcher(ISmartField<LOOKUP_KEY> smartField) {
    super(smartField);

  }

  @Override
  public void update(ISmartFieldSearchParam<LOOKUP_KEY> query, boolean synchronous) {
    String searchText = query.getSearchQuery();
    String text = searchText;
    if (text == null) {
      text = "";
    }

    final String textNonNull = text;
    final int maxCount = getSmartField().getBrowseMaxRowCount();
    final ILookupRowFetchedCallback<LOOKUP_KEY> callback = new P_LookupCallDataCallback(query);
    final int maxRowCount = (maxCount > 0 ? maxCount + 1 : 0);

    if (synchronous) {
      try {
        if (getSmartField().getWildcard().equals(text) || text.isEmpty()) {
          callback.onSuccess(getSmartField().callBrowseLookup(text, maxRowCount));
        }
        else {
          callback.onSuccess(getSmartField().callTextLookup(text, maxRowCount));
        }
      }
      catch (RuntimeException e) {
        callback.onFailure(e);
      }
    }
    else {
      if (getSmartField().getWildcard().equals(textNonNull) || textNonNull.isEmpty()) {
        getSmartField().callBrowseLookupInBackground(textNonNull, maxRowCount, callback);
      }
      else {
        getSmartField().callTextLookupInBackground(textNonNull, maxRowCount, callback);
      }
    }
  }

  private final class P_LookupCallDataCallback implements ILookupRowFetchedCallback<LOOKUP_KEY> {
    private final ISmartFieldSearchParam<LOOKUP_KEY> m_param;

    private P_LookupCallDataCallback(ISmartFieldSearchParam<LOOKUP_KEY> param) {
      m_param = param;
      setResult(null);
    }

    @Override
    public void onSuccess(List<? extends ILookupRow<LOOKUP_KEY>> rows) {
      setResult(new SmartFieldDataFetchResult<>(new ArrayList<ILookupRow<LOOKUP_KEY>>(rows), null, m_param));
    }

    @Override
    public void onFailure(RuntimeException exception) {
      setResult(new SmartFieldDataFetchResult<LOOKUP_KEY>(null, exception, m_param));
    }
  }
}
