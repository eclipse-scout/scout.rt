/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {IChartTableControlConfigDo, ObjectWithType, Page} from '../index';

export class ChartTableControlConfigHelper implements ObjectWithType {

  objectType: string;

  exportConfig(page: Page): JQuery.Promise<IChartTableControlConfigDo> {
    return $.when(this._exportConfig(page));
  }

  protected async _exportConfig(page: Page): Promise<IChartTableControlConfigDo> {
    return null;
  }

  importConfig(page: Page, config: IChartTableControlConfigDo): JQuery.Promise<void> {
    return $.when(this._importConfig(page, config));
  }

  protected async _importConfig(page: Page, config: IChartTableControlConfigDo): Promise<void> {
  }
}
