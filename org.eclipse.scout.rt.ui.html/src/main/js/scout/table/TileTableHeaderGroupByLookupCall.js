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
scout.TileTableHeaderGroupByLookupCall = function() {
  scout.TileTableHeaderGroupByLookupCall.parent.call(this);
  this.table = null;
};
scout.inherits(scout.TileTableHeaderGroupByLookupCall, scout.StaticLookupCall);

scout.TileTableHeaderGroupByLookupCall.prototype._init = function(model) {
  scout.TileTableHeaderGroupByLookupCall.parent.prototype._init.call(this, model);
};

scout.TileTableHeaderGroupByLookupCall.prototype._data = function() {
  var lookupRows = [];
  lookupRows.push([null, this.session.text('NoGrouping'), 'BOLD']);
  this.table.visibleColumns().forEach(function(column) {
    if (this.table.isGroupingPossible(column)) {
      lookupRows.push([column, scout.nvl(column.text, column.headerTooltipText)]);
    }
  }, this);
  return lookupRows;
};

scout.TileTableHeaderGroupByLookupCall.prototype._dataToLookupRow = function(data) {
  return scout.create('LookupRow', {
    key: data[0],
    text: data[1],
    font: data[2]
  });
};
