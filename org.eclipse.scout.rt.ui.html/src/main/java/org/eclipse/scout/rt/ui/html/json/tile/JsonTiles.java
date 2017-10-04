package org.eclipse.scout.rt.ui.html.json.tile;

import java.util.List;

import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.client.ui.tile.ITiles;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonWidget;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;

/**
 * @since 7.1
 */
public class JsonTiles<T extends ITiles> extends AbstractJsonWidget<T> {

  public JsonTiles(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Tiles";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<T>(ITiles.PROP_TILES, model, getUiSession()) {
      @Override
      protected List<? extends ITile> modelValue() {
        return getModel().getTiles();
      }
    });
    putJsonProperty(new JsonProperty<ITiles>(ITiles.PROP_GRID_COLUMN_COUNT, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getGridColumnCount();
      }
    });
    putJsonProperty(new JsonProperty<T>(ITiles.PROP_WITH_PLACEHOLDERS, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isWithPlaceholders();
      }
    });
    putJsonProperty(new JsonProperty<T>(ITiles.PROP_SCROLLABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isScrollable();
      }
    });
    putJsonProperty(new JsonProperty<T>(ITiles.PROP_LOGICAL_GRID, model) {
      @Override
      protected String modelValue() {
        return getModel().getLogicalGrid();
      }
    });
    putJsonProperty(new JsonProperty<T>(ITiles.PROP_LOGICAL_GRID_COLUMN_WIDTH, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getLogicalGridColumnWidth();
      }
    });
    putJsonProperty(new JsonProperty<T>(ITiles.PROP_LOGICAL_GRID_ROW_HEIGHT, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getLogicalGridRowHeight();
      }
    });
    putJsonProperty(new JsonProperty<T>(ITiles.PROP_LOGICAL_GRID_H_GAP, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getLogicalGridHGap();
      }
    });
    putJsonProperty(new JsonProperty<T>(ITiles.PROP_LOGICAL_GRID_V_GAP, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getLogicalGridVGap();
      }
    });
    putJsonProperty(new JsonProperty<T>(ITiles.PROP_MAX_CONTENT_WIDTH, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getMaxContentWidth();
      }
    });
  }
}
