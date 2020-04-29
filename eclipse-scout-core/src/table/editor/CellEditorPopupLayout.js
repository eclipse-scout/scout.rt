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
    cellBounds.x += $cell.cssMarginX(); // first cell popup has a negative left margin
    rowBounds = graphics.bounds($row, {
      exact: true
    });
    rowBounds.y += $row.cssMarginY(); // row has a negative top margin
    margin = this.cellEditorPopup.$container.cssMarginLeft();
    if (margin < 0) {
      // extend the width if the popup has a negative margin (used for the first cell)
      cellBounds.width = cellBounds.width + -margin;
    }
    return new Dimension(cellBounds.width, rowBounds.height);
  }
}
