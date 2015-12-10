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
scout.Column = function() {
  this.minWidth = scout.Column.DEFAULT_MIN_WIDTH;
  this.showSeparator = true; // currently a UI-only property, defaults to true
  this.filterType = 'ColumnUserFilter';
};

scout.Column.DEFAULT_MIN_WIDTH = 50;
scout.Column.NARROW_MIN_WIDTH = 30; // for columns without text (icon, check box)

scout.Column.DEFAULT_COMPARATOR = function(valueA, valueB) {
  if (valueA < valueB) {
    return -1;
  } else if (valueA > valueB) {
    return 1;
  }
  return 0;
};

scout.Column.prototype.init = function(model) {
  this.session = model.session;

  // Copy all properties from model to this
  $.extend(this, model);

  // Fill in the missing default values
  scout.defaultValues.applyTo(this);

  // InitialWidth is only sent if it differs from width
  if (this.initialWidth === undefined) {
    this.initialWidth = scout.nvl(this.width, 0);
  }

  if (this.aggregationFunction) {
    this.setAggregationFunction(this.aggregationFunction);
  }
};

/**
 * Converts the cell if it is of type string to an object with
 * a property 'text' with the original value.
 *
 * Example:
 * 'My Company' --> { text: 'MyCompany'; }
 *
 * @see JsonCell.java
 */
scout.Column.prototype.initCell = function(cell) {
  if (typeof cell !== 'object') {
    cell = {
      text: cell
    };
  }
  this._initCell(cell);
  // when cell doesn't define horiz. alignment - use value from column
  if (cell.horizontalAlignment === undefined) {
    cell.horizontalAlignment = this.horizontalAlignment;
  }
  scout.defaultValues.applyTo(cell, 'Cell');
  return cell;
};

/**
 * Override this method to impl. type specific init cell behavior.
 * The default impl. does nothing.
 */
scout.Column.prototype._initCell = function(cell) {
  // NOP
};

scout.Column.prototype.buildCellForRow = function(row) {
  var cell = this.table.cell(this, row);
  return this.buildCell(cell, row);
};

scout.Column.prototype.buildCell = function(cell, row) {
  var text = cell.text || '';
  if (!cell.htmlEnabled) {
    if (!cell.encodedText) {
      // Encode text and cache it, encoding is expensive
      cell.encodedText = scout.strings.encode(text);
    }
    text = cell.encodedText;
    if (this.table.multilineText) {
      text = scout.strings.nl2br(text, false);
    }
  }
  var iconId = cell.iconId;
  var icon = this._icon(iconId, !!text) || '';
  var cssClass = this._cellCssClass(cell);
  var style = this._cellStyle(cell);

  if (cell.errorStatus) {
    row.hasError = true;
  }

  var content;
  if (!text && !icon) {
    // If every cell of a row is empty the row would collapse, using nbsp makes sure the row is as height as the others even if it is empty
    content = '&nbsp;';
    cssClass = scout.strings.join(' ', cssClass, 'empty');
  } else {
    content = icon + text;
  }

  var cellHtml = '';
  cellHtml += '<div class="' + cssClass + '" style="' + style + '"' + scout.device.unselectableAttribute.string + '>';
  if (scout.device.tableAdditionalDivRequired) {
    cellHtml += '<div class="width-fix" style="max-width: ' + (this.width - this.table.cellHorizontalPadding - 2 /* unknown IE9 extra space */ ) + 'px; ' + '">';
    // same calculation in scout.Table.prototype.resizeColumn
  }
  cellHtml += content;
  if (scout.device.tableAdditionalDivRequired) {
    cellHtml += '</div>';
  }
  cellHtml += '</div>';
  return cellHtml;
};

