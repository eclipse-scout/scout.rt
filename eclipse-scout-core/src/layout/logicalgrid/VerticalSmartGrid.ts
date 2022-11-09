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
      f.gridData = matrix.getGridData(f);
    });
    this.gridRows = matrix.getRowCount();
  }
}
