/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Device, objects, Predicate, strings} from '../index';
import $ from 'jquery';

function isTouchEvent(event: JQuery.Event): event is JQuery.TouchEventBase {
  return event && strings.startsWith(event.type, 'touch');
}

export const events = {
  /**
   * @returns the x coordinate where the event happened, works for touch events as well.
   */
  pageX(event: JQuery.TriggeredEvent): number {
    if (!objects.isNullOrUndefined(event.pageX)) {
      return event.pageX;
    }
    // @ts-expect-error
    return event.originalEvent.touches[0].pageX;
  },

  /**
   * @returns the y coordinate where the event happened, works for touch events as well.
   */
  pageY(event: JQuery.TriggeredEvent): number {
    if (!objects.isNullOrUndefined(event.pageY)) {
      return event.pageY;
    }
    // @ts-expect-error
    return event.originalEvent.touches[0].pageY;
  },

  touchdown(touch: boolean, suffix?: string): string {
    return events.touchOrMouse(touch, 'touchstart', 'mousedown', suffix);
  },

  touchmove(touch: boolean, suffix?: string): string {
    return events.touchOrMouse(touch, 'touchmove', 'mousemove', suffix);
  },

  touchendcancel(touch: boolean, suffix?: string): string {
    return events.touchOrMouse(touch, 'touchend touchcancel', 'mouseup', suffix);
  },

  touchOrMouse(touch: boolean, touchEvent: string, mouseEvent: string, suffix?: string): string {
    suffix = suffix || '';
    if (suffix) {
      suffix = '.' + suffix;
    }
    if (touch) {
      return touchEvent + suffix;
    }
    return mouseEvent + suffix;
  },

  isTouchEvent,

  fixTouchEvent(event: JQuery.Event) {
    if (isTouchEvent(event)) {
      let touches = event.touches || (event.originalEvent ? event.originalEvent.touches : null);
      let touch = touches ? touches[0] : null;
      if (touch) {
        // Touch events may contain fractional values, while mouse events should not
        // - https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent/pageX
        // - https://www.chromestatus.com/feature/6169687914184704
        event.pageX = Math.round(touch.pageX) as undefined;
        event.pageY = Math.round(touch.pageY) as undefined;
      }
    }
  },

  /**
   * @returns an object containing passive: true if the browser supports passive event listeners, otherwise returns false.
   */
  passiveOptions(): boolean | { passive: boolean } {
    let options: boolean | { passive: boolean } = false;
    if (Device.get().supportsPassiveEventListener()) {
      options = {
        passive: true
      };
    }
    return options;
  },

  /**
   * Listens for scroll events and executes startHandler on first event. It then regularly checks for further scroll events and executes endHandler if no event has fired since a certain amount of time and the user has released his finger.
   * If he does not release his finger the endHandler won't be called even if the pane has stopped scrolling.
   */
  onScrollStartEndDuringTouch($elem: JQuery, startHandler: () => void, endHandler: () => void) {
    let scrollTimeout;
    let started = false;
    let touchend = false;
    let scrollHandler = event => {
      // Execute once on first scroll event (and not as soon as user touches the pane because he might not even want to scroll)
      if (!started) {
        startHandler();
        started = true;
      }

      clearTimeout(scrollTimeout);
      // Check some ms later if a scroll event has occurred in the meantime.
      // If yes it (probably) is still scrolling. If no it (probably) is not scrolling anymore -> call handler
      checkLater();
    };

    let touchEndHandler = event => {
      touchend = true;
      checkLater();
    };

    function checkLater() {
      clearTimeout(scrollTimeout);
      scrollTimeout = setTimeout(() => {
        if (touchend) {
          // Only stop processing if user released the finger
          removeHandlers();
          endHandler();
        }
      }, 50);
    }

    function removeHandlers() {
      $elem.off('scroll', scrollHandler);
      $elem.document(false).off('touchend touchcancel', touchEndHandler);
    }

    $elem.on('scroll', scrollHandler);
    // Make sure handler is executed and scroll listener removed if no scroll event occurs
    $elem.document(false).one('touchend touchcancel', touchEndHandler);
  },

  /**
   * Forwards the event to the given target by creating a new event with the same data as the old one.
   * Prevents default action of the original event if preventDefault was called for the forwarded event.
   * Does not use jQuery to make sure the capture phase is executed as well.
   *
   * <p>
   * <b>Important</b>
   * This function only works in browsers supporting the Event constructor (e.g. KeyboardEvent: https://developer.mozilla.org/de/docs/Web/API/KeyboardEvent/KeyboardEvent).
   * </p>
   *
   * @param target the element which should receive the event
   * @param event the original event which should be propagated
   */
  propagateEvent(target: EventTarget, event: Event) {
    if (typeof (Event) !== 'function') {
      return;
    }
    // @ts-expect-error
    let newEvent: PropagatedEvent = new event.constructor(event.type, event);
    newEvent.sourceEvent = event;
    if (!target.dispatchEvent(newEvent)) {
      event.preventDefault();
    }
  },

  /**
   * Adds an event listener for each given type to the source which propagates the events for that type to the target.
   *
   * <p>
   * <b>Important</b>
   * This function only works in browsers supporting the Event constructor (e.g. KeyboardEvent: https://developer.mozilla.org/de/docs/Web/API/KeyboardEvent/KeyboardEvent).
   * </p>
   *
   * @param source the element for which the event listener should be added.
   * @param target the element which should receive the event.
   * @param types an array of event types.
   * @param an optional filter function which can return false if the event should not be propagated.
   */
  addPropagationListener(source: EventTarget, target: EventTarget, types: string[], filter?: Predicate<Event>) {
    types = arrays.ensure(types);
    types.forEach(type => {
      source.addEventListener(type, event => {
        if (filter && !filter(event)) {
          return;
        }
        events.propagateEvent(target, event);
      });
    });
  },

  /**
   * @returns the app-link which triggered the given event, or `null` if the app-link could not be identified
   */
  find$AppLink(event: JQuery.TriggeredEvent): JQuery {
    // AppLinkKeyStroke is typically bound to the container, target is the element with the tabindex (app-link).
    // Click listeners are typically bound to the app-link directly but the link may contain inner elements.
    // In that case the target would be the inner element which is not an app-link.
    let $start = $(event.target);
    let $stop = $(event.delegateTarget);
    let $appLink = $start.findUp($elem => $elem.hasClass('app-link'), $stop);
    if ($appLink.length > 0) {
      return $appLink;
    }
    return null;
  },

  /**
   * Calls the given `triggerFunction` with the reference of the app link that triggered the given `event`.
   */
  triggerAppLinkAction(event: JQuery.TriggeredEvent, triggerFunction: (ref: string) => void) {
    let $appLink = events.find$AppLink(event);
    if (!$appLink) {
      return;
    }
    let ref = $appLink.data('ref');
    triggerFunction(ref);
  },

  /**
   * Prevents text selection for the given `$container` until a mouse up occurs anywhere on the window.
   */
  preventSelectionStartUntilMouseUp($container: JQuery) {
    const preventSelectionHandler = event => event.preventDefault();
    $container
      .on('selectstart', preventSelectionHandler)
      .window().one('mouseup', () => $container.off('selectstart', preventSelectionHandler));
  },

  /**
   * Adds swipe event listeners to the element given.
   *
   * @param $element The element on which the listeners should be attached.
   * @param id An event listener id used to be registered on the window object.
   * @param [onDown] Callback to be invoked when the swipe is started (mouse or touch down).
   * @param [onMove] Callback to be invoked when mouse (or finger if touch) is moved (while being down).
   * @param [onUp] Callback to be invoked when the swipe is ended (mouse or finger released).
   */
  onSwipe($element: JQuery, id: string, onDown?: (event: SwipeCallbackEvent) => boolean, onMove?: (event: SwipeCallbackEvent) => number, onUp?: (event: SwipeCallbackEvent) => void) {
    let $window = $element.window(false);
    let touch = Device.get().supportsOnlyTouch();

    $element.on('touchmove', event => event.preventDefault()); // prevent scrolling the background when swiping (iOS)
    $element.on('remove', event => $window.off('.' + id));

    $element.on(events.touchdown(touch), event => {
      let origPosLeft = $element.position().left;
      let acceptDown = !onDown || !!onDown({originalEvent: event, originalLeft: origPosLeft, deltaX: 0, newLeft: origPosLeft, direction: 0});
      if (!acceptDown) {
        return;
      }

      let dragging = true;
      let origPageX = events.pageX(event);
      let curPosLeft = origPosLeft;
      let direction = 0;

      $window.on(events.touchmove(touch, id), event => {
        let pageX = events.pageX(event);
        let deltaX = pageX - origPageX;
        let newLeft = origPosLeft + deltaX;
        if (newLeft !== curPosLeft) {
          // only update swipe direction if it actually changed
          direction = Math.sign(newLeft - curPosLeft);
        }
        if (onMove) {
          let l = onMove({originalEvent: event, originalLeft: origPosLeft, deltaX: deltaX, newLeft: newLeft, direction: direction});
          curPosLeft = typeof l === 'number' ? l : newLeft;
        } else {
          curPosLeft = newLeft;
        }
      });

      $window.on(events.touchendcancel(touch, id), event => {
        if (!dragging) {
          // On iOS touchcancel and touchend are fired right after each other when swiping twice very fast -> Ignore the second event
          return;
        }
        dragging = false;
        $window.off('.' + id);
        if (onUp) {
          onUp({originalEvent: event, originalLeft: origPosLeft, deltaX: curPosLeft - origPosLeft, newLeft: curPosLeft, direction: direction});
        }
      });
    });
  }
};

