//FIXME NBU the name is confusing because TableControl is something different. Maybe use TableNavigation? Or directly put it in TableKeyStrokeAdapter
scout.TableControlKeyStrokes = function(field) {
  scout.TableControlKeyStrokes.parent.call(this);
  this.drawHint = true;
  this._field = field;
  this.initKeyStrokeParts();
};
scout.inherits(scout.TableControlKeyStrokes, scout.KeyStroke);
/**
 * @Override scout.KeyStroke
 */
scout.TableControlKeyStrokes.prototype.handle = function(event) {
  var $newRowSelection, $prev, $next, i, rows, directionDown = false;
  var keycode = event.which;
  var $rowsAll = this._field.$filteredRows();
  var $rowsSelected = this._field.$selectedRows();

  if (keycode === scout.keys.SPACE) {
    $newRowSelection = $rowsSelected;
    if ($rowsSelected.length > 0) {
      var check = !$($rowsSelected[0]).data('row').checked;
      for (var j = 0; j < $rowsSelected.length; j++) {
        var row = $($rowsSelected[j]).data('row');
        this._field.checkRow(row, check);
      }
    }
  }

  var lastActionRow = this._field.selectionHandler.lastActionRow,
    deselect = false;

  // up: move up
  if (keycode === scout.keys.UP) {
    if ($rowsSelected.length > 0 || lastActionRow) {
      lastActionRow = lastActionRow || $rowsSelected.first().data('row');
      deselect = lastActionRow.$row.isSelected() && lastActionRow.$row.prev('.table-row:not(.invisible)').isSelected();
      $newRowSelection = deselect ? lastActionRow.$row : lastActionRow.$row.prev('.table-row:not(.invisible)');
      this._field.selectionHandler.lastActionRow = this._calculateLastActionRowUp(lastActionRow, deselect);
    } else {
      $newRowSelection = $rowsAll.last();
      this._field.selectionHandler.lastActionRow = $newRowSelection.data('row');
    }
  }

  // down: move down
  if (keycode === scout.keys.DOWN) {
    if ($rowsSelected.length > 0 || lastActionRow) {
      lastActionRow = lastActionRow || $rowsSelected.last().data('row');
      deselect = lastActionRow.$row.isSelected() && lastActionRow.$row.next('.table-row:not(.invisible)').isSelected();
      $newRowSelection = deselect ? lastActionRow.$row : lastActionRow.$row.next('.table-row:not(.invisible)');
      this._field.selectionHandler.lastActionRow = this._calculateLastActionRowDown(lastActionRow, deselect);
    } else {
      $newRowSelection = $rowsAll.first();
      this._field.selectionHandler.lastActionRow = $newRowSelection.data('row');
    }
    directionDown = true;
  }

  // home: top of table
  if (keycode === scout.keys.HOME) {
    if (event.shiftKey && ($rowsSelected.length > 0 || lastActionRow)) {
      lastActionRow = lastActionRow || $rowsSelected.first().data('row');
      deselect = !lastActionRow.$row.isSelected();
      $newRowSelection = lastActionRow.$row.prevAll('.table-row:not(.invisible)');
    } else {
      $newRowSelection = $rowsAll.first();
    }
    this._field.selectionHandler.lastActionRow = $rowsAll.first().data('row');
  }

  // end: bottom of table
  if (keycode === scout.keys.END) {
    if (event.shiftKey && ($rowsSelected.length > 0 || lastActionRow)) {
      lastActionRow = lastActionRow || $rowsSelected.last().data('row');
      deselect = !lastActionRow.$row.isSelected();
      $newRowSelection = lastActionRow.$row.nextAll('.table-row:not(.invisible)');
      this._field.selectionHandler.lastActionRow = lastActionRow.$row.next('.table-row:not(.invisible)').length > 0 ? lastActionRow.$row.next('.table-row:not(.invisible)').data('row') : lastActionRow;
    } else {
      $newRowSelection = $rowsAll.last();
    }
    this._field.selectionHandler.lastActionRow = $rowsAll.last().data('row');
    directionDown = true;
  }

  // pgup: jump up
  if (keycode === scout.keys.PAGE_UP) {
    if (($rowsSelected.length > 0 || lastActionRow)) {
      lastActionRow = lastActionRow || $rowsSelected.first().data('row');
      $prev = this._field.$prevFilteredRows(lastActionRow.$row);
      if (event.shiftKey) {
        if ($prev.length > 10) {
          var $potentialSelection = $prev.slice(0, 10);
          deselect = $potentialSelection.not('.selected').length === 0;
          if (deselect) {
            $newRowSelection = $prev.slice(0, 9);
            $newRowSelection = $newRowSelection.add(lastActionRow.$row);
            this._field.selectionHandler.lastActionRow = $newRowSelection.first().prev() > 0 ? $newRowSelection.first().prev().data('row') : $newRowSelection.first().data('row');
          } else {
            $newRowSelection = $potentialSelection;
            this._field.selectionHandler.lastActionRow = $potentialSelection.last().data('row');
          }
        } else {
          deselect = $prev.not('.selected').length === 0;
          if (!deselect) {
            $newRowSelection = $prev;
            this._field.selectionHandler.lastActionRow = $newRowSelection.last().data('row');
          }
        }
      } else if ($prev.length > 10) {
        $newRowSelection = $prev.eq(10);
        this._field.selectionHandler.lastActionRow = $newRowSelection.last().data('row');
      } else {
        $newRowSelection = $rowsAll.first();
        this._field.selectionHandler.lastActionRow = $newRowSelection.last().data('row');
      }
    } else {
      $newRowSelection = $rowsAll.last();
      this._field.selectionHandler.lastActionRow = $newRowSelection.last().data('row');
    }
  }

  // pgdn: jump down
  if (keycode === scout.keys.PAGE_DOWN) {
    if ($rowsSelected.length > 0 || lastActionRow) {
      lastActionRow = lastActionRow || $rowsSelected.last().data('row');
      $next = this._field.$nextFilteredRows(lastActionRow.$row);
      if (event.shiftKey) {
        if ($next.length > 10) {
          var $potentialSelectionDown = $next.slice(0, 10);
          deselect = $potentialSelectionDown.not('.selected').length === 0;
          $newRowSelection = $next.slice(0, 10);
          if (deselect) {
            $newRowSelection = $next.slice(0, 9);
            $newRowSelection = $newRowSelection.add(lastActionRow.$row);
          } else {
            $newRowSelection = $potentialSelectionDown;
          }
          this._field.selectionHandler.lastActionRow = $potentialSelectionDown.last().data('row');
        } else {
          deselect = $next.not('.selected').length === 0;
          if (!deselect) {
            $newRowSelection = $next;
            this._field.selectionHandler.lastActionRow = $newRowSelection.last().data('row');
          }
        }
      } else if ($next.length > 10) {
        $newRowSelection = $next.eq(10);
        this._field.selectionHandler.lastActionRow = $newRowSelection.last().data('row');
      } else {
        $newRowSelection = $rowsAll.last();
        this._field.selectionHandler.lastActionRow = $newRowSelection.last().data('row');
      }
    } else {
      $newRowSelection = $rowsAll.first();
      this._field.selectionHandler.lastActionRow = $newRowSelection.last().data('row');
    }
    directionDown = true;
  }

  // apply selection
  if ($newRowSelection && $newRowSelection.length > 0) {
    rows = [];
    if (event.shiftKey) {
      if (deselect) {
        $newRowSelection = $rowsSelected.not($newRowSelection);
      } else {
        $newRowSelection = $rowsSelected.add($newRowSelection);
      }
    }
    for (i = 0; $newRowSelection[i] !== undefined; i++) {
      rows.push($($newRowSelection[i]).data('row'));
    }
    var r = rows[0];
    if (directionDown) {
      r = rows[rows.length - 1];
    }
    this._field.selectRows(rows, true);
    // scroll selection into view (if not visible)
    this._field.scrollTo(r);
  }

  // preventDefault() is required here, because Chrome would native scroll a scrollable DIV,
  // which would interfere with our custom scroll behavior.
  event.preventDefault();
};

