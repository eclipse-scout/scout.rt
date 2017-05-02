scout.SmartField2PopupLayout = function(popup, childWidget) {
  scout.SmartField2PopupLayout.parent.call(this, popup, childWidget);
  this.childWidget = childWidget;
};
scout.inherits(scout.SmartField2PopupLayout, scout.SinglePopupLayout);

scout.SmartField2PopupLayout.prototype.layout = function($container) {
  scout.SmartField2PopupLayout.parent.prototype.layout.call(this, $container);

  // The first time it gets layouted, add shown class to be able to animate
  if (!this.popup.htmlComp.layouted) {
    this.popup.htmlComp.$comp.addClassForAnimation('animate-open');
  }
};

scout.SmartField2PopupLayout.prototype.preferredLayoutSize = function($container) {
  var prefSize = scout.SmartField2PopupLayout.parent.prototype.preferredLayoutSize.call(this, $container);

  // Popup is as width as the anchor
  prefSize.width = this.popup.$anchor.outerWidth();
  // table has a top margin of -1 which is not taken into account by pref size
  // -> make popup 1px smaller to prevent 1px white gap at the bottom
  prefSize.height = prefSize.height - 1;
  return prefSize;
};
