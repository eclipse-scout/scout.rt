/**
 * Utility methods for focus.
 */
scout.focusUtils = function() {
};

/**
 * Returns whether the given element is focusable by mouse.
 */
scout.focusUtils.isFocusableByMouse = function(element) {
  var $element = $(element);
  return !$element.hasClass('unfocusable') && !$element.closest('.unfocusable').length;
};

/**
 * Returns whether the given element has a parent which is focusable by mouse.
 */
scout.focusUtils.containsParentFocusableByMouse = function(element, entryPoint) {
  return $(element)
            .parents(':focusable')
            .not(entryPoint) // Exclude $entryPoint as all elements are its descendants. However, the $entryPoint is only focusable to provide Portlet support.
            .filter(function() {
              return scout.focusUtils.isFocusableByMouse(this);
            })
            .length > 0;
};

/**
 * Returns whether the given element contains content which is selectable to the user, e.g. to be copied into clipboard.
 * It also returns true for disabled text-fields, because the user must be able to select and copy text from these text-fields.
 */
scout.focusUtils.isSelectableText = function(element) {
  var $element = $(element);

  if ($element.is('input[disabled][type=text]')) {
    return true;
  }
  return $element
      .clone()
      .children()
      .remove()
      .end()
      .text().trim().length && $element.css('user-select') !== 'none';
};
