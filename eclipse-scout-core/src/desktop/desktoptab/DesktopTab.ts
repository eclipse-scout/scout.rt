/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, ContextMenuPopup, DesktopTabArea, DesktopTabCloseKeyStroke, DesktopTabExecKeyStroke, Form, HAlign, KeyStrokeContext, Menu, scout, SimpleTab} from '../../index';

export class DesktopTab extends SimpleTab<Form> {
  declare parent: DesktopTabArea;

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStroke(new DesktopTabExecKeyStroke(this));
    this.keyStrokeContext.registerKeyStroke(new DesktopTabCloseKeyStroke(this));
  }

  protected override _render() {
    super._render();
    this.$container.addClass('desktop-tab unfocusable');
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
      text: this.session.text('ui.CloseAllTabs'),
      visible: this._closeAllMenuVisible()
    });
    menuCloseAllTabs.on('action', this._onCloseAll.bind(this));

    let menuCloseOtherTabs = scout.create(Menu, {
      parent: this,
      text: this.session.text('ui.CloseOtherTabs'),
      visible: this._closeOtherMenuVisible()
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

  /**
   * Returns a list of all open forms with DISPLAY_HINT_VIEW that are closable.
   *
   * @param includeThis include the form of this desktop tab. Default is `true`.
   */
  protected _getCloseableViews(includeThis = true): Form[] {
    let closeableForms = this.parent.tabs
      .filter(desktopTab => desktopTab.closable)
      .map(desktopTab => desktopTab.view);

    if (!includeThis) {
      arrays.remove(closeableForms, this.view);
    }

    return closeableForms;
  }

  protected _closeAllMenuVisible(): boolean {
    // When the desktop has at least one tab which is closeable, the menu is visible.
    return this._getCloseableViews().length > 0;
  }

  protected _closeOtherMenuVisible(): boolean {
    return this._getCloseableViews(false).length > 0;
  }

  protected _onCloseAll() {
    this.session.desktop.cancelViews(this._getCloseableViews());
  }

  protected _onCloseOther() {
    this.session.desktop.cancelViews(this._getCloseableViews(false));
  }
}
