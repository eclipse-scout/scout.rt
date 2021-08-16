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
import {arrays, Column, lookupField, objects, ProposalChooser, scout, styles, TileGridLayoutConfig} from '../../../index';

export default class TableProposalChooser extends ProposalChooser {

  constructor() {
    super();
    this.table = null;
    this.smartField = null;
  }

  _init(model) {
    super._init(model);
    this.smartField = this.parent.smartField;
  }

  _createModel() {
    let headerVisible = false,
      columns = [],
      descriptors = this.smartField.columnDescriptors;

    if (descriptors) {
      descriptors.forEach(function(descriptor, index) {
        headerVisible = headerVisible || !!descriptor.text;
        columns.push(this._createColumnForDescriptor(descriptor));
      }, this);
    } else {
      columns.push(this._createColumn());
    }

    this.table = this._createTable(columns, headerVisible);
    this.table.on('rowClick', this._onRowClick.bind(this));

    return this.table;
  }

  _createColumn() {
    return scout.create('Column', {
      session: this.session,
      width: Column.NARROW_MIN_WIDTH,
      horizontalAlignment: this.smartField.gridData.horizontalAlignment
    });
  }

  _createColumnForDescriptor(descriptor) {
    let width = Column.NARROW_MIN_WIDTH;
    if (descriptor.width && descriptor.width > 0) { // 0 = default
      width = descriptor.width;
    }
    return scout.create('Column', {
      session: this.session,
      text: descriptor.text,
      cssClass: scout.nvl(descriptor.cssClass, null),
      width: width, // needs to be passed here to make sure initialWidth is also set, if set using setWidth() autoResizeColumn won't work because initialWidth would still be NARROW_MIN_WIDTH
      autoOptimizeWidth: scout.nvl(descriptor.autoOptimizeWidth, false),
      fixedWidth: scout.nvl(descriptor.fixedWidth, false),
      fixedPosition: scout.nvl(descriptor.fixedPosition, false),
      horizontalAlignment: scout.nvl(descriptor.horizontalAlignment, this.smartField.gridData.horizontalAlignment),
      visible: scout.nvl(descriptor.visible, true),
      htmlEnabled: scout.nvl(descriptor.htmlEnabled, false)
    });
  }

  _createTable(columns, headerVisible) {
    let table = scout.create('Table', {
      parent: this,
      headerVisible: headerVisible,
      autoResizeColumns: true,
      multiSelect: false,
      multilineText: true,
      scrollToSelection: true,
      columns: columns,
      headerMenusEnabled: false,
      cssClass: 'table-proposal-chooser',
      tileMode: this.tileMode,
      createTileForRow: row => {
        let model = {
          parent: this,
          cssClass: 'table-proposal-chooser-tile',
          selectable: false,
          gridDataHints: {
            weightX: -1
          }
        };
        if (!arrays.empty(columns)) {
          let cell = columns[0].cell(row),
            icon = columns[0]._icon(cell.iconId, false) || '',
            text = columns[0]._text(cell) || '',
            contentParts = [icon, text];

          for (let i = 1; i < columns.length; i++) {
            cell = columns[i].cell(row);
            contentParts.push(columns[i]._text(cell) || '');
          }

          let content = contentParts.join('');
          if (!content) {
            content = '&nbsp;';
          }

          model.content = content;
        }
        return scout.create('HtmlTile', model);
      },
      _adaptTile: tile => {
      }
    });
    this._updateTableTileGridLayoutConfig(table);

    return table;
  }

  _updateTableTileGridLayoutConfig(table) {
    if (!table || !table.tileMode || !table.tableTileGridMediator) {
      return;
    }

    let height = styles.getSize([this.smartField.cssClass, 'table-proposal-chooser', 'tile-grid-layout-config'], 'height', 'height', -1),
      width = styles.getSize([this.smartField.cssClass, 'table-proposal-chooser', 'tile-grid-layout-config'], 'width', 'width', -1),
      horizontalGap = styles.getSize([this.smartField.cssClass, 'table-proposal-chooser', 'tile-grid-layout-config'], 'margin-left', 'marginLeft', -1),
      verticalGap = styles.getSize([this.smartField.cssClass, 'table-proposal-chooser', 'tile-grid-layout-config'], 'margin-top', 'marginTop', -1),
      tileGridLayoutConfig = new TileGridLayoutConfig({
        rowHeight: height,
        columnWidth: width,
        hgap: horizontalGap,
        vgap: verticalGap,
        minWidth: 0
      });
    table.tableTileGridMediator.setTileGridLayoutConfig(tileGridLayoutConfig);
  }

  setTileMode(tileMode) {
    super.setTileMode(tileMode);
    this.table.setTileMode(tileMode);
    this._updateTableTileGridLayoutConfig(this.table);
  }

  _onRowClick(event) {
    let row = event.row;
    if (!row || !row.enabled) {
      return;
    }
    this.setBusy(true);
    this.triggerLookupRowSelected(row);
  }

  selectedRow() {
    return this.model.selectedRow();
  }

  setLookupResult(result) {
    let
      tableRows = [],
      lookupRows = result.lookupRows,
      multipleColumns = !!this.smartField.columnDescriptors;

    this.model.deleteAllRows();
    lookupRows.forEach(function(lookupRow) {
      tableRows.push(this._createTableRow(lookupRow, multipleColumns));
    }, this);
    this.model.insertRows(tableRows);

    this._selectProposal(result, tableRows);
  }

  trySelectCurrentValue() {
    let currentValue = this.smartField.getValueForSelection();
    if (objects.isNullOrUndefined(currentValue)) {
      return;
    }
    let tableRow = arrays.find(this.model.rows, row => {
      return row.lookupRow.key === currentValue;
    });
    if (tableRow) {
      this.model.selectRow(tableRow);
    }
  }

  selectFirstLookupRow() {
    if (this.model.rows.length) {
      this.model.selectRow(this.model.rows[0]);
    }
  }

  clearSelection() {
    this.model.deselectAll();
  }

  clearLookupRows() {
    this.model.removeAllRows();
  }

  /**
   * Creates a table-row for the given lookup-row.
   *
   * @returns {object} table-row model
   */
  _createTableRow(lookupRow, multipleColumns) {
    let row = lookupField.createTableRow(lookupRow, multipleColumns);
    if (multipleColumns) {
      arrays.pushAll(row.cells, this._transformTableRowData(lookupRow, lookupRow.additionalTableRowData));
    }
    return row;
  }

  _renderModel() {
    this.model.setVirtual(this.smartField.virtual());
    this.model.render();

    // Make sure table never gets the focus, but looks focused
    this.model.$container.setTabbable(false);
    this.model.$container.addClass('focused');
  }

  getSelectedLookupRow() {
    let selectedRow = this.model.selectedRow();
    if (!selectedRow) {
      return null;
    }
    return selectedRow.lookupRow;
  }

  /**
   * Takes the TableRowData bean and the infos provided by the column descriptors to create an
   * array of additional values in the correct order, as defined by the descriptors.
   */
  _transformTableRowData(lookupRow, tableRowData) {
    let descriptors = this.smartField.columnDescriptors;
    let cells = [];
    descriptors.forEach(desc => {
      cells.push(lookupField.createTableCell(lookupRow, desc, tableRowData));
    });
    return cells;
  }
}
