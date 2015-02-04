// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Table = function() {
  scout.Table.parent.call(this);
  this.$container;
  this.$data;
  this.header;
  this.selectionHandler;
  this.keystrokeAdapter;
  this.columns = [];
  this.tableControls = [];
  this.menus = [];
  this.rows = [];
  this.rowsMap = {}; // rows by id
  this.staticMenus = [];
  this._addAdapterProperties(['tableControls', 'menus']);
  this.events = new scout.EventSupport();
  this.selectionHandler = new scout.TableSelectionHandler(this);
  this._filterMap = {};
  this.selectedRowIds = [];
  this.animationRowLimit = 25;
  this.menuBar;
  this._drawDataInProgress = false;
};
scout.inherits(scout.Table, scout.ModelAdapter);

scout.Table.GUI_EVENT_ROWS_DRAWN = 'rowsDrawn';
scout.Table.GUI_EVENT_ROWS_SELECTED = 'rowsSelected';
scout.Table.GUI_EVENT_ROWS_UPDATED = 'rowsUpdated';
scout.Table.GUI_EVENT_ROWS_FILTERED = 'rowsFiltered';
scout.Table.GUI_EVENT_FILTER_RESETTED = 'filterResetted';

scout.Table.CHECKABLE_COLUMN_SIZE = 40;

scout.Table.prototype.init = function(model, session) {
  scout.Table.parent.prototype.init.call(this, model, session);
  this.keystrokeAdapter = new scout.TableKeystrokeAdapter(this);

  var i;
  for (i = 0; i < this.columns.length; i++) {
    // Unwrap data
    scout.defaultValues.applyTo(this.columns[i], 'TableColumn');
    this.columns[i].index = i;
  }
  for (i = 0; i < this.rows.length; i++) {
    var row = this.rows[i];
    // Unwrap data
    this._unwrapCells(row.cells);
    scout.defaultValues.applyTo(row.cells, 'Cell');
    this.rowsMap[row.id] = row;
  }
};

scout.Table.prototype._render = function($parent) {
  var i, layout;

  this._$parent = $parent;
  this.$container = this._$parent.appendDiv('table');
  layout = new scout.TableLayout(this);
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(layout);
  this.htmlComp.pixelBasedSizing = false;

  if (!scout.keystrokeManager.isAdapterInstalled(this.keystrokeAdapter)) {
    this.$container.attr('tabIndex', 0);
    scout.keystrokeManager.installAdapter(this.$container, this.keystrokeAdapter);
  }

  this.$data = this.$container.appendDiv('table-data');
  scout.scrollbars.install(this.$data);
  this.session.detachHelper.pushScrollable(this.$data);

  this.menuBar = new scout.MenuBar(this.$container, 'top', scout.TableMenuItemsOrder.order);

  this._totalWidth = 0;
  if (this.checkable) {
    //TODO NBU if customised checkable colum is implemented use size of customised checkable colum
    //use size for a checkable column
    this._totalWidth += scout.Table.CHECKABLE_COLUMN_SIZE;
  }
  for (i = 0; i < this.columns.length; i++) {
    this._totalWidth += this.columns[i].width;
  }

  this.drawData();
};

scout.Table.prototype._renderProperties = function() {
  this._renderEnabled(this.enabled);
  this._renderTableHeader();
  this._renderTableFooter();
};

scout.Table.prototype._remove = function() {
  this.session.detachHelper.removeScrollable(this.$data);
  this.menuBar.remove();
  this.header = null;
  this.footer = null;
  scout.Table.parent.prototype._remove.call(this);
};

// FIXME AWE: refactor all _render* methods --> remove parameter, always use this.*
// reason: the property on this is already synced at this point, the argument may contain
// just a data-model value (and not a adpater).
scout.Table.prototype._renderTableControls = function(dummy) {
  this._renderTableFooter();
};

scout.Table.prototype._renderTableStatusVisible = function(dummy) {
  this._renderTableFooter();
};

/**
 * Converts each element of the given cell array that is of type string to an object with
 * a property 'text' with the original value.
 *
 * Example:
 * 'My Company' --> { text: 'MyCompany'; }
 *
 * @see JsonCell.java
 */
scout.Table.prototype._unwrapCells = function(cells) {
  for (var i = 0; i < cells.length; i++) {
    var cell = cells[i];
    if (typeof cell === 'string') {
      cell = {
        text: cell
      };
      cells[i] = cell;
    }
  }
};

scout.Table.prototype._isFooterVisible = function() {
  return this.tableStatusVisible || this.tableControls.length > 0;
};

scout.Table.prototype._createHeader = function() {
  return new scout.TableHeader(this, this.session);
};

scout.Table.prototype._createFooter = function() {
  return new scout.TableFooter(this);
};

scout.Table.prototype.dispose = function() {
  scout.keystrokeManager.uninstallAdapter(this.keystrokeAdapter);
};

scout.Table.prototype.clearSelection = function() {
  this.selectionHandler.clearSelection();
};

scout.Table.prototype.toggleSelection = function() {
  this.selectionHandler.toggleSelection();
};

scout.Table.prototype.updateScrollbar = function() {
  scout.scrollbars.update(this.$data);
};

scout.Table.prototype._sort = function() {
  var column, sortIndex,
    sortColumns = [];

  // find all sort columns
  for (var c = 0; c < this.columns.length; c++) {
    column = this.columns[c];
    sortIndex = column.sortIndex;
    if (sortIndex >= 0) {
      sortColumns[sortIndex] = column;
    }
  }

  // Initialize comparators
  var clientSideSortingPossible = this._prepareColumnsForSorting(sortColumns);
  if (!clientSideSortingPossible) {
    return false;
  }

  // compare rows
  function compare(row1, row2) {
    for (var s = 0; s < sortColumns.length; s++) {
      column = sortColumns[s];
      var valueA = this.getCellValue(column, row1);
      var valueB = this.getCellValue(column, row2);
      var direction = column.sortActive && column.sortAscending ? -1 : 1;

      var result = column.compare(valueA, valueB);
      if (result < 0) {
        return direction;
      } else if (result > 0) {
        return -1 * direction;
      }
    }

    return 0;
  }
  this.rows.sort(compare.bind(this));

  //Sort was possible -> return true
  return true;
};

