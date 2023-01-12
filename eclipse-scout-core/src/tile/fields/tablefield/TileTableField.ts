/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {EventHandler, FormFieldTile, MenuBar, PropertyChangeEvent, TableField} from '../../../index';
import $ from 'jquery';

export class TileTableField extends TableField {
  protected _tableBlurHandler: (event: JQuery.BlurEvent) => void;
  protected _tableFocusHandler: (event: JQuery.FocusEvent) => void;
  protected _menuBarPropertyChangeHandler: EventHandler<PropertyChangeEvent<any, MenuBar>>;
  protected _documentMouseDownHandler: (event: MouseEvent) => void;

  constructor() {
    super();

    this._tableBlurHandler = this._onTableBlur.bind(this);
    this._tableFocusHandler = this._onTableFocus.bind(this);
    this._menuBarPropertyChangeHandler = this._onMenuBarPropertyChange.bind(this);
    this._documentMouseDownHandler = this._onDocumentMouseDown.bind(this);
  }

  protected override _render() {
    super._render();
    if (!this.session.focusManager.restrictedFocusGain) {
      this.$container.document(true).addEventListener('mousedown', this._documentMouseDownHandler, true);
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
      this.table.$container
        .on('blur', this._tableBlurHandler)
        .on('focus', this._tableFocusHandler);
      this.table.menuBar.on('propertyChange', this._menuBarPropertyChangeHandler);
      this._toggleHasMenuBar();
      if (document.activeElement !== this.table.$container[0]) {
        this._hideMenuBar(true);
      }
    }
  }

  protected override _removeTable() {
    if ((this.parent as FormFieldTile).displayStyle !== FormFieldTile.DisplayStyle.DASHBOARD) {
      return;
    }
    if (this.table) {
      this.table.$container
        .off('blur', this._tableBlurHandler)
        .off('focus', this._tableFocusHandler);
      this.table.menuBar.off('propertyChange', this._menuBarPropertyChangeHandler);
    }
    super._removeTable();
  }

  protected _onTableBlur(event: JQuery.BlurEvent) {
    let popup = $('.popup').data('widget');

    // hide menu bar if context menu popup is not attached to TileTableField
    if (!this.has(popup)) {
      this._hideMenuBar(true);
    }
  }

  protected _onTableFocus(event: JQuery.FocusEvent) {
    this._hideMenuBar(false);
  }

  protected _hideMenuBar(hiddenByUi: boolean) {
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
