/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {events, graphics, Insets, JQueryMouseWheelEvent, OldWheelEvent, scout, ScrollbarEventMap, ScrollbarModel, scrollbars, Widget} from '../index';
import $ from 'jquery';

export class Scrollbar extends Widget implements ScrollbarModel {
  declare model: ScrollbarModel;
  declare eventMap: ScrollbarEventMap;
  declare self: Scrollbar;

  axis: 'x' | 'y';
  borderless: boolean;
  mouseWheelNeedsShift: boolean;
  /** thumb body for layout purposes */
  $thumb: JQuery;
  /** thumb handle */
  $thumbHandle: JQuery;

  protected _scrollSize: number;
  protected _offsetSize: number;
  protected _dim: 'Width' | 'Height';
  protected _dir: 'left' | 'top';
  protected _dirReverse: 'right' | 'bottom';
  protected _scrollDir: 'scrollLeft' | 'scrollTop';
  protected _thumbClipping: Insets;
  protected _onScrollHandler: (event: JQuery.ScrollEvent<HTMLDivElement>) => void;
  protected _onScrollWheelHandler: (event: JQueryMouseWheelEvent<HTMLDivElement>) => boolean;
  protected _onScrollbarMouseDownHandler: (event: JQuery.MouseDownEvent<HTMLDivElement>) => void;
  protected _onTouchStartHandler: (event: JQuery.TouchStartEvent<HTMLDivElement>) => void;
  protected _onThumbMouseDownHandler: (event: JQuery.MouseDownEvent<HTMLDivElement>) => boolean;
  protected _onDocumentMousemoveHandler: (event: JQuery.MouseMoveEvent<Document>) => void;
  protected _onDocumentMouseUpHandler: (event: JQuery.MouseUpEvent<Document>) => boolean;
  protected _onAncestorScrollOrResizeHandler: (event: JQuery.TriggeredEvent<HTMLDivElement>) => void;
  protected _fixScrollbarHandler: () => void;
  protected _unfixScrollbarHandler: () => void;
  protected _$thumb: JQuery;
  protected _$thumbHandle: JQuery;
  protected _$ancestors: JQuery;

  constructor() {
    super();
    this.$container = null;
    this.$thumb = null;
    this.$thumbHandle = null;
    this.axis = 'y';
    this.borderless = false;
    this.mouseWheelNeedsShift = false;
    this._scrollSize = null;
    this._offsetSize = null;
    this._dim = 'Height';
    this._dir = 'top';
    this._dirReverse = 'bottom';
    this._scrollDir = 'scrollTop';
    this._thumbClipping = new Insets(0, 0, 0, 0);
    this._onScrollHandler = this._onScroll.bind(this);
    this._onScrollWheelHandler = this._onScrollWheel.bind(this);
    this._onScrollbarMouseDownHandler = this._onScrollbarMouseDown.bind(this);
    this._onTouchStartHandler = this._onTouchStart.bind(this);
    this._onThumbMouseDownHandler = this._onThumbMouseDown.bind(this);
    this._onDocumentMousemoveHandler = this._onDocumentMousemove.bind(this);
    this._onDocumentMouseUpHandler = this._onDocumentMouseUp.bind(this);
    this._onAncestorScrollOrResizeHandler = this.update.bind(this);
    this._fixScrollbarHandler = this._fixScrollbar.bind(this);
    this._unfixScrollbarHandler = this._unfixScrollbar.bind(this);
  }

