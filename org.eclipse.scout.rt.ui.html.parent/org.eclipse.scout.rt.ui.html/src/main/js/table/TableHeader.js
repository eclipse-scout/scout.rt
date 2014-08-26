// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TableHeader = function(table, $tableHeader, session) {
  var that = this,
    columns = table.columns;

  this.totalWidth = 0;
  this.dragDone = false;

  for (var i = 0; i < columns.length; i++) {
    var $header = $tableHeader.appendDiv('', 'header-item', columns[i].text)
      .data('index', i)
      .css('min-width', columns[i].width + 'px') // 17 is width of header-resize handle, see table.css (.header-resize)
      .css('max-width', columns[i].width + 'px')
      .on('click', '', clickHeader)
      .on('mousedown', '', dragHeader);

    if (columns[i].type == 'number') $header.css('text-align', 'right');

    this.totalWidth += columns[i].width;

    $tableHeader.appendDiv('', 'header-resize', '')
      .on('mousedown', '', resizeHeader);

    columns[i].$div = $header;
    columns[i].filter = [];
  }

  function clickHeader(event) {
    var $header = $(this);

    if (that.dragDone) {
      that.dragDone = false;
    } else if (event.shiftKey || event.ctrlKey) {
      table.sortChange($header, $header.hasClass('sort-up') ? 'down' : 'up', event.shiftKey);
    } else {
      var x = $header.position().left + $tableHeader.position().left + parseFloat($tableHeader.css('margin-left')),
        y = $header.position().top +  $tableHeader.position().top;
      new scout.TableHeaderMenu(table, $header, x, y, session);
    }

    return false;
  }

  function resizeHeader(event) {
    var startX = event.pageX ,
      $header = $(this).prev(),
      colNum = $header.index() / 2 + 1,
      headerWidth = parseFloat($header.css('min-width')),
      totalWidth = parseFloat($('.table-row').eq(0).css('width'));

    $('body').addClass('col-resize')
      .on('mousemove', '', resizeMove)
      .one('mouseup', '', resizeEnd);

    function resizeMove(event) {
      var diff = event.pageX  - startX,
        wHeader = headerWidth + diff,
        wSummary = totalWidth + diff;

      if (wHeader > 80 || diff > 0) {
        $header.css('min-width', wHeader).css('max-width', wHeader);
        $('.table-row > div:nth-of-type(' + colNum + ' ), .table-row-sum > div:nth-of-type(' + colNum + ' )').css('min-width', wHeader).css('max-width', wHeader);
        $('.table-row, .table-row-sum').css('min-width', wSummary).css('max-width', wSummary);
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
      move = $header.outerWidth(),
      $headers = $header.siblings('.header-item');

    that.dragDone = false;

    // start drag & drop events
    $('body').on('mousemove', '', dragMove)
      .one('mouseup', '', dragEnd);

    function dragMove(event) {
      var diff = event.pageX - startX;

      // change css of draged header
      $header.css('z-index', 50)
        .addClass('header-move');
      $tableHeader.addClass('header-move');

      // move dragged header
      $header.css('left', diff);

      // find other affected headers
      var middle = realMiddle($header);

      $headers.each(function(i) {
        var m = realMiddle($($headers[i]));

        if (middle < m && i < oldPos / 2) {
          $(this).css('left', move);
        } else if (middle > m && i >= oldPos / 2) {
          $(this).css('left', -move);
        } else {
          $(this).css('left', 0);
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

      if (diff !== 0) {
        that.dragDone = true;
      }
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
      // remove events
      $('body').off('mousemove');

      // in case of no movement: return
      if (!that.dragDone)  {
        return true;
      }

      // move column
      if (oldPos !== newPos) {
        table.moveColumn($header, oldPos, newPos, true);
        that.dragDone = false;
      } else {
        $header.animateAVCSD('left', '', function () {that.dragDone = false;});
      }

      // reset css of dragged header
      $headers.each(function() {
        $(this).css('left', '');
      });

      $header.css('z-index', '')
        .css('background', '')
        .removeClass('header-move');
      $tableHeader.removeClass('header-move');

    }
  }
};
