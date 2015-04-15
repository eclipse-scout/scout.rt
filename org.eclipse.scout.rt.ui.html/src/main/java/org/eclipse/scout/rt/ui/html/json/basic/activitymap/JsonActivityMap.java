package org.eclipse.scout.rt.ui.html.json.basic.activitymap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.activitymap.ActivityCell;
import org.eclipse.scout.rt.client.ui.basic.activitymap.ActivityMapEvent;
import org.eclipse.scout.rt.client.ui.basic.activitymap.ActivityMapListener;
import org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMap;
import org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMapUIFacade;
import org.eclipse.scout.rt.client.ui.basic.activitymap.TimeScale;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.json.JSONArray;

public class JsonActivityMap<P extends IActivityMap<RI, AI>, RI, AI> extends AbstractJsonPropertyObserver<P> {

  // from model
  public static final String EVENT_ACTIVITY_MAP_CHANGED = "activityMapChanged";

  // from UI
  private static final String EVENT_SELECTION = "selection";
  private static final String EVENT_SET_DAYS = "setDays";
  private static final String EVENT_SET_SELECTED_ACTIVITY_CELLS = "setSelectedActivityCells";
  private static final String EVENT_CELL_ACTION = "cellAction";

  private ActivityMapListener m_activityMapListener;

