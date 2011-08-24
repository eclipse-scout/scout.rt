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

import java.util.HashMap;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.ui.swt.Activator;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class SwtScoutTableModel implements IStructuredContentProvider, ITableColorProvider, ITableLabelProvider, ITableFontProvider {
  private static final boolean COMPOSITE_ICON_ENABLED = "true".equals(Activator.getDefault().getBundle().getBundleContext().getProperty("scout.fix355669"));
  private transient ListenerList listenerList = null;
  private final ITable m_table;
  private final ISwtEnvironment m_environment;
  private HashMap<ITableRow, HashMap<IColumn<?>, ICell>> m_cachedCells;
  private final SwtScoutTable m_swtTable;
  private final TableColumnManager m_columnManager;
  private Image m_imgCheckboxFalse;
  private Image m_imgCheckboxTrue;
  private Color m_disabledForegroundColor;

  public SwtScoutTableModel(ITable table, SwtScoutTable swtTable, ISwtEnvironment environment, TableColumnManager columnManager) {
    m_table = table;
    m_swtTable = swtTable;
    m_environment = environment;
    m_columnManager = columnManager;
    m_imgCheckboxTrue = m_environment.getIcon(AbstractIcons.CheckboxYes);
    m_imgCheckboxFalse = m_environment.getIcon(AbstractIcons.CheckboxNo);
    m_disabledForegroundColor = m_environment.getColor(UiDecorationExtensionPoint.getLookAndFeel().getColorForegroundDisabled());
    rebuildCache();

  }

  public boolean isMultiline() {
    if (m_table != null) {
      return m_table.isMultilineText();
    }

    return false;
  }

  @Override
  public Object[] getElements(Object inputElement) {
    if (m_table != null) {
      return m_table.getFilteredRows();
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
        return m_environment.getColor(cell.getBackgroundColor());
      }
    }
    return null;
  }

  @Override
  public Color getForeground(Object element, int columnIndex) {
    if (columnIndex > 0) {
      ITableRow scoutRow = (ITableRow) element;
      ICell scoutCell = getCell(element, columnIndex);
      if (scoutCell == null) {
        return null;
      }
      Color col = m_environment.getColor(scoutCell.getForegroundColor());
      if (col == null) {
        if (!scoutRow.isEnabled() || !scoutCell.isEnabled()) {
          col = m_disabledForegroundColor;
        }
      }
      return col;
    }
    return null;
  }

  @Override
  public Image getColumnImage(Object element, int columnIndex) {
    int[] columnOrder = m_swtTable.getSwtField().getColumnOrder();
    if (columnOrder.length > 1) {
      IColumn col = m_columnManager.getColumnByModelIndex(columnIndex - 1);
      ICell cell = getCell(element, columnIndex);
      //checkbox
      Image checkBoxImage = null;
      if (columnOrder[1] == columnIndex && m_swtTable.getScoutObject() != null && m_swtTable.getScoutObject().isCheckable()) {
        if (((ITableRow) element).isChecked()) {
          checkBoxImage = m_imgCheckboxTrue;
        }
        else {
          checkBoxImage = m_imgCheckboxFalse;
        }
      }
      else if (col != null && cell != null && col.getDataType() == Boolean.class) {
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
      Image decoImage = m_environment.getIcon(iconId);
      //merge
      if (COMPOSITE_ICON_ENABLED && checkBoxImage != null && decoImage != null) {
        String key = checkBoxImage.handle + "_" + iconId;
        ImageRegistry reg = Activator.getDefault().getImageRegistry();
        Image compositeImage = reg.get(key);
        if (compositeImage == null) {
          int w1 = checkBoxImage.getBounds().width;
          int w2 = decoImage.getBounds().width;
          int h = Math.max(checkBoxImage.getBounds().height, decoImage.getBounds().height);
          compositeImage = new Image(Display.getCurrent(), w1 + w2, h);
          GC gc = new GC(compositeImage);
          gc.drawImage(checkBoxImage, 0, 0);
          gc.drawImage(decoImage, w1, 0);
          gc.dispose();
          reg.put(key, compositeImage);
        }
        return compositeImage;
      }
      if (checkBoxImage != null) {
        return checkBoxImage;
      }
      if (decoImage != null) {
        return decoImage;
      }
    }
    return null;
  }

  @Override
  public String getColumnText(Object element, int columnIndex) {
    if (columnIndex > 0) {
      ICell cell = getCell(element, columnIndex);
      if (cell == null) {
        return "";
      }
      else {
        String text = cell.getText();
        if (!isMultiline()) {
          text = StringUtility.removeNewLines(text);
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
        return m_environment.getFont(cell.getFont(), m_swtTable.getSwtField().getFont());
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

  public void consumeTableModelEvent(SwtScoutTableEvent swtTableEvent) {
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
    if (m_table != null) {
      for (ITableRow scoutRow : m_table.getRows()) {
        HashMap<IColumn<?>, ICell> cells = new HashMap<IColumn<?>, ICell>();
        for (IColumn<?> col : m_table.getColumnSet().getVisibleColumns()) {
          cells.put(col, m_table.getCell(scoutRow, col));
        }
        m_cachedCells.put(scoutRow, cells);
      }
    }
  }

  public SwtScoutTable getSwtScoutTable() {
    return m_swtTable;
  }

}
