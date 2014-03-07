// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.DesktopTable = function (scout, $parent, model) {
  this.model = model;
  // data and a row-pointer is used by many functions, may be very large
  this._table;
  this._$tableData;
  this._$tableDataScroll;
  this._$infoSelect;
  this._$infoFilter;
  this._$infoLoad;

  var that = this;

  //create container
  var $desktopTable = $parent.appendDiv('DesktopTable'),
    $tableHeader = $desktopTable.appendDiv('TableHeader'),
    $tableData = $desktopTable.appendDiv('TableData'),
    $tableFooter = $desktopTable.appendDiv('TableFooter'),
    $tableControl = $desktopTable.appendDiv('TableControl');
  this._$tableData = $tableData;

  var $tableDataScroll = $tableData.appendDiv('TableDataScroll'),
    scrollbar = new Scout.Scrollbar($tableDataScroll, 'y');
  this._$tableDataScroll = $tableDataScroll;

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
  this._$infoLoad = $tableControl.appendDiv('InfoLoad').on('click', '', loadData);

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
  var tableHeader = new Scout.DesktopTableHeader(this, $tableHeader, model.table.columns);

  // load data and create rows
  loadData();

  // update chart button
  if(model.chart) {
    $controlChart.data('label', model.chart.label)
      .hover(controlIn, controlOut)
      .click(controlClick)
      .click(controlChart);
  } else {
    $controlChart.addClass('disabled');
  }

  // update or disable graph button
  if (model.graph) {
    $controlGraph.data('label', model.graph.label)
      .hover(controlIn, controlOut)
      .click(controlClick)
      .click(controlGraph);
  } else {
    $controlGraph.addClass('disabled');
  }

  // update or disable map button
  if (model.map) {
    $controlMap.data('label', model.map.label)
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
  function controlIn (event) {
    var close = $(event.target).hasClass('selected') ? ' ' + textClose : '';
    $controlLabel.text($(event.target).data('label') + close);
  }

  function controlOut (event) {
    $controlLabel.text('');
  }

  function controlClick (event) {
    var $clicked = $(this);

    // reset handling resize
    $controlResizeTop.off('mousedown');
    $controlResizeBottom.off('mousedown');

    if ($clicked.hasClass('selected')) {
      // classes: unselect and stop resizing
      $clicked.removeClass('selected');
      $clicked.parent().removeClass('resize-on');

      //adjust table
      $tableData.animateAVCSD('height',
        parseFloat($desktopTable.css('height')) - 80,
        function () {$(this).css('height', 'calc(100% - 80px'); },
        scrollbar.initThumb.bind(scrollbar),
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
      $tableData.animateAVCSD('height',
        parseFloat($desktopTable.css('height')) - 430,
        function () {$(this).css('height', 'calc(100% - 430px'); },
        scrollbar.initThumb.bind(scrollbar),
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

  function controlChart (event) {
    new Scout.DesktopTableChart(scout, $controlContainer, model.table.columns, that._table, filterCallback);
  }

  function controlGraph (event) {
    new Scout.DesktopTableGraph(scout, $controlContainer, model);
  }

  function controlMap (event) {
    new Scout.DesktopTableMap(scout, $controlContainer, model, that._table, filterCallback);
  }

  function controlOrganize (event) {
    new Scout.DesktopTableOrganize(scout, $controlContainer, model, model.table.columns, that);
  }

  function resizeControl (event) {
    $('body').addClass('row-resize')
      .on('mousemove', '', resizeMove)
      .one('mouseup', '', resizeEnd);

    var offset = (this.id == 'ControlResizeTop')  ? 58 : 108;

    function resizeMove(event){
      var h = $parent.outerHeight() - event.pageY + offset;
      if ($parent.height() < h + 50) return false;

      $tableControl.height(h);
      $tableData.height('calc(100% - ' + (h + 30) + 'px)');
      $controlContainer.height(h - 60);
      scrollbar.initThumb();
    }

    function resizeEnd(event){
      if ($controlContainer.height() < 50) {
        $('.selected', $tableControl).click();
      }

      $('body').off('mousemove')
        .removeClass('row-resize');
    }

    return false;
  }

  function loadData () {
    var response = scout.sendSync('table', model.outlineId, {"nodeId":model.id});
    that._table = response.events[0].rows;
    $('.table-row').remove();
    drawData(0);
    that._setInfoSelect(0, false);
  }

  function drawData (startRow) {
    // this function has to be fast
    var rowString = '';

    for (var r = startRow; r < Math.min(that._table.length, startRow + 100); r++) {
      var row = that._table[r];

      rowString += '<div class="table-row">';

      for (var c = 0; c < row.length; c++) {
        var column = model.table.columns[c],
          width = column.width,
          style = (width === 0) ? 'display: none; ' : 'width: ' + width + 'px; ',
          allign = (column.type == 'number') ? 'text-align: right; ' : '';
          value = Scout.DesktopMatrix.getCellText(row[c]);

        rowString += '<div style = "' + style + allign + '">' + value + '</div>';
      }

      rowString += '</div>';
    }

    // append block of rows
    $(rowString)
      .appendTo($tableDataScroll)
      .on('mousedown', '', clickData)
      .width(tableHeader.totalWidth + 4);

    // update info and scrollbar
    that._setInfoLoad(r);
    scrollbar.initThumb();

    // repaint and append next block
    if (r < that._table.length) {
      setTimeout(function() { drawData(startRow + 100); }, 0);
    }
  }

  function clickData (event) {
    var $row = $(event.delegateTarget),
      add = true,
      first;

    // click without ctrl always starts new selection, with ctrl toggle
    if (event.shiftKey) {
      first = $('.row-selected').first().index();
    } else if (event.ctrlKey) {
      add = !$row.hasClass('row-selected');
    } else {
      $('.row-selected').removeClass('row-selected');
    }

    // just a click...
    selectData(event);

    // ...or movement with held mouse button
    $(".table-row").one("mousemove", function(event){
      selectData(event);
    });

    // remove all events
    $(".table-row").one("mouseup", function(event){
      $(".table-row").unbind("mousemove");
    });

    // action for all affected rows
    function selectData (event) {
      // affected rows between $row and Target
      var firstIndex = first || $row.index(),
        lastIndex = $(event.delegateTarget).index();

      var startIndex = Math.min(firstIndex, lastIndex),
        endIndex = Math.max(firstIndex, lastIndex) + 1;

      log(firstIndex, lastIndex);

      var $actionRow = $('.table-row', $tableData).slice(startIndex, endIndex);

      // set/remove selection
      if (add) {
        $actionRow.addClass('row-selected');
      } else {
        $actionRow.removeClass('row-selected');
      }

      // draw nice border
      that._selectionBorder();

      // open and animate menu
      selectionMenu(event.pageX, event.pageY);
    }
  }

  function selectionMenu (x, y) {
    // selection
    $rowSelected = $('.row-selected');

    //FIXME added by cgu to make sure clickRowMenu is registered for every instance of DesktopTable
    $('#MenuRow').remove();

    // make menu - if not already there
    var $menuRow = $('#MenuRow');
    if ($menuRow.length === 0) {
      $menuRow = $('body').appendDiv('MenuRow')
        .on('click', '', clickRowMenu);
    }
    // place menu top-left
    $menuRow.css('left', $rowSelected.first().offset().left - 13)
      .css('top', $rowSelected.first().offset().top - 13);

    // move to the mouse pointer
    var moveMenu = function (event) {
      var top = $rowSelected.first().offset().top,
        bottom = $rowSelected.last().offset().top + 32;

      var toTop = Math.abs(top - y) < Math.abs(bottom - y) ? top - 13: bottom - 13,
        toLeft = x - 13;

      $menuRow.stop().animate({'top': toTop},
          {complete: function() {$menuRow.animate({'left': toLeft}, 500); }},
          500);
    };

    // start movement
    moveMenu(event);
  }

  function clickRowMenu (event) {
    var $clicked = $(this),
      nodeId = model.id,
      x = $clicked.offset().left,
      y = $clicked.offset().top;

    new Scout.Menu(scout, model.outlineId, nodeId, x, y);
  }

  function toggleSelect () {
    var $rowSelected = $('.row-selected', $tableData);

    if ($rowSelected.length == that._table.length) {
      $rowSelected.removeClass('row-selected');
    } else {
      $('.table-row', $tableData).addClass('row-selected');
    }

    that._selectionBorder();
  }

  function filterCallback (testFunc) {
    var rowCount = 0,
      $rowSelected = $('.row-selected', $tableData),
      $allRows = $('.table-row', that._$tableDataScroll);

    that._resetSelection();

    $allRows.detach();
    $allRows.each(function (i) {
      var $row = $(this),
        show = testFunc($row);

      if (show){
        showRow($row);
        rowCount++;
      } else {
        hideRow($row);
      }
      scrollbar.initThumb();
    });

    that._setInfoFilter(rowCount);
    $allRows.appendTo(that._$tableDataScroll);
    scrollbar.initThumb();
  }

  function resetFilter (event) {
    $('.table-row', $tableData).each(function (i) { showRow($(this)); });
    that._$infoFilter.animateAVCSD('width', 0, function () {$(this).hide(); });
    $('.main-chart.selected, .map-item.selected').removeClassSVG('selected');
    log($('.main-chart.selected'));
    that._resetSelection();
  }

  function showRow ($row) {
      $row.show()
        .animate({'height': '34', 'padding-top': '2', 'padding-bottom': '2'});
  }

  function hideRow ($row) {
      $row.hide()
        .animate({'height': '0', 'padding-top': '0', 'padding-bottom': '0'},
          {complete: function() {$(this).hide();}});
  }

};

Scout.DesktopTable.prototype._setInfoLoad = function (count) {
  this._$infoLoad.html(this._findInfo(count) + ' geladen</br>Daten neu laden');
  this._$infoLoad.show().widthToContent();
};

Scout.DesktopTable.prototype._setInfoMore = function (count) {
};

Scout.DesktopTable.prototype._setInfoFilter = function (count) {
  this._$infoFilter.html(this._findInfo(count) + ' gefiltert</br>Filter entfernen');
  this._$infoFilter.show().widthToContent();
};

Scout.DesktopTable.prototype._setInfoSelect = function (count, all) {
  var allText = all ? 'Keine' : 'Alle';
  this._$infoSelect.html(this._findInfo(count) + ' selektiert</br>' + (allText) + ' selektieren');
  this._$infoSelect.show().widthToContent();
};

Scout.DesktopTable.prototype._findInfo = function (n) {
  if (n === 0 ) {
    return 'Keine Zeile';
  } else if (n == 1) {
    return 'Eine Zeile';
  } else {
    return n + ' Zeilen';
  }
};

Scout.DesktopTable.prototype._selectionBorder = function () {
  // remove nice border
    $('.select-middle, .select-top, .select-bottom, .select-single')
      .removeClass('select-middle select-top select-bottom select-single');

  // draw nice border
  $rowSelected = $('.row-selected');
  $rowSelected.each(function (i) {
    var hasPrev = $(this).prevAll(':visible:first').hasClass('row-selected'),
      hasNext = $(this).nextAll(':visible:first').hasClass('row-selected');

    if (hasPrev && hasNext) $(this).addClass('select-middle');
    if (!hasPrev && hasNext) $(this).addClass('select-top');
    if (hasPrev && !hasNext) $(this).addClass('select-bottom');
    if (!hasPrev && !hasNext) $(this).addClass('select-single');
  });

  // show count
  this._setInfoSelect($rowSelected.length, $rowSelected.length == this._table.length);
};

Scout.DesktopTable.prototype._resetSelection = function () {
  $('.row-selected', this._$tableData).removeClass('row-selected');
  this._selectionBorder();
  $('#MenuRow').remove();
};

Scout.DesktopTable.prototype._sort = function () {
  var sortColumns = [];

  // remove selection
  this._resetSelection();

  // find all sort columns
  for (var c = 0; c < this.model.table.columns.length; c++) {
    var column = this.model.table.columns[c],
      order = column.$div.data('sort-order'),
      dir =  column.$div.hasClass('sort-up') ? 'up' : (order >= 0 ? 'down' : '');
      sortColumns[order] = {index : c, dir : dir};
  }

  // compare rows
  function compare (a, b) {
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
  $rows.each(function () {
    $(this).data('old-top', $(this).offset().top);
  });

  // change order in dom
  $rows = $rows.sort(compare);
  this._$tableDataScroll.append($rows);

  // for less than 100 rows: move to old position and then animate
  if ($rows.length < 100) {
    $rows.each(function (i) {
      $(this).css('top', $(this).data('old-top') - $(this).offset().top)
        .animateAVCSD('top', 0);
      });
    }
};


Scout.DesktopTable.prototype.sortChange = function  (index, dir, additional) {
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

