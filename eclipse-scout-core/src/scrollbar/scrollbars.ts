/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Device, graphics, HtmlComponent, InitModelOf, Insets, objects, scout, Scrollbar, Session, SomeRequired, WidgetModel} from '../index';
import $ from 'jquery';

export type ScrollDirection = 'x' | 'y' | 'both';

export interface ScrollbarInstallOptions extends WidgetModel {
  /**
   * Default is both
   */
  axis?: ScrollDirection;
  borderless?: boolean;
  mouseWheelNeedsShift?: boolean;
  nativeScrollbars?: boolean;
  hybridScrollbars?: boolean;

  /**
   * Controls the scroll shadow behavior:
   * <ul>
   *   <li>To define where the shadow should appear, use one of the following values: x, y, top, right, bottom, left. Multiple values can be separated by space.
   *   <li>If no positioning value is provided, it is automatically determined based on the axis.</li>
   *   <li>To adjust the style, add one of the following values: large or gradient.</li>
   *   <li>To disable the scroll shadow completely, set the value to none.</li>
   * </ul>
   */
  scrollShadow?: string | string[];

  /**
   * function to customize the scroll shadow
   */
  scrollShadowCustomizer?($container: JQuery, $shadow: JQuery): void;
}

export interface ScrollOptions {
  /**
   * If true, the scroll position will be animated so that the element moves smoothly to its new position. Default is false.
   */
  animate?: boolean;
  /**
   * whether the animation should be stopped. Default is false.
   */
  stop?: boolean;
}

export interface ScrollToOptions extends ScrollOptions {
  /**
   * Specifies where the element should be positioned in the view port. Can either be 'top', 'center' or 'bottom'.
   * If unspecified, the following rules apply:
   *   - If the element is above the visible area it will be aligned to top.
   *   - If the element is below the visible area it will be aligned to bottom.
   *   - If the element is already in the visible area no scrolling is done.
   * Default is undefined.
   */
  align?: string;

  /**
   * If true, all running animations are stopped before executing the current scroll request. Default is true.
   */
  stop?: boolean;

  /**
   * Additional margin to assume at the top of the target element (independent of any actual CSS margin).
   * Useful when elements are positioned outside their boundaries (e.g. focus border). Default is 4.
   */
  scrollOffsetUp?: number;

  /**
   * Additional margin to assume at the bottom of the target element (independent of any actual CSS margin).
   * Useful when elements are positioned outside their boundaries (e.g. focus border). Default is 8.
   */
  scrollOffsetDown?: number;
}

export interface ExpandableElement {
  height: number;
  level?: number;
}

export interface ExpansionParent<T extends ExpandableElement> {
  element: T;
  $element: JQuery;
  $scrollable: JQuery;
  defaultChildHeight: number;
  nodePaddingLevel?: number;

  isExpanded(element: T): boolean;

  getChildren(element: T): T[];
}

/**
 * Static function to install a scrollbar on a container.
 * When the client supports pretty native scrollbars, we use them by default.
 * Otherwise, we install JS-based scrollbars. In that case the install-function
 * creates a new scrollbar.js. For native scrollbars we
 * must set some additional CSS styles.
 */
