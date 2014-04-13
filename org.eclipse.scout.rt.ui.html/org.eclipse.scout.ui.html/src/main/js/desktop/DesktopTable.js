// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.DesktopTable = function(scout, model) {
  this.model = model;
  this.scout = scout;
  this._$desktopTable;
  this._$tableData;
  this._$tableDataScroll;
  this._$infoSelect;
  this._$infoFilter;
  this._$infoLoad;
  this._tableHeader;
  this.scout.widgetMap[model.table.id] = this;
};

Scout.DesktopTable.EVENT_ROWS_SELECTED = 'rowsSelected';
Scout.DesktopTable.EVENT_ROWS_INSERTED = 'rowsInserted';
Scout.DesktopTable.EVENT_ROW_CLICKED = 'rowClicked';
Scout.DesktopTable.EVENT_ROW_ACTION = 'rowAction';
Scout.DesktopTable.EVENT_SELECTION_MENUS_CHANGED = 'selectionMenusChanged';
Scout.DesktopTable.EVENT_MAP_LOADED = 'mapLoaded';
Scout.DesktopTable.EVENT_GRAPH_LOADED = 'graphLoaded';


Scout.DesktopTable.prototype.render = function($parent) {
  this._$parent = $parent;

  //create container
  this._$desktopTable = this._$parent.appendDiv('DesktopTable');

  var $tableHeader = this._$desktopTable.appendDiv('TableHeader'),
    $tableData = this._$desktopTable.appendDiv('TableData');
  this._$desktopTable.appendDiv('TableFooter');
  this._$tableControl = this._$desktopTable.appendDiv('TableControl');

  this._$tableDataScroll = $tableData.appendDiv('TableDataScroll');
  this._$tableData = $tableData;
  this._scrollbar = new Scout.Scrollbar(this._$tableDataScroll, 'y');

  this._$controlContainer = this._$tableControl.appendDiv('ControlContainer');
  this._$controlResizeTop = this._$tableControl.appendDiv('ControlResizeTop');
  this._$controlResizeBottom = this._$tableControl.appendDiv('ControlResizeBottom');

  var that = this;
  var $controlChart = this._$tableControl.appendDiv('ControlChart'),
    $controlGraph = this._$tableControl.appendDiv('ControlGraph'),
    $controlMap = this._$tableControl.appendDiv('ControlMap');

  this._$controlLabel = this._$tableControl.appendDiv('ControlLabel');

  this._$infoSelect = this._$tableControl.appendDiv('InfoSelect').on('click', '', toggleSelect);
  this._$infoFilter = this._$tableControl.appendDiv('InfoFilter').on('click', '', this.filterReset.bind(this));
  this._$infoLoad = this._$tableControl.appendDiv('InfoLoad').on('click', '', this._loadData.bind(this));

  // control buttons have mouse over effects
  $("body").on("mouseenter", "#control_graph, #control_chart, #control_map",
    function() {
      $('#control_label').text($(this).data('label'));
    });

  $("body").on("mouseleave", "#control_graph, #control_chart, #control_map",
    function() {
      $('#control_label').text('');
    });

  // create header
  this._tableHeader = new Scout.DesktopTableHeader(this, $tableHeader);

  // load data and create rows
  this._loadData();

  // update chart button
  if (this.model.chart) {
    $controlChart.data('label', this.model.chart.label)
      .hover(controlIn, controlOut)
      .click(controlClick)
      .click(controlChart);
  } else {
    $controlChart.addClass('disabled');
  }

  // update or disable graph button
  if (this.model.graph) {
    $controlGraph.data('label', this.model.graph.label)
      .hover(controlIn, controlOut)
      .click(controlClick)
      .click(controlGraph);
  } else {
    $controlGraph.addClass('disabled');
  }

  // update or disable map button
  if (this.model.map) {
    $controlMap.data('label', this.model.map.label)
      .hover(controlIn, controlOut)
      .click(controlClick)
      .click(controlMap);
  } else {
    $controlMap.addClass('disabled');
  }

  function controlIn(event) {
    that._controlIn($(event.target));
  }

  function controlOut(event) {
    that._controlOut($(event.target));
  }

  function controlClick(event) {
    var $clicked = $(this);

    // reset handling resize
    that._$controlResizeTop.off('mousedown');
    that._$controlResizeBottom.off('mousedown');

    if ($clicked.hasClass('selected')) {
      // classes: unselect and stop resizing
      $clicked.removeClass('selected');
      $clicked.parent().removeClass('resize-on');

      //adjust table
      that._$tableData.animateAVCSD('height',
        parseFloat(that._$desktopTable.css('height')) - 93,
        function() {
          $(this).css('height', 'calc(100% - 85px');
        },
        that._scrollbar.initThumb.bind(that._scrollbar),
        500);

      // visual: reset label and close control
      that._controlOut(event);
      that._$tableControl.animateAVCSD('height', 50, null, null, 500);

      // do not handle the click
      event.stopImmediatePropagation();
    }
  }

  function controlChart() {
    new Scout.DesktopTableChart(that.scout, that._$controlContainer, that);

    $(this).selectOne();
    that._showTableControl();
  }

  function controlGraph() {
    that.scout.send('graph', that.model.outlineId, {
      "nodeId": that.model.id
    });

    $(this).selectOne();
  }

  function controlMap() {
    that.scout.send('map', that.model.outlineId, {
      "nodeId": that.model.id
    });

    $(this).selectOne();
  }

  function toggleSelect() {
    var $selectedRows = $('.row-selected', that._$tableData);

    if ($selectedRows.length == that.model.table.rows.length) {
      $selectedRows.removeClass('row-selected');
    } else {
      $('.table-row', that._$tableData).addClass('row-selected');
    }

    that._drawSelectionBorder();
  }

};

