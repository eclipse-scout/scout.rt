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

  function onMouseDown(event) {
    var $row = $(event.delegateTarget),
      add = true,
      first,
      $selectedRows = that.table.findSelectedRows(),
      selectionChanged = false;

    // click without ctrl always starts new selection, with ctrl toggle
    if (event.shiftKey) {
      first = $selectedRows.first().index();
    } else if (event.ctrlKey) {
      add = !$row.hasClass('row-selected'); //FIXME why not just selected as in tree?
    } else {
      //Click on the already selected row must not reselect it
      if ($selectedRows.length == 1 && $row.hasClass('row-selected')) {
        return;
      } else {
        $selectedRows.removeClass('row-selected');
      }
    }

    // just a click... right click do not select if clicked in selection
    if (event.which != 3 || !$row.is($selectedRows)) {
      selectData(event);
    }

    if (that.mouseMoveSelectionEnabled) {
      // ...or movement with held mouse button
      $(".table-row").one("mousemove.selectionHandler", function(event) {
        selectData(event);
      });
    }
    $(".table-row").one("mouseup.selectionHandler", function(event) {
      onMouseUp(event);
    });

    // action for all affected rows
    function selectData(event) {
      // affected rows between $row and Target
      var firstIndex = first || $row.index(),
        lastIndex = $(event.delegateTarget).index();

      var startIndex = Math.min(firstIndex, lastIndex),
        endIndex = Math.max(firstIndex, lastIndex) + 1;

      var $actionRow = $('.table-row', that.table.$data).slice(startIndex, endIndex);

      // set/remove selection
      if (add) {
        $actionRow.addClass('row-selected');
      } else {
        $actionRow.removeClass('row-selected');
      }

      $selectedRows = that.table.findSelectedRows();
      that._clearSelectionBorder();
      that._drawSelectionBorder($selectedRows);
      that.table.triggerRowsSelected($selectedRows);

      //FIXME currently also set if selection hasn't changed (same row clicked again). maybe optimize
      selectionChanged = true;
    }

    function onMouseUp(event) {
      $(".table-row").off(".selectionHandler");

      //Handle mouse move selection. Single row selections are handled by onClicks
      if ($row.get(0) != event.delegateTarget) {
        that.table.sendRowsSelected();
      }
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
    $row.addClass('row-selected');
    selectedRows.push($row);
  }

  var $selectedRows = $(selectedRows);
  this._drawSelectionBorder($selectedRows);

  this.table.triggerRowsSelected($selectedRows);
};


scout.TableSelectionHandler.prototype.clearSelection = function(dontFire) {
  this.table.findSelectedRows().removeClass('row-selected');
  this._clearSelectionBorder();

  if (!dontFire) {
    this.table.triggerRowsSelected();
    this.table.sendRowsSelected();
  }
};

scout.TableSelectionHandler.prototype.dataDrawn = function() {
  var $selectedRows = this.table.findSelectedRows();

  this._clearSelectionBorder();
  this._drawSelectionBorder($selectedRows);
  this.table.triggerRowsSelected($selectedRows);
};

/**
 * Adds the css classes for the selection border based on the selected rows.
 */
scout.TableSelectionHandler.prototype._drawSelectionBorder = function($selectedRows) {
  $selectedRows.each(function() {
    var hasPrev = $(this).prevAll(':visible:first').hasClass('row-selected'),
      hasNext = $(this).nextAll(':visible:first').hasClass('row-selected');

    if (hasPrev && hasNext) $(this).addClass('select-middle');
    if (!hasPrev && hasNext) $(this).addClass('select-top');
    if (hasPrev && !hasNext) $(this).addClass('select-bottom');
    if (!hasPrev && !hasNext) $(this).addClass('select-single');
  });
};

scout.TableSelectionHandler.prototype._clearSelectionBorder = function() {
  $('.select-middle, .select-top, .select-bottom, .select-single')
  .removeClass('select-middle select-top select-bottom select-single');
};

scout.TableSelectionHandler.prototype.selectAll = function() {
  this.clearSelection(true);

  var $rows = $('.table-row', this.table.$data);
  $rows.addClass('row-selected');

  this._drawSelectionBorder($rows);

  this.table.triggerRowsSelected($rows);
  this.table.sendRowsSelected();
};

scout.TableSelectionHandler.prototype.toggleSelection = function() {
  if (this.table.selectedRowIds &&
      this.table.selectedRowIds.length === this.table.rows.length) {
    this.clearSelection();
  } else {
    this.selectAll();
  }
};
