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
  this.cssClass;
  this.editable = false;
  this.removable = false;
  this.modifiable = false;
  this.fixedWidth = false;
  this.horizontalAlignment = -1;
  this.htmlEnabled = false;
  this.mandatory = false;
  this.sortActive = false;
  this.sortAscending = true;
  this.sortIndex = -1;
  this.summary = false;
  this.type = 'text';
  this.width = 60;
  this.minWidth = scout.Column.DEFAULT_MIN_WIDTH;
  this.showSeparator = true; // currently a UI-only property, defaults to true
  this.filterType = 'TextColumnUserFilter';
  this.comparator = scout.comparators.TEXT;
};

scout.Column.DEFAULT_MIN_WIDTH = 50;
scout.Column.NARROW_MIN_WIDTH = 32; // for columns without text (icon, check box)

scout.Column.prototype.init = function(model) {
  this.session = model.session;

  // Copy all properties from model to this
  $.extend(this, model);

  // Fill in the missing default values
  scout.defaultValues.applyTo(this);

  // Initial width is only sent if it differs from width
  if (this.initialWidth === undefined) {
    this.initialWidth = scout.nvl(this.width, 0);
  }
  if (this.aggregationFunction) {
    this.setAggregationFunction(this.aggregationFunction);
  }
  scout.texts.resolveTextProperty(this);
  this._init(model);
};

/**
 * Override this function in order to implement custom init logic.
 * The default impl. does nothing.
 */
scout.Column.prototype._init = function(model) {
  // NOP
};

/**
 * Converts the vararg if it is of type string to an object with
 * a property 'text' with the original value.
 *
 * Example:
 * 'My Company' --> { text: 'MyCompany'; }
 *
 * @see JsonCell.java
 */
scout.Column.prototype.initCell = function(vararg, row) {
  if (vararg instanceof scout.Cell) {
    return vararg;
  }
  var cellModel = this._createCellModelInternal(vararg);
  return scout.create('Cell', cellModel);
};

scout.Column.prototype._createCellModelInternal = function(vararg) {
  var cellModel;
  if (vararg && scout.objects.isPlainObject(vararg)) {
    cellModel = vararg;
  } else {
    // in this case 'vararg' is only a scalar value, typically a string
    cellModel = this._createCellModel(vararg);
  }
  this._initCell(cellModel);
  return cellModel;
};

/**
 * Override this method to create a cell model object based on the given scalar value.
 */
scout.Column.prototype._createCellModel = function(text) {
  return {
    text: text
  };
};

/**
 * Override this method to impl. type specific init cell behavior.
 */
scout.Column.prototype._initCell = function(cellModel) {
  // if cell does not define properties, use column values
  cellModel.cssClass = scout.nvl(cellModel.cssClass, this.cssClass);
  cellModel.editable = scout.nvl(cellModel.editable, this.editable);
  cellModel.horizontalAlignment = scout.nvl(cellModel.horizontalAlignment, this.horizontalAlignment);
  cellModel.htmlEnabled = scout.nvl(cellModel.htmlEnabled, this.htmlEnabled);
};

scout.Column.prototype.buildCellForRow = function(row) {
  var cell = this.cell(row);
  return this.buildCell(cell, row);
};

