/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {LogicalGridConfig, scout} from '../../index';

/**
 * Base class for every logical grid. The concrete grids should implement {@link #_validate}.
 */
export default class LogicalGrid {

  constructor(options) {
    options = options || {};
    this.dirty = true;
    this.gridConfig = options.gridConfig || null;
    this._setGridConfig(this.gridConfig);
  }

  setDirty(dirty) {
    this.dirty = dirty;
  }

  setGridConfig(gridConfig) {
    this._setGridConfig(gridConfig);
  }

  _setGridConfig(gridConfig) {
    if (gridConfig && !(gridConfig instanceof LogicalGridConfig)) {
      gridConfig = scout.create('LogicalGridConfig', gridConfig);
    }
    this.gridConfig = gridConfig;
  }

  /**
   * Calls {@link #_validate} if the grid is dirty. Sets dirty to false afterwards.
   */
  validate(gridContainer) {
    if (!this.dirty) {
      return;
    }
    this._validate(gridContainer);
    this.setDirty(false);
  }
}
