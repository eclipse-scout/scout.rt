/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, Device, events, objects, strings} from '../index';

/**
 * @returns {number} the x coordinate where the event happened, works for touch events as well.
 */
export function pageX(event) {
  if (!objects.isNullOrUndefined(event.pageX)) {
    return event.pageX;
  }
  return event.originalEvent.touches[0].pageX;
}

/**
 * @returns {number} the y coordinate where the event happened, works for touch events as well.
 */
export function pageY(event) {
  if (!objects.isNullOrUndefined(event.pageY)) {
    return event.pageY;
  }
  return event.originalEvent.touches[0].pageY;
}

export function touchdown(touch, suffix) {
  return touchOrMouse(touch, 'touchstart', 'mousedown', suffix);
}

export function touchmove(touch, suffix) {
  return touchOrMouse(touch, 'touchmove', 'mousemove', suffix);
}

export function touchendcancel(touch, suffix) {
  return touchOrMouse(touch, 'touchend touchcancel', 'mouseup', suffix);
}

export function touchOrMouse(touch, touchevent, mouseevent, suffix) {
  suffix = suffix || '';
  if (suffix) {
    suffix = '.' + suffix;
  }
  if (touch) {
    return touchevent + suffix;
  }
  return mouseevent + suffix;
}

export function isTouchEvent(event) {
  return event && strings.startsWith(event.type, 'touch');
}

export function fixTouchEvent(event) {
  if (isTouchEvent(event)) {
    let touches = event.touches || (event.originalEvent ? event.originalEvent.touches : null);
    let touch = touches ? touches[0] : null;
    if (touch) {
      // Touch events may contain fractional values, while mouse events should not
      // - https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent/pageX
      // - https://www.chromestatus.com/features/6169687914184704
      event.pageX = Math.round(touch.pageX);
      event.pageY = Math.round(touch.pageY);
    }
  }
}

/**
 * @returns an object containing passive: true if the browser supports passive event listeners, otherwise returns false.
 */
export function passiveOptions() {
  let options = false;
  if (Device.get().supportsPassiveEventListener()) {
    options = {
      passive: true
    };
  }
  return options;
}

/**
 * Listens for scroll events and executes startHandler on first event. It then regularly checks for further scroll events and executes endHandler if no event has fired since a certain amount of time and the user has released his finger.
 * If he does not release his finger the endHandler won't be called even if the pane has stopped scrolling.
 */
export function onScrollStartEndDuringTouch($elem, startHandler, endHandler) {
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
    // Check some ms later if a scroll event has occurred in the mean time.
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
    $elem.document().off('touchend touchcancel', touchEndHandler);
  }

  $elem.on('scroll', scrollHandler);
  // Make sure handler is executed and scroll listener removed if no scroll event occurs
  $elem.document().one('touchend touchcancel', touchEndHandler);
}

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
 * @param target {HTMLElement} the element which should receive the event
 * @param event {Event} the original event which should be propagated
 */
export function propagateEvent(target, event) {
  if (typeof (Event) !== 'function') {
    return;
  }
  let newEvent = new event.constructor(event.type, event);
  if (!target.dispatchEvent(newEvent)) {
    event.preventDefault();
  }
}

/**
 * Adds an event listener for each given type to the source which propagates the events for that type to the target.
 *
 * <p>
 * <b>Important</b>
 * This function only works in browsers supporting the Event constructor (e.g. KeyboardEvent: https://developer.mozilla.org/de/docs/Web/API/KeyboardEvent/KeyboardEvent).
 * </p>
 *
 * @param source {HTMLElement} the element for which the event listener should be added.
 * @param target {HTMLElement} the element which should receive the event.
 * @param types {string[]} an array of event types.
 * @param {function} [filter] an optional filter function which can return false if the event should not be propagated.
 */
export function addPropagationListener(source, target, types, filter) {
  types = arrays.ensure(types);
  types.forEach(type => {
    source.addEventListener(type, event => {
      if (filter && !filter(event)) {
        return;
      }
      propagateEvent(target, event);
    });
  });
}

/**
 * @typedef {Object} SwipeCallbackEvent
 * @property {MouseEvent|TouchEvent} originalEvent The original event received from the browser.
 * @property {number} originalLeft The left position of the element at the moment the swipe was started.
 * @property {number} deltaX The horizontal delta the swipe has already moved (negative values mean to the left of the original left position).
 * @property {number} newLeft The current left position of the element.
 * @property {number} direction -1 if the move is to the left, 1 if the move is to the right, 0 or -0 if it is not moved yet
 */

/**
 * Adds swipe event listeners to the element given.
 *
 * @param {jQuery} $element The element on which the listeners should be attached.
 * @param {string} id An event listener id used to be registered on the window object.
 * @param {function(SwipeCallbackEvent)} [onDown] Callback to be invoked when the swipe is started (mouse or touch down).
 * @param {function(SwipeCallbackEvent)} [onMove] Callback to be invoked when mouse (or finger if touch) is moved (while being down).
 * @param {function(SwipeCallbackEvent)} [onUp] Callback to be invoked when the swipe is ended (mouse or finger released).
 */
export function onSwipe($element, id, onDown, onMove, onUp) {
  let $window = $element.window();
  let touch = Device.get().supportsOnlyTouch();

  $element.on('touchmove', event => event.preventDefault()); // prevent scrolling the background when swiping (iOS)
  $element.on('remove', event => $window.off('.' + id));

  $element.on(touchdown(touch), event => {
    let origPosLeft = $element.position().left;
    let acceptDown = !onDown || !!onDown({originalEvent: event, originalLeft: origPosLeft, deltaX: 0, newLeft: origPosLeft, direction: 0});
    if (!acceptDown) {
      return;
    }

    let dragging = true;
    let origPageX = events.pageX(event);
    let curPosLeft = origPosLeft;
    let direction = 0;

    $window.on(touchmove(touch, id), event => {
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

    $window.on(touchendcancel(touch, id), event => {
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

export default {
  addPropagationListener,
  fixTouchEvent,
  isTouchEvent,
  onScrollStartEndDuringTouch,
  pageX,
  pageY,
  passiveOptions,
  propagateEvent,
  touchOrMouse,
  touchdown,
  touchendcancel,
  touchmove,
  onSwipe
};
