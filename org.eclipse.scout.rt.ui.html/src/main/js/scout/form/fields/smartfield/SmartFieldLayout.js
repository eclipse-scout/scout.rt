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
  var popup = this._smartField._popup;
  if (popup && popup.rendered) {
    // Make sure the popup is correctly layouted and positioned
    popup.position();
    popup.validateLayout();
  }
};
