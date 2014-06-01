scout.TableKeystrokeAdapter = function(table) {
  this.handlers = [];
  this._table = table;

  var that=this;
  //table filter
  var handler = {
    keycodeRangeStart: 65,
    keycodeRangeEnd: 90,
    handle: function() {
      that._table.$container.find('#HeaderOrganize').click();

      // set focus
      var $input = $('#FilterInput'),
        length = $input.val().length;

      $input.focus();
      $input[0].setSelectionRange(length, length);
    }
  };
  this.handlers.push(handler);

  handler = {
    keycodes: [38, 40, 36, 35, 33, 34],
    handle: function(keycode) {
      var $rowsAll = that._table.findRows(),
        $rowsSelected = that._table.findSelectedRows(),
        $newRowSelection;

      // up: move up
      if (keycode == 38) {
        if ($rowsSelected.length > 0) {
          $newRowSelection = $rowsSelected.first().prev();
        } else {
          $newRowSelection = $rowsAll.last();
        }
      }

      // down: move down
      if (keycode == 40) {
        if ($rowsSelected.length > 0) {
          $newRowSelection = $rowsSelected.last().next();
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
      var $prev;
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

      // TODO cru: handling of shift key
      if ($newRowSelection.length > 0) {
        that._table.selectRowsByIds($newRowSelection.attr('id'));
      }

      //FIXME If selection is not visible we need to scroll
      //that._table.scrollToSelection();

      return false;
    }
  };
  this.handlers.push(handler);
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
