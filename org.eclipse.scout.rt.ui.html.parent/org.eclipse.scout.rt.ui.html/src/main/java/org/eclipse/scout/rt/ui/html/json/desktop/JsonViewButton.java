package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserverAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonProperty;

public class JsonViewButton extends AbstractJsonPropertyObserverAdapter<IViewButton> {

  public JsonViewButton(IViewButton modelObject, IJsonSession jsonSession, String id) {
    super(modelObject, jsonSession, id);
    putJsonProperty(new JsonProperty<IViewButton, String>(IViewButton.PROP_TEXT, modelObject) {
      @Override
      protected String getValueImpl(IViewButton button) {
        return button.getText();
      }
    });
    putJsonProperty(new JsonProperty<IViewButton, String>(IViewButton.PROP_ICON_ID, modelObject) {
      @Override
      protected String getValueImpl(IViewButton button) {
        return button.getIconId();
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
    getModelObject().getUIFacade().fireActionFromUI();
  }

}
