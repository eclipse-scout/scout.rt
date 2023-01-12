/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AccordionEventMap, Filter, PropertyChangeEvent, Tile, TileAccordion, TileActionEvent, TileClickEvent} from '../../index';

export interface TileAccordionEventMap extends AccordionEventMap {
  'tileAction': TileActionEvent<TileAccordion>;
  'tileClick': TileClickEvent<TileAccordion>;
  'propertyChange:filters': PropertyChangeEvent<Filter<Tile>[]>;
  'propertyChange:gridColumnCount': PropertyChangeEvent<number>;
  'propertyChange:tiles': PropertyChangeEvent<Filter<Tile>[]>;
  'propertyChange:filteredTiles': PropertyChangeEvent<Tile[]>;
  'propertyChange:selectedTiles': PropertyChangeEvent<Tile[]>;
}
