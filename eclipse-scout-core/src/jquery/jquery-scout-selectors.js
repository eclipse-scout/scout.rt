/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import $ from 'jquery';

/*
 * This file extends jQuery with custom selectors required in Scout.
 * Part of this file is copied with some modifications from jQuery UI.
 */

function focusable(element, requireTabbable, excludeUnfocusable) {
  let tabIndex = Number($.attr(element, 'tabindex'));
  let hasTabIndex = !isNaN(tabIndex);

  // Elements with an explicit negative tabindex are never tabbable
  if (tabIndex < 0 && requireTabbable) {
    return false;
  }
  // SPECIAL CASE: we consider elements with tabindex="-2" to be _never_ focusable, not even programmatically!
  if (tabIndex === -2 && excludeUnfocusable) {
    return false;
  }

  // Some elements are focusable natively, others can be made focusable by adding a tabindex (positive or negative)
  let nodeName = element.nodeName.toLowerCase();
  let focusable = /^(input|select|textarea|button|object)$/.test(nodeName)
    ? !element.disabled
    : hasTabIndex || (nodeName === 'a' && element.href);

  return focusable && visible(element); // the element and all of its ancestors must be visible
}

function visible(element) {
  return $.expr.filters.visible(element) &&
    !$(element).parents().addBack().filter(function() {
      return $.css(this, 'visibility') === 'hidden';
    }).length;
}

// Register selectors
$.extend($.expr[':'], {
  'focusable-native': element => focusable(element, false),
  'focusable': element => focusable(element, false, true),
  'tabbable': element => focusable(element, true)
});
