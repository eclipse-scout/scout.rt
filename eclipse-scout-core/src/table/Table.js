/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AggregateTableControl, AppLinkKeyStroke, arrays, BooleanColumn, clipboard, Column, ContextMenuKeyStroke, ContextMenuPopup, Device, DoubleClickSupport, dragAndDrop, Event, FilterSupport, graphics, HtmlComponent, Insets, KeyStrokeContext, LoadingSupport, MenuBar, MenuDestinations, MenuItemsOrder, menus, NumberColumn, objects, Range, scout, scrollbars, Status, strings, styles, TableCopyKeyStroke, TableLayout, TableNavigationCollapseKeyStroke, TableNavigationDownKeyStroke, TableNavigationEndKeyStroke, TableNavigationExpandKeyStroke, TableNavigationHomeKeyStroke, TableNavigationPageDownKeyStroke, TableNavigationPageUpKeyStroke, TableNavigationUpKeyStroke, TableRefreshKeyStroke, TableRow, TableSelectAllKeyStroke, TableSelectionHandler, TableStartCellEditKeyStroke, TableToggleRowKeyStroke, TableUpdateBuffer, TableUserFilter, tooltips as tooltips_1, Widget} from '../index';
import $ from 'jquery';

export default class Table extends Widget {
  constructor() {

    super();

    this.autoResizeColumns = false;
    this.columnAddable = false;
    this.columnLayoutDirty = false;
    this.columns = [];
    this.contextColumn = null;
    this.checkable = false;
    this.checkableStyle = Table.CheckableStyle.CHECKBOX;
    this.cellEditorPopup = null;
    this.compact = false;
    this.compactHandler = scout.create('TableCompactHandler', {table: this});
    this.compactColumn = null;
    this.dropType = 0;
    this.dropMaximumSize = dragAndDrop.DEFAULT_DROP_MAXIMUM_SIZE;
    this.groupingStyle = Table.GroupingStyle.TOP;
    this.header = null;
    this.headerEnabled = true;
    this.headerVisible = true;
    this.headerMenusEnabled = true;
    this.hasReloadHandler = false;
    this.hierarchical = false;
    this.hierarchicalStyle = Table.HierarchicalStyle.DEFAULT;
    this.keyStrokes = [];
    this.menus = [];
    this.menuBar = null;
    this.menuBarVisible = true;
    this.contextMenu = null;
    this.multiCheck = true;
    this.multiSelect = true;
    this.multilineText = false;
    this.scrollToSelection = false;
    this.scrollTop = 0;
    this.selectedRows = [];
    this.sortEnabled = true;
    this.tableControls = [];
    this.tableStatusVisible = false;
    this.tableTileGridMediator = null;
    this.tileMode = false;
    this.tileTableHeader = null;
    this.footer = null;
    this.footerVisible = false;
    this.filters = [];
    this.rows = [];
    this.rootRows = [];
    this.visibleRows = [];
    this.estimatedRowCount = 0;
    this.maxRowCount = 0;
    this.truncatedCellTooltipEnabled = null;
    this.visibleRowsMap = {}; // visible rows by id
    this.rowLevelPadding = 0;
    this.rowsMap = {}; // rows by id
    this.rowHeight = 0;
    this.rowWidth = 0;
    this.rowInsets = new Insets(); // read-only, set by _calculateRowInsets(), also used in TableHeader.js
    this.rowMargins = new Insets(); // read-only, set by _calculateRowInsets(), also used in TableLayout.js
    this.rowIconVisible = false;
    this.rowIconColumnWidth = Column.NARROW_MIN_WIDTH;
    this.staticMenus = [];
    this.selectionHandler = new TableSelectionHandler(this);
    this.tooltips = [];
    this._filteredRows = [];
    this.tableNodeColumn = null;
    this._maxLevel = 0;
    this._aggregateRows = [];
    this._animationRowLimit = 25;
    this._blockLoadThreshold = 25;
    this.updateBuffer = new TableUpdateBuffer(this);
    // Initial value must be > 0 to make prefSize work (if it is 0, no filler will be generated).
    // If rows have a variable height, prefSize is only correct for 10 rows.
    // Layout will adjust this value depending on the view port size.
    this.viewRangeSize = 10;
    this.viewRangeDirty = false;
    this.viewRangeRendered = new Range(0, 0);
    this.virtual = true;

    this.textFilterEnabled = true;
    this.filterSupport = this._createFilterSupport();
    this.filteredElementsDirty = false;

    this._doubleClickSupport = new DoubleClickSupport();
    this._permanentHeadSortColumns = [];
    this._permanentTailSortColumns = [];
    this._filterMenusHandler = this._filterMenus.bind(this);
    this._popupOpenHandler = this._onDesktopPopupOpen.bind(this);
    this._rerenderViewPortAfterAttach = false;
    this._renderViewPortAfterAttach = false;
    this._postAttachActions = [];
    this._desktopPropertyChangeHandler = this._onDesktopPropertyChange.bind(this);
    this._addWidgetProperties(['tableControls', 'menus', 'keyStrokes', 'staticMenus', 'tileTableHeader', 'tableTileGridMediator']);

    this.$data = null;
    this.$emptyData = null;
    this.$fillBefore = null;
    this.$fillAfter = null;
  }

  // TODO [7.0] cgu create StringColumn.js incl. defaultValues from defaultValues.json

  static HierarchicalStyle = {
    DEFAULT: 'default',
    STRUCTURED: 'structured'
  };

  static GroupingStyle = {
    /**
     * Aggregate row is rendered on top of the row-group.
     */
    TOP: 'top',
    /**
     * Aggregate row is rendered on the bottom of the row-group (default).
     */
    BOTTOM: 'bottom'
  };

  static CheckableStyle = {
    /**
     * When row is checked a boolean column with a checkbox is inserted into the table.
     */
    CHECKBOX: 'checkbox',
    /**
     * When a row is checked the table-row is marked as checked. By default a background
     * color is set on the table-row when the row is checked.
     */
    TABLE_ROW: 'tableRow',
    /**
     * Like the CHECKBOX Style but a click anywhere on the row triggers the check.
     */
    CHECKBOX_TABLE_ROW: 'checkbox_table_row'
  };

  /**
   * This enum defines the reload reasons for a table reload operation
   */
  static ReloadReason = {
    /**
     * No specific reason, just reload data using the current search settings, the current row limits and the current
     * filter (Default)
     */
    UNSPECIFIED: 'unspecified',

    /**
     * Some search parameters changed or the search was reset and the search was triggered
     */
    SEARCH: 'search',

    /**
     * The user requested loading more data than his soft limit, up to the application specific hard limit
     */
    OVERRIDE_ROW_LIMIT: 'overrideRowLimit',

    /**
     * The user requested loading no more data than his soft limit;
     */
    RESET_ROW_LIMIT: 'resetRowLimit',

    /**
     * The column structure of the table was changed
     */
    ORGANIZE_COLUMNS: 'organizeColumns',

    /**
     * Any call to IPage#dataChanged
     */
    DATA_CHANGED_TRIGGER: 'dataChangedTrigger'
  };

  static SELECTION_CLASSES = 'select-middle select-top select-bottom select-single selected';

  _init(model) {
    super._init(model);
    this.resolveConsts([{
      property: 'hierarchicalStyle',
      constType: Table.HierarchicalStyle
    }, {
      property: 'checkableStyle',
      constType: Table.CheckableStyle
    }, {
      property: 'groupingStyle',
      constType: Table.GroupingStyle
    }]);
    this._initColumns();

    this.rows.forEach(function(row, i) {
      this.rows[i] = this._initRow(row);
    }, this);

    this.setFilters(this.filters);

    this._updateRowStructure({
      updateTree: true
    });

    this.menuBar = this._createMenuBar();
    this.menuBar.on('propertyChange:visible', () => this._refreshMenuBarClasses());
    this._setSelectedRows(this.selectedRows);
    this._setKeyStrokes(this.keyStrokes);
    this._setMenus(this.menus);
    this._setTableControls(this.tableControls);
    this._setTableStatus(this.tableStatus);
    this._calculateValuesForBackgroundEffect();
    this._group();
    this._setTileMode(this.tileMode);
    this._setTileTableHeader(this.tileTableHeader);
  }

  _initRow(row) {
    if (!(row instanceof TableRow)) {
      row.parent = this;
      row = scout.create('TableRow', row);
    }
    this.rowsMap[row.id] = row;
    this.trigger('rowInit', {
      row: row
    });
    return row;
  }

  _initColumns() {
    this.columns = this.columns.map(function(colModel, index) {
      let column = colModel;
      column.session = this.session;
      if (column instanceof Column) {
        column._setTable(this);
      } else {
        column.table = this;
        column = scout.create(column);
      }

      if (column.index < 0) {
        column.index = index;
      }
      if (column.checkable) {
        // set checkable column if this column is the checkable one
        this.checkableColumn = column;
      }
      return column;
    }, this);

    // Add gui only checkbox column at the beginning
    this._setCheckable(this.checkable);

    // Add gui only row icon column at the beginning
    if (this.rowIconVisible) {
      this._insertRowIconColumn();
    }

    this._setCompact(this.compact);
    this._calculateTableNodeColumn();

    // Sync head and tail sort columns
    this._setHeadAndTailSortColumns();
    this.columnLayoutDirty = true;
  }

  _destroy() {
    this._destroyColumns();
    super._destroy();
  }

  _destroyColumns() {
    this.columns.forEach(column => {
      column.destroy();
    });
    this.checkableColumn = null;
    this.compactColumn = null;
    this.rowIconColumn = null;
    this.columns = [];
  }

  _calculateTableNodeColumn() {
    let candidateColumns = this.visibleColumns().filter(column => {
      return column.nodeColumnCandidate;
    });

    let tableNodeColumn = arrays.first(candidateColumns);
    if (this.tableNodeColumn && this.tableNodeColumn !== tableNodeColumn) {
      // restore
      this.tableNodeColumn.minWidth = this.tableNodeColumn._initialMinWidth;
    }
    this.tableNodeColumn = tableNodeColumn;
    if (this.tableNodeColumn) {
      this.tableNodeColumn._initialMinWidth = this.tableNodeColumn.minWidth;
      this.tableNodeColumn.minWidth = this.rowLevelPadding * this._maxLevel + this.tableNodeColumn.tableNodeLevel0CellPadding + 8;

      if (this.tableNodeColumn.minWidth > this.tableNodeColumn.width) {
        if (this._isDataRendered()) {
          this.resizeColumn(this.tableNodeColumn, this.tableNodeColumn.minWidth);
        } else {
          this.tableNodeColumn.width = this.tableNodeColumn.minWidth;
        }
      }
    }
  }

  /**
   * @override
   */
  _createLoadingSupport() {
    // noinspection JSCheckFunctionSignatures
    return new LoadingSupport({
      widget: this,
      $container: () => {
        if (this.$container.hasClass('knight-rider-loading')) {
          return this.$container;
        }
        return this.$data;
      }
    });
  }

  /**
   * @override
   */
  _createKeyStrokeContext() {
    return new KeyStrokeContext();
  }

  /**
   * @override
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this._initTableKeyStrokeContext();
  }

  _initTableKeyStrokeContext() {
    this.keyStrokeContext.registerKeyStroke([
      new TableNavigationUpKeyStroke(this),
      new TableNavigationDownKeyStroke(this),
      new TableNavigationPageUpKeyStroke(this),
      new TableNavigationPageDownKeyStroke(this),
      new TableNavigationHomeKeyStroke(this),
      new TableNavigationEndKeyStroke(this),
      new TableNavigationCollapseKeyStroke(this),
      new TableNavigationExpandKeyStroke(this),
      new TableStartCellEditKeyStroke(this),
      new TableSelectAllKeyStroke(this),
      new TableRefreshKeyStroke(this),
      new TableToggleRowKeyStroke(this),
      new TableCopyKeyStroke(this),
      new ContextMenuKeyStroke(this, this.showContextMenu, this),
      new AppLinkKeyStroke(this, this.handleAppLinkAction)
    ]);
  }

  _insertBooleanColumn() {
    // don't add checkbox column when we're in checkableStyle mode
    if (this.checkableStyle === Table.CheckableStyle.TABLE_ROW) {
      return;
    }
    let column = scout.create('BooleanColumn', {
      session: this.session,
      fixedWidth: true,
      fixedPosition: true,
      guiOnly: true,
      nodeColumnCandidate: false,
      headerMenuEnabled: false,
      showSeparator: false,
      width: Column.NARROW_MIN_WIDTH,
      table: this
    });

    arrays.insert(this.columns, column, 0);
    this.checkableColumn = column;
  }

  _insertRowIconColumn() {
    let position = 0,
      column = scout.create('IconColumn', {
        session: this.session,
        fixedWidth: true,
        fixedPosition: true,
        guiOnly: true,
        nodeColumnCandidate: false,
        headerMenuEnabled: false,
        showSeparator: false,
        width: this.rowIconColumnWidth,
        table: this
      });
    if (this.columns[0] === this.checkableColumn) {
      position = 1;
    }
    arrays.insert(this.columns, column, position);
    this.rowIconColumn = column;
  }

  handleAppLinkAction(event) {
    let $appLink = $(event.target);
    let column = this._columnAtX($appLink.offset().left);
    let row = $appLink.findUp($elem => $elem.hasClass('table-row'), this.$container).data('row');
    this._triggerAppLinkAction(column, row, $appLink.data('ref'), $appLink);
  }

  _isDataRendered() {
    return this.rendered && this.$data !== null;
  }

  _render() {
    this.$container = this.$parent.appendDiv('table').addDeviceClass();
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new TableLayout(this));

    if (this.uiCssClass) {
      this.$container.addClass(this.uiCssClass);
    }

    if (this.tileMode) {
      this._renderTileMode();
    } else {
      this._renderData();
    }

    this.session.desktop.on('popupOpen', this._popupOpenHandler);
    this.session.desktop.on('propertyChange', this._desktopPropertyChangeHandler);
  }

  _renderData() {
    this.$data = this.$container.appendDiv('table-data');
    this.$data.on('mousedown', '.table-row', this._onRowMouseDown.bind(this))
      .on('mouseup', '.table-row', this._onRowMouseUp.bind(this))
      .on('dblclick', '.table-row', this._onRowDoubleClick.bind(this))
      .on('contextmenu', event => {
        event.preventDefault();
      });
    this._installScrollbars({
      axis: 'both'
    });
    this._installImageListeners();
    this._installCellTooltipSupport();
    this._calculateRowInsets();
    this._updateRowWidth();
    this._updateRowHeight();
    this._renderViewport();
    if (this.scrollToSelection) {
      this.revealSelection();
    }
  }

  _renderProperties() {
    super._renderProperties();
    this._renderTableHeader();
    this._renderMenuBarVisible();
    this._renderFooterVisible();
    this._renderCheckableStyle();
    this._renderHierarchicalStyle();
    this._renderTextFilterEnabled();
  }

  _setCssClass(cssClass) {
    super._setCssClass(cssClass);
    // calculate row level padding
    let paddingClasses = ['table-row-level-padding'];
    if (this.cssClass) {
      paddingClasses.push(this.cssClass);
    }
    this.setRowLevelPadding(styles.getSize(paddingClasses.reduce((acc, cssClass) => {
      return acc + ' ' + cssClass;
    }, ''), 'width', 'width', 15));
  }

  _removeData() {
    this._removeAggregateRows();
    this._uninstallImageListeners();
    this._uninstallCellTooltipSupport();
    this._uninstallScrollbars();
    this._removeRows();
    this.$fillBefore = null;
    this.$fillAfter = null;
    this.$data.remove();
    this.$data = null;
    this.$emptyData = null;
  }

  _remove() {
    this.session.desktop.off('propertyChange', this._desktopPropertyChangeHandler);
    this.session.desktop.off('popupOpen', this._popupOpenHandler);
    dragAndDrop.uninstallDragAndDropHandler(this);
    // TODO [7.0] cgu do not delete header, implement according to footer
    this.header = null;
    if (this.$data) {
      this._removeData();
    }
    this.filterSupport.remove();
    super._remove();
  }

  setRowLevelPadding(rowLevelPadding) {
    this.setProperty('rowLevelPadding', rowLevelPadding);
  }

  _renderRowLevelPadding() {
    this._rerenderViewport();
  }

  setTableControls(controls) {
    this.setProperty('tableControls', controls);
  }

  _renderTableControls() {
    if (this.footer) {
      this.footer._renderControls();
    }
  }

  _setTableControls(controls) {
    let i;
    for (i = 0; i < this.tableControls.length; i++) {
      this.keyStrokeContext.unregisterKeyStroke(this.tableControls[i]);
    }
    this._setProperty('tableControls', controls);
    for (i = 0; i < this.tableControls.length; i++) {
      this.keyStrokeContext.registerKeyStroke(this.tableControls[i]);
    }
    this._updateFooterVisibility();
    this.tableControls.forEach(function(control) {
      control.tableFooter = this.footer;
    }, this);
  }

  /**
   * When an IMG has been loaded we must update the stored height in the model-row.
   * Note: we don't change the width of the row or table.
   */
  _onImageLoadOrError(event) {
    let $target = $(event.target);
    if ($target.data('measure') === 'in-progress') {
      // Ignore events created by autoOptimizeWidth measurement (see ColumnOptimalWidthMeasurer)
      // Using event.stopPropagation() is not possible because the image load event does not bubble
      return;
    }

    $target.toggleClass('broken', event.type === 'error');

    let $row = $target.closest('.table-row');
    let row = $row.data('row');
    if (!row) {
      return; // row was removed while loading the image
    }
    let oldRowHeight = row.height;
    row.height = this._measureRowHeight($row);
    if (oldRowHeight !== row.height) {
      this.invalidateLayoutTree();
    }
  }

  _onRowMouseDown(event) {
    this._doubleClickSupport.mousedown(event);
    this._$mouseDownRow = $(event.currentTarget);
    this._mouseDownRowId = this._$mouseDownRow.data('row').id;
    this._mouseDownColumn = this._columnAtX(event.pageX);
    this._$mouseDownRow.window().one('mouseup', () => {
      this._$mouseDownRow = null;
      this._mouseDownRowId = null;
      this._mouseDownColumn = null;
    });
    this.setContextColumn(this._columnAtX(event.pageX));
    this.selectionHandler.onMouseDown(event);
    let isRightClick = event.which === 3;
    let row = this._$mouseDownRow.data('row');

    let $target = $(event.target);
    // handle expansion
    if (this._isRowControl($target)) {
      if (row.expanded) {
        this.collapseRow(row);
      } else {
        this.expandRow(row);
      }
    }
    // For checkableStyle TABLE_ROW & CHECKBOX_TABLE_ROW only: check row if left click OR clicked row was not checked yet
    if (scout.isOneOf(this.checkableStyle, Table.CheckableStyle.TABLE_ROW, Table.CheckableStyle.CHECKBOX_TABLE_ROW) &&
      (!isRightClick || !row.checked) &&
      !$(event.target).is('.table-row-control') &&
      // Click on BooleanColumns should not trigger a row check. The only exception is if the BooleanColumn is the checkableColumn of this table (handled in BooleanColumn.js)
      !($target.hasClass('checkable') || $target.parent().hasClass('checkable'))) {
      this.checkRow(row, !row.checked);
    }
    if (isRightClick) {
      this.showContextMenu({
        pageX: event.pageX,
        pageY: event.pageY
      });
      return false;
    }
  }

  _isRowControl($target) {
    return $target.hasClass('table-row-control') || $target.parent().hasClass('table-row-control');
  }

  _onRowMouseUp(event) {
    let $row, $mouseUpRow, column, $appLink,
      mouseButton = event.which;

    if (this._doubleClickSupport.doubleClicked()) {
      // Don't execute on double click events
      return;
    }

    $mouseUpRow = $(event.currentTarget);
    this.selectionHandler.onMouseUp(event, $mouseUpRow);

    if (!this._$mouseDownRow || this._mouseDownRowId !== $mouseUpRow.data('row').id) {
      // Don't accept if mouse up happens on another row than mouse down, or mousedown didn't happen on a row at all
      return;
    }

    $row = $mouseUpRow;
    column = this._columnAtX(event.pageX);
    if (column !== this._mouseDownColumn) {
      // Don't execute click / appLinks when the mouse gets pressed and moved outside of a cell
      return;
    }
    let $target = $(event.target);
    if (this._isRowControl($target)) {
      // Don't start cell editor or trigger click if row control was clicked (expansion itself is handled by the mouse down handler)
      return;
    }
    let row = $row.data('row'); // read row before the $row potentially could be replaced by the column specific logic on mouse up
    if (mouseButton === 1) {
      column.onMouseUp(event, $row);
      $appLink = this._find$AppLink(event);
    }
    if ($appLink) {
      this._triggerAppLinkAction(column, row, $appLink.data('ref'), $appLink);
    } else {
      this._triggerRowClick(event, row, mouseButton, column);
    }
  }