scout.Column.prototype._icon = function(iconId, hasText) {
  var cssClass, icon;
  if (!iconId) {
    return;
  }
  cssClass = 'table-cell-icon';
  if (hasText) {
    cssClass += ' with-text';
  }
  icon = scout.icons.parseIconId(iconId);
  if (icon.isFontIcon()) {
    cssClass += ' font-icon';
    return '<span class="' + icon.appendCssClass(cssClass) + '">' + icon.iconCharacter + '</span>';
  } else {
    cssClass += ' image-icon';
    return '<img class="' + cssClass + '" src="' + icon.iconUrl + '">';
  }
};

scout.Column.prototype._cellCssClass = function(cell) {
  var cssClass = 'table-cell';
  if (this.mandatory) {
    cssClass += ' mandatory';
  }
  if (!this.table.multilineText || !this.textWrap) {
    cssClass += ' white-space-nowrap';
  }
  if (cell.editable) {
    cssClass += ' editable';
  }
  if (cell.errorStatus) {
    cssClass += ' has-error';
  }
  cssClass += ' halign-' + scout.Table.parseHorizontalAlignment(cell.horizontalAlignment);
  var columnPosition = this.table.columns.indexOf(this);
  if (columnPosition === 0) {
    cssClass += ' first';
  }
  if (columnPosition === this.table.columns.length - 1) {
    cssClass += ' last';
  }

  //TODO [5.2] cgu: cssClass is actually only sent for cells, should we change this in model? discuss with jgu
  if (cell.cssClass) {
    cssClass += ' ' + cell.cssClass;
  } else if (this.cssClass) {
    cssClass += ' ' + this.cssClass;
  }
  return cssClass;
};

scout.Column.prototype._cellStyle = function(cell) {
  var style,
    width = this.width;

  if (width === 0) {
    return 'display: none;';
  }

  style = 'min-width: ' + width + 'px; max-width: ' + width + 'px; ';
  style += scout.styles.legacyStyle(cell);

  if (this.backgroundEffect && cell.value !== undefined) {
    if (!this.backgroundEffectFunc) {
      this.backgroundEffectFunc = this._resolveBackgroundEffectFunc();
    }
    var backgroundStyle = this.backgroundEffectFunc(cell.value);
    if (backgroundStyle.backgroundColor) {
      style += 'background-color: ' + backgroundStyle.backgroundColor + ';';
    }
    if (backgroundStyle.backgroundImage) {
      style += 'background-image: ' + backgroundStyle.backgroundImage + ';';
    }
  }
  return style;
};

scout.Column.prototype.onMouseUp = function(event, $row) {
  var row = $row.data('row'),
    cell = this.table.cell(this, row);

  if (this.table.enabled && row.enabled && cell.editable && !event.ctrlKey && !event.shiftKey) {
    this.table.prepareCellEdit(row.id, this.id, true);
  }
};

scout.Column.prototype.startCellEdit = function(row, fieldId) {
  var popup,
    $row = row.$row,
    cell = this.table.cell(this, row),
    $cell = this.table.$cell(this, $row);

  cell.field = this.session.getOrCreateModelAdapter(fieldId, this.table);
  // Override field alignment with the cell's alignment
  cell.field.gridData.horizontalAlignment = cell.horizontalAlignment;

  popup = scout.create('CellEditorPopup', {
    parent: this.table,
    column: this,
    row: row,
    cell: cell
  });
  popup.$anchor = $cell;
  popup.open(this.table.$data);
  return popup;
};

// TODO CGU/AWE cleanup these cellValue/TextForXY methods, currently they are very confusing
/**
 * Returns the cell value to be used for grouping and filtering (chart, column filter).
 */
scout.Column.prototype.cellValueForGrouping = function(row) {
  var cell = this.table.cell(this, row);
  if (cell.value !== undefined) {
    return cell.value;
  }
  if (!cell.text) {
    return null;
  }
  return this._preprocessTextForValueGrouping(cell.text, cell.htmlEnabled);
};

scout.Column.prototype._preprocessTextForValueGrouping = function(text, htmlEnabled) {
  return this._preprocessText(text, {
    removeHtmlTags: htmlEnabled,
    removeNewlines: true,
    trim: true
  });
};

