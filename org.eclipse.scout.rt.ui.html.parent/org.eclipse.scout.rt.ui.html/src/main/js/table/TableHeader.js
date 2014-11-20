// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TableHeader = function(table, $tableHeader, session) {
  var that = this,
    columns = table.columns,
    column, $header, alignment;

  this.totalWidth = 0;
  this.dragDone = false;
  this._$tableHeader = $tableHeader;
  this.table = table;
  this.columns = table.columns;

  for (var i = 0; i < columns.length; i++) {
    column = columns[i];
    $header = $tableHeader.appendDIV('header-item')
      .data('column', column)
      .css('min-width', column.width + 'px') // 17 is width of header-resize handle, see table.css (.header-resize)
      .css('max-width', column.width + 'px')
      .on('click', '', onHeaderClick)
      .on('mousedown', '', dragHeader);

    this._applyColumnText($header, column);
    this._applyColumnSorting($header, column);

    alignment =  scout.Table.parseHorizontalAlignment(column.horizontalAlignment);
    if (alignment !== 'left')  {
      $header.css('text-align', alignment);
    }

    this.totalWidth += columns[i].width;

    $tableHeader.appendDIV('header-resize', '')
      .on('mousedown', '', resizeHeader);

    column.$div = $header;
    column.filter = [];
  }

  function onHeaderClick(event) {
    var $header = $(this);

    if (that.dragDone) {
      that.dragDone = false;
    } else if (event.shiftKey || event.ctrlKey) {
      table.sort($header, $header.hasClass('sort-asc') ? 'desc' : 'asc', event.shiftKey);
    } else if (that._tableHeaderMenu && that._tableHeaderMenu.isOpenFor($(event.target))){
      that._tableHeaderMenu.remove();
      that._tableHeaderMenu = null;
    } else {
      var x = $header.position().left + $tableHeader.position().left + parseFloat($tableHeader.css('margin-left')),
        y = $header.position().top +  $tableHeader.position().top;
      that._tableHeaderMenu = new scout.TableHeaderMenu(table, $header, x, y, session);
    }

    return false;
  }

  function resizeHeader(event) {
    var startX = event.pageX ,
      $header = $(this).prev(),
      headerWidth = parseFloat($header.css('min-width')),
      totalWidth = parseFloat(that.table.findRows().eq(0).css('width'));

    $('body').addClass('col-resize')
      .on('mousemove', '', resizeMove)
      .one('mouseup', '', resizeEnd);

    function resizeMove(event) {
      var diff = event.pageX  - startX,
        wHeader = headerWidth + diff,
        wSummary = totalWidth + diff;

      if (wHeader > 80 || diff > 0) {
        that.table.resizeColumn($header, wHeader, wSummary, true);
      }
    }

    function resizeEnd(event) {
      $('body').off('mousemove')
        .removeClass('col-resize');

      var width = parseFloat($header.css('min-width'));
      that.table.resizingColumnFinished($header, width);
    }
  }

  function dragHeader(event) {
    var diff = 0,
      startX = event.pageX,
      $header = $(this),
      oldPos = that.getColumnViewIndex($header),
      newPos =  oldPos,
      move = $header.outerWidth(),
      $otherHeaders = $header.siblings('.header-item');

    that.dragDone = false;

    // start drag & drop events
    $('body').on('mousemove', '', dragMove)
      .one('mouseup', '', dragEnd);

    function dragMove(event) {
      diff = event.pageX - startX;

      // change css of dragged header
      $header.css('z-index', 50)
        .addClass('header-move');
      $tableHeader.addClass('header-move');

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

      if (diff !== 0) {
        that.dragDone = true;

        if (that._tableHeaderMenu && that._tableHeaderMenu.isOpen()) {
          that._tableHeaderMenu.remove();
          that._tableHeaderMenu = null;
        }
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
      // remove events
      $('body').off('mousemove');

      // find new position of dragged header
      var h = (diff < 0) ? $otherHeaders : $($otherHeaders.get().reverse());

      h.each(function(i) {
        if ($(this).css('left') !== '0px') {
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
        table.moveColumn($header, oldPos, newPos, true);
        that.dragDone = false;
      } else {
        $header.animateAVCSD('left', '', function () {that.dragDone = false;});
      }

      // reset css of dragged header
      $otherHeaders.each(function() {
        $(this).css('left', '');
      });

      $header.css('z-index', '')
        .css('background', '')
        .removeClass('header-move');
      $tableHeader.removeClass('header-move');
    }
  }
};

scout.TableHeader.prototype.onColumnResized = function($header, width) {
  $header
    .css('min-width', width)
    .css('max-width', width);
};

scout.TableHeader.prototype.onSortingChanged = function() {
  for (var i=0; i<this.table.columns.length; i++) {
    var column = this.table.columns[i];
    this._applyColumnSorting(column.$div, column);
  }
};

scout.TableHeader.prototype.onGroupingChanged = function($header, all) {
  if (all) {
    $header.parent().addClass('group-all');
  } else {
    $header.addClass('group-sort');
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
  if (this._tableHeaderMenu && this._tableHeaderMenu.isOpen()) {
    var left = $header.position().left;
    var marginLeft = this._$tableHeader.cssMarginLeft();
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
  for (i=0; i < this.table.columns.length; i++) {
    column = this.table.columns[i];
    $header = column.$div;
    $headerResize = $header.next('.header-resize');

    this._$tableHeader.append($header);
    this._$tableHeader.append($headerResize);
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

scout.TableHeader.prototype.getColumnViewIndex = function($header) {
  return $header.index() / 2;
};

scout.TableHeader.prototype.findHeaderItems = function() {
  return this._$tableHeader.find('.header-item');
};

/**
 * Updates the column headers visualization of the text and sorting state
 */
scout.TableHeader.prototype.updateHeaders = function(columns) {
  for (var i=0;i<columns.length;i++) {
    var column = columns[i];
    var $header = columns[i].$div;
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
