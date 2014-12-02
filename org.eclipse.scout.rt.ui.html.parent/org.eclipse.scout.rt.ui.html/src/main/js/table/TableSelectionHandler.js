/**
 * Enhances the table with selection behaviour.<p>
 *
 * If mouseMoveSelectionEnabled is set to true, the user can select the rows by moving the mouse with pressed left mouse button.
 *
 */
scout.TableSelectionHandler = function(table) {
  this.table = table;
  this.mouseMoveSelectionEnabled = true;

  var that = this;
  this.table.events.on(scout.Table.GUI_EVENT_ROWS_DRAWN, function(event) {
    that._onRowsDrawn(event.$rows);
  });
};

scout.TableSelectionHandler.prototype._onRowsDrawn = function($rows) {
  if (!$rows) {
    return;
  }

  $rows.on('mousedown', '', onMouseDown);

  var that = this;

  // TODO BSH Table Selection | Improve selection handling (up, down)
  // TODO BSH Table Selection | Try to merge this with TableKeystrokeAdapter
  function onMouseDown(event) {
    var $row = $(event.delegateTarget),
      add = true,
      first,
      $selectedRows = that.table.$selectedRows(),
      $allRows = that.table.$rows(),
      selectionChanged = false;

    // click without ctrl always starts new selection, with ctrl toggle
    if (that.table.multiSelect && event.shiftKey) {
      first = $allRows.index($selectedRows.first());
    } else if (that.table.multiSelect && event.ctrlKey) {
      add = !$row.isSelected();
    } else {
      //Click on the already selected row must not clear the selection it to avoid another selection event sent to the server
      //Right click on already selected rows must not clear the selection
      if (!$row.isSelected() ||
        ($selectedRows.length > 1 && event.which !== 3)) {
        $selectedRows.select(false);
        that._clearSelectionBorder($selectedRows);
      }
    }

    // just a click... right click do not select if clicked in selection
    if (event.which !== 3 || !$row.is($selectedRows)) {
      selectData(event);
    }

    if (that.table.multiSelect && that.mouseMoveSelectionEnabled) {
      // ...or movement with held mouse button
      $allRows.one('mousemove.selectionHandler', function(event) {
        selectData(event);
      });
    }
    $allRows.one('mouseup.selectionHandler', function(event) {
      onMouseUp(event);
    });

    // action for all affected rows
    function selectData(event) {
      // affected rows between $row and Target
      var firstIndex = (typeof first !== 'undefined' ? first : $allRows.index($row));
      var lastIndex = $allRows.index($(event.delegateTarget));

      var startIndex = Math.min(firstIndex, lastIndex);
      var endIndex = Math.max(firstIndex, lastIndex) + 1;

      var $actionRow = $allRows.slice(startIndex, endIndex);

      // set/remove selection
      if (add) {
        $actionRow.select(true);
      } else {
        $actionRow.select(false);
        that._clearSelectionBorder($selectedRows);
      }

      $selectedRows = that.table.$selectedRows();
      that._clearSelectionBorder($selectedRows);
      that._drawSelectionBorder($selectedRows);
      that.table.triggerRowsSelected($selectedRows);

      //FIXME currently also set if selection hasn't changed (same row clicked again). maybe optimize
      selectionChanged = true;
    }

    function onMouseUp(event) {
      // TODO BSH Table Selection | This is way too inefficient for many rows!
      $allRows.off('.selectionHandler');

      that.table.onRowsSelected($selectedRows);
    }

  }
};

scout.TableSelectionHandler.prototype.drawSelection = function() {
  this.clearSelection(true);
  var rowIds = this.table.selectedRowIds;
  var selectedRows = [];
  for (var i = 0; i < rowIds.length; i++) {
    var rowId = rowIds[i];
    var $row = $('#' + rowId, this.table.$data);
    $row.select(true);
    selectedRows.push($row);
  }

  var $selectedRows = $(selectedRows);
  this._drawSelectionBorder($selectedRows);

  this.table.onRowsSelected($selectedRows);
};

scout.TableSelectionHandler.prototype.clearSelection = function(dontFire) {
  var $selectedRows = this.table.$selectedRows();
  $selectedRows.select(false);
  this._clearSelectionBorder($selectedRows);

  if (!dontFire) {
    this.table.onRowsSelected();
  }
};

scout.TableSelectionHandler.prototype.dataDrawn = function() {
  var $selectedRows = this.table.$selectedRows();

  this._clearSelectionBorder($selectedRows);
  this._drawSelectionBorder($selectedRows);
  this.table.onRowsSelected($selectedRows);
};

/**
 * Adds the css classes for the selection border based on the selected rows.
 */
scout.TableSelectionHandler.prototype._drawSelectionBorder = function($selectedRows) {
  $selectedRows.each(function() {
    var hasPrev = $(this).prevAll('div:not(.invisible):first').isSelected();
    var hasNext = $(this).nextAll('div:not(.invisible):first').isSelected();

    if (hasPrev && hasNext) {
      $(this).addClass('select-middle');
    }
    if (!hasPrev && hasNext) {
      $(this).addClass('select-top');
    }
    if (hasPrev && !hasNext) {
      $(this).addClass('select-bottom');
    }
    if (!hasPrev && !hasNext) {
      $(this).addClass('select-single');
    }
  });
};

scout.TableSelectionHandler.prototype._clearSelectionBorder = function($selectedRows) {
  $selectedRows.removeClass('select-middle select-top select-bottom select-single');
};

scout.TableSelectionHandler.prototype.selectAll = function() {
  this.clearSelection(true);

  var $rows = this.table.$rows();
  $rows.select(true);

  this._drawSelectionBorder($rows);
  this.table.onRowsSelected($rows);
};

scout.TableSelectionHandler.prototype.toggleSelection = function() {
  if (this.table.selectedRowIds &&
    this.table.selectedRowIds.length === this.table.rows.length) {
    this.clearSelection();
  } else {
    this.selectAll();
  }
};
