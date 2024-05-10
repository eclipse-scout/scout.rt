/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  aria, arrays, Column, ColumnModel, ColumnUserFilter, Device, EventHandler, graphics, GroupBoxMenuItemsOrder, InitModelOf, inspector, MenuBar, MenuDestinations, objects, PropertyChangeEvent, Rectangle, scout, scrollbars, SomeRequired,
  strings, styles, Table, TableColumnMovedEvent, TableColumnResizedEvent, TableFilterAddedEvent, TableFilterRemovedEvent, TableHeaderEventMap, TableHeaderMenu, TableHeaderModel, tooltips, Widget, widgets
} from '../index';
import $ from 'jquery';

export class TableHeader extends Widget implements TableHeaderModel {
  declare model: TableHeaderModel;
  declare initModel: SomeRequired<this['model'], 'parent' | 'table'>;
  declare eventMap: TableHeaderEventMap;
  declare self: TableHeader;

  headerMenusEnabled: boolean;
  table: Table;
  dragging: boolean;
  columnMoved: boolean;
  menuBar: MenuBar;
  tableHeaderMenu: TableHeaderMenu;
  headerLabelId: string;
  $menuBarContainer: JQuery;
  $filler: JQuery;

  protected _tableDataScrollHandler: () => void;
  protected _tableAddFilterRemovedHandler: EventHandler<TableFilterAddedEvent | TableFilterRemovedEvent>;
  protected _tableColumnResizedHandler: EventHandler<TableColumnResizedEvent>;
  protected _tableColumnMovedHandler: EventHandler<TableColumnMovedEvent>;
  protected _renderedColumns: Column<any>[];
  protected _$window: JQuery<Window>;
  protected _$body: JQuery<Body>;
  protected _fixTimeout: number;

  constructor() {
    super();

    this.dragging = false;
    this.headerMenusEnabled = true;
    this.table = null;
    this.headerLabelId = null;
    this._tableDataScrollHandler = this._onTableDataScroll.bind(this);
    this._tableAddFilterRemovedHandler = this._onTableAddFilterRemoved.bind(this);
    this._tableColumnResizedHandler = this._onTableColumnResized.bind(this);
    this._tableColumnMovedHandler = this._onTableColumnMoved.bind(this);
    this._renderedColumns = [];
  }

  protected override _init(options: InitModelOf<this>) {
    super._init(options);

    this.menuBar = scout.create(MenuBar, {
      parent: this,
      tabbable: false,
      position: MenuBar.Position.BOTTOM,
      menuOrder: new GroupBoxMenuItemsOrder()
    });
    this.menuBar.on('propertyChange', this._onMenuBarPropertyChange.bind(this));
    this.updateMenuBar();
  }

