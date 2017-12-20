/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TagChooserPopup = function() {
  scout.TagChooserPopup.parent.call(this);

  this.table = null;
};
scout.inherits(scout.TagChooserPopup, scout.Popup);

scout.TagChooserPopup.prototype._init = function(model) {
  scout.TagChooserPopup.parent.prototype._init.call(this, model);

  var column = scout.create('Column', {
    index: 0,
    session: this.session,
    text: 'Tag'
  });

  this.table = scout.create('Table', {
    parent: this,
    headerVisible: false,
    autoResizeColumns: true,
    multiSelect: false,
//    multilineText: false,
    scrollToSelection: true,
    columns: [column],
    headerMenusEnabled: false
  });

  this.table.on('rowClick', this._onRowClick.bind(this));
};

scout.TagChooserPopup.prototype._render = function() {
  scout.TagChooserPopup.parent.prototype._render.call(this);

  this._renderTable();
};

scout.TagChooserPopup.prototype._renderTable = function() {
  this.table.setVirtual(false);
  this.table.render();

  // Make sure table never gets the focus, but looks focused
  this.table.$container
    .setTabbable(false)
    .addClass('focused');
};

scout.TagChooserPopup.prototype.setLookupResult = function(result) {
  var
    tableRows = [],
    lookupRows = result.lookupRows;

  this.table.deleteAllRows();
  lookupRows.forEach(function(lookupRow) {
    tableRows.push(this._createTableRow(lookupRow, false));
  }, this);
  this.table.insertRows(tableRows);
};

scout.TagChooserPopup.prototype._createTableRow = function(lookupRow, multipleColumns) { // FIXME [awe] share code with TableProposalChooser.js
  var
    cell = scout.create('Cell', {
      text: lookupRow.text
    }),
    cells = [cell],
    row = {
      cells: cells,
      lookupRow: lookupRow
    };

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
  if (lookupRow.enabled === false) {
    row.enabled = false;
  }
  if (lookupRow.active === false) {
    row.active = false;
  }
  if (lookupRow.cssClass) {
    row.cssClass = lookupRow.cssClass;
  }

//  if (multipleColumns && lookupRow.additionalTableRowData) {
//    scout.arrays.pushAll(cells, this._transformTableRowData(lookupRow.additionalTableRowData));
//  }

  return row;
};

scout.TagChooserPopup.prototype._onRowClick = function(event) {
  var row = this.table.selectedRow();
  if (!row) {
    return;
  }
  this.trigger('lookupRowSelected', {
    lookupRow: row.lookupRow
  });
};
