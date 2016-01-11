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
scout.Table = function(model) {
  scout.Table.parent.call(this, model);
  this.$container;
  this.$data;
  this.header;
  this.selectionHandler;
  this.columns = [];
  this.tableControls = [];
  this.menus = [];
  this.rows = [];
  this.rowsMap = {}; // rows by id
  this.rowWidth = 0;
  this.rowBorderWidth; // read-only, set by _calculateRowBorderWidth(), also used in TableLayout.js
  this.rowBorderLeftWidth = 0; // read-only, set by _calculateRowBorderWidth(), also used in TableHeader.js
  this.rowBorderRightWidth = 0; // read-only, set by _calculateRowBorderWidth(), also used in TableHeader.js
  this.staticMenus = [];
  this.selectionHandler = new scout.TableSelectionHandler(this);
  this._keyStrokeSupport = new scout.KeyStrokeSupport(this);
  this._filterMap = {};
  this._filteredRows = [];
  this._filteredRowsDirty = true;
  this.tooltips = [];
  this._aggregateRows = [];
  this._animationRowLimit = 25;
  this._blockLoadThreshold = 25;
  this.menuBar;
  this._renderRowsInProgress = false;
  this._drawDataInProgress = false;
  this._doubleClickSupport = new scout.DoubleClickSupport();
  this.checkableStyle = scout.Table.CheckableStyle.CHECKBOX;
  this._addAdapterProperties(['tableControls', 'menus', 'keyStrokes']);

  this._permanentHeadSortColumns = [];
  this._permanentTailSortColumns = [];
  // Initial value must be > 0 to make prefSize work (if it is 0, no filler will be generated).
  // If rows have a variable height, prefSize is only correct for 10 rows.
  // Layout will adjust this value depending on the view port size.
  this.viewRangeSize = 10;
  this.viewRangeRendered = new scout.Range(0, 0);
  this._filterMenusHandler = this._filterMenus.bind(this);
};
scout.inherits(scout.Table, scout.ModelAdapter);

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

  this.rows.forEach(function(row) {
    this._initRow(row);
  }, this);

  this.menuBar = scout.create('MenuBar', {
    parent: this,
    menuOrder: new scout.MenuItemsOrder(this.session, 'Table'),
    menuFilter: this._filterMenusHandler
  });
  this.menuBar.bottom();

  this._syncSelectedRows(this.selectedRows);
  this._syncFilters(this.filters);
  this._syncKeyStrokes(this.keyStrokes);
  this._syncMenus(this.menus);
  if (this._filterCount() > 0) {
    this._applyFilters(this.rows);
  }
  this._calculateValuesForBackgroundEffect();
  this._group();
};

scout.Table.prototype._initRow = function(row) {
  scout.defaultValues.applyTo(row, 'TableRow');
  this._initCells(row);
  this.rowsMap[row.id] = row;
  this.trigger('rowInitialized', {
    row: row
  });
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
  this._syncCheckable(this.checkable);

  // Sync head and tail sort columns
  this._syncHeadAndTailSortColumns();
};

scout.Table.prototype._initCells = function(row) {
  this.columns.forEach(function(column) {
    if (!column.guiOnly) {
      var cell = row.cells[column.index];
      cell = column.initCell(cell);
      row.cells[column.index] = cell;
    }
  });
};

/**
 * @override ModelAdapter.js
 */
scout.Table.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.Table.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  this._initTableKeyStrokeContext(keyStrokeContext);
};

