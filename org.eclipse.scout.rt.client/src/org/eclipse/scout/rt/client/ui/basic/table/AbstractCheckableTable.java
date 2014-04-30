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
package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.action.keystroke.AbstractKeyStroke;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

/**
 * Legacy
 * 
 * @deprecated use {@link ITable} with {@link ITable#setCheckable(boolean)} Will be removed in Release 3.10.
 */
@Deprecated
@SuppressWarnings("deprecation")
public abstract class AbstractCheckableTable extends AbstractTable implements ICheckableTable {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractCheckableTable.class);

  private IBooleanColumn m_checkboxColumn;

  @Override
  protected boolean getConfiguredAutoResizeColumns() {
    return true;
  }

  @Override
  protected boolean getConfiguredMultiSelect() {
    return false;
  }

  @Override
  protected final boolean getConfiguredCheckable() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(200)
  protected boolean getConfiguredMultiCheckable() {
    return true;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setMultiCheckable(getConfiguredMultiCheckable());
    // default behaviour take the first boolean column as checkboxColumn
    for (IColumn<?> c : getColumns()) {
      if (c instanceof IBooleanColumn) {
        setCheckboxColumn((IBooleanColumn) c);
        break;
      }
      LOG.warn("could not find a check box column (column implements IBooleanColumn) in the checkable table.");
    }

  }

  public void setCheckboxColumn(IBooleanColumn checkboxColumn) {
    m_checkboxColumn = checkboxColumn;
  }

  @Override
  public IBooleanColumn getCheckboxColumn() {
    return m_checkboxColumn;
  }

  public boolean isMultiCheckable() {
    return propertySupport.getPropertyBool(PROP_MULTI_CHECKABLE);
  }

  public void setMultiCheckable(boolean on) {
    propertySupport.setPropertyBool(PROP_MULTI_CHECKABLE, on);
  }

  @Override
  protected void execRowClick(ITableRow row) throws ProcessingException {
    if (isEnabled()) {
      if (row != null && getContextColumn() == getCheckboxColumn()) {
        Boolean oldValue = getCheckboxColumn().getValue(row);
        if (getCheckboxColumn() != null) {
          if (oldValue == null) {
            oldValue = false;
          }
          checkRow(row, !oldValue);
        }
      }
    }
  }

  @Order(10)
  public class SpaceKeyStroke extends AbstractKeyStroke {
    @Override
    protected String getConfiguredKeyStroke() {
      return "space";
    }

    @Override
    protected void execAction() throws ProcessingException {
      if (AbstractCheckableTable.this.isEnabled()) {
        for (ITableRow row : getSelectedRows()) {
          Boolean b = getCheckboxColumn().getValue(row);
          if (b == null) {
            b = false;
          }
          b = !b;
          checkRow(row, b);
        }
      }
    }
  }

  @Override
  public void checkRow(ITableRow row, Boolean value) throws ProcessingException {
    if (!row.isEnabled()) {
      return;
    }
    if (!isMultiCheckable() && value && getCheckedRowCount() > 0) {
      uncheckAllRows();
    }
    getCheckboxColumn().setValue(row, value);
  }

  @Override
  public void checkRow(int row, Boolean value) throws ProcessingException {
    checkRow(getRow(row), value);
  }

  @Override
  public Collection<ITableRow> getCheckedRows() {
    List<ITableRow> rows = new ArrayList<ITableRow>();
    boolean checked = false;
    for (int i = 0; i < getRowCount(); i++) {
      checked = (getCheckboxColumn().getValue(i) != null && getCheckboxColumn().getValue(i));
      if (checked) {
        rows.add(getRow(i));
      }
    }

    return rows;
  }

  @Override
  public ITableRow getCheckedRow() {
    return CollectionUtility.firstElement(getCheckedRows());
  }

  public int getCheckedRowCount() {
    return getCheckedRows().size();
  }

  @Override
  public void checkAllRows() {
    try {
      setTableChanging(true);
      for (int i = 0; i < getRowCount(); i++) {
        checkRow(i, Boolean.TRUE);
      }
    }
    catch (ProcessingException e) {
      LOG.warn(null, e);
    }
    finally {
      setTableChanging(false);
    }
  }

  @Override
  public void uncheckAllRows() {
    try {
      setTableChanging(true);
      for (int i = 0; i < getRowCount(); i++) {
        checkRow(i, Boolean.FALSE);
      }
    }
    catch (ProcessingException e) {
      LOG.warn(null, e);
    }
    finally {
      setTableChanging(false);
    }
  }

}
