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
scout.HorizontalGrid = function() {
  scout.HorizontalGrid.parent.call(this);
};
scout.inherits(scout.HorizontalGrid, scout.AbstractGrid);

scout.HorizontalGrid.prototype.layoutAllDynamic = function(widgets) {
  var matrix = new scout.HorizontalGridMatrix(this.getGridColumnCount());
  matrix.computeGridData(widgets);
  this.gridRows = matrix.getRowCount();
};