scout.Table.prototype._initTableKeyStrokeContext = function(keyStrokeContext) {
  keyStrokeContext.registerKeyStroke([
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
    ]
    .concat(this.tableControls)
    .concat(this.menus));

  // Prevent default action and do not propagate ↓ or ↑ keys if ctrl- or alt-modifier is not pressed.
  // Otherwise, an '↑-event' on the first row, or an '↓-event' on the last row will bubble up (because not consumed by table navigation keystrokes) and cause a superior table to move its selection.
  // Use case: - outline page table with search form that contains a table field;
  //           - shift + '↑-event'/'↓-event' are not consumed by a single selection table, and would propagate otherwise;
  //           - preventDefault because of smartfield, so that the cursor is not moved on first or last row;
  keyStrokeContext.registerStopPropagationInterceptor(function(event) {
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
  this._sendAppLinkAction(column.id, $appLink.data('ref'));
};

scout.Table.prototype._render = function($parent) {
  this.$container = this._$parent.appendDiv('table');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.TableLayout(this));
  this.htmlComp.pixelBasedSizing = false;

  this.$data = this.$container.appendDiv('table-data');
  this.$data.on('mousedown', '.table-row', onMouseDown)
    .on('mouseup', '.table-row', onMouseUp)
    .on('dblclick', '.table-row', onDoubleClick)
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

  this.scrollTop = 0;
  this._calculateRowBorderWidth();
  this._updateRowWidth();
  this._updateRowHeight();
  this._renderViewport();
  if (this.scrollToSelection) {
    // Execute delayed because table may be not layouted yet
    setTimeout(this.revealSelection.bind(this));
  }

  //----- inline methods: --------

  var $mouseDownRow, mouseDownColumn, row, that = this;

  function onMouseDown(event) {
    that._doubleClickSupport.mousedown(event);
    $mouseDownRow = $(event.currentTarget);
    mouseDownColumn = that._columnAtX(event.pageX);
    that.selectionHandler.onMouseDown(event);

    if (that.checkableStyle === scout.Table.CheckableStyle.TABLE_ROW) {
      row = $mouseDownRow.data('row');
      that.checkRow(row, !row.checked);
    }
  }

  function onMouseUp(event) {
    var $row, $mouseUpRow, column, $appLink,
      mouseButton = event.which;

    if (that._doubleClickSupport.doubleClicked()) {
      // Don't execute on double click events
      return;
    }

    $mouseUpRow = $(event.currentTarget);
    that.selectionHandler.onMouseUp(event, $mouseUpRow);

    if ($mouseDownRow && $mouseUpRow && $mouseDownRow.attr('data-rowid') !== $mouseUpRow.attr('data-rowid')) { // FIXME AWE: replace with $data('row')[0] !== $data('row')[0]
      return;
    }

    $row = $mouseUpRow;
    column = that._columnAtX(event.pageX);
    if (column !== mouseDownColumn) {
      // Don't execute click / appLinks when the mouse gets pressed and moved outside of a cell
      return;
    }
    if (mouseButton === 1) {
      column.onMouseUp(event, $row);
      $appLink = that._find$AppLink(event);
    }
    if ($appLink) {
      that._sendAppLinkAction(column.id, $appLink.data('ref'));
    } else if (column.guiOnly) {
      that._sendRowClicked($row, mouseButton);
    } else {
      that._sendRowClicked($row, mouseButton, column.id);
    }
  }

  function onDoubleClick(event) {
    var $row = $(event.currentTarget),
      column = that._columnAtX(event.pageX);
    that.doRowAction($row.data('row'), column);
  }
};

scout.Table.prototype.onContextMenu = function(event) {
  var func = function(event) {
    var menuItems, popup;
    event.preventDefault();
    if (this.selectedRows.length > 0) {
      menuItems = this._filterMenus(this.menus, scout.MenuDestinations.CONTEXT_MENU, true);
      if (!event.pageX && !event.pageY) {
        var $rowToDisplay = this.selectionHandler.lastActionRow ? this.selectionHandler.lastActionRow.$row : this.selectedRows[this.selectedRows.length - 1].$row;
        var offset = $rowToDisplay.offset();
        event.pageX = offset.left + 10;
        event.pageY = offset.top + $rowToDisplay.outerHeight() / 2;
      }
      if (menuItems.length > 0) {
        popup = scout.create('ContextMenuPopup', {
          parent: this,
          menuItems: menuItems,
          location: {
            x: event.pageX,
            y: event.pageY
          },
          $anchor: this.$data,
          menuFilter: this._filterMenusHandler
        });
        popup.open(undefined, event);
      }
    }
  };

  scout.menus.showContextMenuWithWait(this.session, func.bind(this), event);
};

scout.Table.prototype._renderProperties = function() {
  scout.Table.parent.prototype._renderProperties.call(this);
  this._renderTableHeader();
  this._renderTableFooter();
  this._renderMenus();
  this._renderEnabled();
  this._renderDropType();
  this._renderCssClass();
};

scout.Table.prototype._remove = function() {
  scout.scrollbars.uninstall(this.$data, this.session);
  // FIXME CGU do not delete header and footer!
  this.header = null;
  this.footer = null;
  this._removeAggregateRows();
  this._uninstallCellTooltipSupport();
  this._removeRows();
  this.$fillBefore = null;
  this.$fillAfter = null;
  scout.Table.parent.prototype._remove.call(this);
};

// FIXME AWE: refactor all _render* methods --> remove parameter, always use this.*
// reason: the property on this is already synced at this point, the argument may contain
// just a data-model value (and not a adpater).
scout.Table.prototype._renderTableControls = function() {
  this._renderTableFooter();
  if (this.footer) {
    this.footer._renderControls();
  }
};

scout.Table.prototype._renderSortEnabled = function(dummy) {};

scout.Table.prototype._renderUiSortPossible = function(dummy) {};

scout.Table.prototype._renderColumnAddable = function(dummy) {};

scout.Table.prototype._syncTableControls = function(controls) {
  var i;
  for (i = 0; i < this.tableControls.length; i++) {
    this.keyStrokeContext.unregisterKeyStroke(this.tableControls[i]);
  }
  this.tableControls = controls;
  for (i = 0; i < this.tableControls.length; i++) {
    if (this.tableControls[i].enabled) {
      this.keyStrokeContext.registerKeyStroke(this.tableControls[i]);
    }
  }
};

scout.Table.prototype._onDataScroll = function() {
  var scrollTop = this.$data[0].scrollTop;
  if (this.scrollTop === scrollTop) {
    return;
  }
  this._renderViewport();
  this.scrollTop = scrollTop;
};

/**
 * @override
 */
scout.Table.prototype._onChildAdapterCreation = function(propertyName, model) {
  if (propertyName === 'tableControls') {
    model.table = this;
  }
};

scout.Table.prototype._renderTableStatusVisible = function() {
  this._renderTableFooter();
};

scout.Table.prototype._renderTableStatus = function() {
  this.trigger('statusChanged');
};

scout.Table.prototype._isFooterVisible = function() {
  return this.tableStatusVisible || this._hasVisibleTableControls();
};

scout.Table.prototype._hasVisibleTableControls = function() {
  return this.tableControls.some(function(control) {
    if (control.visible) {
      return true;
    }
    return false;
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
  return !this.headerVisible || column.fixedWidth;
};

scout.Table.prototype.reload = function() {
  if (!this.hasReloadHandler) {
    return;
  }
  this._removeRows();
  this._renderFiller();
  this._sendReload();
};

scout.Table.prototype.exportToClipboard = function() {
  this._sendExportToClipboard();
};

scout.Table.prototype.toggleSelection = function() {
  if (this.selectedRows.length === this.filteredRows().length) {
    this.deselectAll();
  } else {
    this.selectAll();
  }
};

scout.Table.prototype.selectAll = function(notifyServer) {
  this.selectRows(this.filteredRows(), notifyServer);
};

scout.Table.prototype.deselectAll = function(notifyServer) {
  this.selectRows([], notifyServer);
};

scout.Table.prototype.checkAll = function(check) {
  check = scout.nvl(check, true);
  var rows = this.filteredRows();
  rows.forEach(function(row) {
    this.checkRow(row, check);
  }, this);
};

scout.Table.prototype.uncheckAll = function() {
  this.checkAll(false);
};

scout.Table.prototype.updateScrollbars = function() {
  scout.scrollbars.update(this.$data);
};

scout.Table.prototype._sort = function(animateAggregateRows) {
  var column, sortIndex,
    sortColumns = [];

  // find all sort columns
  for (var c = 0; c < this.columns.length; c++) {
    column = this.columns[c];
    sortIndex = column.sortIndex;
    if (sortIndex >= 0) {
      sortColumns[sortIndex] = column;
    }
  }

  // Initialize comparators
  var clientSideSortingPossible = this.uiSortPossible && this._prepareColumnsForSorting(sortColumns);
  if (!clientSideSortingPossible) {
    return false;
  }
  this.clearAggregateRows(animateAggregateRows);
  if (!sortColumns.length) {
    // no sort column defined.
    return true;
  }

  this._sortImpl(sortColumns);

  this._filteredRowsDirty = true; // order has been changed
  this._triggerRowOrderChanged();
  this._group(animateAggregateRows);
  if (this.rendered) {
    this._renderRowOrderChanges();
  }

  // Sort was possible -> return true
  return true;
};

scout.Table.prototype._sortImpl = function(sortColumns) {
  // compare rows
  function compare(row1, row2) {
    for (var s = 0; s < sortColumns.length; s++) {
      var column = sortColumns[s];
      var valueA = this.cellValue(column, row1);
      var valueB = this.cellValue(column, row2);
      var direction = column.sortActive && column.sortAscending ? -1 : 1;

      var result = column.compare(valueA, valueB);
      if (result < 0) {
        return direction;
      } else if (result > 0) {
        return -1 * direction;
      }
    }

    return 0;
  }
  this.rows.sort(compare.bind(this));
};

// initialize comparators
scout.Table.prototype._prepareColumnsForSorting = function(sortColumns) {
  var i, column;
  for (i = 0; i < sortColumns.length; i++) {
    column = sortColumns[i];
    if (!column.uiSortPossible) {
      return false;
    }
    if (!column.prepareForSorting()) {
      return false;
    }
  }
  return true;
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
  var data, sorted,
    // Animate if sort removes aggregate rows
    animateAggregateRows = !multiSort;

  this._updateSortColumns(column, direction, multiSort, remove);
  if (this.header) {
    this.header.onSortingChanged();
  }
  sorted = this._sort(animateAggregateRows);

  data = {
    columnId: column.id,
    sortingRemoved: remove,
    multiSort: multiSort,
    sortAscending: column.sortAscending
  };
  if (sorted) {
    this._send('rowsSorted', data);
  } else {
    // Delegate sorting to server when it is not possible on client side
    this._send('sortRows', data);
    // hint to animate the aggregate after the row order changed event
    this._animateAggregateRows = animateAggregateRows;
  }
};

scout.Table.prototype._updateSortColumns = function(column, direction, multiSort, remove) {
  var sortIndex = -1,
    deviation,
    groupColCount,
    sortColCount;

  if (remove) {

    if (!(column.initialAlwaysIncludeSortAtBegin || column.initialAlwaysIncludeSortAtEnd)) {
      column.sortActive = false;
      column.grouped = false;

      // Adjust sibling columns with higher index
      scout.arrays.eachSibling(this.columns, column, function(siblingColumn) {
        if (siblingColumn.sortIndex > column.sortIndex) {
          siblingColumn.sortIndex = siblingColumn.sortIndex - 1;
        }
      });
      column.sortIndex = -1;
    }
    return;
  }

  if (!(column.initialAlwaysIncludeSortAtBegin || column.initialAlwaysIncludeSortAtEnd)) {
    // do not update sort index for permanent head/tail sort columns, their order is fixed (see ColumnSet.java)
    if (multiSort) {
      // if not already sorted set the appropriate sort index
      if (!column.sortActive) {
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
      column.sortIndex = this._permanentHeadSortColumns.length;
    }
  }

  if (!multiSort) {
    groupColCount = this._groupedColumns() ? this._groupedColumns().length : 0;
    sortColCount = 0;
    this.columns.forEach(function(c) {
      if (c.sortActive) {
        sortColCount++;
      }
    });

    // remove sort index for siblings (ignore permanent head/tail columns, only if not multi sort)
    scout.arrays.eachSibling(this.columns, column, function(siblingColumn) {
      if (siblingColumn.sortActive && !(siblingColumn.initialAlwaysIncludeSortAtBegin || siblingColumn.initialAlwaysIncludeSortAtEnd)) {
        siblingColumn.sortIndex = -1;
        siblingColumn.sortActive = false;
        siblingColumn.grouped = false;
      }
    });

    //special case: if it is the only sort column and also grouped, do not remove grouped property.
    if (sortColCount === 1 && groupColCount === 1) {
      //do not remove grouping property
    } else {
      column.grouped = false;
    }

    // set correct sort index for all permanent tail sort columns
    deviation = (column.initialAlwaysIncludeSortAtBegin || column.initialAlwaysIncludeSortAtEnd) ? 0 : 1;
    this._permanentTailSortColumns.forEach(function(c, index) {
      c.sortIndex = this._permanentHeadSortColumns.length + deviation + index;
    }, this);
  }

  if (!(column.initialAlwaysIncludeSortAtBegin || column.initialAlwaysIncludeSortAtEnd)) {
    column.sortAscending = direction === 'asc' ? true : false;
    column.sortActive = true;
  }
};

scout.Table.prototype._isGroupingPossible = function(column) {
  var possible = true;

  //TODO: incorporate this logic into visibility of grouping buttons on column header

  if (this._permanentHeadSortColumns && this._permanentHeadSortColumns.length === 0) {
    // no permanent head sort columns. grouping ok.
    return true;
  }

  if (!column.initialAlwaysIncludeSortAtBegin && !column.initialAlwaysIncludeSortAtEnd) {
    // col itself is not a head or tail sort column. therefore, all head sort columns must be grouped.
    this._permanentHeadSortColumns.forEach(function(c) {
      possible &= c.grouped;
    });
    return possible;
  }

  if (column.initialAlwaysIncludeSortAtEnd) {
    possible = true;
    scout.arrays.eachSibling(this._permanentHeadSortColumns, column, function(c) {
      if (c.sortIndex < column.sortIndex) {
        possible &= c.grouped;
      }
    });
    return possible;
  }

  // else: it is a tail sort column. grouping does not make sense.
  return false;
};

scout.Table.prototype.changeAggregation = function(column, func) {
  column.setAggregationFunction(func);

  this._sendAggregationFunctionChanged(column);
  this._triggerAggregationFunctionChanged(column);

  this._group();
};

scout.Table.prototype._updateSortColumnsForGrouping = function(column, direction, multiGroup, remove) {
  var sortIndex = -1,
    deviation;

  if (remove) {
    column.grouped = false;

    if (column.initialAlwaysIncludeSortAtBegin) {
      //head sort case: remove all groupings after this column.
      this.columns.forEach(function(c) {
        if (c.sortIndex >= column.sortIndex) {
          c.grouped = false;
        }
      });
    }

    return this._updateSortColumns(column, direction, multiGroup, remove);
  }

  if (!this._isGroupingPossible(column)) {
    return;
  }

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
        //column already sorted, update position:
        //move all sort columns between the newly determined sortindex and the old sortindex by one.
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
      //no multigroup:
      sortIndex = this._permanentHeadSortColumns.length;

      if (column.sortActive) {
        //column already sorted, update position:
        //move all sort columns between the newly determined sortindex and the old sortindex by one.
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

      //remove all other grouped properties:
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
      //do not change order or direction. just set grouped to true.
      column.grouped = true;
    }

  }

  column.grouped = true;
};

scout.Table.prototype._buildRowDiv = function(row) {
  var rowWidth = this.rowWidth;
  var rowClass = 'table-row';
  if (!row.enabled) {
    rowClass += ' disabled';
  }
  if (row.checked && this.checkableStyle === scout.Table.CheckableStyle.TABLE_ROW) {
    rowClass += ' checked';
  }
  var rowDiv = '<div class="' + rowClass + '" data-rowid="' + row.id + '" style="width: ' + rowWidth + 'px"' + scout.device.unselectableAttribute.string + '>';
  for (var c = 0; c < this.columns.length; c++) {
    rowDiv += this.columns[c].buildCellForRow(row);
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
  this.rowWidth = this.rowBorderWidth;
  for (var i = 0; i < this.columns.length; i++) {
    this.rowWidth += this.columns[i].width;
  }
};

scout.Table.prototype._updateRowHeight = function() {
  var $emptyRow = this.$data.appendDiv('table-row');
  $emptyRow.appendDiv('table-cell').html('&nbsp;');
  this.rowHeight = $emptyRow.outerHeight(true);
  $emptyRow.remove();
};

/**
 * @param new rows to append at the end of this.$data. If undefined this.rows is used.
 */
scout.Table.prototype._renderRowsInRange = function(range) {
  var $rows,
    rowString = '',
    that = this,
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
  if (newRange.length == 2) {
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
  if (newRange.length == 2) {
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
    var $row = row.$row,
      rowIndex = this.filteredRows().indexOf(row);

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

  this._removeTooltipsForRow(row);
  if (row.hasError) {
    this._showCellErrorForRow(row);
  }
  // Reopen editor popup with cell because state of popup (row, $anchor etc.) is not valid anymore
  if (this.cellEditorPopup && this.cellEditorPopup.row.id === row.id) {
    this.cellEditorPopup.remove();
    var editorField = this.cellEditorPopup.cell.field;
    this._startCellEdit(this.cellEditorPopup.column, row, editorField.id);
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
    scrollType: 'position',
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
  var column = scout.arrays.find(this.columns, function(column) {
    columnOffsetRight = columnOffsetLeft + column.width;
    if (x >= columnOffsetLeft && x < columnOffsetRight) {
      return true;
    }
    columnOffsetLeft = columnOffsetRight;
  });
  if (!column) {
    // No column found (clicked right of last column, on selection border) --> return last column
    column = this.columns[this.columns.length - 1];
  }
  return column;
};

scout.Table.prototype._find$AppLink = function(event) {
  // bubble up from target to delegateTarget
  var $elem = $(event.target);
  var $stop = $(event.delegateTarget);
  var appLink;
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

scout.Table.prototype._filterMenus = function(menus, destination, onlyVisible, enableDisableKeyStroke) {
  return scout.menus.filterAccordingToSelection('Table', this.selectedRows.length, menus, destination, onlyVisible, enableDisableKeyStroke);
};

scout.Table.prototype._renderMenus = function() {
  this._updateMenuBar();
  if (this.header) {
    this.header.updateMenuBar();
  }
};

scout.Table.prototype._updateMenuBar = function() {
  var menuItems = this._filterMenus(this.menus, scout.MenuDestinations.MENU_BAR, false, true);
  menuItems = this.staticMenus.concat(menuItems);
  this.menuBar.updateItems(menuItems);
};

scout.Table.prototype.notifyRowSelectionFinished = function() {
  if (this._sendRowsPending) {
    this._sendRowsSelected(this._rowsToIds(this.selectedRows));
    this._sendRowsPending = false;
  }
  this._triggerRowsSelected();
  this._updateMenuBar();
};

// Only necessary if the table is a root html comp (outline table)
scout.Table.prototype.onResize = function() {
  this.htmlComp.revalidateLayoutTree();
};

scout.Table.prototype._sendRowClicked = function($row, mouseButton, columnId) {
  var data = {
    rowId: $row.data('row').id,
    mouseButton: mouseButton
  };
  if (columnId !== undefined) {
    data.columnId = columnId;
  }
  this._send('rowClicked', data);
};

/**
 * @param openFieldPopupOnCellEdit when this parameter is set to true, the CellEditorPopup sets an
 *    additional property 'cellEditor' on the editor-field. The field instance may use this property
 *    to decide whether or not it should open a popup immediately after it is rendered. This is used
 *    for Smart- and DateFields.
 */
scout.Table.prototype.prepareCellEdit = function(rowId, columnId, openFieldPopupOnCellEdit) {
  this.openFieldPopupOnCellEdit = scout.nvl(openFieldPopupOnCellEdit, false);
  this._sendPrepareCellEdit(rowId, columnId);
};

scout.Table.prototype._sendPrepareCellEdit = function(rowId, columnId) {
  var data = {
    rowId: rowId,
    columnId: columnId
  };
  this._send('prepareCellEdit', data);
};

scout.Table.prototype._sendCompleteCellEdit = function(fieldId) {
  var data = {
    fieldId: fieldId
  };
  this._send('completeCellEdit', data);
};

scout.Table.prototype._sendCancelCellEdit = function(fieldId) {
  var data = {
    fieldId: fieldId
  };
  this._send('cancelCellEdit', data);
};

scout.Table.prototype._sendRowsChecked = function(rows) {
  var data = {
    rows: []
  };

  for (var i = 0; i < rows.length; i++) {
    data.rows.push({
      rowId: rows[i].id,
      checked: rows[i].checked
    });
  }

  this._send('rowsChecked', data);
};

scout.Table.prototype._sendRowsSelected = function(rowIds, debounceSend) {
  var eventData = {
    rowIds: rowIds
  };

  // send delayed to avoid a lot of requests while selecting
  // coalesce: only send the latest selection changed event for a field
  this._send('rowsSelected', eventData, debounceSend ? 250 : 0, function(previous) {
    return this.id === previous.id && this.type === previous.type;
  });
};

scout.Table.prototype._sendRowsFiltered = function(rowIds) {
  var eventData = {};
  if (rowIds.length === this.rows.length) {
    eventData.remove = true;
  } else {
    eventData.rowIds = rowIds;
  }

  // send with timeout, mainly for incremental load of a large table
  // coalesce: only send last event
  this._send('rowsFiltered', eventData, 250, function(previous) {
    return this.id === previous.id && this.type === previous.type;
  });
};

scout.Table.prototype._sendRowAction = function(row, column) {
  this._send('rowAction', {
    rowId: row.id,
    columnId: column.id
  });
};

scout.Table.prototype._sendAppLinkAction = function(columnId, ref) {
  this._send('appLinkAction', {
    columnId: columnId,
    ref: ref
  });
};

scout.Table.prototype._sendReload = function() {
  this._send('reload');
};

scout.Table.prototype._sendExportToClipboard = function() {
  this._send('exportToClipboard');
};

scout.Table.prototype.cell = function(column, row) {
  var cell;
  // Row Icon column and cell icon column don't not have cells -> generate one
  if (column === this.rowIconColumn || column === this.checkableColumn) {
    if (column === this.rowIconColumn) {
      cell = {
        iconId: row.iconId,
        cssClass: 'row-icon-cell ' + row.cssClass
      };
    } else if (column === this.checkableColumn) {
      cell = {
        value: row.checked,
        editable: true,
        cssClass: row.cssClass
      };
    }
    scout.defaultValues.applyTo(cell, 'Cell');
    return cell;
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
  return cell.text || '';
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
  if (this.rendered) {
    this._removeAggregateRows(animate);
  }
  this._aggregateRows = [];
};

/**
 * Executes the aggregate function with the given funcName for each column, but only if the Column
 * has that function, which is currently only the case for NumberColumns.
 *
 * @param states is a reference to an Array containig the results for each column.
 * @param row (optional) if set, an additional cell-value parameter is passed to the aggregate function
 */
scout.Table.prototype._forEachColumn = function(funcName, states, row) {
  var value, that = this;
  this.columns.forEach(function(column, i) {
    if (column[funcName]) {
      if (row) {
        value = that.cellValue(column, row);
      }
      states[i] = column[funcName](states[i], value);
    }
  });
};

scout.Table.prototype._group = function(animate) {
  var rows, nextRow, newGroup,
    groupColumns = this._groupedColumns(),
    states = [];

  this.clearAggregateRows();
  if (!groupColumns.length) {
    return;
  }

  rows = this.filteredRows();
  this._forEachColumn('aggrStart', states);
  rows.forEach(function(row, r) {
    this._forEachColumn('aggrStep', states, row);
    // test if sum should be shown, if yes: reset sum-array
    nextRow = rows[r + 1];
    // test if group is finished
    newGroup = (r === rows.length - 1) || this._isNewGroup(groupColumns, row, nextRow);
    // if group is finished: add group row
    if (newGroup) {
      //finish aggregation
      this._forEachColumn('aggrFinish', states);
      //append sum row
      this._addAggregateRow(states, row);
      //reset after group
      this._forEachColumn('aggrStart', states);
    }
  }.bind(this));

  if (this.rendered) {
    this._renderAggregateRows(animate);
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
    newRow = newRow || (hasCellTextForGroupingFunction && col.cellTextForGrouping(row) !== col.cellTextForGrouping(nextRow));
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

scout.Table.prototype._addAggregateRow = function(contents, afterRow) {
  this._aggregateRows.push({
    contents: contents.slice(),
    refRow: afterRow
  });
};

scout.Table.prototype._removeAggregateRows = function(animate) {
  var deferreds = [];
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
  var c, cell, r, column, row, contents, alignment, $cell, $aggregateRow, that = this;
  animate = scout.nvl(animate, false);

  this._aggregateRows.forEach(function(aggregateRow, r) {
    if (aggregateRow.$row) {
      // already rendered, no need to update again (necessary for subsequent renderAggregateRows calls (eg. in insertRows -> renderRows)
      return;
    }
    row = aggregateRow.refRow;
    if (!row.$row) {
      return;
    }

    $aggregateRow = this.$container.makeDiv('table-aggregate-row')
      .data('aggregateRow', aggregateRow);
    contents = aggregateRow.contents;

    for (c = 0; c < this.columns.length; c++) {
      column = this.columns[c];
      if (column instanceof scout.NumberColumn) {
        cell = column.createAggrValueCell(contents[c]);
      } else if (column.grouped) {
        cell = column.createAggrGroupCell(row);
      } else {
        cell = column.createAggrEmptyCell(row);
      }
      $cell = $(column.buildCell(cell, {}));
      $cell.appendTo($aggregateRow);
    }

    $aggregateRow.insertAfter(row.$row).width(this.rowWidth);
    aggregateRow.$row = $aggregateRow;
    if (animate) {
      this._showRow(aggregateRow);
    }
  }, this);
};

scout.Table.prototype.groupColumn = function(column, multiGroup, direction, remove) {
  var data, sorted;

  this._updateSortColumnsForGrouping(column, direction, multiGroup, remove);

  if (this.header) {
    this.header.onSortingChanged();
  }
  sorted = this._sort(true);

  data = {
    columnId: column.id,
    groupingRemoved: remove,
    multiGroup: multiGroup,
    groupAscending: column.sortAscending
  };
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

scout.Table.prototype.checkRow = function(row, checked) {
  if (!this.checkable || !this.enabled || !row.enabled || row.checked === checked) {
    return;
  }
  var updatedRows = [];
  if (!this.multiCheck && checked) {
    for (var i = 0; i < this.rows.length; i++) {
      if (this.rows[i].checked) {
        this.rows[i].checked = false;
        updatedRows.push(this.rows[i]);
        this._renderRowChecked(this.rows[i]);
      }
    }
  }
  row.checked = checked;
  updatedRows.push(row);
  this._sendRowsChecked(updatedRows);
  if (this.rendered) {
    this._renderRowChecked(row);
  }
  this._triggerRowsChecked();
};

scout.Table.prototype.doRowAction = function(row, column) {
  column = column || this.columns[0];
  if (column && column.guiOnly) {
    column = scout.arrays.find(this.columns, function(col) {
      return !col.guiOnly;
    });
  }
  if (!row || !column) {
    return;
  }
  this._sendRowAction(row, column);
};

scout.Table.prototype.insertRow = function(row) {
  this.insertRows([row]);
};

scout.Table.prototype.insertRows = function(rows, fromServer) {
  var filterChanged = false,
    newHiddenRows = [];

  // Update model
  rows.forEach(function(row) {
    this._initRow(row);
    // Always insert new rows at the end, if the order is wrong a rowOrderChange event will follow
    this.rows.push(row);
  }, this);

  this._applyFilters(rows);
  this._calculateValuesForBackgroundEffect();
  fromServer = scout.nvl(fromServer, false);
  if (!fromServer) {
    // If event comes from server, there will be a row order changed event as well -> no sorting necessary
    this._sort();
  }
  this._rebuildingTable = false;

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
  this._triggerRowsInserted(rows);
};

scout.Table.prototype.deleteRow = function(row) {
  this.deleteRows([row]);
};

scout.Table.prototype.deleteRows = function(rows) {
  var invalidate, i, filterChanged;

  rows.forEach(function(row) {
    // Update HTML
    if (this.rendered) {
      // Cancel cell editing if cell editor belongs to a cell of the deleted row
      if (this.cellEditorPopup && this.cellEditorPopup.row.id === row.id) {
        this.cellEditorPopup.cancelEdit();
      }
      // Remove tooltips for the deleted row
      this._removeTooltipsForRow(row);

      this._removeRows(row);
      invalidate = true;
    }

    // Update model
    scout.arrays.remove(this.rows, row);
    if (scout.arrays.remove(this._filteredRows, row)) {
      filterChanged = true;
    }
    delete this.rowsMap[row.id];

    if (this.selectionHandler.lastActionRow === row) {
      this.selectionHandler.clearLastSelectedRowMarker();
    }
  }.bind(this));

  this.deselectRows(rows, false);
  if (filterChanged) {
    this._rowsFiltered();
  }
  this._group();
  this._updateBackgroundEffect();
  this._triggerRowsDeleted(rows);

  if (invalidate) {
    this._renderFiller();
    this._renderViewport();
    this.invalidateLayoutTree();
  }
};

scout.Table.prototype.deleteAllRows = function() {
  var filterChanged = this._filteredRows.length > 0;

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
  this.deselectAll(false);
  this._filteredRows = [];

  if (filterChanged) {
    this._rowsFiltered();
  }
  this._group();
  this._updateBackgroundEffect();
  this._triggerAllRowsDeleted();

  // Update HTML
  if (this.rendered) {
    this._renderFiller();
    this._renderViewport();
    this.invalidateLayoutTree();
  }
};

scout.Table.prototype.updateRows = function(rows) {
  var filterChanged, newHiddenRows = [];

  // Update model
  for (var i = 0; i < rows.length; i++) {
    var updatedRow = rows[i];

    var oldRow = this.rowsMap[updatedRow.id];
    if (!oldRow) {
      throw new Error('Update event received for non existing row. RowId: ' + updatedRow.id);
    }

    // Replace old row
    this._initRow(updatedRow);
    if (this.selectionHandler.lastActionRow === oldRow) {
      this.selectionHandler.lastActionRow = updatedRow;
    }
    var rowIndex = scout.arrays.replace(this.rows, oldRow, updatedRow);
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
        // If filter state has not changed, just update cached rows
        scout.arrays.replace(this._filteredRows, oldRow, updatedRow);
      }
    }

    // Replace old $row
    if (this.rendered && oldRow.$row) {
      // render row and replace div in DOM
      var $updatedRow = $(this._buildRowDiv(updatedRow));
      scout.Table.linkRowToDiv(updatedRow, $updatedRow);
      $updatedRow.copyCssClasses(oldRow.$row, scout.Table.SELECTION_CLASSES);
      oldRow.$row.replaceWith($updatedRow);
      this._installRow(updatedRow);
    }
  }

  if (filterChanged) {
    this._rowsFiltered(newHiddenRows);
  }
  this._group();
  this._updateBackgroundEffect();
};

scout.Table.prototype._removeTooltipsForRow = function(row) {
  for (var i = this.tooltips.length - 1; i >= 0; i--) {
    if (this.tooltips[i].row.id === row.id) {
      this.tooltips[i].remove();
      this.tooltips.splice(i, 1);
    }
  }
};

scout.Table.prototype._startCellEdit = function(column, row, fieldId) {
  var popup = column.startCellEdit(row, fieldId);
  this.cellEditorPopup = popup;
  return popup;
};

scout.Table.prototype.scrollTo = function(row) {
  if (!row.$row) {
    var rowIndex = this.filteredRows().indexOf(row);
    this._renderViewRangeForRowIndex(rowIndex);
  }
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
  scout.scrollbars.scrollTop(this.$data, scrollTop);
  this.scrollTop = scrollTop;

  // call _renderViewport to make sure rows are rendered immediately. The browser fires the scroll event handled by onDataScroll delayed
  this._renderViewport();
};

scout.Table.prototype.revealSelection = function() {
  if (this.selectedRows.length > 0) {
    this.scrollTo(this.selectedRows[0]);
  }
};

scout.Table.prototype.rowById = function(id) {
  return this.rowsMap[id];
};

scout.Table.prototype._rowsByIds = function(ids) {
  return ids.map(this.rowById.bind(this));
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
scout.Table.prototype.renderSelection = function(rows) {
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
    var row = rows[i],
      thisRowSelected = this.selectedRows.indexOf(row) !== -1,
      filteredRows = this.filteredRows(),
      previousIndex = filteredRows.indexOf(row) - 1,
      previousRowSelected = previousIndex >= 0 && this.selectedRows.indexOf(filteredRows[previousIndex]) !== -1,
      followingIndex = filteredRows.indexOf(row) + 1,
      followingRowSelected = followingIndex < filteredRows.length && this.selectedRows.indexOf(filteredRows[followingIndex]) !== -1;

    if (!row.$row) {
      continue;
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

    if (classChanged && previousRowSelected && rows.indexOf(filteredRows[previousIndex]) == -1) {
      rows.push(filteredRows[previousIndex]);
    }
    if (classChanged && followingRowSelected && rows.indexOf(filteredRows[followingIndex]) == -1) {
      rows.push(filteredRows[followingIndex]);
    }
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
    this.renderSelection(row);
    if (this.scrollToSelection) {
      this.revealSelection();
    }
  }

  this._sendRowsPending = true;
  if (!ongoingSelection) {
    this.notifyRowSelectionFinished();
  }
};

scout.Table.prototype.removeRowFromSelection = function(row, ongoingSelection) {
  ongoingSelection = ongoingSelection !== undefined ? ongoingSelection : true;
  if (scout.arrays.remove(this.selectedRows, row)) {
    if (this.rendered) {
      this.renderSelection(row);
    }
    if (!ongoingSelection) {
      this._triggerRowsSelected();
      this._sendRowsSelected(this._rowsToIds(this.selectedRows));
    } else {
      this._sendRowsPending = true;
    }
  }
};

scout.Table.prototype.selectRows = function(rows, notifyServer, debounceSend) {
  rows = scout.arrays.ensure(rows);
  var selectedEqualRows = scout.arrays.equalsIgnoreOrder(rows, this.selectedRows);
  if (selectedEqualRows) {
    return;
  }

  if (this.rendered) {
    this._removeSelection();
  }

  if (!this.multiSelect && rows.length > 1) {
    rows = [rows[0]];
  }

  this.selectedRows = rows;
  notifyServer = scout.nvl(notifyServer, true);
  if (notifyServer) {
    this._sendRowsSelected(this._rowsToIds(rows), debounceSend);
  }
  this._triggerRowsSelected();

  if (this.rendered) {
    this.renderSelection();
    this.selectedRows.forEach(function(row) {

      // Make sure the cell editor popup is correctly layouted because selection changes the cell bounds
      if (this.cellEditorPopup && this.cellEditorPopup.row.id === row.id) {
        this.cellEditorPopup.position();
        this.cellEditorPopup.pack();
      }
    }, this);
    if (this.scrollToSelection) {
      this.revealSelection();
    }
    this._updateMenuBar();
  }
};

scout.Table.prototype.deselectRows = function(rows, notifyServer) {
  rows = scout.arrays.ensure(rows);
  notifyServer = notifyServer !== undefined ? notifyServer : true;
  var selectedRows = this.selectedRows.slice(); // copy
  if (scout.arrays.removeAll(selectedRows, rows)) {
    this.selectRows(selectedRows, notifyServer);
  }
};

scout.Table.prototype.isRowSelected = function(row) {
  return this.selectedRows.indexOf(row) > -1;
};

scout.Table.prototype._filterCount = function() {
  return Object.keys(this._filterMap).length;
};

scout.Table.prototype.filteredRows = function() {
  if (this._filterCount() === 0) {
    return this.rows;
  }
  // filtered rows are cached to avoid unnecessary loops
  if (this._filteredRowsDirty) {
    this._filteredRows = [];
    this.rows.forEach(function(row) {
      if (row.filterAccepted) {
        this._filteredRows.push(row);
      }
    }, this);
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

scout.Table.prototype.columnById = function(columnId) {
  return scout.arrays.find(this.columns, function(column) {
    return column.id === columnId;
  });
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

    return this.rows.indexOf(row1) > this.rows.indexOf(row2);
  }.bind(this));
};

scout.Table.prototype._order$AggregateRows = function($rows) {
  // Find aggregate rows using jquery because
  // this._aggregateRows may be empty but there may be $aggregateRows which are getting removed by animation
  $rows = $rows || this.$aggregateRows();
  $rows.each(function(i, elem) {
    var $aggrRow = $(elem),
      aggregateRow = $aggrRow.data('aggregateRow');

    $aggrRow.insertAfter(aggregateRow.refRow.$row);
  });
};

scout.Table.prototype._rowsFiltered = function(hiddenRows) {
  // non visible rows must be deselected
  this.deselectRows(hiddenRows);
  // notify
  this._filteredRowsDirty = true;
  this._sendRowsFiltered(this._rowsToIds(this.filteredRows()));
  this._triggerRowsFiltered();
};

scout.Table.prototype._rowAcceptedByFilters = function(row) {
  for (var key in this._filterMap) {
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

  rows.forEach(function(row) {
    if (this._applyFiltersForRow(row)) {
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
  for (var key in this._filterMap) {
    var filter = this._filterMap[key];
    filteredBy.push(filter.createLabel());
  }
  return filteredBy;
};

scout.Table.prototype.resetFilter = function() {
  // remove filters
  for (var key in this._filterMap) {
    this.removeFilterByKey(key);
  }
  this._filterMap = {};

  // reset rows
  this.filter();
  this._triggerFilterResetted();
};

/**
 * @param filter object with createKey() and accept()
 */
scout.Table.prototype.addFilter = function(filter, notifyServer) {
  notifyServer = scout.nvl(notifyServer, true);
  var key = filter.createKey();
  if (!key) {
    throw new Error('key has to be defined');
  }
  this._filterMap[key] = filter;

  if (notifyServer && filter instanceof scout.TableUserFilter) {
    this._send('addFilter', filter.createAddFilterEventData());
  }
  this.trigger('addFilter', {
    filter: filter
  });
};

scout.Table.prototype.removeFilter = function(filter, notifyServer) {
  this.removeFilterByKey(filter.createKey(), notifyServer);
};

scout.Table.prototype.removeFilterByKey = function(key, notifyServer) {
  notifyServer = notifyServer !== undefined ? notifyServer : true;
  if (!key) {
    throw new Error('key has to be defined');
  }
  var filter = this._filterMap[key];
  if (!filter) {
    return;
  }
  delete this._filterMap[key];

  if (notifyServer && filter instanceof scout.TableUserFilter) {
    this._send('removeFilter', filter.createRemoveFilterEventData());
  }
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
  this.rows.forEach(function(row) {
    if (!row.$row) {
      row.height = null;
    } else {
      row.height = row.$row.outerHeight(true);
    }
  });
  this._renderFiller();
  this._renderViewport();
  this.updateScrollbars();

  this._triggerColumnResized(column);
  this._sendColumnResized(column);

  if (column.resizingInProgress) {
    this._renderEmptyData();
  } else {
    this._renderEmptyData(this.rowWidth - this.rowBorderWidth);
  }
};

scout.Table.prototype._sendColumnResized = function(column) {
  if (column.fixedWidth || this.autoResizeColumns) {
    return;
  }

  var eventData = {
    columnId: column.id,
    width: column.width
  };

  // send delayed to avoid a lot of requests while resizing
  // coalesce: only send the latest resize event for a column
  this._send('columnResized', eventData, 750, function(previous) {
    return this.id === previous.id && this.type === previous.type && this.columnId === previous.columnId;
  });
};

scout.Table.prototype._sendColumnMoved = function(column, index) {
  var data = {
    columnId: column.id,
    index: index
  };
  this._send('columnMoved', data);
};

scout.Table.prototype._sendColumnBackgroundEffectChanged = function(column) {
  var data = {
    columnId: column.id,
    backgroundEffect: column.backgroundEffect
  };
  this._send('columnBackgroundEffectChanged', data);
};

scout.Table.prototype._sendAggregationFunctionChanged = function(column) {
  var data = {
    columnId: column.id,
    aggregationFunction: column.aggregationFunction
  };
  this._send('aggregationFunctionChanged', data);
};

scout.Table.prototype.moveColumn = function(column, oldPos, newPos, dragged) {
  var index;

  this.columns.forEach(function(iteratingColumn, i) {
    // Don't allow moving a column before the last column with a fixed position (checkbox col, row icon col ...)
    if (iteratingColumn.fixedPosition && newPos <= i) {
      newPos = i + 1;
    }
  });

  scout.arrays.remove(this.columns, column);
  scout.arrays.insert(this.columns, column, newPos);

  index = newPos;
  this.columns.forEach(function(iteratingColumn, i) {
    // Adjust index if column is only known on the gui
    if (iteratingColumn.guiOnly) {
      index--;
    }
  });

  this._triggerColumnMoved(column, oldPos, newPos, dragged);
  this._sendColumnMoved(column, index);

  // move cells
  this._rerenderViewport();
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
  var event = {
    rows: rows
  };
  this.trigger('rowsInserted', event);
};

scout.Table.prototype._triggerRowsDeleted = function(rows) {
  var event = {
    rows: rows
  };
  this.trigger('rowsDeleted', event);
};

scout.Table.prototype._triggerAllRowsDeleted = function() {
  this.trigger('allRowsDeleted');
};

scout.Table.prototype._triggerRowsSelected = function() {
  var rowCount = this.rows.length,
    allSelected = false;

  if (this.selectedRows) {
    allSelected = this.selectedRows.length === rowCount;
  }

  var event = {
    rows: this.selectedRows,
    allSelected: allSelected
  };
  this.trigger('rowsSelected', event);
};

scout.Table.prototype._triggerRowsChecked = function() {
  this.trigger('rowsChecked');
};

scout.Table.prototype._triggerRowsFiltered = function() {
  this.trigger('rowsFiltered');
};

scout.Table.prototype._triggerFilterResetted = function() {
  this.trigger('filterResetted');
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
    column: column.id
  };
  this.trigger('aggregationFunctionChanged', event);
};

scout.Table.prototype._renderHeaderVisible = function() {
  this._renderTableHeader();
};

scout.Table.prototype._renderHeaderEnabled = function() {
  // Rebuild the table header when this property changes
  this._removeTableHeader();
  this._renderTableHeader();
};

scout.Table.prototype._syncCheckable = function(checkable, oldValue) {
  if (checkable === oldValue) {
    // Do nothing if value has not changed (only on property change, not initially)
    return false;
  }
  this.checkable = checkable;

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

scout.Table.prototype._syncHeadAndTailSortColumns = function() {
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

scout.Table.prototype._syncRowIconVisible = function(rowIconVisible, oldValue) {
  if (rowIconVisible === oldValue) {
    // Do nothing if value has not changed (only on property change, not initially)
    return false;
  }
  this.rowIconVisible = rowIconVisible;

  var column = this.rowIconColumn;
  if (this.rowIconVisible && !column) {
    this._insertRowIconColumn();
  } else if (!this.rowIconVisible && column) {
    scout.arrays.remove(this.columns, column);
    this.rowIconColumn = null;
  }
};

scout.Table.prototype._syncSelectedRows = function(selectedRowIds) {
  this.selectRows(this._rowsByIds(selectedRowIds), false);
  this.selectionHandler.clearLastSelectedRowMarker();
};

scout.Table.prototype._syncMenus = function(newMenus, oldMenus) {
  this._keyStrokeSupport.syncMenus(newMenus, oldMenus);
};

scout.Table.prototype._syncKeyStrokes = function(newKeyStrokes, oldKeyStrokes) {
  this._keyStrokeSupport.syncKeyStrokes(newKeyStrokes, oldKeyStrokes);
};

scout.Table.prototype._syncFilters = function(filters) {
  for (var key in this._filterMap) {
    this.removeFilterByKey(key, false);
  }
  if (filters) {
    filters.forEach(function(filterData) {
      if (filterData.column) {
        filterData.column = this.columnById(filterData.column);
      }
      filterData.table = this;
      filterData.session = this.session;
      this.addFilter(scout.create(filterData), false);
    }, this);
  }
};

scout.Table.prototype._renderCheckable = function() {
  this._redraw();
};

scout.Table.prototype._renderRowIconVisible = function() {
  this._redraw();
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
  if (changed) {
    this.invalidateLayoutTree();
  }
};

scout.Table.prototype._removeTableHeader = function() {
  if (this.header) {
    this.header.remove();
    this.removeChild(this.header);
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
    var headerWidth = scout.nvl(width, this.header.$container[0].scrollWidth);
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

scout.Table.prototype._renderTableFooter = function() {
  var footerVisible = this._isFooterVisible(),
    changed = false;
  if (footerVisible && !this.footer) {
    this.footer = this._createFooter();
    this.footer.render();
    changed = true;
  } else if (!footerVisible && this.footer) {
    this._removeTableFooter();
    changed = true;
  }
  if (changed) {
    this.invalidateLayoutTree();
  }
};

scout.Table.prototype._removeTableFooter = function() {
  if (this.footer) {
    this.footer.remove();
    this.removeChild(this.footer);
    this.footer = null;
  }
};

scout.Table.prototype._renderEnabled = function() {
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

scout.Table.prototype._renderMultiSelect = function() {
  // nop
};

scout.Table.prototype._renderMultiCheck = function() {
  // nop
};

scout.Table.prototype._renderMultilineText = function() {
  // nop
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

scout.Table.prototype._renderCssClass = function() {
  var cssClass = 'table';
  if (this.checkableStyle === scout.Table.CheckableStyle.TABLE_ROW) {
    cssClass += ' checkable';
  }
  if (this.cssClass) {
    cssClass += ' ' + this.cssClass;
  }
  this.$container
    .removeAttr('class')
    .addClass(cssClass);
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
 * Calculates the optimal view range size (number of rows to be rendered).
 * It uses the default row height to estimate how many rows fit in the view port.
 * The view range size is this value * 2.
 */
scout.Table.prototype.calculateViewRangeSize = function() {
  // Make sure row height is up to date (row height may be different after zooming)
  this._updateRowHeight();

  if (this.rowHeight === 0) {
    return 0;
    //FIXME CGU/AWE uncomment and remove return as soon as HtmlComp.validateLayout checks for invisible components
    //    throw new Error('Cannot calculate view range with rowHeight = 0');
  }
  return Math.ceil(this.$data.outerHeight() / this.rowHeight) * 2;
};

scout.Table.prototype.setViewRangeSize = function(viewRangeSize) {
  if (this.viewRangeSize === viewRangeSize) {
    return;
  }
  this.viewRangeSize = viewRangeSize;
  if (this.rendered) {
    this._renderViewport();
  }
};

scout.Table.prototype._calculateCurrentViewRange = function() {
  var pos, rowIndex,
    scrollTop = this.$data[0].scrollTop,
    scrollTopDiff = this.scrollTop - scrollTop,
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
    var rowHeight = this.rowHeight;
    if (row.height) {
      rowHeight = row.height;
    }
    height += rowHeight;
    if (scrollTop < height) {
      index = i;
      return true;
    }
  }.bind(this));
  return index;
};

/**
 * Returns a range of size this.viewRangeSize. Start of range is rowIndex - viewRangeSize / 4.
 * -> 1/4 of the rows are before the viewport 2/4 in the viewport 1/4 after the viewport,
 * assuming viewRangeSize is 2*number of possible rows in the viewport (see calculateViewRangeSize).
 */
scout.Table.prototype._calculateViewRangeForRowIndex = function(rowIndex) {
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
  var viewRange = this._calculateCurrentViewRange(),
    firstRow, lastRow;

  if (this.viewRangeRendered.size() > 0) {
    firstRow = this.filteredRows()[this.viewRangeRendered.from],
    lastRow = this.filteredRows()[this.viewRangeRendered.to - 1];
    firstRow.$row.removeClass('first');
    lastRow.$row.removeClass('last');
  }

  this._renderViewRange(viewRange);

  if (this.viewRangeRendered.size() > 0) {
    firstRow = this.filteredRows()[this.viewRangeRendered.from],
    lastRow = this.filteredRows()[this.viewRangeRendered.to - 1];
    firstRow.$row.addClass('first');
    lastRow.$row.addClass('last');
  }
};

scout.Table.prototype._rerenderViewport = function() {
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
  var scrollTop = this.$data[0].scrollTop;
  var rangesToRender = viewRange.subtract(this.viewRangeRendered);
  var rangesToRemove = this.viewRangeRendered.subtract(viewRange);
  rangesToRemove.forEach(function(range) {
    this._removeRowsInRange(range);
  }.bind(this));
  rangesToRender.forEach(function(range) {
    this._renderRowsInRange(range);
  }.bind(this));

  // check if at least last and first row in range got correctly rendered
  if (this.viewRangeRendered.size > 0) {
    var rows = this.filteredRows();
    var firstRow = rows[this.viewRangeRendered.from].$row;
    var lastRow = rows[this.viewRangeRendered.to].$row;
    if (!firstRow.$row || !lastRow.$row) {
      throw new Error('Rows not rendered as expected. ' + this.viewRangeRendered + '. First: ' + firstRow.$row + '. Last: ' + lastRow.$row);
    }
  }

  this._removeAggregateRows();
  this._renderAggregateRows();
  this._renderFiller();
  this._renderEmptyData();
  this._renderBackgroundEffect();
  this.renderSelection();
  this.$data[0].scrollTop = scrollTop;
  this.viewRangeDirty = false;
};

scout.Table.prototype._renderFiller = function() {
  var fakeRowStyle;
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
    var row = this.filteredRows()[i],
      height = this.rowHeight;

    if (row.height) {
      height = row.height;
    }
    totalHeight += height;
  }
  return totalHeight;
};

scout.Table.prototype.containsNumberColumn = function() {
  return this.columns.some(function(column) {
    return column instanceof scout.NumberColumn;
  });
};

scout.Table.prototype._onRowsInserted = function(rows) {
  this.insertRows(rows, true);
};

scout.Table.prototype._onRowsDeleted = function(rowIds) {
  var rows = this._rowsByIds(rowIds);
  this.deleteRows(rows);
};

scout.Table.prototype._onAllRowsDeleted = function() {
  this.deleteAllRows();
};

scout.Table.prototype._onRowsUpdated = function(rows) {
  this.updateRows(rows);
};

scout.Table.prototype._onRowsSelected = function(rowIds) {
  this._syncSelectedRows(rowIds);
};

scout.Table.prototype._onRowsChecked = function(rows) {
  for (var i = 0; i < rows.length; i++) {
    var row = this.rowsMap[rows[i].id];
    row.checked = rows[i].checked;
    if (this.rendered) {
      this._renderRowChecked(row);
    }
    this._triggerRowsChecked();
  }
};

scout.Table.prototype._onRowOrderChanged = function(rowIds) {
  var newPos, rows, row;
  if (rowIds.length !== this.rows.length) {
    throw new Error('Row order changed event may not be processed because lengths of the arrays differ.');
  }

  // update model
  rows = scout.arrays.init(this.rows.length, 0);
  for (var i = 0; i < this.rows.length; i++) {
    row = this.rows[i];
    newPos = rowIds.indexOf(this.rows[i].id);
    rows[newPos] = row;
  }
  this.rows = rows;
  this._filteredRowsDirty = true; // order has been changed

  this.clearAggregateRows(this._animateAggregateRows);
  if (this.rendered) {
    this._renderRowOrderChanges();
  }
  this._triggerRowOrderChanged();

  this._group(this._animateAggregateRows);
  this._animateAggregateRows = false;
};

/**
 * Rebuilds the header.<br>
 * Does not modify the rows, it expects a deleteAll and insert event to follow which will do the job.
 */
scout.Table.prototype._onColumnStructureChanged = function(columns) {
  this._rebuildingTable = true;

  this.columns = columns;
  this._initColumns();

  if (this.rendered) {
    this._updateRowWidth();
    this.$rows(true).css('width', this.rowWidth);

    this._rerenderHeaderColumns();
  }
  this.trigger('columnStructureChanged');
};

scout.Table.prototype._onColumnOrderChanged = function(columnIds) {
  var i, column, columnId, currentPosition, oldColumnOrder;
  if (columnIds.length !== this.columns.length) {
    throw new Error('Column order changed event may not be processed because lengths of the arrays differ.');
  }

  oldColumnOrder = this.columns.slice();

  for (i = 0; i < columnIds.length; i++) {
    columnId = columnIds[i];
    column = this.columnById(columnId);
    currentPosition = this.columns.indexOf(column);
    if (currentPosition < 0) {
      throw new Error('Column with id ' + columnId + 'not found.');
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
scout.Table.prototype._onColumnHeadersUpdated = function(columns) {
  var column, oldColumnState;

  //Update model columns
  for (var i = 0; i < columns.length; i++) {
    scout.defaultValues.applyTo(columns[i], 'Column');
    column = this.columnById(columns[i].id);
    oldColumnState = $.extend(oldColumnState, column);
    column.text = columns[i].text;
    column.headerCssClass = columns[i].headerCssClass;
    column.sortActive = columns[i].sortActive;
    column.sortAscending = columns[i].sortAscending;

    if (this.rendered && this.header) {
      this.header.updateHeader(column, oldColumnState);
    }
  }
};

scout.Table.prototype._onStartCellEdit = function(columnId, rowId, fieldId) {
  var column = this.columnById(columnId),
    row = this.rowById(rowId);
  this._startCellEdit(column, row, fieldId);
};

scout.Table.prototype._onEndCellEdit = function(fieldId) {
  var field = this.session.getModelAdapter(fieldId);

  // Remove the cell-editor popup prior destroying the field, so that the 'cell-editor-popup's focus context is uninstalled first and the focus can be restored onto the last focused element of the surrounding focus context.
  // Otherwise, if the currently focused field is removed from DOM, the $entryPoint would be focused first, which can be avoided if removing the popup first.
  this.cellEditorPopup.remove();
  this.cellEditorPopup = null;

  field.destroy();
};

scout.Table.prototype._onRequestFocus = function() {
  this.session.focusManager.requestFocus(this.$container);
};

scout.Table.prototype._onScrollToSelection = function() {
  this.revealSelection();
};

scout.Table.prototype._onColumnBackgroundEffectChanged = function(event) {
  var columnId, column, effect;
  event.eventParts.forEach(function(eventPart) {
    columnId = eventPart.columnId;
    column = this.columnById(columnId);
    column.setBackgroundEffect(eventPart.backgroundEffect, false);
  }, this);
};

scout.Table.prototype._onRequestFocusInCell = function(event) {
  var row = this.rowById(event.rowId),
    column = this.columnById(event.columnId),
    cell = this.cell(column, row);
  if (this.enabled && row.enabled && cell.editable) {
    this.prepareCellEdit(event.rowId, event.columnId, true);
  }
};

scout.Table.prototype._onAggregationFunctionChanged = function(event) {
  var columnId, column, func;

  event.eventParts.forEach(function(eventPart) {
    columnId = eventPart.columnId;
    func = eventPart.aggregationFunction;
    column = this.columnById(columnId);
    column.setAggregationFunction(func);

    this._triggerAggregationFunctionChanged(column);
  }, this);

  this._group();
};

scout.Table.prototype._onFiltersChanged = function(filters) {
  this._syncFilters(filters);
  // do not refilter while the table is being rebuilt (because column.index in filter and row.cells may be inconsistent)
  if (!this._rebuildingTable) {
    this.filter();
  }
};

scout.Table.prototype.onModelAction = function(event) {
  // _renderRows() might not have drawn all rows yet, therefore postpone the
  // execution of this method to prevent conflicts on the row objects.
  if (this._renderRowsInProgress) {
    var that = this;
    setTimeout(function() {
      that.onModelAction(event);
    }, 0);
    return;
  }

  if (event.type === 'rowsInserted') {
    this._onRowsInserted(event.rows);
  } else if (event.type === 'rowsDeleted') {
    this._onRowsDeleted(event.rowIds);
  } else if (event.type === 'allRowsDeleted') {
    this._onAllRowsDeleted();
  } else if (event.type === 'rowsSelected') {
    this._onRowsSelected(event.rowIds);
  } else if (event.type === 'rowOrderChanged') {
    this._onRowOrderChanged(event.rowIds);
  } else if (event.type === 'rowsUpdated') {
    this._onRowsUpdated(event.rows);
  } else if (event.type === 'filtersChanged') {
    this._onFiltersChanged(event.filters);
  } else if (event.type === 'rowsChecked') {
    this._onRowsChecked(event.rows);
  } else if (event.type === 'columnStructureChanged') {
    this._onColumnStructureChanged(event.columns);
  } else if (event.type === 'columnOrderChanged') {
    this._onColumnOrderChanged(event.columnIds);
  } else if (event.type === 'columnHeadersUpdated') {
    this._onColumnHeadersUpdated(event.columns);
  } else if (event.type === 'startCellEdit') {
    this._onStartCellEdit(event.columnId, event.rowId, event.fieldId);
  } else if (event.type === 'endCellEdit') {
    this._onEndCellEdit(event.fieldId);
  } else if (event.type === 'requestFocus') {
    this._onRequestFocus();
  } else if (event.type === 'scrollToSelection') {
    this._onScrollToSelection();
  } else if (event.type === 'aggregationFunctionChanged') {
    this._onAggregationFunctionChanged(event);
  } else if (event.type === 'columnBackgroundEffectChanged') {
    this._onColumnBackgroundEffectChanged(event);
  } else if (event.type === 'requestFocusInCell') {
    this._onRequestFocusInCell(event);
  } else if (event.type === 'columnActionsChanged') {
    this._onColumnActionsChanged(event);
  } else {
    scout.Table.parent.prototype.onModelAction.call(this, event);
  }
};

scout.Table.prototype._onColumnActionsChanged = function(event) {
  this.header.onColumnActionsChanged(event);
};

/**
 * Method invoked when this is a 'detailTable' and the outline content is displayed.
 * @override Widget.js
 */
scout.Table.prototype._attach = function() {
  this._$parent.append(this.$container);
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
  scout.Table.parent.prototype._detach.call(this);
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
