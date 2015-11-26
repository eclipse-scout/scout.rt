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
scout.TableNavigationPageUpKeyStroke = function(table) {
  scout.TableNavigationPageUpKeyStroke.parent.call(this, table);
  this.which = [scout.keys.PAGE_UP];
  this.renderingHints.text = 'PgUp';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var viewport = this._viewportInfo(table);
    return viewport.selection ? viewport.$rowBeforeSelection : viewport.$firstRow;
  }.bind(this);
};
scout.inherits(scout.TableNavigationPageUpKeyStroke, scout.AbstractTableNavigationKeyStroke);

scout.TableNavigationPageUpKeyStroke.prototype.handle = function(event) {
  var table = this.field,
    $rows = table.$filteredRows(),
    $selection = table.$selectedRows(),
    lastActionRow = table.selectionHandler.lastActionRow,
    deselect = false,
    $newSelection;

  if (($selection.length > 0 || lastActionRow)) {
    lastActionRow = lastActionRow || $selection.first().data('row');
    var $prev = table.$prevFilteredRows(lastActionRow.$row);
    if (event.shiftKey) {
      if ($prev.length > 10) {
        var $potentialSelection = $prev.slice(0, 10);
        deselect = $potentialSelection.not('.selected').length === 0;
        if (deselect) {
          $newSelection = $prev.slice(0, 9);
          $newSelection = $newSelection.add(lastActionRow.$row);
          table.selectionHandler.lastActionRow = $newSelection.first().prev() > 0 ? $newSelection.first().prev().data('row') : $newSelection.first().data('row');
        } else {
          $newSelection = $potentialSelection;
          table.selectionHandler.lastActionRow = $potentialSelection.last().data('row');
        }
      } else {
        deselect = $prev.not('.selected').length === 0;
        if (!deselect) {
          $newSelection = $prev;
          table.selectionHandler.lastActionRow = $newSelection.last().data('row');
        }
      }
    } else if ($prev.length > 10) {
      $newSelection = $prev.eq(10);
      table.selectionHandler.lastActionRow = $newSelection.last().data('row');
    } else {
      $newSelection = $rows.first();
      table.selectionHandler.lastActionRow = $newSelection.last().data('row');
    }
  } else {
    $newSelection = $rows.last();
    table.selectionHandler.lastActionRow = $newSelection.last().data('row');
  }

  this._applyRowSelection(table, $selection, $newSelection, event.shiftKey, deselect, false);
};
