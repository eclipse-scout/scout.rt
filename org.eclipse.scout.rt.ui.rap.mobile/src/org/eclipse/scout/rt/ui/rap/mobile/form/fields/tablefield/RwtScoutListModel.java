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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.tablefield;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.ui.rap.basic.table.RwtScoutTableEvent;
import org.eclipse.scout.rt.ui.rap.html.HtmlAdapter;
import org.eclipse.scout.rt.ui.rap.util.HtmlTextUtility;
import org.eclipse.swt.graphics.Image;

public class RwtScoutListModel implements IRwtScoutListModel {
  private static final long serialVersionUID = 1L;

  /**
   * Query parameter which will be appended to the actual hyperlink url.
   * <p>
   * Necessary because there is no other possibility to get the row of the clicked hyperlink.
   */
  public static final String HYPERLINK_ROW_PARAM = "1row1Num1";

  private transient ListenerList listenerList = null;
  private final ITable m_scoutTable;
  private final RwtScoutList m_uiList;
  private boolean m_multiline;

  public RwtScoutListModel(ITable scoutTable, RwtScoutList uiTable) {
    m_scoutTable = scoutTable;
    m_uiList = uiTable;
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
    // nop
  }

  @Override
  public void consumeTableModelEvent(RwtScoutTableEvent uiTableEvent) {
    // nop
  }

  protected ICell getCell(Object row) {
    IColumn<?> column = m_scoutTable.getColumnSet().getVisibleColumns()[0];
    if (column != null) {
      ITableRow tableRow = (ITableRow) row;
      return tableRow.getCell(column);
    }
    else {
      return null;
    }
  }

  @Override
  public RwtScoutList getUiList() {
    return m_uiList;
  }

  public ITable getScoutTable() {
    return m_scoutTable;
  }

  @Override
  public Image getImage(Object element) {
    //Has no effect on ListViewer
    return null;
  }

  @Override
  public String getText(Object element) {
    ICell cell = getCell(element);
    if (cell == null) {
      return "";
    }

    String text = cell.getText();
    if (text == null) {
      text = "";
    }
    if (HtmlTextUtility.isTextWithHtmlMarkup(text)) {
      HtmlAdapter htmlAdapter = m_uiList.getUiEnvironment().getHtmlAdapter();
      text = htmlAdapter.adaptHtmlCell(m_uiList, text);

      Map<String, String> params = new HashMap<String, String>();
      ITableRow tableRow = (ITableRow) element;
      params.put(HYPERLINK_ROW_PARAM, tableRow.getRowIndex() + "");
      text = htmlAdapter.convertLinksWithLocalUrlsInHtmlCell(m_uiList, text, params);
    }
    else {
      boolean multiline = false;
      if (text.indexOf("\n") >= 0) {
        multiline = isMultiline();
        if (!multiline) {
          text = StringUtility.replaceNewLines(text, " ");
        }
      }
      boolean markupEnabled = Boolean.TRUE.equals(getUiList().getUiField().getData(RWT.MARKUP_ENABLED));
      if (markupEnabled || multiline) {
        text = HtmlTextUtility.transformPlainTextToHtml(text);
      }
    }
    return text;
  }
}
