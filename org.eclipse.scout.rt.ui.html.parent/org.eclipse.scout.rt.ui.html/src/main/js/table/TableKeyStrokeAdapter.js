scout.TableKeyStrokeAdapter = function(field) {
  scout.TableKeyStrokeAdapter.parent.call(this, field);
  var that = this;

  this.keyStrokes.push({
    accept: function(event) {
      if (event && ((event.which >= 65 && event.which <= 90) || (event.which >= 48 && event.which <= 57)) && // a-z
        !event.ctrlKey && !event.altKey && !event.metaKey) {
        return true;
      }
      return false;
    },
    handle: function(event) {
      // set focus
      var $input = $('.control-filter', that._field.$container);
      var length = $input.val().length;

      $input.focus();
      $input[0].setSelectionRange(length, length);

      return false;
    },
    bubbleUp: false
  });

  this.keyStrokes.push({
    accept: function(event) {
      if (event && $.inArray(event.which, [scout.keys.UP, scout.keys.DOWN, scout.keys.HOME, scout.keys.END, scout.keys.PAGE_UP, scout.keys.PAGE_DOWN, scout.keys.SPACE]) >= 0 && !event.ctrlKey && !event.altKey && !event.metaKey) {
        return true;
      }
      return false;
    },
    handle: function(event) {
      var $newRowSelection, $prev, $next, i, rowIds;
      var keycode = event.which;
      var $rowsAll = that._field.$rows();
      var $rowsSelected = that._field.$selectedRows();

      if (keycode === scout.keys.SPACE) {
        $newRowSelection = $rowsSelected;
        if ($rowsSelected.length > 0) {
          var check = !$($rowsSelected[0]).data('row').checked;
          for (var j = 0; j < $rowsSelected.length; j++) {
            var row = $($rowsSelected[j]).data('row');
            that._field.checkRowAndRender(row, check);
          }
        }
      }

      // up: move up
      if (keycode === scout.keys.UP) {
        if ($rowsSelected.length > 0) {
          $newRowSelection = that._field.$prevFilteredRows($rowsSelected.first()).first();
        } else {
          $newRowSelection = $rowsAll.last();
        }
      }

      // down: move down
      if (keycode === scout.keys.DOWN) {
        if ($rowsSelected.length > 0) {
          $newRowSelection = that._field.$nextFilteredRows($rowsSelected.last()).first();
        } else {
          $newRowSelection = $rowsAll.first();
        }
      }

      // home: top of table
      if (keycode === scout.keys.HOME) {
        $newRowSelection = $rowsAll.first();
      }

      // end: bottom of table
      if (keycode === scout.keys.END) {
        $newRowSelection = $rowsAll.last();
      }

      // pgup: jump up
      if (keycode === scout.keys.PAGE_UP) {
        if ($rowsSelected.length > 0) {
          $prev = that._field.$prevFilteredRows($rowsSelected.first());
          if ($prev.length > 10) {
            $newRowSelection = $prev.eq(10);
          } else {
            $newRowSelection = $rowsAll.first();
          }
        } else {
          $newRowSelection = $rowsAll.last();
        }
      }

      // pgdn: jump down
      if (keycode === scout.keys.PAGE_DOWN) {
        if ($rowsSelected.length > 0) {
          $next = that._field.$nextFilteredRows($rowsSelected.last());
          if ($next.length > 10) {
            $newRowSelection = $next.eq(10);
          } else {
            $newRowSelection = $rowsAll.last();
          }
        } else {
          $newRowSelection = $rowsAll.first();
        }
      }

      // apply selection
      if ($newRowSelection.length > 0) {
        rowIds = [];
        // FIXME CGU: Handling of shift key not perfect, yet... (must remember first selected row)
        if (event.shiftKey) {
          $newRowSelection = $rowsSelected.add($newRowSelection);
        }
        for (i = 0; $newRowSelection[i] !== undefined; i++) {
          rowIds.push($newRowSelection[i].getAttribute('id'));
        }
        that._field.selectRowsByIds(rowIds);
      }

      // scroll selection into scrollable (if not visible)
      if ($newRowSelection.length > 0) {
        that._field.scrollTo($newRowSelection);
      }

      // preventDefault() is required here, because Chrome would native scroll a scrollable DIV,
      // which would interfere with our custom scroll behavior.
      event.preventDefault();
      return false;
    },
    bubbleUp: false
  });
};
scout.inherits(scout.TableKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.TableKeyStrokeAdapter.prototype.drawKeyBox = function() {
  if (this.keyBoxDrawn) {
    return;
  }
  if (this._field.$rows.length >> 0) {
    var offset = 4;
    var $allRows = this._field.$rows();
    var $firstRow = $allRows.first();
    var $lastRow = $allRows.last();
    scout.KeyStrokeUtil.drawSingleKeyBoxItem(offset, 'Home', $firstRow, false, false, false);

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

    scout.KeyStrokeUtil.drawSingleKeyBoxItem(offset, 'PgUp', $firstRow, false, false, false);

    var $upRow, $downRow;
    if ($allRows.length > $rowsSelected.length) {
      if ($rowsSelected.first()[0] !== $firstRow[0]) {
        //take pageUpOffset when upRow is the same as PgUp otherwise take firstRowOffset if up row is equal first row when not take 4.
        if ($rowsSelected.length > 0) {
          $upRow = this._field.$prevFilteredRows($rowsSelected.first()).first();
        } else {
          $upRow = $allRows.first();
        }
        scout.KeyStrokeUtil.drawSingleKeyBoxItem(offset, '↑', $upRow, false, false, false);
      }
      if ($rowsSelected.last()[0] !== $lastRow[0]) {
        //take upRowOffset when $downRow = $upRow if not take pageUpOffset when upRow is the same as PgUp otherwise take
        //firstRowOffset if up row is equal first row when not take 4.
        if ($rowsSelected.length > 0) {
          $downRow = this._field.$nextFilteredRows($rowsSelected.last()).first();
        } else {
          $downRow = $allRows.first();
        }
        scout.KeyStrokeUtil.drawSingleKeyBoxItem(offset, '↓', $downRow, false, false, false);
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
    scout.KeyStrokeUtil.drawSingleKeyBoxItem(offset, 'PgDn', $pgDownRow, false, false, false);
    scout.KeyStrokeUtil.drawSingleKeyBoxItem(offset, 'End', $lastRow, false, false, false);
  }

  // keys for header

  var $filterinput = $('.control-filter', this._field.$container);
  if ($filterinput.length) {
    var filterInputPosition =  $filterinput.position();
    var top = $filterinput.css('margin-top').replace("px", "");
    var left =  filterInputPosition.left + parseInt($filterinput.css('margin-left').replace("px", ""),0) + 4;
    $filterinput.beforeDiv('key-box char', 'a - z').css('left', left +'px').css('top', top +'px');
  }
  this.keyBoxDrawn = true;
};
