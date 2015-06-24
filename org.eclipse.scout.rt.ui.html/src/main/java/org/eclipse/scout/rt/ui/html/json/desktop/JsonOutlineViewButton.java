package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineViewButton;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;
import org.json.JSONObject;

public class JsonOutlineViewButton<T extends IOutlineViewButton> extends JsonViewButton<T> {

  public JsonOutlineViewButton(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "OutlineViewButton";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<T>("outline", model, getUiSession()) {
      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return JsonAdapterPropertyConfigBuilder.globalConfig();
      }

      @Override
      protected IOutline modelValue() {
        return getModel().getOutline();
      }

      @Override
      public boolean accept() {
        return getModel().isSelected();
      }
    });
    getJsonProperty(IAction.PROP_SELECTED).addSlaveProperty(getJsonProperty("outline"));
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("displayStyle", getModel().getDisplayStyle());
    return json;
  }

}
