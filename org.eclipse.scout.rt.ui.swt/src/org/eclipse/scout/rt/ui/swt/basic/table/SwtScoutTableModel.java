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

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
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
import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.ui.swt.Activator;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import org.eclipse.scout.rt.ui.swt.SwtIcons;
import org.eclipse.scout.rt.ui.swt.extension.UiDecorationExtensionPoint;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

public class SwtScoutTableModel implements IStructuredContentProvider, ITableColorProvider, ITableLabelProvider, ITableFontProvider {
  private transient ListenerList listenerList = null;
  private final ITable m_table;
  private final ISwtEnvironment m_environment;
  private final SwtScoutTable m_swtTable;
  private final TableColumnManager m_columnManager;
  private final Image m_imgCheckboxFalse;
  private final Image m_imgCheckboxTrue;
  private final Color m_disabledForegroundColor;
  private final int m_markerIconWith;

  public SwtScoutTableModel(ITable table, SwtScoutTable swtTable, ISwtEnvironment environment, TableColumnManager columnManager) {
    m_table = table;
    m_swtTable = swtTable;
    m_environment = environment;
    m_columnManager = columnManager;
    m_imgCheckboxTrue = Activator.getIcon(SwtIcons.CheckboxYes);
    m_imgCheckboxFalse = Activator.getIcon(SwtIcons.CheckboxNo);
    m_disabledForegroundColor = m_environment.getColor(UiDecorationExtensionPoint.getLookAndFeel().getColorForegroundDisabled());
    Image markerIcon = Activator.getIcon("marker");
    m_markerIconWith = (markerIcon != null) ? markerIcon.getBounds().width : 0;
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
      Image image = null;

      ICell cell = getCell(element, columnIndex);
      IColumn col = m_columnManager.getColumnByModelIndex(columnIndex - 1);
      if (columnOrder[1] == columnIndex && m_swtTable.getScoutObject() != null && m_swtTable.getScoutObject().isCheckable()) {
        if (((ITableRow) element).isChecked()) {
          image = m_imgCheckboxTrue;
        }
        else {
          image = m_imgCheckboxFalse;
        }
      }
      else if (col != null && cell != null && col.getDataType() == Boolean.class && (!(col instanceof ISmartColumn) || ((ISmartColumn) col).getLookupCall() == null)) {
        Boolean b = (Boolean) cell.getValue();
        if (b != null && b.booleanValue()) {
          image = m_imgCheckboxTrue;
        }
        else {
          image = m_imgCheckboxFalse;
        }
      }
      else if (cell != null && cell.getIconId() != null) {
        image = m_environment.getIcon(cell.getIconId());
      }
      else if (columnOrder[1] == columnIndex) {
        ITableRow row = (ITableRow) element;
        image = m_environment.getIcon(row.getIconId());
      }

      if (col != null && col.isEditable()) {
        Display display = m_environment.getDisplay();
        if (image != null) {
          //make sure there is some space for the editable marker
          int origHeight = image.getBounds().height;
          int origWidth = image.getBounds().width;
          int emptyImageHeight = origHeight / origWidth * (origWidth + m_markerIconWith) - origHeight;
          Image emptyImage = getEmptyImage(display, m_markerIconWith, emptyImageHeight);
          return combine(display, emptyImage, image);
        }
      }
      return image;
    }
    return null;
  }

  private static Image getEmptyImage(Display display, int width, int height) {
    if (width == 0 || height == 0) {
      return null;
    }
    Image emptyImage = new Image(display, width, height);
    ImageData imageData = emptyImage.getImageData();
    for (int i = 0; i < imageData.width; i++) {
      for (int j = 0; j < imageData.height; j++) {
        imageData.setAlpha(i, j, 0);
      }
    }
    return new Image(display, imageData);
  }

  private static Image combine(Display display, Image image1, Image image2) {
    if (image1 == null) {
      return image2;
    }
    else if (image2 == null) {
      return image1;
    }
    Rectangle bounds1 = image1.getBounds();
    Rectangle bounds2 = image2.getBounds();
    Image emptyImage = getEmptyImage(display, bounds1.width + bounds2.width, Math.max(bounds1.height, bounds2.height));
    ImageDescriptor desc = ImageDescriptor.createFromImage(image1);
    ImageDescriptor desc2 = ImageDescriptor.createFromImage(image2);
    DecorationOverlayIcon icon = new DecorationOverlayIcon(emptyImage, desc, IDecoration.TOP_LEFT);
    DecorationOverlayIcon icon2 = new DecorationOverlayIcon(icon.createImage(), desc2, IDecoration.BOTTOM_RIGHT);
    return icon2.createImage(display);
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

  protected ICell getCell(Object row, int colIndex) {
    IColumn<?> column = m_columnManager.getColumnByModelIndex(colIndex - 1);
    if (column != null) {
      return m_table.getCell((ITableRow) row, column);
    }
    else {
      return null;
    }
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

  public SwtScoutTable getSwtScoutTable() {
    return m_swtTable;
  }

}
