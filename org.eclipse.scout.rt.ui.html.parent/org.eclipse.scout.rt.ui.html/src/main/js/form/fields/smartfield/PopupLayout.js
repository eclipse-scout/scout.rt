/**
 * The proposal-chooser DIV is not always present.
 */
scout.PopupLayout = function(htmlPopup, onInvalidate) {
  scout.PopupLayout.parent.call(this);
  this._htmlPopup = htmlPopup;
  this._onInvalidate = onInvalidate;
  this._lock = false;
};
scout.inherits(scout.PopupLayout, scout.AbstractLayout);

scout.PopupLayout.prototype.layout = function($container) {
  $.log.info('PopupLayout#layout');
  var htmlProposalChooser = this._htmlProposalChooser($container),
    size = this._htmlPopup.getSize().subtract(this._htmlPopup.getInsets());
  if (htmlProposalChooser) {
    htmlProposalChooser.setSize(size);
  }
};

scout.PopupLayout.prototype.preferredLayoutSize = function($container) {
  var htmlProposalChooser = this._htmlProposalChooser($container),
    prefSize;
  if (htmlProposalChooser) {
    prefSize = htmlProposalChooser.getPreferredSize();
  } else {
    prefSize = new scout.Dimension(
      scout.HtmlEnvironment.formColumnWidth,
      scout.HtmlEnvironment.formRowHeight * 3);
  }
  return prefSize.add(this._htmlPopup.getInsets());
};

scout.PopupLayout.prototype._htmlProposalChooser = function($container) {
  return scout.HtmlComponent.optGet($container.children('.proposal-chooser'));
};

scout.PopupLayout.prototype.invalidate = function() {
  // FIXME AWE: ausprobieren ob es ohne lock geht und stattdessen im layout preferredSize
  // immer verwendet wird UND den eigenen container resized (was sonst nicht passiert).
  if (this._lock) {
    return;
  }
  this._lock = true;
  try {
    this._onInvalidate();
  } finally {
    this._lock = false;
  }
};