  protected override _render() {
    this._ensureParentPosition();

    // Create scrollbar and thumb
    this.$container = this.$parent
      .appendDiv('scrollbar')
      .addClass(this.axis + '-axis');
    this._$thumb = this.$container
      .appendDiv('scrollbar-thumb')
      .addClass(this.axis + '-axis');
    this._$thumbHandle = this._$thumb
      .appendDiv('scrollbar-thumb-handle')
      .addClass(this.axis + '-axis');

    if (this.borderless) {
      this.$container.addClass('borderless');
    }

    // Init helper variables based on axis (x/y)
    this._dim = this.axis === 'x' ? 'Width' : 'Height';
    this._dir = this.axis === 'x' ? 'left' : 'top';
    this._dirReverse = this.axis === 'x' ? 'right' : 'bottom';
    this._scrollDir = this.axis === 'x' ? 'scrollLeft' : 'scrollTop';

    // Install listeners
    let scrollbars = this.$parent.data('scrollbars');
    if (!scrollbars) {
      throw new Error('Data "scrollbars" missing in ' + graphics.debugOutput(this.$parent) + '\nAncestors: ' + this.ancestorsToString(1));
    }
    this.$parent
      .on('DOMMouseScroll mousewheel', this._onScrollWheelHandler)
      .on('scroll', this._onScrollHandler)
      .onPassive('touchstart', this._onTouchStartHandler);
    scrollbars.forEach(scrollbar => {
      scrollbar.on('scrollStart', this._fixScrollbarHandler);
      scrollbar.on('scrollEnd', this._unfixScrollbarHandler);
    });
    this.$container.on('mousedown', this._onScrollbarMouseDownHandler);
    this._$thumb.on('mousedown', this._onThumbMouseDownHandler);
    // Scrollbar might be clipped to prevent overlapping an ancestor. In order to reset this clipping the scrollbar needs
    // an update whenever a parent div is scrolled ore resized.
    this._$ancestors = this.$container.parents('div')
      .on('scroll resize', this._onAncestorScrollOrResizeHandler);
  }

  protected override _remove() {
    // Uninstall listeners
    this.$parent
      .off('DOMMouseScroll mousewheel', this._onScrollWheelHandler)
      .off('scroll', this._onScrollHandler)
      .offPassive('touchstart', this._onTouchStartHandler);
    let scrollbars = this.$parent.data('scrollbars');
    if (scrollbars) {
      // Scrollbars may be undefined if this.$parent is detached -> don't fail
      scrollbars.forEach(scrollbar => {
        scrollbar.off('scrollStart', this._fixScrollbarHandler);
        scrollbar.off('scrollEnd', this._unfixScrollbarHandler);
      });
    }
    this.$container.off('mousedown', this._onScrollbarMouseDownHandler);
    this._$thumb.off('mousedown', '', this._onThumbMouseDownHandler);
    this._$ancestors.off('scroll resize', this._onAncestorScrollOrResizeHandler);
    this._$ancestors = null;

    super._remove();
  }

  protected override _renderOnAttach() {
    super._renderOnAttach();
    this._ensureParentPosition();
  }

  protected _ensureParentPosition() {
    // Container with JS scrollbars must have either relative or absolute position
    // otherwise we cannot determine the correct dimension of the scrollbars
    if (this.$parent && this.$parent.isAttached()) {
      let cssPosition = this.$parent.css('position');
      if (!scout.isOneOf(cssPosition, 'relative', 'absolute')) {
        this.$parent.css('position', 'relative');
      }
    }
  }

  /**
   * scroll by "diff" in px (positive and negative)
   */
  scroll(diff: number) {
    let posOld = Math.max(0, this.$parent[this._scrollDir]());
    this._scrollToAbsolutePoint(posOld + diff);
  }

  /**
   * scroll to absolute point (expressed as absolute point in px)
   */
  protected _scrollToAbsolutePoint(absolutePoint: number) {
    let scrollPos = Math.min(
      (this._scrollSize - this._offsetSize + 1), // scrollPos can't be larger than the start of last page. Add +1 because at least chrome has issues to scroll to the very bottom if scrollTop is fractional
      Math.max(0, Math.round(absolutePoint))); // scrollPos can't be negative

    this.$parent[this._scrollDir](scrollPos);
  }