/**
 * Returns the cell text to be used for table grouping
 */
scout.Column.prototype.cellTextForGrouping = function(row) {
  var cell = this.table.cell(this, row);
  return this._preprocessTextForGrouping(cell.text, cell.htmlEnabled);
};

scout.Column.prototype._preprocessTextForGrouping = function(text, htmlEnabled) {
  return this._preprocessText(text, {
    removeHtmlTags: htmlEnabled,
    trim: true
  });
};

/**
 * Returns the cell text to be used for the text filter
 */
scout.Column.prototype.cellTextForTextFilter = function(row) {
  var cell = this.table.cell(this, row);
  return this._preprocessTextForTextFilter(cell.text, cell.htmlEnabled);
};

scout.Column.prototype._preprocessTextForTextFilter = function(text, htmlEnabled) {
  return this._preprocessText(text, {
    removeHtmlTags: htmlEnabled
  });
};

/**
 * Removes html tags, converts to single line, removes leading and trailing whitespaces.
 */
scout.Column.prototype._preprocessText = function(text, options) {
  options = options || {};
  if (options.removeHtmlTags) {
    text = scout.strings.plainText(text);
  }
  if (options.removeNewlines) {
    text = text.replace('\n', ' ');
  }
  if (options.trim) {
    text = text.trim();
  }
  return text;
};

scout.Column.prototype.setAggregationFunction = function(func) {
  this.aggregationFunction = func;
  if (func === 'sum') {
    this.aggrStart = scout.aggregation.sumStart;
    this.aggrStep = scout.aggregation.sumStep;
    this.aggrFinish = scout.aggregation.sumFinish;
    this.aggrSymbol = scout.aggregation.sumSymbol;
  } else if (func === 'avg') {
    this.aggrStart = scout.aggregation.avgStart;
    this.aggrStep = scout.aggregation.avgStep;
    this.aggrFinish = scout.aggregation.avgFinish;
    this.aggrSymbol = scout.aggregation.avgSymbol;
  } else if (func === 'min') {
    this.aggrStart = scout.aggregation.minStart;
    this.aggrStep = scout.aggregation.minStep;
    this.aggrFinish = scout.aggregation.minFinish;
    this.aggrSymbol = scout.aggregation.minSymbol;
  } else if (func === 'max') {
    this.aggrStart = scout.aggregation.maxStart;
    this.aggrStep = scout.aggregation.maxStep;
    this.aggrFinish = scout.aggregation.maxFinish;
    this.aggrSymbol = scout.aggregation.maxSymbol;
  }
};

scout.Column.prototype.setBackgroundEffect = function(effect, notifyServer) {
  if (this.backgroundEffect === effect) {
    return;
  }

  this.backgroundEffect = effect;
  this.backgroundEffectFunc = this._resolveBackgroundEffectFunc();

  notifyServer = scout.nvl(notifyServer, true);
  if (notifyServer) {
    this.table._sendColumnBackgroundEffectChanged(this);
  }

  if (this.backgroundEffect && (this.minValue === undefined || this.maxValue === undefined)) {
    // No need to calculate the values again when switching background effects
    // If background effect is turned off and on again values will be recalculated
    // This is necessary because in the meantime rows may got updated, deleted etc.
    this.calculateMinMaxValues();
  }
  if (!this.backgroundEffect) {
    // Clear to make sure values are calculated anew the next time a background effect gets set
    this.minValue = undefined;
    this.maxValue = undefined;
  }

  if (this.table.rendered) {
    this._renderBackgroundEffect();
  }
};

/**
 * Recalculates the min / max values and renders the background effect again.
 */
scout.Column.prototype.updateBackgroundEffect = function() {
  this.calculateMinMaxValues();
  if (this.table.rendered) {
    this._renderBackgroundEffect();
  }
};

