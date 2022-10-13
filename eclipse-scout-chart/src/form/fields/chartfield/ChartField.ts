/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FormField} from '@eclipse-scout/core';
import {Chart, ChartFieldModel} from '../../../index';

export default class ChartField extends FormField implements ChartFieldModel {
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
}
