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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.dto.ColumnData;
import org.eclipse.scout.rt.client.dto.ColumnData.SdkColumnCommand;
import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowDataMapper;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("a6216b7d-43bc-48f8-8632-dc4fd60b0cef")
@FormData(value = AbstractTableRowData.class, sdkCommand = FormData.SdkCommand.USE, defaultSubtypeSdkCommand = FormData.DefaultSubtypeSdkCommand.CREATE)
public class ContentAssistFieldTable<LOOKUP_KEY> extends AbstractTable implements IContentAssistFieldTable<LOOKUP_KEY> {

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
    ILookupRow<LOOKUP_KEY> lookupRow = getKeyColumn().getValue(row);
    cell.setTooltipText(lookupRow.getTooltipText());
    cell.setBackgroundColor(lookupRow.getBackgroundColor());
    cell.setForegroundColor(lookupRow.getForegroundColor());
    cell.setFont(lookupRow.getFont());
    if (lookupRow.getIconId() != null) {
      cell.setIconId(lookupRow.getIconId());
    }
  }

  @Override
  public void setLookupRows(List<? extends ILookupRow<LOOKUP_KEY>> lookupRows) {
    List<ITableRow> rows = new ArrayList<ITableRow>();
    for (ILookupRow<LOOKUP_KEY> lookupRow : lookupRows) {
      ITableRow row = createRow();
      row.getCellForUpdate(getKeyColumn()).setValue(lookupRow); // FIXME AWE: we should use a ComparableLookupRow here 
      // because restoreSelection does not work as LookupRow does not implement equals/hashCode
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
      replaceRows(rows);
    }
    finally {
      setTableChanging(false);
    }
  }

  @Override
  public List<ILookupRow<LOOKUP_KEY>> getLookupRows() {
    return getKeyColumn().getValues();
  }

  @Override
  public ILookupRow<LOOKUP_KEY> getSelectedLookupRow() {
    return getKeyColumn().getSelectedValue();
  }

  @Override
  public ILookupRow<LOOKUP_KEY> getCheckedLookupRow() {
    return getKeyColumn().getValue(CollectionUtility.firstElement(getCheckedRows()));
  }

  @Override
  public boolean select(ILookupRow<LOOKUP_KEY> lookupRow) {
    LOOKUP_KEY key = null;
    if (lookupRow != null) {
      key = lookupRow.getKey();
    }
    return select(key);
  }

  @Override
  public boolean select(LOOKUP_KEY key) {
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

  @Order(10)
  @ColumnData(SdkColumnCommand.IGNORE)
  @ClassId("0cc6cfc4-dba0-4e00-a4f3-0ea262bca431")
  public class KeyColumn extends AbstractColumn<ILookupRow<LOOKUP_KEY>> {
    @Override
    protected String getConfiguredHeaderText() {
      return TEXTS.get("Key");
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

  @Order(20)
  @ColumnData(SdkColumnCommand.IGNORE)
  @ClassId("2230f7b2-c1d5-4fbb-8664-90a46148502c")
  public class TextColumn extends AbstractStringColumn {

    @Override
    protected String getConfiguredHeaderText() {
      return TEXTS.get("Text");
    }

    @Override
    protected void execDecorateCell(Cell cell, ITableRow row) {
      ILookupRow<LOOKUP_KEY> lookupRow = getKeyColumn().getValue(row);
      cell.setText(lookupRow.getText());
      decorateCellWithLookupRow(cell, row);
    }

  }

}
