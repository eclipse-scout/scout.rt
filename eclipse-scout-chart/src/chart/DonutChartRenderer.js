/*
 * Copyright (c) 2014-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {PieChartRenderer} from '../index';

export default class DonutChartRenderer extends PieChartRenderer {

  constructor(chart) {
    super(chart);
  }

  _renderInternal() {
    let outerCircleR = Math.min(this.chartBox.height, this.chartBox.width) / 2;
    this.centerCircleR = outerCircleR * 0.65; // donut thickness = 35% of outer circle
    super._renderInternal();
  }

  _renderPieChartPercentage(midPoint, percentage) {
    // NOP
  }
}
