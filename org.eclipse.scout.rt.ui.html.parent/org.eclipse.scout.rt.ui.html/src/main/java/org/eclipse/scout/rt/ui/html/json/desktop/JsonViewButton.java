package org.eclipse.scout.rt.ui.html.json.desktop;

import java.lang.reflect.Field;

import org.eclipse.scout.rt.client.ui.action.view.IViewButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutlineViewButton;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonException;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.json.JSONObject;

public class JsonViewButton<T extends IViewButton> extends AbstractJsonPropertyObserver<T> {

  public JsonViewButton(T model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);

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
  public JSONObject toJson() {
    JSONObject json = super.toJson();

    //Only return if attached
    IOutline outline = getOutline();
    if (getJsonSession().getJsonAdapter(outline) != null) {
      putAdapterIdProperty(json, "outline", outline);
    }
    return json;
  }

  @Override
  protected void disposeChildAdapters() {
    super.disposeChildAdapters();
    optDisposeAdapter(getOutline());
  }

  //FIXME provide proper api on AbstractOutlineViewButton
  private IOutline getOutline() {
    Field field;
    try {
      field = AbstractOutlineViewButton.class.getDeclaredField("m_outline");

      field.setAccessible(true);
      return (IOutline) field.get(getModel());

    }
    catch (NoSuchFieldException | IllegalAccessException e) {
      throw new JsonException(e);
    }
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
    //Lazy attaching
    IOutline outline = getOutline();
    if (outline != null && getJsonSession().getJsonAdapter(outline) == null) {
      IJsonAdapter<?> jsonOutline = attachAdapter(outline);
      addPropertyChangeEvent("outline", jsonOutline.getId());
    }

    getModel().getUIFacade().setSelectedFromUI(true);
    getModel().getUIFacade().fireActionFromUI();
  }

}
