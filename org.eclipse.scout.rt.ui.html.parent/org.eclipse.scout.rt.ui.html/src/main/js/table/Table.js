// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Table = function() {
  scout.Table.parent.call(this);
  this.$container;
  this.$data;
  this._$viewport;
  this._header;
  this.selectionHandler;
  this._keystrokeAdapter;
  this.controls = [];
  this.menus = [];
  this.staticMenus = [];
  this.rows = [];
  this._addAdapterProperties(['controls', 'menus']);
  this.events = new scout.EventSupport();
  this.selectionHandler = new scout.TableSelectionHandler(this);
  this._filterMap = {};
};
scout.inherits(scout.Table, scout.ModelAdapter);

scout.Table.GUI_EVENT_ROWS_DRAWN = 'rowsDrawn';
scout.Table.GUI_EVENT_ROWS_SELECTED = 'rowsSelected';
scout.Table.GUI_EVENT_ROWS_FILTERED = 'rowsFiltered';
scout.Table.GUI_EVENT_FILTER_RESETTED = 'filterResetted';

scout.Table.prototype.init = function(model, session) {
  scout.Table.parent.prototype.init.call(this, model, session);
  this._keystrokeAdapter = new scout.TableKeystrokeAdapter(this);
  for (var i = 0; i < this.columns.length; i++) {
    this.columns[i].index = i;
  }
};

scout.Table.prototype._render = function($parent) {
  this._$parent = $parent;
  this.$container = this._$parent.appendDIV('table');

  var layout = new scout.TableLayout(this);
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(layout);
  this.htmlComp.pixelBasedSizing = false;

  if ($parent.hasClass('desktop-bench')) {
    // TODO cru: desktop table (no input focus required to trigger table keystrokes), is body ok?
    // input A.WE eine komponente sollte nie etwas von seinen parents wissen,
    //   das reduziert die wiederverwendbarkeit der komponente --> refactor: bench oder form
    //   _setzen_ einen keystroke adapter auf der Table.
    scout.keystrokeManager.installAdapter($('body'), this._keystrokeAdapter);
  } else {
    // independent table, i.e. inside form (input focus required to trigger table keystrokes)
    this.$container.attr('tabIndex', 0);
    scout.keystrokeManager.installAdapter(this.$container, this._keystrokeAdapter);
  }

  this.menubar = new scout.Menubar(this.$container);
  this.menubar.menuTypesForLeft1 = ['Table.EmptySpace'];
  this.menubar.menuTypesForLeft2 = ['Table.SingleSelection', 'Table.MultiSelection'];
  this.menubar.menuTypesForRight = ['Table.Header'];
  this.menubar.staticMenus = this.staticMenus;

  this._$header = this.$container.appendDIV('table-header');
  this._header = new scout.TableHeader(this, this._$header, this.session);
  this.$data = this.$container.appendDIV('table-data');
  this._$viewport = scout.Scrollbar2.install(this.$data);

  if (this._isFooterVisible()) {
    this.footer = this._createFooter();
  }

  // load data and create rows
  this.drawData();
};

scout.Table.prototype._renderProperties = function() {
  this._renderHeaderVisible(this.headerVisible);
  this._renderEnabled(this.enabled);
};

//scout.Table.prototype._renderDataHeight = function() {
//  var height = 0;
//  if (this.menubar.$container.isVisible()){
//    height += this.menubar.$container.outerHeight(true);
//  }
//  if (this.footer) {
//    height += this.footer.$container.outerHeight(true);
//  }
//  if (this._$header.isVisible()) {
//    height += this._$header.outerHeight(true);
//  }
//  this.$data.css('height', 'calc(100% - '+ height + 'px)');
//};

scout.Table.prototype._isFooterVisible = function() {
  return this.tableStatusVisible || this.controls.length > 0;
};

scout.Table.prototype._createFooter = function() {
  return new scout.TableFooter(this, this.$container, this.session);
};

scout.Table.prototype.dispose = function() {
  scout.keystrokeManager.uninstallAdapter(this._keystrokeAdapter);
};

scout.Table.prototype.clearSelection = function() {
  this.selectionHandler.clearSelection();
};

