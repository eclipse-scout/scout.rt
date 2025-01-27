/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  AbstractLayout, Cell, CellEditorCancelEditKeyStroke, CellEditorCompleteEditKeyStroke, CellEditorPopupLayout, CellEditorPopupModel, CellEditorTabKeyStroke, Column, EventHandler, events, FormField, graphics, InitModelOf, KeyStroke,
  KeyStrokeManagerKeyStrokeEvent, Point, Popup, Rectangle, scout, SomeRequired, Table, TableRow, TableRowOrderChangedEvent, ValueField
} from '../../index';
import $ from 'jquery';

export class CellEditorPopup<TValue> extends Popup implements CellEditorPopupModel<TValue> {
  declare model: CellEditorPopupModel<TValue>;
  declare initModel: SomeRequired<this['model'], 'parent' | 'column' | 'cell'>;

  table: Table;
  column: Column<TValue>;
  row: TableRow;
  cell: Cell<TValue>;
  protected _rowOrderChangedFunc: EventHandler<TableRowOrderChangedEvent>;
  protected _pendingCompleteCellEdit: JQuery.Promise<void>;
  protected _keyStrokeHandler: EventHandler<KeyStrokeManagerKeyStrokeEvent>;

  constructor() {
    super();
    this.table = null;
    this.column = null;
    this.row = null;
    this.cell = null;
    this._pendingCompleteCellEdit = null;
    this._keyStrokeHandler = this._onKeyStroke.bind(this);
  }

  protected override _init(options: InitModelOf<this>) {
    options.scrollType = options.scrollType || 'position';
    super._init(options);

    this.table = options.column.table;
    this.link(this.cell.field);
  }

  protected override _createLayout(): AbstractLayout {
    return new CellEditorPopupLayout(this);
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStrokes([
      new CellEditorCompleteEditKeyStroke(this),
      new CellEditorTabKeyStroke(this)
    ]);
  }

  protected override _createCloseKeyStroke(): KeyStroke {
    return new CellEditorCancelEditKeyStroke(this);
  }

  protected override _open($parent: JQuery) {
    this.render($parent);
    this.position();
    this.pack();
  }

  protected override _getDefaultOpen$Parent(): JQuery {
    return this.table.$data;
  }

