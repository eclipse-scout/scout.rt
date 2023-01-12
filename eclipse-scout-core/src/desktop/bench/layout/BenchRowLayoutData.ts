/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BenchRowLayoutDataModel, FlexboxLayoutData, InitModelOf, scout} from '../../../index';

export class BenchRowLayoutData extends FlexboxLayoutData implements BenchRowLayoutDataModel {
  declare model: BenchRowLayoutDataModel;

  rows: FlexboxLayoutData[];

  constructor(model?: InitModelOf<BenchRowLayoutData>) {
    super(model);
    model = model || {};
    this.rows = scout.nvl(model.rows, [null, null, null]);
    this._ensureRows();
  }

  getRows(): FlexboxLayoutData[] {
    return this.rows;
  }

  protected _ensureRows() {
    this.rows = this.rows.map((row, i) => new FlexboxLayoutData(row).withOrder(i * 2));
  }
}
