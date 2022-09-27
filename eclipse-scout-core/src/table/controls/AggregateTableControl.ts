/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, Device, graphics, icons, TableControl, tooltips} from '../../index';
import $ from 'jquery';

export default class AggregateTableControl extends TableControl {

  constructor() {
    super();
    this._tableDataScrollHandler = this._onTableDataScroll.bind(this);
    this._tableColumnResizedHandler = this._onTableColumnResized.bind(this);
    this._tableColumnMovedHandler = this._onTableColumnMoved.bind(this);
    this._tableColumnStructureChangedHandler = this._onTableColumnStructureChanged.bind(this);
    this._tableChangedHandler = this._onTableChanged.bind(this);
    this._aggregationFunctionChangedHandler = this._onAggregationFunctionChanged.bind(this);

    this.animateDuration = AggregateTableControl.CONTAINER_ANIMATE_DURATION;
    this.aggregateRow = [];
    this.cssClass = 'aggregate';
    this.height = 0; // Will be as height as a row
    this.iconId = icons.SUM;
    this.tooltipText = '${textKey:ui.Total}';
    this.resizerVisible = false;
  }

  static CONTAINER_ANIMATE_DURATION = 200;

  _init(model) {
    super._init(model);
    this.table.on('columnStructureChanged', this._tableColumnStructureChangedHandler);
    this.table.on('aggregationFunctionChanged', this._aggregationFunctionChangedHandler);
  }

  _destroy() {
    super._destroy();
    this.table.off('columnStructureChanged', this._tableColumnStructureChangedHandler);
    this.table.off('aggregationFunctionChanged', this._aggregationFunctionChangedHandler);
  }

  _render() {
    super._render();
    this._updateEnabledAndSelectedState();
    this.height = this.table.rowHeight + graphics.insets(this.table.footer.$controlContainer).vertical();
  }

  _renderContent($parent) {
    this.$contentContainer = $parent.appendDiv('table-aggregate');

    this._aggregate();
    this._renderAggregate();
    this._reconcileScrollPos();

    this.table.$data.on('scroll', this._tableDataScrollHandler);
    this.table.on('columnResized', this._tableColumnResizedHandler);
    this.table.on('columnMoved', this._tableColumnMovedHandler);
    this.table.on('rowsSelected rowsInserted rowsUpdated rowsDeleted filter group allRowsDeleted', this._tableChangedHandler);
  }

  _removeContent() {
    this.$contentContainer.remove();

    this.table.$data.off('scroll', this._tableDataScrollHandler);
    this.table.off('columnResized', this._tableColumnResizedHandler);
    this.table.off('columnMoved', this._tableColumnMovedHandler);
    this.table.off('rowsSelected rowsInserted rowsUpdated rowsDeleted filter group allRowsDeleted', this._tableChangedHandler);
  }

  _renderAggregate() {
    let aggregateCells = [];
    this.table.visibleColumns().forEach(function(column, c) {
      let aggregateValue, cell, $cell;

      aggregateValue = this.aggregateRow[c];
      // Aggregation functions are not available if column is grouped -> do not show aggregated value
      let isEmpty = aggregateValue === undefined || aggregateValue === null || column.grouped;
      if (isEmpty) {
        cell = column.createAggrEmptyCell();
      } else {
        cell = column.createAggrValueCell(aggregateValue);
      }
      $cell = $(column.buildCell(cell, {}));
      if (!isEmpty) {
        aggregateCells.push($cell);
      }

      // install tooltips
      this._installCellTooltip($cell);

      // If aggregation is based on the selection and not on all rows -> mark it
      if (this.aggregateRow.selection) {
        $cell.addClass('selection');
      }

      $cell.appendTo(this.$contentContainer);
    }, this);

    if (this.aggregateRow.selection) {
      this.$contentContainer.addClass('selection');
    }

    aggregateCells.forEach($c => this.table._resizeAggregateCell($c));
  }

  _rerenderAggregate() {
    this.$contentContainer.empty();
    this._renderAggregate();
    this._reconcileScrollPos();
  }

  _installCellTooltip($cell) {
    tooltips.install($cell, {
      parent: this,
      text: this.table._cellTooltipText.bind(this.table),
      htmlEnabled: true,
      arrowPosition: 50,
      arrowPositionUnit: '%',
      nativeTooltip: !Device.get().isCustomEllipsisTooltipPossible()
    });
  }

  _aggregate() {
    let rows,
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
  }

  _reconcileScrollPos() {
    // When scrolling horizontally scroll aggregate content as well
    let scrollLeft = this.table.$data.scrollLeft();
    this.$contentContainer.scrollLeft(scrollLeft);
  }

  _updateEnabledAndSelectedState(aggregationFunctionChanged) {
    if (!this.initialized) {
      // During init the columns are not resolved yet -> containsAggregatedNumberColumn won't return a correct value
      return;
    }
    let enabled = this.table.containsAggregatedNumberColumn();

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
  }

  _setEnabled(enabled) {
    super._setEnabled(enabled);
    this._updateEnabledAndSelectedState();
  }

  _setSelected(selected) {
    this._setProperty('selected', selected);
    this._updateEnabledAndSelectedState();
  }

  _onTableDataScroll() {
    this._reconcileScrollPos();
  }

  /**
   * Generic handler for various events
   * @private
   */
  _onTableChanged() {
    this._aggregate();
  }

  _onAggregationFunctionChanged() {
    this._updateEnabledAndSelectedState(true);
    if (this.contentRendered && this.selected) {
      this._aggregate();
    }
  }

  _onTableColumnResized() {
    this._rerenderAggregate();
  }

  _onTableColumnMoved(event) {
    // move aggregated value in aggregateRow
    arrays.move(this.aggregateRow, event.oldPos, event.newPos);
    this._rerenderAggregate();
  }

  _onTableColumnStructureChanged() {
    this._updateEnabledAndSelectedState();
    if (this.contentRendered && this.selected) {
      this._aggregate();
    }
  }
}
