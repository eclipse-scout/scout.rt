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
scout.TileTableHeaderSortByLookupCall = function() {
  scout.TileTableHeaderSortByLookupCall.parent.call(this);
  this.table = null;
};
scout.inherits(scout.TileTableHeaderSortByLookupCall, scout.StaticLookupCall);

scout.TileTableHeaderSortByLookupCall.prototype._init = function(model) {
  scout.TileTableHeaderSortByLookupCall.parent.prototype._init.call(this, model);
};

scout.TileTableHeaderSortByLookupCall.prototype._data = function() {
  var lookupRows = [];
  this.table.visibleColumns().forEach(function(column) {
    if (column.isSortingPossible()) {
      lookupRows.push([{
          column: column,
          asc: true
        }, scout.nvl(column.text, column.headerTooltipText) + ' (' + this.session.text('ui.ascending') + ')',
        scout.icons.LONG_ARROW_UP_BOLD
      ]);
      lookupRows.push([{
          column: column,
          asc: false
        }, scout.nvl(column.text, column.headerTooltipText) + ' (' + this.session.text('ui.descending') + ')',
        scout.icons.LONG_ARROW_DOWN_BOLD
      ]);
    }
  }, this);
  return lookupRows;
};

scout.TileTableHeaderSortByLookupCall.prototype._dataToLookupRow = function(data) {
  return scout.create('LookupRow', {
    key: data[0],
    text: data[1],
    iconId: data[2]
  });
};
