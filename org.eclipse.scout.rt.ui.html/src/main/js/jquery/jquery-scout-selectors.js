/*!
 * jQuery UI Core 1.11.2
 * http://jqueryui.com
 *
 * Copyright 2014 jQuery Foundation and other contributors
 * Released under the MIT license.
 * http://jquery.org/license
 *
 * http://api.jqueryui.com/category/ui-core/
 */

/**
 * This file extends jQuery with custom selectors required in Scout.
 * Part of this file is copied with some modifications from jQuery UI.
 */
function focusable(element, isTabIndexNotNaN) {
  var map, mapName, img,
    nodeName = element.nodeName.toLowerCase();

  return (/input|select|textarea|button|object/.test(nodeName) ?
      !element.disabled :
      'a' === nodeName ?
      element.href || isTabIndexNotNaN :
      isTabIndexNotNaN) &&
    // the element and all of its ancestors must be visible
    visible(element);
}

function visible(element) {
  return $.expr.filters.visible(element) &&
    !$(element).parents().addBack().filter(function() {
      return $.css(this, 'visibility') === 'hidden';
    }).length;
}

$.extend($.expr[':'], {
  focusable: function(element) {
    return focusable(element, !isNaN($.attr(element, 'tabindex')));
  },

  tabbable: function(element) {
    var tabIndex = $.attr(element, 'tabindex'),
      isTabIndexNaN = isNaN(tabIndex);
    return (isTabIndexNaN || tabIndex >= 0) && focusable(element, !isTabIndexNaN);
  }
});



