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
import {arrays, graphics, menuNavigationKeyStrokes, Point, PopupWithHead, ViewMenuPopupLayout} from '../../index';

/**
 * Popup menu to switch between outlines.
 */
export default class ViewMenuPopup extends PopupWithHead {

  constructor() {
    super();
    this.$tab;
    this.$headBlueprint;
    this.viewMenus;
    this.viewButtonBoxBounds;
    this._addWidgetProperties('viewMenus');
    this._viewMenuActionHandler = this._onViewMenuAction.bind(this);
  }

  static MAX_MENU_WIDTH = 300;

  _init(options) {
    options.focusableContainer = true;
    super._init(options);

    this.$tab = options.$tab;
    this.$headBlueprint = this.$tab;
    this.viewButtonBoxBounds = options.naviBounds;
  }

  _createLayout() {
    return new ViewMenuPopupLayout(this);
  }

  /**
   * @override Popup.js
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    menuNavigationKeyStrokes.registerKeyStrokes(this.keyStrokeContext, this, 'view-menu-item');
  }

  _render() {
    super._render();

    this.viewMenus.forEach(function(viewMenu) {
      viewMenu.renderAsMenuItem(this.$body);
      viewMenu.on('action', this._viewMenuActionHandler);
    }, this);

    // Add last marker to last visible item
    let lastVisibleMenu = arrays.findFromReverse(this.viewMenus, this.viewMenus.length - 1, viewMenu => {
      return viewMenu.visible;
    }, this);
    lastVisibleMenu.$container.addClass('last');

    this._installScrollbars({
      axis: 'y'
    });
  }

  _remove() {
    this.viewMenus.forEach(function(viewMenu) {
      viewMenu.off('action', this._viewMenuActionHandler);
    }, this);

    super._remove();
  }

  /**
   * @override
   */
  get$Scrollable() {
    return this.$body;
  }

  /**
   * @override PopupWithHead.js
   */
  _renderHead() {
    super._renderHead();

    this._copyCssClassToHead('view-menu');
    this._copyCssClassToHead('unfocusable');
    this.$head.removeClass('popup-head');
    this.$head.addClass('view-menu-popup-head');
  }

  /**
   * @override PopupWithHead.js
   */
  _modifyBody() {
    this.$body.removeClass('popup-body');
    this.$body.addClass('view-menu-popup-body');
  }

  position() {
    let pos = this.$tab.offset(),
      headSize = graphics.size(this.$tab, true),
      bodyTop = headSize.height;

    graphics.setBounds(this.$head, pos.left, pos.top, headSize.width, headSize.height);

    this.$deco.cssLeft(pos.left);
    this.$deco.cssTop(0);
    this.$deco.cssWidth(headSize.width - 1);

    this.$head.cssTop(-bodyTop);
    this.$body.cssTop(0);
    this.$container.cssMarginTop(headSize.height);

    this.setLocation(new Point(0, 0));
  }

  _onViewMenuAction(event) {
    this.close();
  }
}
