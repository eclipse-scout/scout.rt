/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Cell, CheckBoxField, Column, comparators, scout, TableRow} from '../../index';

/**
 * May be an ordinary boolean column or the table's checkable column (table.checkableColumn)
 * Difference: the table's checkable column represents the row.checked state, other boolean columns represent their own value.
 */
export class BooleanColumn extends Column<boolean> {
  triStateEnabled: boolean;

  constructor() {
    super();
    this.comparator = comparators.NUMERIC;
    this.filterType = 'ColumnUserFilter';
    this.horizontalAlignment = 0;
    this.minWidth = Column.SMALL_MIN_WIDTH;
    this.triStateEnabled = false;
    this.textBased = false;
  }

  protected override _formatValue(value: boolean, row?: TableRow): string {
    if (this.triStateEnabled && value === null) {
      return '?';
    }
    return value ? 'X' : '';
  }

  override buildCell(cell: Cell<boolean>, row: TableRow): string {
    let content = '',
      enabled = row.enabled,
      tableNodeColumn = this.table.isTableNodeColumn(this),
      rowPadding = 0;

    if (tableNodeColumn) {
      rowPadding = this.table._calcRowLevelPadding(row);
    }

    if (cell.empty) {
      // if cell wants to be really empty (e.g. no checkbox icon, use logic of base class)
      return super.buildCell(cell, row);
    }

    enabled = enabled && cell.editable;
    let cssClass = this._cellCssClass(cell, tableNodeColumn);
    let style = this._cellStyle(cell, tableNodeColumn, rowPadding);
    if (!enabled) {
      cssClass += ' disabled';
    }

    let checkBoxCssClass = 'check-box';
    if (cell.value === true) {
      checkBoxCssClass += ' checked';
    }
    if (this.triStateEnabled && cell.value !== true && cell.value !== false) {
      checkBoxCssClass += ' undefined';
    }
    if (!enabled) {
      checkBoxCssClass += ' disabled';
    }

    if (tableNodeColumn && row.expandable) {
      this.tableNodeColumn = true;
      content = this._expandIcon(row.expanded, rowPadding) + content;
      if (row.expanded) {
        cssClass += ' expanded';
      }
    }
    content = content + '<div class="' + checkBoxCssClass + '"></div>';

    return this._buildCell(cell, content, style, cssClass);
  }

  $checkBox($row: JQuery): JQuery {
    let $cell = this.table.$cell(this, $row);
    return $cell.children('.check-box');
  }

  protected override _cellCssClass(cell: Cell<boolean>, tableNode?: boolean): string {
    let cssClass = super._cellCssClass(cell);
    cssClass = cssClass.replace(' editable', '');
    cssClass += ' checkable';
    if (tableNode) {
      cssClass += ' table-node';
    }
    return cssClass;
  }

  /**
   * This function does intentionally _not_ call the super function (prepareCellEdit) because we don't want to
   * show an editor for BooleanColumns when user clicks on a cell.
   */
  override onMouseUp(event: JQuery.MouseUpEvent, $row: JQuery) {
    let row = $row.data('row') as TableRow,
      cell = this.cell(row);
    if (this.table.checkableColumn === this) {
      this.table.checkRow(row, !row.checked);
    } else if (this.isCellEditable(row, cell, event)) {
      this._toggleCellValue(row, cell);
    }
  }

  /**
   * In a remote app this function is overridden, the default implementation is the local case.
   * @see TableAdapter
   */
  protected _toggleCellValue(row: TableRow, cell: Cell<boolean>) {
    let value = cell.value as boolean;
    if (!this.triStateEnabled) {
      this.setCellValue(row, !value);
    } else {
      if (value === false) {
        this.setCellValue(row, true);
      } else if (value === true) {
        this.setCellValue(row, null);
      } else if (value === null) {
        this.setCellValue(row, false);
      }
    }
  }

  protected override _createEditor(row: TableRow): CheckBoxField {
    return scout.create(CheckBoxField, {
      parent: this.table,
      triStateEnabled: this.triStateEnabled
    });
  }

  /**
   * @override
   */
  override cellTextForGrouping(row: TableRow): string {
    let cell = this.cell(row);
    if (this.triStateEnabled && cell.value === null) {
      return this.session.text('ui.BooleanColumnGroupingMixed');
    }
    if (cell.value === true) {
      return this.session.text('ui.BooleanColumnGroupingTrue');
    }
    return this.session.text('ui.BooleanColumnGroupingFalse');
  }

  setTriStateEnabled(triStateEnabled: boolean) {
    if (this.triStateEnabled === triStateEnabled) {
      return;
    }
    this.triStateEnabled = triStateEnabled;
    this.table.rows.forEach(row => this._updateCellText(row, this.cell(row)));
  }
}