  public JsonActivityMap(P model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ActivityMap";
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_activityMapListener != null) {
      throw new IllegalStateException();
    }
    m_activityMapListener = new P_ActivityMapListener();
    getModel().addActivityMapListener(m_activityMapListener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_activityMapListener == null) {
      throw new IllegalStateException();
    }
    getModel().removeActivityMapListener(m_activityMapListener);
    m_activityMapListener = null;
  }

  @Override
  protected void initJsonProperties(P model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<P>(IActivityMap.PROP_DAYS, model) {
      @Override
      protected Date[] modelValue() {
        return getModel().getDays();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        if (value == null) {
          return null;
        }
        JSONArray jsonArray = new JSONArray();
        for (Date date : (Date[]) value) {
          jsonArray.put(new JsonDate(date).asJsonString());
        }
        return jsonArray;
      }
    });
    putJsonProperty(new JsonProperty<P>(IActivityMap.PROP_WORK_DAY_COUNT, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getWorkDayCount();
      }
    });
    putJsonProperty(new JsonProperty<P>(IActivityMap.PROP_WORK_DAYS_ONLY, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isWorkDaysOnly();
      }
    });
    putJsonProperty(new JsonProperty<P>(IActivityMap.PROP_FIRST_HOUR_OF_DAY, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getFirstHourOfDay();
      }
    });
    putJsonProperty(new JsonProperty<P>(IActivityMap.PROP_LAST_HOUR_OF_DAY, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getLastHourOfDay();
      }
    });
    putJsonProperty(new JsonProperty<P>(IActivityMap.PROP_INTRADAY_INTERVAL, model) {
      @Override
      protected Long modelValue() {
        return getModel().getIntradayInterval();
      }
    });
    putJsonProperty(new JsonProperty<P>(IActivityMap.PROP_PLANNING_MODE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getPlanningMode();
      }
    });
    putJsonProperty(new JsonProperty<P>(IActivityMap.PROP_RESOURCE_IDS, model) {
      @Override
      protected List<RI> modelValue() {
        return getModel().getResourceIds();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        if (value == null) {
          return null;
        }
        @SuppressWarnings("unchecked")
        List<RI> list = (List<RI>) value;
        return new JSONArray(list);
      }
    });
    putJsonProperty(new JsonProperty<P>(IActivityMap.PROP_SELECTED_BEGIN_TIME, model) {
      @Override
      protected Date modelValue() {
        return getModel().getSelectedBeginTime();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return new JsonDate((Date) value).asJsonString();
      }
    });
    putJsonProperty(new JsonProperty<P>(IActivityMap.PROP_SELECTED_END_TIME, model) {
      @Override
      protected Date modelValue() {
        return getModel().getSelectedEndTime();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return new JsonDate((Date) value).asJsonString();
      }
    });
    putJsonProperty(new JsonProperty<P>(IActivityMap.PROP_SELECTED_RESOURCE_IDS, model) {
      @Override
      protected List<RI> modelValue() {
        return getModel().getSelectedResourceIds();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        if (value == null) {
          return null;
        }
        @SuppressWarnings("unchecked")
        List<RI> list = (List<RI>) value;
        return new JSONArray(list);
      }
    });
    putJsonProperty(new JsonProperty<P>(IActivityMap.PROP_SELECTED_ACTIVITY_CELL, model) {
      @Override
      protected ActivityCell<RI, AI> modelValue() {
        return getModel().getSelectedActivityCell();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        @SuppressWarnings("unchecked")
        ActivityCell<RI, AI> activityCell = (ActivityCell<RI, AI>) value;
        return new JsonActivityCell<RI, AI>(activityCell).toJson();
      }
    });
    putJsonProperty(new JsonProperty<P>(IActivityMap.PROP_TIME_SCALE, model) {
      @Override
      protected TimeScale modelValue() {
        return getModel().getTimeScale();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return new JsonTimeScale((TimeScale) value).toJson();
      }
    });
    putJsonProperty(new JsonProperty<P>(IActivityMap.PROP_DRAW_SECTIONS, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isDrawSections();
      }
    });

    // TODO BSH | Calendar String PROP_CONTEXT_MENU = "contextMenus";
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_SELECTION.equals(event.getType())) {
      handleUiSelection(event);
    }
    else if (EVENT_SET_DAYS.equals(event.getType())) {
      handleUiSetDays(event);
    }
    else if (EVENT_SET_SELECTED_ACTIVITY_CELLS.equals(event.getType())) {
      handleUiSetSelectedActivityCells(event);
    }
    else if (EVENT_CELL_ACTION.equals(event.getType())) {
      handleCellAction(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiSelection(JsonEvent event) {
    JSONArray resourceIdsArray = event.getData().optJSONArray("resourceIds");
    List<RI> resourceIds = new ArrayList<>();
    for (int i = 0; i < resourceIdsArray.length(); i++) {
      // TODO Convert IDs from JSON
      @SuppressWarnings("unchecked")
      RI id = (RI) resourceIdsArray.opt(i);
      resourceIds.add(id);
    }

    JSONArray normalizedRangeArray = event.getData().optJSONArray("normalizedRange");
    double[] normalizedRange = null;
    if (normalizedRangeArray != null) {
      normalizedRange = new double[normalizedRangeArray.length()];
      for (int i = 0; i < normalizedRangeArray.length(); i++) {
        normalizedRange[i] = normalizedRangeArray.optDouble(i);
      }
    }

    @SuppressWarnings("unchecked")
    IActivityMapUIFacade<RI, AI> uiFacade = (IActivityMapUIFacade<RI, AI>) getModel().getUIFacade();
    uiFacade.setSelectionFromUI(resourceIds, normalizedRange);
  }

  protected void handleUiSetDays(JsonEvent event) {
    JSONArray jsonDays = event.getData().optJSONArray("days");
    Date[] days = new Date[jsonDays.length()];
    for (int i = 0; i < jsonDays.length(); i++) {
      days[i] = new JsonDate(jsonDays.optString(i)).asJavaDate();
    }

    @SuppressWarnings("unchecked")
    IActivityMapUIFacade<RI, AI> uiFacade = (IActivityMapUIFacade<RI, AI>) getModel().getUIFacade();
    uiFacade.setDaysFromUI(days);
  }

  protected void handleUiSetSelectedActivityCells(JsonEvent event) {
    ActivityCell<RI, AI> activityCell = null;
    // TODO Map data from JSON

    @SuppressWarnings("unchecked")
    IActivityMapUIFacade<RI, AI> uiFacade = (IActivityMapUIFacade<RI, AI>) getModel().getUIFacade();
    uiFacade.setSelectedActivityCellFromUI(activityCell);
  }

  protected void handleCellAction(JsonEvent event) {
    RI resourceId = null;
    // TODO Map data from JSON

    double[] normalizedRange = null;
    // TODO Map data from JSON

    ActivityCell<RI, AI> activityCell = null;
    // TODO Map data from JSON

    @SuppressWarnings("unchecked")
    IActivityMapUIFacade<RI, AI> uiFacade = (IActivityMapUIFacade<RI, AI>) getModel().getUIFacade();
    uiFacade.fireCellActionFromUI(resourceId, normalizedRange, activityCell);
  }

  protected class P_ActivityMapListener implements ActivityMapListener {

    @Override
    public void activityMapChanged(ActivityMapEvent e) {
      addActionEvent(EVENT_ACTIVITY_MAP_CHANGED, new JsonActivityMapEvent(e).toJson());
    }
  }
}
