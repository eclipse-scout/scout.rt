/**
 * SmartFieldLayout works like FormLayout but additionally layouts its proposal-chooser popup.
 */
scout.SmartFieldLayout = function(smartField) {
  scout.SmartFieldLayout.parent.call(this, smartField);
  this._smartField = smartField;
};
scout.inherits(scout.SmartFieldLayout, scout.FormFieldLayout);

scout.SmartFieldLayout.prototype.layout = function($container) {
  scout.SmartFieldLayout.parent.prototype.layout.call(this, $container);

  // when embedded smart-field layout must not validate the popup
  // since this would lead to an endless recursion because the smart-field
  // is a child of the popup.
  if (this._smartField.embedded) {
    return;
  }

  var popup = this._smartField._popup;
  if (popup && popup.rendered) {
    // Make sure the popup is correctly layouted and positioned
    popup.position();
    popup.validateLayout();
  }
};


/**
 * Layout for icon in multiline smart-field works a bit different because the icon here is _inside_
 * an additional field container, which contains the INPUT field and the icon.
 *
 * @override FormFieldLayout.js
 */
scout.SmartFieldLayout.prototype._layoutIcon = function(formField, right, top) {
  var multiline = formField instanceof scout.SmartFieldMultiline;
  formField.$icon
    .cssRight(formField.$field.cssBorderRightWidth() + (multiline ? 0 : right))
    .cssTop(top);
};
