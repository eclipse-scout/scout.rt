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
import {aggregation, Column, comparators, DecimalFormat, numbers, objects, scout, strings, styles} from '../../index';
import $ from 'jquery';

export default class NumberColumn extends Column {

  constructor() {
    super();
    this.aggregationFunction = 'sum';
    this.backgroundEffect = null;
    this.decimalFormat = null;
    this.minValue = null;
    this.maxValue = null;
    this.calcMinValue = null; // the calculated min value of all rows
    this.calcMaxValue = null; // the calculated max value of all rows
    this.horizontalAlignment = 1;
    this.filterType = 'NumberColumnUserFilter';
    this.comparator = comparators.NUMERIC;
    this.textBased = false;
    this.allowedAggregationFunctions = ['sum', 'avg', 'min', 'max', 'none'];
  }

  /**
   * @override Column.js
   */
  _init(model) {
    super._init(model);
    this._setDecimalFormat(this.decimalFormat);
    this.setAggregationFunction(this.aggregationFunction);
  }

  setDecimalFormat(decimalFormat) {
    if (this.decimalFormat === decimalFormat) {
      return;
    }
    this._setDecimalFormat(decimalFormat);
    if (this.initialized) {
      // if format changes on the fly, just update the cell text
      this.table.rows.forEach(row => {
        this._updateCellText(row, this.cell(row));
      });
    }
  }

  _setDecimalFormat(format) {
    if (!format) {
      format = this._getDefaultFormat(this.session.locale);
    }
    this.decimalFormat = DecimalFormat.ensure(this.session.locale, format);
  }

  _getDefaultFormat(locale) {
    return locale.decimalFormatPatternDefault;
  }

  /**
   * @override Columns.js
   */
  _formatValue(value, row) {
    return this.decimalFormat.format(value);
  }

  /**
   * @override Column.js
   */
  _parseValue(value) {
    // server sends cell.value only if it differs from text -> make sure cell.value is set and has the right type
    return numbers.ensure(value);
  }

  setAggregationFunction(func) {
    this.aggregationFunction = func;
    if (func === 'sum') {
      this.aggrStart = aggregation.sumStart;
      this.aggrStep = aggregation.sumStep;
      this.aggrFinish = aggregation.sumFinish;
      this.aggrSymbol = aggregation.sumSymbol;
    } else if (func === 'avg') {
      this.aggrStart = aggregation.avgStart;
      this.aggrStep = aggregation.avgStep;
      this.aggrFinish = aggregation.avgFinish;
      this.aggrSymbol = aggregation.avgSymbol;
    } else if (func === 'min') {
      this.aggrStart = aggregation.minStart;
      this.aggrStep = aggregation.minStep;
      this.aggrFinish = aggregation.minFinish;
      this.aggrSymbol = aggregation.minSymbol;
    } else if (func === 'max') {
      this.aggrStart = aggregation.maxStart;
      this.aggrStep = aggregation.maxStep;
      this.aggrFinish = aggregation.maxFinish;
      this.aggrSymbol = aggregation.maxSymbol;
    } else if (func === 'none') {
      let undefinedFunc = () => undefined;
      this.aggrStart = undefinedFunc;
      this.aggrStep = undefinedFunc;
      this.aggrFinish = undefinedFunc;
      this.aggrSymbol = undefined;
    }
  }

  createAggrValueCell(value) {
    let formattedValue = this._formatValue(value);
    return scout.create('Cell', {
      text: formattedValue,
      iconId: (formattedValue ? this.aggrSymbol : null),
      horizontalAlignment: this.horizontalAlignment,
      cssClass: 'table-aggregate-cell ' + this.aggregationFunction,
      flowsLeft: this.horizontalAlignment > 0
    });
  }

  _cellStyle(cell, tableNodeColumn, rowPadding) {
    let style = super._cellStyle(cell, tableNodeColumn, rowPadding);
    if (!this.backgroundEffect || cell.value === undefined || strings.contains(cell.cssClass, 'table-aggregate-cell')) {
      return style;
    }
    if (!this.backgroundEffectFunc) {
      this.backgroundEffectFunc = this._resolveBackgroundEffectFunc();
    }
    let backgroundStyle = this.backgroundEffectFunc(this._preprocessValueOrTextForCalculation(cell.value));
    if (backgroundStyle.backgroundColor) {
      style += 'background-color: ' + backgroundStyle.backgroundColor + ';';
    }
    if (backgroundStyle.backgroundImage) {
      style += 'background-image: ' + backgroundStyle.backgroundImage + ';';
    }
    return style;
  }