scout.Table.prototype._prepareColumnsForSorting = function(sortColumns) {
  var collator, column;

  var textComparator = function(valueA, valueB) {
    return collator.compare(valueA, valueB);
  };

  var defaultComparator = function(valueA, valueB) {
    if (valueA < valueB) {
      return -1;
    } else if (valueA > valueB) {
      return 1;
    }
    return 0;
  };

  // initialize comparators
  for (var c = 0; c < sortColumns.length; c++) {
    column = sortColumns[c];
    if (column.type === 'text') {
      if (!scout.device.supportsInternationalization()) {
        //Locale comparison not possible -> do it on server
        return false;
      }

      if (!collator) {
        collator = new window.Intl.Collator(this.session.locale.languageTag);
      }
      column.compare = textComparator;
    } else {
      column.compare = defaultComparator;
    }
  }

  return true;
};

scout.Table.prototype._renderRowOrderChanges = function() {
  var $row, oldTop, i, rowWasInserted, animate, that = this;
  var $rows = this.$rows();
  var $sortedRows = $();

  //store old position
  if ($rows.length < that.animationRowLimit) {
    $rows.each(function() {
      $row = $(this);

      //Prevent the order animation for newly inserted rows (to not confuse the user)
      rowWasInserted = false;
      for (var i in that._insertedRows) {
        if (that._insertedRows[i].id === $row.attr('id')) {
          rowWasInserted = true;
          break;
        }
      }

      if (!rowWasInserted) {
        animate = true;
        $row.data('old-top', $row.offset().top);
      }
    });
  }

  for (i = 0; i < this.rows.length; i++) {
    $row = this.rows[i].$row;
    $sortedRows.push($row[0]);
  }

  // change order in dom
  this.$data.prepend($sortedRows);

  // for less than animationRowLimit rows: move to old position and then animate
  if (animate) {
    $rows.each(function() {
      $row = $(this);
      oldTop = $row.data('old-top');
      if (oldTop !== undefined) {
        $row.css('top', oldTop - $row.offset().top)
          .animateAVCSD('top', 0);
      }
    });
  }
};

/**
 * @param additional true to add the column to list of sorted columns. False to use this column exclusively as sort column (reset other columns)
 */
scout.Table.prototype.sort = function($header, dir, additional, remove) {
  var sortIndex, siblingsResetted,
    maxIndex = -1,
    column = $header.data('column'),
    data = {
      columnId: column.id
    };

  // Update model
  if (remove) {
    data.sortingRemoved = true;
    column.sortActive = false;

    //Adjust sibling columns with higher index
    scout.arrays.eachSibling(this.columns, column, function(siblingColumn) {
      if (siblingColumn.sortIndex > column.sortIndex) {
        siblingColumn.sortIndex = siblingColumn.sortIndex - 1;
      }
    });
    column.sortIndex = undefined;
  } else {
    if (additional) {
      data.multiSort = true;

      // If not already sorted set the appropriate sort index
      if (!column.sortActive) {
        for (var i = 0; i < this.columns.length; i++) {
          sortIndex = this.columns[i].sortIndex;
          if (sortIndex >= 0) {
            maxIndex = Math.max(sortIndex, maxIndex);
          }
        }
        column.sortIndex = maxIndex + 1;
      }
    } else {
      scout.arrays.eachSibling(this.columns, column, function(siblingColumn) {
        if (siblingColumn.sortActive) {
          siblingColumn.sortIndex = undefined;
          siblingColumn.sortActive = false;
          siblingsResetted = true;
        }
      });
      column.sortIndex = 0;
    }
    if (column.sortActive && siblingsResetted) {
      //FIXME CGU this is necessary because the server logic does it (handleSortEvent). In my opinion we have to send sorting details (active, index, asc) instead of just column sorted.
      column.sortAscending = true;
    } else {
      column.sortAscending = dir === 'asc' ? true : false;
    }
    column.sortActive = true;
  }

  this.header.onSortingChanged();

  // sort model
  var sorted = this._sort();
  if (sorted) {
    this.session.send(this.id, 'rowsSorted', data);

    this.clearSelection();
    this._renderRowOrderChanges();
  } else {
    //Delegate sorting to server when it is not possible on client side
    this.session.send(this.id, 'sortRows', data);
  }
};

scout.Table.prototype.drawData = function() {
  this.$rows().remove();
  this._drawData(0);
  this.selectionHandler.dataDrawn();
};

scout.Table.prototype._buildRowDiv = function(row) {
  var column, style, value, tooltipText, tooltip;
  var rowWidth = this._totalWidth + this._tableRowBorderWidth();
  var rowClass = 'table-row ';
  if (this.selectedRowIds && this.selectedRowIds.indexOf(row.id) > -1) {
    rowClass += 'selected ';
  }
  var rowDiv = '<div id="' + row.id + '" class="' + rowClass + '" style="width: ' + rowWidth + 'px"' + scout.device.unselectableAttribute + '>';
  if (this.checkable) {
    rowDiv += '<div class="table-cell checkable-col"  style="min-width:' + scout.Table.CHECKABLE_COLUMN_SIZE +
      'px; max-width:' + scout.Table.CHECKABLE_COLUMN_SIZE + 'px;"' + scout.device.unselectableAttribute +
      '><input type="checkbox" id="' + row.id + '-checkable" ';
    if (row.checked) {
      rowDiv += ' checked="checked" ';
    }
    if (!row.enabled) {
      rowDiv += ' disabled="disabled" ';
    }
    rowDiv += '/><label for="' + row.id + '-checkable">&nbsp;</label></div>';
  }
  for (var c = 0; c < this.columns.length; c++) {
    column = this.columns[c];
    style = this.getCellStyle(column, row);
    value = this.getCellText(column, row);
    tooltipText = this.getCellTooltipText(column, row);
    tooltip = (!scout.strings.hasText(tooltipText) ? '' : ' title="' + tooltipText + '"');

    rowDiv += '<div class="table-cell" data-column-index="' + c + '" style="' + style + '"' + tooltip + scout.device.unselectableAttribute + '>' + value + '</div>';
  }
  rowDiv += '</div>';

  return rowDiv;
};

