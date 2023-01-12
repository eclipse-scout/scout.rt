/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AggregateTableRow, Column, graphics, Table, TableRow} from '../../index';
import $ from 'jquery';

export class ColumnOptimalWidthMeasurer {
  column: Column<any>;
  table: Table;
  deferred: JQuery.Deferred<number>;
  imageCount: number;
  completeImageCount: number;
  $measurement: JQuery;
  protected _imageLoadOrErrorHandler: (Event) => void;
  protected _columnCellContents: Record<string, boolean>;

  constructor(column: Column<any>) {
    this.column = column;
    this.table = null;
    this.$measurement = null;
    this.deferred = null;
    this.imageCount = 0;
    this.completeImageCount = 0;
    this._imageLoadOrErrorHandler = this._onImageLoadOrError.bind(this);
    this._columnCellContents = {};
  }

  measure(): number | JQuery.Promise<number> {
    $.log.isDebugEnabled() && $.log.debug('Optimal width measuring started for column ' + this.column.id);

    // Table is not yet available on the column in the constructor -> set it here
    this.table = this.column.table;

    if (this.$measurement) {
      $.log.isDebugEnabled() && $.log.debug('Optimal width measuring aborted for column ' + this.column.id);

      // If measurement is still in progress, abort it and start a new measurement
      this._resolve(-1);
    }

    // Prepare a temporary container that is not (yet) part of the DOM to prevent
    // expensive "forced reflow" while adding the cell divs. Only after all cells
    // are rendered, the container is added to the DOM.
    this.$measurement = this.table.$data.makeDiv('hidden');
    this.imageCount = 0;
    this.completeImageCount = 0;

    // Create divs for all relevant cells of the column
    this._appendElements();

    // Add to DOM
    this.table.$data.append(this.$measurement);

    if (this.completeImageCount >= this.imageCount) {
      // Measure now
      let optimalWidth = this._measure();
      $.log.isDebugEnabled() && $.log.debug('Optimal width measuring done (sync) for column ' + this.column.id + ': ' + optimalWidth);
      this.remove();
      return optimalWidth;
    }

    // Measure later as soon as every image has been loaded
    $.log.isDebugEnabled() && $.log.debug('Not all images loaded, deferring measurement for column ' + this.column.id + '. Images complete: ' + this.completeImageCount + '/' + this.imageCount);
    this.$measurement[0].addEventListener('load', this._imageLoadOrErrorHandler, true);
    this.$measurement[0].addEventListener('error', this._imageLoadOrErrorHandler, true);
    this.deferred = $.Deferred();
    return this.deferred.promise();
  }

  remove() {
    if (!this.$measurement) {
      return;
    }
    this.$measurement[0].removeEventListener('load', this._imageLoadOrErrorHandler, true);
    this.$measurement[0].removeEventListener('error', this._imageLoadOrErrorHandler, true);
    this.$measurement.remove();
    this.$measurement = null;
  }

  protected _measure(): number {
    let maxWidth = this.column.minWidth;
    let maxOverlap = 0;
    // Since the measurement may be async due to image loading, the $measurement is hidden (=display: none) until the real measurement starts.
    // Otherwise it would influence the scroll width of the real table data
    this.$measurement
      .addClass('invisible')
      .removeClass('hidden')
      .children()
      .each(function() {
        if (this.dataset.overlap) {
          maxOverlap = Math.max(maxOverlap, parseInt(this.dataset.overlap, 10));
        }
        maxWidth = Math.max(maxWidth, graphics.size($(this), true).width);
      });
    return maxWidth + maxOverlap;
  }

  protected _resolve(optimalWidth: number) {
    this.remove();
    if (this.deferred) {
      this.deferred.resolve(optimalWidth);
      this.deferred = null;
    }
  }

  protected _appendElements() {
    this._appendHeader();
    this._appendRows();
    this._appendAggregateRows();
  }

  protected _appendHeader() {
    if (this.column.$header) {
      this._appendToMeasurement(this.column.$header.clone());
    }
  }

