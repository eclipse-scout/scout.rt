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

Scout.DesktopTable.prototype.render = function($parent) {
  this._$parent = $parent;

  //create container
  this._$desktopTable = this._$parent.appendDiv('DesktopTable');

  var $tableHeader = this._$desktopTable.appendDiv('TableHeader'),
    $tableData = this._$desktopTable.appendDiv('TableData');
  this._$desktopTable.appendDiv('TableFooter');
  var $tableControl = this._$desktopTable.appendDiv('TableControl');

  this._$tableDataScroll = $tableData.appendDiv('TableDataScroll');
  this._$tableData = $tableData;
  this._scrollbar = new Scout.Scrollbar(this._$tableDataScroll, 'y');

  var that = this;
  var $controlContainer = $tableControl.appendDiv('ControlContainer'),
    $controlResizeTop = $tableControl.appendDiv('ControlResizeTop'),
    $controlResizeBottom = $tableControl.appendDiv('ControlResizeBottom'),
    $controlChart = $tableControl.appendDiv('ControlChart'),
    $controlGraph = $tableControl.appendDiv('ControlGraph'),
    $controlMap = $tableControl.appendDiv('ControlMap'),
    $controlOrganize = $tableControl.appendDiv('ControlOrganize'),
    $controlLabel = $tableControl.appendDiv('ControlLabel');

  this._$infoSelect = $tableControl.appendDiv('InfoSelect').on('click', '', toggleSelect);
  this._$infoFilter = $tableControl.appendDiv('InfoFilter').on('click', '', resetFilter);
  this._$infoLoad = $tableControl.appendDiv('InfoLoad').on('click', '', this._loadData.bind(this));

  // control buttons have mouse over effects
  $("body").on("mouseenter", "#control_graph, #control_chart, #control_map, #control_organise",
    function() {
      $('#control_label').text($(this).data('label'));
    });

  $("body").on("mouseleave", "#control_graph, #control_chart, #control_map, #control_organise",
    function() {
      $('#control_label').text('');
    });

  // create header
  this._tableHeader = new Scout.DesktopTableHeader(this, $tableHeader, this.model.table.columns);

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

  // organize button
  var textOrganize = 'Spaltenverwaltung',
    textClose = 'schliessen';
  $controlOrganize.data('label', textOrganize)
    .hover(controlIn, controlOut)
    .click(controlClick)
    .click(controlOrganize);

  // named functions
  function controlIn(event) {
    var close = $(event.target).hasClass('selected') ? ' ' + textClose : '';
    $controlLabel.text($(event.target).data('label') + close);
  }

  function controlOut() {
    $controlLabel.text('');
  }

  function controlClick(event) {
    var $clicked = $(this);

    // reset handling resize
    $controlResizeTop.off('mousedown');
    $controlResizeBottom.off('mousedown');

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
      controlOut(event);
      $tableControl.animateAVCSD('height', 50, null, null, 500);

      // do not handel the click
      event.stopImmediatePropagation();
    } else {
      // classes: select and allow resizing
      $clicked.selectOne();
      $clicked.parent().addClass('resize-on');

      //adjust table
      that._$tableData.animateAVCSD('height',
        parseFloat(that._$desktopTable.css('height')) - 444,
        function() {
          $(this).css('height', 'calc(100% - 430px');
        },
        that._scrollbar.initThumb.bind(that._scrollbar),
        500);

      // visual: update label, size container and control
      controlIn(event);
      $controlContainer.height(340);
      $tableControl.animateAVCSD('height', 400, null, null, 500);

      // set events for resizing
      $controlResizeTop.on('mousedown', '', resizeControl);
      $controlResizeBottom.on('mousedown', '', resizeControl);
    }
  }

  function controlChart() {
    new Scout.DesktopTableChart(that.scout, $controlContainer, that.model.table, filterCallback);
  }

  function controlGraph() {
    new Scout.DesktopTableGraph(that.scout, $controlContainer, that.model);
  }

  function controlMap() {
    new Scout.DesktopTableMap(that.scout, $controlContainer, that.model, that.model.table, filterCallback);
  }

  function controlOrganize() {
    new Scout.DesktopTableOrganize(that.scout, $controlContainer, that.model, that.model.table.columns, that);
  }

  function resizeControl() {
    $('body').addClass('row-resize')
      .on('mousemove', '', resizeMove)
      .one('mouseup', '', resizeEnd);

    var offset = (this.id == 'ControlResizeTop') ? 58 : 108;

    function resizeMove(event) {
      var h = that._$parent.outerHeight() - event.pageY + offset;
      if (that._$parent.height() < h + 50) return false;

      $tableControl.height(h);
      that._$tableData.height('calc(100% - ' + (h + 30) + 'px)');
      $controlContainer.height(h - 60);
      that._scrollbar.initThumb();
    }

    function resizeEnd() {
      if ($controlContainer.height() < 75) {
        $('.selected', $tableControl).click();
      }

      $('body').off('mousemove')
        .removeClass('row-resize');
    }

    return false;
  }

  function toggleSelect() {
    var $selectedRows = $('.row-selected', $tableData);

    if ($selectedRows.length == that.model.table.rows.length) {
      $selectedRows.removeClass('row-selected');
    } else {
      $('.table-row', $tableData).addClass('row-selected');
    }

    that._drawSelectionBorder();
  }

  function filterCallback(testFunc) {
    var rowCount = 0,
      $allRows = $('.table-row', that._$tableDataScroll);

    that._resetSelection();

    $allRows.detach();
    $allRows.each(function() {
      var $row = $(this),
        show = testFunc($row);

      if (show) {
        showRow($row);
        rowCount++;
      } else {
        hideRow($row);
      }
      that._scrollbar.initThumb();
    });

    that._setInfoFilter(rowCount);
    $allRows.appendTo(that._$tableDataScroll);
    that._scrollbar.initThumb();
  }

  function resetFilter() {
    $('.table-row', $tableData).each(function() {
      showRow($(this));
    });
    that._$infoFilter.animateAVCSD('width', 0, function() {
      $(this).hide();
    });
    $('.main-chart.selected, .map-item.selected').removeClassSVG('selected');
    that._resetSelection();
  }

  function showRow($row) {
    $row.show()
      .animate({
        'height': '34',
        'padding-top': '2',
        'padding-bottom': '2'
      });
  }

  function hideRow($row) {
    $row.hide()
      .animate({
        'height': '0',
        'padding-top': '0',
        'padding-bottom': '0'
      }, {
        complete: function() {
          $(this).hide();
        }
      });
  }

};

