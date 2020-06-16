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
import {HtmlComponent, icons, MobilePopupLayout, Point, scout, WidgetPopup} from '../index';

export default class MobilePopup extends WidgetPopup {

  constructor() {
    super();
    this.boundToAnchor = false;
    this.closable = true;
    this.title = null;
    this.withGlassPane = true;
  }

  _createLayout() {
    return new MobilePopupLayout(this);
  }

  _createCloseAction() {
    return scout.create('Action', {
      parent: this,
      cssClass: 'closer',
      iconId: icons.REMOVE_BOLD
    });
  }

  prefLocation(verticalAlignment, horizontalAlignment) {
    let popupSize = this.htmlComp.prefSize(),
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
    this._renderTitle();
  }

  _renderWidget() {
    super._renderWidget();
    if (!this.widget) {
      return;
    }
    this.widget.$container.addClass('mobile-popup-widget');
  }

  _renderClosable() {
    if (this.closeAction) {
      this.closeAction.render(this.$header);
    }
    this.$header.setVisible(this.title || this.closable);
  }

  _renderTitle() {
    this.$title.textOrNbsp(this.title);
    this.$header.setVisible(this.title || this.closable);
  }
}
