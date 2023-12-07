/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {GridData, LogicalGrid, LogicalGridContainer, LogicalGridOptions, LogicalGridWidget} from '../../index';

export abstract class AbstractGrid extends LogicalGrid {
  gridRows: number;
  gridColumns: number;

  constructor(options?: LogicalGridOptions) {
    super(options);
    this.gridRows = 0;
    this.gridColumns = 0;
  }

  /**
   * Expects {@link gridConfig} to be set.
   */
  protected _validate(gridContainer: LogicalGridContainer) {
    // reset old state
    this.gridRows = 0;
    this.gridConfig.setWidget(gridContainer);
    // step 0: column count
    this.gridColumns = this.gridConfig.getGridColumnCount();
    let containingGridXYCount = 0;
    let notContainingGridXYCount = 0;
    // build
    let widgets = [];
    this.gridConfig.getGridWidgets().forEach(widget => {
      if (widget.visible) {
        widgets.push(widget);
        let hints = widget.gridDataHints;
        if (hints && hints.x >= 0 && hints.y >= 0) {
          containingGridXYCount++;
        } else {
          notContainingGridXYCount++;
        }
      } else {
        widget._setGridData(GridData.createFromHints(widget, 1));
      }
    });
    if (containingGridXYCount > 0 && notContainingGridXYCount === 0) {
      this.layoutAllStatic(widgets);
    } else {
      this.layoutAllDynamic(widgets);
    }
  }

  layoutAllStatic(widgets: LogicalGridWidget[]) {
    let hints = [];
    widgets.forEach(v => {
      hints.push(GridData.createFromHints(v, 1));
    });
    let totalGridW = hints.reduce((x, y) => {
      let y1 = y.x + y.w;
      return y1 > x ? y1 : x;
    }, 1);
    let totalGridH = hints.reduce((x, y) => {
      let y1 = y.y + y.h;
      return y1 > x ? y1 : x;
    }, 0);
    widgets.forEach(v => {
      v._setGridData(GridData.createFromHints(v, totalGridW));
    });
    this.gridRows = totalGridH;
    this.gridColumns = totalGridW;
  }

  layoutAllDynamic(widgets: LogicalGridWidget[]) {
    // abstract, must be implemented by sub classes
  }

  getGridColumnCount(): number {
    return this.gridColumns;
  }

  getGridRowCount(): number {
    return this.gridRows;
  }

  /**
   * If grid w is greater than column count, grid w will be set to the column count.
   */
  static getGridDataFromHints(widget: LogicalGridWidget, groupBoxColumnCount: number): GridData {
    let data = GridData.createFromHints(widget, groupBoxColumnCount);
    data.w = Math.min(groupBoxColumnCount, data.w);
    return data;
  }
}
