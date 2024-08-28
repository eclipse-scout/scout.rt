/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ColumnLayout, EventHandler, HtmlComponent, Menu, PropertyChangeEvent, widgets} from '../index';

export class ComboMenu extends Menu {
  protected _childVisibleChangeHandler: EventHandler<PropertyChangeEvent<boolean>>;

  constructor() {
    super();
    this._childVisibleChangeHandler = this._onChildVisibleChange.bind(this);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('menu-item combo-menu');
    if (this.uiCssClass) {
      this.$container.addClass(this.uiCssClass);
    }
    this.$container.unfocusable();
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new ColumnLayout());
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderChildActions();
  }

  protected override _setChildActions(childActions: Menu[]) {
    this.childActions.forEach(child => child.off('propertyChange:visible', this._childVisibleChangeHandler));
    super._setChildActions(childActions);
    this.childActions.forEach(child => child.on('propertyChange:visible', this._childVisibleChangeHandler));
  }

  protected override _renderChildActions() {
    super._renderChildActions();

    this.childActions.forEach(childAction => {
      childAction.addCssClass('combo-menu-child');
      childAction.render();
    });
    widgets.updateFirstLastMarker(this.childActions);
  }

  protected override _togglesSubMenu(): boolean {
    return false;
  }

  protected _onChildVisibleChange(event: PropertyChangeEvent<boolean>) {
    if (this.rendered) {
      widgets.updateFirstLastMarker(this.childActions);
    }
  }

  protected override _doActionTogglesPopup(): boolean {
    return false;
  }

  override isToggleAction(): boolean {
    return false;
  }

  override isTabTarget(): boolean {
    // To make children tabbable, combo menu must never be a tab target, even if its a default menu
    return false;
  }
}
