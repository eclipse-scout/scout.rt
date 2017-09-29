package org.eclipse.scout.rt.client.ui.form.fields.tilesfield;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.client.ui.tile.ITiles;

public interface ITilesField<T extends ITiles> extends IFormField {

  /**
   * {@link ITiles}
   */
  String PROP_TILES = "tiles";

  void setTiles(T tiles);

  T getTiles();

  <K extends ITile> K getTileByClass(Class<K> tileClass);
}