Scout.DesktopTable.prototype._setInfoLoad = function(count) {
  this._$infoLoad.html(this._findInfo(count) + ' geladen</br>Daten neu laden');
  this._$infoLoad.show().widthToContent();
};

Scout.DesktopTable.prototype._setInfoMore = function( /*count*/ ) {};

Scout.DesktopTable.prototype._setInfoFilter = function(count) {
  this._$infoFilter.html(this._findInfo(count) + ' gefiltert</br>Filter entfernen');
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
  $('#MenuRow').remove();
};

Scout.DesktopTable.prototype._sort = function() {
  var sortColumns = [];

  // remove selection
  this._resetSelection();

  // find all sort columns
  for (var c = 0; c < this.model.table.columns.length; c++) {
    var column = this.model.table.columns[c],
      order = column.$div.data('sort-order'),
      dir = column.$div.hasClass('sort-up') ? 'up' : (order >= 0 ? 'down' : '');
    sortColumns[order] = {
      index: c,
      dir: dir
    };
  }

  // compare rows
  function compare(a, b) {
    for (var s = 0; s < sortColumns.length; s++) {
      var index = sortColumns[s].index,
        dir = sortColumns[s].dir == 'up' ? -1 : 1;

      if (a.children[index].innerHTML < b.children[index].innerHTML) {
        return dir;
      } else if (a.children[index].innerHTML > b.children[index].innerHTML) {
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

Scout.DesktopTable.prototype.sortChange = function(index, dir, additional) {
  // find new sort direction
  var $header = $('.header-item').eq(index);

  // change sort order of clicked header
  $header.removeClass('sort-up sort-down')
    .addClass('sort-' + dir);

  // when shift pressed: add, otherwise reset
  if (additional) {
    var clickOrder = $header.data('sort-order'),
      maxOrder = -1,
      newOrder;

    $('.header-item').each(function() {
      var value = $(this).data('sort-order');
      maxOrder = (value > maxOrder) ? value : maxOrder;
    });

    /*jshint -W041:false*/
    if (clickOrder != undefined) {
      newOrder = clickOrder;
    } else if (maxOrder > -1) {
      newOrder = maxOrder + 1;
    } else {
      newOrder = 0;
    }

    $header.data('sort-order', newOrder);

  } else {
    $header.data('sort-order', 0)
      .siblings()
      .removeClass('sort-up sort-down')
      .data('sort-order', null);
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

      rowString += '<div id="' + row.id + '" class="' + rowClass + '">';

      for (var c = 0; c < row.cells.length; c++) {
        var column = table.columns[c],
          width = column.width,
          style = (width === 0) ? 'display: none; ' : 'width: ' + width + 'px; ',
          allign = (column.type == 'number') ? 'text-align: right; ' : '',
          value = Scout.DesktopMatrix.getCellText(row.cells[c]);

        rowString += '<div style="' + style + allign + '">' + value + '</div>';
      }

      rowString += '</div>';
    }
    numRowsLoaded = r;

    // append block of rows
    $(rowString)
      .appendTo(this._$tableDataScroll)
      .on('mousedown', '', onMouseDown)
      .width(this._tableHeader.totalWidth + 4);
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
      $selectedRows.removeClass('row-selected');
    }

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

      // open and animate menu
      selectionMenu(event.pageX, event.pageY);

      //FIXME currently also set if selection hasn't changed (same row clicked again). maybe optimize
      selectionChanged = true;
    }

    function onMouseUp(event) {
      $(".table-row").unbind("mousemove");
      $(".table-row").unbind("mouseup");

      //Send click only if mouseDown and mouseUp happened on the same row
      if ($row.get(0) == event.delegateTarget) {
        that.scout.send(Scout.DesktopTable.EVENT_ROW_CLICKED, that.model.table.id, {
          "rowId": $row.attr('id')
        });
      }

      if (selectionChanged) {
        var rowIds = [],
          $selectedRows = $('.row-selected');

        $selectedRows.each(function() {
          rowIds.push($(this).attr('id'));
        });

        that.scout.send(Scout.DesktopTable.EVENT_ROWS_SELECTED, that.model.table.id, {
          "rowIds": rowIds
        });
      }
    }
  }

  function selectionMenu(x, y) {
    // selection
    var $selectedRows = $('.row-selected');

    //FIXME added by cgu to make sure clickRowMenu is registered for every instance of DesktopTable
    $('#MenuRow').remove();

    // make menu - if not already there
    var $menuRow = $('#MenuRow');
    if ($menuRow.length === 0) {
      $menuRow = $('body').appendDiv('MenuRow')
        .on('click', '', clickRowMenu);
    }
    // place menu top-left
    $menuRow.css('left', $selectedRows.first().offset().left - 13)
      .css('top', $selectedRows.first().offset().top - 13);

    // move to the mouse pointer
    var moveMenu = function() {
      var top = $selectedRows.first().offset().top,
        bottom = $selectedRows.last().offset().top + 32;

      var toTop = Math.abs(top - y) < Math.abs(bottom - y) ? top - 13 : bottom - 13,
        toLeft = x - 13;

      $menuRow.stop().animate({
          'top': toTop
        }, {
          complete: function() {
            $menuRow.animate({
              'left': toLeft
            }, 500);
          }
        },
        500);
    };

    // start movement
    moveMenu(event);
  }

  function clickRowMenu() {
    var $clicked = $(this),
      $selectedRows = $('.row-selected'),
      x = $clicked.offset().left,
      y = $clicked.offset().top,
      emptySpace = $selectedRows.length === 0;

    new Scout.Menu(that.scout, that.model.table.id, emptySpace, x, y);
  }

};

Scout.DesktopTable.prototype.sumData = function(draw, groupColumn) {
  $('.table-row-sum', this._$tableDataScroll).animateAVCSD('height', 0, $.removeThis);

  var table = this.model.table;
  if (draw) {
    var $rows = $('.table-row', this._$tableDataScroll);
    var $sumRow = $.makeDiv('', 'table-row-sum'),
      sum = [];

    for (var r = 0; r < $rows.length; r++) {
      var $cells = $rows.eq(r).children();

      for (var c = 0; c < table.columns.length; c++) {
        var value = $cells.eq(c).text();
        if (table.columns[c].type == 'number') {
          sum[c] = (sum[c] || 0) + parseFloat(value);
        }
      }

      if (($cells.eq(groupColumn).text() != $rows.eq(r + 1).children().eq(groupColumn).text() ||
        (r == $rows.length - 1)) && sum.length > 0) {
        for (c = 0; c < table.columns.length; c++) {
          var $div;

          if (typeof sum[c] == 'number') {
            $div = $.makeDiv('', '', sum[c])
              .css('text-align', 'right');
          } else if (c == groupColumn) {
            $div = $.makeDiv('', '', $cells.eq(groupColumn).text())
              .css('text-align', 'left');
          } else {
            $div = $.makeDiv('', '', '&nbsp');
          }

          $div.appendTo($sumRow).width(table.columns[c].width);
        }

        $sumRow.insertAfter($rows.eq(r))
          .width(this._tableHeader.totalWidth + 4)
          .css('height', 0)
          .animateAVCSD('height', 34);

        $sumRow = $.makeDiv('', 'table-row-sum');
        sum = [];
      }
    }
  }

  // update scrollbar
  this._scrollbar.initThumb();
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

Scout.DesktopTable.prototype.onModelAction = function(event) {
  if (event.type_ == Scout.DesktopTable.EVENT_ROWS_INSERTED) {
    this.insertRows(event.rows);
  } else if (event.type_ == Scout.DesktopTable.EVENT_ROWS_SELECTED) {
    this.selectRowsByIds(event.rowIds);
  } else {
    log("Model event not handled. Widget: DesktopTable. Event: " + event.type_ + ".");
  }
};
