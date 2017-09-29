package org.eclipse.scout.rt.client.ui.tile;

import java.util.List;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;

/**
 * @since 7.1
 */
public interface ITiles extends IWidget, ITypeWithClassId {

  String PROP_TILES = "tiles";
  String PROP_GRID_COLUMN_COUNT = "gridColumnCount";
  String PROP_WITH_PLACEHOLDERS = "withPlaceholders";
  String PROP_SCROLLABLE = "scrollable";
  String PROP_LOGICAL_GRID_ROW_HEIGHT = "logicalGridRowHeight";
  String PROP_LOGICAL_GRID_H_GAP = "logicalGridHGap";
  String PROP_LOGICAL_GRID_V_GAP = "logicalGridVGap";

  /**
   * @return list of tiles. Return value is never <code>null</code>.
   */
  List<ITile> getTiles();

  /**
   * @param tiles
   *          the new list of tiles to be set.
   */
  void setTiles(List<ITile> tiles);

  /**
   * @return the preferred number of grid columns.
   */
  int getGridColumnCount();

  /**
   * @param gridColumnCount
   *          the preferred number of grid columns.
   */
  void setGridColumnCount(int gridColumnCount);

  /**
   * @return true to fill up a row with placeholder tiles, false if not
   */
  boolean isWithPlaceholders();

  void setWithPlaceholders(boolean withPlaceholders);

  /**
   * @return true if the box should be vertically scrollable, false if not
   */
  boolean isScrollable();

  void setScrollable(boolean scrollable);

  /**
   * @return the height in pixels to use for tiles with the logical unit "height = 1". Larger logical heights are
   *         multiplied with this value (and gaps are added).
   */
  int getLogicalGridRowHeight();

  /**
   * @param logicalGridRowHeight
   *          the height in pixels to use for tiles with the logical unit "height = 1". Larger logical heights are
   *          multiplied with this value (and gaps are added).
   */
  void setLogicalGridRowHeight(int logicalGridRowHeight);

  /**
   * @return the horizontal gap in pixels to use between single tiles.
   */
  int getLogicalGridHGap();

  /**
   * @param logicalGridGap
   *          the horizontal gap in pixels to use between single tiles.
   */
  void setLogicalGridHGap(int logicalGridGap);

  /**
   * @return the vertical gap in pixels to use between single tiles.
   */
  int getLogicalGridVGap();

  /**
   * @param logicalGridGap
   *          the vertical gap in pixels to use between single tiles.
   */
  void setLogicalGridVGap(int logicalGridGap);

  <T extends ITile> T getTileByClass(Class<T> tileClass);
}
