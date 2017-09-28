/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * Utility methods for focus.
 */
scout.focusUtils = {

  /**
   * @return whether the given element is focusable by mouse.
   * @memberOf scout.focusUtils
   */
  isFocusableByMouse: function(element) {
    var $element = $(element);
    return !$element.hasClass('unfocusable') && !$element.closest('.unfocusable').length;
  },

  /**
   * @return whether the given element has a parent which is focusable by mouse.
   */
  containsParentFocusableByMouse: function(element, entryPoint) {
    var $focusableParentElements = $(element)
      .parents(':focusable')
      .not(entryPoint) /* Exclude $entryPoint as all elements are its descendants. However, the $entryPoint is only focusable to provide Portlet support. */
      .filter(function() {
        return scout.focusUtils.isFocusableByMouse(this);
      });
    return ($focusableParentElements.length > 0);
  },

  /**
   * @return whether the given element contains content which is selectable to the user, e.g. to be copied into clipboard.
   * It also returns true for disabled text-fields, because the user must be able to select and copy text from these text-fields.
   */
  isSelectableText: function(element) {
    var $element = $(element);

    // Find closest element which has a 'user-select' with a value other than 'auto'. If that value
    // is 'none', the text is not selectable. This code mimics the "inheritance behavior" of the CSS
    // property "-moz-user-select: -moz-none" as described in [1].  This does not seem to work in some
    // cases in Firefox, even with bug [2] fixed. As a workaround, we implement the desired behavior here.
    //
    // Note: Some additional CSS rules are required for events other than 'mousedown', see main.css.
    //
    // [1] https://developer.mozilla.org/en-US/docs/Web/CSS/user-select
    // [2] https://bugzilla.mozilla.org/show_bug.cgi?id=648624
    var $el = $element;
    while ($el.css('user-select') === 'auto') {
      $el = $el.parent();
      // Fix for Firefox: parent of BODY element is HtmlDocument. When calling $el.css on the HtmlDocument
      // Firefox throws an error that ownerDocument is undefined. Thus we don't go higher than BODY element
      // and assume body is never selectable.
      if ($el.is('body')) {
        return false;
      }
    }
    if ($el.css('user-select') === 'none') {
      return false;
    }

    if ($element.is('input[disabled][type=text], textarea[disabled]')) {
      return true;
    }
    var text = $element
      .clone()
      .children()
      .remove()
      .end()
      .text()
      .trim();
    return (text.length > 0);
  },


  /**
   * Returns true if the given HTML element is the active element in its own document, false otherwise
   * @param element
   */
  isActiveElement: function(element) {
    if (!element) {
      return false;
    }
    var activeElement;
    if (element instanceof $) {
      activeElement = element.activeElement(true);
      element = element[0];
    } else {
      activeElement = (element instanceof Document ? element : element.ownerDocument).activeElement;
    }
    return activeElement === element;
  }

};
