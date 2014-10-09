scout.Scrollbar2 = {};

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
// TODO AWE: (scrollbar) merge with Scrollbar.js or rename to ScrollbarWrapper(?)
scout.Scrollbar2.install = function($container, options) {
  var prettyScrollbars = true;
  if (prettyScrollbars) {
    $.log.debug('use native scrollbars for container ' + scout.HtmlComponent.debug($container));
    $container.
      css('overflow', 'hidden').
      css('overflow-y', 'auto').
      css('-webkit-overflow-scrolling', 'touch');
      // TODO AWE: (scrollbar) add other styles based on config-object and used browser, for instance:
      // -webkit-overflow-scrolling: touch;
    return $container;
  } else {
    $.log.debug('install JS-scrollbars for container ' + scout.HtmlComponent.debug($container) +  ' and add viewport DIV');
    var $viewport = $container.appendDIV('viewport'),
      scrollbar = new scout.Scrollbar($viewport, options);
    scrollbar.initThumb();
    $viewport.data('scrollbar', scrollbar);
    return $viewport;
  }
};

scout.Scrollbar2.update = function($viewport) {
  var scrollbar = $viewport.data('scrollbar');
  if (scrollbar) {
    scrollbar.initThumb();
  }
};
