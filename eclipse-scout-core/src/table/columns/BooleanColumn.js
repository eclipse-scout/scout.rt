/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Column, comparators, scout} from '../../index';

/**
 * May be an ordinary boolean column or the table's checkable column (table.checkableColumn)
 * Difference: the table's checkable column represents the row.checked state, other boolean columns represent their own value.
 */
export default class BooleanColumn extends Column {

  constructor() {
    super();
    this.comparator = comparators.NUMERIC;
    this.filterType = 'ColumnUserFilter';
    this.horizontalAlignment = 0;
    this.minWidth = Column.SMALL_MIN_WIDTH;
    this.triStateEnabled = false;
    this.textBased = false;
  }

  /**
   * @override
   */
  _formatValue(value, row) {
    if (this.triStateEnabled && value === null) {
      return '?';
    }
    return value ? 'X' : '';
  }

  /**
   * @override
   */
  buildCell(cell, row) {
    let style,
      content = '',
      cssClass,
      checkBoxCssClass,
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
    cssClass = this._cellCssClass(cell, tableNodeColumn);
    style = this._cellStyle(cell, tableNodeColumn, rowPadding);
    if (!enabled) {
      cssClass += ' disabled';
    }

    checkBoxCssClass = 'check-box';
    if (cell.value === true) {
      checkBoxCssClass += ' checked';
    }
    if (this.triStateEnabled && cell.value !== true && cell.value !== false) {
      checkBoxCssClass += ' undefined';
    }
    if (!enabled) {
      checkBoxCssClass += ' disabled';
    }

    if (tableNodeColumn && row._expandable) {
      this.tableNodeColumn = true;
      content = this._expandIcon(row.expanded, rowPadding) + content;
      if (row.expanded) {
        cssClass += ' expanded';
      }
    }
    content = content + '<div class="' + checkBoxCssClass + '"></div>';

    return this._buildCell(cell, content, style, cssClass);
  }

  $checkBox($row) {
    let $cell = this.table.$cell(this, $row);
    return $cell.children('.check-box');
  }

  _cellCssClass(cell, tableNode) {
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
  onMouseUp(event, $row) {
    let row = $row.data('row'),
      cell = this.cell(row);
    if (this.table.checkableColumn === this) {
      this.table.checkRow(row, !row.checked);
    } else if (this.isCellEditable(row, cell, event)) {
      this._toggleCellValue(row, cell);
    }
  }

  /**
   * In a remote app this function is overridden by RemoteApp.js, the default implementation is the local case.
   * @see TableAdapter.js
   */
  _toggleCellValue(row, cell) {
    if (!this.triStateEnabled) {
      this.setCellValue(row, !cell.value);
    } else {
      if (cell.value === false) {
        this.setCellValue(row, true);
      } else if (cell.value === true) {
        this.setCellValue(row, null);
      } else if (cell.value === null) {
        this.setCellValue(row, false);
      }
    }
  }

  /**
   * @override
   */
  _createEditor(row) {
    return scout.create('CheckBoxField', {
      parent: this.table,
      triStateEnabled: this.triStateEnabled
    });
  }

  /**
   * @override
   */
  cellTextForGrouping(row) {
    let cell = this.cell(row);
    if (this.triStateEnabled && cell.value === null) {
      return this.session.text('ui.BooleanColumnGroupingMixed');
    } else if (cell.value === true) {
      return this.session.text('ui.BooleanColumnGroupingTrue');
    }
    return this.session.text('ui.BooleanColumnGroupingFalse');
  }

  setTriStateEnabled(triStateEnabled) {
    if (this.triStateEnabled === triStateEnabled) {
      return;
    }
    this.triStateEnabled = triStateEnabled;
    this.table.rows.forEach(row => this._updateCellText(row, this.cell(row)));
  }
}