scout.Column.prototype._resolveBackgroundEffectFunc = function() {
  var effect = this.backgroundEffect;
  // TODO [5.2] bsh: CRU Don't use hardcoded colors (or make them customizable)
  if (effect === 'colorGradient1') {
    return this._colorGradient1.bind(this);
  }
  if (effect === 'colorGradient2') {
    return this._colorGradient2.bind(this);
  }
  if (effect === 'barChart') {
    return this._barChart.bind(this);
  }

  if (effect !== null) {
    $.log.warn('Unsupported backgroundEffect: ' + effect);
    return function() {
      return {};
    };
  }
};

scout.Column.prototype._renderBackgroundEffect = function() {
  this.table.filteredRows().forEach(function(row) {
    if (!row.$row) {
      return;
    }
    var cell = this.table.cell(this, row),
      $cell = this.table.$cell(this, row.$row);

    if (cell.value !== undefined) {
      $cell[0].style.cssText = this._cellStyle(cell);
    }
  }, this);
};

scout.Column.prototype.calculateMinMaxValues = function() {
  var row, minValue, maxValue, value,
    rows = this.table.rows;

  for (var i = 0; i < rows.length; i++) {
    row = rows[i];
    value = this.table.cellValue(this, row);

    if (value < minValue || minValue === undefined) {
      minValue = value;
    }
    if (value > maxValue || maxValue === undefined) {
      maxValue = value;
    }
  }
  this.minValue = minValue;
  this.maxValue = maxValue;
};

scout.Column.prototype._colorGradient1 = function(value) {
  var startStyle = scout.styles.get('column-background-effect-gradient1-start', 'backgroundColor'),
    endStyle = scout.styles.get('column-background-effect-gradient1-end', 'backgroundColor'),
    startColor = scout.styles.rgb(startStyle.backgroundColor),
    endColor = scout.styles.rgb(endStyle.backgroundColor);

  return this._colorGradient(value, startColor, endColor);
};

scout.Column.prototype._colorGradient2 = function(value) {
  var startStyle = scout.styles.get('column-background-effect-gradient2-start', 'backgroundColor'),
    endStyle = scout.styles.get('column-background-effect-gradient2-end', 'backgroundColor'),
    startColor = scout.styles.rgb(startStyle.backgroundColor),
    endColor = scout.styles.rgb(endStyle.backgroundColor);

  return this._colorGradient(value, startColor, endColor);
};

scout.Column.prototype._colorGradient = function(value, startColor, endColor) {
  var level = (value - this.minValue) / (this.maxValue - this.minValue);

  var r = Math.ceil(startColor.red - level * (startColor.red - endColor.red)),
    g = Math.ceil(startColor.green - level * (startColor.green - endColor.green)),
    b = Math.ceil(startColor.blue - level * (startColor.blue - endColor.blue));

  return {
    backgroundColor: 'rgb(' + r + ',' + g + ', ' + b + ')'
  };
};

scout.Column.prototype._barChart = function(value) {
  var level = Math.ceil((value - this.minValue) / (this.maxValue - this.minValue) * 100) + '';
  var color = scout.styles.get('column-background-effect-bar-chart', 'backgroundColor').backgroundColor;
  return {
    backgroundImage: 'linear-gradient(to left, ' + color + ' 0%, ' + color + ' ' + level + '%, transparent ' + level + '%, transparent 100% )'
  };
};

/**
 * Returns a type specific column user-filter. The default impl. returns a ColumnUserFilter.
 * Sub-classes that must return another type, must simply change the value of the 'filterType' property.
 */
scout.Column.prototype.createFilter = function(model) {
  return scout.create(this.filterType, {
    session: this.session,
    table: this.table,
    column: this
  });
};

/**
 * Override this method to install a specific compare function on a column instance.
 * The default impl. installs a generic comparator working with less than and greater than.
 *
 * @return Whether or not it was possible to install a compare function. If not, client side sorting is disabled.
 *   Default impl. returns a
 */
scout.Column.prototype.prepareForSorting = function() {
  this.compare = scout.Column.DEFAULT_COMPARATOR;
  return true;
};
