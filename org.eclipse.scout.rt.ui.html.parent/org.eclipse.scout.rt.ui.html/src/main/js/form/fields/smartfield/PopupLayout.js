/**
 * The proposal-chooser DIV is not always present.
 */
scout.PopupLayout = function(htmlPopup) {
  scout.PopupLayout.parent.call(this);
  this._htmlPopup = htmlPopup;
  this.autoSize = true;
};
scout.inherits(scout.PopupLayout, scout.AbstractLayout);

scout.PopupLayout.prototype.layout = function($container) {
  $.log.info('PopupLayout#layout autoSize=' + this.autoSize);

  // FIXME AWE: (smart-field) ausprobieren, ob es eine gute idee ist, dass das layout seinen
  // container selber resizen darf (normalerweise macht das der parent mit setSize()).
  // verträgt sich einfach nicht so gut, wenn dann jemand wirklich die size vom popup ändern will
  // z.B. beim resize, ausserdem sind teile der popup grösse von aussen gesteuert, z.B. die field bounds

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
    $.log.info('set propal-chooser size to ' + popupSize);
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

