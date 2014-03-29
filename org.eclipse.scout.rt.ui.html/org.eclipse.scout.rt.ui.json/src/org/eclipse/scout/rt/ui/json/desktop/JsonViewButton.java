package org.eclipse.scout.rt.ui.json.desktop;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.ui.json.AbstractJsonPropertyObserverRenderer;
import org.eclipse.scout.rt.ui.json.IJsonSession;
import org.eclipse.scout.rt.ui.json.JsonEvent;
import org.eclipse.scout.rt.ui.json.JsonResponse;
import org.eclipse.scout.rt.ui.json.JsonUIException;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonViewButton extends AbstractJsonPropertyObserverRenderer<IViewButton> {

  public JsonViewButton(IViewButton modelObject, IJsonSession jsonSession) {
    super(modelObject, jsonSession);
  }

  @Override
  public String getObjectType() {
    return "ViewButton";
  }

  @Override
  public JSONObject toJson() throws JsonUIException {
    JSONObject json = super.toJson();
    try {
      json.put(IViewButton.PROP_TEXT, getModelObject().getText());
      json.put("selected", getModelObject().isSelected());
      return json;
    }
    catch (JSONException e) {
      throw new JsonUIException(e.getMessage(), e);
    }
  }

  @Override
  protected void handleModelPropertyChange(String name, Object newValue) {
    if (IViewButton.PROP_SELECTED.equals(name)) {
      getJsonSession().currentJsonResponse().addPropertyChangeEvent(getId(), name, newValue);
    }
    else {
      super.handleModelPropertyChange(name, newValue);
    }
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) throws JsonUIException {
    if ("click".equals(event.getEventType())) {
      handleUiClick(event, res);
    }
    else {
      throw new IllegalArgumentException("unsupported event type");
    }
  }

  protected void handleUiClick(JsonEvent event, JsonResponse res) {
    ClientSyncJob syncJob = new ClientSyncJob("button click", getJsonSession().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        getModelObject().getUIFacade().fireActionFromUI();
      }
    };
    syncJob.runNow(new NullProgressMonitor());
  }

}
