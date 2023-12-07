/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractGrid, LogicalGridWidget, VerticalGridMatrix} from '../../index';

export class VerticalSmartGrid extends AbstractGrid {

  override layoutAllDynamic(widgets: LogicalGridWidget[]) {
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
      f._setGridData(matrix.getGridData(f));
    });
    this.gridRows = matrix.getRowCount();
  }
}
