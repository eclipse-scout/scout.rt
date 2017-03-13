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
scout.NumberColumn = function() {
  scout.NumberColumn.parent.call(this);
  this.aggregationFunction = 'sum';
  this.backgroundEffect = null;
  this.decimalFormat = null;
  this.horizontalAlignment = 1;
  this.filterType = 'NumberColumnUserFilter';
  this.comparator = scout.comparators.NUMERIC;
  this.textBased = false;
};
scout.inherits(scout.NumberColumn, scout.Column);

/**
 * @override Column.js
 */
scout.NumberColumn.prototype._init = function(model) {
  scout.NumberColumn.parent.prototype._init.call(this, model);
  this._setDecimalFormat(this.decimalFormat);
  this.setAggregationFunction(this.aggregationFunction);
};

scout.NumberColumn.prototype._setDecimalFormat = function(format) {
  if (!format) {
    format = this._getDefaultFormat(this.session.locale);
  }
  format = scout.DecimalFormat.ensure(this.session.locale, format);
  this.decimalFormat = format;
};

scout.NumberColumn.prototype._getDefaultFormat = function(locale) {
  return locale.decimalFormatPatternDefault;
};

/**
 * @override Columns.js
 */
scout.NumberColumn.prototype._formatValue = function(value) {
  return this.decimalFormat.format(value);
};

/**
 * @override Column.js
 */
scout.NumberColumn.prototype._parseValue = function(value) {
  // server sends cell.value only if it differs from text -> make sure cell.value is set and has the right type
  return scout.numbers.ensure(value);
};

scout.NumberColumn.prototype.setAggregationFunction = function(func) {
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
  } else if (func === 'none') {
    var undefinedFunc = function() {
      return undefined;
    };
    this.aggrStart = undefinedFunc;
    this.aggrStep = undefinedFunc;
    this.aggrFinish = undefinedFunc;
    this.aggrSymbol = undefined;
  }
};

scout.NumberColumn.prototype.createAggrValueCell = function(value) {
  var formattedValue = this._formatValue(value);
  return scout.create('Cell', {
    text: formattedValue,
    iconId: (formattedValue ? this.aggrSymbol : null),
    horizontalAlignment: this.horizontalAlignment,
    cssClass: 'table-aggregate-cell'
  });
};

scout.NumberColumn.prototype._cellStyle = function(cell) {
  var style = scout.NumberColumn.parent.prototype._cellStyle.call(this, cell);

  if (this.backgroundEffect && cell.value !== undefined) {
    if (!this.backgroundEffectFunc) {
      this.backgroundEffectFunc = this._resolveBackgroundEffectFunc();
    }
    var backgroundStyle = this.backgroundEffectFunc(this._preprocessValueOrTextForCalculation(cell.value));
    if (backgroundStyle.backgroundColor) {
      style += 'background-color: ' + backgroundStyle.backgroundColor + ';';
    }
    if (backgroundStyle.backgroundImage) {
      style += 'background-image: ' + backgroundStyle.backgroundImage + ';';
    }
  }
  return style;
};

/**
 * @override Column.js
 */
scout.NumberColumn.prototype._preprocessValueOrTextForCalculation = function(value) {
  return this.decimalFormat.round(value);
};

scout.NumberColumn.prototype.setBackgroundEffect = function(effect) {
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
scout.NumberColumn.prototype.updateBackgroundEffect = function() {
  this.calculateMinMaxValues();
  if (this.table.rendered) {
    this._renderBackgroundEffect();
  }
};

scout.NumberColumn.prototype._resolveBackgroundEffectFunc = function() {
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

scout.NumberColumn.prototype._renderBackgroundEffect = function() {
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

scout.NumberColumn.prototype.calculateMinMaxValues = function() {
  var row, minValue, maxValue, value,
    rows = this.table.rows;

  for (var i = 0; i < rows.length; i++) {
    row = rows[i];
    value = this.cellValueOrTextForCalculation(row);

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

scout.NumberColumn.prototype._colorGradient1 = function(value) {
  var startStyle = scout.styles.get('column-background-effect-gradient1-start', 'backgroundColor'),
    endStyle = scout.styles.get('column-background-effect-gradient1-end', 'backgroundColor'),
    startColor = scout.styles.rgb(startStyle.backgroundColor),
    endColor = scout.styles.rgb(endStyle.backgroundColor);

  return this._colorGradient(value, startColor, endColor);
};

scout.NumberColumn.prototype._colorGradient2 = function(value) {
  var startStyle = scout.styles.get('column-background-effect-gradient2-start', 'backgroundColor'),
    endStyle = scout.styles.get('column-background-effect-gradient2-end', 'backgroundColor'),
    startColor = scout.styles.rgb(startStyle.backgroundColor),
    endColor = scout.styles.rgb(endStyle.backgroundColor);

  return this._colorGradient(value, startColor, endColor);
};

scout.NumberColumn.prototype._colorGradient = function(value, startColor, endColor) {
  var level = (value - this.minValue) / (this.maxValue - this.minValue);

  var r = Math.ceil(startColor.red - level * (startColor.red - endColor.red)),
    g = Math.ceil(startColor.green - level * (startColor.green - endColor.green)),
    b = Math.ceil(startColor.blue - level * (startColor.blue - endColor.blue));

  return {
    backgroundColor: 'rgb(' + r + ',' + g + ', ' + b + ')'
  };
};

scout.NumberColumn.prototype._barChart = function(value) {
  var level = Math.ceil((value - this.minValue) / (this.maxValue - this.minValue) * 100) + '';
  var color = scout.styles.get('column-background-effect-bar-chart', 'backgroundColor').backgroundColor;
  return {
    backgroundImage: 'linear-gradient(to left, ' + color + ' 0%, ' + color + ' ' + level + '%, transparent ' + level + '%, transparent 100% )'
  };
};
