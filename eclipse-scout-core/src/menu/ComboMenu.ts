/*
 * Copyright (c) 2014-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {HtmlComponent, Menu, widgets} from '../index';

export default class ComboMenu extends Menu {

  constructor() {
    super();
    this._childVisibleChangeHandler = this._onChildVisibleChange.bind(this);
  }

  _render() {
    this.$container = this.$parent.appendDiv('menu-item combo-menu');
    if (this.uiCssClass) {
      this.$container.addClass(this.uiCssClass);
    }
    this.$container.unfocusable();
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
  }

  _renderProperties() {
    super._renderProperties();
    this._renderChildActions();
  }

  _setChildActions(childActions) {
    this.childActions.forEach(child => child.off('propertyChange:visible', this._childVisibleChangeHandler));
    super._setChildActions(childActions);
    this.childActions.forEach(child => child.on('propertyChange:visible', this._childVisibleChangeHandler));
  }

  _renderChildActions() {
    super._renderChildActions();

    this.childActions.forEach(childAction => {
      childAction.addCssClass('combo-menu-child');
      childAction.render();
    });
    widgets.updateFirstLastMarker(this.childActions);
  }

  // @override
  _togglesSubMenu() {
    return false;
  }

  _onChildVisibleChange(event) {
    if (this.rendered) {
      widgets.updateFirstLastMarker(this.childActions);
    }
  }

  _doActionTogglesPopup() {
    return false;
  }

  isToggleAction() {
    return false;
  }

  isTabTarget() {
    // To make children tabbable, combo menu must never be a tab target, even if its a default menu
    return false;
  }
}
