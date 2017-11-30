package org.eclipse.scout.rt.client.ui.tile;

import java.util.List;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner;
import org.eclipse.scout.rt.client.ui.action.menu.root.ITilesContextMenu;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;

/**
 * @since 7.1
 */
public interface ITiles extends IWidget, ITypeWithClassId, IExtensibleObject, IContributionOwner, IContextMenuOwner {

  String PROP_TILES = "tiles";
  String PROP_SELECTED_TILES = "selectedTiles";
  String PROP_FILTERED_TILES = "filteredTiles";
  String PROP_GRID_COLUMN_COUNT = "gridColumnCount";
  String PROP_WITH_PLACEHOLDERS = "withPlaceholders";
  String PROP_SCROLLABLE = "scrollable";
  String PROP_SELECTABLE = "selectable";
  String PROP_MULTI_SELECT = "multiSelect";
  String PROP_LOGICAL_GRID = "logicalGrid";
  String PROP_LAYOUT_CONFIG = "layoutConfig";
  String PROP_CONTAINER = "container";
  String PROP_CONTEXT_MENU = "contextMenus";

  String PROP_ASYNC_LOAD_JOBNAME_PREFIX = "tileAsyncDataLoadJob";
  String PROP_ASYNC_LOAD_IDENTIFIER_PREFIX = "tileAsyncDataLoadIdentifier";
  String PROP_WINDOW_IDENTIFIER_PREFIX = "tileDataLoadWindowsIdentifier";
  String PROP_RUN_CONTEXT_TILE = "tileDataLoadWindowsIdentifier";

  String LOGICAL_GRID_HORIZONTAL = "HorizontalGrid";
  String LOGICAL_GRID_VERTICAL_SMART = "VerticalSmartGrid";

  /**
   * @return list of tiles. Return value is never <code>null</code>.
   */
  List<? extends ITile> getTiles();

  int getTileCount();

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

  void setLayoutConfig(TilesLayoutConfig config);

  TilesLayoutConfig getLayoutConfig();

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

  int getSelectedTileCount();

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

  List<ITileFilter> getFilters();

  void addFilter(ITileFilter filter);

  void addFilter(ITileFilter filter, boolean applyFilters);

  void removeFilter(ITileFilter filter);

  void removeFilter(ITileFilter filter, boolean applyFilters);

  /**
   * Applies every filter.
   * <p>
   * This method is typically executed automatically, but if you add or remove filters with applyFilters parameter set
   * to false, you need to call this method by yourself.
   */
  void filter();

  List<? extends ITile> getFilteredTiles();

  int getFilteredTileCount();

  /**
   * @param menus
   */
  void setMenus(List<? extends IMenu> menus);

  @Override
  ITilesContextMenu getContextMenu();

  ITilesUIFacade getUIFacade();
}
