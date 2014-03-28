// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.DesktopTableHeader = function(desktopTable, $tableHeader, filterCallback) {
  this.totalWidth = 0;

  var columns = desktopTable.model.table.columns;

  for (var i = 0; i < columns.length; i++) {
    var $header = $tableHeader.appendDiv('', 'header-item', columns[i].text)
      .data('type', columns[i].type)
      .data('index', i)
      .css('width', columns[i].width - 17)
      .on('click', '', clickHeader);

    this.totalWidth += columns[i].width;

    $tableHeader.appendDiv('', 'header-resize', '')
      .on('mousedown', '', resizeHeader);

    columns[i].$div = $header;
  }

  $tableHeader.appendDiv('HeaderOrganize')
    .on('click', '', clickOrganize);

  function clickHeader(event) {
    var $header = $(this);

    if (event.shiftKey || event.ctrlKey) {
      var index = $header.data('index'),
        dir = $header.hasClass('sort-up') ? 'down' : 'up';

      desktopTable.sortChange(index, dir, event.shiftKey);
    } else {
      var x = $header.offset().left,
        y = $header.offset().top;
      new Scout.MenuHeader(desktopTable, $header, filterCallback, x, y);
    }

    return false;
  }

  function clickOrganize(event) {
    var $clicked = $(this),
      x = $clicked.offset().left,
      y = $clicked.offset().top;

    new Scout.MenuTable(desktopTable, x, y);

    return false;
  }

  function resizeHeader(event) {
    var startX = event.pageX - 1,
      $header = $(this).prev(),
      colNum = $header.data('index') + 1,
      headerWidth = $header.width(),
      totalWidth = $('.table-row').first().width();

    $('body').addClass('col-resize')
      .on('mousemove', '', resizeMove)
      .one('mouseup', '', resizeEnd);
    return false;

    function resizeMove(event) {
      var diff = event.pageX - startX;

      if (headerWidth + diff > 80) {
        $header.css('width', headerWidth + diff);
        $('.table-row > div:nth-of-type(' + colNum + ' )').css('width', headerWidth  + 17 + diff);
        $('.table-row').width(totalWidth + diff);

      }
    }

    function resizeEnd(event) {
      $('body').off('mousemove')
        .removeClass('col-resize');

      return false;
    }
  }
};
