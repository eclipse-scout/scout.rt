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
scout.RadioButtonGroupLayoutConfig = function(options) {
  scout.RadioButtonGroupLayoutConfig.parent.call(this, options);
  this.hgap = scout.HtmlEnvironment.smallColumnGap;
  this._extend(options);
};
scout.inherits(scout.RadioButtonGroupLayoutConfig, scout.LogicalGridLayoutConfig);

scout.RadioButtonGroupLayoutConfig.prototype.clone = function() {
  return new scout.RadioButtonGroupLayoutConfig(this);
};

scout.RadioButtonGroupLayoutConfig.ensure = function(layoutConfig) {
  if (!layoutConfig) {
    return layoutConfig;
  }
  if (layoutConfig instanceof scout.RadioButtonGroupLayoutConfig) {
    return layoutConfig;
  }
  return new scout.RadioButtonGroupLayoutConfig(layoutConfig);
};
