// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TableHeader = function(table, session) {
  var column, $header, alignment, $defaultCheckedColumHeader, $separator,
    that = this,
    columns = table.columns;

  this.dragging = false;
  this.$container = table.$data.beforeDiv('table-header');
  this.table = table;
  this.columns = table.columns;

  for (var i = 0; i < columns.length; i++) {
    column = columns[i];
    $header = this.$container.appendDiv('header-item')
      .data('column', column)
      .css('min-width', column.width + 'px')
      .css('max-width', column.width + 'px')
      .on('click', '', onHeaderClick)
      .on('mousedown', '', dragHeader);

    this._applyColumnText($header, column);
    this._applyColumnSorting($header, column);

    alignment = scout.Table.parseHorizontalAlignment(column.horizontalAlignment);
    if (alignment !== 'left') {
      $header.css('text-align', alignment);
    }

    $separator = this.$container.appendDiv('header-resize');
    if (!column.fixedWidth) {
      $separator.on('mousedown', '', resizeHeader);
    } else {
      $separator.addClass('fixed');
    }

    column.$header = $header;
    column.filter = [];
  }

  function onHeaderClick(event) {
    var $header = $(this);

    if (that.dragging) {
      that.dragging = false;
    } else if (event.shiftKey || event.ctrlKey) {
      table.sort($header.data('column'), $header.hasClass('sort-asc') ? 'desc' : 'asc', event.shiftKey);
    } else if (that._tableHeaderMenu && that._tableHeaderMenu.isOpenFor($(event.target))) {
      that._tableHeaderMenu.remove();
      that._tableHeaderMenu = null;
    } else {
      var x = $header.position().left + that.$container.position().left + parseFloat(that.$container.css('margin-left')),
        y = $header.position().top + that.$container.position().top;
      that._tableHeaderMenu = new scout.TableHeaderMenu(table, $header, x, y, session);
    }

    return false;
  }

  function resizeHeader(event) {
    var startX = event.pageX,
      $header = $(this).prev(),
      headerWidth = parseFloat($header.css('min-width'));

    $(window)
      .on('mousemove.tableheader', resizeMove)
      .one('mouseup', resizeEnd);
    $('body').addClass('col-resize');

    function resizeMove(event) {
      var diff = event.pageX - startX,
        wHeader = headerWidth + diff;

      if (wHeader > 80 || diff > 0) {
        that.table.resizeColumn($header.data('column'), wHeader);
      }
    }

    function resizeEnd(event) {
      $(window).off('mousemove.tableheader');
      $('body').removeClass('col-resize');
    }
  }

  function dragHeader(event) {
    var diff = 0,
      startX = event.pageX,
      $header = $(this),
      oldPos = that.columnIndex($header),
      newPos = oldPos,
      move = $header.outerWidth(),
      $otherHeaders = $header.siblings('.header-item');

    that.dragging = false;

    // start drag & drop events
    $(window)
      .on('mousemove.tableheader', '', dragMove)
      .one('mouseup', '', dragEnd);

    function dragMove(event) {
      diff = event.pageX - startX;
      if (diff === 0) {
        return;
      }

      that.dragging = true;

      // change css of dragged header
      $header.css('z-index', 50)
        .addClass('header-move');
      that.$container.addClass('header-move');

      // move dragged header
      $header.css('left', diff);

      // find other affected headers
      var middle = realMiddle($header);

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


      if (that._tableHeaderMenu && that._tableHeaderMenu.isOpen()) {
        that._tableHeaderMenu.remove();
        that._tableHeaderMenu = null;
      }
    }

    function realWidth($div) {
      var html = $div.html(),
        width = $div.html('<span>' + html + '</span>').find('span:first').width();

      $div.html(html);
      return width;
    }

    function realMiddle($div) {
      if ($div.css('text-align') === 'right') {
        return $div.offset().left + $div.outerWidth() - realWidth($div) / 2;
      } else {
        return $div.offset().left + realWidth($div) / 2;
      }
    }

    function dragEnd(event) {
      $(window).off('mousemove.tableheader');

      // in case of no movement: return
      if (!that.dragging) {
        return true;
      }

      // find new position of dragged header
      var h = (diff < 0) ? $otherHeaders : $($otherHeaders.get().reverse());
      h.each(function(i) {
        if ($(this).css('left') !== '0px') {
          newPos = that.columnIndex($(this));
          return false;
        }
      });

      // move column
      if (oldPos !== newPos) {
        table.moveColumn($header.data('column'), oldPos, newPos, true);
        that.dragging = false;
      } else {
        $header.animateAVCSD('left', '', function() {
          that.dragging = false;
        });
      }

      // reset css of dragged header
      $otherHeaders.each(function() {
        $(this).css('left', '');
      });

      $header.css('z-index', '')
        .css('background', '')
        .removeClass('header-move');
      that.$container.removeClass('header-move');
    }
  }
};

