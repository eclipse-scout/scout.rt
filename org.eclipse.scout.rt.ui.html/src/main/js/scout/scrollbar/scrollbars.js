/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.scrollbars = {

  /**
   * Static function to install a scrollbar on a container.
   * When the client supports pretty native scrollbars, we use them by default.
   * Otherwise we install JS-based scrollbars. In that case the install function
   * creates a new scrollbar.js. For native scrollbars we
   * must set some additional CSS styles.
   *
   * @memberOf scout.scrollbars
   */

  _$scrollables: {},

  getScrollables: function(session) {
    // return scrollables for given session
    if (session) {
      return this._$scrollables[session] || [];
    }

    // return all scrollables, no matter to which session they belong
    var $scrollables = [];
    scout.objects.values(this._$scrollables).forEach(function($scrollablesPerSession) {
      scout.arrays.pushAll($scrollables, $scrollablesPerSession);
    });
    return $scrollables;
  },

  pushScrollable: function(session, $container) {
    if (this._$scrollables[session]) {
      if (this._$scrollables[session].indexOf($container) > -1) {
        // already pushed
        return;
      }
      this._$scrollables[session].push($container);
    } else {
      this._$scrollables[session] = [$container];
    }
    $.log.isTraceEnabled() && $.log.trace('Scrollable added: ' + $container.attr('class') + '. New length: ' + this._$scrollables[session].length);
  },

  removeScrollable: function(session, $container) {
    var initLength = 0;
    if (this._$scrollables[session]) {
      initLength = this._$scrollables[session].length;
      scout.arrays.$remove(this._$scrollables[session], $container);
      $.log.isTraceEnabled() && $.log.trace('Scrollable removed: ' + $container.attr('class') + '. New length: ' + this._$scrollables[session].length);
      if (initLength === this._$scrollables[session].length) {
        throw new Error('scrollable could not be removed. Potential memory leak. ' + $container.attr('class'));
      }
    } else {
      throw new Error('scrollable could not be removed. Potential memory leak. ' + $container.attr('class'));
    }
  },

  install: function($container, options) {
    options = this._createDefaultScrollToOptions(options);
    options.axis = options.axis || 'both';

    // Don't use native as variable name because it will break minifying (reserved keyword)
    var nativeScrollbars = scout.nvl(options.nativeScrollbars, scout.device.hasPrettyScrollbars());
    var hybridScrollbars = scout.nvl(options.hybridScrollbars, scout.device.canHideScrollbars());
    if (nativeScrollbars) {
      this._installNative($container, options);
    } else if (hybridScrollbars) {
      $container.addClass('hybrid-scrollable');
      this._installNative($container, options);
      this._installJs($container, options);
    } else {
      $container.css('overflow', 'hidden');
      this._installJs($container, options);
    }
    var htmlContainer = scout.HtmlComponent.optGet($container);
    if (htmlContainer) {
      htmlContainer.scrollable = true;
    }
    $container.data('scrollable', true);
    var session = options.session || options.parent.session;
    this.pushScrollable(session, $container);
    return $container;
  },

  _installNative: function($container, options) {
    if (scout.device.isIos()) {
      // On ios, container sometimes is not scrollable when installing too early
      // Happens often with nested scrollable containers (e.g. scrollable table inside a form inside a scrollable tree data)
      setTimeout(this._installNativeInternal.bind(this, $container, options));
    } else {
      this._installNativeInternal($container, options);
    }
  },

  _installNativeInternal: function($container, options) {
    $.log.isTraceEnabled() && $.log.trace('use native scrollbars for container ' + scout.graphics.debugOutput($container));
    if (options.axis === 'x') {
      $container
        .css('overflow-x', 'auto')
        .css('overflow-y', 'hidden');
    } else if (options.axis === 'y') {
      $container
        .css('overflow-x', 'hidden')
        .css('overflow-y', 'auto');
    } else {
      $container.css('overflow', 'auto');
    }
    $container.css('-webkit-overflow-scrolling', 'touch');
  },

  _installJs: function($container, options) {
    $.log.isTraceEnabled() && $.log.trace('installing JS-scrollbars for container ' + scout.graphics.debugOutput($container));
    var scrollbars = scout.arrays.ensure($container.data('scrollbars'));
    scrollbars.forEach(function(scrollbar) {
      scrollbar.destroy();
    });
    scrollbars = [];
    var scrollbar;
    if (options.axis === 'both') {
      var scrollOptions = $.extend({}, options);
      scrollOptions.axis = 'y';
      scrollbar = scout.create('Scrollbar', $.extend({}, scrollOptions));
      scrollbars.push(scrollbar);

      scrollOptions.axis = 'x';
      scrollOptions.mouseWheelNeedsShift = true;
      scrollbar = scout.create('Scrollbar', $.extend({}, scrollOptions));
      scrollbars.push(scrollbar);
    } else {
      scrollbar = scout.create('Scrollbar', $.extend({}, options));
      scrollbars.push(scrollbar);
    }
    $container.data('scrollbars', scrollbars);

    // Container with JS scrollbars must have either relative or absolute position
    // otherwise we cannot determine the correct dimension of the scrollbars
//    var cssPosition = $container.css('position');
//    if (!scout.isOneOf(cssPosition, 'relative', 'absolute')) {
//      $container.css('position', 'relative');
//    }

    scrollbars.forEach(function(scrollbar) {
      scrollbar.render($container);
//      scrollbar.update();
    });
  },

  /**
   * Removes the js scrollbars for the $container, if there are any.<p>
   */
  uninstall: function($container, session) {
    if (!$container.data('scrollable')) {
      // was not installed previously -> uninstalling not necessary
      return;
    }

    var scrollbars = $container.data('scrollbars');
    if (scrollbars) {
      scrollbars.forEach(function(scrollbar) {
        scrollbar.destroy();
      });
    }
    this.removeScrollable(session, $container);
    $container.removeData('scrollable');
    $container.css('overflow', '');
    $container.removeData('scrollbars');

    var htmlContainer = scout.HtmlComponent.optGet($container);
    if (htmlContainer) {
      htmlContainer.scrollable = false;
    }
  },

  /**
   * Recalculates the scrollbar size and position.
   * @param $scrollable JQuery element that has .data('scrollbars'), when $scrollable is falsy the function returns immediately
   * @param immediate set to true to immediately update the scrollbar, If set to false,
   *        it will be queued in order to prevent unnecessary updates.
   */
  update: function($scrollable, immediate) {
    if (!$scrollable || !$scrollable.data('scrollable')) {
      return;
    }
    var scrollbars = $scrollable.data('scrollbars');
    if (!scrollbars) {
      if (scout.device.isIos()) {
        this._handleIosPaintBug($scrollable);
      }
      return;
    }
    if (immediate) {
      this._update(scrollbars);
      return;
    }
    if ($scrollable.data('scrollbarUpdatePending')) {
      return;
    }
    // Executes the update later to prevent unnecessary updates
    setTimeout(function() {
      this._update(scrollbars);
      $scrollable.removeData('scrollbarUpdatePending');
    }.bind(this), 0);
    $scrollable.data('scrollbarUpdatePending', true);
  },

  _update: function(scrollbars) {
    // Reset the scrollbars first to make sure they don't extend the scrollSize
    scrollbars.forEach(function(scrollbar) {
      if (scrollbar.rendered) {
        scrollbar.reset();
      }
    });
    scrollbars.forEach(function(scrollbar) {
      if (scrollbar.rendered) {
        scrollbar.update();
      }
    });
  },

  /**
   * IOS has problems with nested scrollable containers. Sometimes the outer container goes completely white hiding the elements behind.
   * This happens with the following case: Main box is scrollable but there are no scrollbars because content is smaller than container.
   * In the main box there is a tab box with a scrollable table. This table has scrollbars.
   * If the width of the tab box is adjusted (which may happen if the tab item is selected and eventually prefSize called), the main box will go white.
   * <p>
   * This happens only if -webkit-overflow-scrolling is set to touch.
   * To workaround this bug the flag -webkit-overflow-scrolling will be removed if the scrollable component won't display any scrollbars
   */
  _handleIosPaintBug: function($scrollable) {
    if ($scrollable.data('scrollbarUpdatePending')) {
      return;
    }
    setTimeout(function() {
      workaround();
      $scrollable.removeData('scrollbarUpdatePending');
    });
    $scrollable.data('scrollbarUpdatePending', true);

    function workaround() {
      var size = scout.graphics.size($scrollable).subtract(scout.graphics.insets($scrollable, {
        includePadding: false,
        includeBorder: true
      }));
      if ($scrollable[0].scrollHeight === size.height && $scrollable[0].scrollWidth === size.width) {
        $scrollable.css('-webkit-overflow-scrolling', '');
      } else {
        $scrollable.css('-webkit-overflow-scrolling', 'touch');
      }
    }
  },

  reset: function($scrollable) {
    var scrollbars = $scrollable.data('scrollbars');
    if (!scrollbars) {
      return;
    }
    scrollbars.forEach(function(scrollbar) {
      scrollbar.reset();
    });
  },

  /**
   * Scrolls the $scrollable to the given $element (must be a child of $scrollable)
   *
   * OPTION                   DEFAULT VALUE   DESCRIPTION
   * ------------------------------------------------------------------------------------------------------
   * align                    undefined       Specifies where the element should be positioned in the view port. Can either be 'top', 'center' or 'bottom'.
   *                                          If unspecified, the following rules apply:
   *                                          - If the element is above the visible area it will be aligned to top.
   *                                          - If the element is below the visible area it will be aligned to bottom.
   *                                          - If the element is already in the visible area no scrolling is done.
   *
   * animate                  false           If true, the scroll position will be animated so that the element moves smoothly to its new position
   * stop                     true            If true, all running animations are stopped before executing the current scroll request.
   *
   * @param {$} $scrollable
   *          the scrollable object
   * @param {$} $element
   *          the element to scroll to
   * @param [options]
   *          an optional options object, see table above. Short-hand version: If a string is passed instead
   *          of an object, the value is automatically converted to the option "align".
   */
  scrollTo: function($scrollable, $element, options) {
    var scrollTo,
      scrollOffsetUp = 4,
      scrollOffsetDown = 8,
      scrollableH = $scrollable.height(),
      elementBounds = scout.graphics.offsetBounds($element),
      scrollableBounds = scout.graphics.offsetBounds($scrollable),
      elementTop = elementBounds.y - scrollableBounds.y - scrollOffsetUp, // relative to scrollable y
      elementTopNew = 0,
      elementH = elementBounds.height + scrollOffsetDown,
      elementBottom = elementTop + elementH;

    if (typeof options === 'string') {
      options = {
        align: options
      };
    } else {
      options = this._createDefaultScrollToOptions(options);
    }

    var align = options.align;
    if (!align) {
      // If the element is above the visible area it will be aligned to top.
      // If the element is below the visible area it will be aligned to bottom.
      // If the element is already in the visible area no scrolling is done.
      align = (elementTop < 0) ? 'top' : (elementBottom > scrollableH ? 'bottom' : undefined);
    } else {
      align = align.toLowerCase();
    }

    if (align === 'center') {
      // align center
      scrollTo = $scrollable.scrollTop() + elementTop - Math.max(0, (scrollableH - elementH) / 2);

    } else if (align === 'top') {
      // align top
      // Element is on the top of the view port -> scroll up
      scrollTo = $scrollable.scrollTop() + elementTop;

    } else if (align === 'bottom') {
      // align bottom
      // Element is on the Bottom of the view port -> scroll down
      // On IE, a fractional position gets truncated when using scrollTop -> ceil to make sure the full element is visible
      scrollTo = Math.ceil($scrollable.scrollTop() + elementBottom - scrollableH);

      // If the viewport is very small, make sure the element is not moved outside on top
      // Otherwise when calling this function again, since the element is on the top of the view port, the scroll pane would scroll down which results in flickering
      elementTopNew = elementTop - (scrollTo - $scrollable.scrollTop());
      if (elementTopNew < 0) {
        scrollTo = scrollTo + elementTopNew;
      }
    }
    if (scrollTo) {
      scout.scrollbars.scrollTop($scrollable, scrollTo, options);
    }
  },

  _createDefaultScrollToOptions: function(options) {
    var defaults = {
      anmiate: false,
      stop: true
    };
    return $.extend({}, defaults, options);
  },

  /**
   * Horizontally scrolls the $scrollable to the given $element (must be a child of $scrollable)
   */
  scrollHorizontalTo: function($scrollable, $element, options) {
    var scrollTo,
      scrollableW = $scrollable.width(),
      elementBounds = scout.graphics.bounds($element, true),
      elementLeft = elementBounds.x,
      elementW = elementBounds.width;

    if (elementLeft < 0) {
      scout.scrollbars.scrollLeft($scrollable, $scrollable.scrollLeft() + elementLeft, options);
    } else if (elementLeft + elementW > scrollableW) {
      // On IE, a fractional position gets truncated when using scrollTop -> ceil to make sure the full element is visible
      scrollTo = Math.ceil($scrollable.scrollLeft() + elementLeft + elementW - scrollableW);
      scout.scrollbars.scrollLeft($scrollable, scrollTo, options);
    }
  },

  scrollTop: function($scrollable, scrollTop, options) {
    options = this._createDefaultScrollToOptions(options);
    var scrollbar = scout.scrollbars.scrollbar($scrollable, 'y');
    if (scrollbar) {
      scrollbar.notifyBeforeScroll();
    }

    // Not animated
    if (!options.animate) {
      if (options.stop) {
        $scrollable.stop('scroll');
      }
      $scrollable.scrollTop(scrollTop);
      if (scrollbar) {
        scrollbar.notifyAfterScroll();
      }
      return;
    }

    // Animated
    this.animateScrollTop($scrollable, scrollTop, options);
    $scrollable.promise('scroll').always(function() {
      if (scrollbar) {
        scrollbar.notifyAfterScroll();
      }
    });
  },

  scrollLeft: function($scrollable, scrollLeft, options) {
    options = this._createDefaultScrollToOptions(options);
    var scrollbar = scout.scrollbars.scrollbar($scrollable, 'x');
    if (scrollbar) {
      scrollbar.notifyBeforeScroll();
    }

    // Not animated
    if (!options.animate) {
      if (options.stop) {
        $scrollable.stop('scroll');
      }
      $scrollable.scrollLeft(scrollLeft);
      if (scrollbar) {
        scrollbar.notifyAfterScroll();
      }
      return;
    }

    // Animated
    this.animateScrollLeft($scrollable, scrollLeft, options);
    $scrollable.promise('scroll').always(function() {
      if (scrollbar) {
        scrollbar.notifyAfterScroll();
      }
    });
  },

  animateScrollTop: function($scrollable, scrollTop, options) {
    if (options.stop) {
      $scrollable.stop('scroll');
    }
    $scrollable.animate({
        scrollTop: scrollTop
      }, {
        queue: 'scroll'
      })
      .dequeue('scroll');
  },

  animateScrollLeft: function($scrollable, scrollLeft, options) {
    if (options.stop) {
      $scrollable.stop('scroll');
    }
    $scrollable.animate({
        scrollLeft: scrollLeft
      }, {
        queue: 'scroll'
      })
      .dequeue('scroll');
  },

  scrollbar: function($scrollable, axis) {
    var scrollbars = $scrollable.data('scrollbars') || [];
    return scout.arrays.find(scrollbars, function(scrollbar) {
      return scrollbar.axis === axis;
    });
  },

  scrollToBottom: function($scrollable) {
    scout.scrollbars.scrollTop($scrollable, $scrollable[0].scrollHeight - $scrollable[0].offsetHeight);
  },

  /**
   * Returns true if the location is visible in the current viewport of the $scrollable, or if $scrollable is null
   * @param location object with x and y properties
   *
   */
  isLocationInView: function(location, $scrollable) {
    if (!$scrollable || $scrollable.length === 0) {
      return true;
    }
    var scrollableOffsetBounds = scout.graphics.offsetBounds($scrollable);
    return scrollableOffsetBounds.contains(location.x, location.y);
  },

  /**
   * Attaches the given handler to each scrollable parent, including $anchor if it is scrollable as well.<p>
   * Make sure you remove the handlers when not needed anymore using offScroll.
   */
  onScroll: function($anchor, handler) {
    handler.$scrollParents = [];
    $anchor.scrollParents().each(function() {
      var $scrollParent = $(this);
      $scrollParent.on('scroll', handler);
      handler.$scrollParents.push($scrollParent);
    });
  },

  offScroll: function(handler) {
    var $scrollParents = handler.$scrollParents;
    if (!$scrollParents) {
      throw new Error('$scrollParents are not defined');
    }
    for (var i = 0; i < $scrollParents.length; i++) {
      var $elem = $scrollParents[i];
      $elem.off('scroll', handler);
    }
  },

  /**
   * Sets the position to fixed and updates left and top position.
   * This is necessary to prevent flickering in IE.
   */
  fix: function($elem) {
    if (!$elem.isVisible() || $elem.css('position') === 'fixed') {
      return;
    }

    // getBoundingClientRect used by purpose instead of scout.graphics.offsetBounds to get exact values
    // Also important: offset() of jquery returns getBoundingClientRect().top + window.pageYOffset.
    // In case of IE and zoom = 125%, the pageYOffset is 1 because the height of the navigation is bigger than the height of the desktop which may be fractional.
    var bounds = $elem[0].getBoundingClientRect();
    $elem
      .css('position', 'fixed')
      .cssLeft(bounds.left - $elem.cssMarginLeft())
      .cssTop(bounds.top - $elem.cssMarginTop())
      .cssWidth(bounds.width)
      .cssHeight(bounds.height);
  },

  /**
   * Reverts the changes made by fix().
   */
  unfix: function($elem, timeoutId, immediate) {
    clearTimeout(timeoutId);
    if (immediate) {
      this._unfix($elem);
      return;
    }
    return setTimeout(function() {
      this._unfix($elem);
    }.bind(this), 50);
  },

  _unfix: function($elem) {
    $elem.css({
      position: 'absolute',
      left: '',
      top: '',
      width: '',
      height: ''
    });
  },

  /**
   * Stores the position of all scrollables that belong to an optional session.
   * @param session (optional) when no session is given, scrollables from all sessions are stored
   */
  storeScrollPositions: function($container, session) {
    var $scrollables = this.getScrollables(session);
    if (!$scrollables) {
      return;
    }

    var scrollTop, scrollLeft;
    $scrollables.forEach(function($scrollable) {
      if ($container.isOrHas($scrollable[0])) {
        scrollTop = $scrollable.scrollTop();
        $scrollable.data('scrollTop', scrollTop);
        scrollLeft = $scrollable.scrollLeft();
        $scrollable.data('scrollLeft', $scrollable.scrollLeft());
        $.log.isTraceEnabled() && $.log.trace('Stored scroll position for ' + $scrollable.attr('class') + '. Top: ' + scrollTop + '. Left: ' + scrollLeft);
      }
    });
  },

  /**
   * Restores the position of all scrollables that belong to an optional session.
   * @param session (optional) when no session is given, scrollables from all sessions are restored
   */
  restoreScrollPositions: function($container, session) {
    var $scrollables = this.getScrollables(this.session);
    if (!$scrollables) {
      return;
    }

    var scrollTop, scrollLeft;
    $scrollables.forEach(function($scrollable) {
      if ($container.isOrHas($scrollable[0])) {
        scrollTop = $scrollable.data('scrollTop');
        if (scrollTop) {
          $scrollable.scrollTop(scrollTop);
          $scrollable.removeData('scrollTop');
        }
        scrollLeft = $scrollable.data('scrollLeft');
        if (scrollLeft) {
          $scrollable.scrollLeft(scrollLeft);
          $scrollable.removeData('scrollLeft');
        }
        // Also make sure that scroll bar is up to date
        // Introduced for use case: Open large table page, edit entry, press f5
        // -> outline tab gets rendered, scrollbar gets updated with set timeout, outline tab gets detached
        // -> update event never had any effect because it executed after detaching (due to set timeout)
        scout.scrollbars.update($scrollable);
        $.log.isTraceEnabled() && $.log.trace('Restored scroll position for ' + $scrollable.attr('class') + '. Top: ' + scrollTop + '. Left: ' + scrollLeft);
      }
    });
  },

  setVisible: function($scrollable, visible) {
    if (!$scrollable || !$scrollable.data('scrollable')) {
      return;
    }
    var scrollbars = $scrollable.data('scrollbars');
    if (!scrollbars) {
      return;
    }
    scrollbars.forEach(function(scrollbar) {
      if (scrollbar.rendered) {
        scrollbar.$container.setVisible(visible);
      }
    });
  },

  opacity: function($scrollable, opacity) {
    if (!$scrollable || !$scrollable.data('scrollable')) {
      return;
    }
    var scrollbars = $scrollable.data('scrollbars');
    if (!scrollbars) {
      return;
    }
    scrollbars.forEach(function(scrollbar) {
      if (scrollbar.rendered) {
        scrollbar.$container.css('opacity', opacity);
      }
    });
  },

  _getCompleteChildRowsHeightRecursive: function(children, getChildren, isExpanded, defaultChildHeight) {
    var height = 0;
    children.forEach(function(child) {
      if (child.height) {
        height += child.height;
      } else {
        // fallback for children with unset height
        height += defaultChildHeight;
      }
      if (isExpanded(child) && getChildren(child).length > 0) {
        height += this._getCompleteChildRowsHeightRecursive(getChildren(child), getChildren, isExpanded, defaultChildHeight);
      }
    }.bind(this));
    return height;
  },

  ensureExpansionVisible: function(parent) {
    var isParentExpanded = parent.isExpanded(parent.element);
    var children = parent.getChildren(parent.element);
    var parentPositionTop = parent.$element.position().top;
    var parentHeight = parent.element.height;
    var scrollTop = parent.$scrollable.scrollTop();

    // vertical scrolling
    if (!isParentExpanded) {
      // parent is not expanded, make sure that at least one node above the parent is visible
      if (parentPositionTop < parentHeight) {
        var minScrollTop = Math.max(scrollTop - (parentHeight - parentPositionTop), 0);
        this.scrollTop(parent.$scrollable, minScrollTop, {
          animate: true
        });
      }
    } else if (isParentExpanded && children.length > 0) {
      // parent is expanded and has children, best effort approach to show the expansion
      var fullDataHeight = parent.$scrollable.height();

      // get childRowCount considering already expanded rows
      var childRowsHeight = this._getCompleteChildRowsHeightRecursive(children, parent.getChildren, parent.isExpanded, parent.defaultChildHeight);

      // + 1.5 since its the parent's top position and we want to scroll half a row further to show that there's something after the expansion
      var additionalHeight = childRowsHeight + (1.5 * parentHeight);
      var scrollTo = parentPositionTop + additionalHeight;
      // scroll as much as needed to show the expansion but make sure that the parent row (plus one more) is still visible
      var newScrollTop = scrollTop + Math.min(scrollTo - fullDataHeight, parentPositionTop - parentHeight);
      // only scroll down
      if (newScrollTop > scrollTop) {
        this.scrollTop(parent.$scrollable, newScrollTop, {
          animate: true,
          stop: false
        });
      }
    }

    if (children.length > 0) {
      // horizontal scrolling: at least 3 levels of hierarchy should be visible (only relevant for small fields)
      var minLevelLeft = Math.max(parent.element.level - 3, 0) * parent.nodePaddingLevel;
      this.scrollLeft(parent.$scrollable, minLevelLeft, {
        animate: true,
        stop: false
      });
    }
  }
};
