/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, Column, ColumnUserFilter, Device, graphics, GroupBoxMenuItemsOrder, inspector, MenuBar, MenuDestinations, objects, scout, scrollbars, strings, styles, Table, tooltips, Widget} from '../index';
import $ from 'jquery';

export default class TableHeader extends Widget {

  constructor() {
    super();

    this.dragging = false;
    this.headerMenusEnabled = true;
    this.table = null;
    this._tableDataScrollHandler = this._onTableDataScroll.bind(this);
    this._tableAddFilterRemovedHandler = this._onTableAddFilterRemoved.bind(this);
    this._tableColumnResizedHandler = this._onTableColumnResized.bind(this);
    this._tableColumnMovedHandler = this._onTableColumnMoved.bind(this);
    this._renderedColumns = [];
  }

  _init(options) {
    super._init(options);

    this.menuBar = scout.create('MenuBar', {
      parent: this,
      tabbable: false,
      position: MenuBar.Position.BOTTOM,
      menuOrder: new GroupBoxMenuItemsOrder()
    });
    this.menuBar.on('propertyChange', this._onMenuBarPropertyChange.bind(this));
    this.updateMenuBar();
  }

  _render() {
    this.$container = this.table.$data.beforeDiv('table-header')
      .cssBorderLeftWidth(this.table.rowBorders.left || '');

    // Filler is necessary to make sure the header is always as large as the table data, otherwise horizontal scrolling does not work correctly
    this.$filler = this.$container.appendDiv('table-header-item filler').css('visibility', 'hidden');

    // Required to make "height: 100%" rule work. menuBarContainer and menuBar itself must have the same visibility.
    // Otherwise they could cover the sorting/filter icons on the table-header of the column.
    this.$menuBarContainer = this.$container
      .appendDiv('menubar-container')
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
  }

  _remove() {
    this.table.$data.off('scroll', this._tableDataScrollHandler);
    this.table.off('filterAdded', this._tableAddFilterRemovedHandler);
    this.table.off('filterRemoved', this._tableAddFilterRemovedHandler);
    this.table.off('columnResized', this._tableColumnResizedHandler);
    this.table.off('columnMoved', this._tableColumnMovedHandler);

    this._removeColumns();

    super._remove();
  }

  rerenderColumns() {
    this._removeColumns();
    this._renderColumns();
  }

  _renderColumns() {
    let visibleColumns = this._visibleColumns();
    visibleColumns.forEach(this._renderColumn, this);
    if (visibleColumns.length === 0) {
      // If there are no columns, make the filler visible and make sure the header is as large as normally using nbsp
      this.$filler.css('visibility', 'visible').html('&nbsp;').addClass('empty');
    }
    this._reconcileScrollPos();
  }

  _renderColumn(column, index) {
    let columnWidth = column._realWidthIfAvailable(),
      visibleColumns = this._visibleColumns(),
      isFirstColumn = (index === 0),
      isLastColumn = (index === visibleColumns.length - 1);

    let $header = this.$filler.beforeDiv('table-header-item')
      .setEnabled(this.enabled) // enabledComputed not used on purpose
      .data('column', column);

    let margins = graphics.margins($header);
    columnWidth -= margins.horizontal();
    $header.cssMinWidth(columnWidth).cssMaxWidth(columnWidth);

    $header.appendSpan('table-header-item-text');
    if (this.enabled) { // enabledComputed not used on purpose
      $header
        .on('click', this._onHeaderItemClick.bind(this))
        .on('mousedown', this._onHeaderItemMouseDown.bind(this));
    }

    if (this.session.inspector) {
      inspector.applyInfo(column, $header);
    }

    if (isFirstColumn) {
      $header.addClass('first');
    }
    if (isLastColumn) {
      $header.addClass('last');
    }

    column.$header = $header;

    this._installHeaderItemTooltip(column);
    this._decorateHeader(column);

    let showSeparator = column.showSeparator;
    if (isLastColumn && !this.enabled) { // enabledComputed not used on purpose
      showSeparator = false;
    }
    if (showSeparator) {
      let $separator = this.$filler.beforeDiv('table-header-resize');
      if (column.fixedWidth || !this.enabled) { // enabledComputed not used on purpose
        $separator.setEnabled(false);
      } else {
        $separator
          .on('mousedown', '', this._onSeparatorMouseDown.bind(this))
          .on('dblclick', this._onSeparatorDblclick.bind(this));
      }
      column.$separator = $separator;
    }
    this._renderedColumns.push(column);
  }

  _removeColumns() {
    this._renderedColumns.slice().forEach(this._removeColumn, this);
  }

