package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineViewButton;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.action.JsonAction;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonGlobalAdapterProperty;

public class JsonOutlineViewButton<T extends IOutlineViewButton> extends JsonAction<T> {

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
    putJsonProperty(new JsonGlobalAdapterProperty<T>("outline", model, getUiSession()) {
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
  public void handleUiEvent(JsonEvent event) {
    if (JsonEventType.CLICKED.matches(event)) {
      handleUiClick(event);
    }
    else if (EVENT_DO_ACTION.equals(event.getType())) {
      handleUiClick(event);
    }
    else {
      throw new IllegalArgumentException("unsupported event type");
    }
  }

  protected void handleUiClick(JsonEvent event) {
    getModel().getUIFacade().setSelectedFromUI(true);
    getModel().getUIFacade().fireActionFromUI();
  }

}
