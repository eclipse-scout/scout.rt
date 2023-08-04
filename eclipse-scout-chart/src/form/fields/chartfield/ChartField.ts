/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormField} from '@eclipse-scout/core';
import {Chart, ChartFieldModel} from '../../../index';

export class ChartField extends FormField implements ChartFieldModel {
  declare model: ChartFieldModel;

  chart: Chart;

  constructor() {
    super();
    this._addWidgetProperties(['chart']);

    this.chart = null;
  }

  protected override _render() {
    this.addContainer(this.$parent, 'chart-field');
    this.addLabel();
    this.addMandatoryIndicator();
    this.addStatus();
    this._renderChart();
  }

  protected _renderChart() {
    if (this.chart) {
      this.chart.render();
      this.addField(this.chart.$container);
    }
  }

  protected _removeChart() {
    this.chart.remove();
    this._removeField();
  }

  protected override _linkWithLabel($element: JQuery) {
    // nop, the chart renderers will take care of labeling and describing the chart
  }
}
