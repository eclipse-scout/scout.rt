/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ChartTableControlConfigHelper, Page, scout, TableMatrixDateGroup, TableMatrixNumberGroup} from '@eclipse-scout/core';
import {ChartTableControl, ChartTableControlConfigDo, ChartTableUserFilter, TableControlChartType} from '../index';

export class ChartTableControlConfigHelperImpl extends ChartTableControlConfigHelper {

  protected override async _exportConfig(page: Page): Promise<ChartTableControlConfigDo> {
    if (!page?.detailTable) {
      return null;
    }
    let chartTableControl = page.detailTable.findTableControl(ChartTableControl);
    if (chartTableControl && (chartTableControl.selected || page.detailTable.filters.some(filter => filter instanceof ChartTableUserFilter))) {
      return scout.create(ChartTableControlConfigDo, {
        selected: chartTableControl.selected,
        chartTypeId: chartTableControl.chartType,
        chartGroup1ColumnId: chartTableControl.chartGroup1?.id,
        chartGroup1Modifier: chartTableControl.chartGroup1?.modifier,
        chartGroup2ColumnId: chartTableControl.chartGroup2?.id,
        chartGroup2Modifier: chartTableControl.chartGroup2?.modifier,
        chartAggregationColumnId: chartTableControl.chartAggregation?.id,
        chartAggregationModifier: chartTableControl.chartAggregation?.modifier
      });
    }
    return null;
  }

  protected override async _importConfig(page: Page, config: ChartTableControlConfigDo): Promise<void> {
    if (!page?.detailTable || !config) {
      return;
    }
    let chartTableControl = page.detailTable.findTableControl(ChartTableControl);
    if (chartTableControl) {
      chartTableControl.setSelected(!!config.selected);
      chartTableControl.setChartType(config.chartTypeId as TableControlChartType);
      chartTableControl.setChartGroup1({id: config.chartGroup1ColumnId, modifier: config.chartGroup1Modifier as TableMatrixNumberGroup | TableMatrixDateGroup});
      chartTableControl.setChartGroup2({id: config.chartGroup2ColumnId, modifier: config.chartGroup2Modifier as TableMatrixNumberGroup | TableMatrixDateGroup});
      chartTableControl.setChartAggregation({id: config.chartAggregationColumnId, modifier: config.chartAggregationModifier as TableMatrixNumberGroup});
    }
  }
}
