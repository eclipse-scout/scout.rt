/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {BooleanColumn, CompactBean, CompactLine, objects} from '../index';

export default class TableCompactHandler {

  constructor(table) {
    this.table = null;
    this.useOnlyVisibleColumns = true;
    this.maxContentLines = 3;
    this._oldStates = objects.createMap();
    this._updateHandler = null;
  }

  init(model) {
    $.extend(this, model);
  }

  setUseOnlyVisibleColumns(useOnlyVisibleColumns) {
    this.useOnlyVisibleColumns = useOnlyVisibleColumns;
  }

  setMaxContentLines(maxContentLines) {
    this.maxContentLines = maxContentLines;
  }

  handle(compact) {
    if (compact) {
      this._compactColumns(true);
      this._attachTableHandler();
    } else {
      this._detachTableHandler();
      this._compactColumns(false);
    }
    this._adjustTable(compact);
    if (compact) {
      this.updateValues(this.table.rows);
    }
  }

  _adjustTable(compact) {
    if (compact) {
      this._cacheAndSetProperty('headerVisible', () => this.table.headerVisible, () => this.table.setHeaderVisible(false));
      this._cacheAndSetProperty('autoResizeColumns', () => this.table.autoResizeColumns, () => this.table.setAutoResizeColumns(true));
    } else {
      this._resetProperty('headerVisible', value => this.table.setHeaderVisible(value));
      this._resetProperty('autoResizeColumns', value => this.table.setAutoResizeColumns(value));
    }
  }

  _cacheAndSetProperty(propertyName, getter, setter) {
    if (objects.isNullOrUndefined(this._oldStates[propertyName])) {
      this._oldStates[propertyName] = getter();
    }
    setter();
  }

  _resetProperty(propertyName, setter) {
    if (!objects.isNullOrUndefined(this._oldStates[propertyName])) {
      setter(this._oldStates[propertyName]);
      delete this._oldStates[propertyName];
    }
  }

  _compactColumns(compact) {
    this.table.displayableColumns(false).forEach(column => column.setCompacted(compact, false));
    this.table.onColumnVisibilityChanged();
  }

  _attachTableHandler() {
    if (this._updateHandler == null) {
      this._updateHandler = this._onTableEvent.bind(this);
      this.table.on('rowsInserted rowsUpdated columnStructureChanged', this._updateHandler);
    }
  }

  _detachTableHandler() {
    if (this._updateHandler != null) {
      this.table.off('rowsInserted rowsUpdated columnStructureChanged', this._updateHandler);
      this._updateHandler = null;
    }
  }

  updateValues(rows) {
    if (rows.length === 0) {
      return;
    }
    let columns = this._getColumns();
    rows.forEach(row => this._updateValue(columns, row));
  }

  _updateValue(columns, row) {
    row.setCompactValue(this.buildValue(columns, row));
  }

  buildValue(columns, row) {
    return this._buildValue(this._createBean(columns, row));
  }

  _createBean(columns, row) {
    let bean = new CompactBean();
    this._processColumns(columns, row, bean);
    this._postProcessBean(bean);
    return bean;
  }

  _processColumns(columns, row, bean) {
    columns.forEach((column, i) => this._processColumn(column, i, row, bean));
  }

  _getColumns() {
    return this.table.filterColumns(column => this._acceptColumn(column));
  }

  _acceptColumn(column) {
    return !column.guiOnly && (!this.useOnlyVisibleColumns || (column.visible && column.displayable));
  }

  _processColumn(column, index, row, bean) {
    this._updateBean(bean, column, index, row);
  }

  /**
   * @param {CompactBean} bean
   *          the bean for the current row
   * @param {Column} column
   *          the currently processed column
   * @param index
   *          visible column index of the currently processed column
   * @param {TableRow} row
   *          the current row
   */
  _updateBean(bean, column, index, row) {
    if (this._acceptColumnForTitle(column, index)) {
      bean.setTitleLine(this._createCompactLine(column, index, row));
    } else if (this._acceptColumnForTitleSuffix(column, index)) {
      bean.setTitleSuffixLine(this._createCompactLine(column, index, row));
    } else if (this._acceptColumnForSubtitle(column, index)) {
      bean.setSubtitleLine(this._createCompactLine(column, index, row));
    } else {
      bean.addContentLine(this._createCompactLine(column, index, row));
    }
  }

  _acceptColumnForTitle(column, index) {
    return index === 0;
  }

  _acceptColumnForSubtitle(column, index) {
    return index === 1;
  }

  _acceptColumnForTitleSuffix(column, index) {
    return false;
  }

  _createCompactLine(column, index, row) {
    let headerCell;
    if (this._showLabel(column, index, row)) {
      headerCell = column.headerCell();
    }
    let cell = column.cell(row);
    let line = new CompactLine(headerCell, cell);
    this._adaptCompactLine(line, column, headerCell, cell);
    return line;
  }

  _showLabel(column, index, row) {
    return !this._acceptColumnForTitle(column, index) && !this._acceptColumnForSubtitle(column, index) && !this._acceptColumnForTitleSuffix(column, index);
  }

  _adaptCompactLine(line, column, headerCell, cell) {
    if (column instanceof BooleanColumn) {
      let text = '';
      let value = cell.value;
      if (value) {
        text = 'X';
      } else if (value === null) {
        text = '?';
      }
      line.textBlock.setText(text);
    }
  }

  /**
   * @param {CompactBean} bean
   */
  _postProcessBean(bean) {
    bean.transform({maxContentLines: this.maxContentLines});

    // If only title is set move it to content. A title without content does not look good.
    if (bean.title && !bean.subtitle && !bean.titleSuffix && !bean.content) {
      bean.setContent(bean.title);
      bean.setTitle('');
    }
  }

  _buildValue(bean) {
    let hasHeader = (bean.title + bean.titleSuffix + bean.subTitle) ? ' has-header' : '';
    let moreLink = bean.moreContent ? `<div class="compact-cell-more"><span class="more-link link">${this.table.session.text('More')}</span></div>` : '';

    return `
<div class="compact-cell-header">
  <div class="compact-cell-title">
    <span class="left">${bean.title}</span>
    <span class="right">${bean.titleSuffix}</span>
    </div>
  <div class="compact-cell-subtitle">${bean.subtitle}</div>
</div>
<div class="compact-cell-content${hasHeader}">${bean.content}</div>
<div class="compact-cell-more-content hidden${hasHeader}">${bean.moreContent}</div>
  ${moreLink}`;
  }

  _onTableEvent(event) {
    let rows = event.rows;
    if (event.type === 'columnStructureChanged') {
      rows = this.table.rows;
    }
    this.updateValues(rows);
  }
}
