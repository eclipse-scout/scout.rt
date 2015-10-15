scout.DatePickerMobilePopupLayout = function(popup) {
  scout.DatePickerMobilePopupLayout.parent.call(this);
  this._popup = popup;
};
scout.inherits(scout.DatePickerMobilePopupLayout, scout.AbstractLayout);

scout.DatePickerMobilePopupLayout.prototype.layout = function($container) {
  var popup = this._popup,
    dateField = popup._dateField,
    sfLeftMargin = 4,
    sfRightMargin = 6,
    sfTopMargin = 4,
    sfBottomMargin = 5,
    popupSize = this._popup.htmlComp.getSize(),
    sfHeight = popup._dateField.htmlComp.getPreferredSize().height,
    sfWidth = popupSize.width - sfLeftMargin - sfRightMargin,
    datePickerVOffset = sfTopMargin + sfHeight + sfBottomMargin;

  popup._dateField.htmlComp.setBounds(new scout.Rectangle(sfLeftMargin, sfTopMargin, sfWidth, sfHeight));
  popup.datePickerContainerHtmlComp.setBounds(new scout.Rectangle(0, datePickerVOffset, popupSize.width, popupSize.height - datePickerVOffset));
};

scout.DatePickerMobilePopupLayout.prototype.preferredLayoutSize = function($container) {
  return new scout.Dimension(400, 300);
};
