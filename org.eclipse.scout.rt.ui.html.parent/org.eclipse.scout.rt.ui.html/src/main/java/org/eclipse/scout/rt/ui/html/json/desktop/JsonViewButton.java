package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonProperty;

public class JsonViewButton extends AbstractJsonPropertyObserver<IViewButton> {

  public JsonViewButton(IViewButton model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);
  }

  @Override
  protected void initProperties(IViewButton model) {
    super.initProperties(model);

    putJsonProperty(new JsonProperty<IViewButton>(IViewButton.PROP_TEXT, model) {
      @Override
      protected String modelValue() {
        return getModel().getText();
      }
    });
    putJsonProperty(new JsonProperty<IViewButton>(IViewButton.PROP_ICON_ID, model) {
      @Override
      protected String modelValue() {
        return getModel().getIconId();
      }
    });
    putJsonProperty(new JsonProperty<IViewButton>(IViewButton.PROP_SELECTED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isSelected();
      }
    });
    putJsonProperty(new JsonProperty<IViewButton>(IViewButton.PROP_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isEnabled();
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
    // TODO selection call
    getModel().getUIFacade().setSelectedFromUI(true);
    getModel().getUIFacade().fireActionFromUI();
  }

}
