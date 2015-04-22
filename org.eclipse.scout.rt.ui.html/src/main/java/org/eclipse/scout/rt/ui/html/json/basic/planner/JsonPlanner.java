package org.eclipse.scout.rt.ui.html.json.basic.planner;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.planner.Activity;
import org.eclipse.scout.rt.client.ui.basic.planner.IPlanner;
import org.eclipse.scout.rt.client.ui.basic.planner.IPlannerUIFacade;
import org.eclipse.scout.rt.client.ui.basic.planner.PlannerEvent;
import org.eclipse.scout.rt.client.ui.basic.planner.PlannerListener;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.Resource;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IIdProvider;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonPlanner<P extends IPlanner<RI, AI>, RI, AI> extends AbstractJsonPropertyObserver<P> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonPlanner.class);

  // from model
  public static final String EVENT_PLANNER_CHANGED = "plannerChanged";

  // from UI
  private static final String EVENT_SELECTION = "selection";
  private static final String EVENT_SET_DAYS = "setDays";
  private static final String EVENT_SET_SELECTED_ACTIVITY_CELLS = "setSelectedActivityCells";
  private static final String EVENT_CELL_ACTION = "cellAction";

  private PlannerListener m_plannerListener;
  private final Map<String, Activity<?, ?>> m_cells;
  private final Map<Activity<?, ?>, String> m_cellIds;

  public JsonPlanner(P model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
    m_cells = new HashMap<>();
    m_cellIds = new HashMap<>();
  }

  @Override
  public String getObjectType() {
    return "Planner";
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_plannerListener != null) {
      throw new IllegalStateException();
    }
    m_plannerListener = new P_PlannerListener();
    getModel().addPlannerListener(m_plannerListener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_plannerListener == null) {
      throw new IllegalStateException();
    }
    getModel().removePlannerListener(m_plannerListener);
    m_plannerListener = null;
  }

  @Override
  protected void initJsonProperties(P model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<P>(IPlanner.PROP_PLANNING_MODE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getPlanningMode();
      }
    });
    putJsonProperty(new JsonProperty<P>(IPlanner.PROP_DAYS, model) {
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
    //FIXME CGU remove
//    putJsonProperty(new JsonProperty<P>(IPlanner.PROP_WORK_DAY_COUNT, model) {
//      @Override
//      protected Integer modelValue() {
//        return getModel().getWorkDayCount();
//      }
//    });
    //FIXME CGU not part of planner mode?
//    putJsonProperty(new JsonProperty<P>(IPlanner.PROP_WORK_DAYS_ONLY, model) {
//      @Override
//      protected Boolean modelValue() {
//        return getModel().isWorkDaysOnly();
//      }
//    });
    //FIXME CGU needed?
//    putJsonProperty(new JsonProperty<P>(IPlanner.PROP_FIRST_HOUR_OF_DAY, model) {
//      @Override
//      protected Integer modelValue() {
//        return getModel().getFirstHourOfDay();
//      }
//    });
//    putJsonProperty(new JsonProperty<P>(IPlanner.PROP_LAST_HOUR_OF_DAY, model) {
//      @Override
//      protected Integer modelValue() {
//        return getModel().getLastHourOfDay();
//      }
//    });
//    putJsonProperty(new JsonProperty<P>(IPlanner.PROP_INTRADAY_INTERVAL, model) {
//      @Override
//      protected Long modelValue() {
//        return getModel().getIntradayInterval();
//      }
//    });
    putJsonProperty(new JsonProperty<P>(IPlanner.PROP_SELECTED_BEGIN_TIME, model) {
      @Override
      protected Date modelValue() {
        return getModel().getSelectedBeginTime();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return new JsonDate((Date) value).asJsonString();
      }
    });
    putJsonProperty(new JsonProperty<P>(IPlanner.PROP_SELECTED_END_TIME, model) {
      @Override
      protected Date modelValue() {
        return getModel().getSelectedEndTime();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return new JsonDate((Date) value).asJsonString();
      }
    });
    putJsonProperty(new JsonProperty<P>(IPlanner.PROP_SELECTED_RESOURCES, model) {
      @Override
      protected List<Resource> modelValue() {
        return getModel().getSelectedResources();
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
    putJsonProperty(new JsonProperty<P>(IPlanner.PROP_SELECTED_ACTIVITY_CELL, model) {
      @Override
      protected Activity<RI, AI> modelValue() {
        return getModel().getSelectedActivityCell();
      }

      @SuppressWarnings("unchecked")
      @Override
      public Object prepareValueForToJson(Object value) {
        Activity<RI, AI> activityCell = (Activity<RI, AI>) value;
        return new P_GetOrCreateCellIdProvider().getId(activityCell);
      }
    });
    putJsonProperty(new JsonProperty<P>(IPlanner.PROP_DRAW_SECTIONS, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isDrawSections();
      }
    });

    // TODO BSH | Calendar String PROP_CONTEXT_MENU = "contextMenus";
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    List<Resource> resources = getModel().getResources();
    JSONArray jsonRows = new JSONArray();
    for (Resource resource : resources) {
      JsonResource jsonRow = new JsonResource(resource, new P_NewCellIdProvider());
      Object row = jsonRow.toJson();
      LOG.debug("Id: " + getId() + ". Resources: " + row.toString());
      jsonRows.put(row);
    }
    json.put("rows", jsonRows);
    return json;
  }

  protected String getCellId(Activity<?, ?> cell) {
    return m_cellIds.get(cell);
  }

  protected String createCellId(Activity<?, ?> cell) {
    String id = getCellId(cell);
    if (id != null) {
      throw new IllegalStateException("Cell already has an id. " + cell);
    }
    id = getUiSession().createUniqueIdFor(null);
    m_cells.put(id, cell);
    m_cellIds.put(cell, id);
    return id;
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
    List<Resource> resources = new ArrayList<>();
    for (int i = 0; i < resourceIdsArray.length(); i++) {
      // TODO Convert IDs from JSON
//      @SuppressWarnings("unchecked")
//      RI id = (RI) resourceIdsArray.opt(i);
//      resources.add(id);
    }

    @SuppressWarnings("unchecked")
    IPlannerUIFacade<RI, AI> uiFacade = (IPlannerUIFacade<RI, AI>) getModel().getUIFacade();
    Date endTime = null;
    Date beginTime = null;
    uiFacade.setSelectionFromUI(resources, beginTime, endTime);
  }

  protected void handleUiSetDays(JsonEvent event) {
    JSONArray jsonDays = event.getData().optJSONArray("days");
    Date[] days = new Date[jsonDays.length()];
    for (int i = 0; i < jsonDays.length(); i++) {
      days[i] = new JsonDate(jsonDays.optString(i)).asJavaDate();
    }

    @SuppressWarnings("unchecked")
    IPlannerUIFacade<RI, AI> uiFacade = (IPlannerUIFacade<RI, AI>) getModel().getUIFacade();
    uiFacade.setDaysFromUI(days);
  }

  protected void handleUiSetSelectedActivityCells(JsonEvent event) {
    Activity<RI, AI> activityCell = null;
    // TODO Map data from JSON

    @SuppressWarnings("unchecked")
    IPlannerUIFacade<RI, AI> uiFacade = (IPlannerUIFacade<RI, AI>) getModel().getUIFacade();
    uiFacade.setSelectedActivityCellFromUI(activityCell);
  }

  protected void handleCellAction(JsonEvent event) {
    Resource resource = null;
    // TODO Map data from JSON

    Activity<RI, AI> activityCell = null;
    // TODO Map data from JSON

    @SuppressWarnings("unchecked")
    IPlannerUIFacade<RI, AI> uiFacade = (IPlannerUIFacade<RI, AI>) getModel().getUIFacade();
    uiFacade.fireCellActionFromUI(resource, activityCell);
  }

  protected class P_PlannerListener implements PlannerListener {

    @Override
    public void plannerChanged(PlannerEvent e) {
      addActionEvent(EVENT_PLANNER_CHANGED, new JsonPlannerEvent(e).toJson());
    }
  }

  protected class P_CellIdProvider implements IIdProvider<Activity<?, ?>> {

    @Override
    public String getId(Activity<?, ?> cell) {
      return getCellId(cell);
    }
  }

  protected class P_NewCellIdProvider implements IIdProvider<Activity<?, ?>> {

    @Override
    public String getId(Activity<?, ?> cell) {
      return createCellId(cell);
    }
  }

  protected class P_GetOrCreateCellIdProvider implements IIdProvider<Activity<?, ?>> {

    @Override
    public String getId(Activity<?, ?> cell) {
      String id = getCellId(cell);
      if (id == null) {
        id = createCellId(cell);
      }
      return id;
    }
  }
}
