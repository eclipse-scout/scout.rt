/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BenchColumnLayoutDataModel, BenchRowLayoutData, InitModelOf, ObjectOrModel} from '../../../index';
import $ from 'jquery';

export class BenchColumnLayoutData implements BenchColumnLayoutDataModel {
  declare model: BenchColumnLayoutDataModel;

  columns: BenchRowLayoutData[];

  constructor(model?: InitModelOf<BenchColumnLayoutData>) {
    this.columns = [null, null, null];
    $.extend(this, model);

    this._ensureColumns();
  }

  protected _ensureColumns() {
    this.columns = this.columns.map((col, i) => {
      return new BenchRowLayoutData(col).withOrder(i * 2);
    });
  }

  getColumns(): BenchRowLayoutData[] {
    return this.columns;
  }

  static ensure(layoutData: ObjectOrModel<BenchColumnLayoutData>): BenchColumnLayoutData {
    if (!layoutData) {
      return new BenchColumnLayoutData();
    }
    if (layoutData instanceof BenchColumnLayoutData) {
      return layoutData;
    }
    return new BenchColumnLayoutData(layoutData);
  }
}
