/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {graphics} from '../../index';
import $ from 'jquery';

export default class ColumnOptimalWidthMeasurer {

  constructor(column) {
    this.column = column;
    this.table = null;
    this.$measurement = null;
    this.deferred = null;
    this.imageCount = 0;
    this.completeImageCount = 0;
    this._imageLoadOrErrorHandler = this._onImageLoadOrError.bind(this);
    this._columnCellContents = {};
  }

  measure(promise) {
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

  _measure() {
    let optimalWidth = this.column.minWidth;
    // Since the measurement may be async due to image loading, the $measurement is hidden (=display: none) until the real measurement starts.
    // Otherwise it would influence the scroll width of the real table data
    this.$measurement
      .addClass('invisible')
      .removeClass('hidden')
      .children()
      .each(function() {
        optimalWidth = Math.max(optimalWidth, graphics.size($(this)).width);
      });
    return optimalWidth;
  }

  _resolve(optimalWidth) {
    this.remove();
    if (this.deferred) {
      this.deferred.resolve(optimalWidth);
      this.deferred = null;
    }
  }

  _appendElements() {
    this._appendHeader();
    this._appendRows();
    this._appendAggregateRows();
  }

  _appendHeader() {
    if (this.column.$header) {
      this._appendToMeasurement(this.column.$header.clone());
    }
  }

  _appendRows() {
    this.table.rows.forEach(this._appendRow.bind(this));
    this._columnCellContents = {};
  }

  _appendRow(row) {
    let columnContent = this.column.buildCellForRow(row);
    if (this._columnCellContents[columnContent]) {
      return;
    }
    this._columnCellContents[columnContent] = true;

    this._appendToMeasurement($(columnContent));
  }

  _appendAggregateRows() {
    this.table._aggregateRows.forEach(this._appendAggregateRow.bind(this));
  }

  _appendAggregateRow(row) {
    this._appendToMeasurement($(this.column.buildCellForAggregateRow(row)));
  }

  _appendToMeasurement($calc) {
    // Count images
    let $calcImgs = $calc.find('img');
    $calcImgs.each((index, elem) => {
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

  _onImageLoadOrError(event) {
    let $img = $(event.target);
    if ($img.data('complete')) {
      // Ignore images which were already complete and therefore already incremented the _imageCompleteCount
      return;
    }

    this.completeImageCount++;
    $.log.isTraceEnabled() && $.log.trace('Images complete (async) ' + this.completeImageCount + '/' + this.imageCount, event.target.src);
    if (this.completeImageCount >= this.imageCount) {
      let optimalWidth = this._measure();
      $.log.isDebugEnabled() && $.log.debug('Optimal width measuring done (async) for column ' + this.column.id + ': ' + optimalWidth);
      this._resolve(optimalWidth);
    }
  }
}
