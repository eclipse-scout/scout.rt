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
package org.eclipse.scout.rt.ui.swt.basic.table;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.scout.commons.BooleanUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IProposalColumn;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.SwtIcons;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * {@link LabelProvider} for {@link IColumn}s.
 */
public class SwtScoutCellLabelProvider extends CellLabelProvider {
  private final ISwtEnvironment m_environment;
  private final Color m_disabledForeground;
  private final Color m_disabledBackground;
  private final boolean m_useNativeToolTips;

  private Table m_swtTable;
  private TableColumn m_swtColumn;
  private IColumn<?> m_scoutColumn;

  public SwtScoutCellLabelProvider(IColumn<?> scoutColumn, ISwtEnvironment environment) {
    m_scoutColumn = scoutColumn;
    m_environment = environment;
    m_disabledForeground = m_environment.getColor(UiDecorationExtensionPoint.getLookAndFeel().getColorForegroundDisabled());
    m_disabledBackground = m_environment.getColor(UiDecorationExtensionPoint.getLookAndFeel().getColorBackgroundDisabled());
    m_useNativeToolTips = UiDecorationExtensionPoint.getLookAndFeel().useNativeToolTips();
  }

  @Override
  protected void initialize(ColumnViewer viewer, ViewerColumn column) {
    m_swtTable = ((TableViewer) viewer).getTable();
    m_swtColumn = ((TableViewerColumn) column).getColumn();
    super.initialize(viewer, column);
  }

  @Override
  public void update(ViewerCell cell) {
    // In 'SwtScoutTable' an invisible 'TableColumn' is installed as the very first column to address SWT bug 43910.
    // That is why the SWT column index must be decremented prior accessing the Scout column model.
    final int currentColumnIndex = cell.getColumnIndex() - 1;
    final int firstColumnIndex = m_swtTable.getColumnOrder()[1];
    final boolean firstColumn = (currentColumnIndex == firstColumnIndex);

    final ITableRow scoutRow = (ITableRow) cell.getElement();
    final ICell scoutCell = scoutRow.getCell(m_scoutColumn);

    cell.setText(getColumnText(scoutCell));
    cell.setImage(getColumnImage(scoutCell, scoutRow, m_scoutColumn, firstColumn));

    cell.setBackground(getBackground(scoutCell));
    cell.setForeground(getForeground(scoutCell, scoutRow));

    cell.setFont(getFont(scoutCell));
  }

  @Override
  public String getToolTipText(Object element) {
    ITableRow scoutRow = (ITableRow) element;

    ICell scoutCell = scoutRow.getCell(m_scoutColumn);
    String tooltipText = scoutCell.getTooltipText();
    if (StringUtility.isNullOrEmpty(tooltipText)) {
      return null;
    }
    else {
      return tooltipText;
    }
  }

  @Override
  public boolean useNativeToolTip(Object object) {
    return m_useNativeToolTips;
  }

  protected Color getBackground(ICell scoutCell) {
    Color color = m_environment.getColor(scoutCell.getBackgroundColor());
    if (color == null && !scoutCell.isEnabled()) {
      return m_disabledBackground;
    }
    else {
      return color;
    }
  }

  protected Color getForeground(ICell scoutCell, ITableRow scoutRow) {
    Color color = m_environment.getColor(scoutCell.getForegroundColor());
    if (color == null && (!scoutRow.isEnabled() || !scoutCell.isEnabled())) {
      return m_disabledForeground;
    }
    else {
      return color;
    }
  }

  protected Image getColumnImage(ICell scoutCell, ITableRow scoutRow, IColumn<?> scoutColumn, boolean firstColumn) {
    String iconId = null;

    // 1. Checkbox if being the first column of a checkable table.
    if (firstColumn && m_scoutColumn.getTable().isCheckable()) {
      iconId = scoutRow.isChecked() ? SwtIcons.CheckboxYes : SwtIcons.CheckboxNo;
    }
    // 2. Checkbox if being a boolean column.
    else if (scoutColumn.getDataType() == Boolean.class && (!(scoutColumn instanceof IProposalColumn) || ((IProposalColumn) scoutColumn).getLookupCall() == null)) {
      iconId = BooleanUtility.nvl((Boolean) scoutCell.getValue()) ? SwtIcons.CheckboxYes : SwtIcons.CheckboxNo;
    }
    // 3. Error marker in case of a cell error.
    else if (scoutCell.getErrorStatus() != null && scoutCell.getErrorStatus().getSeverity() == IStatus.ERROR) {
      iconId = AbstractIcons.StatusError;
    }
    // 4. Cell icon if set.
    else if (scoutCell.getIconId() != null) {
      iconId = scoutCell.getIconId();
    }
    // 5. Row icon if set and being the first column.
    else if (firstColumn) {
      iconId = scoutRow.getIconId();
    }

    return (iconId != null ? m_environment.getIcon(iconId) : null);
  }

  protected String getColumnText(ICell scoutCell) {
    String text = StringUtility.emptyIfNull(scoutCell.getText());
    if (m_scoutColumn.getTable().isMultilineText()) {
      return text;
    }
    else {
      return StringUtility.removeNewLines(text);
    }
  }

  protected Font getFont(ICell scoutCell) {
    return m_environment.getFont(scoutCell.getFont(), m_swtTable.getFont());
  }
}
