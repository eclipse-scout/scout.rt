// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Table = function(session, model) {
  this.model = model;
  this.session = session;

  this.$container;
  this.$data;
  this.$dataScroll;
  this._header;
  this.scrollbar;
  this.selectionHandler;
  this.rowMenuHandler;

  if (session && model) {
    this.session.widgetMap[model.id] = this;
  }

  //non inheritance based initialization
  if(arguments.length>0) {
    this.events = new scout.EventSupport();
    this._filterMap = {};

    this.configurator = this._createTableConfigurator();
    if (this.configurator) {
      this.configurator.configure(this);
    }
    this._keystrokeAdapter = new scout.TableKeystrokeAdapter(this);
  }
};

scout.Table.EVENT_ROWS_SELECTED = 'rowsSelected';
scout.Table.EVENT_ROWS_INSERTED = 'rowsInserted';
scout.Table.EVENT_ROW_CLICKED = 'rowClicked';
scout.Table.EVENT_ROW_ACTION = 'rowAction';
scout.Table.EVENT_SELECTION_MENUS_CHANGED = 'selectionMenusChanged';

scout.Table.GUI_EVENT_ROWS_DRAWN = 'rowsDrawn';
scout.Table.GUI_EVENT_ROWS_SELECTED = 'rowsSelected';
scout.Table.GUI_EVENT_ROWS_FILTERED = 'rowsFiltered';
scout.Table.GUI_EVENT_FILTER_RESETTED = 'filterResetted';

scout.Table.prototype._createTableConfigurator = function () {
  return new scout.TableConfigurator(this);
};

scout.Table.prototype._render = function($parent) {
  this._$parent = $parent;

  //create container
  this.$container = this._$parent.appendDiv(this.model.id, 'table');

  this._$header = this.$container.appendDiv(this.model.id + '_header', 'table-header');
  if (!this.model.headerVisible) {
    //FIXME maybe better to not create at all?
    this._$header.hide();
  }
  this._header = new scout.TableHeader(this.session, this, this._$header);

  this.$data = this.$container.appendDiv(this.model.id + '_data', 'table-data');

  this._$footer = this.$container.appendDiv(this.model.id + '_footer');
  this.footer = new scout.TableFooter(this.session, this, this._$footer);

  if (this.configurator && this.configurator.render) {
    this.configurator.render();
  }

  // load data and create rows
  this.drawData();
};

scout.Table.prototype.detach = function() {
  this.$container.detach();
  scout.keystrokeManager.removeAdapter(this._keystrokeAdapter);
};

scout.Table.prototype.attach = function($container) {
  if (!this.$container) {
    this._render($container);
  } else {
    this.$container.appendTo($container);
  }
  scout.keystrokeManager.addAdapter(this._keystrokeAdapter);
};

scout.Table.prototype.drawSelection = function() {
  if(this.selectionHandler) {
    this.selectionHandler.drawSelection();
  }
};

scout.Table.prototype.resetSelection = function() {
  if(this.selectionHandler) {
    this.selectionHandler.resetSelection();
  }
};

scout.Table.prototype.updateScrollbar = function() {
  if (this.scrollbar) {
    this.scrollbar.initThumb();
  }
};

scout.Table.prototype._sort = function() {
  var sortColumns = [];

  // remove selection
  this.resetSelection();

  // find all sort columns
  for (var c = 0; c < this.model.columns.length; c++) {
    var column = this.model.columns[c],
      order = column.$div.attr('data-sort-order'),
      dir = column.$div.hasClass('sort-up') ? 'up' : (order >= 0 ? 'down' : '');
    if (order >= 0) {
      sortColumns[order] = {
        index: c,
        dir: dir
      };
    }
  }

  // compare rows
  var that = this;

  function compare(a, b) {
    for (var s = 0; s < sortColumns.length; s++) {
      var index = sortColumns[s].index,
        valueA = that.getValue(index, $(a).data('row')),
        valueB = that.getValue(index, $(b).data('row')),
        dir = sortColumns[s].dir == 'up' ? -1 : 1;

      if (valueA < valueB) {
        return dir;
      } else if (valueA > valueB) {
        return -1 * dir;
      }
    }

    return 0;
  }

  // find all rows
  var $rows = $('.table-row');

  // store old position
  $rows.each(function() {
    $(this).data('old-top', $(this).offset().top);
  });

  // change order in dom
  $rows = $rows.sort(compare);
  this.$dataScroll.prepend($rows);

  // for less than 100 rows: move to old position and then animate
  if ($rows.length < 100) {
    $rows.each(function() {
      $(this).css('top', $(this).data('old-top') - $(this).offset().top)
        .animateAVCSD('top', 0);
    });
  }
};

