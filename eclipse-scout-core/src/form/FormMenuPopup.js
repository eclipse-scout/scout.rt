/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FormMenuPopupLayout, PopupWithHead} from '../index';

export default class FormMenuPopup extends PopupWithHead {

  constructor() {
    super();
    this.formMenu = null;
    this.formMenuPropertyChangeHandler = this._onFormMenuPropertyChange.bind(this);
    this._addWidgetProperties('form');
  }

  _init(options) {
    options.form = options.formMenu.form;
    options.initialFocus = options.formMenu.form._initialFocusElement.bind(options.formMenu.form);
    super._init(options);

    this.$formMenu = this.formMenu.$container;
    this.$headBlueprint = this.$formMenu;
  }

  _createLayout() {
    return new FormMenuPopupLayout(this);
  }

  _render() {
    super._render();
    this.$container.addClass('form-menu-popup');

    this.form.renderInitialFocusEnabled = false;
    this.form.render(this.$body);

    // We add this here for symmetry reasons (because _removeHead is not called on remove())
    if (this._headVisible) {
      this.formMenu.on('propertyChange', this.formMenuPropertyChangeHandler);
    }
  }

  _remove() {
    super._remove();

    if (this._headVisible) {
      this.formMenu.off('propertyChange', this.formMenuPropertyChangeHandler);
    }
  }

  _renderHead() {
    super._renderHead();
    if (this.formMenu.uiCssClass) {
      this._copyCssClassToHead(this.formMenu.uiCssClass);
    }
    if (this.formMenu.cssClass) {
      this._copyCssClassToHead(this.formMenu.cssClass);
    }
    this._copyCssClassToHead('unfocusable');
  }

  _onFormMenuPropertyChange(event) {
    this.session.layoutValidator.schedulePostValidateFunction(() => {
      // Because this post layout validation function is executed asynchronously,
      // we have to check again if the popup is still rendered.
      if (!this.rendered) {
        return;
      }
      this.rerenderHead();
      this.position();
    });
  }

  /**
   * @override
   */
  _onWindowResize() {
    if (!this.rendered) {
      // may already be removed if a parent popup is closed during the resize event
      return;
    }
    // Don't close but layout and position, especially important for mobile devices if the popup contains an input field.
    // In that case activating the field opens the keyboard which may resize the screen (android tablets).
    this.revalidateLayout();
    this.position(false);
  }
}
