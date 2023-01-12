/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import $ from 'jquery';

/**
 * This file extends jQuery with custom selectors required in Scout.
 * Part of this file is copied with some modifications from jQuery UI.
 */
function focusable(element, isTabIndexNotNaN) {
  let nodeName = element.nodeName.toLowerCase();
  return (/input|select|textarea|button|object/.test(nodeName) ?
    !element.disabled :
    'a' === nodeName ?
      element.href || isTabIndexNotNaN :
      isTabIndexNotNaN) &&
    // the element and all of its ancestors must be visible
    visible(element);
}

function visible(element) {
  return $.expr.filters.visible(element) &&
    !$(element).parents().addBack().filter(function() {
      return $.css(this, 'visibility') === 'hidden';
    }).length;
}

$.extend($.expr[':'], {

  focusable: element => focusable(element, !isNaN($.attr(element, 'tabindex'))),

  tabbable: element => {
    let tabIndex = $.attr(element, 'tabindex'),
      isTabIndexNaN = isNaN(tabIndex);
    return (isTabIndexNaN || tabIndex >= 0) && focusable(element, !isTabIndexNaN);
  }
});
