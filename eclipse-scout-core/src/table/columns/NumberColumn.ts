/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  aggregation, AvgAggregationState, Cell, Column, comparators, DecimalFormat, DecimalFormatOptions, InitModelOf, Locale, NumberColumnEventMap, NumberColumnModel, NumberField, numbers, objects, Rgba, scout, strings, styles, TableRow
} from '../../index';
import $ from 'jquery';

export type NumberColumnAggregationFunction = 'sum' | 'avg' | 'min' | 'max' | 'none';
export type NumberColumnBackgroundEffect = 'colorGradient1' | 'colorGradient2' | 'barChart';
export type NumberColumnBackgroundStyle = { backgroundColor?: string; backgroundImage?: string };
export type NumberColumnBackgroundEffectFunc = (value: number) => NumberColumnBackgroundStyle;

export class NumberColumn extends Column<number> implements NumberColumnModel {
  declare model: NumberColumnModel;
  declare eventMap: NumberColumnEventMap;
  declare self: NumberColumn;

  aggregationFunction: NumberColumnAggregationFunction;
  backgroundEffect: NumberColumnBackgroundEffect;
  decimalFormat: DecimalFormat;
  fractionDigits: number;
  minValue: number;
  maxValue: number;

  /** the calculated min value of all rows */
  calcMinValue: number;

  /** the calculated max value of all rows */
  calcMaxValue: number;

  allowedAggregationFunctions: NumberColumnAggregationFunction[];

  aggrStart: () => number | AvgAggregationState;
  aggrStep: (currentState?: number | AvgAggregationState, newVal?: number) => number | AvgAggregationState;
  aggrFinish: (currentState?: number | AvgAggregationState) => number;
  aggrSymbol: string;
  backgroundEffectFunc: NumberColumnBackgroundEffectFunc;

  constructor() {
    super();
    this.aggregationFunction = 'sum';
    this.backgroundEffect = null;
    this.decimalFormat = null;
    this.fractionDigits = null;
    this.minValue = null;
    this.maxValue = null;
    this.calcMinValue = null;
    this.calcMaxValue = null;
    this.horizontalAlignment = 1;
    this.filterType = 'NumberColumnUserFilter';
    this.comparator = comparators.NUMERIC;
    this.textBased = false;
    this.allowedAggregationFunctions = ['sum', 'avg', 'min', 'max', 'none'];
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._setDecimalFormat(this.decimalFormat);
    this._setFractionDigits(this.fractionDigits);
    this._setAggregationFunction(this.aggregationFunction);
  }

  setDecimalFormat(decimalFormat: DecimalFormat | string | DecimalFormatOptions) {
    this.setProperty('decimalFormat', decimalFormat);
  }

  protected _setDecimalFormat(decimalFormat: DecimalFormat | string | DecimalFormatOptions) {
    if (!decimalFormat) {
      decimalFormat = this._getDefaultFormat(this.session.locale);
    }
    let format = DecimalFormat.ensure(this.session.locale, decimalFormat);
    this._setProperty('decimalFormat', format);
    if (this.initialized) {
      // if format changes on the fly, just update the cell text
      this.table.rows.forEach(row => this._updateCellText(row, this.cell(row)));
    }
  }

  protected _getDefaultFormat(locale: Locale): string {
    return locale.decimalFormatPatternDefault;
  }

  protected override _formatValue(value: number, row?: TableRow): string {
    return this.decimalFormat.format(value);
  }

  setFractionDigits(fractionDigits: number) {
    this.setProperty('fractionDigits', fractionDigits);
  }

  protected _setFractionDigits(fractionDigits: number) {
    this._setProperty('fractionDigits', fractionDigits);
  }

  protected override _ensureValue(value: number | string): number {
    // server sends cell.value only if it differs from text -> make sure cell.value is set and has the right type
    return numbers.ensure(value);
  }

  setAggregationFunction(aggregationFunction: NumberColumnAggregationFunction) {
    this.setProperty('aggregationFunction', aggregationFunction);
  }

