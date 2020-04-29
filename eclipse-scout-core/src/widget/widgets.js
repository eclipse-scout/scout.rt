/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, Widget} from '../index';
import $ from 'jquery';

let uniqueIdSeqNo = 0;

/**
 * @param {(HTMLElement|$)} a HTML or jQuery element to find the corresponding widget for
 * @returns {Widget} the widget for the given element. If the element is not linked with a widget directly, it searches its ancestors for the widget.
 */
export function get($elem) {
  $elem = $.ensure($elem);
  while ($elem && $elem.length > 0) {
    let widget = $elem.data('widget');
    if (widget) {
      return widget;
    }
    $elem = $elem.parent();
  }
  return null;
}

/**
 * Creates a "unique" id which may be used in the id attribute of a HTML element.
 * <p>
 * It actually just increases a sequence number prefixed by 'sc' or the given prefix.
 * 'sc' (short for Scout) is added to reduce the possibility of a duplication
 * if scout widgets are embedded into a foreign web site which already uses numbers in its id attributes.
 * So it is not really unique but should be good enough when you know what you are doing.
 */
export function createUniqueId(prefix) {
  prefix = prefix || 'sc';
  return prefix + uniqueIdSeqNo++;
}

/**
 * Iterates through the given widgets and toggles the 'first' and 'last' classes on the corresponding widgets if the widgets are visible and rendered.
 */
export function updateFirstLastMarker(widgets) {
  widgets.filter((widget, i, widgets) => {
    return widget.rendered && widget.isVisible();
  }).forEach((widget, i, widgets) => {
    widget.$container.toggleClass('first', i === 0);
    widget.$container.toggleClass('last', i === widgets.length - 1);
  });
}

/**
 * @param {Widget[]} widgets the widgets to check.
 * @param {Widget} [container] if specified, the function returns false if the container is not visible or not rendered. This allows for an early return without the need to check every given widget.
 * @returns {boolean} the first widget of the given list which is focusable.
 */
export function findFirstFocusableWidget(widgets, container) {
  if (container && (!container.rendered || !container.visible)) {
    return false;
  }
  return arrays.find(widgets, widget => {
    return widget.isFocusable();
  });
}

export default {
  createUniqueId,
  findFirstFocusableWidget,
  get,
  uniqueIdSeqNo,
  updateFirstLastMarker
};