  _onRowDoubleClick(event) {
    let $row = $(event.currentTarget),
      column = this._columnAtX(event.pageX);

    this.doRowAction($row.data('row'), column);
  }

  showContextMenu(options) {
    this.session.onRequestsDone(this._showContextMenu.bind(this, options));
  }

  _showContextMenu(options) {
    options = options || {};
    if (!this._isDataRendered() || !this.attached) { // check needed because function is called asynchronously
      return;
    }
    if (this.selectedRows.length === 0) {
      return;
    }
    let menuItems = this._filterMenusForContextMenu();
    if (menuItems.length === 0) {
      return;
    }
    let pageX = scout.nvl(options.pageX, null);
    let pageY = scout.nvl(options.pageY, null);
    if (pageX === null || pageY === null) {
      let rowToDisplay = this.isRowSelectedAndVisible(this.selectionHandler.lastActionRow) ? this.selectionHandler.lastActionRow : this.getLastSelectedAndVisibleRow();
      if (rowToDisplay !== null) {
        let $rowToDisplay = rowToDisplay.$row;
        let offset = $rowToDisplay.offset();
        let dataOffsetBounds = graphics.offsetBounds(this.$data);
        offset.left += this.$data.scrollLeft();
        pageX = offset.left + 10;
        pageY = offset.top + $rowToDisplay.outerHeight() / 2;
        pageY = Math.min(Math.max(pageY, dataOffsetBounds.y + 1), dataOffsetBounds.bottom() - 1);
      } else {
        pageX = this.$data.offset().left + 10;
        pageY = this.$data.offset().top + 10;
      }
    }
    // Prevent firing of 'onClose'-handler during contextMenu.open()
    // (Can lead to null-access when adding a new handler to this.contextMenu)
    if (this.contextMenu) {
      this.contextMenu.close();
    }
    this.contextMenu = scout.create('ContextMenuPopup', {
      parent: this,
      menuItems: menuItems,
      location: {
        x: pageX,
        y: pageY
      },
      $anchor: this.$data,
      menuFilter: this._filterMenusHandler
    });
    this.contextMenu.open();
  }

  isRowSelectedAndVisible(row) {
    if (!this.isRowSelected(row) || !row.$row) {
      return false;
    }
    return graphics.offsetBounds(row.$row).intersects(graphics.offsetBounds(this.$data));
  }

  getLastSelectedAndVisibleRow() {
    for (let i = this.viewRangeRendered.to; i >= this.viewRangeRendered.from; i--) {
      if (this.isRowSelectedAndVisible(this.rows[i])) {
        return this.rows[i];
      }
    }
    return null;
  }

  onColumnVisibilityChanged() {
    this.columnLayoutDirty = true;
    this._calculateTableNodeColumn();
    this.trigger('columnStructureChanged');
    if (this._isDataRendered()) {
      this._updateRowWidth();
      this._redraw();
      this.invalidateLayoutTree();
    }
  }

  /**
   * @override
   */
  _onScroll() {
    let scrollTop = this.$data[0].scrollTop;
    let scrollLeft = this.$data[0].scrollLeft;
    if (this.scrollTop !== scrollTop) {
      this._renderViewport();
    }
    this.scrollTop = scrollTop;
    this.scrollLeft = scrollLeft;
  }

  _renderTableStatus() {
    this.trigger('statusChanged');
  }

  setContextColumn(contextColumn) {
    this.setProperty('contextColumn', contextColumn);
  }

  _hasVisibleTableControls() {
    return this.tableControls.some(control => {
      return control.visible;
    });
  }

  hasAggregateTableControl() {
    return this.tableControls.some(control => {
      return control instanceof AggregateTableControl;
    });
  }

  _createHeader() {
    return scout.create('TableHeader', {
      parent: this,
      table: this,
      enabled: this.headerEnabled,
      headerMenusEnabled: this.headerMenusEnabled
    });
  }

  _createFooter() {
    return scout.create('TableFooter', {
      parent: this,
      table: this
    });
  }

  _installCellTooltipSupport() {
    tooltips_1.install(this.$data, {
      parent: this,
      selector: '.table-cell',
      text: this._cellTooltipText.bind(this),
      htmlEnabled: this._isAggregatedTooltip.bind(this),
      arrowPosition: 50,
      arrowPositionUnit: '%',
      nativeTooltip: !Device.get().isCustomEllipsisTooltipPossible()
    });
  }

  _uninstallCellTooltipSupport() {
    tooltips_1.uninstall(this.$data);
  }

  _cellTooltipText($cell) {
    let cell, tooltipText,
      $row = $cell.parent(),
      column = this.columnFor$Cell($cell, $row),
      row = $row.data('row');

    if (row) {
      cell = this.cell(column, row);
      tooltipText = cell.tooltipText;
    }

    if (tooltipText) {
      return tooltipText;
    }
    if (this._isAggregatedTooltip($cell) && $cell.text().trim()) {
      let $textSpan = $cell.children('.text');
      let $iconSpan = $cell.children('.table-cell-icon');
      let iconAvailableButHidden = $iconSpan.length && !$iconSpan.isVisible();
      if ($textSpan.length && $textSpan.isContentTruncated() || iconAvailableButHidden) {
        let $clone = $cell.clone();
        $clone.children('.table-cell-icon').setVisible(true);
        if ($cell.css('direction') === 'rtl') {
          return strings.join('', $clone.children().get().map(c => c.outerHTML).reverse());
        }
        return $clone.html();
      }
    }
    if (this._isTruncatedCellTooltipEnabled(column) && $cell.isContentTruncated()) {
      return strings.plainText($cell.html(), {
        trim: true,
        removeFontIcons: true
      });
    }
  }

  setTruncatedCellTooltipEnabled(truncatedCellTooltipEnabled) {
    this.setProperty('truncatedCellTooltipEnabled', truncatedCellTooltipEnabled);
  }

  /**
   * Decides if a cell tooltip should be shown for a truncated cell.
   */
  _isTruncatedCellTooltipEnabled(column) {
    if (this.truncatedCellTooltipEnabled === null) {
      // Show cell tooltip only if it is not possible to resize the column.
      return !this.headerVisible || !this.headerEnabled || column.fixedWidth;
    }
    return this.truncatedCellTooltipEnabled;
  }

  _isAggregatedTooltip($cell) {
    let $row = $cell.parent();
    return $row.data('aggregateRow') /* row in the table */
      || $row.hasClass('table-aggregate'); /* aggregate table control */
  }

  reload(reloadReason) {
    if (!this.hasReloadHandler) {
      return;
    }
    this._removeRows();
    if (this._isDataRendered()) {
      this._removeAggregateRows();
      this._renderFiller();
    }
    this._triggerReload(reloadReason);
  }

  /**
   * @override
   */
  setLoading(loading) {
    if (!loading && this.updateBuffer.isBuffering()) {
      // Don't abort loading while buffering, the buffer will do it at the end
      return;
    }
    super.setLoading(loading);
  }

  exportToClipboard() {
    this._triggerClipboardExport();
  }

  /**
   * JS implementation of AbstractTable.execCopy(rows)
   */
  _exportToClipboard() {
    clipboard.copyText({
      parent: this,
      text: this._selectedRowsToText()
    });
  }

  _selectedRowsToText() {
    let columns = this.visibleColumns();
    return this.selectedRows.map(row => {
      return columns.map(column => {
        let cell = column.cell(row);
        let text;
        if (column instanceof BooleanColumn) {
          text = cell.value ? 'X' : '';
        } else if (cell.htmlEnabled) {
          text = strings.plainText(cell.text);
        } else {
          text = cell.text;
        }
        // unwrap
        return this._unwrapText(text);
      }).join('\t');
    }).join('\n');
  }

  _unwrapText(text) {
    // Same implementation as in AbstractTable#unwrapText(String)
    return strings.nvl(text)
      .split(/[\n\r]/)
      .map(line => line.replace(/\t/g, ' '))
      .map(line => line.trim())
      .filter(line => !!line.length)
      .join(' ');
  }

  setMultiSelect(multiSelect) {
    this.setProperty('multiSelect', multiSelect);
  }

  toggleSelection() {
    if (this.selectedRows.length === this.visibleRows.length) {
      this.deselectAll();
    } else {
      this.selectAll();
    }
  }

  selectAll() {
    this.selectRows(this.visibleRows);
  }

  deselectAll() {
    this.selectRows([]);
  }

  checkAll(checked, options) {
    let opts = $.extend(options, {
      checked: checked
    });
    this.checkRows(this.visibleRows, opts);
  }

  uncheckAll(options) {
    this.checkAll(false, options);
  }

  updateScrollbars() {
    scrollbars.update(this.$data);
  }

  _sort(animateAggregateRows) {
    let sortColumns = this._sortColumns();

    // Initialize comparators
    if (!this._isSortingPossible(sortColumns)) {
      return false;
    }
    this.clearAggregateRows(animateAggregateRows);
    if (!sortColumns.length) {
      // no sort column defined.
      return true;
    }

    // add all visible columns as fallback sorting to guarantee same sorting as in Java.
    sortColumns = arrays.union(sortColumns, this.columns);

    this._sortImpl(sortColumns);
    this._triggerRowOrderChanged();
    if (this._isDataRendered()) {
      this._renderRowOrderChanges();
    }

    // Do it after row order has been rendered, because renderRowOrderChanges rerenders the whole viewport which would destroy the animation
    this._group(animateAggregateRows);

    // Sort was possible -> return true
    return true;
  }

  /**
   * @returns whether or not sorting is possible. Asks each column to answer this question by calling Column#isSortingPossible.
   */
  _isSortingPossible(sortColumns) {
    return sortColumns.every(column => {
      return column.isSortingPossible();
    });
  }

  _sortColumns() {
    let sortColumns = [];
    for (let c = 0; c < this.columns.length; c++) {
      let column = this.columns[c];
      let sortIndex = column.sortIndex;
      if (sortIndex >= 0) {
        sortColumns[sortIndex] = column;
      }
    }
    return sortColumns;
  }

  _sortImpl(sortColumns) {
    let sortFunction = (row1, row2) => {
      for (let s = 0; s < sortColumns.length; s++) {
        let column = sortColumns[s];
        let result = column.compare(row1, row2);
        if (column.sortActive && !column.sortAscending) {
          // only consider sortAscending flag when sort is active
          // columns with !sortActive are always sorted ascending (sortAscending represents last state for those, thus not considered)
          result = -result;
        }
        if (result !== 0) {
          return result;
        }
      }
      return 0;
    };

    if (this.hierarchical) {
      // sort tree and set flat row array afterwards.
      this._sortHierarchical(sortFunction);
      let sortedFlatRows = [];
      this.visitRows(row => {
        sortedFlatRows.push(row);
      });
      this.rows = sortedFlatRows;
    } else {
      // sort the flat rows and set the rootRows afterwards.
      this.rows.sort(sortFunction);
      this.rootRows = this.rows;
    }

    this._updateRowStructure({
      filteredRows: true,
      applyFilters: false,
      visibleRows: true
    });
  }

  /**
   * Pre-order (top-down) traversal of all rows in this table (if hierarchical).
   */
  visitRows(visitFunc, rows, level) {
    level = scout.nvl(level, 0);
    rows = rows || this.rootRows;
    rows.forEach(function(row) {
      visitFunc(row, level);
      this.visitRows(visitFunc, row.childRows, level + 1);
    }, this);
  }

  _sortHierarchical(sortFunc, rows) {
    rows = rows || this.rootRows;
    rows.sort(sortFunc);
    rows.forEach(function(row) {
      this._sortHierarchical(sortFunc, row.childRows);
    }, this);
  }

  _renderRowOrderChanges() {
    let animate,
      $rows = this.$rows(),
      oldRowPositions = {};

    // store old position
    // animate only if every row is rendered, otherwise some rows would be animated and some not
    if ($rows.length === this.visibleRows.length) {
      $rows.each((index, elem) => {
        let rowWasInserted = false,
          $row = $(elem),
          row = $row.data('row');

        // Prevent the order animation for newly inserted rows (to not confuse the user)
        if (this._insertedRows) {
          for (let i = 0; i < this._insertedRows.length; i++) {
            if (this._insertedRows[i].id === row.id) {
              rowWasInserted = true;
              break;
            }
          }
        }

        if (!rowWasInserted) {
          animate = true;
          oldRowPositions[row.id] = $row.offset().top;
        }
      });
    }

    this._rerenderViewport();
    // If aggregate rows are being removed by animation, rerenderViewport does not delete them -> reorder
    // This may happen if grouping gets deactivated and another column will get the new first sort column
    this._order$AggregateRows();

    // Ensure selected row is visible after ordering
    if (this.scrollToSelection) {
      this.revealSelection();
    }

    // for less than animationRowLimit rows: move to old position and then animate
    if (animate) {
      $rows = this.$rows();
      $rows.each((index, elem) => {
        let $row = $(elem),
          row = $row.data('row'),
          oldTop = oldRowPositions[row.id];

        if (oldTop !== undefined) {
          $row.css('top', oldTop - $row.offset().top).animate({
            top: 0
          }, {
            progress: function() {
              this._triggerRowOrderChanged(row, true);
              this.updateScrollbars();
            }.bind(this)
          });
        }
      });
    }
  }

  setSortEnabled(sortEnabled) {
    this.setProperty('sortEnabled', sortEnabled);
  }

  /**
   * @param {Column} column the column to sort by.
   * @param {string} [direction] the sorting direction. Either 'asc' or 'desc'. If not specified the direction specified by the column is used {@link Column.sortAscending}.
   * @param {boolean} [multiSort] true to add the column to the list of sorted columns. False to use this column exclusively as sort column (reset other columns). Default is true.
   * @param {boolean} [remove] true to remove the column from the sort columns. Default is false.
   */
  sort(column, direction, multiSort, remove) {
    let data, sorted, animateAggregateRows;
    multiSort = scout.nvl(multiSort, false);
    remove = scout.nvl(remove, false);
    // Animate if sort removes aggregate rows
    animateAggregateRows = !multiSort;
    if (remove) {
      this._removeSortColumn(column);
    } else {
      this._addSortColumn(column, direction, multiSort);
    }
    if (this.header) {
      this.header.onSortingChanged();
    }
    sorted = this._sort(animateAggregateRows);

    data = {
      column: column,
      sortAscending: column.sortAscending
    };
    if (remove) {
      data.sortingRemoved = true;
    }
    if (multiSort) {
      data.multiSort = true;
    }
    if (!sorted) {
      // Delegate sorting to server when it is not possible on client side
      data.sortingRequested = true;
      // hint to animate the aggregate after the row order changed event
      this._animateAggregateRows = animateAggregateRows;
    }
    this.trigger('sort', data);
  }

  _addSortColumn(column, direction, multiSort) {
    let groupColCount, sortColCount;
    direction = scout.nvl(direction, column.sortAscending ? 'asc' : 'desc');
    multiSort = scout.nvl(multiSort, true);

    this._updateSortIndexForColumn(column, multiSort);

    // Reset grouped flag if column should be sorted exclusively
    if (!multiSort) {
      groupColCount = this._groupedColumns().length;
      sortColCount = this._sortColumns().length;
      if (sortColCount === 1 && groupColCount === 1) {
        // special case: if it is the only sort column and also grouped, do not remove grouped property.
      } else {
        column.grouped = false;
      }
    }

    column.sortAscending = direction === 'asc';
    column.sortActive = true;
  }

  /**
   * Intended to be called for new sort columns.
   * Sets the sortIndex of the given column and its siblings.
   */
  _updateSortIndexForColumn(column, multiSort) {
    let deviation,
      sortIndex = -1;

    if (multiSort) {
      // if not already sorted set the appropriate sort index (check for sortIndex necessary if called by _onColumnHeadersUpdated)
      if (!column.sortActive || column.sortIndex === -1) {
        sortIndex = Math.max(-1, arrays.max(this.columns.map(c => {
          return c.sortIndex === undefined || c.initialAlwaysIncludeSortAtEnd ? -1 : c.sortIndex;
        })));
        column.sortIndex = sortIndex + 1;

        // increase sortIndex for all permanent tail columns (a column has been added in front of them)
        this._permanentTailSortColumns.forEach(c => {
          c.sortIndex++;
        });
      }
    } else {
      // do not update sort index for permanent head/tail sort columns, their order is fixed (see ColumnSet.java)
      if (!(column.initialAlwaysIncludeSortAtBegin || column.initialAlwaysIncludeSortAtEnd)) {
        column.sortIndex = this._permanentHeadSortColumns.length;
      }

      // remove sort index for siblings (ignore permanent head/tail columns, only if not multi sort)
      arrays.eachSibling(this.columns, column, siblingColumn => {
        if (siblingColumn.sortActive) {
          this._removeSortColumnInternal(siblingColumn);
        }
      });

      // set correct sort index for all permanent tail sort columns
      deviation = column.initialAlwaysIncludeSortAtBegin || column.initialAlwaysIncludeSortAtEnd ? 0 : 1;
      this._permanentTailSortColumns.forEach(function(c, index) {
        c.sortIndex = this._permanentHeadSortColumns.length + deviation + index;
      }, this);
    }
  }

  _removeSortColumn(column) {
    if (column.initialAlwaysIncludeSortAtBegin || column.initialAlwaysIncludeSortAtEnd) {
      return;
    }
    // Adjust sibling columns with higher index
    arrays.eachSibling(this.columns, column, siblingColumn => {
      if (siblingColumn.sortIndex > column.sortIndex) {
        siblingColumn.sortIndex = siblingColumn.sortIndex - 1;
      }
    });
    this._removeSortColumnInternal(column);
  }

  _removeSortColumnInternal(column) {
    if (column.initialAlwaysIncludeSortAtBegin || column.initialAlwaysIncludeSortAtEnd) {
      return;
    }
    column.sortActive = false;
    column.grouped = false;
    column.sortIndex = -1;
  }

  isGroupingPossible(column) {
    let possible = true;

    if (this.hierarchical) {
      return false;
    }

    if (!this.sortEnabled) {
      // grouping without sorting is not possible
      return false;
    }

    if (this._permanentHeadSortColumns && this._permanentHeadSortColumns.length === 0) {
      // no permanent head sort columns. grouping ok.
      return true;
    }

    if (column.initialAlwaysIncludeSortAtBegin) {
      possible = true;
      arrays.eachSibling(this._permanentHeadSortColumns, column, c => {
        if (c.sortIndex < column.sortIndex) {
          possible = possible && c.grouped;
        }
      });
      return possible;
    }

    if (column.initialAlwaysIncludeSortAtEnd) {
      // it is a tail sort column. Grouping does not make sense.
      return false;
    }

    // column itself is not a head or tail sort column. Therefore, all head sort columns must be grouped.
    this._permanentHeadSortColumns.forEach(c => {
      possible = possible && c.grouped;
    });
    return possible;
  }

  isAggregationPossible(column) {
    if (!(column instanceof NumberColumn)) {
      return false;
    }

    if (column.grouped) {
      // Aggregation is not possible if column is grouped
      return false;
    }

    if (!column.allowedAggregationFunctions || column.allowedAggregationFunctions.length <= 1) {
      // Aggregation is not possible if no aggregation functions are allowed or only exactly one aggregation is pre-defined.
      return false;
    }

    // Aggregation is possible if it is grouped by another column or aggregation control is available
    return this.isGrouped() || this.hasAggregateTableControl();
  }

  changeAggregation(column, func) {
    this.changeAggregations([column], [func]);
  }

  changeAggregations(columns, functions) {
    columns.forEach(function(column, i) {
      let func = functions[i];
      if (column.aggregationFunction !== func) {
        column.setAggregationFunction(func);
        this._triggerAggregationFunctionChanged(column);
      }
    }, this);

    this._group();
  }