  protected _appendRows() {
    this.table.rows.forEach(this._appendRow.bind(this));
    this._columnCellContents = {};
  }

  protected _appendRow(row: TableRow) {
    let columnContent = this.column.buildCellForRow(row);
    if (this._columnCellContents[columnContent]) {
      return;
    }
    this._columnCellContents[columnContent] = true;

    this._appendToMeasurement($(columnContent));
  }

  protected _appendAggregateRows() {
    this.table._aggregateRows.forEach(this._appendAggregateRow.bind(this));
  }

  protected _appendAggregateRow(row: AggregateTableRow) {
    this._appendToMeasurement(this._build$CellForAggregateRow(row));
  }

  /**
   * For aggregate rows the text of neighbour cells may overlap into the own cell.
   * To ensure there is enough space for the content of this cell, the overlap of the neighbour must be included.
   * To have access to neighbour cells and to measure its sizes the whole aggregate row must be constructed.
   *
   * @returns The created cell
   */
  protected _build$CellForAggregateRow(row: AggregateTableRow): JQuery {
    let columns = this.table.visibleColumns();
    let colIndex = columns.indexOf(this.column);
    let $row = this.table._build$AggregateRow(row);

    $row.appendTo(this.table.$data);
    columns
      .map(c => c.buildCellForAggregateRow(row))
      .forEach(c => $(c).appendTo($row));
    let $cell = $row.children().eq(colIndex);

    let aggregateOverlap = this._getAggregateOverlap($cell); // compute the overlap
    if (aggregateOverlap > 0) {
      $cell[0].dataset.overlap = aggregateOverlap + '';
    }
    $row.detach();

    return $cell;
  }

  /**
   * Compute how much the neighbour cell overlaps into the given cell.
   * @param $cell The cell for which the overlap should be computed
   * @returns The overlap in pixels.
   */
  protected _getAggregateOverlap($cell: JQuery): number {
    if (!$cell || !$cell.length || $cell.hasClass('empty')) {
      return 0;
    }
    let cellRange = this.table._getAggrCellRange($cell);
    if (cellRange.length < 2) {
      return 0;
    }
    let $neighbour = cellRange[cellRange.length - 1];
    if ($neighbour.hasClass('empty') || $cell.hasClass('halign-right') === $neighbour.hasClass('halign-right')) {
      return 0;
    }
    let $neighbourText = $neighbour.children('.text');
    if (!$neighbourText || !$neighbourText.length) {
      return 0;
    }
    let overlap = graphics.size($neighbourText).width - $neighbour.cssMaxWidth() + $neighbour.cssPaddingLeft();
    if (overlap <= 0) {
      return 0;
    }
    for (let i = cellRange.length - 1; i > 0; i--) {
      let $aggrCell = cellRange[i];
      if ($aggrCell.hasClass('empty')) {
        overlap -= $aggrCell.cssMaxWidth();
      }
    }
    return Math.max(0, overlap);
  }

  protected _appendToMeasurement($calc: JQuery) {
    // Count images
    let $calcImages = $calc.find('img');
    $calcImages.each((index, elem) => {
      let $img = $(elem);
      $img.data('measure', 'in-progress');
      if (elem.complete) {
        $img.data('complete', elem.complete);
        this.completeImageCount++;
      }
      this.imageCount++;
    });

    // Append to measurement element
    $calc.css({
      minWidth: '',
      maxWidth: ''
    }).appendTo(this.$measurement);
  }

  protected _onImageLoadOrError(event: Event) {
    let $img = $(event.target);
    if ($img.data('complete')) {
      // Ignore images which were already complete and therefore already incremented the _imageCompleteCount
      return;
    }

    this.completeImageCount++;
    $.log.isTraceEnabled() && $.log.trace('Images complete (async) ' + this.completeImageCount + '/' + this.imageCount, event.target['src']);
    if (this.completeImageCount >= this.imageCount) {
      let optimalWidth = this._measure();
      $.log.isDebugEnabled() && $.log.debug('Optimal width measuring done (async) for column ' + this.column.id + ': ' + optimalWidth);
      this._resolve(optimalWidth);
    }
  }
}
