/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.ColumnOptimalWidthMeasurer = function(column) {
  this.column = column;
  this.table = null;
  this.$measurement = null;
  this.deferred = null;
  this.imageCount = 0;
  this.completeImageCount = 0;
  this._imageLoadOrErrorHandler = this._onImageLoadOrError.bind(this);
};

scout.ColumnOptimalWidthMeasurer.prototype.measure = function(promise) {
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
    var optimalWidth = this._measure();
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
};

scout.ColumnOptimalWidthMeasurer.prototype.remove = function() {
  if (!this.$measurement) {
    return;
  }
  this.$measurement[0].removeEventListener('load', this._imageLoadOrErrorHandler, true);
  this.$measurement[0].removeEventListener('error', this._imageLoadOrErrorHandler, true);
  this.$measurement.remove();
  this.$measurement = null;
};

scout.ColumnOptimalWidthMeasurer.prototype._measure = function() {
  var optimalWidth = this.column.minWidth;
  // Since the measurement may be async due to image loading, the $measurement is hidden (=display: none) until the real measurement starts.
  // Otherwise it would influence the scroll width of the real table data
  this.$measurement
    .addClass('invisible')
    .removeClass('hidden')
    .children().each(function() {
      optimalWidth = Math.max(optimalWidth, scout.graphics.size($(this)).width);
    });
  return optimalWidth;
};

scout.ColumnOptimalWidthMeasurer.prototype._resolve = function(optimalWidth) {
  this.remove();
  if (this.deferred) {
    this.deferred.resolve(optimalWidth);
    this.deferred = null;
  }
};

scout.ColumnOptimalWidthMeasurer.prototype._appendElements = function() {
  this._appendHeader();
  this._appendRows();
  this._appendAggregateRows();
};

scout.ColumnOptimalWidthMeasurer.prototype._appendHeader = function() {
  if (this.column.$header) {
    this._appendToMeasurement(this.column.$header.clone());
  }
};

scout.ColumnOptimalWidthMeasurer.prototype._appendRows = function() {
  this.table.rows.forEach(this._appendRow.bind(this));
};

scout.ColumnOptimalWidthMeasurer.prototype._appendRow = function(row) {
  this._appendToMeasurement($(this.column.buildCellForRow(row)));
};

scout.ColumnOptimalWidthMeasurer.prototype._appendAggregateRows = function() {
  this.table._aggregateRows.forEach(this._appendAggregateRow.bind(this));
};

scout.ColumnOptimalWidthMeasurer.prototype._appendAggregateRow = function(row) {
  this._appendToMeasurement($(this.column.buildCellForAggregateRow(row)));
};

scout.ColumnOptimalWidthMeasurer.prototype._appendToMeasurement = function($calc) {
  // Count images
  var $calcImgs = $calc.find('img');
  $calcImgs.each(function(index, elem) {
    var $img = $(elem);
    $img.data('measure', 'in-progress');
    if (elem.complete) {
      $img.data('complete', elem.complete);
      this.completeImageCount++;
    }
    this.imageCount++;
  }.bind(this));

  // Append to measurement element
  $calc.css({
    minWidth: '',
    maxWidth: ''
  }).appendTo(this.$measurement);
};

scout.ColumnOptimalWidthMeasurer.prototype._onImageLoadOrError = function(event) {
  var $img = $(event.target);
  if ($img.data('complete')) {
    // Ignore images which were already complete and therefore already incremented the _imageCompleteCount
    return;
  }

  this.completeImageCount++;
  $.log.isTraceEnabled() && $.log.trace('Images complete (async) ' + this.completeImageCount + '/' + this.imageCount, event.target.src);
  if (this.completeImageCount >= this.imageCount) {
    var optimalWidth = this._measure();
    $.log.isDebugEnabled() && $.log.debug('Optimal width measuring done (async) for column ' + this.column.id + ': ' + optimalWidth);
    this._resolve(optimalWidth);
  }
};
