/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, ContextMenuPopup, HAlign, Menu, scout, SimpleTab, SimpleTabArea} from '../../index';

export default class DesktopTab extends SimpleTab {
  declare parent: SimpleTabArea;

  constructor() {
    super();
  }

  protected override _render() {
    super._render();
    this.$container.addClass('desktop-tab');
    this.$container.on('contextmenu', this._onContextMenu.bind(this));
    this.$container.prependDiv('edge left');
    this.$container.appendDiv('edge right');
  }

  protected override _renderClosable() {
    super._renderClosable();
    if (this.closable && this.view.closeKeyStroke) {
      this.view.closeKeyStroke.renderingHints = {
        hAlign: HAlign.RIGHT,
        render: () => !!this.$close,
        $drawingArea: () => this.$close
      };
    }
  }

  protected _onContextMenu(event: JQuery.ContextMenuEvent) {
    let menuCloseAllTabs = scout.create(Menu, {
      parent: this,
      text: this.session.text('ui.CloseAllTabs')
    });
    menuCloseAllTabs.on('action', this._onCloseAll.bind(this));

    let menuCloseOtherTabs = scout.create(Menu, {
      parent: this,
      text: this.session.text('ui.CloseOtherTabs'),
      enabled: this.parent.tabs.length > 1
    });
    menuCloseOtherTabs.on('action', this._onCloseOther.bind(this));

    let popup = scout.create(ContextMenuPopup, {
      parent: this,
      menuItems: [menuCloseAllTabs, menuCloseOtherTabs],
      cloneMenuItems: false,
      location: {
        x: event.pageX,
        y: event.pageY
      }
    });
    popup.open();
  }

  protected _onCloseAll() {
    let openViews = this.parent.tabs.map(desktopTab => desktopTab.view);
    this.session.desktop.cancelViews(openViews);
  }

  protected _onCloseOther() {
    let openViews = this.parent.tabs.map(desktopTab => desktopTab.view);
    arrays.remove(openViews, this.view);
    this.session.desktop.cancelViews(openViews);
  }
}
