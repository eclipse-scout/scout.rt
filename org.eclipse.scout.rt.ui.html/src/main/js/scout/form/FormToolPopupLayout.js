scout.FormToolPopupLayout = function(popup) {
  scout.FormToolPopupLayout.parent.call(this, popup);
  this.popup = popup;
};
scout.inherits(scout.FormToolPopupLayout, scout.PopupWithHeadLayout);

scout.FormToolPopupLayout.prototype.layout = function($container) {
  var popupSize,
    htmlForm = this.popup.form.htmlComp;

  scout.FormToolPopupLayout.parent.prototype.layout.call(this, $container);

  popupSize = scout.graphics.getSize(this.popup.$body);

  // set size of form
  popupSize = popupSize.subtract(scout.graphics.getInsets(this.popup.$body));
  htmlForm.setSize(popupSize);
};

scout.FormToolPopupLayout.prototype.preferredLayoutSize = function($container) {
  var htmlComp = this.popup.htmlComp,
    htmlForm = this.popup.form.htmlComp,
    prefSize;

  prefSize = htmlForm.getPreferredSize()
    .add(htmlComp.getInsets())
    .add(scout.graphics.getInsets(this.popup.$body, {includeMargin: true}))
    .add(htmlForm.getMargins());

  return prefSize;
};
