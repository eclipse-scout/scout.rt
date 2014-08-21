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
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBox;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.security.DeleteCustomColumnPermission;

public class RemoveCustomColumnMenu extends AbstractMenu {
  private final ITable m_table;

  public RemoveCustomColumnMenu(ITable table) {
    m_table = table;
  }

  @Override
  protected String getConfiguredText() {
    return ScoutTexts.get("RemoveCustomColumnMenu");
  }

  @Override
  protected Set<IMenuType> getConfiguredMenuTypes() {
    return CollectionUtility.<IMenuType> hashSet(TableMenuType.Header);
  }

  @Override
  protected void execInitAction() throws ProcessingException {
    setVisiblePermission(new DeleteCustomColumnPermission());
    m_table.addPropertyChangeListener(new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (ITable.PROP_CONTEXT_COLUMN.equals(evt.getPropertyName())
            || ITable.PROP_TABLE_CUSTOMIZER.equals(evt.getPropertyName())) {
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
        if (MessageBox.showDeleteConfirmationMessage(TEXTS.get("Columns"), col.getHeaderCell().getText())) {
          cst.removeColumn((ICustomColumn<?>) col);
        }
      }
    }
  }
}