  /**
   * do not use this internal method (triggered by scroll event)
   */
  update() {
    if (!this.rendered) {
      return;
    }
    let margin = this.$container['cssMargin' + this.axis.toUpperCase()]();
    let scrollPos = this.$parent[this._scrollDir]();
    let scrollLeft = this.$parent.scrollLeft();
    let scrollTop = this.$parent.scrollTop();

    this.reset();

    this._offsetSize = this.$parent[0]['offset' + this._dim];
    this._scrollSize = this.$parent[0]['scroll' + this._dim];

    // calc size and range of thumb
    let thumbSize = Math.max(this._offsetSize * this._offsetSize / this._scrollSize - margin, 25);
    let thumbRange = this._offsetSize - thumbSize - margin;

    // set size of thumb
    this._$thumb.css(this._dim.toLowerCase(), thumbSize);

    // set location of thumb
    let posNew = scrollPos / (this._scrollSize - this._offsetSize) * thumbRange;
    this._$thumb.css(this._dir, posNew);

    // Add 1px to make sure scroll bar is not shown if width is a floating point value.
    // Even if we were using getBoundingClientRect().width to get an exact width,
    // it would not help because scroll size is always an integer
    let offsetFix = 1;

    // show scrollbar
    if (this._offsetSize + offsetFix >= this._scrollSize) {
      this.$container.css('display', 'none');
    } else {
      this.$container.css('display', '');

      // indicate that thumb movement is not possible
      if (this._isContainerTooSmallForThumb()) {
        this._$thumb.addClass('container-too-small-for-thumb');
      } else {
        this._$thumb.removeClass('container-too-small-for-thumb');
      }
    }

    this._clipWhenOverlappingAncestor();

    // Position the scrollbar(s)
    // Always update both to make sure every scrollbar (x and y) is positioned correctly
    this.$container.cssRight(-1 * scrollLeft);
    this.$container.cssBottom(-1 * scrollTop);
  }

  protected _resetClipping() {
    // Only reset dimension and position for the secondary axis,
    // for the scroll-axis these properties are set during update()
    if (this.axis === 'y') {
      this._$thumb
        .css('width', '')
        .css('left', '');
    } else {
      this._$thumb
        .css('height', '')
        .css('top', '');
    }
    this._$thumb.removeClass('clipped-left clipped-right clipped-top clipped-bottom');
    this._thumbClipping = new Insets(0, 0, 0, 0);
  }

  /**
   * Make sure scrollbar does not appear outside an ancestor when fixed
   */
  protected _clipWhenOverlappingAncestor() {
    this._resetClipping();

    // Clipping is only needed when scrollbar has a fixed position.
    // Otherwise the over-size is handled by 'overflow: hidden;'.
    if (this.$container.css('position') === 'fixed') {
      let thumbBounds = graphics.offsetBounds(this._$thumb);
      let thumbWidth = thumbBounds.width;
      let thumbHeight = thumbBounds.height;
      let thumbEndX = thumbBounds.x + thumbBounds.width;
      let thumbEndY = thumbBounds.y + thumbBounds.height;
      let biggestAncestorBeginX = 0;
      let biggestAncestorBeginY = 0;
      let smallestAncestorEndX = thumbEndX;
      let smallestAncestorEndY = thumbEndY;

      // Find nearest clip boundaries: It is not necessarily the boundary of the closest ancestor-div in the DOM,
      // because ancestor-divs themselves may be scrolled.
      this.$container.parents('div').each(function() {
        let $ancestor = $(this);
        let ancestorBounds = graphics.offsetBounds($ancestor);
        if ($ancestor.css('overflow-x') !== 'visible') {
          if (ancestorBounds.x > biggestAncestorBeginX) {
            biggestAncestorBeginX = ancestorBounds.x;
          }
          let ancestorEndX = ancestorBounds.x + ancestorBounds.width;
          if (ancestorEndX < smallestAncestorEndX) {
            smallestAncestorEndX = ancestorEndX;
          }
        }
        if ($ancestor.css('overflow-y') !== 'visible') {
          if (ancestorBounds.y > biggestAncestorBeginY) {
            biggestAncestorBeginY = ancestorBounds.y;
          }
          let ancestorEndY = ancestorBounds.y + ancestorBounds.height;
          if (ancestorEndY < smallestAncestorEndY) {
            smallestAncestorEndY = ancestorEndY;
          }
        }
      });

      let clipLeft = 0;
      let clipRight = 0;
      let clipTop = 0;
      let clipBottom = 0;

      // clip left
      if (biggestAncestorBeginX > thumbBounds.x) {
        clipLeft = biggestAncestorBeginX - thumbBounds.x;
        thumbWidth -= clipLeft;
        this._$thumb
          .css('width', thumbWidth)
          .css('left', graphics.bounds(this._$thumb).x + clipLeft)
          .addClass('clipped-left');
      }

      // clip top
      if (biggestAncestorBeginY > thumbBounds.y) {
        clipTop = biggestAncestorBeginY - thumbBounds.y;
        thumbHeight -= clipTop;
        this._$thumb
          .css('height', thumbHeight)
          .css('top', graphics.bounds(this._$thumb).y + clipTop)
          .addClass('clipped-top');
      }

      // clip right
      if (thumbEndX > smallestAncestorEndX) {
        clipRight = thumbEndX - smallestAncestorEndX;
        this._$thumb
          .css('width', thumbWidth - clipRight)
          .addClass('clipped-right');
      }

      // clip bottom
      if (thumbEndY > smallestAncestorEndY) {
        clipBottom = thumbEndY - smallestAncestorEndY;
        this._$thumb
          .css('height', thumbHeight - clipBottom)
          .addClass('clipped-bottom');
      }

      this._thumbClipping = new Insets(clipTop, clipRight, clipBottom, clipLeft);
    }
  }