scout.Table.prototype.sortChange = function($header, dir, additional, remove) {
  $header.removeClass('sort-up sort-down');

  if (remove) {
    var attr = $header.attr('data-sort-order');
    $header.siblings().each(function() {
      if ($(this).attr('data-sort-order') > attr) {
        $(this).attr('data-sort-order', parseInt($(this).attr('data-sort-order'), 0) - 1);
      }
    });
    $header.removeAttr('data-sort-order');
  } else {
    // change sort order of clicked header
    $header.addClass('sort-' + dir);

    // when shift pressed: add, otherwise reset
    if (additional) {
      var clickOrder = $header.data('sort-order'),
        maxOrder = -1;

      $('.header-item').each(function() {
        var value = parseInt($(this).attr('data-sort-order'), 0);
        maxOrder = (value > maxOrder) ? value : maxOrder;
      });

      if (clickOrder !== undefined && clickOrder !== null) {
        $header.attr('data-sort-order', clickOrder);
      } else {
        $header.attr('data-sort-order', maxOrder + 1);
      }

    } else {
      $header.attr('data-sort-order', 0)
        .siblings()
        .removeClass('sort-up sort-down')
        .attr('data-sort-order', null);
    }
  }

  // sort and visualize
  this._sort();
};

scout.Table.prototype.drawData = function() {
  $('.table-row').remove();
  this._drawData(0);
  this.drawSelection();
};

scout.Table.prototype._buildRowDiv = function(row, index) {
  var rowClass = 'table-row ',
    table = this.model,
    rowWidth = this._header.totalWidth + 4;

  if (table.selectedRowIds && table.selectedRowIds.indexOf(row.id) > -1) {
    rowClass += 'row-selected ';
  }
  var rowDiv = '<div id="' + row.id + '" class="' + rowClass + '" data-row=' + index + ' style="width: ' + rowWidth + 'px">';
  for (var c = 0; c < row.cells.length; c++) {
    var column = table.columns[c],
      width = column.width,
      style = (width === 0) ? 'display: none; ' : 'width: ' + width + 'px; ',
      allign = (column.type == 'number') ? 'text-align: right; ' : '',
      value = this.getText(c, index);

    rowDiv += '<div style="' + style + allign + '">' + value + '</div>';
  }
  rowDiv += '</div>';

  return rowDiv;
};

scout.Table.prototype._drawData = function(startRow) {
  // this function has to be fast
  var rowString = '',
    table = this.model,
    that = this,
    numRowsLoaded = startRow,
    $rows;

  if (table.rows && table.rows.length > 0) {
    for (var r = startRow; r < Math.min(table.rows.length, startRow + 100); r++) {
      var row = table.rows[r];
      rowString += this._buildRowDiv(row, r);
    }
    numRowsLoaded = r;

    // append block of rows

    $rows = $(rowString);
    $rows.appendTo(this.$dataScroll)
      .on('clicks', '', onClicks);
  }

  // update info and scrollbar
  this._triggerRowsDrawn($rows, numRowsLoaded);
  this.updateScrollbar();

  // repaint and append next block
  if (table.rows && table.rows.length > 0) {
    if (numRowsLoaded < table.rows.length) {
      setTimeout(function() {
        that._drawData(startRow + 100);
      }, 0);
    }
  }

  function onClicks(event) {
    if (event.type == 'singleClick') {
      onClick(event);
    }

    that.sendRowsSelected();

    if (event.type == 'doubleClick') {
      onDoubleClick(event);
    }
  }

  function onClick(event) {
    var $row = $(event.delegateTarget);
    //Send click only if mouseDown and mouseUp happened on the same row
    that.session.send(scout.Table.EVENT_ROW_CLICKED, that.model.id, {
      "rowId": $row.attr('id')
    });
  }

  function onDoubleClick(event) {
    var $row = $(event.delegateTarget);
    that.sendRowAction($row);
  }

};

scout.Table.prototype.sendRowsSelected = function() {
  var rowIds = [],
    $selectedRows = $('.row-selected');

  $selectedRows.each(function() {
    rowIds.push($(this).attr('id'));
  });

  if (scout.arrays.equalsIgnoreOrder(rowIds, this.model.selectedRowIds)) {
    return;
  }

  this.model.selectedRowIds = rowIds;
  if (this.model.selectedRowIds) {
    this.session.send(scout.Table.EVENT_ROWS_SELECTED, this.model.id, {
      "rowIds": rowIds
    });
  }
};

