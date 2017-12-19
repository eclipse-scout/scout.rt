/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.tile;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.scout.rt.client.ui.accordion.IAccordion;
import org.eclipse.scout.rt.client.ui.group.IGroup;

public interface ITileAccordion<T extends ITile> extends IAccordion {

  String PROP_SELECTABLE = "selectable";
  String PROP_MULTI_SELECT = "multiSelect";
  String PROP_GRID_COLUMN_COUNT = "gridColumnCount";
  String PROP_WITH_PLACEHOLDERS = "withPlaceholders";
  String PROP_TILE_GRID_LAYOUT_CONFIG = "tileGridLayoutConfig";
  String PROP_SHOW_FILTER_COUNT = "showFilterCount";
  String PROP_SELECTED_TILES = "selectedTiles";

  void addTile(T tile);

  void addTiles(List<T> tilesToAdd);

  IGroup getGroupByTile(T tile);

  <G extends IGroup> G getDefaultGroup();

  void setTiles(List<T> tiles);

  boolean isShowFilterCount();

  void setShowFilterCount(boolean showFilterCount);

  /**
   * Adds and activates the given group manager.
   */
  void setGroupManager(ITileAccordionGroupManager<T> groupManager);

  /**
   * @returns the active group manager
   */
  ITileAccordionGroupManager<T> getGroupManager();

  void addGroupManager(ITileAccordionGroupManager<T> groupManager);

  void removeGroupManager(ITileAccordionGroupManager<T> groupManager);

  /**
   * Activates a group manager that matches the given ID.
   */
  void activateGroupManager(Object groupManagerId);

  List<ITileGrid<T>> getTileGrids();

  Stream<T> streamTiles();

  List<T> getTiles();

  int getTileCount();

  void addTilesFilter(ITileFilter filter);

  void removeTilesFilter(ITileFilter filter);

  void deleteAllTiles();

  void deleteTiles(Collection<T> tiles);

  void deleteTile(T tile);

  void deleteTiles(List<T> tilesToDelete);

  void filterTiles();

  void setTileComparator(Comparator<T> comparator);

  T getSelectedTile();

  List<T> getSelectedTiles();

  int getSelectedTileCount();

  void selectTile(T tile);

  void selectTiles(List<T> tiles);

  void selectAllTiles();

  void deselectTile(T tile);

  void deselectTiles(List<T> tiles);

  void deselectAllTiles();

  void setGridColumnCount(int gridColumnCount);

  /**
   * @return the value of {@link ITileGrid#getGridColumnCount()} of the first tile grid assuming that all tile grids use
   *         the same column count
   */
  int getGridColumnCount();

  void setSelectable(boolean selectable);

  /**
   * @return the value of {@link ITileGrid#isSelectable()} of the first tile grid assuming that all tile grids use the same
   *         value
   */
  boolean isSelectable();

  void setMultiSelect(boolean multiSelect);

  /**
   * @return the value of {@link ITileGrid#isMultiSelect()} of the first tile grid assuming that all tile grids use the
   *         same value
   */
  boolean isMultiSelect();

  void setWithPlaceholders(boolean withPlaceholders);

  /**
   * @return the value of {@link ITileGrid#isWithPlaceholders()} of the first tile grid assuming that all tile grids use
   *         the same value
   */
  boolean isWithPlaceholders();

  void setTileGridLayoutConfig(TileGridLayoutConfig layoutConfig);

  /**
   * @return the value of {@link ITileGrid#getLayoutConfig()} of the first tile grid assuming that all tile grids use the
   *         same value
   */
  TileGridLayoutConfig getTileGridLayoutConfig();

}
