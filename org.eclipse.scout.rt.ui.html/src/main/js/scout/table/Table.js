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
scout.Table = function() {
  scout.Table.parent.call(this);

  this.autoResizeColumns = false;
  this.columnAddable = false;
  this.checkable = false;
  this.dropType = 0;
  this.dropMaximumSize = scout.dragAndDrop.DEFAULT_DROP_MAXIMUM_SIZE;
  this.enabled = true;
  this.headerEnabled = true;
  this.headerVisible = true;
  this.hasReloadHandler = false;
  this.keyStrokes = [];
  this.keyboardNavigation = true;
  this.menus = [];
  this.contextMenu;
  this.multiCheck = true;
  this.multiSelect = true;
  this.multilineText = false;
  this.scrollToSelection = false;
  this.scrollTop = 0;
  this.selectedRows = [];
  this.sortEnabled = true;
  this.tableControls = [];
  this.tableStatusVisible = false;
  this.footerVisible = false;
  this.filters = [];
  this.columns = [];
  this.rows = [];
  this.rowsMap = {}; // rows by id
  this.rowWidth = 0;
  this.rowBorderWidth; // read-only, set by _calculateRowBorderWidth(), also used in TableLayout.js
  this.rowBorderLeftWidth = 0; // read-only, set by _calculateRowBorderWidth(), also used in TableHeader.js
  this.rowBorderRightWidth = 0; // read-only, set by _calculateRowBorderWidth(), also used in TableHeader.js
  this.rowIconVisible = false;
  this.staticMenus = [];
  this.selectionHandler = new scout.TableSelectionHandler(this);
  this.header;
  this.footer;
  this._filterMap = {};
  this._filteredRows = [];
  this._filteredRowsDirty = true;
  this.tooltips = [];
  this._aggregateRows = [];
  this._animationRowLimit = 25;
  this._blockLoadThreshold = 25;
  this.menuBar;
  this._doubleClickSupport = new scout.DoubleClickSupport();
  this.checkableStyle = scout.Table.CheckableStyle.CHECKBOX;
  this.$container;
  this.$data;
  this._addAdapterProperties(['tableControls', 'menus', 'keyStrokes']);

  this._permanentHeadSortColumns = [];
  this._permanentTailSortColumns = [];
  // Initial value must be > 0 to make prefSize work (if it is 0, no filler will be generated).
  // If rows have a variable height, prefSize is only correct for 10 rows.
  // Layout will adjust this value depending on the view port size.
  this.viewRangeSize = 10;
  this.viewRangeRendered = new scout.Range(0, 0);
  this._filterMenusHandler = this._filterMenus.bind(this);
  this.virtual = true;
  this.contextColumn;
  this._rerenderViewPortAfterAttach = false;
  this.groupingStyle = scout.Table.GroupingStyle.BOTTOM;
};
scout.inherits(scout.Table, scout.Widget);

// TODO [7.0] cgu create StringColumn.js incl. defaultValues from defaultValues.json

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
  this._initColumns();

  this.rows.forEach(function(row, i) {
    this.rows[i] = this._initRow(row);
  }, this);

  this.menuBar = scout.create('MenuBar', {
    parent: this,
    menuOrder: new scout.MenuItemsOrder(this.session, 'Table'),
    menuFilter: this._filterMenusHandler
  });
  this.menuBar.bottom();

  this._setSelectedRows(this.selectedRows);
  this.setFilters(this.filters);
  this._setKeyStrokes(this.keyStrokes);
  this._setMenus(this.menus);
  this._setTableControls(this.tableControls);
  this._setTableStatus(this.tableStatus);
  this._applyFilters(this.rows);
  this._calculateValuesForBackgroundEffect();
  this._group();
};

scout.Table.prototype._initRow = function(row) {
  if (!(row instanceof scout.TableRow)) {
    row.parent = this;
    row = scout.create('TableRow', row);
  }
  this.rowsMap[row.id] = row;
  this.trigger('rowInitialized', {
    row: row
  });
  return row;
};

scout.Table.prototype._initColumns = function() {
  var column, i;
  for (i = 0; i < this.columns.length; i++) {
    this.columns[i].session = this.session;
    column = scout.create(this.columns[i]);
    column.table = this;
    this.columns[i] = column;

    if (column.checkable) {
      // set checkable column if this column is the checkable one
      this.checkableColumn = column;
    }
  }

  // Add gui only checkbox column at the beginning
  if (this.rowIconVisible) {
    this._insertRowIconColumn();
  }
  this._setCheckable(this.checkable);

  // Sync head and tail sort columns
  this._setHeadAndTailSortColumns();
};

/**
 * @override
 */
