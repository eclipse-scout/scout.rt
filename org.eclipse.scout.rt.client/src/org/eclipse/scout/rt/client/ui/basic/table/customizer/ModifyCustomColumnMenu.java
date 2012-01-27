/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.customizer;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.security.UpdateCustomColumnPermission;

public class ModifyCustomColumnMenu extends AbstractMenu {
  private final ITable m_table;

  public ModifyCustomColumnMenu(ITable table) {
    m_table = table;
  }

  @Override
  protected String getConfiguredText() {
    return ScoutTexts.get("ModifyCustomColumnMenu");
  }

  @Override
  protected void execInitAction() throws ProcessingException {
    setVisiblePermission(new UpdateCustomColumnPermission());
  }

  @Override
  protected void execPrepareAction() throws ProcessingException {
    ITableCustomizer cst = m_table.getTableCustomizer();
    IColumn<?> col = m_table.getContextColumn();
    setVisible(cst != null && col instanceof ICustomColumn<?>);
  }

  @Override
  protected void execAction() throws ProcessingException {
    if (m_table != null) {
      ITableCustomizer cst = m_table.getTableCustomizer();
      IColumn<?> col = m_table.getContextColumn();
      if (cst != null && col instanceof ICustomColumn<?>) {
        cst.modifyColumn((ICustomColumn<?>) col);
      }
    }
  }
}
