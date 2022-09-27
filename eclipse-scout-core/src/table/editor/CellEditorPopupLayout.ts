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
import {AbstractLayout, Dimension, graphics} from '../../index';

export default class CellEditorPopupLayout extends AbstractLayout {

  constructor(cellEditorPopup) {
    super();
    this.cellEditorPopup = cellEditorPopup;
    this._field = cellEditorPopup.cell.field;
    this._htmlContainer = cellEditorPopup.htmlComp;
  }

  layout($container) {
    let size,
      htmlField = this._field.htmlComp;

    size = this._htmlContainer.availableSize({
      exact: true
    })
      .subtract(this._htmlContainer.insets())
      .subtract(htmlField.margins());
    htmlField.setSize(size);
  }

  preferredLayoutSize($container) {
    let cellBounds, rowBounds, margin,
      $row = this.cellEditorPopup.row.$row,
      $cell = this.cellEditorPopup.$anchor;

    cellBounds = graphics.bounds($cell, {
      exact: true
    });
    rowBounds = graphics.bounds($row, {
      exact: true
    });
    margin = this.cellEditorPopup.$container.cssMarginLeft();
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
