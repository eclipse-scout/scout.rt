/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, Device, objects, strings} from '../index';

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
    // Check some ms later if a scroll event has occured in the mean time.
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
 * -> It does not work for IE 11.
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
 * -> It does not work for IE 11.
 * </p>
 *
 * @param source {HTMLElement} the element for which the event listener should be added.
 * @param target {HTMLElement} the element which should receive the event.
 * @param types {string[]} an array of event types.
 * @param [filter] {function} an optional filter function which can return false if the event should not be propagated.
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
  touchmove
};
