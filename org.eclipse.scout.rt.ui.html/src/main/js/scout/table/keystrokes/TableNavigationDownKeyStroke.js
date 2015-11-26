/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TableNavigationDownKeyStroke = function(table) {
  scout.TableNavigationDownKeyStroke.parent.call(this, table);
  this.which = [scout.keys.DOWN];
  this.renderingHints.text = '↓';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var viewport = this._viewportInfo(table);
    return viewport.selection ? viewport.$rowAfterSelection : viewport.$firstRow;
  }.bind(this);
};
scout.inherits(scout.TableNavigationDownKeyStroke, scout.AbstractTableNavigationKeyStroke);

scout.TableNavigationDownKeyStroke.prototype.handle = function(event) {
  var table = this.field,
    $rows = table.$filteredRows(),
    $selection = table.$selectedRows(),
    lastActionRow = table.selectionHandler.lastActionRow,
    deselect = false,
    $newSelection;

  if ($selection.length > 0 || lastActionRow) {
    lastActionRow = lastActionRow || $selection.last().data('row');
    deselect = lastActionRow.$row.isSelected() && lastActionRow.$row.nextAll('.table-row:not(.invisible):first').isSelected();
    $newSelection = deselect ? lastActionRow.$row : lastActionRow.$row.nextAll('.table-row:not(.invisible):first');
    table.selectionHandler.lastActionRow = this._calculateLastActionRowDown(lastActionRow, deselect);
  } else {
    $newSelection = $rows.first();
    table.selectionHandler.lastActionRow = $newSelection.data('row');
  }

  this._applyRowSelection(table, $selection, $newSelection, event.shiftKey, deselect, true);
};
