/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Cell, LookupRow, scout, TableRowModel} from '../../../index';

export const lookupField = {
  /**
   * Creates a table-row for the given lookup-row.
   */
  createTableRow(lookupRow: LookupRow<any>, multipleColumns?: boolean): TableRowModel {
    multipleColumns = scout.nvl(multipleColumns, false);
    let cells: Cell[] = [],
      row: TableRowModel = {
        cells: cells,
        lookupRow: lookupRow
      };
    if (lookupRow.enabled === false) {
      row.enabled = false;
    }
    if (lookupRow.cssClass) {
      row.cssClass = lookupRow.cssClass;
    }
    if (lookupRow.active === false) {
      row.cssClass = (row.cssClass ? (row.cssClass + ' ') : '') + 'inactive';
    }

    if (!multipleColumns) {
      cells.push(lookupField.createTableCell(lookupRow, null, null));
    }

    return row;
  },

  /**
   * Creates a table cell for a descriptor. If no descriptor is provided, the default lookupRow cell is created.
   */
  createTableCell<T>(lookupRow: LookupRow<T>, desc?: { propertyName?: string }, tableRowData?: object): Cell<T> {
    let cell = scout.create(Cell);

    // default column descriptor (first column) has propertyName null
    if (!(desc && desc.propertyName)) {
      cell.text = lookupRow.text;
      if (lookupRow.iconId) {
        cell.iconId = lookupRow.iconId;
      }
      if (lookupRow.tooltipText) {
        cell.tooltipText = lookupRow.tooltipText;
      }
      if (lookupRow.backgroundColor) {
        cell.backgroundColor = lookupRow.backgroundColor;
      }
      if (lookupRow.foregroundColor) {
        cell.foregroundColor = lookupRow.foregroundColor;
      }
      if (lookupRow.font) {
        cell.font = lookupRow.font;
      }
    } else {
      cell.text = tableRowData[desc.propertyName];
    }

    return cell;
  }
};
