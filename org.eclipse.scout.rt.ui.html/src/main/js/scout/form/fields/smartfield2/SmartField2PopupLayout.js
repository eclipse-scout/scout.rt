/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * The popup layout is different from other layouts, since it can determine its own size
 * when the autoSize flag is set to true. Otherwise it uses the given size, like a regular
 * layout. The autoSize feature is used, when a child of the SmartFieldPopupLayout invalidates the
 * tree up to the popup. Since the popup is a validate root it must re-layout itself.
 * However: the size of the popup dependes on the field it belongs to.
 *
 *  The proposal-chooser DIV is not always present.
 */
scout.SmartField2PopupLayout = function(popup) {
  scout.SmartField2PopupLayout.parent.call(this, popup);

  this.animating = false;
};
scout.inherits(scout.SmartField2PopupLayout, scout.PopupLayout);

scout.SmartField2PopupLayout.prototype.layout = function($container) {
  var size, popupSize,
    htmlProposalChooser = this._htmlProposalChooser();

  // skip layout while CSS animation is running
  if (this.animating) {
    return;
  }

  scout.SmartField2PopupLayout.parent.prototype.layout.call(this, $container);

  popupSize = this.popup.htmlComp.getSize();
  size = popupSize.subtract(this.popup.htmlComp.getInsets());
  htmlProposalChooser.setSize(size);

  if (this.popup.htmlComp.layouted) {
    // Reposition because opening direction may have to be switched if popup gets bigger
    // Don't do it the first time (will be done by popup.open), only if the popup is already open and gets layouted again
    this.popup.position();
  } else {
    // The first time it gets layouted, add CSS class to be able to animate
    this.animating = true;
    this.popup.htmlComp.$comp.oneAnimationEnd(function() {
      this.animating = false;
    }.bind(this));
    this.popup.htmlComp.$comp.addClassForAnimation('animate-open');
  }
};

/**
 * @override AbstractLayout.js
 */
scout.SmartField2PopupLayout.prototype.preferredLayoutSize = function($container) {
  var prefSize,
    htmlProposalChooser = this._htmlProposalChooser(),
    fieldBounds = scout.graphics.offsetBounds(this.popup.smartField.$field);

  if (htmlProposalChooser) {
    prefSize = htmlProposalChooser.getPreferredSize();
    prefSize = prefSize.add(this.popup.htmlComp.getInsets());
  } else {
    prefSize = new scout.Dimension(
      scout.HtmlEnvironment.formColumnWidth,
      scout.HtmlEnvironment.formRowHeight * 2);
  }

  prefSize.width = Math.max(fieldBounds.width, prefSize.width);
  prefSize.height = Math.max(15, Math.min(350, prefSize.height)); // at least some pixels height in case there is no data, no status, no active filter

  if (prefSize.width > this._maxWindowSize()) {
    prefSize.width = this._maxWindowSize();
  }

  return prefSize;
};

scout.SmartField2PopupLayout.prototype._htmlProposalChooser = function() {
  var proposalChooser = this.popup.proposalChooser;
  if (!proposalChooser) {
    return null;
  }
  return proposalChooser.htmlComp;
};

scout.SmartField2PopupLayout.prototype._maxWindowSize = function() {
  return this.popup.$container.window().width() - (2 * this.popup.windowPaddingX);
};
