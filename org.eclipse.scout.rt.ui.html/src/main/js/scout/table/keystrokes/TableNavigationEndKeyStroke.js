/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TableNavigationEndKeyStroke = function(table) {
  scout.TableNavigationEndKeyStroke.parent.call(this, table);
  this.which = [scout.keys.END];
  this.renderingHints.text = 'End';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var viewport = this._viewportInfo(table);
    return viewport.selection ? viewport.$rowAfterSelection : viewport.$firstRow;
  }.bind(this);
};
scout.inherits(scout.TableNavigationEndKeyStroke, scout.AbstractTableNavigationKeyStroke);

scout.TableNavigationEndKeyStroke.prototype._acceptForNavigation = function(event) {
  var accepted = scout.TableNavigationEndKeyStroke.parent.prototype._acceptForNavigation.call(this, event);
  return accepted;
};

scout.TableNavigationEndKeyStroke.prototype.handle = function(event) {
  var table = this.field,
    rows = table.filteredRows(),
    lastRow = scout.arrays.last(rows),
    selectedRows = table.selectedRows,
    newSelectedRows = [],
    lastActionRow = table.selectionHandler.lastActionRow,
    lastActionRowIndex = -1;

  if (event.shiftKey && selectedRows.length > 0) {
    if (lastActionRow) {
      lastActionRowIndex = rows.indexOf(lastActionRow);
    }
    // last action row index maybe < 0 if row got invisible (e.g. due to filtering), or if the user has not made a selection before
    if (lastActionRowIndex < 0) {
      lastActionRow = scout.arrays.last(selectedRows);
      lastActionRowIndex = rows.indexOf(lastActionRow);
    }
    newSelectedRows = rows.slice(lastActionRowIndex + 1, rows.length);

    // add existing selection to new one, avoid duplicate rows
    selectedRows.forEach(function(row) {
      if (newSelectedRows.indexOf(row) < 0) {
        newSelectedRows.push(row);
      }
    });
  } else {
    newSelectedRows = lastRow;
  }
  table.selectionHandler.lastActionRow = lastRow;
  table.selectRows(newSelectedRows);
  table.scrollTo(lastRow);
};