  _addGroupColumn(column, direction, multiGroup) {
    let sortIndex = -1;

    if (!this.isGroupingPossible(column)) {
      return;
    }

    direction = scout.nvl(direction, column.sortAscending ? 'asc' : 'desc');
    multiGroup = scout.nvl(multiGroup, true);
    if (!(column.initialAlwaysIncludeSortAtBegin || column.initialAlwaysIncludeSortAtEnd)) {
      // do not update sort index for permanent head/tail sort columns, their order is fixed (see ColumnSet.java)
      if (multiGroup) {
        sortIndex = Math.max(-1, arrays.max(this.columns.map(c => {
          return c.sortIndex === undefined || c.initialAlwaysIncludeSortAtEnd || !c.grouped ? -1 : c.sortIndex;
        })));

        if (!column.sortActive) {
          // column was not yet present: insert at determined position
          // and move all subsequent nodes by one.
          // add just after all other grouping columns in column set.
          column.sortIndex = sortIndex + 1;
          arrays.eachSibling(this.columns, column, siblingColumn => {
            if (siblingColumn.sortActive && !(siblingColumn.initialAlwaysIncludeSortAtBegin || siblingColumn.initialAlwaysIncludeSortAtEnd) && siblingColumn.sortIndex > sortIndex) {
              siblingColumn.sortIndex++;
            }
          });

          // increase sortIndex for all permanent tail columns (a column has been added in front of them)
          this._permanentTailSortColumns.forEach(c => {
            c.sortIndex++;
          });
        } else {
          // column already sorted, update position:
          // move all sort columns between the newly determined sortindex and the old sortindex by one.
          arrays.eachSibling(this.columns, column, siblingColumn => {
            if (siblingColumn.sortActive && !(siblingColumn.initialAlwaysIncludeSortAtBegin || siblingColumn.initialAlwaysIncludeSortAtEnd) &&
              siblingColumn.sortIndex > sortIndex &&
              siblingColumn.sortIndex < column.sortIndex) {
              siblingColumn.sortIndex++;
            }
          });
          column.sortIndex = sortIndex + 1;
        }
      } else {
        // no multigroup:
        sortIndex = this._permanentHeadSortColumns.length;

        if (column.sortActive) {
          // column already sorted, update position:
          // move all sort columns between the newly determined sortindex and the old sortindex by one.
          arrays.eachSibling(this.columns, column, siblingColumn => {
            if (siblingColumn.sortActive && !(siblingColumn.initialAlwaysIncludeSortAtBegin || siblingColumn.initialAlwaysIncludeSortAtEnd) &&
              siblingColumn.sortIndex >= sortIndex &&
              siblingColumn.sortIndex < column.sortIndex) {
              siblingColumn.sortIndex++;
            }
          });
          column.sortIndex = sortIndex;
        } else { // not sorted yet
          arrays.eachSibling(this.columns, column, siblingColumn => {
            if (siblingColumn.sortActive && !(siblingColumn.initialAlwaysIncludeSortAtBegin || siblingColumn.initialAlwaysIncludeSortAtEnd) && siblingColumn.sortIndex >= sortIndex) {
              siblingColumn.sortIndex++;
            }
          });

          column.sortIndex = sortIndex;

          // increase sortIndex for all permanent tail columns (a column has been added in front of them)
          this._permanentTailSortColumns.forEach(c => {
            c.sortIndex++;
          });
        }

        // remove all other grouped properties:
        arrays.eachSibling(this.columns, column, siblingColumn => {
          if (siblingColumn.sortActive && !(siblingColumn.initialAlwaysIncludeSortAtBegin || siblingColumn.initialAlwaysIncludeSortAtEnd) && siblingColumn.sortIndex >= sortIndex) {
            siblingColumn.grouped = false;
          }
        });
      }

      column.sortAscending = direction === 'asc';
      column.sortActive = true;
    } else if (column.initialAlwaysIncludeSortAtBegin) {
      // do not change order or direction. just set grouped to true.
      column.grouped = true;
    }

    column.grouped = true;
  }

  _removeGroupColumn(column) {
    column.grouped = false;

    if (column.initialAlwaysIncludeSortAtBegin) {
      // head sort case: remove all groupings after this column.
      this.columns.forEach(c => {
        if (c.sortIndex >= column.sortIndex) {
          c.grouped = false;
        }
      });
    }

    this._removeSortColumn(column);
  }

  _buildRowDiv(row) {
    let rowWidth = this.rowWidth;
    let rowClass = 'table-row';
    if (row.cssClass) {
      rowClass += ' ' + row.cssClass;
    }
    if (!row.enabled) {
      rowClass += ' disabled';
    }
    if (row.checked && this.checkableStyle === Table.CheckableStyle.TABLE_ROW) {
      rowClass += ' checked';
    }
    // if a row is not filterAccepted it must be visible since any of its child rows are filter accepted.
    if (!row.filterAccepted) {
      rowClass += ' filter-not-accepted';
    }
    if (arrays.empty(row.childRows)) {
      rowClass += ' leaf';
    }

    let i, column,
      rowDiv = '<div class="' + rowClass + '" style="width: ' + rowWidth + 'px">';
    for (i = 0; i < this.columns.length; i++) {
      column = this.columns[i];
      if (column.isVisible()) {
        rowDiv += column.buildCellForRow(row);
      }
    }
    rowDiv += '</div>';

    return rowDiv;
  }

  _calculateRowInsets() {
    let $tableRowDummy = this.$data.appendDiv('table-row');
    this.rowMargins = graphics.margins($tableRowDummy);
    this.rowBorders = graphics.borders($tableRowDummy);
    $tableRowDummy.remove();
  }

  _updateRowWidth() {
    this.rowWidth = this.visibleColumns().reduce((sum, column) => {
      if (this.autoResizeColumns) {
        return sum + column.width;
      }
      // Ensure the row is as long as all cells. Only necessary to use the _realWidth if the device.hasTableCellZoomBug().
      // If autoResizeColumns is enabled, it is not possible to do a proper calculation with this bug
      // -> Use regular width and live with the consequence that the last cell of a table with many columns is not fully visible
      return sum + column._realWidthIfAvailable();
    }, this.rowBorders.horizontal());
  }

  /**
   * A html element with display: table-cell gets the wrong width in Chrome when zoom is enabled, see
   * https://bugs.chromium.org/p/chromium/issues/detail?id=740502.
   * Because the table header items don't use display: table-cell, theirs width is correct.
   * -> Header items and table cells are not in sync which is normally not a big deal but gets visible very well with a lot of columns.
   * This method reads the real width and stores it on the column so that the header can use it when setting the header item's size.
   * It is also necessary to update the row width accordingly otherwise it would be cut at the very right.
   */
  _updateRealColumnWidths($row) {
    if (!Device.get().hasTableCellZoomBug()) {
      return false;
    }
    let changed = false;
    $row = $row || this.$rows().eq(0);
    this.visibleColumns().forEach(function(column, colIndex) {
      if (this._updateRealColumnWidth(column, colIndex, $row)) {
        changed = true;
      }
    }, this);
    return changed;
  }

  _updateRealColumnWidth(column, colIndex, $row) {
    if (!Device.get().hasTableCellZoomBug()) {
      return false;
    }
    $row = $row || this.$rows().eq(0);
    let $cell = this.$cell(scout.nvl(colIndex, column), $row);
    if ($cell.length === 0 && column._realWidth !== null) {
      column._realWidth = null;
      return true;
    }
    let realWidth = graphics.size($cell, {exact: true}).width;
    if (realWidth !== column._realWidth) {
      column._realWidth = realWidth;
      return true;
    }
    return false;
  }

  _updateRowHeight() {
    let $emptyRow = this.$data.appendDiv('table-row');
    let $emptyAggrRow = this._build$AggregateRow().appendTo(this.$data);

    $emptyRow.appendDiv('table-cell').html('&nbsp;');
    $emptyAggrRow.appendDiv('table-cell table-aggregate-cell').appendSpan('text').html('&nbsp;');
    this.rowHeight = this._measureRowHeight($emptyRow);
    this.aggregateRowHeight = this._measureRowHeight($emptyAggrRow);
    $emptyRow.remove();
    $emptyAggrRow.remove();
  }

  /**
   * Updates the row heights for every visible row and aggregate row and clears the height of the others
   */
  _updateRowHeights() {
    this.rows.forEach(row => {
      if (!row.$row) {
        row.height = null;
      } else {
        row.height = this._measureRowHeight(row.$row);
      }
    });
    this._aggregateRows.forEach(aggregateRow => {
      if (!aggregateRow.$row) {
        aggregateRow.height = null;
      } else {
        aggregateRow.height = this._measureRowHeight(aggregateRow.$row);
      }
    });
  }

  _renderRowsInRange(range) {
    let $rows,
      rowString = '',
      numRowsRendered = 0,
      prepend = false;

    let rows = this.visibleRows;
    if (rows.length === 0) {
      return;
    }

    let maxRange = new Range(0, this.rows.length);
    range = maxRange.intersect(range);
    if (this.viewRangeRendered.size() > 0 && !range.intersect(this.viewRangeRendered).equals(new Range(0, 0))) {
      throw new Error('New range must not intersect with existing.');
    }
    if (range.to <= this.viewRangeRendered.from) {
      prepend = true;
    }
    let newRange = this.viewRangeRendered.union(range);
    if (newRange.length === 2) {
      throw new Error('Can only prepend or append rows to the existing range. Existing: ' + this.viewRangeRendered + '. New: ' + newRange);
    }
    this.viewRangeRendered = newRange[0];
    this._removeEmptyData();

    // Build $rows (as string instead of jQuery objects due to efficiency reasons)
    for (let r = range.from; r < range.to; r++) {
      let row = rows[r];
      rowString += this._buildRowDiv(row);
      numRowsRendered++;
    }

    // append block of rows
    $rows = this.$data.makeElement(rowString);
    if (prepend) {
      if (this.$fillBefore) {
        $rows = $rows.insertAfter(this.$fillBefore);
      } else {
        $rows = $rows.prependTo(this.$data);
      }
    } else if (this.$fillAfter) {
      $rows = $rows.insertBefore(this.$fillAfter);
    } else {
      $rows = $rows.appendTo(this.$data);
    }

    $rows.each((index, rowObject) => {
      let $row = $(rowObject);
      // Workaround for Chrome bug, see _updateRealColumnWidths
      // Can be removed when Chrome bug is resolved.
      // This is only necessary once (when the first row is rendered)
      if (this.viewRangeRendered.size() === numRowsRendered && this._updateRealColumnWidths($row)) {
        this._updateRowWidth();
        if (this.header && this.header.rendered) {
          this.header.resizeHeaderItems();
        }
      }
      $row.cssWidth(this.rowWidth);
      // End workaround
      let row = rows[range.from + index];
      Table.linkRowToDiv(row, $row);
      this._installRow(row);
    });

    if ($.log.isTraceEnabled()) {
      $.log.trace(numRowsRendered + ' new rows rendered from ' + range);
      $.log.trace(this._rowsRenderedInfo());
    }
  }

  _rowsRenderedInfo() {
    let numRenderedRows = this.$rows().length,
      renderedRowsRange = '(' + this.viewRangeRendered + ')';
    return numRenderedRows + ' rows rendered ' + renderedRowsRange;
  }

  /**
   * Moves the row to the top.
   */
  moveRowToTop(row) {
    let rowIndex = this.rows.indexOf(row);
    this.moveRow(rowIndex, 0);
  }

  /**
   * Moves the row to the bottom.
   */
  moveRowToBottom(row) {
    let rowIndex = this.rows.indexOf(row);
    this.moveRow(rowIndex, this.rows.length - 1);
  }

  /**
   * Moves the row one up, disregarding filtered rows.
   */
  moveRowUp(row) {
    let rowIndex = this.rows.indexOf(row),
      targetIndex = rowIndex - 1;
    if (this.hierarchical) {
      // find index with same parent
      let siblings = this.rows.filter(candidate => {
          return row.parentRow === candidate.parentRow;
        }, this),
        rowIndexSiblings = siblings.indexOf(row),
        sibling = siblings[rowIndexSiblings - 1];
      if (sibling) {
        targetIndex = this.rows.indexOf(sibling);
      } else {
        targetIndex = 0;
      }
    }

    this.moveRow(rowIndex, targetIndex);
  }

  /**
   * Moves the row one down, disregarding filtered rows.
   */
  moveRowDown(row) {
    let rowIndex = this.rows.indexOf(row),
      targetIndex = rowIndex + 1;
    if (this.hierarchical) {
      // find index with same parent
      let siblings = this.rows.filter(candidate => {
          return row.parentRow === candidate.parentRow;
        }, this),
        rowIndexSiblings = siblings.indexOf(row),
        sibling = siblings[rowIndexSiblings + 1];
      if (sibling) {
        targetIndex = this.rows.indexOf(sibling);
      } else {
        targetIndex = this.rows.length;
      }
    }
    this.moveRow(rowIndex, targetIndex);
  }

  moveVisibleRowUp(row) {
    let rowIndex = this.rows.indexOf(row),
      visibleIndex = this.visibleRows.indexOf(row),
      sibling,
      targetIndex;

    if (this.hierarchical) {
      let siblings = this.visibleRows.filter(candidate => {
        return row.parentRow === candidate.parentRow;
      }, this);
      sibling = siblings[siblings.indexOf(row) - 1];
      if (sibling) {
        targetIndex = this.rows.indexOf(sibling);
      } else {
        // no previous sibling
        return;
      }
    } else {
      sibling = this.visibleRows[visibleIndex - 1];
      if (!sibling) {
        // no previous sibling
        return;
      }
      targetIndex = this.rows.indexOf(sibling);
    }
    this.moveRow(rowIndex, targetIndex);
  }

  moveVisibleRowDown(row) {
    let rowIndex = this.rows.indexOf(row),
      visibleIndex = this.visibleRows.indexOf(row),
      sibling,
      targetIndex;

    if (this.hierarchical) {
      let siblings = this.visibleRows.filter(candidate => {
        return row.parentRow === candidate.parentRow;
      }, this);
      sibling = siblings[siblings.indexOf(row) + 1];
      if (sibling) {
        targetIndex = this.rows.indexOf(sibling);
      } else {
        // no following sibling
        return;
      }
    } else {
      sibling = this.visibleRows[visibleIndex + 1];
      if (!sibling) {
        // no following sibling
        return;
      }
      targetIndex = this.rows.indexOf(sibling);
    }
    this.moveRow(rowIndex, targetIndex);
  }

  moveRow(sourceIndex, targetIndex) {
    let rowCount = this.rows.length;
    sourceIndex = Math.max(sourceIndex, 0);
    sourceIndex = Math.min(sourceIndex, rowCount - 1);
    targetIndex = Math.max(targetIndex, 0);
    targetIndex = Math.min(targetIndex, rowCount - 1);

    if (sourceIndex === targetIndex) {
      return;
    }

    arrays.move(this.rows, sourceIndex, targetIndex);
    this.updateRowOrder(this.rows);
  }

  _removeRowsInRange(range) {
    let row, i,
      numRowsRemoved = 0,
      rows = this.visibleRows;

    let maxRange = new Range(0, rows.length);
    range = maxRange.intersect(range);

    let newRange = this.viewRangeRendered.subtract(range);
    if (newRange.length === 2) {
      throw new Error('Can only remove rows at the beginning or end of the existing range. ' + this.viewRangeRendered + '. New: ' + newRange);
    }
    this.viewRangeRendered = newRange[0];

    for (i = range.from; i < range.to; i++) {
      row = rows[i];
      this._removeRow(row);
      numRowsRemoved++;
    }

    if ($.log.isTraceEnabled()) {
      $.log.trace(numRowsRemoved + ' rows removed from ' + range + '.');
      $.log.trace(this._rowsRenderedInfo());
    }
  }

  removeAllRows() {
    if (this._isDataRendered()) {
      this.$rows().each((i, elem) => {
        let $row = $(elem),
          row = $row.data('row');
        if ($row.hasClass('hiding')) {
          // Do not remove rows which are removed using an animation
          // row.$row may already point to a new row -> don't call removeRow to not accidentally remove the new row
          return;
        }
        this._removeRow(row);
      });
    }
    this.viewRangeRendered = new Range(0, 0);
  }

  /**
   *
   * @param rows if undefined, all rows are removed
   */
  _removeRows(rows) {
    if (!rows) {
      this.removeAllRows();
      return;
    }

    let tableAttached = this.isAttachedAndRendered();
    rows = arrays.ensure(rows);
    rows.forEach(row => {
      let rowIndex = this.visibleRows.indexOf(row);
      if (rowIndex === -1) {
        // row is not visible
        return;
      }
      let rowRendered = Boolean(row.$row);
      let rowInViewRange = this.viewRangeRendered.contains(rowIndex);

      // Note: these checks can only be done, when table is rendered _and_ attached. When the table is detached it can
      // still add rows, but these new rows are not rendered while the table is detached. Thus this check would fail,
      // when a row that has been added in detached state is removed again while table is still detached.
      if (tableAttached) {
        // if row is not rendered but its row-index is inside the view range -> inconsistency
        if (!rowRendered && rowInViewRange) {
          throw new Error('Inconsistency found while removing row. Row is undefined but inside rendered view range. RowIndex: ' + rowIndex);
        }
        // if row is rendered but its row-index is not inside the view range -> inconsistency
        if (rowRendered && !rowInViewRange) {
          throw new Error('Inconsistency found while removing row. Row is rendered but not inside rendered view range. RowIndex: ' + rowIndex);
        }
      }
      this._removeRow(row);

      // Adjust view range if row is inside or before range
      if (rowInViewRange || rowIndex < this.viewRangeRendered.from) {
        if (rowIndex < this.viewRangeRendered.from) {
          this.viewRangeRendered.from--;
          this.viewRangeRendered.to--;
        } else if (rowIndex <= this.viewRangeRendered.to) {
          this.viewRangeRendered.to--;
        }
      }
    });
  }

  /**
   * Just removes the row, does NOT adjust this.viewRangeRendered
   */
  _removeRow(row) {
    let $row = row.$row;
    if (!$row) {
      return;
    }

    this._destroyTooltipsForRow(row);
    this._destroyCellEditorForRow(row);

    // Do not remove rows which are removed using an animation
    if (!$row.hasClass('hiding')) {
      $row.remove();
      row.$row = null;
    }
  }

  /**
   * Animates the rendering of a row by setting it to invisible before doing a slideDown animation. The row needs to already be rendered.
   */
  _showRow(row) {
    let $row = row.$row;
    if (!$row) {
      return;
    }
    if ($row.is('.showing')) {
      return;
    }

    $row.hide(); // intentionally don't use setVisible(false) here
    $row.addClass('showing');
    $row.removeClass('hiding');
    $row.stop().slideDown({
      duration: 250,
      complete: function() {
        $row.removeClass('showing');
        this.updateScrollbars();
      }.bind(this)
    });
  }

  /**
   * Animates the removal of a row by doing a slideUp animation. The row will be removed after the animation finishes.
   */
  _hideRow(row) {
    let $row = row.$row;
    if (!$row) {
      return;
    }
    if ($row.is('.hiding')) {
      return;
    }

    $row.addClass('hiding');
    $row.removeClass('showing');
    $row.stop().slideUp({
      duration: 250,
      complete: function() {
        if (!row.$row) {
          // ignore already removed rows
          return;
        }
        $row.remove();
        if ($row[0] === row.$row[0]) {
          // Only set to null if row still is linked to to original $row
          // If row got rendered again while the animation is still running, row.$row points to the new $row
          row.$row = null;
        }
        this.updateScrollbars();
      }.bind(this)
    });
  }

  /**
   * This method should be used after a row is added to the DOM (new rows, updated rows). The 'row'
   * is expected to be linked with the corresponding '$row' (row.$row and $row.data('row')).
   */
  _installRow(row) {
    row.height = this._measureRowHeight(row.$row);

    if (row.hasError) {
      this._showCellErrorForRow(row);
    }
    // Reopen editor popup (closed when row was removed)
    if (this.cellEditorPopup && !this.cellEditorPopup.rendered && this.cellEditorPopup.row.id === row.id) {
      let editorField = this.cellEditorPopup.cell.field;
      this.startCellEdit(this.cellEditorPopup.column, row, editorField);
    }
  }

  _calcRowLevelPadding(row) {
    if (!row) {
      return -this.rowLevelPadding;
    }
    return this._calcRowLevelPadding(row.parentRow) + this.rowLevelPadding;
  }

  _showCellErrorForRow(row) {
    let $cells = this.$cellsForRow(row.$row),
      that = this;

    $cells.each(function(index) {
      let $cell = $(this);
      let cell = that.cellByCellIndex(index, row);
      if (cell.errorStatus) {
        that._showCellError(row, $cell, cell.errorStatus);
      }
    });
  }

  _showCellError(row, $cell, errorStatus) {
    let tooltip, opts,
      text = errorStatus.message;

    opts = {
      parent: this,
      text: text,
      autoRemove: false,
      $anchor: $cell,
      table: this
    };
    tooltip = scout.create('TableTooltip', opts);
    tooltip.render();
    // link to be able to remove it when row gets deleted
    tooltip.row = row;
    this.tooltips.push(tooltip);
  }

