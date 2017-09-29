package org.eclipse.scout.rt.ui.html.json.tile;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.shared.data.tile.ITileColorScheme;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonWidget;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonGridData;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;

/**
 * @since 7.1
 */
public class JsonTile<T extends ITile> extends AbstractJsonWidget<T> {

  public JsonTile(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Tile";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<ITile>(ITile.PROP_COLOR_SCHEME, model) {
      @Override
      protected ITileColorScheme modelValue() {
        return getModel().getColorScheme();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return ((ITileColorScheme) value).getIdentifier();
      }
    });
    putJsonProperty(new JsonProperty<T>(ITile.PROP_GRID_DATA_HINTS, model) {
      @Override
      protected GridData modelValue() {
        return getModel().getGridDataHints();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return JsonGridData.toJson((GridData) value);
      }
    });
  }

}
