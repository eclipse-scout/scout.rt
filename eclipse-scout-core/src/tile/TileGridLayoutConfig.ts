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
import {LogicalGridLayoutConfig, TileGridLayout} from '../index';
import {LogicalGridLayoutConfigModel} from '../layout/logicalgrid/LogicalGridLayoutConfig';

export interface TileGridLayoutConfigModel extends LogicalGridLayoutConfigModel {
  /**
   * The maximum width in pixels to use for the content.
   * There is no maximum if this value is <= 0.
   *
   * Default is -1;
   */
  maxWidth?: number;
}

/**
 * Configures layouting hints for tiles layouted by {@link TileGridLayout}.
 *
 * The configured hints only have an effect if theirs value is >=0.
 * Otherwise, the default values specified by CSS are applied (see {@link TileGridLayout._initDefaults}).
 */
export default class TileGridLayoutConfig extends LogicalGridLayoutConfig implements TileGridLayoutConfigModel {
  maxWidth: number;

  constructor(options?: TileGridLayoutConfigModel) {
    super(options);
    options = options || {};
    if (options.maxWidth > -2) {
      this.maxWidth = options.maxWidth;
    }
  }

  override applyToLayout(layout: TileGridLayout) {
    super.applyToLayout(layout);
    if (this.maxWidth) {
      layout.maxWidth = this.maxWidth;
    }
  }

  override clone(): TileGridLayoutConfig {
    return new TileGridLayoutConfig(this);
  }

  static override ensure(layoutConfig: TileGridLayoutConfig | TileGridLayoutConfigModel): TileGridLayoutConfig {
    if (!layoutConfig) {
      return null;
    }
    if (layoutConfig instanceof TileGridLayoutConfig) {
      return layoutConfig;
    }
    return new TileGridLayoutConfig(layoutConfig);
  }
}
