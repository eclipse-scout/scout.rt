package org.eclipse.scout.rt.client.ui.tile;

import java.util.List;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;

/**
 * @since 7.1
 */
public interface ITiles extends IWidget, ITypeWithClassId, IExtensibleObject {

  String PROP_TILES = "tiles";
  String PROP_SELECTED_TILES = "selectedTiles";
  String PROP_GRID_COLUMN_COUNT = "gridColumnCount";
  String PROP_WITH_PLACEHOLDERS = "withPlaceholders";
  String PROP_SCROLLABLE = "scrollable";
  String PROP_SELECTABLE = "selectable";
  String PROP_MULTI_SELECT = "multiSelect";
  String PROP_LOGICAL_GRID = "logicalGrid";
  String PROP_LOGICAL_GRID_COLUMN_WIDTH = "logicalGridColumnWidth";
  String PROP_LOGICAL_GRID_ROW_HEIGHT = "logicalGridRowHeight";
  String PROP_LOGICAL_GRID_H_GAP = "logicalGridHGap";
  String PROP_LOGICAL_GRID_V_GAP = "logicalGridVGap";
  String PROP_MAX_CONTENT_WIDTH = "maxContentWidth";
  String PROP_CONTAINER = "container";

  String PROP_ASYNC_LOAD_JOBNAME_PREFIX = "tileAsyncDataLoadJob";
  String PROP_ASYNC_LOAD_IDENTIFIER_PREFIX = "tileAsyncDataLoadIdentifier";
  String PROP_WINDOW_IDENTIFIER_PREFIX = "tileDataLoadWindowsIdentifier";
  String PROP_RUN_CONTEXT_TILE = "tileDataLoadWindowsIdentifier";

  String LOGICAL_GRID_HORIZONTAL = "HorizontalGrid";
  String LOGICAL_GRID_VERTICAL_SMART = "VerticalSmartGrid";

  void initTiles();

  void postInitTilesConfig();

  void disposeTiles();

  /**
   * @return list of tiles. Return value is never <code>null</code>.
   */
  List<? extends ITile> getTiles();

  /**
   * @param tiles
   *          the new list of tiles to be set.
   */
  void setTiles(List<? extends ITile> tiles);

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

  boolean isSelectable();

  void setSelectable(boolean selectable);

  boolean isMultiSelect();

  void setMultiSelect(boolean multiSelect);

  /**
   * @return true if the box should be vertically scrollable, false if not
   */
  boolean isScrollable();

  void setScrollable(boolean scrollable);

  String getLogicalGrid();

  void setLogicalGrid(String logicalGrid);

  /**
   * @return the width in pixels to use for tiles with the logical unit "width = 1". Larger logical widths are
   *         multiplied with this value (and gaps are added).
   */
  int getLogicalGridColumnWidth();

  /**
   * @param logicalGridColumnWidth
   *          the width in pixels to use for tiles with the logical unit "width = 1". Larger logical widths are
   *          multiplied with this value (and gaps are added).
   */
  void setLogicalGridColumnWidth(int logicalGridColumnWidth);

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
   *          the horizontal gap in pixels to use between two logical grid columns.
   */
  void setLogicalGridHGap(int logicalGridGap);

  /**
   * @return the vertical gap in pixels to use between two logical grid rows.
   */
  int getLogicalGridVGap();

  /**
   * @param logicalGridGap
   *          the vertical gap in pixels to use between single tiles.
   */
  void setLogicalGridVGap(int logicalGridGap);

  /**
   * @return the maximum width in pixels to use for the content. The maximum is disabled if this value is
   *         <code>&lt;= 0</code>.
   */
  int getMaxContentWidth();

  /**
   * @param maxContentWidth
   *          the maximum width in pixels to use for the content. The maximum is disabled if this value is
   *          <code>&lt;= 0</code>
   */
  void setMaxContentWidth(int logicalGridMaxContentWidth);

  void addTiles(List<? extends ITile> tiles);

  void addTile(ITile tile);

  void deleteTiles(List<? extends ITile> tiles);

  void deleteTile(ITile tile);

  void deleteAllTiles();

  void selectTiles(List<? extends ITile> tiles);

  void selectTile(ITile tile);

  void selectAllTiles();

  void deselectTiles(List<? extends ITile> tiles);

  void deselectTile(ITile tile);

  void deselectAllTiles();

  List<? extends ITile> getSelectedTiles();

  ITile getSelectedTile();

  <T extends ITile> T getTileByClass(Class<T> tileClass);

  /**
   * Container of the tiles.
   */
  ITypeWithClassId getContainer();

  /**
   * Creates a {@link JobInput} which is used by {@link AbstractTile} to schedule the asynchronous data load.<br>
   * It can be used to add properties to a job so tile loading jobs can be identified later on if necessary
   */
  JobInput createAsyncLoadJobInput(ITile tile);

  void loadTileData();

  void ensureTileDataLoaded();

  ITilesUIFacade getUIFacade();
}
