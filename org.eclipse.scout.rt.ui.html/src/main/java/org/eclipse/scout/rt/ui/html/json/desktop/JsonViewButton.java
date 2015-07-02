package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.action.JsonAction;
import org.json.JSONObject;

public class JsonViewButton<VIEW_BUTTON extends IViewButton> extends JsonAction<VIEW_BUTTON> {

  public JsonViewButton(VIEW_BUTTON model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ViewButton";
  }

  @Override
  protected void handleUiDoAction(JsonEvent event) {
    getModel().getUIFacade().setSelectedFromUI(true);
    super.handleUiDoAction(event);
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("displayStyle", getModel().getDisplayStyle());
    return json;
  }

}