  /**
   * @override Column.js
   */
  _preprocessValueOrTextForCalculation(value) {
    return this.decimalFormat.round(value);
  }

  setBackgroundEffect(effect) {
    if (this.backgroundEffect === effect) {
      return;
    }

    this.backgroundEffect = effect;
    this.backgroundEffectFunc = this._resolveBackgroundEffectFunc();

    this.table.trigger('columnBackgroundEffectChanged', {
      column: this
    });

    if (this.backgroundEffect && (this.calcMinValue === null || this.calcMaxValue === null)) {
      // No need to calculate the values again when switching background effects
      // If background effect is turned off and on again values will be recalculated
      // This is necessary because in the meantime rows may got updated, deleted etc.
      this.calculateMinMaxValues();
    }
    if (!this.backgroundEffect) {
      // Clear to make sure values are calculated anew the next time a background effect gets set
      this.calcMinValue = null;
      this.calcMaxValue = null;
    }

    if (this.table.rendered) {
      this._renderBackgroundEffect();
    }
  }

  /**
   * Recalculates the min / max values and renders the background effect again.
   */
  updateBackgroundEffect() {
    this.calculateMinMaxValues();
    if (this.table.rendered) {
      this._renderBackgroundEffect();
    }
  }

  _resolveBackgroundEffectFunc() {
    let effect = this.backgroundEffect;
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
      return () => ({});
    }
  }

  _renderBackgroundEffect() {
    this.table.visibleRows.forEach(function(row) {
      if (!row.$row) {
        return;
      }
      let cell = this.cell(row),
        $cell = this.table.$cell(this, row.$row);

      if (cell.value !== undefined) {
        $cell[0].style.cssText = this._cellStyle(cell);
      }
    }, this);
  }

  calculateMinMaxValues() {
    let row, calcMinValue, calcMaxValue, value,
      rows = this.table.rows;

    for (let i = 0; i < rows.length; i++) {
      row = rows[i];
      value = this.cellValueOrTextForCalculation(row);

      if (value < calcMinValue || calcMinValue === undefined) {
        calcMinValue = value;
      }
      if (value > calcMaxValue || calcMaxValue === undefined) {
        calcMaxValue = value;
      }
    }
    this.calcMinValue = scout.nvl(calcMinValue, null);
    this.calcMaxValue = scout.nvl(calcMaxValue, null);
  }

  _colorGradient1(value) {
    let startStyle = styles.get('column-background-effect-gradient1-start', 'backgroundColor'),
      endStyle = styles.get('column-background-effect-gradient1-end', 'backgroundColor'),
      startColor = styles.rgb(startStyle.backgroundColor),
      endColor = styles.rgb(endStyle.backgroundColor);

    return this._colorGradient(value, startColor, endColor);
  }

  _colorGradient2(value) {
    let startStyle = styles.get('column-background-effect-gradient2-start', 'backgroundColor'),
      endStyle = styles.get('column-background-effect-gradient2-end', 'backgroundColor'),
      startColor = styles.rgb(startStyle.backgroundColor),
      endColor = styles.rgb(endStyle.backgroundColor);

    return this._colorGradient(value, startColor, endColor);
  }

  _colorGradient(value, startColor, endColor) {
    let level = (value - this.calcMinValue) / (this.calcMaxValue - this.calcMinValue);

    let r = Math.ceil(startColor.red - level * (startColor.red - endColor.red)),
      g = Math.ceil(startColor.green - level * (startColor.green - endColor.green)),
      b = Math.ceil(startColor.blue - level * (startColor.blue - endColor.blue));

    return {
      backgroundColor: 'rgb(' + r + ',' + g + ', ' + b + ')'
    };
  }

  _barChart(value) {
    let level = Math.ceil((value - this.calcMinValue) / (this.calcMaxValue - this.calcMinValue) * 100) + '';
    let color = styles.get('column-background-effect-bar-chart', 'backgroundColor').backgroundColor;
    return {
      backgroundImage: 'linear-gradient(to left, ' + color + ' 0%, ' + color + ' ' + level + '%, transparent ' + level + '%, transparent 100% )'
    };
  }

  /**
   * @override Column.js
   */
  _createEditor() {
    return scout.create('NumberField', {
      parent: this.table,
      maxValue: this.maxValue,
      minValue: this.minValue,
      decimalFormat: this.decimalFormat
    });
  }

  _hasCellValue(cell) {
    return !objects.isNullOrUndefined(cell.value); // Zero (0) is valid too
  }
}
