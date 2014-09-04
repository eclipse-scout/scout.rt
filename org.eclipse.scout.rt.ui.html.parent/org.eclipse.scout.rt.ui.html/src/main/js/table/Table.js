  // SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Table = function() {
  scout.Table.parent.call(this);

  this.$container;
  this.$data;
  this.$dataScroll;
  this._header;
  this.scrollbar;
  this.selectionHandler;
  this._keystrokeAdapter;
  this.controls = [];
  this.menus = [];
  this.staticMenus = [];
  this.rows = [];
  this._addAdapterProperties(['controls', 'menus']);
  this.events = new scout.EventSupport();
  this._filterMap = {};
};
scout.inherits(scout.Table, scout.ModelAdapter);

scout.Table.GUI_EVENT_ROWS_DRAWN = 'rowsDrawn';
scout.Table.GUI_EVENT_ROWS_SELECTED = 'rowsSelected';
scout.Table.GUI_EVENT_ROWS_FILTERED = 'rowsFiltered';
scout.Table.GUI_EVENT_FILTER_RESETTED = 'filterResetted';

scout.Table.prototype.init = function(model, session) {
  scout.Table.parent.prototype.init.call(this, model, session);

  this.configurator = this._createTableConfigurator();
  if (this.configurator) {
    this.configurator.configure(this);
  }
  this._keystrokeAdapter = new scout.TableKeystrokeAdapter(this);

  for (var i=0; i < model.columns.length;i++) {
    model.columns[i].index = i;
  }
};

scout.Table.prototype._createTableConfigurator = function() {
  return new scout.TableConfigurator(this);
};

