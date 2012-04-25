/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.basic.table;

import java.util.HashMap;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.ui.rap.RwtIcons;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.rap.util.HtmlTextUtility;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class RwtScoutTableModel implements IRwtScoutTableModelForPatch {
  private static final long serialVersionUID = 1L;

  private transient ListenerList listenerList = null;
  private final ITable m_scoutTable;
  private HashMap<ITableRow, HashMap<IColumn<?>, ICell>> m_cachedCells;
  private final RwtScoutTable m_uiTable;
  private final TableColumnManager m_columnManager;
  private Image m_imgCheckboxFalse;
  private Image m_imgCheckboxTrue;
  private Color m_disabledForegroundColor;
  private boolean m_multiline;

  public RwtScoutTableModel(ITable scoutTable, RwtScoutTable uiTable, TableColumnManager columnManager) {
    m_scoutTable = scoutTable;
    m_uiTable = uiTable;
    m_columnManager = columnManager;
    m_imgCheckboxTrue = m_uiTable.getUiEnvironment().getIcon(RwtIcons.CheckboxYes);
    m_imgCheckboxFalse = m_uiTable.getUiEnvironment().getIcon(RwtIcons.CheckboxNo);
    m_disabledForegroundColor = m_uiTable.getUiEnvironment().getColor(UiDecorationExtensionPoint.getLookAndFeel().getColorForegroundDisabled());
    rebuildCache();
  }

  @Override
  public void setMultiline(boolean multiline) {
    m_multiline = multiline;
  }

  @Override
  public boolean isMultiline() {
    return m_multiline;
  }

  @Override
  public Object[] getElements(Object inputElement) {
    if (m_scoutTable != null) {
      return m_scoutTable.getFilteredRows();
    }
    else {
      return new Object[0];
    }
  }

  @Override
  public Color getBackground(Object element, int columnIndex) {

    if (columnIndex > 0) {
      ICell cell = getCell(element, columnIndex);
      if (cell != null) {
        return m_uiTable.getUiEnvironment().getColor(cell.getBackgroundColor());
      }
    }
    return null;
  }

  @Override
  public Color getForeground(Object element, int columnIndex) {
    if (columnIndex > 0) {
      ITableRow scoutRow = (ITableRow) element;
      ICell cell = getCell(element, columnIndex);
      if (cell != null) {
        Color col = m_uiTable.getUiEnvironment().getColor(cell.getForegroundColor());
        if (col == null) {
          if (!scoutRow.isEnabled() || !cell.isEnabled()) {
            col = m_disabledForegroundColor;
          }
        }
        return col;
      }
    }
    return null;
  }

  @Override
  public Image getColumnImage(Object element, int columnIndex) {
    int[] columnOrder = m_uiTable.getUiField().getColumnOrder();
    if (columnOrder.length <= 1) {
      return null;
    }
    IColumn col = m_columnManager.getColumnByModelIndex(columnIndex - 1);
    ICell cell = getCell(element, columnIndex);
    //checkbox
    Image checkBoxImage = null;
    if (columnOrder[1] == columnIndex && m_uiTable.getScoutObject() != null && m_uiTable.getScoutObject().isCheckable()) {
      if (((ITableRow) element).isChecked()) {
        checkBoxImage = m_imgCheckboxTrue;
      }
      else {
        checkBoxImage = m_imgCheckboxFalse;
      }
    }
    else if (col != null && cell != null && col.getDataType() == Boolean.class && (!(col instanceof ISmartColumn) || ((ISmartColumn) col).getLookupCall() == null)) {
      Boolean b = (Boolean) cell.getValue();
      if (b != null && b.booleanValue()) {
        checkBoxImage = m_imgCheckboxTrue;
      }
      else {
        checkBoxImage = m_imgCheckboxFalse;
      }
    }
    //deco
    String iconId = null;
    if (cell != null && cell.getIconId() != null) {
      iconId = cell.getIconId();
    }
    else if (columnOrder[1] == columnIndex) {
      ITableRow row = (ITableRow) element;
      iconId = row.getIconId();
    }
    Image decoImage = m_uiTable.getUiEnvironment().getIcon(iconId);
    //merge
    if (checkBoxImage != null && decoImage != null) {
      //TODO rap/rwt: new GC(Image) is not possible since in rwt an image does not implement Drawable.
      return checkBoxImage;
    }
    if (checkBoxImage != null) {
      return checkBoxImage;
    }
    if (decoImage != null) {
      return decoImage;
    }
    return null;
  }

  @Override
  public String getColumnText(Object element, int columnIndex) {
    if (columnIndex > 0) {
      ICell cell = getCell(element, columnIndex);
      if (cell != null) {
        String text = cell.getText();
        if (text == null) {
          text = "";
        }
        if (HtmlTextUtility.isTextWithHtmlMarkup(text)) {
          text = m_uiTable.getUiEnvironment().adaptHtmlCell(m_uiTable, text);
          text = m_uiTable.getUiEnvironment().convertLinksWithLocalUrlsInHtmlCell(m_uiTable, text);
        }
        else if (text.indexOf("\n") >= 0) {
          if (isMultiline()) {
            //transform to html
            text = "<html>" + HtmlTextUtility.transformPlainTextToHtml(text) + "</html>";
            text = m_uiTable.getUiEnvironment().adaptHtmlCell(m_uiTable, text);
          }
          else {
            text = StringUtility.replace(text, "\n", " ");
          }
        }
        return text;
      }
    }
    return "";
  }

  @Override
  public Font getFont(Object element, int columnIndex) {
    if (columnIndex > 0) {
      ICell cell = getCell(element, columnIndex);
      if (cell != null) {
        return m_uiTable.getUiEnvironment().getFont(cell.getFont(), m_uiTable.getUiField().getFont());
      }
    }
    return null;
  }

  @Override
  public boolean isLabelProperty(Object element, String property) {
    return false;
  }

  @Override
  public void addListener(ILabelProviderListener listener) {
    if (listenerList == null) {
      listenerList = new ListenerList(ListenerList.IDENTITY);
    }
    listenerList.add(listener);
  }

  @Override
  public void removeListener(ILabelProviderListener listener) {
    if (listenerList != null) {
      listenerList.remove(listener);
      if (listenerList.isEmpty()) {
        listenerList = null;
      }
    }
  }

  private Object[] getListeners() {
    final ListenerList list = listenerList;
    if (list == null) {
      return new Object[0];
    }

    return list.getListeners();
  }

  @Override
  public void dispose() {
    if (listenerList != null) {
      listenerList.clear();
    }
  }

  protected void fireLabelProviderChanged(final LabelProviderChangedEvent event) {
    Object[] listeners = getListeners();
    for (int i = 0; i < listeners.length; ++i) {
      final ILabelProviderListener l = (ILabelProviderListener) listeners[i];
      SafeRunnable.run(new SafeRunnable() {
        private static final long serialVersionUID = 1L;

        @Override
        public void run() {
          l.labelProviderChanged(event);
        }
      });
    }
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }

  @Override
  public void consumeTableModelEvent(RwtScoutTableEvent uiTableEvent) {
    rebuildCache();
  }

  protected ICell getCell(Object row, int colIndex) {
    IColumn<?> column = m_columnManager.getColumnByModelIndex(colIndex - 1);
    if (column != null) {
      if (m_cachedCells.get(row) == null) {
        rebuildCache();
      }
      return m_cachedCells.get(row).get(column);
    }
    else {
      return null;
    }
  }

  private void rebuildCache() {
    m_cachedCells = new HashMap<ITableRow, HashMap<IColumn<?>, ICell>>();
    if (m_scoutTable != null) {
      for (ITableRow scoutRow : m_scoutTable.getRows()) {
        HashMap<IColumn<?>, ICell> cells = new HashMap<IColumn<?>, ICell>();
        for (IColumn<?> col : m_scoutTable.getColumnSet().getVisibleColumns()) {
          cells.put(col, m_scoutTable.getCell(scoutRow, col));
        }
        m_cachedCells.put(scoutRow, cells);
      }
    }
  }

  @Override
  public RwtScoutTable getRwtScoutTable() {
    return m_uiTable;
  }
}
