// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Table = function(session, model) {
  this.model = model;
  this.session = session;

  this.config = {
    contextMenuEnabled: true
  };
  this.$container;
  this.$data;
  this._$dataScroll;
  this._header;
  this.scrollbar;

  if (session && model) {
    this.session.widgetMap[model.id] = this;
  }

  this.events = new scout.EventSupport();
  this._filterMap = {};
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

  this.$data = this.$container.appendDiv(this.model.id + '_data', 'table-data'); //FIXME maybe create TableData.js
  this._$dataScroll = this.$data.appendDiv(this.model.id + '_dataScroll', 'table-data-scroll');
  this.scrollbar = new scout.Scrollbar(this._$dataScroll, 'y');

  this._$footer = this.$container.appendDiv(this.model.id + '_footer');
  this.footer = new scout.TableFooter(this.session, this, this._$footer);

  // load data and create rows
  this.drawData();
};

scout.Table.prototype.detach = function() {
  this.$container.detach();
};

scout.Table.prototype.attach = function($container) {
  if (!this.$container) {
    this._render($container);
  } else {
    this.$container.appendTo($container);
  }
};

scout.Table.prototype.drawSelection = function() {
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
  if (this.model.rows) {
    rowCount = this.model.rows.length;
  }
  this._triggerRowsSelected($selectedRows, $selectedRows.length == rowCount);
};

scout.Table.prototype._resetSelection = function() {
  $('.row-selected', this.$data).removeClass('row-selected');
  this.drawSelection();
  $('#RowMenu, #RowDrill, #RowMenuContainer').remove();
};