  _removeColumn(column) {
    if (column.$header) {
      column.$header.remove();
      column.$header = null;
    }
    if (column.$separator) {
      column.$separator.remove();
      column.$separator = null;
    }
    arrays.remove(this._renderedColumns, column);
  }

  resizeHeaderItem(column) {
    if (!column) {
      // May be undefined if there are no columns
      return;
    }
    if (!column.$header) {
      // May be undefined if called when header item is not rendered yet (may caused by _adjustColumnMinWidth)
      return;
    }

    let remainingHeaderSpace, adjustment,
      $header = column.$header,
      columnWidth = column._realWidthIfAvailable(),
      margins = graphics.margins($header),
      menuBarWidth = (this.menuBar.visible ? this.$menuBarContainer.outerWidth(true) : 0),
      visibleColumns = this._visibleColumns(),
      visibleColumnIndex = visibleColumns.indexOf(column),
      isLastColumn = visibleColumnIndex === visibleColumns.length - 1;

    columnWidth -= margins.horizontal();

    if (isLastColumn && menuBarWidth > 0) {
      remainingHeaderSpace = this.$container.width() - this.table.rowWidth + graphics.insets(this.$container).right;

      if (remainingHeaderSpace < menuBarWidth) {
        adjustment = menuBarWidth;
        if (column.$separator) {
          adjustment += column.$separator.width();
        }
        if (remainingHeaderSpace > 0) {
          adjustment -= remainingHeaderSpace;
        }

        let origColumnWidth = columnWidth;
        columnWidth = Math.max(columnWidth - adjustment, column.minWidth);
        this.$filler.cssWidth(origColumnWidth - columnWidth);
      }
    }

    $header
      .cssMinWidth(columnWidth)
      .cssMaxWidth(columnWidth);
  }

  /**
   * Resizes all header items to theirs desired widths.
   */
  resizeHeaderItems() {
    this._visibleColumns().forEach(this.resizeHeaderItem.bind(this));
  }

  _reconcileScrollPos() {
    // When scrolling horizontally scroll header as well
    let
      scrollLeft = this.table.get$Scrollable().scrollLeft(),
      lastColumn = this._lastVisibleColumn();

    this.resizeHeaderItem(lastColumn);
    this.$container.scrollLeft(scrollLeft);
    this.$menuBarContainer.cssRight(-1 * scrollLeft);
  }

  _arrangeHeaderItems($headers) {
    let that = this;
    $headers.each(function() {
      // move to old position and then animate
      $(this).css('left', $(this).data('old-pos') - $(this).offset().left)
        .animate({
          left: 0
        }, {
          progress: function(animation, progress, remainingMs) {
            let $headerItem = $(this);
            if (!$headerItem.isSelected()) {
              return;
            }
            // make sure selected header item is visible
            scrollbars.scrollHorizontalTo(that.table.$data, $headerItem);

            // move menu
            if (that.tableHeaderMenu && that.tableHeaderMenu.rendered) {
              that.tableHeaderMenu.position();
            }
          }
        });
    });
  }

  _installHeaderItemTooltip(column) {
    tooltips.install(column.$header, {
      parent: this,
      text: this._headerItemTooltipText.bind(this),
      arrowPosition: 50,
      arrowPositionUnit: '%',
      nativeTooltip: !Device.get().isCustomEllipsisTooltipPossible(),
      htmlEnabled: this._headerItemTooltipHtmlEnabled.bind(this)
    });
  }

  _installHeaderItemTooltips() {
    this._visibleColumns().forEach(this._installHeaderItemTooltip, this);
  }

  _uninstallHeaderItemTooltip(column) {
    tooltips.uninstall(column.$header);
  }

  _uninstallHeaderItemTooltips() {
    this._visibleColumns().forEach(this._uninstallHeaderItemTooltip, this);
  }

  _headerItemTooltipText($col) {
    let column = $col.data('column');
    if (column && strings.hasText(column.headerTooltipText)) {
      return column.headerTooltipText;
    }
    let $text = $col.children('.table-header-item-text');
    if ($text.isContentTruncated() || ($col.width() + $col.position().left) > $col.parent().width()) {
      let text = strings.plainText($text.html(), {
        trim: true
      });
      if (strings.hasText(text)) {
        return text;
      }
    }
    return null;
  }

  _headerItemTooltipHtmlEnabled($col) {
    let column = $col.data('column');
    return column.headerTooltipHtmlEnabled;
  }

  setHeaderMenusEnabled(headerMenusEnabled) {
    this.setProperty('headerMenusEnabled', headerMenusEnabled);
  }

