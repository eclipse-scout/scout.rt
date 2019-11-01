/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
export default class LogicalGridLayoutConfig {

  constructor(options) {
    this._extend(options);
  }

  _extend(options) {
    // -1 means use the UI defaults
    options = options || {};
    if (options.hgap > -1) {
      this.hgap = options.hgap;
    }
    if (options.vgap > -1) {
      this.vgap = options.vgap;
    }
    if (options.columnWidth > -1) {
      this.columnWidth = options.columnWidth;
    }
    if (options.rowHeight > -1) {
      this.rowHeight = options.rowHeight;
    }
    if (options.minWidth > -1) {
      this.minWidth = options.minWidth;
    }
  }

  clone() {
    return new LogicalGridLayoutConfig(this);
  }

  applyToLayout(layout) {
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

  static ensure(layoutConfig) {
    if (!layoutConfig) {
      return layoutConfig;
    }
    if (layoutConfig instanceof LogicalGridLayoutConfig) {
      return layoutConfig;
    }
    return new LogicalGridLayoutConfig(layoutConfig);
  }
}
