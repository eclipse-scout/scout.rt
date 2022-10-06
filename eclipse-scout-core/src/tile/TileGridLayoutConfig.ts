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
import {LogicalGridLayoutConfig} from '../index';

export default class TileGridLayoutConfig extends LogicalGridLayoutConfig {

  constructor(options) {
    super(options);
  }

  _extend(options) {
    super._extend(options);
    options = options || {};
    if (options.maxWidth > -2) {
      this.maxWidth = options.maxWidth;
    }
  }

  applyToLayout(layout) {
    super.applyToLayout(layout);
    if (this.maxWidth) {
      layout.maxWidth = this.maxWidth;
    }
  }

  clone() {
    return new TileGridLayoutConfig(this);
  }

  static ensure(layoutConfig) {
    if (!layoutConfig) {
      return layoutConfig;
    }
    if (layoutConfig instanceof TileGridLayoutConfig) {
      return layoutConfig;
    }
    return new TileGridLayoutConfig(layoutConfig);
  }
}
