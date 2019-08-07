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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class TableTileGridMediator implements PropertyChangeListener, TableListener {

  protected ITable m_table;

  public TableTileGridMediator(ITable table) {
    m_table = table;
    m_table.addPropertyChangeListener(ITable.PROP_TILE_MODE, this);
    m_table.addTableListener(this, TableEvent.TYPE_ROWS_INSERTED);
  }

  @Override
  public void tableChanged(TableEvent e) {
    if (m_table.isTileMode()) {
      loadTiles(e.getRows());
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (m_table.isTileMode()) {
      loadTiles(m_table.getRows());
    }
  }

  protected void loadTiles(List<ITableRow> rows) {
    m_table.createTiles(rows);
  }
}
