/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ColorScheme, FormFieldTile, PropertyChangeEvent} from '@eclipse-scout/core';
import {Chart, ChartField, ChartFieldTileModel} from '../index';
import {ChartConfig} from '../chart/Chart';

export class ChartFieldTile extends FormFieldTile implements ChartFieldTileModel {
  declare model: ChartFieldTileModel;
  declare tileWidget: ChartField;

  protected _chartConfigChangeHandler: (event: PropertyChangeEvent<ChartConfig, Chart>) => void;

  constructor() {
    super();
    this._chartConfigChangeHandler = this._onChartConfigChange.bind(this);
  }

  protected override _setColorScheme(colorScheme: ColorScheme | string) {
    super._setColorScheme(colorScheme);
    this._updateChartColorScheme();
  }

  protected _updateChartColorScheme() {
    let config = $.extend(true, {}, this.tileWidget.chart.config, {
      options: {
        colorScheme: this.colorScheme
      }
    });

    this.tileWidget.chart.setConfig(config);
  }

  protected _onChartConfigChange(event: PropertyChangeEvent<ChartConfig, Chart>) {
    this._updateChartColorScheme();
  }

  protected override _setTileWidget(tileWidget: ChartField) {
    if (this.tileWidget) {
      this.tileWidget.chart.off('propertyChange:config', this._chartConfigChangeHandler);
    }
    super._setTileWidget(tileWidget);
    this._updateChartColorScheme();
    this.tileWidget.chart.on('propertyChange:config', this._chartConfigChangeHandler);
  }
}
