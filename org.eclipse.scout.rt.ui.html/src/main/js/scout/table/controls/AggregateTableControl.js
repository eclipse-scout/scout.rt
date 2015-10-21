// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.AggregateTableControl = function() {
  scout.AggregateTableControl.parent.call(this);
  this._tableDataScrollHandler = this._onTableDataScroll.bind(this);
  this._tableRowsDrawnHandler = this._onTableRowsDrawn.bind(this);
  this._tableRowsFilteredHandler = this._onTableRowsFiltered.bind(this);
  this._tableRowsSelectedHandler = this._onTableRowsSelected.bind(this);
  this._tableColumnResizedHandler = this._onTableColumnResized.bind(this);
  this.cssClass = 'aggregate';
  this.height = 42;
  this.resizerVisible = false;
  this.aggregate;
};
scout.inherits(scout.AggregateTableControl, scout.TableControl);

scout.AggregateTableControl.prototype._renderContent = function($parent) {
  this.$contentContainer = $parent.appendDiv('table-aggregate');

  this._aggregate();
  this._renderAggregate();
  this._reconcileScrollPos();

  this.table.$data.on('scroll', this._tableDataScrollHandler);
  this.table.on('rowsDrawn', this._tableRowsDrawnHandler);
  this.table.on('rowsFiltered', this._tableRowsFilteredHandler);
  this.table.on('rowsSelected', this._tableRowsSelectedHandler);
  this.table.on('columnResized', this._tableColumnResizedHandler);
};

scout.AggregateTableControl.prototype._removeContent = function() {
  this.$contentContainer.remove();

  this.table.$data.off('scroll', this._tableDataScrollHandler);
  this.table.off('rowsDrawn', this._tableRowsDrawnHandler);
  this.table.off('rowsFiltered', this._tableRowsFilteredHandler);
  this.table.off('rowsSelected', this._tableRowsSelectedHandler);
  this.table.off('columnResized', this._tableColumnResizedHandler);
};

scout.AggregateTableControl.prototype._renderAggregate = function() {
  var $cell,
    aggregate = this.aggregate;

  this.table.columns.forEach(function(column, c) {
    var aggregateValue, decimalFormat;

    if (aggregate[c] === undefined) {
      $cell = $.makeDiv('table-cell', '&nbsp').addClass('empty');
    } else {
      aggregateValue = aggregate[c];
      if (column.format) {
        decimalFormat = new scout.DecimalFormat(this.session.locale, column.format);
        aggregateValue = decimalFormat.format(aggregateValue);
      }
      $cell = $.makeDiv('table-cell table-aggregate-cell', aggregateValue);

      // If aggregation is based on the selection and not on all rows -> mark it
      if (aggregate.selection) {
        $cell.addClass('selection');
      }
    }
    $cell.addClass('halign-' + scout.Table.parseHorizontalAlignment(column.horizontalAlignment));

    $cell.appendTo(this.$contentContainer)
      .css('min-width', column.width)
      .css('max-width', column.width);
  }, this);

  if (aggregate.selection) {
    this.$contentContainer.addClass('selection');
  }
};

scout.AggregateTableControl.prototype._rerenderAggregate = function() {
  this.$contentContainer.empty();
  this._renderAggregate();
};

scout.AggregateTableControl.prototype._aggregate = function() {
  var value, rows,
    aggregate = [],
    selectedRows = this.table.selectedRows;

  if (selectedRows.length > 1) {
    rows = selectedRows;
    aggregate.selection = true;
  } else {
    rows = this.table.filteredRows();
  }

  this.table.columns.forEach(function(column, c) {
    rows.forEach(function(row) {
      value = this.table.cellValue(column, row);

      if (column.type === 'number') {
        value = value || 0;
        aggregate[c] = aggregate[c] || 0;

        aggregate[c] += value;
      }
    }, this);
  }, this);

  this.aggregate = aggregate;
};

scout.AggregateTableControl.prototype._reconcileScrollPos = function() {
  // When scrolling horizontally scroll aggregate content as well
  var scrollLeft = this.table.$data.scrollLeft();
  this.$contentContainer.scrollLeft(scrollLeft);
};

scout.AggregateTableControl.prototype.isContentAvailable = function() {
  return true;
};

scout.AggregateTableControl.prototype._onTableDataScroll = function() {
  this._reconcileScrollPos();
};

scout.AggregateTableControl.prototype._onTableRowsDrawn = function(event) {
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
