/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.table.fixtures;

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTableRowBuilder;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox.DefaultListBoxTable.ActiveColumn;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox.DefaultListBoxTable.KeyColumn;
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox.DefaultListBoxTable.TextColumn;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;

/**
 * @author nbu
 */
@ClassId("a3d2f35b-50f4-4590-9d2c-96fc16115ec9")
public class ListBoxTable extends AbstractListBox<Long> {

  @Override
  public AbstractTableRowBuilder<Long> getTableRowBuilder() {
    return new P_TableRowBuilder();
  }

  @SuppressWarnings("unchecked")
  protected DefaultListBoxTable.KeyColumn getKeyColumnInternal() {
    return (DefaultListBoxTable.KeyColumn) getTable().getColumnSet().getColumnByClass(KeyColumn.class);
  }

  protected TextColumn getTextColumnInternal() {
    return getTable().getColumnSet().getColumnByClass(TextColumn.class);
  }

  protected ActiveColumn getActiveColumnInternal() {
    return getTable().getColumnSet().getColumnByClass(ActiveColumn.class);
  }

  private class P_TableRowBuilder extends AbstractTableRowBuilder<Long> {

    @Override
    public ITableRow createTableRow(ILookupRow<Long> dataRow) {
      TableRow tableRow = (TableRow) super.createTableRow(dataRow);

      // fill values to tableRow
      getKeyColumnInternal().setValue(tableRow, dataRow.getKey());
      getTextColumnInternal().setValue(tableRow, dataRow.getText());
      getActiveColumnInternal().setValue(tableRow, dataRow.isActive());

      // enable/disabled row
      tableRow.setEnabled(dataRow.isEnabled());

      // hint for inactive
      if (!dataRow.isActive()) {
        tableRow.setCssClass("inactive");
      }
      return tableRow;
    }

    @Override
    protected ITableRow createEmptyTableRow() {
      return new TableRow(getTable().getColumnSet());
    }
  }
}
