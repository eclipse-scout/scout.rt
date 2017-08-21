/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TableHeader = function() {
  scout.TableHeader.parent.call(this);

  this.enabled = true;
  this.dragging = false;
  this.headerMenusEnabled = true;
  this.table = null;
  this._tableDataScrollHandler = this._onTableDataScroll.bind(this);
  this._tableAddFilterRemovedHandler = this._onTableAddFilterRemoved.bind(this);
  this._tableColumnResizedHandler = this._onTableColumnResized.bind(this);
  this._tableColumnMovedHandler = this._onTableColumnMoved.bind(this);
  this._renderedColumns = [];
};
scout.inherits(scout.TableHeader, scout.Widget);

scout.TableHeader.prototype._init = function(options) {
  scout.TableHeader.parent.prototype._init.call(this, options);

  this.menuBar = scout.create('MenuBar', {
    parent: this,
    menuOrder: new scout.GroupBoxMenuItemsOrder()
  });
  this.menuBar.tabbable = false;
  this.menuBar.bottom();
  this.menuBar.on('propertyChange', this._onMenuBarPropertyChange.bind(this));
  this.updateMenuBar();
};

scout.TableHeader.prototype._render = function() {
  this.$container = this.table.$data.beforeDiv('table-header');

  // Filler is necessary to make sure the header is always as large as the table data, otherwise horizontal scrolling does not work correctly
  this.$filler = this.$container.appendDiv('table-header-item filler').css('visibility', 'hidden');

  if (!this.enabled) {
    this.menuBar.hiddenByUi = true;
  }
  // Required to make "height: 100%" rule work. menuBarContainer and menuBar itself must have the same visibility.
  // Otherwise they could cover the sorting/filter icons on the table-header of the column.
  this.$menuBarContainer = this.$container
    .appendDiv('menubar-container')
    .addDeviceClass()
    .setVisible(this.menuBar.visible);
  this.menuBar.render(this.$menuBarContainer);
  this._$window = this.$container.window();
  this._$body = this.$container.body();

  this._renderColumns();

  this.table.$data.on('scroll', this._tableDataScrollHandler);
  this.table.on('filterAdded', this._tableAddFilterRemovedHandler);
  this.table.on('filterRemoved', this._tableAddFilterRemovedHandler);
  this.table.on('columnResized', this._tableColumnResizedHandler);
  this.table.on('columnMoved', this._tableColumnMovedHandler);
};

scout.TableHeader.prototype._remove = function() {
  this.table.$data.off('scroll', this._tableDataScrollHandler);
  this.table.off('filterAdded', this._tableAddFilterRemovedHandler);
  this.table.off('filterRemoved', this._tableAddFilterRemovedHandler);
  this.table.off('columnResized', this._tableColumnResizedHandler);
  this.table.off('columnMoved', this._tableColumnMovedHandler);

  this._removeColumns();

  scout.TableHeader.parent.prototype._remove.call(this);
};

scout.TableHeader.prototype.rerenderColumns = function() {
  this._removeColumns();
  this._renderColumns();
};

scout.TableHeader.prototype._renderColumns = function() {
  var visibleColumns = this._visibleColumns();
  visibleColumns.forEach(this._renderColumn, this);
  if (visibleColumns.length === 0) {
    // If there are no columns, make the filler visible and make sure the header is as large as normally using nbsp
    this.$filler.css('visibility', 'visible').html('&nbsp;').addClass('empty');
  }
  this._reconcileScrollPos();
};

