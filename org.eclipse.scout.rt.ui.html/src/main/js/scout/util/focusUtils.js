/**
 * Utility methods for focus.
 */
scout.focusUtils = {

  /**
   * Returns whether the given element is focusable by mouse.
   */
  isFocusableByMouse: function(element) {
    var $element = $(element);
    return !$element.hasClass('unfocusable') && !$element.closest('.unfocusable').length;
  },

  /**
   * Returns whether the given element has a parent which is focusable by mouse.
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
   * Returns whether the given element contains content which is selectable to the user, e.g. to be copied into clipboard.
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
  }

};
