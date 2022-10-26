/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {LogicalGridConfig, TileGrid} from '../index';
import {LogicalGridWidget} from '../layout/logicalgrid/LogicalGridData';

export default class TileGridGridConfig extends LogicalGridConfig {
  declare widget: TileGrid;

  constructor() {
    super();
  }

  override getGridWidgets(): LogicalGridWidget[] {
    return this.widget.filteredTiles;
  }
}