scout.Table.prototype._sort = function() {
  var sortColumns = [];

  // remove selection
  this._resetSelection();

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
  this._$dataScroll.prepend($rows);

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
    table = this.model;

  if (table.selectedRowIds && table.selectedRowIds.indexOf(row.id) > -1) {
    rowClass += 'row-selected ';
  }
  var rowDiv = '<div id="' + row.id + '" class="' + rowClass + '" data-row=' + index + '>';
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
  var rowString = '';
  var table = this.model,
    that = this;

  var numRowsLoaded = startRow;
  if (table.rows && table.rows.length > 0) {
    for (var r = startRow; r < Math.min(table.rows.length, startRow + 100); r++) {
      var row = table.rows[r];
      rowString += this._buildRowDiv(row, r);
    }
    numRowsLoaded = r;

    // append block of rows
    $(rowString)
      .appendTo(this._$dataScroll)
      .on('mousedown', '', onMouseDown)
      .on('clicks', '', onClicks)
      .width(this._header.totalWidth + 4)
      .on('contextmenu', function(e) {
        e.preventDefault();
      });

  }

  // update info and scrollbar
  this._triggerRowsDrawn(numRowsLoaded);
  this.scrollbar.initThumb();

  // repaint and append next block
  if (table.rows && table.rows.length > 0) {
    if (numRowsLoaded < table.rows.length) {
      setTimeout(function() {
        that._drawData(startRow + 100);
      }, 0);
    }
  }

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

    $('#RowMenu, #RowDrill, #RowMenuContainer').remove();

    // just a click...
    selectData(event);

    // ...or movement with held mouse button
    $(".table-row").one("mousemove", function(event) {
      selectData(event);
    });

    $(".table-row").one("mouseup", function(event) {
      onMouseUp(event);
    });

    // action for all affected rows
    function selectData(event) {
      // affected rows between $row and Target
      var firstIndex = first || $row.index(),
        lastIndex = $(event.delegateTarget).index();

      var startIndex = Math.min(firstIndex, lastIndex),
        endIndex = Math.max(firstIndex, lastIndex) + 1;

      var $actionRow = $('.table-row', that.$data).slice(startIndex, endIndex);

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
      $(".table-row").unbind("mousemove");
      $(".table-row").unbind("mouseup");

      //Handle mouse move selection. Single row selections are handled by onClicks
      if ($row.get(0) != event.delegateTarget) {
        sendRowsSelected();
      }

      if (that.config.contextMenuEnabled) {
        showSelectionMenu(event.pageX, event.pageY, event.button);
      }
    }
  }

  function sendRowsSelected() {
    var rowIds = [],
      $selectedRows = $('.row-selected');

    $selectedRows.each(function() {
      rowIds.push($(this).attr('id'));
    });

    if (scout.arrays.equalsIgnoreOrder(rowIds, that.model.selectedRowIds)) {
      return;
    }

    that.model.selectedRowIds = rowIds;
    if (that.model.selectedRowIds) {
      that.session.send(scout.Table.EVENT_ROWS_SELECTED, that.model.id, {
        "rowIds": rowIds
      });
    }
  }

  function onClicks(event) {
    if (event.type == 'singleClick') {
      onClick(event);
    }

    sendRowsSelected();

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
    sendRowAction($row);
  }

  function sendRowAction($row) {
    that.session.send(scout.Table.EVENT_ROW_ACTION, that.model.id, {
      "rowId": $row.attr('id')
    });
  }

  function showSelectionMenu(x, y, button) {
    // selection
    var $selectedRows = $('.row-selected'),
      $firstRow = $selectedRows.first();

    // make menu - if not already there
    var $RowDrill = $('#RowDrill');
    if ($RowDrill.length === 0) {
      $RowDrill = that._$dataScroll.appendDiv('RowDrill')
        .on('click', '', function() {
          sendRowAction($firstRow);
        });

      var h1 = $RowDrill.outerHeight();
      $RowDrill.height(0).animateAVCSD('height', h1, null, null, 75);
    }
    var $RowMenu = $('#RowMenu');
    if ($RowMenu.length === 0) {
      $RowMenu = that._$dataScroll.appendDiv('RowMenu')
        .on('click', '', clickRowMenu);

      var h2 = $RowMenu.outerHeight();
      $RowMenu.height(0).animateAVCSD('height', h2, null, null, 75);
    }

    // place menu
    // TODO cru: place on top if mouse movement goes up?
    var top = $selectedRows.last().offset().top - that._$dataScroll.offset().top + 32,
      left = Math.max(25, Math.min($firstRow.outerWidth() - 164, x - that._$dataScroll.offset().left - 13));

    $RowDrill.css('left', left - 16).css('top', top);
    $RowMenu.css('left', left + 16).css('top', top);

    // mouse over effect
    var $showMenu = $selectedRows
      .add($selectedRows.first().prev())
      .add($selectedRows.last().next())
      .add($selectedRows.last().next().next())
      .add($RowDrill)
      .add($RowMenu);

    $showMenu
      .on('mouseenter', '', enterSelection)
      .on('mouseleave', '', leaveSelection);

    if (button == 2) {
      clickRowMenu();
    }

    // TODO cru: remove events?

    function enterSelection(event) {
      $RowDrill.animateAVCSD('height', h1, null, null, 75);
      $RowMenu.animateAVCSD('height', h2, null, null, 75);
    }

    function leaveSelection(event) {
      if (!$(event.toElement).is($showMenu) && !$('#RowMenuContainer').length) {
        $RowDrill.animateAVCSD('height', 6, null, null, 75);
        $RowMenu.animateAVCSD('height', 6, null, null, 75);
      }
    }

    function clickRowMenu() {
      if ($('#RowMenuContainer').length) {
        removeMenu();
        return;
      }

      var menus = that.model.selectionMenus;
      if (menus && menus.length > 0) {
        // create 2 container, animate do not allow overflow
        var $RowMenuContainer = $RowMenu.beforeDiv('RowMenuContainer')
          .css('left', left + 16).css('top', top);

        $showMenu = $showMenu.add($RowMenuContainer);

        // create menu-item and menu-button
        for (var i = 0; i < menus.length; i++) {
          if (menus[i].iconId) {
            $RowMenuContainer.appendDiv('', 'menu-button')
              .attr('id', menus[i].id)
              .attr('data-icon', menus[i].iconId)
              .attr('data-label', menus[i].text)
              .on('click', '', onMenuItemClicked)
              .hover(onHoverIn, onHoverOut);
          } else {
            $RowMenuContainer.appendDiv('', 'menu-item', menus[i].text)
              .attr('id', menus[i].id)
              .on('click', '', onMenuItemClicked);
          }
        }

        // wrap menu-buttons and add one div for label
        $('.menu-button', $RowMenuContainer).wrapAll('<div id="MenuButtons"></div>');
        $('#MenuButtons', $RowMenuContainer).appendDiv('MenuButtonsLabel');
        $RowMenuContainer.append($('#MenuButtons', $RowMenuContainer));

        // animated opening
        var h = $RowMenuContainer.outerHeight();
        $RowMenuContainer.css('height', 0).animateAVCSD('height', h);

        var t = parseInt($RowMenu.css('top'), 0);
        $RowMenu.css('top', t).animateAVCSD('top', t + h - 2);

        // TODO cru; every user action will close menu
        //$('*').one('mousedown keydown mousewheel', removeMenu);
      }

      function onHoverIn() {
        $('#MenuButtonsLabel').text($(this).data('label'));
      }

      function onHoverOut() {
        $('#MenuButtonsLabel').text('');
      }

      function onMenuItemClicked() {}

      function removeMenu() {
        var $RowMenuContainer = $('#RowMenuContainer'),
          h = $RowMenuContainer.outerHeight();

        $RowMenuContainer.animateAVCSD('height', 0, $.removeThis);

        var t = parseInt($RowMenu.css('top'), 0);
        $RowMenu.css('top', t).animateAVCSD('top', t - h + 2);
      }
    }
  }
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
  $('.table-row-sum', this._$dataScroll).animateAVCSD('height', 0, $.removeThis, that.scrollbar.initThumb.bind(that.scrollbar));

  // find group type
  if ($('.group-all', this.$container).length) {
    all = true;
  } else if ($group.length) {
    groupIndex = $group.data('index');
  } else {
    return;
  }

  // prepare data
  var $rows = $('.table-row:visible', this._$dataScroll),
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
        .animateAVCSD('height', 34, null, that.scrollbar.initThumb.bind(that.scrollbar));

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

  var $rows = $('.table-row:visible', this._$dataScroll),
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
  var table = this.model;
  table.selectedRowIds = rowIds;

  if (this._$dataScroll) {
    this._resetSelection();

    //select rows
    for (var i = 0; i < rowIds.length; i++) {
      var rowId = rowIds[i];
      var $row = $('#' + rowId);
      $row.addClass('row-selected');
    }

    this.drawSelection();
  }

  if (!this.updateFromModelInProgress) {
    //not necessary for now since selectRowsByIds is only called by onModelAction, but does no harm either
    this.session.send(scout.Table.EVENT_ROWS_SELECTED, this.model.id, {
      "rowIds": rowIds
    });
  }
};

