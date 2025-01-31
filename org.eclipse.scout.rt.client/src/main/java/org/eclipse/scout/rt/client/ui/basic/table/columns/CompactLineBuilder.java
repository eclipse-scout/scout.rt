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

import java.util.function.Supplier;

import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.IHeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;

public class CompactLineBuilder {
  private Supplier<IColumn<?>> m_columnSupplier;
  private IColumn<?> m_cachedColumn;
  private boolean m_showLabel;

  public CompactLineBuilder(Supplier<IColumn<?>> columnSupplier) {
    m_columnSupplier = columnSupplier;
    withShowLabel(true);
  }

  public CompactLineBuilder() {
    this(null);
  }

  public void prepare() {
    m_cachedColumn = null;
  }

  public Supplier<IColumn<?>> getColumnSupplier() {
    return m_columnSupplier;
  }

  public IColumn<?> getColumn() {
    if (m_cachedColumn != null) {
      return m_cachedColumn;
    }
    if (getColumnSupplier() == null) {
      return null;
    }
    m_cachedColumn = getColumnSupplier().get();
    return m_cachedColumn;
  }

  public CompactLineBuilder withShowLabel(boolean showLabel) {
    m_showLabel = showLabel;
    return this;
  }

  public boolean isShowLabel() {
    return m_showLabel;
  }

  public boolean accept(IColumn<?> column) {
    return getColumn() == column;
  }

  /**
   * Builds a compact line by using the text of the header cell (column name) as label (if {@link #isShowLabel()} is
   * true) and the text of the actual cell as text.<br>
   * Also enables nl to br conversion if {@link #shouldConvertTextNlToBr(ITable)} returns true <br>
   * If you want to explicitly enable nl to br or influence line creation in another way, you can override this build
   * function and adjust the flags of the label or text block.<br>
   * Available flags: {@link CompactLineBlock#isEncodeHtmlEnabled()} {@link CompactLineBlock#isHtmlToPlainTextEnabled()}
   * {@link CompactLineBlock#isNlToBrEnabled()}
   */
  public CompactLine build(IColumn<?> column, ITableRow row) {
    IHeaderCell headerCell = null;
    if (isShowLabel()) {
      headerCell = column.getHeaderCell();
    }
    ICell cell = row.getCell(column);
    CompactLine line = new CompactLine(headerCell, cell);
    if (shouldConvertTextNlToBr(column.getTable())) {
      line.getTextBlock().setNlToBrEnabled(true);
    }
    return line;
  }

  /**
   * Returns true if multi line is enabled. This is how Table.js itself does it. But if there is a label, don't do it.
   * Reason: If there is a label and there are maybe only 2 lines it looks strange if first line is on the right of the
   * label and the second one on the bottom.
   *
   * @param table
   *     necessary to check the {@link ITable#isMultilineText()} if
   */
  protected boolean shouldConvertTextNlToBr(ITable table) {
    if (!isShowLabel() && table.isMultilineText()) {
      return true;
    }
    return false;
  }
}
