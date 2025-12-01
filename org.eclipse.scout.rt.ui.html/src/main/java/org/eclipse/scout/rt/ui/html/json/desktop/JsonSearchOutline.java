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

import java.util.Map;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.desktop.outline.ISearchOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.ISearchPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.ISearchState;
import org.eclipse.scout.rt.client.ui.desktop.outline.SearchOutlineEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.SearchOutlineEventListener;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterUtility;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.json.JSONObject;

public class JsonSearchOutline<SEARCH_OUTLINE extends ISearchOutline> extends JsonOutline<SEARCH_OUTLINE> {

  public static final String EVENT_SEARCH = "search";
  public static final String EVENT_RESET_SEARCH = "resetSearch";

  private SearchOutlineEventListener m_listener;

  public JsonSearchOutline(SEARCH_OUTLINE model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "SearchOutline";
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_listener != null) {
      throw new IllegalStateException();
    }
    m_listener = new P_SearchOutlineEventListener();
    getModel().addSearchOutlineEventListener(m_listener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    getModel().removeSearchOutlineEventListener(m_listener);
  }

  @Override
  protected void initJsonProperties(SEARCH_OUTLINE model) {
    super.initJsonProperties(model);

    putJsonProperty(new JsonAdapterProperty<>(ISearchOutline.PROP_SEARCH_STATES, model, getUiSession()) {

      @Override
      protected Map<ISearchPage, ISearchState> modelValue() {
        return getModel().getSearchStates();
      }

      @Override
      protected void createAdapters(Object modelValue) {
        if (modelValue == null) {
          return;
        }
        if (modelValue instanceof Map) {
          ((Map<?, ?>) modelValue).values().forEach(this::createAdapter);
          return;
        }
        throw new IllegalArgumentException("modelValue must be a Map");
      }

      @Override
      protected void disposeObsoleteAdapters(Object newModels) {
        if (newModels == null) {
          return;
        }
        if (newModels instanceof Map) {
          super.disposeObsoleteAdapters(((Map<?, ?>) newModels).values());
          return;
        }
        throw new IllegalArgumentException("modelValue must be a Map");
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        if (value == null) {
          return null;
        }
        if (value instanceof Map) {
          JSONObject json = new JSONObject();
          //noinspection unchecked
          ((Map<ISearchPage, ISearchState>) value).forEach((page, searchState) -> json.put(getOrCreateNodeId(page), JsonAdapterUtility.getAdapterIdForModel(getUiSession(), searchState, getParentJsonAdapter(), getFilter())));
          return json;
        }
        throw new IllegalArgumentException("modelValue must be a Map");
      }
    });

    putJsonProperty(new JsonProperty<>(ISearchOutline.PROP_SEARCH_QUERY, model) {
      @Override
      protected String modelValue() {
        return getModel().getSearchQuery();
      }
    });
    putJsonProperty(new JsonProperty<>(ISearchOutline.PROP_MAX_SEARCH_QUERY_LENGTH, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getMaxSearchQueryLength();
      }
    });
    putJsonProperty(new JsonProperty<>(ISearchOutline.PROP_MIN_SEARCH_TOKEN_LENGTH, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getMinSearchTokenLength();
      }
    });
    putJsonProperty(new JsonProperty<>(ISearchOutline.PROP_REQUEST_FOCUS_QUERY_FIELD, model) {
      @Override
      protected String modelValue() {
        return null; // This property is not really a property, but an event, therefore it does not have a value
      }
    });
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    switch (event.getType()) {
      case EVENT_SEARCH -> handleUiSearch(event);
      case EVENT_RESET_SEARCH -> handleUiResetSearch(event);
      case null, default -> super.handleUiEvent(event);
    }
  }

  protected void handleUiSearch(JsonEvent event) {
    getModel().getUIFacade().fireSearchFromUI();
  }

  protected void handleUiResetSearch(JsonEvent event) {
    getModel().getUIFacade().fireResetSearchFromUI();
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    switch (propertyName) {
      case ISearchOutline.PROP_SEARCH_QUERY -> {
        String searchQuery = data.getString(propertyName);
        addPropertyEventFilterCondition(propertyName, searchQuery);
        getModel().getUIFacade().setSearchQueryFromUI(searchQuery);
      }
      case null, default -> super.handleUiPropertyChange(propertyName, data);
    }
  }

  protected void handleModelSearchOutlineEvent(SearchOutlineEvent event) {
    switch (event.getType()) {
      case SearchOutlineEvent.TYPE_SEARCH_EVENT -> addActionEvent("search");
      default -> throw new IllegalArgumentException("Unsupported event type");
    }
  }

  protected class P_SearchOutlineEventListener implements SearchOutlineEventListener {
    @Override
    public void handle(SearchOutlineEvent e) {
      ModelJobs.assertModelThread();
      handleModelSearchOutlineEvent(e);
    }
  }
}
