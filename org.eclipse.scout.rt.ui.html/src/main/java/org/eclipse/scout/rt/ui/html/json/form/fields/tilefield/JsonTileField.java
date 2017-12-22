package org.eclipse.scout.rt.ui.html.json.form.fields.tilefield;

import org.eclipse.scout.rt.client.ui.form.fields.tilefield.ITileField;
import org.eclipse.scout.rt.client.ui.tile.ITileGrid;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;

/**
 * @since 7.1
 */
public class JsonTileField<T extends ITileField> extends JsonFormField<T> {

  public JsonTileField(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "TileField";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<T>(ITileField.PROP_TILE_GRID, model, getUiSession()) {
      @Override
      protected ITileGrid modelValue() {
        return getModel().getTileGrid();
      }
    });
  }
}
