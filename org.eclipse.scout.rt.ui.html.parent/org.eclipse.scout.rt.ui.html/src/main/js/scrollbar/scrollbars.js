scout.scrollbars = {

  /**
   * Static function to install a scrollbar on a container.
   * When the client supports pretty native scrollbars, we use them by default.
   * Otherwise we install JS-based scrollbars. In that case the install function
   * adds an additional 'viewport' DIV to the DOM. For native scrollbars we
   * must set some additional CSS styles.
   *
   * The function returns either the $container (when native) or the additional
   * viewport (when JS-based), you should use the returned DOM element when you
   * add elements.
   */
  install: function($container, options) {
    var nativeScrollbars = false;
    if (nativeScrollbars) {
      $.log.debug('use native scrollbars for container ' + scout.graphics.debugOutput($container));
      $container.
        css('overflow-x', 'hidden').
        css('overflow-y', 'auto').
        css('-webkit-overflow-scrolling', 'touch');
      return $container;
    } else {
      $.log.debug('install JS-scrollbars for container ' + scout.graphics.debugOutput($container) +  ' and add viewport DIV');
      var $viewport = $container.appendDIV('viewport'),
        scrollbar = new scout.Scrollbar($viewport, options);
      scrollbar.initThumb();
      $viewport.data('scrollbar', scrollbar);
      return $viewport;
    }
  },

  update: function($viewport) {
    var scrollbar = $viewport.data('scrollbar');
    if (scrollbar) {
      scrollbar.initThumb();
    }
  },

  /**
   * Scrolls the viewport to the given object (must be a child of viewport)
   * @param $viewport
   * @param $selection
   */
  scrollTo: function($viewport, $selection) {
    var scrollbar = $viewport.data('scrollbar'),
      viewportH = $viewport.height(),
      optionH = scout.graphics.getSize($selection, true).height,
      optionY,
      scrollTopFunc;

    if (scrollbar) {
      scrollTopFunc = scrollbar.scrollTop.bind(scrollbar);
      optionY = $selection.offset().top - $selection.parent().parent().offset().top;
    } else {
      scrollTopFunc = $viewport.scrollTop.bind($viewport);
      optionY = $selection.position().top;
    }

    if (optionY < 0) {
      scrollTopFunc(scrollTopFunc() + optionY);
    } else if (optionY + optionH > viewportH) {
      scrollTopFunc(scrollTopFunc() + optionY + optionH - viewportH);
    }
  }

};
