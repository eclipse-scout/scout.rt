scout.DatePickerTouchPopupLayout = function(popup) {
  scout.DatePickerTouchPopupLayout.parent.call(this, popup);
};
scout.inherits(scout.DatePickerTouchPopupLayout, scout.PopupLayout);

scout.DatePickerTouchPopupLayout.prototype.layout = function($container) {
  scout.DatePickerTouchPopupLayout.parent.prototype.layout.call(this, $container);

  var dateField = this.popup._dateField,
    sfLeftMargin = 4,
    sfRightMargin = 6,
    sfTopMargin = 4,
    sfBottomMargin = 5,
    popupSize = this.popup.htmlComp.getSize(),
    sfHeight = dateField.htmlComp.getPreferredSize().height,
    sfWidth = popupSize.width - sfLeftMargin - sfRightMargin,
    datePickerVOffset = sfTopMargin + sfHeight + sfBottomMargin;

  dateField.htmlComp.setBounds(new scout.Rectangle(sfLeftMargin, sfTopMargin, sfWidth, sfHeight));
  this.popup.datePickerContainerHtmlComp.setBounds(new scout.Rectangle(0, datePickerVOffset, popupSize.width, popupSize.height - datePickerVOffset));
};

/**
 * @override AbstractLayout.js
 */
scout.DatePickerTouchPopupLayout.prototype.preferredLayoutSize = function($container) {
  var screenWidth = $(document).width(),
    screenHeight = $(document).height(),
    minPopupWidth = scout.HtmlEnvironment.formColumnWidth / 2,
    maxPopupHeight = scout.HtmlEnvironment.formRowHeight * 15,
    popupWidth = scout.HtmlEnvironment.formColumnWidth,
    popupHeight = screenHeight / 2 - scout.SmartFieldTouchPopup.TOP_MARGIN;

  popupWidth = Math.max(popupWidth, minPopupWidth);
  popupHeight = Math.min(popupHeight, maxPopupHeight);

  return new scout.Dimension(popupWidth, popupHeight);
};