scout.TableHeader.prototype._renderColumn = function(column, index) {
  var columnWidth = column.width,
    marginLeft = '',
    marginRight = '',
    visibleColumns = this._visibleColumns(),
    isFirstColumn = (index === 0),
    isLastColumn = (index === visibleColumns.length - 1);

  if (isFirstColumn) {
    marginLeft = this.table.rowBorderLeftWidth;
  } else if (isLastColumn) {
    marginRight = this.table.rowBorderRightWidth;
  }

  var $header = this.$filler.beforeDiv('table-header-item')
    .setEnabled(this.enabled)
    .data('column', column)
    .cssMinWidth(columnWidth)
    .cssMaxWidth(columnWidth)
    .cssMarginLeft(marginLeft)
    .cssMarginRight(marginRight);
  $header.appendSpan('table-header-item-text');
  if (this.enabled) {
    $header
      .on('click', this._onHeaderItemClick.bind(this))
      .on('mousedown', this._onHeaderItemMouseDown.bind(this));
  }

  if (this.session.inspector) {
    scout.inspector.applyInfo(column, $header);
  }

  if (isFirstColumn) {
    $header.addClass('first');
  } else if (isLastColumn) {
    $header.addClass('last');
  }

  column.$header = $header;

  scout.tooltips.install($header, {
    parent: this,
    text: this._headerItemTooltipText.bind(this),
    arrowPosition: 50,
    arrowPositionUnit: '%',
    nativeTooltip: !scout.device.isCustomEllipsisTooltipPossible()
  });

  this._decorateHeader(column);
  $header.addClass('halign-' + scout.Table.parseHorizontalAlignment(column.horizontalAlignment));

  var showSeparator = column.showSeparator;
  if (isLastColumn && !this.enabled) {
    showSeparator = false;
  }
  if (showSeparator) {
    var $separator = this.$filler.beforeDiv('table-header-resize');
    if (column.fixedWidth || !this.enabled) {
      $separator.setEnabled(false);
    } else {
      $separator
        .on('mousedown', '', this._onSeparatorMouseDown.bind(this))
        .on('dblclick', this._onSeparatorDblclick.bind(this));
    }
    column.$separator = $separator;
  }
  this._renderedColumns.push(column);
};

scout.TableHeader.prototype._removeColumns = function() {
  this._renderedColumns.slice().forEach(this._removeColumn, this);
};

scout.TableHeader.prototype._removeColumn = function(column) {
  if (column.$header) {
    column.$header.remove();
    column.$header = null;
  }
  if (column.$separator) {
    column.$separator.remove();
    column.$separator = null;
  }
  scout.arrays.remove(this._renderedColumns, column);
};

scout.TableHeader.prototype.resizeHeaderItem = function(column) {
  if (!column) {
    // May be undefined if there are no columns
    return;
  }
  if (!column.$header) {
    // May be undefined if called when header item is not rendered yet (may caused by _adjustColumnMinWidth)
    return;
  }

  var remainingHeaderSpace, adjustment,
    $header = column.$header,
    $headerResize,
    columnWidth = column.width,
    marginLeft = '',
    marginRight = '',
    menuBarWidth = (this.menuBar.visible ? this.$menuBarContainer.outerWidth(true) : 0),
    visibleColumns = this._visibleColumns(),
    isFirstColumn = visibleColumns.indexOf(column) === 0,
    isLastColumn = visibleColumns.indexOf(column) === visibleColumns.length - 1;

  if (isFirstColumn) {
    marginLeft = this.table.rowBorderLeftWidth;
  } else if (isLastColumn) {
    marginRight = this.table.rowBorderRightWidth;
    remainingHeaderSpace = this.$container.width() - this.table.rowWidth + scout.graphics.insets(this.table.$data).right;
    $headerResize = $header.next('.table-header-resize');

    if (remainingHeaderSpace < menuBarWidth) {
      adjustment = menuBarWidth;
      adjustment += $headerResize.width();
      if (remainingHeaderSpace > 0) {
        adjustment -= remainingHeaderSpace;
      }

      var origColumnWidth = columnWidth;
      columnWidth = Math.max(columnWidth - adjustment, column.minWidth);
      this.$filler.cssWidth(origColumnWidth - columnWidth);
    }
  }

  $header
    .css('min-width', columnWidth)
    .css('max-width', columnWidth)
    .css('margin-left', marginLeft)
    .css('margin-right', marginRight);

  if (this._tableHeaderMenu && this._tableHeaderMenu.rendered && this._tableHeaderMenu.column === column) {
    this._tableHeaderMenu.onColumnResized();
  }
};