  /**
   * @returns {Column} the column at position x (e.g. from event.pageX)
   */
  _columnAtX(x) {
    let columnOffsetRight = 0,
      columnOffsetLeft = this.$data.offset().left + this.rowBorders.left + this.rowMargins.left,
      scrollLeft = this.$data.scrollLeft();

    if (x < columnOffsetLeft) {
      // Clicked left of first column (on selection border) --> return first column
      return this.columns[0];
    }

    columnOffsetLeft -= scrollLeft;
    let visibleColumns = this.visibleColumns();
    let column = arrays.find(visibleColumns, column => {
      columnOffsetRight = columnOffsetLeft + column.width;
      if (x >= columnOffsetLeft && x < columnOffsetRight) {
        return true;
      }
      columnOffsetLeft = columnOffsetRight;
    });
    if (!column) {
      // No column found (clicked right of last column, on selection border) --> return last column
      column = visibleColumns[visibleColumns.length - 1];
    }
    return column;
  }

  _find$AppLink(event) {
    let $start = $(event.target);
    let $stop = $(event.delegateTarget);
    let $appLink = $start.findUp($elem => $elem.hasClass('app-link'), $stop);
    if ($appLink.length > 0) {
      return $appLink;
    }
    return null;
  }

  _filterMenus(menuItems, destination, onlyVisible, enableDisableKeyStroke, notAllowedTypes) {
    return menus.filterAccordingToSelection('Table', this.selectedRows.length, menuItems, destination, onlyVisible, enableDisableKeyStroke, notAllowedTypes);
  }

  _filterMenusForContextMenu() {
    return this._filterMenus(this.menus, MenuDestinations.CONTEXT_MENU, true, false, ['Header']);
  }

  setStaticMenus(staticMenus) {
    this.setProperty('staticMenus', staticMenus);
    this._updateMenuBar();
  }

  _removeMenus() {
    // menubar takes care about removal
  }

  notifyRowSelectionFinished() {
    if (this._triggerRowsSelectedPending) {
      this._triggerRowsSelected();
      this._triggerRowsSelectedPending = false;
    }
    this.session.onRequestsDone(this._updateMenuBar.bind(this));
  }

  _triggerRowClick(originalEvent, row, mouseButton, column) {
    let event = {
      originalEvent: originalEvent,
      row: row,
      mouseButton: mouseButton,
      column: column
    };
    this.trigger('rowClick', event);
  }

  _triggerRowAction(row, column) {
    this.trigger('rowAction', {
      row: row,
      column: column
    });
  }

  /**
   * Starts cell editing for the cell at the given column and row, but only if editing is allowed.
   * @see prepareCellEdit
   */
  focusCell(column, row) {
    let cell = this.cell(column, row);
    if (this.enabledComputed && row.enabled && cell.editable) {
      this.prepareCellEdit(column, row);
    }
  }

  /**
   * Creates a cell editor for the cell at the given column and row, ensures the row is selected and passes the editor
   * to {@link #startCellEdit} which starts the editing by rendering the editor in a {@link CellEditorPopup}.<br>
   * If the completion of a previous cell edit is still in progress, the preparation is delayed until the completion is finished.
   *
   * @param {boolean} [openFieldPopupOnCellEdit] true to instruct the editor to open its control popup when the editor is rendered.
   *    This only has an effect if the editor has a popup (e.g. SmartField or DateField).
   * @returns Promise the promise will be resolved when the preparation has been finished.
   */
  prepareCellEdit(column, row, openFieldPopupOnCellEdit) {
    let promise = $.resolvedPromise();
    if (this.cellEditorPopup) {
      promise = this.cellEditorPopup.waitForCompleteCellEdit();
    }
    return promise.then(this.prepareCellEditInternal.bind(this, column, row, openFieldPopupOnCellEdit));
  }

  /**
   * @param {boolean} [openFieldPopupOnCellEdit] when this parameter is set to true, the CellEditorPopup sets an
   *    additional property 'cellEditor' on the editor-field. The field instance may use this property
   *    to decide whether or not it should open a popup immediately after it is rendered. This is used
   *    for Smart- and DateFields. Default is false.
   */
  prepareCellEditInternal(column, row, openFieldPopupOnCellEdit) {
    let event = new Event({
      column: column,
      row: row
    });
    this.openFieldPopupOnCellEdit = scout.nvl(openFieldPopupOnCellEdit, false);
    this.trigger('prepareCellEdit', event);

    if (!event.defaultPrevented) {
      this.selectRow(row);
      let field = column.createEditor(row);
      this.startCellEdit(column, row, field);
    }
  }

  /**
   * @returns {Cell} a cell for the given column and row. Row Icon column and cell icon column don't not have cells --> generate one.
   */
  cell(column, row) {
    if (column === this.rowIconColumn) {
      return scout.create('Cell', {
        iconId: row.iconId,
        cssClass: strings.join(' ', 'row-icon-cell', row.cssClass)
      });
    }

    if (column === this.checkableColumn) {
      return scout.create('Cell', {
        value: row.checked,
        editable: true,
        cssClass: row.cssClass
      });
    }

    if (column === this.compactColumn) {
      return scout.create('Cell', {
        text: row.compactValue,
        htmlEnabled: true
      });
    }

    return row.cells[column.index];
  }

  cellByCellIndex(cellIndex, row) {
    return this.cell(this.columns[cellIndex], row);
  }

  cellValue(column, row) {
    let cell = this.cell(column, row);
    if (!cell) {
      return cell;
    }
    if (cell.value !== undefined) {
      return cell.value;
    }
    return '';
  }

  cellText(column, row) {
    let cell = this.cell(column, row);
    if (!cell) {
      return '';
    }
    return cell.text || '';
  }

  /**
   *
   * @returns {object} the next editable position in the table, starting from the cell at (currentColumn / currentRow).
   * A position is an object containing row and column (cell has no reference to a row or column due to memory reasons).
   */
  nextEditableCellPos(currentColumn, currentRow, reverse) {
    let pos, startColumnIndex, rowIndex, startRowIndex, predicate,
      colIndex = this.columns.indexOf(currentColumn);

    startColumnIndex = colIndex + 1;
    if (reverse) {
      startColumnIndex = colIndex - 1;
    }
    pos = this.nextEditableCellPosForRow(startColumnIndex, currentRow, reverse);
    if (pos) {
      return pos;
    }

    predicate = function(row) {
      if (!row.$row) {
        return false;
      }

      startColumnIndex = 0;
      if (reverse) {
        startColumnIndex = this.columns.length - 1;
      }
      pos = this.nextEditableCellPosForRow(startColumnIndex, row, reverse);
      if (pos) {
        return true;
      }
    }.bind(this);

    rowIndex = this.rows.indexOf(currentRow);
    startRowIndex = rowIndex + 1;
    if (reverse) {
      startRowIndex = rowIndex - 1;
    }
    arrays.findFrom(this.rows, startRowIndex, predicate, reverse);

    return pos;
  }

  nextEditableCellPosForRow(startColumnIndex, row, reverse) {
    let cell, column, predicate;

    predicate = function(column) {
      if (!column.isVisible() || column.guiOnly) {
        // does not support tabbing
        return false;
      }
      cell = this.cell(column, row);
      return this.enabledComputed && row.enabled && cell.editable;
    }.bind(this);

    column = arrays.findFrom(this.columns, startColumnIndex, predicate, reverse);
    if (column) {
      return {
        column: column,
        row: row
      };
    }
  }

  clearAggregateRows(animate) {
    // Remove "hasAggregateRow" markers from real rows
    this._aggregateRows.forEach(aggregateRow => {
      if (aggregateRow.prevRow) {
        aggregateRow.prevRow.aggregateRowAfter = null;
      }
      if (aggregateRow.nextRow) {
        aggregateRow.nextRow.aggregateRowBefore = null;
      }
    }, this);

    if (this._isDataRendered()) {
      this._removeAggregateRows(animate);
      this._renderSelection(); // fix selection borders
    }
    this._aggregateRows = [];
  }

  /**
   * Executes the aggregate function with the given funcName for each visible column, but only if the Column
   * has that function, which is currently only the case for NumberColumns.
   *
   * @param states is a reference to an Array containing the results for each column.
   * @param row (optional) if set, an additional cell-value parameter is passed to the aggregate function
   */
  _forEachVisibleColumn(funcName, states, row) {
    let value;
    this.visibleColumns().forEach((column, i) => {
      if (column[funcName]) {
        if (row) {
          value = column.cellValueOrTextForCalculation(row);
        }
        states[i] = column[funcName](states[i], value);
      } else {
        states[i] = undefined;
      }
    });
  }

  _group(animate) {
    let rows, nextRow, newGroup, firstRow, lastRow,
      groupColumns = this._groupedColumns(),
      onTop = this.groupingStyle === Table.GroupingStyle.TOP,
      states = [];

    this.clearAggregateRows();
    if (!groupColumns.length) {
      return;
    }

    rows = this.visibleRows;
    this._forEachVisibleColumn('aggrStart', states);

    rows.forEach((row, r) => {
      if (!firstRow) {
        firstRow = row;
      }
      this._forEachVisibleColumn('aggrStep', states, row);
      // test if sum should be shown, if yes: reset sum-array
      nextRow = rows[r + 1];
      // test if group is finished
      newGroup = r === rows.length - 1 || this._isNewGroup(groupColumns, row, nextRow);
      // if group is finished: add group row
      if (newGroup) {
        // finish aggregation
        this._forEachVisibleColumn('aggrFinish', states);
        // append sum row
        this._addAggregateRow(states,
          onTop ? lastRow : row,
          onTop ? firstRow : nextRow);
        // reset after group
        this._forEachVisibleColumn('aggrStart', states);
        firstRow = null;
        lastRow = row;
      }
    });

    if (this._isDataRendered()) {
      this._renderAggregateRows(animate);
      this._renderSelection(); // fix selection borders
    }
  }

  _isNewGroup(groupedColumns, row, nextRow) {
    let i, col, newRow = false,
      hasCellTextForGroupingFunction;

    if (!nextRow) {
      return true; // row is last row
    }

    for (i = 0; i < groupedColumns.length; i++) {
      col = groupedColumns[i];
      hasCellTextForGroupingFunction = col && col.cellTextForGrouping && typeof col.cellTextForGrouping === 'function';
      newRow = newRow || hasCellTextForGroupingFunction && col.cellTextForGrouping(row) !== col.cellTextForGrouping(nextRow); // NOSONAR
      newRow = newRow || !hasCellTextForGroupingFunction && this.cellText(col, row) !== this.cellText(col, nextRow);
      if (newRow) {
        return true;
      }
    }
    return false;
  }

  _groupedColumns() {
    return this.columns.filter(col => {
      return col.grouped;
    });
  }

  /**
   * Inserts a new aggregation row between 'prevRow' and 'nextRow'.
   *
   * @param contents cells of the new aggregate row
   * @param prevRow row _before_ the new aggregate row
   * @param nextRow row _after_ the new aggregate row
   */
  _addAggregateRow(contents, prevRow, nextRow) {
    let aggregateRow = {
      contents: contents.slice(),
      prevRow: prevRow,
      nextRow: nextRow
    };
    this._aggregateRows.push(aggregateRow);
    if (prevRow) {
      prevRow.aggregateRowAfter = aggregateRow;
    }
    if (nextRow) {
      nextRow.aggregateRowBefore = aggregateRow;
    }
  }

  _removeAggregateRows(animate) {
    if (this._aggregateRows.length === 0) {
      return;
    }
    animate = scout.nvl(animate, false);
    if (!animate) {
      this._aggregateRows.forEach(function(aggregateRow) {
        this._removeRow(aggregateRow);
      }, this);
      this.updateScrollbars();
    } else {
      this._aggregateRows.forEach(function(aggregateRow, i) {
        this._hideRow(aggregateRow);
      }, this);
    }
  }

  _renderAggregateRows(animate) {
    let onTop = this.groupingStyle === Table.GroupingStyle.TOP,
      insertFunc = onTop ? 'insertBefore' : 'insertAfter';
    animate = scout.nvl(animate, false);

    this._aggregateRows.forEach(function(aggregateRow, r) {
      if (aggregateRow.$row) {
        // already rendered, no need to update again (necessary for subsequent renderAggregateRows calls (e.g. in insertRows -> renderRows)
        return;
      }

      let refRow = onTop ? aggregateRow.nextRow : aggregateRow.prevRow;
      if (!refRow || !refRow.$row) {
        return;
      }

      let $aggregateRow = this._build$AggregateRow(aggregateRow);
      $aggregateRow[insertFunc](refRow.$row).width(this.rowWidth);

      this.visibleColumns()
        .map(column => $(column.buildCellForAggregateRow(aggregateRow)).appendTo($aggregateRow))
        .forEach($c => this._resizeAggregateCell($c));

      aggregateRow.height = this._measureRowHeight($aggregateRow);
      aggregateRow.$row = $aggregateRow;
      if (animate) {
        this._showRow(aggregateRow);
      }
    }, this);
  }

  _build$AggregateRow(aggregateRow) {
    let onTop = this.groupingStyle === Table.GroupingStyle.TOP;
    let $aggregateRow = this.$container
      .makeDiv('table-aggregate-row')
      .data('aggregateRow', aggregateRow);
    $aggregateRow.toggleClass('grouping-style-top', onTop);
    $aggregateRow.toggleClass('grouping-style-bottom', !onTop);
    return $aggregateRow;
  }

  groupColumn(column, multiGroup, direction, remove) {
    let data, sorted;
    multiGroup = scout.nvl(multiGroup, false);
    remove = scout.nvl(remove, false);
    if (remove) {
      this._removeGroupColumn(column);
    }
    if (!this.isGroupingPossible(column)) {
      return;
    }
    if (!remove) {
      this._addGroupColumn(column, direction, multiGroup);
    }

    if (this.header) {
      this.header.onSortingChanged();
    }
    sorted = this._sort(true);

    data = {
      column: column,
      groupAscending: column.sortAscending
    };
    if (remove) {
      data.groupingRemoved = true;
    }
    if (multiGroup) {
      data.multiGroup = true;
    }
    if (!sorted) {
      // Delegate sorting to server when it is not possible on client side
      data.groupingRequested = true;

      // hint to animate the aggregate after the row order changed event
      this._animateAggregateRows = true;
    }
    this.trigger('group', data);
  }

  removeColumnGrouping(column) {
    if (column) {
      this.groupColumn(column, false, 'asc', true);
    }
  }

  removeAllColumnGroupings() {
    this.columns
      .filter(column => {
        return column.grouped;
      })
      .forEach(this.removeColumnGrouping.bind(this));
  }

  /**
   * @returns {boolean} true if at least one column has grouped=true
   */
  isGrouped() {
    return this.columns.some(column => {
      return column.grouped;
    });
  }

  setColumnBackgroundEffect(column, effect) {
    column.setBackgroundEffect(effect);
  }

  /**
   * Updates the background effect of every column, if column.backgroundEffect is set.
   * Meaning: Recalculates the min / max values and renders the background effect again.
   */
  _updateBackgroundEffect() {
    this.columns.forEach(column => {
      if (!column.backgroundEffect) {
        return;
      }
      column.updateBackgroundEffect();
    }, this);
  }

  /**
   * Recalculates the values necessary for the background effect of every column, if column.backgroundEffect is set
   */
  _calculateValuesForBackgroundEffect() {
    this.columns.forEach(column => {
      if (!column.backgroundEffect) {
        return;
      }
      column.calculateMinMaxValues();
    }, this);
  }

  _markAutoOptimizeWidthColumnsAsDirty() {
    this.columns.forEach(column => {
      column.autoOptimizeWidthRequired = true;
    });
  }

  _markAutoOptimizeWidthColumnsAsDirtyIfNeeded(autoOptimizeWidthColumns, oldRow, newRow) {
    let i,
      marked = false;
    for (i = autoOptimizeWidthColumns.length - 1; i >= 0; i--) {
      let column = autoOptimizeWidthColumns[i];
      if (this.cellValue(column, oldRow) !== this.cellValue(column, newRow)) {
        column.autoOptimizeWidthRequired = true;
        // Remove column from list since it is now marked and does not have to be processed next time
        autoOptimizeWidthColumns.splice(i, 1);
        marked = true;
      }
    }
    return marked;
  }

  setMultiCheck(multiCheck) {
    this.setProperty('multiCheck', multiCheck);
  }

  checkedRows() {
    return this.rows.filter(row => {
      return row.checked;
    });
  }

  checkRow(row, checked, options) {
    let opts = $.extend(options, {
      checked: checked
    });
    this.checkRows([row], opts);
  }

  checkRows(rows, options) {
    let opts = $.extend({
      checked: true,
      checkOnlyEnabled: true
    }, options);
    let checkedRows = [];
    // use enabled computed because when the parent of the table is disabled, it should not be allowed to check rows
    if (!this.checkable || !this.enabledComputed && opts.checkOnlyEnabled) {
      return;
    }
    rows = arrays.ensure(rows);
    rows.forEach(function(row) {
      if (!row.enabled && opts.checkOnlyEnabled || row.checked === opts.checked) {
        return;
      }
      if (!this.multiCheck && opts.checked) {
        for (let i = 0; i < this.rows.length; i++) {
          if (this.rows[i].checked) {
            this.rows[i].checked = false;
            checkedRows.push(this.rows[i]);
          }
        }
      }
      row.checked = opts.checked;
      checkedRows.push(row);
    }, this);

    if (this._isDataRendered()) {
      checkedRows.forEach(function(row) {
        this._renderRowChecked(row);
      }, this);
    }
    this._triggerRowsChecked(checkedRows);
  }

  uncheckRow(row, options) {
    this.uncheckRows([row], options);
  }

  uncheckRows(rows, options) {
    let opts = $.extend({
      checked: false
    }, options);
    this.checkRows(rows, opts);
  }

  isTableNodeColumn(column) {
    return this.hierarchical && this.tableNodeColumn === column;
  }

  collapseRow(row) {
    this.collapseRows(arrays.ensure(row));
  }

  collapseAll() {
    this.expandRowsInternal(this.rootRows, false, true);
  }

  expandAll() {
    this.expandRowsInternal(this.rootRows, true, true);
  }

  collapseRows(rows, recursive) {
    this.expandRowsInternal(rows, false, recursive);
  }

  expandRow(row, recursive) {
    this.expandRows(arrays.ensure(row));
  }

  expandRows(rows, recursive) {
    this.expandRowsInternal(rows, true, recursive);
  }

  expandRowsInternal(rows, expanded, recursive) {
    let changedRows = [],
      rowsForAnimation = [];
    rows = rows || this.rootRows;
    expanded = scout.nvl(expanded, true);
    recursive = scout.nvl(recursive, false);
    if (recursive) {
      // collect rows
      this.visitRows(row => {
        let changed = row.expanded !== expanded;
        if (changed) {
          row.expanded = expanded;
          changedRows.push(row);
          if (row.$row) {
            rowsForAnimation.push(row);
          }
        }
      }, rows);
    } else {
      changedRows = rows.filter(row => {
        let changed = row.expanded !== expanded;
        if (changed && row.$row) {
          rowsForAnimation.push(row);
        }
        row.expanded = expanded;
        return changed;
      });
    }
    if (changedRows.length === 0) {
      return;
    }
    this._updateRowStructure({
      visibleRows: true
    });
    this._triggerRowsExpanded(changedRows);

    if (this._isDataRendered()) {
      this._renderRowDelta();
      rowsForAnimation.forEach(row => {
        row.animateExpansion();
      });

      if (rows[0].$row) {
        scrollbars.ensureExpansionVisible({
          element: rows[0],
          $element: rows[0].$row,
          $scrollable: this.get$Scrollable(),
          isExpanded: element => element.expanded,
          getChildren: parent => parent.childRows,
          defaultChildHeight: this.rowHeight
        });
      }
    }
  }

  doRowAction(row, column) {
    if (this.selectedRows.length !== 1 || this.selectedRows[0] !== row) {
      // Only allow row action if the selected row was double clicked because the handler of the event expects a selected row.
      // This may happen if the user modifies the selection using ctrl or shift while double clicking.
      return;
    }

    column = column || this.columns[0];
    if (!row || !column) {
      return;
    }
    this._triggerRowAction(row, column);
  }

  insertRow(row) {
    this.insertRows([row]);
  }

