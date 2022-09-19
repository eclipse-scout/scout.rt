/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {HtmlComponent, Menu, MenuBar, MenuBoxEventMap, MenuBoxLayout, MenuBoxModel, Widget} from '../../index';

export default class MenuBox extends Widget implements MenuBoxModel {
  declare model: MenuBoxModel;
  declare eventMap: MenuBoxEventMap;

  compact: boolean;
  compactOrig: boolean;
  menus: Menu[];
  uiMenuCssClass: string;

  constructor(menuBar: MenuBar) {
    super();
    this.compact = false;
    this.menus = [];
    this._addWidgetProperties('menus');
  }

  protected override _init(options: MenuBoxModel) {
    super._init(options);
    this.menus = options.menus || [];
    this.uiMenuCssClass = options.uiMenuCssClass || '';
    this.uiMenuCssClass += ' ' + 'menu-box-item';
    this._initMenus(this.menus);
  }

  protected _initMenus(menus: Menu[]) {
    menus.forEach(this._initMenu.bind(this));
  }

  protected _initMenu(menu: Menu) {
    menu.uiCssClass = this.uiMenuCssClass;
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('menu-box');

    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new MenuBoxLayout(this));
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderMenus();
    this._renderCompact();
  }

  setMenus(menus: Menu[]) {
    this.setProperty('menus', menus);
  }

  protected _renderMenus() {
    this.menus.forEach(menu => menu.render());
    this.invalidateLayoutTree();
  }

  protected _removeMenus() {
    this.menus.forEach(menu => menu.remove());
    this.invalidateLayoutTree();
  }

  setCompact(compact: boolean) {
    this.setProperty('compact', compact);
  }

  protected _renderCompact() {
    this.$container.toggleClass('compact', this.compact);
    this.invalidateLayoutTree();
  }
}