  /**
   * Resets thumb size and scrollbar position to make sure it does not extend the scrollSize
   */
  reset() {
    this._$thumb.css(this._dim.toLowerCase(), 0);
    this.$container.cssRight(0);
    this.$container.cssBottom(0);
  }

  /*
   * EVENT HANDLING
   */

  protected override _onScroll(event: JQuery.ScrollEvent) {
    this.update();
  }

  protected _onTouchStart(event: JQuery.TouchStartEvent<HTMLDivElement>) {
    // In hybrid mode scroll bar is moved by the scroll event.
    // On a mobile device scroll events are fired delayed so the update will be delayed as well.
    // This will lead to flickering and could be prevented by calling fixScrollbar. But unfortunately calling fix will stop the scroll pane from scrolling immediately, at least in Edge.
    // In order to reduce the flickering the current approach is to hide the scrollbars while scrolling (only in this specific hybrid touch scrolling)
    events.onScrollStartEndDuringTouch(this.$parent, () => {
      if (!this.rendered) {
        return;
      }
      this.$container.css('opacity', 0);
    }, () => {
      if (!this.rendered) {
        return;
      }
      this.$container.css('opacity', '');
    });
  }

  protected _onScrollWheel(event: JQueryMouseWheelEvent<HTMLDivElement>): boolean {
    if (!this.$container.isVisible()) {
      return true; // ignore scroll wheel event if there is no scroll bar visible
    }
    if (event.ctrlKey) {
      return true; // allow ctrl + mousewheel to zoom the page
    }
    if (this.mouseWheelNeedsShift !== event.shiftKey) {
      return true; // only scroll if shift modifier matches
    }
    let originalEvent: OldWheelEvent = event.originalEvent || this.$container.window(true).event['originalEvent'];
    let w = originalEvent.wheelDelta ? -originalEvent.wheelDelta / 2 : originalEvent.detail * 20;

    this.notifyBeforeScroll();
    this.scroll(w);
    this.notifyAfterScroll();

    return false;
  }

  protected _onScrollbarMouseDown(event: JQuery.MouseDownEvent<HTMLDivElement>) {
    this.notifyBeforeScroll();

    let clickableAreaSize = this.$container[this._dim.toLowerCase()]();

    let offset = this.$container.offset()[this._dir];
    let clicked = (this.axis === 'x' ? event.pageX : event.pageY) - offset;

    let percentage;

    if (this._isContainerTooSmallForThumb()) {
      percentage = Math.min(1, Math.max(0, (clicked / clickableAreaSize))); // percentage can't be larger than 1, nor negative
      this._scrollToAbsolutePoint((percentage * this._scrollSize) - Math.round(this._offsetSize / 2));
    } else { // move the thumb center to clicked point
      let thumbSize = this._$thumb['outer' + this._dim](true);
      let minPossible = Math.round(thumbSize / 2);
      let maxPossible = clickableAreaSize - Math.round(thumbSize / 2);

      let rawPercentage = ((clicked - minPossible) * (1 / (maxPossible - minPossible)));
      percentage = Math.min(1, Math.max(0, rawPercentage)); // percentage can't be larger than 1, nor negative

      this._scrollToAbsolutePoint(percentage * (this._scrollSize - this._offsetSize));
    }

    this.notifyAfterScroll();
  }

