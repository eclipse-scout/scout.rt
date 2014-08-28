scout.TableKeystrokeAdapter = function(table) {
  var that = this;

  this.$target = undefined; // set by KeystrokeManager
  this.controller = undefined; // set by KeystrokeManager
  this.handlers = [];
  this._table = table;

  //table filter
  this.handlers.push({
    accept: function(event) {
      if (event && event.which >= 65 && event.which <= 90 && // a-z
        !event.ctrlKey && !event.altKey && !event.metaKey) {
        return true;
      }
      return false;
    },
    handle: function(event) {
      // set focus
      var $input = $('.control-filter', that._table.$container);
      var length = $input.val().length;

      $input.focus();
      $input[0].setSelectionRange(length, length);

      return false;
    }
  });

  this.handlers.push({
    accept: function(event) {
      if (event && $.inArray(event.which, [38, 40, 36, 35, 33, 34]) >= 0 && // up, down, home, end, pgup, pgdown
        !event.ctrlKey && !event.altKey && !event.metaKey) {
        return true;
      }
      return false;
    },
    handle: function(event) {
      var $newRowSelection, $prev, i, rowIds;
      var keycode = event.which;
      var $rowsAll = that._table.findRows();
      var $rowsSelected = that._table.findSelectedRows();

      // up: move up
      if (keycode == 38) {
        if ($rowsSelected.length > 0) {
          $newRowSelection = $rowsSelected.first().prev('.table-row');
        } else {
          $newRowSelection = $rowsAll.last();
        }
      }

      // down: move down
      if (keycode == 40) {
        if ($rowsSelected.length > 0) {
          $newRowSelection = $rowsSelected.last().next('.table-row');
        } else {
          $newRowSelection = $rowsAll.first();
        }
      }

      // home: top of table
      if (keycode == 36) {
        $newRowSelection = $rowsAll.first();
      }

      // end: bottom of table
      if (keycode == 35) {
        $newRowSelection = $rowsAll.last();
      }

      // pgup: jump up
      if (keycode == 33) {
        if ($rowsSelected.length > 0) {
          $prev = $rowsSelected.first().prevAll();
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
      if (keycode == 34) {
        if ($rowsSelected.length > 0) {
          $prev = $rowsSelected.last().nextAll();
          if ($prev.length > 10) {
            $newRowSelection = $prev.eq(10);
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
        // FIXME Handling of shift key not perfect, yet... (must remember first selected row)
        if (event.shiftKey) {
          $newRowSelection = $rowsSelected.add($newRowSelection);
        }
        for (i = 0; typeof($newRowSelection[i]) != 'undefined'; i++) {
          rowIds.push($newRowSelection[i].getAttribute('id'));
        }
        that._table.selectRowsByIds(rowIds);
      }

      //FIXME If selection is not visible we need to scroll
      //that._table.scrollToSelection();

      return false;
    }
  });
};

scout.TableKeystrokeAdapter.prototype.drawKeyBox = function() {
  var $tableData = $('.table-data', this._table.$container);
  if ($tableData.length) {
    $tableData.appendDiv('', 'key-box top3', 'Home');
    $tableData.appendDiv('', 'key-box top2', 'PgUp');
    $tableData.appendDiv('', 'key-box top1', '↑');
    $tableData.appendDiv('', 'key-box bottom1', '↓');
    $tableData.appendDiv('', 'key-box bottom2', 'PgDn');
    $tableData.appendDiv('', 'key-box bottom3', 'End');
  }

  // keys for header
  var $tableHeader = $('.table-header', this._table.$container);
  if ($tableHeader.length) {
    $tableHeader.prependDiv('', 'key-box char', 'a - z');
  }
};
