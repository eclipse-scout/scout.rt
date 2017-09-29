/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.VerticalSmartGrid = function() {
  scout.VerticalSmartGrid.parent.call(this);
};
scout.inherits(scout.VerticalSmartGrid, scout.AbstractGrid);

scout.VerticalSmartGrid.prototype.layoutAllDynamic = function(widgets) {
  var cellCount = 0;
  widgets.forEach(function(f) {
    var hints = scout.AbstractGrid.getGridDataFromHints(f, this.getGridColumnCount());
    cellCount += hints.w * hints.h;
  }.bind(this));

  // do the calc
  var rowCount = Math.floor((cellCount + this.getGridColumnCount() - 1) / this.getGridColumnCount());
  var matrix = new scout.VerticalGridMatrix(this.getGridColumnCount(), rowCount);
  while (!matrix.computeGridData(widgets)) {
    matrix.resetAll(this.getGridColumnCount(), ++rowCount);
  }

  // set gridData
  widgets.forEach(function(f) {
    f.gridData = matrix.getGridData(f);
  });
  this.gridRows = matrix.getRowCount();
};
