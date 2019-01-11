/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TileGridLayoutConfig = function(options) {
  scout.TileGridLayoutConfig.parent.call(this, options);
};
scout.inherits(scout.TileGridLayoutConfig, scout.LogicalGridLayoutConfig);

scout.TileGridLayoutConfig.prototype._extend = function(options) {
  scout.TileGridLayoutConfig.parent.prototype._extend.call(this, options);
  options = options || {};
  if (options.maxWidth > -2) {
    this.maxWidth = options.maxWidth;
  }
};

scout.TileGridLayoutConfig.prototype.applyToLayout = function(layout) {
  scout.TileGridLayoutConfig.parent.prototype.applyToLayout.call(this, layout);
  if (this.maxWidth) {
    layout.maxWidth = this.maxWidth;
  }
};

scout.TileGridLayoutConfig.prototype.clone = function() {
  return new scout.TileGridLayoutConfig(this);
};

scout.TileGridLayoutConfig.ensure = function(layoutConfig) {
  if (!layoutConfig) {
    return layoutConfig;
  }
  if (layoutConfig instanceof scout.TileGridLayoutConfig) {
    return layoutConfig;
  }
  return new scout.TileGridLayoutConfig(layoutConfig);
};
