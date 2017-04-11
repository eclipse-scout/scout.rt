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
scout.BenchColumnLayoutData = function(model) {
  // initial
  this.columns = [null, null, null];
  $.extend(this, model);

  this._ensureColumns();
};

scout.BenchColumnLayoutData.prototype._ensureColumns = function() {
  this.columns = this.columns.map(function(col, i) {
    return new scout.BenchRowLayoutData(col).withOrder(i*2);
  });
};

scout.BenchColumnLayoutData.prototype.getColumns = function() {
  return this.columns;
};

scout.BenchColumnLayoutData.ensure = function(layoutData) {
  if (!layoutData) {
    layoutData = new scout.BenchColumnLayoutData();
    return layoutData;
  }
  if (layoutData instanceof scout.BenchColumnLayoutData) {
    return layoutData;
  }
  return new scout.BenchColumnLayoutData(layoutData);
};
