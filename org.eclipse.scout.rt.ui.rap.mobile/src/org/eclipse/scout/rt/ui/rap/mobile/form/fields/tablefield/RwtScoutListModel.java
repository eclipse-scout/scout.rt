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
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.basic.AbstractRwtScoutCellTextHelper;
import org.eclipse.scout.rt.ui.rap.basic.IRwtScoutCellTextHelper;
import org.eclipse.scout.rt.ui.rap.basic.IRwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.basic.table.RwtScoutTableEvent;
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
  private final IRwtEnvironment m_env;

  public RwtScoutListModel(ITable scoutTable, RwtScoutList uiTable) {
    m_scoutTable = scoutTable;
    m_uiList = uiTable;
    m_env = uiTable.getUiEnvironment();
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
      return m_scoutTable.getFilteredRows().toArray();
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
    IColumn<?> column = CollectionUtility.firstElement(m_scoutTable.getColumnSet().getVisibleColumns());
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
    final ICell cell = getCell(element);
    final IRwtScoutCellTextHelper cellTextHelper = createCellTextHelper(m_env, m_uiList, (ITableRow) element);

    return cellTextHelper.processCellText(cell);
  }

  protected IRwtScoutCellTextHelper createCellTextHelper(IRwtEnvironment env, IRwtScoutComposite<?> uiComposite, ITableRow tableRow) {
    return new P_RwtScoutListModelCellTextHelper(env, uiComposite, tableRow);
  }

  private class P_RwtScoutListModelCellTextHelper extends AbstractRwtScoutCellTextHelper {

    private final ITableRow m_tableRow;

    public P_RwtScoutListModelCellTextHelper(IRwtEnvironment env, IRwtScoutComposite<?> uiComposite, ITableRow tableRow) {
      super(env, uiComposite);
      m_tableRow = tableRow;
    }

    @Override
    protected Map<String, String> createAdditionalLinkParams() {
      Map<String, String> params = new HashMap<String, String>();
      params.put(HYPERLINK_ROW_PARAM, Integer.toString(m_tableRow.getRowIndex()));
      return params;
    }

    @Override
    protected boolean isMultilineScoutObject() {
      return isMultiline();
    }

    @Override
    protected boolean isWrapText() {
      return false;
    }
  }
}
