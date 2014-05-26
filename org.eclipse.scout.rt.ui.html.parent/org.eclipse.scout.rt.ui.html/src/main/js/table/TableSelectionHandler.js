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
      $selectedRows = $('.row-selected'),
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

    // just a click...
    selectData(event);

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

      // draw nice border
      that.drawSelection();

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
  // remove nice border
  $('.select-middle, .select-top, .select-bottom, .select-single')
    .removeClass('select-middle select-top select-bottom select-single');

  // draw nice border
  var $selectedRows = $('.row-selected');
  $selectedRows.each(function() {
    var hasPrev = $(this).prevAll(':visible:first').hasClass('row-selected'),
      hasNext = $(this).nextAll(':visible:first').hasClass('row-selected');

    if (hasPrev && hasNext) $(this).addClass('select-middle');
    if (!hasPrev && hasNext) $(this).addClass('select-top');
    if (hasPrev && !hasNext) $(this).addClass('select-bottom');
    if (!hasPrev && !hasNext) $(this).addClass('select-single');
  });

  // show count
  var rowCount = 0;
  if (this.table.model.rows) {
    rowCount = this.table.model.rows.length;
  }
  this.table.triggerRowsSelected($selectedRows, $selectedRows.length == rowCount);
};

scout.TableSelectionHandler.prototype.resetSelection = function() {
  $('.row-selected', this.table.$data).removeClass('row-selected');
  this.drawSelection();
};

scout.TableSelectionHandler.prototype.selectRowsByIds = function(rowIds) {
  if (this.table.$dataScroll) {
    this.resetSelection();

    for (var i = 0; i < rowIds.length; i++) {
      var rowId = rowIds[i];
      var $row = $('#' + rowId);
      $row.addClass('row-selected');
    }

    this.drawSelection();
  }

  //FIXME row menu is not shown when using this method

  if (!this.table.updateFromModelInProgress) {
    //not necessary for now since selectRowsByIds is only called by onModelAction, but does no harm either
    this.table.session.send(scout.Table.EVENT_ROWS_SELECTED, this.table.model.id, {
      "rowIds": rowIds
    });
  }
};

scout.TableSelectionHandler.prototype.toggleSelection = function() {
  var $selectedRows = $('.row-selected', this.table.$data);

  if ($selectedRows.length == this.table.model.rows.length) {
    $selectedRows.removeClass('row-selected');
  } else {
    $('.table-row', this.table.$data).addClass('row-selected');
  }

  this.drawSelection();
};
