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
import {AbstractGrid, VerticalGridMatrix} from '../../index';

export default class VerticalSmartGrid extends AbstractGrid {

  constructor(options) {
    super(options);
  }

  layoutAllDynamic(widgets) {
    let cellCount = 0;
    widgets.forEach(f => {
      let hints = AbstractGrid.getGridDataFromHints(f, this.getGridColumnCount());
      cellCount += hints.w * hints.h;
    });

    // do the calc
    let rowCount = Math.floor((cellCount + this.getGridColumnCount() - 1) / this.getGridColumnCount());
    let matrix = new VerticalGridMatrix(this.getGridColumnCount(), rowCount);
    while (!matrix.computeGridData(widgets)) {
      matrix.resetAll(this.getGridColumnCount(), ++rowCount);
    }

    // set gridData
    widgets.forEach(f => {
      f.gridData = matrix.getGridData(f);
    });
    this.gridRows = matrix.getRowCount();
  }
}
