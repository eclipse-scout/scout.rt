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
    $rows = table.$filteredRows(),
    $selection = table.$selectedRows(),
    lastActionRow = table.selectionHandler.lastActionRow,
    deselect = false,
    $newSelection;

  if (event.shiftKey && ($selection.length > 0 || lastActionRow)) {
    lastActionRow = lastActionRow || $selection.last().data('row');
    deselect = !lastActionRow.$row.isSelected();
    $newSelection = lastActionRow.$row.nextAll('.table-row:not(.invisible)');
  } else {
    $newSelection = $rows.last();
  }
  table.selectionHandler.lastActionRow = $rows.last().data('row');

  this._applyRowSelection(table, $selection, $newSelection, event.shiftKey, deselect, true);
};
