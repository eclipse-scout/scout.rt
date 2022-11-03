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
import {arrays, HtmlComponent, Menu, MenuBarBoxEventMap, MenuBarBoxLayout, MenuBarBoxModel, MenuModel, RefModel, Widget} from '../../index';
import {TooltipPosition} from '../../tooltip/Tooltip';

export default class MenuBarBox extends Widget implements MenuBarBoxModel {
  declare model: MenuBarBoxModel;
  declare eventMap: MenuBarBoxEventMap;
  declare self: MenuBarBox;

  menuItems: Menu[];
  tooltipPosition: TooltipPosition;

  constructor() {
    super();
    this.menuItems = [];
    this.tooltipPosition = 'top';
    this._addWidgetProperties('menuItems');
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('menubar-box');

    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new MenuBarBoxLayout(this));
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderMenuItems();
  }

  protected override _remove() {
    this._removeMenuItems();
    super._remove();
  }

  setMenuItems(menuOrModels: Menu | RefModel<MenuModel> | (Menu | RefModel<MenuModel>)[]) {
    let menuItems = arrays.ensure(menuOrModels);
    if (!arrays.equals(this.menuItems, menuItems)) {
      this.setProperty('menuItems', menuItems);
    }
  }

  protected _setMenuItems(menuItems: Menu[]) {
    this._setProperty('menuItems', menuItems);
    this._updateTooltipPosition();
  }

  protected _removeMenuItems() {
    this.menuItems.forEach(item => item.remove());
  }

  protected _renderMenuItems() {
    this.menuItems.forEach(item => {
      item.render(this.$container);
      item.$container.addClass('menubar-item');
    });

    if (!this.rendering) {
      this.invalidateLayoutTree();
    }
  }

  protected override _renderVisible() {
    super._renderVisible();
    this.revalidateLayout();
  }

  setTooltipPosition(position: TooltipPosition) {
    this.setProperty('tooltipPosition', position);
  }

  protected _setTooltipPosition(position: TooltipPosition) {
    this._setProperty('tooltipPosition', position);
    this._updateTooltipPosition();
  }

  protected _updateTooltipPosition() {
    this.menuItems.forEach(item => item.setTooltipPosition(this.tooltipPosition));
  }
}
