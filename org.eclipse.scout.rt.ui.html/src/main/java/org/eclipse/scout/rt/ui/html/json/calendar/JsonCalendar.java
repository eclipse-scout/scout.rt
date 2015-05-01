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
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarAdapter;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarEvent;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarListener;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendarUIFacade;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
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

public class JsonCalendar<T extends ICalendar> extends AbstractJsonPropertyObserver<T> implements IJsonContextMenuOwner {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonCalendar.class);

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

  public JsonCalendar(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
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
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapters(getModel().getComponents());
    attachAdapter(getModel().getContextMenu(), new DisplayableActionFilter<IMenu>());
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
    // FIXME CGU/BSH improve adapter-property so that components are not disposed when changed?
    putJsonProperty(new JsonAdapterProperty<T>(ICalendar.PROP_COMPONENTS, model, getUiSession()) {
      @Override
      protected Set<? extends CalendarComponent> modelValue() {
        return getModel().getComponents();
      }
    });
    putJsonProperty(new JsonAdapterProperty<T>(ICalendar.PROP_SELECTED_COMPONENT, model, getUiSession()) {
      @Override
      protected CalendarComponent modelValue() {
        return getModel().getSelectedComponent();
      }

      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return new JsonAdapterPropertyConfigBuilder().disposeOnChange(false).build();
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
        JSONObject json = JsonObjectUtility.newOrderedJSONObject();
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

    // FIXME BSH | Calendar String PROP_CONTEXT_MENU = "contextMenus";
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

//   FIXME BSH Check if we need event filter here, as in the following example
//   FIXME AWE: (calendar) check how the selected property is used in swing UI, decide if we can remove the property/event
//    protected void handleUiSelectionChanged(JsonEvent event) {
//      boolean selected = JsonObjectUtility.getBoolean(event.getData(), IAction.PROP_SELECTED);
//      addPropertyEventFilterCondition(IAction.PROP_SELECTED, selected);
//      getModel().getUIFacade().setSelectedFromUI(selected);
//    }

  protected void handleUiComponentAction(JsonEvent event) {
    getModel().getUIFacade().fireComponentActionFromUI();
  }

  protected void handleUiComponentMoved(JsonEvent event) {
    JSONObject data = event.getData();
    Date newDate = toJavaDate(data, "newDate");
    JsonCalendarComponent<CalendarComponent> comp = resolveCalendarComponent(event.getData().optString("component"));
    getModel().getUIFacade().fireComponentMovedFromUI(comp.getModel(), newDate);
  }

  protected void handleUiReload(JsonEvent event) {
    getModel().getUIFacade().fireReloadFromUI();
  }

  // FIXME AWE: (calendar) replace selectedDate with selectionRange(from/to)

  protected void handleUiSelectionChanged(JsonEvent event) {
    // FIXME AWE: (calendar) currently the UI does not support selection of components, only an entire day can be selected
    JSONObject data = event.getData();
    Date date = toJavaDate(data, "date");
    String componentId = data.optString("componentId");
    JsonCalendarComponent<CalendarComponent> component = resolveCalendarComponent(componentId);
    addPropertyEventFilterCondition(ICalendar.PROP_SELECTED_COMPONENT, component.getModel());
    getModel().getUIFacade().setSelectionFromUI(date, component.getModel());
    LOG.debug("date=" + date + " componentId=" + componentId);
  }

  protected void handleUiModelChanged(JsonEvent event) {
    JSONObject data = event.getData();
    int displayMode = data.optInt("displayMode");
    ICalendarUIFacade uiFacade = getModel().getUIFacade();
    uiFacade.setDisplayModeFromUI(displayMode);
    Range<Date> viewRange = extractViewRange(data);
    uiFacade.setViewRangeFromUI(viewRange);
    Date selectedDate = extractSelectedDate(data);
    uiFacade.setSelectedDateFromUI(selectedDate);
    LOG.debug("displayMode=" + displayMode + " viewRange=" + viewRange + " selectedDate=" + selectedDate);
  }

  protected void handleUiViewRangeChanged(JsonEvent event) {
    Range<Date> viewRange = extractViewRange(event.getData());
    getModel().getUIFacade().setViewRangeFromUI(viewRange);
    LOG.debug("viewRange=" + viewRange);
  }

  private Range<Date> extractViewRange(JSONObject data) {
    JSONObject viewRange = data.optJSONObject("viewRange");
    Date fromDate = toJavaDate(viewRange, "from");
    Date toDate = toJavaDate(viewRange, "to");
    return new Range<Date>(fromDate, toDate);
  }

  private Date extractSelectedDate(JSONObject data) {
    return toJavaDate(data, "selectedDate");
  }

  private Date toJavaDate(JSONObject data, String propertyName) {
    return new JsonDate(data.optString(propertyName)).asJavaDate();
  }

  @Override
  public void handleModelContextMenuChanged(List<IJsonAdapter<?>> menuAdapters) {
    addPropertyChangeEvent(PROP_MENUS, JsonObjectUtility.adapterIdsToJson(menuAdapters));
  }

  // FIXME AWE: (calendar) discuss with C.GU: this is copy&paste. See other impl. if IJsonContextMenuOwner
  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    JsonContextMenu<IContextMenu> jsonContextMenu = getAdapter(getModel().getContextMenu());
    if (jsonContextMenu != null) {
      JsonObjectUtility.putProperty(json, PROP_MENUS, jsonContextMenu.childActionsToJson());
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
      JsonObjectUtility.putProperty(json, "batch", jsonArray); // FIXME BSH: Calendar | Check if this works
      addActionEvent(EVENT_CALENDAR_CHANGED_BATCH, json);
    }
  }

}
