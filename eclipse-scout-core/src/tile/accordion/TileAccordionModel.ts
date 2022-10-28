/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AccordionModel, Filter, FilterSupport, TextFilter, Tile, TileAccordionSelectionHandler, TileGridLayoutConfig} from '../../index';
import {Comparator} from '../../types';
import {LogicalGridLayoutConfigModel} from '../../layout/logicalgrid/LogicalGridLayoutConfig';

export default interface TileAccordionModel extends AccordionModel {
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
  tileGridLayoutConfig?: TileGridLayoutConfig | LogicalGridLayoutConfigModel;
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
