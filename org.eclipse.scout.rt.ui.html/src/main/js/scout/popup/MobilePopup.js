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
scout.MobilePopup = function() {
  scout.MobilePopup.parent.call(this);
  this.boundToAnchor = false;
  this.windowPaddingX = 0;
  this.windowPaddingY = 0;
  this.closable = true;
  this.animateRemoval = true;
  this.widget;
  this.title;
  this.withGlassPane = true;
  this._addWidgetProperties('widget');
};
scout.inherits(scout.MobilePopup, scout.Popup);

scout.MobilePopup.prototype._createLayout = function() {
  return new scout.MobilePopupLayout(this);
};

/**
 * @override Popup.js
 */
scout.MobilePopup.prototype.prefLocation = function(openingDirectionY) {
  var popupSize = this.htmlComp.prefSize(),
    windowHeight = this.$container.window().height(),
    y = Math.max(windowHeight - popupSize.height, 0);
  return new scout.Point(0, y);
};

scout.MobilePopup.prototype._render = function() {
  this.$container = this.$parent.appendDiv('popup mobile-popup');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.validateRoot = true;
  this.htmlComp.setLayout(this._createLayout());

  this.$header = this.$container.appendDiv('mobile-popup-header');
  this.$title = this.$header.appendDiv('title');
};

scout.MobilePopup.prototype._renderProperties = function() {
  scout.MobilePopup.parent.prototype._renderProperties.call(this);
  this._renderWidget();
  this._renderTitle();
  this._renderClosable();
};

scout.MobilePopup.prototype.setWidget = function(widget) {
  this.setProperty('widget', widget);
};

scout.MobilePopup.prototype._renderWidget = function() {
  if (!this.widget) {
    return;
  }
  this.widget.render();
  this.widget.htmlComp.pixelBasedSizing = true;
  this.widget.$container.addClass('mobile-popup-widget');
  this.invalidateLayoutTree();
};

scout.MobilePopup.prototype._renderClosable = function() {
  this.$container.toggleClass('closable');
  if (this.closable) {
    if (this.$close) {
      return;
    }
    this.$close = this.$title
      .afterDiv('closer')
      .on('click', this.close.bind(this));
  } else {
    if (!this.$close) {
      return;
    }
    this.$close.remove();
    this.$close = null;
  }
};

scout.MobilePopup.prototype._renderTitle = function() {
  this.$title.textOrNbsp(this.title);
};
