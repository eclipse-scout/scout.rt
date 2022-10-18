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

/* eslint-disable new-cap */

// JQuery extensions for testing purpose

export interface TriggerMouseOptions {
  clicks?: number;
  click?: number;
  which?: number;
  modifier?: KeyStrokeModifier;
  position?: {
    top: number;
    left: number;
  };
}

export type KeyStrokeModifier = 'alt' | 'ctrl' | 'shift' | 'meta';

export function triggerBlur($elem: JQuery) {
  let event = jQuery.Event('blur', {
    originalEvent: jQuery.Event('dummy') // create dummy object
  });
  $elem.trigger(event);
}

export function triggerRightClick($elem: JQuery) {
  $elem.trigger(jQuery.Event('mousedown', {which: 3}));
  $elem.trigger(jQuery.Event('mouseup', {which: 3}));
}

export function triggerKeyUp($elem, key: number, modifier: KeyStrokeModifier) {
  let event = jQuery.Event('keyup', {
    originalEvent: jQuery.Event('dummy'), // create dummy object
    which: key
  });
  extendEventWithModifier(event, modifier);
  $elem.trigger(event);
}

export function triggerKeyDown($elem: JQuery, key: number, modifier?: KeyStrokeModifier) {
  let event = jQuery.Event('keydown', {
    originalEvent: jQuery.Event('dummy'), // create dummy object
    which: key
  });
  extendEventWithModifier(event, modifier);
  $elem.trigger(event);
}

function extendEventWithModifier(event: JQuery.Event, modifier?: KeyStrokeModifier) {
  event.altKey = modifier === 'alt';
  event.ctrlKey = modifier === 'ctrl';
  event.shiftKey = modifier === 'shift';
  event.metaKey = modifier === 'meta';
}

export function triggerMouseEnter($elem: JQuery, opts?: TriggerMouseOptions) {
  triggerMouseAction($elem, 'mouseenter', opts);
}

export function triggerMouseLeave($elem: JQuery, opts?: TriggerMouseOptions) {
  triggerMouseAction($elem, 'mouseleave', opts);
}

export function triggerMouseDown($elem: JQuery, opts?: TriggerMouseOptions) {
  triggerMouseAction($elem, 'mousedown', opts);
}

/**
 * Does not use jQuery to create the event to make sure capture phase listeners are notified as well.
 */
export function triggerMouseDownCapture($elem: JQuery) {
  let event;
  try {
    event = new MouseEvent('mousedown', {
      'view': window,
      'bubbles': true,
      'cancelable': true
    });
  } catch (e) {
    // Phantom JS only supports the old, deprecated API
    event = document.createEvent('MouseEvent');
    event.initEvent('mousedown', true, true);
  }
  $elem[0].dispatchEvent(event);
}

export function triggerKeyDownCapture($elem: JQuery, which: number, modifier?: KeyStrokeModifier) {
  triggerKeyCapture($elem, 'keydown', which, modifier);
}

export function triggerKeyUpCapture($elem: JQuery, which: number, modifier?: KeyStrokeModifier) {
  triggerKeyCapture($elem, 'keyup', which, modifier);
}

/**
 * Triggers key down and key up events.
 */
export function triggerKeyInputCapture($elem: JQuery, which: number, modifier?: KeyStrokeModifier) {
  triggerKeyCapture($elem, 'keydown', which, modifier);
  triggerKeyCapture($elem, 'keyup', which, modifier);
}

export function triggerKeyCapture($elem: JQuery, eventType: string, which: number, modifier?: KeyStrokeModifier) {
  // Due to a Chrome bug, "new KeyboardEvent" cannot be used,
  // as it doesn't set "which". We have to use this less specific
  // constructor.
  let eventObj;

  try {
    eventObj = new Event(eventType, {
      'bubbles': true,
      'cancelable': true
    });
  } catch (e) {
    // Workaround for PhantomJS
    eventObj = document.createEvent('CustomEvent');
    eventObj.initEvent(eventType, true, true);
  }

  eventObj.keyCode = which;
  eventObj.which = which;
  extendEventWithModifier(eventObj, modifier);

  $elem[0].dispatchEvent(eventObj);
}

export function triggerMouseUp($elem: JQuery, opts?: TriggerMouseOptions) {
  triggerMouseAction($elem, 'mouseup', opts);
}

export function triggerMouseAction($elem: JQuery, eventType: string, opts: TriggerMouseOptions) {
  let event;
  opts = opts || {};

  if (!opts.position) {
    opts.position = $elem.offset();
  }
  if (!opts.clicks) {
    opts.clicks = 1;
  }
  if (!opts.which) {
    opts.which = 1;
  }
  event = jQuery.Event(eventType, {
    which: opts.which,
    originalEvent: jQuery.Event(eventType, {
      detail: opts.clicks
    }),
    pageX: opts.position.left,
    pageY: opts.position.top
  });
  if (opts.modifier) {
    extendEventWithModifier(event, opts.modifier);
  }

  $elem.trigger(event);
}

export function triggerContextMenu($elem: JQuery) {
  let opts = {
    position: $elem.offset(),
    which: 3
  };

  triggerMouseDown($elem, opts);
  triggerMouseUp($elem, opts);
  $elem.trigger(jQuery.Event('contextmenu', {
    pageX: opts.position.left,
    pageY: opts.position.top
  }));
}

/**
 * Triggers mouse down, mouse up and click events. <br>
 * Also sets the detail property of the originalEvent which contains the numbers of clicks.
 * @param opts options object passed to triggerMouse* functions
 */
export function triggerClick($elem: JQuery, opts?: TriggerMouseOptions) {
  opts = opts || {};

  if (!opts.click) {
    opts.click = 1;
  }

  triggerMouseDown($elem, opts);
  triggerMouseUp($elem, opts);
  triggerMouseAction($elem, 'click', opts);
}

export function triggerDoubleClick($elem: JQuery) {
  triggerClick($elem);
  triggerClick($elem, {click: 2});
  $elem.trigger(jQuery.Event('dblclick', {
    originalEvent: jQuery.Event('dummy', {
      detail: 2
    })
  }));
}

export function triggerImageLoadCapture($elem: JQuery) {
  let event;
  try {
    event = new Event('load', {
      'bubbles': true,
      'cancelable': true
    });
  } catch (e) {
    // Phantom JS only supports the old, deprecated API
    event = document.createEvent('Event');
    event.initEvent('load', true, true);
  }
  $elem[0].dispatchEvent(event);
}

export default {
  triggerBlur,
  triggerRightClick,
  triggerKeyUp,
  triggerKeyDown,
  triggerMouseEnter,
  triggerMouseLeave,
  triggerMouseDownCapture,
  triggerKeyDownCapture,
  triggerKeyUpCapture,
  triggerKeyInputCapture,
  triggerKeyCapture,
  triggerMouseUp,
  triggerMouseAction,
  triggerContextMenu,
  triggerClick,
  triggerDoubleClick,
  triggerImageLoadCapture
};