scout.TableHeader.prototype._reconcileScrollPos = function() {
  // When scrolling horizontally scroll header as well
  var
    scrollLeft = this.table.$data.scrollLeft(),
    lastColumn = this._lastVisibleColumn();

  this.resizeHeaderItem(lastColumn);
  this.$container.scrollLeft(scrollLeft);
  this.$menuBarContainer.cssRight(-1 * scrollLeft);
};

scout.TableHeader.prototype._arrangeHeaderItems = function($headers) {
  var that = this;
  $headers.each(function() {
    // move to old position and then animate
    $(this).css('left', $(this).data('old-pos') - $(this).offset().left)
      .animate({
        left: 0
      }, {
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

scout.TableHeader.prototype._headerItemTooltipText = function($col) {
  var column = $col.data('column');
  if (column && scout.strings.hasText(column.headerTooltipText)) {
    return column.headerTooltipText;
  } else if ($col.isContentTruncated() || ($col.width() + $col.position().left) > $col.parent().width()) {
    $col = $col.clone();
    $col.children('.table-header-item-state').remove();
    return $col.text();
  }
};

scout.TableHeader.prototype.setHeaderMenusEnabled = function(headerMenusEnabled) {
  this.setProperty('headerMenusEnabled', headerMenusEnabled);
};

scout.TableHeader.prototype._renderHeaderMenusEnabled = function() {
  this._visibleColumns().forEach(function(column) {
    this._decorateHeader(column);
  }, this);
};

scout.TableHeader.prototype.openHeaderMenu = function(column) {
  var $header = column.$header;
  this._tableHeaderMenu = scout.create('TableHeaderMenu', {
    parent: this,
    column: $header.data('column'),
    tableHeader: this,
    $anchor: $header,
    focusableContainer: true
  });
  this._tableHeaderMenu.open();
};

scout.TableHeader.prototype.closeHeaderMenu = function() {
  this._tableHeaderMenu.destroy();
  this._tableHeaderMenu = null;
};

scout.TableHeader.prototype.onColumnActionsChanged = function(event) {
  if (this._tableHeaderMenu) {
    this._tableHeaderMenu.onColumnActionsChanged(event);
  }
};

scout.TableHeader.prototype.findHeaderItems = function() {
  return this.$container.find('.table-header-item:not(.filler)');
};

/**
 * Updates the column headers visualization of the text, sorting and styling state
 */
scout.TableHeader.prototype.updateHeader = function(column, oldColumnState) {
  this._decorateHeader(column, oldColumnState);
};

scout.TableHeader.prototype._decorateHeader = function(column, oldColumnState) {
  this._renderColumnCssClass(column, oldColumnState);
  this._renderColumnText(column);
  this._renderColumnIconId(column);
  this._renderColumnState(column);
  this._renderColumnLegacyStyle(column);
  this._renderColumnHeaderMenuEnabled(column);
};

scout.TableHeader.prototype._renderColumnCssClass = function(column, oldColumnState) {
  var $header = column.$header;
  if (oldColumnState) {
    $header.removeClass(oldColumnState.headerCssClass);
  }
  $header.addClass(column.headerCssClass);
};

scout.TableHeader.prototype._renderColumnText = function(column) {
  var text = column.text,
    $header = column.$header,
    $headerText = $header.children('.table-header-item-text');

  if (!column.headerHtmlEnabled) {
    text = scout.strings.nl2br(text);
  }
  // Make sure empty header is as height as the others to make it properly clickable
  $headerText.htmlOrNbsp(text, 'empty');
  this._updateColumnIconAndTextStyle(column);
};

scout.TableHeader.prototype._renderColumnIconId = function(column) {
  column.$header.icon(column.headerIconId);
  this._updateColumnIconAndTextStyle(column);
};

scout.TableHeader.prototype._updateColumnIconAndTextStyle = function(column) {
  var $icon = column.$header.data('$icon'),
    $text = column.$header.children('.table-header-item-text');

  if ($icon) {
    $icon.toggleClass('with-text', !!column.text);
  }
  // Make text invisible if there is an icon but no text
  $text.setVisible(!($icon && $text.html() === '&nbsp;'));
};

scout.TableHeader.prototype._renderColumnLegacyStyle = function(column) {
  scout.styles.legacyStyle(column, column.$header, 'header');
};

scout.TableHeader.prototype._renderColumnHeaderMenuEnabled = function(column) {
  column.$header.toggleClass('disabled', !this._isHeaderMenuEnabled(column) || !this.enabled);
};

scout.TableHeader.prototype._renderColumnState = function(column) {
  var sortDirection, $state,
    $header = column.$header,
    filtered = this.table.getFilter(column.id);

  $header.children('.table-header-item-state').remove();
  $state = $header.appendSpan('table-header-item-state');
  $state.empty();
  $header.removeClass('sort-asc sort-desc sorted group-asc group-desc grouped filtered');
  $state.removeClass('sort-asc sort-desc sorted group-asc group-desc grouped filtered');

  if (column.sortActive) {
    sortDirection = column.sortAscending ? 'asc' : 'desc';
    if (column.grouped) {
      $header.addClass('group-' + sortDirection);
    }
    $header.addClass('sorted sort-' + sortDirection);
    $state.addClass('sorted sort-' + sortDirection);
  }

  if (column.grouped || filtered) {
    // contains group and filter symbols
    var $left = $state.appendDiv('left');
    if (column.grouped) {
      $header.addClass('grouped');
      $state.addClass('grouped');
      $left.appendDiv().text('G');
    }
    if (filtered) {
      $header.addClass('filtered');
      $state.addClass('filtered');
      $left.appendDiv().text('F');
    }
  }
  // Contains sort arrow
  $state.appendDiv('right');

  this._adjustColumnMinWidth(column);
};

/**
 * Makes sure state is fully visible by adjusting width (happens if column.minWidth is < DEFAULT_MIN_WIDTH)
 */
scout.TableHeader.prototype._adjustColumnMinWidth = function(column) {
  var filtered = this.table.getFilter(column.id);
  if (column.sortActive || column.grouped || filtered) {
    if (column.minWidth < scout.Column.DEFAULT_MIN_WIDTH) {
      column.prefMinWidth = column.minWidth;
      column.minWidth = scout.Column.DEFAULT_MIN_WIDTH;
    }
    if (column.width < column.minWidth) {
      this.table.resizeColumn(column, column.minWidth);
    }
  } else {
    // Reset to preferred min width if no state is visible
    if (column.prefMinWidth) {
      column.minWidth = column.prefMinWidth;
      column.prefMinWidth = null;
      // Resize to old min width, assuming user has not manually changed the size because column is still as width as default_min_width
      if (column.width === scout.Column.DEFAULT_MIN_WIDTH) {
        this.table.resizeColumn(column, column.minWidth);
      }
    }
  }
};

scout.TableHeader.prototype.updateMenuBar = function() {
  var menuItems = this.table._filterMenus(this.table.menus, scout.MenuDestinations.HEADER);
  this.menuBar.setMenuItems(menuItems);
};

scout.TableHeader.prototype._onTableColumnResized = function(event) {
  var column = event.column,
    lastColumn = this._lastVisibleColumn();
  this.resizeHeaderItem(column);
  if (lastColumn !== column) {
    this.resizeHeaderItem(lastColumn);
  }
};

scout.TableHeader.prototype.onSortingChanged = function() {
  this._visibleColumns().forEach(this._renderColumnState, this);
};

scout.TableHeader.prototype._onTableColumnMoved = function(event) {
  var
    column = event.column,
    oldPos = event.oldPos,
    newPos = event.newPos,
    $header = column.$header,
    $headers = this.findHeaderItems(),
    $moveHeader = $headers.eq(oldPos),
    $moveResize = $moveHeader.next(),
    visibleColumns = this._visibleColumns(),
    lastColumnPos = visibleColumns.length - 1;

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

  // Update first/last markers
  if ($headers.length > 0) {
    $headers.eq(0).removeClass('first');
    $headers.eq($headers.length - 1).removeClass('last');
  }

  if (visibleColumns.length > 0) {
    visibleColumns[0].$header.addClass('first');
    visibleColumns[lastColumnPos].$header.addClass('last');
  }

  // Update header size due to header menu items if moved from or to last position
  if (oldPos === lastColumnPos || newPos === lastColumnPos) {
    visibleColumns.forEach(function(column) {
      this.resizeHeaderItem(column);
    }.bind(this));
  }

  // move to old position and then animate
  if (event.dragged) {
    $header.css('left', parseInt($header.css('left'), 0) + $header.data('old-pos') - $header.offset().left)
      .animateAVCSD('left', 0);
  } else {
    this._arrangeHeaderItems($headers);
  }
};

scout.TableHeader.prototype._visibleColumns = function() {
  return this.table.visibleColumns();
};

scout.TableHeader.prototype._lastVisibleColumn = function() {
  return scout.arrays.last(this._visibleColumns());
};

scout.TableHeader.prototype.onOrderChanged = function(oldColumnOrder) {
  var $header, $headerResize;
  var $headers = this.findHeaderItems();

  // store old position of headers
  $headers.each(function() {
    $(this).data('old-pos', $(this).offset().left);
  });

  // change order in dom of header
  this._visibleColumns().forEach(function(column) {
    $header = column.$header;
    $headerResize = $header.next('.table-header-resize');

    this.$container.append($header);
    this.$container.append($headerResize);
  }, this);

  this._arrangeHeaderItems($headers);
};

/**
 * Header menus are enabled when property is enabled on the header itself and on the column too.
 */
scout.TableHeader.prototype._isHeaderMenuEnabled = function(column) {
  return !!(column.headerMenuEnabled && this.headerMenusEnabled);
};

scout.TableHeader.prototype._onHeaderItemClick = function(event) {
  var $headerItem = $(event.currentTarget),
    column = $headerItem.data('column');


  if (this.dragging || this.columnMoved) {
    this.dragging = false;
    this.columnMoved = false;
  } else if (this.table.sortEnabled && (event.shiftKey || event.ctrlKey)) {
    this.table.removeColumnGrouping();
    this.table.sort(column, $headerItem.hasClass('sort-asc') ? 'desc' : 'asc', event.shiftKey);
  } else if (this._tableHeaderMenu && this._tableHeaderMenu.isOpenFor($headerItem)) {
    this.closeHeaderMenu();
  } else if (this._isHeaderMenuEnabled(column)) {
    this.openHeaderMenu(column);
  }

  return false;
};

scout.TableHeader.prototype._onHeaderItemMouseDown = function(event) {
  var diff = 0,
    that = this,
    startX = Math.floor(event.pageX),
    $header = $(event.currentTarget),
    column = $header.data('column'),
    oldPos = this._visibleColumns().indexOf(column),
    newPos = oldPos,
    move = $header.outerWidth(),
    $otherHeaders = $header.siblings('.table-header-item:not(.filler)');

  if (column.fixedPosition) {
    // Don't allow moving a column with fixed position
    return;
  }

  this.dragging = false;
  // firefox fires a click action after a column has been droped at the new location, chrome doesn't -> we need a hint to avoid menu gets opened after drop
  this.columnMoved = false;

  // start drag & drop events
  this._$window
    .on('mousemove.tableheader', '', dragMove)
    .one('mouseup', '', dragEnd);

  function dragMove(event) {
    diff = Math.floor(event.pageX) - startX;
    if (diff === 0) {
      return;
    }

    that.dragging = true;

    // change css of dragged header
    $header.addClass('moving');
    that.$container.addClass('moving');

    // move dragged header
    $header.css('left', diff);

    // find other affected headers
    var middle = realMiddle($header);

    $otherHeaders.each(function(i) {
      var m = realMiddle($(this));

      if (middle < m && i < oldPos) {
        $(this).css('left', move);
      } else if (middle > m && i >= oldPos) {
        $(this).css('left', -move);
      } else {
        $(this).css('left', 0);
      }
    });

    if (that._tableHeaderMenu) {
      that._tableHeaderMenu.destroy();
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
    if ($div.hasClass('halign-right')) {
      return $div.offset().left + $div.outerWidth() - realWidth($div) / 2;
    } else {
      return $div.offset().left + realWidth($div) / 2;
    }
  }

  function dragEnd(event) {
    that._$window.off('mousemove.tableheader');

    // in case of no movement: return
    if (!that.dragging) {
      return true;
    }

    // find new position of dragged header
    var h = (diff < 0) ? $otherHeaders : $($otherHeaders.get().reverse());
    h.each(function(i) {
      if ($(this).css('left') !== '0px') {
        newPos = that._visibleColumns().indexOf(($(this).data('column')));
        return false;
      }
    });

    // move column
    if (newPos > -1 && oldPos !== newPos) {
      that.table.moveColumn($header.data('column'), oldPos, newPos, true);
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

    $header.removeClass('moving');
    that.$container.removeClass('moving');
  }
};

scout.TableHeader.prototype._onSeparatorDblclick = function(event) {
  if (event.shiftKey) {
    // Optimize all columns
    this._visibleColumns().forEach(function(column) {
      this.table.resizeToFit(column);
    }, this);
  } else {
    // Optimize the column left of the separator
    var $header = $(event.target).prev(),
      column = $header.data('column');
    this.table.resizeToFit(column);
  }
};

scout.TableHeader.prototype._onSeparatorMouseDown = function(event) {
  var startX = Math.floor(event.pageX),
    $header = $(event.target).prev(),
    column = $header.data('column'),
    that = this,
    headerWidth = column.width;

  column.resizingInProgress = true;

  // Install resize helpers. Those helpers make sure the header and the data element keep their
  // current width until the resizing has finished. Otherwise, make a column smaller while the
  // table has been horizontally scrolled to the right would behave very strange.
  this.$headerColumnResizedHelper = this.$container
    .appendDiv('table-column-resize-helper')
    .css('width', this.table.rowWidth + this.table.rowBorderWidth);
  this.$dataColumnResizedHelper = this.table.$data
    .appendDiv('table-column-resize-helper')
    .css('width', this.table.rowWidth);

  this._$window
    .on('mousemove.tableheader', resizeMove)
    .one('mouseup', resizeEnd);
  this._$body.addClass('col-resize');

  // Prevent text selection in a form, don't stop propagation to allow others (e.g. cell editor) to react
  event.preventDefault();

  function resizeMove(event) {
    var diff = Math.floor(event.pageX) - startX,
      wHeader = headerWidth + diff;

    wHeader = Math.max(wHeader, column.minWidth);
    if (wHeader !== column.width) {
      that.table.resizeColumn(column, wHeader);
    }
  }

  function resizeEnd(event) {
    delete column.resizingInProgress;

    // Remove resize helpers
    that.$headerColumnResizedHelper.remove();
    that.$headerColumnResizedHelper = null;
    that.$dataColumnResizedHelper.remove();
    that.$dataColumnResizedHelper = null;

    that._$window.off('mousemove.tableheader');
    that._$body.removeClass('col-resize');

    if (column.width !== headerWidth) {
      that.table.resizeColumn(column, column.width);
    }
  }
};

scout.TableHeader.prototype._onTableDataScroll = function() {
  this._reconcileScrollPos();
};

scout.TableHeader.prototype._onMenuBarPropertyChange = function(event) {
  if (event.propertyName === 'visible') {
    this.$menuBarContainer.setVisible(event.newValue);
  }
};

scout.TableHeader.prototype._onTableAddFilterRemoved = function(event) {
  var column = event.filter.column;
  // Check for column.$header because column may have been removed in the mean time due to a structure changed event -> don't try to render state
  if (event.filter.filterType === scout.ColumnUserFilter.Type && column.$header) {
    this._renderColumnState(column);
  }
};