  insertRows(rows) {
    rows = arrays.ensure(rows);
    if (rows.length === 0) {
      return;
    }
    let wasEmpty = this.rows.length === 0;

    // Update model
    rows.forEach(function(row, i) {
      row = this._initRow(row);
      row.status = TableRow.Status.INSERTED;
      rows[i] = row;
      // Always insert new rows at the end, if the order is wrong a rowOrderChanged event will follow
      this.rows.push(row);
    }, this);

    this.filterSupport.applyFilters(rows);
    this._updateRowStructure({
      updateTree: true,
      filteredRows: true,
      applyFilters: false,
      visibleRows: true
    });
    // Notify changed filter if there are user filters and at least one of the new rows is accepted by them
    if (this._filterCount() > 0 && rows.some(row => row.filterAccepted)) {
      this._triggerFilter();
    }

    this._calculateValuesForBackgroundEffect();
    this._markAutoOptimizeWidthColumnsAsDirty();

    // this event should be triggered before the rowOrderChanged event (triggered by the _sort function).
    this._triggerRowsInserted(rows);
    this._sortAfterInsert(wasEmpty);

    // Update HTML
    if (this._isDataRendered()) {
      if (this.hierarchical) {
        this._renderRowOrderChanges();
      }
      // Remember inserted rows for future events like rowOrderChanged
      if (!this._insertedRows) {
        this._insertedRows = rows;
        setTimeout(() => {
          this._insertedRows = null;
        }, 0);
      } else {
        arrays.pushAll(this._insertedRows, rows);
      }

      this.viewRangeDirty = true;
      this._renderViewport();
      this.invalidateLayoutTree();
    }
  }

  _sortAfterInsert(wasEmpty) {
    this._sort();
  }

  deleteRow(row) {
    this.deleteRows([row]);
  }

  deleteRows(rows) {
    rows = arrays.ensure(rows);
    if (rows.length === 0) {
      return;
    }
    let invalidate,
      filterChanged,
      removedRows = [];

    this.visitRows(row => {
      if (!this.rowsMap[row.id]) {
        return;
      }

      removedRows.push(row);
      // Update HTML
      if (this._isDataRendered()) {
        // Cancel cell editing if cell editor belongs to a cell of the deleted row
        if (this.cellEditorPopup && this.cellEditorPopup.row.id === row.id) {
          this.cellEditorPopup.cancelEdit();
        }

        this._removeRows(row);
        invalidate = true;
      }

      // Update model
      arrays.remove(this.rows, row);
      arrays.remove(this.visibleRows, row);
      if (this._filterCount() > 0 && arrays.remove(this._filteredRows, row)) {
        filterChanged = true;
      }
      delete this.rowsMap[row.id];

      if (this.selectionHandler.lastActionRow === row) {
        this.selectionHandler.clearLastSelectedRowMarker();
      }
    }, rows);

    this.deselectRows(removedRows);

    this._updateRowStructure({
      updateTree: true,
      filteredRows: true,
      applyFilters: false,
      visibleRows: true
    });
    if (filterChanged) {
      this._triggerFilter();
    }
    this._group();
    this._updateBackgroundEffect();
    this._markAutoOptimizeWidthColumnsAsDirty();
    this._triggerRowsDeleted(rows);

    if (invalidate) {
      this._renderViewport();
      // Update markers and filler because row may be removed by removeRows. RenderViewport doesn't do it if view range is already correctly rendered.
      this._renderRangeMarkers();
      this._renderFiller();
      this._renderEmptyData();
      this.invalidateLayoutTree();
    }
  }

  deleteAllRows() {
    let filterChanged = this._filterCount() > 0 && this._filteredRows.length > 0,
      rows = this.rows;

    // Update HTML
    if (this._isDataRendered()) {
      // Cancel cell editing
      if (this.cellEditorPopup) {
        this.cellEditorPopup.cancelEdit();
      }

      this.selectionHandler.clearLastSelectedRowMarker();
      this._removeRows();
    }

    // Update model
    this.rows = [];
    this.rowsMap = {};
    this._filteredRows = [];
    this.deselectAll();

    this._updateRowStructure({
      updateTree: true,
      filteredRows: true,
      applyFilters: false,
      visibleRows: true
    });
    if (filterChanged) {
      this._triggerFilter();
    }

    this._markAutoOptimizeWidthColumnsAsDirty();
    this._group();
    this._updateBackgroundEffect();
    this._triggerAllRowsDeleted(rows);

    // Update HTML
    if (this._isDataRendered()) {
      this._renderFiller();
      this._renderViewport();
      this._renderEmptyData();
      this.invalidateLayoutTree();
    }
  }

  updateRow(row) {
    this.updateRows([row]);
  }

  updateRows(rows) {
    rows = arrays.ensure(rows);
    if (rows.length === 0) {
      return;
    }
    if (this.updateBuffer.isBuffering()) {
      this.updateBuffer.buffer(rows);
      return;
    }
    let filterChanged, expansionChanged, autoOptimizeWidthColumnsDirty;
    let autoOptimizeWidthColumns = this.columns.filter(column => {
      return column.autoOptimizeWidth && !column.autoOptimizeWidthRequired;
    });

    let rowsToIndex = {};
    this.rows.forEach((row, index) => {
      rowsToIndex[row.id] = index;
    }, this);

    let oldRowsMap = {};
    let structureChanged = false;
    rows = rows.map(function(row) {
      let parentRowId = row.parentRow,
        oldRow = this.rowsMap[row.id];
      // collect old rows
      oldRowsMap[row.id] = oldRow;
      if (!oldRow) {
        throw new Error('Update event received for non existing row. RowId: ' + row.id);
      }
      // check structure changes
      if (row.parentRow && !objects.isNullOrUndefined(row.parentRow.id)) {
        parentRowId = row.parentRow.id;
      }
      structureChanged = structureChanged || (scout.nvl(oldRow._parentRowId, null) !== scout.nvl(parentRowId, null));
      expansionChanged = expansionChanged || (oldRow.expanded !== scout.nvl(row.expanded, false));
      row = this._initRow(row);
      // Check if cell values have changed
      if (row.status === TableRow.Status.NON_CHANGED) {
        row.cells.some((cell, i) => {
          let oldCell = oldRow.cells[i];
          if (!oldCell || oldCell.value !== cell.value) {
            row.status = TableRow.Status.UPDATED;
            return true; // break "some()" loop
          }
          return false;
        });
      }
      // selection
      if (this.selectionHandler.lastActionRow === oldRow) {
        this.selectionHandler.lastActionRow = row;
      }
      arrays.replace(this.selectedRows, oldRow, row);
      // replace row use index lookup for performance reasons
      this.rows[rowsToIndex[row.id]] = row;
      // filter
      row.filterAccepted = oldRow.filterAccepted;
      if (this._filterCount() > 0) {
        filterChanged = this._applyFiltersForRow(row) || filterChanged;
      }
      // Check if cell content changed and if yes mark auto optimize width column as dirty
      autoOptimizeWidthColumnsDirty = this._markAutoOptimizeWidthColumnsAsDirtyIfNeeded(autoOptimizeWidthColumns, oldRow, row);
      return row;
    }, this);

    this._updateRowStructure({
      updateTree: true,
      filteredRows: true,
      applyFilters: false,
      visibleRows: true
    });

    this._triggerRowsUpdated(rows);

    if (this._isDataRendered()) {
      this._renderUpdateRows(rows, oldRowsMap);
      if (structureChanged) {
        this._renderRowOrderChanges();
      }
    }

    if (filterChanged) {
      this._triggerFilter();
    }
    if (filterChanged || expansionChanged) {
      this._renderRowDelta();
    }

    this._sortAfterUpdate();
    this._updateBackgroundEffect();
    this.invalidateLayoutTree(); // this will also update the scroll-bars
  }

  _renderUpdateRows(rows, oldRowsMap) {
    // render row and replace div in DOM
    rows.forEach(function(row) {
      let oldRow = oldRowsMap[row.id],
        $updatedRow;
      if (!oldRow.$row || oldRow.$row.hasClass('hiding')) {
        // If row is not rendered or being removed by an animation, don't try to update it.
        // If it were updated during animated removal, the new row would immediately be inserted again, so the removal would not work.
        return;
      }
      $updatedRow = $(this._buildRowDiv(row));
      $updatedRow.copyCssClasses(oldRow.$row, Table.SELECTION_CLASSES + ' first last');
      oldRow.$row.replaceWith($updatedRow);
      Table.linkRowToDiv(row, $updatedRow);
      this._destroyTooltipsForRow(row);
      this._destroyCellEditorForRow(row);
      this._installRow(row);
      if (oldRow.$row.hasClass('showing') && oldRow.$row.outerHeight() < row.$row.outerHeight() / 3) {
        // If the row was being shown by an animation, start the animation again for the new row, otherwise row would immediately appear without animation.
        // Do it only, if the current running time of the animation does not exceed 33% (this won't be correct if the height of the new and old rows differ).
        // Goal: if the update happens immediately after the animation started, the new row will be animated nicely. If the update happens later, don't start the animation again from the start.
        this._showRow(row);
      }
    }, this);
  }

  _sortAfterUpdate() {
    this._sort();
  }

  isHierarchical() {
    return this.hierarchical;
  }

  _setHierarchical(hierarchical) {
    if (this.hierarchical === hierarchical) {
      return;
    }

    // Has to be called before the property is set! Otherwise the grouping will not completely removed,
    // since isGroupingPossible() will return false.
    if (hierarchical) {
      this.removeAllColumnGroupings();
    }

    this._setProperty('hierarchical', hierarchical);
  }

  /**
   * The given rows must be rows of this table in desired order.
   * @param {TableRow[]} rows
   */
  updateRowOrder(rows) {
    rows = arrays.ensure(rows);
    if (rows.length !== this.rows.length) {
      throw new Error('Row order may not be updated because lengths of the arrays differ.');
    }

    // update model (make a copy so that original array stays untouched)
    this.rows = rows.slice();
    this._updateRowStructure({
      updateTree: true,
      filteredRows: true,
      applyFilters: false,
      visibleRows: true
    });
    this.clearAggregateRows(this._animateAggregateRows);
    if (this._isDataRendered()) {
      this._renderRowOrderChanges();
    }
    this._triggerRowOrderChanged();

    this._group(this._animateAggregateRows);
    this._animateAggregateRows = false;
  }

  _destroyTooltipsForRow(row) {
    for (let i = this.tooltips.length - 1; i >= 0; i--) {
      if (this.tooltips[i].row.id === row.id) {
        this.tooltips[i].destroy();
        this.tooltips.splice(i, 1);
      }
    }
  }

  _destroyCellEditorForRow(row) {
    if (this.cellEditorPopup && this.cellEditorPopup.rendered && this.cellEditorPopup.row.id === row.id) {
      this.cellEditorPopup.destroy();
    }
  }

  startCellEdit(column, row, field) {
    if (field.destroyed) {
      // May happen if the action was postponed and the field destroyed in the meantime using endCellEdit.
      return;
    }
    if (!this._isDataRendered()) {
      this._postRenderActions.push(this.startCellEdit.bind(this, column, row, field));
      return;
    }
    if (!this.$container.isAttached()) {
      this._postAttachActions.push(this.startCellEdit.bind(this, column, row, field));
      return;
    }

    this.trigger('startCellEdit', {
      column: column,
      row: row,
      field: field
    });
    this.ensureRowRendered(row);
    let popup = column.startCellEdit(row, field);
    this.cellEditorPopup = popup;
    this.$container.toggleClass('has-cell-editor-popup', !!popup);
    return popup;
  }

  /**
   * @param saveEditorValue when this parameter is set to true, the value of the editor field is set as
   *    new value on the edited cell. In remote case this parameter is always false, because the cell
   *    value is updated by an updateRow event instead.
   */
  endCellEdit(field, saveEditorValue) {
    if (!this.cellEditorPopup) {
      // the cellEditorPopup could already be removed by scrolling (out of view range) or be removed by update rows
      field.destroy();
      return;
    }
    // Remove the cell-editor popup prior to destroying the field, so that the 'cell-editor-popup's focus context is
    // uninstalled first and the focus can be restored onto the last focused element of the surrounding focus context.
    // Otherwise, if the currently focused field is removed from DOM, the $entryPoint would be focused first, which can
    // be avoided if removing the popup first.
    // Also, Column.setCellValue needs to be called _after_ cellEditorPopup is set to null
    // because in updateRows we check if the popup is still there and start cell editing mode again.
    this._destroyCellEditorPopup(this._updateCellFromEditor.bind(this, this.cellEditorPopup, field, saveEditorValue));
  }

  _updateCellFromEditor(cellEditorPopup, field, saveEditorValue) {
    saveEditorValue = scout.nvl(saveEditorValue, false);
    if (saveEditorValue) {
      let column = cellEditorPopup.column;
      column.updateCellFromEditor(cellEditorPopup.row, field);
    }
    field.destroy();
  }

  completeCellEdit() {
    let field = this.cellEditorPopup.cell.field;
    let event = new Event({
      field: field,
      row: this.cellEditorPopup.row,
      column: this.cellEditorPopup.column,
      cell: this.cellEditorPopup.cell
    });
    this.trigger('completeCellEdit', event);

    if (!event.defaultPrevented) {
      return this.endCellEdit(field, true);
    }
  }

  cancelCellEdit() {
    let field = this.cellEditorPopup.cell.field;
    let event = new Event({
      field: field,
      row: this.cellEditorPopup.row,
      column: this.cellEditorPopup.column,
      cell: this.cellEditorPopup.cell
    });
    this.trigger('cancelCellEdit', event);

    if (!event.defaultPrevented) {
      this.endCellEdit(field);
    }
  }

  scrollTo(row, options) {
    if (this.viewRangeRendered.size() === 0) {
      // Cannot scroll to a row no row is rendered
      return;
    }
    this.ensureRowRendered(row);
    if (!row.$row) {
      // Row may not be visible due to the filter -> don't try to scroll because it would fail
      return;
    }
    scrollbars.scrollTo(this.$data, row.$row, options);
  }

  scrollPageUp() {
    let newScrollTop = Math.max(0, this.$data[0].scrollTop - this.$data.height());
    this.setScrollTop(newScrollTop);
  }

  scrollPageDown() {
    let newScrollTop = Math.min(this.$data[0].scrollHeight, this.$data[0].scrollTop + this.$data.height());
    this.setScrollTop(newScrollTop);
  }

  /**
   * @override
   */
  setScrollTop(scrollTop) {
    this.setProperty('scrollTop', scrollTop);
    // call _renderViewport to make sure rows are rendered immediately. The browser fires the scroll event handled by onDataScroll delayed
    if (this._isDataRendered()) {
      this._renderViewport();
    }
  }

  /**
   * @override
   */
  _renderScrollTop() {
    if (this.rendering) {
      // Not necessary to do it while rendering since it will be done by the layout
      return;
    }
    scrollbars.scrollTop(this.get$Scrollable(), this.scrollTop);
  }

  /**
   * @override
   */
  get$Scrollable() {
    if (this.$data) {
      return this.$data;
    }
    return this.$container;
  }

  setScrollToSelection(scrollToSelection) {
    this.setProperty('scrollToSelection', scrollToSelection);
  }

  revealSelection() {
    if (!this._isDataRendered()) {
      // Execute delayed because table may be not layouted yet
      this.session.layoutValidator.schedulePostValidateFunction(this.revealSelection.bind(this));
      return;
    }

    if (this.selectedRows.length > 0) {
      this.scrollTo(this.selectedRows[0]);
    }
  }

  revealChecked() {
    let firstCheckedRow = arrays.find(this.rows, row => {
      return row.checked === true;
    });
    if (firstCheckedRow) {
      this.scrollTo(firstCheckedRow);
    }
  }

  _rowById(id) {
    return this.rowsMap[id];
  }

  _rowsByIds(ids) {
    return ids.map(this._rowById.bind(this));
  }

  _rowsToIds(rows) {
    return rows.map(row => {
      return row.id;
    });
  }

  /**
   * Checks whether the given row is contained in the table. Uses the id of the row for the lookup.
   */
  hasRow(row) {
    return Boolean(this.rowsMap[row.id]);
  }

  /**
   * render borders and selection of row. default select if no argument or false is passed in deselect
   * model has to be updated before calling this method.
   */
  _renderSelection(rows) {
    rows = arrays.ensure(rows || this.selectedRows);

    // helper function adds/removes a class for a row only if necessary, return true if classes have been changed
    let addOrRemoveClassIfNeededFunc = ($row, condition, classname) => {
      let hasClass = $row.hasClass(classname);
      if (condition && !hasClass) {
        $row.addClass(classname);
        return true;
      } else if (!condition && hasClass) {
        $row.removeClass(classname);
        return true;
      }
      return false;
    };
    this._renderNoRowsSelectedMarker();

    for (let i = 0; i < rows.length; i++) { // traditional for loop, elements might be added during loop
      let row = rows[i];
      if (!row.$row) {
        continue;
      }

      let thisRowSelected = this.selectedRows.indexOf(row) !== -1,
        visibleRows = this.visibleRows,
        previousIndex = visibleRows.indexOf(row) - 1,
        previousRowSelected = previousIndex >= 0 && this.selectedRows.indexOf(visibleRows[previousIndex]) !== -1,
        followingIndex = visibleRows.indexOf(row) + 1,
        followingRowSelected = followingIndex < visibleRows.length && this.selectedRows.indexOf(visibleRows[followingIndex]) !== -1;

      // Don't collapse selection borders if two consecutively selected (real) rows are separated by an aggregation row
      if (thisRowSelected && previousRowSelected && row.aggregateRowBefore) {
        previousRowSelected = false;
      }
      if (thisRowSelected && followingRowSelected && row.aggregateRowAfter) {
        followingRowSelected = false;
      }

      // Note: We deliberately use the '+' operator on booleans here! That way, _all_ methods are executed (boolean
      // operators might stop in between) and the variable classChanged contains a number > 1 (which is truthy) when
      // at least one method call returned true.
      let classChanged = 0 +
        addOrRemoveClassIfNeededFunc(row.$row, thisRowSelected, 'selected') +
        addOrRemoveClassIfNeededFunc(row.$row, thisRowSelected && !previousRowSelected && followingRowSelected, 'select-top') +
        addOrRemoveClassIfNeededFunc(row.$row, thisRowSelected && previousRowSelected && !followingRowSelected, 'select-bottom') +
        addOrRemoveClassIfNeededFunc(row.$row, thisRowSelected && !previousRowSelected && !followingRowSelected, 'select-single') +
        addOrRemoveClassIfNeededFunc(row.$row, thisRowSelected && previousRowSelected && followingRowSelected, 'select-middle');

      if (classChanged && previousRowSelected && rows.indexOf(visibleRows[previousIndex]) === -1) {
        rows.push(visibleRows[previousIndex]);
      }
      if (classChanged && followingRowSelected && rows.indexOf(visibleRows[followingIndex]) === -1) {
        rows.push(visibleRows[followingIndex]);
      }
    }

    // Make sure the cell editor popup is correctly layouted because selection changes the cell bounds
    if (this.cellEditorPopup && this.cellEditorPopup.rendered && this.selectedRows.indexOf(this.cellEditorPopup.row) > -1) {
      this.cellEditorPopup.position();
      this.cellEditorPopup.pack();
    }
  }

  _removeSelection() {
    this.$container.addClass('no-rows-selected');
    this.selectedRows.forEach(row => {
      if (!row.$row) {
        return;
      }
      row.$row.select(false);
      row.$row.toggleClass(Table.SELECTION_CLASSES, false);
    }, this);
  }

  _renderNoRowsSelectedMarker() {
    this.$container.toggleClass('no-rows-selected', this.selectedRows.length === 0);
  }

  addRowToSelection(row, ongoingSelection) {
    if (this.selectedRows.indexOf(row) > -1) {
      return;
    }
    ongoingSelection = ongoingSelection !== undefined ? ongoingSelection : true;
    this.selectedRows.push(row);

    if (row.$row && this._isDataRendered()) {
      row.$row.select(true);
      this._renderSelection(row);
      if (this.scrollToSelection) {
        this.revealSelection();
      }
    }

    this._triggerRowsSelectedPending = true;
    if (!ongoingSelection) {
      this.notifyRowSelectionFinished();
    }
  }

  removeRowFromSelection(row, ongoingSelection) {
    ongoingSelection = ongoingSelection !== undefined ? ongoingSelection : true;
    if (arrays.remove(this.selectedRows, row)) {
      if (this._isDataRendered()) {
        this._renderSelection(row);
      }
      if (!ongoingSelection) {
        this._triggerRowsSelected();
      } else {
        this._triggerRowsSelectedPending = true;
      }
    }
  }

  selectRow(row, debounceSend) {
    this.selectRows(row, debounceSend);
  }

  selectRows(rows, debounceSend) {
    // Exclude rows that are currently not visible because of a filter (they cannot be selected)
    rows = arrays.ensure(rows).filter(function(row) {
      return Boolean(this.visibleRowsMap[row.id]);
    }, this);

    let selectedEqualRows = arrays.equalsIgnoreOrder(rows, this.selectedRows);
    // TODO [7.0] cgu: maybe make sure selectedRows are in correct order, this would make logic in AbstractTableNavigationKeyStroke or renderSelection easier
    // but requires some effort (remember rowIndex, keep array in order after sort, ... see java Table)
    if (selectedEqualRows) {
      return;
    }

    if (this._isDataRendered()) {
      this._removeSelection();
    }

    if (!this.multiSelect && rows.length > 1) {
      rows = [rows[0]];
    }

    this.selectedRows = rows; // (Note: direct assignment is safe because the initial filtering created a copy of the original array)
    this._triggerRowsSelected(debounceSend);

    this._updateMenuBar();
    if (this._isDataRendered()) {
      this._renderSelection();
      if (this.scrollToSelection) {
        this.revealSelection();
      }
    }
  }

