/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Desktop, DesktopLogoEventMap, DesktopLogoModel, EventHandler, HtmlComponent, Image, InitModelOf, PropertyChangeEvent, scout, Widget} from '../index';

export class DesktopLogo extends Widget implements DesktopLogoModel {
  declare model: DesktopLogoModel;
  declare eventMap: DesktopLogoEventMap;
  declare self: DesktopLogo;

  desktop: Desktop;
  clickable: boolean;
  url: string;
  image: Image;
  protected _desktopPropertyChangeHandler: EventHandler<PropertyChangeEvent<any, Desktop>>;
  protected _clickHandler: (event: JQuery.ClickEvent) => void;

  constructor() {
    super();
    this.desktop = null;
    this.clickable = false;
    this.image = null;
    this._desktopPropertyChangeHandler = this._onDesktopPropertyChange.bind(this);
    this._clickHandler = this._onClick.bind(this);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.desktop = this.session.desktop;
    this.clickable = this.desktop.logoActionEnabled;
    this.url = model.url;
    this.image = scout.create(Image, {
      parent: this,
      imageUrl: this.url
    });
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('desktop-logo');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.image.render();
    this.desktop.on('propertyChange', this._desktopPropertyChangeHandler);
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderClickable();
  }

  protected override _remove() {
    this.desktop.off('propertyChange', this._desktopPropertyChangeHandler);
    super._remove();
  }

  protected _renderClickable() {
    this.$container.toggleClass('clickable', this.clickable);
    if (this.clickable) {
      this.$container.on('click', this._clickHandler);
    } else {
      this.$container.off('click', this._clickHandler);
    }
  }

  setUrl(url: string) {
    this.setProperty('url', url);
    this.image.setImageUrl(url);
  }

  setClickable(clickable: boolean) {
    this.setProperty('clickable', clickable);
  }

  protected _onDesktopPropertyChange(event: PropertyChangeEvent<any, Desktop>) {
    if (event.propertyName === 'logoActionEnabled') {
      this.setClickable(event.newValue);
    }
  }

  protected _onClick(event: JQuery.ClickEvent) {
    this.desktop.logoAction();
  }
}
