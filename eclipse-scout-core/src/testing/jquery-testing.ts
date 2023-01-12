/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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

export const JQueryTesting = {
  triggerBlur($elem: JQuery) {
    let event = $.Event('blur', {
      originalEvent: $.Event('dummy') // create dummy object
    });
    $elem.trigger(event);
  },

  triggerRightClick($elem: JQuery) {
    $elem.trigger($.Event('mousedown', {which: 3}));
    $elem.trigger($.Event('mouseup', {which: 3}));
  },

  triggerKeyUp($elem: JQuery, key: number, modifier: KeyStrokeModifier) {
    let event = $.Event('keyup', {
      originalEvent: $.Event('dummy'), // create dummy object
      which: key
    });
    JQueryTesting.extendEventWithModifier(event, modifier);
    $elem.trigger(event);
  },

  triggerKeyDown($elem: JQuery, key: number, modifier?: KeyStrokeModifier) {
    let event = $.Event('keydown', {
      originalEvent: $.Event('dummy'), // create dummy object
      which: key
    });
    JQueryTesting.extendEventWithModifier(event, modifier);
    $elem.trigger(event);
  },

  extendEventWithModifier(event: JQuery.Event, modifier?: KeyStrokeModifier) {
    event.altKey = modifier === 'alt';
    event.ctrlKey = modifier === 'ctrl';
    event.shiftKey = modifier === 'shift';
    event.metaKey = modifier === 'meta';
  },

  triggerMouseEnter($elem: JQuery, opts?: TriggerMouseOptions) {
    JQueryTesting.triggerMouseAction($elem, 'mouseenter', opts);
  },

  triggerMouseLeave($elem: JQuery, opts?: TriggerMouseOptions) {
    JQueryTesting.triggerMouseAction($elem, 'mouseleave', opts);
  },

  triggerMouseDown($elem: JQuery, opts?: TriggerMouseOptions) {
    JQueryTesting.triggerMouseAction($elem, 'mousedown', opts);
  },

  /**
   * Does not use jQuery to create the event to make sure capture phase listeners are notified as well.
   */
  triggerMouseDownCapture($elem: JQuery) {
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
  },

  triggerKeyDownCapture($elem: JQuery, which: number, modifier?: KeyStrokeModifier) {
    JQueryTesting.triggerKeyCapture($elem, 'keydown', which, modifier);
  },

  triggerKeyUpCapture($elem: JQuery, which: number, modifier?: KeyStrokeModifier) {
    JQueryTesting.triggerKeyCapture($elem, 'keyup', which, modifier);
  },

  /**
   * Triggers key down and key up events.
   */
  triggerKeyInputCapture($elem: JQuery, which: number, modifier?: KeyStrokeModifier) {
    JQueryTesting.triggerKeyCapture($elem, 'keydown', which, modifier);
    JQueryTesting.triggerKeyCapture($elem, 'keyup', which, modifier);
  },

  triggerKeyCapture($elem: JQuery, eventType: string, which: number, modifier?: KeyStrokeModifier) {
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
    JQueryTesting.extendEventWithModifier(eventObj, modifier);

    $elem[0].dispatchEvent(eventObj);
  },

  triggerMouseUp($elem: JQuery, opts?: TriggerMouseOptions) {
    JQueryTesting.triggerMouseAction($elem, 'mouseup', opts);
  },

  triggerMouseAction($elem: JQuery, eventType: string, opts: TriggerMouseOptions) {
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
    event = $.Event(eventType, {
      which: opts.which,
      originalEvent: $.Event(eventType, {
        detail: opts.clicks
      }),
      pageX: opts.position.left,
      pageY: opts.position.top
    });
    if (opts.modifier) {
      JQueryTesting.extendEventWithModifier(event, opts.modifier);
    }

    $elem.trigger(event);
  },

  triggerContextMenu($elem: JQuery) {
    let opts = {
      position: $elem.offset(),
      which: 3
    };

    JQueryTesting.triggerMouseDown($elem, opts);
    JQueryTesting.triggerMouseUp($elem, opts);
    $elem.trigger($.Event('contextmenu', {
      pageX: opts.position.left,
      pageY: opts.position.top
    }));
  },

  /**
   * Triggers mouse down, mouse up and click events. <br>
   * Also sets the detail property of the originalEvent which contains the numbers of clicks.
   * @param opts options object passed to triggerMouse* functions
   */
  triggerClick($elem: JQuery, opts?: TriggerMouseOptions) {
    opts = opts || {};

    if (!opts.click) {
      opts.click = 1;
    }

    JQueryTesting.triggerMouseDown($elem, opts);
    JQueryTesting.triggerMouseUp($elem, opts);
    JQueryTesting.triggerMouseAction($elem, 'click', opts);
  },

  triggerDoubleClick($elem: JQuery) {
    JQueryTesting.triggerClick($elem);
    JQueryTesting.triggerClick($elem, {click: 2});
    $elem.trigger($.Event('dblclick', {
      originalEvent: $.Event('dummy', {
        detail: 2
      })
    }));
  },

  triggerImageLoadCapture($elem: JQuery) {
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
};
