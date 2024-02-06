/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Comparator, Filter, FilterSupport, Menu, ObjectOrChildModel, ObjectOrModel, TextFilter, Tile, TileGridLayoutConfig, TileGridSelectionHandler, VirtualScrolling, WidgetModel} from '../index';

export interface TileGridModel extends WidgetModel {
  /**
   * Specifies whether tiles should be animated when they are deleted. Default is true.
   */
  animateTileRemoval?: boolean;
  /**
   * Specifies whether tiles should be animated when they are inserted. Default is true.
   */
  animateTileInsertion?: boolean;
  /**
   * If a comparator is set, the tiles are sorted according to that comparator. Default is null.
   */
  comparator?: Comparator<Tile>;
  /**
   * The filters control which tiles are allowed to be displayed in the grid.
   *
   * If one of the filters does not accept a specific tile, the tile won't be shown. Hence, all filters must agree to make a tile visible.
   *
   * By default, there are no filters.
   *
   * @see TileGrid.filteredTiles
   * @see Table.tiles
   */
  filters?: Filter<Tile>[];
  /**
   * Configures the preferred number of columns in the tile grid. Default is 4.
   *
   * The number of columns will be reduced automatically by the {@link TileGridLayout},
   * if the width of the tile grid is too small to display all tiles of a row.
   */
  gridColumnCount?: number;
  /**
   * Specifies the layouting hints for the tile grid used by {@link TileGridLayout}.
   *
   * By default, an empty {@link TileGridLayoutConfig} is used which means the values are read by CSS.
   * @see TileGridLayout._initDefaults
   */
  layoutConfig?: ObjectOrModel<TileGridLayoutConfig>;
  menus?: ObjectOrChildModel<Menu>[];
  /**
   * Specifies whether multiple tiles can be selected at once. Default is true.
   */
  multiSelect?: boolean;
  /**
   * Specifies whether an animation should be executed whenever the tile grid is rendered.
   *
   * The animation is quite small and discreet and just moves the tiles a little.
   * It will happen if the startup animation is disabled or done and every time the tiles are rendered anew.
   *
   * Default is false.
   */
  renderAnimationEnabled?: boolean;
  /**
   * Specifies whether the tiles can be selected. Default is false.
   */
  selectable?: boolean;
  /**
   * Specifies the tiles to be selected. By default, no tiles are selected.
   */
  selectedTiles?: (Tile | string)[];
  selectionHandler?: TileGridSelectionHandler;
  /**
   * Specifies whether the tile grid should be scrollable or not.
   *
   * If true, it will be vertically scrollable.
   * It will also be horizontally scrollable but only if {@link LogicalGridLayoutConfig.minWidth} is set.
   *
   * Default is false.
   */
  scrollable?: boolean;
  /**
   * Specifies whether an animation should be executed when the tile grid is rendered the first time.
   *
   * Default is false.
   */
  startupAnimationEnabled?: boolean;
  /**
   * Specifies the tiles of the tile grid. By default, there are no tiles.
   */
  tiles?: ObjectOrChildModel<Tile>[];
  /**
   * Specifies the number of grid rows to be rendered if virtual scrolling is active (if {@link virtual} is true).
   * The value depends on the grid size and will be calculated automatically by the layout using {@link VirtualScrolling.calculateViewRangeSize}.
   *
   * It can be set manually for testing purposes. Keep in mind that it will be overridden as soon as the grid gets layouted.
   */
  viewRangeSize?: number;
  /**
   * Virtual relates to the term "Virtual Scrolling". This means, only the tiles in the view port and some more will be
   * rendered. The others will be rendered as soon as they will be moved into the view port, either by scrolling or by
   * any other action like sorting, filtering etc. This can lead to a big performance boost when having many tiles.
   *
   * To make Virtual Scrolling work, the real width and height needs to be known so that scroll bar position can be
   * calculated correctly. This means Virtual Scrolling does only work if all tiles have the same size, so the following
   * {@link GridData} properties cannot be used: useUiWidth, useUiHeight, widthInPixel, heightInPixel, w, h.
   *
   * If these preconditions are given, you can use the virtual mode and your grid will be able to handle a lot of tiles.
   *
   * Default is false.
   */
  virtual?: boolean;
  /**
   * If enabled, artificial placeholder tiles are added automatically if the tiles in the last row don't fill the whole row.
   *
   * Default is false.
   */
  withPlaceholders?: boolean;
  /**
   * If enabled, a text field is shown when the tile grid is focused or hovered so the user can filter the tiles by typing.
   *
   * Default is false.
   */
  textFilterEnabled?: boolean;
  filterSupport?: FilterSupport<Tile>;
  /**
   * Factory function to create a custom {@link TextFilter} that is used if {@link textFilterEnabled} is set to true.
   *
   * If no function is set, the {@link TileTextFilter} will be used that will convert the rendered content of the tile to plain text and apply the filter to that.
   *
   * Default is null which means the {@link TileTextFilter} is active.
   */
  createTextFilter?: () => TextFilter<Tile>;
  /**
   * A function to update the {@link TextFilter.acceptedText}.
   *
   * Setting this property may be necessary if a custom {@link TextFilter} is set using {@link createTextFilter}.
   *
   * Default is null which means {@link TileGrid._updateTextFilterText} is used that works with {@link TileTextFilter.setText}.
   */
  updateTextFilterText?: string;
  defaultMenuTypes?: string[];
  /**
   * Defines whether the grid column count should be dynamically adjusted if the preferred width of the grid exceeds the available width.
   *
   * Default is true.
   */
  wrappable?: boolean;
}
