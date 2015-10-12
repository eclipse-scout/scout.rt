scout.SmartFieldMobilePopupLayout = function(popup) {
  scout.SmartFieldMobilePopupLayout.parent.call(this);
  this._popup = popup;
};
scout.inherits(scout.SmartFieldMobilePopupLayout, scout.AbstractLayout);

scout.SmartFieldMobilePopupLayout.prototype.layout = function($container) {
  var popup = this._popup,
    smartField = popup._smartField,
    sfLeftMargin = 4,
    sfRightMargin = 6,
    sfTopMargin = 4,
    sfBottomMargin = 5,
    popupSize = this._popup.htmlComp.getSize(),
    sfHeight = popup._smartField.htmlComp.getPreferredSize().height,
    sfWidth = popupSize.width - sfLeftMargin - sfRightMargin,
    proposalChooserVOffset = sfTopMargin + sfHeight + sfBottomMargin;

  popup._smartField.htmlComp.setBounds(new scout.Rectangle(sfLeftMargin, sfTopMargin, sfWidth, sfHeight));
  popup.proposalChooserContainerHtmlComp.setBounds(new scout.Rectangle(0, proposalChooserVOffset, popupSize.width, popupSize.height - proposalChooserVOffset));
};

scout.SmartFieldMobilePopupLayout.prototype.preferredLayoutSize = function($container) {
  return new scout.Dimension(400, 300);
};
