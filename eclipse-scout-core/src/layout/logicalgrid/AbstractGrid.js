/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {GridData, LogicalGrid} from '../../index';

/**
 * @abstract
 */
export default class AbstractGrid extends LogicalGrid {

  constructor(options) {
    super(options);
    this.gridRows = 0;
    this.gridColumns = 0;
  }

  /**
   * Expects this.gridConfig to be set
   * @override
   */
  _validate(gridContainer) {
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
      if (widget.isVisible()) {
        widgets.push(widget);
        let hints = widget.gridDataHints;
        if (hints && hints.x >= 0 && hints.y >= 0) {
          containingGridXYCount++;
        } else {
          notContainingGridXYCount++;
        }
      } else {
        let gd = GridData.createFromHints(widget, 1);
        widget.gridData = gd;
      }
    });
    if (containingGridXYCount > 0 && notContainingGridXYCount === 0) {
      this.layoutAllStatic(widgets);
    } else {
      this.layoutAllDynamic(widgets);
    }
  }

  layoutAllStatic(widgets) {
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
      v.gridData = GridData.createFromHints(v, totalGridW);
    });
    this.gridRows = totalGridH;
    this.gridColumns = totalGridW;
  }

  layoutAllDynamic(widgets) {
    // abstract, must be implemented by sub classes
  }

  getGridColumnCount() {
    return this.gridColumns;
  }

  getGridRowCount() {
    return this.gridRows;
  }

  /**
   * If grid w is greater than column count, grid w will be set to the column count.
   */
  static getGridDataFromHints(widget, groupBoxColumnCount) {
    let data = GridData.createFromHints(widget, groupBoxColumnCount);
    data.w = Math.min(groupBoxColumnCount, data.w);
    return data;
  }
}
