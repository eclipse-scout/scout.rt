package org.eclipse.scout.rt.client.ui.form.fields.tilefield;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.tile.ITileGrid;

public interface ITileField<T extends ITileGrid> extends IFormField {

  /**
   * {@link ITileGrid}
   */
  String PROP_TILE_GRID = "tileGrid";

  void setTileGrid(T tileGrid);

  T getTileGrid();
}
