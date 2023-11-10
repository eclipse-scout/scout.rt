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

/**
 * Stores layouting options for elements layouted by {@link LogicalGridLayout}.
 *
 * The object can be initialized with user-defined options. These options only have an effect if their value is >=0.
 * If no user-defined options are specified or their value is < 0, the default values specified by CSS are applied (see {@link _readEnvDefaults})
 * unless explicit defaults are provided using constructor or {@link withDefaults}.
 *
 * Important: always create a new object or use {@link clone} to modify the options.
 * Don't set the members directly. Otherwise, your values may be overridden if new defaults are set using {@link withDefaults}.
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
    this.withDefaults(defaults);
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

  /**
   * Sets default values that are used if no user-defined options are provided or their values are < 0
   */
  withDefaults(defaults?: InitModelOf<LogicalGridLayoutConfig>): LogicalGridLayoutConfig {
    this._defaults = $.extend({}, defaults);
    this._init();
    return this;
  }

  withSmallHgapDefaults(): LogicalGridLayoutConfig {
    return this.withDefaults({hgap: HtmlEnvironment.get().smallColumnGap});
  }

  protected _init() {
    $.extend(this, this._readEnvDefaults(), this._defaults, this._options);
  }

  /**
   * @returns the environment defaults that are used if no explicit {@link _defaults} or custom {@link _options} are set.
   */
  protected _readEnvDefaults(): InitModelOf<LogicalGridLayoutConfig> {
    let env = HtmlEnvironment.get();
    return {
      hgap: env.formColumnGap,
      vgap: env.formRowGap,
      columnWidth: env.formColumnWidth,
      rowHeight: env.formRowHeight,
      minWidth: 0
    };
  }

  /**
   * @returns a clone of this logical grid layout config enriched with the given options.
   */
  clone(options?: InitModelOf<LogicalGridLayoutConfig>): LogicalGridLayoutConfig {
    return new LogicalGridLayoutConfig($.extend({}, this._options, options), this._defaults);
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

  /**
   * Adds a property change handler to the {@link HtmlEnvironment} that calls the given setter to update the layout config on the given widget whenever the html environment changes.
   * This is necessary because the html environment contains the default values for the layout config object which change when switching to compact mode.
   *
   * This function needs to be called exactly once during the initialization of a widget.
   * It does nothing if it is called after the initialization.
   */
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
}

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
