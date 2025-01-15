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
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;

@Bean
public class TableRowTileMapping extends AbstractPropertyObserver implements ITableRowTileMapping {

  @Override
  public ITableRow getTableRow() {
    return (ITableRow) propertySupport.getProperty(PROP_TABLE_ROW);
  }

  @Override
  public TableRowTileMapping withTableRow(ITableRow tableRow) {
    propertySupport.setProperty(PROP_TABLE_ROW, tableRow);
    return this;
  }

  @Override
  public ITile getTile() {
    return (ITile) propertySupport.getProperty(PROP_TILE);
  }

  @Override
  public TableRowTileMapping withTile(ITile tile) {
    propertySupport.setProperty(PROP_TILE, tile);
    return this;
  }
}
