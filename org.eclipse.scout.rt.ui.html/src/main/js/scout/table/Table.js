// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Table = function() {
  scout.Table.parent.call(this);
  this.$container;
  this.$data;
  this.header;
  this.selectionHandler;
  this.columns = [];
  this.tableControls = [];
  this.menus = [];
  this.rows = [];
  this.rowsMap = {}; // rows by id
  this._rowWidth = 0;
  this.staticMenus = [];
  this._addAdapterProperties(['tableControls', 'menus', 'keyStrokes']);
  this.events = new scout.EventSupport();
  this.selectionHandler = new scout.TableSelectionHandler(this);
  this._filterMap = {};
  this.tooltips = [];
  this.animationRowLimit = 25;
  this.menuBar;
  this._renderRowsInProgress = false;
  this._drawDataInProgress = false;
  this._appLinkKeyStroke = new scout.AppLinkKeyStroke(this, this.handleAppLinkAction);

  this.attached = false; // Indicates whether this table is currently visible to the user.
};
scout.inherits(scout.Table, scout.ModelAdapter);

scout.Table.GUI_EVENT_ROWS_DRAWN = 'rowsDrawn';
scout.Table.GUI_EVENT_ROWS_SELECTED = 'rowsSelected';
scout.Table.GUI_EVENT_ROWS_UPDATED = 'rowsUpdated';
scout.Table.GUI_EVENT_ROWS_FILTERED = 'rowsFiltered';
scout.Table.GUI_EVENT_FILTER_RESETTED = 'filterResetted';
scout.Table.GUI_EVENT_ROW_ORDER_CHANGED = 'rowOrderChanged';

scout.Table.COLUMN_MIN_WIDTH = 30;

scout.Table.prototype._init = function(model, session) {
  scout.Table.parent.prototype._init.call(this, model, session);

  this._initColumns();
  this.rows.forEach(function(row) {
    this._initRow(row);
  }, this);

  var menuSorter = new scout.MenuItemsOrder(this.session, this.objectType);
  this.menuBar = new scout.MenuBar(this.session, menuSorter);
  this.menuBar.bottom();
  this.addChild(this.menuBar);

  this._syncSelectedRows(this.selectedRows);
  this.keyStrokeAdapter.registerKeyStroke(this._appLinkKeyStroke);
};

scout.Table.prototype._initRow = function(row) {
  scout.defaultValues.applyTo(row, 'TableRow');
  this._unwrapCells(row.cells);
  scout.defaultValues.applyTo(row.cells, 'Cell');
  this.rowsMap[row.id] = row;
};

scout.Table.prototype._initColumns = function() {
  var column, i;
  for (i = 0; i < this.columns.length; i++) {
    column = this.session.objectFactory.create(this.columns[i]);
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
  this._updateRowWidth();
};

scout.Table.prototype._createKeyStrokeAdapter = function() {
  return new scout.TableKeyStrokeAdapter(this);
};

scout.Table.prototype._insertCheckBoxColumn = function() {
  var column = new scout.CheckBoxColumn(),
    model = {
      fixedWidth: true,
      fixedPosition: true,
      guiOnly: true,
      disallowHeaderMenu: true,
      width: scout.Table.COLUMN_MIN_WIDTH,
      table: this
    };
  column.init(model, this.session);

  scout.arrays.insert(this.columns, column, 0);
  this.checkableColumn = column;
};

scout.Table.prototype._insertRowIconColumn = function() {
  var position = 0,
    column = new scout.Column(),
    model = {
      fixedWidth: true,
      fixedPosition: true,
      guiOnly: true,
      disallowHeaderMenu: true,
      width: scout.Table.COLUMN_MIN_WIDTH,
      table: this
    };
  column.init(model, this.session);
  if (this.columns[0] === this.checkableColumn) {
    position = 1;
  }
  scout.arrays.insert(this.columns, column, position);
  this.rowIconColumn = column;
};

scout.Table.prototype.handleAppLinkAction = function(event) {
  var $appLink = $(event.target);
  var column = this._columnAtX($appLink.offset().left);
  this.sendAppLinkAction(column.id, $appLink.data('ref'));
};

scout.Table.prototype._render = function($parent) {
  this._$parent = $parent;
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
  scout.scrollbars.install(this.$data, this.session, {
    axis: 'both'
  });
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
    this.cellHorizontalPadding = test.cssPxValue("padding-left") + test.cssPxValue("padding-right");
    test.remove();
  }

  this._renderRows();

  //----- inline methods: --------

  var $mouseDownRow, mouseDownColumn, that = this;

  function onMouseDown(event) {
    $mouseDownRow = $(event.currentTarget);
    mouseDownColumn = that._columnAtX(event.pageX);
    that.selectionHandler.onMouseDown(event);
  }

  function onMouseUp(event) {
    var $row, $mouseUpRow, column, $appLink,
      mouseButton = event.which;
    if (event.originalEvent.detail > 1) {
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
      that.sendAppLinkAction(column.id, $appLink.data('ref'));
    } else if (column.guiOnly) {
      that.sendRowClicked($row, mouseButton);
    } else {
      that.sendRowClicked($row, mouseButton, column.id);
    }
  }

  function onDoubleClick(event) {
    var $row = $(event.currentTarget),
      column = that._columnAtX(event.pageX);
    that.sendRowAction($row, column.id);
  }

  this.attached = true;
};