scout.Table.prototype.findSelectedRows = function() {
  return this._$dataScroll.find('.row-selected');
};

scout.Table.prototype._onSelectionMenusChanged = function(selectedRowIds, menus) {
  this.model.selectionMenus = menus;

  var $selectedRows = this.findSelectedRows();
  //FIXME see tree for reference
};

scout.Table.prototype.filter = function() {
  var that = this,
    rowCount = 0,
    origin = [],
    $allRows = $('.table-row', that._$dataScroll);

  that._resetSelection();
  $('.table-row-sum', this._$dataScroll).hide();

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
      show &= filter.accept($row);
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

  $(':animated', that._$dataScroll).promise().done(function() {
    that._group();
  });

};

scout.Table.prototype.resetFilter = function() {
  this._resetSelection();

  // reset rows
  var that = this;
  $('.table-row', that._$dataScroll).each(function() {
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
          that.scrollbar.initThumb();
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
          that.scrollbar.initThumb();
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

  $('#TableColumnHeaderMenuTitle').animateAVCSD('left', left - 12);
  $('#TableColumnHeaderMenu').animateAVCSD('left', left - 12);

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


scout.Table.prototype._triggerRowsDrawn = function(numRows) {
  var type = scout.Table.GUI_EVENT_ROWS_DRAWN;
  var event = {
    numRows: numRows
  };
  this.events.trigger(type, event);
};

scout.Table.prototype._triggerRowsSelected = function($rows, allSelected) {
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
  if (event.headerVisible !== undefined) {
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