scout.Table.prototype._tableRowBorderWidth = function() {
  if (typeof this._tableRowBorderWidth !== 'undefined') {
    return this._tableRowBorderWidth;
  }
  var $tableRowDummy = this.$data.appendDiv('table-row');
  this._tableRowBorderWidth = $tableRowDummy.cssBorderLeftWidth() + $tableRowDummy.cssBorderRightWidth();
  $tableRowDummy.remove();
  return this._tableRowBorderWidth;
};

scout.Table.prototype._drawData = function(startRow) {
  // this function has to be fast
  var rowString = '',
    that = this,
    numRowsLoaded = startRow,
    $rows;

  if (this.rows.length > 0) {
    // Build $rows (as string instead of jQuery objects for efficiency reasons)
    for (var r = startRow; r < Math.min(this.rows.length, startRow + 100); r++) {
      var row = this.rows[r];
      rowString += this._buildRowDiv(row, r);
    }
    numRowsLoaded = r;
    $rows = $(rowString);

    // Link model and jQuery objects
    $rows.each(function(index, rowObject) {
      var $row = $(rowObject);
      var row = that.rows[startRow + index];
      scout.Table.linkRowToDiv(row, $row);
    });

    // append block of rows
    $rows.appendTo(this.$data);

    // Add row listeners, inform subscribers and update scrollbar
    this._installRows($rows);
  }

  // repaint and append next block
  this._drawDataInProgress = false;
  if (this.rows.length > numRowsLoaded) {
    this._drawDataInProgress = true;
    setTimeout(function() {
      that._drawData(startRow + 100);
    }, 0);
  }
};

/**
 * Adds row listeners, triggers "rows drawn" event and updates the scrollbar.
 * This method should be used after the $rows are added to the DOM. The '$rows'
 * are expected to be linked with the corresponding 'rows' (row.$row and $row.data('row')).
 */
scout.Table.prototype._installRows = function($rows) {
  var that = this;

  // Attach listeners
  $rows.each(function() {
    var $row = $(this);
    $row.on('mousedown', '', onMouseDown)
      .on('mouseup', '', onMouseUp)
      .on('dblclick', '', onDoubleClick)
      .on('contextmenu', onContextMenu); // mouseup is used instead of click to make sure the event is fired before mouseup in table selection handler
    $('.checkable-col label', $row).on('mouseup', $row, onRowChecked);
  });

  // update info and scrollbar
  this._triggerRowsDrawn($rows);
  this.updateScrollbar();

  // ----- inline methods: --------

  var $mouseDownRow;

  function onMouseDown(event) {
    $mouseDownRow = $(event.delegateTarget);
  }

  function onMouseUp(event) {
    if (event.originalEvent.detail > 1) {
      //don't execute on double click events
      return;
    }

    var $mouseUpRow = $(event.delegateTarget);
    if ($mouseDownRow && $mouseDownRow[0] !== $mouseUpRow[0]) {
      return;
    }

    var $row = $(event.delegateTarget);
    var colId = that._findColumnId(event);
    var hyperLink = that._findHyperLink(event);
    if (hyperLink) {
      that.sendHyperlinkAction($row, colId, hyperLink);
    } else {
      that.sendRowClicked($row, colId);
    }
  }

  function onDoubleClick(event) {
    var $row = $(event.delegateTarget);
    var colId = that._findColumnId(event);
    that.sendRowAction($row, colId);
  }

  function onContextMenu(event) {
    event.preventDefault();

    var $selectedRows = that.$selectedRows(),
      x = event.pageX,
      y = event.pageY;

    if ($selectedRows.length > 0) {
      waitForServer(that.session, showMenuPopup.bind(that));
    }

    /* TODO AWE/CGU: (scout, menu) try to get rid of aboutToShow, than delete this method
     * or move to a better suited location if we cannot remove it. Reason: with the new UI
     * menu-items are added to the menu-bar. There, all items are visible from the start.
     * So there's no point in time where it makes sense to execute the aboutToShow() method
     * which was called when a context menu was about to open. As a replacement for aboutTo
     * Show we could use a listener to enabled/disable menu-items.
     *
     * When aboutToShow is deleted, we can simplify the code here. waitForServer is no longer
     * needed.
     */
    function waitForServer(session, func) {
      if (session.offline) {
        // don't show context menus in offline mode, they won't work
        return;
      }
      if (session.areRequestsPending() || session.areEventsQueued()) {
        session.listen().done(func);
      } else {
        func();
      }
    }

    function showMenuPopup() {
      var menuItems = that._filterMenus($selectedRows);
      if (menuItems.length > 0) {
        var popup = new scout.Popup();
        popup.$origin = this.$data;
        popup.render();
        scout.menus.appendMenuItems(popup, menuItems);
        popup.setLocation(new scout.Point(x, y));
      }
    }
  }

  function onRowChecked(event) {
    var $row = event.data;
    var row = $row.data('row');
    that.checkRow(row, !row.checked);
  }
};

