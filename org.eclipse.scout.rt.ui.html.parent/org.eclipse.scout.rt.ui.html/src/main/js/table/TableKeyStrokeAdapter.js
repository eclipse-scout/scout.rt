scout.TableKeystrokeAdapter = function(field) {
  scout.TableKeystrokeAdapter.parent.call(this, field);
  var that = this;

  this.keyStrokes.push({
    accept: function(event) {
      if (event && event.which >= 65 && event.which <= 90 && // a-z
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
scout.inherits(scout.TableKeystrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.TableKeystrokeAdapter.prototype.drawKeyBox = function() {
  if (this.keyBoxDrawn) {
    return;
  }
  var $tableData = $('.table-data', this._field.$container);
  if ($tableData.length) {
    $tableData.appendDiv('key-box top3', 'Home');
    $tableData.prependDiv('key-box-additional ', ';');
    $tableData.appendDiv('key-box ', 'PgUp').css('left', '' + this._calcKeybox(1) + 'px');
    $tableData.prependDiv('key-box-additional ', ';').css('left', '' + this._calcKeyboxSeparator(1) + 'px');
    $tableData.appendDiv('key-box ', '↑').css('left', '' + this._calcKeybox(2) + 'px');
    $tableData.prependDiv('key-box-additional ', ';').css('left', '' + this._calcKeyboxSeparator(2) + 'px');
    $tableData.appendDiv('key-box ', '↓').css('left', '' + this._calcKeybox(3) + 'px');
    $tableData.prependDiv('key-box-additional ', ';').css('left', '' + this._calcKeyboxSeparator(3) + 'px');
    $tableData.appendDiv('key-box ', 'PgDn').css('left', '' + this._calcKeybox(4) + 'px');
    $tableData.prependDiv('key-box-additional ', ';').css('left', '' + this._calcKeyboxSeparator(4) + 'px');
    $tableData.appendDiv('key-box ', 'End').css('left', '' + this._calcKeybox(5) + 'px');
  }

  // keys for header
  var $tableHeader = $('.table-header', this._field.$container);
  if ($tableHeader.length) {
    $tableHeader.prependDiv('key-box char', 'a - z');
  }
  this.keyBoxDrawn = true;
};