Scout.DesktopTable.prototype._setInfoLoad = function(count) {
  this._$infoLoad.html(this._findInfo(count) + ' geladen</br>Daten neu laden');
  this._$infoLoad.show().widthToContent();
};

Scout.DesktopTable.prototype._setInfoMore = function( /*count*/ ) {};

Scout.DesktopTable.prototype._setInfoFilter = function(count, origin) {
  this._$infoFilter.html(this._findInfo(count) + ' gefiltert' + (origin ? ' durch ' + origin : '') + '</br>Filter entfernen');
  this._$infoFilter.show().widthToContent();
};

Scout.DesktopTable.prototype._setInfoSelect = function(count, all) {
  var allText = all ? 'Keine' : 'Alle';
  this._$infoSelect.html(this._findInfo(count) + ' selektiert</br>' + (allText) + ' selektieren');
  this._$infoSelect.show().widthToContent();
};

Scout.DesktopTable.prototype._findInfo = function(n) {
  if (n === 0) {
    return 'Keine Zeile';
  } else if (n == 1) {
    return 'Eine Zeile';
  } else {
    return n + ' Zeilen';
  }
};

Scout.DesktopTable.prototype._drawSelectionBorder = function() {
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
  if (this.model.table.rows) {
    rowCount = this.model.table.rows.length;
  }
  this._setInfoSelect($selectedRows.length, $selectedRows.length == rowCount);
};

Scout.DesktopTable.prototype._resetSelection = function() {
  $('.row-selected', this._$tableData).removeClass('row-selected');
  this._drawSelectionBorder();
  $('#RowMenu, #RowDrill, #RowMenuContainer').remove();
};

