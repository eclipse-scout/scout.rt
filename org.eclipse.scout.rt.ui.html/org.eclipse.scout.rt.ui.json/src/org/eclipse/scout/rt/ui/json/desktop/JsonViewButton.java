package org.eclipse.scout.rt.ui.json.desktop;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.ui.json.AbstractJsonPropertyObserverRenderer;
import org.eclipse.scout.rt.ui.json.IJsonSession;
import org.eclipse.scout.rt.ui.json.JsonEvent;
import org.eclipse.scout.rt.ui.json.JsonEventType;
import org.eclipse.scout.rt.ui.json.JsonResponse;
import org.json.JSONObject;

public class JsonViewButton extends AbstractJsonPropertyObserverRenderer<IViewButton> {

  public JsonViewButton(IViewButton modelObject, IJsonSession jsonSession, String id) {
    super(modelObject, jsonSession, id);
    delegateProperty(IViewButton.PROP_TEXT);
    delegateProperty(IViewButton.PROP_SELECTED);
  }

  @Override
  public String getObjectType() {
    return "ViewButton";
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putProperty(json, IViewButton.PROP_TEXT, getModelObject().getText());
    putProperty(json, IViewButton.PROP_SELECTED, getModelObject().isSelected());
    return json;
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if (JsonEventType.CLICK.matches(event)) {
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