scout.Table.prototype._render = function($parent) {
  this._$parent = $parent;

  this.$container = this._$parent.appendDIV('table');
  if ($parent.hasClass('desktop-bench')) {
    // TODO cru: desktop table (no input focus required to trigger table keystrokes), is body ok?
    scout.keystrokeManager.installAdapter($('body'), this._keystrokeAdapter);
  } else {
    // independent table, i.e. inside form (input focus required to trigger table keystrokes)
    this.$container.attr('tabIndex', 0);
    this.$container.css('outline', 'none');
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
  this.footer = new scout.TableFooter(this, this.$container, this.session);

  if (this.configurator && this.configurator.render) {
    this.configurator.render();
  }

  // load data and create rows
  this.drawData();
};

scout.Table.prototype._callSetters = function() {
  this._setHeaderVisible(this.headerVisible);
  this._setEnabled(this.enabled);
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
  if (this.scrollbar) {
    this.scrollbar.initThumb();
  }
};

scout.Table.prototype._sort = function() {
  var sortColumns = [];

  // remove selection
  this.clearSelection();

  // find all sort columns
  for (var c = 0; c < this.columns.length; c++) {
    var column = this.columns[c],
      order = column.$div.attr('data-sort-order');
    if (order >= 0) {
      sortColumns[order] = column;
    }
  }

  // compare rows
  function compare(row1, row2) {
    for (var s = 0; s < sortColumns.length; s++) {
      column = sortColumns[s];
      var valueA = this.getValue(column.index, row1);
      var valueB = this.getValue(column.index, row2);
      var dir = column.sortActive && column.sortAscending ? -1 : 1;

      if (valueA < valueB) {
        return dir;
      } else if (valueA > valueB) {
        return -1 * dir;
      }
    }

    return 0;
  }
  this.rows.sort(compare.bind(this));
  this._renderRowOrder();
};

scout.Table.prototype._renderRowOrder = function() {
  var $rows = this.findRows();
  var $sortedRows = $();
  var animationRowLimit = 50;

  //store old position
  if ($rows.length < animationRowLimit) {
    $rows.each(function() {
      $(this).data('old-top', $(this).offset().top);
    });
  }

  for (var i=0;i<this.rows.length;i++) {
    var $row = this.findRowById(this.rows[i].id);
    $sortedRows.push($row[0]);
  }

  // change order in dom
  this.$dataScroll.prepend($sortedRows);

  // for less than animationRowLimit rows: move to old position and then animate
  if ($rows.length < animationRowLimit) {
    $rows.each(function() {
      $(this).css('top', $(this).data('old-top') - $(this).offset().top)
        .animateAVCSD('top', 0);
    });
  }
};

scout.Table.prototype.sort = function($header, dir, additional, remove) {
  var index = $header.data('index');
  var column = this.columns[index];
  column.sortAscending = dir === 'asc' ? true : false;
  column.sortActive = true;

  var data = { columnId: column.id };
  if (additional) {
    data.multiSort = true;
  }
  if (remove) {
    data.sortingRemoved = true;
    column.sortActive = false;
  }

  this.session.send('columnSortingChanged', this.id, data);

  this._header.onSortingChanged($header, dir, additional, remove);

  // sort and visualize
  this._sort();
};

scout.Table.prototype.drawData = function() {
  this.findRows().remove();
  this._drawData(0);
  this.selectionHandler.dataDrawn();
};

scout.Table.prototype._buildRowDiv = function(row) {
  var column, width, style, align, value, alignment;
  var rowWidth = this._header.totalWidth + 4;
  var rowClass = 'table-row ';

  if (this.selectedRowIds && this.selectedRowIds.indexOf(row.id) > -1) {
    rowClass += 'row-selected ';
  }
  // FIXME Check if possible to use $.makeDiv (but maybe it's too slow)
  var unselectable = (scout.device.supportsCssUserSelect() ? '' : ' unselectable="on"'); // workaround for IE 9

  var rowDiv = '<div id="' + row.id + '" class="' + rowClass + '" style="width: ' + rowWidth + 'px"' + unselectable + '>';
  for (var c = 0; c < row.cells.length; c++) {
    column = this.columns[c];
    width = column.width;
    style = (width === 0) ? 'display: none; ' : 'min-width: ' + width + 'px; max-width: ' + width + 'px; ';
    alignment =  scout.Table.parseHorizontalAlignment(column.horizontalAlignment);
    align = alignment !== 'left' ? 'text-align: '+ alignment + '; ' : '';
    value = this.getText(c, row);

    rowDiv += '<div class="table-cell" style="' + style + align + '"' + unselectable + '>' + value + '</div>';
  }
  rowDiv += '</div>';

  return rowDiv;
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
    $rows.appendTo(this.$dataScroll)
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
    x = event.pageX - that.$dataScroll.offset().left;
    y = event.pageY - that.$dataScroll.offset().top;

    if ($selectedRows.length > 0) {
      scout.menus.showContextMenuWithWait(that.session, showContextMenu);
    }

    function showContextMenu() {
      scout.menus.showContextMenu(that._getRowMenus($selectedRows, false), that.$dataScroll, $(that), x, undefined, y);
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

scout.Table.prototype._setMenus = function(menus) {
  var $selectedRows = this.findSelectedRows();
  this._renderMenus($selectedRows);
};

scout.Table.prototype._renderMenus = function($selectedRows) {
  var menus = this._getRowMenus($selectedRows, true);
  this.menubar.updateItems(menus);
};

scout.Table.prototype.onRowsSelected = function($selectedRows) {
  var rowIds = [];

  this.triggerRowsSelected($selectedRows);
  this._renderMenus($selectedRows);

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

scout.Table.prototype.sendRowAction = function($row) {
  this.session.send('rowAction', this.id, {
    'rowId': $row.attr('id')
  });
};

scout.Table.prototype.sendReload = function() {
  this.session.send('reload', this.id);
};

scout.Table.prototype.getValue = function(colIndex, row) {
  var cell = row.cells[colIndex];

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

scout.Table.prototype.getText = function(col, row) {
  var cell = row.cells[col];

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
    all,
    groupIndex,
    $group = $('.group-sort', this.$container);

  // remove all sum rows
  $('.table-row-sum', this.$dataScroll).animateAVCSD('height', 0, $.removeThis, that.updateScrollbar.bind(that));

  // find group type
  if ($('.group-all', this.$container).length) {
    all = true;
  } else if ($group.length) {
    groupIndex = $group.data('index');
  } else {
    return;
  }

  // prepare data
  var $rows = $('.table-row:visible', this.$dataScroll),
    $cols = $('.header-item', this.$container),
    $sumRow = $.makeDiv('', 'table-row-sum'),
    sum = [];

  for (var r = 0; r < $rows.length; r++) {
    var rowId = $rows.eq(r).attr('id');
    //FIXME CGU is it possible to link row to $row? because table.getModelRowById does a lookup
    var row = this.getModelRowById(rowId);

    // calculate sum per column
    for (var c = 0; c < $cols.length; c++) {
      var index = $cols.eq(c).data('index'),
        value = this.getValue(index, row);

      if (this.columns[index].type == 'number') {
        sum[c] = (sum[c] || 0) + value;
      }
    }

    // test if sum should be shown, if yes: reset sum-array
    var nextRowId = $rows.eq(r + 1).attr('id');
    var nextRow = this.getModelRowById(rowId);

    if ((r == $rows.length - 1) || (!all && this.getText(groupIndex, row) != this.getText(groupIndex, nextRow)) && sum.length > 0) {
      for (c = 0; c < $cols.length; c++) {
        var $div;

        //FIXME CGU alignment (use scout.Table.parseAlignment) as soon as SUM works again
        if (typeof sum[c] == 'number') {
          $div = $.makeDiv('', '', sum[c])
            .css('text-align', 'end');
        } else if (!all && $cols.eq(c).data('index') == groupIndex) {
          $div = $.makeDiv('', '', this.getText(groupIndex, row))
            .css('text-align', 'start');
        } else {
          $div = $.makeDiv('', '', '&nbsp');
        }

        $div.appendTo($sumRow).width($rows.eq(r).children().eq(c).outerWidth());
      }

      $sumRow.insertAfter($rows.eq(r))
        .width(this._header.totalWidth + 4)
        .css('height', 0)
        .animateAVCSD('height', 34, null, that.updateScrollbar.bind(that));

      $sumRow = $.makeDiv('', 'table-row-sum');
      sum = [];
    }
  }
};

scout.Table.prototype.group = function($headerItem, draw, all) {
  $('.group-sort', this.$container).removeClass('group-sort');
  $('.group-all', this.$container).removeClass('group-all');

  if (draw) {
    if (!all) {
      this.sort($headerItem, 'asc', false);
    }
    this._header.onGroupingChanged($headerItem, all);
  }

  this._group();
};

scout.Table.prototype.colorData = function(mode, colorColumn) {
  var minValue,
    maxValue,
    colorFunc;

  for (var r = 0; r < this.rows.length; r++) {
    var v = this.getValue(colorColumn, r);

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

  var $rows = $('.table-row:visible', this.$dataScroll),
    c;

  $('.header-item', this.$container).each(function(i) {
    if ($(this).data('index') == colorColumn) c = i;
  });

  for (var s = 0; s < $rows.length; s++) {
    var rowId = $rows.eq(s).attr('id'),
      row = this.getModelRowById(rowId),
      value = this.getValue(colorColumn, row);

    colorFunc($rows.eq(s).children().eq(c), value);

  }
};

scout.Table.prototype.insertRows = function(rows) {
  //always insert new rows at the end
  scout.arrays.pushAll(this.rows, rows);

  if (this.rendered) {
    this.drawData();
  }
};

scout.Table.prototype.deleteRowsByIds = function(rowIds) {
  var rows, $row, i, row;

  //update model
  rows = this.getModelRowsByIds(rowIds);
  for (i=0; i < rows.length; i++){
    row = rows[i];
    scout.arrays.remove(this.rows, row);
  }

  //update html doc
  if (this.rendered) {
    for (i=0; i < rowIds.length; i++){
      $row = this.findRowById(rowIds[i]);
      $row.remove();
    }
    this.updateScrollbar();
  }
};

scout.Table.prototype.deleteAllRows = function() {
  this.rows = [];

  if (this.rendered) {
    this.drawData();
  }
};

scout.Table.prototype.selectRowsByIds = function(rowIds) {
  if (!Array.isArray(rowIds)) {
    rowIds = [rowIds];
  }

  this.selectedRowIds = rowIds;

  if (this.rendered) {
    this.selectionHandler.drawSelection();
  }

  if (!this.session.processingEvents) {
    this.session.send('rowsSelected', this.id, {
      'rowIds': rowIds
    });
  }
};

scout.Table.prototype.findSelectedRows = function() {
  if (!this.$dataScroll) {
    return $();
  }
  return this.$dataScroll.find('.row-selected');
};

scout.Table.prototype.findRows = function() {
  if (!this.$dataScroll) {
    return $();
  }
  return this.$dataScroll.find('.table-row');
};

scout.Table.prototype.findRowById = function(rowId) {
  return this.$dataScroll.find('#' + rowId);
};

scout.Table.prototype.getModelRowById = function(rowId) {
  var row, i;
  for (i=0; i < this.rows.length; i++){
    row = this.rows[i];
    if (row.id === rowId) {
      return row;
    }
  }
};

scout.Table.prototype.getModelRowsByIds = function(rowIds) {
  var i, row, rows = [];

  for (i=0; i < this.rows.length; i++){
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
    origin = [],
    $allRows = this.findRows();

  that.clearSelection();
  $('.table-row-sum', this.$dataScroll).hide();

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

  // find info text
  for (var i = 0; i < this.columns.length; i++) {
    if (this.columns[i].filterFunc) {
      origin.push(that.columns[i].$div.text());
    }
  }

  for (var key in this._filterMap) {
    var filter = this._filterMap[key];
    origin.push(filter.label);
  }

  this._triggerRowsFiltered(rowCount, origin);

  $(':animated', that.$dataScroll).promise().done(function() {
    that._group();
  });

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
  this._triggerFilterResetted();
  $('.control-filter', this.$container, '').val('');
  this.unregisterFilter(scout.MapTableControl.FILTER_KEY);
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

// move column

scout.Table.prototype.moveColumn = function($header, oldPos, newPos, dragged) {
  this._header.onColumnMoved($header, oldPos, newPos, dragged);

  // move cells
  $('.table-row, .table-row-sum').each(function() {
    var $cells = $(this).children();
    if (newPos < 0) {
      $(this).prepend($cells.eq(oldPos / 2));
    } else {
      $cells.eq(newPos / 2).after($cells.eq(oldPos / 2));
    }
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

scout.Table.prototype._setHeaderVisible = function(headerVisible) {
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
};

scout.Table.prototype._setEnabled = function(enabled) {
  //FIXME CGU remove/add events. Maybe extend jquery to not fire on disabled events?
  this.$dataScroll.setEnabled(enabled);
};

scout.Table.prototype._setMultiSelect = function(multiSelect) {
  // nop
};

scout.Table.prototype._onRowOrderChanged = function(rowIds) {
  var newPos, rows, row;
  if (rowIds.length !== this.rows.length) {
    throw "Order changed event may not be processed because lengths of the arrays differ.";
  }

  // update model
  rows = new Array(this.rows.length);
  for (var i=0; i < this.rows.length; i++) {
    row = this.rows[i];
    newPos = rowIds.indexOf(this.rows[i].id);
    rows[newPos] = row;
  }
  this.rows = rows;

  if (this.rendered) {
    this._renderRowOrder();
  }
};

scout.Table.prototype.onModelAction = function(event) {
  if (event.type == 'rowsInserted') {
    this.insertRows(event.rows);
  } else if (event.type == 'rowsDeleted') {
    this.deleteRowsByIds(event.rowIds);
  } else if (event.type == 'allRowsDeleted') {
    this.deleteAllRows();
  } else if (event.type == 'rowsSelected') {
    this.selectRowsByIds(event.rowIds);
  } else if (event.type == 'rowOrderChanged') {
    this._onRowOrderChanged(event.rowIds);
  } else {
    $.log('Model event not handled. Widget: scout.Table. Event: ' + event.type + '.');
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
