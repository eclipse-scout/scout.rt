/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, ContextMenuPopup, DesktopTabArea, Form, HAlign, Menu, scout, SimpleTab} from '../../index';

export class DesktopTab extends SimpleTab<Form> {
  declare parent: DesktopTabArea;

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
