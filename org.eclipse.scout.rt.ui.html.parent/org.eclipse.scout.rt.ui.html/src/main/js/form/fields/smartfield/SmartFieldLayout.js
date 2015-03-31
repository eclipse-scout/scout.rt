scout.SmartFieldLayout = function(smartField) {
  scout.SmartFieldLayout.parent.call(this, smartField);
  this._smartField = smartField;
};
scout.inherits(scout.SmartFieldLayout, scout.FormFieldLayout);

scout.SmartFieldLayout.prototype.layout = function($container) {
  scout.SmartFieldLayout.parent.prototype.layout.call(this, $container);

  if (this._smartField._$popup) {
    var htmlPopup = scout.HtmlComponent.get(this._smartField._$popup),
      fieldBounds = this._smartField._getInputBounds(),
      popupLayout = htmlPopup.layoutManager;

    popupLayout.autoSize = true;
    popupLayout.adjustAutoSize = function(prefSize) {
      return new scout.Dimension(
          Math.max(fieldBounds.width, prefSize.width),
          Math.min(350, prefSize.height));
    };

    htmlPopup.setBounds(new scout.Rectangle(
        fieldBounds.x,
        fieldBounds.y + fieldBounds.height,
        fieldBounds.width,
        scout.HtmlEnvironment.formRowHeight * 2));
  }
}
