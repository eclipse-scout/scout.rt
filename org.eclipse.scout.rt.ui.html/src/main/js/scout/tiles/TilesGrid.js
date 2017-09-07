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
/**
 * @abstract
 */
scout.TilesGrid = function() {
  scout.TilesGrid.parent.call(this);
  this.gridRows = 0;
  this.gridColumns = 0;
};
scout.inherits(scout.TilesGrid, scout.LogicalGrid);

/**
 * @override
 */
scout.TilesGrid.prototype._validate = function(widget) {
  var x = 0,
    y = 0,
    tiles = widget.tiles,
    columnCount = widget.gridColumnCount;

  tiles.forEach(function(tile) {
    var gridData = scout.GridData.createFromHints(tile.gridDataHints);
    gridData.x = x;
    gridData.y = y;
    tile.gridData = gridData;
    if (columnCount === 1 || (x > 0 && x % (columnCount - 1) === 0)) {
      x = 0;
      y++;
    } else {
      x++;
    }
  });
};

