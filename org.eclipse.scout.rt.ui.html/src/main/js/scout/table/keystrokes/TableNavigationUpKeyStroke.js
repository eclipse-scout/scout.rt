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
scout.TableNavigationUpKeyStroke = function(table) {
  scout.TableNavigationUpKeyStroke.parent.call(this, table);
  this.which = [scout.keys.UP];
  this.renderingHints.text = '↑';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var viewport = this._viewportInfo(table);
    return viewport.selection ? viewport.$rowBeforeSelection : viewport.$firstRow;
  }.bind(this);
};
scout.inherits(scout.TableNavigationUpKeyStroke, scout.AbstractTableNavigationKeyStroke);

scout.TableNavigationUpKeyStroke.prototype.handle = function(event) {
  var table = this.field,
    $rows = table.$filteredRows(),
    $selection = table.$selectedRows(),
    lastActionRow = table.selectionHandler.lastActionRow,
    deselect = false,
    $newSelection;

  if ($selection.length > 0 || lastActionRow) {
    lastActionRow = lastActionRow || $selection.first().data('row');
    deselect = lastActionRow.$row.isSelected() && lastActionRow.$row.prevAll('.table-row:not(.invisible):first').isSelected();
    $newSelection = deselect ? lastActionRow.$row : lastActionRow.$row.prevAll('.table-row:not(.invisible):first');
    table.selectionHandler.lastActionRow = this._calculateLastActionRowUp(lastActionRow, deselect);
  } else {
    $newSelection = $rows.last();
    table.selectionHandler.lastActionRow = $newSelection.data('row');
  }

  this._applyRowSelection(table, $selection, $newSelection, event.shiftKey, deselect, false);
};
