package org.eclipse.scout.rt.ui.html.json.form.fields.treebox;

import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.ITreeBox;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.json.JSONObject;

public class JsonTreeBox<T extends ITreeBox> extends JsonValueField<T> {

  public JsonTreeBox(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "TreeBox";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<T>(ITreeBox.PROP_FILTER_ACTIVE_NODES, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isFilterActiveNodes();
      }
    });
    putJsonProperty(new JsonProperty<T>(ITreeBox.PROP_FILTER_CHECKED_NODES, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isFilterCheckedNodes();
      }
    });
    putJsonProperty(new JsonProperty<T>(ITreeBox.PROP_FILTER_ACTIVE_NODES_VALUE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().getFilterCheckedNodesValue();
      }
    });
    putJsonProperty(new JsonProperty<T>(ITreeBox.PROP_FILTER_ACTIVE_NODES_VALUE, model) {
      @Override
      protected TriState modelValue() {
        return getModel().getFilterActiveNodesValue();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        if (value == null) {
          return null;
        }
        return ((TriState) value).getBooleanValue();
      }
    });
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getTree());
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putAdapterIdProperty(json, "tree", getModel().getTree());
    return json;
  }
}