Scout.DesktopTable.prototype._sort = function() {
  var sortColumns = [];

  // remove selection
  this._resetSelection();

  // find all sort columns
  for (var c = 0; c < this.model.table.columns.length; c++) {
    var column = this.model.table.columns[c],
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
  this._$tableDataScroll.prepend($rows);

  // for less than 100 rows: move to old position and then animate
  if ($rows.length < 100) {
    $rows.each(function() {
      $(this).css('top', $(this).data('old-top') - $(this).offset().top)
        .animateAVCSD('top', 0);
    });
  }
};

Scout.DesktopTable.prototype.sortChange = function($header, dir, additional, remove) {
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

Scout.DesktopTable.prototype._loadData = function() {
  $('.table-row').remove();
  this._drawData(0);
  this._drawSelectionBorder();
};

Scout.DesktopTable.prototype._drawData = function(startRow) {
  // this function has to be fast
  var rowString = '';
  var table = this.model.table,
    that = this;

  var numRowsLoaded = startRow;
  if (table.rows && table.rows.length > 0) {
    for (var r = startRow; r < Math.min(table.rows.length, startRow + 100); r++) {
      var row = table.rows[r];

      var rowClass = 'table-row ';
      if (table.selectedRowIds && table.selectedRowIds.indexOf(row.id) > -1) {
        rowClass += 'row-selected ';
      }

      rowString += '<div id="' + row.id + '" class="' + rowClass + '" data-row=' + r + '>';

      for (var c = 0; c < row.cells.length; c++) {
        var column = table.columns[c],
          width = column.width,
          style = (width === 0) ? 'display: none; ' : 'width: ' + width + 'px; ',
          allign = (column.type == 'number') ? 'text-align: right; ' : '',
          value = this.getText(c, r);

        rowString += '<div style="' + style + allign + '">' + value + '</div>';
      }

      rowString += '</div>';
    }
    numRowsLoaded = r;

    // append block of rows
    $(rowString)
      .appendTo(this._$tableDataScroll)
      .on('mousedown', '', onMouseDown)
      .on('clicks', '', onClicks)
      .width(this._tableHeader.totalWidth + 4)
      .on('contextmenu', function(e) {
        e.preventDefault();
      });

  }

  // update info and scrollbar
  this._setInfoLoad(numRowsLoaded);
  this._scrollbar.initThumb();

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

      var $actionRow = $('.table-row', that._$tableData).slice(startIndex, endIndex);

      // set/remove selection
      if (add) {
        $actionRow.addClass('row-selected');
      } else {
        $actionRow.removeClass('row-selected');
      }

      // draw nice border
      that._drawSelectionBorder();

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

      // open and animate menu
      selectionMenu(event.pageX, event.pageY);

    }
  }

  function sendRowsSelected() {
    var rowIds = [],
      $selectedRows = $('.row-selected');

    $selectedRows.each(function() {
      rowIds.push($(this).attr('id'));
    });

    if (arrays.equalsIgnoreOrder(rowIds, that.model.table.selectedRowIds)) {
      return;
    }

    that.model.table.selectedRowIds = rowIds;
    if (that.model.table.selectedRowIds) {
      that.scout.send(Scout.DesktopTable.EVENT_ROWS_SELECTED, that.model.table.id, {
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
    that.scout.send(Scout.DesktopTable.EVENT_ROW_CLICKED, that.model.table.id, {
      "rowId": $row.attr('id')
    });
  }

  function onDoubleClick(event) {
    var $row = $(event.delegateTarget);
    sendRowAction($row);
  }

  function sendRowAction($row) {
    that.scout.send(Scout.DesktopTable.EVENT_ROW_ACTION, that.model.table.id, {
      "rowId": $row.attr('id')
    });
  }

  function selectionMenu(x, y) {
    // selection
    var $selectedRows = $('.row-selected'),
      $firstRow = $selectedRows.first();

    // make menu - if not already there
    var $RowDrill = $('#RowDrill');
    if ($RowDrill.length === 0) {
      $RowDrill = that._$tableDataScroll.appendDiv('RowDrill')
        .on('click', '', function() {
          sendRowAction($firstRow);
        });

      var h1 = $RowDrill.outerHeight();
      $RowDrill.height(0).animateAVCSD('height', h1, null, null, 75);
    }
    var $RowMenu = $('#RowMenu');
    if ($RowMenu.length === 0) {
      $RowMenu = that._$tableDataScroll.appendDiv('RowMenu')
        .on('click', '', clickRowMenu);

      var h2 = $RowMenu.outerHeight();
      $RowMenu.height(0).animateAVCSD('height', h2, null, null, 75);
    }

    // place menu
    // TODO cru: place on top if mouse movement goes up?
    var top = $selectedRows.last().offset().top - that._$tableDataScroll.offset().top + 32,
      left = Math.max(25, Math.min($firstRow.outerWidth() - 164, x - that._$tableDataScroll.offset().left - 13));

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

    if (event.button == 2) {
      clickRowMenu();
    }

    // TODO cru: remove events?

    function enterSelection (event) {
        $RowDrill.animateAVCSD('height', h1, null, null, 75);
        $RowMenu.animateAVCSD('height', h2, null, null, 75);
    }

    function leaveSelection (event) {
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

      var menus = that.model.table.selectionMenus;
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


      function onHoverIn () {
        $('#MenuButtonsLabel').text($(this).data('label'));
      }

      function onHoverOut () {
        $('#MenuButtonsLabel').text('');
      }

      function onMenuItemClicked () {
      }

      function removeMenu () {
        var $RowMenuContainer = $('#RowMenuContainer'),
          h = $RowMenuContainer.outerHeight();

        $RowMenuContainer.animateAVCSD('height', 0, $.removeThis);

        var t = parseInt($RowMenu.css('top'), 0);
        $RowMenu.css('top', t).animateAVCSD('top', t - h + 2);
      }
    }
  }
};

Scout.DesktopTable.prototype.getValue = function(col, row) {
  var cell = this.model.table.rows[row].cells[col];

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

Scout.DesktopTable.prototype.getText = function(col, row) {
  var cell = this.model.table.rows[row].cells[col];

  if (cell === null) { //cell may be a number so don't use !cell
    return '';
  }
  if (typeof cell !== 'object') {
    return cell;
  }
  return cell.text;
};

Scout.DesktopTable.prototype._group = function() {
  var that = this,
    table = this.model.table,
    all,
    groupIndex,
    $group = $('.group-sort', this._$desktopTable);

  // remove all sum rows
  $('.table-row-sum', this._$tableDataScroll).animateAVCSD('height', 0, $.removeThis, that._scrollbar.initThumb.bind(that._scrollbar));

  // find group type
  if ($('.group-all', this._$desktopTable).length) {
    all = true;
  } else if ($group.length) {
    groupIndex = $group.data('index');
  } else {
    return;
  }

  // prepare data
  var $rows = $('.table-row:visible', this._$tableDataScroll),
    $cols = $('.header-item', this._$desktopTable),
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
        .width(this._tableHeader.totalWidth + 4)
        .css('height', 0)
        .animateAVCSD('height', 34, null, that._scrollbar.initThumb.bind(that._scrollbar));

      $sumRow = $.makeDiv('', 'table-row-sum');
      sum = [];
    }
  }
};

Scout.DesktopTable.prototype.groupChange = function($header, draw, all) {
  $('.group-sort', this._$desktopTable).removeClass('group-sort');
  $('.group-all', this._$desktopTable).removeClass('group-all');

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

Scout.DesktopTable.prototype.colorData = function(mode, colorColumn) {
  var minValue,
    maxValue,
    colorFunc;

  for (var r = 0; r < this.model.table.rows.length; r++) {
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

  var $rows = $('.table-row:visible', this._$tableDataScroll),
    c;

  $('.header-item', this._$desktopTable).each(function(i) {
    if ($(this).data('index') == colorColumn) c = i;
  });

  for (var s = 0; s < $rows.length; s++) {
    var row = $rows.eq(s).data('row'),
      value = this.getValue(colorColumn, row);

    colorFunc($rows.eq(s).children().eq(c), value);

  }
};

Scout.DesktopTable.prototype.detach = function() {
  this._$desktopTable.detach();
};

Scout.DesktopTable.prototype.attach = function($container) {
  if (!this._$desktopTable) {
    this.render($container);
  } else {
    this._$desktopTable.appendTo($container);
  }
};

Scout.DesktopTable.prototype.insertRows = function(rows) {
  //always insert new rows at the end
  var table = this.model.table;
  if (table.rows) {
    table.rows.push.apply(table.rows, rows);
  } else {
    table.rows = rows;
  }
  if (this._$desktopTable) {
    this._loadData();
  }
};

Scout.DesktopTable.prototype.selectRowsByIds = function(rowIds) {
  var table = this.model.table;
  table.selectedRowIds = rowIds;

  if (this._$tableDataScroll) {
    this._resetSelection();

    //select rows
    for (var i = 0; i < rowIds.length; i++) {
      var rowId = rowIds[i];
      var $row = $('#' + rowId);
      $row.addClass('row-selected');
    }

    this._drawSelectionBorder();
  }

  if (!this.updateFromModelInProgress) {
    //not necessary for now since selectRowsByIds is only called by onModelAction, but does no harm either
    this.scout.send(Scout.DesktopTable.EVENT_ROWS_SELECTED, this.model.table.id, {
      "rowIds": rowIds
    });
  }
};

Scout.DesktopTable.prototype.findSelectedRows = function() {
  return this._$tableDataScroll.find('.row-selected');
};

Scout.DesktopTable.prototype._onSelectionMenusChanged = function(selectedRowIds, menus) {
  this.model.table.selectionMenus = menus;

  var $selectedRows = this.findSelectedRows();
  //FIXME see tree for reference
};

// filter function
Scout.DesktopTable.prototype.filter = function() {
  var that = this,
    rowCount = 0,
    origin = [],
    $allRows = $('.table-row', that._$tableDataScroll);

  that._resetSelection();
  $('.table-row-sum', this._$tableDataScroll).hide();

  $allRows.each(function() {
    var $row = $(this),
      rowText = $row.text().toLowerCase(),
      show = true;

    for (var i = 0; i < that.model.table.columns.length; i++) {
      if (that.model.table.columns[i].filterFunc) {
        show = show && that.model.table.columns[i].filterFunc($row);
      }
    }

    if (that.model.table.filter) {
      show = show && (rowText.indexOf(that.model.table.filter) > -1);
    }

    if (that.model.chart.filterFunc) {
      show = show && that.model.chart.filterFunc($row);
    }

    if (that.model.map.filterFunc) {
      show = show && that.model.map.filterFunc($row);
    }

    if (show) {
      that.showRow($row);
      rowCount++;
    } else {
      that.hideRow($row);
    }
  });

  // find info text
  for (var i = 0; i < that.model.table.columns.length; i++) {
    if (that.model.table.columns[i].filterFunc) {
      origin.push(that.model.table.columns[i].$div.text());
    }
  }

  if (that.model.table.filter) {
    origin.push(that.model.table.filter);
  }

  if (that.model.chart.filterFunc) {
    origin.push(that.model.chart.label);
  }

  if (that.model.map.filterFunc) {
    origin.push(that.model.map.label);
  }

  if (origin.length) {
    that._setInfoFilter(rowCount, origin.join(', '));
  } else {
    this._$infoFilter.animateAVCSD('width', 0, function() {
      $(this).hide();
    });
  }

  $(':animated', that._$tableDataScroll).promise().done(function() {
    that._group();
  });

};

Scout.DesktopTable.prototype.filterReset = function() {
  var that = this;

  // reset rows
  that._resetSelection();
  $('.table-row', that._$tableDataScroll).each(function() {
    that.showRow($(this));
  });
  that._group();

  // set back all filter functions
  for (var i = 0; i < this.model.table.columns.length; i++) {
    this.model.table.columns[i].filter = [];
    this.model.table.columns[i].filterFunc = null;
  }
  this.model.chart.filterFunc = null;
  this.model.map.filterFunc = null;
  this.model.table.filter = null;
  $('.main-chart.selected, .map-item.selected').removeClassSVG('selected');

  // hide info section
  this._$infoFilter.animateAVCSD('width', 0, function() {
    $(this).hide();
  });
};

Scout.DesktopTable.prototype.showRow = function($row) {
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
          that._scrollbar.initThumb();
        }
      });
  }
};

