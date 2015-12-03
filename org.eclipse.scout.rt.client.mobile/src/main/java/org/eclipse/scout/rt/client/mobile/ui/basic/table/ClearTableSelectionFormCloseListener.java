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
package org.eclipse.scout.rt.client.mobile.ui.basic.table;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;

/**
 * @since 3.9.0
 */
public class ClearTableSelectionFormCloseListener implements FormListener {
  private ITable m_table;

  public ClearTableSelectionFormCloseListener(ITable table) {
    m_table = table;
  }

  @Override
  public void formChanged(FormEvent e) {
    if (FormEvent.TYPE_CLOSED == e.getType()) {
      handleFormClosed();
    }
  }

  protected void handleFormClosed() {
    clearSelection();
  }

  protected void clearSelection() {
    m_table.selectRow(null);
  }

}