  deselectRow(row) {
    this.deselectRows(row);
  }

  deselectRows(rows) {
    rows = arrays.ensure(rows);
    let selectedRows = this.selectedRows.slice(); // copy
    if (arrays.removeAll(selectedRows, rows)) {
      this.selectRows(selectedRows);
    }
  }

  isRowSelected(row) {
    return this.selectedRows.indexOf(row) > -1;
  }

  _filterCount() {
    return this.filters.length;
  }

  filteredRows() {
    return this._filteredRows;
  }

  $rows(includeAggrRows) {
    let selector = '.table-row';
    if (includeAggrRows) {
      selector += ', .table-aggregate-row';
    }
    return this.$data.find(selector);
  }

  $aggregateRows() {
    return this.$data.find('.table-aggregate-row');
  }

  /**
   * @returns {TableRow} the first selected row of this table or null when no row is selected
   */
  selectedRow() {
    if (this.selectedRows.length > 0) {
      return this.selectedRows[0];
    }
    return null;
  }

  $selectedRows() {
    if (!this.$data) {
      return $();
    }
    return this.$data.find('.selected');
  }

  $cellsForColIndex(colIndex, includeAggrRows) {
    let selector = '.table-row > div:nth-of-type(' + colIndex + ')';
    if (includeAggrRows) {
      selector += ', .table-aggregate-row > div:nth-of-type(' + colIndex + ')';
    }
    return this.$data.find(selector);
  }

  $cellsForRow($row) {
    return $row.children('.table-cell');
  }

  /**
   * @param {Column|number} column or columnIndex
   * @returns {$}
   */
  $cell(column, $row) {
    let columnIndex = column;
    if (typeof column !== 'number') {
      columnIndex = this.visibleColumns().indexOf(column);
    }
    return $row.children('.table-cell').eq(columnIndex);
  }

  columnById(columnId) {
    return arrays.find(this.columns, column => {
      return column.id === columnId;
    });
  }

  /**
   * @param {$} $cell the $cell to get the column for
   * @param {$} [$row] the $row which contains the $cell. If not passed it will be determined automatically
   * @returns {Column} the column for the given $cell
   */
  columnFor$Cell($cell, $row) {
    $row = $row || $cell.parent();
    let cellIndex = this.$cellsForRow($row).index($cell);
    return this.visibleColumns()[cellIndex];
  }

  columnsByIds(columnIds) {
    return columnIds.map(this.columnById.bind(this));
  }

  getVisibleRows() {
    return this.visibleRows;
  }

  _updateRowStructure(options) {
    let updateTree = scout.nvl(options.updateTree, false),
      updateFilteredRows = scout.nvl(options.filteredRows, updateTree),
      applyFilters = scout.nvl(options.applyFilters, updateFilteredRows),
      filtersChanged = scout.nvl(options.filtersChanged, false),
      updateVisibleRows = scout.nvl(options.visibleRows, updateFilteredRows);
    if (updateTree) {
      this._rebuildTreeStructure();
    }
    if (updateFilteredRows) {
      this._updateFilteredRows(applyFilters, filtersChanged);
    }
    if (updateVisibleRows) {
      this._updateVisibleRows();
    }
  }

  _rebuildTreeStructure() {
    let hierarchical = false;
    this.rows.forEach(row => {
      row.childRows = [];
      hierarchical = hierarchical || !objects.isNullOrUndefined(row.parentRow);
    }, this);
    if (!hierarchical) {
      this.rootRows = this.rows;
      this._setHierarchical(hierarchical);
      return;
    }

    this._setHierarchical(hierarchical);
    this.rootRows = [];
    this.rows.forEach(function(row) {
      let parentRow;
      if (objects.isNullOrUndefined(row.parentRow)) {
        // root row
        row.parentRow = null;
        row._parentRowId = null;
        this.rootRows.push(row);
        return;
      }
      if (!objects.isNullOrUndefined(row.parentRow.id)) {
        parentRow = this.rowsMap[row.parentRow.id];
      } else {
        // expect id
        parentRow = this.rowsMap[row.parentRow];
      }
      if (parentRow) {
        row.parentRow = parentRow;
        row._parentRowId = parentRow.id;
        parentRow.childRows.push(row);
      } else {
        // do not allow unresolvable parent rows.
        throw new Error('Parent row of ' + row + ' can not be resolved.');
      }
    }, this);

    // traverse row tree to have minimal order of rows.
    this._maxLevel = 0;
    this.rows = [];
    this.visitRows((row, level) => {
      row.hierarchyLevel = level;
      this._maxLevel = Math.max(level, this._maxLevel);
      this.rows.push(row);
    });

    this._calculateTableNodeColumn();
  }

  _updateFilteredRows(applyFilters, changed) {
    changed = Boolean(changed);
    applyFilters = scout.nvl(applyFilters, true);
    this._filteredRows = this.rows.filter(function(row) {
      if (applyFilters) {
        changed = this._applyFiltersForRow(row) || changed;
      }
      return row.filterAccepted;
    }, this);

    if (changed) {
      this._triggerFilter();
    }
  }

  _updateVisibleRows() {
    this.visibleRows = this._computeVisibleRows();
    // rebuild the rows by id map of visible rows
    this.visibleRowsMap = this.visibleRows.reduce((map, row) => {
      map[row.id] = row;
      return map;
    }, {});

    if (this.initialized) {
      // deselect not visible rows
      this.deselectRows(this.selectedRows.filter(function(selectedRow) {
        return !this.visibleRowsMap[selectedRow.id];
      }, this));
    }
  }

  _computeVisibleRows(rows) {
    let visibleRows = [];
    rows = rows || this.rootRows;
    rows.forEach(function(row) {
      let visibleChildRows = this._computeVisibleRows(row.childRows);
      if (row.filterAccepted) {
        visibleRows.push(row);
      } else if (visibleChildRows.length > 0) {
        visibleRows.push(row);
      }
      row._expandable = visibleChildRows.length > 0;
      if (row.expanded) {
        visibleRows = visibleRows.concat(visibleChildRows);
      }
    }, this);
    return visibleRows;
  }

  visibleChildRows(row) {
    return row.childRows.filter(function(child) {
      return Boolean(this.visibleRowsMap[child.id]);
    }, this);
  }

  _renderRowDelta() {
    if (!this._isDataRendered()) {
      return;
    }
    let renderedRows = [];
    let rowsToHide = [];
    this.$rows().each((i, elem) => {
      let $row = $(elem),
        row = $row.data('row');
      if (this.visibleRows.indexOf(row) < 0) {
        // remember for remove animated
        row.$row.detach();
        rowsToHide.push(row);
      } else {
        renderedRows.push(row);
      }
    });

    this._rerenderViewport();
    // insert rows to remove animated
    rowsToHide.forEach(function(row) {
      row.$row.insertAfter(this.$fillBefore);
    }, this);
    // Rows removed by an animation are still there, new rows were appended -> reset correct row order
    this._order$Rows().insertAfter(this.$fillBefore);
    // Also make sure aggregate rows are at the correct position (_renderAggregateRows does nothing because they are already rendered)
    this._order$AggregateRows();

    rowsToHide.forEach(function(row) {
      // remove animated
      this._hideRow(row);
    }, this);

    this.$rows().each((i, elem) => {
      let $row = $(elem),
        row = $row.data('row');
      if ($row.hasClass('hiding')) {
        // Do not remove rows which are removed using an animation
        // row.$row may already point to a new row -> don't call removeRow to not accidentally remove the new row
        return;
      }
      if (renderedRows.indexOf(row) < 0) {
        this._showRow(row);
      }
    });
    this._renderScrollTop();
    this._renderEmptyData();
  }

  /**
   * Sorts the given $rows according to the row index
   */
  _order$Rows($rows) {
    // Find rows using jquery because
    // this.filteredRows() may be empty but there may be $rows which are getting removed by animation
    $rows = $rows || this.$rows();
    return $rows.sort((elem1, elem2) => {
      let $row1 = $(elem1),
        $row2 = $(elem2),
        row1 = $row1.data('row'),
        row2 = $row2.data('row');

      return this.rows.indexOf(row1) - this.rows.indexOf(row2);
    });
  }

  _order$AggregateRows($rows) {
    // Find aggregate rows using jquery because
    // this._aggregateRows may be empty but there may be $aggregateRows which are getting removed by animation
    $rows = $rows || this.$aggregateRows();
    $rows.each((i, elem) => {
      let $aggrRow = $(elem),
        aggregateRow = $aggrRow.data('aggregateRow');
      if (!aggregateRow || !aggregateRow.prevRow) {
        return;
      }
      $aggrRow.insertAfter(aggregateRow.prevRow.$row);
    });
  }

  /**
   * @returns {Boolean} true if row state has changed, false if not
   */
  _applyFiltersForRow(row) {
    return this.filterSupport.applyFiltersForElement(row);
  }

  /**
   * @returns {String[]} labels of the currently active Filters that provide a createLabel() function
   */
  filteredBy() {
    let filteredBy = [];
    this.filters.forEach(filter => {
      // check if filter supports label
      if (typeof filter.createLabel === 'function') {
        filteredBy.push(filter.createLabel());
      }
    });
    return filteredBy;
  }

  resetUserFilter(applyFilter = true) {
    this.filters.filter(filter => filter instanceof TableUserFilter)
      .forEach(filter => this.removeFilter(filter, applyFilter));

    this._triggerFilterReset();
  }

  hasUserFilter() {
    return this.filters
      .filter(filter => {
        return filter instanceof TableUserFilter;
      })
      .length > 0;
  }

  resizeToFit(column, maxWidth) {
    if (column.fixedWidth) {
      return;
    }
    let returnValue = column.calculateOptimalWidth();
    if (objects.isPlainObject(returnValue)) {
      // Function returned a promise -> delay resizing
      returnValue.always(this._resizeToFit.bind(this, column, maxWidth));
    } else {
      this._resizeToFit(column, maxWidth, returnValue);
    }
  }

  _resizeToFit(column, maxWidth, calculatedSize) {
    if (calculatedSize === -1) {
      // Calculation has been aborted -> don't resize
      return;
    }
    if (maxWidth && maxWidth > 0 && calculatedSize > maxWidth) {
      calculatedSize = maxWidth;
    }
    if (column.width !== calculatedSize) {
      this.resizeColumn(column, calculatedSize);
    }
    column.autoOptimizeWidthRequired = false;
    this._triggerColumnResizedToFit(column);
  }

  /**
   * @param {Filter|function|(Filter|function)[]} filter The filters to add.
   * @param {boolean} applyFilter Whether to apply the filters after modifying the filter list or not. Default is true.
   */
  addFilter(filter, applyFilter = true) {
    if (filter instanceof TableUserFilter) {
      let previousFilter = this.getFilter(filter.createKey());
      this.filterSupport.removeFilter(previousFilter, false);
    }

    let added = this.filterSupport.addFilter(filter, applyFilter);
    if (added && added.length) {
      this.trigger('filterAdded', {
        filter: added[0]
      });
    }
  }

  /**
   * @param {Filter|function|(Filter|function)[]} filter The filters to remove.
   * @param {boolean} applyFilter Whether to apply the filters after modifying the filter list or not. Default is true.
   */
  removeFilter(filter, applyFilter = true) {
    let removed = this.filterSupport.removeFilter(filter, applyFilter);
    if (removed && removed.length) {
      this.trigger('filterRemoved', {
        filter: removed[0]
      });
    }
  }

  removeFilterByKey(key, applyFilter = true) {
    this.removeFilter(this.getFilter(key), applyFilter);
  }

  getFilter(key) {
    return arrays.find(this.filters, f => {
      if (!(f instanceof TableUserFilter)) {
        return false;
      }
      return objects.equals(f.createKey(), key);
    });
  }

  /**
   * @param {Filter|function|(Filter|function)[]} filter The new filters.
   * @param {boolean} applyFilter Whether to apply the filters after modifying the filter list or not. Default is true.
   */
  setFilters(filters, applyFilter = true) {
    this.resetUserFilter(false);
    filters = filters.map(filter => this._ensureFilter(filter));
    let result = this.filterSupport.setFilters(filters, applyFilter);
    result.filtersAdded.forEach(filter => this.trigger('filterAdded', {
      filter: filter
    }));
    result.filtersRemoved.forEach(filter => this.trigger('filtersRemoved', {
      filter: filter
    }));
  }

  _ensureFilter(filter) {
    if (filter instanceof TableUserFilter || !filter.objectType) {
      return filter;
    }
    if (filter.column) {
      filter.column = this.columnById(filter.column);
    }
    filter.table = this;
    filter.session = this.session;
    return scout.create(filter);
  }

  filter() {
    this.filterSupport.filter();
  }

  _filter(options) {
    this._updateRowStructure($.extend({}, options, {filteredRows: true}));
    this._renderRowDelta();
    this._group();
    this.revealSelection();
  }

  /**
   * @returns {FilterSupport}
   */
  _createFilterSupport() {
    return new FilterSupport({
      widget: this,
      $container: () => this.$container,
      getElementsForFiltering: () => this.rows,
      createTextFilter: () => scout.create('TableTextUserFilter', {
        session: this.session,
        table: this
      }),
      updateTextFilterText: (filter, text) => {
        if (objects.equals(filter.text, text)) {
          return false;
        }
        filter.text = text;
        return true;
      }
    });
  }

  setTextFilterEnabled(textFilterEnabled) {
    this.setProperty('textFilterEnabled', textFilterEnabled);
  }

  isTextFilterFieldVisible() {
    return this.textFilterEnabled && !this.footerVisible;
  }

  _renderTextFilterEnabled() {
    this.filterSupport.renderFilterField();
  }

  updateFilteredElements(result, opts) {
    if (this.filteredElementsDirty) {
      this._filter({
        filteredRows: true,
        applyFilters: false,
        filtersChanged: true
      });
      this.filteredElementsDirty = false;
    }
  }

  /**
   * Resizes the given column to the new size.
   *
   * @param column
   *          column to resize
   * @param width
   *          new column size
   */
  resizeColumn(column, width) {
    if (column.fixedWidth) {
      return;
    }
    width = Math.floor(width);
    column.width = width;

    let visibleColumnIndex = this.visibleColumns().indexOf(column);
    if (visibleColumnIndex !== -1) {
      let colNum = visibleColumnIndex + 1;
      this.$cellsForColIndex(colNum, true)
        .css('min-width', width)
        .css('max-width', width);

      this._updateRealColumnWidth(column);
      this._updateRowWidth();
      this.$rows(true)
        .css('width', this.rowWidth);

      // If resized column contains cells with wrapped text, view port needs to be updated
      // Remove row height for non rendered rows because it may have changed due to resizing (wrap text)
      this._updateRowHeights();
      this._renderFiller();
      this._renderViewport();
      this.updateScrollbars();
      this._renderEmptyData();
    }

    this._aggregateRows.forEach(aggregateRow => {
      if (aggregateRow.$row) {
        this._resizeAggregateCell(this.$cell(column, aggregateRow.$row));
      }
    }, this);

    this._triggerColumnResized(column);
  }

  _resizeAggregateCell($cell) {
    // A resize of aggregate columns might also be necessary if the current resize column itself does not contain any content.
    // E.g. when having 3 columns: first and last have content, middle column is empty. While resizing the middle column,
    // it might happen that the overlapping content from the first and the last collide. Therefore always update aggregate columns
    // closest to $cell.
    let range = this._getAggrCellRange($cell);
    this._updateAggrCell(range[0]);
    if (range.length > 1) {
      this._updateAggrCell(range[range.length - 1]);
    }
  }

  /**
   * Gets the aggregation cell range. this is the range from one aggregation cell with content to the next (or the table end).
   * @param {$} $cell The cell to get the surrounding range
   * @returns {$[]} All cells of the range to which the given cell belongs
   */
  _getAggrCellRange($cell) {
    let $cells = [], $row = $cell.parent(), visibleColumns = this.visibleColumns(), $start = $cell, direction;
    let hasContent = $c => !$c.hasClass('empty');
    if (hasContent($cell)) {
      direction = $cell.hasClass('halign-right') ? -1 : 1; // do not use column.horizontalAlignment here because it might be different in the aggregation row (e.g. when the first column is a number column).
    } else {
      direction = 1; // after start has been found: always walk to the right
      let colIndex = visibleColumns.indexOf(this.columnFor$Cell($cell));
      for (let pos = colIndex - 1; pos >= 0; pos--) {
        $start = this.$cell(visibleColumns[pos], $row);
        if (hasContent($start)) {
          break; // next column with content reached
        }
      }
    }

    $cells.push($start);
    let startIndex = visibleColumns.indexOf(this.columnFor$Cell($start));
    for (let pos = startIndex + direction; pos >= 0 && pos < visibleColumns.length; pos += direction) {
      let $curCell = this.$cell(visibleColumns[pos], $row);
      $cells.push($curCell);
      if (hasContent($curCell)) {
        break; // next column with content reached
      }
    }
    return $cells;
  }

  /**
   * Updates the width and icon visibility of an aggregate cell in groupingStyle=title
   * @param {$} $cell The aggregation cell that should be updated.
   */
  _updateAggrCell($cell) {
    let $cellText = $cell.children('.text');
    if (!$cellText || !$cellText.length || $cell.hasClass('empty')) {
      return; // nothing to update (empty cell)
    }

    let $icon = $cell.children('.table-cell-icon').first();
    let cellWidth = this._getAggrCellWidth($cell, $icon);
    cellWidth = this._updateAggrIconVisibility($icon, $cellText, cellWidth);
    $cellText.cssMaxWidth(cellWidth);
  }

  _getWidthWithMarginCached($element) {
    if (!$element || !$element.length) {
      return 0;
    }

    let widthKey = 'widthWithMargin';
    let precomputed = $element.data(widthKey);
    if (precomputed) {
      return precomputed;
    }

    let originallyVisible = $element.isVisible();
    if (!originallyVisible) {
      $element.setVisible(true); // set visible so that the width can be read
    }
    let width = graphics.size($element, {includeMargin: true}).width;
    if (!originallyVisible) {
      $element.setVisible(false); // restore original visibility
    }
    $element.data(widthKey, width); // cache width
    return width;
  }

  /**
   * Gets the computed space that is available to the content of the cell.
   *
   * @param {$} $cell The aggregation cell for which the info should be computed
   * @param {$} $icon The icon of the cell. May be null.
   * @returns {number} The unused space available into flow direction of the cell til the end of the table or the next cell with content.
   */
  _getAggrCellWidth($cell, $icon) {
    let cellSpaceForText = $cell.cssMaxWidth() - $cell.cssPaddingX();
    if ($icon && $icon.length && $icon.isVisible()) {
      cellSpaceForText -= this._getWidthWithMarginCached($icon);
    }

    let range = this._getAggrCellRange($cell);
    for (let i = 1; i < range.length; i++) {
      let $aggrCell = range[i];
      let cellIsEmpty = $aggrCell.hasClass('empty');
      if (cellIsEmpty) {
        // consume full space available
        cellSpaceForText += $aggrCell.cssMaxWidth();
      } else {
        // first neighbour with content found
        let growsTowardsEachOthers = $cell.hasClass('halign-right') ^ $aggrCell.hasClass('halign-right');
        if (growsTowardsEachOthers) {
          // cells grow towards each other: use the free space of the cell
          let cellWidth = $aggrCell.cssMaxWidth();
          let cellConsumedSpace = $aggrCell.children().toArray()
            .map(item => $(item))
            .filter($item => $item.isVisible())
            .map($item => graphics.size($item, {includeMargin: true}).width)
            .reduce((a, b) => a + b, 0) + $cell.cssPaddingLeft();
          let cellAvailSpace = cellWidth - cellConsumedSpace;
          if ($aggrCell.hasClass('halign-center')) {
            // if the cell uses center-alignment, the available space is consumed by left and right side
            // -> only half of the space is available as overflow
            cellAvailSpace /= 2;
          }
          cellSpaceForText += cellAvailSpace;
        }
        break;
      }
    }

    return Math.max(0, cellSpaceForText);
  }

