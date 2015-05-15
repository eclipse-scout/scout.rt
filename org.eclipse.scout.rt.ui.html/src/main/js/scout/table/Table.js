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
  this.menuBarPosition = 'bottom';
  this._drawDataInProgress = false;
};
scout.inherits(scout.Table, scout.ModelAdapter);

scout.Table.GUI_EVENT_ROWS_DRAWN = 'rowsDrawn';
scout.Table.GUI_EVENT_ROWS_SELECTED = 'rowsSelected';
scout.Table.GUI_EVENT_ROWS_UPDATED = 'rowsUpdated';
scout.Table.GUI_EVENT_ROWS_FILTERED = 'rowsFiltered';
scout.Table.GUI_EVENT_FILTER_RESETTED = 'filterResetted';
scout.Table.GUI_EVENT_ROW_ORDER_CHANGED = 'rowOrderChanged';

scout.Table.prototype.init = function(model, session) {
  scout.Table.parent.prototype.init.call(this, model, session);

  this._initColumns();
  for (var i = 0; i < this.rows.length; i++) {
    this._initRow(this.rows[i]);
  }
  this._syncSelectedRows(this.selectedRows);
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
  }

  // Add gui only checkbox column at the beginning
  if (this.checkable) {
    this._insertCheckBoxColumn();
  }
};

scout.Table.prototype._createKeyStrokeAdapter = function() {
  return new scout.TableKeyStrokeAdapter(this);
};

scout.Table.prototype._syncMenus = function(menus){
  var i;
  for (i= 0; i < this.menus.length; i++) {
    this.keyStrokeAdapter.unregisterKeyStroke(this.menus[i]);
  }
 this.menus = menus;
 for (i = 0; i < this.menus.length; i++) {
   if(this.menus[i].enabled){
     this.keyStrokeAdapter.registerKeyStroke(this.menus[i]);
   }
 }
};

scout.Table.prototype._insertCheckBoxColumn = function() {
  var column = new scout.CheckBoxColumn(),
    columnWidth = 30;
  column.init();
  column.table = this;
  column.fixedWidth = true;
  column.guiOnlyCheckBoxColumn = true;
  column.width = columnWidth;
  scout.arrays.insert(this.columns, column, 0);

  this.checkableColumn = column;
};

scout.Table.prototype._render = function($parent) {
  this._$parent = $parent;
  this.$container = this._$parent.appendDiv('table');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.TableLayout(this));
  this.htmlComp.pixelBasedSizing = false;
  this.$data = this.$container.appendDiv('table-data');
  scout.scrollbars.install(this.$data);
  this.session.detachHelper.pushScrollable(this.$data);
  this.menuBar = new scout.MenuBar(this.$container, this.menuBarPosition, scout.TableMenuItemsOrder.order);
  this._updateRowWidth();
  this.drawData();
};

scout.Table.prototype._renderProperties = function() {
  scout.Table.parent.prototype._renderProperties.call(this);
  this._renderMenus();
  this._renderTableHeader();
  this._renderTableFooter();
  this._renderEnabled();
};

scout.Table.prototype._remove = function() {
  this.session.detachHelper.removeScrollable(this.$data);
  this.menuBar.remove();
  this.header = null;
  this.footer = null;
  scout.Table.parent.prototype._remove.call(this);
};

// FIXME AWE: refactor all _render* methods --> remove parameter, always use this.*
// reason: the property on this is already synced at this point, the argument may contain
// just a data-model value (and not a adpater).
scout.Table.prototype._renderTableControls = function(dummy) {
  this._renderTableFooter();
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
  return new scout.TableHeader(this, this.session);
};

scout.Table.prototype._createFooter = function() {
  return new scout.TableFooter(this);
};

scout.Table.prototype.clearSelection = function() {
  this.selectionHandler.clearSelection();
};

scout.Table.prototype.toggleSelection = function() {
  this.selectionHandler.toggleSelection();
};