  protected _setAggregationFunction(func: NumberColumnAggregationFunction) {
    this._setProperty('aggregationFunction', func);
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

  override createAggrValueCell(value: number): Cell<number> {
    let formattedValue = this._formatValue(value);
    let cellType = Cell<number>;
    return scout.create(cellType, {
      text: formattedValue,
      iconId: (formattedValue ? this.aggrSymbol : null),
      horizontalAlignment: this.horizontalAlignment,
      cssClass: 'table-aggregate-cell ' + this.aggregationFunction,
      flowsLeft: this.horizontalAlignment > 0
    });
  }

  protected override _cellStyle(cell: Cell<number>, tableNodeColumn?: boolean, rowPadding?: number): string {
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

  protected override _preprocessValueOrTextForCalculation(value: number, cell?: Cell<number>): number {
    return this.decimalFormat.round(value);
  }

  setBackgroundEffect(backgroundEffect: NumberColumnBackgroundEffect) {
    let changed = this.setProperty('backgroundEffect', backgroundEffect);
    if (!changed) {
      return;
    }
    this.backgroundEffectFunc = this._resolveBackgroundEffectFunc();

    this.table.trigger('columnBackgroundEffectChanged', {
      column: this
    });

    if (this.backgroundEffect && (this.calcMinValue === null || this.calcMaxValue === null)) {
      // No need to calculate the values again when switching background effects
      // If background effect is turned off and on again values will be recalculated
      // This is necessary because in the meantime rows may get updated, deleted etc.
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

  protected _resolveBackgroundEffectFunc(): NumberColumnBackgroundEffectFunc {
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

  /** @internal */
  _renderBackgroundEffect() {
    if (!this.visible) {
      return;
    }
    this.table.visibleRows.forEach(row => {
      if (!row.$row) {
        return;
      }
      let cell = this.cell(row),
        $cell = this.table.$cell(this, row.$row);

      if (cell.value !== undefined) {
        $cell[0].style.cssText = this._cellStyle(cell);
      }
    });
  }

  calculateMinMaxValues() {
    let calcMinValue: number, calcMaxValue: number,
      rows = this.table.rows;

    for (let i = 0; i < rows.length; i++) {
      let row = rows[i];
      let value = this.cellValueOrTextForCalculation(row) as number;

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

  protected _colorGradient1(value: number): NumberColumnBackgroundStyle {
    let startStyle = styles.get('column-background-effect-gradient1-start', 'backgroundColor'),
      endStyle = styles.get('column-background-effect-gradient1-end', 'backgroundColor'),
      startColor = styles.rgb(startStyle.backgroundColor),
      endColor = styles.rgb(endStyle.backgroundColor);

    return this._colorGradient(value, startColor, endColor);
  }

  protected _colorGradient2(value: number): NumberColumnBackgroundStyle {
    let startStyle = styles.get('column-background-effect-gradient2-start', 'backgroundColor'),
      endStyle = styles.get('column-background-effect-gradient2-end', 'backgroundColor'),
      startColor = styles.rgb(startStyle.backgroundColor),
      endColor = styles.rgb(endStyle.backgroundColor);

    return this._colorGradient(value, startColor, endColor);
  }

  protected _colorGradient(value: number, startColor: Rgba, endColor: Rgba): NumberColumnBackgroundStyle {
    let level = (value - this.calcMinValue) / (this.calcMaxValue - this.calcMinValue);

    let r = Math.ceil(startColor.red - level * (startColor.red - endColor.red)),
      g = Math.ceil(startColor.green - level * (startColor.green - endColor.green)),
      b = Math.ceil(startColor.blue - level * (startColor.blue - endColor.blue));

    return {
      backgroundColor: 'rgb(' + r + ', ' + g + ', ' + b + ')'
    };
  }

  protected _barChart(value: number): NumberColumnBackgroundStyle {
    let level = Math.ceil((value - this.calcMinValue) / (this.calcMaxValue - this.calcMinValue) * 100) + '';
    let color = styles.get('column-background-effect-bar-chart', 'backgroundColor').backgroundColor;
    return {
      backgroundImage: 'linear-gradient(to left, ' + color + ' 0%, ' + color + ' ' + level + '%, transparent ' + level + '%, transparent 100% )'
    };
  }

  protected override _createEditor(row: TableRow): NumberField {
    return scout.create(NumberField, {
      parent: this.table,
      maxValue: this.maxValue,
      minValue: this.minValue,
      decimalFormat: this.decimalFormat,
      fractionDigits: this.fractionDigits
    });
  }

  protected override _hasCellValue(cell: Cell<number>): boolean {
    return !objects.isNullOrUndefined(cell.value); // Zero (0) is valid too
  }
}
