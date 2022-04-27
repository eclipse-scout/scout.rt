/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {CellEditorCancelEditKeyStroke, CellEditorCompleteEditKeyStroke, CellEditorPopupLayout, CellEditorTabKeyStroke, events, FormField, graphics, Point, Popup, Rectangle, scout} from '../../index';
import $ from 'jquery';

export default class CellEditorPopup extends Popup {

  constructor() {
    super();
    this.table = null;
    this.column = null;
    this.row = null;
    this.cell = null;
    this._pendingCompleteCellEdit = null;
    this._keyStrokeHandler = this._onKeyStroke.bind(this);
  }

  _init(options) {
    options.scrollType = options.scrollType || 'position';
    super._init(options);

    this.table = options.column.table;
    this.link(this.cell.field);
  }

  _createLayout() {
    return new CellEditorPopupLayout(this);
  }

  /**
   * @override
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStroke([
      new CellEditorCompleteEditKeyStroke(this),
      new CellEditorTabKeyStroke(this)
    ]);
  }

  /**
   * @override
   */
  _createCloseKeyStroke() {
    return new CellEditorCancelEditKeyStroke(this);
  }

  /**
   * @override
   */
  _open($parent, event) {
    this.render($parent, event);
    this.position();
    this.pack();
  }

  /**
   * @override
   */
  _getDefaultOpen$Parent() {
    return this.table.$data;
  }

  _render() {
    super._render();

    // determine CSS class for first and last column, required for additional margins/padding in cell-editor
    let cssClass = '',
      visibleCols = this.table.visibleColumns(),
      colPos = visibleCols.indexOf(this.column);
    if (colPos === 0) { // first cell
      cssClass = 'first';
    } else if (colPos === visibleCols.length - 1) { // last cell
      cssClass = 'last';
    }

    this.$container
      .addClass('cell-editor-popup ' + cssClass)
      .data('popup', this);

    let field = this.cell.field;
    field.mode = FormField.Mode.CELLEDITOR; // hint that this field is used within a cell-editor
    field.render();
    field.prepareForCellEdit({
      cssClass: cssClass
    });

    // Make sure cell content is not visible while the editor is open (especially necessary for transparent editors like checkboxes)
    this.$anchor.css('visibility', 'hidden');

    this._rowOrderChangedFunc = event => {
      if (event.animating) {
        // row is only set while animating
        if (event.row === this.row) {
          this.position();
        }
      } else {
        this.position();
      }
    };
    this.table.on('rowOrderChanged', this._rowOrderChangedFunc);
    // Set table style to focused, so that it looks as it still has the focus.
    // This prevents flickering if the cell editor gets opened, especially when tabbing to the next cell editor.
    if (this.table.enabled) {
      this.table.$container.addClass('focused');
    }
    this.session.keyStrokeManager.on('keyStroke', this._keyStrokeHandler);
  }

  /**
   * Selection border is an after element that is moved to top a little to cover the border of the previous row.
   * This won't happen for the first row if there is no table header, since there is no space on top to move it up.
   * In that case the selection is moved down by 1px to ensure the height of the selection always stays the same.
   * If there is no border between the rows, there is no adjustment necessary, the selection is as height as the row.
   * -> Position and size of the cell editor popup depends on the selection of the current row and the table style (with or without row borders)
   */
  _alignWithSelection() {
    let selectionTop = this._rowSelectionBounds().y;
    if (selectionTop < 0) {
      this.$container.cssMarginTop(selectionTop);
      this.$container.addClass('overflow-top');
    }
  }

  _rowSelectionBounds() {
    let bounds = new Rectangle();
    let style = getComputedStyle(this.row.$row[0], ':after');
    if (style) {
      bounds = new Rectangle($.pxToNumber(style['left']), $.pxToNumber(style['top']), $.pxToNumber(style['width']), $.pxToNumber(style['height']));
    }
    return bounds;
  }

  _postRender() {
    super._postRender(); // installs the focus context for this popup

    // If applicable, invoke the field's function 'onCellEditorRendered' to signal the cell-editor to be rendered.
    let field = this.cell.field;
    if (field.onCellEditorRendered) {
      field.onCellEditorRendered({
        openFieldPopup: this.table.openFieldPopupOnCellEdit,
        cellEditorPopup: this
      });
    }
  }