  protected override _render() {
    super._render();

    // determine CSS class for first and last column, required for additional margins/padding in cell-editor
    let cssClass = '',
      visibleCols = this.table.visibleColumns(),
      colPos = visibleCols.indexOf(this.column);
    if (colPos === 0) { // first cell
      cssClass = 'first';
    } else if (colPos === visibleCols.length - 1) {
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
  protected _alignWithSelection() {
    let selectionTop = this._rowSelectionBounds().y;
    if (selectionTop < 0) {
      this.$container.cssMarginTop(selectionTop);
      this.$container.addClass('overflow-top');
    }
  }

  /** @internal */
  _rowSelectionBounds(): Rectangle {
    let bounds = new Rectangle();
    let style = getComputedStyle(this.row.$row[0], ':after');
    if (style) {
      bounds = new Rectangle($.pxToNumber(style['left']), $.pxToNumber(style['top']), $.pxToNumber(style['width']), $.pxToNumber(style['height']));
    }
    return bounds;
  }

  protected override _postRender() {
    super._postRender(); // installs the focus context for this popup

    // If applicable, invoke the field's function 'onCellEditorRendered' to signal the cell-editor to be rendered.
    let field = this.cell.field as ValueFieldWithCellEditorRenderedCallback<TValue>;
    if (field.onCellEditorRendered) {
      field.onCellEditorRendered({
        openFieldPopup: this.table.openFieldPopupOnCellEdit,
        cellEditorPopup: this
      });
    }
  }

  protected override _remove() {
    super._remove(); // uninstalls the focus context for this popup

    this.session.keyStrokeManager.off('keyStroke', this._keyStrokeHandler);
    this.table.off('rowOrderChanged', this._rowOrderChangedFunc);
    // table may have been removed in the meantime
    if (this.table.rendered) {
      this.table.$container.removeClass('focused');
    }
    this.$anchor.css('visibility', '');
  }

  override position(switchIfNecessary?: boolean) {
    if (!this.rendered) {
      return;
    }

    this._alignWithSelection();

    let $cell = this.$anchor;
    let cellBounds = graphics.bounds($cell);
    let $row = this.row.$row;
    let rowBounds = graphics.bounds($row);
    let $tableData = this.table.$data;
    let insetsLeft = $tableData.cssPaddingLeft() + $row.cssMarginLeft() + $row.cssBorderLeftWidth();
    this.setLocation(new Point(insetsLeft + cellBounds.x, $tableData.scrollTop() + rowBounds.y));
  }

  /**
   * @param waitForAcceptInput default is true
   * @returns A promise resolved when acceptInput is performed on the editor field
   */
  completeEdit(waitForAcceptInput?: boolean): JQuery.Promise<any> {
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
      this._pendingCompleteCellEdit = acceptInputPromise.then(() => this.table.completeCellEdit());
    }

    this._pendingCompleteCellEdit.then(() => {
      // Ensure complete will never be called more than once
      this._pendingCompleteCellEdit = $.resolvedPromise();
    });

    return this._pendingCompleteCellEdit;
  }

  isCompleteCellEditRequested(): boolean {
    return !!this._pendingCompleteCellEdit;
  }

  cancelEdit() {
    this.table.cancelCellEdit();
    this.remove();
  }

  protected override _onMouseDownOutside(event: MouseEvent) {
    let $clickedRow = $(event.target).closest('.table-row', this.table.$container[0]);
    this.completeEdit();

    // When the edit completes the edited row is updated and replaced with new html elements.
    // When the user clicks on a cell of such a row that will be updated in order to complete the edit, the mouse down handler of the table won't be triggered.
    // The mouse up handler will be triggered but does nothing because _$mouseDownRow is not set (which would be done by the mouse down handler).
    // To make sure the new cell editor opens correctly we need to delegate the event to the new row that should receive the click to ensure table._onRowMouseDown is executed.
    if ($clickedRow.length > 0 && !$clickedRow.isAttached()) {
      this._propagateMouseDownToTableRow(event);
    }
  }

  protected _propagateMouseDownToTableRow(event: MouseEvent) {
    let doc = this.table.$container.document(true);
    let clickedElement = doc.elementFromPoint(event.pageX, event.pageY) as HTMLElement;
    let $target = $(clickedElement);
    let $clickedRow = $target.closest('.table-row', this.table.$container[0]);
    if ($clickedRow.length === 0) {
      return;
    }
    events.propagateEvent($target[0], event);
  }

  protected _onKeyStroke(event: KeyStrokeManagerKeyStrokeEvent) {
    if (!this._invokeCompleteEditBeforeKeyStroke(event)) {
      return;
    }
    // Make sure completeEdit is called immediately after calling acceptInput.
    // Otherwise, the keystroke will be executed before completing the edit which prevents the input from being saved
    this.completeEdit(false);
  }

  protected _invokeCompleteEditBeforeKeyStroke(event: KeyStrokeManagerKeyStrokeEvent): boolean {
    if (!this.session.keyStrokeManager.invokeAcceptInputOnActiveValueField(event.keyStroke, event.keyStrokeContext)) {
      return false;
    }
    let $target = event.keyStrokeContext.$getScopeTarget();
    if (this.$container.isOrHas($target)) {
      // Don't interfere with keystrokes of the popup or children of the popup (otherwise pressing enter would close both the popup and the form at once)
      return false;
    }
    // Not all elements created by the popup are necessarily descendants of the cell editor popup. An example
    // would be a form that is opened from within the popup. To identify these kind of elements, we additionally
    // check the widget hierarchy.
    let target = widgets.get($target);
    if (this.cell.field.isOrHas(target)) {
      return false; // event target belongs to the cell editor popup -> don't close the popup
    }
    return true;
  }

  waitForCompleteCellEdit(): JQuery.Promise<void> {
    if (this._pendingCompleteCellEdit) {
      return this._pendingCompleteCellEdit.promise();
    }
    return $.resolvedPromise();
  }
}

export type CellEditorRenderedOptions<TValue> = {
  openFieldPopup: boolean;
  cellEditorPopup: CellEditorPopup<TValue>;
};

export interface ValueFieldWithCellEditorRenderedCallback<TValue extends TModelValue, TModelValue = TValue> extends ValueField<TValue, TModelValue> {
  onCellEditorRendered(options: CellEditorRenderedOptions<TValue>): void;
}
