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
    var scrollbar,
      nativeScrollbars = false,
      htmlContainer = scout.HtmlComponent.optGet($container);

    options = options || {};
    if (nativeScrollbars) {
      $.log.debug('use native scrollbars for container ' + scout.graphics.debugOutput($container));
      $container.
        css('overflow-x', 'hidden').
        css('overflow-y', 'auto').
        css('-webkit-overflow-scrolling', 'touch');
    } else {
      $.log.debug('installing JS-scrollbars for container ' + scout.graphics.debugOutput($container));
      scrollbar = new scout.Scrollbar($container, options);
      scrollbar.updateThumb();
      $container.data('scrollbar', scrollbar);
    }
    if (htmlContainer) {
      htmlContainer.scrollable = true;
    }
    $container.data('scrollable', true);
    return $container;
  },

  update: function($scrollable) {
    var scrollbar = $scrollable.data('scrollbar');
    if (scrollbar) {
      scrollbar.updateThumb();
    }
  },

  /**
   * Scrolls the scrollable to the given object (must be a child of scrollable)
   * @param $scrollable
   * @param $selection
   */
  scrollTo: function($scrollable, $selection) {
    var scrollbar = $scrollable.data('scrollbar'),
      scrollableH = $scrollable.height(),
      optionH = scout.graphics.getSize($selection, true).height,
      optionY,
      scrollTopFunc;

    if (scrollbar) {
      scrollTopFunc = scrollbar.scrollTop.bind(scrollbar);
      optionY = $selection.offset().top - $selection.parent().offset().top;
    } else {
      scrollTopFunc = $scrollable.scrollTop.bind($scrollable);
      optionY = $selection.position().top;
    }

    if (optionY < 0) {
      scrollTopFunc(scrollTopFunc() + optionY);
    } else if (optionY + optionH > scrollableH) {
      scrollTopFunc(scrollTopFunc() + optionY + optionH - scrollableH);
    }
  },

  scrollToBottom: function($scrollable) {
    $scrollable.scrollTop($scrollable[0].scrollHeight - $scrollable[0].offsetHeight);
  },

  /**
   * Attaches the given handler to each scrollable parent, including $origin if it is scrollable as well.<p>
   * Make sure you remove the handlers when not needed anymore using detachScrollHandlers.
   */
  attachScrollHandlers: function($origin, handler) {
    var $scrollParents = [],
      $elem = $origin;

    while ($elem.length > 0) {
      if ($elem.data('scrollable')) {
        $elem.scroll(handler);
        $elem.data('scrollHandler', handler);
        $scrollParents.push($elem);
      }
      $elem = $elem.parent();
    }
    $origin.data('scrollParents', $scrollParents);
  },

  detachScrollHandlers: function($origin) {
    var $scrollParents = $origin.data('scrollParents');
    if (!$scrollParents) {
      return;
    }
    for (var i=0; i < $scrollParents.length; i++) {
      var $elem = $scrollParents[i];
      var handler = $elem.data('scrollHandler');
      $elem.off('scroll', handler);
      $elem.removeData('scrollHandler');
    }
    $origin.removeData('scrollParents');
  }

};
