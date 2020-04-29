/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {BenchRowLayoutData} from '../../../index';
import $ from 'jquery';

export default class BenchColumnLayoutData {

  constructor(model) {
    // initial
    this.columns = [null, null, null];
    $.extend(this, model);

    this._ensureColumns();
  }

  _ensureColumns() {
    this.columns = this.columns.map((col, i) => {
      return new BenchRowLayoutData(col).withOrder(i * 2);
    });
  }

  getColumns() {
    return this.columns;
  }

  static ensure(layoutData) {
    if (!layoutData) {
      layoutData = new BenchColumnLayoutData();
      return layoutData;
    }
    if (layoutData instanceof BenchColumnLayoutData) {
      return layoutData;
    }
    return new BenchColumnLayoutData(layoutData);
  }
}