scout.Table.prototype.sendRowAction = function ($row) {
  this.session.send(scout.Table.EVENT_ROW_ACTION, this.model.id, {
    "rowId": $row.attr('id')
  });
};

scout.Table.prototype.getValue = function(col, row) {
  var cell = this.model.rows[row].cells[col];

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
  var cell = this.model.rows[row].cells[col];

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
    table = this.model,
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
    var row = $rows.eq(r).data('row');

    // calculate sum per column
    for (var c = 0; c < $cols.length; c++) {
      var index = $cols.eq(c).data('index'),
        value = this.getValue(index, row);

      if (table.columns[index].type == 'number') {
        sum[c] = (sum[c] || 0) + value;
      }
    }

    // test if sum should be shown, if yes: reset sum-array
    var nextRow = $rows.eq(r + 1).data('row');
    if ((r == $rows.length - 1) || (!all && this.getText(groupIndex, row) != this.getText(groupIndex, nextRow)) && sum.length > 0) {
      for (c = 0; c < $cols.length; c++) {
        var $div;

        if (typeof sum[c] == 'number') {
          $div = $.makeDiv('', '', sum[c])
            .css('text-align', 'right');
        } else if (!all && $cols.eq(c).data('index') == groupIndex) {
          $div = $.makeDiv('', '', this.getText(groupIndex, row))
            .css('text-align', 'left');
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

scout.Table.prototype.groupChange = function($header, draw, all) {
  $('.group-sort', this.$container).removeClass('group-sort');
  $('.group-all', this.$container).removeClass('group-all');

  if (draw) {
    if (all) {
      $header.parent().addClass('group-all');
    } else {
      this.sortChange($header, 'up', false);
      $header.addClass('group-sort');
    }
  }

  this._group();
};

scout.Table.prototype.colorData = function(mode, colorColumn) {
  var minValue,
    maxValue,
    colorFunc;

  for (var r = 0; r < this.model.rows.length; r++) {
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
    var row = $rows.eq(s).data('row'),
      value = this.getValue(colorColumn, row);

    colorFunc($rows.eq(s).children().eq(c), value);

  }
};

scout.Table.prototype.insertRows = function(rows) {
  //always insert new rows at the end
  var table = this.model;
  if (table.rows) {
    table.rows.push.apply(table.rows, rows);
  } else {
    table.rows = rows;
  }
  if (this.$container) {
    this.drawData();
  }
};

scout.Table.prototype.selectRowsByIds = function(rowIds) {
  if (!Array.isArray(rowIds)) {
    rowIds = [rowIds];
  }

  var table = this.model;
  table.selectedRowIds = rowIds;

  if (this.$dataScroll) {
    this.resetSelection();

    //select rows
    for (var i = 0; i < rowIds.length; i++) {
      var rowId = rowIds[i];
      var $row = $('#' + rowId);
      $row.addClass('row-selected');
    }

    this.drawSelection();
  }

  //FIXME selection menu is not shown when using this method

  if (!this.updateFromModelInProgress) {
    //not necessary for now since selectRowsByIds is only called by onModelAction, but does no harm either
    this.session.send(scout.Table.EVENT_ROWS_SELECTED, this.model.id, {
      "rowIds": rowIds
    });
  }
};

scout.Table.prototype.findSelectedRows = function() {
  return this.$dataScroll.find('.row-selected');
};

scout.Table.prototype._onSelectionMenusChanged = function(selectedRowIds, menus) {
  this.model.selectionMenus = menus;

  var $selectedRows = this.findSelectedRows();
  //FIXME see tree for reference

  //delgate to handler
  if (this.rowMenuHandler) {
//  this.rowMenuHandler.xxx
  }
};

scout.Table.prototype.filter = function() {
  var that = this,
    rowCount = 0,
    origin = [],
    $allRows = $('.table-row', that.$dataScroll);

  that.resetSelection();
  $('.table-row-sum', this.$dataScroll).hide();

  $allRows.each(function() {
    var $row = $(this),
      rowText = $row.text().toLowerCase(),
      show = true,
      i;

    for (i = 0; i < that.model.columns.length; i++) {
      if (that.model.columns[i].filterFunc) {
        show = show && that.model.columns[i].filterFunc($row);
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
  for (var i = 0; i < this.model.columns.length; i++) {
    if (this.model.columns[i].filterFunc) {
      origin.push(that.model.columns[i].$div.text());
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
  this.resetSelection();

  // reset rows
  var that = this;
  $('.table-row', that.$dataScroll).each(function() {
    that.showRow($(this));
  });
  this._group();

  // set back all filter functions
  for (var i = 0; i < this.model.columns.length; i++) {
    this.model.columns[i].filter = [];
    this.model.columns[i].filterFunc = null;
  }
  this._filterMap = {};
  this._triggerFilterResetted();
};

/**
 * @param filter object with name and accept()
 */
scout.Table.prototype.registerFilter = function(key, filter) {
  if (!key) {
    throw 'key has to be defined';
  }

  this._filterMap[key] = filter;
};

scout.Table.prototype.getFilter = function(key, filter) {
  if (!key) {
    throw 'key has to be defined';
  }

  return this._filterMap[key];
};

scout.Table.prototype.unregisterFilter = function(key) {
  if (!key) {
    throw 'key has to be defined';
  }

  delete this._filterMap[key];
};

scout.Table.prototype.showRow = function($row) {
  var that = this;

  if ($row.is(':hidden')) {
    $row.show()
      .stop()
      .animate({
        'height': '34',
        'padding-top': '2',
        'padding-bottom': '2'
      }, {
        complete: function() {
          that.updateScrollbar();
        }
      });
  }
};

scout.Table.prototype.hideRow = function($row) {
  var that = this;

  if ($row.is(':visible')) {
    $row
      .stop()
      .animate({
        'height': '0',
        'padding-top': '0',
        'padding-bottom': '0'
      }, {
        complete: function() {
          $(this).hide();
          that.updateScrollbar();
        }
      });
  }
};

// move column

scout.Table.prototype.moveColumn = function($header, oldPos, newPos, dragged) {
  var $headers = $('.header-item, .header-resize'),
    $moveHeader = $headers.eq(oldPos),
    $moveResize = $headers.eq(oldPos + 1);

  // store old position of header
  $headers.each(function() {
    $(this).data('old-pos', $(this).offset().left);
  });

  // change order in dom of header
  if (newPos < 0) {
    this._$header.prepend($moveResize);
    this._$header.prepend($moveHeader);
  } else {
    $headers.eq(newPos).after($moveHeader);
    $headers.eq(newPos).after($moveResize);
  }

  // move menu
  var left = $header.offset().left;

  $('#TableHeaderMenuTitle').animateAVCSD('left', left - 12);
  $('#TableHeaderMenu').animateAVCSD('left', left - 12);

  // move cells
  $('.table-row, .table-row-sum').each(function() {
    var $cells = $(this).children();
    if (newPos < 0) {
      $(this).prepend($cells.eq(oldPos / 2));
    } else {
      $cells.eq(newPos / 2).after($cells.eq(oldPos / 2));
    }
  });

  // move to old position and then animate
  if (dragged) {
    $header.css('left', parseInt($header.css('left'), 0) + $header.data('old-pos') - $header.offset().left)
      .animateAVCSD('left', 0);
  } else {
    $headers.each(function() {
      $(this).css('left', $(this).data('old-pos') - $(this).offset().left)
        .animateAVCSD('left', 0);
    });
  }

};

scout.Table.prototype._triggerRowsDrawn = function($rows, numRows) {
  var type = scout.Table.GUI_EVENT_ROWS_DRAWN;
  var event = {
    $rows: $rows,
    numRows: numRows
  };
  this.events.trigger(type, event);
};

scout.Table.prototype.triggerRowsSelected = function($rows, allSelected) {
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

scout.Table.prototype._triggerFilterResetted = function(numRows, filterName) {
  var type = scout.Table.GUI_EVENT_FILTER_RESETTED;
  this.events.trigger(type);
};

scout.Table.prototype._handleModelHeaderVisibleChange = function(headerVisible) {
  if (headerVisible) {
    this._$header.show();
  } else {
    this._$header.hide();
  }
};

scout.Table.prototype.onModelPropertyChange = function(event) {
  if (event.hasOwnProperty('headerVisible')) {
    this._handleModelHeaderVisibleChange(event.headerVisible);
  }
};

scout.Table.prototype.onModelAction = function(event) {
  if (event.type_ == scout.Table.EVENT_ROWS_INSERTED) {
    this.insertRows(event.rows);
  } else if (event.type_ == scout.Table.EVENT_ROWS_SELECTED) {
    this.selectRowsByIds(event.rowIds);
  } else if (event.type_ == scout.Table.EVENT_SELECTION_MENUS_CHANGED) {
    this._onSelectionMenusChanged(event.selectedRowIds, event.menus);
  } else {
    $.log("Model event not handled. Widget: scout.Table. Event: " + event.type_ + ".");
  }
};
