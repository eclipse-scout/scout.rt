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
scout.BenchRowLayoutData = function(model) {
  this.rows = [null,null,null];
  scout.BenchRowLayoutData.parent.call(this, model);

  this._ensureRows();
};

scout.inherits(scout.BenchRowLayoutData, scout.FlexboxLayoutData);

scout.BenchRowLayoutData.prototype.getRows = function(){
  return this.rows;
};

scout.BenchRowLayoutData.prototype._ensureRows = function() {
  this.rows = this.rows.map(function(row,i){
    return new scout.FlexboxLayoutData(row).withOrder(i*2);
  });
};


scout.BenchRowLayoutData.prototype.updateVisibilities = function(rows) {
  rows.forEach(function(row, index){
    this.rows[index].visible = row.rendered;

  }.bind(this));
};

