/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.VerticalSmartGroupBoxBodyGrid = function() {
  scout.VerticalSmartGroupBoxBodyGrid.parent.call(this);
};
scout.inherits(scout.VerticalSmartGroupBoxBodyGrid, scout.GroupBoxBodyGrid);

scout.VerticalSmartGroupBoxBodyGrid.prototype.layoutAllDynamic = function(fields) {
  var cellCount = 0;
  fields.forEach(function(f) {
    var hints = scout.GroupBoxBodyGrid.getGridDataFromHints(f, this.getGridColumnCount());
    cellCount += hints.w * hints.h;
  }.bind(this));

  // do the calc
  var rowCount = Math.floor((cellCount + this.getGridColumnCount() - 1) / this.getGridColumnCount());
  var matrix = new scout.VerticalGridMatrixGroupBox(this.getGridColumnCount(), rowCount);
  while (!matrix.computeGridData(fields)) {
    matrix.resetAll(this.getGridColumnCount(), ++rowCount);
  }

  // set gridData
  fields.forEach(function(f) {
    f.gridData = matrix.getGridData(f);
  });
  this.gridRows = matrix.getRowCount();
};
