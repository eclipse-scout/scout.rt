/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.basic.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.basic.table.IHeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ICustomColumn;
import org.eclipse.scout.rt.ui.swing.Activator;
import org.eclipse.scout.rt.ui.swing.SwingIcons;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.icons.CompositeIcon;

public class SwingTableHeaderCellRenderer implements TableCellRenderer {
  private static final long serialVersionUID = 1L;

  private TableCellRenderer m_internalRenderer;
  private SwingScoutTable m_swingScoutTable;
  private Icon m_sortUpIcon;
  private Icon m_sortDownIcon;
  private Icon m_filterActiveIcon;
  private Icon m_customColumnIcon;

  public SwingTableHeaderCellRenderer(TableCellRenderer internalRenderer, SwingScoutTable t) {
    super();
    m_internalRenderer = internalRenderer;
    m_swingScoutTable = t;
    m_sortUpIcon = Activator.getIcon(SwingIcons.TableSortAsc);
    m_sortDownIcon = Activator.getIcon(SwingIcons.TableSortDesc);
    m_filterActiveIcon = Activator.getIcon(SwingIcons.TableColumnFilterActive);
    m_customColumnIcon = Activator.getIcon(SwingIcons.TableCustomColumn);
  }

  @Override
  public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean hasFocus, int rowIndex, int colIndex) {
    Component comp = m_internalRenderer.getTableCellRendererComponent(t, value, selected, hasFocus, rowIndex, colIndex);
    JLabel label = (comp instanceof JLabel ? (JLabel) comp : null);
    if (label == null) {
      return comp;
    }
    //
    JTableHeader header = (t != null ? t.getTableHeader() : null);
    if (header != null) {
      label.setForeground(header.getForeground());
      label.setBackground(header.getBackground());
      label.setFont(header.getFont());
    }
    label.setVerticalAlignment(SwingConstants.TOP);
    // scout cell
    JTable swingTable = m_swingScoutTable.getSwingTable();
    ITable scoutTable = m_swingScoutTable.getScoutObject();
    IColumn scoutCol = ((SwingTableColumn) swingTable.getColumnModel().getColumn(colIndex)).getScoutColumn();
    if (scoutTable != null) {
      IHeaderCell cell = scoutTable.getHeaderCell(scoutCol);
      // align
      int align = cell.getHorizontalAlignment();
      // first column always left-aligned
      if (colIndex == 0 && !StringUtility.isNullOrEmpty(cell.getIconId())) {
        align = -1;
      }
      if (align > 0) {
        label.setHorizontalAlignment(JLabel.RIGHT);
      }
      else if (align == 0) {
        label.setHorizontalAlignment(JLabel.CENTER);
      }
      else {
        label.setHorizontalAlignment(JLabel.LEFT);
      }
      // tooltip
      label.setToolTipText(SwingUtility.createHtmlLabelText(cell.getTooltipText(), true));
      // value
      String text = value != null ? value.toString() : null;
      if (SwingUtility.isMultilineLabelText(text)) {
        text = SwingUtility.createHtmlLabelText(text, false);
      }
      label.setText(text);
      // icon
      Icon sortIcon = null;
      if (cell.isSortActive() && cell.isSortExplicit()) {
        sortIcon = SortIconUtility.createSortIcon(scoutCol, scoutTable.getColumnSet().getSortColumns(), cell.isSortAscending());
      }
      Icon filterIcon = null;
      if (scoutCol.isColumnFilterActive()) {
        filterIcon = m_filterActiveIcon;
      }
      Icon customColumnIcon = null;
      if (scoutCol instanceof ICustomColumn) {
        customColumnIcon = m_customColumnIcon;
      }
      label.setIcon(null);
      if (sortIcon != null || filterIcon != null || customColumnIcon != null) {
        label.setIcon(new CompositeIcon(0, sortIcon, filterIcon, customColumnIcon));
      }
      // background
      if (cell.getBackgroundColor() != null) {
        Color color = SwingUtility.createColor(cell.getBackgroundColor());
        if (selected) {
          color = color.darker();
        }
        label.setBackground(color);
      }
      // foreground
      if (cell.getForegroundColor() != null) {
        Color color = SwingUtility.createColor(cell.getForegroundColor());
        if (selected) {
          color = color.brighter();
        }
        label.setForeground(color);
      }
      // font
      if (cell.getFont() != null) {
        Font oldf = label.getFont();
        Font newf = SwingUtility.createFont(cell.getFont(), oldf);
        if (oldf != null) {// only override font style, not size and face
          label.setFont(new Font(oldf.getName(), newf.getStyle(), oldf.getSize()));
        }
      }
      // enabled
      label.setEnabled(scoutTable.isEnabled());
    }
    return label;
  }
}
