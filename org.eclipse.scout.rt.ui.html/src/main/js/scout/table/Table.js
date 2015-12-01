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
  this._blockLoadThreshold = 100;
  this.menuBar;
  this._renderRowsInProgress = false;
  this._drawDataInProgress = false;
  this._doubleClickSupport = new scout.DoubleClickSupport();
  this._addAdapterProperties(['tableControls', 'menus', 'keyStrokes']);

  this._permanentHeadSortColumns = [];
  this._permanentTailSortColumns = [];
  this._filterMenusHandler = this._filterMenus.bind(this);
};
scout.inherits(scout.Table, scout.ModelAdapter);

scout.Table.prototype._init = function(model) {
  scout.Table.parent.prototype._init.call(this, model);

  this._initColumns();
  this.rows.forEach(function(row) {
    this._initRow(row);
  }, this);

  this.menuBar = scout.create(scout.MenuBar, {
    parent: this,
    menuOrder: new scout.MenuItemsOrder(this.session, 'Table')
  });
  this.menuBar.bottom();

  this._syncSelectedRows(this.selectedRows);
  this._syncFilters(this.filters);
  this._syncKeyStrokes(this.keyStrokes);
  this._syncMenus(this.menus);
  this._calculateValuesForBackgroundEffect();
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

scout.Table.prototype._insertCheckBoxColumn = function() {
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

  this.dragAndDropHandler = scout.dragAndDrop.handler(this,
    scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER,
    function() {
      return this.dropType;
    }.bind(this),
    function() {
      return this.dropMaximumSize;
    }.bind(this),
    function(event) {
      var row = this._rowAtY(event.originalEvent.pageY);
      return {
        'rowId': (row ? row.id : '')
      };
    }.bind(this));
  this.dragAndDropHandler.install(this.$data);

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
  this._renderRows();

  //----- inline methods: --------

  var $mouseDownRow, mouseDownColumn, that = this;

  function onMouseDown(event) {
    that._doubleClickSupport.mousedown(event);
    $mouseDownRow = $(event.currentTarget);
    mouseDownColumn = that._columnAtX(event.pageX);
    that.selectionHandler.onMouseDown(event);
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

    if ($mouseDownRow && $mouseUpRow && $mouseDownRow.attr('data-rowid') !== $mouseUpRow.attr('data-rowid')) {
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
      menuItems = this._filterMenus(this.menus, 'contextMenu', true); //TODO nbu move to menu ->filterFunc
      if (!event.pageX && !event.pageY) {
        var $rowToDisplay = this.selectionHandler.lastActionRow ? this.selectionHandler.lastActionRow.$row : this.selectedRows[this.selectedRows.length - 1].$row;
        var offset = $rowToDisplay.offset();
        event.pageX = offset.left + 10;
        event.pageY = offset.top + $rowToDisplay.outerHeight() / 2;
      }
      if (menuItems.length > 0) {
        popup = scout.create(scout.ContextMenuPopup, {
          parent: this,
          menuItems: menuItems,
          location: {
            x: event.pageX,
            y: event.pageY
          },
          $anchor: this.$data
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
};

scout.Table.prototype._remove = function() {
  scout.scrollbars.uninstall(this.$data, this.session);
  // FIXME CGU do not delete header and footer!
  this.header = null;
  this.footer = null;
  this._removeAggregateRows();
  this._uninstallCellTooltipSupport();
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
  return scout.create(scout.TableHeader, {
    parent: this,
    table: this,
    enabled: this.headerEnabled
  });
};

scout.Table.prototype._createFooter = function() {
  return scout.create(scout.TableFooter, {
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
    cellIndex = $cell.index(),
    row = $row.data('row');

  if (row) {
    cell = this.cellByCellIndex(cellIndex, row);
    tooltipText = cell.tooltipText;
  }

  if (tooltipText) {
    return tooltipText;
  } else if ($cell.isContentTruncated()) {
    return $cell.text();
  }
};

scout.Table.prototype.reload = function() {
  this._sendReload();
};

scout.Table.prototype.exportToClipboard = function() {
  this._sendExportToClipboard();
};

scout.Table.prototype.clearSelection = function(dontFire) {
  this.selectedRows.forEach(function(row) {
    if (row.$row) {
      row.$row.select(false);
      row.$row.toggleClass('select-middle select-top select-bottom select-single selected', false);
    }
  });
  this.selectedRows = [];
  if (!dontFire) {
    this._sendRowsPending = true;
    this.notifyRowSelectionFinished();
  }
};

scout.Table.prototype.toggleSelection = function() {
  if (this.selectedRows.length === this.rows.length) {
    this.clearSelection();
  } else {
    this.selectAll();
  }
};

scout.Table.prototype.selectAll = function() {
  //TODO nbu async? only select visible rows and then others.
  if (!this.multiSelect) {
    return; // not possible
  }
  this.clearSelection(true);
  var rows = this.filteredRows();
  rows.forEach(function(row) {
    this.addRowToSelection(row, true);
  }, this);
  this._sendRowsPending = true;
  this.notifyRowSelectionFinished();
};

scout.Table.prototype.updateScrollbars = function() {
  scout.scrollbars.update(this.$data);
};

scout.Table.prototype._sort = function() {
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

  if (!sortColumns.length) {
    // no sort column defined. nop
    return true;
  }

  // compare rows
  function compare(row1, row2) {
    for (var s = 0; s < sortColumns.length; s++) {
      column = sortColumns[s];
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
  this._filteredRowsDirty = true; // order has been changed

  //Sort was possible -> return true
  return true;
};

scout.Table.prototype._prepareColumnsForSorting = function(sortColumns) {
  var collator, column;

  var textComparator = function(valueA, valueB) {
    return collator.compare(valueA, valueB);
  };

  var defaultComparator = function(valueA, valueB) {
    if (valueA < valueB) {
      return -1;
    } else if (valueA > valueB) {
      return 1;
    }
    return 0;
  };

  // initialize comparators
  for (var c = 0; c < sortColumns.length; c++) {
    column = sortColumns[c];

    if (!column.uiSortPossible) {
      return false;
    }

    if (column.type === 'text') {
      if (!scout.device.supportsInternationalization()) {
        //Locale comparison not possible -> do it on server
        return false;
      }

      if (!collator) {
        collator = new window.Intl.Collator(this.session.locale.languageTag);
      }
      column.compare = textComparator;
    } else {
      column.compare = defaultComparator;
    }
  }

  return true;
};

scout.Table.prototype._renderRowOrderChanges = function() {
  var $row, oldTop, i, rowWasInserted, animate,
    that = this,
    $rows = this.$rows(),
    $sortedRows = $();

  //store old position
  if ($rows.length < that._animationRowLimit) {
    $rows.each(function() {
      $row = $(this);

      //Prevent the order animation for newly inserted rows (to not confuse the user)
      rowWasInserted = false;
      for (var i in that._insertedRows) {
        if (that._insertedRows[i].id === $row.data('row').id) {
          rowWasInserted = true;
          break;
        }
      }

      if (!rowWasInserted) {
        animate = true;
        $row.data('old-top', $row.offset().top);
      }
    });
  }

  for (i = 0; i < this.rows.length; i++) {
    $row = this.rows[i].$row;
    $sortedRows.push($row[0]);
  }

  // change order in dom
  this.$data.prepend($sortedRows);

  // for less than animationRowLimit rows: move to old position and then animate
  if (animate) {
    $rows.each(function() {
      $row = $(this);
      oldTop = $row.data('old-top');
      if (oldTop !== undefined) {
        $row.css('top', oldTop - $row.offset().top).animate({
          top: 0
        }, {
          progress: that._triggerRowOrderChanged.bind(that, $row.data('row'), true)
        });
      }
    });
  }

  this.renderSelection();
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
  sorted = this._sort();

  data = {
    columnId: column.id,
    sortingRemoved: remove,
    multiSort: multiSort,
    sortAscending: column.sortAscending
  };
  if (sorted) {
    this._send('rowsSorted', data);
    this._triggerRowOrderChanged();
    this.clearAggregateRows(animateAggregateRows);
    if (this.rendered) {
      this._renderRowOrderChanges();
    }
    this._group();
  } else {
    // Delegate sorting to server when it is not possible on client side
    this._send('sortRows', data);
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
    //no permanent head sort columns. grouping ok.
    return true;
  }

  if (!column.initialAlwaysIncludeSortAtBegin && !column.initialAlwaysIncludeSortAtEnd) {
    //col itself is not a head or tail sort column. therefore, all head sort columns must be grouped.
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

scout.Table.prototype._buildRowDiv = function(row, rowSelected, previousRowSelected, followingRowSelected) {
  var rowWidth = this.rowWidth;
  var rowClass = 'table-row';
  if (rowSelected) {
    rowClass += ' selected';
    if (previousRowSelected && followingRowSelected) {
      rowClass += ' select-middle';
    } else if (!previousRowSelected && followingRowSelected) {
      rowClass += ' select-top';
    } else if (!previousRowSelected && !followingRowSelected) {
      rowClass += ' select-single';
    } else if (previousRowSelected && !followingRowSelected) {
      rowClass += ' select-bottom';
    }
  }
  if (!row.enabled) {
    rowClass += ' disabled';
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

/**
 * @param new rows to append at the end of this.$data. If undefined this.rows is used.
 */
scout.Table.prototype._renderRows = function(rows, startRowIndex, lastRowOfBlockSelected) {
  var $rows, numRowsLoaded,
    rowString = '',
    that = this;

  lastRowOfBlockSelected = lastRowOfBlockSelected ? lastRowOfBlockSelected : false;
  startRowIndex = startRowIndex !== undefined ? startRowIndex : 0;
  rows = rows || this.rows;
  numRowsLoaded = startRowIndex;
  if (rows.length > 0) {
    this._removeEmptyData();

    // Build $rows (as string instead of jQuery objects for efficiency reasons)
    var previousRowSelected = false,
      followingRowSelected = false;
    for (var r = startRowIndex; r < Math.min(rows.length, startRowIndex + this._blockLoadThreshold); r++) {
      var row = rows[r],
        rowSelected = this.selectedRows.indexOf(row) > -1;

      if (r === startRowIndex) {
        previousRowSelected = lastRowOfBlockSelected;
      } else {
        previousRowSelected = this.selectedRows.indexOf(rows[r - 1]) > -1;
      }
      if (r < Math.min(rows.length, startRowIndex + this._blockLoadThreshold) - 1) {
        followingRowSelected = this.selectedRows.indexOf(rows[r + 1]) > -1;
      } else {
        followingRowSelected = false;
      }
      rowString += this._buildRowDiv(row, rowSelected, previousRowSelected, followingRowSelected);
    }
    numRowsLoaded = r;

    // append block of rows
    $rows = this.$data.appendElement(rowString);

    // Link model and jQuery objects and render selection borders
    $rows.each(function(index, rowObject) {
      var $row = $(rowObject);
      var row = rows[startRowIndex + index];
      scout.Table.linkRowToDiv(row, $row);
      lastRowOfBlockSelected = $row.isSelected();
    });

    // Apply row filters
    // This cannot be done in the above loop because the filter calculates the cube which calls column.cellValueForGrouping for every row
    // -> depending on the implementation row.$row has to exist (see BeanColumn.js)
    // This cannot be done in install rows as well, because the notification handling differs when rows are updated
    if (this._filterCount() > 0) {
      this._applyFilters($rows);
    }

    this._installRows($rows);

    // notify
    this._triggerRowsSelected();

    if (this.scrollToSelection) {
      // Execute delayed because table may be not layouted yet
      setTimeout(this.revealSelection.bind(this));
    }
  }

  // repaint and append next block
  this._renderRowsInProgress = false;
  if (rows.length > numRowsLoaded) {
    this._renderRowsInProgress = true;
    setTimeout(function() {
      that._renderRows(rows, startRowIndex + that._blockLoadThreshold, lastRowOfBlockSelected);
      // Manual validation necessary due to set timeout
      that.validateLayoutTree();
    }, 0);
  } else {
    // When all blocks are rendered, render the aggregate rows
    // Grouping cannot be done in init() because row filter only works with rendered rows -> needs to be done always when rows get rendered
    this._group();
    this._renderAggregateRows();
    this._renderBackgroundEffect();
  }
};

scout.Table.prototype._removeRows = function($rows) {
  $rows = $rows || this.$rows(true);
  $rows.remove();
  this._renderEmptyData();
};

/**
 * This method should be used after the $rows are added to the DOM (new rows, updated rows). The '$rows'
 * are expected to be linked with the corresponding 'rows' (row.$row and $row.data('row')).
 */
scout.Table.prototype._installRows = function($rows) {
  var newInvisibleRows = [],
    that = this;

  $rows.each(function(entry, index, $rows) {
    var editorField,
      $row = $(this),
      row = $row.data('row');

    that._removeTooltipsForRow(row);
    if (row.hasError) {
      that._showCellErrorForRow(row);
    }
    // Reopen editor popup with cell because state of popup (row, $anchor etc.) is not valid anymore
    if (that.cellEditorPopup && that.cellEditorPopup.row.id === row.id) {
      that.cellEditorPopup.remove();
      editorField = that.cellEditorPopup.cell.field;
      that._startCellEdit(that.cellEditorPopup.column, row, editorField.id);
    }
  });
  this.invalidateLayoutTree();
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
  tooltip = scout.create(scout.TableTooltip, opts);
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

/**
 * @returns the row at position y (e.g. from event.pageY)
 */
scout.Table.prototype._rowAtY = function(y) {
  var rowOffsetBottom = 0,
    rowOffsetTop = this.$data.offset().top,
    scrollTop = this.$data.scrollTop();

  rowOffsetTop -= scrollTop;
  return scout.arrays.find(this.rows, function(row) {
    rowOffsetBottom = rowOffsetTop + row.$row.height();
    if (rowOffsetTop >= 0 && y >= rowOffsetTop && y < rowOffsetBottom) {
      return true;
    }
    rowOffsetTop = rowOffsetBottom;
  });
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
  var allowedTypes = [];
  if (destination === 'menuBar') {
    allowedTypes = ['Table.EmptySpace', 'Table.SingleSelection', 'Table.MultiSelection'];
  } else if (destination === 'contextMenu') {
    allowedTypes = ['Table.SingleSelection', 'Table.MultiSelection'];
  } else if (destination === 'header') {
    allowedTypes = ['Table.Header'];
  }

  if (allowedTypes.indexOf('Table.SingleSelection') > -1 && this.selectedRows.length !== 1) {
    scout.arrays.remove(allowedTypes, 'Table.SingleSelection');
  }
  if (allowedTypes.indexOf('Table.MultiSelection') > -1 && this.selectedRows.length <= 1) {
    scout.arrays.remove(allowedTypes, 'Table.MultiSelection');
  }
  return scout.menus.filter(menus, allowedTypes, onlyVisible, enableDisableKeyStroke);
};

scout.Table.prototype._renderMenus = function() {
  this._updateMenuBar();
  if (this.header) {
    this.header.updateMenuBar();
  }
};

scout.Table.prototype._updateMenuBar = function() {
  var menuItems = this._filterMenus(this.menus, 'menuBar', false, true);
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
  if (this.hasReloadHandler) {
    this.$data.empty();
    // scoll bar must be (re)installed after all content has been removed (because also scrollbars are removed)..
    scout.scrollbars.install(this.$data, {
      parent: this,
      axis: 'both'
    });
    this._send('reload');
  }
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
    if (!row.$row.isVisible()) {
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
  this._aggregateRows.length = 0;
};

scout.Table.prototype._group = function(animate) {
  var alignment, rows, states, columnGroupingActive, value, nextRow, newGroup,
    that = this,
    groupColumns = this._groupedColumns();

  this.clearAggregateRows();

  columnGroupingActive = (groupColumns ? groupColumns.length > 0 : false);
  if (!columnGroupingActive) {
    return;
  }

  // prepare data
  rows = this.filteredRows();
  states = [];

  // pre-define functions for inline use.
  var prepare = function(column, c) {
    if (column.type === 'number') {
      states[c] = column.aggrStart();
    }
  };

  var aggregate = function(row, column, c) {
    if (column.type === 'number') {
      value = this.cellValue(column, row);
      states[c] = column.aggrStep(states[c], value);
    }
  };

  var finish = function(column, c) {
    if (column.type === 'number') {
      states[c] = column.aggrFinish(states[c]);
    }
  };

  // prepare columns
  this.columns.forEach(prepare);

  rows.forEach(function(row, r) {

    this.columns.forEach(aggregate.bind(this, row));

    // test if sum should be shown, if yes: reset sum-array
    nextRow = rows[r + 1];

    // test if group is finished
    newGroup = (r === rows.length - 1) || this._isNewGroup(groupColumns, row, nextRow);
    // if group is finished: add group row

    if (newGroup) {
      //finish aggregation
      this.columns.forEach(finish);
      //append sum row
      this._addAggregateRow(states, row);
      //reset after group
      this.columns.forEach(prepare);
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
    row: afterRow
  });
};

scout.Table.prototype._removeAggregateRows = function(animate) {
  var deferreds = [];
  animate = scout.nvl(animate, false);
  if (!animate) {
    this._aggregateRows.forEach(function(aggregateRow) {
      if (!aggregateRow.$aggregateRow) {
        return;
      }
      aggregateRow.$aggregateRow.remove();
      aggregateRow.$aggregateRow = null;
    }, this);
    this.renderSelection();
    this.updateScrollbars();
  } else {
    this._aggregateRows.forEach(function(aggregateRow, i) {
      if (!aggregateRow.$aggregateRow) {
        return;
      }

      var deferred = $.Deferred();
      deferreds.push(deferred);

      aggregateRow.$aggregateRow.slideUp({
        duration: 200,
        progress: this.updateScrollbars.bind(this),
        complete: function() {
          aggregateRow.$aggregateRow.remove();
          aggregateRow.$aggregateRow = null;
          deferred.resolve();
        }.bind(this)
      });
    }, this);

    // when all animations have been finished
    if (deferreds.length > 0) {
      $.when.apply($, deferreds).done(function() {
        this.renderSelection();
      }.bind(this));
    }
  }
};

scout.Table.prototype._renderAggregateRows = function(animate) {
  var c, cell, r, column, row, contents, alignment, $cell, $aggregateRow, that = this;
  animate = scout.nvl(animate, false);

  this._aggregateRows.forEach(function(aggregateRow, r) {
    if (aggregateRow.$aggregateRow) {
      // already rendered, no need to update again (necessary for subsequent renderAggregateRows calls (eg. in insertRows -> renderRows)
      return;
    }
    $aggregateRow = this.$container.makeDiv('table-aggregate-row');
    row = aggregateRow.row;
    contents = aggregateRow.contents;

    for (c = 0; c < this.columns.length; c++) {
      column = this.columns[c];
      if (typeof contents[c] === 'number') {
        var aggrValue = contents[c];
        if (column.format) {
          var decimalFormat = new scout.DecimalFormat(this.session.locale, column.format);
          aggrValue = decimalFormat.format(aggrValue);
        }
        cell = {
          text: aggrValue,
          iconId: column.aggrSymbol,
          horizontalAlignment: column.horizontalAlignment,
          cssClass: 'table-aggregate-cell'
        };
      } else if (column.grouped) {
        cell = {
          // value necessary for value based columns (e.g. checkbox column)
          value: this.cellValue(column, row),
          text: column.cellTextForGrouping(row),
          horizontalAlignment: column.horizontalAlignment,
          cssClass: 'table-aggregate-cell'
        };
      } else {
        cell = {
          empty: true
        };
      }

      $cell = $(column.buildCell(cell, {}));
      $cell.appendTo($aggregateRow);
    }

    $aggregateRow.insertAfter(row.$row).width(this.rowWidth);
    aggregateRow.$aggregateRow = $aggregateRow;
    if (animate) {
      $aggregateRow
        .hide()
        .slideDown({
          duration: 200,
          progress: this.updateScrollbars.bind(this)
        });
    }
  }, this);
};

scout.Table.prototype.groupColumn = function(column, multiGroup, direction, remove) {
  var data, sorted;

  this._updateSortColumnsForGrouping(column, direction, multiGroup, remove);

  if (this.header) {
    this.header.onSortingChanged();
  }
  sorted = this._sort();

  data = {
    columnId: column.id,
    groupingRemoved: remove,
    multiGroup: multiGroup,
    groupAscending: column.sortAscending
  };
  if (sorted) {
    this._send('rowsGrouped', data);
    this._triggerRowOrderChanged();
    this.clearAggregateRows(true);
    if (this.rendered) {
      this._renderRowOrderChanges();
    }
    this._group(true);
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
  if (!this.checkableColumn) {
    throw new Error('checkableColumn not set');
  }

  var $checkbox = this.checkableColumn.$checkBox(row.$row);
  $checkbox.toggleClass('checked', row.checked);
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

scout.Table.prototype._insertRow = function(row) {
  this._insertRows([row]);
};

scout.Table.prototype._insertRows = function(rows) {
  var filterChanged = false;
  // Update model
  for (var i = 0; i < rows.length; i++) {
    var row = rows[i];
    this._initRow(row);
    // Always insert new rows at the end, if the order is wrong a rowOrderChange event will follow
    this.rows.push(row);
  }
  this._calculateValuesForBackgroundEffect();

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

    this._renderRows(rows);
  }
  this._triggerRowsInserted(rows);
};

scout.Table.prototype._deleteRow = function(row) {
  this._deleteRows([row]);
};

scout.Table.prototype._deleteRows = function(rows) {
  var invalidate, i, filterChanged;

  this.deselectRows(rows, false);
  rows.forEach(function(row) {
    // Update model
    scout.arrays.remove(this.rows, row);
    if (scout.arrays.remove(this._filteredRows, row)) {
      filterChanged = true;
    }
    delete this.rowsMap[row.id];

    if (this.selectionHandler.lastActionRow === row) {
      this.selectionHandler.clearLastSelectedRowMarker();
    }

    // Update HTML
    if (this.rendered) {
      // Cancel cell editing if cell editor belongs to a cell of the deleted row
      if (this.cellEditorPopup && this.cellEditorPopup.row.id === row.id) {
        this.cellEditorPopup.cancelEdit();
      }
      // Remove tooltips for the deleted row
      this._removeTooltipsForRow(row);

      this._removeRows(row.$row);
      delete row.$row;
      invalidate = true;
    }
  }.bind(this));

  if (filterChanged) {
    this._rowsFiltered();
  }
  this._group();
  this._updateBackgroundEffect();
  this._triggerRowsDeleted(rows);
  if (invalidate) {
    this.invalidateLayoutTree();
  }
};

scout.Table.prototype._updateRows = function(rows) {
  var filterChanged, newInvisibleRows = [],
    $updatedRows = $();

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

    // Replace old $row
    if (this.rendered && oldRow.$row) {
      var rowSelected = this.selectedRows.indexOf(updatedRow) > -1,
        previousRowSelected = false,
        followingRowSelected = false;

      if (rowIndex > 0) {
        previousRowSelected = this.selectedRows.indexOf(this.rows[rowIndex - 1]) > -1;
      }
      if (rowIndex + 1 < this.rows.length) {
        followingRowSelected = this.selectedRows.indexOf(this.rows[rowIndex + 1]) > -1;
      }

      // render row and replace div in DOM
      var $updatedRow = $(this._buildRowDiv(updatedRow, rowSelected, previousRowSelected, followingRowSelected));
      scout.Table.linkRowToDiv(updatedRow, $updatedRow);
      oldRow.$row.replaceWith($updatedRow);
      $updatedRows = $updatedRows.add($updatedRow);

      // Apply row filter
      updatedRow.filterAccepted = oldRow.filterAccepted;
      if (this._filterCount() > 0) {
        if (this._applyFiltersForRow(updatedRow)) {
          filterChanged = true;
          if (!updatedRow.filterAccepted) {
            newInvisibleRows.push(updatedRow);
          }
        } else {
          // If filter state has not changed, just update cached rows
          scout.arrays.replace(this._filteredRows, oldRow, updatedRow);
        }
        this._renderRowFilterAccepted(updatedRow);
      }
    }
  }

  if ($updatedRows.length > 0) {
    this._installRows($updatedRows);
  }
  if (filterChanged) {
    this._rowsFiltered(newInvisibleRows);
  }
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

scout.Table.prototype._deleteAllRows = function() {
  var filterChanged = this._filteredRows.length > 0;

  // Update model
  this.rows = [];
  this.rowsMap = {};
  this.selectRows([], false);
  this._filteredRows = [];

  if (filterChanged) {
    this._rowsFiltered();
  }

  // Update HTML
  if (this.rendered) {
    // Cancel cell editing
    if (this.cellEditorPopup) {
      this.cellEditorPopup.cancelEdit();
    }

    this.selectionHandler.clearLastSelectedRowMarker();
    this._removeRows();
    this.invalidateLayoutTree();
  }
  this._triggerAllRowsDeleted();
};

scout.Table.prototype._startCellEdit = function(column, row, fieldId) {
  var popup = column.startCellEdit(row, fieldId);
  this.cellEditorPopup = popup;
  return popup;
};

scout.Table.prototype.scrollTo = function(row) {
  scout.scrollbars.scrollTo(this.$data, row.$row);
};

scout.Table.prototype.scrollPageUp = function() {
  var newScrollTop = Math.max(0, this.$data[0].scrollTop - this.$data.height());
  scout.scrollbars.scrollTop(this.$data, newScrollTop);
};

scout.Table.prototype.scrollPageDown = function() {
  var newScrollTop = Math.min(this.$data[0].scrollHeight, this.$data[0].scrollTop + this.$data.height());
  scout.scrollbars.scrollTop(this.$data, newScrollTop);
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
  var selectedEqualsRows = scout.arrays.equalsIgnoreOrder(rows, this.selectedRows);
  if (selectedEqualsRows) {
    return;
  }

  // never fire clear selection because of notification thru select row
  this.clearSelection(true);
  this.selectedRows = rows;
  notifyServer = scout.nvl(notifyServer, true);
  if (notifyServer) {
    this._sendRowsSelected(this._rowsToIds(rows), debounceSend);
  }

  if (this.rendered) {
    this.selectedRows.forEach(function(row) {
      this.renderSelection(row);

      // Make sure the cell editor popup is correctly layouted because selection changes the cell bounds
      if (this.cellEditorPopup && this.cellEditorPopup.row.id === row.id) {
        this.cellEditorPopup.position();
        this.cellEditorPopup.pack();
      }
    }, this);
    this._triggerRowsSelected();
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

scout.Table.prototype.$selectedRows = function() {
  if (!this.$data) {
    return $();
  }
  return this.$data.find('.selected');
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
      // row.$row check is necessary because filterAccepted state is only correct for rendered rows (_applyFilters is only called for rendered rows)
      if (row.$row && row.filterAccepted) {
        this._filteredRows.push(row);
      }
    }, this);
    this._filteredRowsDirty = false;
  }
  return this._filteredRows;
};

//TODO CGU still necessary? maybe better remove and use rows(), filteredRows() and selectedRows instead
scout.Table.prototype.$rows = function(includeAggrRows) {
  var selector = '.table-row';
  if (includeAggrRows) {
    selector += ', .table-aggregate-row';
  }
  return this.$data.find(selector);
};

scout.Table.prototype.newFilteredRowsSelector = function(includeAggrRows) {
  var selector = '.table-row:not(.invisible)';
  if (includeAggrRows) {
    selector += ', .table-aggregate-row:not(.invisible)';
  }
  return selector;
};

scout.Table.prototype.$filteredRows = function(includeAggrRows) {
  return this.$data.find(this.newFilteredRowsSelector(includeAggrRows));
};

scout.Table.prototype.$prevFilteredRow = function($row, includeAggrRow) {
  return $row.prevAll(this.newFilteredRowsSelector(includeAggrRow)).first();
};

scout.Table.prototype.$prevFilteredRows = function($row, includeAggrRows) {
  return $row.prevAll(this.newFilteredRowsSelector(includeAggrRows));
};

scout.Table.prototype.$nextFilteredRow = function($row, includeAggrRow) {
  return $row.nextAll(this.newFilteredRowsSelector(includeAggrRow)).first();
};

scout.Table.prototype.$nextFilteredRows = function($row, includeAggrRows) {
  return $row.nextAll(this.newFilteredRowsSelector(includeAggrRows));
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
  var i, useAnimation,
    that = this,
    rowsToHide = [],
    rowsToShow = [];

  // Filter rows
  this.rows.forEach(function(row) {
    var $row = row.$row;

    that._applyFiltersForRow(row);
    if (row.filterAccepted) {
      if ($row.hasClass('invisible')) {
        rowsToShow.push(row);
      }
    } else {
      if (!$row.hasClass('invisible')) {
        rowsToHide.push(row);
      }
    }
  });

  // Show / hide rows that changed their state during filtering
  useAnimation = ((rowsToShow.length + rowsToHide.length) <= that._animationRowLimit);
  rowsToHide.forEach(function(row) {
    that.hideRow(row.$row, useAnimation);
  });
  rowsToShow.forEach(function(row) {
    that.showRow(row.$row, useAnimation);
  });

  // notify and regroup only if at least one row changed it's state
  if (rowsToShow.length > 0 || rowsToHide.length > 0) {
    this._rowsFiltered(rowsToHide);
    this._group(useAnimation);
    this.renderSelection();
  }
};

scout.Table.prototype._rowsFiltered = function(invisibleRows) {
  // non visible rows must be deselected
  this.deselectRows(invisibleRows);
  // notify
  this._filteredRowsDirty = true;
  this._sendRowsFiltered(this._rowsToIds(this.filteredRows()));
  this._triggerRowsFiltered();
};

scout.Table.prototype._rowAcceptedByFilters = function(row) {
  for (var key in this._filterMap) {
    var filter = this._filterMap[key];
    if (!filter.accept(row.$row)) {
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
      // flag is necessary to get correct filter count even when animation is still in progress
      // and to store filter state to prevent unnecessary events
      row.filterAccepted = false;
      return true;
    }
  }
  return false;
};

/**
 * Applies the filters for the given $rows.<p>
 * This function is intended to be used for new rows. That's why rowsFiltered event is only triggered if there are accepted rows in the given list.
 */
scout.Table.prototype._applyFilters = function($rows) {
  var filterChanged,
    newInvisibleRows = [],
    that = this;

  $rows.each(function() {
    var $row = $(this),
      row = $row.data('row');

    if (that._applyFiltersForRow(row)) {
      if (!row.filterAccepted) {
        newInvisibleRows.push(row);
      }
    }
    // always notify if there are new rows which accept the filter
    if (row.filterAccepted) {
      filterChanged = true;
    }
    that._renderRowFilterAccepted(row);
  });

  if (filterChanged) {
    this._rowsFiltered(newInvisibleRows);
  }
};

scout.Table.prototype._renderRowFilterAccepted = function(row) {
  if (row.filterAccepted) {
    this.showRow(row.$row);
  } else {
    this.hideRow(row.$row);
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
  notifyServer = notifyServer !== undefined ? notifyServer : true;
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

scout.Table.prototype.showRow = function($row, useAnimation) {
  var that = this,
    row = $row.data('row');
  if (!$row.hasClass('invisible')) {
    return;
  }

  if (useAnimation) {
    $row.stop().slideDown({
      duration: 250,
      complete: function() {
        $row.removeClass('invisible');
        that.updateScrollbars();
      }
    });
  } else {
    $row.showFast();
    $row.removeClass('invisible');
    that.updateScrollbars();
  }
};

scout.Table.prototype.hideRow = function($row, useAnimation) {
  var that = this,
    row = $row.data('row');
  if ($row.hasClass('invisible')) {
    return;
  }

  if (useAnimation) {
    $row.stop().slideUp({
      duration: 250,
      complete: function() {
        $row.addClass('invisible');
        that.updateScrollbars();
      }
    });
  } else {
    $row.hideFast();
    $row.addClass('invisible');
    that.updateScrollbars();
  }
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
  this._removeRows();
  this._renderRows();
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
    this._insertCheckBoxColumn();
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
  this._injectFilterFuncToMenus(newMenus);
  this._keyStrokeSupport.syncMenus(newMenus, oldMenus);
};

scout.Table.prototype._injectFilterFuncToMenus = function(menus) {
  menus.forEach(function(menu) {
    menu.filterFunc = this._filterMenusHandler;
    this._injectFilterFuncToMenus(menu.childActions);
  }.bind(this));
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
      var filter = scout.create(filterData);
      this.addFilter(filter, false);
    }, this);
  }
};

scout.Table.prototype._renderFilters = function() {
  // _renderFilters is only called after initialization (due to a property change). In that case -> filter()
  this.filter();
};

scout.Table.prototype._renderCheckable = function() {
  this._redraw();
};

scout.Table.prototype._renderRowIconVisible = function() {
  this._redraw();
};

scout.Table.prototype._redraw = function() {
  this._rerenderHeaderColumns();
  this._removeRows();
  this._renderRows();
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
  if (this.header && this.rows.length === 0) {
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
  if (this.rows.length > 0 && this.$emptyData) {
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

scout.Table.prototype._onRowsInserted = function(rows) {
  this._insertRows(rows);
};

scout.Table.prototype._onRowsDeleted = function(rowIds) {
  var rows = this._rowsByIds(rowIds);
  this._deleteRows(rows);
};

scout.Table.prototype._onAllRowsDeleted = function() {
  this._deleteAllRows();
};

scout.Table.prototype._onRowsUpdated = function(rows) {
  this._updateRows(rows);
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
  } else if (event.type === 'rowFilterChanged') {
    this._onRowFilterChanged(event.rows);
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
  } else {
    scout.Table.parent.prototype.onModelAction.call(this, event);
  }
};

/**
 * Method invoked when this is a 'detailTable' and the outline content is displayed.
 * @override Widget.js
 */
scout.Table.prototype._attach = function(event) {
  if (event.target === this) {
    this._$parent.append(this.$container);
    var htmlParent = this.htmlComp.getParent();
    this.htmlComp.setSize(htmlParent.getSize());
    this.session.detachHelper.afterAttach(this.$container);
    scout.Table.parent.prototype._attach.call(this, event);
  }
};

/**
 * Method invoked when this is a 'detailTable' and the outline content is not displayed anymore.
 * @override Widget.js
 */
scout.Table.prototype._detach = function(event) {
  if (event.target === this) {
    this.session.detachHelper.beforeDetach(this.$container);
    this.$container.detach();
    scout.Table.parent.prototype._detach.call(this, event);
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
