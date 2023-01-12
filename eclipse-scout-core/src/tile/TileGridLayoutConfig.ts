/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {InitModelOf, LogicalGridLayoutConfig, LogicalGridLayoutConfigModel, ObjectOrModel, TileGridLayout} from '../index';

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
export class TileGridLayoutConfig extends LogicalGridLayoutConfig implements TileGridLayoutConfigModel {
  declare model: TileGridLayoutConfigModel;

  maxWidth: number;

  constructor(options?: InitModelOf<TileGridLayoutConfig>) {
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

  static override ensure(layoutConfig: ObjectOrModel<TileGridLayoutConfig>): TileGridLayoutConfig {
    if (!layoutConfig) {
      return null;
    }
    if (layoutConfig instanceof TileGridLayoutConfig) {
      return layoutConfig;
    }
    return new TileGridLayoutConfig(layoutConfig);
  }
}
