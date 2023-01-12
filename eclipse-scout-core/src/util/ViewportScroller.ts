/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {InitModelOf, ObjectWithType, Point, SomeRequired, ViewportScrollerModel} from '../index';

export class ViewportScroller implements ViewportScrollerModel, ObjectWithType {
  declare model: ViewportScrollerModel;
  declare initModel: SomeRequired<this['model'], 'viewportWidth' | 'viewportHeight'>;
  objectType: string;

  static SPEED_FACTOR_SLOW = 1 / 20;
  static SPEED_FACTOR_MEDIUM = 1 / 10;
  static SPEED_FACTOR_FAST = 1 / 5;

  viewportWidth: number;
  viewportHeight: number;
  e: number;
  f: number;
  initialDelay: number;
  active: () => boolean;
  scroll: (dx: number, dy: number) => void;

  dx: number;
  dy: number;
  started: boolean;
  moved: boolean;
  protected _timeoutId: number;

  constructor(model?: InitModelOf<ViewportScroller>) {
    this.viewportWidth = 0;
    this.viewportHeight = 0;
    this.e = 30;
    this.f = -30;
    this.initialDelay = 500;
    this.active = () => true;
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    this.scroll = (dx: number, dy: number) => {
    };

    $.extend(this, model);

    // --- Do not change the following properties manually ---

    this.dx = 0;
    this.dy = 0;
    this.started = false;
    this.moved = false;

    this._timeoutId = null;
  }

  protected _tick() {
    if (!this.active()) {
      return;
    }
    if (this.started && (this.dx || this.dy)) {
      this.scroll(this.dx, this.dy);
      this.moved = true;
    }
    // Reschedule for continuous scrolling
    let delay = (this.moved ? 16 : this.initialDelay); // 16ms = 60fps
    this._timeoutId = setTimeout(() => this._tick(), delay);
  }

  /**
   * Normally, it should not be necessary to call this method manually. Use update() instead.
   */
  start() {
    clearTimeout(this._timeoutId);
    this._tick();
    this.started = true;
  }

  /**
   * Normally, it should not be necessary to call this method manually. Use update() instead.
   */
  stop() {
    clearTimeout(this._timeoutId);
    this._timeoutId = null;
    this.started = false;
    this.moved = false;
    this.dx = 0;
    this.dy = 0;
  }

  /**
   * This method is intended to be called with the current mouse position (viewport-relative coordinates in pixel)
   * on every mouse move event. It automatically computes the required delta scroll positions in both directions.
   */
  update(mouse: Point) {
    let e = this.e;
    let f = this.f;
    // f2 = Half-way between e and f
    let f2 = Math.floor((e + f) / 2);

    let scrollAreaLeft = e;
    let scrollAreaRight = this.viewportWidth - e;
    let scrollAreaTop = e;
    let scrollAreaBottom = this.viewportHeight - e;

    // Slow scrolling between e and f2, medium scrolling between f2 and f, fast scrolling after f
    const SLOW = ViewportScroller.SPEED_FACTOR_SLOW;
    const MEDIUM = ViewportScroller.SPEED_FACTOR_MEDIUM;
    const FAST = ViewportScroller.SPEED_FACTOR_FAST;
    let speedFactorX = 0;
    let speedFactorY = 0;

    // dx/dy = distance (positive or negative) in pixel
    let dx = 0;
    let dy = 0;
    if (mouse.x < scrollAreaLeft) {
      dx = -(scrollAreaLeft - mouse.x);
      speedFactorX = (mouse.x > f2 ? SLOW : (mouse.x > f ? MEDIUM : FAST));
    } else if (mouse.x > scrollAreaRight) {
      dx = (mouse.x - scrollAreaRight);
      speedFactorX = (mouse.x > this.viewportWidth - f ? FAST : (mouse.x > this.viewportWidth - f2 ? MEDIUM : SLOW));
    }
    if (mouse.y < scrollAreaTop) {
      dy = -(scrollAreaTop - mouse.y);
      speedFactorY = (mouse.y > f2 ? SLOW : (mouse.y > f ? MEDIUM : FAST));
    } else if (mouse.y > scrollAreaBottom) {
      dy = (mouse.y - scrollAreaBottom);
      speedFactorY = (mouse.y > this.viewportHeight - f ? FAST : (mouse.y > this.viewportHeight - f2 ? MEDIUM : SLOW));
    }
    // ax/ay = absolute distance in pixel
    let ax = Math.abs(dx);
    let ay = Math.abs(dy);
    let a = Math.max(ax, ay);

    if (a === 0) {
      // Mouse not in scroll area -> stop previously started loop
      this.stop();
      return;
    }

    // Compute distance to scroll
    let speedX = 1 + Math.floor(ax * speedFactorX);
    let speedY = 1 + Math.floor(ay * speedFactorY);
    this.dx = Math.sign(dx) * speedX;
    this.dy = Math.sign(dy) * speedY;

    // --- Start loop ---
    this.started || this.start();

    if (!this.moved && a > e) {
      // If mouse is outside the viewport, ensure scrolling starts immediately (by calling start() again)
      this.start();
    }
  }
}
