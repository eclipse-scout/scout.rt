package org.eclipse.scout.rt.ui.html.json.form.fields.plannerfield;

import org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMap;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.IPlannerField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.json.JSONObject;

public class JsonPlannerField<T extends ITable, P extends IActivityMap<RI, AI>, RI, AI> extends JsonFormField<IPlannerField<T, P, RI, AI>> {

  public JsonPlannerField(IPlannerField<T, P, RI, AI> model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "PlannerField";
  }

  @Override
  protected void initJsonProperties(IPlannerField<T, P, RI, AI> model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IPlannerField<T, P, RI, AI>>(IPlannerField.PROP_MINI_CALENDAR_COUNT, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getMiniCalendarCount();
      }
    });
    putJsonProperty(new JsonProperty<IPlannerField<T, P, RI, AI>>(IPlannerField.PROP_SPLITTER_POSITION, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getSplitterPosition();
      }
    });
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getActivityMap());
    attachAdapter(getModel().getResourceTable());
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putAdapterIdProperty(json, "activityMap", getModel().getActivityMap());
    putAdapterIdProperty(json, "resourceTable", getModel().getResourceTable());
    return json;
  }
}