  /**
   * Sets the aggregation cell icon visible or not depending on the space available.
   * If the available space is big enough to hold the content of the cell text and the icon, the icon will become visible. Otherwise the icon will be hidden.
   * This way no ellipsis will be shown as long as there is enough space for the text only.
   *
   * @param {$} $icon The icon for which the visibility should be changed
   * @param {$} $cellText The element holding the text of the cell. Required to compute if there is still enough space for text and icon or not
   * @param {number} spaceAvailableForText The newly computed space available for the cell text.
   * @returns {number} The new space available for the text. If the visibility of the icon changed, more or less space might become available.
   */
  _updateAggrIconVisibility($icon, $cellText, spaceAvailableForText) {
    if (!$icon || !$icon.length) {
      return spaceAvailableForText;
    }
    let isIconVisible = $icon.isVisible();
    let iconWidth = this._getWidthWithMarginCached($icon);
    let cellTextWidth = this._getWidthWithMarginCached($cellText);
    let spaceForIcon = spaceAvailableForText - cellTextWidth + (isIconVisible ? iconWidth : 0);
    let newIsIconVisible = spaceForIcon > iconWidth;
    if (newIsIconVisible === isIconVisible) {
      return spaceAvailableForText;
    }

    $icon.setVisible(newIsIconVisible);
    return spaceAvailableForText + (newIsIconVisible ? -iconWidth : iconWidth);
  }

  moveColumn(column, visibleOldPos, visibleNewPos, dragged) {
    // If there are fixed columns, don't allow moving the column onto the other side of the fixed columns
    visibleNewPos = this._considerFixedPositionColumns(visibleOldPos, visibleNewPos);

    // Translate position of 'visible columns' array to position in 'all columns' array
    let visibleColumns = this.visibleColumns();
    let newColumn = visibleColumns[visibleNewPos];
    let newPos = this.columns.indexOf(newColumn);

    arrays.remove(this.columns, column);
    arrays.insert(this.columns, column, newPos);

    visibleColumns = this.visibleColumns();
    visibleNewPos = visibleColumns.indexOf(column); // we must re-evaluate visible columns
    this._calculateTableNodeColumn();

    this._triggerColumnMoved(column, visibleOldPos, visibleNewPos, dragged);

    // move aggregated rows
    this._aggregateRows.forEach(aggregateRow => {
      arrays.move(aggregateRow.contents, visibleOldPos, visibleNewPos);
    });

    // move cells
    if (this._isDataRendered()) {
      this._rerenderViewport();
    }
  }

  /**
   * Ensures the given newPos does not pass a fixed column boundary (necessary when moving columns)
   */
  _considerFixedPositionColumns(visibleOldPos, visibleNewPos) {
    let fixedColumnIndex = -1;
    if (visibleNewPos > visibleOldPos) {
      // move to right
      fixedColumnIndex = arrays.findIndexFrom(this.visibleColumns(), visibleOldPos, col => {
        return col.fixedPosition;
      });
      if (fixedColumnIndex > -1) {
        visibleNewPos = Math.min(visibleNewPos, fixedColumnIndex - 1);
      }
    } else {
      // move to left
      fixedColumnIndex = arrays.findIndexFromReverse(this.visibleColumns(), visibleOldPos, col => {
        return col.fixedPosition;
      });
      if (fixedColumnIndex > -1) {
        visibleNewPos = Math.max(visibleNewPos, fixedColumnIndex + 1);
      }
    }
    return visibleNewPos;
  }

  _renderColumnOrderChanges(oldColumnOrder) {
    let column, i, j, $orderedCells, $cell, $cells, that = this,
      $row;

    if (this.header) {
      this.header.onOrderChanged(oldColumnOrder);
    }

    // move cells
    this.$rows(true).each(function() {
      $row = $(this);
      $orderedCells = $();
      $cells = $row.children();
      for (i = 0; i < that.columns.length; i++) {
        column = that.columns[i];

        // Find $cell for given column
        for (j = 0; j < oldColumnOrder.length; j++) {
          if (oldColumnOrder[j] === column) {
            $cell = $cells[j];
            break;
          }
        }
        $orderedCells.push($cell);
      }
      $row.prepend($orderedCells);
    });
  }

  _triggerRowsInserted(rows) {
    this.trigger('rowsInserted', {
      rows: rows
    });
  }

  _triggerRowsDeleted(rows) {
    this.trigger('rowsDeleted', {
      rows: rows
    });
  }

  _triggerRowsUpdated(rows) {
    this.trigger('rowsUpdated', {
      rows: rows
    });
  }

  _triggerAllRowsDeleted(rows) {
    this.trigger('allRowsDeleted', {
      rows: rows
    });
  }

  _triggerRowsSelected(debounce) {
    this.trigger('rowsSelected', {
      debounce: debounce
    });
  }

  _triggerRowsChecked(rows) {
    this.trigger('rowsChecked', {
      rows: rows
    });
  }

  _triggerRowsExpanded(rows) {
    this.trigger('rowsExpanded', {
      rows: rows
    });
  }

  _triggerFilter() {
    this.trigger('filter');
  }

  _triggerFilterReset() {
    this.trigger('filterReset');
  }

  _triggerAppLinkAction(column, row, ref, $appLink) {
    this.trigger('appLinkAction', {
      column: column,
      row: row,
      ref: ref,
      $appLink: $appLink
    });
  }

  _triggerReload(reloadReason) {
    this.trigger('reload', {
      reloadReason: reloadReason
    });
  }

  _triggerClipboardExport() {
    let event = new Event();
    this.trigger('clipboardExport', event);
    if (!event.defaultPrevented) {
      this._exportToClipboard();
    }
  }

  _triggerRowOrderChanged(row, animating) {
    let event = {
      row: row,
      animating: animating
    };
    this.trigger('rowOrderChanged', event);
  }

  _triggerColumnResized(column) {
    let event = {
      column: column
    };
    this.trigger('columnResized', event);
  }

  _triggerColumnResizedToFit(column) {
    let event = {
      column: column
    };
    this.trigger('columnResizedToFit', event);
  }

  _triggerColumnMoved(column, oldPos, newPos, dragged) {
    let event = {
      column: column,
      oldPos: oldPos,
      newPos: newPos,
      dragged: dragged
    };
    this.trigger('columnMoved', event);
  }

  _triggerAggregationFunctionChanged(column) {
    let event = {
      column: column
    };
    this.trigger('aggregationFunctionChanged', event);
  }

  setHeaderVisible(visible) {
    this.setProperty('headerVisible', visible);
  }

  _renderHeaderVisible() {
    this._renderTableHeader();
  }

  setHeaderEnabled(headerEnabled) {
    this.setProperty('headerEnabled', headerEnabled);
  }

  _renderHeaderEnabled() {
    // Rebuild the table header when this property changes
    this._removeTableHeader();
    this._renderTableHeader();
  }

  setHeaderMenusEnabled(headerMenusEnabled) {
    this.setProperty('headerMenusEnabled', headerMenusEnabled);
    if (this.header) {
      this.header.setHeaderMenusEnabled(this.headerMenusEnabled);
    }
  }

  hasPermanentHeadOrTailSortColumns() {
    return this._permanentHeadSortColumns.length !== 0 || this._permanentTailSortColumns.length !== 0;
  }

  _setHeadAndTailSortColumns() {
    // find all sort columns (head and tail sort columns should always be included)
    let sortColumns = this.columns.filter(c => {
      return c.sortIndex >= 0;
    });
    sortColumns.sort((a, b) => {
      return a.sortIndex - b.sortIndex;
    });

    this._permanentHeadSortColumns = [];
    this._permanentTailSortColumns = [];

    sortColumns.forEach(function(c) {
      if (c.initialAlwaysIncludeSortAtBegin) {
        this._permanentHeadSortColumns.push(c);
      } else if (c.initialAlwaysIncludeSortAtEnd) {
        this._permanentTailSortColumns.push(c);
      }
    }, this);
  }

  setTileMode(tileMode) {
    this.setProperty('tileMode', tileMode);
  }

  _setTileMode(tileMode) {
    if (tileMode) {
      this._ensureMediator();
      if (!this.tileTableHeader) {
        this._setTileTableHeader(this._createTileTableHeader());
      }
      this.tableTileGridMediator.loadTiles();
      this.tableTileGridMediator.activate();
    }
    this._setProperty('tileMode', tileMode);

    if (!tileMode && this.tableTileGridMediator) {
      this.tableTileGridMediator.deactivate();
    }
  }

  _ensureMediator() {
    if (!this.tableTileGridMediator) {
      this.tableTileGridMediator = scout.create('TableTileGridMediator', {
        parent: this,
        gridColumnCount: 6
      });
    }
  }

  _renderTileMode() {
    if (this.tableTileGridMediator) {
      this.tableTileGridMediator.renderTileMode();
    }
  }

  createTiles(rows) {
    return rows.map(function(row) {
      let tile = this.createTileForRow(row);
      this._adaptTile(tile);
      tile.rowId = row.id;
      return tile;
    }, this);
  }

  _adaptTile(tile) {
    tile.gridDataHints = {
      weightX: 0
    };
  }

  createTileForRow(row) {
    throw new Error('Not implemented');
  }

  _setTileTableHeader(tileTableHeader) {
    if (tileTableHeader) {
      tileTableHeader.addCssClass('tile-table-header');
    }
    this._setProperty('tileTableHeader', tileTableHeader);
  }

  _createTileTableHeader() {
    return scout.create('TileTableHeaderBox', {
      parent: this
    });
  }

  setRowIconVisible(rowIconVisible) {
    this.setProperty('rowIconVisible', rowIconVisible);
  }

  _setRowIconVisible(rowIconVisible) {
    this._setProperty('rowIconVisible', rowIconVisible);
    let column = this.rowIconColumn;
    if (this.rowIconVisible && !column) {
      this._insertRowIconColumn();
      this._calculateTableNodeColumn();
      this.trigger('columnStructureChanged');
    } else if (!this.rowIconVisible && column) {
      column.destroy();
      arrays.remove(this.columns, column);
      this.rowIconColumn = null;
      this._calculateTableNodeColumn();
      this.trigger('columnStructureChanged');
    }
  }

  _renderRowIconVisible() {
    this.columnLayoutDirty = true;
    this._updateRowWidth();
    this._redraw();
    this.invalidateLayoutTree();
  }

  _renderRowIconColumnWidth() {
    if (!this.rowIconVisible) {
      return;
    }
    this._renderRowIconVisible();
  }

  setRowIconColumnWidth(width) {
    this.setProperty('rowIconColumnWidth', width);
  }

  _setRowIconColumnWidth(width) {
    this._setProperty('rowIconColumnWidth', width);
    let column = this.rowIconColumn;
    if (column) {
      column.width = width;
    }
  }

  _setSelectedRows(selectedRows) {
    if (typeof selectedRows[0] === 'string') {
      selectedRows = this._rowsByIds(selectedRows);
    }
    this._setProperty('selectedRows', selectedRows);
  }

  setMenus(menus) {
    this.setProperty('menus', menus);
  }

  _setMenus(menus, oldMenus) {
    this.updateKeyStrokes(menus, oldMenus);
    this._setProperty('menus', menus);
    this._updateMenuBar();

    if (this.header) {
      this.header.updateMenuBar();
    }
  }

  setMenuBarVisible(visible) {
    this.setProperty('menuBarVisible', visible);
  }

  _setMenuBarVisible(visible) {
    this._setProperty('menuBarVisible', visible);
    this._updateMenuBar();
  }

  _renderMenuBarVisible() {
    if (this.menuBarVisible) {
      this.menuBar.render();
      this._refreshMenuBarPosition();
    } else {
      this.menuBar.remove();
    }
    this._updateMenuBar();
    this.invalidateLayoutTree();
  }

  _refreshMenuBarPosition() {
    this.$container.removeClass('menubar-top');
    this.$container.removeClass('menubar-bottom');
    if (this.menuBarVisible && this.menuBar.rendered) {
      if (this.menuBar.position === MenuBar.Position.TOP) {
        this.menuBar.$container.prependTo(this.$container);
        this.$container.addClass('menubar-top');
      } else {
        this.menuBar.$container.appendTo(this.$container);
        this.$container.addClass('menubar-bottom');
      }
    }
  }

  _createMenuBar() {
    return scout.create('MenuBar', {
      parent: this,
      position: MenuBar.Position.BOTTOM,
      menuOrder: new MenuItemsOrder(this.session, 'Table'),
      menuFilter: this._filterMenusHandler,
      cssClass: 'bounded'
    });
  }

  _updateMenuBar() {
    if (this.menuBarVisible) {
      // Do not update menuBar while it is invisible, the menus may now be managed by another widget.
      // -> this makes sure the parent is not accidentally set to the table, the other widget should remain responsible
      let notAllowedTypes = ['Header'];
      let menuItems = this._filterMenus(this.menus, MenuDestinations.MENU_BAR, false, true, notAllowedTypes);
      menuItems = this.staticMenus.concat(menuItems);
      this.menuBar.setMenuItems(menuItems);
    } else {
      this.menuBar.setMenuItems([]);
    }
    this._refreshMenuBarClasses();
    if (this.contextMenu) {
      let contextMenuItems = this._filterMenus(this.menus, MenuDestinations.CONTEXT_MENU, true, false, ['Header']);
      this.contextMenu.updateMenuItems(contextMenuItems);
    }
  }

  _refreshMenuBarClasses() {
    if (!this.$container) {
      return;
    }
    this.$container.toggleClass('has-menubar', this.menuBar && this.menuBar.visible);
    this.$container.toggleClass('menubar-top', this.menuBar && this.menuBar.position === MenuBar.Position.TOP);
    this.$container.toggleClass('menubar-bottom', this.menuBar && this.menuBar.position !== MenuBar.Position.TOP);

  }

  _setKeyStrokes(keyStrokes) {
    this.updateKeyStrokes(keyStrokes, this.keyStrokes);
    this._setProperty('keyStrokes', keyStrokes);
  }

  setTableStatus(status) {
    this.setProperty('tableStatus', status);
  }

  _setTableStatus(status) {
    status = Status.ensure(status);
    this._setProperty('tableStatus', status);
  }

  setTableStatusVisible(visible) {
    this.setProperty('tableStatusVisible', visible);
    this._updateFooterVisibility();
  }

  _updateFooterVisibility() {
    this.setFooterVisible(this.tableStatusVisible || this._hasVisibleTableControls());
  }

  setHierarchicalStyle(style) {
    this.setProperty('hierarchicalStyle', style);
  }

  _renderHierarchicalStyle() {
    this.$container.toggleClass('structured', Table.HierarchicalStyle.STRUCTURED === this.hierarchicalStyle);
  }

  setFooterVisible(visible) {
    this._setProperty('footerVisible', visible);
    if (visible && !this.footer) {
      this.footer = this._createFooter();
    }

    // relink table controls to new footer
    this.tableControls.forEach(function(control) {
      control.tableFooter = this.footer;
    }, this);

    if (this.rendered) {
      this._renderFooterVisible();
    }
    if (!visible && this.footer) {
      this.footer.destroy();
      this.footer = null;
    }
  }

  /**
   * Renders the background effect of every column, if column.backgroundEffect is set
   */
  _renderBackgroundEffect() {
    this.columns.forEach(column => {
      if (!column.backgroundEffect) {
        return;
      }
      column._renderBackgroundEffect();
    }, this);
  }

  _renderRowChecked(row) {
    if (!this.checkable) {
      return;
    }
    if (!row.$row) {
      return;
    }
    let $styleElem;
    if (this.checkableStyle === Table.CheckableStyle.TABLE_ROW) {
      $styleElem = row.$row;
    } else {
      if (!this.checkableColumn) {
        throw new Error('checkableColumn not set');
      }
      $styleElem = this.checkableColumn.$checkBox(row.$row);
    }
    $styleElem.toggleClass('checked', row.checked);
  }

  setCheckable(checkable) {
    this.setProperty('checkable', checkable);
  }

  _setCheckable(checkable) {
    this._setProperty('checkable', checkable);
    this._updateCheckableColumn();
  }

  _updateCheckableColumn() {
    let column = this.checkableColumn;
    let showCheckBoxes = this.checkable && scout.isOneOf(this.checkableStyle, Table.CheckableStyle.CHECKBOX, Table.CheckableStyle.CHECKBOX_TABLE_ROW);
    if (showCheckBoxes && !column) {
      this._insertBooleanColumn();
      this._calculateTableNodeColumn();
      this.trigger('columnStructureChanged');
    } else if (!showCheckBoxes && column && column.guiOnly) {
      column.destroy();
      arrays.remove(this.columns, column);
      this.checkableColumn = null;
      this._calculateTableNodeColumn();
      this.trigger('columnStructureChanged');
    }
  }

  _renderCheckable() {
    this.columnLayoutDirty = true;
    this._updateRowWidth();
    this._redraw();
    this.invalidateLayoutTree();
  }

  setCheckableStyle(checkableStyle) {
    this.setProperty('checkableStyle', checkableStyle);
  }

  _setCheckableStyle(checkableStyle) {
    this._setProperty('checkableStyle', checkableStyle);
    this._updateCheckableColumn();
  }

  _renderCheckableStyle() {
    this.$container.toggleClass('checkable', scout.isOneOf(this.checkableStyle, Table.CheckableStyle.TABLE_ROW, Table.CheckableStyle.CHECKBOX_TABLE_ROW));
    this.$container.toggleClass('table-row-check', this.checkableStyle === Table.CheckableStyle.TABLE_ROW);
    if (this._isDataRendered()) {
      this._updateRowWidth();
      this._redraw();
      this.invalidateLayoutTree();
    }
  }

  /**
   * @param {boolean} compact
   */
  setCompact(compact) {
    this.setProperty('compact', compact);
  }

  _setCompact(compact) {
    this._setProperty('compact', compact);
    this._updateCompactColumn();
    if (this.compactHandler) {
      this.compactHandler.handle(this.compact);
    }
  }

  _updateCompactColumn() {
    let column = this.compactColumn;
    if (this.compact && !column) {
      this._insertCompactColumn();
      return true;
    }
    if (!this.compact && column) {
      column.destroy();
      arrays.remove(this.columns, column);
      this.compactColumn = null;
      return true;
    }
    return false;
  }

  _insertCompactColumn() {
    let column = scout.create('CompactColumn', {
      session: this.session,
      table: this,
      guiOnly: true,
      headerMenuEnabled: false
    });
    this.columns.push(column); // Insert after the other ui columns
    this.compactColumn = column;
  }

  _renderCompact() {
    this.columnLayoutDirty = true;
    this._updateRowWidth();
    this._redraw();
    this.invalidateLayoutTree();
  }

  setGroupingStyle(groupingStyle) {
    this.setProperty('groupingStyle', groupingStyle);
  }

  _setGroupingStyle(groupingStyle) {
    this._setProperty('groupingStyle', groupingStyle);
    this._group();
  }

  _renderGroupingStyle() {
    this._rerenderViewport();
  }

  _redraw() {
    if (this._isDataRendered()) {
      this._rerenderHeaderColumns();
      this._rerenderViewport();
    }
  }

  _rerenderHeaderColumns() {
    if (this.header) {
      this.header.rerenderColumns();
      this.invalidateLayoutTree();
    }
  }

  _renderTableHeader() {
    if (this.tileMode) {
      return;
    }
    let changed = false;
    if (this.headerVisible && !this.header) {
      this.header = this._createHeader();
      this.header.render();
      this._renderEmptyData();
      changed = true;
    } else if (!this.headerVisible && this.header) {
      this._removeTableHeader();
      this._removeEmptyData();
      changed = true;
    }
    this.$container.toggleClass('header-invisible', !this.header);
    if (changed) {
      this.invalidateLayoutTree();
    }
  }

  _removeTableHeader() {
    if (this.header) {
      this.header.destroy();
      this.header = null;
    }
  }

  /**
   * @param width optional width of emptyData, if omitted the width is set to the header's scrollWidth.
   */
  _renderEmptyData() {
    if (!this.header || this.visibleRows.length > 0) {
      return;
    }
    if (!this.$emptyData) {
      this.$emptyData = this.$data.appendDiv().html('&nbsp;');
    }
    this.$emptyData
      .css('min-width', this.rowWidth)
      .css('max-width', this.rowWidth);
    this.updateScrollbars();
  }

  _removeEmptyData() {
    if (this.header && this.visibleRows.length === 0) {
      return;
    }
    if (this.$emptyData) {
      this.$emptyData.remove();
      this.$emptyData = null;
      this.updateScrollbars();
    }
  }

  _renderFooterVisible() {
    if (!this.footer) {
      return;
    }
    if (this.footerVisible) {
      this._renderFooter();
    } else {
      this._removeFooter();
    }
    this.invalidateLayoutTree();

    this.filterSupport.renderFilterField();
  }

  _renderFooter() {
    if (this.footer.rendered) {
      return;
    }

    this.footer.render();
  }

  _removeFooter() {
    if (!this.footer.rendered) {
      return;
    }
    this.footer.remove();
  }

