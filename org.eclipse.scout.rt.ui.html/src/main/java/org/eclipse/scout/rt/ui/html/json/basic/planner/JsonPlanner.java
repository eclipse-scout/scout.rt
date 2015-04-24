package org.eclipse.scout.rt.ui.html.json.basic.planner;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.client.ui.basic.planner.Activity;
import org.eclipse.scout.rt.client.ui.basic.planner.IPlanner;
import org.eclipse.scout.rt.client.ui.basic.planner.PlannerAdapter;
import org.eclipse.scout.rt.client.ui.basic.planner.PlannerEvent;
import org.eclipse.scout.rt.client.ui.basic.planner.PlannerListener;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.Resource;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IIdProvider;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonPlanner<P extends IPlanner<?, ?>> extends AbstractJsonPropertyObserver<P> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonPlanner.class);

  // from model
  public static final String EVENT_PLANNER_CHANGED = "plannerChanged";
  public static final String EVENT_RESOURCES_INSERTED = "resourcesInserted";
  public static final String EVENT_RESOURCES_UPDATED = "resourcesUpdated";
  public static final String EVENT_RESOURCES_DELETED = "resourcesDeleted";
  public static final String EVENT_ALL_RESOURCES_DELETED = "allResourcesDeleted";

  // from UI
  private static final String EVENT_SET_SELECTION = "setSelection";
  private static final String EVENT_SET_DAYS = "setDays";
  private static final String EVENT_SET_SELECTED_ACTIVITY_CELLS = "setSelectedActivityCells";
  private static final String EVENT_CELL_ACTION = "cellAction";

  private PlannerListener m_plannerListener;
  private final Map<String, Activity<?, ?>> m_cells;
  private final Map<Activity<?, ?>, String> m_cellIds;
  private final Map<String, Resource<?>> m_resources;
  private final Map<Resource<?>, String> m_resourceIds;
  private final AbstractEventBuffer<PlannerEvent> m_eventBuffer;

  public JsonPlanner(P model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
    m_cells = new HashMap<>();
    m_cellIds = new HashMap<>();
    m_resources = new HashMap<>();
    m_resourceIds = new HashMap<>();
    m_eventBuffer = model.createEventBuffer();
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
    putJsonProperty(new JsonProperty<P>(IPlanner.PROP_DISPLAY_MODE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getDisplayMode();
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
      protected List<?> modelValue() {
        return getModel().getSelectedResources();
      }

      @Override
      @SuppressWarnings("unchecked")
      public Object prepareValueForToJson(Object value) {
        List<Resource<?>> resources = (List<Resource<?>>) value;
        resources = new ArrayList<Resource<?>>();
        return resourceIdsToJson(resources, new P_GetOrCreateResourceIdProvider());
      }
    });
    putJsonProperty(new JsonProperty<P>(IPlanner.PROP_SELECTED_ACTIVITY_CELL, model) {
      @Override
      protected Activity<?, ?> modelValue() {
        return getModel().getSelectedActivityCell();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        Activity<?, ?> activityCell = (Activity<?, ?>) value;
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
    List<? extends Resource<?>> resources = getModel().getResources();
    JSONArray jsonResources = new JSONArray();
    for (Resource<?> resource : resources) {
      //FIXME CGU can't use NewResourceId Provider because properties come before toJson and already create an id, make sure properties come after?
      Object jsonResource = resourceToJson(resource, new P_GetOrCreateResourceIdProvider(), new P_GetOrCreateCellIdProvider());
      LOG.debug("Id: " + getId() + ". Resources: " + jsonResource.toString());
      jsonResources.put(jsonResource);
    }
    json.put("resources", jsonResources);
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

  protected String getResourceId(Resource<?> resource) {
    return m_resourceIds.get(resource);
  }

  protected String createResourceId(Resource<?> resource) {
    String id = getResourceId(resource);
    if (id != null) {
      throw new IllegalStateException("Resource already has an id. " + resource);
    }
    id = getUiSession().createUniqueIdFor(null);
    m_resources.put(id, resource);
    m_resourceIds.put(resource, id);
    return id;
  }

  protected void disposeResource(Resource resource) {
    String resourceId = getResourceId(resource);
    m_resourceIds.remove(resource);
    m_resources.remove(resourceId);
  }

  protected void disposeAllResources() {
    m_resourceIds.clear();
    m_resources.clear();
  }

  protected void handleModelEvent(PlannerEvent event) {
    // Add event to buffer instead of handling it immediately. (This allows coalescing the events at JSON response level.)
    m_eventBuffer.add(event);
    registerAsBufferedEventsAdapter();
  }

  @Override
  public void processBufferedEvents() {
    if (m_eventBuffer.isEmpty()) {
      return;
    }
    List<PlannerEvent> coalescedEvents = m_eventBuffer.consumeAndCoalesceEvents();
    for (PlannerEvent event : coalescedEvents) {
      processEvent(event);
    }
  }

  protected void processEvent(PlannerEvent event) {
    switch (event.getType()) {
      case PlannerEvent.TYPE_RESOURCES_INSERTED:
        handleModelResourcesInserted(event.getResources());
        break;
      case PlannerEvent.TYPE_RESOURCES_UPDATED:
        handleModelResourcesUpdated(event.getResources());
        break;
      case PlannerEvent.TYPE_RESOURCES_DELETED:
        handleModelResourcesDeleted(event.getResources());
        break;
      case PlannerEvent.TYPE_ALL_RESOURCES_DELETED:
        handleModelAllResourcesDeleted();
        break;
//      case PlannerEvent.TYPE_RESOURCE_ORDER_CHANGED:
//        handleModelResourceOrderChanged(event.getResources());
//        break;
      default:
        // NOP
    }
  }

  protected void handleModelResourcesInserted(List<? extends Resource> resources) {
    JSONArray jsonResources = new JSONArray();
    for (Resource resource : resources) {
      Object jsonResource = new JsonResource(resource, new P_NewResourceIdProvider(), new P_NewCellIdProvider()).toJson();
      jsonResources.put(jsonResource);
    }
    if (jsonResources.length() == 0) {
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    jsonEvent.put("resources", jsonResources);
    addActionEvent(EVENT_RESOURCES_INSERTED, jsonEvent);
  }

  protected void handleModelResourcesUpdated(List<? extends Resource> resources) {
    JSONArray jsonResources = new JSONArray();
    for (Resource resource : resources) {
      Object jsonResource = resourceToJson(resource);
      jsonResources.put(jsonResource);
    }
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, "resources", jsonResources);
    addActionEvent(EVENT_RESOURCES_UPDATED, jsonEvent);
  }

  protected void handleModelResourcesDeleted(List<? extends Resource> resources) {
    if (resources.isEmpty()) {
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    for (Resource resource : resources) {
      String resourceId = getResourceId(resource);
      jsonEvent.append("resourceIds", resourceId);
      disposeResource(resource);
    }
    addActionEvent(EVENT_RESOURCES_DELETED, jsonEvent);
  }

  protected void handleModelAllResourcesDeleted() {
    disposeAllResources();
    addActionEvent(EVENT_ALL_RESOURCES_DELETED, new JSONObject());
  }

  protected void handleModelResourceOrderChanged(List<? extends Resource> resources) {
    JSONObject jsonEvent = new JSONObject();
    jsonEvent.put("resourceIds", resourceIdsToJson(resources, new P_ResourceIdProvider()));
    JSONArray jsonResourceIds = new JSONArray();
    for (Resource resource : resources) {
      String resourceId = getResourceId(resource);
      jsonResourceIds.put(resourceId);
    }
    if (jsonResourceIds.length() == 0) {
      return;
    }
    addActionEvent("resourceOrderChanged", jsonEvent);
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_SET_SELECTION.equals(event.getType())) {
      handleUiSetSelection(event);
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

  @SuppressWarnings("unchecked")
  protected void handleUiSetSelection(JsonEvent event) {
    Date beginTime = new JsonDate(event.getData().optString("beginTime")).asJavaDate();
    Date endTime = new JsonDate(event.getData().optString("endTime")).asJavaDate();
    List<Resource<?>> resources = extractResources(event.getData());
    addPropertyEventFilterCondition(IPlanner.PROP_SELECTED_RESOURCES, resources);
    getModel().getUIFacade().setSelectionFromUI(resources, beginTime, endTime);
  }

  protected void handleUiSetDays(JsonEvent event) {
    JSONArray jsonDays = event.getData().optJSONArray("days");
    Date[] days = new Date[jsonDays.length()];
    for (int i = 0; i < jsonDays.length(); i++) {
      days[i] = new JsonDate(jsonDays.optString(i)).asJavaDate();
    }

    getModel().getUIFacade().setDaysFromUI(days);
  }

  protected void handleUiSetSelectedActivityCells(JsonEvent event) {
    Activity<?, ?> activityCell = null;
    // TODO Map data from JSON

    getModel().getUIFacade().setSelectedActivityCellFromUI(activityCell);
  }

  protected void handleCellAction(JsonEvent event) {
    Resource resource = null;
    // TODO Map data from JSON

    Activity<?, ?> activityCell = null;
    // TODO Map data from JSON

//    getModel().getUIFacade().fireCellActionFromUI(resource, activityCell);
  }

  protected List<Resource<?>> extractResources(JSONObject json) {
    return jsonToResources(JsonObjectUtility.getJSONArray(json, "resourceIds"));
  }

  protected List<Resource<?>> jsonToResources(JSONArray resourceIds) {
    List<Resource<?>> resources = new ArrayList<>(resourceIds.length());
    for (int i = 0; i < resourceIds.length(); i++) {
      resources.add(m_resources.get(resourceIds.get(i)));
    }
    return resources;
  }

  protected Object resourceToJson(Resource resource) {
    return new JsonResource(resource, new P_NewResourceIdProvider(), new P_NewCellIdProvider()).toJson();
  }

  protected Object resourceToJson(Resource resource, IIdProvider<Resource<?>> idProvider, IIdProvider<Activity<?, ?>> cellIdProvider) {
    return new JsonResource(resource, idProvider, cellIdProvider).toJson();
  }

  protected JSONArray resourceIdsToJson(List<? extends Resource> resources, IIdProvider<Resource<?>> idProvider) {
    JSONArray jsonResourceIds = new JSONArray();
    for (Resource resource : resources) {
      jsonResourceIds.put(idProvider.getId(resource));
    }
    return jsonResourceIds;
  }

//  @Override
//  protected void handleModelPropertyChange(String propertyName, Object oldValue, Object newValue) {
//    if (IPlanner.PROP_SELECTED_RESOURCES.equals(propertyName)) {
//      List<? extends Resource<?>> selectedResources = getModel().getSelectedResources();
//      JSONArray resourceIds = resourceIdsToJson(selectedResources);
//      addPropertyChangeEvent("selectedResourceIds, newValue);
//    }else {
//      super.handleModelPropertyChange(propertyName, oldValue, newValue);
//    }
//  }

  protected class P_PlannerListener extends PlannerAdapter {

    @Override
    public void plannerChanged(PlannerEvent event) {
      handleModelEvent(event);
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

  protected class P_ResourceIdProvider implements IIdProvider<Resource<?>> {

    @Override
    public String getId(Resource<?> resource) {
      return getResourceId(resource);
    }
  }

  protected class P_NewResourceIdProvider implements IIdProvider<Resource<?>> {

    @Override
    public String getId(Resource<?> resource) {
      return createResourceId(resource);
    }
  }

  protected class P_GetOrCreateResourceIdProvider implements IIdProvider<Resource<?>> {

    @Override
    public String getId(Resource<?> resource) {
      String id = getResourceId(resource);
      if (id == null) {
        id = createResourceId(resource);
      }
      return id;
    }
  }
}
