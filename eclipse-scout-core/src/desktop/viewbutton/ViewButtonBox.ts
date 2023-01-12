/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Desktop, Event, EventHandler, HtmlComponent, InitModelOf, ObjectOrChildModel, OutlineViewButton, PropertyChangeEvent, scout, ViewButton, ViewButtonBoxEventMap, ViewButtonBoxModel, ViewMenuTab, Widget, widgets} from '../../index';

export class ViewButtonBox extends Widget implements ViewButtonBoxModel {
  declare model: ViewButtonBoxModel;
  declare eventMap: ViewButtonBoxEventMap;
  declare self: ViewButtonBox;

  desktop: Desktop;
  viewMenuTab: ViewMenuTab;
  viewButtons: ViewButton[];
  menuButtons: ViewButton[];
  tabButtons: ViewButton[];
  selectedMenuButtonAlwaysVisible: boolean;
  protected _desktopOutlineChangeHandler: EventHandler<Event<Desktop>>;
  protected _viewButtonPropertyChangeHandler: EventHandler<PropertyChangeEvent<any, ViewButton>>;

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

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.desktop = this.session.desktop;
    this.viewMenuTab = scout.create(ViewMenuTab, {
      parent: this
    });
    this._setViewButtons(this.viewButtons);
    this.desktop.on('outlineChange', this._desktopOutlineChangeHandler);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('view-button-box');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);

    this.viewMenuTab.render();
    this._onDesktopOutlineChange();
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderTabButtons();
  }

  protected override _remove() {
    this.desktop.off('outlineChange', this._desktopOutlineChangeHandler);
    this.viewButtons.forEach(viewButton => viewButton.off('selected', this._viewButtonPropertyChangeHandler));

    super._remove();
  }

  setSelectedMenuButtonVisible(selectedMenuButtonVisible: boolean) {
    this.viewMenuTab.setSelectedButtonVisible(selectedMenuButtonVisible);
  }

  setSelectedMenuButtonAlwaysVisible(selectedMenuButtonAlwaysVisible: boolean) {
    this.setProperty('selectedMenuButtonAlwaysVisible', selectedMenuButtonAlwaysVisible);
  }

  protected _setSelectedMenuButtonAlwaysVisible(selectedMenuButtonAlwaysVisible: boolean) {
    this._updateSelectedMenuButtonVisibility();
  }

  setViewButtons(viewButtons: ObjectOrChildModel<ViewButton>[]) {
    this.setProperty('viewButtons', viewButtons);
  }

  protected _setViewButtons(viewButtons: ViewButton[]) {
    if (this.viewButtons) {
      this.viewButtons.forEach(viewButton => viewButton.off('propertyChange', this._viewButtonPropertyChangeHandler));
    }
    this._setProperty('viewButtons', viewButtons);
    this.viewButtons.forEach(viewButton => viewButton.on('propertyChange', this._viewButtonPropertyChangeHandler));
    this._updateViewButtons();
  }

  protected _setTabButtons(tabButtons: ViewButton[]) {
    this._setProperty('tabButtons', tabButtons);
  }

  protected _removeTabButtons() {
    this.tabButtons.forEach(button => button.remove());
  }

  protected _renderTabButtons() {
    this.tabButtons.forEach((viewTab, i) => viewTab.renderAsTab(this.$container));
    widgets.updateFirstLastMarker(this.tabButtons);
  }

  protected _updateViewButtons() {
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

  protected _updateVisibility() {
    this.setVisible((this.tabButtons.length + this.menuButtons.length) > 1);
  }

  protected _setMenuButtons(menuButtons: ViewButton[]) {
    this._setProperty('menuButtons', menuButtons);
    this.viewMenuTab.setViewButtons(this.menuButtons);
  }

  protected _updateSelectedMenuButtonVisibility() {
    this.setSelectedMenuButtonVisible(this.selectedMenuButtonAlwaysVisible || (this.tabButtons.length >= 1 && this.menuButtons.length >= 1));
  }

  /**
   * This method updates the state of the view-menu-tab and the selected state of outline-view-button-box.
   * This method must also work in offline mode.
   */
  protected _onDesktopOutlineChange() {
    let outline = this.desktop.outline;
    this.viewButtons.forEach(viewTab => {
      if (viewTab instanceof OutlineViewButton) {
        viewTab.onOutlineChange(outline);
      }
    });
  }

  protected _onViewButtonSelected(event: PropertyChangeEvent<boolean, ViewButton>) {
    // Deselect other toggleable view buttons
    this.viewButtons.forEach(viewButton => {
      if (viewButton !== event.source && viewButton.isToggleAction()) {
        viewButton.setSelected(false);
      }
    });

    // Inform viewMenu tab about new selection
    this.viewMenuTab.onViewButtonSelected();
  }

  protected _onViewButtonPropertyChange(event: PropertyChangeEvent<any, ViewButton>) {
    if (event.propertyName === 'selected') {
      this._onViewButtonSelected(event);
    } else if (event.propertyName === 'visible' || event.propertyName === 'displayStyle') {
      this._updateViewButtons();
    }
  }
}
