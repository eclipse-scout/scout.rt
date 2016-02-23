/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;

/**
 * This column may be used if the value is relevant for the gui, and not just the display text.
 */
public abstract class AbstractBeanColumn<VALUE> extends AbstractColumn<VALUE> implements IBeanColumn<VALUE> {

  @Override
  protected boolean getConfiguredUiSortPossible() {
    return true;
  }

  protected String getPlainText(ITableRow row) {
    return null;
  }

  @Override
  protected void execDecorateCell(Cell cell, ITableRow row) {
    cell.setText(getPlainText(row)); // used for excel export
  }
}
