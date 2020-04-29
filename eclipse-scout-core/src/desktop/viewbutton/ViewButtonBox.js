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
import {HtmlComponent, OutlineViewButton, scout, ViewButtonBoxLayout, Widget} from '../../index';

export default class ViewButtonBox extends Widget {

  constructor() {
    super();
    this.viewMenuTab = null;
    this.viewButtons = [];
    this.menuButtons = [];
    this.tabButtons = [];
    this._desktopOutlineChangeHandler = this._onDesktopOutlineChange.bind(this);
    this._viewButtonPropertyChangeHandler = this._onViewButtonPropertyChange.bind(this);
    this._addWidgetProperties(['tabButtons']);
  }

  _init(model) {
    super._init(model);
    this.desktop = this.session.desktop;
    this.viewMenuTab = scout.create('ViewMenuTab', {
      parent: this
    });
    this._setViewButtons(this.viewButtons);
    this.desktop.on('outlineChange', this._desktopOutlineChangeHandler);
  }

  _render() {
    this.$container = this.$parent.appendDiv('view-button-box');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new ViewButtonBoxLayout(this));

    this.viewMenuTab.render();
    this._onDesktopOutlineChange();
  }

  _renderProperties() {
    super._renderProperties();
    this._renderTabButtons();
  }

  _remove() {
    this.desktop.off('outlineChange', this._desktopOutlineChangeHandler);
    this.viewButtons.forEach(function(viewButton) {
      viewButton.off('selected', this._viewButtonPropertyChangeHandler);
    }, this);

    super._remove();
  }

  setMenuTabVisible(menuTabVisible) {
    this.viewMenuTab.setViewTabVisible(menuTabVisible);
    this.invalidateLayoutTree();
  }

  setViewButtons(viewButtons) {
    this.setProperty('viewButtons', viewButtons);
  }

  _setViewButtons(viewButtons) {
    if (this.viewButtons) {
      this.viewButtons.forEach(function(viewButton) {
        viewButton.off('propertyChange', this._viewButtonPropertyChangeHandler);
      }, this);
    }
    this._setProperty('viewButtons', viewButtons);
    this.viewButtons.forEach(function(viewButton) {
      viewButton.on('propertyChange', this._viewButtonPropertyChangeHandler);
    }, this);
    this._updateViewButtons();
  }

  setTabButtons(tabButtons) {
    this.setProperty('tabButtons', tabButtons);
  }

  _renderTabButtons() {
    this.tabButtons.forEach(function(viewTab, i) {
      viewTab.renderAsTab();
      viewTab.tab();
      if (i === this.tabButtons.length - 1) {
        viewTab.last();
      }
    }, this);
  }

  _updateViewButtons() {
    let viewButtons = this.viewButtons.filter(b => {
        return b.visible;
      }),
      menuButtons = viewButtons.filter(b => {
        return b.displayStyle === 'MENU';
      }),
      tabButtons = null;
    // render as tab if length is < 1
    if (menuButtons.length > 1) {
      tabButtons = viewButtons.filter(b => {
        return b.displayStyle === 'TAB';
      });
    } else {
      // all visible view buttons are rendered as tab
      tabButtons = viewButtons;
      menuButtons = [];
    }

    this._setMenuButtons(menuButtons);

    this.setTabButtons(tabButtons);
    this._updateVisibility();
  }

  _updateVisibility(menuButtons) {
    this.setVisible((this.tabButtons.length + this.menuButtons.length) > 1);
  }

  setMenuButtons(menuButtons) {
    this.setProperty('menuButtons', menuButtons);
    this._updateVisibility();
  }

  _setMenuButtons(menuButtons) {
    this._setProperty('menuButtons', menuButtons);
    this.viewMenuTab.setViewButtons(this.menuButtons);
  }

  sendToBack() {
    this.viewMenuTab.sendToBack();
  }

  bringToFront() {
    this.viewMenuTab.bringToFront();
  }

  /**
   * This method updates the state of the view-menu-tab and the selected state of outline-view-button-box.
   * This method must also work in offline mode.
   */
  _onDesktopOutlineChange(event) {
    let outline = this.desktop.outline;
    this.viewButtons.forEach(viewTab => {
      if (viewTab instanceof OutlineViewButton) {
        viewTab.onOutlineChange(outline);
      }
    });
  }

  _onViewButtonSelected(event) {
    // Deselect other togglable view buttons
    this.viewButtons.forEach(viewButton => {
      if (viewButton !== event.source && viewButton.isToggleAction()) {
        viewButton.setSelected(false);
      }
    }, this);

    // Inform viewMenu tab about new selection
    this.viewMenuTab.onViewButtonSelected();
  }

  _onViewButtonPropertyChange(event) {
    if (event.propertyName === 'selected') {
      this._onViewButtonSelected(event);
    } else if (event.propertyName === 'visible' ||
      event.propertyName === 'displayStyle') {
      this._updateViewButtons();
    }
  }
}
