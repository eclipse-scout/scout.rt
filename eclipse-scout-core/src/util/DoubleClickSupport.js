/*
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {scout} from '../index';

/**
 * Simple helper to determine if two consecutive 'mousedown' events should be considered as a double click.
 *
 * How to use:
 * 1. Feed all mousedown events to the mousedown() method.
 * 2. The method doubleClicked() returns true if the two last added events happened so fast after
 *    each other that hey should be considered a 'double click'. If the distance or interval between
 *    the last two events is too large, false is returned.
 *
 * Options:
 * [maxDoubleClickInterval, default=500]
 *   Maximum time in milliseconds between two consecutive mousedown events to consider as a double
 *   click event. If the interval is larger than this value, doubleClicked() will return false.
 * [maxDoubleClickDistance, default=10]
 *   Maximum distance (in both directions) between two consecutive mousedown events to consider as
 *   a double click event. If the distance is larger than this value, doubleClicked() will return false.
 */
export default class DoubleClickSupport {

  /**
   * @param {object} [options]
   * @param options.maxDoubleClickInterval default 500ms
   * @param options.maxDoubleClickDistance default 10px
   */
  constructor(options) {
    options = options || {};

    this._lastPosX = null;
    this._lastPosY = null;
    this._lastTimestamp = null;

    this._maxDoubleClickInterval = options.maxDoubleClickInterval || 500; // ms
    this._maxDoubleClickDistance = options.maxDoubleClickDistance || 10; // px
    this._doubleClicked = false;
  }

  mousedown(event) {
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

  doubleClicked() {
    return this._doubleClicked;
  }
}
