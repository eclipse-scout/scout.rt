scout.scrollbars = {

  /**
   * Static function to install a scrollbar on a container.
   * When the client supports pretty native scrollbars, we use them by default.
   * Otherwise we install JS-based scrollbars. In that case the install function
   * adds an additional 'scrollable' DIV to the DOM. For native scrollbars we
   * must set some additional CSS styles.
   *
   * The function returns either the $container (when native) or the additional
   * scrollable (when JS-based), you should use the returned DOM element when you
   * add elements.
   */
  install: function($container, options) {
    var nativeScrollbars = false,
      htmlContainer = scout.HtmlComponent.optGet($container),
      htmlScrollable, $scrollable, scrollbar;

    options = options || {};
    if (nativeScrollbars) {
      $.log.debug('use native scrollbars for container ' + scout.graphics.debugOutput($container));
      $container.
        css('overflow-x', 'hidden').
        css('overflow-y', 'auto').
        css('-webkit-overflow-scrolling', 'touch');
      htmlScrollable = htmlContainer;
      $scrollable = $container;
    } else {
      $.log.debug('install JS-scrollbars for container ' + scout.graphics.debugOutput($container) +  ' and add scrollable DIV');
      $scrollable = $container.appendDiv('scrollable');
      scrollbar = new scout.Scrollbar($scrollable, options);
      scrollbar.updateThumb();
      $scrollable.data('scrollbar', scrollbar);

      // Create a htmlComponent with a layout.
      // This is necessary in order to properly propagate the layout call to its children.
      // It is only necessary if the children use the html component pattern.
      if (htmlContainer && options.createHtmlComponent) {
        htmlScrollable = new scout.HtmlComponent($scrollable, htmlContainer.session);
        // Disable pixel based sizing to avoid having the size set. Otherwise no scrollbars would appear since it actually is the viewport size.
        htmlScrollable.pixelBasedSizing = false;
        htmlContainer.setLayout(new scout.SingleLayout(htmlScrollable));
      }
    }
    if (htmlScrollable) {
      htmlScrollable.scrollable = true;
    }
    $container.data('scrollable', true);
    return $scrollable;
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
      optionY = $selection.offset().top - $selection.parent().parent().offset().top;
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