scout.Table.prototype._findColumnId = function(event) {
  //bubble up from target to delegateTarget
  var $elem = $(event.target);
  var $stop = $(event.delegateTarget);
  var colIndex = '';
  while ($elem.length > 0) {
    colIndex = $elem.data('column-index');
    if (colIndex >= 0) {
      return this.columns[colIndex].id;
    }
    if ($elem[0] === $stop[0]) {
      return null;
    }
    $elem = $elem.parent();
  }
  return null;
};

scout.Table.prototype._findHyperLink = function(event) {
  //bubble up from target to delegateTarget
  var $elem = $(event.target);
  var $stop = $(event.delegateTarget);
  var hyperLink;
  while ($elem.length > 0) {
    hyperLink = $elem.data('hyperlink');
    if (hyperLink) {
      return hyperLink;
    }
    if ($elem[0] === $stop[0]) {
      return null;
    }
    $elem = $elem.parent();
  }
  return null;
};

scout.Table.prototype._filterMenus = function($selectedRows, allowedTypes) {
  allowedTypes = allowedTypes || [];
  if ($selectedRows && $selectedRows.length === 1) {
    allowedTypes.push('Table.SingleSelection');
  } else if ($selectedRows && $selectedRows.length > 1) {
    allowedTypes.push('Table.MultiSelection');
  }
  return scout.menus.filter(this.menus, allowedTypes);
};

scout.Table.prototype._renderMenus = function(menus) {
  this._renderRowMenus(this.$selectedRows());
};

scout.Table.prototype._renderRowMenus = function($selectedRows) {
  var menuItems = this._filterMenus($selectedRows, ['Table.EmptySpace', 'Table.Header']);
  menuItems = this.staticMenus.concat(menuItems);
  this.menuBar.updateItems(menuItems);
};

scout.Table.prototype.onRowsSelected = function($selectedRows) {
  var rowIds = [];

  this.triggerRowsSelected($selectedRows);
  this._renderRowMenus($selectedRows);

  if ($selectedRows) {
    $selectedRows.each(function() {
      rowIds.push($(this).attr('id'));
    });
  }

  if (!scout.arrays.equalsIgnoreOrder(rowIds, this.selectedRowIds)) {
    this.selectedRowIds = rowIds;
    if (!this.session.processingEvents) {
      this.session.send(this.id, 'rowsSelected', {
        rowIds: rowIds
      });
    }
  }
};

scout.Table.prototype.onResize = function() {
  if (this.footer) {
    // Delegate window resize events to footer (actually only width changes are relevant)
    this.footer.onResize();
  }
  // Only necessary for outline table. If the table is on a form, the update is triggered by the table layout
  scout.scrollbars.update(this.$data);
};

scout.Table.prototype.sendRowClicked = function($row, columnIdParam) {
  this.session.send(this.id, 'rowClicked', {
    rowId: $row.attr('id'),
    columnId: columnIdParam
  });
};

scout.Table.prototype.sendRowsChecked = function(rows) {
  var data = {
    rows: []
  };

  for (var i = 0; i < rows.length; i++) {
    data.rows.push({
      rowId: rows[i].id,
      checked: rows[i].checked
    });
  }

  this.session.send(this.id, 'rowsChecked', data);
};

scout.Table.prototype.sendRowAction = function($row, columnIdParam) {
  this.session.send(this.id, 'rowAction', {
    rowId: $row.attr('id'),
    columnId: columnIdParam
  });
};

scout.Table.prototype.sendHyperlinkAction = function($row, columnIdParam, hyperlinkParam) {
  this.session.send(this.id, 'hyperlinkAction', {
    rowId: $row.attr('id'),
    columnId: columnIdParam,
    hyperlink: hyperlinkParam
  });
};

scout.Table.prototype.sendReload = function() {
  this.session.send(this.id, 'reload');
};

scout.Table.prototype.getCellValue = function(column, row) {
  var cell = row.cells[column.index];

  if (cell === null) { //cell may be a number so don't use !cell
    return null;
  }
  if (typeof cell !== 'object') {
    return cell;
  }
  if (cell.value !== undefined) {
    return cell.value;
  }
  return cell.text || '';
};

scout.Table.prototype.getCellText = function(column, row) {
  var cell = row.cells[column.index];

  if (!cell) {
    return '';
  }
  if (typeof cell !== 'object') {
    return cell;
  }
  return cell.text || '';
};

scout.Table.prototype.getCellStyle = function(column, row) {
  var style, hAlign,
    cell = row.cells[column.index],
    width = column.width;

  if (width === 0) {
    return 'display: none;';
  }

  hAlign = scout.Table.parseHorizontalAlignment(cell.horizontalAlignment);
  style = 'min-width: ' + width + 'px; max-width: ' + width + 'px; ';
  if (typeof cell === 'object' && cell !== null) {
    style += scout.helpers.legacyCellStyle(cell);
    // TODO BSH Table | iconId, editable, errorStatus
  }
  return style + (hAlign === 'left' ? '' : 'text-align: ' + hAlign + '; ');
};

scout.Table.prototype.getCellTooltipText = function(column, row) {
  var cell = row.cells[column.index];
  if (typeof cell === 'object' && cell !== null && scout.strings.hasText(cell.tooltipText)) {
    return cell.tooltipText;
  }
  return '';
};

