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
}
