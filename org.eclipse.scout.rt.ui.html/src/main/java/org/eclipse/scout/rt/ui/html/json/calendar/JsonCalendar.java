/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.calendar;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarAdapter;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarEvent;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarListener;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendarUIFacade;
import org.eclipse.scout.rt.platform.util.Range;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonDateRange;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.action.DisplayableActionFilter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;
import org.eclipse.scout.rt.ui.html.json.menu.IJsonContextMenuOwner;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonCalendar<CALENDAR extends ICalendar> extends AbstractJsonPropertyObserver<CALENDAR> implements IJsonContextMenuOwner {

  private static final Logger LOG = LoggerFactory.getLogger(JsonCalendar.class);

  // from model
  public static final String EVENT_CALENDAR_CHANGED = "calendarChanged";
  public static final String EVENT_CALENDAR_CHANGED_BATCH = "calendarChangedBatch";

  // from UI
  private static final String EVENT_COMPONENT_ACTION = "componentAction";
  private static final String EVENT_COMPONENT_MOVED = "componentMoved";
  private static final String EVENT_RELOAD = "reload";
  private static final String EVENT_SELECTION_CHANGED = "selectionChanged";
  private static final String EVENT_VIEW_RANGE_CHANGED = "viewRangeChanged";
  private static final String EVENT_MODEL_CHANGED = "modelChanged";

  private CalendarListener m_calendarListener;

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
    attachAdapter(getModel().getContextMenu(), new DisplayableActionFilter<IMenu>());
  }

  @Override
  protected void initJsonProperties(CALENDAR model) {
    putJsonProperty(new JsonAdapterProperty<CALENDAR>(ICalendar.PROP_COMPONENTS, model, getUiSession()) {
      @Override
      protected Set<? extends CalendarComponent> modelValue() {
        return getModel().getComponents();
      }
    });
    putJsonProperty(new JsonAdapterProperty<CALENDAR>(ICalendar.PROP_SELECTED_COMPONENT, model, getUiSession()) {
      @Override
      protected CalendarComponent modelValue() {
        return getModel().getSelectedComponent();
      }

      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return new JsonAdapterPropertyConfigBuilder().disposeOnChange(false).build();
      }
    });
    putJsonProperty(new JsonProperty<CALENDAR>(ICalendar.PROP_DISPLAY_MODE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getDisplayMode();
      }
    });
    putJsonProperty(new JsonProperty<CALENDAR>(ICalendar.PROP_DISPLAY_CONDENSED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isDisplayCondensed();
      }
    });
    putJsonProperty(new JsonProperty<CALENDAR>(ICalendar.PROP_TITLE, model) {
      @Override
      protected String modelValue() {
        return getModel().getTitle();
      }
    });
    putJsonProperty(new JsonProperty<CALENDAR>(ICalendar.PROP_VIEW_RANGE, model) {
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
    putJsonProperty(new JsonProperty<CALENDAR>(ICalendar.PROP_SELECTED_DATE, model) {
      @Override
      protected Date modelValue() {
        return getModel().getSelectedDate();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return new JsonDate((Date) value).asJsonString();
      }
    });
    putJsonProperty(new JsonProperty<CALENDAR>(ICalendar.PROP_LOAD_IN_PROGRESS, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isLoadInProgress();
      }
    });
    putJsonProperty(new JsonProperty<CALENDAR>(ICalendar.PROP_START_HOUR, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getStartHour();
      }
    });
    putJsonProperty(new JsonProperty<CALENDAR>(ICalendar.PROP_END_HOUR, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getEndHour();
      }
    });
    putJsonProperty(new JsonProperty<CALENDAR>(ICalendar.PROP_USE_OVERFLOW_CELLS, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().getUseOverflowCells();
      }
    });
    putJsonProperty(new JsonProperty<CALENDAR>(ICalendar.PROP_SHOW_DISPLAY_MODE_SELECTION, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().getShowDisplayModeSelection();
      }
    });
    putJsonProperty(new JsonProperty<CALENDAR>(ICalendar.PROP_MARK_NOON_HOUR, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().getMarkNoonHour();
      }
    });
    putJsonProperty(new JsonProperty<CALENDAR>(ICalendar.PROP_MARK_OUT_OF_MONTH_DAYS, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().getMarkOutOfMonthDays();
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

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_COMPONENT_ACTION.equals(event.getType())) {
      handleUiComponentAction(event);
    }
    else if (EVENT_COMPONENT_MOVED.equals(event.getType())) {
      handleUiComponentMoved(event);
    }
    else if (EVENT_RELOAD.equals(event.getType())) {
      handleUiReload(event);
    }
    else if (EVENT_SELECTION_CHANGED.equals(event.getType())) {
      handleUiSelectionChanged(event);
    }
    else if (EVENT_MODEL_CHANGED.equals(event.getType())) {
      handleUiModelChanged(event);
    }
    else if (EVENT_VIEW_RANGE_CHANGED.equals(event.getType())) {
      handleUiViewRangeChanged(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

//   FIXME bsh: Check if we need event filter here, as in the following example
//   FIXME awe: (calendar) check how the selected property is used in swing UI, decide if we can remove the property/event
//    protected void handleUiSelectionChanged(JsonEvent event) {
//      boolean selected = event.getData().getBoolean(IAction.PROP_SELECTED);
//      addPropertyEventFilterCondition(IAction.PROP_SELECTED, selected);
//      getModel().getUIFacade().setSelectedFromUI(selected);
//    }

  protected void handleUiComponentAction(JsonEvent event) {
    getModel().getUIFacade().fireComponentActionFromUI();
  }

  protected void handleUiComponentMoved(JsonEvent event) {
    JSONObject data = event.getData();
    Date newDate = toJavaDate(data, "newDate");
    String componentId = data.optString("component", null);

    JsonCalendarComponent<CalendarComponent> component = resolveCalendarComponent(componentId);
    if (component != null) {
      getModel().getUIFacade().fireComponentMovedFromUI(component.getModel(), newDate);
    }
    else if (componentId != null) {
      LOG.warn("Unkown component with ID {} [event='{}']", componentId, EVENT_COMPONENT_MOVED);
    }
  }

  protected void handleUiReload(JsonEvent event) {
    getModel().getUIFacade().fireReloadFromUI();
  }

  // FIXME awe: (calendar) replace selectedDate with selectionRange(from/to)

  protected void handleUiSelectionChanged(JsonEvent event) {
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
      LOG.warn("Unkown component with ID {} [event='{}']", componentId, EVENT_SELECTION_CHANGED);
    }

    addPropertyEventFilterCondition(ICalendar.PROP_SELECTED_DATE, selectedDate);
    getModel().getUIFacade().setSelectionFromUI(selectedDate, selectedComponent);
    LOG.debug("date={} componentId={}", selectedDate, componentId);
  }

  protected void handleUiModelChanged(JsonEvent event) {
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

    LOG.debug("displayMode={} viewRange={} selectedDate=", displayMode, viewRange, selectedDate);
  }

  /**
   * The current calendar model has a strange behavior (bug?): when the view-range changes
   */
  protected void handleUiViewRangeChanged(JsonEvent event) {
    Range<Date> viewRange = extractViewRange(event.getData());
    addPropertyEventFilterCondition(ICalendar.PROP_VIEW_RANGE, viewRange);
    getModel().getUIFacade().setViewRangeFromUI(viewRange);
    LOG.debug("viewRange={}", viewRange);
  }

  protected Range<Date> extractViewRange(JSONObject data) {
    JSONObject viewRange = data.optJSONObject("viewRange");
    Date fromDate = toJavaDate(viewRange, "from");
    Date toDate = toJavaDate(viewRange, "to");
    return new Range<Date>(fromDate, toDate);
  }

  protected Date extractSelectedDate(JSONObject data) {
    return toJavaDate(data, "selectedDate");
  }

  protected Date toJavaDate(JSONObject data, String propertyName) {
    if (data == null || propertyName == null) {
      return null;
    }
    return new JsonDate(data.optString(propertyName, null)).asJavaDate();
  }

  @Override
  public void handleModelContextMenuChanged(List<IJsonAdapter<?>> menuAdapters) {
    addPropertyChangeEvent(PROP_MENUS, JsonObjectUtility.adapterIdsToJson(menuAdapters));
  }

  // FIXME awe: (calendar) discuss with C.GU: this is copy&paste. See other impl. if IJsonContextMenuOwner
  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    JsonContextMenu<IContextMenu> jsonContextMenu = getAdapter(getModel().getContextMenu());
    if (jsonContextMenu != null) {
      json.put(PROP_MENUS, jsonContextMenu.childActionsToJson());
    }
    return json;
  }

  protected class P_CalendarListener extends CalendarAdapter {

    @Override
    public void calendarChanged(CalendarEvent e) {
      addActionEvent(EVENT_CALENDAR_CHANGED, new JsonCalendarEvent(JsonCalendar.this, e).toJson());
    }

    @Override
    public void calendarChangedBatch(List<CalendarEvent> batch) {
      JSONArray jsonArray = new JSONArray();
      for (CalendarEvent event : batch) {
        jsonArray.put(new JsonCalendarEvent(JsonCalendar.this, event).toJson());
      }
      JSONObject json = new JSONObject();
      json.put("batch", jsonArray); // FIXME bsh: Calendar | Check if this works
      addActionEvent(EVENT_CALENDAR_CHANGED_BATCH, json);
    }
  }
}
