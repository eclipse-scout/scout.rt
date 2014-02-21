// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.DesktopTableHeader = function ($tableHeader, columns) {
  var  totalWidth = 0;

  // create header based on model
  for (var i = 0; i < columns.length; i++) {
    var $header = $tableHeader.appendDiv('', 'header-item', columns[i].label)
      .data('type', columns[i].type)
      .css('width', columns[i].width);

    if (columns[i].width === 0) $header.hide();

    totalWidth += columns[i].width;

    $header.appendDiv('', 'header-control', '')
      .on('click', '', clickHeaderMenu);

    $header.appendDiv('', 'header-resize', '')
      .on('mousedown', '', resizeHeader);
  }

  this.totalWidth = totalWidth;

  function resizeHeader (event) {
    var startX = event.pageX - 8,
      $header = $(this).parent(),
      colNum = $header.index() + 1,
      headerWidth = $header.width(),
      totalWidth = $('.table-row').first().width();

    $('body').addClass('col-resize')
      .on('mousemove', '', resizeMove)
      .one('mouseup', '', resizeEnd);

    return false;

    function resizeMove (event) {
      var diff = event.pageX - startX;

      if (headerWidth + diff > 80) {
        $header.css('width', headerWidth + diff);
        $('.table-row > div:nth-of-type(' + colNum + ' )').css('width', headerWidth + diff);
        $('.table-row').width(totalWidth + diff);

      }
    }

    function resizeEnd (event){
      $('body').off('mousemove')
        .removeClass('col-resize');
    }
  }

  function clickHeaderMenu (event) {
    var $clicked = $(this),
      x = $clicked.offset().left,
      y = $clicked.offset().top;

    new Scout.Menu.Header(x, y);
  }
};
