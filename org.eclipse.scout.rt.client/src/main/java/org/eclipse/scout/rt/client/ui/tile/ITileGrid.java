/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.tile;

import java.util.Comparator;
import java.util.List;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner;
import org.eclipse.scout.rt.client.ui.action.menu.root.ITileGridContextMenu;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;

/**
 * @since 8.0
 */
public interface ITileGrid<T extends ITile> extends IWidget, IExtensibleObject, IContributionOwner, IContextMenuOwner {

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
  String PROP_CONTEXT_MENU = "contextMenus";
  String PROP_VIRTUAL = "virtual";
  String PROP_ANIMATE_TILE_REMOVAL = "animateTileRemoval";
  String PROP_ANIMATE_TILE_INSERTION = "animateTileInsertion";
  String PROP_TEXT_FILTER_ENABLED = "textFilterEnabled";
  String PROP_WRAPPABLE = "wrappable";

  String PROP_ASYNC_LOAD_JOBNAME_PREFIX = "tileAsyncDataLoadJob";
  String PROP_ASYNC_LOAD_IDENTIFIER_PREFIX = "tileAsyncDataLoadIdentifier";
  String PROP_WINDOW_IDENTIFIER_PREFIX = "tileDataLoadWindowsIdentifier";
  String PROP_RUN_CONTEXT_TILE_LOAD_CANCELLABLE = "tileDataLoadWindowsIdentifier";

  String LOGICAL_GRID_HORIZONTAL = "HorizontalGrid";
  String LOGICAL_GRID_VERTICAL_SMART = "VerticalSmartGrid";

  /**
   * @return list of tiles. Return value is never <code>null</code>.
   */
  List<T> getTiles();

  int getTileCount();

  /**
   * @param tiles
   *          the new list of tiles to be set.
   */
  void setTiles(List<T> tiles);

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

  boolean isWrappable();

  void setWrappable(boolean wrappable);

  String getLogicalGrid();

  void setLogicalGrid(String logicalGrid);

  void setLayoutConfig(TileGridLayoutConfig config);

  TileGridLayoutConfig getLayoutConfig();

  boolean isVirtual();

  void setVirtual(boolean virtual);

  boolean isAnimateTileRemoval();

  void setAnimateTileRemoval(boolean animateTileRemoval);

  boolean isAnimateTileInsertion();

  void setAnimateTileInsertion(boolean animateTileInsertion);

  boolean isTextFilterEnabled();

  void setTextFilterEnabled(boolean textFilterEnabled);

  void addTiles(List<T> tiles);

  void addTile(T tile);

  void deleteTiles(List<T> tiles);

  void deleteTile(T tile);

  void deleteAllTiles();

  void selectTiles(List<T> tiles);

  void selectTile(T tile);

  void selectAllTiles();

  void deselectTiles(List<T> tiles);

  void deselectTile(T tile);

  void deselectAllTiles();

  List<T> getSelectedTiles();

  int getSelectedTileCount();

  T getSelectedTile();

  T getTileByClass(Class<T> tileClass);

  /**
   * Creates a {@link JobInput} which is used by {@link AbstractTile} to schedule the asynchronous data load.<br>
   * It can be used to add properties to a job so tile loading jobs can be identified later on if necessary
   */
  JobInput createAsyncLoadJobInput(ITile tile);

  void loadTileData();

  void ensureTileDataLoaded();

  List<ITileFilter<T>> getFilters();

  /**
   * Adds a filter and calls {@link #filter()}.
   */
  void addFilter(ITileFilter<T> filter);

  void addFilter(ITileFilter<T> filter, boolean applyFilters);

  /**
   * Removes a filter and calls {@link #filter()}.
   */
  void removeFilter(ITileFilter<T> filter);

  void removeFilter(ITileFilter<T> filter, boolean applyFilters);

  /**
   * Applies every filter.
   * <p>
   * This method is typically executed automatically, but if you add or remove filters with applyFilters parameter set
   * to false, you need to call this method by yourself.
   */
  void filter();

  List<T> getFilteredTiles();

  int getFilteredTileCount();

  /**
   * Sets a comparator which is used by {@link #sort()} and calls {@link #sort()} immediately. The tiles are sorted as
   * well whenever new tiles are added.
   */
  void setComparator(Comparator<T> comparator);

  void setComparator(Comparator<T> comparator, boolean sortNow);

  Comparator<T> getComparator();

  /**
   * Sorts the tiles by using the active {@link Comparator}. If no comparator is set the tiles are displayed according
   * to the insertion order.
   * <p>
   * This method is typically executed automatically, but if you set a comparator with sortNow parameter set to false,
   * you need to call this method by yourself.
   */
  void sort();

  void setMenus(List<? extends IMenu> menus);

  @Override
  ITileGridContextMenu getContextMenu();

  IFastListenerList<TileGridListener> tileGridListeners();

  default void addTileGridListener(TileGridListener listener) {
    tileGridListeners().add(listener);
  }

  default void removeTileGridListener(TileGridListener listener) {
    tileGridListeners().remove(listener);
  }

  ITileGridUIFacade getUIFacade();
}
