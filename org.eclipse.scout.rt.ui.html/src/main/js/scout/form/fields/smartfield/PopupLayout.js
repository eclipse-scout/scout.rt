/**
 * The popup layout is different from other layouts, since it can determine its own size
 * when the autoSize flag is set to true. Otherwise it uses the given size, like a regular
 * layout. The autoSize feature is used, when a child of the PopupLayout invalidates the
 * tree up to the popup. Since the popup is a validate root it must re-layout itself.
 * However: the size of the popup dependes on the field it belongs to. That's why we
 * can inject a function adjustAutoSize() into the PopupLayout, to provide the bounds
 * of the field, when layout is re-calculated.
 *
 *  The proposal-chooser DIV is not always present.
 */
scout.PopupLayout = function(htmlPopup) {
  scout.PopupLayout.parent.call(this);
  this._htmlPopup = htmlPopup;
  this.autoSize = true;
};
scout.inherits(scout.PopupLayout, scout.AbstractLayout);

scout.PopupLayout.prototype.layout = function($container) {
  var size, prefSize, popupSize,
    htmlProposalChooser = this._htmlProposalChooser($container);
  if (this.autoSize) {
    prefSize = this.preferredLayoutSize($container);
    popupSize = this.adjustAutoSize(prefSize);
    // don't use setSize() here because this would invalidate the popup and trigger layout() again
    scout.graphics.setSize(this._htmlPopup.$comp, popupSize);
  } else {
    popupSize = this._htmlPopup.getSize();
  }

  if (htmlProposalChooser) {
    size = popupSize.subtract(this._htmlPopup.getInsets());
    htmlProposalChooser.setSize(size);
  }
};

/**
 * Gives a chance to override the default preferred size of the popup when autoSize is enabled.
 * The default implementation simply returns the given prefSize.
 */
scout.PopupLayout.prototype.adjustAutoSize = function(prefSize) {
  return prefSize;
};

scout.PopupLayout.prototype.preferredLayoutSize = function($container) {
  var htmlProposalChooser = this._htmlProposalChooser($container),
    prefSize;
  if (htmlProposalChooser) {
    prefSize = htmlProposalChooser.getPreferredSize();
  } else {
    prefSize = new scout.Dimension(
      scout.HtmlEnvironment.formColumnWidth,
      scout.HtmlEnvironment.formRowHeight * 2);
  }
  return prefSize.add(this._htmlPopup.getInsets());
};

scout.PopupLayout.prototype._htmlProposalChooser = function($container) {
  return scout.HtmlComponent.optGet($container.children('.proposal-chooser'));
};