export const scrollbars = {
  /** @internal */
  _$scrollables: {} as Record<string, JQuery[]>,
  intersectionObserver: null as IntersectionObserver,

  getScrollables(session?: Session): JQuery[] {
    // return scrollables for given session
    if (session) {
      return scrollbars._$scrollables[session + ''] || [];
    }

    // return all scrollables, no matter to which session they belong
    let $scrollables: JQuery[] = [];
    objects.values(scrollbars._$scrollables).forEach(($scrollablesPerSession: JQuery[]) => {
      arrays.pushAll($scrollables, $scrollablesPerSession);
    });
    return $scrollables;
  },

  pushScrollable(session: Session, $container: JQuery) {
    let key = session + '';
    if (scrollbars._$scrollables[key]) {
      if (scrollbars._$scrollables[key].indexOf($container) > -1) {
        // already pushed
        return;
      }
      scrollbars._$scrollables[key].push($container);
    } else {
      scrollbars._$scrollables[key] = [$container];
    }
    $.log.isTraceEnabled() && $.log.trace('Scrollable added: ' + $container.attr('class') + '. New length: ' + scrollbars._$scrollables[key].length);
  },

  removeScrollable(session: Session, $container: JQuery) {
    let initLength = 0;
    let key = session + '';
    if (scrollbars._$scrollables[key]) {
      initLength = scrollbars._$scrollables[key].length;
      arrays.$remove(scrollbars._$scrollables[key], $container);
      $.log.isTraceEnabled() && $.log.trace('Scrollable removed: ' + $container.attr('class') + '. New length: ' + scrollbars._$scrollables[key].length);
      if (initLength === scrollbars._$scrollables[key].length) {
        throw new Error('scrollable could not be removed. Potential memory leak. ' + $container.attr('class'));
      }
    } else {
      throw new Error('scrollable could not be removed. Potential memory leak. ' + $container.attr('class'));
    }
  },

  install($container: JQuery, options?: SomeRequired<ScrollbarInstallOptions, 'parent'>): JQuery {
    options = options || {} as SomeRequired<ScrollbarInstallOptions, 'parent'>;
    options.axis = options.axis || 'both';
    options.scrollShadow = options.scrollShadow || 'auto';

    // Don't use native as variable name because it will break minifying (reserved keyword)
    let nativeScrollbars = scout.nvl(options.nativeScrollbars, Device.get().hasPrettyScrollbars());
    let hybridScrollbars = scout.nvl(options.hybridScrollbars, Device.get().canHideScrollbars());
    if (nativeScrollbars) {
      scrollbars._installNative($container, options);
    } else if (hybridScrollbars) {
      $container.addClass('hybrid-scrollable');
      scrollbars._installNative($container, options);
      scrollbars._installJs($container, options);
    } else {
      $container.css('overflow', 'hidden');
      scrollbars._installJs($container, options);
    }
    let htmlContainer = HtmlComponent.optGet($container);
    if (htmlContainer) {
      htmlContainer.scrollable = true;
    }
    $container.data('scrollable', true);
    let session = options.session || options.parent.session;
    scrollbars.pushScrollable(session, $container);
    if (options.scrollShadow) {
      scrollbars.installScrollShadow($container, options);
    }
    return $container;
  },

  /** @internal */
  _installNative($container: JQuery, options: ScrollbarInstallOptions) {
    if (Device.get().isIos()) {
      // On ios, container sometimes is not scrollable when installing too early
      // Happens often with nested scrollable containers (e.g. scrollable table inside a form inside a scrollable tree data)
      setTimeout(scrollbars._installNativeInternal.bind(this, $container, options));
    } else {
      scrollbars._installNativeInternal($container, options);
    }
  },

  /** @internal */
  _installNativeInternal($container: JQuery, options: ScrollbarInstallOptions) {
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
  },

  installScrollShadow($container: JQuery, options: ScrollbarInstallOptions) {
    if (!Device.get().supportsIntersectionObserver()) {
      return;
    }
    let scrollShadowStyle = scrollbars._computeScrollShadowStyle(options);
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
    let handler = () => scrollbars.updateScrollShadowWhileScrolling($container);
    $container.data('scroll-shadow-handler', handler);
    $container.on('scroll', handler);
    scrollbars.updateScrollShadow($container);
    scrollbars._installMutationObserver($container.entryPoint());
    scrollbars._installIntersectionObserver();
    scrollbars.intersectionObserver.observe($container[0]);

    // this is required in addition to the intersection observer because the observer events are handled asynchronously later after all the setTimeout calls.
    // Then the shadow might stay visible too long which has an impact on layout updates.
    let containerElement = $container[0];
    let visibleListener = e => {
      if (e.target === containerElement) {
        scrollbars._onScrollableVisibleChange(containerElement, e.type === 'show');
      }
    };
    $container.data('scroll-shadow-visible-listener', visibleListener);
    $container.on('hide show', visibleListener);
  },

  uninstallScrollShadow($container: JQuery, session: Session) {
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
    if (scrollbars.intersectionObserver) {
      scrollbars.intersectionObserver.unobserve($container[0]);
    }
    let visibleListener = $container.data('scroll-shadow-visible-listener');
    if (visibleListener) {
      $container.off('hide show', visibleListener);
    }
    if (!scrollbars._hasScrollShadow(session, $container.entryPoint(true))) {
      scrollbars._uninstallMutationObserver($container.entryPoint());
    }
    if (!scrollbars._hasScrollShadow(session)) {
      scrollbars._uninstallIntersectionObserver();
    }
  },

  _hasScrollShadow(session: Session, entryPoint?: HTMLElement) {
    const $scrollables = scrollbars._$scrollables[session + ''];
    return $scrollables && $scrollables.some($scrollable => $scrollable.data('scroll-shadow') && (!entryPoint || $scrollable.entryPoint(true) === entryPoint));
  },

  /** @internal */
  _computeScrollShadowStyle(options: ScrollbarInstallOptions): string[] {
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
  },

  updateScrollShadowWhileScrolling($container: JQuery
  ) {
    let $animatingParent = $container.findUp($elem => $elem.hasAnimationClass());
    if ($animatingParent.length > 0) {
      // If the container is scrolled while being animated, the shadow will likely get the wrong size and/or position if the animation changes the bounds.
      // The scroll event is mostly probably not triggered by the user directly but by the scrollable container itself, e.g. to reveal a focused / selected / checked element.
      $animatingParent.oneAnimationEnd(() => scrollbars.updateScrollShadow($container));
      return;
    }
    scrollbars.updateScrollShadow($container);
  },

  updateScrollShadow($container: JQuery) {
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

    function atStart(scrollPos: number): boolean {
      return scrollPos === 0;
    }

    function atEnd(scrollPos: number, scrollSize: number, offsetSize: number): boolean {
      return scrollPos + 1 >= scrollSize - offsetSize;
    }

    function insets($shadow: JQuery): Insets {
      return new Insets($shadow.cssPxValue('--scroll-shadow-inset-top'),
        $shadow.cssPxValue('--scroll-shadow-inset-right'),
        $shadow.cssPxValue('--scroll-shadow-inset-bottom'),
        $shadow.cssPxValue('--scroll-shadow-inset-left'));
    }
  },

  /**
   * Installs a dom mutation observer that tracks all scrollables in order to move the scroll shadow along with the scrollable.
   * @internal
   */
  _installMutationObserver($entryPoint: JQuery) {
    if (!$entryPoint || !$entryPoint[0] || $entryPoint.data('mutation-observer')) {
      return;
    }
    const mutationObserver = new MutationObserver(scrollbars._onDomMutation);
    $entryPoint.data('mutation-observer', mutationObserver);
    mutationObserver.observe($entryPoint[0], {
      subtree: true,
      childList: true
    });
  },

  /** @internal */
  _onDomMutation(mutationList: MutationRecord[], observer: MutationObserver) {
    mutationList.forEach(scrollbars._processDomMutation);
  },

  /** @internal */
  _processDomMutation(mutation: MutationRecord) {
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
  },

  /** @internal */
  _uninstallMutationObserver($entryPoint: JQuery) {
    if (!$entryPoint || !$entryPoint.data('mutation-observer')) {
      return;
    }
    $entryPoint.data('mutation-observer').disconnect();
    $entryPoint.removeData('mutation-observer');
  },

  /**
   * Installs an intersection observer that tracks the visibility of a scrollable in order to update the visibility of the scroll shadow accordingly.
   * @internal
   */
  _installIntersectionObserver() {
    if (scrollbars.intersectionObserver) {
      return;
    }
    scrollbars.intersectionObserver = new IntersectionObserver((entries: IntersectionObserverEntry[], observer: IntersectionObserver) => {
      entries.forEach(entry => scrollbars._onScrollableVisibleChange(entry.target, entry.intersectionRatio > 0));
    });
  },

  /** @internal */
  _uninstallIntersectionObserver() {
    if (!scrollbars.intersectionObserver) {
      return;
    }
    scrollbars.intersectionObserver.disconnect();
    scrollbars.intersectionObserver = null;
  },

  /** @internal */
  _onScrollableVisibleChange(element: Element, visible: boolean) {
    let $element = $(element);
    let $shadow = $element.data('scroll-shadow');
    if (!$shadow) {
      return;
    }
    $shadow.setVisible($element.isVisible());
  },

  hasScrollShadow($container: JQuery, position: string): boolean {
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
  },

  isHybridScrolling($scrollable: JQuery): boolean {
    return $scrollable.hasClass('hybrid-scrollable');
  },

  isNativeScrolling($scrollable: JQuery): boolean {
    return scout.isOneOf('auto', $scrollable.css('overflow'), $scrollable.css('overflow-x'), $scrollable.css('overflow-y'));
  },

  isJsScrolling($scrollable: JQuery): boolean {
    return !!$scrollable.data('scrollbars');
  },

  /** @internal */
  _installJs($container: JQuery, options: SomeRequired<ScrollbarInstallOptions, 'parent'>) {
    $.log.isTraceEnabled() && $.log.trace('installing JS-scrollbars for container ' + graphics.debugOutput($container));
    let scrollbarArr = arrays.ensure($container.data('scrollbars'));
    scrollbarArr.forEach(scrollbar => {
      scrollbar.destroy();
    });
    scrollbarArr = [];
    let scrollbar;
    if (options.axis === 'both') {
      let scrollbarModel: InitModelOf<Scrollbar> = $.extend({}, options as InitModelOf<Scrollbar>, {axis: 'y'});
      scrollbar = scout.create(Scrollbar, scrollbarModel);
      scrollbarArr.push(scrollbar);

      scrollbarModel = $.extend({}, scrollbarModel, {
        axis: 'x',
        mouseWheelNeedsShift: true
      });
      scrollbar = scout.create(Scrollbar, scrollbarModel);
      scrollbarArr.push(scrollbar);
    } else {
      let scrollbarModel: InitModelOf<Scrollbar> = $.extend({}, options, {axis: options.axis});
      scrollbar = scout.create(Scrollbar, scrollbarModel);
      scrollbarArr.push(scrollbar);
    }
    $container.data('scrollbars', scrollbarArr);

    scrollbarArr.forEach(scrollbar => {
      scrollbar.render($container);
      scrollbar.update();
    });
  },

  /**
   * Removes the js scrollbars for the $container, if there are any.<p>
   */
  uninstall($container: JQuery, session: Session) {
    if (!$container.data('scrollable')) {
      // was not installed previously -> uninstalling not necessary
      return;
    }

    let scrollbarArr = $container.data('scrollbars');
    if (scrollbarArr) {
      scrollbarArr.forEach(scrollbar => {
        scrollbar.destroy();
      });
    }
    scrollbars.removeScrollable(session, $container);
    $container.removeData('scrollable');
    $container.css('overflow', '');
    $container.removeClass('hybrid-scrollable');
    $container.removeData('scrollbars');

    let htmlContainer = HtmlComponent.optGet($container);
    if (htmlContainer) {
      htmlContainer.scrollable = false;
    }
    scrollbars.uninstallScrollShadow($container, session);
  },

  /**
   * Recalculates the scrollbar size and position.
   * @param $scrollable JQuery element that has .data('scrollbars'), when $scrollable is falsy the function returns immediately
   * @param immediate set to true to immediately update the scrollbar. If set to false, it will be queued in order to prevent unnecessary updates.
   */
  update($scrollable: JQuery, immediate?: boolean) {
    if (!$scrollable || !$scrollable.data('scrollable')) {
      return;
    }
    scrollbars.updateScrollShadow($scrollable);
    let scrollbarArr: Scrollbar[] = $scrollable.data('scrollbars');
    if (!scrollbarArr) {
      if (Device.get().isIos()) {
        scrollbars._handleIosPaintBug($scrollable);
      }
      return;
    }
    if (immediate) {
      scrollbars._update(scrollbarArr);
      return;
    }
    if ($scrollable.data('scrollbarUpdatePending')) {
      return;
    }
    // Executes the update later to prevent unnecessary updates
    setTimeout(() => {
      scrollbars._update(scrollbarArr);
      $scrollable.removeData('scrollbarUpdatePending');
    }, 0);
    $scrollable.data('scrollbarUpdatePending', true);
  },

  /** @internal */
  _update(scrollbarArr: Scrollbar[]) {
    // Reset the scrollbars first to make sure they don't extend the scrollSize
    scrollbarArr.forEach(scrollbar => {
      if (scrollbar.rendered) {
        scrollbar.reset();
      }
    });
    scrollbarArr.forEach(scrollbar => {
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
   * To work around this bug the flag -webkit-overflow-scrolling will be removed if the scrollable component won't display any scrollbars
   * @internal
   */
  _handleIosPaintBug($scrollable: JQuery) {
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
  },

  reset($scrollable: JQuery) {
    let scrollbarArr: Scrollbar[] = $scrollable.data('scrollbars');
    if (!scrollbarArr) {
      return;
    }
    scrollbarArr.forEach(scrollbar => scrollbar.reset());
  },

  /**
   * Scrolls the $scrollable to the given $element (must be a child of $scrollable)
   *
   * @param $scrollable
   *          the scrollable object
   * @param $element
   *          the element to scroll to
   * @param opts
   *          Shorthand version: If a string is passed instead
   *          of an object, the value is automatically converted to the option {@link ScrollToOptions.align}.
   */
  scrollTo($scrollable: JQuery, $element: JQuery, opts?: ScrollToOptions | string) {
    let options: ScrollToOptions;
    if (typeof opts === 'string') {
      options = {
        align: opts
      };
    } else {
      options = scrollbars._createDefaultScrollToOptions(opts);
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
      scrollbars.scrollTop($scrollable, scrollTo, options);
    }
  },

  /** @internal */
  _createDefaultScrollToOptions(options?: ScrollbarInstallOptions): ScrollToOptions {
    let defaults: ScrollToOptions = {
      animate: false,
      stop: true
    };
    return $.extend({}, defaults, options);
  },

  /**
   * Horizontally scrolls the $scrollable to the given $element (must be a child of $scrollable)
   */
  scrollHorizontalTo($scrollable: JQuery, $element: JQuery, options?: ScrollOptions) {
    let scrollTo,
      scrollableW = $scrollable.width(),
      elementBounds = graphics.bounds($element, true),
      elementLeft = elementBounds.x,
      elementW = elementBounds.width;

    if (elementLeft < 0) {
      scrollbars.scrollLeft($scrollable, $scrollable.scrollLeft() + elementLeft, options);
    } else if (elementLeft + elementW > scrollableW) {
      // On IE, a fractional position gets truncated when using scrollTop -> ceil to make sure the full element is visible
      scrollTo = Math.ceil($scrollable.scrollLeft() + elementLeft + elementW - scrollableW);
      scrollbars.scrollLeft($scrollable, scrollTo, options);
    }
  },

  /**
   * @param $scrollable the scrollable object
   * @param scrollTop the new scroll position
   */
  scrollTop($scrollable: JQuery, scrollTop: number, options?: ScrollOptions) {
    options = scrollbars._createDefaultScrollToOptions(options);
    let scrollbarElement = scrollbars.scrollbar($scrollable, 'y');
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
    scrollbars.animateScrollTop($scrollable, scrollTop);
    $scrollable.promise('scroll').always(() => {
      if (scrollbarElement) {
        scrollbarElement.notifyAfterScroll();
      }
    });
  },

  /**
   * @param $scrollable the scrollable object
   * @param scrollLeft the new scroll position
   */
  scrollLeft($scrollable: JQuery, scrollLeft: number, options?: ScrollOptions) {
    options = scrollbars._createDefaultScrollToOptions(options);
    let scrollbarElement = scrollbars.scrollbar($scrollable, 'x');
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
    scrollbars.animateScrollLeft($scrollable, scrollLeft);
    $scrollable.promise('scroll').always(() => {
      if (scrollbarElement) {
        scrollbarElement.notifyAfterScroll();
      }
    });
  },

  animateScrollTop($scrollable: JQuery, scrollTop: number) {
    $scrollable.animate({
      scrollTop: scrollTop
    }, {
      queue: 'scroll'
    })
      .dequeue('scroll');
  },

  animateScrollLeft($scrollable: JQuery, scrollLeft: number) {
    $scrollable.animate({
      scrollLeft: scrollLeft
    }, {
      queue: 'scroll'
    })
      .dequeue('scroll');
  },

  scrollbar($scrollable: JQuery, axis: 'x' | 'y'): Scrollbar {
    let scrollbarArr: Scrollbar[] = $scrollable.data('scrollbars') || [];
    return arrays.find(scrollbarArr, scrollbar => scrollbar.axis === axis);
  },

  scrollToBottom($scrollable: JQuery, options?: ScrollOptions) {
    scrollbars.scrollTop($scrollable, $scrollable[0].scrollHeight - $scrollable[0].offsetHeight, options);
  },

  /**
   * @param $scrollables one or more scrollables to check against
   * @returns true if the location is visible in the current viewport of all the $scrollables, or if $scrollables is null
   */
  isLocationInView(location: { x: number; y: number }, $scrollables: JQuery): boolean {
    if (!$scrollables || $scrollables.length === 0) {
      return true;
    }
    return $scrollables.toArray().every(scrollable => {
      let scrollableOffsetBounds = graphics.offsetBounds($(scrollable));
      return scrollableOffsetBounds.contains(location.x, location.y);
    });
  },

  /**
   * Attaches the given handler to each scrollable parent, including $anchor if it is scrollable as well.
   * Make sure you remove the handlers when not needed anymore using offScroll.
   */
  onScroll($anchor: JQuery, handler: (event: JQuery.ScrollEvent<HTMLElement>) => void) {
    handler['$scrollParents'] = [];
    $anchor.scrollParents().each(function() {
      let $scrollParent = $(this);
      $scrollParent.on('scroll', handler);
      handler['$scrollParents'].push($scrollParent);
    });
  },

  offScroll(handler: (event: JQuery.ScrollEvent<HTMLElement>) => void) {
    let $scrollParents: JQuery[] = handler['$scrollParents'];
    if (!$scrollParents) {
      throw new Error('$scrollParents are not defined');
    }
    for (let i = 0; i < $scrollParents.length; i++) {
      let $elem = $scrollParents[i];
      $elem.off('scroll', handler);
    }
  },

  /**
   * Sets the position to fixed and updates left and top position.
   * This is necessary to prevent flickering in IE.
   */
  fix($elem: JQuery) {
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
  },

  /**
   * Reverts the changes made by fix().
   */
  unfix($elem: JQuery, timeoutId: number, immediate?: boolean): number {
    clearTimeout(timeoutId);
    if (immediate) {
      scrollbars._unfix($elem);
      return;
    }
    return setTimeout(() => {
      scrollbars._unfix($elem);
    }, 50);
  },

  /** @internal */
  _unfix($elem: JQuery) {
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
   * @param [session] when no session is given, scrollables from all sessions are stored
   */
  storeScrollPositions($container: JQuery, session?: Session) {
    let $scrollables = scrollbars.getScrollables(session);
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
  },

  /**
   * Restores the position of all scrollables that belong to an optional session.
   * @param session when no session is given, scrollables from all sessions are restored
   */
  restoreScrollPositions($container: JQuery, session?: Session) {
    let $scrollables = scrollbars.getScrollables(session);
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
        // Also make sure that scroll bar is up-to-date
        // Introduced for use case: Open large table page, edit entry, press f5
        // -> outline tab gets rendered, scrollbar gets updated with set timeout, outline tab gets detached
        // -> update event never had any effect because it executed after detaching (due to set timeout)
        scrollbars.update($scrollable);
        $.log.isTraceEnabled() && $.log.trace('Restored scroll position for ' + $scrollable.attr('class') + '. Top: ' + scrollTop + '. Left: ' + scrollLeft);
      }
    });
  },

  setVisible($scrollable: JQuery, visible: boolean) {
    if (!$scrollable || !$scrollable.data('scrollable')) {
      return;
    }
    let scrollbarArr = $scrollable.data('scrollbars');
    if (!scrollbarArr) {
      return;
    }
    scrollbarArr.forEach(scrollbar => {
      if (scrollbar.rendered) {
        scrollbar.$container.setVisible(visible);
      }
    });
  },

  opacity($scrollable: JQuery, opacity: number) {
    if (!$scrollable || !$scrollable.data('scrollable')) {
      return;
    }
    let scrollbarArr = $scrollable.data('scrollbars');
    if (!scrollbarArr) {
      return;
    }
    scrollbarArr.forEach(scrollbar => {
      if (scrollbar.rendered) {
        scrollbar.$container.css('opacity', opacity);
      }
    });
  },

  /** @internal */
  _getCompleteChildRowsHeightRecursive(children: ExpandableElement[], getChildren: (element: ExpandableElement) => ExpandableElement[], isExpanded: (element: ExpandableElement) => boolean, defaultChildHeight: number): number {
    let height = 0;
    children.forEach(child => {
      if (child.height) {
        height += child.height;
      } else {
        // fallback for children with unset height
        height += defaultChildHeight;
      }
      if (isExpanded(child) && getChildren(child).length > 0) {
        height += scrollbars._getCompleteChildRowsHeightRecursive(getChildren(child), getChildren, isExpanded, defaultChildHeight);
      }
    });
    return height;
  },

  ensureExpansionVisible<T extends ExpandableElement>(parent: ExpansionParent<T>) {
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
        scrollbars.scrollTop(parent.$scrollable, minScrollTop, {
          animate: true
        });
      }
    } else if (isParentExpanded && children.length > 0) {
      // parent is expanded and has children, the best effort approach to show the expansion
      let fullDataHeight = parent.$scrollable.height();

      // get childRowCount considering already expanded rows
      let childRowsHeight = scrollbars._getCompleteChildRowsHeightRecursive(children, parent.getChildren, parent.isExpanded, parent.defaultChildHeight);

      // + 1.5 since it's the parent's top position, and we want to scroll half a row further to show that there's something after the expansion
      let additionalHeight = childRowsHeight + (1.5 * parentHeight);
      let scrollTo = parentPositionTop + additionalHeight;
      // scroll as much as needed to show the expansion but make sure that the parent row (plus one more) is still visible
      let newScrollTop = scrollTopPos + Math.min(scrollTo - fullDataHeight, parentPositionTop - parentHeight);
      // only scroll down
      if (newScrollTop > scrollTopPos) {
        scrollbars.scrollTop(parent.$scrollable, newScrollTop, {
          animate: true,
          stop: false
        });
      }
    }

    if (children.length > 0) {
      // horizontal scrolling: at least 3 levels of hierarchy should be visible (only relevant for small fields)
      let minLevelLeft = Math.max(parent.element.level - 3, 0) * parent.nodePaddingLevel;
      scrollbars.scrollLeft(parent.$scrollable, minLevelLeft, {
        animate: true,
        stop: false
      });
    }
  }
};
