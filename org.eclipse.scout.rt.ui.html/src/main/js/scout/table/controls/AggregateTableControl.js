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
scout.AggregateTableControl = function() {
  scout.AggregateTableControl.parent.call(this);
  this._tableDataScrollHandler = this._onTableDataScroll.bind(this);
  this._tableColumnResizedHandler = this._onTableColumnResized.bind(this);
  this._tableColumnMovedHandler = this._onTableColumnMoved.bind(this);
  this._tableColumnStructureChangedHandler = this._onTableColumnStructureChanged.bind(this);
  this._tableChangedHandler = this._onTableChanged.bind(this);
  this._aggregationFunctionChangedHandler = this._onAggregationFunctionChanged.bind(this);
  this.cssClass = 'aggregate';
  this.height = 0; // Will be as height as a row
  this.animateDuration = scout.AggregateTableControl.CONTAINER_ANIMATE_DURATION;
  this.resizerVisible = false;
  this.aggregateRow;
};
scout.inherits(scout.AggregateTableControl, scout.TableControl);

scout.AggregateTableControl.CONTAINER_ANIMATE_DURATION = 200;

scout.AggregateTableControl.prototype._init = function(model) {
  scout.AggregateTableControl.parent.prototype._init.call(this, model);
  this.table.on('columnStructureChanged', this._tableColumnStructureChangedHandler);
  this.table.on('aggregationFunctionChanged', this._aggregationFunctionChangedHandler);
};

scout.AggregateTableControl.prototype._destroy = function() {
  scout.AggregateTableControl.parent.prototype._destroy.call(this);
  this.table.off('columnStructureChanged', this._tableColumnStructureChangedHandler);
  this.table.off('aggregationFunctionChanged', this._aggregationFunctionChangedHandler);
};

scout.AggregateTableControl.prototype._render = function() {
  scout.AggregateTableControl.parent.prototype._render.call(this);
  this._updateEnabledAndSelectedState();
  this.height = this.table.rowHeight + scout.graphics.insets(this.table.footer.$controlContainer).vertical();
};

scout.AggregateTableControl.prototype._renderContent = function($parent) {
  this.$contentContainer = $parent.appendDiv('table-aggregate');

  this._aggregate();
  this._renderAggregate();
  this._reconcileScrollPos();

  this.table.$data.on('scroll', this._tableDataScrollHandler);
  this.table.on('columnResized', this._tableColumnResizedHandler);
  this.table.on('columnMoved', this._tableColumnMovedHandler);
  this.table.on('rowsSelected rowsInserted rowsDeleted filter group allRowsDeleted',
    this._tableChangedHandler);
};

scout.AggregateTableControl.prototype._removeContent = function() {
  this.$contentContainer.remove();

  this.table.$data.off('scroll', this._tableDataScrollHandler);
  this.table.off('columnResized', this._tableColumnResizedHandler);
  this.table.off('columnMoved', this._tableColumnMovedHandler);
  this.table.off('rowsSelected rowsInserted rowsDeleted filter group allRowsDeleted',
    this._tableChangedHandler);
};

scout.AggregateTableControl.prototype._renderAggregate = function() {
  this.table.visibleColumns().forEach(function(column, c) {
    var aggregateValue, cell, $cell;

    aggregateValue = this.aggregateRow[c];
    // Aggregation functions are not available if column is grouped -> do not show aggregated value
    if (aggregateValue === undefined || aggregateValue === null || column.grouped) {
      cell = column.createAggrEmptyCell();
    } else {
      cell = column.createAggrValueCell(aggregateValue);
    }
    $cell = $(column.buildCell(cell, {}));

    // If aggregation is based on the selection and not on all rows -> mark it
    if (this.aggregateRow.selection) {
      $cell.addClass('selection');
    }

    $cell.appendTo(this.$contentContainer);
  }, this);

  if (this.aggregateRow.selection) {
    this.$contentContainer.addClass('selection');
  }
};

scout.AggregateTableControl.prototype._rerenderAggregate = function() {
  this.$contentContainer.empty();
  this._renderAggregate();
};

scout.AggregateTableControl.prototype._aggregate = function() {
  var rows,
    aggregateRow = [],
    selectedRows = this.table.selectedRows;

  if (selectedRows.length > 1) {
    rows = selectedRows;
    aggregateRow.selection = true;
  } else {
    rows = this.table.filteredRows();
  }

  this.table._forEachVisibleColumn('aggrStart', aggregateRow);
  rows.forEach(function(row) {
    this.table._forEachVisibleColumn('aggrStep', aggregateRow, row);
  }, this);
  this.table._forEachVisibleColumn('aggrFinish', aggregateRow);

  this.aggregateRow = aggregateRow;
  if (this.contentRendered && this.selected) {
    this._rerenderAggregate();
  }
};

scout.AggregateTableControl.prototype._reconcileScrollPos = function() {
  // When scrolling horizontally scroll aggregate content as well
  var scrollLeft = this.table.$data.scrollLeft();
  this.$contentContainer.scrollLeft(scrollLeft);
};

scout.AggregateTableControl.prototype._updateEnabledAndSelectedState = function(aggregationFunctionChanged) {
  var enabled = this.table.containsAggregatedNumberColumn();

  // Select control if enabled, aggregation function changed and table is not grouped
  if (enabled) {
    if (aggregationFunctionChanged && !this.table.isGrouped()) {
      this.setSelected(true);
    }
  } else if (this.selected) {
    // Make sure a disabled control is not selected
    this.setSelected(false);
  }
  this.setEnabled(enabled);
};

scout.AggregateTableControl.prototype._setEnabled = function(enabled) {
  this._setProperty('enabled', enabled);
  this._updateEnabledAndSelectedState();
};

scout.AggregateTableControl.prototype._setSelected = function(selected) {
  this._setProperty('selected', selected);
  this._updateEnabledAndSelectedState();
};

scout.AggregateTableControl.prototype._onTableDataScroll = function() {
  this._reconcileScrollPos();
};

/**
 * Generic handler for various events
 * @private
 */
scout.AggregateTableControl.prototype._onTableChanged = function() {
  this._aggregate();
};

scout.AggregateTableControl.prototype._onAggregationFunctionChanged = function() {
  this._updateEnabledAndSelectedState(true);
  if (this.contentRendered && this.selected) {
    this._aggregate();
  }
};

scout.AggregateTableControl.prototype._onTableColumnResized = function() {
  this._rerenderAggregate();
};

scout.AggregateTableControl.prototype._onTableColumnMoved = function(event) {
  // move aggregated value in aggregateRow
  scout.arrays.move(this.aggregateRow, event.oldPos, event.newPos);
  this._rerenderAggregate();
};

scout.AggregateTableControl.prototype._onTableColumnStructureChanged = function() {
  this._updateEnabledAndSelectedState();
  if (this.contentRendered && this.selected) {
    this._aggregate();
  }
};
