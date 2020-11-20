/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.basic.table;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;

public class MobileSummaryCellBuilder implements ISummaryCellBuilder {
  private ITableCompactHandler m_compactHandler;

  public MobileSummaryCellBuilder(ITableCompactHandler compactHandler) {
    m_compactHandler = compactHandler;
  }

  public ITableCompactHandler getCompactHandler() {
    return m_compactHandler;
  }

  public void setCompactHandler(ITableCompactHandler compactHandler) {
    m_compactHandler = compactHandler;
  }

  @Override
  public ICell build(ITableRow row) {
    Cell cell = new Cell();
    if (cell.getIconId() == null) {
      cell.setIconId(row.getIconId());
    }
    String compactValue = m_compactHandler.buildValue(row);
    cell.setText(compactValue);
    cell.setHtmlEnabled(true);
    return cell;
  }
}
