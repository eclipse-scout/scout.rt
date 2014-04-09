// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

Scout.DesktopTableHeader = function(desktopTable, $tableHeader, filterCallback) {
  var that = this,
    columns = desktopTable.model.table.columns;

  this.totalWidth = 0;
  this.dragDone = false;

  for (var i = 0; i < columns.length; i++) {
    var $header = $tableHeader.appendDiv('', 'header-item', columns[i].text)
      .data('index', i)
      .css('width', columns[i].width - 17)
      .on('click', '', clickHeader)
      .on('mousedown', '', dragHeader);

    if (columns[i].type == 'number') $header.css('text-align', 'right');

    this.totalWidth += columns[i].width;

    $tableHeader.appendDiv('', 'header-resize', '')
      .on('mousedown', '', resizeHeader);

    columns[i].$div = $header;
    columns[i].filter = [];
  }

  $tableHeader.appendDiv('HeaderOrganize')
    .on('click', '', clickOrganize);

  function clickHeader(event) {
    var $header = $(this);

    if (that.dragDone) {
      that.dragDone = false;
    } else if (event.shiftKey || event.ctrlKey) {
      desktopTable.sortChange($header, $header.hasClass('sort-up') ? 'down' : 'up', event.shiftKey);
    } else {
      var x = $header.offset().left,
        y = $header.offset().top;
      new Scout.MenuHeader(desktopTable, $header, x, y);
    }

    return false;
  }

  function clickOrganize(event) {
    var $clicked = $(this),
      x = $clicked.offset().left,
      y = $clicked.offset().top;

    new Scout.MenuTable(desktopTable, x, y);
  }

  function resizeHeader(event) {
    var startX = event.pageX - 1,
      $header = $(this).prev(),
      colNum = $header.index() / 2 + 1,
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
        $('.table-row > div:nth-of-type(' + colNum + ' ), .table-row-sum > div:nth-of-type(' + colNum + ' )').css('width', headerWidth + 17 + diff);
        $('.table-row, .table-row-sum').width(totalWidth + diff);

      }
    }

    function resizeEnd(event) {
      $('body').off('mousemove')
        .removeClass('col-resize');
    }
  }

  function dragHeader(event) {
    var startX = event.pageX,
      $header = $(this),
      oldPos = $header.index(),
      newPos =  oldPos,
      move = $header.outerWidth() + $header.next().outerWidth(),
      $headers = $header.siblings('.header-item');

    // change css of draged header
    $header.css('z-index', 50)
      .addClass('header-move');

    // start drag & drop events
    $('body').on('mousemove', '', dragMove)
      .one('mouseup', '', dragEnd);

    function dragMove(event) {
      var diff = event.pageX - startX;

      // move dragged header
      $header.css('left', diff);

      // find other affected headers
      var middle = realMiddle($header);

      $headers.each(function(i) {
        var m = realMiddle($($headers[i]));

        if (middle < m && i < oldPos / 2) {
          $(this).css('left', move);
          $(this).next().css('left', move);
        } else if (middle > m && i >= oldPos / 2) {
          $(this).css('left', -move);
          $(this).next().css('left', -move);
        } else {
          $(this).css('left', 0);
          $(this).next().css('left', 0);
        }
      });

      // find new position of dragged header
      var h = (diff < 0) ? $headers : $($headers.get().reverse()),
          d = (diff < 0) ? 2 : 0;

      h.each(function(i) {
        if ($(this).css('left') != '0px') {
          newPos = $(this).index() - d;
          return false;
        }
      });

      that.dragDone = true;
    }

    function realWidth($div) {
      var html = $div.html(),
        width = $div.html('<span>' + html + '</span>').find('span:first').width();

      $div.html(html);
      return width;
    }

    function realMiddle($div) {
      if ($div.css('text-align') == 'right') {
        return $div.offset().left + $div.outerWidth() - realWidth($div) / 2;
      } else {
        return $div.offset().left + realWidth($div) / 2;
      }
    }


    function dragEnd(event) {
      // reset css of dragged header
      $header.css('z-index', '')
        .css('background', '')
        .removeClass('header-move');

      $headers.each(function() {
          $(this).css('left', '');
          $(this).next().css('left', '');
        });

      // remove events
      $('body').off('mousemove');

      // move column
      if (oldPos !== newPos) {
        desktopTable.moveColumn($header, oldPos, newPos, true);
        that.dragDone = false;
      } else {
        $header.animateAVCSD('left', '', function () {that.dragDone = false;});
      }


    }
  }
};