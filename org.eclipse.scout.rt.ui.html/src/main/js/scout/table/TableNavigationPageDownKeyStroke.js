scout.TableNavigationPageDownKeyStroke = function(table) {
  scout.TableNavigationPageDownKeyStroke.parent.call(this, table);
  this.which = [scout.keys.PAGE_DOWN];
  this.renderingHints.text = 'PgDn';
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var viewport = this._viewportInfo(table);
    return viewport.selection ? viewport.$rowAfterSelection : viewport.$firstRow;
  }.bind(this);
};
scout.inherits(scout.TableNavigationPageDownKeyStroke, scout.AbstractTableNavigationKeyStroke);

scout.TableNavigationPageDownKeyStroke.prototype._acceptForNavigation = function(event) {
  var accepted = scout.TableNavigationPageDownKeyStroke.parent.prototype._acceptForNavigation.call(this, event);
  return accepted && !this._isLastRowSelected();
};

scout.TableNavigationPageDownKeyStroke.prototype.handle = function(event) {
  var table = this.field,
    $rows = table.$filteredRows(),
    $selection = table.$selectedRows(),
    lastActionRow = table.selectionHandler.lastActionRow,
    deselect = false,
    $newSelection;

  if ($selection.length > 0 || lastActionRow) {
    lastActionRow = lastActionRow || $selection.last().data('row');
    var $next = table.$nextFilteredRows(lastActionRow.$row);
    if (event.shiftKey) {
      if ($next.length > 10) {
        var $potentialSelectionDown = $next.slice(0, 10);
        deselect = $potentialSelectionDown.not('.selected').length === 0;
        $newSelection = $next.slice(0, 10);
        if (deselect) {
          $newSelection = $next.slice(0, 9);
          $newSelection = $newSelection.add(lastActionRow.$row);
        } else {
          $newSelection = $potentialSelectionDown;
        }
        table.selectionHandler.lastActionRow = $potentialSelectionDown.last().data('row');
      } else {
        deselect = $next.not('.selected').length === 0;
        if (!deselect) {
          $newSelection = $next;
          table.selectionHandler.lastActionRow = $newSelection.last().data('row');
        }
      }
    } else if ($next.length > 10) {
      $newSelection = $next.eq(10);
      table.selectionHandler.lastActionRow = $newSelection.last().data('row');
    } else {
      $newSelection = $rows.last();
      table.selectionHandler.lastActionRow = $newSelection.last().data('row');
    }
  } else {
    $newSelection = $rows.first();
    table.selectionHandler.lastActionRow = $newSelection.last().data('row');
  }

  this._applyRowSelection(table, $selection, $newSelection, event.shiftKey, deselect, true);
};
