/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HtmlEnvironment, InitModelOf, LogicalGridLayout, ObjectOrModel, Widget} from '../../index';

export interface LogicalGridLayoutConfigModel {
  /**
   * The horizontal gap in pixels to use between two logical grid columns.
   */
  hgap?: number;
  /**
   * The vertical gap in pixels to use between two logical grid rows.
   */
  vgap?: number;
  /**
   * The width in pixels to use for elements with the logical unit "width = 1".
   * Larger logical widths are multiplied with this value (and gaps are added).
   */
  columnWidth?: number;
  /**
   * The height in pixels to use for elements with the logical unit "height = 1".
   * Larger logical heights are multiplied with this value (and gaps are added).
   */
  rowHeight?: number;
  /**
   * The minimum width of the widget.
   * If this width is > 0 a horizontal scrollbar is shown when the widgets get smaller than this value.
   */
  minWidth?: number;
}

/**
 * Configures layouting hints for elements layouted by {@link LogicalGridLayout}.
 *
 * The configured hints only have an effect if their value is >=0.
 * Otherwise, the default values specified by CSS are applied (see {@link _readDefaults}).
 */
export class LogicalGridLayoutConfig implements LogicalGridLayoutConfigModel {
  declare model: LogicalGridLayoutConfigModel;

  hgap: number;
  vgap: number;
  columnWidth: number;
  rowHeight: number;
  minWidth: number;
  protected _defaults: InitModelOf<LogicalGridLayoutConfig>;
  protected _options: InitModelOf<LogicalGridLayoutConfig>;

  constructor(options?: InitModelOf<LogicalGridLayoutConfig>, defaults?: InitModelOf<LogicalGridLayoutConfig>) {
    this._options = this._prepareOptions(options || {});
    this.setDefaults(defaults);
  }

  protected _prepareOptions(options?: InitModelOf<LogicalGridLayoutConfig>) {
    let opts = $.extend({}, options);
    // -1 means use the UI defaults
    opts.hgap = options.hgap > -1 ? options.hgap : undefined;
    opts.vgap = options.vgap > -1 ? options.vgap : undefined;
    opts.columnWidth = options.columnWidth > -1 ? options.columnWidth : undefined;
    opts.rowHeight = options.rowHeight > -1 ? options.rowHeight : undefined;
    opts.minWidth = options.minWidth > -1 ? options.minWidth : undefined;
    return opts;
  }

  setDefaults(defaults?: InitModelOf<LogicalGridLayoutConfig>) {
    this._defaults = $.extend({}, defaults);
    this._initDefaults();
  }

  protected _initDefaults() {
    $.extend(this, this._defaults, this._readDefaults(), this._options);
  }

  protected _readDefaults(): InitModelOf<LogicalGridLayoutConfig> {
    let env = HtmlEnvironment.get();
    return {
      hgap: env.formColumnGap,
      vgap: env.formRowGap,
      columnWidth: env.formColumnWidth,
      rowHeight: env.formRowHeight,
      minWidth: 0
    };
  }

  clone(): LogicalGridLayoutConfig {
    return new LogicalGridLayoutConfig(this._options, this._defaults);
  }

  applyToLayout(layout: LogicalGridLayout) {
    layout.layoutConfig = this;
    if (this.hgap !== null && this.hgap !== undefined) {
      layout.hgap = this.hgap;
    }
    if (this.vgap !== null && this.vgap !== undefined) {
      layout.vgap = this.vgap;
    }
    if (this.columnWidth !== null && this.columnWidth !== undefined) {
      layout.columnWidth = this.columnWidth;
    }
    if (this.rowHeight !== null && this.rowHeight !== undefined) {
      layout.rowHeight = this.rowHeight;
    }
    if (this.minWidth !== null && this.minWidth !== undefined) {
      layout.minWidth = this.minWidth;
    }
  }

  static ensure(layoutConfig: ObjectOrModel<LogicalGridLayoutConfig>): LogicalGridLayoutConfig {
    if (!layoutConfig) {
      return null;
    }
    if (layoutConfig instanceof LogicalGridLayoutConfig) {
      return layoutConfig;
    }
    return new LogicalGridLayoutConfig(layoutConfig);
  }

  static initHtmlEnvChangeHandler(widget: Widget, getter: () => LogicalGridLayoutConfig, setter: (config: LogicalGridLayoutConfig) => void) {
    if (widget.initialized) {
      return;
    }
    let handler = () => {
      setter(getter()?.clone()); // Clone will read the new defaults and apply the custom options
    };
    HtmlEnvironment.get().on('propertyChange', handler);
    widget.one('destroy', () => {
      HtmlEnvironment.get().off('propertyChange', handler);
    });
  }

  static prepareSmallHgapConfig(layoutConfig: ObjectOrModel<LogicalGridLayoutConfig>) {
    if (!layoutConfig) {
      layoutConfig = new LogicalGridLayoutConfig();
    }
    let realLayoutConfig = LogicalGridLayoutConfig.ensure(layoutConfig);
    realLayoutConfig.setDefaults({
      hgap: HtmlEnvironment.get().smallColumnGap
    });
    return realLayoutConfig;
  }
}
