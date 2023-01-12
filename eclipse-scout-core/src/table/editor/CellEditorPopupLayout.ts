/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, CellEditorPopup, Dimension, graphics, HtmlComponent, HtmlCompPrefSizeOptions, ValueField} from '../../index';

export class CellEditorPopupLayout<TValue> extends AbstractLayout {
  cellEditorPopup: CellEditorPopup<TValue>;
  protected _field: ValueField<TValue>;
  protected _htmlContainer: HtmlComponent;

  constructor(cellEditorPopup: CellEditorPopup<TValue>) {
    super();
    this.cellEditorPopup = cellEditorPopup;
    this._field = cellEditorPopup.cell.field;
    this._htmlContainer = cellEditorPopup.htmlComp;
  }

  override layout($container: JQuery) {
    let htmlField = this._field.htmlComp;
    let size = this._htmlContainer.availableSize({exact: true})
      .subtract(this._htmlContainer.insets())
      .subtract(htmlField.margins());
    htmlField.setSize(size);
  }

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    let $row = this.cellEditorPopup.row.$row,
      $cell = this.cellEditorPopup.$anchor;
    let cellBounds = graphics.bounds($cell, {exact: true});
    let rowBounds = graphics.bounds($row, {exact: true});
    let margin = this.cellEditorPopup.$container.cssMarginLeft();
    if (margin < 0) {
      // extend the width if the popup has a negative margin
      cellBounds.width = cellBounds.width + -margin;
    }
    let selectionHeight = this.cellEditorPopup._rowSelectionBounds().height;
    if (selectionHeight) {
      // Use height of selection if available (the selection may be larger than the row if it covers the border of the previous row)
      this._htmlContainer.$comp.toggleClass('overflow-bottom', selectionHeight > rowBounds.height && this._htmlContainer.$comp.cssMarginTop() === 0);
      rowBounds.height = selectionHeight;
    }
    return new Dimension(cellBounds.width, rowBounds.height);
  }
}
