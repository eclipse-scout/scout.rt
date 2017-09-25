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

import org.eclipse.scout.rt.client.ui.form.fields.smartfield.result.IQueryParam;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.result.QueryParam;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.result.SmartFieldResult;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRowFetchedCallback;

public class SmartFieldDataFetcher<LOOKUP_KEY> extends AbstractSmartFieldLookupRowFetcher<LOOKUP_KEY> {

  public SmartFieldDataFetcher(ISmartField<LOOKUP_KEY> smartField) {
    super(smartField);

  }

  @Override
  public void update(IQueryParam queryParam, boolean synchronous) {
    int maxCount = getSmartField().getBrowseMaxRowCount();
    ILookupRowFetchedCallback<LOOKUP_KEY> callback = new P_LookupCallDataCallback(queryParam);
    int maxRowCount = (maxCount > 0 ? maxCount + 1 : 0);

    if (synchronous) {
      try {
        if (QueryParam.isKeyQuery(queryParam)) {
          callback.onSuccess(getSmartField().callKeyLookup(QueryParam.getKey(queryParam)));
        }
        else if (QueryParam.isAllQuery(queryParam)) {
          callback.onSuccess(getSmartField().callBrowseLookup(QueryParam.getText(queryParam), maxRowCount));
        }
        else if (QueryParam.isTextQuery(queryParam)) {
          callback.onSuccess(getSmartField().callTextLookup(QueryParam.getText(queryParam), maxRowCount));
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
      if (QueryParam.isKeyQuery(queryParam)) {
        getSmartField().callKeyLookupInBackground(QueryParam.getKey(queryParam), callback);
      }
      else if (QueryParam.isAllQuery(queryParam)) {
        getSmartField().callBrowseLookupInBackground(QueryParam.getText(queryParam), maxRowCount, callback);
      }
      else if (QueryParam.isTextQuery(queryParam)) {
        getSmartField().callTextLookupInBackground(QueryParam.getText(queryParam), maxRowCount, callback);
      }
      else {
        throw new IllegalStateException();
      }
    }
  }

  private final class P_LookupCallDataCallback implements ILookupRowFetchedCallback<LOOKUP_KEY> {
    private final IQueryParam m_param;

    private P_LookupCallDataCallback(IQueryParam param) {
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
