/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AccordionModel, Comparator, Filter, FilterSupport, ObjectOrModel, TextFilter, Tile, TileAccordionSelectionHandler, TileGridLayoutConfig} from '../../index';

export interface TileAccordionModel extends AccordionModel {
  /** @see TileGridModel.gridColumnCount */
  gridColumnCount?: number;
  /** @see TileGridModel.multiSelect */
  multiSelect?: boolean;
  /** @see TileGridModel.selectable */
  selectable?: boolean;
  takeTileFiltersFromGroup?: boolean;
  /** @see TileGridModel.comparator */
  tileComparator?: Comparator<Tile>;
  /** @see TileGridModel.filters */
  filters?: Filter<Tile>[];
  /** @see TileGridModel.layoutConfig */
  tileGridLayoutConfig?: ObjectOrModel<TileGridLayoutConfig>;
  tileGridSelectionHandler?: TileAccordionSelectionHandler;
  /** @see TileGridModel.withPlaceholders */
  withPlaceholders?: boolean;
  /** @see TileGridModel.virtual */
  virtual?: boolean;
  /** @see TileGridModel.textFilterEnabled */
  textFilterEnabled?: boolean;
  /** @see TileGridModel.filterSupport */
  filterSupport?: FilterSupport<Tile>;
  /** @see TileGridModel.createTextFilter */
  createTextFilter?: () => TextFilter<Tile>;
  /** @see TileGridModel.updateTextFilterText */
  updateTextFilterText?: string;
}