scout.Table.prototype.updateScrollbar = function() {
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

  this.selectionHandler.renderSelection();
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
    this.session.send(this.id, 'rowsSorted', data);
    this._renderRowOrderChanges();
    this._triggerRowOrderChanged();
  } else {
    // Delegate sorting to server when it is not possible on client side
    this.session.send(this.id, 'sortRows', data);
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

scout.Table.prototype.drawData = function() {
  this.$rows().remove();
  this._drawData(0);
};

scout.Table.prototype._buildRowDiv = function(row) {
  var rowWidth = this._rowWidth;
  var rowClass = 'table-row';
  if (this.selectedRows.indexOf(row) > -1) {
    rowClass += ' selected';
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

scout.Table.prototype._drawData = function(startRow) {
  // this function has to be fast
  var rowString = '',
    that = this,
    numRowsLoaded = startRow,
    $rows;

  if (this.rows.length > 0) {
    // Build $rows (as string instead of jQuery objects for efficiency reasons)
    for (var r = startRow; r < Math.min(this.rows.length, startRow + 100); r++) {
      var row = this.rows[r];
      rowString += this._buildRowDiv(row, r);
    }
    numRowsLoaded = r;
    $rows = $(rowString);

    // Link model and jQuery objects
    //FIXME CGU merge with loop in installRows
    $rows.each(function(index, rowObject) {
      var $row = $(rowObject);
      var row = that.rows[startRow + index];
      scout.Table.linkRowToDiv(row, $row);
    });

    // append block of rows
    $rows.appendTo(this.$data);

    // Add row listeners, inform subscribers and update scrollbar
    this._installRows($rows);
  }

  // repaint and append next block
  this._drawDataInProgress = false;
  if (this.rows.length > numRowsLoaded) {
    this._drawDataInProgress = true;
    setTimeout(function() {
      that._drawData(startRow + 100);
    }, 0);
  }
};

/**
 * Adds row listeners, triggers "rows drawn" event and updates the scrollbar.
 * This method should be used after the $rows are added to the DOM. The '$rows'
 * are expected to be linked with the corresponding 'rows' (row.$row and $row.data('row')).
 */
scout.Table.prototype._installRows = function($rows) {
  var $selectedRows,
    that = this;

  this.tooltips.forEach(function(tooltip) {
    tooltip.remove();
  }.bind(this));
  this.tooltips = [];

  // Attach listeners
  $rows.each(function() {
    var editorField,
      $row = $(this),
      row = $row.data('row');
    $row.on('mousedown', '', onMouseDown)
      .on('mouseup', '', onMouseUp)
      .on('dblclick', '', onDoubleClick)
      .on('contextmenu', onContextMenu);
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

  // update info and scrollbar
  this._triggerRowsDrawn($rows);
  $selectedRows = this.selectionHandler.dataDrawn();
  this._triggerRowsSelected($selectedRows);
  this.updateScrollbar();

  // update grouping if data was grouped
  this._group();

  // ----- inline methods: --------

  var $mouseDownRow, mouseDownColumn;

  function onMouseDown(event) {
    $mouseDownRow = $(event.delegateTarget);
    mouseDownColumn = that._columnAtX(event.pageX);
    that.selectionHandler.onMouseDown(event, $mouseDownRow);
  }

  function onMouseUp(event) {
    var $row, $mouseUpRow, column, $appLink,
      mouseButton = event.which;
    if (event.originalEvent.detail > 1) {
      // Don't execute on double click events
      return;
    }

    $mouseUpRow = $(event.delegateTarget);
    that.selectionHandler.onMouseUp(event, $mouseUpRow);

    if ($mouseDownRow && $mouseDownRow[0] !== $mouseUpRow[0]) {
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
    } else if (column.guiOnlyCheckBoxColumn) {
      that.sendRowClicked($row, mouseButton);
    } else {
      that.sendRowClicked($row, mouseButton, column.id);
    }
  }

  function onDoubleClick(event) {
    var $row = $(event.delegateTarget),
      column = that._columnAtX(event.pageX);
    that.sendRowAction($row, column.id);
  }

  function onContextMenu(event) {
    var menuItems, popup;
    event.preventDefault();
    if (that.$selectedRows().length > 0) {
      menuItems = that._filterMenus('', true);
      if (menuItems.length > 0) {
        popup = new scout.ContextMenuPopup(that.session, menuItems);
        popup.$anchor = that.$data;
        popup.render();
        popup.setLocation(new scout.Point(event.pageX, event.pageY));
      }
    }
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
    text: text,
    autoRemove: false,
    scrollType: 'position',
    $anchor: $cell,
    table: this
  };
  tooltip = new scout.TableTooltip(opts);
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
    offsetLeft = this.$data.offset().left;

  return scout.arrays.find(this.columns, function(column) {
    columnOffsetRight = offsetLeft + column.width;
    if (x >= offsetLeft && x < columnOffsetRight) {
      return true;
    }
    offsetLeft = columnOffsetRight;
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

scout.Table.prototype._filterMenus = function(allowedTypes) {
  allowedTypes = allowedTypes || [];
  if (!this.headerVisible) {
    //if no header is visible header menues should not be displayed
    delete allowedTypes[allowedTypes.indexOf('Table.Header')];
  }
  if (this.selectedRows.length === 1) {
    allowedTypes.push('Table.SingleSelection');
  } else if (this.selectedRows.length > 1) {
    allowedTypes.push('Table.MultiSelection');
  }
  return scout.menus.filter(this.menus, allowedTypes);
};

scout.Table.prototype._renderMenus = function() {
  var menuItems = this._filterMenus(['Table.EmptySpace', 'Table.Header']);
  menuItems = this.staticMenus.concat(menuItems);
  this.menuBar.updateItems(menuItems);
};

scout.Table.prototype.notifyRowsSelected = function($selectedRows, whileSelecting) {
  var rows = [];
  if ($selectedRows) {
    $selectedRows.each(function() {
      rows.push($(this).data('row'));
    });
  }

  if (!scout.arrays.equalsIgnoreOrder(rows, this.selectedRows)) {
    this.selectedRows = rows;
    this._sendRowsPending = true;
  }
  // Don't send event if user is still selecting (using mouse move selection)
  if (!whileSelecting && this._sendRowsPending) {
    this.sendRowsSelected(this._rowsToIds(rows));
    this._sendRowsPending = false;
  }
  this._triggerRowsSelected($selectedRows);
  this._renderMenus();
};

// Only necessary if the table is a root html comp (outline table)
// FIXME CGU what if a table on a form contains a footer?
// also, if tree gets resized while a tablecontrol and a form is open, the content of the table control is not resized, because desktop does table.setSize after attaching
scout.Table.prototype.onResize = function() {
  if (this.footer) {
    // Delegate window resize events to footer (actually only width changes are relevant)
    this.footer.onResize();
  }
  this.htmlComp.revalidate();
};

scout.Table.prototype.sendRowClicked = function($row, mouseButton, columnId) {
  var data = {
    rowId: $row.data('row').id,
    mouseButton: mouseButton
  };
  if (columnId !== undefined) {
    data.columnId = columnId;
  }
  this.session.send(this.id, 'rowClicked', data);
};

scout.Table.prototype.sendPrepareCellEdit = function(rowId, columnId) {
  var data = {
    rowId: rowId,
    columnId: columnId
  };
  this.session.send(this.id, 'prepareCellEdit', data);
};

scout.Table.prototype.sendCompleteCellEdit = function(fieldId) {
  var data = {
    fieldId: fieldId
  };
  this.session.send(this.id, 'completeCellEdit', data);
};

scout.Table.prototype.sendCancelCellEdit = function(fieldId) {
  var data = {
    fieldId: fieldId
  };
  this.session.send(this.id, 'cancelCellEdit', data);
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

  this.session.send(this.id, 'rowsChecked', data);
};

scout.Table.prototype.sendRowsSelected = function(rowIds) {
  this.session.send(this.id, 'rowsSelected', {
    rowIds: rowIds
  });
};

scout.Table.prototype.sendRowAction = function($row, columnId) {
  this.session.send(this.id, 'rowAction', {
    rowId: $row.data('row').id,
    columnId: columnId
  });
};

scout.Table.prototype.sendAppLinkAction = function(columnId, ref) {
  this.session.send(this.id, 'appLinkAction', {
    columnId: columnId,
    ref: ref
  });
};

scout.Table.prototype.sendReload = function() {
  this.session.send(this.id, 'reload');
};

/**
 * @columnIndex column.index is expected and not the index of the column in this.columns
 */
scout.Table.prototype.cell = function(columnIndex, row) {
  return row.cells[columnIndex];
};

scout.Table.prototype.cellByCellIndex = function(cellIndex, row) {
  return this.cell(this.columns[cellIndex].index, row);
};

scout.Table.prototype.cellValue = function(column, row) {
  var cell = this.cell(column.index, row);

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
  var cell = this.cell(column.index, row);

  if (!cell) {
    return '';
  }
  if (typeof cell !== 'object') {
    return cell;
  }
  return cell.text || '';
};

scout.Table.prototype.cellStyle = function(column, row) {
  var style, hAlign,
    cell = row.cells[column.index],
    width = column.width;

  if (width === 0) {
    return 'display: none;';
  }

  style = 'min-width: ' + width + 'px; max-width: ' + width + 'px; ';
  if (!column.guiOnlyCheckBoxColumn) {
    // guiOnly column doesn't have cells
    style += scout.helpers.legacyCellStyle(cell);
    hAlign = scout.Table.parseHorizontalAlignment(cell.horizontalAlignment);
    // TODO BSH Table | iconId, editable, errorStatus
  }
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
    cell = this.cell(column.index, row);
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

scout.Table.prototype._group = function() {
  var column, alignment, rows, sum, row, value, nextRow,
    that = this,
    groupColumn = this._groupColumn();

  // remove all sum rows
  this.$sumRows().animateAVCSD('height', 0, $.removeThis, that.updateScrollbar.bind(that));

  if (!this.grouped && !groupColumn) {
    return;
  }

  // prepare data
  rows = this.filteredRows();
  sum = [];

  for (var r = 0; r < rows.length; r++) {
    row = rows[r];

    // calculate sum per column
    for (var c = 0; c < this.columns.length; c++) {
      column = this.columns[c];
      value = this.cellValue(column, row);

      if (column.type === 'number') {
        sum[c] = (sum[c] || 0) + value;
      }
    }

    // test if sum should be shown, if yes: reset sum-array
    nextRow = rows[r + 1];
    if ((r === rows.length - 1) || (!this.grouped && this.cellText(groupColumn, row) !== this.cellText(groupColumn, nextRow)) && sum.length > 0) {
      this._appendSumRow(sum, groupColumn, row, this.grouped);
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
scout.Table.prototype._appendSumRow = function(sum, groupColumn, row, all) {
  var c, column, alignment, $cell,
    $sumRow = $.makeDiv('table-row-sum');

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
      $cell = $.makeDiv('table-cell', this.cellText(groupColumn, row))
        .css('text-align', alignment);
    } else {
      $cell = $.makeDiv('table-cell', '&nbsp');
    }

    $cell.appendTo($sumRow)
      .css('min-width', column.width)
      .css('max-width', column.width);
  }

  $sumRow.insertAfter(row.$row)
    .width(this._rowWidth)
    .hide()
    .slideDown();
};

scout.Table.prototype.removeGrouping = function() {
  this.group('', true);
};

scout.Table.prototype.group = function(column, remove) {
  this.grouped = false;
  for (var i = 0; i < this.columns.length; i++) {
    this.columns[i].grouped = false;
  }

  if (remove) {
    this._group();
    return;
  }

  if (!column) {
    this.grouped = true;
    this._group();
  } else {
    column.grouped = true;

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
    colorFunc($cell, value);
  }
};

scout.Table.prototype._onRowsSelected = function(rowIds) {
  var $selectedRows;
  this._syncSelectedRows(rowIds);

  if (this.rendered) {
    $selectedRows = this.selectionHandler.renderSelection();
    this._triggerRowsSelected($selectedRows);
  }
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
    scout.arrays.replace(this.rows, oldRow, updatedRow);

    // Replace old $row
    if (this.rendered && oldRow.$row) {
      var $updatedRow = $(this._buildRowDiv(updatedRow));
      scout.Table.linkRowToDiv(updatedRow, $updatedRow);
      // replace div in DOM
      oldRow.$row.replaceWith($updatedRow);
      $updatedRows = $updatedRows.add($updatedRow);
    }
  }

  // Re-attach listeners and inform subscribers
  if ($updatedRows.length > 0) {
    this._installRows($updatedRows);
  }
};

scout.Table.prototype._renderRowChecked = function(row) {
  if (!this.checkableColumn) {
    throw new Error('checkableColumn not set');
  }

  var $checkbox = this.checkableColumn.$checkBox(row.$row);
  $checkbox.prop('checked', row.checked);
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

scout.Table.prototype._deleteRows = function(rows) {
  var invalidate, i;

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
      for (i = this.tooltips.length - 1; i >= 0; i--) {
        if (this.tooltips[i].row === row) {
          this.tooltips[i].remove();
          this.tooltips.splice(i, 1);
        }
      }

      row.$row.remove();
      delete row.$row;
      invalidate = true;
    }
  }.bind(this));

  if (invalidate) {
    this.htmlComp.invalidateTree();
  }
};

scout.Table.prototype._deleteAllRows = function() {
  // Update model
  this.rows = [];
  this.rowsMap = {};

  // Update HTML
  if (this.rendered) {
    // Cancel cell editing
    if (this.cellEditorPopup) {
      this.cellEditorPopup.cancelEdit();
    }

    this.drawData();
    this.htmlComp.invalidateTree();
  }
};

scout.Table.prototype._startCellEdit = function(column, row, fieldId) {
  var popup = column.startCellEdit(row, fieldId);
  this.cellEditorPopup = popup;
  return popup;
};

scout.Table.prototype.scrollTo = function($selection) {
  scout.scrollbars.scrollTo(this.$data, $selection);
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

scout.Table.prototype.selectRows = function(rows) {
  var $selectedRows;
  if (!scout.arrays.equalsIgnoreOrder(rows, this.selectedRows)) {
    this.selectedRows = rows;
    // FIXME CGU send delayed in case of key navigation
    this.sendRowsSelected(this._rowsToIds(rows));
  }

  $selectedRows = this.selectionHandler.renderSelection();
  this._triggerRowsSelected($selectedRows);
  this._renderMenus();
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
    if (row.$row.isVisible()) {
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
  var selector = '.table-row > div:nth-of-type(' + colIndex + ' )';
  if (includeSumRows) {
    selector += ', .table-row-sum > div:nth-of-type(' + colIndex + ' )';
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
    oldSelectedRows[this.selectedRows[i]] = true;
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
    if (oldSelectedRows[row]) {
      oldSelectedRows[row] = false;
    }
  });
  $(rowsToShow).each(function() {
    that.showRow($(this), useAnimation);
  });

  // Restore selection
  var newSelectedRows = [];
  for (var row in oldSelectedRows) {
    if (oldSelectedRows[row]) {
      newSelectedRows.push(row);
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
        that.updateScrollbar();
      }
    });
  } else {
    $row.show();
    $row.removeClass('invisible');
    that.updateScrollbar();
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
        that.updateScrollbar();
      }
    });
  } else {
    $row.hide();
    $row.addClass('invisible');
    that.updateScrollbar();
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
  this.$rows(true)
    .css('width', this._rowWidth);

  if (this.header) {
    this.header.onColumnResized(column, width);
  }

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
  var index = newPos,
    column0 = this.columns[0];

  // If column 0 is the gui only checkbox column, don't allow moving a column before this one.
  if (column0.guiOnlyCheckBoxColumn) {
    if (newPos === 0) {
      newPos = 1;
    }
    // Adjust index because column is only known on the gui
    index = newPos - 1;
  }

  scout.arrays.remove(this.columns, column);
  scout.arrays.insert(this.columns, column, newPos);

  var data = {
    columnId: column.id,
    index: index
  };
  this.session.send(this.id, 'columnMoved', data);

  if (this.header) {
    this.header.onColumnMoved(column, oldPos, newPos, dragged);
  }

  // move cells
  this.$rows(true).each(function() {
    var $cells = $(this).children();
    if (newPos < oldPos) {
      $cells.eq(newPos).before($cells.eq(oldPos));
    } else {
      $cells.eq(newPos).after($cells.eq(oldPos));
    }
  });
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

scout.Table.prototype._triggerRowsSelected = function($rows) {
  var rowCount = this.rows.length,
    allSelected = false;

  if ($rows) {
    allSelected = $rows.length === rowCount;
  }

  var type = scout.Table.GUI_EVENT_ROWS_SELECTED;
  var event = {
    $rows: $rows,
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

  var column = this.columns[0];
  if (this.checkable && !column.guiOnlyCheckBoxColumn) {
    this._insertCheckBoxColumn();
  } else if (!this.checkable && column.guiOnlyCheckBoxColumn) {
    scout.arrays.remove(this.columns, column);
    this.checkableColumn = null;
  }
};

scout.Table.prototype._syncSelectedRows = function(selectedRowIds) {
  this.selectedRows = this._rowsByIds(selectedRowIds);
};

scout.Table.prototype._renderCheckable = function() {
  this._redraw();
};

scout.Table.prototype._redraw = function() {
  if (this.header) {
    this.header.remove();
    this.header = this._createHeader();
  }
  this._updateRowWidth();
  this.drawData();
};

scout.Table.prototype._renderTableHeader = function() {
  if (this.headerVisible && !this.header) {
    this.header = this._createHeader();
  } else if (!this.headerVisible && this.header) {
    this.header.remove();
    this.header = null;
  }
  if (this.rendered) {
    this.htmlComp.invalidateTree();
  }
};

scout.Table.prototype._renderTableFooter = function() {
  var footerVisible = this._isFooterVisible();
  if (footerVisible) {
    if (!this.footer) {
      this.footer = this._createFooter();
    } else {
      this.footer.update();
    }
  } else if (!footerVisible && this.footer) {
    this.footer.remove();
    this.footer = null;
  }
  if (this.rendered) {
    this.htmlComp.invalidateTree();
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

scout.Table.prototype._renderMultilineText = function() {
  // nop
};

scout.Table.prototype._renderAutoResizeColumns = function() {
  if (this.autoResizeColumns && this.rendered) {
    this.htmlComp.invalidateTree();
  }
};

scout.Table.prototype.injectKeyStrokeAdapter = function(adapter, target) {
  if (adapter === this.keyStrokeAdapter) {
    return;
  }
  if (scout.keyStrokeManager.isAdapterInstalled(this.keyStrokeAdapter)) {
    scout.keyStrokeManager.uninstallAdapter(this.keyStrokeAdapter);
  }
  this.keyStrokeAdapter = adapter;
  scout.keyStrokeManager.installAdapter(target, this.keyStrokeAdapter);
};

scout.Table.prototype._onRowsInserted = function(rows) {
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

    this.drawData();
    this.htmlComp.invalidateTree();
  }
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

scout.Table.prototype._onColumnStructureChanged = function(columns) {
  this.columns = columns;
  this._initColumns();

  if (this.rendered) {
    this._redraw();
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
  this.cellEditorPopup = null;
};

scout.Table.prototype._onRequestFocus = function() {
  this.$container.focus();
};

scout.Table.prototype._onScrollToSelection = function() {
  // TODO BSH Implement
};

scout.Table.prototype.onModelAction = function(event) {
  // _drawData() might not have drawn all rows yet, therefore postpone the
  // execution of this method to prevent conflicts on the row objects.
  if (this._drawDataInProgress) {
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
    $.log.warn('Model event not handled. Widget: scout.Table. Event: ' + event.type + '.');
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
