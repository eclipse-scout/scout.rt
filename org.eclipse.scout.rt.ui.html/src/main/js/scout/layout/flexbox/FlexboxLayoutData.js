/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.FlexboxLayoutData = function(model) {
  // inital
  this.relative = true;
  this.grow = 1;
  this.shrink = 1;
  this.initial = 1;
  this.order = 0;
  $.extend(this, model);
  // ui properties
  this.sizePx; /*current display size in pixel*/
  this.initialPx; /*initial in pixel */
  this.delta;
};

scout.FlexboxLayoutData.prototype.withOrder = function(order) {
  this.order = order;
  return this;
};

scout.FlexboxLayoutData.prototype.acceptDelta = function(delta, apply) {
  if (delta > 0) {
    return this._grow(delta, apply);
  } else {
    return this._shrink(delta, apply);
  }
};

scout.FlexboxLayoutData.prototype.validate = function(size) {
  if (this.grow === 0) {
    size = Math.min(this.initialPx, size);
  }
  if (this.shrink === 0) {
    size = Math.max(this.initialPx, size);
  }
  return size;
};

scout.FlexboxLayoutData.prototype._grow = function(delta, apply) {
  var maxDelta = 0,
    consumedDelta = 0;
  if (this.grow > 0) {
    maxDelta = delta;
  } else if (this.initialPx > this.sizePx) {
    maxDelta = this.initialPx - this.sizePx;
  }
  consumedDelta = Math.min(delta, maxDelta);
  if (apply) {
    this.sizePx = this.sizePx + consumedDelta;
  }
  return delta - consumedDelta;
};

scout.FlexboxLayoutData.prototype._shrink = function(delta, apply) {
  var maxDelta = 0,
    consumedDelta = 0;
  if (this.shrink > 0) {
    maxDelta = -this.sizePx + 20;

  } else if (this.initialPx < this.sizePx) {
    maxDelta = this.initialPx - this.sizePx;
  }
  consumedDelta = Math.max(delta, maxDelta);
  if (apply) {
    this.sizePx = this.sizePx + consumedDelta;
  }
  return delta - consumedDelta;
};

/*Static functions*/
scout.FlexboxLayoutData.fixed = function(size) {
  var layoutData = new scout.FlexboxLayoutData();
  layoutData.relative = false;
  layoutData.initial = size || -1;
  layoutData.grow = 0;
  layoutData.shrink = 0;
  return layoutData;
};