scout.Table.prototype._createLoadingSupport = function() {
  return new scout.LoadingSupport({
    widget: this
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

    new scout.TableFocusFilterFieldKeyStroke(this),
    new scout.TableStartCellEditKeyStroke(this),
    new scout.TableSelectAllKeyStroke(this),
    new scout.TableRefreshKeyStroke(this),
    new scout.TableToggleRowKeyStroke(this),
    new scout.TableCopyKeyStroke(this),
    new scout.ContextMenuKeyStroke(this, this.onContextMenu, this),
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
    disallowHeaderMenu: true,
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
      disallowHeaderMenu: true,
      showSeparator: false,
      width: scout.Column.NARROW_MIN_WIDTH,
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

scout.Table.prototype._render = function($parent) {
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
    .on('scroll', this._onDataScroll.bind(this))
    .on('contextmenu', '.table-row', function(event) {
      event.preventDefault();
      event.stopPropagation();
      return false;
    });
  scout.scrollbars.install(this.$data, {
    parent: this,
    axis: 'both'
  });
  this._installImageListeners();
  this._installCellTooltipSupport();
  this.menuBar.render(this.$container);

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
};

scout.Table.prototype._renderProperties = function() {
  scout.Table.parent.prototype._renderProperties.call(this);
  this._renderTableHeader();
  this._renderFooterVisible();
  this._renderDropType();
  this._renderCheckableStyle();
};

scout.Table.prototype._remove = function() {
  scout.scrollbars.uninstall(this.$data, this.session);
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
  var
    $row = $(event.target).closest('.table-row'),
    row = $row.data('row');
  row.height = $row.outerHeight(true);
  this.invalidateLayoutTree();
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

  if (this.checkableStyle === scout.Table.CheckableStyle.TABLE_ROW) {
    var row = this._$mouseDownRow.data('row');
    this.checkRow(row, !row.checked);
  }
};

scout.Table.prototype._onRowMouseUp = function(event) {
  var $row, $mouseUpRow, column, $appLink, row,
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
  if (mouseButton === 1) {
    column.onMouseUp(event, $row);
    $appLink = this._find$AppLink(event);
  }
  row = $row.data('row');
  if ($appLink) {
    this._triggerAppLinkAction(column, $appLink.data('ref'));
  } else {
    this._triggerRowClicked(row, mouseButton);
  }
};

scout.Table.prototype._onRowDoubleClick = function(event) {
  var $row = $(event.currentTarget),
    column = this._columnAtX(event.pageX);

  this.doRowAction($row.data('row'), column);
};

scout.Table.prototype.onContextMenu = function(event) {
  var func = function(event) {
    event.preventDefault();

    var menuItems;
    if (this.selectedRows.length > 0) {
      menuItems = this._filterMenus(this.menus, scout.MenuDestinations.CONTEXT_MENU, true, false, ['Header']);
      if (!event.pageX && !event.pageY) {
        var $rowToDisplay = this.selectionHandler.lastActionRow ? this.selectionHandler.lastActionRow.$row : this.selectedRows[this.selectedRows.length - 1].$row;
        var offset = $rowToDisplay.offset();
        event.pageX = offset.left + 10;
        event.pageY = offset.top + $rowToDisplay.outerHeight() / 2;
      }
      if (menuItems.length > 0) {
        // Prevent firing of 'onClose'-handler during contextMenu.open()
        // (Can lead to null-access when adding a new handler to this.contextMenu)
        if (this.contextMenu) {
          this.contextMenu.close();
        }
        this.contextMenu = scout.create('ContextMenuPopup', {
          parent: this,
          menuItems: menuItems,
          location: {
            x: event.pageX,
            y: event.pageY
          },
          $anchor: this.$data,
          menuFilter: this._filterMenusHandler
        });
        this.contextMenu.open();

        // Set table style to focused, so that it looks as it still has the focus.
        // Must be called after open(), because opening the popup might cause another
        // popup to close first (which will remove the 'focused' class).
        if (this.enabled) {
          this.$container.addClass('focused');
          this.contextMenu.on('close', function(event) {
            this.$container.removeClass('focused');
            this.contextMenu = null;
          }.bind(this));
        }
      }
    }
  };

  scout.menus.showContextMenuWithWait(this.session, func.bind(this), event);
};

scout.Table.prototype.onColumnVisibilityChanged = function(column) {
  if (this.rendered) {
    this._updateRowWidth();
    this._redraw();
  }
  this.trigger('columnStructureChanged');
};

scout.Table.prototype._onDataScroll = function() {
  var scrollTop = this.$data[0].scrollTop;
  if (this.scrollTop === scrollTop) {
    return;
  }
  this._renderViewport();
  this.scrollTop = scrollTop;
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
    enabled: this.headerEnabled
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
    cellIndex = this.$cellsForRow($row).index($cell),
    column = this.columns[cellIndex],
    row = $row.data('row');

  if (row) {
    cell = this.cell(column, row);
    tooltipText = cell.tooltipText;
  }

  if (tooltipText) {
    return tooltipText;
  } else if (this._isTruncatedCellTooltipEnabled(column) && $cell.isContentTruncated()) {
    return $cell.text();
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

scout.Table.prototype.exportToClipboard = function() {
  this._triggerExportToClipboard();
};

scout.Table.prototype.setMultiSelect = function(multiSelect) {
  this.setProperty('multiSelect', multiSelect);
};

scout.Table.prototype.toggleSelection = function() {
  if (this.selectedRows.length === this.filteredRows().length) {
    this.deselectAll();
  } else {
    this.selectAll();
  }
};

scout.Table.prototype.selectAll = function() {
  this.selectRows(this.filteredRows());
};

scout.Table.prototype.deselectAll = function() {
  this.selectRows([]);
};

scout.Table.prototype.checkAll = function(checked) {
  this.checkRows(this.filteredRows(), {
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
  if (!this._isUiSortPossible(sortColumns)) {
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
  this._filteredRowsDirty = true; // order has been changed
  this._triggerRowOrderChanged();
  if (this.rendered) {
    this._renderRowOrderChanges();
  }

  // Do it after row order has been rendered, because renderRowOrderChanges rerenders the whole viewport which would destroy the animation
  this._group(animateAggregateRows);

  // Sort was possible -> return true
  return true;
};

/**
 * In a JS only app the flag 'uiSortPossible' is never set and thus defaults to true. Additionally we check if each column can install
 * its comparator used to sort. If installation failed for some reason, sorting is not possible. In a remote app the server sets the
 * 'uiSortPossible' flag, which decides if the column must be sorted by the server or can be sorted by the client.
 * @see RemoteApp.js
 */
scout.Table.prototype._isUiSortPossible = function(sortColumns) {
  var uiSortPossible = scout.nvl(this.uiSortPossible, true);
  return uiSortPossible && this._isColumnsUiSortPossible(sortColumns);
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
  // compare rows
  function compare(row1, row2) {
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
  }
  this.rows.sort(compare.bind(this));
};

/**
 * @returns whether or not the column can be sorted (as a side effect a comparator is installed on each column)
 */
scout.Table.prototype._isColumnsUiSortPossible = function(sortColumns) {
  return sortColumns.every(function(column) {
    return column.isUiSortPossible();
  });
};

scout.Table.prototype._renderRowOrderChanges = function() {
  var animate,
    $rows = this.$rows(),
    oldRowPositions = {};

  // store old position
  // animate only if every row is rendered, otherwise some rows would be animated and some not
  if ($rows.length === this.filteredRows().length) {
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
    columnId: column.id,
    sortAscending: column.sortAscending
  };
  if (remove) {
    data.sortingRemoved = true;
  }
  if (multiSort) {
    data.multiSort = true;
  }
  if (sorted) {
    this._send('rowsSorted', data);
  } else {
    // Delegate sorting to server when it is not possible on client side
    this._send('sortRows', data);
    // hint to animate the aggregate after the row order changed event
    this._animateAggregateRows = animateAggregateRows;
  }
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

/**
 * @param new rows to append at the end of this.$data. If undefined this.rows is used.
 */
scout.Table.prototype._renderRowsInRange = function(range) {
  var $rows,
    rowString = '',
    numRowsRendered = 0,
    prepend = false;

  var rows = this.filteredRows();
  if (rows.length === 0) {
    return;
  }

  var maxRange = new scout.Range(0, this.rows.length);
  range = maxRange.intersect(range);
  if (!range.intersect(this.viewRangeRendered).equals(new scout.Range(0, 0))) {
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

scout.Table.prototype._removeRowsInRange = function(range) {
  var fromRow, toRow, row, i,
    numRowsRemoved = 0,
    rows = this.filteredRows();

  var maxRange = new scout.Range(0, rows.length);
  range = maxRange.intersect(range);
  fromRow = rows[range.from];
  toRow = rows[range.to];

  var newRange = this.viewRangeRendered.subtract(range);
  if (newRange.length === 2) {
    throw new Error('Can only remove rows at the beginning or end of the existing range. ' + this.viewRangeRendered + '. New: ' + newRange);
  }
  this.viewRangeRendered = newRange[0];
  this._removeEmptyData();

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

scout.Table.prototype._removeAllRows = function() {
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
  this.viewRangeRendered = new scout.Range(0, 0);
};

/**
 *
 * @param rows if undefined, all rows are removed
 */
scout.Table.prototype._removeRows = function(rows) {
  if (!rows) {
    rows = scout.arrays.ensure(rows);
    this._removeAllRows();
    return;
  }
  rows = scout.arrays.ensure(rows);
  rows.forEach(function(row) {
    var rowIndex = this.filteredRows().indexOf(row);

    if (rowIndex === -1) {
      throw new Error('Row not found, cannot remove $row');
    }
    // if row is not rendered but its rowindex is inside the view range -> inconsistency
    if (!row.$row && this.viewRangeRendered.contains(rowIndex) && !row.$row) {
      throw new Error('Inconsistency found while removing row. Row is not but inside rendered view range. RowIndex: ' + rowIndex);
    }
    // if row is rendered but its rowindex is not inside the view range -> inconsistency
    if (row.$row && !this.viewRangeRendered.contains(rowIndex)) {
      throw new Error('Inconsistency found while removing row. Row is rendered but not inside rendered view range. RowIndex: ' + rowIndex);
    }

    this._removeRow(row);

    // Adjust view range if row is inside or before range
    if (this.viewRangeRendered.contains(rowIndex) || rowIndex < this.viewRangeRendered.from) {
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

  $row.setVisible(false);
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

  var visibleColumns = this.visibleColumns();
  if (x < columnOffsetLeft) {
    // Clicked left of first column (on selection border) --> return first column
    return visibleColumns[0];
  }

  columnOffsetLeft -= scrollLeft;
  var column = scout.arrays.find(visibleColumns, function(column) {
    columnOffsetRight = columnOffsetLeft + column.width;
    if (x >= columnOffsetLeft && x < columnOffsetRight) {
      return true;
    }
    columnOffsetLeft = columnOffsetRight;
  });
  if (!column) {
    // No column found (clicked right of last column, on selection border) --> return last column
    column = scout.arrays.last(visibleColumns);
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
  this._setProperty('staticMenus', staticMenus);
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
  this._updateMenuBar();
};

scout.Table.prototype._triggerRowClicked = function(row, mouseButton, column) {
  var event = {
    row: row,
    mouseButton: mouseButton
  };
  this.trigger('rowClicked', event);
};

scout.Table.prototype._triggerRowAction = function(row, column) {
  this.trigger('rowAction', {
    row: row,
    column: column
  });
};

/**
 * @param openFieldPopupOnCellEdit when this parameter is set to true, the CellEditorPopup sets an
 *    additional property 'cellEditor' on the editor-field. The field instance may use this property
 *    to decide whether or not it should open a popup immediately after it is rendered. This is used
 *    for Smart- and DateFields.
 */
scout.Table.prototype.prepareCellEdit = function(column, row, openFieldPopupOnCellEdit) {
  this.openFieldPopupOnCellEdit = scout.nvl(openFieldPopupOnCellEdit, false);
  var field = column.createDefaultEditor(row);
  this.startCellEdit(column, row, field);
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
scout.Table.prototype.nextEditableCellPos = function(currentColumn, currentRow, backwards) {
  var pos, startColumnIndex, rowIndex, startRowIndex, predicate,
    colIndex = this.columns.indexOf(currentColumn);

  startColumnIndex = colIndex + 1;
  if (backwards) {
    startColumnIndex = colIndex - 1;
  }
  pos = this.nextEditableCellPosForRow(startColumnIndex, currentRow, backwards);
  if (pos) {
    return pos;
  }

  predicate = function(row) {
    if (!row.$row) {
      return false;
    }

    startColumnIndex = 0;
    if (backwards) {
      startColumnIndex = this.columns.length - 1;
    }
    pos = this.nextEditableCellPosForRow(startColumnIndex, row, backwards);
    if (pos) {
      return true;
    }
  }.bind(this);

  rowIndex = this.rows.indexOf(currentRow);
  startRowIndex = rowIndex + 1;
  if (backwards) {
    startRowIndex = rowIndex - 1;
  }
  scout.arrays.findFrom(this.rows, startRowIndex, predicate, backwards);

  return pos;
};

scout.Table.prototype.nextEditableCellPosForRow = function(startColumnIndex, row, backwards) {
  var cell, column, predicate;

  predicate = function(column) {
    if (column.guiOnly) {
      // does not support tabbing
      return false;
    }
    cell = this.cell(column, row);
    return this.enabled && row.enabled && cell.editable;
  }.bind(this);

  column = scout.arrays.findFrom(this.columns, startColumnIndex, predicate, backwards);
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

  rows = this.filteredRows();
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
    columnId: column.id,
    groupAscending: column.sortAscending
  };
  if (remove) {
    data.groupingRemoved = true;
  }
  if (multiGroup) {
    data.multiGroup = true;
  }
  this._triggerGroupingChanged();
  if (sorted) {
    this._send('rowsGrouped', data);
  } else {
    // Delegate sorting to server when it is not possible on client side
    this._send('groupRows', data);

    // hint to animate the aggregate after the row order changed event
    this._animateAggregateRows = true;
  }
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
  if (!this.checkable || (!this.enabled && opts.checkOnlyEnabled)) {
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
    rows[i] = row;
    // Always insert new rows at the end, if the order is wrong a rowOrderChange event will follow
    this.rows.push(row);
  }, this);

  this._applyFilters(rows);
  this._calculateValuesForBackgroundEffect();

  // this event should be triggered before the rowOrderChanged event (triggered by the _sort function).
  this._triggerRowsInserted(rows);
  this._sortAfterInsert(wasEmpty);

  // Update HTML
  if (this.rendered) {
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
  var invalidate, filterChanged;

  rows.forEach(function(row) {
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
    if (this._filterCount() > 0 && scout.arrays.remove(this._filteredRows, row)) {
      filterChanged = true;
    }
    delete this.rowsMap[row.id];

    if (this.selectionHandler.lastActionRow === row) {
      this.selectionHandler.clearLastSelectedRowMarker();
    }
  }.bind(this));

  this.deselectRows(rows);
  if (filterChanged) {
    this._rowsFiltered();
  }
  this._group();
  this._updateBackgroundEffect();
  this._triggerRowsDeleted(rows);

  if (invalidate) {
    this._renderViewport();
    // Update markers and filler because row may be removed by removeRows. RenderViewport doesn't do it if view range is already correctly rendered.
    this._renderRangeMarkers();
    this._renderFiller();
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
  this.deselectAll();
  this._filteredRows = [];

  if (filterChanged) {
    this._rowsFiltered();
  }
  this._group();
  this._updateBackgroundEffect();
  this._triggerAllRowsDeleted(rows);

  // Update HTML
  if (this.rendered) {
    this._renderFiller();
    this._renderViewport();
    this.invalidateLayoutTree();
  }
};

scout.Table.prototype.updateRow = function(row) {
  this.updateRows([row]);
};

scout.Table.prototype.updateRows = function(rows) {
  var filterChanged, newHiddenRows = [];

  // Update model
  rows.forEach(function(updatedRow) {
    var oldRow = this.rowsMap[updatedRow.id];
    if (!oldRow) {
      throw new Error('Update event received for non existing row. RowId: ' + updatedRow.id);
    }

    // Replace old row
    updatedRow = this._initRow(updatedRow);
    if (this.selectionHandler.lastActionRow === oldRow) {
      this.selectionHandler.lastActionRow = updatedRow;
    }
    // TODO [7.0] cgu: remove this replace functions, they are slow due to indexOf. Either create maps (rowId/rowIndex) before the loop or even store rowIndex for each row
    scout.arrays.replace(this.rows, oldRow, updatedRow);
    scout.arrays.replace(this.selectedRows, oldRow, updatedRow);

    // Apply row filter
    updatedRow.filterAccepted = oldRow.filterAccepted;
    if (this._filterCount() > 0) {
      if (this._applyFiltersForRow(updatedRow)) {
        filterChanged = true;
        if (!updatedRow.filterAccepted) {
          newHiddenRows.push(updatedRow);
        }
      } else {
        // If filter state has not changed, just make sure filteredRows will be recalculated the next time its used
        this._filteredRowsDirty = true;
      }
    }

    // Replace old $row
    if (this.rendered && oldRow.$row) {
      // render row and replace div in DOM
      var $updatedRow = $(this._buildRowDiv(updatedRow));
      $updatedRow.copyCssClasses(oldRow.$row, scout.Table.SELECTION_CLASSES + ' first last');
      oldRow.$row.replaceWith($updatedRow);
      scout.Table.linkRowToDiv(updatedRow, $updatedRow);
      this._destroyTooltipsForRow(updatedRow);
      this._removeCellEditorForRow(updatedRow);
      this._installRow(updatedRow);
    }
  }, this);

  this._triggerRowsUpdated(rows);

  if (filterChanged) {
    this._rowsFiltered(newHiddenRows);
    if (this.rendered) {
      // Make sure filtered rows get removed and viewport is completely rendered
      this._rerenderViewport();
    }
  }
  this._sortAfterUpdate();
  this._updateBackgroundEffect();
};

scout.Table.prototype._sortAfterUpdate = function() {
  this._sort();
};

scout.Table.prototype.updateRowOrder = function(rows) {
  rows = scout.arrays.ensure(rows);
  if (rows.length !== this.rows.length) {
    throw new Error('Row order may not be updated because lengths of the arrays differ.');
  }

  // update model (make a copy so that original array stays untouched)
  this.rows = rows.slice();
  this._filteredRowsDirty = true; // order has changed

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

  this.ensureRowRendered(row);
  var popup = column.startCellEdit(row, field);
  this.cellEditorPopup = popup;
  return popup;
};

/**
 * In a remote app this function is overridden by RemoteApp.js, the default implementation is the local case.
 * @param saveEditorValue when this parameter is set to true, the value of the editor field is set as
 *    new value on the edited cell. In remote case this parameter is always false, because the cell
 *    value is updated by an updateRow event instead.
 * @see RemoteApp.js
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

/**
 * In a remote app this function is overridden by RemoteApp.js, the default implementation is the local case.
 * @see RemoteApp.js
 */
scout.Table.prototype.completeCellEdit = function(field) {
  this.endCellEdit(field, true);
};

/**
 * In a remote app this function is overridden by RemoteApp.js, the default implementation is the local case.
 * @see RemoteApp.js
 */
scout.Table.prototype.cancelCellEdit = function(field) {
  this.endCellEdit(field);
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

scout.Table.prototype.setScrollTop = function(scrollTop) {
  this.setProperty('scrollTop', scrollTop);
  // call _renderViewport to make sure rows are rendered immediately. The browser fires the scroll event handled by onDataScroll delayed
  if (this.rendered) {
    this._renderViewport();
  }
};

scout.Table.prototype._renderScrollTop = function() {
  scout.scrollbars.scrollTop(this.$data, this.scrollTop);
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
      filteredRows = this.filteredRows(),
      previousIndex = filteredRows.indexOf(row) - 1,
      previousRowSelected = previousIndex >= 0 && this.selectedRows.indexOf(filteredRows[previousIndex]) !== -1,
      followingIndex = filteredRows.indexOf(row) + 1,
      followingRowSelected = followingIndex < filteredRows.length && this.selectedRows.indexOf(filteredRows[followingIndex]) !== -1;

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

    if (classChanged && previousRowSelected && rows.indexOf(filteredRows[previousIndex]) === -1) {
      rows.push(filteredRows[previousIndex]);
    }
    if (classChanged && followingRowSelected && rows.indexOf(filteredRows[followingIndex]) === -1) {
      rows.push(filteredRows[followingIndex]);
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
  rows = scout.arrays.ensure(rows);
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

  // Make a copy so that original array stays untouched
  this.selectedRows = rows.slice();
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
  // filtered rows are cached to avoid unnecessary loops
  if (this._filteredRowsDirty) {
    if (this._filterCount() === 0) {
      this._filteredRows = this.rows;
    } else {
      this._filteredRows = [];
      this.rows.forEach(function(row) {
        if (row.filterAccepted) {
          this._filteredRows.push(row);
        }
      }, this);
    }
    this._filteredRowsDirty = false;
  }
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
  var columnIndex = this.columns.indexOf(column);
  return $row.children().eq(columnIndex);
};

scout.Table.prototype._columnById = function(columnId) { // TODO [7.0] awe: make this function 'public'
  return scout.arrays.find(this.columns, function(column) {
    return column.id === columnId;
  });
};

scout.Table.prototype._columnsByIds = function(columnIds) {
  return columnIds.map(this._columnById.bind(this));
};

scout.Table.prototype.filter = function() {
  var animate = true,
    numChangedRows = 0,
    newHiddenRows = [],
    newShownRows = [];

  // Filter rows
  this.rows.forEach(function(row) {
    var changed = this._applyFiltersForRow(row);
    if (changed) {
      if (!row.filterAccepted) {
        newHiddenRows.push(row);
      } else {
        newShownRows.push(row);
      }
    }
  }, this);

  numChangedRows = newHiddenRows.length + newShownRows.length;
  if (numChangedRows === 0) {
    return;
  }

  this._rowsFiltered(newHiddenRows);
  this._group(animate);

  if (this.rendered) {
    animate = numChangedRows <= this._animationRowLimit;
    if (!animate) {
      this._rerenderViewport();
    } else {
      newHiddenRows.forEach(function(row) {
        this._hideRow(row);
      }.bind(this));

      this._rerenderViewport();
      // Rows removed by an animation are still there, new rows were appended -> reset correct row order
      this._order$Rows().insertAfter(this.$fillBefore);
      // Also make sure aggregate rows are at the correct position (_renderAggregateRows does nothing because they are already rendered)
      this._order$AggregateRows();
      newShownRows.forEach(function(row) {
        this._showRow(row);
      }, this);
    }
    this._renderEmptyData();
  }
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

scout.Table.prototype._rowsFiltered = function(hiddenRows) {
  // non visible rows must be deselected
  this.deselectRows(hiddenRows);
  // notify
  this._filteredRowsDirty = true;
  this._triggerRowsFiltered();
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
 * Applies the filters for the given rows.<p>
 * This function is intended to be used for new rows. That's why rowsFiltered event is only triggered if there are accepted rows in the given list.
 */
scout.Table.prototype._applyFilters = function(rows) {
  var filterChanged,
    newHiddenRows = [];

  if (this._filterCount() === 0) {
    this._filteredRowsDirty = true;
    return;
  }

  rows.forEach(function(row) {
    if (this._applyFiltersForRow(row)) {
      filterChanged = true;
      if (!row.filterAccepted) {
        newHiddenRows.push(row);
      }
    }
    // always notify if there are new rows which accept the filter
    if (row.filterAccepted) {
      filterChanged = true;
    }
  }, this);

  if (filterChanged) {
    this._rowsFiltered(newHiddenRows);
  }
};

/**
 *
 * @returns array of filter names which are currently active
 */
scout.Table.prototype.filteredBy = function() {
  var filteredBy = [];
  for (var key in this._filterMap) { // NOSONAR
    var filter = this._filterMap[key];
    filteredBy.push(filter.createLabel());
  }
  return filteredBy;
};

scout.Table.prototype.resetFilter = function() {
  // remove filters
  for (var key in this._filterMap) { // NOSONAR
    this.removeFilterByKey(key);
  }
  this._filterMap = {};

  // reset rows
  this.filter();
  this._triggerFilterResetted();
};

scout.Table.prototype.resizeToFit = function(column) {
  if (column.fixedWidth) {
    return;
  }
  var calculatedSize = column.calculateOptimalWidth();
  if (scout.device.browser === scout.Device.Browser.INTERNET_EXPLORER && calculatedSize !== column.minWidth) {
    calculatedSize++;
  }
  if (column.width !== calculatedSize) {
    this.resizeColumn(column, calculatedSize);
  }
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
  this.trigger('addFilter', {
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
  this.trigger('removeFilter', {
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
 * While resizing a column, this method is called for each change of the width. As long as the resizing is in
 * progress (e.g. the mouse button has not been released), the column is marked with the flag "resizingInProgress".
 * When the resizing has finished, this method has to be called again without the flag "resizingInProgress" to
 * correctly set the width of the "empty data" div.
 *
 * @param column
 *          (required) column to resize
 * @param width
 *          (required) new column size
 */
scout.Table.prototype.resizeColumn = function(column, width) {
  if (column.fixedWidth) {
    return;
  }
  var colNum = this.columns.indexOf(column) + 1;
  width = Math.floor(width);
  column.width = width;
  this._updateRowWidth();

  this.$cellsForColIndex(colNum, true)
    .css('min-width', width)
    .css('max-width', width);
  if (scout.device.tableAdditionalDivRequired) {
    this.$cellsForColIndexWidthFix(colNum, true)
      .css('max-width', (width - this.cellHorizontalPadding - 2 /* unknown IE9 extra space */ ));
    // same calculation in scout.Column.prototype.buildCellForRow;
  }
  this.$rows(true)
    .css('width', this.rowWidth);

  // If resized column contains cells with wrapped text, view port needs to be updated
  // Remove row height for non rendered rows because it may have changed due to resizing (wrap text)
  this._updateRowHeights();
  this._renderFiller();
  this._renderViewport();
  this.updateScrollbars();

  this._triggerColumnResized(column);

  if (column.resizingInProgress) {
    this._renderEmptyData();
  } else {
    this._renderEmptyData(this.rowWidth - this.rowBorderWidth);
  }
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

  visibleNewPos = this.visibleColumns().indexOf(column); // we must re-evaluate visible columns
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

scout.Table.prototype._triggerRowsFiltered = function() {
  this.trigger('rowsFiltered');
};

scout.Table.prototype._triggerFilterResetted = function() {
  this.trigger('filterResetted');
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

scout.Table.prototype._triggerExportToClipboard = function() {
  this.trigger('exportToClipboard');
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

scout.Table.prototype._triggerGroupingChanged = function() {
  this.trigger('groupingChanged');
};

scout.Table.prototype._renderHeaderVisible = function() {
  this._renderTableHeader();
};

scout.Table.prototype._renderHeaderEnabled = function() {
  // Rebuild the table header when this property changes
  this._removeTableHeader();
  this._renderTableHeader();
};

scout.Table.prototype._setCheckable = function(checkable) {
  this._setProperty('checkable', checkable);

  var column = this.checkableColumn;
  if (this.checkable && !column) {
    this._insertBooleanColumn();
  } else if (!this.checkable && column) {
    scout.arrays.remove(this.columns, column);
    this.checkableColumn = null;
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

scout.Table.prototype._setRowIconVisible = function(rowIconVisible) {
  this._setProperty('rowIconVisible', rowIconVisible);
  var column = this.rowIconColumn;
  if (this.rowIconVisible && !column) {
    this._insertRowIconColumn();
  } else if (!this.rowIconVisible && column) {
    scout.arrays.remove(this.columns, column);
    this.rowIconColumn = null;
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
  for (var key in this._filterMap) { // NOSONAR
    this.removeFilterByKey(key);
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
    filter.column = this._columnById(filter.column);
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

scout.Table.prototype.setFooterVisible = function(visible) {
  this._setProperty('footerVisible', visible);
  if (visible && !this.footer) {
    this.footer = this._createFooter();
  }
  if (this.rendered) {
    this._renderFooterVisible();
  }
  if (!visible && this.footer) {
    this.footer.destroy();
    this.footer = null;
  }
};

scout.Table.prototype.setHeaderVisible = function(visible) {
  this.setProperty('headerVisible', visible);
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

scout.Table.prototype._renderCheckable = function() {
  this._redraw();
};

scout.Table.prototype._renderRowIconVisible = function() {
  this._redraw();
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
scout.Table.prototype._renderEmptyData = function(width) {
  if (this.header && this.filteredRows().length === 0) {
    if (!this.$emptyData) {
      this.$emptyData = this.$data.appendDiv().html('&nbsp;');
    }
    // measure header-width and subtract insets from table-data
    var
      horizInsets = scout.graphics.getInsets(this.$data).horizontal(),
      headerWidth = scout.nvl(width, this.header.$container[0].scrollWidth) - horizInsets;
    this.$emptyData
      .css('min-width', headerWidth)
      .css('max-width', headerWidth);
  }
  this.updateScrollbars();
};

scout.Table.prototype._removeEmptyData = function() {
  if (this.filteredRows().length > 0 && this.$emptyData) {
    this.$emptyData.remove();
    this.$emptyData = undefined;
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

scout.Table.prototype._renderAutoResizeColumns = function() {
  if (this.autoResizeColumns) {
    this.invalidateLayoutTree();
  }
};

scout.Table.prototype._renderDropType = function() {
  if (this.dropType) {
    this._installDragAndDropHandler();
  } else {
    this._uninstallDragAndDropHandler();
  }
};

scout.Table.prototype._renderCheckableStyle = function() {
  this.$container.toggleClass('checkable', this.checkableStyle === scout.Table.CheckableStyle.TABLE_ROW);
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
  this.filteredRows().some(function(row, i) {
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
    return new scout.Range(0, this.filteredRows().length);
  }

  var viewRange = new scout.Range(),
    quarterRange = Math.floor(this.viewRangeSize / 4),
    diff;

  viewRange.from = Math.max(rowIndex - quarterRange, 0);
  viewRange.to = Math.min(viewRange.from + this.viewRangeSize, this.filteredRows().length);

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
    var rows = this.filteredRows();
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
  var firstRow, lastRow;
  if (this.viewRangeRendered.size() === 0) {
    return;
  }
  firstRow = this.filteredRows()[this.viewRangeRendered.from];
  lastRow = this.filteredRows()[this.viewRangeRendered.to - 1];
  firstRow.$row.removeClass('first');
  lastRow.$row.removeClass('last');
};

scout.Table.prototype._renderRangeMarkers = function() {
  var firstRow, lastRow;
  if (this.viewRangeRendered.size() === 0) {
    return;
  }
  firstRow = this.filteredRows()[this.viewRangeRendered.from];
  lastRow = this.filteredRows()[this.viewRangeRendered.to - 1];
  firstRow.$row.addClass('first');
  lastRow.$row.addClass('last');
};

scout.Table.prototype.ensureRowRendered = function(row) {
  if (!row.$row) {
    var rowIndex = this.filteredRows().indexOf(row);
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
  $.log.trace('FillBefore height: ' + fillBeforeHeight);

  if (!this.$fillAfter) {
    this.$fillAfter = this.$data.appendDiv('table-data-fill');
    this._applyFillerStyle(this.$fillAfter);
  }

  var fillAfterHeight = this._calculateFillerHeight(new scout.Range(this.viewRangeRendered.to, this.filteredRows().length));
  this.$fillAfter.cssHeight(fillAfterHeight);
  this.$fillAfter.cssWidth(this.rowWidth);
  $.log.trace('FillAfter height: ' + fillAfterHeight);
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
    var row = this.filteredRows()[i];
    totalHeight += this._heightForRow(row);
  }
  return totalHeight;
};

scout.Table.prototype.containsAggregatedNumberColumn = function() {
  return this.visibleColumns().some(function(column) {
    return column instanceof scout.NumberColumn && column.aggregationFunction !== 'none';
  });
};

/**
 * Rebuilds the header.<br>
 * Does not modify the rows, it expects a deleteAll and insert operation to follow which will do the job.
 */
scout.Table.prototype.updateColumnStructure = function(columns) {
  this.columns = columns;
  this._initColumns();

  if (this.rendered) {
    this._updateRowWidth();
    this.$rows(true).css('width', this.rowWidth);
    this._rerenderHeaderColumns();
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
    scout.defaultValues.applyTo(columns[i], 'Column');
    column = this._columnById(columns[i].id);
    oldColumnState = $.extend(oldColumnState, column);
    column.text = columns[i].text;
    column.tooltipText = columns[i].tooltipText;
    column.headerCssClass = columns[i].headerCssClass;
    column.headerBackgroundColor = columns[i].headerBackgroundColor;
    column.headerForegroundColor = columns[i].headerForegroundColor;
    column.headerFont = columns[i].headerFont;
    column.headerIconId = columns[i].headerIconId;
    column.sortActive = columns[i].sortActive;
    column.sortAscending = columns[i].sortAscending;
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

scout.Table.prototype.requestFocusInCell = function(column, row) {
  if (!this.rendered || !this.isAttachedAndRendered()) {
    this._postRenderActions.push(this.requestFocusInCell.bind(this, column, row));
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
  this.htmlComp.setSize(htmlParent.getSize());
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

scout.Table.prototype.visibleColumns = function() {
  return this.columns.filter(function(column) {
    return column.visible;
  });
};

/**
 * @override Widget.js
 */
scout.Table.prototype._afterAttach = function(parent) {
  if (this._rerenderViewPortAfterAttach) {
    this._rerenderViewport();
    this._rerenderViewPortAfterAttach = false;
  }
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
