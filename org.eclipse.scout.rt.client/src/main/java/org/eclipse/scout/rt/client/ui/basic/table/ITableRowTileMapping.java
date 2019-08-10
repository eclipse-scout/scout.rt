/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.basic.table;

import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;

public interface ITableRowTileMapping extends IPropertyObserver {

  String PROP_TABLE_ROW = "tableRow";
  String PROP_TILE = "tile";

  ITableRow getTableRow();

  TableRowTileMapping withTableRow(ITableRow tableRow);

  ITile getTile();

  TableRowTileMapping withTile(ITile tile);

}
