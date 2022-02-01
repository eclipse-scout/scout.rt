/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {HtmlComponent, OutlineViewButton, scout, Widget, widgets} from '../../index';

export default class ViewButtonBox extends Widget {

  constructor() {
    super();
    this.viewMenuTab = null;
    this.viewButtons = [];
    this.menuButtons = [];
    this.tabButtons = [];
    this._desktopOutlineChangeHandler = this._onDesktopOutlineChange.bind(this);
    this._viewButtonPropertyChangeHandler = this._onViewButtonPropertyChange.bind(this);
    this.selectedMenuButtonAlwaysVisible = false;
    this._addWidgetProperties(['viewButtons']);
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

    this.viewMenuTab.render();
    this._onDesktopOutlineChange();
  }

  _renderProperties() {
    super._renderProperties();
    this._renderTabButtons();
  }

  _remove() {
    this.desktop.off('outlineChange', this._desktopOutlineChangeHandler);
    this.viewButtons.forEach(viewButton => viewButton.off('selected', this._viewButtonPropertyChangeHandler));

    super._remove();
  }

  setSelectedMenuButtonVisible(selectedMenuButtonVisible) {
    this.viewMenuTab.setSelectedButtonVisible(selectedMenuButtonVisible);
  }

  setSelectedMenuButtonAlwaysVisible(selectedMenuButtonAlwaysVisible) {
    this.setProperty('selectedMenuButtonAlwaysVisible', selectedMenuButtonAlwaysVisible);
  }

  _setSelectedMenuButtonAlwaysVisible(selectedMenuButtonAlwaysVisible) {
    this._updateSelectedMenuButtonVisibility();
  }

  setViewButtons(viewButtons) {
    this.setProperty('viewButtons', viewButtons);
  }

  _setViewButtons(viewButtons) {
    if (this.viewButtons) {
      this.viewButtons.forEach(viewButton => viewButton.off('propertyChange', this._viewButtonPropertyChangeHandler));
    }
    this._setProperty('viewButtons', viewButtons);
    this.viewButtons.forEach(viewButton => viewButton.on('propertyChange', this._viewButtonPropertyChangeHandler));
    this._updateViewButtons();
  }

  _setTabButtons(tabButtons) {
    this._setProperty('tabButtons', tabButtons);
  }

  _removeTabButtons() {
    this.tabButtons.forEach(button => button.remove());
  }

  _renderTabButtons() {
    this.tabButtons.forEach((viewTab, i) => viewTab.renderAsTab(this.$container));
    widgets.updateFirstLastMarker(this.tabButtons);
  }

  _updateViewButtons() {
    let viewButtons = this.viewButtons.filter(b => b.visible);
    let menuButtons = viewButtons.filter(b => b.displayStyle === 'MENU');
    let tabButtons = null;
    // render as tab if length is < 1
    if (menuButtons.length > 1) {
      tabButtons = viewButtons.filter(b => b.displayStyle === 'TAB');
    } else {
      // all visible view buttons are rendered as tab
      tabButtons = viewButtons;
      menuButtons = [];
    }

    this._setMenuButtons(menuButtons);
    if (this.rendered) {
      this._removeTabButtons();
    }
    this._setTabButtons(tabButtons);
    if (this.rendered) {
      this._renderTabButtons();
    }
    this._updateVisibility();
    this._updateSelectedMenuButtonVisibility();
  }

  _updateVisibility(menuButtons) {
    this.setVisible((this.tabButtons.length + this.menuButtons.length) > 1);
  }

  _setMenuButtons(menuButtons) {
    this._setProperty('menuButtons', menuButtons);
    this.viewMenuTab.setViewButtons(this.menuButtons);
  }

  _updateSelectedMenuButtonVisibility() {
    this.setSelectedMenuButtonVisible(this.selectedMenuButtonAlwaysVisible || (this.tabButtons.length >= 1 && this.menuButtons.length >= 1));
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
    // Deselect other toggleable view buttons
    this.viewButtons.forEach(viewButton => {
      if (viewButton !== event.source && viewButton.isToggleAction()) {
        viewButton.setSelected(false);
      }
    });

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