  protected override _render() {
    this.$container = this.table.$data.beforeDiv('table-header')
      .cssBorderLeftWidth(this.table.rowBorders.left || '');
    aria.role(this.$container, 'row');

    // Filler is necessary to make sure the header is always as large as the table data, otherwise horizontal scrolling does not work correctly
    this.$filler = this.$container.appendDiv('table-header-item filler').css('visibility', 'hidden');

    // Required to make "height: 100%" rule work. menuBarContainer and menuBar itself must have the same visibility.
    // Otherwise, they could cover the sorting/filter icons on the table-header of the column.
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

  protected override _remove() {
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

  protected _renderColumns() {
    let visibleColumns = this._visibleColumns();
    visibleColumns.forEach(this._renderColumn, this);
    if (visibleColumns.length === 0) {
      // If there are no columns, make the filler visible and make sure the header is as large as normally using nbsp
      this.$filler.css('visibility', 'visible').html('&nbsp;').addClass('empty');
    }
    this._reconcileScrollPos();
  }

  protected _renderColumn(column: Column<any>, index: number) {
    let columnWidth = column.realWidthIfAvailable(),
      visibleColumns = this._visibleColumns(),
      isFirstColumn = (index === 0),
      isLastColumn = (index === visibleColumns.length - 1);

    let $header = this.$filler.beforeDiv('table-header-item')
      .setEnabled(this.enabled) // enabledComputed not used on purpose
      .data('column', column);

    aria.role($header, 'columnheader');

    let margins = graphics.margins($header);
    columnWidth -= margins.horizontal();
    $header.cssMinWidth(columnWidth).cssMaxWidth(columnWidth);

    // add label id to header item text, so table cells can reference it for screen readers
    this.headerLabelId = widgets.createUniqueId('lbl');
    $header.appendSpan('table-header-item-text').attr('id', this.headerLabelId);

    if (this.enabled) { // enabledComputed not used on purpose
      $header
        .on('click', this._onHeaderItemClick.bind(this))
        .on('mousedown', this._onHeaderItemMouseDown.bind(this));
    }

    $header.attrOrRemove('data-uuid', column.uuid);
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
      aria.role($separator, 'none');
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

  protected _removeColumns() {
    this._renderedColumns.slice().forEach(this._removeColumn, this);
  }

  protected _removeColumn(column: Column<any>) {
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

  resizeHeaderItem(column: Column<any>) {
    if (!column) {
      // May be undefined if there are no columns
      return;
    }
    if (!column.$header) {
      // May be undefined if called when header item is not rendered yet (e.g. coming from _adjustColumnMinWidth)
      return;
    }

    let $header = column.$header,
      columnWidth = column.realWidthIfAvailable(),
      margins = graphics.margins($header),
      menuBarWidth = (this.menuBar.visible ? this.$menuBarContainer.outerWidth(true) : 0),
      visibleColumns = this._visibleColumns(),
      visibleColumnIndex = visibleColumns.indexOf(column),
      isLastColumn = visibleColumnIndex === visibleColumns.length - 1;

    columnWidth -= margins.horizontal();

    if (isLastColumn && menuBarWidth > 0) {
      let remainingHeaderSpace = this.$container.width() - this.table.rowWidth + graphics.insets(this.$container).right;

      if (remainingHeaderSpace < menuBarWidth) {
        let adjustment = menuBarWidth;
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

  protected _reconcileScrollPos() {
    // When scrolling horizontally scroll header as well
    let
      scrollLeft = this.table.get$Scrollable().scrollLeft(),
      lastColumn = this._lastVisibleColumn();

    this.resizeHeaderItem(lastColumn);
    this.$container.scrollLeft(scrollLeft);
    this.$menuBarContainer.cssRight(-1 * scrollLeft);
  }

  protected _arrangeHeaderItems($headers: JQuery) {
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

  protected _installHeaderItemTooltip(column: Column<any>) {
    tooltips.install(column.$header, {
      parent: this,
      text: this._headerItemTooltipText.bind(this),
      arrowPosition: 50,
      arrowPositionUnit: '%',
      originProducer: this._headerItemTooltipOrigin.bind(this),
      nativeTooltip: !Device.get().isCustomEllipsisTooltipPossible(),
      htmlEnabled: this._headerItemTooltipHtmlEnabled.bind(this)
    });
  }

  protected _installHeaderItemTooltips() {
    this._visibleColumns().forEach(this._installHeaderItemTooltip, this);
  }

  protected _uninstallHeaderItemTooltip(column: Column<any>) {
    tooltips.uninstall(column.$header);
  }

  protected _uninstallHeaderItemTooltips() {
    this._visibleColumns().forEach(this._uninstallHeaderItemTooltip, this);
  }

  protected _headerItemTooltipText($col: JQuery): string {
    let column = $col.data('column') as Column<any>;
    if (column && strings.hasText(column.headerTooltipText)) {
      return column.headerTooltipText;
    }
    let $text = $col.children('.table-header-item-text');
    if ($text.isContentTruncated() || !this._textInView($text)) {
      // Show a tooltip if the content is truncated (text shows an ellipsis) or if the text is partially out of view because of the horizontal scroll position of the table
      let text = strings.plainText($text.html(), {trim: true});
      if (strings.hasText(text)) {
        return text;
      }
    }
    return null;
  }

  protected _textInView($text: JQuery): boolean {
    let textBounds = graphics.offsetBounds($text);
    let containerBounds = this._offsetBoundsWithoutMenuBar();
    return textBounds.right() <= containerBounds.right() && textBounds.x >= containerBounds.x;
  }

  /**
   * @returns the part of the header item that is visible in the current viewport of the table so the tooltip won't point to an invisible part of the header item
   */
  protected _headerItemTooltipOrigin($col: JQuery): Rectangle {
    let headerItemBounds = graphics.offsetBounds($col);
    let containerBounds = this._offsetBoundsWithoutMenuBar();
    return containerBounds.intersect(headerItemBounds);
  }

  protected _offsetBoundsWithoutMenuBar(): Rectangle {
    let containerBounds = graphics.offsetBounds(this.$container);
    containerBounds.width -= graphics.size(this.$menuBarContainer).width;
    return containerBounds;
  }

  protected _headerItemTooltipHtmlEnabled($col: JQuery): boolean {
    let column = $col.data('column') as Column<any>;
    return column.headerTooltipHtmlEnabled;
  }

  setHeaderMenusEnabled(headerMenusEnabled: boolean) {
    this.setProperty('headerMenusEnabled', headerMenusEnabled);
  }

  protected _renderHeaderMenusEnabled() {
    this._visibleColumns().forEach(column => this._decorateHeader(column));
  }

  openHeaderMenu(column: Column<any>) {
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

  findHeaderItems(): JQuery {
    return this.$container.find('.table-header-item:not(.filler)');
  }

  /**
   * Updates the column headers visualization of the text, sorting and styling state
   * @param oldColumnState only necessary when the css class was updated
   */
  updateHeader(column: Column<any>, oldColumnState?: ColumnModel<any>) {
    if (!column.visible) {
      return;
    }
    this._decorateHeader(column, oldColumnState);
  }

  protected _decorateHeader(column: Column<any>, oldColumnState?: ColumnModel<any>) {
    this._renderColumnCssClass(column, oldColumnState);
    this._renderColumnText(column);
    this._renderColumnIconId(column);
    this._renderColumnState(column);
    this._renderColumnLegacyStyle(column);
    this._renderColumnHeaderMenuEnabled(column);
    this._renderColumnHorizontalAlignment(column);
    this._renderColumnTooltipText(column);
  }

  protected _renderColumnCssClass(column: Column<any>, oldColumnState?: ColumnModel<any>) {
    let $header = column.$header;
    if (oldColumnState) {
      $header.removeClass(oldColumnState.headerCssClass);
    }
    $header.addClass(column.headerCssClass);
  }

  protected _renderColumnText(column: Column<any>) {
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

  protected _renderColumnTooltipText(column: Column<any>) {
    // add tooltip as invisible text for screen readers
    if (column.$header && strings.hasText(column.headerTooltipText)) {
      let $descriptionElement = column.$header.appendDiv().addClass('text').text(column.headerTooltipText);
      aria.screenReaderOnly($descriptionElement);
    }
  }

  protected _renderColumnIconId(column: Column<any>) {
    column.$header.icon(column.headerIconId);
    this._updateColumnIconAndTextStyle(column);
  }

  protected _renderColumnHorizontalAlignment(column: Column<any>) {
    column.$header.removeClass('halign-left halign-center halign-right');
    column.$header.addClass('halign-' + Table.parseHorizontalAlignment(column.horizontalAlignment));
  }

  protected _updateColumnIconAndTextStyle(column: Column<any>) {
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

  protected _renderColumnLegacyStyle(column: Column<any>) {
    styles.legacyStyle(column, column.$header, 'header');
  }

  protected _renderColumnHeaderMenuEnabled(column: Column<any>) {
    column.$header.toggleClass('disabled', !this._isHeaderMenuEnabled(column) || !this.enabled); // enabledComputed not used on purpose
  }

  protected _renderColumnState(column: Column<any>) {
    let $header = column.$header,
      filtered = this.table.getFilter(column.id);

    $header.children('.table-header-item-state').remove();
    let $state = $header.appendSpan('table-header-item-state');
    $state.empty();
    $header.removeClass('sort-asc sort-desc sorted group-asc group-desc grouped filtered');
    $state.removeClass('sort-asc sort-desc sorted group-asc group-desc grouped filtered');

    let accessibleStateText = '';
    if (column.sortActive) {
      let sortDirection = column.sortAscending ? 'asc' : 'desc';
      accessibleStateText += this.session.text('ui.Sorting') + ' ' + (column.sortAscending ? this.session.text('ui.ascending') : this.session.text('ui.descending'));
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
        let $g = $left.appendDiv().text('G');
        aria.hidden($g, true);
        accessibleStateText += ' ' + this.session.text('ui.Grouping');
      }
      if (filtered) {
        $header.addClass('filtered');
        $state.addClass('filtered');
        let $f = $left.appendDiv().text('F');
        aria.hidden($f, true);
        accessibleStateText += ' ' + this.session.text('ui.Filter');
      }
    }
    let $accessibleState = $state.appendDiv().addClass('text').text(accessibleStateText);
    aria.screenReaderOnly($accessibleState);
    // Contains sort arrow
    let sortArrow = $state.appendDiv('right');
    aria.hidden(sortArrow, true);

    this._adjustColumnMinWidth(column);
  }

  /**
   * Makes sure state is fully visible by adjusting width (happens if column.minWidth is < DEFAULT_MIN_WIDTH)
   */
  protected _adjustColumnMinWidth(column: Column<any> & { __minWidthWithoutState?: number; __widthWithoutState?: number }) {
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

  protected _onTableColumnResized(event: TableColumnResizedEvent) {
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

  protected _onTableColumnMoved(event: TableColumnMovedEvent) {
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
      visibleColumns.forEach(column => this.resizeHeaderItem(column));
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

  protected _visibleColumns(): Column<any>[] {
    return this.table.visibleColumns();
  }

  protected _lastVisibleColumn(): Column<any> {
    return arrays.last(this._visibleColumns());
  }

  onOrderChanged(oldColumnOrder: Column<any>[]) {
    let $headers = this.findHeaderItems();

    // store old position of headers
    $headers.each(function() {
      $(this).data('old-pos', $(this).offset().left);
    });

    // change order in dom of header
    this._visibleColumns().forEach(column => {
      let $header = column.$header;
      let $headerResize = $header.next('.table-header-resize');

      this.$container.append($header);
      this.$container.append($headerResize);
    });

    // ensure filler is at the end
    this.$container.append(this.$filler);

    this._arrangeHeaderItems($headers);
  }

  /**
   * Header menus are enabled when property is enabled on the header itself and on the column too.
   */
  protected _isHeaderMenuEnabled(column: Column<any>): boolean {
    return !!(column.headerMenuEnabled && this.headerMenusEnabled);
  }

  protected _onHeaderItemClick(event: JQuery.ClickEvent): boolean {
    let $headerItem = $(event.currentTarget),
      column = $headerItem.data('column') as Column<any>;

    if (this.dragging || this.columnMoved) {
      this.dragging = false;
      this.columnMoved = false;
    } else if (this.table.sortEnabled && (event.shiftKey || event.ctrlKey || !this._isHeaderMenuEnabled(column))) {
      this.table.sort(column, $headerItem.hasClass('sort-asc') ? 'desc' : 'asc', event.shiftKey);
    } else if (this.tableHeaderMenu && this.tableHeaderMenu.isOpenFor($headerItem)) {
      this.closeHeaderMenu();
    } else if (this._isHeaderMenuEnabled(column)) {
      this.openHeaderMenu(column);
    }

    return false;
  }

  protected _onHeaderItemMouseDown(event: JQuery.MouseDownEvent) {
    if (event.button > 0) {
      return; // ignore buttons other than the main (left) mouse button
    }

    let diff = 0,
      that = this,
      startX = Math.floor(event.pageX),
      $header = $(event.currentTarget),
      column = $header.data('column') as Column<any>,
      oldPos = this._visibleColumns().indexOf(column),
      newPos = oldPos,
      move = $header.outerWidth(),
      $otherHeaders = $header.siblings('.table-header-item:not(.filler)');

    if (column.fixedPosition) {
      // Don't allow moving a column with fixed position
      return;
    }

    this.dragging = false;
    // firefox fires a click action after a column has been dropped at the new location, chrome doesn't -> we need a hint to avoid menu gets opened after drop
    this.columnMoved = false;

    // start drag & drop events
    this._$window
      .on('mousemove.tableheader', '', dragMove)
      .one('mouseup', '', dragEnd);

    function dragMove(event: JQuery.MouseMoveEvent) {
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

    function realWidth($div: JQuery): number {
      let html = $div.html(),
        width = $div.html('<span>' + html + '</span>').find('span:first').width();

      $div.html(html);
      return width;
    }

    /**
     * @returns the middle of the text (not the middle of the whole header item)
     */
    function realMiddle($div: JQuery): number {
      if ($div.hasClass('halign-right')) {
        return $div.offset().left + $div.outerWidth() - realWidth($div) / 2;
      }
      return $div.offset().left + realWidth($div) / 2;
    }

    function dragEnd(event: JQuery.MouseUpEvent): boolean {
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

  protected _onSeparatorDblclick(event: JQuery.DoubleClickEvent) {
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

  protected _onSeparatorMouseDown(event: JQuery.MouseDownEvent) {
    if (event.button > 0) {
      return; // ignore buttons other than the main (left) mouse button
    }

    let startX = Math.floor(event.pageX),
      $header = $(event.target).prev(),
      column = $header.data('column') as Column<any>,
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

    function resizeMove(event: JQuery.MouseMoveEvent) {
      let diff = Math.floor(event.pageX) - startX,
        wHeader = headerWidth + diff;

      wHeader = Math.max(wHeader, column.minWidth);
      if (that.rendered && wHeader !== column.width) {
        that.table.resizeColumn(column, wHeader);
      }
    }

    function resizeEnd(event: JQuery.MouseUpEvent) {
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

  protected _onTableDataScroll() {
    scrollbars.fix(this.$menuBarContainer);
    this._reconcileScrollPos();
    this._fixTimeout = scrollbars.unfix(this.$menuBarContainer, this._fixTimeout);
  }

  protected _onMenuBarPropertyChange(event: PropertyChangeEvent<any, MenuBar>) {
    if (this.rendered && event.propertyName === 'visible') {
      this.$menuBarContainer.setVisible(event.newValue);
    }
  }

  protected _onTableAddFilterRemoved(event: TableFilterAddedEvent | TableFilterRemovedEvent) {
    if (!(event.filter instanceof ColumnUserFilter)) {
      return;
    }
    let column = event.filter.column;
    // Check for column.$header because column may have been removed in the meantime due to a structure changed event -> don't try to render state
    if (event.filter.filterType === ColumnUserFilter.TYPE && column.$header) {
      this._renderColumnState(column);
    }
  }
}
