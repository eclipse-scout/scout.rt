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
