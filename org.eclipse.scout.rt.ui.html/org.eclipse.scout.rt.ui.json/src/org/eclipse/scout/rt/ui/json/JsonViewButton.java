package org.eclipse.scout.rt.ui.json;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonViewButton extends AbstractJsonRenderer<IViewButton> {

  public JsonViewButton(IViewButton scoutObject,
      IJsonSession jsonSession) {
    super(scoutObject, jsonSession);
  }

  @Override
  public JSONObject toJson() throws JsonUIException {
    try {
      JSONObject json = new JSONObject();
      json.put("objectType", "ViewButton");
      json.put("id", getId());
      json.put(IViewButton.PROP_TEXT, getModelObject().getText());
      json.put("selected", getModelObject().isSelected());
      return json;
    }
    catch (JSONException e) {
      throw new JsonUIException(e.getMessage(), e);
    }
  }

  @Override
  public void handleUiEvent(JsonRequest req, JsonResponse res) throws JsonUIException {
    if ("click".equals(req.getEventType())) {
      handleUiClickEvent(req, res);
    }
    else {
      throw new IllegalArgumentException("unsupported event type");
    }
  }

  protected void handleUiClickEvent(JsonRequest req, JsonResponse res) {
    ClientSyncJob syncJob = new ClientSyncJob("button click", getJsonSession().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        getModelObject().getUIFacade().setSelectedFromUI(true);
        getModelObject().getUIFacade().fireActionFromUI();
      }
    };
    syncJob.runNow(new NullProgressMonitor());
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (IViewButton.PROP_SELECTED.equals(name)) {
      getJsonSession().currentJsonResponse().addPropertyChangeEvent(getId(), name, newValue);
    }
    else {
      super.handleScoutPropertyChange(name, newValue);
    }
  }

}
