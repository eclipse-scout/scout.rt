/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HtmlComponent, InitModelOf, Menu, MenuBoxEventMap, MenuBoxLayout, MenuBoxModel, ObjectOrChildModel, Widget} from '../../index';

export class MenuBox extends Widget implements MenuBoxModel {
  declare model: MenuBoxModel;
  declare eventMap: MenuBoxEventMap;
  declare self: MenuBox;

  compact: boolean;
  compactOrig: boolean;
  menus: Menu[];
  uiMenuCssClass: string;

  constructor() {
    super();
    this.compact = false;
    this.menus = [];
    this.uiMenuCssClass = 'menu-box-item';
    this._addWidgetProperties('menus');
  }

  protected override _init(options: InitModelOf<this>) {
    super._init(options);
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

  setMenus(menus: ObjectOrChildModel<Menu>[]) {
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
