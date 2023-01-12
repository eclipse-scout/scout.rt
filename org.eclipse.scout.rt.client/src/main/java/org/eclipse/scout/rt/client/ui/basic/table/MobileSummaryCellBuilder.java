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
