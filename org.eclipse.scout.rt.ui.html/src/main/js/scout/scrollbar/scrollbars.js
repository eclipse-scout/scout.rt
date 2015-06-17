scout.scrollbars = {

  /**
   * Static function to install a scrollbar on a container.
   * When the client supports pretty native scrollbars, we use them by default.
   * Otherwise we install JS-based scrollbars. In that case the install function
   * creates a new scrollbar.js. For native scrollbars we
   * must set some additional CSS styles.
   *
   */
  install: function($container, options) {
    var scrollbars, scrollbar, nativeScrollbars,
      htmlContainer = scout.HtmlComponent.optGet($container);

    options = options || {};
    options.axis = options.axis || 'both';

    if (options.nativeScrollbars !== undefined) {
      nativeScrollbars = options.nativeScrollbars;
    } else {
      nativeScrollbars = scout.device.hasPrettyScrollbars();
    }
    if (nativeScrollbars) {
      installNativeScrollbars();
    } else {
      installJsScrollbars();
    }
    if (htmlContainer) {
      htmlContainer.scrollable = true;
    }
    $container.data('scrollable', true);
    return $container;

    function installNativeScrollbars() {
      $.log.debug('use native scrollbars for container ' + scout.graphics.debugOutput($container));
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
    }

    function installJsScrollbars() {
      $.log.debug('installing JS-scrollbars for container ' + scout.graphics.debugOutput($container));
      scrollbars = [];
      if (options.axis === 'both') {
        options.axis = 'y';
        scrollbar = new scout.Scrollbar(options);
        scrollbars.push(scrollbar);

        options.axis = 'x';
        options.mouseWheelNeedsShift = true;
        scrollbar = new scout.Scrollbar(options);
        scrollbars.push(scrollbar);
      } else {
        scrollbar = new scout.Scrollbar(options);
        scrollbars.push(scrollbar);
      }
      $container.css('overflow', 'hidden');
      $container.data('scrollbars', scrollbars);
      scrollbars.forEach(function(scrollbar) {
        scrollbar.render($container);
      });
    }
  },

  update: function($scrollable) {
    var scrollbars = $scrollable.data('scrollbars');
    if (!scrollbars) {
      return;
    }
    scrollbars.forEach(function(scrollbar) {
      scrollbar.updateThumb();
    });
  },

  /**
   * Scrolls the $scrollable to the given $element (must be a child of $scrollable)
   *
   */
  scrollTo: function($scrollable, $element) {
    var scrollTo,
      scrollableH = $scrollable.height(),
      elementBounds = scout.graphics.bounds($element, true, true),
      elementTop = elementBounds.y,
      elementH = elementBounds.height,
      scrollbars;

    if (elementTop < 0) {
      scout.scrollbars.scrollTop($scrollable, $scrollable.scrollTop() + elementTop);
    } else if (elementTop + elementH > scrollableH) {
      // On IE, a fractional position gets truncated when using scrollTop -> ceil to make sure the full element is visible
      scrollTo = Math.ceil($scrollable.scrollTop() + elementTop + elementH - scrollableH);
      scout.scrollbars.scrollTop($scrollable, scrollTo);
    }
  },

  scrollTop: function($scrollable, scrollTop) {
    var scrollbar = scout.scrollbars.scrollbar($scrollable, 'y');
    if (scrollbar) {
      // js scrolling
      scrollbar.notifyBeforeScroll();
      $scrollable.scrollTop(scrollTop);
      scrollbar.notifyAfterScroll();
    } else {
      // native scrolling
      $scrollable.scrollTop(scrollTop);
    }
  },

  scrollLeft: function($scrollable, scrollLeft) {
    var scrollbar = scout.scrollbars.scrollbar($scrollable, 'x');
    scrollbar.notifyBeforeScroll();
    $scrollable.scrollLeft(scrollLeft);
    scrollbar.notifyAfterScroll();
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
   * Returns true if the location is visible in the current viewport of the $scrollable
   */
  isLocationInView: function(location, $scrollable) {
    if (!$scrollable || $scrollable.length === 0) {
      return;
    }
    var inViewY, inViewX,
      scrollableOffsetBounds = scout.graphics.offsetBounds($scrollable);

    inViewY = location.y >= scrollableOffsetBounds.y &&
      location.y < scrollableOffsetBounds.y + scrollableOffsetBounds.height;
    inViewX = location.x >= scrollableOffsetBounds.x &&
      location.x < scrollableOffsetBounds.x + scrollableOffsetBounds.width;

    return inViewY && inViewX;
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
    var bounds = scout.graphics.offsetBounds($elem);
    $elem
      .css('position', 'fixed')
      .cssLeft(bounds.x - $elem.cssMarginLeft())
      .cssTop(bounds.y - $elem.cssMarginTop())
      .cssWidth(bounds.width)
      .cssHeight(bounds.height);
  },

  /**
   * Reverts the changes made by fix().
   */
  unfix: function($elem, timeoutId) {
    clearTimeout(timeoutId);
    return setTimeout(function() {
      $elem.css({
        position: 'absolute',
        left: '',
        top: '',
        width: '',
        height: ''});
    }.bind(this), 50);
  }
};