  _remove() {
    super._remove(); // uninstalls the focus context for this popup

    this.session.keyStrokeManager.off('keyStroke', this._keyStrokeHandler);
    this.table.off('rowOrderChanged', this._rowOrderChangedFunc);
    // table may have been removed in the meantime
    if (this.table.rendered) {
      this.table.$container.removeClass('focused');
    }
    this.$anchor.css('visibility', '');
  }

  position() {
    let $tableData = this.table.$data,
      $row = this.row.$row,
      $cell = this.$anchor,
      insetsLeft = $tableData.cssPaddingLeft() + $row.cssMarginLeft() + $row.cssBorderLeftWidth();

    this._alignWithSelection();

    let cellBounds = graphics.bounds($cell);
    let rowBounds = graphics.bounds($row);
    this.setLocation(new Point(insetsLeft + cellBounds.x, $tableData.scrollTop() + rowBounds.y));
  }

  /**
   * @returns {Promise} resolved when acceptInput is performed on the editor field
   */
  completeEdit(waitForAcceptInput) {
    if (this._pendingCompleteCellEdit) {
      // Make sure complete cell edit does not get sent twice since it will lead to exceptions. This may happen if user clicks very fast multiple times.
      return this._pendingCompleteCellEdit;
    }

    // There is no blur event when the popup gets closed -> trigger blur so that the field may react (accept display text, close popups etc.)
    // When acceptInput returns a promise, we must wait until input is accepted
    // Otherwise call completeEdit immediately, also call it immediately if waitForAcceptInput is false (see _onKeyStroke)
    let field = this.cell.field;
    let acceptInputPromise = field.acceptInput();
    if (!acceptInputPromise || !scout.nvl(waitForAcceptInput, true)) {
      this._pendingCompleteCellEdit = $.resolvedPromise();
      this.table.completeCellEdit();
    } else {
      this._pendingCompleteCellEdit = acceptInputPromise.then(() => {
        this.table.completeCellEdit();
      });
    }

    this._pendingCompleteCellEdit.then(() => {
      // Ensure complete will never be called more than once
      this._pendingCompleteCellEdit = $.resolvedPromise();
    });

    return this._pendingCompleteCellEdit;
  }

  isCompleteCellEditRequested() {
    return !!this._pendingCompleteCellEdit;
  }

  cancelEdit() {
    this.table.cancelCellEdit();
    this.remove();
  }

  _onMouseDownOutside(event) {
    let $clickedRow = $(event.target).closest('.table-row', this.table.$container[0]);
    // noinspection JSIgnoredPromiseFromCall
    this.completeEdit();

    // When the edit completes the edited row is updated and replaced with new html elements.
    // When the user clicks on a cell of such a row that will be updated in order to complete the edit, the mouse down handler of the table won't be triggered.
    // The mouse up handler will be triggered but does nothing because _$mouseDownRow is not set (which would be done by the mouse down handler).
    // To make sure the new cell editor opens correctly we need to delegate the event to the new row that should receive the click to ensure table._onRowMouseDown is executed.
    if ($clickedRow.length > 0 && !$clickedRow.isAttached()) {
      this._propagateMouseDownToTableRow(event);
    }
  }

  _propagateMouseDownToTableRow(event) {
    let doc = this.table.$container.document(true);
    let $target = $(doc.elementFromPoint(event.pageX, event.pageY));
    let $clickedRow = $target.closest('.table-row', this.table.$container[0]);
    if ($clickedRow.length === 0) {
      return;
    }
    events.propagateEvent($target[0], event);
  }

  _onKeyStroke(event) {
    if (!this._invokeCompleteEditBeforeKeyStroke(event)) {
      return;
    }
    // Make sure completeEdit is called immediately after calling acceptInput.
    // Otherwise the key stroke will be executed before completing the edit which prevents the input from being saved
    // noinspection JSIgnoredPromiseFromCall
    this.completeEdit(false);
  }

  _invokeCompleteEditBeforeKeyStroke(event) {
    if (!this.session.keyStrokeManager.invokeAcceptInputOnActiveValueField(event.keyStroke, event.keyStrokeContext)) {
      return false;
    }
    if (this.$container.isOrHas(event.keyStrokeContext.$getScopeTarget())) {
      // Don't interfere with key strokes of the popup or children of the popup (otherwise pressing enter would close both the popup and the form at once)
      return false;
    }
    return true;
  }

  waitForCompleteCellEdit() {
    if (this._pendingCompleteCellEdit) {
      return this._pendingCompleteCellEdit.promise();
    }
    return $.resolvedPromise();
  }
}