scout.Column.prototype.buildCell = function(cell, row) {
  scout.assertParameter('cell', cell, scout.Cell);

  var text = cell.text || '';
  if (!cell.htmlEnabled) {
    text = cell.encodedText() || '';
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

  if (cell.cssClass) {
    cssClass += ' ' + cell.cssClass;
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
    var backgroundStyle = this.backgroundEffectFunc(this._preprocessValueForGrouping(cell.value));
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
    cell = this.cell(row);

  if (this.isCellEditable(row, cell, event)) {
    this.table.prepareCellEdit(this, row, true);
  }
};

scout.Column.prototype.isCellEditable = function(row, cell, event) {
  return this.table.enabled && row.enabled && cell.editable && !event.ctrlKey && !event.shiftKey;
};

scout.Column.prototype.startCellEdit = function(row, field) {
  var popup,
    $row = row.$row,
    cell = this.cell(row),
    $cell = this.table.$cell(this, $row);

  cell.field = field;
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
 * @returns the cell value to be used for grouping and filtering (chart, column filter).
 */
scout.Column.prototype.cellValueForGrouping = function(row) {
  var cell = this.cell(row);
  if (cell.value !== undefined) {
    return this._preprocessValueForGrouping(cell.value);
  }
  if (!cell.text) {
    return null;
  }
  return this._preprocessTextForValueGrouping(cell.text, cell.htmlEnabled);
};

scout.Column.prototype._preprocessValueForGrouping = function(value) {
  return value;
};

scout.Column.prototype._preprocessTextForValueGrouping = function(text, htmlEnabled) {
  return this._preprocessText(text, {
    removeHtmlTags: htmlEnabled,
    removeNewlines: true,
    trim: true
  });
};

/**
 * @returns the cell text to be used for table grouping
 */
scout.Column.prototype.cellTextForGrouping = function(row) {
  var cell = this.cell(row);
  return this._preprocessTextForGrouping(cell.text, cell.htmlEnabled);
};

/**
 * @returns the cell object for this column from the given row.
 */
scout.Column.prototype.cell = function(row) {
  return this.table.cell(this, row);
};

/**
 * @returns the cell object for this column from the first selected row in the table.
 */
scout.Column.prototype.selectedCell = function() {
  var selectedRow = this.table.selectedRow();
  return this.table.cell(this, selectedRow);
};

scout.Column.prototype._preprocessTextForGrouping = function(text, htmlEnabled) {
  return this._preprocessText(text, {
    removeHtmlTags: htmlEnabled,
    trim: true
  });
};

/**
 * @returns the cell text to be used for the text filter
 */
scout.Column.prototype.cellTextForTextFilter = function(row) {
  var cell = this.cell(row);
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
  if (text === null || text === undefined) {
    return text;
  }
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

scout.Column.prototype.setCellValue = function(row, value) {
  var cellModel = this._createCellModelInternal(value);
  this.cell(row).update(cellModel);
};

scout.Column.prototype.createAggrGroupCell = function(row) {
  var cell = this.cell(row);
  return this.initCell({
    // value necessary for value based columns (e.g. checkbox column)
    value: cell.value,
    text: this.cellTextForGrouping(row),
    iconId: cell.iconId,
    horizontalAlignment: this.horizontalAlignment,
    cssClass: 'table-aggregate-cell'
  });
};

scout.Column.prototype.createAggrEmptyCell = function() {
  return this.initCell({
    empty: true
  });
};

scout.Column.prototype.setBackgroundEffect = function(effect) {
  if (this.backgroundEffect === effect) {
    return;
  }

  this.backgroundEffect = effect;
  this.backgroundEffectFunc = this._resolveBackgroundEffectFunc();

  this.table.trigger('columnBackgroundEffectChanged', {
    column: this
  });

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
    var cell = this.cell(row),
      $cell = this.table.$cell(this, row.$row);

    if (cell.value !== undefined) {
      $cell[0].style.cssText = this._cellStyle(cell);
    }
  }, this);
};

scout.Column.prototype.calculateOptimalWidth = function() {
  var row, rows = this.table.rows,
    optimalWidth = this.minWidth;
  for (var i = 0; i < rows.length; i++) {
    row = rows[i];
    var $div = $(this.buildCellForRow(row));
    $div.removeAttr('style');
    $div.hide();
    this.table.$data.append($div);
    optimalWidth = optimalWidth < $div.outerWidth() ? $div.outerWidth() : optimalWidth;
    $div.remove();
  }
  return optimalWidth;
};

scout.Column.prototype.calculateMinMaxValues = function() {
  var row, minValue, maxValue, value,
    rows = this.table.rows;

  for (var i = 0; i < rows.length; i++) {
    row = rows[i];
    value = this.cellValueForGrouping(row);

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
 * @returns a field instance used as editor when a cell of this column is in edit mode.
 */
scout.Column.prototype.createDefaultEditor = function(row) {
  var field = scout.create('StringField', {
    parent: this.table,
    labelVisible: false
  });
  field.setValue(this.cell(row).text);
  return field;
};

/**
 * Override this function to install a specific compare function on a column instance.
 * The default impl. installs a generic comparator working with less than and greater than.
 *
 * @returns whether or not it was possible to install a compare function. If not, client side sorting is disabled.
 */
scout.Column.prototype.installComparator = function() {
  return this.comparator.install(this.session);
};

/**
 * @returns whether or not this column can be used to sort on the client side. In a JS only the flag 'uiSortPossible'
 *     is never set and defaults to true. As a side effect of this function a comparator is installed on each column.
 *     In a remote app the server sets the 'uiSortPossible' flag, which decides if the column must be sorted by the
 *     server or can be sorted by the client.
 */
scout.Column.prototype.isUiSortPossible = function() {
  var uiSortPossible = scout.nvl(this.uiSortPossible, true);
  return uiSortPossible && this.installComparator();
};

scout.Column.prototype.compare = function(row1, row2) {
  var valueA = this.table.cellValue(this, row1);
  var valueB = this.table.cellValue(this, row2);
  return this.comparator.compare(valueA, valueB);
};
