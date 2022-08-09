/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, Device, graphics, HtmlComponent, Insets, objects, scout} from '../index';
import $ from 'jquery';

/**
 * Static function to install a scrollbar on a container.
 * When the client supports pretty native scrollbars, we use them by default.
 * Otherwise we install JS-based scrollbars. In that case the install function
 * creates a new scrollbar.js. For native scrollbars we
 * must set some additional CSS styles.
 */

let _$scrollables = {};
let mutationObserver;
let intersectionObserver;

export function getScrollables(session) {
  // return scrollables for given session
  if (session) {
    return _$scrollables[session] || [];
  }

  // return all scrollables, no matter to which session they belong
  let $scrollables = [];
  objects.values(_$scrollables).forEach($scrollablesPerSession => {
    arrays.pushAll($scrollables, $scrollablesPerSession);
  });
  return $scrollables;
}

export function pushScrollable(session, $container) {
  if (_$scrollables[session]) {
    if (_$scrollables[session].indexOf($container) > -1) {
      // already pushed
      return;
    }
    _$scrollables[session].push($container);
  } else {
    _$scrollables[session] = [$container];
  }
  $.log.isTraceEnabled() && $.log.trace('Scrollable added: ' + $container.attr('class') + '. New length: ' + _$scrollables[session].length);
}

export function removeScrollable(session, $container) {
  let initLength = 0;
  if (_$scrollables[session]) {
    initLength = _$scrollables[session].length;
    arrays.$remove(_$scrollables[session], $container);
    $.log.isTraceEnabled() && $.log.trace('Scrollable removed: ' + $container.attr('class') + '. New length: ' + _$scrollables[session].length);
    if (initLength === _$scrollables[session].length) {
      throw new Error('scrollable could not be removed. Potential memory leak. ' + $container.attr('class'));
    }
  } else {
    throw new Error('scrollable could not be removed. Potential memory leak. ' + $container.attr('class'));
  }
}

/**
 * @param [options]
 * @param {string} [options.axis] x, y or both. Default is both.
 * @param {boolean} [options.nativeScrollbars]
 * @param {boolean} [options.hybridScrollbars]
 * @param {string|[string]} [options.scrollShadow] controls the scroll shadow behavior.
 *        <ul>
 *          <li>To define where the shadow should appear, use one of the following values: x, y, top, right, bottom, left. Multiple values can be separated by space.
 *          <li>If no positioning value is provided, it is automatically determined based on the axis.</li>
 *          <li>To adjust the style, add one of the following values: large or gradient.</li>
 *          <li>To disable the scroll shadow completely, set the value to none.</li>
 *        </ul>
 * @param {function} [options.scrollShadowCustomizer] function to customize the scroll shadow
 * @param {Session} [options.session]
 * @param {Widget} [options.parent]
 */
export function install($container, options) {
  options = _createDefaultScrollToOptions(options);
  options.axis = options.axis || 'both';
  options.scrollShadow = options.scrollShadow || 'auto';

  // Don't use native as variable name because it will break minifying (reserved keyword)
  let nativeScrollbars = scout.nvl(options.nativeScrollbars, Device.get().hasPrettyScrollbars());
  let hybridScrollbars = scout.nvl(options.hybridScrollbars, Device.get().canHideScrollbars());
  if (nativeScrollbars) {
    _installNative($container, options);
  } else if (hybridScrollbars) {
    $container.addClass('hybrid-scrollable');
    _installNative($container, options);
    _installJs($container, options);
  } else {
    $container.css('overflow', 'hidden');
    _installJs($container, options);
  }
  let htmlContainer = HtmlComponent.optGet($container);
  if (htmlContainer) {
    htmlContainer.scrollable = true;
  }
  $container.data('scrollable', true);
  let session = options.session || options.parent.session;
  pushScrollable(session, $container);
  if (options.scrollShadow) {
    installScrollShadow($container, session, options);
  }
  return $container;
}

export function _installNative($container, options) {
  if (Device.get().isIos()) {
    // On ios, container sometimes is not scrollable when installing too early
    // Happens often with nested scrollable containers (e.g. scrollable table inside a form inside a scrollable tree data)
    setTimeout(_installNativeInternal.bind(this, $container, options));
  } else {
    _installNativeInternal($container, options);
  }
}

