/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.desktop.outline.ISearchOutline;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;

public class JsonSearchOutline<SEARCH_OUTLINE extends ISearchOutline> extends JsonOutline<SEARCH_OUTLINE> {

  public static final String EVENT_SEARCH = "search";

  public JsonSearchOutline(SEARCH_OUTLINE model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "SearchOutline";
  }

  @Override
  protected void initJsonProperties(SEARCH_OUTLINE model) {
    super.initJsonProperties(model);

    putJsonProperty(new JsonProperty<SEARCH_OUTLINE>(ISearchOutline.PROP_SEARCH_QUERY, model) {
      @Override
      protected String modelValue() {
        return getModel().getSearchQuery();
      }
    });
    putJsonProperty(new JsonProperty<SEARCH_OUTLINE>(ISearchOutline.PROP_SEARCH_STATUS, model) {
      @Override
      protected String modelValue() {
        return getModel().getSearchStatus();
      }
    });
    putJsonProperty(new JsonProperty<SEARCH_OUTLINE>(ISearchOutline.PROP_REQUEST_FOCUS_QUERY_FIELD, model) {
      @Override
      protected String modelValue() {
        return null; // This property is not really a property, but an event, therefore it does not have a value
      }
    });
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_SEARCH.equals(event.getType())) {
      handleUiSearch(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiSearch(JsonEvent event) {
    String query = event.getData().getString("query");
    addPropertyEventFilterCondition(ISearchOutline.PROP_SEARCH_QUERY, query);
    getModel().getUIFacade().search(query);
  }
}