scout.Table.prototype.toggleSelection = function() {
  this.selectionHandler.toggleSelection();
};

scout.Table.prototype.updateScrollbar = function() {
  scout.Scrollbar2.update(this._$viewport);
};

scout.Table.prototype._sort = function() {
  var sortColumns = [], column, sortIndex;

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
  var $rows = this.findRows();
  var $sortedRows = $();
  var animationRowLimit = 50;

  //store old position
  if ($rows.length < animationRowLimit) {
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
    $row = this.findRowById(this.rows[i].id);
    $sortedRows.push($row[0]);
  }

  // change order in dom
  this._$viewport.prepend($sortedRows);

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
  var maxIndex = -1, sortIndex, siblingsResetted;
  var column = $header.data('column');
  var data = {
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
        for (var i=0; i < this.columns.length; i++) {
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

  this._header.onSortingChanged();

  // sort model
  var sorted = this._sort();
  if (sorted) {
    this.session.send('rowsSorted', this.id, data);

    this.clearSelection();
    this._renderRowOrderChanges();
  } else {
    //Delegate sorting to server when it is not possible on client side
    this.session.send('sortRows', this.id, data);
  }
};

scout.Table.prototype.drawData = function() {
  this.findRows().remove();
  this._drawData(0);
  this.selectionHandler.dataDrawn();
};

scout.Table.prototype._buildRowDiv = function(row) {
  var column, width, style, align, value, alignment;
  var rowWidth = this._header.totalWidth + this._getTableRowBorderWidth();
  var rowClass = 'table-row ';

  if (this.selectedRowIds && this.selectedRowIds.indexOf(row.id) > -1) {
    rowClass += 'row-selected ';
  }
  // FIXME Check if possible to use $.makeDiv (but maybe it's too slow)
  var unselectable = (scout.device.supportsCssUserSelect() ? '' : ' unselectable="on"'); // workaround for IE 9

  var rowDiv = '<div id="' + row.id + '" class="' + rowClass + '" style="width: ' + rowWidth + 'px"' + unselectable + '>';
  for (var c = 0; c < this.columns.length; c++) {
    column = this.columns[c];
    width = column.width;
    style = (width === 0) ? 'display: none; ' : 'min-width: ' + width + 'px; max-width: ' + width + 'px; ';
    alignment = scout.Table.parseHorizontalAlignment(column.horizontalAlignment);
    align = alignment !== 'left' ? 'text-align: ' + alignment + '; ' : '';
    value = this.getCellText(column, row);

    rowDiv += '<div class="table-cell" style="' + style + align + '"' + unselectable + '>' + value + '</div>';
  }
  rowDiv += '</div>';

  return rowDiv;
};

scout.Table.prototype._getTableRowBorderWidth = function() {
  if (this._tablRowBorderWidth !== undefined) {
    return this._tablRowBorderWidth;
  }

  var $tableRowDummy = this._$viewport.appendDIV('table-row');
  this._tablRowBorderWidth = $tableRowDummy.cssBorderLeftWidth() + $tableRowDummy.cssBorderRightWidth();
  $tableRowDummy.remove();
  return this._tablRowBorderWidth;
};

scout.Table.prototype._drawData = function(startRow) {
  // this function has to be fast
  var rowString = '',
    that = this,
    numRowsLoaded = startRow,
    $rows,
    $mouseDownRow;

  if (this.rows.length > 0) {
    for (var r = startRow; r < Math.min(this.rows.length, startRow + 100); r++) {
      var row = this.rows[r];
      rowString += this._buildRowDiv(row, r);
    }
    numRowsLoaded = r;

    // append block of rows
    $rows = $(rowString);
    $rows.appendTo(this._$viewport)
      .on('mousedown', '', onMouseDown)
      .on('mouseup', '', onMouseUp)
      .on('dblclick', '', onDoubleClick)
      .on('contextmenu', onContextMenu); //mouseup is used instead of click to make sure the event is fired before mouseup in table selection handler
  }

  // update info and scrollbar
  this._triggerRowsDrawn($rows, numRowsLoaded);
  this.updateScrollbar();

  // repaint and append next block
  if (this.rows.length > 0) {
    if (numRowsLoaded < this.rows.length) {
      setTimeout(function() {
        that._drawData(startRow + 100);
      }, 0);
    }
  }

  function onContextMenu(event) {
    var $selectedRows, x, y;
    event.preventDefault();

    $selectedRows = that.findSelectedRows();
    x = event.pageX - that._$viewport.offset().left;
    y = event.pageY - that._$viewport.offset().top;

    if ($selectedRows.length > 0) {
      scout.menus.showContextMenuWithWait(that.session, showContextMenu);
    }

    function showContextMenu() {
      scout.menus.showContextMenu(that._getRowMenus($selectedRows, false), that._$viewport, $(that), x, undefined, y);
    }
  }

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
    //Send click only if mouseDown and mouseUp happened on the same row
    that.session.send('rowClicked', that.id, {
      'rowId': $row.attr('id')
    });
  }

  function onDoubleClick(event) {
    var $row = $(event.delegateTarget);
    that.sendRowAction($row);
  }

};

scout.Table.prototype._getRowMenus = function($selectedRows, all) {
  var menus, check;

  if (all) {
    check = ['Table.EmptySpace', 'Table.Header'];
  } else {
    check = [];
  }

  if ($selectedRows && $selectedRows.length == 1) {
    check.push('Table.SingleSelection');
  } else if ($selectedRows && $selectedRows.length > 1) {
    check.push('Table.MultiSelection');
  }

  return scout.menus.filter(this.menus, check);
};

scout.Table.prototype._renderMenus = function(menus) {
  var $selectedRows = this.findSelectedRows();
  this._renderRowMenus($selectedRows);
};

scout.Table.prototype._renderRowMenus = function($selectedRows) {
  var menus = this._getRowMenus($selectedRows, true);
  this.menubar.updateItems(menus);
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
      this.session.send('rowsSelected', this.id, {
        'rowIds': rowIds
      });
    }
  }
};

