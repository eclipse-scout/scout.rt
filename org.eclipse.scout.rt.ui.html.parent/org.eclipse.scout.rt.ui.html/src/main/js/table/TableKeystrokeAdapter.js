scout.TableKeystrokeAdapter = function(table) {
  var that = this;

  this.$target = undefined; // set by KeystrokeManager
  this.controller = undefined; // set by KeystrokeManager
  this.handlers = [];
  this._table = table;

  function ignoreEvent() {
    // Don't accept if table is detached or when tool-form is opened
    // FIXME CGU/AWE: better remove adapter on detach and reinstall on attach to increase performance?
    // also refactor selectedTool-if below when activeForm is implemented on desktop
    return !that._table.$container.isAttached() ||
            table.session.desktop.selectedTool;
  }

  //table filter
  this.handlers.push({
    accept: function(event) {
      if (ignoreEvent()) {
        return false;
      }
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
      if (ignoreEvent()) {
        return false;
      }
      if (event && $.inArray(event.which,
          [scout.keys.UP, scout.keys.DOWN, scout.keys.HOME, scout.keys.END, scout.keys.PAGE_UP,scout.keys.PAGE_DOWN]) >= 0 &&
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
      if (keycode === scout.keys.UP) {
        if ($rowsSelected.length > 0) {
          $newRowSelection = $rowsSelected.first().prev('.table-row');
        } else {
          $newRowSelection = $rowsAll.last();
        }
      }

      // down: move down
      if (keycode === scout.keys.DOWN) {
        if ($rowsSelected.length > 0) {
          $newRowSelection = $rowsSelected.last().next('.table-row');
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
      if (keycode === scout.keys.PAGE_DOWN) {
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
        // FIXME CGU: Handling of shift key not perfect, yet... (must remember first selected row)
        if (event.shiftKey) {
          $newRowSelection = $rowsSelected.add($newRowSelection);
        }
        for (i = 0; typeof($newRowSelection[i]) != 'undefined'; i++) {
          rowIds.push($newRowSelection[i].getAttribute('id'));
        }
        that._table.selectRowsByIds(rowIds);
      }

      // scroll selection into viewport (if not visible)
      if ($newRowSelection.length > 0) {
        that._table.scrollTo($newRowSelection);
      }

      // preventDefault() is required here, because Chrome would native scroll a scrollable DIV,
      // which would interfere with our custom scroll behavior.
      event.preventDefault();
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
