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
package org.eclipse.scout.rt.client.ui.form.fields.tablefield;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValidateContentDescriptor;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.platform.status.IStatus;

/**
 * This interface is used to check fields for valid content and - in case invalid - activate / select / focus the
 * appropriate location
 * <p>
 * see {@link IFormField#validateContent()}
 */
public class ValidateTableFieldDescriptor implements IValidateContentDescriptor {
  private final ITableField<?> m_tableField;
  private final ITableRow m_row;
  private final IColumn<?> m_col;
  private String m_displayText;

  public ValidateTableFieldDescriptor(ITableField<?> tableField, ITableRow row, IColumn<?> col) {
    m_tableField = tableField;
    m_row = row;
    m_col = col;
  }

  @Override
  public String getDisplayText() {
    return m_displayText;
  }

  public void setDisplayText(String displayText) {
    m_displayText = displayText;
  }

  @Override
  public IStatus getErrorStatus() {
    return null;
  }

  @Override
  public void activateProblemLocation() {
    //make sure the table is showing (activate parent tabs)
    IGroupBox g = m_tableField.getParentGroupBox();
    while (g != null) {
      if (g.getParentField() instanceof ITabBox) {
        ITabBox t = (ITabBox) g.getParentField();
        if (t.getSelectedTab() != g) {
          t.setSelectedTab(g);
        }
      }
      g = g.getParentGroupBox();
    }
    if (m_row != null && m_col != null) {
      m_tableField.getTable().requestFocusInCell(m_col, m_row);
    }
  }
}
