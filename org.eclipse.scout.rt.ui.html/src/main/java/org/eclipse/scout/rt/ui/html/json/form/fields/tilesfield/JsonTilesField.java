package org.eclipse.scout.rt.ui.html.json.form.fields.tilesfield;

import org.eclipse.scout.rt.client.ui.form.fields.tilesfield.ITilesField;
import org.eclipse.scout.rt.client.ui.tile.ITiles;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;

/**
 * @since 7.1
 */
public class JsonTilesField<T extends ITilesField> extends JsonFormField<T> {

  public JsonTilesField(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "TilesField";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<T>(ITilesField.PROP_TILES, model, getUiSession()) {
      @Override
      protected ITiles modelValue() {
        return getModel().getTiles();
      }
    });
  }
}
