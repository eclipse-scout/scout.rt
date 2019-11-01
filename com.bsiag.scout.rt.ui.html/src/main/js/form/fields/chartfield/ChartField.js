/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FormField} from '@eclipse-scout/core';

export default class ChartField extends FormField {

  constructor() {
    super();
    this._addWidgetProperties(['chart']);
  }


  _render() {
    this.addContainer(this.$parent, 'chart-field');
    this.addLabel();
    this.addMandatoryIndicator();
    this.addStatus();
    this._renderChart();
  }

  _renderChart() {
    if (this.chart) {
      this.chart.render();
      this.addField(this.chart.$container);
    }
  }

  _removeChart() {
    this.chart.remove();
    this._removeField();
  }

  _renderOnAttach() {
    super._renderOnAttach();
    if (this.chart && this.chart.chartRenderer) {
      this.chart.chartRenderer.checkCompletlyRendered();
    }
  }
}
