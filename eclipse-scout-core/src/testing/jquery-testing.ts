/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
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

export type KeyStrokeModifier = 'alt' | 'ctrl' | 'shift' | 'meta' | 'ctrl-shift' | 'ctrl-alt' | 'ctrl-alt-shift';

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

  extendEventWithModifier<T extends { altKey: boolean; ctrlKey: boolean; shiftKey: boolean; metaKey: boolean }>(event: T, modifier?: KeyStrokeModifier): T {
    if (!modifier) {
      return event;
    }
    event.altKey = modifier.includes('alt');
    event.ctrlKey = modifier.includes('ctrl');
    event.shiftKey = modifier.includes('shift');
    event.metaKey = modifier.includes('meta');
    return event;
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

  triggerMouseMove($elem: JQuery, opts?: TriggerMouseOptions) {
    JQueryTesting.triggerMouseAction($elem, 'mousemove', opts);
  },

  /**
   * Does not use jQuery to create the event to make sure capture phase listeners are notified as well.
   */
  triggerMouseDownCapture($elem: JQuery) {
    let event = new MouseEvent('mousedown', {
      view: window,
      bubbles: true,
      cancelable: true
    });
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
    let eventObj = new KeyboardEvent(eventType, this.extendEventWithModifier({
      bubbles: true,
      cancelable: true,
      which: which,
      keyCode: which
    }, modifier));

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
    let event = new Event('load', {
      bubbles: true,
      cancelable: true
    });
    $elem[0].dispatchEvent(event);
  },

  whenAnimationEnd($elem: JQuery): JQuery.Promise<Document> {
    let def = $.Deferred();
    $elem.oneAnimationEnd(() => {
      def.resolve();
    });
    return def.promise();
  },

  /**
   * Selects text in the given html element.
   *
   * @param $elem a text node or a html element containing a text node
   * @param customWindow Needs to be specified if a text inside an iframe should be selected. Otherwise, the regular window object will be used.
   */
  selectText($elem: JQuery, begin: number, end: number, customWindow?: Window) {
    let win = customWindow || $elem.window(true);
    let range = document.createRange();
    let textNode;
    if ($elem[0].nodeType === 3) {
      textNode = $elem[0];
    } else {
      textNode = $elem[0].childNodes[0];
    }
    range.setStart(textNode, begin);
    range.setEnd(textNode, end);
    win.getSelection().removeAllRanges();
    win.getSelection().addRange(range);
  },

  /**
   * The returned promise resolves when the document of the iframe is loaded.
   */
  whenDocLoad($iframe: JQuery<HTMLIFrameElement>): JQuery.Promise<Document> {
    let def = $.Deferred();
    $iframe.on('load', () => {
      def.resolve($iframe[0].contentDocument);
    });
    return def.promise();
  }
};
