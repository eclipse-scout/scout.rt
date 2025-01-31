/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
