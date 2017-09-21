package org.eclipse.scout.rt.client.ui.tile;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.platform.IOrdered;

/**
 * @since 7.1
 */
public interface ITile extends IWidget, IOrdered {
  String PROP_ORDER = "order";
  String PROP_COLOR_SCHEME = "colorScheme";
  String PROP_GRID_DATA_HINTS = "gridDataHints";

  ITileColorScheme getColorScheme();

  void setColorScheme(ITileColorScheme colorScheme);

  /**
   * @return the grid data hints used by the logical grids to create the final grid data
   */
  GridData getGridDataHints();

  void setGridDataHints(GridData data);
}