scout.Table.prototype._group = function() {
  var that = this,
    all, groupColumn, column, alignment,
    $group = $('.group-sort', this.$container);

  // remove all sum rows
  this.$sumRows().animateAVCSD('height', 0, $.removeThis, that.updateScrollbar.bind(that));

  // find group type
  if ($('.group-all', this.$container).length) {
    all = true;
  } else if ($group.length) {
    groupColumn = $group.data('column');
  } else {
    return;
  }

  // prepare data
  var $rows = $('.table-row:visible', this.$data),
    $sumRow = $.makeDiv('table-row-sum'),
    sum = [];

  for (var r = 0; r < $rows.length; r++) {
    var row = $rows.data('row');
    // calculate sum per column
    for (var c = 0; c < this.columns.length; c++) {
      column = this.columns[c];
      var value = this.getCellValue(column, row);

      if (column.type === 'number') {
        sum[c] = (sum[c] || 0) + value;
      }
    }

    // test if sum should be shown, if yes: reset sum-array
    var nextRow = $rows.data('row');

    if ((r === $rows.length - 1) || (!all && this.getCellText(groupColumn, row) !== this.getCellText(groupColumn, nextRow)) && sum.length > 0) {
      for (c = 0; c < this.columns.length; c++) {
        var $cell;

        column = this.columns[c];
        alignment = scout.Table.parseHorizontalAlignment(column.horizontalAlignment);
        if (typeof sum[c] === 'number') {
          $cell = $.makeDiv('table-cell', sum[c])
            .css('text-align', alignment);
        } else if (!all && column === groupColumn) {
          $cell = $.makeDiv('table-cell', this.getCellText(groupColumn, row))
            .css('text-align', alignment);
        } else {
          $cell = $.makeDiv('table-cell', '&nbsp');
        }

        $cell.appendTo($sumRow)
          .css('min-width', column.width)
          .css('max-width', column.width);
      }

      // TODO BSH Table Sum | There is something wrong here...
      $sumRow.insertAfter($rows.eq(r))
        .width(this._totalWidth + this._tableRowBorderWidth())
        .hide()
        .slideDown();

      $sumRow = $.makeDiv('table-row-sum');
      sum = [];
    }
  }
};

scout.Table.prototype.group = function($header, draw, all) {
  $('.group-sort', this.$container).removeClass('group-sort');
  $('.group-all', this.$container).removeClass('group-all');

  if (draw) {
    if (!all) {
      this.sort($header, 'asc', false);
    }
    this.header.onGroupingChanged($header, all);
  }

  this._group();
};

scout.Table.prototype.colorData = function(mode, colorColumn) {
  var minValue, maxValue, colorFunc, row, value, v, c, $rows;

  for (var r = 0; r < this.rows.length; r++) {
    row = this.rows[r];
    v = this.getCellValue(colorColumn, row);

    if (v < minValue || minValue === undefined) {
      minValue = v;
    }
    if (v > maxValue || maxValue === undefined) {
      maxValue = v;
    }
  }

  // TODO CRU Don't use hardcoded colors (or make them customizable)
  // TODO CRU Handle case where model already has set specific cell background colors
  if (mode === 'red') {
    colorFunc = function(cell, value) {
      var level = (value - minValue) / (maxValue - minValue);

      var r = Math.ceil(255 - level * (255 - 171)),
        g = Math.ceil(175 - level * (175 - 214)),
        b = Math.ceil(175 - level * (175 - 147));

      cell.css('background-color', 'rgb(' + r + ',' + g + ', ' + b + ')');
      cell.css('background-image', '');
    };
  } else if (mode === 'green') {
    colorFunc = function(cell, value) {
      var level = (value - minValue) / (maxValue - minValue);

      var r = Math.ceil(171 - level * (171 - 255)),
        g = Math.ceil(214 - level * (214 - 175)),
        b = Math.ceil(147 - level * (147 - 175));

      cell.css('background-color', 'rgb(' + r + ',' + g + ', ' + b + ')');
      cell.css('background-image', '');
    };
  } else if (mode === 'bar') {
    colorFunc = function(cell, value) {
      var level = Math.ceil((value - minValue) / (maxValue - minValue) * 100) + '';

      cell.css('background-color', 'transparent');
      cell.css('background-image', 'linear-gradient(to left, #80c1d0 0%, #80c1d0 ' + level + '%, transparent ' + level + '%, transparent 100% )');
    };
  } else if (mode === 'remove') {
    colorFunc = function(cell, value) {
      cell.css('background-image', '');
      cell.css('background-color', 'transparent');
    };
  }

  $rows = $('.table-row:visible', this.$data);

  $('.header-item', this.$container).each(function(i) {
    if ($(this).data('column') === colorColumn) {
      c = i;
    }
  });

  for (var s = 0; s < $rows.length; s++) {
    row = $rows.data('row');
    value = this.getCellValue(colorColumn, row);

    colorFunc($rows.eq(s).children().eq(c), value);
  }
};

scout.Table.prototype._onRowsSelected = function(rowIds) {
  this.selectedRowIds = rowIds;

  if (this.rendered) {
    this.selectionHandler.drawSelection();
  }
};

scout.Table.prototype._onRowsChecked = function(rows) {
  for (var i = 0; i < rows.length; i++) {
    this.rowsMap[rows[i].id].checked = rows[i].checked;
    if (this.rendered) {
      this._renderRowChecked(rows[i]);
    }
  }
};

scout.Table.prototype._onRowsUpdated = function(rows) {
  var $updatedRows = $();

  // Update model
  for (var i = 0; i < rows.length; i++) {
    var updatedRow = rows[i];
    // Unwrap data
    this._unwrapCells(updatedRow.cells);
    scout.defaultValues.applyTo(updatedRow.cells, 'Cell');

    // Replace old row
    var oldRow = this.rowsMap[updatedRow.id];
    scout.arrays.replace(this.rows, oldRow, updatedRow);
    this.rowsMap[updatedRow.id] = updatedRow;

    // Replace old $row
    if (this.rendered && oldRow.$row) {
      var $updatedRow = $(this._buildRowDiv(updatedRow));
      scout.Table.linkRowToDiv(updatedRow, $updatedRow);
      // replace div in DOM
      oldRow.$row.replaceWith($updatedRow);
      $updatedRows = $updatedRows.add($updatedRow);
    }
  }

  // Re-attach listeners and inform subscribers
  if ($updatedRows.length > 0) {
    this._installRows($updatedRows);
  }
};

