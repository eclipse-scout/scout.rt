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
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.security.CreateCustomColumnPermission;

public class AddCustomColumnMenu extends AbstractMenu {
  private final ITable m_table;

  public AddCustomColumnMenu(ITable table) {
    m_table = table;
  }

  @Override
  protected String getConfiguredText() {
    return ScoutTexts.get("AddCustomColumnMenu");
  }

  @Override
  protected void execInitAction() throws ProcessingException {
    setVisiblePermission(new CreateCustomColumnPermission());
  }

  @Override
  protected void execPrepareAction() throws ProcessingException {
    setVisible(m_table.getTableCustomizer() != null);
  }

  @Override
  protected void execAction() throws ProcessingException {
    if (m_table != null) {
      if (m_table.getTableCustomizer() != null) {
        m_table.getTableCustomizer().addColumn();
      }
    }
  }
}