  protected _onThumbMouseDown(event: JQuery.MouseDownEvent<HTMLDivElement>): boolean {
    // ignore event if container is too small for thumb movement
    if (this._isContainerTooSmallForThumb()) {
      return true; // let _onScrollbarMouseDown handle the click event
    }
    this.notifyBeforeScroll();
    // calculate thumbCenterOffset in px (offset from clicked point to thumb center)
    let clipped = (this.axis === 'x' ? this._thumbClipping.horizontal() : this._thumbClipping.vertical());
    let thumbSize = clipped + this._$thumb['outer' + this._dim](true); // including border, margin and padding
    let thumbClippingOffset = (this.axis === 'x' ? this._thumbClipping.left : this._thumbClipping.top);
    let thumbCenter = this._$thumb.offset()[this._dir] + Math.floor(thumbSize / 2) - thumbClippingOffset;
    let thumbCenterOffset = Math.round((this.axis === 'x' ? event.pageX : event.pageY) - thumbCenter);

    this._$thumb.addClass('scrollbar-thumb-move');
    this._$thumb.document()
      .on('mousemove', {thumbCenterOffset: thumbCenterOffset}, this._onDocumentMousemoveHandler)
      .one('mouseup', this._onDocumentMouseUpHandler);

    return false;
  }

  protected _onDocumentMousemove(event: JQuery.MouseMoveEvent<Document>) {
    // Scrollbar may be removed in the meantime
    if (!this.rendered) {
      return;
    }

    // represents offset in px of clicked point in thumb to the center of the thumb (positive and negative)
    let thumbCenterOffset = event.data.thumbCenterOffset as number;

    let clipped = (this.axis === 'x' ? this._thumbClipping.horizontal() : this._thumbClipping.vertical());
    let thumbSize = clipped + this._$thumb['outer' + this._dim](true); // including border, margin and padding
    let size = this.$container[this._dim.toLowerCase()]() - thumbSize; // size of div excluding margin/padding/border
    let offset = this.$container.offset()[this._dir] + (thumbSize / 2);

    let movedTo = Math.min(
      size,
      Math.max(0, (this.axis === 'x' ? event.pageX : event.pageY) - offset - thumbCenterOffset));

    let percentage = Math.min(
      1, // percentage can't be larger than 1
      Math.max(0, (movedTo / size))); // percentage can't be negative

    let posNew = (percentage * (this._scrollSize - this._offsetSize));
    this._scrollToAbsolutePoint(posNew);
  }

  protected _onDocumentMouseUp(event: JQuery.MouseUpEvent<Document>): boolean {
    let $document = $(event.currentTarget);
    $document.off('mousemove', this._onDocumentMousemoveHandler);
    if (this.rendered) {
      this._$thumb.removeClass('scrollbar-thumb-move');
    }
    this.notifyAfterScroll();
    return false;
  }

  notifyBeforeScroll() {
    this.trigger('scrollStart');
  }

  notifyAfterScroll() {
    this.trigger('scrollEnd');
  }

  /*
   * Fix Scrollbar
   */

  /**
   * Sets the position to fixed and updates left and top position
   * (This is necessary to prevent flickering in IE)
   */
  protected _fixScrollbar() {
    scrollbars.fix(this.$container);
    this.update();
  }

  /**
   * Reverts the changes made by _fixScrollbar
   */
  protected _unfixScrollbar() {
    // true = do it immediately without a timeout.
    // This is important because scrollTop may be set during layout but before the element is positioned correctly (e.g. popup)
    // which could have the effect that the scroll bar is drown outside the widget
    scrollbars.unfix(this.$container, null, true);
    this.update();
  }

  /*
   * INTERNAL METHODS
   */

  /**
   * If the thumb gets bigger than its container this method will return true, otherwise false
   */
  protected _isContainerTooSmallForThumb(): boolean {
    let thumbSize: number = this._$thumb['outer' + this._dim](true);
    let thumbMovableAreaSize: number = this.$container[this._dim.toLowerCase()]();
    return thumbSize >= thumbMovableAreaSize;
  }
}
