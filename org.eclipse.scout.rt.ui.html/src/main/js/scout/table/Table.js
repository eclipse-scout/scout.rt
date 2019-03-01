/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.Table = function() {
  scout.Table.parent.call(this);

  this.autoResizeColumns = false;
  this.columnAddable = false;
  this.columnLayoutDirty = false;
  this.columns = [];
  this.contextColumn = null;
  this.checkable = false;
  this.checkableStyle = scout.Table.CheckableStyle.CHECKBOX;
  this.dropType = 0;
  this.dropMaximumSize = scout.dragAndDrop.DEFAULT_DROP_MAXIMUM_SIZE;
  this.enabled = true;
  this.groupingStyle = scout.Table.GroupingStyle.BOTTOM;
  this.header = null;
  this.headerEnabled = true;
  this.headerVisible = true;
  this.headerMenusEnabled = true;
  this.hasReloadHandler = false;
  this.hierarchical = false;
  this.hierarchicalStyle = scout.Table.HierarchicalStyle.DEFAULT;
  this.keyStrokes = [];
  this.keyboardNavigation = true;
  this.menus = [];
  this.menuBar = null;
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
  this.footer = null;
  this.footerVisible = false;
  this.filters = [];
  this.rows = [];
  this.rootRows = [];
  this.visibleRows = [];
  this.visibleRowsMap = {}; // visible rows by id
  this.rowLevelPadding;
  this.rowsMap = {}; // rows by id
  this.rowHeight = 0;
  this.rowWidth = 0;
  this.rowBorderWidth; // read-only, set by _calculateRowBorderWidth(), also used in TableLayout.js
  this.rowBorderLeftWidth = 0; // read-only, set by _calculateRowBorderWidth(), also used in TableHeader.js
  this.rowBorderRightWidth = 0; // read-only, set by _calculateRowBorderWidth(), also used in TableHeader.js
  this.rowIconVisible = false;
  this.rowIconColumnWidth = scout.Column.NARROW_MIN_WIDTH;
  this.staticMenus = [];
  this.selectionHandler = new scout.TableSelectionHandler(this);
  this.tooltips = [];
  this._filterMap = {};
  this._filteredRows = [];
  this.tableNodeColumn = null;
  this._maxLevel = 0;
  this._aggregateRows = [];
  this._animationRowLimit = 25;
  this._blockLoadThreshold = 25;
  this.updateBuffer = new scout.TableUpdateBuffer(this);
  // Initial value must be > 0 to make prefSize work (if it is 0, no filler will be generated).
  // If rows have a variable height, prefSize is only correct for 10 rows.
  // Layout will adjust this value depending on the view port size.
  this.viewRangeSize = 10;
  this.viewRangeDirty = false;
  this.viewRangeRendered = new scout.Range(0, 0);
  this.virtual = true;
  this._doubleClickSupport = new scout.DoubleClickSupport();
  this._permanentHeadSortColumns = [];
  this._permanentTailSortColumns = [];
  this._filterMenusHandler = this._filterMenus.bind(this);
  this._popupOpenHandler = this._onDesktopPopupOpen.bind(this);
  this._rerenderViewPortAfterAttach = false;
  this._renderViewPortAfterAttach = false;
  this._addWidgetProperties(['tableControls', 'menus', 'keyStrokes', 'staticMenus']);

  this.$data = null;
  this.$emptyData = null;
};
scout.inherits(scout.Table, scout.Widget);

// TODO [7.0] cgu create StringColumn.js incl. defaultValues from defaultValues.json

scout.Table.HierarchicalStyle = {
  DEFAULT: 'default',
  STRUCTURED: 'structured'
};

scout.Table.GroupingStyle = {
  /**
   * Aggregate row is rendered on top of the row-group.
   */
  TOP: 'top',
  /**
   * Aggregate row is rendered on the bottom of the row-group (default).
   */
  BOTTOM: 'bottom'
};

scout.Table.CheckableStyle = {
  /**
   * When row is checked a boolean column with a checkbox is inserted into the table.
   */
  CHECKBOX: 'checkbox',
  /**
   * When a row is checked the table-row is marked as checked. By default a background
   * color is set on the table-row when the row is checked.
   */
  TABLE_ROW: 'tableRow'
};

scout.Table.SELECTION_CLASSES = 'select-middle select-top select-bottom select-single selected';

scout.Table.prototype._init = function(model) {
  scout.Table.parent.prototype._init.call(this, model);
  this.resolveConsts([{
    property: 'hierarchicalStyle',
    constType: scout.Table.HierarchicalStyle}]);
  this._initColumns();

  this.rows.forEach(function(row, i) {
    this.rows[i] = this._initRow(row);
  }, this);

  this.setFilters(this.filters);

  this._updateRowStructure({
    updateTree: true
  });

  this.menuBar = scout.create('MenuBar', {
    parent: this,
    menuOrder: new scout.MenuItemsOrder(this.session, 'Table'),
    menuFilter: this._filterMenusHandler
  });
  this.menuBar.bottom();

  this._setSelectedRows(this.selectedRows);

  this._setKeyStrokes(this.keyStrokes);
  this._setMenus(this.menus);
  this._setTableControls(this.tableControls);
  this._setTableStatus(this.tableStatus);
  this._calculateValuesForBackgroundEffect();
  this._group();
};

scout.Table.prototype._initRow = function(row) {
  if (!(row instanceof scout.TableRow)) {
    row.parent = this;
    row = scout.create('TableRow', row);
  }
  this.rowsMap[row.id] = row;
  this.trigger('rowInit', {
    row: row
  });
  return row;
};

