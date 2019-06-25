/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
