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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.annotations.ColumnData;
import org.eclipse.scout.commons.annotations.ColumnData.SdkColumnCommand;
import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowDataMapper;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

/**
 *
 */
@FormData(value = AbstractTableRowData.class, sdkCommand = FormData.SdkCommand.USE, defaultSubtypeSdkCommand = FormData.DefaultSubtypeSdkCommand.CREATE)
public class ContentAssistFieldTable<KEY> extends AbstractTable implements IContentAssistFieldTable<KEY> {

  @Override
  protected boolean getConfiguredAutoResizeColumns() {
    return true;
  }

  @Override
  protected boolean getConfiguredHeaderVisible() {
    return false;
  }

  @Override
  protected boolean getConfiguredMultiSelect() {
    return false;
  }

  @Override
  protected boolean getConfiguredMultiCheck() {
    return false;
  }

  @Override
  protected boolean getConfiguredScrollToSelection() {
    return true;
  }

  @SuppressWarnings("unchecked")
  public KeyColumn getKeyColumn() {
    return getColumnSet().getColumnByClass(KeyColumn.class);
  }

  @SuppressWarnings("unchecked")
  public TextColumn getTextColumn() {
    return getColumnSet().getColumnByClass(TextColumn.class);
  }

  /**
   * This method might be used to decorate the passed cell with the decoration properties of the lookup row.
   * 
   * @param cell
   * @param row
   */
  protected void decorateCellWithLookupRow(Cell cell, ITableRow row) {
    ILookupRow<KEY> lookupRow = getKeyColumn().getValue(row);
    cell.setTooltipText(lookupRow.getTooltipText());
    cell.setBackgroundColor(lookupRow.getBackgroundColor());
    cell.setForegroundColor(lookupRow.getForegroundColor());
    cell.setFont(lookupRow.getFont());
    if (lookupRow.getIconId() != null) {
      cell.setIconId(lookupRow.getIconId());
    }
  }

  @Override
  public void setLookupRows(List<? extends ILookupRow<KEY>> lookupRows) throws ProcessingException {
    List<ITableRow> rows = new ArrayList<ITableRow>();
    for (ILookupRow<KEY> lookupRow : lookupRows) {
      ITableRow row = createRow();
      row.getCellForUpdate(getKeyColumn()).setValue(lookupRow);
      rows.add(row);
      row.setEnabled(lookupRow.isEnabled());
      AbstractTableRowData tableRowBean = lookupRow.getAdditionalTableRowData();
      if (tableRowBean != null) {
        ITableRowDataMapper mapper = createTableRowDataMapper(tableRowBean.getClass());
        mapper.importTableRowData(row, tableRowBean);
      }
    }
    try {
      setTableChanging(true);
      discardAllRows();
      addRows(rows);
    }
    finally {
      setTableChanging(false);
    }
  }

  @Override
  public List<ILookupRow<KEY>> getLookupRows() {
    return getKeyColumn().getValues();
  }

  @Override
  public ILookupRow<KEY> getSelectedLookupRow() {
    return getKeyColumn().getSelectedValue();
  }

  @Override
  public ILookupRow<KEY> getCheckedLookupRow() {
    return getKeyColumn().getValue(CollectionUtility.firstElement(getCheckedRows()));
  }

  @Override
  public boolean select(ILookupRow<KEY> lookupRow) throws ProcessingException {
    KEY key = null;
    if (lookupRow != null) {
      key = lookupRow.getKey();
    }
    return select(key);
  }

  @Override
  public boolean select(KEY key) throws ProcessingException {
    for (ITableRow row : getRows()) {
      if (CompareUtility.equals(key, getKeyColumn().getValue(row).getKey())) {
        selectRow(row);
        if (isCheckable()) {
          checkRow(row, true);
        }
        return true;
      }
    }
    return false;
  }

  @Order(10.0)
  @ColumnData(SdkColumnCommand.IGNORE)
  public class KeyColumn extends AbstractColumn<ILookupRow<KEY>> {
    @Override
    protected String getConfiguredHeaderText() {
      return TEXTS.get("Key");
    }

    @Override
    protected int getConfiguredWidth() {
      return 100;
    }

    @Override
    protected boolean getConfiguredVisible() {
      return false;
    }

    @Override
    protected boolean getConfiguredDisplayable() {
      return false;
    }

    @Override
    protected boolean getConfiguredPrimaryKey() {
      return true;
    }
  }

  @Order(20.0)
  @ColumnData(SdkColumnCommand.IGNORE)
  public class TextColumn extends AbstractStringColumn {

    @Override
    protected String getConfiguredHeaderText() {
      return TEXTS.get("Text");
    }

    @Override
    protected void execDecorateCell(Cell cell, ITableRow row) {
      ILookupRow<KEY> lookupRow = getKeyColumn().getValue(row);
      cell.setText(lookupRow.getText());
      decorateCellWithLookupRow(cell, row);
    }

    @Override
    protected int getConfiguredWidth() {
      return 200;
    }
  }

}
