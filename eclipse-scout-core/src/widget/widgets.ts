/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Widget} from '../index';
import $ from 'jquery';

let uniqueIdSeqNo = 0;

export const widgets = {
  /**
   * @param $elem HTML or jQuery element to find the corresponding widget for
   * @returns the widget for the given element. If the element is not linked with a widget directly, it searches its ancestors for the widget.
   */
  get($elem: HTMLElement | JQuery): Widget {
    $elem = $.ensure($elem);
    while ($elem && $elem.length > 0) {
      let widget = $elem.data('widget');
      if (widget) {
        return widget;
      }
      $elem = $elem.parent();
    }
    return null;
  },

  /**
   * Creates a "unique" id which may be used in the id attribute of an HTML element.
   * <p>
   * It actually just increases a sequence number prefixed by 'sc' or the given prefix.
   * 'sc' (short for Scout) is added to reduce the possibility of a duplication
   * if scout widgets are embedded into a foreign website which already uses numbers in its id attributes.
   * So it is not unique but should be good enough when you know what you are doing.
   */
  createUniqueId(prefix: string): string {
    prefix = prefix || 'sc';
    return prefix + uniqueIdSeqNo++;
  },

  /**
   * Iterates through the given widgets and toggles the 'first' and 'last' classes on the corresponding widgets if the widgets are visible and rendered.
   */
  updateFirstLastMarker(widgets: Widget[]) {
    widgets.filter((widget, i, widgets) => {
      return widget.rendered && widget.isVisible();
    }).forEach((widget, i, widgets) => {
      widget.$container.toggleClass('first', i === 0);
      widget.$container.toggleClass('last', i === widgets.length - 1);
    });
  },

  /**
   * @param widgets the widgets to check.
   * @param container if specified, the function returns null if the container is not visible or not rendered. This allows for an early return without the need to check every given widget.
   * @param checkTabbable if true, the resulting widget has to be tabbable, not only focusable. Default is true.
   * @returns the first widget of the given list which is focusable (and tabbable, unless checkTabbable is set to false).
   */
  findFirstFocusableWidget(widgets: Widget[], container?: Widget, checkTabbable?: boolean): Widget {
    if (container && (!container.rendered || !container.visible)) {
      return null;
    }
    return arrays.find(widgets, widget => widget.isFocusable(checkTabbable));
  },

  /**
   * Sets a property using the given setter after reading the value using the getter and preserving it on the preserver using the preserverName.
   * The preserved property can be reset using {@link resetProperty}.
   */
  preserveAndSetProperty(setter: () => void, getter: () => any, preserver: object, preserverName: string) {
    if (preserver[preserverName] === null) {
      preserver[preserverName] = getter();
    }
    setter();
  },

  /**
   * Resets a property that has been preserved on the preserver by {@link preserveAndSetProperty} using the given setter. Sets the preserved property to null afterwards.
   * @param setter
   * @param preserver
   * @param preserverName
   */
  resetProperty(setter: (value) => void, preserver: object, preserverName: string) {
    if (preserver[preserverName] != null) {
      setter(preserver[preserverName]);
      preserver[preserverName] = null;
    }
  }
};
