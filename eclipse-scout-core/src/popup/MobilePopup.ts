/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, HtmlComponent, icons, MobilePopupLayout, MobilePopupModel, Point, PopupAlignment, PopupLayout, scout, WidgetPopup} from '../index';

export class MobilePopup extends WidgetPopup implements MobilePopupModel {
  declare model: MobilePopupModel;

  title: string;
  $header: JQuery;
  $title: JQuery;

  constructor() {
    super();
    this.boundToAnchor = false;
    this.closable = true;
    this.title = null;
    this.withGlassPane = true;
  }

  protected override _createLayout(): PopupLayout {
    return new MobilePopupLayout(this);
  }

  protected override _createCloseAction(): Action {
    return scout.create(Action, {
      parent: this,
      cssClass: 'closer',
      iconId: icons.REMOVE_BOLD
    });
  }

  override prefLocation(verticalAlignment?: PopupAlignment, horizontalAlignment?: PopupAlignment): Point {
    let popupSize = this.htmlComp.prefSize(),
      windowHeight = this.$container.window().height(),
      y = Math.max(windowHeight - popupSize.height, 0);
    return new Point(0, y);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('popup mobile-popup');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.validateRoot = true;
    this.htmlComp.setLayout(this._createLayout());

    this.$header = this.$container.appendDiv('mobile-popup-header');
    this.$title = this.$header.appendDiv('title');
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderTitle();
  }

  protected override _renderContent() {
    super._renderContent();
    if (!this.content) {
      return;
    }
    this.content.$container.addClass('mobile-popup-widget');
  }

  protected override _renderClosable() {
    if (this.closeAction) {
      this.closeAction.render(this.$header);
    }
    this.$header.setVisible(!!this.title || this.closable);
  }

  protected _renderTitle() {
    this.$title.textOrNbsp(this.title);
    this.$header.setVisible(!!this.title || this.closable);
  }
}
