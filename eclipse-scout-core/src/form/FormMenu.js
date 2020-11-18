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
import {ContextMenuPopup, Device, FormMenuActionKeyStroke, GroupBox, Menu, scout} from '../index';

export default class FormMenu extends Menu {

  constructor() {
    super();
    this.form = null;
    this.toggleAction = true;
    this.popupStyle = null;
    this._addWidgetProperties('form');
  }

  static PopupStyle = {
    DEFAULT: 'default',
    MOBILE: 'mobile'
  };

  _init(model) {
    super._init(model);

    if (!this.popupStyle) {
      if (this.session.userAgent.deviceType === Device.Type.MOBILE) {
        this.popupStyle = FormMenu.PopupStyle.MOBILE;
      } else {
        this.popupStyle = FormMenu.PopupStyle.DEFAULT;
      }
    }
    this._setSelected(this.selected);
  }

  _renderForm() {
    if (!this.rendered) {
      // Don't execute initially since _renderSelected will be executed
      return;
    }
    this._renderSelected();
  }

  /**
   * @override
   */
  clone(modelOverride, options) {
    modelOverride = modelOverride || {};
    // If the FormMenu is put into a context menu it will be cloned.
    // Cloning a form is not possible because it may non clonable components (Table, TabBox, etc.) -> exclude
    // Luckily, it is not necessary to clone it since the form is never shown multiple times at once -> Just use the same instance
    modelOverride.form = this.form;
    return super.clone(modelOverride, options);
  }

  _addFormRemoveHandler() {
    if (!this.form) {
      return;
    }

    this.form.one('remove', event => {
      this._onFormRemove(event);
    });
  }

  /**
   * Called when the popup form is removed (closed). Either by clicking the FormMenu again (toggle), the menu closed or if the Form closed itself.
   */
  _onFormRemove(event) {
    if (!this.selected) {
      return; // the menu is no longer selected. It was closed by the user (toggle). There is no need to unselect and close the popups
    }
    if (!this.destroying && !this.removing) {
      // no need to change the selection state if the widget is destroying.
      this.setSelected(false);
    }

    let parentContextMenuPopup = this.findParent(p => {
      return p instanceof ContextMenuPopup;
    });
    if (parentContextMenuPopup && !(parentContextMenuPopup.destroying || parentContextMenuPopup.removing)) {
      // only explicitly close the popup if it is not already being closed. Otherwise it is removed twice.
      parentContextMenuPopup.close();
    }
  }

  _setSelected(selected) {
    this._setProperty('selected', selected);
    if (this.popupStyle === FormMenu.PopupStyle.MOBILE && this._doActionTogglesPopup()) {
      // Mobile Popup can be rendered even if menu is not. This is useful if a tool form menu should be opened while the desktop bench is open instead of the outline
      // Open will be called in renderSelected again but won't do anything
      if (this.selected) {
        this._openPopup();
      } else {
        this._closePopup();
      }
    }
  }

  /**
   * @override Menu.js
   */
  _openPopup() {
    if (this.popup) {
      // already open
      return;
    }
    super._openPopup();
    this._addFormRemoveHandler();
  }

  /**
   * @override Menu.js
   */
  _createPopup() {
    // Menu bar should always be on the bottom
    this.form.rootGroupBox.setMenuBarPosition(GroupBox.MenuBarPosition.BOTTOM);

    if (this.popupStyle === FormMenu.PopupStyle.MOBILE) {
      return scout.create('MobilePopup', {
        parent: this.session.desktop, // use desktop to make _handleSelectedInEllipsis work (if parent is this and this were not rendered, popup.entryPoint would not work)
        widget: this.form,
        title: this.form.title
      });
    }

    return scout.create('FormMenuPopup', {
      parent: this,
      formMenu: this,
      horizontalAlignment: this.popupHorizontalAlignment,
      verticalAlignment: this.popupVerticalAlignment
    });
  }

  /**
   * @override
   */
  _doActionTogglesPopup() {
    return !!this.form;
  }

  _handleSelectedInEllipsis() {
    if (this.popupStyle === FormMenu.PopupStyle.MOBILE) {
      // The mobile popup is not attached to a header -> no need to open the overflow menu (popup is already open due to _setSelected)
      return;
    }
    super._handleSelectedInEllipsis();
  }

  _createActionKeyStroke() {
    return new FormMenuActionKeyStroke(this);
  }
}
