/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {EventHandler, focusUtils, FormFieldTile, MenuBar, PropertyChangeEvent, TableField} from '../../../index';
import $ from 'jquery';

export class TileTableField extends TableField {
  protected _menuBarPropertyChangeHandler: EventHandler<PropertyChangeEvent<any, MenuBar>>;
  protected _documentMouseDownHandler: (event: MouseEvent) => void;

  constructor() {
    super();

    this._menuBarPropertyChangeHandler = this._onMenuBarPropertyChange.bind(this);
    this._documentMouseDownHandler = this._onDocumentMouseDown.bind(this);
  }

  protected override _render() {
    super._render();
    if ((this.parent as FormFieldTile).displayStyle !== FormFieldTile.DisplayStyle.DASHBOARD) {
      return;
    }

    if (!this.session.focusManager.restrictedFocusGain) {
      this.$container.document(true).addEventListener('mousedown', this._documentMouseDownHandler, true);
    }
    this.$container
      .on('focusout', this._onTileFocusOut.bind(this))
      .on('focusin', this._onTileFocusIn.bind(this));
    if (!focusUtils.isOrHasActiveElement(this.$container)) {
      this._hideMenuBar(true);
    }
  }

  protected override _remove() {
    this.$container.document(true).removeEventListener('mousedown', this._documentMouseDownHandler, true);
    super._remove();
  }

  protected override _renderTable() {
    super._renderTable();
    if ((this.parent as FormFieldTile).displayStyle !== FormFieldTile.DisplayStyle.DASHBOARD) {
      return;
    }
    if (this.table) {
      this.table.menuBar.on('propertyChange', this._menuBarPropertyChangeHandler);
      this._toggleHasMenuBar();
    }
  }

  protected override _removeTable() {
    if ((this.parent as FormFieldTile).displayStyle !== FormFieldTile.DisplayStyle.DASHBOARD) {
      return;
    }
    if (this.table) {
      this.table.menuBar.off('propertyChange', this._menuBarPropertyChangeHandler);
    }
    super._removeTable();
  }

  protected _onTileFocusOut(event: JQuery.FocusOutEvent) {
    if (this.$container.isOrHas(event.relatedTarget as Element)) {
      // If focus stays in tile, don't remove menubar
      return;
    }
    let popup = $('.popup').data('widget');

    // Hide menu bar when focus leaves tile, but only if there is no popup open that belongs to the table (e.g. context menu or table header popup)
    // If a popup is open, the focus is on the popup but the menubar should still be visible
    requestAnimationFrame(() => {
      if (!this.rendered) {
        return;
      }

      if (!this.has(popup)) {
        this._hideMenuBar(true);
      }
    });
  }

  protected _onTileFocusIn(event: JQuery.FocusInEvent) {
    this._hideMenuBar(false);
  }

  protected _hideMenuBar(hiddenByUi: boolean) {
    if (!this.table) {
      return;
    }
    this.table.menuBar.hiddenByUi = hiddenByUi;
    this.table.menuBar.updateVisibility();
  }

  protected _onMenuBarPropertyChange(event: PropertyChangeEvent<any, MenuBar>) {
    if (event.propertyName === 'visible') {
      this._toggleHasMenuBar();
    }
  }

  protected _toggleHasMenuBar() {
    if (!this.rendered && !this.rendering) {
      return;
    }
    // adjust menu bar on TileTableField with the additional class has-menubar.
    this.$container.toggleClass('has-menubar', this.table.menuBar.visible);
  }

  protected _onDocumentMouseDown(event: MouseEvent) {
    let $popup = $('.popup');
    let popup = $popup.data('widget');
    if (this.table && popup && this.table.has(popup) && $popup.has(event.target as HTMLElement)) {
      // Ensure focus is not removed from table when clicking on a context-menu
      // Otherwise the menubar would get invisible while closing the context-menu (e.g. by clicking on header)
      // This is only necessary if restrictedFocusGain is false (e.g. on mobile).
      event.preventDefault();
    }
  }
}
