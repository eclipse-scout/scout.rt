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
import {HtmlComponent, MenuBoxLayout, Widget} from '../../index';

export default class MenuBox extends Widget {

  constructor(menuBar) {
    super();
    this.compact = false;
    this.menus = [];
    this._addWidgetProperties('menus');
  }

  _init(options) {
    super._init(options);
    this.menus = options.menus || [];
    this.uiMenuCssClass = options.uiMenuCssClass || '';
    this.uiMenuCssClass += ' ' + 'menu-box-item';
    this._initMenus(this.menus);
  }

  _initMenus(menus) {
    menus.forEach(this._initMenu.bind(this));
  }

  _initMenu(menu) {
    menu.uiCssClass = this.uiMenuCssClass;
  }

  /**
   * @override Widget.js
   */
  _render() {
    this.$container = this.$parent.appendDiv('menu-box');

    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new MenuBoxLayout(this));
  }

  _renderProperties() {
    super._renderProperties();
    this._renderMenus();
    this._renderCompact();
  }

  setMenus(menus) {
    this.setProperty('menus', menus);
  }

  _renderMenus() {
    this.menus.forEach(menu => {
      menu.render();
    }, this);
    this.invalidateLayoutTree();
  }

  _removeMenus() {
    this.menus.forEach(menu => {
      menu.remove();
    });
    this.invalidateLayoutTree();
  }

  setCompact(compact) {
    this.setProperty('compact', compact);
  }

  _renderCompact() {
    this.$container.toggleClass('compact', this.compact);
    this.invalidateLayoutTree();
  }
}
