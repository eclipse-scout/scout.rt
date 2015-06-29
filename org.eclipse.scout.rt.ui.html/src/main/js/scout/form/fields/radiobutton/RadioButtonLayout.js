/**
 * RadioButtonLayout. Sets the input behind the displayed Radio-Button ui-elemnt in label::before.
 */
scout.RadioButtonLayout = function(formField) {
  scout.RadioButtonLayout.parent.call(this);
  this.formField = formField;
};
scout.inherits(scout.RadioButtonLayout, scout.FormFieldLayout);

/**
 * Adds the width of the bubble to the natural with because before element is not included in calculation.
 * @Override FormFieldLayout.js
 * @param formField
 * @returns {scout.Dimension}
 */
scout.RadioButtonLayout.prototype.naturalSize = function(formField) {
  var radioButtonBubbleWidth = this.formField.$field.retriveBeforeElementCss('width');

  return new scout.Dimension(Number(formField.$fieldContainer.outerWidth(true))+Number(radioButtonBubbleWidth.replace('px','')), formField.$fieldContainer.outerHeight(true));
};
