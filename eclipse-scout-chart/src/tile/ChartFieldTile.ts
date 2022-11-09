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
import {FormFieldTile, PropertyChangeEvent} from '@eclipse-scout/core';
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

  protected override _renderColorScheme() {
    super._renderColorScheme();
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
    this.tileWidget.chart.on('propertyChange:config', this._chartConfigChangeHandler);
  }
}
