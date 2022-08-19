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
import {LayoutData} from '../../index';

export default class FlexboxLayoutData implements LayoutData {
  grow: number;
  initial: number;
  order: number;
  relative: boolean;
  shrink: number;
  sizePx: number;
  initialPx: number;

  constructor(model?: FlexboxLayoutData) {
    this._prepare();
    // initial
    this.relative = true;
    this.grow = 1;
    this.shrink = 1;
    this.initial = 1;
    this.order = 0;
    $.extend(this, model);
    // ui properties
    this.sizePx = null; // current display size in pixel
    this.initialPx = null; // initial in pixel
  }

  protected _prepare() {
    // nop. may be overridden by sub classes
  }

  withOrder(order: number): this {
    this.order = order;
    return this;
  }

  acceptDelta(delta: number, apply?: boolean): number {
    if (delta > 0) {
      return this._grow(delta, apply);
    }
    return this._shrink(delta, apply);
  }

  validate(size: number): number {
    if (this.grow === 0) {
      size = Math.min(this.initialPx, size);
    }
    if (this.shrink === 0) {
      size = Math.max(this.initialPx, size);
    }
    return size;
  }

  protected _grow(delta: number, apply?: boolean): number {
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

  protected _shrink(delta: number, apply?: boolean): number {
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
  static fixed(size: number): FlexboxLayoutData {
    let layoutData = new FlexboxLayoutData();
    layoutData.relative = false;
    layoutData.initial = size || -1;
    layoutData.grow = 0;
    layoutData.shrink = 0;
    return layoutData;
  }
}
