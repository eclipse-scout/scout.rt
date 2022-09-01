/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {LogicalGridConfig, LogicalGridContainer, scout} from '../../index';
import {ObjectWithType} from '../../scout';

export interface LogicalGridOptions {
  gridConfig: LogicalGrid | object;
}

/**
 * Base class for every logical grid. The concrete grids should implement {@link _validate}.
 */
export default abstract class LogicalGrid implements ObjectWithType {
  dirty: boolean;
  gridConfig: LogicalGridConfig;
  objectType: string;

  constructor(options?: LogicalGridOptions) {
    options = scout.nvl(options, {});
    this.dirty = true;
    this._setGridConfig(options.gridConfig || null);
  }

  setDirty(dirty: boolean) {
    this.dirty = dirty;
  }

  setGridConfig(gridConfig: LogicalGridConfig | object) {
    this._setGridConfig(gridConfig);
  }

  protected _setGridConfig(gridConfig: LogicalGridConfig | object) {
    if (gridConfig && !(gridConfig instanceof LogicalGridConfig)) {
      gridConfig = scout.create(LogicalGridConfig, gridConfig);
    }
    this.gridConfig = gridConfig as LogicalGridConfig;
  }

  /**
   * Calls {@link _validate} if the grid is dirty. Sets dirty to false afterwards.
   */
  validate(gridContainer: LogicalGridContainer) {
    if (!this.dirty) {
      return;
    }
    this._validate(gridContainer);
    this.setDirty(false);
  }

  protected abstract _validate(gridContainer: LogicalGridContainer);
}
