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
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.AggregateTableControl = function() {
  scout.AggregateTableControl.parent.call(this);
  this._tableDataScrollHandler = this._onTableDataScroll.bind(this);
  this._tableRowsChangedHandler = this._onTableRowsChanged.bind(this);
  this._tableRowsFilteredHandler = this._onTableRowsFiltered.bind(this);
  this._tableRowsSelectedHandler = this._onTableRowsSelected.bind(this);
  this._tableColumnResizedHandler = this._onTableColumnResized.bind(this);
  this._tableColumnStructureChangedHandler = this._onTableColumnStructureChanged.bind(this);
  this._tableAggregationFunctionHandler = this._onTableAggregationFunctionChanged.bind(this);
  this._tableGroupingHandler = this._onTableGroupingChanged.bind(this);
  this.cssClass = 'aggregate';
  this.height = scout.AggregateTableControl.CONTAINER_SIZE;
  this.animateDuration = scout.AggregateTableControl.CONTAINER_ANIMATE_DURATION;
  this.resizerVisible = false;
  this.aggregateRow;
};
scout.inherits(scout.AggregateTableControl, scout.TableControl);

scout.AggregateTableControl.CONTAINER_SIZE = 40;
scout.AggregateTableControl.CONTAINER_ANIMATE_DURATION = 200;

scout.AggregateTableControl.prototype._init = function(model) {
  scout.AggregateTableControl.parent.prototype._init.call(this, model);

  this.table.on('columnStructureChanged', this._tableColumnStructureChangedHandler);
};

scout.AggregateTableControl.prototype.destroy = function() {
  scout.AggregateTableControl.parent.prototype.destroy.call(this);

  this.table.off('columnStructureChanged', this._tableColumnStructureChangedHandler);
};

scout.AggregateTableControl.prototype._render = function($parent) {
  scout.AggregateTableControl.parent.prototype._render.call(this, $parent);

  this._updateEnabledAndSelectedState();
};

scout.AggregateTableControl.prototype._renderContent = function($parent) {
  this.$contentContainer = $parent.appendDiv('table-aggregate');

  this._aggregate();
  this._renderAggregate();
  this._reconcileScrollPos();

  this.table.$data.on('scroll', this._tableDataScrollHandler);
  this.table.on('rowsInserted', this._tableRowsChangedHandler);
  this.table.on('rowsDeleted', this._tableRowsChangedHandler);
  this.table.on('allRowsDeleted', this._tableRowsChangedHandler);
  this.table.on('rowsFiltered', this._tableRowsFilteredHandler);
  this.table.on('rowsSelected', this._tableRowsSelectedHandler);
  this.table.on('columnResized', this._tableColumnResizedHandler);
  this.table.on('aggregationFunctionChanged', this._tableAggregationFunctionHandler);
  this.table.on('groupingChanged', this._tableGroupingHandler);
};

scout.AggregateTableControl.prototype._removeContent = function() {
  this.$contentContainer.remove();

  this.table.$data.off('scroll', this._tableDataScrollHandler);
  this.table.off('rowsInserted', this._tableRowsChangedHandler);
  this.table.off('rowsDeleted', this._tableRowsChangedHandler);
  this.table.off('allRowsDeleted', this._tableRowsChangedHandler);
  this.table.off('rowsFiltered', this._tableRowsFilteredHandler);
  this.table.off('rowsSelected', this._tableRowsSelectedHandler);
  this.table.off('columnResized', this._tableColumnResizedHandler);
  this.table.off('aggregationFunctionChanged', this._tableAggregationFunctionHandler);
  this.table.off('groupingChanged', this._tableGroupingHandler);
};

scout.AggregateTableControl.prototype._renderAggregate = function() {
  this.table.columns.forEach(function(column, c) {
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

  this.table._forEachColumn('aggrStart', aggregateRow);
  rows.forEach(function(row) {
    this.table._forEachColumn('aggrStep', aggregateRow, row);
  }, this);
  this.table._forEachColumn('aggrFinish', aggregateRow);

  this.aggregateRow = aggregateRow;
};

scout.AggregateTableControl.prototype._reconcileScrollPos = function() {
  // When scrolling horizontally scroll aggregate content as well
  var scrollLeft = this.table.$data.scrollLeft();
  this.$contentContainer.scrollLeft(scrollLeft);
};

scout.AggregateTableControl.prototype._updateEnabledAndSelectedState = function() {
  var enabled = this.table.containsNumberColumn();
  // Make sure a disabled control is not selected
  if (!enabled && this.selected) {
    this.setSelected(false);
  }
  this.setEnabled(enabled);
};

scout.AggregateTableControl.prototype._syncEnabled = function(enabled) {
  this._setProperty('enabled', enabled);
  this._updateEnabledAndSelectedState();
};

scout.AggregateTableControl.prototype._syncSelected = function(selected) {
  this._setProperty('selected', selected);
  this._updateEnabledAndSelectedState();
};

scout.AggregateTableControl.prototype._onTableDataScroll = function() {
  this._reconcileScrollPos();
};

scout.AggregateTableControl.prototype._onTableRowsChanged = function(event) {
  this._aggregate();
  this._rerenderAggregate();
};

scout.AggregateTableControl.prototype._onTableRowsFiltered = function(event) {
  this._aggregate();
  this._rerenderAggregate();
};

scout.AggregateTableControl.prototype._onTableRowsSelected = function(event) {
  this._aggregate();
  this._rerenderAggregate();
};

scout.AggregateTableControl.prototype._onTableColumnResized = function(event) {
  this._rerenderAggregate();
};

scout.AggregateTableControl.prototype._onTableColumnStructureChanged = function(event) {
  this._updateEnabledAndSelectedState();
  if (this.selected && this.rendered) {
    this._aggregate();
    this._rerenderAggregate();
  }
};

scout.AggregateTableControl.prototype._onTableAggregationFunctionChanged = function(event) {
  this._aggregate();
  this._rerenderAggregate();
};

scout.AggregateTableControl.prototype._onTableGroupingChanged = function(event) {
  this._aggregate();
  this._rerenderAggregate();
};
