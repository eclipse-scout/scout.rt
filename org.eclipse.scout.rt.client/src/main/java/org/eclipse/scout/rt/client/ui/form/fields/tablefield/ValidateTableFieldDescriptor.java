/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.tablefield;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValidateContentDescriptor;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * This interface is used to check fields for valid content and - in case invalid - activate / select / focus the
 * appropriate location
 * <p>
 * see {@link IFormField#validateContent()}
 */
public class ValidateTableFieldDescriptor extends AbstractValidateContentDescriptor {
  private final ITableField<?> m_tableField;
  private ITableRow m_row;
  private IColumn<?> m_col;

  public ValidateTableFieldDescriptor(ITableField<?> tableField) {
    m_tableField = tableField;
  }

  public ITableRow getRow() {
    return m_row;
  }

  public void setRow(ITableRow row) {
    m_row = row;
  }

  public IColumn<?> getColumn() {
    return m_col;
  }

  public void setColumn(IColumn<?> col) {
    m_col = col;
  }

  @Override
  public String getDisplayText() {
    String displayText = super.getDisplayText();
    if (StringUtility.isNullOrEmpty(displayText)) {
      return m_tableField.getFullyQualifiedLabel(LABEL_SEPARATOR); // do not set default in constructor. qualified label may change
    }
    return displayText;
  }

  @Override
  public IStatus getErrorStatus() {
    return m_tableField.getErrorStatus();
  }

  @Override
  protected void activateProblemLocationDefault() {
    // make sure the table is showing (activate parent tabs)
    selectAllParentTabsOf(m_tableField);
    ITableRow row = getRow();
    IColumn<?> col = getColumn();
    if (row != null && col != null) {
      m_tableField.getTable().requestFocusInCell(col, row);
    }
  }
}
