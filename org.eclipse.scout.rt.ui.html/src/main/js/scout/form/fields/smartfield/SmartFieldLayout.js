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
  if (this._smartField._$popup) {
    var htmlPopup = scout.HtmlComponent.get(this._smartField._$popup),
      // Because PopupLayout has autoSize set to true, the size we pass here is not relevant
      // the popup resizes itself when layout() is called. However: we must set the location
      // of the popup.
      prefSize = new scout.Dimension(0, 0),
      fieldBounds = this._smartField._fieldBounds(),
      popupBounds = this._smartField._popupBounds(fieldBounds, prefSize);
    htmlPopup.setBounds(popupBounds);
  }
};
