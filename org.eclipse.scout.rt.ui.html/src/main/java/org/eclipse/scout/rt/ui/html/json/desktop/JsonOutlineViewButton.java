package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineViewButton;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;

public class JsonOutlineViewButton<OUTLINE_VIEW_BUTTON extends IOutlineViewButton> extends JsonViewButton<OUTLINE_VIEW_BUTTON> {

  public JsonOutlineViewButton(OUTLINE_VIEW_BUTTON model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "OutlineViewButton";
  }

  @Override
  protected void initJsonProperties(OUTLINE_VIEW_BUTTON model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<OUTLINE_VIEW_BUTTON>("outline", model, getUiSession()) {
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

}