scout.TableHeader.prototype.remove = function() {
  this.$container.remove();
};

scout.TableHeader.prototype.onColumnResized = function(column, width) {
  var $header = column.$header;
  $header
    .css('min-width', width)
    .css('max-width', width);
};

scout.TableHeader.prototype.onSortingChanged = function() {
  for (var i = 0; i < this.table.columns.length; i++) {
    var column = this.table.columns[i];
    this._applyColumnSorting(column.$header, column);
  }
};

scout.TableHeader.prototype.onColumnMoved = function(column, oldPos, newPos, dragged) {
  var $header = column.$header,
    $headers = this.findHeaderItems(),
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
  if (this._tableHeaderMenu && this._tableHeaderMenu.isOpen()) {
    var left = $header.position().left;
    var marginLeft = this.$container.cssMarginLeft();
    this._tableHeaderMenu.$headerMenu.animateAVCSD('left', left + marginLeft);
  }

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

scout.TableHeader.prototype.onOrderChanged = function(oldColumnOrder) {
  var column, i, $header, $headerResize;
  var $headers = this.findHeaderItems();

  // store old position of headers
  $headers.each(function() {
    $(this).data('old-pos', $(this).offset().left);
  });

  // change order in dom of header
  for (i = 0; i < this.table.columns.length; i++) {
    column = this.table.columns[i];
    $header = column.$header;
    $headerResize = $header.next('.header-resize');

    this.$container.append($header);
    this.$container.append($headerResize);
  }

  // move menu
  //Menu may only be open at this time if the user opened the menu right before the columnOrderChanged event arrives from the server
  if (this._tableHeaderMenu && this._tableHeaderMenu.isOpen()) {
    var left = this._tableHeaderMenu.$header.position().left;
    this._tableHeaderMenu.$headerMenu.animateAVCSD('left', left + 20);
  }

  // move to old position and then animate
  $headers.each(function() {
    $(this).css('left', $(this).data('old-pos') - $(this).offset().left)
      .animateAVCSD('left', 0);
  });
};

scout.TableHeader.prototype.columnIndex = function($header) {
  // divide index by 2, because after each column $header, there is an additional header-resize div
  return $header.index() / 2;
};

scout.TableHeader.prototype.findHeaderItems = function() {
  return this.$container.find('.header-item');
};

/**
 * Updates the column headers visualization of the text and sorting state
 */
scout.TableHeader.prototype.updateHeaders = function(columns) {
  for (var i = 0; i < columns.length; i++) {
    var column = columns[i];
    var $header = columns[i].$header;
    this._applyColumnText($header, column);
    this._applyColumnSorting($header, column);
  }
};

scout.TableHeader.prototype._applyColumnText = function($header, column) {
  var text = column.text;
  if (!text) {
    text = '';
  }
  $header.text(text);
};

scout.TableHeader.prototype._applyColumnSorting = function($header, column) {
  $header.removeClass('sort-asc');
  $header.removeClass('sort-desc');

  var sortDirection;
  if (column.sortActive) {
    sortDirection = column.sortAscending ? 'asc' : 'desc';
    $header.addClass('sort-' + sortDirection);
  }
};
