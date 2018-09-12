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
scout.SequenceBoxLayoutConfig = function(options) {
  scout.SequenceBoxLayoutConfig.parent.call(this, options);
  this.hgap = scout.HtmlEnvironment.smallColumnGap;
  this._extend(options);
};
scout.inherits(scout.SequenceBoxLayoutConfig, scout.LogicalGridLayoutConfig);

scout.SequenceBoxLayoutConfig.prototype.clone = function() {
  return new scout.SequenceBoxLayoutConfig(this);
};

scout.SequenceBoxLayoutConfig.ensure = function(layoutConfig) {
  if (!layoutConfig) {
    return layoutConfig;
  }
  if (layoutConfig instanceof scout.SequenceBoxLayoutConfig) {
    return layoutConfig;
  }
  return new scout.SequenceBoxLayoutConfig(layoutConfig);
};
