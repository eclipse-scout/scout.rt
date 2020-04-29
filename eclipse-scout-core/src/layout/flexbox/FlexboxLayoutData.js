/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import $ from 'jquery';

export default class FlexboxLayoutData {

  constructor(model) {
    this._prepare();
    // inital
    this.relative = true;
    this.grow = 1;
    this.shrink = 1;
    this.initial = 1;
    this.order = 0;
    $.extend(this, model);
    // ui properties
    this.sizePx; // current display size in pixel
    this.initialPx; // initial in pixel
    this.delta;
  }

  _prepare() {
    // nop. may be overridden by sub classes
  }

  withOrder(order) {
    this.order = order;
    return this;
  }

  acceptDelta(delta, apply) {
    if (delta > 0) {
      return this._grow(delta, apply);
    }
    return this._shrink(delta, apply);

  }

  validate(size) {
    if (this.grow === 0) {
      size = Math.min(this.initialPx, size);
    }
    if (this.shrink === 0) {
      size = Math.max(this.initialPx, size);
    }
    return size;
  }

  _grow(delta, apply) {
    let maxDelta = 0,
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
  }

  _shrink(delta, apply) {
    let maxDelta = 0,
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
  }

  /* Static functions */
  static fixed(size) {
    let layoutData = new FlexboxLayoutData();
    layoutData.relative = false;
    layoutData.initial = size || -1;
    layoutData.grow = 0;
    layoutData.shrink = 0;
    return layoutData;
  }
}