  _renderHeaderMenusEnabled() {
    this._visibleColumns().forEach(function(column) {
      this._decorateHeader(column);
    }, this);
  }

  openHeaderMenu(column) {
    if (this.tableHeaderMenu) {
      // Make sure existing header menu is closed first
      this.closeHeaderMenu();
    }

    this.tableHeaderMenu = column.createTableHeaderMenu(this);
    this.tableHeaderMenu.open();

    // Trigger events on column to make it possible to react to the opening of the menu
    column.trigger('headerMenuOpen', {
      menu: this.tableHeaderMenu
    });
    this.tableHeaderMenu.one('destroy', () => {
      column.trigger('headerMenuClose', {
        menu: this.tableHeaderMenu
      });
    });
  }

  closeHeaderMenu() {
    this.tableHeaderMenu.destroy();
    this.tableHeaderMenu = null;
  }

  onColumnActionsChanged(event) {
    if (this.tableHeaderMenu) {
      this.tableHeaderMenu.onColumnActionsChanged(event);
    }
  }

  findHeaderItems() {
    return this.$container.find('.table-header-item:not(.filler)');
  }

  /**
   * Updates the column headers visualization of the text, sorting and styling state
   * @param [oldColumnState] only necessary when the css class was updated
   */
  updateHeader(column, oldColumnState) {
    if (!column.isVisible()) {
      return;
    }
    this._decorateHeader(column, oldColumnState);
  }

  _decorateHeader(column, oldColumnState) {
    this._renderColumnCssClass(column, oldColumnState);
    this._renderColumnText(column);
    this._renderColumnIconId(column);
    this._renderColumnState(column);
    this._renderColumnLegacyStyle(column);
    this._renderColumnHeaderMenuEnabled(column);
    this._renderColumnHorizontalAlignment(column);
  }

  _renderColumnCssClass(column, oldColumnState) {
    let $header = column.$header;
    if (oldColumnState) {
      $header.removeClass(oldColumnState.headerCssClass);
    }
    $header.addClass(column.headerCssClass);
  }

  _renderColumnText(column) {
    let text = column.text,
      $header = column.$header,
      $headerText = $header.children('.table-header-item-text');

    if (!column.headerHtmlEnabled) {
      text = strings.nl2br(text);
    }
    // Make sure empty header is as height as the others to make it properly clickable
    $headerText.htmlOrNbsp(text, 'empty');
    this._updateColumnIconAndTextStyle(column);
  }

  _renderColumnIconId(column) {
    column.$header.icon(column.headerIconId);
    this._updateColumnIconAndTextStyle(column);
  }

  _renderColumnHorizontalAlignment(column) {
    column.$header.removeClass('halign-left halign-center halign-right');
    column.$header.addClass('halign-' + Table.parseHorizontalAlignment(column.horizontalAlignment));
  }

  _updateColumnIconAndTextStyle(column) {
    let $icon = column.$header.data('$icon'),
      $text = column.$header.children('.table-header-item-text');

    if ($icon) {
      $icon.toggleClass('with-text', !!column.text);
    }
    // Make text invisible if there is an icon but no text
    $text.setVisible(!($icon && $text.html() === '&nbsp;'));
    // Mark icon-only columns to prevent ellipsis (like IconColumn.js does for table cells)
    column.$header.toggleClass('table-header-item-icon-only', !!(column.headerIconId && !column.text));
  }

  _renderColumnLegacyStyle(column) {
    styles.legacyStyle(column, column.$header, 'header');
  }

  _renderColumnHeaderMenuEnabled(column) {
    column.$header.toggleClass('disabled', !this._isHeaderMenuEnabled(column) || !this.enabled); // enabledComputed not used on purpose
  }

  _renderColumnState(column) {
    let sortDirection, $state,
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
      let $left = $state.appendDiv('left');
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
  }

  /**
   * Makes sure state is fully visible by adjusting width (happens if column.minWidth is < DEFAULT_MIN_WIDTH)
   */
  _adjustColumnMinWidth(column) {
    let filtered = this.table.getFilter(column.id);
    if (column.sortActive || column.grouped || filtered) {
      if (column.minWidth < Column.DEFAULT_MIN_WIDTH) {
        column.__minWidthWithoutState = column.minWidth;
        column.__widthWithoutState = column.width;
        column.minWidth = Column.DEFAULT_MIN_WIDTH;
      }
      if (column.width < column.minWidth) {
        this.table.resizeColumn(column, column.minWidth);
      }
    } else {
      // Reset to previous min width if no state is visible
      if (!objects.isNullOrUndefined(column.__minWidthWithoutState)) {
        column.minWidth = column.__minWidthWithoutState;
        // Resize to previous min width, assuming user has not manually changed the size because column is still as width as default_min_width
        if (column.width === Column.DEFAULT_MIN_WIDTH) {
          this.table.resizeColumn(column, column.__widthWithoutState);
        }
        column.__minWidthWithoutState = null;
        column.__widthWithoutState = null;
      }
    }
  }

