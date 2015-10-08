scout.SmartFieldMobilePopupLayout = function(popup) {
  scout.SmartFieldMobilePopupLayout.parent.call(this);
  this._popup = popup;
};
scout.inherits(scout.SmartFieldMobilePopupLayout, scout.AbstractLayout);

scout.SmartFieldMobilePopupLayout.prototype.layout = function($container) {
  var popupSize = this._popup.htmlComp.getSize(),
    popup = this._popup,
    smartFieldHeight = popup._smartField.htmlComp.getPreferredSize().height,
    proposalChooserVOffset = smartFieldHeight + scout.HtmlEnvironment.formRowGap;

  popup._smartField.htmlComp.setBounds(new scout.Rectangle(0, 0, popupSize.width, smartFieldHeight));
  popup._proposalChooserHtmlComp.setBounds(new scout.Rectangle(0, proposalChooserVOffset, popupSize.width, popupSize.height - proposalChooserVOffset));
};

scout.SmartFieldMobilePopupLayout.prototype.preferredLayoutSize = function($container) {
  return new scout.Dimension(400, 300);
};
