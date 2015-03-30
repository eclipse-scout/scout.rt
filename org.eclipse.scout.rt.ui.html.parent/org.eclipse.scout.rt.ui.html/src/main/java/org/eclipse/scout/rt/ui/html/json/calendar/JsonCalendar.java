/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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

import org.eclipse.scout.commons.Range;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarAdapter;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarEvent;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarListener;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonCalendar<T extends ICalendar> extends AbstractJsonPropertyObserver<T> {

  // from model
  public static final String EVENT_CALENDAR_CHANGED = "calendarChanged";
  public static final String EVENT_CALENDAR_CHANGED_BATCH = "calendarChangedBatch";

  // from UI
  private static final String EVENT_COMPONENT_ACTION = "componentAction";
  private static final String EVENT_COMPONENT_MOVED = "componentMoved";
  private static final String EVENT_RELOAD = "reload";
  private static final String EVENT_SET_VISIBLE_RANGE = "setVisibleRange";
  private static final String EVENT_SET_SELECTION = "setSelection";
  private static final String EVENT_SET_DISPLAY_MODE = "setDisplayMode";

  private CalendarListener m_calendarListener;

  public JsonCalendar(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
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
  protected void initJsonProperties(T model) {
    //FIXME CGU/BSH improve adapterproperty so that components are not disposed when changed?
    putJsonProperty(new JsonAdapterProperty<T>(ICalendar.PROP_COMPONENTS, model, getJsonSession()) {
      @Override
      protected Set<? extends CalendarComponent> modelValue() {
        return getModel().getComponents();
      }
    });
    putJsonProperty(new JsonAdapterProperty<T>(ICalendar.PROP_SELECTED_COMPONENT, model, getJsonSession()) {
      @Override
      protected CalendarComponent modelValue() {
        return getModel().getSelectedComponent();
      }
    });
    putJsonProperty(new JsonProperty<T>(ICalendar.PROP_DISPLAY_MODE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getDisplayMode();
      }
    });
    putJsonProperty(new JsonProperty<T>(ICalendar.PROP_DISPLAY_CONDENSED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isDisplayCondensed();
      }
    });
    putJsonProperty(new JsonProperty<T>(ICalendar.PROP_TITLE, model) {
      @Override
      protected String modelValue() {
        return getModel().getTitle();
      }
    });
    putJsonProperty(new JsonProperty<T>(ICalendar.PROP_VIEW_RANGE, model) {
      @Override
      protected Range<Date> modelValue() {
        return getModel().getViewRange();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        if (value == null) {
          return null;
        }
        @SuppressWarnings("unchecked")
        Range<Date> modelValue = (Range<Date>) value;
        JSONObject json = new JSONObject();
        JsonObjectUtility.putProperty(json, "from", new JsonDate(modelValue.getFrom()).asJsonString());
        JsonObjectUtility.putProperty(json, "to", new JsonDate(modelValue.getTo()).asJsonString());
        return json;
      }
    });
    putJsonProperty(new JsonProperty<T>(ICalendar.PROP_SELECTED_DATE, model) {
      @Override
      protected Date modelValue() {
        return getModel().getSelectedDate();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return new JsonDate((Date) value).asJsonString();
      }
    });
    putJsonProperty(new JsonProperty<T>(ICalendar.PROP_LOAD_IN_PROGRESS, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isLoadInProgress();
      }
    });
    putJsonProperty(new JsonProperty<T>(ICalendar.PROP_START_HOUR, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getStartHour();
      }
    });
    putJsonProperty(new JsonProperty<T>(ICalendar.PROP_END_HOUR, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getEndHour();
      }
    });
    putJsonProperty(new JsonProperty<T>(ICalendar.PROP_USE_OVERFLOW_CELLS, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().getUseOverflowCells();
      }
    });
    putJsonProperty(new JsonProperty<T>(ICalendar.PROP_SHOW_DISPLAY_MODE_SELECTION, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().getShowDisplayModeSelection();
      }
    });
    putJsonProperty(new JsonProperty<T>(ICalendar.PROP_MARK_NOON_HOUR, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().getMarkNoonHour();
      }
    });
    putJsonProperty(new JsonProperty<T>(ICalendar.PROP_MARK_OUT_OF_MONTH_DAYS, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().getMarkOutOfMonthDays();
      }
    });

    // TODO BSH | Calendar String PROP_CONTEXT_MENU = "contextMenus";
  }

  @SuppressWarnings("unchecked")
  public <C extends CalendarComponent> JsonCalendarComponent<C> resolveCalendarComponent(String id) {
    if (id == null) {
      return null;
    }
    return (JsonCalendarComponent<C>) getJsonSession().getJsonAdapter(id);
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
    else if (EVENT_SET_VISIBLE_RANGE.equals(event.getType())) {
      handleUiSetVisibleRange(event);
    }
    else if (EVENT_SET_SELECTION.equals(event.getType())) {
      handleUiSetSelection(event);
    }
    else if (EVENT_SET_DISPLAY_MODE.equals(event.getType())) {
      handleUiSetDisplayMode(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  // TODO BSH Check if we need event filter here, as in the following example
//  protected void handleUiSelected(JsonEvent event) {
//    boolean selected = JsonObjectUtility.getBoolean(event.getData(), IAction.PROP_SELECTED);
//    addPropertyEventFilterCondition(IAction.PROP_SELECTED, selected);
//    getModel().getUIFacade().setSelectedFromUI(selected);
//  }

  protected void handleUiComponentAction(JsonEvent event) {
    getModel().getUIFacade().fireComponentActionFromUI();
  }

  protected void handleUiComponentMoved(JsonEvent event) {
    JsonCalendarComponent<CalendarComponent> comp = resolveCalendarComponent(event.getData().optString("component"));
    Date newDate = new JsonDate(event.getData().optString("newDate")).asJavaDate();
    getModel().getUIFacade().fireComponentMovedFromUI(comp.getModel(), newDate);
  }

  protected void handleUiReload(JsonEvent event) {
    getModel().getUIFacade().fireReloadFromUI();
  }

  protected void handleUiSetVisibleRange(JsonEvent event) {
    JSONObject dateRange = event.getData().optJSONObject("dateRange");
    Date fromDate = new JsonDate(dateRange.optString("from")).asJavaDate();
    Date toDate = new JsonDate(dateRange.optString("to")).asJavaDate();
    getModel().getUIFacade().setVisibleRangeFromUI(fromDate, toDate);
  }

  protected void handleUiSetSelection(JsonEvent event) {
    Date date = new JsonDate(event.getData().optString("date")).asJavaDate();
    JsonCalendarComponent<CalendarComponent> comp = resolveCalendarComponent(event.getData().optString("component"));
    getModel().getUIFacade().setSelectionFromUI(date, comp.getModel());
  }

  protected void handleUiSetDisplayMode(JsonEvent event) {
    int displayMode = event.getData().optInt("displayMode");
    // TODO BSH Calendar | Check if we should add this method to the UI facade
    getModel().setDisplayMode(displayMode);
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
      JsonObjectUtility.putProperty(json, "batch", jsonArray); // TODO BSH Calendar | Check if this works
      addActionEvent(EVENT_CALENDAR_CHANGED_BATCH, json);
    }
  }

}
