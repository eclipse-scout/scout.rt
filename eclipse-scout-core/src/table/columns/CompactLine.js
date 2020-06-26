/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Cell, CompactLineBlock} from '../../index';

export default class CompactLine {

  constructor(labelOrHeaderCell, textOrCell) {
    /** @type {CompactLineBlock} */
    this.labelBlock = null;
    /** @type {CompactLineBlock} */
    this.textBlock = null;
    if (labelOrHeaderCell instanceof Cell) {
      this.setLabelBlock(this.convertHeaderCellToBlock(labelOrHeaderCell));
    } else {
      this.setLabelBlock(new CompactLineBlock(labelOrHeaderCell));
    }
    if (textOrCell instanceof Cell) {
      this.setTextBlock(this.convertCellToBlock(textOrCell));
    } else {
      this.setTextBlock(new CompactLineBlock(textOrCell));
    }
  }

  convertHeaderCellToBlock(cell) {
    return this.convertCellToBlock(cell);
  }

  convertCellToBlock(cell) {
    let block = new CompactLineBlock();
    if (cell != null) {
      block.setText(cell.text);
      block.setIcon(cell.iconId);
      block.setEncodeHtmlEnabled(!cell.htmlEnabled);
    }
    return block;
  }

  setLabelBlock(block) {
    scout.assertParameter('block', block);
    this.labelBlock = block;
  }

  setTextBlock(block) {
    scout.assertParameter('block', block);
    this.textBlock = block;
  }

  build() {
    let label = this.labelBlock.build();
    if (label) {
      label += ': ';
    }
    let value = this.textBlock.build();
    if (!value) {
      return '';
    }
    return `
<div class="compact-cell-line">
  <span class="compact-cell-line-label">${label}</span>
  <span class="compact-cell-line-value">${value}</span>
</div>`;
  }
}
