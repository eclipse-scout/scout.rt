scout.TableNavigationHomeKeyStroke = function(table) {
  scout.TableNavigationHomeKeyStroke.parent.call(this, table);
  this.which = [scout.keys.HOME];
  this.renderingHints.text = 'Home';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var viewport = this._viewportInfo(table);
    return viewport.selection ? viewport.$rowBeforeSelection : viewport.$firstRow;
  }.bind(this);
};
scout.inherits(scout.TableNavigationHomeKeyStroke, scout.AbstractTableNavigationKeyStroke);

scout.TableNavigationHomeKeyStroke.prototype._acceptForNavigation = function(event) {
  var accepted = scout.TableNavigationHomeKeyStroke.parent.prototype._acceptForNavigation.call(this, event);
  return accepted;
};

scout.TableNavigationHomeKeyStroke.prototype.handle = function(event) {
  var table = this.field,
    $rows = table.$filteredRows(),
    $selection = table.$selectedRows(),
    lastActionRow = table.selectionHandler.lastActionRow,
    deselect = false,
    $newSelection;

  if (event.shiftKey && ($selection.length > 0 || lastActionRow)) {
    lastActionRow = lastActionRow || $selection.first().data('row');
    deselect = !lastActionRow.$row.isSelected();
    $newSelection = lastActionRow.$row.prevAll('.table-row:not(.invisible)');
  } else {
    $newSelection = $rows.first();
  }
  table.selectionHandler.lastActionRow = $rows.first().data('row');

  this._applyRowSelection(table, $selection, $newSelection, event.shiftKey, deselect, false);
};
