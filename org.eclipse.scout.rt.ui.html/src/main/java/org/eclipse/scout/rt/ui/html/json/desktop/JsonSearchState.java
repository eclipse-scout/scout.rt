/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.desktop.outline.ISearchState;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.json.JSONObject;

public class JsonSearchState<SEARCH_STATE extends ISearchState> extends AbstractJsonPropertyObserver<SEARCH_STATE> {

  public JsonSearchState(SEARCH_STATE model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "SearchState";
  }

  @Override
  protected void initJsonProperties(SEARCH_STATE model) {
    super.initJsonProperties(model);

    putJsonProperty(new JsonProperty<>(ISearchState.PROP_RESULT_COUNT, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getResultCount();
      }
    });
    putJsonProperty(new JsonProperty<>(ISearchState.PROP_LIMITED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isLimited();
      }
    });
    putJsonProperty(new JsonProperty<>(ISearchState.PROP_PENDING, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isPending();
      }
    });
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    switch (propertyName) {
      case ISearchState.PROP_RESULT_COUNT -> {
        int resultCount = data.getInt(propertyName);
        addPropertyEventFilterCondition(propertyName, resultCount);
        getModel().getUIFacade().setResultCountFromUI(resultCount);
      }
      case ISearchState.PROP_LIMITED -> {
        boolean limited = data.getBoolean(propertyName);
        addPropertyEventFilterCondition(propertyName, limited);
        getModel().getUIFacade().setLimitedFromUI(limited);
      }
      case ISearchState.PROP_PENDING -> {
        boolean pending = data.getBoolean(propertyName);
        addPropertyEventFilterCondition(propertyName, pending);
        getModel().getUIFacade().setPendingFromUI(pending);
      }
      case null, default -> super.handleUiPropertyChange(propertyName, data);
    }
  }
}
