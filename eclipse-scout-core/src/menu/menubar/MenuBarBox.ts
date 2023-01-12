/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, HtmlComponent, Menu, MenuBarBoxEventMap, MenuBarBoxLayout, MenuBarBoxModel, ObjectOrChildModel, TooltipPosition, Widget} from '../../index';

export class MenuBarBox extends Widget implements MenuBarBoxModel {
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

  setMenuItems(menuOrModels: ObjectOrChildModel<Menu> | ObjectOrChildModel<Menu>[]) {
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