scout.TableControlKeyStrokes.prototype._calculateLastActionRowUp = function (lastActionRow, deselect){
  var $prev =  lastActionRow.$row.prev('.table-row:not(.invisible)');
  if($prev.prev().isSelected()&& !deselect){
   return this._calculateLastActionRowUp($prev.data('row'),deselect);
  }
  return $prev.length > 0 ? $prev.data('row') : lastActionRow;
};

scout.TableControlKeyStrokes.prototype._calculateLastActionRowDown = function (lastActionRow, deselect){
  var $next =  lastActionRow.$row.next('.table-row:not(.invisible)');
  if($next.next().isSelected() && !deselect){
   return this._calculateLastActionRowDown($next.data('row'), deselect);
  }
  return $next.length > 0 ? $next.data('row') : lastActionRow;
};

/**
 * @Override scout.KeyStroke
 */
scout.TableControlKeyStrokes.prototype._drawKeyBox = function($container, drawedKeys) {
  if (this._field.$rows.length > 0) {
    var offset = 4;
    var $allRows = this._field.$rows();
    var $firstRow = $allRows.first();
    var $lastRow = $allRows.last();
    if (!scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.HOME)) {
      scout.keyStrokeBox.drawSingleKeyBoxItem(offset, 'Home', $firstRow, false, false, false);
    }
    //TODO nbu refactor
    var $rowsSelected = this._field.$selectedRows();

    var $pageUpRow;
    var $prev;
    if ($rowsSelected.length > 0) {
      $prev = this._field.$prevFilteredRows($rowsSelected.first());
      if ($prev.length > 10) {
        $pageUpRow = $prev.eq(10);
      } else {
        $pageUpRow = $allRows.first();
      }
    } else {
      $pageUpRow = $allRows.last();
    }

    if (scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.PAGE_UP)) {
      scout.keyStrokeBox.drawSingleKeyBoxItem(offset, 'PgUp', $firstRow, false, false, false);
    }

    var $upRow, $downRow;
    if ($allRows.length > $rowsSelected.length) {
      if ($rowsSelected.first()[0] !== $firstRow[0] && !scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.UP)) {
        //take pageUpOffset when upRow is the same as PgUp otherwise take firstRowOffset if up row is equal first row when not take 4.
        if ($rowsSelected.length > 0) {
          $upRow = this._field.$prevFilteredRows($rowsSelected.first()).first();
        } else {
          $upRow = $allRows.first();
        }
        scout.keyStrokeBox.drawSingleKeyBoxItem(offset, '↑', $upRow, false, false, false);
      }
      if ($rowsSelected.last()[0] !== $lastRow[0] && !scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.DOWN)) {
        //take upRowOffset when $downRow = $upRow if not take pageUpOffset when upRow is the same as PgUp otherwise take
        //firstRowOffset if up row is equal first row when not take 4.
        if ($rowsSelected.length > 0) {
          $downRow = this._field.$nextFilteredRows($rowsSelected.last()).first();
        } else {
          $downRow = $allRows.first();
        }
        scout.keyStrokeBox.drawSingleKeyBoxItem(offset, '↓', $downRow, false, false, false);
      }
    }
    // pgdn: jump down
    var $pgDownRow;
    if ($rowsSelected.length > 0) {
      var $next = this._field.$nextFilteredRows($rowsSelected.last());
      if ($next.length > 10) {
        $pgDownRow = $next.eq(10);
      } else {
        $pgDownRow = $allRows.last();
      }
    } else {
      $pgDownRow = $allRows.first();
    }
    if (!scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.PAGE_DOWN)) {
      scout.keyStrokeBox.drawSingleKeyBoxItem(offset, 'PgDn', $pgDownRow, false, false, false);
    }

    if (!scout.keyStrokeBox.keyStrokeAlreadyDrawnAndDraw(drawedKeys, this.ctrl, this.alt, this.shift, scout.keys.END)) {
      scout.keyStrokeBox.drawSingleKeyBoxItem(offset, 'End', $lastRow, false, false, false);
    }
  }

};

/**
 * @Override scout.KeyStroke
 */
scout.TableControlKeyStrokes.prototype.checkAndDrawKeyBox = function($container, drawedKeys) {
  this._drawKeyBox($container, drawedKeys);
};
/**
 * @Override scout.KeyStroke
 */
scout.TableControlKeyStrokes.prototype.accept = function(event) {
  var elementType = document.activeElement.tagName.toLowerCase();

  if (document.activeElement.className !== 'control-filter' &&
    (elementType === 'textarea' || elementType === 'input') &&
    (!event.originalEvent || (event.originalEvent && !event.originalEvent.smartFieldEvent))) {
    return false;
  }

  return event &&
    $.inArray(event.which, [scout.keys.UP, scout.keys.DOWN, scout.keys.HOME, scout.keys.END, scout.keys.PAGE_UP, scout.keys.PAGE_DOWN, scout.keys.SPACE]) >= 0 &&
    event.ctrlKey === this.ctrl &&
    event.altKey === this.alt;
};
