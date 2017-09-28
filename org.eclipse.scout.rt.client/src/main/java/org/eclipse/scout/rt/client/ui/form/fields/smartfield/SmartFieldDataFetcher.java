/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.client.ui.form.fields.smartfield.result.IQueryParam;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.result.IQueryParam.QueryBy;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.result.SmartFieldResult;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRowFetchedCallback;

public class SmartFieldDataFetcher<LOOKUP_KEY> extends AbstractSmartFieldLookupRowFetcher<LOOKUP_KEY> {

  public SmartFieldDataFetcher(ISmartField<LOOKUP_KEY> smartField) {
    super(smartField);
  }

  @Override
  public void update(IQueryParam<LOOKUP_KEY> queryParam, boolean synchronous) {
    int maxCount = getSmartField().getBrowseMaxRowCount();
    ILookupRowFetchedCallback<LOOKUP_KEY> callback = new P_LookupCallDataCallback(queryParam);
    int maxRowCount = (maxCount > 0 ? maxCount + 1 : 0);

    if (synchronous) {
      try {
        if (queryParam.is(QueryBy.KEY)) {
          callback.onSuccess(getSmartField().callKeyLookup(queryParam.getKey()));
        }
        else if (queryParam.is(QueryBy.ALL)) {
          callback.onSuccess(getSmartField().callBrowseLookup(maxRowCount));
        }
        else if (queryParam.is(QueryBy.TEXT)) {
          callback.onSuccess(getSmartField().callTextLookup(queryParam.getText(), maxRowCount));
        }
        else {
          throw new IllegalStateException();
        }
      }
      catch (RuntimeException e) {
        callback.onFailure(e);
      }
    }
    else {
      if (queryParam.is(QueryBy.KEY)) {
        getSmartField().callKeyLookupInBackground(queryParam.getKey(), callback);
      }
      else if (queryParam.is(QueryBy.ALL)) {
        getSmartField().callBrowseLookupInBackground(maxRowCount, callback);
      }
      else if (queryParam.is(QueryBy.TEXT)) {
        getSmartField().callTextLookupInBackground(queryParam.getText(), maxRowCount, callback);
      }
      else {
        throw new IllegalStateException();
      }
    }
  }

  private final class P_LookupCallDataCallback implements ILookupRowFetchedCallback<LOOKUP_KEY> {
    private final IQueryParam<LOOKUP_KEY> m_param;

    private P_LookupCallDataCallback(IQueryParam<LOOKUP_KEY> param) {
      m_param = param;
      setResult(null);
    }

    @Override
    public void onSuccess(List<? extends ILookupRow<LOOKUP_KEY>> rows) {
      setResult(new SmartFieldResult<>(new ArrayList<>(rows), m_param, null));
    }

    @Override
    public void onFailure(RuntimeException exception) {
      setResult(new SmartFieldResult<>(null, m_param, exception));
    }
  }
}
