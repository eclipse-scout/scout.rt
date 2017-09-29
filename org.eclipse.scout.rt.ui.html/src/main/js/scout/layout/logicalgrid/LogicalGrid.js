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
/**
 * Base class for every logical grid. The concrete grids should implement {@link #_validate}.
 */
scout.LogicalGrid = function(options) {
  options = options || {};
  this.dirty = true;
  this.gridConfig = options.gridConfig || null;
};

scout.LogicalGrid.prototype.setDirty = function(dirty) {
  this.dirty = dirty;
};

scout.LogicalGrid.prototype.setGridConfig = function(gridConfig) {
  this.gridConfig = gridConfig;
};

/**
 * Calls {@link #_validate} if the grid is dirty. Sets dirty to false afterwards.
 */
scout.LogicalGrid.prototype.validate = function(gridContainer) {
  if (!this.dirty) {
    return;
  }
  this._validate(gridContainer);
  this.setDirty(false);
};