  updateMenuBar() {
    let menuItems = this.table._filterMenus(this.table.menus, MenuDestinations.HEADER);
    this.menuBar.setHiddenByUi(!this.enabled); // enabledComputed not used on purpose
    this.menuBar.setMenuItems(menuItems);
  }

  _onTableColumnResized(event) {
    let column = event.column,
      lastColumn = this._lastVisibleColumn();
    this.resizeHeaderItem(column);
    if (lastColumn !== column) {
      this.resizeHeaderItem(lastColumn);
    }
  }

  onSortingChanged() {
    this._visibleColumns().forEach(this._renderColumnState, this);
  }

  _onTableColumnMoved(event) {
    let
      column = event.column,
      oldPos = event.oldPos,
      newPos = event.newPos,
      $header = column.$header,
      $headers = this.findHeaderItems(),
      $moveHeader = $headers.eq(oldPos),
      $moveResize = $moveHeader.next('.table-header-resize'),
      visibleColumns = this._visibleColumns(),
      lastColumnPos = visibleColumns.length - 1;

    // store old position of header
    $headers.each(function() {
      $(this).data('old-pos', $(this).offset().left);
    });

    // change order in dom of header
    if (newPos < oldPos) {
      $headers.eq(newPos).before($moveHeader);
    } else {
      $headers.eq(newPos).after($moveHeader);
      $moveHeader.before($moveHeader.next('.table-header-resize'));
    }
    // The resizer belongs to a column which is especially relevant for fixed width columns where resizer is disabled -> ensure it is always positioned after the header
    $moveHeader.after($moveResize);

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
      visibleColumns.forEach(column => {
        this.resizeHeaderItem(column);
      });
    }