scout.Table.prototype.onResize = function() {
  if (this.footer) {
    this.footer.onResize();
  }
};

scout.Table.prototype.sendRowAction = function($row) {
  this.session.send('rowAction', this.id, {
    'rowId': $row.attr('id')
  });
};

scout.Table.prototype.sendReload = function() {
  this.session.send('reload', this.id);
};

scout.Table.prototype.getCellValue = function(col, row) {
  var cell = row.cells[col.index];

  if (cell === null) { //cell may be a number so don't use !cell
    return null;
  }
  if (typeof cell !== 'object') {
    return cell;
  }
  if (cell.value !== undefined) {
    return cell.value;
  }
  return cell.text;
};

scout.Table.prototype.getCellText = function(col, row) {
  var cell = row.cells[col.index];

  if (cell === null) { //cell may be a number so don't use !cell
    return '';
  }
  if (typeof cell !== 'object') {
    return cell;
  }
  return cell.text;
};

scout.Table.prototype._group = function() {
  var that = this,
    all, groupColumn, column, alignment,
    $group = $('.group-sort', this.$container);

  // remove all sum rows
  this.findSumRows().animateAVCSD('height', 0, $.removeThis, that.updateScrollbar.bind(that));

  // find group type
  if ($('.group-all', this.$container).length) {
    all = true;
  } else if ($group.length) {
    groupColumn = $group.data('column');
  } else {
    return;
  }

  // prepare data
  var $rows = $('.table-row:visible', this._$viewport),
    $sumRow = $.makeDiv('', 'table-row-sum'),
    sum = [];

  for (var r = 0; r < $rows.length; r++) {
    var rowId = $rows.eq(r).attr('id');
    // FIXME CGU is it possible to link row to $row? because table.getModelRowById does a lookup
    var row = this.getModelRowById(rowId);

    // calculate sum per column
    for (var c = 0; c < this.columns.length; c++) {
      column = this.columns[c];
      var value = this.getCellValue(column, row);

      if (column.type == 'number') {
        sum[c] = (sum[c] || 0) + value;
      }
    }

    // test if sum should be shown, if yes: reset sum-array
    var nextRowId = $rows.eq(r + 1).attr('id');
    var nextRow = this.getModelRowById(nextRowId);

    if ((r == $rows.length - 1) || (!all && this.getCellText(groupColumn, row) != this.getCellText(groupColumn, nextRow)) && sum.length > 0) {
      for (c = 0; c < this.columns.length; c++) {
        var $cell;

        column = this.columns[c];
        alignment = scout.Table.parseHorizontalAlignment(column.horizontalAlignment);
        if (typeof sum[c] == 'number') {
          $cell = $.makeDiv('', 'table-cell', sum[c])
            .css('text-align', alignment);
        } else if (!all && column == groupColumn) {
          $cell = $.makeDiv('', 'table-cell', this.getCellText(groupColumn, row))
            .css('text-align', alignment);
        } else {
          $cell = $.makeDiv('', 'table-cell', '&nbsp');
        }

        $cell.appendTo($sumRow).width($rows.eq(r).children().eq(c).outerWidth());
      }

      $sumRow.insertAfter($rows.eq(r))
        .width(this._header.totalWidth + 4)
        .hide()
        .slideDown();

      $sumRow = $.makeDiv('', 'table-row-sum');
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
    this._header.onGroupingChanged($header, all);
  }

  this._group();
};

scout.Table.prototype.colorData = function(mode, colorColumn) {
  var minValue, maxValue, colorFunc, row, rowId, value, v, c, $rows;

  for (var r = 0; r < this.rows.length; r++) {
    row = this.rows[r];
    v = this.getCellValue(colorColumn, row);

    if (v < minValue || minValue === undefined) minValue = v;
    if (v > maxValue || maxValue === undefined) maxValue = v;
  }

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

      cell.css('background-color', '#fff');
      cell.css('background-image', 'linear-gradient(to left, #80c1d0 0%, #80c1d0 ' + level + '%, white ' + level + '%, white 100% )');
    };
  } else if (mode === 'remove')
    colorFunc = function(cell, value) {
      cell.css('background-image', '');
      cell.css('background-color', '#fff');
    };

  $rows = $('.table-row:visible', this._$viewport);

  $('.header-item', this.$container).each(function(i) {
    if ($(this).data('column') == colorColumn) c = i;
  });

  for (var s = 0; s < $rows.length; s++) {
    rowId = $rows.eq(s).attr('id');
    row = this.getModelRowById(rowId);
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

scout.Table.prototype._onRowsInserted = function(rows) {
  //always insert new rows at the end, if the order is wrong a rowOrderChange event will follow
  scout.arrays.pushAll(this.rows, rows);

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
  var rows, $row, i, row;

  //update model
  rows = this.getModelRowsByIds(rowIds);
  for (i = 0; i < rows.length; i++) {
    row = rows[i];
    scout.arrays.remove(this.rows, row);
  }

  //update html doc
  if (this.rendered) {
    for (i = 0; i < rowIds.length; i++) {
      $row = this.findRowById(rowIds[i]);
      $row.remove();
    }
    this.updateScrollbar();
  }
};

scout.Table.prototype._onAllRowsDeleted = function() {
  this.rows = [];

  if (this.rendered) {
    this.drawData();
  }
};

scout.Table.prototype.scrollTo = function($selection) {
  scout.Scrollbar2.scrollTo(this._$viewport, $selection);
};

scout.Table.prototype.selectRowsByIds = function(rowIds) {
  if (!scout.arrays.equalsIgnoreOrder(rowIds, this.selectedRowIds)) {
    this.selectedRowIds = rowIds;

    this.session.send('rowsSelected', this.id, {
      'rowIds': rowIds
    });
  }

  this.selectionHandler.drawSelection();
};

scout.Table.prototype.findSelectedRows = function() {
  if (!this._$viewport) {
    return $();
  }
  return this._$viewport.find('.row-selected');
};

scout.Table.prototype.findRows = function(includeSumRows) {
  var selector = '.table-row';
  if (includeSumRows) {
    selector += ', .table-row-sum';
  }
  return this._$viewport.find(selector);
};

scout.Table.prototype.findSumRows = function() {
  return this._$viewport.find('.table-row-sum');
};

scout.Table.prototype.findCellsForColIndex = function(colIndex, includeSumRows) {
  var selector = '.table-row > div:nth-of-type(' + colIndex + ' )';
  if (includeSumRows) {
    selector += ', .table-row-sum > div:nth-of-type(' + colIndex + ' )';
  }
  return this._$viewport.find(selector);
};

scout.Table.prototype.findRowById = function(rowId) {
  return this._$viewport.find('#' + rowId);
};

scout.Table.prototype.getModelRowById = function(rowId) {
  var row, i;
  for (i = 0; i < this.rows.length; i++) {
    row = this.rows[i];
    if (row.id === rowId) {
      return row;
    }
  }
};

scout.Table.prototype.getModelColumnById = function(columnId) {
  var column, i;
  for (i = 0; i < this.columns.length; i++) {
    column = this.columns[i];
    if (column.id === columnId) {
      return column;
    }
  }
};

scout.Table.prototype.getModelRowsByIds = function(rowIds) {
  var i, row, rows = [];

  for (i = 0; i < this.rows.length; i++) {
    row = this.rows[i];
    if (rowIds.indexOf(row.id) > -1) {
      rows.push(this.rows[i]);
      if (rows.length === rowIds.length) {
        return rows;
      }
    }
  }
  return rows;
};

scout.Table.prototype.filter = function() {
  var that = this,
    rowCount = 0,
    $allRows = this.findRows();

  that.clearSelection();
  this.findSumRows().hide();

  $allRows.each(function() {
    var $row = $(this),
      rowText = $row.text().toLowerCase(),
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
      that.showRow($row);
      rowCount++;
    } else {
      that.hideRow($row);
    }
  });

  //Used by table footer
  this.filteredRowCount = rowCount;

  this._triggerRowsFiltered(rowCount, this.filteredBy());

  $(':animated', that._$viewport).promise().done(function() {
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
  this.findRows().each(function() {
    that.showRow($(this));
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

scout.Table.prototype.showRow = function($row) {
  var that = this;

  // FIXME is(), slideDown() and the complete callback are very slow, which blocks
  //       the UI when filtering many rows (1000+). Therefore we use no animation.
  //       Could this be optimized, maybe depending on the number for rows?
  $row.show();
  $row.removeClass('invisible');
  that.updateScrollbar();
  //  if ($row.is(':hidden')) {
  //    $row.stop().slideDown({
  //      complete: function() {
  //        that.updateScrollbar();
  //      }
  //    });
  //  }
};

scout.Table.prototype.hideRow = function($row) {
  var that = this;

  // FIXME Same issue as in showRow()
  $row.hide();
  $row.addClass('invisible');
  that.updateScrollbar();
  //  if ($row.is(':visible')) {
  //    $row.stop().slideUp({
  //      complete: function() {
  //        that.updateScrollbar();
  //      }
  //    });
  //  }
};

/**
 * @param resizingInProgress set this to true when calling this function several times in a row. If resizing is finished you have to call resizingColumnFinished.
 */
scout.Table.prototype.resizeColumn = function($header, width, summaryWidth, resizingInProgress) {
  var colNum = this._header.getColumnViewIndex($header) + 1;
  var column = $header.data('column');

  column.width = width;

  this.findCellsForColIndex(colNum, true)
    .css('min-width', width)
    .css('max-width', width);
  this.findRows(true)
    .css('min-width', summaryWidth)
    .css('max-width', summaryWidth);

  this._header.onColumnResized($header, width);

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
  this.session.send('columnResized', this.id, data);
};

scout.Table.prototype.moveColumn = function($header, oldPos, newPos, dragged) {
  var column = $header.data('column');

  scout.arrays.remove(this.columns, column);
  scout.arrays.insert(this.columns, column, newPos);

  var data = {
    columnId: column.id,
    index: newPos
  };
  this.session.send('columnMoved', this.id, data);

  this._header.onColumnMoved($header, oldPos, newPos, dragged);

  // move cells
  this.findRows(true).each(function() {
    var $cells = $(this).children();
    if (newPos < oldPos) {
      $cells.eq(newPos).before($cells.eq(oldPos));
    } else {
      $cells.eq(newPos).after($cells.eq(oldPos));
    }
  });
};

scout.Table.prototype._renderColumnOrderChanges = function(oldColumnOrder) {
  var column, i, j, $orderedCells, $cell, $cells, that = this, $row;

  this._header.onOrderChanged(oldColumnOrder);

  // move cells
  this.findRows(true).each(function() {
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

scout.Table.prototype._triggerRowsDrawn = function($rows, numRows) {
  var type = scout.Table.GUI_EVENT_ROWS_DRAWN;
  var event = {
    $rows: $rows,
    numRows: numRows
  };
  this.events.trigger(type, event);
};

scout.Table.prototype.triggerRowsSelected = function($rows) {
  var rowCount = this.rows.length,
    allSelected = false;

  if ($rows) {
    allSelected = $rows.length == rowCount;
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

scout.Table.prototype._renderHeaderVisible = function(headerVisible) {
  //FIXME CGU this would be better than show/hide, but buildRow relies on header -> refactor
  //  if (this.headerVisible && !this._header) {
  //      this._$header = $('table-header').prependTo(this.$data);
  //      this._header = new scout.TableHeader(this, this._$header, this.session);
  //    }
  //  else if (!this.headerVisible && this._header) {
  //    this._$header.remove();
  //    this._header = null;
  //  }
  this._$header.setVisible(headerVisible);
  if (this.rendered) {
    this.htmlComp.revalidate();
  }
};

scout.Table.prototype._renderTableStatusVisible = function(tableStatusVisible) {
  var footerVisible = this._isFooterVisible();
  if (footerVisible) {
    if (!this.footer) {
      this.footer = this._createFooter();
    }
    else {
      this.footer.setTableStatusVisible(tableStatusVisible);
    }
  }
  else if (!footerVisible && this.footer){
    this.footer.remove();
    this.footer = null;
  }
  if (this.rendered) {
    this.htmlComp.revalidate();
  }
};

scout.Table.prototype._renderEnabled = function(enabled) {
  // FIXME CGU remove/add events. Maybe extend jquery to not fire on disabled events?
  this._$viewport.setEnabled(enabled);
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
  rows = new Array(this.rows.length);
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
    for (var j = 0; j < this.columns.length; j++) {
      if (columns[i].id === this.columns[j].id) {
        columns[i].index = this.columns[j].index;
        break;
      }
    }
  }
  this.columns = columns;

  if (this.rendered) {
    this._$header.empty();
    this._header = new scout.TableHeader(this, this._$header, this.session);
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
    column = this.getModelColumnById(columnId);
    currentPosition = this.columns.indexOf(column);
    if (currentPosition < 0) {
      throw new Error('Column with id ' + columnId + 'not found.');
    }

    if (currentPosition !== i) {
      //Update model
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
  var updatedColumns = [], column;

  //Update model columns
  for (var i = 0; i < columns.length; i++) {
    column = this.getModelColumnById(columns[i].id);
    column.text = columns[i].text;
    column.sortActive = columns[i].sortActive;
    column.sortAscending = columns[i].sortAscending;

    updatedColumns.push(column);
  }

  if (this.rendered) {
    this._header.updateHeaders(updatedColumns);
  }
};

scout.Table.prototype.onModelAction = function(event) {
  if (event.type == 'rowsInserted') {
    this._onRowsInserted(event.rows);
  } else if (event.type == 'rowsDeleted') {
    this._onRowsDeleted(event.rowIds);
  } else if (event.type == 'allRowsDeleted') {
    this._onAllRowsDeleted();
  } else if (event.type == 'rowsSelected') {
    this._onRowsSelected(event.rowIds);
  } else if (event.type == 'rowOrderChanged') {
    this._onRowOrderChanged(event.rowIds);
  } else if (event.type == 'columnStructureChanged') {
    this._onColumnStructureChanged(event.columns);
  } else if (event.type == 'columnOrderChanged') {
    this._onColumnOrderChanged(event.columnIds);
  } else if (event.type == 'columnHeadersUpdated') {
    this._onColumnHeadersUpdated(event.columns);
  } else {
    $.log.warn('Model event not handled. Widget: scout.Table. Event: ' + event.type + '.');
  }
};

scout.Table.prototype.onMenuPropertyChange = function(event) {
  //FIXME CGU implement
};

/*
 * Helpers
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