export function _installNativeInternal($container, options) {
  $.log.isTraceEnabled() && $.log.trace('use native scrollbars for container ' + graphics.debugOutput($container));
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

export function installScrollShadow($container, session, options) {
  if (!Device.get().supportsIntersectionObserver()) {
    return;
  }
  let scrollShadowStyle = _computeScrollShadowStyle(options);
  if (scrollShadowStyle.length === 0) {
    return;
  }
  let $shadow = $container.afterDiv('scroll-shadow');
  $shadow.toggleClass('large', scrollShadowStyle.indexOf('large') > -1);
  $shadow.toggleClass('gradient', scrollShadowStyle.indexOf('gradient') > -1);
  $shadow.data('scroll-shadow-parent', $container);
  $container.data('scroll-shadow', $shadow);
  $container.data('scroll-shadow-style', scrollShadowStyle);
  $container.data('scroll-shadow-customizer', options.scrollShadowCustomizer);
  let handler = () => updateScrollShadowWhileScrolling($container);
  $container.data('scroll-shadow-handler', handler);
  $container.on('scroll', handler);
  updateScrollShadow($container);
  _installMutationObserver(session);
  _installIntersectionObserver();
  intersectionObserver.observe($container[0]);

  // this is required in addition to the intersection observer because the observer events are handled asynchronously later after all the setTimeout calls.
  // Then the shadow might stay visible too long which has an impact on layout updates.
  let containerElement = $container[0];
  let visibleListener = e => {
    if (e.target === containerElement) {
      _onScrollableVisibleChange(containerElement, e.type === 'show');
    }
  };
  $container.data('scroll-shadow-visible-listener', visibleListener);
  $container.on('hide show', visibleListener);
}

export function uninstallScrollShadow($container, session) {
  let $shadow = $container.data('scroll-shadow');
  if ($shadow) {
    $shadow.remove();
    $container.removeData('scroll-shadow');
  }
  $container.removeData('scroll-shadow-style');
  $container.removeData('scroll-shadow-customizer');
  let handler = $container.data('scroll-shadow-handler');
  if (handler) {
    $container.off('scroll', handler);
    $container.removeData('scroll-shadow-handler');
  }
  if (intersectionObserver) {
    intersectionObserver.unobserve($container[0]);
  }
  let visibleListener = $container.data('scroll-shadow-visible-listener');
  if (visibleListener) {
    $container.off('hide show', visibleListener);
  }
  let $scrollables = _$scrollables[session];
  if (!$scrollables || !$scrollables.some($scrollable => $scrollable.data('scroll-shadow'))) {
    _uninstallMutationObserver();
    _uninstallIntersectionObserver();
  }
}

/**
 * @param options
 * @return {[string]}
 */
function _computeScrollShadowStyle(options) {
  let scrollShadow = options.scrollShadow;
  if (!scrollShadow) {
    return [];
  }
  if (typeof scrollShadow === 'string') {
    scrollShadow = scrollShadow.split(' ');
  }
  scrollShadow = scrollShadow.slice(); // copy to don't modify parameter
  if (scrollShadow.indexOf('none') > -1) {
    return [];
  }
  if (!arrays.containsAny(scrollShadow, ['y', 'x', 'top', 'right', 'bottom', 'left'])) {
    // If no position was set, determine it automatically based on the axis
    if (options.axis === 'both' || options.axis === 'y') {
      scrollShadow.push('y');
    }
    if (options.axis === 'both' || options.axis === 'x') {
      scrollShadow.push('x');
    }
  }
  if (scrollShadow.indexOf('y') > -1) {
    scrollShadow.push('top');
    scrollShadow.push('bottom');
  }
  if (scrollShadow.indexOf('x') > -1) {
    scrollShadow.push('left');
    scrollShadow.push('right');
  }
  arrays.removeAll(scrollShadow, ['all', 'y', 'x', 'auto', 'none']);
  return scrollShadow;
}

export function updateScrollShadowWhileScrolling($container) {
  let $animatingParent = $container.findUp($elem => $elem.hasAnimationClass());
  if ($animatingParent.length > 0) {
    // If the container is scrolled while being animated, the shadow will likely get the wrong size and/or position if the animation changes the bounds.
    // The scroll event is mostly probably not triggered by the user directly but by the scrollable container itself, e.g. to reveal a focused / selected / checked element.
    $animatingParent.oneAnimationEnd(() => updateScrollShadow($container));
    return;
  }
  updateScrollShadow($container);
}

export function updateScrollShadow($container) {
  let $shadow = $container.data('scroll-shadow');
  if (!$shadow) {
    return;
  }
  let scrollTop = $container[0].scrollTop;
  let scrollLeft = $container[0].scrollLeft;
  let atTop = atStart(scrollTop);
  let atBottom = atEnd(scrollTop, $container[0].scrollHeight, $container[0].offsetHeight);
  let atLeft = atStart(scrollLeft);
  let atRight = atEnd(scrollLeft, $container[0].scrollWidth, $container[0].offsetWidth);
  let style = $container.data('scroll-shadow-style');
  $shadow.toggleClass('top', !atTop && style.indexOf('top') > -1);
  $shadow.toggleClass('bottom', !atBottom && style.indexOf('bottom') > -1);
  $shadow.toggleClass('left', !atLeft && style.indexOf('left') > -1);
  $shadow.toggleClass('right', !atRight && style.indexOf('right') > -1);
  graphics.setBounds($shadow, graphics.bounds($container, {exact: true}).subtract(insets($shadow)));
  graphics.setMargins($shadow, graphics.margins($container));
  $shadow.css('border-radius', $container.css('border-radius'));

  let customizer = $container.data('scroll-shadow-customizer');
  if (customizer) {
    customizer($container, $shadow);
  }

  function atStart(scrollPos) {
    return scrollPos === 0;
  }

  function atEnd(scrollPos, scrollSize, offsetSize) {
    return scrollPos + 1 >= scrollSize - offsetSize;
  }

  function insets($shadow) {
    return new Insets($shadow.cssPxValue('--scroll-shadow-inset-top'),
      $shadow.cssPxValue('--scroll-shadow-inset-right'),
      $shadow.cssPxValue('--scroll-shadow-inset-bottom'),
      $shadow.cssPxValue('--scroll-shadow-inset-left'));
  }
}

/**
 * Installs a dom mutation observer that tracks all scrollables in order to move the scroll shadow along with the scrollable.
 */
function _installMutationObserver(session) {
  if (mutationObserver) {
    return;
  }
  mutationObserver = new MutationObserver(_onDomMutation);
  mutationObserver.observe(session.$entryPoint[0], {
    subtree: true,
    childList: true
  });
}

function _onDomMutation(mutationList, observer) {
  mutationList.forEach(_processDomMutation);
}

export function _processDomMutation(mutation) {
  // addedNodes if of type NodeList and therefore does not support array functions
  for (let i = 0; i < mutation.addedNodes.length; i++) {
    let elem = mutation.addedNodes[i];
    let $elem = $(elem);
    if ($elem.data('scrollable')) {
      // Move scroll shadow after scrollable when scrollable was moved (=inserted again)
      let $scrollShadow = $elem.data('scroll-shadow');
      if ($scrollShadow) {
        $scrollShadow.insertAfter($elem);
      }
    }
  }
}

function _uninstallMutationObserver() {
  if (!mutationObserver) {
    return;
  }
  mutationObserver.disconnect();
  mutationObserver = null;
}

/**
 * Installs an intersection observer that tracks the visibility of a scrollable in order to update the visibility of the scroll shadow accordingly.
 */
function _installIntersectionObserver() {
  if (intersectionObserver) {
    return;
  }
  intersectionObserver = new IntersectionObserver((entries, observer) => {
    entries.forEach(entry => {
      _onScrollableVisibleChange(entry.target, entry.intersectionRatio > 0);
    });
  });
}

function _uninstallIntersectionObserver() {
  if (!intersectionObserver) {
    return;
  }
  intersectionObserver.disconnect();
  intersectionObserver = null;
}

export function _onScrollableVisibleChange(element, visible) {
  let $element = $(element);
  let $shadow = $element.data('scroll-shadow');
  if (!$shadow) {
    return;
  }
  $shadow.setVisible($element.isVisible());
}

export function hasScrollShadow($container, position) {
  if (!$container) {
    return false;
  }
  let $scrollShadow = $container.data('scroll-shadow');
  if (!$scrollShadow) {
    return false;
  }
  if (!position) {
    return true;
  }
  return $scrollShadow.hasClass(position);
}

export function isHybridScrolling($scrollable) {
  return $scrollable.hasClass('hybrid-scrollable');
}

export function isNativeScrolling($scrollable) {
  return scout.isOneOf('auto', $scrollable.css('overflow'), $scrollable.css('overflow-x'), $scrollable.css('overflow-y'));
}

export function isJsScrolling($scrollable) {
  return !!$scrollable.data('scrollbars');
}

export function _installJs($container, options) {
  $.log.isTraceEnabled() && $.log.trace('installing JS-scrollbars for container ' + graphics.debugOutput($container));
  let scrollbars = arrays.ensure($container.data('scrollbars'));
  scrollbars.forEach(scrollbar => {
    scrollbar.destroy();
  });
  scrollbars = [];
  let scrollbar;
  if (options.axis === 'both') {
    let scrollOptions = $.extend({}, options);
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

  scrollbars.forEach(scrollbar => {
    scrollbar.render($container);
    scrollbar.update();
  });
}

/**
 * Removes the js scrollbars for the $container, if there are any.<p>
 */
export function uninstall($container, session) {
  if (!$container.data('scrollable')) {
    // was not installed previously -> uninstalling not necessary
    return;
  }

  let scrollbars = $container.data('scrollbars');
  if (scrollbars) {
    scrollbars.forEach(scrollbar => {
      scrollbar.destroy();
    });
  }
  removeScrollable(session, $container);
  $container.removeData('scrollable');
  $container.css('overflow', '');
  $container.removeClass('hybrid-scrollable');
  $container.removeData('scrollbars');

  let htmlContainer = HtmlComponent.optGet($container);
  if (htmlContainer) {
    htmlContainer.scrollable = false;
  }
  uninstallScrollShadow($container, session);
}

/**
 * Recalculates the scrollbar size and position.
 * @param $scrollable JQuery element that has .data('scrollbars'), when $scrollable is falsy the function returns immediately
 * @param immediate set to true to immediately update the scrollbar, If set to false,
 *        it will be queued in order to prevent unnecessary updates.
 */
export function update($scrollable, immediate) {
  if (!$scrollable || !$scrollable.data('scrollable')) {
    return;
  }
  updateScrollShadow($scrollable);
  let scrollbars = $scrollable.data('scrollbars');
  if (!scrollbars) {
    if (Device.get().isIos()) {
      _handleIosPaintBug($scrollable);
    }
    return;
  }
  if (immediate) {
    _update(scrollbars);
    return;
  }
  if ($scrollable.data('scrollbarUpdatePending')) {
    return;
  }
  // Executes the update later to prevent unnecessary updates
  setTimeout(() => {
    _update(scrollbars);
    $scrollable.removeData('scrollbarUpdatePending');
  }, 0);
  $scrollable.data('scrollbarUpdatePending', true);
}

export function _update(scrollbars) {
  // Reset the scrollbars first to make sure they don't extend the scrollSize
  scrollbars.forEach(scrollbar => {
    if (scrollbar.rendered) {
      scrollbar.reset();
    }
  });
  scrollbars.forEach(scrollbar => {
    if (scrollbar.rendered) {
      scrollbar.update();
    }
  });
}

/**
 * IOS has problems with nested scrollable containers. Sometimes the outer container goes completely white hiding the elements behind.
 * This happens with the following case: Main box is scrollable but there are no scrollbars because content is smaller than container.
 * In the main box there is a tab box with a scrollable table. This table has scrollbars.
 * If the width of the tab box is adjusted (which may happen if the tab item is selected and eventually prefSize called), the main box will go white.
 * <p>
 * This happens only if -webkit-overflow-scrolling is set to touch.
 * To workaround this bug the flag -webkit-overflow-scrolling will be removed if the scrollable component won't display any scrollbars
 */

export function _handleIosPaintBug($scrollable) {
  if ($scrollable.data('scrollbarUpdatePending')) {
    return;
  }
  setTimeout(() => {
    workaround();
    $scrollable.removeData('scrollbarUpdatePending');
  });
  $scrollable.data('scrollbarUpdatePending', true);

  function workaround() {
    let size = graphics.size($scrollable).subtract(graphics.insets($scrollable, {
      includePadding: false,
      includeBorder: true
    }));
    if ($scrollable[0].scrollHeight === size.height && $scrollable[0].scrollWidth === size.width) {
      $scrollable.css('-webkit-overflow-scrolling', '');
    } else {
      $scrollable.css('-webkit-overflow-scrolling', 'touch');
    }
  }
}

export function reset($scrollable) {
  let scrollbars = $scrollable.data('scrollbars');
  if (!scrollbars) {
    return;
  }
  scrollbars.forEach(scrollbar => {
    scrollbar.reset();
  });
}

/**
 * Scrolls the $scrollable to the given $element (must be a child of $scrollable)
 *
 * @param {$} $scrollable
 *          the scrollable object
 * @param {$} $element
 *          the element to scroll to
 * @param {object|string} [options]
 *          an optional options object. Short-hand version: If a string is passed instead
 *          of an object, the value is automatically converted to the option "align".
 * @param {string} [options.align]
 *          Specifies where the element should be positioned in the view port. Can either be 'top', 'center' or 'bottom'.
 *          If unspecified, the following rules apply:
 *          - If the element is above the visible area it will be aligned to top.
 *          - If the element is below the visible area it will be aligned to bottom.
 *          - If the element is already in the visible area no scrolling is done.
 *          Default is undefined.
 * @param {boolean} [options.animate]
 *          If true, the scroll position will be animated so that the element moves smoothly to its new position. Default is false.
 * @param {boolean} [options.stop]
 *          If true, all running animations are stopped before executing the current scroll request. Default is true.
 * @param {number} [options.scrollOffsetUp]
 *          Additional margin to assume at the top of the target element (independent from any actual CSS margin).
 *          Useful when elements are positioned outside of their boundaries (e.g. focus border). Default is 4.
 * @param {number} [options.scrollOffsetDown]
 *          Additional margin to assume at the bottom of the target element (independent from any actual CSS margin).
 *          Useful when elements are positioned outside of their boundaries (e.g. focus border). Default is 8.
 */
export function scrollTo($scrollable, $element, options) {
  if (typeof options === 'string') {
    options = {
      align: options
    };
  } else {
    options = _createDefaultScrollToOptions(options);
  }

  let align = (options.align ? options.align.toLowerCase() : undefined);

  let scrollTo,
    scrollOffsetUp = scout.nvl(options.scrollOffsetUp, align === 'center' ? 0 : 4),
    scrollOffsetDown = scout.nvl(options.scrollOffsetDown, align === 'center' ? 0 : 8),
    scrollableH = $scrollable.height(),
    elementBounds = graphics.offsetBounds($element),
    scrollableBounds = graphics.offsetBounds($scrollable),
    elementY = elementBounds.y - scrollableBounds.y,
    elementH = elementBounds.height,
    elementTop = elementY - scrollOffsetUp, // relative to scrollable y
    elementBottom = elementY + elementH + scrollOffsetDown;

  //        ---          ^                     <-- elementTop
  //         |           | scrollOffsetUp
  //         |           v
  //   +------------+    ^                     <-- elementY
  //   |  element   |    | elementH
  //   +------------+    v
  //         |           ^
  //         |           | scrollOffsetDown
  //        ---          v                     <-- elementBottom

  if (!align) {
    // If the element is above the visible area it will be aligned to top.
    // If the element is below the visible area it will be aligned to bottom.
    // If the element is already in the visible area no scrolling is done.
    align = (elementTop < 0) ? 'top' : (elementBottom > scrollableH ? 'bottom' : undefined);
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
    let elementTopNew = elementTop - (scrollTo - $scrollable.scrollTop());
    if (elementTopNew < 0) {
      scrollTo = scrollTo + elementTopNew;
    }
  }
  if (scrollTo !== undefined) {
    scrollTop($scrollable, scrollTo, options);
  }
}

export function _createDefaultScrollToOptions(options) {
  let defaults = {
    animate: false,
    stop: true
  };
  return $.extend({}, defaults, options);
}

/**
 * Horizontally scrolls the $scrollable to the given $element (must be a child of $scrollable)
 */
export function scrollHorizontalTo($scrollable, $element, options) {
  let scrollTo,
    scrollableW = $scrollable.width(),
    elementBounds = graphics.bounds($element, true),
    elementLeft = elementBounds.x,
    elementW = elementBounds.width;

  if (elementLeft < 0) {
    scrollLeft($scrollable, $scrollable.scrollLeft() + elementLeft, options);
  } else if (elementLeft + elementW > scrollableW) {
    // On IE, a fractional position gets truncated when using scrollTop -> ceil to make sure the full element is visible
    scrollTo = Math.ceil($scrollable.scrollLeft() + elementLeft + elementW - scrollableW);
    scrollLeft($scrollable, scrollTo, options);
  }
}

/**
 * @param {$} $scrollable the scrollable object
 * @param {number} scrollTop the new scroll position
 * @param {object} [options]
 * @param {boolean} [options.animate] whether the scrolling should be animated. Default is false.
 * @param {boolean} [options.stop] whether the animation should be stopped. Default is false.
 */
export function scrollTop($scrollable, scrollTop, options) {
  options = _createDefaultScrollToOptions(options);
  let scrollbarElement = scrollbar($scrollable, 'y');
  if (scrollbarElement) {
    scrollbarElement.notifyBeforeScroll();
  }

  if (options.stop) {
    $scrollable.stop('scroll');
  }

  // Not animated
  if (!options.animate) {
    $scrollable.scrollTop(scrollTop);
    if (scrollbarElement) {
      scrollbarElement.notifyAfterScroll();
    }
    return;
  }

  // Animated
  animateScrollTop($scrollable, scrollTop);
  $scrollable.promise('scroll').always(() => {
    if (scrollbarElement) {
      scrollbarElement.notifyAfterScroll();
    }
  });
}

/**
 * @param {$} $scrollable the scrollable object
 * @param {number} scrollLeft the new scroll position
 * @param {object} [options]
 * @param {boolean} [options.animate] whether the scrolling should be animated. Default is false.
 * @param {boolean} [options.stop] whether the animation should be stopped. Default is false.
 */
export function scrollLeft($scrollable, scrollLeft, options) {
  options = _createDefaultScrollToOptions(options);
  let scrollbarElement = scrollbar($scrollable, 'x');
  if (scrollbarElement) {
    scrollbarElement.notifyBeforeScroll();
  }

  if (options.stop) {
    $scrollable.stop('scroll');
  }

  // Not animated
  if (!options.animate) {
    $scrollable.scrollLeft(scrollLeft);
    if (scrollbarElement) {
      scrollbarElement.notifyAfterScroll();
    }
    return;
  }

  // Animated
  animateScrollLeft($scrollable, scrollLeft);
  $scrollable.promise('scroll').always(() => {
    if (scrollbarElement) {
      scrollbarElement.notifyAfterScroll();
    }
  });
}

function animateScrollTop($scrollable, scrollTop) {
  $scrollable.animate({
    scrollTop: scrollTop
  }, {
    queue: 'scroll'
  })
    .dequeue('scroll');
}

function animateScrollLeft($scrollable, scrollLeft) {
  $scrollable.animate({
    scrollLeft: scrollLeft
  }, {
    queue: 'scroll'
  })
    .dequeue('scroll');
}

export function scrollbar($scrollable, axis) {
  let scrollbars = $scrollable.data('scrollbars') || [];
  return arrays.find(scrollbars, scrollbar => {
    return scrollbar.axis === axis;
  });
}

export function scrollToBottom($scrollable, options) {
  scrollTop($scrollable, $scrollable[0].scrollHeight - $scrollable[0].offsetHeight, options);
}

/**
 * @param location object with x and y properties
 * @param $scrollables one or more scrollables to check against
 * @returns {boolean} true if the location is visible in the current viewport of all the $scrollables, or if $scrollables is null
 */
export function isLocationInView(location, $scrollables) {
  if (!$scrollables || $scrollables.length === 0) {
    return true;
  }
  return $scrollables.toArray().every(scrollable => {
    let scrollableOffsetBounds = graphics.offsetBounds($(scrollable));
    return scrollableOffsetBounds.contains(location.x, location.y);
  });
}

/**
 * Attaches the given handler to each scrollable parent, including $anchor if it is scrollable as well.<p>
 * Make sure you remove the handlers when not needed anymore using offScroll.
 */
export function onScroll($anchor, handler) {
  handler.$scrollParents = [];
  $anchor.scrollParents().each(function() {
    let $scrollParent = $(this);
    $scrollParent.on('scroll', handler);
    handler.$scrollParents.push($scrollParent);
  });
}

export function offScroll(handler) {
  let $scrollParents = handler.$scrollParents;
  if (!$scrollParents) {
    throw new Error('$scrollParents are not defined');
  }
  for (let i = 0; i < $scrollParents.length; i++) {
    let $elem = $scrollParents[i];
    $elem.off('scroll', handler);
  }
}

/**
 * Sets the position to fixed and updates left and top position.
 * This is necessary to prevent flickering in IE.
 */
export function fix($elem) {
  if (!$elem.isVisible() || $elem.css('position') === 'fixed') {
    return;
  }

  // getBoundingClientRect used by purpose instead of graphics.offsetBounds to get exact values
  // Also important: offset() of jquery returns getBoundingClientRect().top + window.pageYOffset.
  // In case of IE and zoom = 125%, the pageYOffset is 1 because the height of the navigation is bigger than the height of the desktop which may be fractional.
  let bounds = $elem[0].getBoundingClientRect();
  $elem
    .css('position', 'fixed')
    .cssLeft(bounds.left - $elem.cssMarginLeft())
    .cssTop(bounds.top - $elem.cssMarginTop())
    .cssWidth(bounds.width)
    .cssHeight(bounds.height);
}

/**
 * Reverts the changes made by fix().
 */
export function unfix($elem, timeoutId, immediate) {
  clearTimeout(timeoutId);
  if (immediate) {
    _unfix($elem);
    return;
  }
  return setTimeout(() => {
    _unfix($elem);
  }, 50);
}

export function _unfix($elem) {
  $elem.css({
    position: 'absolute',
    left: '',
    top: '',
    width: '',
    height: ''
  });
}

/**
 * Stores the position of all scrollables that belong to an optional session.
 * @param session (optional) when no session is given, scrollables from all sessions are stored
 */
export function storeScrollPositions($container, session) {
  let $scrollables = getScrollables(session);
  if (!$scrollables) {
    return;
  }

  let scrollTop, scrollLeft;
  $scrollables.forEach($scrollable => {
    if ($container.isOrHas($scrollable[0])) {
      scrollTop = $scrollable.scrollTop();
      $scrollable.data('scrollTop', scrollTop);
      scrollLeft = $scrollable.scrollLeft();
      $scrollable.data('scrollLeft', $scrollable.scrollLeft());
      $.log.isTraceEnabled() && $.log.trace('Stored scroll position for ' + $scrollable.attr('class') + '. Top: ' + scrollTop + '. Left: ' + scrollLeft);
    }
  });
}

/**
 * Restores the position of all scrollables that belong to an optional session.
 * @param session (optional) when no session is given, scrollables from all sessions are restored
 */
export function restoreScrollPositions($container, session) {
  let $scrollables = getScrollables(session);
  if (!$scrollables) {
    return;
  }

  let scrollTop, scrollLeft;
  $scrollables.forEach($scrollable => {
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
      update($scrollable);
      $.log.isTraceEnabled() && $.log.trace('Restored scroll position for ' + $scrollable.attr('class') + '. Top: ' + scrollTop + '. Left: ' + scrollLeft);
    }
  });
}

export function setVisible($scrollable, visible) {
  if (!$scrollable || !$scrollable.data('scrollable')) {
    return;
  }
  let scrollbars = $scrollable.data('scrollbars');
  if (!scrollbars) {
    return;
  }
  scrollbars.forEach(scrollbar => {
    if (scrollbar.rendered) {
      scrollbar.$container.setVisible(visible);
    }
  });
}

export function opacity($scrollable, opacity) {
  if (!$scrollable || !$scrollable.data('scrollable')) {
    return;
  }
  let scrollbars = $scrollable.data('scrollbars');
  if (!scrollbars) {
    return;
  }
  scrollbars.forEach(scrollbar => {
    if (scrollbar.rendered) {
      scrollbar.$container.css('opacity', opacity);
    }
  });
}

export function _getCompleteChildRowsHeightRecursive(children, getChildren, isExpanded, defaultChildHeight) {
  let height = 0;
  children.forEach(child => {
    if (child.height) {
      height += child.height;
    } else {
      // fallback for children with unset height
      height += defaultChildHeight;
    }
    if (isExpanded(child) && getChildren(child).length > 0) {
      height += _getCompleteChildRowsHeightRecursive(getChildren(child), getChildren, isExpanded, defaultChildHeight);
    }
  });
  return height;
}

export function ensureExpansionVisible(parent) {
  let isParentExpanded = parent.isExpanded(parent.element);
  let children = parent.getChildren(parent.element);
  let parentPositionTop = parent.$element.position().top;
  let parentHeight = parent.element.height;
  let scrollTopPos = parent.$scrollable.scrollTop();

  // vertical scrolling
  if (!isParentExpanded) {
    // parent is not expanded, make sure that at least one node above the parent is visible
    if (parentPositionTop < parentHeight) {
      let minScrollTop = Math.max(scrollTopPos - (parentHeight - parentPositionTop), 0);
      scrollTop(parent.$scrollable, minScrollTop, {
        animate: true
      });
    }
  } else if (isParentExpanded && children.length > 0) {
    // parent is expanded and has children, best effort approach to show the expansion
    let fullDataHeight = parent.$scrollable.height();

    // get childRowCount considering already expanded rows
    let childRowsHeight = _getCompleteChildRowsHeightRecursive(children, parent.getChildren, parent.isExpanded, parent.defaultChildHeight);

    // + 1.5 since its the parent's top position and we want to scroll half a row further to show that there's something after the expansion
    let additionalHeight = childRowsHeight + (1.5 * parentHeight);
    let scrollTo = parentPositionTop + additionalHeight;
    // scroll as much as needed to show the expansion but make sure that the parent row (plus one more) is still visible
    let newScrollTop = scrollTopPos + Math.min(scrollTo - fullDataHeight, parentPositionTop - parentHeight);
    // only scroll down
    if (newScrollTop > scrollTopPos) {
      scrollTop(parent.$scrollable, newScrollTop, {
        animate: true,
        stop: false
      });
    }
  }

  if (children.length > 0) {
    // horizontal scrolling: at least 3 levels of hierarchy should be visible (only relevant for small fields)
    let minLevelLeft = Math.max(parent.element.level - 3, 0) * parent.nodePaddingLevel;
    scrollLeft(parent.$scrollable, minLevelLeft, {
      animate: true,
      stop: false
    });
  }
}

export default {
  ensureExpansionVisible,
  fix,
  getScrollables,
  install,
  installScrollShadow,
  isHybridScrolling,
  isJsScrolling,
  isLocationInView,
  isNativeScrolling,
  hasScrollShadow,
  offScroll,
  onScroll,
  opacity,
  pushScrollable,
  removeScrollable,
  reset,
  restoreScrollPositions,
  scrollHorizontalTo,
  scrollLeft,
  scrollTo,
  scrollToBottom,
  scrollTop,
  scrollbar,
  setVisible,
  storeScrollPositions,
  unfix,
  uninstall,
  uninstallScrollShadow,
  update
};
