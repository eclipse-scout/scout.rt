/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Popup} from '../index';
import {HtmlComponent} from '../index';
import {Point} from '../index';
import {MobilePopupLayout} from '../index';

export default class MobilePopup extends Popup {

constructor() {
  super();
  this.animateOpening = true;
  this.boundToAnchor = false;
  this.windowPaddingX = 0;
  this.windowPaddingY = 0;
  this.closable = true;
  this.animateRemoval = true;
  this.widget = null;
  this.title = null;
  this.withGlassPane = true;
  this._addWidgetProperties('widget');
}


_createLayout() {
  return new MobilePopupLayout(this);
}

/**
 * @override Popup.js
 */
prefLocation(verticalAlignment, horizontalAlignment) {
  var popupSize = this.htmlComp.prefSize(),
    windowHeight = this.$container.window().height(),
    y = Math.max(windowHeight - popupSize.height, 0);
  return new Point(0, y);
}

_render() {
  this.$container = this.$parent.appendDiv('popup mobile-popup');
  this.htmlComp = HtmlComponent.install(this.$container, this.session);
  this.htmlComp.validateRoot = true;
  this.htmlComp.setLayout(this._createLayout());

  this.$header = this.$container.appendDiv('mobile-popup-header');
  this.$title = this.$header.appendDiv('title');
}

_renderProperties() {
  super._renderProperties();
  this._renderWidget();
  this._renderTitle();
  this._renderClosable();
}

setWidget(widget) {
  this.setProperty('widget', widget);
}

_renderWidget() {
  if (!this.widget) {
    return;
  }
  this.widget.render();
  this.widget.$container.addClass('mobile-popup-widget');
  this.invalidateLayoutTree();
}

_renderClosable() {
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
}

_renderTitle() {
  this.$title.textOrNbsp(this.title);
}
}
