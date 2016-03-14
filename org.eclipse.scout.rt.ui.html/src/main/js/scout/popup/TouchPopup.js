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
scout.TouchPopup = function() {
  scout.TouchPopup.parent.call(this);

  // the original touch field from the form
  this._touchField;
  // the cloned field from the popup
  this._field;
  // the widget placed below the field
  this._widget;
  this._$widgetContainer;
  this._widgetContainerHtmlComp;

};
scout.inherits(scout.TouchPopup, scout.Popup);

scout.TouchPopup.TOP_MARGIN = 45;

scout.TouchPopup.prototype._init = function(options) {
  scout.TouchPopup.parent.prototype._init.call(this, options);
  this._touchField = options.field;

  // clone original touch field
  // original and clone both point to the same _popup instance
  this._field = this._touchField.cloneAdapter({
    parent: this,
    popup: this,
    labelPosition: scout.FormField.LABEL_POSITION_ON_FIELD,
    statusVisible: false,
    embedded: true,
    touch: false
  });

  this._initWidget(options);
};

scout.TouchPopup.prototype._initWidget = function(options) {
  // NOP
};

scout.TouchPopup.prototype._createLayout = function() {
  return new scout.TouchPopupLayout(this);
};

/**
 * @override Popup.js
 */
scout.TouchPopup.prototype.prefLocation = function($container, openingDirectionY) {
  var popupSize = this.htmlComp.getPreferredSize(),
    windowWidth = $container.window().width(),
    x = Math.max(this.windowPaddingX, (windowWidth - popupSize.width) / 2);
  return new scout.Point(x, scout.TouchPopup.TOP_MARGIN);
};

scout.TouchPopup.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('touch-popup');
    //    .on('mousedown', this._onContainerMouseDown.bind(this)) // FIXME awe: (popups) is this line required?

  this._$widgetContainer = this.$container.appendDiv('widget-container');
  this._widgetContainerHtmlComp = new scout.HtmlComponent(this._$widgetContainer, this.session);
  this._widgetContainerHtmlComp.setLayout(new scout.SingleLayout());

  // field may render something into the widget container -> render after widget container and move to correct place
  this._field.render(this.$container);

  // Move to top
  this._field.$container.insertBefore(this._$widgetContainer);

  if (this._widget) {
    this._widget.render(this._$widgetContainer);
  }

  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(this._createLayout());
};
