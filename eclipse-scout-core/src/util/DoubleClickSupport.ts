/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {scout} from '../index';
import MouseDownEvent = JQuery.MouseDownEvent;

export interface DoubleClickSupportOptions {
  /**
   * Maximum time in milliseconds between two consecutive mousedown events to consider as a double
   * click event. If the interval is larger than this value, doubleClicked() will return false. Default is 500.
   */
  maxDoubleClickInterval?: number;

  /**
   * Maximum distance (in all directions in pixels) between two consecutive mousedown events to consider as
   * a double click event. If the distance is larger than this value, doubleClicked() will return false. Default is 10.
   */
  maxDoubleClickDistance?: number;
}

/**
 * Simple helper to determine if two consecutive 'mousedown' events should be considered as a double click.
 *
 * How to use:
 * 1. Feed all mousedown events to the mousedown() method.
 * 2. The method doubleClicked() returns true if the two last added events happened so fast after
 *    each other that hey should be considered a 'double click'. If the distance or interval between
 *    the last two events is too large, false is returned.
 */
export class DoubleClickSupport {
  protected _lastPosX: number;
  protected _lastPosY: number;
  protected _lastTimestamp: number;
  protected _maxDoubleClickInterval: number;
  protected _maxDoubleClickDistance: number;
  protected _doubleClicked: boolean;

  constructor(options?: DoubleClickSupportOptions) {
    options = options || {};

    this._lastPosX = null;
    this._lastPosY = null;
    this._lastTimestamp = null;

    this._maxDoubleClickInterval = options.maxDoubleClickInterval || 500;
    this._maxDoubleClickDistance = options.maxDoubleClickDistance || 10;
    this._doubleClicked = false;
  }

  mousedown(event: MouseDownEvent<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    if (event && event.type === 'mousedown') {
      let posX = scout.nvl(event.pageX, 0);
      let posY = scout.nvl(event.pageY, 0);
      let timestamp = Date.now();
      let isDoubleClick = false;
      if (this._lastTimestamp !== undefined) {
        let interval = timestamp - this._lastTimestamp;
        if (interval <= this._maxDoubleClickInterval) {
          let distance = Math.max(Math.abs(posX - this._lastPosX), Math.abs(posY - this._lastPosY));
          if (distance <= this._maxDoubleClickDistance) {
            // Double click detected
            isDoubleClick = true;
          }
        }
      }
      this._lastPosX = posX;
      this._lastPosY = posY;
      this._lastTimestamp = timestamp;
      this._doubleClicked = isDoubleClick;
    }
  }

  doubleClicked(): boolean {
    return this._doubleClicked;
  }
}
