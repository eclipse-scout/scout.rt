// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TableHeader = function(table, $tableHeader, session) {
  var that = this,
    columns = table.columns,
    column, $headerItem, sortDirection, alignment;

  this.totalWidth = 0;
  this.dragDone = false;
  this._$tableHeader = $tableHeader;

  for (var i = 0; i < columns.length; i++) {
    column = columns[i];
    $headerItem = $tableHeader.appendDIV('header-item', columns[i].text)
      .data('column', column)
      .css('min-width', column.width + 'px') // 17 is width of header-resize handle, see table.css (.header-resize)
      .css('max-width', column.width + 'px')
      .on('click', '', onHeaderClick)
      .on('mousedown', '', dragHeader);

    if (column.sortActive) {
      sortDirection = column.sortAscending ? 'asc' : 'desc';
      $headerItem.addClass('sort-' + sortDirection);
//      FIXME CGU consider index
//      if (column.sortIndex >= 0) {
//        $headerItem.attr('data-sort-order', column.sortIndex);
//      }
    }

    alignment =  scout.Table.parseHorizontalAlignment(column.horizontalAlignment);
    if (alignment !== 'left')  {
      $headerItem.css('text-align', alignment);
    }

    this.totalWidth += columns[i].width;

    $tableHeader.appendDIV('header-resize', '')
      .on('mousedown', '', resizeHeader);

    column.$div = $headerItem;
    column.filter = [];
  }

  function onHeaderClick(event) {
    var $headerItem = $(this);

    if (that.dragDone) {
      that.dragDone = false;
    } else if (event.shiftKey || event.ctrlKey) {
      table.sort($headerItem, $headerItem.hasClass('sort-asc') ? 'desc' : 'asc', event.shiftKey);
    } else {
      var x = $headerItem.position().left + $tableHeader.position().left + parseFloat($tableHeader.css('margin-left')),
        y = $headerItem.position().top +  $tableHeader.position().top;
      new scout.TableHeaderMenu(table, $headerItem, x, y, session);
    }

    return false;
  }

  function resizeHeader(event) {
    var startX = event.pageX ,
      $headerItem = $(this).prev(),
      colNum = that.getColumnViewIndex($headerItem) + 1,
      headerWidth = parseFloat($headerItem.css('min-width')),
      totalWidth = parseFloat($('.table-row').eq(0).css('width'));

    $('body').addClass('col-resize')
      .on('mousemove', '', resizeMove)
      .one('mouseup', '', resizeEnd);

    function resizeMove(event) {
      var diff = event.pageX  - startX,
        wHeader = headerWidth + diff,
        wSummary = totalWidth + diff;

      if (wHeader > 80 || diff > 0) {
        $headerItem.css('min-width', wHeader).css('max-width', wHeader);
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
    var diff = 0,
      startX = event.pageX,
      $headerItem = $(this),
      oldPos = that.getColumnViewIndex($headerItem),
      newPos =  oldPos,
      move = $headerItem.outerWidth(),
      $otherHeaders = $headerItem.siblings('.header-item');

    that.dragDone = false;

    // start drag & drop events
    $('body').on('mousemove', '', dragMove)
      .one('mouseup', '', dragEnd);

    function dragMove(event) {
      diff = event.pageX - startX;

      // change css of dragged header
      $headerItem.css('z-index', 50)
        .addClass('header-move');
      $tableHeader.addClass('header-move');

      // move dragged header
      $headerItem.css('left', diff);

      // find other affected headers
      var middle = realMiddle($headerItem);

      $otherHeaders.each(function(i) {
        var m = realMiddle($($otherHeaders[i]));

        if (middle < m && i < oldPos) {
          $(this).css('left', move);
        } else if (middle > m && i >= oldPos) {
          $(this).css('left', -move);
        } else {
          $(this).css('left', 0);
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

      // find new position of dragged header
      var h = (diff < 0) ? $otherHeaders : $($otherHeaders.get().reverse());

      h.each(function(i) {
        if ($(this).css('left') != '0px') {
          newPos = that.getColumnViewIndex($(this));
          return false;
        }
      });

      // in case of no movement: return
      if (!that.dragDone)  {
        return true;
      }

      // move column
      if (oldPos !== newPos) {
        table.moveColumn($headerItem, oldPos, newPos, true);
        that.dragDone = false;
      } else {
        $headerItem.animateAVCSD('left', '', function () {that.dragDone = false;});
      }

      // reset css of dragged header
      $otherHeaders.each(function() {
        $(this).css('left', '');
      });

      $headerItem.css('z-index', '')
        .css('background', '')
        .removeClass('header-move');
      $tableHeader.removeClass('header-move');
    }
  }
};

scout.TableHeader.prototype.onSortingChanged = function($header, dir, additional, remove) {
  $header.removeClass('sort-asc sort-desc');

  if (remove) {
    var attr = $header.attr('data-sort-order');
    $header.siblings().each(function() {
      if ($(this).attr('data-sort-order') > attr) {
        var sortOrder = parseInt($(this).attr('data-sort-order'), 0) - 1;
        $(this).attr('data-sort-order', sortOrder);
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
        .removeClass('sort-asc sort-desc')
        .attr('data-sort-order', null);
    }
  }
};

scout.TableHeader.prototype.onGroupingChanged = function($headerItem, all) {
  if (all) {
    $headerItem.parent().addClass('group-all');
  } else {
    $headerItem.addClass('group-sort');
  }
};

scout.TableHeader.prototype.onColumnMoved = function($header, oldPos, newPos, dragged) {
  var $headers = this.findHeaderItems(),
    $moveHeader = $headers.eq(oldPos),
    $moveResize = $moveHeader.next();

  // store old position of header
  $headers.each(function() {
  $(this).data('old-pos', $(this).offset().left);
  });

  // change order in dom of header
  if (newPos < oldPos) {
    $headers.eq(newPos).before($moveHeader);
    $headers.eq(newPos).before($moveResize);
  } else {
    $headers.eq(newPos).after($moveHeader);
    $headers.eq(newPos).after($moveResize);
  }

  // move menu
  var left = $header.position().left;

  $('.table-header-menu').animateAVCSD('left', left + 20);

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

scout.TableHeader.prototype.getColumnViewIndex = function($headerItem) {
  return $headerItem.index() / 2;
};

scout.TableHeader.prototype.findHeaderItems = function() {
  return this._$tableHeader.find('.header-item');
};
