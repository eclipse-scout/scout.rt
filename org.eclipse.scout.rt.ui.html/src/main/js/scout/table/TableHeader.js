// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TableHeader = function(table) {
  scout.TableHeader.parent.call(this);
  this.init(table.session);

  this.dragging = false;
  this.table = table;
  this.columns = table.columns;
  this.menuBar = new scout.MenuBar(this.session, new scout.GroupBoxMenuItemsOrder());
  this.menuBar.tabbable = false;
  this.menuBar.bottom();
  this.addChild(this.menuBar);
};
scout.inherits(scout.TableHeader, scout.Widget);

scout.TableHeader.prototype._render = function() {
  var column, $header, alignment, $defaultCheckedColumHeader, $separator,
    that = this,
    columns = this.columns,
    table = this.table,
    tooltipFunction = function(col) {
      if (col.data('column') && scout.strings.hasText(col.data('column').headerTooltip)) {
        return col.data('column').headerTooltip;
      } else if (col.isContentTruncated() || (col.width() + col.position().left) > col.parent().width()) {
        return col.text();
      }
    };
  this.$container = table.$data.beforeDiv('table-header');

  this._dataScrollHandler = this._onDataScroll.bind(this);
  table.$data.on('scroll', this._dataScrollHandler);

  for (var i = 0; i < columns.length; i++) {
    column = columns[i];
    $header = this.$container.appendDiv('header-item')
      .data('column', column)
      .css('min-width', column.width + 'px')
      .css('max-width', column.width + 'px')
      .on('click', this._onHeaderItemClick.bind(this))
      .on('mousedown', dragHeader);
    $header.toggleAttr('data-modelclass', column.modelClass, column.modelClass);
    $header.toggleAttr('data-classid', column.classId, column.classId);

    column.$header = $header;

    scout.tooltips.install($header, this.session, {
      tooltipText: tooltipFunction
    });

    this._decorateHeader(column);
    alignment = scout.Table.parseHorizontalAlignment(column.horizontalAlignment);
    if (alignment !== 'left') {
      $header.css('text-align', alignment);
    }

    if (!column.fixedWidth) {
      $separator = this.$container.appendDiv('header-resize');
      $separator.on('mousedown', '', resizeHeader);
    }
  }

  // Filler is necessary to make sure the header is always as large as the table data, otherwise horizontal scrolling does not work correctly
  this.$filler = this.$container.appendDiv('header-item filler').css('visibility', 'hidden');
  if (this.columns.length === 0) {
    // If there are no columns, make the filler visible and make sure the header is as large as normally using nbsp
    this.$filler.css('visibility', 'visible').html('&nbsp;').addClass('empty');
  }

  this.menuBar.render(this.$container);
  this._$menuBar = this.menuBar.$container;
  this.updateMenuBar();
  this._reconcileScrollPos();

  function resizeHeader(event) {
    var startX = event.pageX,
      $header = $(this).prev(),
      headerWidth = parseFloat($header.css('min-width'));

    $(window)
      .on('mousemove.tableheader', resizeMove)
      .one('mouseup', resizeEnd);
    $('body').addClass('col-resize');

    // Prevent text selection in a form, don't stop propagation to allow others (e.g. cell editor) to react
    event.preventDefault();

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
      column = $header.data('column'),
      oldPos = that.table.columns.indexOf(column),
      newPos = oldPos,
      move = $header.outerWidth(),
      $otherHeaders = $header.siblings('.header-item');

    that.dragging = false;
    // firefox fires a click action after a column has been droped at the new location, chrome doesn't -> we need a hint to avoid menu gets opened after drop
    that.columnMoved = false;

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
      $header.addClass('header-move');
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

      if (that._tableHeaderMenu && that._tableHeaderMenu.rendered) {
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
          newPos = that.table.columns.indexOf(($(this).data('column')));
          return false;
        }
      });

      // move column
      if (newPos > -1 && oldPos !== newPos) {
        table.moveColumn($header.data('column'), oldPos, newPos, true);
        that.dragging = false;
        that.columnMoved = true;
      } else {
        $header.animateAVCSD('left', '', function() {
          that.dragging = false;
        });
      }

      // reset css of dragged header
      $otherHeaders.each(function() {
        $(this).css('left', '');
      });

      $header.css('background', '')
        .removeClass('header-move');
      that.$container.removeClass('header-move');
    }
  }
};

scout.TableHeader.prototype._remove = function() {
  scout.TableHeader.parent.prototype._remove.call(this);
  this.table.$data.off('scroll', this._dataScrollHandler);
};

scout.TableHeader.prototype.onColumnResized = function(column) {
  var lastColumn = this.table.columns[this.table.columns.length - 1];
  this.resizeHeaderItem(column);
  if (lastColumn !== column) {
    this.resizeHeaderItem(lastColumn);
  }
};

