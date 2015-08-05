// TODO [dwi][nbu] Is this adapter really used? See DesktopViewButtonPopupKeyStroke
scout.ViewMenuPopupKeyStrokeAdapter = function(viewMenuPopup) {
  scout.ViewMenuPopupKeyStrokeAdapter.parent.call(this, viewMenuPopup);

  this.registerKeyStroke(new scout.MenuControlKeyStrokes(viewMenuPopup, 'view-button-menu'));
  var closeKeyStroke = new scout.PopupCloseKeyStroke(viewMenuPopup);
  closeKeyStroke.keyStroke = 'F2';
  closeKeyStroke.initKeyStrokeParts();
  this.registerKeyStroke(closeKeyStroke);
};
scout.inherits(scout.ViewMenuPopupKeyStrokeAdapter, scout.PopupKeyStrokeAdapter);
