/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Device, EventHandler, graphics, icons, InitModelOf, TableColumnMovedEvent, TableControl, TableRow, tooltips} from '../../index';
import $ from 'jquery';

export class AggregateTableControl extends TableControl {

  /**
   * List of aggregated values per {@link Table#visibleColumns visible column}. If a column has no aggregated value,
   * the corresponding entry is empty. This array needs to be updated whenever the list of visible columns changes.
   *
   * The additional "selection" property indicates whether the values are based on the current table selection (true)
   * or all rows (false, default).
   */
  aggregateRow: any[] & { selection?: boolean };

  protected _tableDataScrollHandler: () => void;
  protected _tableColumnResizedHandler: () => void;
  protected _tableColumnMovedHandler: EventHandler<TableColumnMovedEvent>;
  protected _tableColumnStructureChangedHandler: () => void;
  protected _tableChangedHandler: () => void;
  protected _aggregationFunctionChangedHandler: () => void;

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
    this.height = 0;
    this.iconId = icons.SUM;
    this.tooltipText = '${textKey:ui.Total}';
    this.resizerVisible = false;
  }

  static override CONTAINER_ANIMATE_DURATION = 200;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.table.on('columnStructureChanged', this._tableColumnStructureChangedHandler);
    this.table.on('aggregationFunctionChanged', this._aggregationFunctionChangedHandler);
  }

  protected override _destroy() {
    super._destroy();
    this.table.off('columnStructureChanged', this._tableColumnStructureChangedHandler);
    this.table.off('aggregationFunctionChanged', this._aggregationFunctionChangedHandler);
  }

  protected override _render() {
    super._render();
    this._updateEnabledAndSelectedState();
    this.height = this.table.rowHeight + graphics.insets(this.table.footer.$controlContainer).vertical();
  }

  protected override _renderContent($parent: JQuery) {
    this.$contentContainer = $parent.appendDiv('table-aggregate');

    this._aggregate();
    this._renderAggregate();
    this._reconcileScrollPos();

    this.table.$data.on('scroll', this._tableDataScrollHandler);
    this.table.on('columnResized', this._tableColumnResizedHandler);
    this.table.on('columnMoved', this._tableColumnMovedHandler);
    this.table.on('rowsSelected rowsInserted rowsUpdated rowsDeleted filter group allRowsDeleted', this._tableChangedHandler);
  }

  protected override _removeContent() {
    this.$contentContainer.remove();

    this.table.$data.off('scroll', this._tableDataScrollHandler);
    this.table.off('columnResized', this._tableColumnResizedHandler);
    this.table.off('columnMoved', this._tableColumnMovedHandler);
    this.table.off('rowsSelected rowsInserted rowsUpdated rowsDeleted filter group allRowsDeleted', this._tableChangedHandler);
  }

  protected _renderAggregate() {
    let aggregateCells: JQuery[] = [];
    this.table.visibleColumns().forEach((column, c) => {
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
    });

    if (this.aggregateRow.selection) {
      this.$contentContainer.addClass('selection');
    }

    aggregateCells.forEach($c => this.table._resizeAggregateCell($c));
  }

  protected _rerenderAggregate() {
    this.$contentContainer.empty();
    this._renderAggregate();
    this._reconcileScrollPos();
  }

  protected _installCellTooltip($cell: JQuery) {
    tooltips.install($cell, {
      parent: this,
      text: this.table._cellTooltipText.bind(this.table),
      htmlEnabled: true,
      arrowPosition: 50,
      arrowPositionUnit: '%',
      nativeTooltip: !Device.get().isCustomEllipsisTooltipPossible()
    });
  }

  protected _aggregate() {
    let rows: TableRow[],
      aggregateRow: any[] & { selection?: boolean } = [],
      selectedRows = this.table.selectedRows;

    if (selectedRows.length > 1) {
      rows = selectedRows;
      aggregateRow.selection = true;
    } else {
      rows = this.table.filteredRows();
    }

    this.table._forEachVisibleColumn('aggrStart', aggregateRow);
    rows.forEach(row => this.table._forEachVisibleColumn('aggrStep', aggregateRow, row));
    this.table._forEachVisibleColumn('aggrFinish', aggregateRow);

    this.aggregateRow = aggregateRow;
    if (this.contentRendered && this.selected) {
      this._rerenderAggregate();
    }
  }

  protected _reconcileScrollPos() {
    // When scrolling horizontally scroll aggregate content as well
    let scrollLeft = this.table.$data.scrollLeft();
    this.$contentContainer.scrollLeft(scrollLeft);
  }

  protected _updateEnabledAndSelectedState(aggregationFunctionChanged?: boolean) {
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

  protected override _setEnabled(enabled: boolean) {
    super._setEnabled(enabled);
    this._updateEnabledAndSelectedState();
  }

  protected override _setSelected(selected: boolean) {
    this._setProperty('selected', selected);
    this._updateEnabledAndSelectedState();
  }

  protected _onTableDataScroll() {
    this._reconcileScrollPos();
  }

  /**
   * Generic handler for various events
   */
  protected _onTableChanged() {
    this._aggregate();
  }

  protected _onAggregationFunctionChanged() {
    this._updateEnabledAndSelectedState(true);
    if (this.contentRendered && this.selected) {
      this._aggregate();
    }
  }

  protected _onTableColumnResized() {
    this._rerenderAggregate();
  }

  protected _onTableColumnMoved(event: TableColumnMovedEvent) {
    // move aggregated value in aggregateRow
    arrays.move(this.aggregateRow, event.oldPos, event.newPos);
    this._rerenderAggregate();
  }

  protected _onTableColumnStructureChanged() {
    this._updateEnabledAndSelectedState();
    if (this.contentRendered && this.selected) {
      this._aggregate();
    }
  }
}
