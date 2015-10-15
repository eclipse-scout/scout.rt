scout.SmartFieldMobilePopupLayout = function(popup) {
  scout.SmartFieldMobilePopupLayout.parent.call(this, popup);
};
scout.inherits(scout.SmartFieldMobilePopupLayout, scout.PopupLayout);

scout.SmartFieldMobilePopupLayout.prototype.layout = function($container) {
  scout.SmartFieldMobilePopupLayout.parent.prototype.layout.call(this, $container);

  var smartField = this.popup._smartField,
    sfLeftMargin = 4,
    sfRightMargin = 6,
    sfTopMargin = 4,
    sfBottomMargin = 5,
    popupSize = this.popup.htmlComp.getSize(),
    sfHeight = smartField.htmlComp.getPreferredSize().height,
    sfWidth = popupSize.width - sfLeftMargin - sfRightMargin,
    proposalChooserVOffset = sfTopMargin + sfHeight + sfBottomMargin;

  smartField.htmlComp.setBounds(new scout.Rectangle(sfLeftMargin, sfTopMargin, sfWidth, sfHeight));
  this.popup.proposalChooserContainerHtmlComp.setBounds(new scout.Rectangle(0, proposalChooserVOffset, popupSize.width, popupSize.height - proposalChooserVOffset));
};

/**
 * @override AbstractLayout.js
 */
scout.SmartFieldMobilePopupLayout.prototype.preferredLayoutSize = function($container) {
  var screenWidth = $(document).width(),
    screenHeight = $(document).height(),
    minPopupWidth = scout.HtmlEnvironment.formColumnWidth / 2,
    maxPopupHeight = scout.HtmlEnvironment.formRowHeight * 15,
    popupWidth = scout.HtmlEnvironment.formColumnWidth,
    popupHeight = screenHeight / 2 - scout.SmartFieldMobilePopup.TOP_MARGIN;

  popupWidth = Math.max(popupWidth, minPopupWidth);
  popupHeight = Math.min(popupHeight, maxPopupHeight);

  return new scout.Dimension(popupWidth, popupHeight);
};