  /**
   * @override Widget.js
   */
  _renderEnabled() {
    super._renderEnabled();

    this._installOrUninstallDragAndDropHandler();
    let enabled = this.enabledComputed;
    if (!this.tileMode) {
      this.$data.setEnabled(enabled);
    }

    this.$container.setTabbableOrFocusable(enabled);
  }

  /**
   * @override Widget.js
   */
  _renderDisabledStyle() {
    super._renderDisabledStyle();
    this._renderDisabledStyleInternal(this.$data);
  }

  setAutoResizeColumns(autoResizeColumns) {
    this.setProperty('autoResizeColumns', autoResizeColumns);
  }

  _renderAutoResizeColumns() {
    if (!this.autoResizeColumns && Device.get().hasTableCellZoomBug()) {
      // Clear real width so that row width is updated correctly by the table layout if autoResizeColumns is disabled on the fly
      this.visibleColumns().forEach((column, colIndex) => {
        column._realWidth = null;
      });
    }
    this.columnLayoutDirty = true;
    this.invalidateLayoutTree();
  }

  setMultilineText(multilineText) {
    this.setProperty('multilineText', multilineText);
  }

  _renderMultilineText() {
    this._markAutoOptimizeWidthColumnsAsDirty();
    this._redraw();
    this.invalidateLayoutTree();
  }

  setDropType(dropType) {
    this.setProperty('dropType', dropType);
  }

  _renderDropType() {
    this._installOrUninstallDragAndDropHandler();
  }

  setDropMaximumSize(dropMaximumSize) {
    this.setProperty('dropMaximumSize', dropMaximumSize);
  }

  _installOrUninstallDragAndDropHandler() {
    dragAndDrop.installOrUninstallDragAndDropHandler(
      {
        target: this,
        doInstall: () => this.dropType && this.enabledComputed,
        selector: '.table-data,.table-row',
        onDrop: event => this.trigger('drop', event),
        dropType: () => this.dropType,
        additionalDropProperties: event => {
          let $target = $(event.currentTarget);
          let properties = {
            rowId: ''
          };
          if ($target.hasClass('table-row')) {
            let row = $target.data('row');
            properties.rowId = row.id;
          }
          return properties;
        }
      });
  }

  /**
   * This listener is used to invalidate table layout when an image icon has been loaded (which happens async in the browser).
   */
  _installImageListeners() {
    this._imageLoadListener = this._onImageLoadOrError.bind(this);
    // Image events don't bubble -> use capture phase instead
    this.$data[0].addEventListener('load', this._imageLoadListener, true);
    this.$data[0].addEventListener('error', this._imageLoadListener, true);
  }

  _uninstallImageListeners() {
    this.$data[0].removeEventListener('load', this._imageLoadListener, true);
    this.$data[0].removeEventListener('error', this._imageLoadListener, true);
  }

  /**
   * Calculates the optimal view range size (number of rows to be rendered).
   * It uses the default row height to estimate how many rows fit in the view port.
   * The view range size is this value * 2.
   */
  calculateViewRangeSize() {
    // Make sure row height is up to date (row height may be different after zooming)
    this._updateRowHeight();

    if (this.rowHeight === 0) {
      throw new Error('Cannot calculate view range with rowHeight = 0');
    }
    return Math.ceil(this.$data.outerHeight() / this.rowHeight) * 2;
  }

  setViewRangeSize(viewRangeSize) {
    if (this.viewRangeSize === viewRangeSize) {
      return;
    }
    this._setProperty('viewRangeSize', viewRangeSize);
    if (this._isDataRendered()) {
      this._renderViewport();
    }
  }

  _calculateCurrentViewRange() {
    let rowIndex,
      scrollTop = this.$data[0].scrollTop,
      maxScrollTop = this.$data[0].scrollHeight - this.$data[0].clientHeight;

    if (maxScrollTop === 0) {
      // no scrollbars visible
      rowIndex = 0;
    } else {
      rowIndex = this._rowIndexAtScrollTop(scrollTop);
    }

    return this._calculateViewRangeForRowIndex(rowIndex);
  }

  /**
   * Returns the index of the row which is at position scrollTop.
   */
  _rowIndexAtScrollTop(scrollTop) {
    let height = 0,
      index = -1;
    this.visibleRows.some((row, i) => {
      height += this._heightForRow(row);
      if (scrollTop < height) {
        index = i;
        return true;
      }
      return false;
    });
    return index;
  }

  _heightForRow(row) {
    let height = 0;
    let aggregateRow = row.aggregateRowBefore;
    if (this.groupingStyle === Table.GroupingStyle.BOTTOM) {
      aggregateRow = row.aggregateRowAfter;
    }

    if (row.height) {
      height = row.height;
    } else {
      height = this.rowHeight;
    }

    // Add height of aggregate row as well
    if (aggregateRow) {
      if (aggregateRow.height) {
        height += aggregateRow.height;
      } else {
        height += this.aggregateRowHeight;
      }
    }

    return height;
  }

  _measureRowHeight($row) {
    return graphics.size($row, {includeMargin: true, exact: true}).height;
  }

  /**
   * Returns a range of size this.viewRangeSize. Start of range is rowIndex - viewRangeSize / 4.
   * -> 1/4 of the rows are before the viewport 2/4 in the viewport 1/4 after the viewport,
   * assuming viewRangeSize is 2*number of possible rows in the viewport (see calculateViewRangeSize).
   */
  _calculateViewRangeForRowIndex(rowIndex) {
    // regular / non-virtual scrolling? -> all rows are already rendered in the DOM
    if (!this.virtual) {
      return new Range(0, this.visibleRows.length);
    }

    let viewRange = new Range(),
      quarterRange = Math.floor(this.viewRangeSize / 4),
      diff;

    viewRange.from = Math.max(rowIndex - quarterRange, 0);
    viewRange.to = Math.min(viewRange.from + this.viewRangeSize, this.visibleRows.length);

    // Try to use the whole viewRangeSize (extend from if necessary)
    diff = this.viewRangeSize - viewRange.size();
    if (diff > 0) {
      viewRange.from = Math.max(viewRange.to - this.viewRangeSize, 0);
    }
    return viewRange;
  }

  /**
   * Calculates and renders the rows which should be visible in the current viewport based on scroll top.
   */
  _renderViewport() {
    if (!this.isAttachedAndRendered()) {
      // if table is not attached the correct viewPort can not be evaluated. Mark for render after attach.
      this._renderViewPortAfterAttach = true;
      return;
    }
    if (this._renderViewportBlocked) {
      return;
    }
    if (this.visibleColumns().length === 0) {
      return;
    }
    if (!this.$container.isEveryParentVisible()) {
      // If the table is invisible, the height of the rows cannot be determined.
      // In that case, the table won't be layouted either -> as soon as it will be layouted, renderViewport will be called again
      this.invalidateLayoutTree();
      return;
    }
    let viewRange = this._calculateCurrentViewRange();
    this._renderViewRange(viewRange);
    this._renderLastRowAtBottomMarker();
    this._renderNoRowsSelectedMarker(); // Necessary to call it here if there are no rows at all
  }

  _rerenderViewport() {
    if (!this.isAttachedAndRendered()) {
      // if table is not attached the correct viewPort can not be evaluated. Mark for rerender after attach.
      this._rerenderViewPortAfterAttach = true;
      return;
    }
    this._removeRows();
    this._removeAggregateRows();
    this._renderFiller();
    this._renderViewport();
  }

  _renderViewRangeForRowIndex(rowIndex) {
    let viewRange = this._calculateViewRangeForRowIndex(rowIndex);
    this._renderViewRange(viewRange);
  }

  /**
   * Renders the rows visible in the viewport and removes the other rows
   */
  _renderViewRange(viewRange) {
    if (viewRange.from === this.viewRangeRendered.from && viewRange.to === this.viewRangeRendered.to && !this.viewRangeDirty) {
      // Range already rendered -> do nothing
      return;
    }
    this._removeRangeMarkers();
    let rangesToRender = viewRange.subtract(this.viewRangeRendered);
    let rangesToRemove = this.viewRangeRendered.subtract(viewRange);
    rangesToRemove.forEach(range => {
      this._removeRowsInRange(range);
    });
    rangesToRender.forEach(range => {
      this._renderRowsInRange(range);
    });

    // check if at least last and first row in range got correctly rendered
    if (this.viewRangeRendered.size() > 0) {
      let rows = this.visibleRows;
      let firstRow = rows[this.viewRangeRendered.from];
      let lastRow = rows[this.viewRangeRendered.to - 1];
      if (!firstRow.$row || !lastRow.$row) {
        throw new Error('Rows not rendered as expected. ' + this.viewRangeRendered +
          '. First: ' + graphics.debugOutput(firstRow.$row) +
          '. Last: ' + graphics.debugOutput(lastRow.$row) +
          '. Length: visibleRows=' + this.visibleRows.length + ' rows=' + this.rows.length);
      }
    }

    this._renderRangeMarkers();
    this._removeAggregateRows();
    this._renderAggregateRows();
    this._renderFiller();
    this._renderEmptyData();
    this._renderBackgroundEffect();
    this._renderSelection();
    this.viewRangeDirty = false;
  }

  _renderLastRowAtBottomMarker() {
    if (this.$rows().is(':animated')) {
      // Don't change state if rows are animated (e.g. by filtering) because the last row may temporarily be at the end but will be moved up by animation.
      // State will be updated before the animation runs.
      return;
    }
    let lastVisibleRow = this._lastVisibleRow();
    if (lastVisibleRow && lastVisibleRow.$row) {
      // Use ceil because position may be fractional (offsetBounds uses ceil for the height only)
      this.$data.toggleClass('last-row-at-bottom', Math.ceil(graphics.offsetBounds(lastVisibleRow.$row).bottom()) >= graphics.offsetBounds(this.$data).bottom());
    } else {
      this.$data.removeClass('last-row-at-bottom');
    }
  }

  _lastVisibleRow() {
    return this.visibleRows[this.visibleRows.length - 1];
  }

  _removeRangeMarkers() {
    this._modifyRangeMarkers('removeClass');
  }

  _renderRangeMarkers() {
    this._modifyRangeMarkers('addClass');
  }

  _modifyRangeMarkers(funcName) {
    if (this.viewRangeRendered.size() === 0) {
      return;
    }
    let visibleRows = this.visibleRows;
    modifyRangeMarker(visibleRows[this.viewRangeRendered.from], 'first');
    modifyRangeMarker(visibleRows[this.viewRangeRendered.to - 1], 'last');

    function modifyRangeMarker(row, cssClass) {
      if (row && row.$row) {
        row.$row[funcName](cssClass);
      }
    }
  }

  /**
   * Renders the view range that contains the given row.<br>
   * Does nothing if the row is already rendered or not visible (e.g. due to filtering).
   * @param {TableRow} row
   */
  ensureRowRendered(row) {
    if (row.$row) {
      return;
    }
    let rowIndex = this.visibleRows.indexOf(row);
    if (rowIndex < 0) {
      return;
    }
    this._renderViewRangeForRowIndex(rowIndex);
  }

  _renderFiller() {
    if (!this.$fillBefore) {
      this.$fillBefore = this.$data.prependDiv('table-data-fill');
      this._applyFillerStyle(this.$fillBefore);
    }

    let fillBeforeHeight = this._calculateFillerHeight(new Range(0, this.viewRangeRendered.from));
    this.$fillBefore.cssHeight(fillBeforeHeight);
    this.$fillBefore.cssWidth(this.rowWidth);
    $.log.isTraceEnabled() && $.log.trace('FillBefore height: ' + fillBeforeHeight);

    if (!this.$fillAfter) {
      this.$fillAfter = this.$data.appendDiv('table-data-fill');
      this._applyFillerStyle(this.$fillAfter);
    }

    let fillAfterHeight = this._calculateFillerHeight(new Range(this.viewRangeRendered.to, this.visibleRows.length));
    this.$fillAfter.cssHeight(fillAfterHeight);
    this.$fillAfter.cssWidth(this.rowWidth);
    $.log.isTraceEnabled() && $.log.trace('FillAfter height: ' + fillAfterHeight);
  }

  _applyFillerStyle($filler) {
    let lineColor = $filler.css('background-color');
    // In order to get a 1px border we need to get the right value in percentage for the linear gradient
    let lineWidth = ((1 - 1 / this.rowHeight) * 100).toFixed(2) + '%';
    $filler.css({
      background: 'linear-gradient(to bottom, transparent, transparent ' + lineWidth + ', ' + lineColor + ' ' + lineWidth + ', ' + lineColor + ')',
      backgroundSize: '100% ' + this.rowHeight + 'px',
      backgroundColor: 'transparent'
    });
  }

  _calculateFillerHeight(range) {
    let totalHeight = 0;
    for (let i = range.from; i < range.to; i++) {
      let row = this.visibleRows[i];
      totalHeight += this._heightForRow(row);
    }
    return totalHeight;
  }

  containsAggregatedNumberColumn() {
    if (!this.initialized) {
      return false;
    }
    return this.visibleColumns().some(column => {
      return column instanceof NumberColumn && column.aggregationFunction !== 'none';
    });
  }

  /**
   * Rebuilds the header.<br>
   * Does not modify the rows, it expects a deleteAll and insert operation to follow which will do the job.
   */
  updateColumnStructure(columns) {
    this._destroyColumns();
    this.columns = columns;
    this._initColumns();
    this.trigger('columnStructureChanged');
    if (this._isDataRendered()) {
      this._updateRowWidth();
      this.$rows(true).css('width', this.rowWidth);
      this._rerenderHeaderColumns();
      this._renderEmptyData();
    }
  }

  updateColumnOrder(columns) {
    let i, column, currentPosition, oldColumnOrder;
    if (columns.length !== this.columns.length) {
      throw new Error('Column order may not be updated because lengths of the arrays differ.');
    }

    oldColumnOrder = this.columns.slice();

    for (i = 0; i < columns.length; i++) {
      column = columns[i];
      currentPosition = arrays.findIndex(this.columns, element => element.id === column.id);
      if (currentPosition < 0) {
        throw new Error('Column with id ' + column.id + 'not found.');
      }

      if (currentPosition !== i) {
        // Update model
        arrays.move(this.columns, currentPosition, i);
      }
    }

    if (this._isDataRendered()) {
      this._renderColumnOrderChanges(oldColumnOrder);
    }
  }

  /**
   * @param columns array of columns which were updated.
   */
  updateColumnHeaders(columns) {
    let column, oldColumnState;

    // Update model columns
    for (let i = 0; i < columns.length; i++) {
      column = this.columnById(columns[i].id);
      oldColumnState = $.extend(oldColumnState, column);
      column.text = columns[i].text;
      column.headerTooltipText = columns[i].headerTooltipText;
      column.headerTooltipHtmlEnabled = columns[i].headerTooltipHtmlEnabled;
      column.headerCssClass = columns[i].headerCssClass;
      column.headerHtmlEnabled = columns[i].headerHtmlEnabled;
      column.headerBackgroundColor = columns[i].headerBackgroundColor;
      column.headerForegroundColor = columns[i].headerForegroundColor;
      column.headerFont = columns[i].headerFont;
      column.headerIconId = columns[i].headerIconId;
      column.sortActive = columns[i].sortActive;
      column.sortAscending = columns[i].sortAscending;
      if (column.grouped && !columns[i].grouped) {
        this._removeGroupColumn(column);
      }
      column.grouped = columns[i].grouped;
      if (!column.sortActive && column.sortIndex !== -1) {
        // Adjust indices of other sort columns (if a sort column in the middle got removed, there won't necessarily be an event for the other columns)
        this._removeSortColumn(column);
      } else if (column.grouped && column.sortActive && column.sortIndex === -1) {
        this._addGroupColumn(column);
      } else if (column.sortActive && column.sortIndex === -1) {
        // Necessary if there is a tail sort column (there won't be an event for the tail sort column if another sort column was added before)
        this._addSortColumn(column);
      } else {
        column.sortIndex = columns[i].sortIndex;
      }

      if (this._isDataRendered() && this.header) {
        this.header.updateHeader(column, oldColumnState);
      }
    }
  }

  _attach() {
    this.$parent.append(this.$container);
    super._attach();
  }

  /**
   * Method invoked when this is a 'detailTable' and the outline content is displayed.
   * @override Widget.js
   */
  _postAttach() {
    this._rerenderViewportAfterAttach();
    let htmlParent = this.htmlComp.getParent();
    this.htmlComp.setSize(htmlParent.size());
    super._postAttach();
  }

  /**
   * @override Widget.js
   */
  _renderOnAttach() {
    super._renderOnAttach();
    this._rerenderViewportAfterAttach();
    this._renderViewportAfterAttach();
    let actions = this._postAttachActions;
    this._postAttachActions = [];
    actions.forEach(action => action());
  }

  _rerenderViewportAfterAttach() {
    if (this._rerenderViewPortAfterAttach) {
      this._rerenderViewport();
      this._rerenderViewPortAfterAttach = false;
      this._renderViewPortAfterAttach = false;
    }
  }

  _renderViewportAfterAttach() {
    if (this._renderViewPortAfterAttach) {
      this._renderViewport();
      this._renderViewPortAfterAttach = false;
    }
  }

  /**
   * Method invoked when this is a 'detailTable' and the outline content is not displayed anymore.
   * @override Widget.js
   */
  _detach() {
    this.$container.detach();
    // Detach helper stores the current scroll pos and restores in attach.
    // To make it work scrollTop needs to be reset here otherwise viewport won't be rendered by _onDataScroll
    super._detach();
  }

  /**
   * @param {function} [callback] function to be called right after the popup is destroyed
   */
  _destroyCellEditorPopup(callback) {
    // When a cell editor popup is open and table is detached, we close the popup immediately
    // and don't wait for the model event 'endCellEdit'. By doing this we can avoid problems
    // with invalid focus contexts.
    // However: when 'completeCellEdit' is already scheduled, we must wait because Scout classic
    // must send a request to the server first #249385.
    if (!this.cellEditorPopup) {
      return;
    }
    let destroyEditor = () => {
      this.cellEditorPopup.destroy();
      this.cellEditorPopup = null;
      if (this.$container) {
        this.$container.removeClass('has-cell-editor-popup');
      }
      if (callback) {
        callback();
      }
    };
    let promise = this.cellEditorPopup.waitForCompleteCellEdit();
    if (promise.state() === 'resolved') {
      // Do it immediately if promise has already been resolved.
      // This makes sure updateRow does not immediately reopen the editor after closing.
      destroyEditor();
    } else {
      promise.then(destroyEditor);
    }
  }

  setVirtual(virtual) {
    this._setProperty('virtual', virtual);
  }

  setCellValue(column, row, value) {
    column.setCellValue(row, value);
  }

  setCellText(column, row, displayText) {
    column.setCellText(row, displayText);
  }

  setCellErrorStatus(column, row, errorStatus) {
    column.setCellErrorStatus(row, errorStatus);
  }

  visibleColumns(includeGuiColumns) {
    return this.filterColumns(column => column.isVisible(), includeGuiColumns);
  }

  displayableColumns(includeGuiColumns) {
    return this.filterColumns(column => column.displayable, includeGuiColumns);
  }

  filterColumns(filter, includeGuiColumns) {
    includeGuiColumns = scout.nvl(includeGuiColumns, true);
    return this.columns.filter(column => filter(column) && (includeGuiColumns || !column.guiOnly));
  }

  // same as on Tree.prototype._onDesktopPopupOpen
  _onDesktopPopupOpen(event) {
    let popup = event.popup;
    if (!this.isFocusable(false)) {
      return;
    }
    // Set table style to focused if a context menu or a menu bar popup opens, so that it looks as it still has the focus
    if (this.has(popup) && popup instanceof ContextMenuPopup) {
      this.$container.addClass('focused');
      popup.one('destroy', () => {
        if (this._isDataRendered()) {
          this.$container.removeClass('focused');
        }
      });
    }
  }

  _onDesktopPropertyChange(event) {
    // The height of the menuBar changes by css when switching to or from the dense mode
    if (event.propertyName === 'dense') {
      this.menuBar.invalidateLayoutTree();
    }
  }

  markRowsAsNonChanged(rows) {
    arrays.ensure(rows || this.rows).forEach(row => {
      row.status = TableRow.Status.NON_CHANGED;
    });
  }

  /* --- STATIC HELPERS ------------------------------------------------------------- */

  static parseHorizontalAlignment(alignment) {
    if (alignment > 0) {
      return 'right';
    }
    if (alignment === 0) {
      return 'center';
    }
    return 'left';
  }

  static linkRowToDiv(row, $row) {
    if (row) {
      row.$row = $row;
    }
    if ($row) {
      $row.data('row', row);
    }
  }
}
