/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields;

public class LogicalGridLayoutConfig {
  private int m_columnWidth = -1;
  private int m_rowHeight = -1;
  private int m_hgap = -1;
  private int m_vgap = -1;
  private int m_minWidth = 0;

  public LogicalGridLayoutConfig() {
  }

  public LogicalGridLayoutConfig(LogicalGridLayoutConfig config) {
    setHGap(config.getHGap());
    setVGap(config.getVGap());
    setColumnWidth(config.getColumnWidth());
    setRowHeight(config.getRowHeight());
    setMinWidth(config.getMinWidth());
  }

  /**
   * @return the width in pixels to use for elements with the logical unit "width = 1". Larger logical widths are
   *         multiplied with this value (and gaps are added).
   */
  public int getColumnWidth() {
    return m_columnWidth;
  }

  /**
   * @param columnWidth
   *          the width in pixels to use for elements with the logical unit "width = 1". Larger logical widths are
   *          multiplied with this value (and gaps are added).
   */
  public void setColumnWidth(int columnWidth) {
    m_columnWidth = columnWidth;
  }

  /**
   * @see #setColumnWidth(int)
   */
  public LogicalGridLayoutConfig withColumnWidth(int columnWidth) {
    setColumnWidth(columnWidth);
    return this;
  }

  /**
   * @return the height in pixels to use for elements with the logical unit "height = 1". Larger logical heights are
   *         multiplied with this value (and gaps are added).
   */
  public int getRowHeight() {
    return m_rowHeight;
  }

  /**
   * @param rowHeight
   *          the height in pixels to use for elements with the logical unit "height = 1". Larger logical heights are
   *          multiplied with this value (and gaps are added).
   */
  public void setRowHeight(int rowHeight) {
    m_rowHeight = rowHeight;
  }

  /**
   * @see #setRowHeight(int)
   */
  public LogicalGridLayoutConfig withRowHeight(int rowHeight) {
    setRowHeight(rowHeight);
    return this;
  }

  /**
   * @return the horizontal gap in pixels to use between two logical grid columns.
   */
  public int getHGap() {
    return m_hgap;
  }

  /**
   * @param hgap
   *          the horizontal gap in pixels to use between two logical grid columns.
   */
  public void setHGap(int hgap) {
    m_hgap = hgap;
  }

  /**
   * @see #setHGap(int)
   */
  public LogicalGridLayoutConfig withHGap(int hgap) {
    setHGap(hgap);
    return this;
  }

  /**
   * @return the vertical gap in pixels to use between two logical grid rows.
   */
  public int getVGap() {
    return m_vgap;
  }

  /**
   * @param vgap
   *          the vertical gap in pixels to use between two logical grid rows.
   */
  public void setVGap(int vgap) {
    m_vgap = vgap;
  }

  /**
   * @see #setVGap(int)
   */
  public LogicalGridLayoutConfig withVGap(int vgap) {
    setVGap(vgap);
    return this;
  }

  /**
   * @return the minimum width of the widget. If this width is > 0 a horizontal scrollbar is shown when the widgets gets
   *         smaller than this value.
   */
  public int getMinWidth() {
    return m_minWidth;
  }

  /**
   * @param minWidth
   *          the minimum width of the widget. If this width is > 0 a horizontal scrollbar is shown when the widget gets
   *          smaller than this value.
   */
  public void setMinWidth(int minWidth) {
    m_minWidth = minWidth;
  }

  /**
   * @see #setMinWidth(int)
   */
  public LogicalGridLayoutConfig withMinWidth(int minWidth) {
    setMinWidth(minWidth);
    return this;
  }

  public LogicalGridLayoutConfig copy() {
    return new LogicalGridLayoutConfig(this);
  }
}
