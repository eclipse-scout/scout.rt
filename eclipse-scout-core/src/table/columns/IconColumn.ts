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
import {Cell, Column, TableRow} from '../../index';

export class IconColumn extends Column<string> {

  constructor() {
    super();
    this.minWidth = Column.NARROW_MIN_WIDTH;
    this.filterType = 'ColumnUserFilter';
    this.textBased = false;
  }

  protected override _initCell(cell: Cell<string>): Cell<string> {
    super._initCell(cell);
    // only display icon, no text
    cell.text = null;
    cell.iconId = cell.value || cell.iconId;
    return cell;
  }

  protected override _formatValue(value: string, row?: TableRow): string {
    // only display icon, no text
    return null;
  }

  override setCellValue(row: TableRow, value: string) {
    super.setCellValue(row, value);
    this.setCellIconId(row, this.cell(row).value);
  }

  override cellTextForGrouping(row: TableRow): string {
    let cell = this.table.cell(this, row);
    return cell.value;
  }

  override createAggrGroupCell(row: TableRow): Cell<string> {
    let cell = super.createAggrGroupCell(row);
    // Make sure only icon and no text is displayed
    cell.text = null;
    return cell;
  }
}
