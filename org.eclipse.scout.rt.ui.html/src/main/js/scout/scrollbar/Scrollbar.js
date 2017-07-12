/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.Scrollbar = function() {
  scout.Scrollbar.parent.call(this);

  // jQuery Elements
  this.$container; // Scrollbar <div>
  this.$thumb; // Scrollbar Thumb <div> ("handle")

  // Defaults
  this.axis = 'y';
  this.borderless = false;
  this.mouseWheelNeedsShift = false;

  // Varaibles for calculation
  this._scrollSize;
  this._offsetSize;

  // Axis based helper variables (y)
  this._dim = 'Height'; // x: 'Width'
  this._dir = 'top'; // x: 'left'
  this._dirReverse = 'bottom'; // x: 'right'
  this._scrollDir = 'scrollTop'; // x: 'scrollLeft
  this._thumbClipping = new scout.Insets(0, 0, 0, 0);

  // Event Handling
  this._onScrollHandler = this._onScroll.bind(this);
  this._onScrollWheelHandler = this._onScrollWheel.bind(this);
  this._onScrollbarMouseDownHandler = this._onScrollbarMouseDown.bind(this);
  this._onThumbMouseDownHandler = this._onThumbMouseDown.bind(this);
  this._onDocumentMousemoveHandler = this._onDocumentMousemove.bind(this);
  this._onDocumentMouseUpHandler = this._onDocumentMouseUp.bind(this);
  this._onAncestorScrollOrResizeHandler = this.update.bind(this);

  // Fix Scrollbar
  this._fixScrollbarHandler = this._fixScrollbar.bind(this);
  this._unfixScrollbarHandler = this._unfixScrollbar.bind(this);
};
scout.inherits(scout.Scrollbar, scout.Widget);

scout.Scrollbar.prototype._render = function() {
  // Create scrollbar and thumb
  this.$container = this.$parent
    .appendDiv('scrollbar')
    .addClass(this.axis + '-axis');
  this._$thumb = this.$container
    .appendDiv('scrollbar-thumb')
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
  var scrollbars = this.$parent.data('scrollbars');
  if (!scrollbars) {
    throw new Error('Data "scrollbars" missing in ' + scout.graphics.debugOutput(this.$parent) + '\nAncestors: ' + this.ancestorsToString(1));
  }
  this.$parent
    .on('DOMMouseScroll mousewheel', this._onScrollWheelHandler)
    .on('scroll', this._onScrollHandler);
  scrollbars.forEach(function(scrollbar) {
    scrollbar.on('scrollStart', this._fixScrollbarHandler);
    scrollbar.on('scrollEnd', this._unfixScrollbarHandler);
  }.bind(this));
  this.$container.on('mousedown', this._onScrollbarMouseDownHandler);
  this._$thumb.on('mousedown', this._onThumbMouseDownHandler);
  // Scrollbar might be clipped to prevent overlapping an ancestor. In order to reset this clipping the scrollbar needs
  // an update whenever a parent div is scrolled ore resized.
  this._$anchestors = this.$container.parents('div')
    .on('scroll resize', this._onAncestorScrollOrResizeHandler);
};

scout.Scrollbar.prototype._remove = function() {
  // Uninstall listeners
  var scrollbars = this.$parent.data('scrollbars');
  this.$parent
    .off('DOMMouseScroll mousewheel', this._onScrollWheelHandler)
    .off('scroll', this._onScrollHandler);
  scrollbars.forEach(function(scrollbar) {
    scrollbar.off('scrollStart', this._fixScrollbarHandler);
    scrollbar.off('scrollEnd', this._unfixScrollbarHandler);
  }.bind(this));
  this.$container.off('mousedown', this._onScrollbarMouseDownHandler);
  this._$thumb.off('mousedown', '', this._onThumbMouseDownHandler);
  this._$anchestors.off('scroll resize', this._onAncestorScrollOrResizeHandler);
  this._$anchestors = null;

  scout.Scrollbar.parent.prototype._remove.call(this);
};

