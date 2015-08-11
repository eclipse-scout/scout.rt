/**
 * Utility to deal with valueFields
 */
scout.valueFieldUtils = function() {
};

scout.valueFieldUtils.acceptInputOnActiveField = function() {
  var activeValueField = $(document.activeElement).data('valuefield');
  if (activeValueField === undefined) {
    // try parent, some times the value field is the parent of the input field (e.g. DateField.js)
    activeValueField = $(document.activeElement).parent().data('valuefield');
  }
  if (activeValueField) {
    activeValueField.acceptInput();

  }
};