scout.Table.prototype._renderRowChecked = function(row) {
  var $checkbox = $('#' + row.id + '-checkable', this.$data);
  $checkbox.prop('checked', row.checked);
};

scout.Table.prototype.checkRowAndRender = function(row, checked) {
  this.checkRow(row, checked, true);
};

scout.Table.prototype.checkRow = function(row, checked, render) {
  if (!this.checkable || !this.enabled || !row.enabled || row.checked === checked ) {
    return;
  }
  var updatedRows = [];
  if (!this.multiCheck && checked) {
    for (var i = 0; i < this.rows.length; i++) {
      if (this.rows[i].checked) {
        this.rows[i].checked = false;
        updatedRows.push(this.rows[i]);
        this._renderRowChecked(this.rows[i]);
      }
    }
  }
  row.checked = checked;
  updatedRows.push(row);
  this.sendRowsChecked(updatedRows);
  if (render) {
    this._renderRowChecked(row);
  }
};

scout.Table.prototype._onRowsInserted = function(rows) {
  // Update model
  for (var i = 0; i < rows.length; i++) {
    var row = rows[i];
    // Unwrap data
    this._unwrapCells(row.cells);
    scout.defaultValues.applyTo(row.cells, 'Cell');
    // Always insert new rows at the end, if the order is wrong a rowOrderChange event will follow
    this.rows.push(row);
    this.rowsMap[row.id] = row;
  }

  // Update HTML
  if (this.rendered) {
    // Remember inserted rows for future events like rowOrderChanged
    if (!this._insertedRows) {
      this._insertedRows = rows;
      setTimeout(function() {
        this._insertedRows = null;
      }.bind(this), 0);
    } else {
      scout.arrays.pushAll(this._insertedRows, rows);
    }

    this.drawData();
  }
};

scout.Table.prototype._onRowsDeleted = function(rowIds) {
  // Update model
  for (var i = 0; i < rowIds.length; i++) {
    var row = this.rowsMap[rowIds[i]];
    scout.arrays.remove(this.rows, row);
    delete this.rowsMap[rowIds[i]];
    // Update HTML
    if (this.rendered) {
      row.$row.remove();
      delete row.$row;
    }
  }
  // Update HTML
  if (this.rendered) {
    this.updateScrollbar();
  }
};

scout.Table.prototype._onAllRowsDeleted = function() {
  // Update model
  this.rows = [];
  this.rowsMap = {};

  // Update HTML
  if (this.rendered) {
    this.drawData();
  }
};

scout.Table.prototype.scrollTo = function($selection) {
  scout.scrollbars.scrollTo(this.$data, $selection);
};

scout.Table.prototype.rowById = function(id) {
  return this.rowsMap[id];
};

scout.Table.prototype.selectRowsByIds = function(rowIds) {
  if (!scout.arrays.equalsIgnoreOrder(rowIds, this.selectedRowIds)) {
    this.selectedRowIds = rowIds;

    this.session.send(this.id, 'rowsSelected', {
      rowIds: rowIds
    });
  }

  this.selectionHandler.drawSelection();
};

scout.Table.prototype.$selectedRows = function() {
  if (!this.$data) {
    return $();
  }
  return this.$data.find('.selected');
};

scout.Table.prototype.$rows = function(includeSumRows) {
  var selector = '.table-row';
  if (includeSumRows) {
    selector += ', .table-row-sum';
  }
  return this.$data.find(selector);
};

scout.Table.prototype.$sumRows = function() {
  return this.$data.find('.table-row-sum');
};

scout.Table.prototype.$cellsForColIndex = function(colIndex, includeSumRows) {
  var selector = '.table-row > div:nth-of-type(' + colIndex + ' )';
  if (includeSumRows) {
    selector += ', .table-row-sum > div:nth-of-type(' + colIndex + ' )';
  }
  return this.$data.find(selector);
};

scout.Table.prototype.columnById = function(columnId) {
  var column, i;
  for (i = 0; i < this.columns.length; i++) {
    column = this.columns[i];
    if (column.id === columnId) {
      return column;
    }
  }
};

scout.Table.prototype.filter = function() {
  var that = this,
    rowCount = 0,
    $allRows = this.$rows();

  // TODO BSH Table Selection | Selection should be preserved if possible
  that.clearSelection();
  // TODO BSH Table Sum | See also _group(), this does not seem to be too good
  this.$sumRows().hide();

  // Filter rows
  var rowsToHide = [];
  var rowsToShow = [];
  $allRows.each(function() {
    var $row = $(this),
      show = true,
      i;

    for (i = 0; i < that.columns.length; i++) {
      if (that.columns[i].filterFunc) {
        show = show && that.columns[i].filterFunc($row);
      }
    }

    for (var key in that._filterMap) {
      var filter = that._filterMap[key];
      show = show && filter.accept($row);
    }

    if (show) {
      if ($row.hasClass('invisible')) {
        rowsToShow.push($row);
      }
      rowCount++;
    } else {
      if (!$row.hasClass('invisible')) {
        rowsToHide.push($row);
      }
    }
  });

  // Show / hide rows that changed their state during filtering
  var useAnimation = ((rowsToShow.length + rowsToHide.length) <= that.animationRowLimit);
  $(rowsToHide).each(function() {
    that.hideRow($(this), useAnimation);
  });
  $(rowsToShow).each(function() {
    that.showRow($(this), useAnimation);
  });

  //Used by table footer
  this.filteredRowCount = rowCount;

  this._triggerRowsFiltered(rowCount, this.filteredBy());

  $(':animated', that.$data).promise().done(function() {
    that._group();
  });
};

/**
 *
 * @returns array of filter names which are currently active
 */
