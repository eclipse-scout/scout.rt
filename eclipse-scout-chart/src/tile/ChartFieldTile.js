/*
 * Copyright (c) 2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FormFieldTile} from '@eclipse-scout/core';

export default class ChartFieldTile extends FormFieldTile {

  constructor() {
    super();
    this._chartRendererChangeHandler = this._onChartRendererChange.bind(this);
  }

  _renderColorScheme() {
    super._renderColorScheme();
    this._updateChartRendererColorScheme();
  }

  _updateChartRendererColorScheme() {
    if (!this.$container) {
      return;
    }
    let colorScheme = 'tile';
    if (this.$container.hasClass('color-alternative')) {
      colorScheme += ' color-alternative';
    }
    if (this.$container.hasClass('color-rainbow')) {
      colorScheme += ' color-rainbow';
    }
    if (this.$container.hasClass('inverted')) {
      colorScheme += ' inverted';
    }
    this.tileWidget.chart.chartRenderer.renderColorScheme(colorScheme);
  }

  _onChartRendererChange(event) {
    this._updateChartRendererColorScheme();
  }

  _setTileWidget(tileWidget) {
    if (this.tileWidget) {
      this.tileWidget.chart.off('propertyChange:chartRenderer', this._chartRendererChangeHandler);
    }
    super._setTileWidget(tileWidget);
    this.tileWidget.chart.on('propertyChange:chartRenderer', this._chartRendererChangeHandler);
  }
}
