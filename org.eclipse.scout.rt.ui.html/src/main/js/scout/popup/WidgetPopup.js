/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.WidgetPopup = function() {
  scout.WidgetPopup.parent.call(this);
  this.animateOpening = true;
  this.animateResize = true;
  this.animateRemoval = true;
  this.widget = null;
  this.windowPaddingX = 0;
  this.windowPaddingY = 0;
  this.windowResizeType = 'layoutAndPosition';
  this._addWidgetProperties(['widget']);
};
scout.inherits(scout.WidgetPopup, scout.Popup);

scout.WidgetPopup.prototype._createLayout = function() {
  return new scout.WidgetPopupLayout(this);
};

scout.WidgetPopup.prototype._render = function() {
  scout.WidgetPopup.parent.prototype._render.call(this);
  this.$container.addClass('widget-popup');
};

scout.WidgetPopup.prototype._renderProperties = function() {
  scout.WidgetPopup.parent.prototype._renderProperties.call(this);
  this._renderWidget();
};

scout.WidgetPopup.prototype.setWidget = function(widget) {
  this.setProperty('widget', widget);
};

scout.WidgetPopup.prototype._renderWidget = function() {
  if (!this.widget) {
    return;
  }
  this.widget.render();
  this.widget.$container.addClass('popup-widget');
  this.invalidateLayoutTree();
};
