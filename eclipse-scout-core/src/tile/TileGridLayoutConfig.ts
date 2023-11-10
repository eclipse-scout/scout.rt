/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {InitModelOf, LogicalGridLayoutConfig, LogicalGridLayoutConfigModel, ObjectOrModel, styles, TileGridLayout} from '../index';

/**
 * Configures layouting options for tiles layouted by {@link TileGridLayout}.
 *
 * @see LogicalGridLayoutConfig
 */
export class TileGridLayoutConfig extends LogicalGridLayoutConfig implements TileGridLayoutConfigModel {
  declare model: TileGridLayoutConfigModel;

  maxWidth: number;

  protected static _DEFAULT_CONFIG: InitModelOf<TileGridLayoutConfig> = undefined;

  constructor(options?: InitModelOf<TileGridLayoutConfig>, defaults?: InitModelOf<TileGridLayoutConfig>) {
    super(options, defaults);
    this._init(); // maxWidth will be reset to undefined after super() call, _init() will set to correct value again (see tsconfig.json:useDefineForClassFields)
  }

  static getTileDefaultLayoutConfig(): InitModelOf<TileGridLayoutConfig> {
    if (!TileGridLayoutConfig._DEFAULT_CONFIG) {
      TileGridLayoutConfig._DEFAULT_CONFIG = {
        hgap: styles.getSize('tile-grid-layout-config', 'margin-left', 'marginLeft', -1),
        vgap: styles.getSize('tile-grid-layout-config', 'margin-top', 'marginTop', -1),
        columnWidth: styles.getSize('tile-grid-layout-config', 'width', 'width', -1),
        rowHeight: styles.getSize('tile-grid-layout-config', 'height', 'height', -1),
        minWidth: 0,
        maxWidth: -1
      };
    }
    return TileGridLayoutConfig._DEFAULT_CONFIG;
  }

  protected override _prepareOptions(options?: InitModelOf<TileGridLayoutConfig>): InitModelOf<TileGridLayoutConfig> {
    let opts = super._prepareOptions(options) as InitModelOf<TileGridLayoutConfig>;
    opts.maxWidth = options.maxWidth > -2 ? options.maxWidth : undefined;
    return opts;
  }

  protected override _readEnvDefaults(): InitModelOf<TileGridLayoutConfig> {
    return TileGridLayoutConfig.getTileDefaultLayoutConfig();
  }

  override applyToLayout(layout: TileGridLayout) {
    super.applyToLayout(layout);
    if (this.maxWidth) {
      layout.maxWidth = this.maxWidth;
    }
  }

  override clone(options?: InitModelOf<TileGridLayoutConfig>): TileGridLayoutConfig {
    return new TileGridLayoutConfig($.extend({}, this._options, options), this._defaults);
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

export interface TileGridLayoutConfigModel extends LogicalGridLayoutConfigModel {
  /**
   * The maximum width in pixels to use for the content.
   * There is no maximum if this value is <= 0.
   *
   * Default is -1;
   */
  maxWidth?: number;
}