export interface SwipeCallbackEvent {
  /**
   * The original event received from the browser.
   */
  originalEvent: JQuery.TriggeredEvent;

  /**
   * The left position of the element at the moment the swipe was started.
   */
  originalLeft: number;

  /**
   * The horizontal delta the swipe has already moved (negative values mean to the left of the original left position).
   */
  deltaX: number;

  /**
   * The current left position of the element.
   */
  newLeft: number;

  /**
   * -1 if the move is to the left, 1 if the move is to the right, 0 or -0 if it is not moved yet
   */
  direction: number;
}

export interface PropagatedEvent extends Event {
  sourceEvent: Event;
}

/**
 * Constants for {@link MouseEvent.button} values.
 */
export enum MouseButton {
  /** Main button, usually the left button, or the un-initialized state */
  PRIMARY = 0,
  /** Auxiliary button, usually the wheel button or the middle button (if present) */
  AUXILIARY = 1,
  /** Secondary button, usually the right button */
  SECONDARY = 2,
  /** Fourth button, typically the "Browser Back" button */
  BACK = 3,
  /** Fifth button, typically the "Browser Forward" button */
  FORWARD = 4
}

/**
 * Constants for {@link MouseEvent.buttons} values.
 */
export enum MouseButtons {
  /** No button or un-initialized */
  NONE = 0,
  /** Primary button, usually the left button */
  PRIMARY = 1,
  /** Secondary button, usually the right button */
  SECONDARY = 2,
  /** Auxiliary button, usually the wheel button or the middle button (if present) */
  AUXILIARY = 4,
  /** Fourth button, typically the "Browser Back" button */
  BACK = 8,
  /** Fifth button, typically the "Browser Forward" button */
  FORWARD = 16
}
