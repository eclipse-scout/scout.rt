/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, EllipsisMenu, HtmlComponent, InitModelOf, Menu, MenuBoxEventMap, MenuBoxLayout, MenuBoxModel, ObjectOrChildModel, scout, TabbableCoordinator, Widget} from '../../index';

export class MenuBox extends Widget implements MenuBoxModel {
  declare model: MenuBoxModel;
  declare eventMap: MenuBoxEventMap;
  declare self: MenuBox;

  compact: boolean;
  menus: Menu[];
  uiMenuCssClass: string;
  tabbableCoordinator: TabbableCoordinator;

  protected _compactOrig: boolean;

  constructor() {
    super();
    this.compact = false;
    this.menus = [];
    this.uiMenuCssClass = 'menu-box-item';
    this._addWidgetProperties('menus');
  }

  protected override _init(options: InitModelOf<this>) {
    super._init(options);
    this.tabbableCoordinator = scout.create(TabbableCoordinator, {parent: this});
    this._setMenus(this.menus);
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

  protected _setMenus(menus: Menu[]) {
    menus.forEach(menu => this._initMenu(menu));
    this._setProperty('menus', menus);
    this._updateTabbableItems();
  }

  protected _initMenu(menu: Menu) {
    menu.uiCssClass = this.uiMenuCssClass;
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
    if (this._compactOrig !== undefined) {
      return; // already done
    }
    this._compactOrig = this.compact;
    this.setCompact(true);
  }

  /**
   * Undoes the effect of {@link #makeCompact}, i.e. restores the previous compact state.
   * If {@link #makeCompact} was not called previously, nothing happens.
   */
  undoMakeCompact() {
    if (this._compactOrig === undefined) {
      return; // nothing to undo
    }
    this.setCompact(this._compactOrig);
    this._compactOrig = undefined;
  }

  /**
   * @internal
   */
  _updateTabbableItems(ellipsisMenu?: EllipsisMenu) {
    let items = [...this.menus];
    if (ellipsisMenu) {
      items.push(ellipsisMenu);
    }
    this.tabbableCoordinator.setItems(items);
  }
}
