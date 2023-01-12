/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
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

  grow: number;
  initial: number;
  order: number;
  relative: boolean;
  shrink: number;
  sizePx: number;
  initialPx: number;
  diff: number;

  constructor(model?: InitModelOf<FlexboxLayoutData>) {
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
    this.diff = null;
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

  static fixed(size?: number): FlexboxLayoutData {
    let layoutData = new FlexboxLayoutData();
    layoutData.relative = false;
    layoutData.initial = size || -1;
    layoutData.grow = 0;
    layoutData.shrink = 0;
    return layoutData;
  }
}
