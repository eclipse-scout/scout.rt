/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import static org.eclipse.scout.rt.platform.html.HTML.*;

import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.IHeaderCell;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;

public class CompactLine {

  private CompactLineBlock m_labelBlock;
  private CompactLineBlock m_textBlock;

  public CompactLine() {
    setLabelBlock(new CompactLineBlock());
    setTextBlock(new CompactLineBlock());
  }

  public CompactLine(String label, String text) {
    setLabelBlock(new CompactLineBlock(label, null));
    setTextBlock(new CompactLineBlock(text, null));
  }

  public CompactLine(IHeaderCell headerCell, ICell cell) {
    setLabelBlock(convertHeaderCellToBlock(headerCell));
    setTextBlock(convertCellToBlock(cell));
  }

  protected CompactLineBlock convertHeaderCellToBlock(IHeaderCell cell) {
    CompactLineBlock block = new CompactLineBlock();
    if (cell != null) {
      block.setText(cell.getText());
      block.setIcon(cell.getIconId());
      block.setEncodeHtmlEnabled(!cell.isHtmlEnabled());
    }
    return block;
  }

  protected CompactLineBlock convertCellToBlock(ICell cell) {
    CompactLineBlock block = new CompactLineBlock();
    if (cell != null) {
      block.setText(cell.getText());
      block.setIcon(cell.getIconId());
      block.setEncodeHtmlEnabled(!cell.isHtmlEnabled());
    }
    return block;
  }

  public CompactLineBlock getLabelBlock() {
    return m_labelBlock;
  }

  public void setLabelBlock(CompactLineBlock block) {
    Assertions.assertNotNull(block);
    m_labelBlock = block;
  }

  public CompactLineBlock getTextBlock() {
    return m_textBlock;
  }

  public void setTextBlock(CompactLineBlock block) {
    Assertions.assertNotNull(block);
    m_textBlock = block;
  }

  public String build() {
    String label = getLabelBlock().build();
    if (StringUtility.hasText(label)) {
      label += ": ";
    }
    String value = getTextBlock().build();
    if (!StringUtility.hasText(value)) {
      return "";
    }
    return HTML.div(
            span(raw(label)).cssClass("compact-cell-line-label"),
            span(raw(value)).cssClass("compact-cell-line-value"))
        .cssClass("compact-cell-line").toHtml();
  }
}
