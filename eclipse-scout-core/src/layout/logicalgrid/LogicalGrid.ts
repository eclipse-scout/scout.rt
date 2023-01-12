/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LogicalGridConfig, LogicalGridContainer, ObjectModel, ObjectWithType, scout} from '../../index';

export interface LogicalGridOptions extends ObjectModel<LogicalGrid> {
  gridConfig?: LogicalGridConfig | object;
}

/**
 * Base class for every logical grid. The concrete grids should implement {@link LogicalGrid._validate}.
 */
export abstract class LogicalGrid implements ObjectWithType {
  declare model: LogicalGridOptions;

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