    // move to old position and then animate
    if (event.dragged) {
      $header.css('left', parseInt($header.css('left'), 0) + $header.data('old-pos') - $header.offset().left)
        .addClass('releasing')
        .animateAVCSD('left', 0, () => $header.removeClass('releasing'));
    } else {
      this._arrangeHeaderItems($headers);
    }
  }

  _visibleColumns() {
    return this.table.visibleColumns();
  }

  _lastVisibleColumn() {
    return arrays.last(this._visibleColumns());
  }

  onOrderChanged(oldColumnOrder) {
    let $header, $headerResize;
    let $headers = this.findHeaderItems();

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

    // ensure filler is at the end
    this.$container.append(this.$filler);

    this._arrangeHeaderItems($headers);
  }

  /**
   * Header menus are enabled when property is enabled on the header itself and on the column too.
   */
  _isHeaderMenuEnabled(column) {
    return !!(column.headerMenuEnabled && this.headerMenusEnabled);
  }

  _onHeaderItemClick(event) {
    let $headerItem = $(event.currentTarget),
      column = $headerItem.data('column');

    if (this.dragging || this.columnMoved) {
      this.dragging = false;
      this.columnMoved = false;
    } else if (this.table.sortEnabled && (event.shiftKey || event.ctrlKey || !this._isHeaderMenuEnabled(column))) {
      this.table.removeColumnGrouping();
      this.table.sort(column, $headerItem.hasClass('sort-asc') ? 'desc' : 'asc', event.shiftKey);
    } else if (this.tableHeaderMenu && this.tableHeaderMenu.isOpenFor($headerItem)) {
      this.closeHeaderMenu();
    } else if (this._isHeaderMenuEnabled(column)) {
      this.openHeaderMenu(column);
    }

    return false;
  }

  _onHeaderItemMouseDown(event) {
    if (event.button > 0) {
      return; // ignore buttons other than the main (left) mouse button
    }

    let diff = 0,
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
      if (-2 < diff && diff < 2) {
        // Don't move if it was no movement or just a very small one
        return;
      }

      that.dragging = true;

      // change css of dragged header
      $header.addClass('moving');
      that.$container && that.$container.addClass('moving');

      // move dragged header
      $header.css('left', diff);

      // find other affected headers
      let middle = realMiddle($header);

      $otherHeaders.each(function(i) {
        let $otherHeader = $(this);
        let m = realMiddle($otherHeader);
        if (middle < m && i < oldPos) {
          $otherHeader.css('left', move);
        } else if (middle > m && i >= oldPos) {
          $otherHeader.css('left', -move);
        } else {
          $otherHeader.css('left', 0);
        }
      });

      if (that.tableHeaderMenu) {
        that.tableHeaderMenu.destroy();
        that.tableHeaderMenu = null;
      }

      // Don't show tooltips while dragging
      that.rendered && that._uninstallHeaderItemTooltips();
    }

    function realWidth($div) {
      let html = $div.html(),
        width = $div.html('<span>' + html + '</span>').find('span:first').width();

      $div.html(html);
      return width;
    }

    /**
     * @return {number} the middle of the text (not the middle of the whole header item)
     */
    function realMiddle($div) {
      if ($div.hasClass('halign-right')) {
        return $div.offset().left + $div.outerWidth() - realWidth($div) / 2;
      }
      return $div.offset().left + realWidth($div) / 2;
    }

    function dragEnd(event) {
      that._$window && that._$window.off('mousemove.tableheader');

      // in case of no movement: return
      if (!that.dragging) {
        return true;
      }

      // find new position of dragged header
      let h = (diff < 0) ? $otherHeaders : $($otherHeaders.get().reverse());
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
        $header.addClass('releasing');
        $header.animateAVCSD('left', '', () => {
          that.dragging = false;
          $header.removeClass('releasing');
        });
      }

      // reset css of dragged header
      $otherHeaders.each(function() {
        $(this).css('left', '');
      });

      $header.removeClass('moving');
      that.$container && that.$container.removeClass('moving');

      // Reinstall tooltips
      that.rendered && that._installHeaderItemTooltips();
    }
  }

  _onSeparatorDblclick(event) {
    if (event.button > 0) {
      return; // ignore buttons other than the main (left) mouse button
    }

    if (event.shiftKey) {
      // Optimize all columns
      this._visibleColumns().forEach(function(column) {
        this.table.resizeToFit(column);
      }, this);
    } else {
      // Optimize the column left of the separator
      let $header = $(event.target).prev(),
        column = $header.data('column');
      this.table.resizeToFit(column);
    }
  }

  _onSeparatorMouseDown(event) {
    if (event.button > 0) {
      return; // ignore buttons other than the main (left) mouse button
    }

    let startX = Math.floor(event.pageX),
      $header = $(event.target).prev(),
      column = $header.data('column'),
      that = this,
      headerWidth = column.width;

    // Install resize helpers. Those helpers make sure the header and the data element keep their
    // current width until the resizing has finished. Otherwise, make a column smaller while the
    // table has been horizontally scrolled to the right would behave very strange.
    let $headerColumnResizedHelper = this.$container
      .appendDiv('table-column-resize-helper')
      .css('width', this.table.rowWidth + this.table.rowBorders.horizontal());
    let $dataColumnResizedHelper = this.table.$data
      .appendDiv('table-column-resize-helper')
      .css('width', this.table.rowWidth);

    this._$window
      .on('mousemove.tableheader', resizeMove)
      .one('mouseup', resizeEnd);
    this._$body.addClass('col-resize');

    // Prevent text selection in a form, don't stop propagation to allow others (e.g. cell editor) to react
    event.preventDefault();

    function resizeMove(event) {
      let diff = Math.floor(event.pageX) - startX,
        wHeader = headerWidth + diff;

      wHeader = Math.max(wHeader, column.minWidth);
      if (that.rendered && wHeader !== column.width) {
        that.table.resizeColumn(column, wHeader);
      }
    }

    function resizeEnd(event) {
      // Remove resize helpers
      $headerColumnResizedHelper.remove();
      $dataColumnResizedHelper.remove();

      that._$window && that._$window.off('mousemove.tableheader');
      that._$body && that._$body.removeClass('col-resize');

      if (that.table.rendered && column.width !== headerWidth) {
        that.table.resizeColumn(column, column.width);
      }
    }
  }

  _onTableDataScroll() {
    scrollbars.fix(this.$menuBarContainer);
    this._reconcileScrollPos();
    this._fixTimeout = scrollbars.unfix(this.$menuBarContainer, this._fixTimeout);
  }

  _onMenuBarPropertyChange(event) {
    if (this.rendered && event.propertyName === 'visible') {
      this.$menuBarContainer.setVisible(event.newValue);
    }
  }

  _onTableAddFilterRemoved(event) {
    let column = event.filter.column;
    // Check for column.$header because column may have been removed in the mean time due to a structure changed event -> don't try to render state
    if (event.filter.filterType === ColumnUserFilter.TYPE && column.$header) {
      this._renderColumnState(column);
    }
  }
}
