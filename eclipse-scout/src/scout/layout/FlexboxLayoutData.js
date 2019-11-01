import * as $ from 'jquery';

export default class FlexboxLayoutData {

  constructor(model) {
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
  }


  withOrder(order) {
    this.order = order;
    return this;
  };

  acceptDelta(delta, apply) {
    if (delta > 0) {
      return this._grow(delta, apply);
    } else {
      return this._shrink(delta, apply);
    }
  };

  validate(size) {
    if (this.grow === 0) {
      size = Math.min(this.initialPx, size);
    }
    if (this.shrink === 0) {
      size = Math.max(this.initialPx, size);
    }
    return size;
  };

  _grow(delta, apply) {
    var maxDelta = 0,
      consumedDelta;
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

  _shrink(delta, apply) {
    var maxDelta = 0,
      consumedDelta;
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

  static fixed(size) {
    var layoutData = new FlexboxLayoutData();
    layoutData.relative = false;
    layoutData.initial = size || -1;
    layoutData.grow = 0;
    layoutData.shrink = 0;
    return layoutData;
  };
}
