/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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

    if ($element.css('user-select') === 'none') {
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
    if (element instanceof jQuery) {
      activeElement = element.getActiveElement();
      element = element[0];
    } else {
      activeElement = element.ownerDocument.activeElement;
    }
    return activeElement === element;
  }

};