/**
 * scroll by "diff" in px (positive and negative)
 */
scout.Scrollbar.prototype.scroll = function(diff) {
  var posOld = Math.max(0, this.$parent[this._scrollDir]());
  var posNew = posOld + (diff * (this._scrollSize / this._offsetSize));

  this._scrollToAbsolutePoint(posNew);
};

/**
 * scroll to absolute point (expressed as absolute point in px)
 */
scout.Scrollbar.prototype._scrollToAbsolutePoint = function(absolutePoint) {
  var scrollPos = Math.min(
    (this._scrollSize - this._offsetSize), // scrollPos can't be larger than the start of last page
    Math.max(0, Math.round(absolutePoint))); // scrollPos can't be negative

  this.$parent[this._scrollDir](scrollPos);
};

/**
 * do not use this internal method (triggered by scroll event)
 */
scout.Scrollbar.prototype.update = function() {
  var margin = this.$container['cssMargin' + this.axis.toUpperCase()]();
  var scrollPos = this.$parent[this._scrollDir]();
  var scrollLeft = this.$parent.scrollLeft();
  var scrollTop = this.$parent.scrollTop();

  this.reset();

  this._offsetSize = this.$parent[0]['offset' + this._dim];
  this._scrollSize = this.$parent[0]['scroll' + this._dim];

  // calc size and range of thumb
  var thumbSize = Math.max(this._offsetSize * this._offsetSize / this._scrollSize - margin, 25);
  var thumbRange = this._offsetSize - thumbSize - margin;

  // set size of thumb
  this._$thumb.css(this._dim.toLowerCase(), thumbSize);

  // set location of thumb
  var posNew = scrollPos / (this._scrollSize - this._offsetSize) * thumbRange;
  this._$thumb.css(this._dir, posNew);

  // In IE scrollsize sometimes is 1px bigger than offsetSize even in situations when the content should easily fit in
  // the rendered size. This fix prevents the scrollbar from being shown in IE when not expected.
  var ieOffsetFix = (scout.device.browser === scout.Device.Browser.INTERNET_EXPLORER) ? 1 : 0;

  // show scrollbar
  if (this._offsetSize + ieOffsetFix >= this._scrollSize) {
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
};

scout.Scrollbar.prototype._resetClipping = function() {
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
  this._thumbClipping = new scout.Insets(0, 0, 0, 0);
};

/**
 * Make sure scrollbar does not appear outside an ancestor when fixed
 */
scout.Scrollbar.prototype._clipWhenOverlappingAncestor = function() {
  this._resetClipping();

  // Clipping is only needed when scrollbar has a fixed position.
  // Otherwise the over-size is handled by 'overflow: hidden;'.
  if (this.$container.css('position') === 'fixed') {
    var thumbBounds = scout.graphics.offsetBounds(this._$thumb);
    var thumbWidth = thumbBounds.width;
    var thumbHeight = thumbBounds.height;
    var thumbEndX = thumbBounds.x + thumbBounds.width;
    var thumbEndY = thumbBounds.y + thumbBounds.height;
    var biggestAncestorBeginX = 0;
    var biggestAncestorBeginY = 0;
    var smallestAncestorEndX = thumbEndX;
    var smallestAncestorEndY = thumbEndY;

    // Find nearest clip boundaries: It is not necessarily the boundary of the closest ancestor-div in the DOM,
    // because ancestor-divs themselves may be scrolled.
    this.$container.parents('div').each(function() {
      var $ancestor = $(this);
      var ancestorBounds = scout.graphics.offsetBounds($ancestor);
      if ($ancestor.css('overflow-x') !== 'visible') {
        if (ancestorBounds.x > biggestAncestorBeginX) {
          biggestAncestorBeginX = ancestorBounds.x;
        }
        var ancestorEndX = ancestorBounds.x + ancestorBounds.width;
        if (ancestorEndX < smallestAncestorEndX) {
          smallestAncestorEndX = ancestorEndX;
        }
      }
      if ($ancestor.css('overflow-y') !== 'visible') {
        if (ancestorBounds.y > biggestAncestorBeginY) {
          biggestAncestorBeginY = ancestorBounds.y;
        }
        var ancestorEndY = ancestorBounds.y + ancestorBounds.height;
        if (ancestorEndY < smallestAncestorEndY) {
          smallestAncestorEndY = ancestorEndY;
        }
      }
    });

    var clipLeft = 0;
    var clipRight = 0;
    var clipTop = 0;
    var clipBottom = 0;

    // clip left
    if (biggestAncestorBeginX > thumbBounds.x) {
      clipLeft = biggestAncestorBeginX - thumbBounds.x;
      thumbWidth -= clipLeft;
      this._$thumb
        .css('width', thumbWidth)
        .css('left', scout.graphics.bounds(this._$thumb).x + clipLeft)
        .addClass('clipped-left');
    }

    // clip top
    if (biggestAncestorBeginY > thumbBounds.y) {
      clipTop = biggestAncestorBeginY - thumbBounds.y;
      thumbHeight -= clipTop;
      this._$thumb
        .css('height', thumbHeight)
        .css('top', scout.graphics.bounds(this._$thumb).y + clipTop)
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

    this._thumbClipping = new scout.Insets(clipTop, clipRight, clipBottom, clipLeft);
  }
};

/**
 * Resets thumb size and scrollbar position to make sure it does not extend the scrollSize
 */
scout.Scrollbar.prototype.reset = function() {
  this._$thumb.css(this._dim.toLowerCase(), 0);
  this.$container.cssRight(0);
  this.$container.cssBottom(0);
};

/*
 * EVENT HANDLING
 */

scout.Scrollbar.prototype._onScroll = function(event) {
  this.update();
};

scout.Scrollbar.prototype._onScrollWheel = function(event) {
  var w, d;
  if (!this.$container.isVisible()) {
    return true; // ignore scroll wheel event if there is no scroll bar visible
  }
  if (event.ctrlKey) {
    return true; // allow ctrl + mousewheel to zoom the page
  }
  if (this.mouseWheelNeedsShift !== event.shiftKey) {
    return true; // only scroll if shift modifier matches
  }
  event = event.originalEvent || this.$container.window(true).event.originalEvent;
  w = event.wheelDelta ? -event.wheelDelta / 2 : event.detail * 20;
  d = this._scrollSize / this._offsetSize;

  this.notifyBeforeScroll();
  this.scroll(w / d);
  this.notifyAfterScroll();

  return false;
};

scout.Scrollbar.prototype._onScrollbarMouseDown = function(event) {
  this.notifyBeforeScroll();

  var clickableAreaSize = this.$container[this._dim.toLowerCase()]();

  var offset = this.$container.offset()[this._dir];
  var clicked = (this.axis === 'x' ? event.pageX : event.pageY) - offset;

  var percentage;

  if (this._isContainerTooSmallForThumb()) {
    percentage = Math.min(1, Math.max(0, (clicked / clickableAreaSize))); // percentage can't be larger than 1, nor negative
    this._scrollToAbsolutePoint((percentage * this._scrollSize) - Math.round(this._offsetSize / 2));
  } else { // move the thumb center to clicked point
    var thumbSize = this._$thumb['outer' + this._dim](true);
    var minPossible = Math.round(thumbSize / 2);
    var maxPossible = clickableAreaSize - Math.round(thumbSize / 2);

    var rawPercentage = ((clicked - minPossible) * (1 / (maxPossible - minPossible)));
    percentage = Math.min(1, Math.max(0, rawPercentage)); // percentage can't be larger than 1, nor negative

    this._scrollToAbsolutePoint(percentage * (this._scrollSize - this._offsetSize));
  }

  this.notifyAfterScroll();
};

scout.Scrollbar.prototype._onThumbMouseDown = function(event) {
  // ignore event if container is too small for thumb movement
  if (this._isContainerTooSmallForThumb()) {
    return true; // let _onScrollbarMouseDown handle the click event
  } else {
    this.notifyBeforeScroll();
    // calculate thumbCenterOffset in px (offset from clicked point to thumb center)
    var clipped = (this.axis === 'x' ? this._thumbClipping.horizontal() : this._thumbClipping.vertical());
    var thumbSize = clipped + this._$thumb['outer' + this._dim](true); //including border, margin and padding
    var thumbClippingOffset = (this.axis === 'x' ? this._thumbClipping.left : this._thumbClipping.top);
    var thumbCenter = this._$thumb.offset()[this._dir] + Math.floor(thumbSize / 2) - thumbClippingOffset;
    var thumbCenterOffset = Math.round((this.axis === 'x' ? event.pageX : event.pageY) - thumbCenter);

    this._$thumb.addClass('scrollbar-thumb-move');
    this._$thumb
      .document()
      .on('mousemove', {
        'thumbCenterOffset': thumbCenterOffset
      }, this._onDocumentMousemoveHandler)
      .one('mouseup', this._onDocumentMouseUpHandler);

    return false;
  }
};

scout.Scrollbar.prototype._onDocumentMousemove = function(event) {
  // Scrollbar may be removed in the meantime
  if (!this.rendered) {
    return;
  }

  // represents offset in px of clicked point in thumb to the center of the thumb (positive and negative)
  var thumbCenterOffset = event.data.thumbCenterOffset;

  var clipped = (this.axis === 'x' ? this._thumbClipping.horizontal() : this._thumbClipping.vertical());
  var thumbSize = clipped + this._$thumb['outer' + this._dim](true); //including border, margin and padding
  var size = this.$container[this._dim.toLowerCase()]() - thumbSize; // size of div excluding margin/padding/border
  var offset = this.$container.offset()[this._dir] + (thumbSize / 2);

  var movedTo = Math.min(
    size,
    Math.max(0, (this.axis === 'x' ? event.pageX : event.pageY) - offset - thumbCenterOffset));

  var percentage = Math.min(
    1, // percentage can't be larger than 1
    Math.max(0, (movedTo / size))); // percentage can't be negative

  var posNew = (percentage * (this._scrollSize - this._offsetSize));
  this._scrollToAbsolutePoint(posNew);
};

scout.Scrollbar.prototype._onDocumentMouseUp = function(event) {
  var $document = $(event.currentTarget);
  $document.off('mousemove', this._onDocumentMousemoveHandler);
  if (this.rendered) {
    this._$thumb.removeClass('scrollbar-thumb-move');
  }
  this.notifyAfterScroll();
  return false;
};

scout.Scrollbar.prototype.notifyBeforeScroll = function() {
  this.trigger('scrollStart');
};

scout.Scrollbar.prototype.notifyAfterScroll = function() {
  this.trigger('scrollEnd');
};

/*
 * Fix Scrollbar
 */

/**
 * Sets the position to fixed and updates left and top position
 * (This is necessary to prevent flickering in IE)
 */
scout.Scrollbar.prototype._fixScrollbar = function() {
  scout.scrollbars.fix(this.$container);
  this.update();
};

/**
 * Reverts the changes made by _fixScrollbar
 */
scout.Scrollbar.prototype._unfixScrollbar = function() {
  this._unfixTimeoutId = scout.scrollbars.unfix(this.$container, this._unfixTimeoutId);
  this.update();
};

/*
 * INTERNAL METHODS
 */

/**
 * If the thumb gets bigger than its container this method will return true, otherwise false
 */
scout.Scrollbar.prototype._isContainerTooSmallForThumb = function() {
  var thumbSize = this._$thumb['outer' + this._dim](true);
  var thumbMovableAreaSize = this.$container[this._dim.toLowerCase()]();
  return thumbSize >= thumbMovableAreaSize;
};
