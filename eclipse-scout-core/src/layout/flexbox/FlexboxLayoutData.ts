/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import $ from 'jquery';
import {FlexboxLayoutDataModel, InitModelOf, LayoutData} from '../../index';

export class FlexboxLayoutData implements LayoutData, FlexboxLayoutDataModel {
  declare model: FlexboxLayoutDataModel;

  static readonly MIN_SIZE = 20;

  initial: number;
  relative: boolean;
  grow: number;
  shrink: number;
  min: number;
  max: number;
  order: number;

  sizePx: number;
  initialPx: number;
  diff: number;

  constructor(model?: InitModelOf<FlexboxLayoutData>) {
    // initial
    this.initial = 1;
    this.relative = true;
    this.grow = 1;
    this.shrink = 1;
    this.min = -1;
    this.max = -1;
    this.order = 0;
    $.extend(this, model);
    // ui properties
    this.sizePx = null; // current display size in pixel
    this.initialPx = null; // initial in pixel
    this.diff = null;
  }

  withOrder(order: number): this {
    this.order = order;
    return this;
  }

  withMin(min: number): this {
    this.min = min;
    return this;
  }

  withMax(max: number): this {
    this.max = max;
    return this;
  }

  protected _minSize(): number {
    return this.min < 0 ? FlexboxLayoutData.MIN_SIZE : this.min;
  }

  protected _maxSize(): number {
    return this.max < 0 ? Number.MAX_SAFE_INTEGER : Math.max(this.max, this._minSize());
  }

  acceptDelta(delta: number, apply?: boolean): number {
    if (delta > 0) {
      return this._grow(delta, apply);
    }
    return this._shrink(delta, apply);
  }

  validate(sizePx: number): number {
    if (this.grow === 0) {
      sizePx = Math.min(this.initialPx, sizePx);
    }
    if (this.shrink === 0) {
      sizePx = Math.max(this.initialPx, sizePx);
    }
    sizePx = Math.max(this._minSize(), sizePx);
    sizePx = Math.min(this._maxSize(), sizePx);
    return sizePx;
  }

  reset() {
    this.sizePx = null;
    this.initialPx = null;
    this.diff = null;
  }

  protected _grow(delta: number, apply?: boolean): number {
    let maxDelta = 0;
    if (this.grow > 0) {
      maxDelta = Math.min(delta, Math.max(0, this._maxSize() - this.sizePx));
    } else if (this.sizePx < this.initialPx) {
      // was previously shrunken, can only grow back to initial size
      maxDelta = this.initialPx - this.sizePx;
    }

    let consumedDelta = Math.min(delta, maxDelta);
    if (apply) {
      this.sizePx = this.sizePx + consumedDelta;
    }
    return delta - consumedDelta;
  }

  protected _shrink(delta: number, apply?: boolean): number {
    let maxDelta = 0;
    if (this.shrink > 0) {
      maxDelta = Math.max(delta, Math.min(0, this._minSize() - this.sizePx));
    } else if (this.sizePx > this.initialPx) {
      // was previously grown, can only shrink back to initialize size
      maxDelta = this.initialPx - this.sizePx;
    }

    let consumedDelta = Math.max(delta, maxDelta);
    if (apply) {
      this.sizePx = this.sizePx + consumedDelta;
    }
    return delta - consumedDelta;
  }

  static fixed(size?: number): FlexboxLayoutData {
    let layoutData = new FlexboxLayoutData();
    layoutData.initial = size || -1;
    layoutData.relative = false;
    layoutData.grow = 0;
    layoutData.shrink = 0;
    return layoutData;
  }
}
