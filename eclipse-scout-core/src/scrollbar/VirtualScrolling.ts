/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {numbers, Range, scout} from '../index';
import $ from 'jquery';

export default class VirtualScrolling {

  constructor(options) {
    this.enabled = true;
    this.minRowHeight = 0;
    this.scrollHandler = null;
    this.viewRangeSize = new Range();
    this.widget = null;
    this.$scrollable = null;

    $.extend(this, options);
  }

  setEnabled(enabled) {
    this.enabled = enabled;
  }

  set$Scrollable($scrollable) {
    if (this.$scrollable === $scrollable) {
      return;
    }
    this.$scrollable = $scrollable;
  }

  setMinRowHeight(minRowHeight) {
    if (this.minRowHeight === minRowHeight) {
      return;
    }
    if (!numbers.isNumber(minRowHeight)) {
      throw new Error('minRowHeight is not a number: ' + minRowHeight);
    }
    this.minRowHeight = minRowHeight;
    if (this.widget.rendered) {
      this.setViewRangeSize(this.calculateViewRangeSize());
    }
  }

  setViewRangeSize(viewRangeSize, updateViewPort) {
    if (this.viewRangeSize === viewRangeSize) {
      return;
    }
    this.viewRangeSize = viewRangeSize;
    if (this.widget.rendered && scout.nvl(updateViewPort, true)) {
      this._renderViewPort();
    }
  }

  /**
   * Calculates the optimal view range size (number of rows to be rendered).
   * It uses the default row height to estimate how many rows fit in the view port.
   * The view range size is this value * 2.
   */
  calculateViewRangeSize() {
    if (!this.enabled || this.$scrollable.length === 0) {
      return this.rowCount();
    }
    if (this.minRowHeight === 0) {
      throw new Error('Cannot calculate view range with rowHeight = 0');
    }
    return Math.ceil(this.$scrollable.height() / this.minRowHeight) * 2;
  }

  calculateCurrentViewRange() {
    if (!this.enabled) {
      return this.maxViewRange();
    }
    if (this.viewRangeSize === 0) {
      return new Range(0, 0);
    }
    let rowIndex;
    if (this.$scrollable.length === 0) {
      return this.maxViewRange();
    }
    let scrollTop = this.$scrollable[0].scrollTop;
    let maxScrollTop = this.$scrollable[0].scrollHeight - this.$scrollable[0].clientHeight;
    let widgetBounds = this.widget.$container[0].getBoundingClientRect();
    let scrollableBounds = this.$scrollable[0].getBoundingClientRect();
    if (widgetBounds.height > 0 && (
      widgetBounds.bottom < scrollableBounds.top ||
      widgetBounds.top > scrollableBounds.bottom)) {
      // If widget is not in the view port, no need to draw any row
      return new Range(0, 0);
    }

    if (maxScrollTop === 0) {
      // no scrollbars visible
      rowIndex = 0;
    } else {
      rowIndex = this._rowIndexAtScrollTop(scrollTop);
    }

    return this.calculateViewRangeForRowIndex(rowIndex);
  }

  maxViewRange() {
    return new Range(0, this.rowCount());
  }

  /**
   * Returns a range of size this.viewRangeSize. Start of range is rowIndex - viewRangeSize / 4.
   * -> 1/4 of the rows are before the viewport 2/4 in the viewport 1/4 after the viewport,
   * assuming viewRangeSize is 2*number of possible rows in the viewport (see calculateViewRangeSize).
   */
  calculateViewRangeForRowIndex(rowIndex) {
    if (!this.enabled) {
      return this.maxViewRange();
    }

    let viewRange = new Range(),
      quarterRange = Math.floor(this.viewRangeSize / 4),
      diff;

    viewRange.from = Math.max(rowIndex - quarterRange, 0);
    viewRange.to = Math.min(viewRange.from + this.viewRangeSize, this.rowCount());

    // Try to use the whole viewRangeSize (extend from if necessary)
    diff = this.viewRangeSize - viewRange.size();
    if (diff > 0) {
      viewRange.from = Math.max(viewRange.to - this.viewRangeSize, 0);
    }
    return viewRange;
  }

  /**
   * Returns the index of the row which is at position scrollTop.
   */
  _rowIndexAtScrollTop(scrollTop) {
    let height = 0,
      rowCount = this.rowCount(),
      index = rowCount - 1;

    if (this.widget.$container[0] !== this.$scrollable[0]) {
      // If container itself is not scrollable but a parent, height must not start at 0
      height = scrollTop + (this.widget.$container.offset().top - this.$scrollable.offset().top);
    }

    for (let row = 0; row < rowCount; row++) {
      height += this.rowHeight(row);
      if (scrollTop < height) {
        index = row;
        break;
      }
    }
    return index;
  }

  rowHeight(row) {
    throw new Error('Function has to be provided by widget');
  }

  /**
   * @returns {number}
   */
  rowCount() {
    throw new Error('Function has to be provided by widget');
  }

  /**
   * Calculates and renders the rows which should be visible in the current viewport based on scroll top.
   */
  _renderViewPort() {
    let viewRange = this.calculateCurrentViewRange();
    this._renderViewRange(viewRange);
  }

  _renderViewRangeForRowIndex(rowIndex) {
    let viewRange = this.calculateViewRangeForRowIndex(rowIndex);
    this._renderViewRange(viewRange);
  }

  /**
   * Renders the rows visible in the viewport and removes the other rows
   */
  _renderViewRange(viewRange) {
    throw new Error('Function has to be provided by widget');
  }
}