scout.Table.prototype.filteredBy = function() {
  var filteredBy = [];
  for (var i = 0; i < this.columns.length; i++) {
    if (this.columns[i].filterFunc) {
      filteredBy.push(this.columns[i].$div.text());
    }
  }
  for (var key in this._filterMap) {
    var filter = this._filterMap[key];
    filteredBy.push(filter.label);
  }
  return filteredBy;
};

scout.Table.prototype.resetFilter = function() {
  this.clearSelection();

  // reset rows
  var that = this;
  var $rows = this.$rows();
  $rows.each(function() {
    that.showRow($(this), ($rows.length <= that.animationRowLimit));
  });
  this._group();

  // set back all filter functions
  for (var i = 0; i < this.columns.length; i++) {
    this.columns[i].filter = [];
    this.columns[i].filterFunc = null;
  }
  this._filterMap = {};
  this.filteredRowCount = undefined;
  this._triggerFilterResetted();
};

/**
 * @param filter object with name and accept()
 */
scout.Table.prototype.registerFilter = function(key, filter) {
  if (!key) {
    throw new Error('key has to be defined');
  }
  this._filterMap[key] = filter;
};

scout.Table.prototype.getFilter = function(key, filter) {
  if (!key) {
    throw new Error('key has to be defined');
  }
  return this._filterMap[key];
};

scout.Table.prototype.unregisterFilter = function(key) {
  if (!key) {
    throw new Error('key has to be defined');
  }
  delete this._filterMap[key];
};

scout.Table.prototype.showRow = function($row, useAnimation) {
  var that = this;
  if (!$row.hasClass('invisible')) {
    return;
  }

  if (useAnimation) {
    $row.stop().slideDown({
      duration: 250,
      complete: function() {
        $row.removeClass('invisible');
        that.updateScrollbar();
      }
    });
  } else {
    $row.show();
    $row.removeClass('invisible');
    that.updateScrollbar();
  }
};

scout.Table.prototype.hideRow = function($row, useAnimation) {
  var that = this;
  if ($row.hasClass('invisible')) {
    return;
  }

  if (useAnimation) {
    $row.stop().slideUp({
      duration: 250,
      complete: function() {
        $row.addClass('invisible');
        that.updateScrollbar();
      }
    });
  } else {
    $row.hide();
    $row.addClass('invisible');
    that.updateScrollbar();
  }
};

/**
 * @param resizingInProgress set this to true when calling this function several times in a row. If resizing is finished you have to call resizingColumnFinished.
 */
scout.Table.prototype.resizeColumn = function($header, width, totalWidth, resizingInProgress) {
  var colNum = this.header.getColumnViewIndex($header) + 1;
  var column = $header.data('column');

  column.width = width;
  this._totalWidth = totalWidth;

  this.$cellsForColIndex(colNum, true)
    .css('min-width', width)
    .css('max-width', width);
  this.$rows(true)
    .css('width', totalWidth);

  this.header.onColumnResized($header, width);

  if (!resizingInProgress) {
    this.resizingColumnFinished($header, width);
  }
};

scout.Table.prototype.resizingColumnFinished = function($header, width) {
  var column = $header.data('column');
  var data = {
    columnId: column.id,
    width: width
  };
  this.session.send(this.id, 'columnResized', data);
};

scout.Table.prototype.moveColumn = function($header, oldPos, newPos, dragged) {
  var column = $header.data('column'),
    uiOnlyColumnOffset = 0;

  if (this.checkable && newPos === 0) {
    uiOnlyColumnOffset = 1;
  }

  scout.arrays.remove(this.columns, column);
  scout.arrays.insert(this.columns, column, newPos + uiOnlyColumnOffset);

  var data = {
    columnId: column.id,
    index: newPos
  };
  this.session.send(this.id, 'columnMoved', data);

  this.header.onColumnMoved($header, oldPos, newPos + uiOnlyColumnOffset, dragged);

  // move cells
  this.$rows(true).each(function() {
    var $cells = $(this).children();
    if (newPos + uiOnlyColumnOffset < oldPos) {
      $cells.eq(newPos + uiOnlyColumnOffset).before($cells.eq(oldPos));
    } else {
      $cells.eq(newPos + uiOnlyColumnOffset).after($cells.eq(oldPos));
    }
  });
};

scout.Table.prototype._renderColumnOrderChanges = function(oldColumnOrder) {
  var column, i, j, $orderedCells, $cell, $cells, that = this,
    $row;

  if (this.header) {
    this.header.onOrderChanged(oldColumnOrder);
  }

  // move cells
  this.$rows(true).each(function() {
    $row = $(this);
    $orderedCells = $();
    $cells = $row.children();
    for (i = 0; i < that.columns.length; i++) {
      column = that.columns[i];

      //Find $cell for given column
      for (j = 0; j < oldColumnOrder.length; j++) {
        if (oldColumnOrder[j] === column) {
          $cell = $cells[j];
          break;
        }
      }
      $orderedCells.push($cell);
    }
    $row.prepend($orderedCells);
  });
};

scout.Table.prototype._triggerRowsDrawn = function($rows) {
  var type = scout.Table.GUI_EVENT_ROWS_DRAWN;
  var event = {
    $rows: $rows
  };
  this.events.trigger(type, event);
};

scout.Table.prototype.triggerRowsSelected = function($rows) {
  var rowCount = this.rows.length,
    allSelected = false;

  if ($rows) {
    allSelected = $rows.length === rowCount;
  }

  var type = scout.Table.GUI_EVENT_ROWS_SELECTED;
  var event = {
    $rows: $rows,
    allSelected: allSelected
  };
  this.events.trigger(type, event);
};

scout.Table.prototype._triggerRowsFiltered = function(numRows, filterName) {
  var type = scout.Table.GUI_EVENT_ROWS_FILTERED;
  var event = {
    numRows: numRows,
    filterName: filterName
  };
  this.events.trigger(type, event);
};

