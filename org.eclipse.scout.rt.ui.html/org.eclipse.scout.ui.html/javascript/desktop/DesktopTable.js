// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.DesktopTable = function (scout, $bench, model) {
  var textOrganize = 'Spaltenverwaltung',
    textClose = 'schliessen';

  //create container
  var $desktopTable = $bench.appendDiv('DesktopTable'),
    $tableHeader = $desktopTable.appendDiv('TableHeader'),
    $tableData = $desktopTable.appendDiv('TableData'),
    $tableFooter = $desktopTable.appendDiv('TableFooter'),
    $tableControl = $desktopTable.appendDiv('TableControl');

  var $tableDataScroll = $tableData.appendDiv('TableDataScroll');
    scrollbar = new Scout.Scrollbar($tableDataScroll, 'y');

  var $controlResizeTop = $tableControl.appendDiv('ControlResizeTop'),
    $controlResizeBottom = $tableControl.appendDiv('ControlResizeBottom');

  var  $controlChart = $tableControl.appendDiv('ControlChart'),
    $controlGraph = $tableControl.appendDiv('ControlGraph'),
    $controlMap = $tableControl.appendDiv('ControlMap'),
    $controlOrganize = $tableControl.appendDiv('ControlOrganize'),
    $controlLabel = $tableControl.appendDiv('ControlLabel');

  var $controlContainer = $tableControl.appendDiv('ControlContainer');

  var $infoSelect = $tableControl.appendDiv('InfoSelect').on('click', '', toggleSelect),
    $infoFilter = $tableControl.appendDiv('InfoFilter').on('click', '', resetFilter),
    $infoMore = $tableControl.appendDiv('InfoMore'),
    $infoLoad = $tableControl.appendDiv('InfoLoad').on('click', '', loadData);

  // control buttons has mouse over effects
  $("body").on("mouseenter", "#control_graph, #control_chart, #control_map, #control_organise",
    function() {
      $('#control_label').text($(this).data('label'));
    });

  $("body").on("mouseleave", "#control_graph, #control_chart, #control_map, #control_organise",
    function() {
      $('#control_label').text('');
    });

  // data and a row-pointer is used by many functions, may be very large
  var table;

  // create header
  var tableHeader = new Scout.DesktopTableHeader($tableHeader, model.columns);

  // load data and create rows
  loadData();

  // update chart button
  $controlChart.data('label', model.chart)
    .hover(controlIn, controlOut)
    .click(controlClick)
    .click(controlChart);

  // update or disable graph button
  if (model.graph) {
    $controlGraph.data('label', model.graph)
      .hover(controlIn, controlOut)
      .click(controlClick)
      .click(controlGraph);
  } else {
    $controlGraph.addClass('disabled');
  }

  // update or disable map button
  if (model.map) {
    $controlMap.data('label', model.map)
      .hover(controlIn, controlOut)
      .click(controlClick)
      .click(controlMap);
  } else {
    $controlMap.addClass('disabled');
  }

  // organize button
  $controlOrganize.data('label', textOrganize)
    .hover(controlIn, controlOut)
    .click(controlClick)
    .click(controlOrganize);

  // named funktions

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
        scrollbar.initThumb,
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
        scrollbar.initThumb,
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
    new Scout.DesktopTableChart(scout, $controlContainer, model.columns, table, filterCallback);
  }

  function controlGraph (event) {
    new Scout.DesktopTableGraph(scout, $controlContainer, model);
  }

  function controlMap (event) {
    new Scout.DesktopTableMap(scout, $controlContainer, model, table, filterCallback);
  }

  function controlOrganize (event) {
    new Scout.DesktopTableOrganize(scout, $controlContainer, model, model.columns, table);
  }

  function resizeControl (event) {
    $('body').addClass('row-resize')
      .on('mousemove', '', resizeMove)
      .one('mouseup', '', resizeEnd);

    var offset = (this.id == 'ControlResizeTop')  ? 58 : 108;

    function resizeMove(event){
      var h = $bench.outerHeight() - event.pageY + offset;
      $tableControl.height(h);
      $tableData.height('calc(100% - ' + (h + 30) + 'px)');
      $controlContainer.height(h - 60);
      scrollbar.initThumb();
    }

    function resizeEnd(event){
      $('body').off('mousemove')
        .removeClass('row-resize');
    }

    return false;
  }

  function loadData () {
    var response = scout.syncAjax('table', model.outlineId, {"nodeId":model.nodeId});
    table = response.events[0].rows;
    $('.table-row').remove();
    drawData(0);
    setInfoSelect(0, false);
  }

  function drawData (startRow) {
    // this function hast to be fast
    var rowString = '';

    for (var r = startRow; r < Math.min(table.length, startRow + 100); r++) {
      var row = table[r];

      rowString += '<div class="table-row">';
      for (var c = 0; c < row.length; c++) {
        var width = model.columns[c].width,
          style = (width === 0) ? 'display: none' : 'width: ' + width + 'px';
        rowString += '<div style = "' + style + ';">' + row[c] + '</div>';
      }

      rowString += '</div>';
    }

    // append block of rows
    $(rowString)
      .appendTo($tableDataScroll)
      .on('mousedown', '', clickData)
      .width(tableHeader.totalWidth + 4);

    // update info and scrollbar
    setInfoLoad(r);
    scrollbar.initThumb();

    // repaint and append next block
    if (r < table.length) {
      setTimeout(function() { drawData(startRow + 100); }, 0);
    }
  }

  function clickData (event) {
    var $row = $(event.delegateTarget),
      add = true;

    // click without ctrl always starts new selection, with ctrl toggle
    if (event.ctrlKey) {
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
      var firstIndex = $row.index(),
        lastIndex = $(event.delegateTarget).index();

      var startIndex = Math.min(firstIndex, lastIndex),
        endIndex = Math.max(firstIndex, lastIndex) + 1;

      var $actionRow = $('.table-row', $tableData).slice(startIndex, endIndex);

      // set/remove selection
      if (add) {
        $actionRow.addClass('row-selected');
      } else {
        $actionRow.removeClass('row-selected');
      }

      // draw nice border
      selectionBorder();

      // open and animate menu
      selectionMenu(event.pageX, event.pageY);
    }
  }

  function selectionBorder () {
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
    setInfoSelect($rowSelected.length, $rowSelected.length == table.length);
  }

  function selectionMenu (x, y) {
    // selection
    $rowSelected = $('.row-selected');

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
      id = model.id,
      x = $clicked.offset().left,
      y = $clicked.offset().top;

    new Scout.Menu(scout, model.outlineId, id, x, y);
  }

  function toggleSelect () {
    var $rowSelected = $('.row-selected', $tableData);

    if ($rowSelected.length == table.length) {
      $rowSelected.removeClass('row-selected');
    } else {
      $('.table-row', $tableData).addClass('row-selected');
    }

    selectionBorder();
  }

  function filterCallback (testFunc) {
    var rowCount = 0,
      $rowSelected = $('.row-selected', $tableData),
      $allRows = $('.table-row', $tableDataScroll);

    $rowSelected.removeClass('row-selected');
    selectionBorder();

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

    setInfoFilter(rowCount);
    $allRows.appendTo($tableDataScroll);
    scrollbar.initThumb();
  }

  function resetFilter (event) {
    $('.table-row', $tableData).each(function (i) { showRow($(this)); });
    $infoFilter.animateAVCSD('width', 0, function () {$(this).hide(); });
    $('.main-chart.selected').removeClassSVG('selected');
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

  function setInfoLoad (count) {
    $infoLoad.html(findInfo(count) + ' geladen</br>Daten neu laden');
    $infoLoad.show().widthToContent();
  }

  function setInfoMore (count) {
  }

  function setInfoFilter (count) {
    $infoFilter.html(findInfo(count) + ' gefiltert</br>Filter entfernen');
    $infoFilter.show().widthToContent();
  }

  function setInfoSelect (count, all) {
    var allText = all ? 'Keine' : 'Alle';
    $infoSelect.html(findInfo(count) + ' selektiert</br>' + (all ? 'Keine' : 'Alle') + ' selektieren');
    $infoSelect.show().widthToContent();
  }

  function findInfo (n) {
    if (n === 0 ) {
      return 'Keine Zeile';
    } else if (n == 1) {
      return 'Eine Zeile';
    } else {
      return n + ' Zeilen';
    }
  }

};
