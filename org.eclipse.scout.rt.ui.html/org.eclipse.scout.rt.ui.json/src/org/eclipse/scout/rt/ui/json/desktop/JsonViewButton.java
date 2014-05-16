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
import org.eclipse.scout.rt.ui.json.form.fields.JsonProperty;

public class JsonViewButton extends AbstractJsonPropertyObserverRenderer<IViewButton> {

  public JsonViewButton(IViewButton modelObject, IJsonSession jsonSession, String id) {
    super(modelObject, jsonSession, id);
    putJsonProperty(new JsonProperty<IViewButton, String>(IViewButton.PROP_TEXT, modelObject) {
      @Override
      protected String getValueImpl(IViewButton button) {
        return button.getText();
      }
    });
    putJsonProperty(new JsonProperty<IViewButton, Boolean>(IViewButton.PROP_SELECTED, modelObject) {
      @Override
      protected Boolean getValueImpl(IViewButton button) {
        return button.isSelected();
      }
    });
  }

  @Override
  public String getObjectType() {
    return "ViewButton";
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
    new ClientSyncJob("button click", getJsonSession().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        getModelObject().getUIFacade().fireActionFromUI();
      }
    }.runNow(new NullProgressMonitor());
  }

}
