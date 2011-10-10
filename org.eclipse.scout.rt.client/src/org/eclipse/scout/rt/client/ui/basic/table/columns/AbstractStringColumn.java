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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;

/**
 * Column holding Strings
 */
public abstract class AbstractStringColumn extends AbstractColumn<String> implements IStringColumn {
  // DO NOT init members, this has the same effect as if they were set AFTER
  // initConfig()
  private boolean m_inputMasked;
  private String m_format;
  private boolean m_wrap;

  public AbstractStringColumn() {
    super();
  }

  /*
   * Configuration
   */

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(130)
  @ConfigPropertyValue("4000")
  protected int getConfiguredMaxLength() {
    return 4000;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(140)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredInputMasked() {
    return false;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(150)
  @ConfigPropertyValue("null")
  protected String getConfiguredDisplayFormat() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(160)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredTextWrap() {
    return false;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setInputMasked(getConfiguredInputMasked());
    setDisplayFormat(getConfiguredDisplayFormat());
    setTextWrap(getConfiguredTextWrap());
  }

  /*
   * Runtime
   */
  @Override
  public void setInputMasked(boolean b) {
    m_inputMasked = b;
  }

  @Override
  public boolean isInputMasked() {
    return m_inputMasked;
  }

  @Override
  public void setDisplayFormat(String s) {
    m_format = s;
  }

  @Override
  public String getDisplayFormat() {
    return m_format;
  }

  @Override
  public void setTextWrap(boolean b) {
    m_wrap = b;
  }

  @Override
  public boolean isTextWrap() {
    return m_wrap;
  }

  @Override
  public boolean isEmpty() {
    ITable table = getTable();
    if (table != null) {
      for (int i = 0, ni = table.getRowCount(); i < ni; i++) {
        String value = getValue(table.getRow(i));
        if (value != null && value.length() > 0) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  protected String parseValueInternal(ITableRow row, Object rawValue) throws ProcessingException {
    String validValue = null;
    if (rawValue == null) {
      validValue = null;
    }
    else if (rawValue instanceof String) {
      validValue = (String) rawValue;
    }
    else {
      validValue = rawValue.toString();
    }
    return validValue;
  }

  @Override
  protected IFormField prepareEditInternal(ITableRow row) throws ProcessingException {
    AbstractStringField f = new AbstractStringField() {
    };
    f.setMaxLength(getConfiguredMaxLength());
    f.setInputMasked(isInputMasked());
    boolean multi = (getTable() != null ? getTable().isMultilineText() : isTextWrap());
    f.setMultilineText(multi);
    f.setWrapText(true); //Avoid to have an horizontal scroll bar
    GridData gd = f.getGridData();
    gd.h = 4;
    f.setGridDataInternal(gd);
    return f;
  }

  @Override
  protected void decorateCellInternal(Cell cell, ITableRow row) {
    super.decorateCellInternal(cell, row);
    if (m_format != null && cell.getValue() != null) {
      if (FORMAT_LOWER.equals(m_format)) {
        cell.setText(((String) cell.getValue()).toLowerCase());
      }
      else if (FORMAT_UPPER.equals(m_format)) {
        cell.setText(((String) cell.getValue()).toUpperCase());
      }
    }
    else {
      cell.setText((String) cell.getValue());
    }
  }

  @Override
  public int compareTableRows(ITableRow r1, ITableRow r2) {
    String s1 = getValue(r1);
    String s2 = getValue(r2);
    return StringUtility.compareIgnoreCase(s1, s2);
  }
}
