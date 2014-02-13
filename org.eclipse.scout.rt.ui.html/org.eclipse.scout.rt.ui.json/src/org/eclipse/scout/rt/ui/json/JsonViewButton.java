package org.eclipse.scout.rt.ui.json;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonViewButton extends AbstractJsonRenderer<IViewButton> {

  public JsonViewButton(IViewButton scoutObject,
      IJsonEnvironment jsonEnvironment) {
    super(scoutObject, jsonEnvironment);
  }

  @Override
  public JSONObject toJson() throws ProcessingException {
    try {
      JSONObject json = new JSONObject();
      json.put("id", getId());
      json.put(IViewButton.PROP_TEXT, getScoutObject().getText());
      json.put("selected", getScoutObject().isSelected());
      return json;
    }
    catch (JSONException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }

  @Override
  public void handleUiEvent(String type) {
    if ("click".equals(type)) {
      ClientSyncJob syncJob = new ClientSyncJob("button click", getJsonEnvironment().getClientSession()) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          getScoutObject().getUIFacade().setSelectedFromUI(true);
          getScoutObject().getUIFacade().fireActionFromUI();
        }
      };
      syncJob.runNow(new NullProgressMonitor());
    }
    else {
      throw new IllegalArgumentException("unsupported event type");
    }
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    if (IViewButton.PROP_SELECTED.equals(name)) {
      getJsonEnvironment().addUpdateEvent(getId(), name, newValue);
    }
    else {
      super.handleScoutPropertyChange(name, newValue);
    }
  }

}