Scout.DesktopTable.prototype.hideRow = function($row) {
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
          that._scrollbar.initThumb();
        }
      });
  }
};

// move column

Scout.DesktopTable.prototype.moveColumn = function($header, oldPos, newPos, dragged) {
  var $headers = $('.header-item, .header-resize'),
    $moveHeader = $headers.eq(oldPos),
    $moveResize = $headers.eq(oldPos + 1);

  // store old position of header
  $headers.each(function() {
    $(this).data('old-pos', $(this).offset().left);
  });

  // change order in dom of header
  if (newPos < 0) {
    $('#TableHeader').prepend($moveResize);
    $('#TableHeader').prepend($moveHeader);
  } else {
    $headers.eq(newPos).after($moveHeader);
    $headers.eq(newPos).after($moveResize);
  }

  // move menu
  var left = $header.offset().left;

  $('#MenuHeaderTitle').animateAVCSD('left', left - 12);
  $('#MenuHeader').animateAVCSD('left', left - 12);

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

Scout.DesktopTable.prototype._controlIn = function($control) {
  var close = $control.hasClass('selected') ? ' schliessen' : '';
  this._$controlLabel.text($control.data('label') + close);
};

Scout.DesktopTable.prototype._controlOut = function() {
  this._$controlLabel.text('');
};

Scout.DesktopTable.prototype._showTableControl = function() {
  // allow resizing
  this._$tableControl.addClass('resize-on');

  //adjust table
  this._$tableData.animateAVCSD('height',
    parseFloat(this._$desktopTable.css('height')) - 444,
    function() {
      $(this).css('height', 'calc(100% - 430px');
    },
    this._scrollbar.initThumb.bind(this._scrollbar),
    500);

  // visual: update label, size container and control
  this._controlIn(this._$tableControl.children('.selected').first());
  this._$controlContainer.height(340);
  this._$tableControl.animateAVCSD('height', 400, null, null, 500);

  // set events for resizing
  this._$controlResizeTop.on('mousedown', '', resizeControl);
  this._$controlResizeBottom.on('mousedown', '', resizeControl);

  var that = this;
  function resizeControl(event) {
    $('body').addClass('row-resize')
      .on('mousemove', '', resizeMove)
      .one('mouseup', '', resizeEnd);

    var offset = (this.id == 'ControlResizeTop') ? 58 : 108;

    function resizeMove(event) {
      var h = that._$parent.outerHeight() - event.pageY + offset;
      if (that._$parent.height() < h + 50) return false;

      that._$tableControl.height(h);
      that._$tableData.height('calc(100% - ' + (h + 30) + 'px)');
      that._$controlContainer.height(h - 60);
      that._scrollbar.initThumb();
    }

    function resizeEnd() {
      if (that._$controlContainer.height() < 75) {
        $('.selected', that._$tableControl).click();
      }

      $('body').off('mousemove')
        .removeClass('row-resize');
    }

    return false;
  }
};

Scout.DesktopTable.prototype.onModelAction = function(event) {
  if (event.type_ == Scout.DesktopTable.EVENT_ROWS_INSERTED) {
    this.insertRows(event.rows);
  } else if (event.type_ == Scout.DesktopTable.EVENT_ROWS_SELECTED) {
    this.selectRowsByIds(event.rowIds);
  } else if (event.type_ == Scout.DesktopTable.EVENT_SELECTION_MENUS_CHANGED) {
    this._onSelectionMenusChanged(event.selectedRowIds, event.menus);
  } else if (event.type_ == Scout.DesktopTable.EVENT_MAP_LOADED) {
    new Scout.DesktopTableMap(this._$controlContainer, this, event.map);
    //FIXME select control button (only necessary if event is not triggered by user)
    this._showTableControl();
  } else if (event.type_ == Scout.DesktopTable.EVENT_GRAPH_LOADED) {
    new Scout.DesktopTableGraph(this._$controlContainer, event.graph);
    //FIXME select control button (only necessary if event is not triggered by user)
    this._showTableControl();
  } else {
    log("Model event not handled. Widget: DesktopTable. Event: " + event.type_ + ".");
  }
};
