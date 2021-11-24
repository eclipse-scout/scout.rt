/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.tile;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.scout.rt.client.ui.accordion.IAccordion;
import org.eclipse.scout.rt.client.ui.group.IGroup;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;

public interface ITileAccordion<T extends ITile> extends IAccordion, IExtensibleObject {

  String PROP_SELECTABLE = "selectable";
  String PROP_MULTI_SELECT = "multiSelect";
  String PROP_GRID_COLUMN_COUNT = "gridColumnCount";
  String PROP_WITH_PLACEHOLDERS = "withPlaceholders";
  String PROP_VIRTUAL = "virtual";
  String PROP_TILE_GRID_LAYOUT_CONFIG = "tileGridLayoutConfig";
  String PROP_TILE_COMPARATOR = "tileComparator";
  String PROP_SELECTED_TILES = "selectedTiles";
  String PROP_TEXT_FILTER_ENABLED = "textFilterEnabled";

  void addTile(T tile);

  void addTiles(List<T> tilesToAdd);

  IGroup getGroupById(Object groupId);

  IGroup getGroupByTile(T tile);

  <G extends IGroup> G getDefaultGroup();

  void setTiles(List<T> tiles);

  /**
   * Adds and activates the given group manager.
   */
  void setGroupManager(ITileAccordionGroupManager<T> groupManager);

  /**
   * @return the active group manager
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

  List<T> getFilteredTiles();

  int getFilteredTileCount();

  void addTileFilter(ITileFilter<T> filter);

  void removeTileFilter(ITileFilter<T> filter);

  void deleteAllTiles();

  void deleteTiles(Collection<T> tiles);

  void deleteTile(T tile);

  void deleteTiles(List<T> tilesToDelete);

  void filterTiles();

  void setTileComparator(Comparator<T> comparator);

  Comparator<T> getTileComparator();

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

  int getGridColumnCount();

  void setSelectable(boolean selectable);

  boolean isSelectable();

  void setMultiSelect(boolean multiSelect);

  boolean isMultiSelect();

  void setWithPlaceholders(boolean withPlaceholders);

  boolean isWithPlaceholders();

  void setVirtual(boolean virtual);

  boolean isVirtual();

  void setTextFilterEnabled(boolean textFilterEnabled);

  boolean isTextFilterEnabled();

  void setTileGridLayoutConfig(TileGridLayoutConfig layoutConfig);

  TileGridLayoutConfig getTileGridLayoutConfig();

  IFastListenerList<TileGridListener> tileGridListeners();

  default void addTileGridListener(TileGridListener listener) {
    tileGridListeners().add(listener);
  }

  default void removeTileGridListener(TileGridListener listener) {
    tileGridListeners().remove(listener);
  }

}
