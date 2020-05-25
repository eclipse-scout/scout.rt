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
import {ContextMenuPopup} from '../../index';

/**
 * The MenuBarPopup is a special Popup that is used in the menu-bar. It is tightly coupled with a menu-item and shows a header
 * which has a different size than the popup-body.
 */
export default class MenuBarPopup extends ContextMenuPopup {

  constructor() {
    super();
    this.menu = null;
    this.$headBlueprint = null;
    this.ignoreEvent = null;
    this._headVisible = true;
    this.parentMenuPropertyChangeHandler = this._onParentMenuPropertyChange.bind(this);
  }

  _init(options) {
    options.$anchor = options.menu.$container;
    super._init(options);

    this.$headBlueprint = this.menu.$container;
  }

  /**
   * @override ContextMenuPopup.js
   */
  _getMenuItems() {
    return this.menu.childActions || this.menu.menus;
  }

  /**
   * @override Popup.js
   */
  close(event) {
    if (!event || !this.ignoreEvent || event.originalEvent !== this.ignoreEvent.originalEvent) {
      super.close(event);
    }
  }

  _render() {
    super._render();
    this.menu.on('propertyChange', this.parentMenuPropertyChangeHandler);
  }

  _remove() {
    this.menu.off('propertyChange', this.parentMenuPropertyChangeHandler);
    super._remove();
  }

  /**
   * @override PopupWithHead.js
   */
  _renderHead() {
    super._renderHead();

    if (this.menu.$container.parent().hasClass('main-menubar')) {
      this.$head.addClass('in-main-menubar');
    }

    if (this.menu.uiCssClass) {
      this._copyCssClassToHead(this.menu.uiCssClass);
    }
    this._copyCssClassToHead('unfocusable');
    this._copyCssClassToHead('button');
    this._copyCssClassToHead('menu-textandicon');
    this._copyCssClassToHead('bottom-text');
  }

  _onParentMenuPropertyChange(event) {
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
}
