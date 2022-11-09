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
