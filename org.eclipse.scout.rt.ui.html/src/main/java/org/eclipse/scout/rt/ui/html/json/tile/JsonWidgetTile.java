package org.eclipse.scout.rt.ui.html.json.tile;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.tile.IWidgetTile;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;

/**
 * @since 7.1
 */
public class JsonWidgetTile<T extends IWidgetTile> extends JsonTile<T> {

  public JsonWidgetTile(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "WidgetTile";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<T>(IWidgetTile.PROP_TILE_WIDGET, model, getUiSession()) {
      @Override
      protected IWidget modelValue() {
        return getModel().getTileWidget();
      }
    });
  }

}
