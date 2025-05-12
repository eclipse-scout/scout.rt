/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, HtmlComponent, InitModelOf, Menu, MenuBoxEventMap, MenuBoxLayout, MenuBoxModel, ObjectOrChildModel, Widget} from '../../index';

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
    aria.role(this.$container, 'menubar');

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

  /**
   * Sets the menu box into compact mode. Can be reversed by calling {@link #undoMakeCompact}.
   */
  makeCompact() {
    if (this.compactOrig !== undefined) {
      return; // already done
    }
    this.compactOrig = this.compact;
    this.setCompact(true);
  }

  /**
   * Undoes the effect of {@link #makeCompact}, i.e. restores the previous compact state.
   * If {@link #makeCompact} was not called previously, nothing happens.
   */
  undoMakeCompact() {
    if (this.compactOrig === undefined) {
      return; // nothing to undo
    }
    this.setCompact(this.compactOrig);
    this.compactOrig = undefined;
  }
}
