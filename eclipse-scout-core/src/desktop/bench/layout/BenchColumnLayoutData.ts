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
import {BenchRowLayoutData} from '../../../index';
import $ from 'jquery';
import BenchColumnLayoutDataModel from './BenchColumnLayoutDataModel';

export default class BenchColumnLayoutData implements BenchColumnLayoutDataModel {
  declare model: BenchColumnLayoutDataModel;

  columns: BenchRowLayoutData[];

  constructor(model?: BenchColumnLayoutDataModel) {
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

  static ensure(layoutData: BenchColumnLayoutData | BenchColumnLayoutDataModel): BenchColumnLayoutData {
    if (!layoutData) {
      return new BenchColumnLayoutData();
    }
    if (layoutData instanceof BenchColumnLayoutData) {
      return layoutData;
    }
    return new BenchColumnLayoutData(layoutData);
  }
}
