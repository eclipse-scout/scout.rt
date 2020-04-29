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
import {FlexboxLayoutData} from '../../../index';

export default class BenchRowLayoutData extends FlexboxLayoutData {

  constructor(model) {
    super(model);
    this._ensureRows();
  }

  _prepare() {
    super._prepare();
    this.rows = [null, null, null];
  }

  getRows() {
    return this.rows;
  }

  _ensureRows() {
    this.rows = this.rows.map((row, i) => {
      return new FlexboxLayoutData(row).withOrder(i * 2);
    });
  }

  updateVisibilities(rows) {
    rows.forEach((row, index) => {
      this.rows[index].visible = row.rendered;

    });
  }
}