scout.Table.prototype.onContextMenu = function(event) {
  var func = function(event) {
    var menuItems, popup;
    event.preventDefault();
    if (this.selectedRows.length > 0) {
      menuItems = this._filterMenus(['Table.SingleSelection', 'Table.MultiSelection'], true);
      if (!event.pageX && !event.pageY) {
        var $rowToDisplay = this.selectionHandler.lastActionRow ? this.selectionHandler.lastActionRow.$row : this.selectedRows[this.selectedRows.length - 1].$row;
        var offset = $rowToDisplay.offset();
        event.pageX = offset.left + 10;
        event.pageY = offset.top + $rowToDisplay.outerHeight() / 2;
      }
      if (menuItems.length > 0) {
        popup = new scout.ContextMenuPopup(this.session, {
          menuItems: menuItems,
          location: {
            x: event.pageX,
            y: event.pageY
          },
          $anchor: this.$data
        });
        popup.render(undefined, event);
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
  this.header = null;
  this.footer = null;
  this.attached = false;
  scout.Table.parent.prototype._remove.call(this);
};

// FIXME AWE: refactor all _render* methods --> remove parameter, always use this.*
// reason: the property on this is already synced at this point, the argument may contain
// just a data-model value (and not a adpater).
scout.Table.prototype._renderTableControls = function(dummy) {
  this._renderTableFooter();
};

scout.Table.prototype._syncTableControls = function(controls) {
  var i;
  for (i = 0; i < this.tableControls.length; i++) {
    this.keyStrokeAdapter.unregisterKeyStroke(this.tableControls[i]);
  }
  this.tableControls = controls;
  for (i = 0; i < this.tableControls.length; i++) {
    if (this.tableControls[i].enabled) {
      this.keyStrokeAdapter.registerKeyStroke(this.tableControls[i]);
    }
  }
};

scout.Table.prototype._renderTableStatusVisible = function(dummy) {
  this._renderTableFooter();
};

/**
 * Converts each element of the given cell array that is of type string to an object with
 * a property 'text' with the original value.
 *
 * Example:
 * 'My Company' --> { text: 'MyCompany'; }
 *
 * @see JsonCell.java
 */
scout.Table.prototype._unwrapCells = function(cells) {
  for (var i = 0; i < cells.length; i++) {
    var cell = cells[i];
    if (typeof cell === 'string') {
      cell = {
        text: cell
      };
      cells[i] = cell;
    }
  }
};

scout.Table.prototype._isFooterVisible = function() {
  return this.tableStatusVisible || this.tableControls.length > 0;
};

scout.Table.prototype._createHeader = function() {
  return new scout.TableHeader(this);
};

scout.Table.prototype._createFooter = function() {
  return new scout.TableFooter(this);
};

scout.Table.prototype.clearSelection = function(dontFire) {
  this.selectedRows.forEach(function(row) {
    if (row.$row) {
      row.$row.select(false);
      row.$row.toggleClass('select-middle select-top select-bottom select-single', false);
    }
  });
  this.selectedRows.length = 0;
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
  var clientSideSortingPossible = this._prepareColumnsForSorting(sortColumns);
  if (!clientSideSortingPossible) {
    return false;
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
  if ($rows.length < that.animationRowLimit) {
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

  // update grouping if group by column is active
  if (this._groupColumn()) {
    this._group();
  }
};

/**
 * @param multiSort true to add the column to list of sorted columns. False to use this column exclusively as sort column (reset other columns)
 * @param remove true to remove the column from the sort columns
 */
scout.Table.prototype.sort = function(column, direction, multiSort, remove) {
  var data, sorted;

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
    this.remoteHandler(this.id, 'rowsSorted', data);
    this._renderRowOrderChanges();
    this._triggerRowOrderChanged();
  } else {
    // Delegate sorting to server when it is not possible on client side
    this.remoteHandler(this.id, 'sortRows', data);
  }
};

scout.Table.prototype._updateSortColumns = function(column, direction, multiSort, remove) {
  var sortIndex, maxIndex = -1;

  if (remove) {
    column.sortActive = false;

    // Adjust sibling columns with higher index
    scout.arrays.eachSibling(this.columns, column, function(siblingColumn) {
      if (siblingColumn.sortIndex > column.sortIndex) {
        siblingColumn.sortIndex = siblingColumn.sortIndex - 1;
      }
    });
    column.sortIndex = -1;
    return;
  }

  if (multiSort) {
    // If not already sorted set the appropriate sort index
    if (!column.sortActive) {
      for (var i = 0; i < this.columns.length; i++) {
        sortIndex = this.columns[i].sortIndex;
        if (sortIndex >= 0) {
          maxIndex = Math.max(sortIndex, maxIndex);
        }
      }
      column.sortIndex = maxIndex + 1;
    }
  } else {
    scout.arrays.eachSibling(this.columns, column, function(siblingColumn) {
      if (siblingColumn.sortActive) {
        siblingColumn.sortIndex = -1;
        siblingColumn.sortActive = false;
      }
    });
    column.sortIndex = 0;
  }

  column.sortAscending = direction === 'asc' ? true : false;
  column.sortActive = true;
};

scout.Table.prototype._buildRowDiv = function(row, rowSelected, previousRowSelected, followingRowSelected) {
  var rowWidth = this._rowWidth;
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
  var rowDiv = '<div class="' + rowClass + '" data-rowid="' + row.id + '" style="width: ' + rowWidth + 'px"' + scout.device.unselectableAttribute + '>';
  for (var c = 0; c < this.columns.length; c++) {
    rowDiv += this.columns[c].buildCell(row);
  }
  rowDiv += '</div>';

  return rowDiv;
};

scout.Table.prototype._updateRowWidth = function() {
  this._rowWidth = 0;
  for (var i = 0; i < this.columns.length; i++) {
    this._rowWidth += this.columns[i].width;
  }
};

/**
 * @param new rows to append at the end of this.$data. If undefined this.rows is used.
 */
scout.Table.prototype._renderRows = function(rows, startRowIndex, lastRowOfBlockSelected) {
  // this function has to be fast
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
    for (var r = startRowIndex; r < Math.min(rows.length, startRowIndex + 100); r++) {
      var row = rows[r],
        rowSelected = this.selectedRows.indexOf(row) > -1;

      if (r === startRowIndex) {
        previousRowSelected = lastRowOfBlockSelected;
      } else {
        previousRowSelected = this.selectedRows.indexOf(rows[r - 1]) > -1;
      }
      if (r < Math.min(rows.length, startRowIndex + 100) - 1) {
        followingRowSelected = this.selectedRows.indexOf(rows[r + 1]) > -1;
      } else {
        followingRowSelected = false;
      }
      rowString += this._buildRowDiv(row, rowSelected, previousRowSelected, followingRowSelected);
    }
    numRowsLoaded = r;
    $rows = $(rowString);

    // Link model and jQuery objects and render selection borders
    //FIXME CGU merge with loop in installRows
    $rows.each(function(index, rowObject) {
      var $row = $(rowObject);
      var row = rows[startRowIndex + index];
      scout.Table.linkRowToDiv(row, $row);
      lastRowOfBlockSelected = $row.isSelected();
    });

    // append block of rows
    $rows.appendTo(this.$data);
    this._installRows($rows);
    this._triggerRowsDrawn($rows);

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
    // FIXME CGU: von A.WE - siehe E-Mail. Das hier macht Probleme, weil später invalidateLayoutTree() aufgerufen wird
    // prüfen ob auf setTimeout verzichtet werden kann, oder _renderRows ohne Seiteneffekte funktionieren kann.
    // siehe _installRows
    setTimeout(function() {
      that._renderRows(rows, startRowIndex + 100, lastRowOfBlockSelected);
    }, 0);
  }
};

scout.Table.prototype._removeRows = function($rows) {
  $rows = $rows || this.$rows();
  $rows.remove();
  this._renderEmptyData();
  this._triggerRowsDrawn($rows);
};

/**
 * This method should be used after the $rows are added to the DOM (new rows, updated rows). The '$rows'
 * are expected to be linked with the corresponding 'rows' (row.$row and $row.data('row')).
 */
scout.Table.prototype._installRows = function($rows) {
  var that = this;

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

  // update grouping if data was grouped
  this._group();
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
    text: text,
    autoRemove: false,
    scrollType: 'position',
    $anchor: $cell,
    table: this
  };
  tooltip = new scout.TableTooltip(this.session, opts);
  tooltip.render();
  // link to be able to remove it when row gets deleted
  tooltip.row = row;
  this.tooltips.push(tooltip);
  // link so that it gets removed when table gets removed
  this.addChild(tooltip);
};

/**
 * @returns the column at position x (e.g. from event.pageX)
 */
scout.Table.prototype._columnAtX = function(x) {
  var columnOffsetRight = 0,
    columnOffsetLeft = this.$data.offset().left,
    scrollLeft = this.$data.scrollLeft();

  columnOffsetLeft -= scrollLeft;
  return scout.arrays.find(this.columns, function(column) {
    columnOffsetRight = columnOffsetLeft + column.width;
    if (columnOffsetLeft >= 0 && x >= columnOffsetLeft && x < columnOffsetRight) {
      return true;
    }
    columnOffsetLeft = columnOffsetRight;
  });
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

scout.Table.prototype._filterMenus = function(allowedTypes, onlyVisible) {
  allowedTypes = allowedTypes || [];
  if (allowedTypes.indexOf('Table.SingleSelection') > -1 && this.selectedRows.length !== 1) {
    scout.arrays.remove(allowedTypes, 'Table.SingleSelection');
  }
  if (allowedTypes.indexOf('Table.MultiSelection') > -1 && this.selectedRows.length <= 1) {
    scout.arrays.remove(allowedTypes, 'Table.MultiSelection');
  }
  return scout.menus.filter(this.menus, allowedTypes, onlyVisible);
};

scout.Table.prototype._renderMenus = function() {
  this._updateMenuBar();
  if (this.header) {
    this.header.updateMenuBar();
  }
};

scout.Table.prototype._updateMenuBar = function() {
  var menuItems = this._filterMenus(['Table.EmptySpace', 'Table.SingleSelection', 'Table.MultiSelection']);
  menuItems = this.staticMenus.concat(menuItems);
  this.menuBar.updateItems(menuItems);
};

scout.Table.prototype.notifyRowSelectionFinished = function() {
  if (this._sendRowsPending) {
    this.sendRowsSelected(this._rowsToIds(this.selectedRows));
    this._sendRowsPending = false;
  }
  this._triggerRowsSelected();
  this._updateMenuBar();

  if (this.groupedSelection) {
    this._group(true);
  }
};

// Only necessary if the table is a root html comp (outline table)
// FIXME CGU what if a table on a form contains a footer?
// also, if tree gets resized while a tablecontrol and a form is open, the content of the table control is not resized, because desktop does table.setSize after attaching
scout.Table.prototype.onResize = function() {
  if (this.footer) {
    // Delegate window resize events to footer (actually only width changes are relevant)
    this.footer.onResize();
  }
  this.htmlComp.revalidateLayoutTree();
};

scout.Table.prototype.sendRowClicked = function($row, mouseButton, columnId) {
  var data = {
    rowId: $row.data('row').id,
    mouseButton: mouseButton
  };
  if (columnId !== undefined) {
    data.columnId = columnId;
  }
  this.remoteHandler(this.id, 'rowClicked', data);
};

/**
 * @param openFieldPopupOnCellEdit when this parameter is set to true, the CellEditorPopup sets an
 *    additional property 'cellEditor' on the editor-field. The field instance may use this property
 *    to decide whether or not it should open a popup immediately after it is rendered. This is used
 *    for Smart- and DateFields.
 */
scout.Table.prototype.prepareCellEdit = function(rowId, columnId, openFieldPopupOnCellEdit) {
  this.openFieldPopupOnCellEdit = scout.helpers.nvl(openFieldPopupOnCellEdit, false);
  this.sendPrepareCellEdit(rowId, columnId);
};

scout.Table.prototype.sendPrepareCellEdit = function(rowId, columnId) {
  var data = {
    rowId: rowId,
    columnId: columnId
  };
  this.remoteHandler(this.id, 'prepareCellEdit', data);
};

scout.Table.prototype.sendCompleteCellEdit = function(fieldId) {
  var data = {
    fieldId: fieldId
  };
  this.remoteHandler(this.id, 'completeCellEdit', data);
};

scout.Table.prototype.sendCancelCellEdit = function(fieldId) {
  var data = {
    fieldId: fieldId
  };
  this.remoteHandler(this.id, 'cancelCellEdit', data);
};

scout.Table.prototype.sendRowsChecked = function(rows) {
  var data = {
    rows: []
  };

  for (var i = 0; i < rows.length; i++) {
    data.rows.push({
      rowId: rows[i].id,
      checked: rows[i].checked
    });
  }

  this.remoteHandler(this.id, 'rowsChecked', data);
};

scout.Table.prototype.sendRowsSelected = function(rowIds) {
  this.remoteHandler(this.id, 'rowsSelected', {
    rowIds: rowIds
  });
};

scout.Table.prototype.sendRowAction = function($row, columnId) {
  this.remoteHandler(this.id, 'rowAction', {
    rowId: $row.data('row').id,
    columnId: columnId
  });
};

scout.Table.prototype.sendAppLinkAction = function(columnId, ref) {
  this.remoteHandler(this.id, 'appLinkAction', {
    columnId: columnId,
    ref: ref
  });
};

scout.Table.prototype.sendReload = function() {
  if (this.hasReloadHandler) {
    this.$data.empty();
    // scoll bar must be (re)installed after all content has been removed (because also scrollbars are removed)..
    scout.scrollbars.install(this.$data, this.session, {
      axis: 'both'
    });
    this.remoteHandler(this.id, 'reload');
  }
};

scout.Table.prototype.sendExportToClipboard = function() {
  this.remoteHandler(this.id, 'exportToClipboard');
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

  if (cell === null) { //cell may be a number so don't use !cell
    return null;
  }
  if (typeof cell !== 'object') {
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
  if (typeof cell !== 'object') {
    return cell;
  }
  return cell.text || '';
};

scout.Table.prototype.cellStyle = function(column, cell) {
  var style, hAlign,
    width = column.width;

  if (width === 0) {
    return 'display: none;';
  }

  style = 'min-width: ' + width + 'px; max-width: ' + width + 'px; ';
  style += scout.helpers.legacyStyle(cell);
  hAlign = scout.Table.parseHorizontalAlignment(cell.horizontalAlignment);
  return style + (hAlign === 'left' || !hAlign ? '' : 'text-align: ' + hAlign + '; ');
};

scout.Table.prototype.cellTooltipText = function(column, row) {
  var cell = row.cells[column.index];
  if (typeof cell === 'object' && cell !== null && scout.strings.hasText(cell.tooltipText)) {
    return cell.tooltipText;
  }
  return '';
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

scout.Table.prototype._group = function(update) {
  var column, alignment, rows, sum, row, value, nextRow, useRow, skip,
    that = this,
    groupColumn = this._groupColumn();

  // remove all sum rows
  if (update) {
    this.$sumRows().remove();
  } else {
    this.$sumRows().animateAVCSD('height', 0, $.removeThis, that.updateScrollbars.bind(that));
  }

  if (!this.grouped && !groupColumn) {
    return;
  }

  // prepare data
  rows = this.filteredRows();
  sum = [];

  for (var r = 0; r < rows.length; r++) {
    row = rows[r];
    useRow = !this.groupedSelection || row.$row.isSelected();

    // calculate sum per column
    for (var c = 0; c < this.columns.length; c++) {
      column = this.columns[c];
      value = this.cellValue(column, row);

      if (column.type === 'number') {
        sum[c] = (sum[c] || 0) + (value === '' || !useRow ? 0 : value);
      }
    }

    // test if sum should be shown, if yes: reset sum-array
    nextRow = rows[r + 1];

    // test if group is finished
    skip = (r === rows.length - 1);
    skip = skip || (groupColumn && groupColumn.type !== 'date' && !this.grouped && this.cellText(groupColumn, row) !== this.cellText(groupColumn, nextRow));
    skip = skip || (groupColumn && groupColumn.type === 'date' && !this.grouped && scout.dates.parseJsonDate(this.cellValue(groupColumn, row)).getFullYear() !== scout.dates.parseJsonDate(this.cellValue(groupColumn, nextRow)).getFullYear());

    // if group is finished: add group row
    if (sum.length > 0 && skip) {
      this._appendSumRow(sum, groupColumn, row, this.grouped, update);
      sum = [];
    }
  }
};

scout.Table.prototype._groupColumn = function() {
  return scout.arrays.find(this.columns, function(column) {
    return column.grouped;
  });
};

/**
 * Appends a new sum row after row.$row
 */
scout.Table.prototype._appendSumRow = function(sum, groupColumn, row, all, update) {
  var c, column, alignment, $cell,
    $sumRow = $.makeDiv('table-row-sum');

  if (this.groupedSelection) {
    $sumRow.addClass('sum-selection');
  }

  for (c = 0; c < this.columns.length; c++) {
    column = this.columns[c];
    alignment = scout.Table.parseHorizontalAlignment(column.horizontalAlignment);
    if (typeof sum[c] === 'number') {
      var sumValue = sum[c];
      if (column.format) {
        var decimalFormat = new scout.DecimalFormat(this.session.locale, column.format);
        sumValue = decimalFormat.format(sumValue);
      }
      $cell = $.makeDiv('table-cell', sumValue)
        .css('text-align', alignment);
    } else if (!all && column === groupColumn) {
      if (column.type === 'date') {
        $cell = $.makeDiv('table-cell', scout.dates.parseJsonDate(this.cellValue(groupColumn, row)).getFullYear())
          .css('text-align', alignment);
      } else {
        $cell = $.makeDiv('table-cell', this.cellText(groupColumn, row))
        .css('text-align', alignment);
      }
    } else {
      $cell = $.makeDiv('table-cell', '&nbsp').addClass('empty');
    }

    $cell.appendTo($sumRow)
      .css('min-width', column.width)
      .css('max-width', column.width);
  }

  $sumRow.insertAfter(row.$row).width(this._rowWidth);

  if (!update) {
    $sumRow
      .hide()
      .slideDown();
  }
};

scout.Table.prototype.removeGrouping = function() {
  this.grouped = false;
  for (var i = 0; i < this.columns.length; i++) {
    this.columns[i].grouped = false;
  }

  this._group();
};

scout.Table.prototype.group = function(column, selection) {
  this.grouped = false;
  for (var i = 0; i < this.columns.length; i++) {
    this.columns[i].grouped = false;
  }

  if (!column) {
    this.grouped = true;
    this.groupedSelection = selection;
    this._group();
  } else {
    column.grouped = true;
    this.groupedSelection = false;

    // sort also takes care about the grouping
    this.sort(column, 'asc', false);
  }
};

scout.Table.prototype.colorData = function(column, mode) {
  var minValue, maxValue, colorFunc, row, value, v, i, $cell,
    filteredRows = this.filteredRows();

  for (i = 0; i < this.rows.length; i++) {
    row = this.rows[i];
    v = this.cellValue(column, row);

    if (v < minValue || minValue === undefined) {
      minValue = v;
    }
    if (v > maxValue || maxValue === undefined) {
      maxValue = v;
    }
  }

  // TODO CRU Don't use hardcoded colors (or make them customizable)
  // TODO CRU Handle case where model already has set specific cell background colors
  if (mode === 'red') {
    colorFunc = function($cell, value) {
      var level = (value - minValue) / (maxValue - minValue);

      var r = Math.ceil(255 - level * (255 - 171)),
        g = Math.ceil(175 - level * (175 - 214)),
        b = Math.ceil(175 - level * (175 - 147));

      $cell.css('background-color', 'rgb(' + r + ',' + g + ', ' + b + ')');
      $cell.css('background-image', '');
    };
  } else if (mode === 'green') {
    colorFunc = function($cell, value) {
      var level = (value - minValue) / (maxValue - minValue);

      var r = Math.ceil(171 - level * (171 - 255)),
        g = Math.ceil(214 - level * (214 - 175)),
        b = Math.ceil(147 - level * (147 - 175));

      $cell.css('background-color', 'rgb(' + r + ',' + g + ', ' + b + ')');
      $cell.css('background-image', '');
    };
  } else if (mode === 'bar') {
    colorFunc = function($cell, value) {
      var level = Math.ceil((value - minValue) / (maxValue - minValue) * 100) + '';

      $cell.css('background-color', 'transparent');
      $cell.css('background-image', 'linear-gradient(to left, #80c1d0 0%, #80c1d0 ' + level + '%, transparent ' + level + '%, transparent 100% )');
    };
  } else if (mode === 'remove') {
    colorFunc = function($cell, value) {
      $cell.css('background-image', '');
      $cell.css('background-color', 'transparent');
    };
  }

  for (i = 0; i < filteredRows.length; i++) {
    row = filteredRows[i];
    value = this.cellValue(column, row);
    $cell = this.$cell(column, row.$row);

    if (value !== '') {
      colorFunc($cell, value);
    }
  }
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

scout.Table.prototype._onRowsUpdated = function(rows) {
  var $updatedRows = $();

  // Update model
  for (var i = 0; i < rows.length; i++) {
    var updatedRow = rows[i];

    var oldRow = this.rowsMap[updatedRow.id];
    if (!oldRow) {
      throw new Error('Update event received for non existing row. RowId: ' + updatedRow.id);
    }

    // Replace old row
    this._initRow(updatedRow);
    var rowIndex = scout.arrays.replace(this.rows, oldRow, updatedRow);
    scout.arrays.replace(this.selectedRows, oldRow, updatedRow);

    // Replace old $row
    if (this.rendered && oldRow.$row) {
      var rowSelected = this.selectedRows.indexOf(updatedRow) > -1;

      var previousRowSelected = false,
        followingRowSelected = false;
      if (rowIndex > 0) {
        previousRowSelected = this.selectedRows.indexOf(this.rows[rowIndex - 1]) > -1;
      }
      if (rowIndex + 1 < this.rows.length) {
        followingRowSelected = this.selectedRows.indexOf(this.rows[rowIndex + 1]) > -1;
      }

      var $updatedRow = $(this._buildRowDiv(updatedRow, rowSelected, previousRowSelected, followingRowSelected));
      scout.Table.linkRowToDiv(updatedRow, $updatedRow);
      // replace div in DOM
      oldRow.$row.replaceWith($updatedRow);
      $updatedRows = $updatedRows.add($updatedRow);
    }
  }

  if ($updatedRows.length > 0) {
    this._installRows($updatedRows);
  }
};

scout.Table.prototype._renderRowChecked = function(row) {
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
  this.sendRowsChecked(updatedRows);
  if (this.rendered) {
    this._renderRowChecked(row);
  }
};

scout.Table.prototype._insertRows = function(rows) {
  // Update model
  for (var i = 0; i < rows.length; i++) {
    var row = rows[i];
    this._initRow(row);
    // Always insert new rows at the end, if the order is wrong a rowOrderChange event will follow
    this.rows.push(row);
  }

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
};

scout.Table.prototype._deleteRows = function(rows) {
  var invalidate, i;

  this.deselectRows(rows, false);
  rows.forEach(function(row) {
    //Update model
    scout.arrays.remove(this.rows, row);
    delete this.rowsMap[row.id];

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

  if (invalidate) {
    this.invalidateLayoutTree();
  }
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
  // Update model
  this.rows = [];
  this.rowsMap = {};
  this.selectRows([], false);

  // Update HTML
  if (this.rendered) {
    // Cancel cell editing
    if (this.cellEditorPopup) {
      this.cellEditorPopup.cancelEdit();
    }

    this._removeRows();
    this.invalidateLayoutTree();
  }
};

scout.Table.prototype._startCellEdit = function(column, row, fieldId) {
  var popup = column.startCellEdit(row, fieldId);
  this.cellEditorPopup = popup;
  return popup;
};

scout.Table.prototype.scrollTo = function(row) {
  scout.scrollbars.scrollTo(this.$data, row.$row);
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
scout.Table.prototype.renderSelection = function($row, deselect) {
  //TODO CGU rewrite, render selection based on model and not using jquery selectors
  var $previousElement = $row.prevAll('.table-row:not(.invisible)').first(),
    $followingElement = $row.nextAll('.table-row:not(.invisible)').first();
  $row.removeClass('select-middle select-top select-bottom select-single selected');
  if (!deselect) {
    $row.select(true);
    if (!$previousElement.isSelected()) {
      if ($followingElement.isSelected()) {
        $row.addClass('select-top');
      } else {
        $row.addClass('select-single');
      }
    } else {
      if ($followingElement.isSelected()) {
        $row.addClass('select-middle');
      } else {
        $row.addClass('select-bottom');
      }
      if ($previousElement.hasClass('select-bottom')) {
        $previousElement.toggleClass('select-middle', true);
        $previousElement.toggleClass('select-bottom', false);
      } else {
        $previousElement.toggleClass('select-single', false);
        $previousElement.toggleClass('select-top', true);
      }
    }
    if ($followingElement.isSelected()) {
      if ($followingElement.hasClass('select-single')) {
        $followingElement.toggleClass('select-single', false);
        $followingElement.toggleClass('select-bottom', true);
      } else if ($followingElement.hasClass('select-top')) {
        $followingElement.toggleClass('select-top', false);
        $followingElement.toggleClass('select-middle', true);
      }
    }
  } else {
    if ($previousElement.isSelected()) {
      if ($previousElement.hasClass('select-middle')) {
        $previousElement.toggleClass('select-middle', false);
        $previousElement.toggleClass('select-bottom', true);
      } else if ($previousElement.hasClass('select-top')) {
        $previousElement.toggleClass('select-top', false);
        $previousElement.toggleClass('select-single', true);
      }
    }
    if ($followingElement.isSelected()) {
      if ($followingElement.hasClass('select-bottom')) {
        $followingElement.toggleClass('select-single', true);
        $followingElement.toggleClass('select-bottom', false);
      } else if ($followingElement.hasClass('select-middle')) {
        $followingElement.toggleClass('select-top', true);
        $followingElement.toggleClass('select-middle', false);
      }
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
    this.renderSelection(row.$row);
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
      this.renderSelection(row.$row, true);
    }
    if (!ongoingSelection) {
      this._triggerRowsSelected();
      this.sendRowsSelected(this._rowsToIds(this.selectedRows));
    } else {
      this._sendRowsPending = true;
    }
  }
};

scout.Table.prototype.selectRows = function(rows, notifyServer) {
  rows = scout.arrays.ensure(rows);
  var selectedEqualsRows = scout.arrays.equalsIgnoreOrder(rows, this.selectedRows);
  if (!selectedEqualsRows) {
    this.clearSelection(!notifyServer);
    this.selectedRows = rows;
    // FIXME CGU send delayed in case of key navigation
    if (notifyServer) {
      this.sendRowsSelected(this._rowsToIds(rows));
    }
  }

  if (this.rendered && !selectedEqualsRows) {
    this.selectedRows.forEach(function(row) {
      this.renderSelection(row.$row, false);

      // Make sure the cell editor popup is correctly layouted because selection changes the cell bounds
      if (this.cellEditorPopup && this.cellEditorPopup.row.id === row.id) {
        this.cellEditorPopup.alignTo();
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

scout.Table.prototype.$selectedRows = function() {
  if (!this.$data) {
    return $();
  }
  return this.$data.find('.selected');
};

scout.Table.prototype.filteredRows = function(includeSumRows) {
  var filteredRows = [];
  for (var i = 0; i < this.rows.length; i++) {
    var row = this.rows[i];
    if (row.$row && row.$row.isVisible()) {
      filteredRows.push(row);
    }
  }
  return filteredRows;
};

scout.Table.prototype.$rows = function(includeSumRows) {
  var selector = '.table-row';
  if (includeSumRows) {
    selector += ', .table-row-sum';
  }
  return this.$data.find(selector);
};

scout.Table.prototype.$filteredRows = function(includeSumRows) {
  var selector = '.table-row:not(.invisible)';
  if (includeSumRows) {
    selector += ', .table-row-sum:not(.invisible)';
  }
  return this.$data.find(selector);
};

scout.Table.prototype.$prevFilteredRows = function($row, includeSumRows) {
  var selector = '.table-row:not(.invisible)';
  if (includeSumRows) {
    selector += ', .table-row-sum:not(.invisible)';
  }
  return $row.prevAll(selector);
};

scout.Table.prototype.$nextFilteredRows = function($row, includeSumRows) {
  var selector = '.table-row:not(.invisible)';
  if (includeSumRows) {
    selector += ', .table-row-sum:not(.invisible)';
  }
  return $row.nextAll(selector);
};

scout.Table.prototype.$sumRows = function() {
  return this.$data.find('.table-row-sum');
};

scout.Table.prototype.$cellsForColIndex = function(colIndex, includeSumRows) {
  var selector = '.table-row > div:nth-of-type(' + colIndex + ')';
  if (includeSumRows) {
    selector += ', .table-row-sum > div:nth-of-type(' + colIndex + ')';
  }
  return this.$data.find(selector);
};

scout.Table.prototype.$cellsForColIndexWidthFix = function(colIndex, includeSumRows) {
  var selector = '.table-row > div:nth-of-type(' + colIndex + ') > .width-fix ';
  if (includeSumRows) {
    selector += ', .table-row-sum > div:nth-of-type(' + colIndex + ') > .width-fix';
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
  var that = this,
    rowCount = 0,
    $allRows = this.$rows(),
    i;

  this.$sumRows().hide();

  // Remember current selection
  var oldSelectedRows = {};
  for (i = 0; i < this.selectedRows.length; i++) {
    oldSelectedRows[this.selectedRows[i].id] = true;
  }

  // Filter rows
  var rowsToHide = [];
  var rowsToShow = [];
  $allRows.each(function() {
    var $row = $(this),
      show = true;

    for (i = 0; i < that.columns.length; i++) {
      if (that.columns[i].filterFunc) {
        show = show && that.columns[i].filterFunc($row);
      }
    }

    for (var key in that._filterMap) {
      var filter = that._filterMap[key];
      show = show && filter.accept($row);
    }

    if (show) {
      if ($row.hasClass('invisible')) {
        rowsToShow.push($row);
      }
      rowCount++;
    } else {
      if (!$row.hasClass('invisible')) {
        rowsToHide.push($row);
      }
    }
  });

  // Show / hide rows that changed their state during filtering
  var useAnimation = ((rowsToShow.length + rowsToHide.length) <= that.animationRowLimit);
  $(rowsToHide).each(function() {
    var $row = $(this);
    that.hideRow($row, useAnimation);
    // Remove hidden rows from the map of previously selected rows
    var row = $row.data('row');
    if (oldSelectedRows[row.id]) {
      oldSelectedRows[row.id] = false;
    }
  });
  $(rowsToShow).each(function() {
    that.showRow($(this), useAnimation);
  });

  // Restore selection
  var newSelectedRows = [];
  for (var rowId in oldSelectedRows) {
    if (oldSelectedRows[rowId]) {
      newSelectedRows.push(this.rowById(rowId));
    }
  }
  this.selectRows(newSelectedRows); // this will update the server model if necessary

  // Used by table footer
  this.filteredRowCount = rowCount;

  this._triggerRowsFiltered(rowCount, this.filteredBy());

  $(':animated', that.$data).promise().done(function() {
    that._group();
  });
};

/**
 *
 * @returns array of filter names which are currently active
 */
scout.Table.prototype.filteredBy = function() {
  var filteredBy = [];
  for (var i = 0; i < this.columns.length; i++) {
    if (this.columns[i].filterFunc) {
      filteredBy.push(this.columns[i].text || '');
    }
  }
  for (var key in this._filterMap) {
    var filter = this._filterMap[key];
    filteredBy.push(filter.label);
  }
  return filteredBy;
};

scout.Table.prototype.resetFilter = function() {
  this.clearSelection();

  // reset rows
  var that = this;
  var $rows = this.$rows();
  $rows.each(function() {
    that.showRow($(this), ($rows.length <= that.animationRowLimit));
  });
  this._group();

  // set back all filter functions
  for (var i = 0; i < this.columns.length; i++) {
    this.columns[i].filter = [];
    this.columns[i].filterFunc = null;
  }
  this._filterMap = {};
  this.filteredRowCount = undefined;
  this._triggerFilterResetted();
};

/**
 * @param filter object with name and accept()
 */
scout.Table.prototype.registerFilter = function(key, filter) {
  if (!key) {
    throw new Error('key has to be defined');
  }
  this._filterMap[key] = filter;
};

scout.Table.prototype.getFilter = function(key, filter) {
  if (!key) {
    throw new Error('key has to be defined');
  }
  return this._filterMap[key];
};

scout.Table.prototype.unregisterFilter = function(key) {
  if (!key) {
    throw new Error('key has to be defined');
  }
  delete this._filterMap[key];
};

scout.Table.prototype.showRow = function($row, useAnimation) {
  var that = this;
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
    $row.show();
    $row.removeClass('invisible');
    that.updateScrollbars();
  }
};

scout.Table.prototype.hideRow = function($row, useAnimation) {
  var that = this;
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
    $row.hide();
    $row.addClass('invisible');
    that.updateScrollbars();
  }
};

/**
 * @param resizingInProgress set this to true when calling this function several times in a row. If resizing is finished you have to call resizingColumnFinished.
 */
scout.Table.prototype.resizeColumn = function(column, width) {
  if (column.fixedWidth) {
    return;
  }
  var colNum = this.columns.indexOf(column) + 1;
  column.width = width;
  this._updateRowWidth();

  this.$cellsForColIndex(colNum, true)
    .css('min-width', width)
    .css('max-width', width);
  if (scout.device.tableAdditionalDivRequired) {
    this.$cellsForColIndexWidthFix(colNum, true)
      .css('max-width', (width - this.cellHorizontalPadding - 2 /* unknown IE9 extra space */));
    // same calculation in scout.Column.prototype.buildCell;
  }
  this.$rows(true)
    .css('width', this._rowWidth);

  if (this.header) {
    this.header.onColumnResized(column);
  }
  this._renderEmptyData();
  this._sendColumnResized(column);
};

scout.Table.prototype._sendColumnResized = function(column) {
  if (column.fixedWidth || this.autoResizeColumns) {
    return;
  }

  var event = new scout.Event(this.id, 'columnResized', {
    columnId: column.id,
    width: column.width
  });

  // Only send the latest resize event for a column
  event.coalesce = function(previous) {
    return this.id === previous.id && this.type === previous.type && this.columnId === previous.columnId;
  };

  // send delayed to avoid a lot of requests while resizing
  this.session.sendEvent(event, 750);
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

  var data = {
    columnId: column.id,
    index: index
  };
  this.remoteHandler(this.id, 'columnMoved', data);

  if (this.header) {
    this.header.onColumnMoved(column, oldPos, newPos, dragged);
  }

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

scout.Table.prototype._triggerRowsDrawn = function($rows) {
  var type = scout.Table.GUI_EVENT_ROWS_DRAWN;
  var event = {
    $rows: $rows
  };
  this.events.trigger(type, event);
};

scout.Table.prototype._triggerRowsSelected = function() {
  var rowCount = this.rows.length,
    allSelected = false;

  if (this.selectedRows) {
    allSelected = this.selectedRows.length === rowCount;
  }

  var type = scout.Table.GUI_EVENT_ROWS_SELECTED;
  var event = {
    rows: this.selectedRows,
    allSelected: allSelected
  };
  this.events.trigger(type, event);
};

scout.Table.prototype._triggerRowsFiltered = function(numRows, filterName) {
  var type = scout.Table.GUI_EVENT_ROWS_FILTERED;
  var event = {
    numRows: numRows,
    filterName: filterName
  };
  this.events.trigger(type, event);
};

scout.Table.prototype._triggerFilterResetted = function() {
  var type = scout.Table.GUI_EVENT_FILTER_RESETTED;
  this.events.trigger(type);
};

scout.Table.prototype._triggerRowOrderChanged = function(row, animating) {
  var event = {
    row: row,
    animating: animating
  };
  this.events.trigger(scout.Table.GUI_EVENT_ROW_ORDER_CHANGED, event);
};

scout.Table.prototype._renderHeaderVisible = function() {
  this._renderTableHeader();
};

scout.Table.prototype._syncCheckable = function(checkable) {
  this.checkable = checkable;

  var column = this.checkableColumn;
  if (this.checkable && !column) {
    this._insertCheckBoxColumn();
  } else if (!this.checkable && column) {
    scout.arrays.remove(this.columns, column);
    this.checkableColumn = null;
  }
};

scout.Table.prototype._syncRowIconVisible = function(rowIconVisible) {
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

scout.Table.prototype._syncMenus = function(menus) {
  var i;
  for (i = 0; i < this.menus.length; i++) {
    this.keyStrokeAdapter.unregisterKeyStroke(this.menus[i]);
  }
  this.menus = menus;
  for (i = 0; i < this.menus.length; i++) {
    if (this.menus[i].enabled) {
      this.keyStrokeAdapter.registerKeyStroke(this.menus[i]);
    }
  }
};

scout.Table.prototype._renderCheckable = function() {
  this._redraw();
};

scout.Table.prototype._renderRowIconVisible = function() {
  this._redraw();
};

scout.Table.prototype._redraw = function() {
  this._removeTableHeader();
  this._renderTableHeader();
  this._removeRows();
  this._renderRows();
};

scout.Table.prototype._renderTableHeader = function() {
  if (this.headerVisible && !this.header) {
    this.header = this._createHeader();
    this.addChild(this.header);
    this.header.render();
    this._renderEmptyData();
  } else if (!this.headerVisible && this.header) {
    this._removeTableHeader();
  }
  this.invalidateLayoutTree();
};

scout.Table.prototype._removeTableHeader = function() {
  if (this.header) {
    this.header.remove();
    this.removeChild(this.header);
    this.header = null;
  }
};

scout.Table.prototype._renderEmptyData = function() {
  if (this.header && this.rows.length === 0) {
    if (!this.$emptyData) {
      this.$emptyData = $.makeDiv()
        .html('&nbsp;')
        .appendTo(this.$data);
    }
    this.$emptyData.css('min-width', this.header.$container[0].scrollWidth)
      .css('max-width', this.header.$container[0].scrollWidth);
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
  var footerVisible = this._isFooterVisible();
  if (footerVisible) {
    if (!this.footer) {
      this.footer = this._createFooter();
      this.addChild(this.footer);
      this.footer.render();
    } else {
      this.footer.update();
    }
  } else if (!footerVisible && this.footer) {
    this._removeTableFooter();
  }
  this.invalidateLayoutTree();
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

scout.Table.prototype.injectKeyStrokeAdapter = function(adapter, target) {
  if (adapter === this.keyStrokeAdapter) {
    return;
  }
  scout.keyStrokeUtils.uninstallAdapter(this.keyStrokeAdapter);
  this.keyStrokeAdapter = adapter;
  this.keyStrokeAdapter.registerKeyStroke(this._appLinkKeyStroke);
  scout.keyStrokeUtils.installAdapter(this.session, this.keyStrokeAdapter, target);
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

  if (this.rendered) {
    this._renderRowOrderChanges();
  }
  this._triggerRowOrderChanged();
};

/**
 * Rebuilds the header.<br>
 * Does not modify the rows, it expects a deleteAll and insert event to follow which will do the job.
 */
scout.Table.prototype._onColumnStructureChanged = function(columns) {
  this.columns = columns;
  this._initColumns();

  if (this.rendered) {
    this._removeTableHeader();
    this._renderTableHeader();
  }
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
  field.destroy();
  this.cellEditorPopup.remove();
  this.cellEditorPopup = null;
};

scout.Table.prototype._onRequestFocus = function() {
  this.session.focusManager.requestFocus(this.$container);
};

scout.Table.prototype._onScrollToSelection = function() {
  this.revealSelection();
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
  } else {
    scout.Table.parent.prototype.onModelAction.call(this, event);
  }
};

/**
 * === Method required for objects that act as 'outlineContent' ===
 *
 * Method invoked when this is a 'detailTable' and the outline content is displayed;
 *
 *  In contrast to 'render/remove', this method uses 'JQuery attach/detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
 *  This method has no effect if already attached.
 */
scout.Table.prototype.attach = function() {
  if (this.attached || !this.rendered) {
    return;
  }

  this._$parent.append(this.$container);

  var htmlComp = scout.HtmlComponent.get(this.$container);
  var htmlParent = htmlComp.getParent();
  htmlComp.setSize(htmlParent.getSize());

  this.session.detachHelper.afterAttach(this.$container);
  scout.keyStrokeUtils.installAdapter(this.session, this.keyStrokeAdapter, this.$container);

  this.attached = true;
};

/**
 * === Method required for objects that act as 'outlineContent' ===
 *
 * Method invoked when this is a 'detailTable' and the outline content is not displayed anymore.
 *
 *  In contrast to 'render/remove', this method uses 'JQuery attach/detach mechanism' to retain CSS properties, so that the model must not be interpreted anew.
 *  This method has no effect if already attached.
 */
scout.Table.prototype.detach = function() {
  if (!this.attached || !this.rendered) {
    return;
  }

  scout.keyStrokeUtils.uninstallAdapter(this.keyStrokeAdapter);
  this.session.detachHelper.beforeDetach(this.$container);
  this.$container.detach();

  this.attached = false;
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