scout.Table.prototype._initColumns = function() {
  this.columns = this.columns.map(function(colModel, index) {
    var column = colModel;
    column.session = this.session;
    if (column instanceof scout.Column) {
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

  this._calculateTableNodeColumn();

  // Sync head and tail sort columns
  this._setHeadAndTailSortColumns();
  this.columnLayoutDirty = true;
};

scout.Table.prototype._destroy = function() {
  this._destroyColumns();
  scout.Table.parent.prototype._destroy.call(this);
};

scout.Table.prototype._destroyColumns = function() {
  this.columns.forEach(function(column) {
    column.destroy();
  });
  this.checkableColumn = null;
  this.columns = [];
};

scout.Table.prototype._calculateTableNodeColumn = function() {
  var tableNodeColumn = scout.arrays.first(this.visibleColumns(false));
  if (this.tableNodeColumn && this.tableNodeColumn !== tableNodeColumn) {
    // restore
    this.tableNodeColumn.minWidth = this.tableNodeColumn._initialMinWidth;
  }
  this.tableNodeColumn = tableNodeColumn;
  if (this.tableNodeColumn) {
    this.tableNodeColumn._initialMinWidth = this.tableNodeColumn.minWidth;
    this.tableNodeColumn.minWidth = this.rowLevelPadding * this._maxLevel + this.tableNodeColumn.tableNodeLevel0CellPadding + 8;

    if (this.tableNodeColumn.minWidth > this.tableNodeColumn.width) {
      if (this.rendered) {
        this.resizeColumn(this.tableNodeColumn, this.tableNodeColumn.minWidth);
      } else {
        this.tableNodeColumn.width = this.tableNodeColumn.minWidth;
      }
    }
  }
};

/**
 * @override
 */
scout.Table.prototype._createLoadingSupport = function() {
  return new scout.LoadingSupport({
    widget: this,
    $container: function() {
      return this.$data;
    }.bind(this)
  });
};

/**
 * @override
 */
scout.Table.prototype._createKeyStrokeContext = function() {
  return new scout.KeyStrokeContext();
};

/**
 * @override
 */
scout.Table.prototype._initKeyStrokeContext = function() {
  scout.Table.parent.prototype._initKeyStrokeContext.call(this);

  this._initTableKeyStrokeContext();
};

scout.Table.prototype._initTableKeyStrokeContext = function() {
  this.keyStrokeContext.registerKeyStroke([
    new scout.TableNavigationUpKeyStroke(this),
    new scout.TableNavigationDownKeyStroke(this),
    new scout.TableNavigationPageUpKeyStroke(this),
    new scout.TableNavigationPageDownKeyStroke(this),
    new scout.TableNavigationHomeKeyStroke(this),
    new scout.TableNavigationEndKeyStroke(this),
    new scout.TableNavigationCollapseKeyStroke(this),
    new scout.TableNavigationExpandKeyStroke(this),
    new scout.TableFocusFilterFieldKeyStroke(this),
    new scout.TableStartCellEditKeyStroke(this),
    new scout.TableSelectAllKeyStroke(this),
    new scout.TableRefreshKeyStroke(this),
    new scout.TableToggleRowKeyStroke(this),
    new scout.TableCopyKeyStroke(this),
    new scout.ContextMenuKeyStroke(this, this.showContextMenu, this),
    new scout.AppLinkKeyStroke(this, this.handleAppLinkAction)
  ]);

  // Prevent default action and do not propagate ↓ or ↑ keys if ctrl- or alt-modifier is not pressed.
  // Otherwise, an '↑-event' on the first row, or an '↓-event' on the last row will bubble up (because not consumed by table navigation keystrokes) and cause a superior table to move its selection.
  // Use case: - outline page table with search form that contains a table field;
  //           - shift + '↑-event'/'↓-event' are not consumed by a single selection table, and would propagate otherwise;
  //           - preventDefault because of smartfield, so that the cursor is not moved on first or last row;
  this.keyStrokeContext.registerStopPropagationInterceptor(function(event) {
    if (!event.ctrlKey && !event.altKey && scout.isOneOf(event.which, scout.keys.UP, scout.keys.DOWN)) {
      event.stopPropagation();
      event.preventDefault();
    }
  });
};

scout.Table.prototype._insertBooleanColumn = function() {
  // don't add checkbox column when we're in checkableStyle mode
  if (this.checkableStyle === scout.Table.CheckableStyle.TABLE_ROW) {
    return;
  }
  var column = scout.create('BooleanColumn', {
    session: this.session,
    fixedWidth: true,
    fixedPosition: true,
    guiOnly: true,
    headerMenuEnabled: false,
    showSeparator: false,
    width: scout.Column.NARROW_MIN_WIDTH,
    table: this
  });

  scout.arrays.insert(this.columns, column, 0);
  this.checkableColumn = column;
};

scout.Table.prototype._insertRowIconColumn = function() {
  var position = 0,
    column = scout.create('IconColumn', {
      session: this.session,
      fixedWidth: true,
      fixedPosition: true,
      guiOnly: true,
      headerMenuEnabled: false,
      showSeparator: false,
      width: this.rowIconColumnWidth,
      table: this
    });
  if (this.columns[0] === this.checkableColumn) {
    position = 1;
  }
  scout.arrays.insert(this.columns, column, position);
  this.rowIconColumn = column;
};

scout.Table.prototype.handleAppLinkAction = function(event) {
  var $appLink = $(event.target);
  var column = this._columnAtX($appLink.offset().left);
  this._triggerAppLinkAction(column, $appLink.data('ref'));
};

scout.Table.prototype._render = function() {
  this.$container = this.$parent.appendDiv('table');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.TableLayout(this));
  this.htmlComp.pixelBasedSizing = false;

  if (this.uiCssClass) {
    this.$container.addClass(this.uiCssClass);
  }

  this.$data = this.$container.appendDiv('table-data');
  this.$data.on('mousedown', '.table-row', this._onRowMouseDown.bind(this))
    .on('mouseup', '.table-row', this._onRowMouseUp.bind(this))
    .on('dblclick', '.table-row', this._onRowDoubleClick.bind(this))
    .on('contextmenu', function(event) {
      event.preventDefault();
    });
  this._installScrollbars({
    axis: 'both'
  });
  this._installImageListeners();
  this._installCellTooltipSupport();
  this.menuBar.render();

  // layout bugfix for IE9 (and maybe other browsers)
  if (scout.device.tableAdditionalDivRequired) {
    // determine @table-cell-padding-left and @table-cell-padding-right (actually the sum)
    var test = this.$data.appendDiv('table-cell');
    test.text('&nbsp;');
    this.cellHorizontalPadding = test.cssPxValue('padding-left') + test.cssPxValue('padding-right');
    test.remove();
  }

  this._calculateRowBorderWidth();
  this._updateRowWidth();
  this._updateRowHeight();
  this._renderViewport();
  if (this.scrollToSelection) {
    this.revealSelection();
  }
  this.session.desktop.on('popupOpen', this._popupOpenHandler);
};

scout.Table.prototype._renderProperties = function() {
  scout.Table.parent.prototype._renderProperties.call(this);
  this._renderTableHeader();
  this._renderFooterVisible();
  this._renderDropType();
  this._renderCheckableStyle();
  this._renderHierarchicalStyle();
};

scout.Table.prototype._setCssClass = function(cssClass) {
  scout.Table.parent.prototype._setCssClass.call(this, cssClass);
  // calculate row level padding
  var paddingClasses = ['table-row-level-padding'];
  if (this.cssClass) {
    paddingClasses.push(this.cssClass);
  }
  this.setRowLevelPadding(scout.styles.getSize(paddingClasses.reduce(function(acc, cssClass) {
    return acc + ' ' + cssClass;
  }, ''), 'width', 'width', 15));
};

scout.Table.prototype._remove = function() {
  this.session.desktop.off('popupOpen', this._popupOpenHandler);
  this._uninstallDragAndDropHandler();
  // TODO [7.0] cgu do not delete header, implement according to footer
  this.header = null;
  this._destroyCellEditorPopup();
  this._removeAggregateRows();
  this._uninstallImageListeners();
  this._uninstallCellTooltipSupport();
  this._removeRows();
  this.$fillBefore = null;
  this.$fillAfter = null;
  this.$data = null;
  this.$emptyData = null;
  scout.Table.parent.prototype._remove.call(this);
};

scout.Table.prototype.setRowLevelPadding = function(rowLevelPadding) {
  this.setProperty('rowLevelPadding', rowLevelPadding);
};

scout.Table.prototype._renderRowLevelPadding = function() {
  this._rerenderViewport();
};

scout.Table.prototype.setTableControls = function(controls) {
  this.setProperty('tableControls', controls);
};

scout.Table.prototype._renderTableControls = function() {
  if (this.footer) {
    this.footer._renderControls();
  }
};

scout.Table.prototype._setTableControls = function(controls) {
  var i;
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
};

/**
 * When an IMG has been loaded we must update the stored height in the model-row.
 * Note: we don't change the width of the row or table.
 */
scout.Table.prototype._onImageLoadOrError = function(event) {
  var $target = $(event.target);
  if ($target.data('measure') === 'in-progress') {
    // Ignore events created by autoOptimizeWidth measurement (see ColumnOptimalWidthMeasurer)
    // Using event.stopPropagation() is not possible because the image load event does not bubble
    return;
  }

  var $row = $target.closest('.table-row');
  var row = $row.data('row');
  if (!row) {
    return; // row was removed while loading the image
  }
  var oldRowHeight = row.height;
  row.height = $row.outerHeight(true);
  if (oldRowHeight !== row.height) {
    this.invalidateLayoutTree();
  }
};

scout.Table.prototype._onRowMouseDown = function(event) {
  this._doubleClickSupport.mousedown(event);
  this._$mouseDownRow = $(event.currentTarget);
  this._mouseDownRowId = this._$mouseDownRow.data('row').id;
  this._mouseDownColumn = this._columnAtX(event.pageX);
  this._$mouseDownRow.window().one('mouseup', function() {
    this._$mouseDownRow = null;
    this._mouseDownRowId = null;
    this._mouseDownColumn = null;
  }.bind(this));
  this.setContextColumn(this._columnAtX(event.pageX));
  this.selectionHandler.onMouseDown(event);
  var isRightClick = event.which === 3;
  var row = this._$mouseDownRow.data('row');

  // For checkableStyle TableRow only: check row if left click OR clicked row was not checked yet
  if (this.checkableStyle === scout.Table.CheckableStyle.TABLE_ROW && (!isRightClick || !row.checked)) {
    this.checkRow(row, !row.checked);
  }
  if (isRightClick) {
    this.showContextMenu({
      pageX: event.pageX,
      pageY: event.pageY
    });
    return false;
  }
};

scout.Table.prototype._onRowMouseUp = function(event) {
  var $row, $mouseUpRow, column, $appLink, row,
    mouseButton = event.which,
    $target = $(event.target);

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

  row = $row.data('row');
  // handle expansion
  if ($target.hasClass('table-row-control') ||
    $target.parent().hasClass('table-row-control')) {
    if (row.expanded) {
      this.collapseRow(row);
    } else {
      this.expandRow(row);
    }
    return;
  }
  if (mouseButton === 1) {
    column.onMouseUp(event, $row);
    $appLink = this._find$AppLink(event);
  }
  if ($appLink) {
    this._triggerAppLinkAction(column, $appLink.data('ref'));
  } else {
    this._triggerRowClick(row, mouseButton);
  }
};

scout.Table.prototype._onRowDoubleClick = function(event) {
  var $row = $(event.currentTarget),
    column = this._columnAtX(event.pageX);

  this.doRowAction($row.data('row'), column);
};

scout.Table.prototype.showContextMenu = function(options) {
  this.session.onRequestsDone(this._showContextMenu.bind(this, options));
};

scout.Table.prototype._showContextMenu = function(options) {
  options = options || {};
  if (!this.rendered || !this.attached) { // check needed because function is called asynchronously
    return;
  }
  if (this.selectedRows.length === 0) {
    return;
  }
  var menuItems = this._filterMenus(this.menus, scout.MenuDestinations.CONTEXT_MENU, true, false, ['Header']);
  if (menuItems.length === 0) {
    return;
  }
  var pageX = scout.nvl(options.pageX, null);
  var pageY = scout.nvl(options.pageY, null);
  if (pageX === null || pageY === null) {
    var rowToDisplay = this.isRowSelectedAndVisible(this.selectionHandler.lastActionRow) ? this.selectionHandler.lastActionRow : this.getLastSelectedAndVisibleRow();
    if (rowToDisplay !== null) {
      var $rowToDisplay = rowToDisplay.$row;
      var offset = $rowToDisplay.offset();
      var dataOffsetBounds = scout.graphics.offsetBounds(this.$data);
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
};

scout.Table.prototype.isRowSelectedAndVisible = function(row) {
  if (!this.isRowSelected(row) || !row.$row) {
    return false;
  }
  return scout.graphics.offsetBounds(row.$row).intersects(scout.graphics.offsetBounds(this.$data));
};

scout.Table.prototype.getLastSelectedAndVisibleRow = function() {
  for (var i = this.viewRangeRendered.to; i >= this.viewRangeRendered.from; i--) {
    if (this.isRowSelectedAndVisible(this.rows[i])) {
      return this.rows[i];
    }
  }
  return null;
};

scout.Table.prototype.onColumnVisibilityChanged = function(column) {
  this.columnLayoutDirty = true;
  if (this.rendered) {
    this._updateRowWidth();
    this._redraw();
    this.invalidateLayoutTree();
  }
  this.trigger('columnStructureChanged');
};

/**
 * @override
 */
scout.Table.prototype._onScroll = function() {
  var scrollTop = this.$data[0].scrollTop;
  var scrollLeft = this.$data[0].scrollLeft;
  if (this.scrollTop !== scrollTop) {
    this._renderViewport();
  }
  this.scrollTop = scrollTop;
  this.scrollLeft = scrollLeft;
};

scout.Table.prototype._renderTableStatus = function() {
  this.trigger('statusChanged');
};

scout.Table.prototype.setContextColumn = function(contextColumn) {
  this.setProperty('contextColumn', contextColumn);
};

scout.Table.prototype._hasVisibleTableControls = function() {
  return this.tableControls.some(function(control) {
    return control.visible;
  });
};

scout.Table.prototype.hasAggregateTableControl = function() {
  return this.tableControls.some(function(control) {
    return control instanceof scout.AggregateTableControl;
  });
};

scout.Table.prototype._createHeader = function() {
  return scout.create('TableHeader', {
    parent: this,
    table: this,
    enabled: this.headerEnabled,
    headerMenusEnabled: this.headerMenusEnabled
  });
};

scout.Table.prototype._createFooter = function() {
  return scout.create('TableFooter', {
    parent: this,
    table: this
  });
};

scout.Table.prototype._installCellTooltipSupport = function() {
  scout.tooltips.install(this.$data, {
    parent: this,
    selector: '.table-cell',
    text: this._cellTooltipText.bind(this),
    arrowPosition: 50,
    arrowPositionUnit: '%',
    nativeTooltip: !scout.device.isCustomEllipsisTooltipPossible()
  });
};

scout.Table.prototype._uninstallCellTooltipSupport = function() {
  scout.tooltips.uninstall(this.$data);
};

scout.Table.prototype._cellTooltipText = function($cell) {
  var cell, tooltipText,
    $row = $cell.parent(),
    column = this.columnFor$Cell($cell, $row),
    row = $row.data('row');

  if (row) {
    cell = this.cell(column, row);
    tooltipText = cell.tooltipText;
  }

  if (tooltipText) {
    return tooltipText;
  } else if (this._isTruncatedCellTooltipEnabled(column) && $cell.isContentTruncated()) {
    return scout.strings.plainText($cell.html(), {
      trim: true
    });
  }
};

/**
 * Show cell tooltip only if it is not possible to resize the column
 */
scout.Table.prototype._isTruncatedCellTooltipEnabled = function(column) {
  return !this.headerVisible || !this.headerEnabled || column.fixedWidth;
};

scout.Table.prototype.reload = function() {
  if (!this.hasReloadHandler) {
    return;
  }
  this._removeRows();
  this._renderFiller();
  this._triggerReload();
};

/**
 * @override
 */
scout.Table.prototype.setLoading = function(loading) {
  if (!loading && this.updateBuffer.isBuffering()) {
    // Don't abort loading while buffering, the buffer will do it at the end
    return;
  }
  scout.Table.parent.prototype.setLoading.call(this, loading);
};

scout.Table.prototype.exportToClipboard = function() {
  this._triggerClipboardExport();
};

/**
 * JS implementation of AbstractTable.execCopy(rows)
 */
scout.Table.prototype._exportToClipboard = function() {
  scout.clipboard.copyText({
    parent: this,
    text: this._selectedRowsToText()
  });
};

scout.Table.prototype._selectedRowsToText = function() {
  var columns = this.visibleColumns();
  return this.selectedRows.map(function(row) {
    return columns.map(function(column) {
      var cell = column.cell(row);
      var text;
      if (column instanceof scout.BooleanColumn) {
        text = (cell.value ? 'X' : '');
      } else if (cell.htmlEnabled) {
        text = scout.strings.plainText(cell.text);
      } else {
        text = cell.text;
      }
      // unwrap
      return scout.strings.nvl(text)
        .replace(/\r/g, '')
        .replace(/[\n\t]/g, ' ')
        .replace(/[ ]+/g, ' ');
    }).join('\t');
  }).join('\n');
};

scout.Table.prototype.setMultiSelect = function(multiSelect) {
  this.setProperty('multiSelect', multiSelect);
};

scout.Table.prototype.toggleSelection = function() {
  if (this.selectedRows.length === this.visibleRows.length) {
    this.deselectAll();
  } else {
    this.selectAll();
  }
};

scout.Table.prototype.selectAll = function() {
  this.selectRows(this.visibleRows);
};

scout.Table.prototype.deselectAll = function() {
  this.selectRows([]);
};

scout.Table.prototype.checkAll = function(checked) {
  this.checkRows(this.visibleRows, {
    checked: checked
  });
};

scout.Table.prototype.uncheckAll = function() {
  this.checkAll(false);
};

scout.Table.prototype.updateScrollbars = function() {
  scout.scrollbars.update(this.$data);
};

scout.Table.prototype._sort = function(animateAggregateRows) {
  var sortColumns = this._sortColumns();

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
  sortColumns = scout.arrays.union(sortColumns, this.columns);

  this._sortImpl(sortColumns);
  this._triggerRowOrderChanged();
  if (this.rendered) {
    this._renderRowOrderChanges();
  }

  // Do it after row order has been rendered, because renderRowOrderChangeds rerenders the whole viewport which would destroy the animation
  this._group(animateAggregateRows);

  // Sort was possible -> return true
  return true;
};

/**
 * @returns whether or not sorting is possible. Asks each column to answer this question by calling Column#isSortingPossible.
 */
scout.Table.prototype._isSortingPossible = function(sortColumns) {
  return sortColumns.every(function(column) {
    return column.isSortingPossible();
  });
};

scout.Table.prototype._sortColumns = function() {
  var sortColumns = [];
  for (var c = 0; c < this.columns.length; c++) {
    var column = this.columns[c];
    var sortIndex = column.sortIndex;
    if (sortIndex >= 0) {
      sortColumns[sortIndex] = column;
    }
  }
  return sortColumns;
};

scout.Table.prototype._sortImpl = function(sortColumns) {
  var sortFunction = function(row1, row2) {
    for (var s = 0; s < sortColumns.length; s++) {
      var column = sortColumns[s];
      var result = column.compare(row1, row2);
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
  }.bind(this);

  if (this.hierarchical) {
    // sort tree and set flat row array afterwards.
    this._sortHierarchical(sortFunction);
    var sortedFlatRows = [];
    this.visitRows(function(row) {
      sortedFlatRows.push(row);
    }.bind(this));
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
};

/**
 * Pre-order (top-down) traversal of all rows in this table (if hierarchical).
 */
scout.Table.prototype.visitRows = function(visitFunc, rows, level) {
  level = scout.nvl(level, 0);
  rows = rows || this.rootRows;
  rows.forEach(function(row) {
    visitFunc(row, level);
    this.visitRows(visitFunc, row.childRows, level + 1);
  }, this);
};

scout.Table.prototype._sortHierarchical = function(sortFunc, rows) {
  rows = rows || this.rootRows;
  rows.sort(sortFunc);
  rows.forEach(function(row) {
    this._sortHierarchical(sortFunc, row.childRows);
  }, this);
};

scout.Table.prototype._renderRowOrderChanges = function() {
  var animate,
    $rows = this.$rows(),
    oldRowPositions = {};

  // store old position
  // animate only if every row is rendered, otherwise some rows would be animated and some not
  if ($rows.length === this.visibleRows.length) {
    $rows.each(function(index, elem) {
      var rowWasInserted = false,
        $row = $(elem),
        row = $row.data('row');

      // Prevent the order animation for newly inserted rows (to not confuse the user)
      if (this._insertedRows) {
        for (var i = 0; i < this._insertedRows.length; i++) {
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
    }.bind(this));
  }

  this._rerenderViewport();
  // If aggregate rows are being removed by animation, rerenderViewport does not delete them -> reorder
  // This may happen if grouping gets deactivated and another column will get the new first sort column
  this._order$AggregateRows();

  // for less than animationRowLimit rows: move to old position and then animate
  if (animate) {
    $rows = this.$rows();
    $rows.each(function(index, elem) {
      var $row = $(elem),
        row = $row.data('row'),
        oldTop = oldRowPositions[row.id];

      if (oldTop !== undefined) {
        $row.css('top', oldTop - $row.offset().top).animate({
          top: 0
        }, {
          progress: this._triggerRowOrderChanged.bind(this, row, true)
        });
      }
    }.bind(this));
  }
};

scout.Table.prototype.setSortEnabled = function(sortEnabled) {
  this.setProperty('sortEnabled', sortEnabled);
};

/**
 * @param multiSort true to add the column to list of sorted columns. False to use this column exclusively as sort column (reset other columns)
 * @param remove true to remove the column from the sort columns
 */
scout.Table.prototype.sort = function(column, direction, multiSort, remove) {
  var data, sorted, animateAggregateRows;
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
};

scout.Table.prototype._addSortColumn = function(column, direction, multiSort) {
  var groupColCount, sortColCount;
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

  column.sortAscending = direction === 'asc' ? true : false;
  column.sortActive = true;
};

/**
 * Intended to be called for new sort columns.
 * Sets the sortIndex of the given column and its siblings.
 */
scout.Table.prototype._updateSortIndexForColumn = function(column, multiSort) {
  var deviation,
    sortIndex = -1;

  if (multiSort) {
    // if not already sorted set the appropriate sort index (check for sortIndex necessary if called by _onColumnHeadersUpdated)
    if (!column.sortActive || column.sortIndex === -1) {
      sortIndex = Math.max(-1, scout.arrays.max(this.columns.map(function(c) {
        return (c.sortIndex === undefined || c.initialAlwaysIncludeSortAtEnd) ? -1 : c.sortIndex;
      })));
      column.sortIndex = sortIndex + 1;

      // increase sortIndex for all permanent tail columns (a column has been added in front of them)
      this._permanentTailSortColumns.forEach(function(c) {
        c.sortIndex++;
      });
    }
  } else {
    // do not update sort index for permanent head/tail sort columns, their order is fixed (see ColumnSet.java)
    if (!(column.initialAlwaysIncludeSortAtBegin || column.initialAlwaysIncludeSortAtEnd)) {
      column.sortIndex = this._permanentHeadSortColumns.length;
    }

    // remove sort index for siblings (ignore permanent head/tail columns, only if not multi sort)
    scout.arrays.eachSibling(this.columns, column, function(siblingColumn) {
      if (siblingColumn.sortActive) {
        this._removeSortColumnInternal(siblingColumn);
      }
    }.bind(this));

    // set correct sort index for all permanent tail sort columns
    deviation = (column.initialAlwaysIncludeSortAtBegin || column.initialAlwaysIncludeSortAtEnd) ? 0 : 1;
    this._permanentTailSortColumns.forEach(function(c, index) {
      c.sortIndex = this._permanentHeadSortColumns.length + deviation + index;
    }, this);
  }
};

scout.Table.prototype._removeSortColumn = function(column) {
  if (column.initialAlwaysIncludeSortAtBegin || column.initialAlwaysIncludeSortAtEnd) {
    return;
  }
  // Adjust sibling columns with higher index
  scout.arrays.eachSibling(this.columns, column, function(siblingColumn) {
    if (siblingColumn.sortIndex > column.sortIndex) {
      siblingColumn.sortIndex = siblingColumn.sortIndex - 1;
    }
  });
  this._removeSortColumnInternal(column);
};

scout.Table.prototype._removeSortColumnInternal = function(column) {
  if (column.initialAlwaysIncludeSortAtBegin || column.initialAlwaysIncludeSortAtEnd) {
    return;
  }
  column.sortActive = false;
  column.grouped = false;
  column.sortIndex = -1;
};

scout.Table.prototype.isGroupingPossible = function(column) {
  var possible = true;

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
    scout.arrays.eachSibling(this._permanentHeadSortColumns, column, function(c) {
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
  this._permanentHeadSortColumns.forEach(function(c) {
    possible = possible && c.grouped;
  });
  return possible;
};

scout.Table.prototype.isAggregationPossible = function(column) {
  if (!(column instanceof scout.NumberColumn)) {
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
};

scout.Table.prototype.changeAggregation = function(column, func) {
  this.changeAggregations([column], [func]);
};

scout.Table.prototype.changeAggregations = function(columns, functions) {
  columns.forEach(function(column, i) {
    var func = functions[i];
    if (column.aggregationFunction !== func) {
      column.setAggregationFunction(func);
      this._triggerAggregationFunctionChanged(column);
    }
  }, this);

  this._group();
};

scout.Table.prototype._addGroupColumn = function(column, direction, multiGroup) {
  var sortIndex = -1;

  if (!this.isGroupingPossible(column)) {
    return;
  }

  direction = scout.nvl(direction, column.sortAscending ? 'asc' : 'desc');
  multiGroup = scout.nvl(multiGroup, true);
  if (!(column.initialAlwaysIncludeSortAtBegin || column.initialAlwaysIncludeSortAtEnd)) {
    // do not update sort index for permanent head/tail sort columns, their order is fixed (see ColumnSet.java)
    if (multiGroup) {

      sortIndex = Math.max(-1, scout.arrays.max(this.columns.map(function(c) {
        return (c.sortIndex === undefined || c.initialAlwaysIncludeSortAtEnd || !c.grouped) ? -1 : c.sortIndex;
      })));

      if (!column.sortActive) {
        // column was not yet present: insert at determined position
        // and move all subsequent nodes by one.
        // add just after all other grouping columns in column set.
        column.sortIndex = sortIndex + 1;
        scout.arrays.eachSibling(this.columns, column, function(siblingColumn) {
          if (siblingColumn.sortActive && !(siblingColumn.initialAlwaysIncludeSortAtBegin || siblingColumn.initialAlwaysIncludeSortAtEnd) && siblingColumn.sortIndex > sortIndex) {
            siblingColumn.sortIndex++;
          }
        });

        // increase sortIndex for all permanent tail columns (a column has been added in front of them)
        this._permanentTailSortColumns.forEach(function(c) {
          c.sortIndex++;
        });
      } else {
        // column already sorted, update position:
        // move all sort columns between the newly determined sortindex and the old sortindex by one.
        scout.arrays.eachSibling(this.columns, column, function(siblingColumn) {
          if (siblingColumn.sortActive && !(siblingColumn.initialAlwaysIncludeSortAtBegin || siblingColumn.initialAlwaysIncludeSortAtEnd) &&
            (siblingColumn.sortIndex > sortIndex) &&
            (siblingColumn.sortIndex < column.sortIndex)) {
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
        scout.arrays.eachSibling(this.columns, column, function(siblingColumn) {
          if (siblingColumn.sortActive && !(siblingColumn.initialAlwaysIncludeSortAtBegin || siblingColumn.initialAlwaysIncludeSortAtEnd) &&
            (siblingColumn.sortIndex >= sortIndex) &&
            (siblingColumn.sortIndex < column.sortIndex)) {
            siblingColumn.sortIndex++;
          }
        });
        column.sortIndex = sortIndex;
      } else { //not sorted yet
        scout.arrays.eachSibling(this.columns, column, function(siblingColumn) {
          if (siblingColumn.sortActive && !(siblingColumn.initialAlwaysIncludeSortAtBegin || siblingColumn.initialAlwaysIncludeSortAtEnd) && siblingColumn.sortIndex >= sortIndex) {
            siblingColumn.sortIndex++;
          }
        });

        column.sortIndex = sortIndex;

        // increase sortIndex for all permanent tail columns (a column has been added in front of them)
        this._permanentTailSortColumns.forEach(function(c) {
          c.sortIndex++;
        });
      }

      // remove all other grouped properties:
      scout.arrays.eachSibling(this.columns, column, function(siblingColumn) {
        if (siblingColumn.sortActive && !(siblingColumn.initialAlwaysIncludeSortAtBegin || siblingColumn.initialAlwaysIncludeSortAtEnd) && siblingColumn.sortIndex >= sortIndex) {
          siblingColumn.grouped = false;
        }
      });

    }

    column.sortAscending = direction === 'asc' ? true : false;
    column.sortActive = true;

  } else {

    if (column.initialAlwaysIncludeSortAtBegin) {
      // do not change order or direction. just set grouped to true.
      column.grouped = true;
    }

  }

  column.grouped = true;
};

scout.Table.prototype._removeGroupColumn = function(column) {
  column.grouped = false;

  if (column.initialAlwaysIncludeSortAtBegin) {
    // head sort case: remove all groupings after this column.
    this.columns.forEach(function(c) {
      if (c.sortIndex >= column.sortIndex) {
        c.grouped = false;
      }
    });
  }

  this._removeSortColumn(column);
};

scout.Table.prototype._buildRowDiv = function(row) {
  var rowWidth = this.rowWidth;
  var rowClass = 'table-row';
  if (row.cssClass) {
    rowClass += ' ' + row.cssClass;
  }
  if (!row.enabled) {
    rowClass += ' disabled';
  }
  if (row.checked && this.checkableStyle === scout.Table.CheckableStyle.TABLE_ROW) {
    rowClass += ' checked';
  }
  // if a row is not filterAccepted it must be visible since any of its child rows are filter accepted.
  if (!row.filterAccepted) {
    rowClass += ' filter-not-accepted';
  }
  if (scout.arrays.empty(row.childRows)) {
    rowClass += ' leaf';
  }

  var i, column,
    rowDiv = '<div class="' + rowClass + '" style="width: ' + rowWidth + 'px"' + scout.device.unselectableAttribute.string + '>';
  for (i = 0; i < this.columns.length; i++) {
    column = this.columns[i];
    if (column.isVisible()) {
      rowDiv += column.buildCellForRow(row);
    }
  }
  rowDiv += '</div>';

  return rowDiv;
};

scout.Table.prototype._calculateRowBorderWidth = function() {
  var $tableRowDummy = this.$data.appendDiv('table-row');
  this.rowBorderLeftWidth = $tableRowDummy.cssBorderLeftWidth();
  this.rowBorderRightWidth = $tableRowDummy.cssBorderRightWidth();
  this.rowBorderWidth = this.rowBorderLeftWidth + this.rowBorderRightWidth;
  $tableRowDummy.remove();
};

scout.Table.prototype._updateRowWidth = function() {
  this.rowWidth = this.visibleColumns().reduce(function(sum, column) {
    return sum + column.width;
  }, this.rowBorderWidth);
};

scout.Table.prototype._updateRowHeight = function() {
  var $emptyRow = this.$data.appendDiv('table-row');
  var $emptyAggrRow = this.$data.appendDiv('table-aggregate-row');

  $emptyRow.appendDiv('table-cell').html('&nbsp;');
  $emptyAggrRow.appendDiv('table-cell').html('&nbsp;');
  this.rowHeight = $emptyRow.outerHeight(true);
  this.aggregateRowHeight = $emptyAggrRow.outerHeight(true);
  $emptyRow.remove();
  $emptyAggrRow.remove();
};

/**
 * Updates the row heights for every visible row and aggregate row and clears the height of the others
 */
scout.Table.prototype._updateRowHeights = function() {
  this.rows.forEach(function(row) {
    if (!row.$row) {
      row.height = null;
    } else {
      row.height = row.$row.outerHeight(true);
    }
  });
  this._aggregateRows.forEach(function(aggregateRow) {
    if (!aggregateRow.$row) {
      aggregateRow.height = null;
    } else {
      aggregateRow.height = aggregateRow.$row.outerHeight(true);
    }
  });
};

scout.Table.prototype._renderRowsInRange = function(range) {
  var $rows,
    rowString = '',
    numRowsRendered = 0,
    prepend = false;

  var rows = this.visibleRows;
  if (rows.length === 0) {
    return;
  }

  var maxRange = new scout.Range(0, this.rows.length);
  range = maxRange.intersect(range);
  if (this.viewRangeRendered.size() > 0 && !range.intersect(this.viewRangeRendered).equals(new scout.Range(0, 0))) {
    throw new Error('New range must not intersect with existing.');
  }
  if (range.to <= this.viewRangeRendered.from) {
    prepend = true;
  }
  var newRange = this.viewRangeRendered.union(range);
  if (newRange.length === 2) {
    throw new Error('Can only prepend or append rows to the existing range. Existing: ' + this.viewRangeRendered + '. New: ' + newRange);
  }
  this.viewRangeRendered = newRange[0];
  this._removeEmptyData();

  // Build $rows (as string instead of jQuery objects due to efficiency reasons)
  for (var r = range.from; r < range.to; r++) {
    var row = rows[r];
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
  } else {
    if (this.$fillAfter) {
      $rows = $rows.insertBefore(this.$fillAfter);
    } else {
      $rows = $rows.appendTo(this.$data);
    }
  }

  $rows.each(function(index, rowObject) {
    var $row = $(rowObject);
    var row = rows[range.from + index];
    scout.Table.linkRowToDiv(row, $row);
    this._installRow(row);
  }.bind(this));

  if ($.log.isTraceEnabled()) {
    $.log.trace(numRowsRendered + ' new rows rendered from ' + range);
    $.log.trace(this._rowsRenderedInfo());
  }
};

scout.Table.prototype._rowsRenderedInfo = function() {
  var numRenderedRows = this.$rows().length,
    renderedRowsRange = '(' + this.viewRangeRendered + ')',
    text = numRenderedRows + ' rows rendered ' + renderedRowsRange;
  return text;
};

/**
 * Moves the row to the top.
 */
scout.Table.prototype.moveRowToTop = function(row) {
  var rowIndex = this.rows.indexOf(row);
  this.moveRow(rowIndex, 0);
};

/**
 * Moves the row to the bottom.
 */
scout.Table.prototype.moveRowToBottom = function(row) {
  var rowIndex = this.rows.indexOf(row);
  this.moveRow(rowIndex, this.rows.length - 1);
};

/**
 * Moves the row one up, disregarding filtered rows.
 */
scout.Table.prototype.moveRowUp = function(row) {
  var rowIndex = this.rows.indexOf(row),
    targetIndex = rowIndex - 1;
  if (this.hierarchical) {
    // find index with same parent
    var siblings = this.rows.filter(function(candidate) {
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
};

/**
 * Moves the row one down, disregarding filtered rows.
 */
scout.Table.prototype.moveRowDown = function(row) {
  var rowIndex = this.rows.indexOf(row),
    targetIndex = rowIndex + 1;
  if (this.hierarchical) {
    // find index with same parent
    var siblings = this.rows.filter(function(candidate) {
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
};

/**
 * Moves the row one up with respected to filtered rows. Row must be one of the filtered rows.
 * @deprecated use moveVisibleRowUp instead
 */
scout.Table.prototype.moveFilteredRowUp = function(row) {
  this.moveVisibleRowUp(row);
};

scout.Table.prototype.moveVisibleRowUp = function(row) {
  var rowIndex = this.rows.indexOf(row),
    visibleIndex = this.visibleRows.indexOf(row),
    sibling,
    targetIndex;

  if (this.hierarchical) {
    var siblings = this.visibleRows.filter(function(candidate) {
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
};

/**
 * Moves the row one down with respected to filtered rows. Row must be one of the filtered rows.
 * @deprecated use moveVisibleRowDown instead
 */
scout.Table.prototype.moveFilteredRowDown = function(row) {
  this.moveVisibleRowDown(row);
};

scout.Table.prototype.moveVisibleRowDown = function(row) {
  var rowIndex = this.rows.indexOf(row),
    visibleIndex = this.visibleRows.indexOf(row),
    sibling,
    targetIndex;

  if (this.hierarchical) {
    var siblings = this.visibleRows.filter(function(candidate) {
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
};

scout.Table.prototype.moveRow = function(sourceIndex, targetIndex) {
  var rowCount = this.rows.length;
  sourceIndex = Math.max(sourceIndex, 0);
  sourceIndex = Math.min(sourceIndex, rowCount - 1);
  targetIndex = Math.max(targetIndex, 0);
  targetIndex = Math.min(targetIndex, rowCount - 1);

  if (sourceIndex === targetIndex) {
    return;
  }

  scout.arrays.move(this.rows, sourceIndex, targetIndex);
  this.updateRowOrder(this.rows);
};

scout.Table.prototype._removeRowsInRange = function(range) {
  var row, i,
    numRowsRemoved = 0,
    rows = this.visibleRows;

  var maxRange = new scout.Range(0, rows.length);
  range = maxRange.intersect(range);

  var newRange = this.viewRangeRendered.subtract(range);
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
};

scout.Table.prototype.removeAllRows = function() {
  if (this.rendered) {
    this.$rows().each(function(i, elem) {
      var $row = $(elem),
        row = $row.data('row');
      if ($row.hasClass('hiding')) {
        // Do not remove rows which are removed using an animation
        // row.$row may already point to a new row -> don't call removeRow to not accidentally remove the new row
        return;
      }
      this._removeRow(row);
    }.bind(this));
  }
  this.viewRangeRendered = new scout.Range(0, 0);
};

/**
 *
 * @param rows if undefined, all rows are removed
 */
scout.Table.prototype._removeRows = function(rows) {
  if (!rows) {
    this.removeAllRows();
    return;
  }

  var tableAttached = this.isAttachedAndRendered();
  rows = scout.arrays.ensure(rows);
  rows.forEach(function(row) {
    var rowIndex = this.visibleRows.indexOf(row);
    if (rowIndex === -1) {
      // row is not visible
      return;
    }
    var rowRendered = !!row.$row;
    var rowInViewRange = this.viewRangeRendered.contains(rowIndex);

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
  }.bind(this));
};

/**
 * Just removes the row, does NOT adjust this.viewRangeRendered
 */
scout.Table.prototype._removeRow = function(row) {
  var $row = row.$row;
  if (!$row) {
    return;
  }

  this._destroyTooltipsForRow(row);
  this._removeCellEditorForRow(row);

  // Do not remove rows which are removed using an animation
  if (!$row.hasClass('hiding')) {
    $row.remove();
    row.$row = null;
  }
};

/**
 * Animates the rendering of a row by setting it to invisible before doing a slideDown animation. The row needs to already be rendered.
 */
scout.Table.prototype._showRow = function(row) {
  var $row = row.$row;
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
};

/**
 * Animates the removal of a row by doing a slideUp animation. The row will be removed after the animation finishes.
 */
scout.Table.prototype._hideRow = function(row) {
  var $row = row.$row;
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
};

/**
 * This method should be used after a row is added to the DOM (new rows, updated rows). The 'row'
 * is expected to be linked with the corresponding '$row' (row.$row and $row.data('row')).
 */
scout.Table.prototype._installRow = function(row) {
  row.height = row.$row.outerHeight(true);

  if (row.hasError) {
    this._showCellErrorForRow(row);
  }
  // Reopen editor popup (closed when row was removed)
  if (this.cellEditorPopup && !this.cellEditorPopup.rendered && this.cellEditorPopup.row.id === row.id) {
    var editorField = this.cellEditorPopup.cell.field;
    this.startCellEdit(this.cellEditorPopup.column, row, editorField);
  }
};

scout.Table.prototype._calcRowLevelPadding = function(row) {
  if (!row) {
    return -this.rowLevelPadding;
  }
  return this._calcRowLevelPadding(row.parentRow) + this.rowLevelPadding;
};

scout.Table.prototype._showCellErrorForRow = function(row) {
  var $cells = this.$cellsForRow(row.$row),
    that = this;

  $cells.each(function(index) {
    var $cell = $(this);
    var cell = that.cellByCellIndex(index, row);
    if (cell.errorStatus) {
      that._showCellError(row, $cell, cell.errorStatus);
    }
  });
};

scout.Table.prototype._showCellError = function(row, $cell, errorStatus) {
  var tooltip, opts,
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
};

/**
 * @returns the column at position x (e.g. from event.pageX)
 */
scout.Table.prototype._columnAtX = function(x) {
  var columnOffsetRight = 0,
    columnOffsetLeft = this.$data.offset().left + this.rowBorderLeftWidth,
    scrollLeft = this.$data.scrollLeft();

  if (x < columnOffsetLeft) {
    // Clicked left of first column (on selection border) --> return first column
    return this.columns[0];
  }

  columnOffsetLeft -= scrollLeft;
  var visibleColumns = this.visibleColumns();
  var column = scout.arrays.find(visibleColumns, function(column) {
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
};

scout.Table.prototype._find$AppLink = function(event) {
  // bubble up from target to delegateTarget
  var $elem = $(event.target);
  var $stop = $(event.delegateTarget);
  while ($elem.length > 0) {
    if ($elem.hasClass('app-link')) {
      return $elem;
    }
    if ($elem[0] === $stop[0]) {
      return null;
    }
    $elem = $elem.parent();
  }
  return null;
};

scout.Table.prototype._filterMenus = function(menus, destination, onlyVisible, enableDisableKeyStroke, notAllowedTypes) {
  return scout.menus.filterAccordingToSelection('Table', this.selectedRows.length, menus, destination, onlyVisible, enableDisableKeyStroke, notAllowedTypes);
};

scout.Table.prototype.setStaticMenus = function(staticMenus) {
  this.setProperty('staticMenus', staticMenus);
  this._updateMenuBar();
};

scout.Table.prototype._removeMenus = function() {
  // menubar takes care about removal
};

scout.Table.prototype.notifyRowSelectionFinished = function() {
  if (this._triggerRowsSelectedPending) {
    this._triggerRowsSelected();
    this._triggerRowsSelectedPending = false;
  }
  this.session.onRequestsDone(this._updateMenuBar.bind(this));
};

scout.Table.prototype._triggerRowClick = function(row, mouseButton, column) {
  var event = {
    row: row,
    mouseButton: mouseButton
  };
  this.trigger('rowClick', event);
};

scout.Table.prototype._triggerRowAction = function(row, column) {
  this.trigger('rowAction', {
    row: row,
    column: column
  });
};

/**
 * This functions starts the cell editor for the given row and column. Prepare must wait until
 * a pending completeCellEdit operation is resolved.
 */
scout.Table.prototype.prepareCellEdit = function(column, row, openFieldPopupOnCellEdit) {
  var promise = $.resolvedPromise();
  if (this.cellEditorPopup) {
    promise = this.cellEditorPopup.waitForCompleteCellEdit();
  }
  promise.then(this.prepareCellEditInternal.bind(this, column, row, openFieldPopupOnCellEdit));
};

/**
 * @param openFieldPopupOnCellEdit when this parameter is set to true, the CellEditorPopup sets an
 *    additional property 'cellEditor' on the editor-field. The field instance may use this property
 *    to decide whether or not it should open a popup immediately after it is rendered. This is used
 *    for Smart- and DateFields.
 */
scout.Table.prototype.prepareCellEditInternal = function(column, row, openFieldPopupOnCellEdit) {
  var event = new scout.Event({
    column: column,
    row: row
  });
  this.openFieldPopupOnCellEdit = scout.nvl(openFieldPopupOnCellEdit, false);
  this.trigger('prepareCellEdit', event);

  if (!event.defaultPrevented) {
    var field = column.createEditor(row);
    this.startCellEdit(column, row, field);
  }
};

/**
 * @returns returns a cell for the given column and row. Row Icon column and cell icon column don't not have cells --> generate one.
 */
scout.Table.prototype.cell = function(column, row) {
  if (column === this.rowIconColumn) {
    return scout.create('Cell', {
      iconId: row.iconId,
      cssClass: scout.strings.join(' ', 'row-icon-cell', row.cssClass)
    });
  }

  if (column === this.checkableColumn) {
    return scout.create('Cell', {
      value: row.checked,
      editable: true,
      cssClass: row.cssClass
    });
  }

  return row.cells[column.index];
};

scout.Table.prototype.cellByCellIndex = function(cellIndex, row) {
  return this.cell(this.columns[cellIndex], row);
};

scout.Table.prototype.cellValue = function(column, row) {
  var cell = this.cell(column, row);
  if (!cell) {
    return cell;
  }
  if (cell.value !== undefined) {
    return cell.value;
  }
  return '';
};

scout.Table.prototype.cellText = function(column, row) {
  var cell = this.cell(column, row);
  if (!cell) {
    return '';
  }
  return cell.text || '';
};

/**
 *
 * @returns the next editable position in the table, starting from the cell at (currentColumn / currentRow).
 * A position is an object containing row and column (cell has no reference to a row or column due to memory reasons).
 */
scout.Table.prototype.nextEditableCellPos = function(currentColumn, currentRow, reverse) {
  var pos, startColumnIndex, rowIndex, startRowIndex, predicate,
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
  scout.arrays.findFrom(this.rows, startRowIndex, predicate, reverse);

  return pos;
};

scout.Table.prototype.nextEditableCellPosForRow = function(startColumnIndex, row, reverse) {
  var cell, column, predicate;

  predicate = function(column) {
    if (column.guiOnly) {
      // does not support tabbing
      return false;
    }
    cell = this.cell(column, row);
    return this.enabled && row.enabled && cell.editable;
  }.bind(this);

  column = scout.arrays.findFrom(this.columns, startColumnIndex, predicate, reverse);
  if (column) {
    return {
      column: column,
      row: row
    };
  }
};

scout.Table.prototype.clearAggregateRows = function(animate) {
  // Remove "hasAggregateRow" markers from real rows
  this._aggregateRows.forEach(function(aggregateRow) {
    if (aggregateRow.prevRow) {
      aggregateRow.prevRow.aggregateRowAfter = null;
    }
    if (aggregateRow.nextRow) {
      aggregateRow.nextRow.aggregateRowBefore = null;
    }
  }, this);

  if (this.rendered) {
    this._removeAggregateRows(animate);
    this._renderSelection(); // fix selection borders
  }
  this._aggregateRows = [];
};

/**
 * Executes the aggregate function with the given funcName for each visible column, but only if the Column
 * has that function, which is currently only the case for NumberColumns.
 *
 * @param states is a reference to an Array containing the results for each column.
 * @param row (optional) if set, an additional cell-value parameter is passed to the aggregate function
 */
scout.Table.prototype._forEachVisibleColumn = function(funcName, states, row) {
  var value;
  this.visibleColumns().forEach(function(column, i) {
    if (column[funcName]) {
      if (row) {
        value = column.cellValueOrTextForCalculation(row);
      }
      states[i] = column[funcName](states[i], value);
    } else {
      states[i] = undefined;
    }
  });
};

scout.Table.prototype._group = function(animate) {
  var rows, nextRow, newGroup, firstRow, lastRow,
    groupColumns = this._groupedColumns(),
    onTop = this.groupingStyle === scout.Table.GroupingStyle.TOP,
    states = [];

  this.clearAggregateRows();
  if (!groupColumns.length) {
    return;
  }

  rows = this.visibleRows;
  this._forEachVisibleColumn('aggrStart', states);

  rows.forEach(function(row, r) {
    if (!firstRow) {
      firstRow = row;
    }
    this._forEachVisibleColumn('aggrStep', states, row);
    // test if sum should be shown, if yes: reset sum-array
    nextRow = rows[r + 1];
    // test if group is finished
    newGroup = (r === rows.length - 1) || this._isNewGroup(groupColumns, row, nextRow);
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
  }.bind(this));

  if (this.rendered) {
    this._renderAggregateRows(animate);
    this._renderSelection(); // fix selection borders
  }
};

scout.Table.prototype._isNewGroup = function(groupedColumns, row, nextRow) {
  var i, col, newRow = false,
    hasCellTextForGroupingFunction;

  if (!nextRow) {
    return true; //row is last row
  }

  for (i = 0; i < groupedColumns.length; i++) {
    col = groupedColumns[i];
    hasCellTextForGroupingFunction = col && col.cellTextForGrouping && typeof col.cellTextForGrouping === 'function';
    newRow = newRow || (hasCellTextForGroupingFunction && col.cellTextForGrouping(row) !== col.cellTextForGrouping(nextRow)); // NOSONAR
    newRow = newRow || (!hasCellTextForGroupingFunction && this.cellText(col, row) !== this.cellText(col, nextRow));
    if (newRow) {
      return true;
    }
  }
  return false;
};

scout.Table.prototype._groupedColumns = function() {
  return this.columns.filter(function(col) {
    return col.grouped;
  });
};

/**
 * Inserts a new aggregation row between 'prevRow' and 'nextRow'.
 *
 * @param contents cells of the new aggregate row
 * @param prevRow row _before_ the new aggregate row
 * @param nextRow row _after_ the new aggregate row
 */
scout.Table.prototype._addAggregateRow = function(contents, prevRow, nextRow) {
  var aggregateRow = {
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
};

scout.Table.prototype._removeAggregateRows = function(animate) {
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
};

scout.Table.prototype._renderAggregateRows = function(animate) {
  var onTop = this.groupingStyle === scout.Table.GroupingStyle.TOP,
    insertFunc = onTop ? 'insertBefore' : 'insertAfter';
  animate = scout.nvl(animate, false);

  this._aggregateRows.forEach(function(aggregateRow, r) {
    var refRow, $cell, $aggregateRow;

    if (aggregateRow.$row) {
      // already rendered, no need to update again (necessary for subsequent renderAggregateRows calls (e.g. in insertRows -> renderRows)
      return;
    }

    refRow = (onTop ? aggregateRow.nextRow : aggregateRow.prevRow);
    if (!refRow || !refRow.$row) {
      return;
    }

    $aggregateRow = this.$container.makeDiv('table-aggregate-row')
      .data('aggregateRow', aggregateRow);

    this.visibleColumns().forEach(function(column) {
      $cell = $(column.buildCellForAggregateRow(aggregateRow));
      $cell.appendTo($aggregateRow);
    });

    $aggregateRow[insertFunc](refRow.$row).width(this.rowWidth);
    aggregateRow.height = $aggregateRow.outerHeight(true);
    aggregateRow.$row = $aggregateRow;
    if (animate) {
      this._showRow(aggregateRow);
    }
  }, this);
};

scout.Table.prototype.groupColumn = function(column, multiGroup, direction, remove) {
  var data, sorted;
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
};

scout.Table.prototype.removeColumnGrouping = function(column) {
  if (column) {
    this.groupColumn(column, false, 'asc', true);
  }
};

/**
 * @returns {boolean} true if at least one column has grouped=true
 */
scout.Table.prototype.isGrouped = function() {
  return this.columns.some(function(column) {
    return column.grouped;
  });
};

scout.Table.prototype.setColumnBackgroundEffect = function(column, effect) {
  column.setBackgroundEffect(effect);
};

/**
 * Updates the background effect of every column, if column.backgroundEffect is set.
 * Meaning: Recalculates the min / max values and renders the background effect again.
 */
scout.Table.prototype._updateBackgroundEffect = function() {
  this.columns.forEach(function(column) {
    if (!column.backgroundEffect) {
      return;
    }
    column.updateBackgroundEffect();
  }, this);
};

/**
 * Recalculates the values necessary for the background effect of every column, if column.backgroundEffect is set
 */
scout.Table.prototype._calculateValuesForBackgroundEffect = function() {
  this.columns.forEach(function(column) {
    if (!column.backgroundEffect) {
      return;
    }
    column.calculateMinMaxValues();
  }, this);
};

scout.Table.prototype._markAutoOptimizeWidthColumnsAsDirty = function() {
  this.columns.forEach(function(column) {
    column.autoOptimizeWidthRequired = true;
  });
};

scout.Table.prototype._markAutoOptimizeWidthColumnsAsDirtyIfNeeded = function(autoOptimizeWidthColumns, oldRow, newRow) {
  var i,
    marked = false;
  for (i = autoOptimizeWidthColumns.length - 1; i >= 0; i--) {
    var column = autoOptimizeWidthColumns[i];
    if (this.cellValue(column, oldRow) !== this.cellValue(column, newRow)) {
      column.autoOptimizeWidthRequired = true;
      // Remove column from list since it is now marked and does not have to be processed next time
      autoOptimizeWidthColumns.splice(i, 1);
      marked = true;
    }
  }
  return marked;
};

scout.Table.prototype.setMultiCheck = function(multiCheck) {
  this.setProperty('multiCheck', multiCheck);
};

scout.Table.prototype.checkedRows = function() {
  return this.rows.filter(function(row) {
    return row.checked;
  });
};

scout.Table.prototype.checkRow = function(row, checked) {
  this.checkRows([row], {
    checked: checked
  });
};

scout.Table.prototype.checkRows = function(rows, options) {
  var opts = $.extend({
    checked: true,
    checkOnlyEnabled: true
  }, options);
  var checkedRows = [];
  // use enabled computed because when the parent of the table is disabled, it should not be allowed to check rows
  if (!this.checkable || (!this.enabledComputed && opts.checkOnlyEnabled)) {
    return;
  }
  rows = scout.arrays.ensure(rows);
  rows.forEach(function(row) {
    if ((!row.enabled && opts.checkOnlyEnabled) || row.checked === opts.checked) {
      return;
    }
    if (!this.multiCheck && opts.checked) {
      for (var i = 0; i < this.rows.length; i++) {
        if (this.rows[i].checked) {
          this.rows[i].checked = false;
          checkedRows.push(this.rows[i]);
        }
      }
    }
    row.checked = opts.checked;
    checkedRows.push(row);
  }, this);

  if (this.rendered) {
    checkedRows.forEach(function(row) {
      this._renderRowChecked(row);
    }, this);
  }
  this._triggerRowsChecked(checkedRows);
};

scout.Table.prototype.uncheckRow = function(row) {
  this.uncheckRows([row]);
};

scout.Table.prototype.uncheckRows = function(rows, options) {
  var opts = $.extend({
    checked: false
  }, options);
  this.checkRows(rows, opts);
};

scout.Table.prototype.isTableNodeColumn = function(column) {
  return this.hierarchical && this.tableNodeColumn === column;
};

scout.Table.prototype.collapseRow = function(row) {
  this.collapseRows(scout.arrays.ensure(row));
};

scout.Table.prototype.collapseAll = function() {
  this.expandRowsInternal(this.rootRows, false, true);
};

scout.Table.prototype.expandAll = function() {
  this.expandRowsInternal(this.rootRows, true, true);
};

scout.Table.prototype.collapseRows = function(rows, recursive) {
  this.expandRowsInternal(rows, false, recursive);
};

scout.Table.prototype.expandRow = function(row, recursive) {
  this.expandRows(scout.arrays.ensure(row));
};

scout.Table.prototype.expandRows = function(rows, recursive) {
  this.expandRowsInternal(rows, true, recursive);
};

scout.Table.prototype.expandRowsInternal = function(rows, expanded, recursive) {
  var changedRows = [],
    rowsForAnimation = [];
  rows = rows || this.rootRows;
  expanded = scout.nvl(expanded, true);
  recursive = scout.nvl(recursive, false);
  if (recursive) {
    // collect rows
    this.visitRows(function(row) {
      var changed = row.expanded !== expanded;
      if (changed) {
        row.expanded = expanded;
        changedRows.push(row);
        if (row.$row) {
          rowsForAnimation.push(row);
        }
      }
    }.bind(this), rows);
  } else {
    changedRows = rows.filter(function(row) {
      var changed = row.expanded !== expanded;
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

  if (this.rendered) {
    this._renderRowDelta();
    rowsForAnimation.forEach(function(row) {
      row.animateExpansion();
    });
  }
};

scout.Table.prototype.doRowAction = function(row, column) {
  if (this.selectedRows.length !== 1 || this.selectedRows[0] !== row) {
    // Only allow row action if the selected row was double clicked because the handler of the event expects a selected row.
    // This may happen if the user modifies the selection using ctrl or shift while double clicking.
    return;
  }

  column = column || this.columns[0];
  if (column && column.guiOnly) {
    column = scout.arrays.find(this.columns, function(col) {
      return !col.guiOnly;
    });
  }
  if (!row || !column) {
    return;
  }
  this._triggerRowAction(row, column);
};

scout.Table.prototype.insertRow = function(row) {
  this.insertRows([row]);
};

scout.Table.prototype.insertRows = function(rows) {
  var wasEmpty = this.rows.length === 0;

  // Update model
  rows.forEach(function(row, i) {
    row = this._initRow(row);
    row.status = scout.TableRow.Status.INSERTED;
    rows[i] = row;
    // Always insert new rows at the end, if the order is wrong a rowOrderChanged event will follow
    this.rows.push(row);
  }, this);

  var filterAcceptedRows = rows.filter(function(row) {
    this._applyFiltersForRow(row);
    return row.filterAccepted;
  }, this);

  this._updateRowStructure({
    updateTree: true,
    filteredRows: true,
    applyFilters: false,
    visibleRows: true
  });
  if (filterAcceptedRows.length > 0) {
    this._triggerFilter();
  }

  this._calculateValuesForBackgroundEffect();
  this._markAutoOptimizeWidthColumnsAsDirty();

  // this event should be triggered before the rowOrderChanged event (triggered by the _sort function).
  this._triggerRowsInserted(rows);
  this._sortAfterInsert(wasEmpty);

  // Update HTML
  if (this.rendered) {
    if (this.hierarchical) {
      this._renderRowOrderChanges();
    }
    // Remember inserted rows for future events like rowOrderChanged
    if (!this._insertedRows) {
      this._insertedRows = rows;
      setTimeout(function() {
        this._insertedRows = null;
      }.bind(this), 0);
    } else {
      scout.arrays.pushAll(this._insertedRows, rows);
    }

    this.viewRangeDirty = true;
    this._renderViewport();
    this.invalidateLayoutTree();
  }
};

scout.Table.prototype._sortAfterInsert = function(wasEmpty) {
  this._sort();
};

scout.Table.prototype.deleteRow = function(row) {
  this.deleteRows([row]);
};

scout.Table.prototype.deleteRows = function(rows) {
  var invalidate,
    filterChanged,
    removedRows = [];

  this.visitRows(function(row) {
    if (!this.rowsMap[row.id]) {
      return;
    }

    removedRows.push(row);
    // Update HTML
    if (this.rendered) {
      // Cancel cell editing if cell editor belongs to a cell of the deleted row
      if (this.cellEditorPopup && this.cellEditorPopup.row.id === row.id) {
        this.cellEditorPopup.cancelEdit();
      }

      this._removeRows(row);
      invalidate = true;
    }

    // Update model
    scout.arrays.remove(this.rows, row);
    scout.arrays.remove(this.visibleRows, row);
    if (this._filterCount() > 0 && scout.arrays.remove(this._filteredRows, row)) {
      filterChanged = true;
    }
    delete this.rowsMap[row.id];

    if (this.selectionHandler.lastActionRow === row) {
      this.selectionHandler.clearLastSelectedRowMarker();
    }
  }.bind(this), rows);

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
};

scout.Table.prototype.deleteAllRows = function() {
  var filterChanged = this._filterCount() > 0 && this._filteredRows.length > 0,
    rows = this.rows;

  // Update HTML
  if (this.rendered) {
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
  if (this.rendered) {
    this._renderFiller();
    this._renderViewport();
    this._renderEmptyData();
    this.invalidateLayoutTree();
  }
};

scout.Table.prototype.updateRow = function(row) {
  this.updateRows([row]);
};

scout.Table.prototype.updateRows = function(rows) {
  if (this.updateBuffer.isBuffering()) {
    this.updateBuffer.buffer(rows);
    return;
  }
  var filterChanged, autoOptimizeWidthColumnsDirty;
  var autoOptimizeWidthColumns = this.columns.filter(function(column) {
    return column.autoOptimizeWidth && !column.autoOptimizeWidthRequired;
  });

  var rowsToIndex = {};
  this.rows.forEach(function(row, index) {
    rowsToIndex[row.id] = index;
  }, this);

  var oldRowsMap = {};
  var structureChanged = false;
  rows = rows.map(function(row) {
    var parentRowId = row.parentRow,
      oldRow = this.rowsMap[row.id];
    // collect old rows
    oldRowsMap[row.id] = oldRow;
    if (!oldRow) {
      throw new Error('Update event received for non existing row. RowId: ' + row.id);
    }
    // check structure changes
    if (row.parentRow && !scout.objects.isNullOrUndefined(row.parentRow.id)) {
      parentRowId = row.parentRow.id;
    }
    structureChanged = structureChanged || row._parentRowId !== parentRowId;
    row = this._initRow(row);
    // Check if cell values have changed
    row.cells.some(function(cell, i) {
      var oldCell = oldRow.cells[i];
      if (!oldCell || oldCell.value !== cell.value) {
        row.status = scout.TableRow.Status.UPDATED;
        return true; // break loop
      }
    });
    // selection
    if (this.selectionHandler.lastActionRow === oldRow) {
      this.selectionHandler.lastActionRow = row;
    }
    scout.arrays.replace(this.selectedRows, oldRow, row);
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

  if (this.rendered) {
    // render row and replace div in DOM
    rows.forEach(function(row) {
      var oldRow = oldRowsMap[row.id],
        $updatedRow;
      if (!oldRow.$row) {
        return;
      }
      $updatedRow = $(this._buildRowDiv(row));
      $updatedRow.copyCssClasses(oldRow.$row, scout.Table.SELECTION_CLASSES + ' first last');
      oldRow.$row.replaceWith($updatedRow);
      scout.Table.linkRowToDiv(row, $updatedRow);
      this._destroyTooltipsForRow(row);
      this._removeCellEditorForRow(row);
      this._installRow(row);
    }, this);

    if (structureChanged) {
      this._renderRowOrderChanges();
    }
  }

  if (filterChanged) {
    this._triggerFilter();
    this._renderRowDelta();
  }

  this._sortAfterUpdate();
  this._updateBackgroundEffect();
  this.invalidateLayoutTree(); // this will also update the scroll-bars
};

scout.Table.prototype._sortAfterUpdate = function() {
  this._sort();
};

scout.Table.prototype.isHierarchical = function() {
  return this.hierarchical;
};

/**
 * The given rows must be rows of this table in desired order.
 * @param {scout.TableRow[]} rows
 */
scout.Table.prototype.updateRowOrder = function(rows) {
  rows = scout.arrays.ensure(rows);
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
  if (this.rendered) {
    this._renderRowOrderChanges();
  }
  this._triggerRowOrderChanged();

  this._group(this._animateAggregateRows);
  this._animateAggregateRows = false;
};

scout.Table.prototype._destroyTooltipsForRow = function(row) {
  for (var i = this.tooltips.length - 1; i >= 0; i--) {
    if (this.tooltips[i].row.id === row.id) {
      this.tooltips[i].destroy();
      this.tooltips.splice(i, 1);
    }
  }
};

scout.Table.prototype._removeCellEditorForRow = function(row) {
  if (this.cellEditorPopup && this.cellEditorPopup.rendered && this.cellEditorPopup.row.id === row.id) {
    this.cellEditorPopup.remove();
  }
};

scout.Table.prototype.startCellEdit = function(column, row, field) {
  if (!this.rendered || !this.isAttachedAndRendered()) {
    this._postRenderActions.push(this.startCellEdit.bind(this, column, row, field));
    return;
  }

  this.trigger('startCellEdit', {
    column: column,
    row: row,
    field: field
  });
  this.ensureRowRendered(row);
  var popup = column.startCellEdit(row, field);
  this.cellEditorPopup = popup;
  return popup;
};

/**
 * @param saveEditorValue when this parameter is set to true, the value of the editor field is set as
 *    new value on the edited cell. In remote case this parameter is always false, because the cell
 *    value is updated by an updateRow event instead.
 */
scout.Table.prototype.endCellEdit = function(field, saveEditorValue) {
  if (!this.rendered || !this.isAttachedAndRendered()) {
    this._postRenderActions.push(this.endCellEdit.bind(this, field, saveEditorValue));
    return;
  }

  // the cellEditorPopup could already be removed by scrolling (out of view range) or be removed by update rows
  if (this.cellEditorPopup) {
    var context = this.cellEditorPopup;

    // Remove the cell-editor popup prior destroying the field, so that the 'cell-editor-popup's focus context is
    // uninstalled first and the focus can be restored onto the last focused element of the surrounding focus context.
    // Otherwise, if the currently focused field is removed from DOM, the $entryPoint would be focused first, which can
    // be avoided if removing the popup first.
    this._destroyCellEditorPopup();

    // Must store context in a local variable and call setCellValue _after_ cellEditorPopup is set to null
    // because in updateRows we check if the popup is still there and start cell editing mode again.
    saveEditorValue = scout.nvl(saveEditorValue, false);
    if (saveEditorValue) {
      this.setCellValue(context.column, context.row, field.value);
    }
  }

  field.destroy();
};

scout.Table.prototype.completeCellEdit = function() {
  var field = this.cellEditorPopup.cell.field;
  var event = new scout.Event({
    field: field,
    row: this.cellEditorPopup.row,
    column: this.cellEditorPopup.column,
    cell: this.cellEditorPopup.celll
  });
  this.trigger('completeCellEdit', event);

  if (!event.defaultPrevented) {
    return this.endCellEdit(field, true);
  }
};

scout.Table.prototype.cancelCellEdit = function() {
  var field = this.cellEditorPopup.cell.field;
  var event = new scout.Event({
    field: field,
    row: this.cellEditorPopup.row,
    column: this.cellEditorPopup.column,
    cell: this.cellEditorPopup.celll
  });
  this.trigger('cancelCellEdit', event);

  if (!event.defaultPrevented) {
    this.endCellEdit(field);
  }
};

scout.Table.prototype.scrollTo = function(row) {
  if (this.viewRangeRendered.size() === 0) {
    // Cannot scroll to a row no row is rendered
    return;
  }
  this.ensureRowRendered(row);
  scout.scrollbars.scrollTo(this.$data, row.$row);
};

scout.Table.prototype.scrollPageUp = function() {
  var newScrollTop = Math.max(0, this.$data[0].scrollTop - this.$data.height());
  this.setScrollTop(newScrollTop);
};

scout.Table.prototype.scrollPageDown = function() {
  var newScrollTop = Math.min(this.$data[0].scrollHeight, this.$data[0].scrollTop + this.$data.height());
  this.setScrollTop(newScrollTop);
};

/**
 * @override
 */
scout.Table.prototype.setScrollTop = function(scrollTop) {
  this.setProperty('scrollTop', scrollTop);
  // call _renderViewport to make sure rows are rendered immediately. The browser fires the scroll event handled by onDataScroll delayed
  if (this.rendered) {
    this._renderViewport();
  }
};

/**
 * @override
 */
scout.Table.prototype._renderScrollTop = function() {
  if (this.rendering) {
    // Not necessary to do it while rendering since it will be done by the layout
    return;
  }
  scout.scrollbars.scrollTop(this.$data, this.scrollTop);
};

/**
 * @override
 */
scout.Table.prototype.get$Scrollable = function() {
  return this.$data;
};

scout.Table.prototype.setScrollToSelection = function(scrollToSelection) {
  this.setProperty('scrollToSelection', scrollToSelection);
};

scout.Table.prototype.revealSelection = function() {
  if (!this.rendered) {
    // Execute delayed because table may be not layouted yet
    this.session.layoutValidator.schedulePostValidateFunction(this.revealSelection.bind(this));
    return;
  }

  if (this.selectedRows.length > 0) {
    this.scrollTo(this.selectedRows[0]);
  }
};

scout.Table.prototype.revealChecked = function() {
  var firstCheckedRow = scout.arrays.find(this.rows, function(row) {
    return row.checked === true;
  });
  if (firstCheckedRow) {
    this.scrollTo(firstCheckedRow);
  }
};

scout.Table.prototype._rowById = function(id) {
  return this.rowsMap[id];
};

scout.Table.prototype._rowsByIds = function(ids) {
  return ids.map(this._rowById.bind(this));
};

scout.Table.prototype._rowsToIds = function(rows) {
  return rows.map(function(row) {
    return row.id;
  });
};

/**
 * render borders and selection of row. default select if no argument or false is passed in deselect
 * model has to be updated before calling this method.
 */
scout.Table.prototype._renderSelection = function(rows) {
  rows = scout.arrays.ensure(rows || this.selectedRows);

  // helper function adds/removes a class for a row only if necessary, return true if classes have been changed
  var addOrRemoveClassIfNeededFunc = function($row, condition, classname) {
    var hasClass = $row.hasClass(classname);
    if (condition && !hasClass) {
      $row.addClass(classname);
      return true;
    } else if (!condition && hasClass) {
      $row.removeClass(classname);
      return true;
    }
    return false;
  };

  for (var i = 0; i < rows.length; i++) { // traditional for loop, elements might be added during loop
    var row = rows[i];
    if (!row.$row) {
      continue;
    }

    var thisRowSelected = this.selectedRows.indexOf(row) !== -1,
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
    var classChanged = 0 +
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
};

scout.Table.prototype._removeSelection = function() {
  this.selectedRows.forEach(function(row) {
    if (!row.$row) {
      return;
    }
    row.$row.select(false);
    row.$row.toggleClass(scout.Table.SELECTION_CLASSES, false);
  }, this);
};

scout.Table.prototype.addRowToSelection = function(row, ongoingSelection) {
  if (this.selectedRows.indexOf(row) > -1) {
    return;
  }
  ongoingSelection = ongoingSelection !== undefined ? ongoingSelection : true;
  this.selectedRows.push(row);

  if (row.$row && this.rendered) {
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
};

scout.Table.prototype.removeRowFromSelection = function(row, ongoingSelection) {
  ongoingSelection = ongoingSelection !== undefined ? ongoingSelection : true;
  if (scout.arrays.remove(this.selectedRows, row)) {
    if (this.rendered) {
      this._renderSelection(row);
    }
    if (!ongoingSelection) {
      this._triggerRowsSelected();
    } else {
      this._triggerRowsSelectedPending = true;
    }
  }
};

scout.Table.prototype.selectRow = function(row, debounceSend) {
  this.selectRows(row, debounceSend);
};

scout.Table.prototype.selectRows = function(rows, debounceSend) {
  // Exclude rows that are currently not visible because of a filter (they cannot be selected)
  rows = scout.arrays.ensure(rows).filter(function(row) {
    return !!this.visibleRowsMap[row.id];
  }, this);

  var selectedEqualRows = scout.arrays.equalsIgnoreOrder(rows, this.selectedRows);
  // TODO [7.0] cgu: maybe make sure selectedRows are in correct order, this would make logic in AbstractTableNavigationKeyStroke or renderSelection easier
  // but requires some effort (remember rowIndex, keep array in order after sort, ... see java Table)
  if (selectedEqualRows) {
    return;
  }

  if (this.rendered) {
    this._removeSelection();
  }

  if (!this.multiSelect && rows.length > 1) {
    rows = [rows[0]];
  }

  this.selectedRows = rows; // (Note: direct assignment is safe because the initial filtering created a copy of the original array)
  this._triggerRowsSelected(debounceSend);

  this._updateMenuBar();
  if (this.rendered) {
    this._renderSelection();
    if (this.scrollToSelection) {
      this.revealSelection();
    }
  }
};

scout.Table.prototype.deselectRow = function(row) {
  this.deselectRows(row);
};

scout.Table.prototype.deselectRows = function(rows) {
  rows = scout.arrays.ensure(rows);
  var selectedRows = this.selectedRows.slice(); // copy
  if (scout.arrays.removeAll(selectedRows, rows)) {
    this.selectRows(selectedRows);
  }
};

scout.Table.prototype.isRowSelected = function(row) {
  return this.selectedRows.indexOf(row) > -1;
};

scout.Table.prototype._filterCount = function() {
  return Object.keys(this._filterMap).length;
};

scout.Table.prototype.filteredRows = function() {
  return this._filteredRows;
};

scout.Table.prototype.$rows = function(includeAggrRows) {
  var selector = '.table-row';
  if (includeAggrRows) {
    selector += ', .table-aggregate-row';
  }
  return this.$data.find(selector);
};

scout.Table.prototype.$aggregateRows = function() {
  return this.$data.find('.table-aggregate-row');
};

/**
 * @returns {scout.TableRow} the first selected row of this table or null when no row is selected
 */
scout.Table.prototype.selectedRow = function() {
  if (this.selectedRows.length > 0) {
    return this.selectedRows[0];
  }
  return null;
};

scout.Table.prototype.$selectedRows = function() {
  if (!this.$data) {
    return $();
  }
  return this.$data.find('.selected');
};

scout.Table.prototype.$cellsForColIndex = function(colIndex, includeAggrRows) {
  var selector = '.table-row > div:nth-of-type(' + colIndex + ')';
  if (includeAggrRows) {
    selector += ', .table-aggregate-row > div:nth-of-type(' + colIndex + ')';
  }
  return this.$data.find(selector);
};

scout.Table.prototype.$cellsForColIndexWidthFix = function(colIndex, includeAggrRows) {
  var selector = '.table-row > div:nth-of-type(' + colIndex + ') > .width-fix ';
  if (includeAggrRows) {
    selector += ', .table-aggregate-row > div:nth-of-type(' + colIndex + ') > .width-fix';
  }
  return this.$data.find(selector);
};

scout.Table.prototype.$cellsForRow = function($row) {
  return $row.children('.table-cell');
};

scout.Table.prototype.$cell = function(column, $row) {
  var columnIndex = column;
  if (typeof column !== 'number') {
    columnIndex = this.visibleColumns().indexOf(column);
  }
  return $row.children('.table-cell').eq(columnIndex);
};

scout.Table.prototype.columnById = function(columnId) {
  return scout.arrays.find(this.columns, function(column) {
    return column.id === columnId;
  });
};

/**
 * @param {$} $cell the $cell to get the column for
 * @param {$} [$row] the $row which contains the $cell. If not passed it will be determined automatically
 * @returns {scout.Column} the column for the given $cell
 */
scout.Table.prototype.columnFor$Cell = function($cell, $row) {
  $row = $row || $cell.closest('.table-row');
  var cellIndex = this.$cellsForRow($row).index($cell);
  return this.visibleColumns()[cellIndex];
};

scout.Table.prototype.columnsByIds = function(columnIds) {
  return columnIds.map(this.columnById.bind(this));
};

scout.Table.prototype.getVisibleRows = function() {
  return this.visibleRows;
};

scout.Table.prototype._updateRowStructure = function(options) {
  var updateTree = scout.nvl(options.updateTree, false),
    updateFilteredRows = scout.nvl(options.filteredRows, updateTree),
    applyFilters = scout.nvl(options.applyFilters, updateFilteredRows),
    updateVisibleRows = scout.nvl(options.visibleRows, updateFilteredRows);
  if (updateTree) {
    this._rebuildTreeStructure();
  }
  if (updateFilteredRows) {
    this._updateFilteredRows(applyFilters);
  }
  if (updateVisibleRows) {
    this._updateVisibleRows();
  }
};

scout.Table.prototype._rebuildTreeStructure = function() {
  var hierarchical = false;
  this.rows.forEach(function(row) {
    row.childRows = [];
    hierarchical = hierarchical || !scout.objects.isNullOrUndefined(row.parentRow);
  }, this);
  if (!hierarchical) {
    this.rootRows = this.rows;
    this.hierarchical = hierarchical;
    return;
  }

  this.hierarchical = hierarchical;
  this.rootRows = [];
  this.rows.forEach(function(row) {
    var parentRow;
    if (scout.objects.isNullOrUndefined(row.parentRow)) {
      // root row
      row.parentRow = null;
      row._parentRowId = null;
      this.rootRows.push(row);
      return;
    }
    if (!scout.objects.isNullOrUndefined(row.parentRow.id)) {
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
  this.visitRows(function(row, level) {
    row._hierarchyLevel = level;
    this._maxLevel = Math.max(level, this._maxLevel);
    this.rows.push(row);
  }.bind(this));

  this._calculateTableNodeColumn();
};

scout.Table.prototype._updateFilteredRows = function(applyFilters, changed) {
  changed = !!changed;
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
};

scout.Table.prototype._updateVisibleRows = function() {
  this.visibleRows = this._computeVisibleRows();
  // rebuild the rows by id map of visible rows
  this.visibleRowsMap = this.visibleRows.reduce(function(map, row) {
    map[row.id] = row;
    return map;
  }, {});

  if (this.initialized) {
    // deselect not visible rows
    this.deselectRows(this.selectedRows.filter(function(selectedRow) {
      return !this.visibleRowsMap[selectedRow.id];
    }, this));
  }
};

scout.Table.prototype._computeVisibleRows = function(rows) {
  var visibleRows = [];
  rows = rows || this.rootRows;
  rows.forEach(function(row) {
    var visibleChildRows = this._computeVisibleRows(row.childRows);
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
};

scout.Table.prototype.visibleChildRows = function(row) {
  return row.childRows.filter(function(child) {
    return !!this.visibleRowsMap[child.id];
  }, this);
};

scout.Table.prototype._renderRowDelta = function() {
  if (!this.rendered) {
    return;
  }
  var renderedRows = [];
  this.$rows().each(function(i, elem) {
    var $row = $(elem),
      row = $row.data('row');
    if (this.visibleRows.indexOf(row) < 0) {
      // remove animated
      this._hideRow(row);
    } else {
      renderedRows.push(row);
    }
  }.bind(this));

  this._rerenderViewport();
  // Rows removed by an animation are still there, new rows were appended -> reset correct row order
  this._order$Rows().insertAfter(this.$fillBefore);
  // Also make sure aggregate rows are at the correct position (_renderAggregateRows does nothing because they are already rendered)
  this._order$AggregateRows();

  this.$rows().each(function(i, elem) {
    var $row = $(elem),
      row = $row.data('row');
    if ($row.hasClass('hiding')) {
      // Do not remove rows which are removed using an animation
      // row.$row may already point to a new row -> don't call removeRow to not accidentally remove the new row
      return;
    }
    if (renderedRows.indexOf(row) < 0) {
      this._showRow(row);
    }
  }.bind(this));
  this._renderScrollTop();
  this._renderEmptyData();
};

scout.Table.prototype.filter = function() {
  this._updateRowStructure({
    filteredRows: true
  });
  this._renderRowDelta();
  this._group();
  this.revealSelection();
};

/**
 * Sorts the given $rows according to the row index
 */
scout.Table.prototype._order$Rows = function($rows) {
  // Find rows using jquery because
  // this.filteredRows() may be empty but there may be $rows which are getting removed by animation
  $rows = $rows || this.$rows();
  return $rows.sort(function(elem1, elem2) {
    var $row1 = $(elem1),
      $row2 = $(elem2),
      row1 = $row1.data('row'),
      row2 = $row2.data('row');

    return this.rows.indexOf(row1) - this.rows.indexOf(row2);
  }.bind(this));
};

scout.Table.prototype._order$AggregateRows = function($rows) {
  // Find aggregate rows using jquery because
  // this._aggregateRows may be empty but there may be $aggregateRows which are getting removed by animation
  $rows = $rows || this.$aggregateRows();
  $rows.each(function(i, elem) {
    var $aggrRow = $(elem),
      aggregateRow = $aggrRow.data('aggregateRow');
    if (!aggregateRow || !aggregateRow.prevRow) {
      return;
    }
    $aggrRow.insertAfter(aggregateRow.prevRow.$row);
  });
};

scout.Table.prototype._rowAcceptedByFilters = function(row) {
  for (var key in this._filterMap) { // NOSONAR
    var filter = this._filterMap[key];
    if (!filter.accept(row)) {
      return false;
    }
  }
  return true;
};

/**
 * @returns {Boolean} true if row state has changed, false if not
 */
scout.Table.prototype._applyFiltersForRow = function(row) {
  if (this._rowAcceptedByFilters(row)) {
    if (!row.filterAccepted) {
      row.filterAccepted = true;
      return true;
    }
  } else {
    if (row.filterAccepted) {
      row.filterAccepted = false;
      return true;
    }
  }
  return false;
};

/**
 * @returns {String[]} labels of the currently active TableUserFilters
 */
scout.Table.prototype.filteredBy = function() {
  var filteredBy = [];
  for (var key in this._filterMap) { // NOSONAR
    var filter = this._filterMap[key];
    if (filter instanceof scout.TableUserFilter) {
      filteredBy.push(filter.createLabel());
    }
  }
  return filteredBy;
};

scout.Table.prototype.resetUserFilter = function() {
  var filter;
  for (var key in this._filterMap) { // NOSONAR
    filter = this._filterMap[key];
    if (filter instanceof scout.TableUserFilter) {
      this.removeFilterByKey(key);
    }
  }

  // reset rows
  this.filter();
  this._triggerFilterReset();
};

scout.Table.prototype.resizeToFit = function(column, maxWidth) {
  if (column.fixedWidth) {
    return;
  }
  var returnValue = column.calculateOptimalWidth();
  if (scout.objects.isPlainObject(returnValue)) {
    // Function returned a promise -> delay resizing
    returnValue.always(this._resizeToFit.bind(this, column, maxWidth));
  } else {
    this._resizeToFit(column, maxWidth, returnValue);
  }
};

scout.Table.prototype._resizeToFit = function(column, maxWidth, calculatedSize) {
  if (calculatedSize === -1) {
    // Calculation has been aborted -> don't resize
    return;
  }
  if (maxWidth && maxWidth > 0 && calculatedSize > maxWidth) {
    calculatedSize = maxWidth;
  }
  if (scout.device.isInternetExplorer() && calculatedSize !== column.minWidth) {
    calculatedSize++;
  }
  if (column.width !== calculatedSize) {
    this.resizeColumn(column, calculatedSize);
  }
  column.autoOptimizeWidthRequired = false;
};

/**
 * @param filter object with createKey() and accept()
 */
scout.Table.prototype.addFilter = function(filter) {
  var key = filter.createKey();
  if (!key) {
    throw new Error('key has to be defined');
  }
  this._filterMap[key] = filter;

  this.trigger('filterAdded', {
    filter: filter
  });
};

scout.Table.prototype.removeFilter = function(filter) {
  this.removeFilterByKey(filter.createKey());
};

scout.Table.prototype.removeFilterByKey = function(key) {
  if (!key) {
    throw new Error('key has to be defined');
  }
  var filter = this._filterMap[key];
  if (!filter) {
    return;
  }
  delete this._filterMap[key];
  this.trigger('filterRemoved', {
    filter: filter
  });
};

scout.Table.prototype.getFilter = function(key) {
  if (!key) {
    throw new Error('key has to be defined');
  }
  return this._filterMap[key];
};

/**
 * Resizes the given column to the new size.
 *
 * @param column
 *          column to resize
 * @param width
 *          new column size
 */
scout.Table.prototype.resizeColumn = function(column, width) {
  if (column.fixedWidth) {
    return;
  }
  width = Math.floor(width);
  column.width = width;

  var visibleColumnIndex = this.visibleColumns().indexOf(column);
  if (visibleColumnIndex !== -1) {
    var colNum = visibleColumnIndex + 1;
    this.$cellsForColIndex(colNum, true)
      .css('min-width', width)
      .css('max-width', width);
    if (scout.device.tableAdditionalDivRequired) {
      this.$cellsForColIndexWidthFix(colNum, true)
        .css('max-width', (width - this.cellHorizontalPadding - 2 /* unknown IE9 extra space */ ));
      // same calculation in scout.Column.prototype.buildCellForRow;
    }

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

  this._triggerColumnResized(column);
};

scout.Table.prototype.moveColumn = function(column, visibleOldPos, visibleNewPos, dragged) {
  // translate position of 'visible columns' array to position in 'all columns' array
  var visibleColumns = this.visibleColumns(),
    newColumn = visibleColumns[visibleNewPos],
    newPos = this.columns.indexOf(newColumn);

  // Don't allow moving a column before the last column with a fixed position (checkbox col, row icon col ...)
  this.columns.forEach(function(iteratingColumn, i) {
    if (iteratingColumn.fixedPosition && newPos <= i) {
      newPos = i + 1;
    }
  });

  scout.arrays.remove(this.columns, column);
  scout.arrays.insert(this.columns, column, newPos);

  visibleColumns = this.visibleColumns();
  visibleNewPos = visibleColumns.indexOf(column); // we must re-evaluate visible columns
  this._calculateTableNodeColumn();

  this._triggerColumnMoved(column, visibleOldPos, visibleNewPos, dragged);

  // move aggregated rows
  this._aggregateRows.forEach(function(aggregateRow) {
    scout.arrays.move(aggregateRow.contents, visibleOldPos, visibleNewPos);
  });

  // move cells
  if (this.rendered) {
    this._rerenderViewport();
  }
};

scout.Table.prototype._renderColumnOrderChanges = function(oldColumnOrder) {
  var column, i, j, $orderedCells, $cell, $cells, that = this,
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

      //Find $cell for given column
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
};

scout.Table.prototype._triggerRowsInserted = function(rows) {
  this.trigger('rowsInserted', {
    rows: rows
  });
};

scout.Table.prototype._triggerRowsDeleted = function(rows) {
  this.trigger('rowsDeleted', {
    rows: rows
  });
};

scout.Table.prototype._triggerRowsUpdated = function(rows) {
  this.trigger('rowsUpdated', {
    rows: rows
  });
};

scout.Table.prototype._triggerAllRowsDeleted = function(rows) {
  this.trigger('allRowsDeleted', {
    rows: rows
  });
};

scout.Table.prototype._triggerRowsSelected = function(debounce) {
  this.trigger('rowsSelected', {
    debounce: debounce
  });
};

scout.Table.prototype._triggerRowsChecked = function(rows) {
  this.trigger('rowsChecked', {
    rows: rows
  });
};

scout.Table.prototype._triggerRowsExpanded = function(rows) {
  this.trigger('rowsExpanded', {
    rows: rows
  });
};

scout.Table.prototype._triggerFilter = function() {
  this.trigger('filter');
};

scout.Table.prototype._triggerFilterReset = function() {
  this.trigger('filterReset');
};

scout.Table.prototype._triggerAppLinkAction = function(column, ref) {
  this.trigger('appLinkAction', {
    column: column,
    ref: ref
  });
};

scout.Table.prototype._triggerReload = function() {
  this.trigger('reload');
};

scout.Table.prototype._triggerClipboardExport = function() {
  var event = new scout.Event();
  this.trigger('clipboardExport', event);
  if (!event.defaultPrevented) {
    this._exportToClipboard();
  }
};

scout.Table.prototype._triggerRowOrderChanged = function(row, animating) {
  var event = {
    row: row,
    animating: animating
  };
  this.trigger('rowOrderChanged', event);
};

scout.Table.prototype._triggerColumnResized = function(column) {
  var event = {
    column: column
  };
  this.trigger('columnResized', event);
};

scout.Table.prototype._triggerColumnMoved = function(column, oldPos, newPos, dragged) {
  var event = {
    column: column,
    oldPos: oldPos,
    newPos: newPos,
    dragged: dragged
  };
  this.trigger('columnMoved', event);
};

scout.Table.prototype._triggerAggregationFunctionChanged = function(column) {
  var event = {
    column: column
  };
  this.trigger('aggregationFunctionChanged', event);
};

scout.Table.prototype.setHeaderVisible = function(visible) {
  this.setProperty('headerVisible', visible);
};

scout.Table.prototype._renderHeaderVisible = function() {
  this._renderTableHeader();
};

scout.Table.prototype.setHeaderEnabled = function(headerEnabled) {
  this.setProperty('headerEnabled', headerEnabled);
};

scout.Table.prototype._renderHeaderEnabled = function() {
  // Rebuild the table header when this property changes
  this._removeTableHeader();
  this._renderTableHeader();
};

scout.Table.prototype.setHeaderMenusEnabled = function(headerMenusEnabled) {
  this.setProperty('headerMenusEnabled', headerMenusEnabled);
  if (this.header) {
    this.header.setHeaderMenusEnabled(this.headerMenusEnabled);
  }
};

scout.Table.prototype.hasPermanentHeadOrTailSortColumns = function() {
  return this._permanentHeadSortColumns.length !== 0 || this._permanentTailSortColumns.length !== 0;
};

scout.Table.prototype._setHeadAndTailSortColumns = function() {
  // find all sort columns (head and tail sort columns should always be included)
  var sortColumns = this.columns.filter(function(c) {
    return c.sortIndex >= 0;
  });
  sortColumns.sort(function(a, b) {
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
};

scout.Table.prototype.setRowIconVisible = function(rowIconVisible) {
  this.setProperty('rowIconVisible', rowIconVisible);
};

scout.Table.prototype._setRowIconVisible = function(rowIconVisible) {
  this._setProperty('rowIconVisible', rowIconVisible);
  var column = this.rowIconColumn;
  if (this.rowIconVisible && !column) {
    this._insertRowIconColumn();
    this._calculateTableNodeColumn();
    this.trigger('columnStructureChanged');
  } else if (!this.rowIconVisible && column) {
    scout.arrays.remove(this.columns, column);
    this.rowIconColumn = null;
    this._calculateTableNodeColumn();
    this.trigger('columnStructureChanged');
  }
};

scout.Table.prototype.setRowIconColumnWidth = function(width) {
  this.setProperty('rowIconColumnWidth', width);
};

scout.Table.prototype._setRowIconColumnWidth = function(width) {
  this._setProperty('rowIconColumnWidth', width);
  var column = this.rowIconColumn;
  if (column) {
    column.width = width;
  }
};

scout.Table.prototype._setSelectedRows = function(selectedRows) {
  if (typeof selectedRows[0] === 'string') {
    selectedRows = this._rowsByIds(selectedRows);
  }
  this._setProperty('selectedRows', selectedRows);
};

scout.Table.prototype.setMenus = function(menus) {
  this.setProperty('menus', menus);
};

scout.Table.prototype._setMenus = function(menus, oldMenus) {
  this.updateKeyStrokes(menus, oldMenus);
  this._setProperty('menus', menus);
  this._updateMenuBar();

  if (this.header) {
    this.header.updateMenuBar();
  }
};

scout.Table.prototype._updateMenuBar = function() {
  var notAllowedTypes = ['Header'];
  var menuItems = this._filterMenus(this.menus, scout.MenuDestinations.MENU_BAR, false, true, notAllowedTypes);
  menuItems = this.staticMenus.concat(menuItems);
  this.menuBar.setMenuItems(menuItems);
  if (this.contextMenu) {
    var contextMenuItems = this._filterMenus(this.menus, scout.MenuDestinations.CONTEXT_MENU, true, false, ['Header']);
    this.contextMenu.updateMenuItems(contextMenuItems);
  }
};

scout.Table.prototype._setKeyStrokes = function(keyStrokes) {
  this.updateKeyStrokes(keyStrokes, this.keyStrokes);
  this._setProperty('keyStrokes', keyStrokes);
};

scout.Table.prototype.setFilters = function(filters) {
  var filter;
  for (var key in this._filterMap) { // NOSONAR
    filter = this._filterMap[key];
    if (filter instanceof scout.TableUserFilter) {
      this.removeFilterByKey(key);
    }
  }
  if (filters) {
    filters.forEach(function(filter) {
      filter = this._ensureFilter(filter);
      this.addFilter(filter);
    }, this);
  }
};

scout.Table.prototype._ensureFilter = function(filter) {
  if (filter instanceof scout.TableUserFilter) {
    return filter;
  }
  if (filter.column) {
    filter.column = this.columnById(filter.column);
  }
  filter.table = this;
  filter.session = this.session;
  return scout.create(filter);
};

scout.Table.prototype.setTableStatus = function(status) {
  this.setProperty('tableStatus', status);
};

scout.Table.prototype._setTableStatus = function(status) {
  status = scout.Status.ensure(status);
  this._setProperty('tableStatus', status);
};

scout.Table.prototype.setTableStatusVisible = function(visible) {
  this.setProperty('tableStatusVisible', visible);
  this._updateFooterVisibility();
};

scout.Table.prototype._updateFooterVisibility = function() {
  this.setFooterVisible(this.tableStatusVisible || this._hasVisibleTableControls());
};

scout.Table.prototype.setHierarchicalStyle = function(style) {
  this.setProperty('hierarchicalStyle', style);
};

scout.Table.prototype._renderHierarchicalStyle = function() {
  this.$container.toggleClass('structured', scout.Table.HierarchicalStyle.STRUCTURED === this.hierarchicalStyle);
};

scout.Table.prototype.setFooterVisible = function(visible) {
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
};

/**
 * Renders the background effect of every column, if column.backgroundEffect is set
 */
scout.Table.prototype._renderBackgroundEffect = function() {
  this.columns.forEach(function(column) {
    if (!column.backgroundEffect) {
      return;
    }
    column._renderBackgroundEffect();
  }, this);
};

scout.Table.prototype._renderRowChecked = function(row) {
  if (!this.checkable) {
    return;
  }
  if (!row.$row) {
    return;
  }
  var $styleElem;
  if (this.checkableStyle === scout.Table.CheckableStyle.TABLE_ROW) {
    $styleElem = row.$row;
  } else {
    if (!this.checkableColumn) {
      throw new Error('checkableColumn not set');
    }
    $styleElem = this.checkableColumn.$checkBox(row.$row);
  }
  $styleElem.toggleClass('checked', row.checked);
};

scout.Table.prototype.setCheckable = function(checkable) {
  this.setProperty('checkable', checkable);
};

scout.Table.prototype._setCheckable = function(checkable) {
  this._setProperty('checkable', checkable);
  this._updateCheckableColumn();
};

scout.Table.prototype._updateCheckableColumn = function() {
  var column = this.checkableColumn;
  var showCheckBoxes = this.checkable && this.checkableStyle === scout.Table.CheckableStyle.CHECKBOX;
  if (showCheckBoxes && !column) {
    this._insertBooleanColumn();
    this._calculateTableNodeColumn();
    this.trigger('columnStructureChanged');
  } else if (!showCheckBoxes && column && column.guiOnly) {
    scout.arrays.remove(this.columns, column);
    this.checkableColumn = null;
    this._calculateTableNodeColumn();
    this.trigger('columnStructureChanged');
  }
};

scout.Table.prototype._renderCheckable = function() {
  this.columnLayoutDirty = true;
  this._updateRowWidth();
  this._redraw();
  this.invalidateLayoutTree();
};

scout.Table.prototype.setCheckableStyle = function(checkableStyle) {
  this.setProperty('checkableStyle', checkableStyle);
};

scout.Table.prototype._setCheckableStyle = function(checkableStyle) {
  this._setProperty('checkableStyle', checkableStyle);
  this._updateCheckableColumn();
};

scout.Table.prototype._renderCheckableStyle = function() {
  this.$container.toggleClass('checkable', this.checkableStyle === scout.Table.CheckableStyle.TABLE_ROW);
  if (this.rendered) {
    this._redraw();
  }
};

scout.Table.prototype._renderRowIconVisible = function() {
  this.columnLayoutDirty = true;
  this._updateRowWidth();
  this._redraw();
  this.invalidateLayoutTree();
};

scout.Table.prototype._renderRowIconColumnWidth = function() {
  if (!this.rowIconVisible) {
    return;
  }
  this._renderRowIconVisible();
};

scout.Table.prototype.setGroupingStyle = function(groupingStyle) {
  this.setProperty('groupingStyle', groupingStyle);
};

scout.Table.prototype._setGroupingStyle = function(groupingStyle) {
  this._setProperty('groupingStyle', groupingStyle);
  this._group();
};

scout.Table.prototype._renderGroupingStyle = function() {
  this._rerenderViewport();
};

scout.Table.prototype._redraw = function() {
  this._rerenderHeaderColumns();
  this._rerenderViewport();
};

scout.Table.prototype._rerenderHeaderColumns = function() {
  if (this.header) {
    this.header.rerenderColumns();
    this.invalidateLayoutTree();
  }
};

scout.Table.prototype._renderTableHeader = function() {
  var changed = false;
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
};

scout.Table.prototype._removeTableHeader = function() {
  if (this.header) {
    this.header.destroy();
    this.header = null;
  }
};

/**
 * @param width optional width of emptyData, if omitted the width is set to the header's scrollWidth.
 */
scout.Table.prototype._renderEmptyData = function() {
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
};

scout.Table.prototype._removeEmptyData = function() {
  if (this.header && this.visibleRows.length === 0) {
    return;
  }
  if (this.$emptyData) {
    this.$emptyData.remove();
    this.$emptyData = null;
    this.updateScrollbars();
  }
};

scout.Table.prototype._renderFooterVisible = function() {
  if (!this.footer) {
    return;
  }
  if (this.footerVisible) {
    this._renderFooter();
  } else {
    this._removeFooter();
  }
  this.invalidateLayoutTree();
};

scout.Table.prototype._renderFooter = function() {
  if (this.footer.rendered) {
    return;
  }

  this.footer.render();
};

scout.Table.prototype._removeFooter = function() {
  if (!this.footer.rendered) {
    return;
  }
  this.footer.remove();
};

/**
 * @override Widget.js
 */
scout.Table.prototype._renderEnabled = function() {
  scout.Table.parent.prototype._renderEnabled.call(this);

  var enabled = this.enabled;
  this.$data.setEnabled(enabled);
  this.$container.setTabbable(enabled);

  if (this.rendered) {
    // Enable/disable all checkboxes
    this.$rows().each(function() {
      var $row = $(this),
        row = $row.data('row');
      $row.find('input').setEnabled(enabled && row.enabled);
    });
  }
};

/**
 * @override Widget.js
 */
scout.Table.prototype._renderDisabledStyle = function() {
  scout.Table.parent.prototype._renderDisabledStyle.call(this);
  this._renderDisabledStyleInternal(this.$data);
};

scout.Table.prototype.setAutoResizeColumns = function(autoResizeColumns) {
  this.setProperty('autoResizeColumns', autoResizeColumns);
};

scout.Table.prototype._renderAutoResizeColumns = function() {
  if (this.autoResizeColumns) {
    this.columnLayoutDirty = true;
    this.invalidateLayoutTree();
  }
};

scout.Table.prototype.setMultilineText = function(multilineText) {
  this.setProperty('multilineText', multilineText);
};

scout.Table.prototype._renderMultilineText = function() {
  this._markAutoOptimizeWidthColumnsAsDirty();
  this._redraw();
  this.invalidateLayoutTree();
};

scout.Table.prototype._renderDropType = function() {
  if (this.dropType) {
    this._installDragAndDropHandler();
  } else {
    this._uninstallDragAndDropHandler();
  }
};

scout.Table.prototype._installDragAndDropHandler = function(event) {
  if (this.dragAndDropHandler) {
    return;
  }
  this.dragAndDropHandler = scout.dragAndDrop.handler(this, {
    supportedScoutTypes: scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER,
    dropType: function() {
      return this.dropType;
    }.bind(this),
    dropMaximumSize: function() {
      return this.dropMaximumSize;
    }.bind(this),
    additionalDropProperties: function(event) {
      var $target = $(event.currentTarget);
      var properties = {
        rowId: ''
      };
      if ($target.hasClass('table-row')) {
        var row = $target.data('row');
        properties.rowId = row.id;
      }
      return properties;
    }.bind(this)
  });
  this.dragAndDropHandler.install(this.$container, '.table-data,.table-row');
};

scout.Table.prototype._uninstallDragAndDropHandler = function(event) {
  if (!this.dragAndDropHandler) {
    return;
  }
  this.dragAndDropHandler.uninstall();
  this.dragAndDropHandler = null;
};

/**
 * This listener is used to invalidate table layout when an image icon has been loaded (which happens async in the browser).
 */
scout.Table.prototype._installImageListeners = function() {
  this._imageLoadListener = this._onImageLoadOrError.bind(this);
  // Image events don't bubble -> use capture phase instead
  this.$data[0].addEventListener('load', this._imageLoadListener, true);
  this.$data[0].addEventListener('error', this._imageLoadListener, true);
};

scout.Table.prototype._uninstallImageListeners = function() {
  this.$data[0].removeEventListener('load', this._imageLoadListener, true);
  this.$data[0].removeEventListener('error', this._imageLoadListener, true);
};

/**
 * Calculates the optimal view range size (number of rows to be rendered).
 * It uses the default row height to estimate how many rows fit in the view port.
 * The view range size is this value * 2.
 */
scout.Table.prototype.calculateViewRangeSize = function() {
  // Make sure row height is up to date (row height may be different after zooming)
  this._updateRowHeight();

  if (this.rowHeight === 0) {
    throw new Error('Cannot calculate view range with rowHeight = 0');
  }
  return Math.ceil(this.$data.outerHeight() / this.rowHeight) * 2;
};

scout.Table.prototype.setViewRangeSize = function(viewRangeSize) {
  if (this.viewRangeSize === viewRangeSize) {
    return;
  }
  this._setProperty('viewRangeSize', viewRangeSize);
  if (this.rendered) {
    this._renderViewport();
  }
};

scout.Table.prototype._calculateCurrentViewRange = function() {
  var rowIndex,
    scrollTop = this.$data[0].scrollTop,
    maxScrollTop = this.$data[0].scrollHeight - this.$data[0].clientHeight;

  if (maxScrollTop === 0) {
    // no scrollbars visible
    rowIndex = 0;
  } else {
    rowIndex = this._rowIndexAtScrollTop(scrollTop);
  }

  return this._calculateViewRangeForRowIndex(rowIndex);
};

/**
 * Returns the index of the row which is at position scrollTop.
 */
scout.Table.prototype._rowIndexAtScrollTop = function(scrollTop) {
  var height = 0,
    index = -1;
  this.visibleRows.some(function(row, i) {
    height += this._heightForRow(row);
    if (scrollTop < height) {
      index = i;
      return true;
    }
  }.bind(this));
  return index;
};

scout.Table.prototype._heightForRow = function(row) {
  var height = 0,
    aggregateRow = row.aggregateRowAfter;

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
};

/**
 * Returns a range of size this.viewRangeSize. Start of range is rowIndex - viewRangeSize / 4.
 * -> 1/4 of the rows are before the viewport 2/4 in the viewport 1/4 after the viewport,
 * assuming viewRangeSize is 2*number of possible rows in the viewport (see calculateViewRangeSize).
 */
scout.Table.prototype._calculateViewRangeForRowIndex = function(rowIndex) {
  // regular / non-virtual scrolling? -> all rows are already rendered in the DOM
  if (!this.virtual) {
    return new scout.Range(0, this.visibleRows.length);
  }

  var viewRange = new scout.Range(),
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
};

/**
 * Calculates and renders the rows which should be visible in the current viewport based on scroll top.
 */
scout.Table.prototype._renderViewport = function() {
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
  var viewRange = this._calculateCurrentViewRange();
  this._renderViewRange(viewRange);
};

scout.Table.prototype._rerenderViewport = function() {
  if (!this.isAttachedAndRendered()) {
    // if table is not attached the correct viewPort can not be evaluated. Mark for rerender after attach.
    this._rerenderViewPortAfterAttach = true;
    return;
  }
  this._removeRows();
  this._removeAggregateRows();
  this._renderFiller();
  this._renderViewport();
};

scout.Table.prototype._renderViewRangeForRowIndex = function(rowIndex) {
  var viewRange = this._calculateViewRangeForRowIndex(rowIndex);
  this._renderViewRange(viewRange);
};

/**
 * Renders the rows visible in the viewport and removes the other rows
 */
scout.Table.prototype._renderViewRange = function(viewRange) {
  if (viewRange.from === this.viewRangeRendered.from && viewRange.to === this.viewRangeRendered.to && !this.viewRangeDirty) {
    // Range already rendered -> do nothing
    return;
  }
  this._removeRangeMarkers();
  var rangesToRender = viewRange.subtract(this.viewRangeRendered);
  var rangesToRemove = this.viewRangeRendered.subtract(viewRange);
  rangesToRemove.forEach(function(range) {
    this._removeRowsInRange(range);
  }.bind(this));
  rangesToRender.forEach(function(range) {
    this._renderRowsInRange(range);
  }.bind(this));

  // check if at least last and first row in range got correctly rendered
  if (this.viewRangeRendered.size() > 0) {
    var rows = this.visibleRows;
    var firstRow = rows[this.viewRangeRendered.from];
    var lastRow = rows[this.viewRangeRendered.to - 1];
    if (!firstRow.$row || !lastRow.$row) {
      throw new Error('Rows not rendered as expected. ' + this.viewRangeRendered + '. First: ' + firstRow.$row + '. Last: ' + lastRow.$row);
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
};

scout.Table.prototype._removeRangeMarkers = function() {
  this._modifyRangeMarkers('removeClass');
};

scout.Table.prototype._renderRangeMarkers = function() {
  this._modifyRangeMarkers('addClass');
};

scout.Table.prototype._modifyRangeMarkers = function(funcName) {
  if (this.viewRangeRendered.size() === 0) {
    return;
  }
  var visibleRows = this.visibleRows;
  modifyRangeMarker(visibleRows[this.viewRangeRendered.from], 'first');
  modifyRangeMarker(visibleRows[this.viewRangeRendered.to - 1], 'last');

  function modifyRangeMarker(row, cssClass) {
    if (row && row.$row) {
      row.$row[funcName](cssClass);
    }
  }
};

scout.Table.prototype.ensureRowRendered = function(row) {
  if (!row.$row) {
    var rowIndex = this.visibleRows.indexOf(row);
    this._renderViewRangeForRowIndex(rowIndex);
  }
};

scout.Table.prototype._renderFiller = function() {
  if (!this.$fillBefore) {
    this.$fillBefore = this.$data.prependDiv('table-data-fill');
    this._applyFillerStyle(this.$fillBefore);
  }

  var fillBeforeHeight = this._calculateFillerHeight(new scout.Range(0, this.viewRangeRendered.from));
  this.$fillBefore.cssHeight(fillBeforeHeight);
  this.$fillBefore.cssWidth(this.rowWidth);
  $.log.isTraceEnabled() && $.log.trace('FillBefore height: ' + fillBeforeHeight);

  if (!this.$fillAfter) {
    this.$fillAfter = this.$data.appendDiv('table-data-fill');
    this._applyFillerStyle(this.$fillAfter);
  }

  var fillAfterHeight = this._calculateFillerHeight(new scout.Range(this.viewRangeRendered.to, this.visibleRows.length));
  this.$fillAfter.cssHeight(fillAfterHeight);
  this.$fillAfter.cssWidth(this.rowWidth);
  $.log.isTraceEnabled() && $.log.trace('FillAfter height: ' + fillAfterHeight);
};

scout.Table.prototype._applyFillerStyle = function($filler) {
  var lineColor = $filler.css('background-color');
  // In order to get a 1px border we need to get the right value in percentage for the linear gradient
  var lineWidth = ((1 - (1 / this.rowHeight)) * 100).toFixed(2) + '%';
  $filler.css({
    background: 'linear-gradient(to bottom, transparent, transparent ' + lineWidth + ', ' + lineColor + ' ' + lineWidth + ', ' + lineColor + ')',
    backgroundSize: '100% ' + this.rowHeight + 'px',
    backgroundColor: 'transparent'
  });
};

scout.Table.prototype._calculateFillerHeight = function(range) {
  var totalHeight = 0;
  for (var i = range.from; i < range.to; i++) {
    var row = this.visibleRows[i];
    totalHeight += this._heightForRow(row);
  }
  return totalHeight;
};

scout.Table.prototype.containsAggregatedNumberColumn = function() {
  if (!this.initialized) {
    return false;
  }
  return this.visibleColumns().some(function(column) {
    return column instanceof scout.NumberColumn && column.aggregationFunction !== 'none';
  });
};

/**
 * Rebuilds the header.<br>
 * Does not modify the rows, it expects a deleteAll and insert operation to follow which will do the job.
 */
scout.Table.prototype.updateColumnStructure = function(columns) {
  this._destroyColumns();
  this.columns = columns;
  this._initColumns();

  if (this.rendered) {
    this._updateRowWidth();
    this.$rows(true).css('width', this.rowWidth);
    this._rerenderHeaderColumns();
    this._renderEmptyData();
  }
  this.trigger('columnStructureChanged');
};

scout.Table.prototype.updateColumnOrder = function(columns) {
  var i, column, currentPosition, oldColumnOrder;
  if (columns.length !== this.columns.length) {
    throw new Error('Column order may not be updated because lengths of the arrays differ.');
  }

  oldColumnOrder = this.columns.slice();

  for (i = 0; i < columns.length; i++) {
    column = columns[i];
    currentPosition = this.columns.indexOf(column);
    if (currentPosition < 0) {
      throw new Error('Column with id ' + column.id + 'not found.');
    }

    if (currentPosition !== i) {
      // Update model
      scout.arrays.remove(this.columns, column);
      scout.arrays.insert(this.columns, column, i);
    }
  }

  if (this.rendered) {
    this._renderColumnOrderChanges(oldColumnOrder);
  }
};

/**
 * @param columns array of columns which were updated.
 */
scout.Table.prototype.updateColumnHeaders = function(columns) {
  var column, oldColumnState;

  // Update model columns
  for (var i = 0; i < columns.length; i++) {
    column = this.columnById(columns[i].id);
    oldColumnState = $.extend(oldColumnState, column);
    column.text = columns[i].text;
    column.headerTooltipText = columns[i].headerTooltipText;
    column.headerCssClass = columns[i].headerCssClass;
    column.headerHtmlEnabled = columns[i].headerHtmlEnabled;
    column.headerBackgroundColor = columns[i].headerBackgroundColor;
    column.headerForegroundColor = columns[i].headerForegroundColor;
    column.headerFont = columns[i].headerFont;
    column.headerIconId = columns[i].headerIconId;
    column.sortActive = columns[i].sortActive;
    column.sortAscending = columns[i].sortAscending;
    column.grouped = columns[i].grouped;
    if (!column.sortActive && column.sortIndex !== -1) {
      // Adjust indices of other sort columns (if a sort column in the middle got removed, there won't necessarily be an event for the other columns)
      this._removeSortColumn(column);
    } else if (column.sortActive && column.sortIndex === -1) {
      // Necessary if there is a tail sort column (there won't be an event for the tail sort column if another sort column was added before)
      this._addSortColumn(column);
    } else {
      column.sortIndex = columns[i].sortIndex;
    }

    if (this.rendered && this.header) {
      this.header.updateHeader(column, oldColumnState);
    }
  }
};

scout.Table.prototype.focusCell = function(column, row) {
  if (!this.rendered || !this.isAttachedAndRendered()) {
    this._postRenderActions.push(this.focusCell.bind(this, column, row));
    return;
  }

  var cell = this.cell(column, row);
  if (this.enabled && row.enabled && cell.editable) {
    this.prepareCellEdit(column, row, false);
  }
};

/**
 * Method invoked when this is a 'detailTable' and the outline content is displayed.
 * @override Widget.js
 */
scout.Table.prototype._attach = function() {
  this.$parent.append(this.$container);
  var htmlParent = this.htmlComp.getParent();
  this.htmlComp.setSize(htmlParent.size());
  this.session.detachHelper.afterAttach(this.$container);
  scout.Table.parent.prototype._attach.call(this);
};

/**
 * Method invoked when this is a 'detailTable' and the outline content is not displayed anymore.
 * @override Widget.js
 */
scout.Table.prototype._detach = function() {
  this.session.detachHelper.beforeDetach(this.$container);
  this.$container.detach();
  // Detach helper stores the current scroll pos and restores in attach.
  // To make it work scrollTop needs to be reset here otherwise viewport won't be rendered by _onDataScroll
  scout.Table.parent.prototype._detach.call(this);
};

scout.Table.prototype._destroyCellEditorPopup = function() {
  // When a cell editor popup is open and table is detached, we close the popup immediately
  // and don't wait for the model event 'endCellEdit'. By doing this we can avoid problems
  // with invalid focus contexts.
  if (this.cellEditorPopup) {
    this.cellEditorPopup.destroy();
    this.cellEditorPopup = null;
  }
};

scout.Table.prototype._beforeDetach = function() {
  this._destroyCellEditorPopup();
};
scout.Table.prototype.setVirtual = function(virtual) {
  this._setProperty('virtual', virtual);
};

scout.Table.prototype.setCellValue = function(column, row, value) {
  column.setCellValue(row, value);
};

scout.Table.prototype.visibleColumns = function(includeGuiColumns) {
  includeGuiColumns = scout.nvl(includeGuiColumns, true);
  return this.columns.filter(function(column) {
    return column.isVisible() && (includeGuiColumns || !column.guiOnly);
  }, this);
};

/**
 * @override Widget.js
 */
scout.Table.prototype._afterAttach = function() {
  // this is an "if... else if..." to avoid rendering the viewport multiple
  // times in case all ...afterAttach flags are set to true.
  if (this._rerenderViewPortAfterAttach) {
    this._rerenderViewport();
    this._rerenderViewPortAfterAttach = false;
  } else if (this._renderViewPortAfterAttach) {
    this._renderViewport();
    this._renderViewPortAfterAttach = false;
  }
};

// same as on scout.Tree.prototype._onDesktopPopupOpen
scout.Table.prototype._onDesktopPopupOpen = function(event) {
  var popup = event.popup;
  if (!this.enabled) {
    return;
  }
  // Set table style to focused if a context menu or a menu bar popup opens, so that it looks as it still has the focus
  if (this.has(popup) && popup instanceof scout.ContextMenuPopup) {
    this.$container.addClass('focused');
    popup.one('destroy', function() {
      if (this.rendered) {
        this.$container.removeClass('focused');
      }
    }.bind(this));
  }
};

scout.Table.prototype.markRowsAsNonChanged = function(rows) {
  scout.arrays.ensure(rows || this.rows).forEach(function(row) {
    row.status = scout.TableRow.Status.NON_CHANGED;
  });
};

/* --- STATIC HELPERS ------------------------------------------------------------- */

/**
 * @memberOf scout.Table
 */
scout.Table.parseHorizontalAlignment = function(alignment) {
  if (alignment > 0) {
    return 'right';
  }
  if (alignment === 0) {
    return 'center';
  }
  return 'left';
};

scout.Table.linkRowToDiv = function(row, $row) {
  if (row) {
    row.$row = $row;
  }
  if ($row) {
    $row.data('row', row);
  }
};
