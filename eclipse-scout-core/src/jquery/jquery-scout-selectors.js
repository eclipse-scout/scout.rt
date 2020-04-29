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
