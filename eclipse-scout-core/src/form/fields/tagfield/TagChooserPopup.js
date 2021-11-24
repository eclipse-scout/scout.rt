/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {lookupField, Popup, scout, TagChooserPopupLayout} from '../../../index';

export default class TagChooserPopup extends Popup {

  constructor() {
    super();

    this.table = null;
    this.windowPaddingX = 0;
    this.windowPaddingY = 0;
  }

  _init(model) {
    super._init(model);

    let column = scout.create('Column', {
      index: 0,
      session: this.session,
      text: 'Tag',
      autoOptimizeWidth: false
    });

    this.table = scout.create('Table', {
      parent: this,
      headerVisible: false,
      autoResizeColumns: true,
      multiSelect: false,
      scrollToSelection: true,
      columns: [column],
      headerMenusEnabled: false,
      textFilterEnabled: false
    });

    this.table.on('rowClick', this._onRowClick.bind(this));
  }

  _createLayout() {
    return new TagChooserPopupLayout(this);
  }

  _render() {
    super._render();

    this.$container
      .addClass('tag-chooser-popup')
      .on('mousedown', this._onContainerMouseDown.bind(this));
    this._renderTable();
  }

  _renderTable() {
    this.table.setVirtual(false);
    this.table.render();

    // Make sure table never gets the focus, but looks focused
    this.table.$container
      .setTabbable(false)
      .addClass('focused');
  }

  setLookupResult(result) {
    let
      tableRows = [],
      lookupRows = result.lookupRows;

    this.table.deleteAllRows();
    lookupRows.forEach(lookupRow => {
      tableRows.push(lookupField.createTableRow(lookupRow));
    }, this);
    this.table.insertRows(tableRows);
  }

  _onRowClick(event) {
    this.triggerLookupRowSelected();
  }

  /**
   * This event handler is called before the mousedown handler on the _document_ is triggered
   * This allows us to prevent the default, which is important for the CellEditorPopup which
   * should stay open when the SmartField popup is closed. It also prevents the focus blur
   * event on the SmartField input-field.
   */
  _onContainerMouseDown(event) {
    // when user clicks on proposal popup with table or tree (prevent default,
    // so input-field does not lose the focus, popup will be closed by the
    // proposal chooser impl.
    return false;
  }

  _isMouseDownOnAnchor(event) {
    return this.field.$field.isOrHas(event.target);
  }

  delegateKeyEvent(event) {
    event.originalEvent.smartFieldEvent = true;
    this.table.$container.trigger(event);
  }

  triggerLookupRowSelected(row) {
    row = row || this.selectedRow();
    if (!row || !row.enabled) {
      return;
    }
    this.trigger('lookupRowSelected', {
      lookupRow: row.lookupRow
    });
  }

  selectedRow() {
    return this.table.selectedRow();
  }
}
