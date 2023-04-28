/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.calendar;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarAdapter;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarEvent;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarListener;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendarUIFacade;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.platform.util.Range;
import org.eclipse.scout.rt.shared.services.common.calendar.CalendarResourceDo;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonWidget;
import org.eclipse.scout.rt.ui.html.json.FilteredJsonAdapterIds;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonDateRange;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;
import org.eclipse.scout.rt.ui.html.json.menu.IJsonContextMenuOwner;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonCalendar<CALENDAR extends ICalendar> extends AbstractJsonWidget<CALENDAR> implements IJsonContextMenuOwner {

  private static final Logger LOG = LoggerFactory.getLogger(JsonCalendar.class);

  // from model
  public static final String EVENT_CALENDAR_CHANGED = "calendarChanged";
  public static final String EVENT_CALENDAR_CHANGED_BATCH = "calendarChangedBatch";

  // from UI
  private static final String EVENT_COMPONENT_ACTION = "componentAction";
  private static final String EVENT_COMPONENT_MOVE = "componentMove";
  private static final String EVENT_RELOAD = "reload";
  private static final String EVENT_SELECTION_CHANGE = "selectionChange";
  private static final String EVENT_VIEW_RANGE_CHANGE = "viewRangeChange";
  private static final String EVENT_SELECTED_RANGE_CHANGE = "selectedRangeChange";
  private static final String EVENT_MODEL_CHANGE = "modelChange";
  private static final String EVENT_RESOURCE_VISIBILITY_CHANGE = "resourceVisibilityChange";
  private static final String EVENT_SELECTED_RESOURCE_CHANGE = "selectedResourceChange";

  private CalendarListener m_calendarListener;
  private JsonContextMenu<IContextMenu> m_jsonContextMenu;

  public JsonCalendar(CALENDAR model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Calendar";
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_calendarListener != null) {
      throw new IllegalStateException();
    }
    m_calendarListener = new P_CalendarListener();
    getModel().addCalendarListener(m_calendarListener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_calendarListener == null) {
      throw new IllegalStateException();
    }
    getModel().removeCalendarListener(m_calendarListener);
    m_calendarListener = null;
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getSelectedComponent());
    attachAdapters(getModel().getComponents());
    m_jsonContextMenu = new JsonContextMenu<>(getModel().getContextMenu(), this);
    m_jsonContextMenu.init();
  }

  @Override
  protected void disposeChildAdapters() {
    m_jsonContextMenu.dispose();
    super.disposeChildAdapters();
  }

  @Override
  protected void initJsonProperties(CALENDAR model) {
    putJsonProperty(new JsonAdapterProperty<>(ICalendar.PROP_COMPONENTS, model, getUiSession()) {
      @Override
      protected Set<? extends CalendarComponent> modelValue() {
        return getModel().getComponents();
      }
    });
    putJsonProperty(new JsonAdapterProperty<>(ICalendar.PROP_SELECTED_COMPONENT, model, getUiSession()) {
      @Override
      protected CalendarComponent modelValue() {
        return getModel().getSelectedComponent();
      }

      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return new JsonAdapterPropertyConfigBuilder().disposeOnChange(false).build();
      }
    });
    putJsonProperty(new JsonProperty<>(ICalendar.PROP_DISPLAY_MODE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getDisplayMode();
      }
    });
    putJsonProperty(new JsonProperty<>(ICalendar.PROP_DISPLAY_CONDENSED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isDisplayCondensed();
      }
    });
    putJsonProperty(new JsonProperty<>(ICalendar.PROP_TITLE, model) {
      @Override
      protected String modelValue() {
        return getModel().getTitle();
      }
    });
    putJsonProperty(new JsonProperty<>(ICalendar.PROP_RESOURCES, model) {
      @Override
      protected List<CalendarResourceDo> modelValue() {
        return getModel().getResources();
      }

      @SuppressWarnings("unchecked")
      @Override
      public Object prepareValueForToJson(Object value) {
        return resourcesToJsonArray(((List<IDoEntity>) value));
      }
    });
    putJsonProperty(new JsonProperty<>(ICalendar.PROP_VIEW_RANGE, model) {
      @Override
      protected Range<Date> modelValue() {
        return getModel().getViewRange();
      }

      @SuppressWarnings("unchecked")
      @Override
      public Object prepareValueForToJson(Object value) {
        if (value == null) {
          return null;
        }
        return new JsonDateRange((Range<Date>) value).toJson();
      }
    });
    putJsonProperty(new JsonProperty<>(ICalendar.PROP_SELECTED_DATE, model) {
      @Override
      protected Date modelValue() {
        return getModel().getSelectedDate();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return new JsonDate((Date) value).asJsonString();
      }
    });
    putJsonProperty(new JsonProperty<>(ICalendar.PROP_SELECTED_RANGE, model) {
      @Override
      protected Range<Date> modelValue() {
        return getModel().getSelectedRange();
      }

      @SuppressWarnings("unchecked")
      @Override
      public Object prepareValueForToJson(Object value) {
        if (value == null) {
          return null;
        }
        return new JsonDateRange((Range<Date>) value).toJson();
      }
    });
    putJsonProperty(new JsonProperty<>(ICalendar.PROP_LOAD_IN_PROGRESS, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isLoadInProgress();
      }
    });
    putJsonProperty(new JsonProperty<>(ICalendar.PROP_START_HOUR, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getStartHour();
      }
    });
    putJsonProperty(new JsonProperty<>(ICalendar.PROP_USE_OVERFLOW_CELLS, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().getUseOverflowCells();
      }
    });
    putJsonProperty(new JsonProperty<>(ICalendar.PROP_SHOW_DISPLAY_MODE_SELECTION, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().getShowDisplayModeSelection();
      }
    });
    putJsonProperty(new JsonProperty<>(ICalendar.PROP_RANGE_SELECTION_ALLOWED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().getRangeSelectionAllowed();
      }
    });
    putJsonProperty(new JsonProperty<>(ICalendar.PROP_MENU_INJECTION_TARGET, model) {
      @Override
      protected String modelValue() {
        return getUiSession().getJsonAdapters(getModel().getMenuInjectionTarget()).stream()
            .findAny()
            .map(IJsonAdapter::getId)
            .orElse(null);
      }
    });
    putJsonProperty(new JsonProperty<>(ICalendar.PROP_SHOW_CALENDAR_SIDEBAR, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().getShowCalendarSidebar();
      }
    });
    putJsonProperty(new JsonProperty<>(ICalendar.PROP_SHOW_RESOURCE_PANEL, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().getShowResourcePanel();
      }
    });
    putJsonProperty(new JsonProperty<>(ICalendar.PROP_SHOW_LIST_PANEL, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().getShowListPanel();
      }
    });
  }

  @SuppressWarnings("unchecked")
  public <C extends CalendarComponent> JsonCalendarComponent<C> resolveCalendarComponent(String adapterId) {
    if (adapterId == null) {
      return null;
    }
    return (JsonCalendarComponent<C>) getUiSession().getJsonAdapter(adapterId);
  }

  protected JSONArray resourcesToJsonArray(List<IDoEntity> resources) {
    String str = BEANS.get(IDataObjectMapper.class).writeValue(resources);
    return new JSONArray(str);
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (ICalendar.PROP_SHOW_CALENDAR_SIDEBAR.equals(propertyName)) {
      getModel().getUIFacade().setShowYearPanelFromUI(data.getBoolean(propertyName));
    }
    else if (ICalendar.PROP_SHOW_RESOURCE_PANEL.equals(propertyName)) {
      getModel().getUIFacade().setShowResourcePanelFromUI(data.getBoolean(propertyName));
    }
    else if (ICalendar.PROP_SHOW_LIST_PANEL.equals(propertyName)) {
      getModel().getUIFacade().setShowListPanelFromUI(data.getBoolean(propertyName));
    }
    else {
      super.handleUiPropertyChange(propertyName, data);
    }
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_COMPONENT_ACTION.equals(event.getType())) {
      handleUiComponentAction(event);
    }
    else if (EVENT_COMPONENT_MOVE.equals(event.getType())) {
      handleUiComponentMove(event);
    }
    else if (EVENT_RELOAD.equals(event.getType())) {
      handleUiReload(event);
    }
    else if (EVENT_SELECTION_CHANGE.equals(event.getType())) {
      handleUiSelectionChange(event);
    }
    else if (EVENT_MODEL_CHANGE.equals(event.getType())) {
      handleUiModelChange(event);
    }
    else if (EVENT_VIEW_RANGE_CHANGE.equals(event.getType())) {
      handleUiViewRangeChange(event);
    }
    else if (EVENT_SELECTED_RANGE_CHANGE.equals(event.getType())) {
      handleUiSelectedRangeChange(event);
    }
    else if (EVENT_RESOURCE_VISIBILITY_CHANGE.equals(event.getType())) {
      handleUiResourceVisibilityChange(event);
    }
    else if (EVENT_SELECTED_RESOURCE_CHANGE.equals(event.getType())) {
      handleUiSelectedResourceChange(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiComponentAction(JsonEvent event) {
    getModel().getUIFacade().fireComponentActionFromUI();
  }

  protected void handleUiComponentMove(JsonEvent event) {
    JSONObject data = event.getData();
    Date fromDate = toJavaDate(data, "fromDate");
    Date toDate = toJavaDate(data, "toDate");
    String componentId = data.optString("componentId", null);

    JsonCalendarComponent<CalendarComponent> component = resolveCalendarComponent(componentId);
    if (component != null) {
      getModel().getUIFacade().fireComponentMoveFromUI(component.getModel(), fromDate, toDate);
    }
    else if (componentId != null) {
      LOG.info("Unkown component with ID {} [event='{}']", componentId, EVENT_COMPONENT_MOVE);
    }
  }

  protected void handleUiReload(JsonEvent event) {
    getModel().getUIFacade().fireReloadFromUI();
  }

  protected void handleUiSelectionChange(JsonEvent event) {
    JSONObject data = event.getData();
    Date selectedDate = toJavaDate(data, "date");
    String componentId = data.optString("componentId", null);

    CalendarComponent selectedComponent = null;
    JsonCalendarComponent<CalendarComponent> jsonComponent = resolveCalendarComponent(componentId);
    if (jsonComponent != null) {
      selectedComponent = jsonComponent.getModel();
      addPropertyEventFilterCondition(ICalendar.PROP_SELECTED_COMPONENT, selectedComponent);
    }
    else if (componentId != null) {
      LOG.info("Unkown component with ID {} [event='{}']", componentId, EVENT_SELECTION_CHANGE);
    }

    addPropertyEventFilterCondition(ICalendar.PROP_SELECTED_DATE, selectedDate);
    getModel().getUIFacade().setSelectionFromUI(selectedDate, selectedComponent);
    LOG.debug("date={} componentId={}", selectedDate, componentId);
  }

  protected void handleUiModelChange(JsonEvent event) {
    JSONObject data = event.getData();

    int displayMode = data.optInt("displayMode");
    addPropertyEventFilterCondition(ICalendar.PROP_DISPLAY_MODE, displayMode);
    ICalendarUIFacade uiFacade = getModel().getUIFacade();
    uiFacade.setDisplayModeFromUI(displayMode);

    Range<Date> viewRange = extractViewRange(data);
    addPropertyEventFilterCondition(ICalendar.PROP_VIEW_RANGE, viewRange);
    uiFacade.setViewRangeFromUI(viewRange);

    Date selectedDate = extractSelectedDate(data);
    addPropertyEventFilterCondition(ICalendar.PROP_SELECTED_DATE, selectedDate);
    uiFacade.setSelectedDateFromUI(selectedDate);

    Range<Date> selectedRange = extractSelectedRange(data);
    addPropertyEventFilterCondition(ICalendar.PROP_SELECTED_RANGE, selectedRange);
    uiFacade.setSelectedRangeFromUI(selectedRange);

    LOG.debug("displayMode={} viewRange={} selectedDate={} selectedRange={}", displayMode, viewRange, selectedDate, selectedRange);
  }

  /**
   * The current calendar model has a strange behavior (bug?): when the view-range changes
   */
  protected void handleUiViewRangeChange(JsonEvent event) {
    Range<Date> viewRange = extractViewRange(event.getData());
    addPropertyEventFilterCondition(ICalendar.PROP_VIEW_RANGE, viewRange);
    getModel().getUIFacade().setViewRangeFromUI(viewRange);
    LOG.debug("viewRange={}", viewRange);
  }

  protected Range<Date> extractViewRange(JSONObject data) {
    JSONObject viewRange = data.optJSONObject("viewRange");
    Date fromDate = toJavaDate(viewRange, "from");
    Date toDate = toJavaDate(viewRange, "to");
    return new Range<>(fromDate, toDate);
  }

  protected Date extractSelectedDate(JSONObject data) {
    return toJavaDate(data, "selectedDate");
  }

  protected void handleUiSelectedRangeChange(JsonEvent event) {
    Range<Date> selectedRange = extractSelectedRange(event.getData());
    addPropertyEventFilterCondition(ICalendar.PROP_SELECTED_RANGE, selectedRange);
    getModel().getUIFacade().setSelectedRangeFromUI(selectedRange);
    LOG.debug("selectedRange={}", selectedRange);
  }

  protected Range<Date> extractSelectedRange(JSONObject data) {
    JSONObject selectedRange = data.optJSONObject("selectedRange");
    Date fromDate = toJavaDate(selectedRange, "from");
    Date toDate = toJavaDate(selectedRange, "to");
    return new Range<>(fromDate, toDate);
  }

  protected Date toJavaDate(JSONObject data, String propertyName) {
    if (data == null || propertyName == null) {
      return null;
    }
    return new JsonDate(data.optString(propertyName, null)).asJavaDate();
  }

  protected void handleUiResourceVisibilityChange(JsonEvent event) {
    Pair<String, Boolean> resourceVisibility = extractResourceVisibility(event.getData());
    getModel().getUIFacade().setResourceVisibilityFromUI(resourceVisibility.getLeft(), resourceVisibility.getRight());
    LOG.debug("resourceId={} visible={}", resourceVisibility.getLeft(), resourceVisibility.getRight());
  }

  protected void handleUiSelectedResourceChange(JsonEvent event) {
    String resourceId = event.getData().optString("resourceId");
    getModel().getUIFacade().setSelectedResourceFromUI(resourceId);
    LOG.debug("resourceId={}", resourceId);
  }

  protected Pair<String, Boolean> extractResourceVisibility(JSONObject data) {
    String resourcesId = data.optString("resourceId");
    Boolean visible = data.optBoolean("visible");
    return new ImmutablePair<>(resourcesId, visible);
  }

  @Override
  public void handleModelContextMenuChanged(FilteredJsonAdapterIds<?> filteredAdapters) {
    addPropertyChangeEvent(PROP_MENUS, filteredAdapters);
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put(PROP_MENUS, m_jsonContextMenu.childActionsToJson());
    return json;
  }

  protected class P_CalendarListener extends CalendarAdapter {

    @Override
    public void calendarChanged(CalendarEvent e) {
      ModelJobs.assertModelThread();
      addActionEvent(EVENT_CALENDAR_CHANGED, new JsonCalendarEvent(JsonCalendar.this, e).toJson());
    }

    @Override
    public void calendarChangedBatch(List<CalendarEvent> batch) {
      ModelJobs.assertModelThread();
      JSONArray jsonArray = new JSONArray();
      for (CalendarEvent event : batch) {
        jsonArray.put(new JsonCalendarEvent(JsonCalendar.this, event).toJson());
      }
      JSONObject json = new JSONObject();
      json.put("batch", jsonArray);
      addActionEvent(EVENT_CALENDAR_CHANGED_BATCH, json);
    }
  }
}
