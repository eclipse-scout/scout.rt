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
import {Action, ViewButtonActionKeyStroke} from '../../index';

export default class ViewButton extends Action {

  constructor() {
    super();
    this.showTooltipWhenSelected = false;
    this.displayStyle = 'TAB';
    /**
     * Indicates if this view button is currently the "selected button" in the ViewMenuTab widget,
     * i.e. if it was the last view button of type MENU to have been selected. Note that the
     * "selected" property does not necessarily have to be true as well, since an other button of
     * type TAB might currently be selected. This information is used when restoring the "selected
     * button" when the ViewMenuTab widget is removed and restored again, e.g. when toggling the
     * desktop's 'navigationVisible' property.
     * @type {boolean}
     */
    this.selectedAsMenu = false;
    this._renderedAsMenu = false;
  }

  renderAsMenuItem($parent) {
    this._renderedAsMenu = true;
    super.render($parent);
  }

  renderAsTab($parent) {
    this._renderedAsMenu = false;
    super.render($parent);
  }

  _render() {
    if (this._renderedAsMenu) {
      this._renderAsMenuItem();
    } else {
      this._renderAsTab();
    }
  }

  _renderAsMenuItem() {
    this.$container = this.$parent.appendDiv('view-menu-item')
      .on('click', this._onMouseEvent.bind(this));
  }

  _renderAsTab() {
    this.$container = this.$parent.appendDiv('view-button-tab')
      .on('mousedown', this._onMouseEvent.bind(this));
  }

  /**
   * @override Action.js
   */
  _renderText() {
    if (this._renderedAsMenu) {
      super._renderText();
    }
  }

  setDisplayStyle(displayStyle) {
    this.setProperty('displayStyle', displayStyle);
  }

  last() {
    this.$container.addClass('last');
  }

  tab() {
    this.$container.addClass('view-tab');
  }

  _onMouseEvent(event) {
    this.doAction();
  }

  /**
   * @override Action.js
   */
  _createActionKeyStroke() {
    return new ViewButtonActionKeyStroke(this);
  }

  setSelectedAsMenu(selectedAsMenu) {
    this.selectedAsMenu = selectedAsMenu;
  }
}