scout.TableHeader.prototype.resizeHeaderItem = function(column) {
  if (!column) {
    //May be undefined if there are no columns
    return;
  }
  var remainingHeaderSpace, adjustment,
    $header = column.$header,
    width = column.width,
    menuBarWidth = this._$menuBar.outerWidth(),
    isLastColumn = this.table.columns.indexOf(column) === this.table.columns.length - 1;

  if (isLastColumn) {
    remainingHeaderSpace = Math.max(this.$container.width() - this.table._rowWidth, 0);
    if (remainingHeaderSpace < menuBarWidth) {
      // add 1 px to make the resizer visible
      adjustment = menuBarWidth - remainingHeaderSpace + 1;
      width -= adjustment;
      width = Math.max(width, scout.Table.COLUMN_MIN_WIDTH);
      this.$filler.cssWidth(adjustment);
    }
  }
  $header
    .css('min-width', width)
    .css('max-width', width);
};

scout.TableHeader.prototype._onHeaderItemClick = function(event) {
  var $headerItem = $(event.target),
    column = $headerItem.data('column');

  if (column.disallowHeaderMenu) {
    return;
  }

  if (this.dragging || this.columnMoved) {
    this.dragging = false;
    this.columnMoved = false;
  } else if (event.shiftKey || event.ctrlKey) {
    this.table.removeGrouping();
    this.table.sort(column, $headerItem.hasClass('sort-asc') ? 'desc' : 'asc', event.shiftKey);
  } else if (this._tableHeaderMenu && this._tableHeaderMenu.isOpenFor($headerItem)) {
    this.closeTableHeaderMenu();
  } else {
    this.openTableHeaderMenu(column);
  }

  return false;
};

scout.TableHeader.prototype._onDataScroll = function() {
  scout.scrollbars.fix(this._$menuBar);
  this._reconcileScrollPos();
  this._fixTimeout = scout.scrollbars.unfix(this._$menuBar, this._fixTimeout);
};

scout.TableHeader.prototype._reconcileScrollPos = function() {
  // When scrolling horizontally scroll header as well
  var scrollLeft = this.table.$data.scrollLeft(),
    lastColumn = this.table.columns[this.table.columns.length - 1];

  this.resizeHeaderItem(lastColumn);
  this.$container.scrollLeft(scrollLeft);
  this._$menuBar.cssRight(-1 * scrollLeft);
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
    $moveResize = $moveHeader.next(),
    lastColumnPos = this.table.columns.length - 1;

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

  // Update header size due to header menu items if moved from or to last position
  if (oldPos === lastColumnPos || newPos === lastColumnPos) {
    this.table.columns.forEach(function(column) {
      this.resizeHeaderItem(column);
    }.bind(this));
  }

  // move to old position and then animate
  if (dragged) {
    $header.css('left', parseInt($header.css('left'), 0) + $header.data('old-pos') - $header.offset().left)
      .animateAVCSD('left', 0);
  } else {
    this._arrangeHeaderItems($headers);
  }
};

scout.TableHeader.prototype._arrangeHeaderItems = function($headers) {
  var that = this;
  $headers.each(function() {
    // move to old position and then animate
    $(this).css('left', $(this).data('old-pos') - $(this).offset().left)
    .animate({left: 0}, {
      progress: function(animation, progress, remainingMs) {
        var $headerItem = $(this);
        if (!$headerItem.isSelected()) {
          return;
        }
        // make sure selected header item is visible
        scout.scrollbars.scrollHorizontalTo(that.table.$data, $headerItem);

        // move menu
        if (that._tableHeaderMenu && that._tableHeaderMenu.rendered) {
          that._tableHeaderMenu.position();
        }
      }
    });
  });
};

scout.TableHeader.prototype.openTableHeaderMenu = function(column) {
  var $header = column.$header;
  this._tableHeaderMenu = new scout.TableHeaderMenu(this.session, {
    tableHeader: this,
    $anchor: $header,
    focusableContainer: true
  });
  this._tableHeaderMenu.render();
  this.addChild(this._tableHeaderMenu);
};

scout.TableHeader.prototype.closeTableHeaderMenu = function() {
  this._tableHeaderMenu.remove();
  this._tableHeaderMenu = null;
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

  this._arrangeHeaderItems($headers);
};

scout.TableHeader.prototype.findHeaderItems = function() {
  return this.$container.find('.header-item:not(.filler)');
};

/**
 * Updates the column headers visualization of the text, sorting and styling state
 */
scout.TableHeader.prototype.updateHeader = function(column, oldColumnState) {
  this._decorateHeader(column, oldColumnState);
};

scout.TableHeader.prototype._decorateHeader = function(column, oldColumnState) {
  var $header = column.$header;
  if (oldColumnState) {
    $header.removeClass(oldColumnState.headerCssClass);
  }
  $header.addClass(column.headerCssClass);
  if (column.disallowHeaderMenu) {
    $header.addClass('disabled');
  }
  this._applyColumnText($header, column);
  this._applyColumnSorting($header, column);
};

scout.TableHeader.prototype._applyColumnText = function($header, column) {
  var text = column.text;
  if (!text) {
    // Make sure empty header is as height as the others to make it properly clickable
    $header.html('&nbsp;');
    $header.addClass('empty');
  } else {
    $header.text(text);
    $header.removeClass('empty');
  }
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

scout.TableHeader.prototype.updateMenuBar = function() {
  var menuItems = this.table._filterMenus(['Table.Header']);
  this.menuBar.updateItems(menuItems);
};
