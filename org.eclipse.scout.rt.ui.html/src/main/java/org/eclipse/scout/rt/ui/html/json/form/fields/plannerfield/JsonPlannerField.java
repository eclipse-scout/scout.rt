package org.eclipse.scout.rt.ui.html.json.form.fields.plannerfield;

import org.eclipse.scout.rt.client.ui.basic.planner.IPlanner;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.IPlannerField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.json.JSONObject;

public class JsonPlannerField<P extends IPlanner<RI, AI>, RI, AI> extends JsonFormField<IPlannerField<P, RI, AI>> {

  public JsonPlannerField(IPlannerField<P, RI, AI> model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "PlannerField";
  }

  @Override
  protected void initJsonProperties(IPlannerField<P, RI, AI> model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IPlannerField<P, RI, AI>>(IPlannerField.PROP_SPLITTER_POSITION, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getSplitterPosition();
      }
    });
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getPlanner());
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putAdapterIdProperty(json, "planner", getModel().getPlanner());
    return json;
  }
}
