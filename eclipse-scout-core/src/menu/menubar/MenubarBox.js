/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, HtmlComponent, MenuBar, MenubarBoxLayout, Widget} from '../../index';

export default class MenubarBox extends Widget {

  constructor() {
    super();
    this.menuItems = [];
    this.tooltipPosition = MenuBar.Position.TOP;
    this._addWidgetProperties('menuItems');
    this._menuItemPropertyChangeHandler = this._onMenuItemPropertyChange.bind(this);
  }

  _destroy() {
    super._destroy();
    this._removeMenuHandlers();
  }

  _render() {
    this.$container = this.$parent.appendDiv('menubox');

    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new MenubarBoxLayout(this));
  }

  _renderProperties() {
    super._renderProperties();
    this._renderMenuItems();
  }

  /**
   * @override Widget.js
   */
  _remove() {
    this._removeMenuItems();
    super._remove();
  }

  setMenuItems(menuItems) {
    menuItems = arrays.ensure(menuItems);
    if (!arrays.equals(this.menuItems, menuItems)) {
      this.setProperty('menuItems', menuItems);
    }
  }

  _setMenuItems(menuItems) {
    // remove property listeners of old menu items.
    this._removeMenuHandlers();

    this._setProperty('menuItems', menuItems);
    // add property listener of new menus
    this._addMenuHandlers();
    this._updateTooltipPosition();
  }

  _removeMenuItems() {
    this._removeMenuHandlers();
    this.menuItems.forEach(item => {
      item.overflow = false;
      item.remove();
    });
  }

  _renderMenuItems() {
    this.menuItems.forEach(item => {
      item.render(this.$container);
      item.$container.addClass('menubar-item');
    });

    if (!this.rendering) {
      this.invalidateLayoutTree();
    }
  }

  _addMenuHandlers() {
    this.menuItems.forEach(function(item) {
      item.off('propertyChange', this._menuItemPropertyChangeHandler);
    }, this);
  }

  _removeMenuHandlers() {
    this.menuItems.forEach(function(item) {
      item.off('propertyChange', this._menuItemPropertyChangeHandler);
    }, this);
  }

  _renderVisible() {
    super._renderVisible();
    this.revalidateLayout();
  }

  _onMenuItemPropertyChange(event) {
    if (event.propertyName === 'visible') {
      this.setVisible(this.menuItems.some(m => {
        return m.visible && !m.ellipsis;
      }));
    }
  }

  setTooltipPosition(position) {
    this.setProperty('tooltipPosition', position);
  }

  _setTooltipPosition(position) {
    this._setProperty('tooltipPosition', position);
    this._updateTooltipPosition();
  }

  _updateTooltipPosition() {
    this.menuItems.forEach(function(item) {
      item.setTooltipPosition(this.tooltipPosition);
    }, this);
  }
}
