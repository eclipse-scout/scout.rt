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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumSet;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractTableMenu;
import org.eclipse.scout.rt.client.ui.action.menu.ITableMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.security.CreateCustomColumnPermission;

public class AddCustomColumnMenu extends AbstractTableMenu {
  private final ITable m_table;

  public AddCustomColumnMenu(ITable table) {
    m_table = table;
  }

  @Override
  protected String getConfiguredText() {
    return ScoutTexts.get("AddCustomColumnMenu");
  }

  @Override
  protected EnumSet<TableMenuType> getConfiguredMenuType() {
    return EnumSet.of(ITableMenu.TableMenuType.Header);
  }

  @Override
  protected void execInitAction() throws ProcessingException {
    setVisiblePermission(new CreateCustomColumnPermission());
    m_table.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (ITable.PROP_TABLE_CUSTOMIZER.equals(evt.getPropertyName())) {
          updateVisibility();
        }
      }
    });
    updateVisibility();

  }

  /**
   *
   */
  private void updateVisibility() {
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