scout.Table.prototype._triggerFilterResetted = function() {
  var type = scout.Table.GUI_EVENT_FILTER_RESETTED;
  this.events.trigger(type);
};

scout.Table.prototype._renderHeaderVisible = function() {
  this._renderTableHeader();
};

scout.Table.prototype._renderTableHeader = function() {
  if (this.headerVisible && !this.header) {
    this.header = this._createHeader();
  } else if (!this.headerVisible && this.header) {
    this.header.remove();
    this.header = null;
  }
  if (this.rendered) {
    this.htmlComp.revalidate();
  }
};

scout.Table.prototype._renderTableFooter = function() {
  var footerVisible = this._isFooterVisible();
  if (footerVisible) {
    if (!this.footer) {
      this.footer = this._createFooter();
    } else {
      this.footer.update();
    }
  } else if (!footerVisible && this.footer) {
    this.footer.remove();
    this.footer = null;
  }
  if (this.rendered) {
    this.htmlComp.revalidate();
  }
};

scout.Table.prototype._renderEnabled = function(enabled) {
  // FIXME CGU remove/add events. Maybe extend jquery to not fire on disabled events?
  this.$data.setEnabled(enabled);
};

scout.Table.prototype._renderMultiSelect = function(multiSelect) {
  // nop
};

scout.Table.prototype._onRowOrderChanged = function(rowIds) {
  var newPos, rows, row;
  if (rowIds.length !== this.rows.length) {
    throw new Error('Row order changed event may not be processed because lengths of the arrays differ.');
  }

  // update model
  rows = scout.arrays.init(this.rows.length, 0);
  for (var i = 0; i < this.rows.length; i++) {
    row = this.rows[i];
    newPos = rowIds.indexOf(this.rows[i].id);
    rows[newPos] = row;
  }
  this.rows = rows;

  if (this.rendered) {
    this._renderRowOrderChanges();
  }
};

scout.Table.prototype._onColumnStructureChanged = function(columns) {
  //Index is not sent -> update received columns with the current indices
  for (var i = 0; i < columns.length; i++) {
    // Unwrap data
    scout.defaultValues.applyTo(columns[i], 'TableColumn');

    for (var j = 0; j < this.columns.length; j++) {
      if (columns[i].id === this.columns[j].id) {
        columns[i].index = this.columns[j].index;
        break;
      }
    }
  }
  this.columns = columns;

  if (this.rendered) {
    if (this.header) {
      this.header.remove();
      this.header = this._createHeader();
    }
    this.drawData();
  }
};

scout.Table.prototype._onColumnOrderChanged = function(columnIds) {
  var i, column, columnId, currentPosition, oldColumnOrder;
  if (columnIds.length !== this.columns.length) {
    throw new Error('Column order changed event may not be processed because lengths of the arrays differ.');
  }

  oldColumnOrder = this.columns.slice();

  for (i = 0; i < columnIds.length; i++) {
    columnId = columnIds[i];
    column = this.columnById(columnId);
    currentPosition = this.columns.indexOf(column);
    if (currentPosition < 0) {
      throw new Error('Column with id ' + columnId + 'not found.');
    }

    if (currentPosition !== i) {
      // Update model
      scout.arrays.remove(this.columns, column);
      scout.arrays.insert(this.columns, column, i);
    }
  }

  if (this.rendered) {
    this._renderColumnOrderChanges(oldColumnOrder);
  }
};

/**
 * @param columns array of columns which were updated.
 */
scout.Table.prototype._onColumnHeadersUpdated = function(columns) {
  var column,
    updatedColumns = [];

  //Update model columns
  for (var i = 0; i < columns.length; i++) {
    column = this.columnById(columns[i].id);
    column.text = columns[i].text;
    column.sortActive = columns[i].sortActive;
    column.sortAscending = columns[i].sortAscending;

    updatedColumns.push(column);
  }

  if (this.rendered && this.header) {
    this.header.updateHeaders(updatedColumns);
  }
};

scout.Table.prototype.onModelAction = function(event) {
  // _drawData() might not have drawn all rows yet, therefore postpone the
  // execution of this method to prevent conflicts on the row objects.
  if (this._drawDataInProgress) {
    var that = this;
    setTimeout(function() {
      that.onModelAction(event);
    }, 0);
    return;
  }

  if (event.type === 'rowsInserted') {
    this._onRowsInserted(event.rows);
  } else if (event.type === 'rowsDeleted') {
    this._onRowsDeleted(event.rowIds);
  } else if (event.type === 'allRowsDeleted') {
    this._onAllRowsDeleted();
  } else if (event.type === 'rowsSelected') {
    this._onRowsSelected(event.rowIds);
  } else if (event.type === 'rowOrderChanged') {
    this._onRowOrderChanged(event.rowIds);
  } else if (event.type === 'rowsUpdated') {
    this._onRowsUpdated(event.rows);
  } else if (event.type === 'rowsChecked') {
    this._onRowsChecked(event.rows);
  } else if (event.type === 'columnStructureChanged') {
    this._onColumnStructureChanged(event.columns);
  } else if (event.type === 'columnOrderChanged') {
    this._onColumnOrderChanged(event.columnIds);
  } else if (event.type === 'columnHeadersUpdated') {
    this._onColumnHeadersUpdated(event.columns);
  } else {
    $.log.warn('Model event not handled. Widget: scout.Table. Event: ' + event.type + '.');
  }
};

/* --- STATIC HELPERS ------------------------------------------------------------- */

/**
 * @memberOf scout.Table
 */
scout.Table.parseHorizontalAlignment = function(alignment) {
  if (alignment > 0) {
    return 'right';
  }
  if (alignment === 0) {
    return 'center';
  }
  return 'left';
};

scout.Table.linkRowToDiv = function(row, $row) {
  if (row) {
    row.$row = $row;
  }
  if ($row) {
    $row.data('row', row);
  }
};
