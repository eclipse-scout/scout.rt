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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rwt.RWT;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.RowIndexComparator;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableListener;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.basic.table.RwtScoutTable;
import org.eclipse.scout.rt.ui.rap.basic.table.RwtScoutTableEvent;
import org.eclipse.scout.rt.ui.rap.keystroke.RwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.rt.ui.rap.util.UiRedrawHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

/**
 * List with basic functionalities which processes a {@link ITable} but only the first column.<br/>
 * Compared to {@link RwtScoutTable} there are a lot of missing features:
 * <ul>
 * <li>It's not possible to display images, nor changing the font or color as jface list does not support it</li>
 * <li>There is no keyboard navigation support</li>
 * <li>There is no drag and drop support</li>
 * <li>There are no header menus nor context menus displayed</li>
 * <li>It cannot handle a lot of rows.</li>
 * </ul>
 * One essential difference to the table widget is that scrolling works better. On tables scrolling is done row by row
 * as only the displayed rows are rendered. On a list widget every row is rendered at beginning which makes scrolling
 * smoother. That's why it is the preferred widget on touch devices.
 */
public class RwtScoutList extends RwtScoutComposite<ITable> implements IRwtScoutList {
  private P_ScoutTableListener m_scoutTableListener;
  private UiRedrawHandler m_redrawHandler;
  private ListViewer m_uiViewer;
  private String m_variant = "";

  public RwtScoutList() {
  }

  public RwtScoutList(String variant) {
    m_variant = variant;
  }

  @Override
  protected void initializeUi(Composite parent) {
    m_redrawHandler = new UiRedrawHandler(parent);
    int style;
    if (getScoutObject() != null && getScoutObject().isMultiSelect()) {
      style = SWT.MULTI;
    }
    else {
      style = SWT.SINGLE;
    }
    style |= SWT.V_SCROLL;
    ListEx list = new ListEx(parent, style);

    if (StringUtility.hasText(m_variant)) {
      list.setData(WidgetUtil.CUSTOM_VARIANT, m_variant);
    }
    list.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
//    list.setData(MarkupValidator.MARKUP_VALIDATION_DISABLED, Boolean.TRUE);

    ListViewer viewer = new ListViewer(list);
    viewer.setUseHashlookup(true);
    setUiTableViewer(viewer);
    setUiField(list);

    RwtScoutListModel listModel = createUiListModel();
    listModel.setMultiline(getScoutObject().isMultilineText());
    viewer.setContentProvider(listModel);
    viewer.setLabelProvider(listModel);
    viewer.setInput(listModel);

    // ui listeners
    viewer.addSelectionChangedListener(new P_RwtSelectionListener());
    P_RwtTableListener rwtTableListener = new P_RwtTableListener();
    list.addListener(SWT.MouseUp, rwtTableListener);
    list.addListener(SWT.MouseDoubleClick, rwtTableListener);
    getUiEnvironment().addKeyStroke(list, new RwtKeyStroke((int) ' ') {

      @Override
      public void handleUiAction(Event e) {
        handleUiToggleAcction(e);
      }
    }, false);

  }

  @Override
  public boolean isUiDisposed() {
    return getUiField() == null || getUiField().isDisposed();
  }

  protected RwtScoutListModel createUiListModel() {
    return new RwtScoutListModel(getScoutObject(), this);
  }

  @Override
  protected void attachScout() {
    super.attachScout();

    if (getScoutObject() == null) {
      return;
    }

    if (m_scoutTableListener == null) {
      m_scoutTableListener = new P_ScoutTableListener();
      getScoutObject().addUITableListener(m_scoutTableListener);
    }
    setHeaderVisibleFromScout(getScoutObject().isHeaderVisible());
    setSelectionFromScout(getScoutObject().getSelectedRows());
    setRowHeightFromScout();

    //handle events from recent history
    final IEventHistory<TableEvent> h = getScoutObject().getEventHistory();
    if (h != null) {
      getUiEnvironment().getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          for (TableEvent e : h.getRecentEvents()) {
            handleScoutTableEventInUi(e);
          }
        }
      });
    }
  }

  @Override
  protected void detachScout() {
    super.detachScout();

    if (getScoutObject() == null) {
      return;
    }

    if (m_scoutTableListener != null) {
      getScoutObject().removeTableListener(m_scoutTableListener);
      m_scoutTableListener = null;
    }
  }

  @Override
  public ListEx getUiField() {
    return (ListEx) super.getUiField();
  }

  @Override
  public ListViewer getUiTableViewer() {
    return m_uiViewer;
  }

  public void setUiTableViewer(ListViewer uiViewer) {
    m_uiViewer = uiViewer;
  }

  public ITableRow getUiSelectedRow() {
    ITableRow[] rows = getUiSelectedRows();
    if (rows.length > 0) {
      return rows[0];
    }
    return null;
  }

  public ITableRow[] getUiSelectedRows() {
    StructuredSelection uiSelection = (StructuredSelection) getUiTableViewer().getSelection();
    TreeSet<ITableRow> sortedRows = new TreeSet<ITableRow>(new RowIndexComparator());
    if (uiSelection != null && !uiSelection.isEmpty()) {
      for (Object o : uiSelection.toArray()) {
        ITableRow row = (ITableRow) o;
        sortedRows.add(row);
      }
    }
    return sortedRows.toArray(new ITableRow[sortedRows.size()]);
  }

  protected void setRowHeightFromScout() {
    int h = getScoutObject().getRowHeightHint();
    if (h <= 0 && getScoutObject().isMultilineText()) {
      h = 40; // Enough for 2 lines fully visible (further lines are cut off) --> cannot be dynamic at the moment, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=346768
    }
    if (h >= 0) {
      getUiField().setData(RWT.CUSTOM_ITEM_HEIGHT, h);
    }
    if (isCreated()) {
      getUiTableViewer().refresh();
    }
  }

  /**
   * scout property observer
   */
  @Override
  protected void handleScoutPropertyChange(String propName, Object newValue) {
    if (propName.equals(ITable.PROP_HEADER_VISIBLE)) {
      setHeaderVisibleFromScout(((Boolean) newValue).booleanValue());
    }
    else if (propName.equals(ITable.PROP_ROW_HEIGHT_HINT)) {
      setRowHeightFromScout();
    }
    else if (propName.equals(ITable.PROP_SCROLL_TO_SELECTION)) {
      updateScrollToSelectionFromScout();
    }
  }

  /**
   * scout table observer
   */
  protected boolean isHandleScoutTableEvent(TableEvent[] a) {
    for (TableEvent element : a) {
      switch (element.getType()) {
        case TableEvent.TYPE_REQUEST_FOCUS:
        case TableEvent.TYPE_REQUEST_FOCUS_IN_CELL:
        case TableEvent.TYPE_ROWS_INSERTED:
        case TableEvent.TYPE_ROWS_UPDATED:
        case TableEvent.TYPE_ROWS_DELETED:
        case TableEvent.TYPE_ALL_ROWS_DELETED:
        case TableEvent.TYPE_ROW_ORDER_CHANGED:
        case TableEvent.TYPE_ROW_FILTER_CHANGED:
        case TableEvent.TYPE_COLUMN_ORDER_CHANGED:
        case TableEvent.TYPE_COLUMN_HEADERS_UPDATED:
        case TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED:
        case TableEvent.TYPE_ROWS_SELECTED:
        case TableEvent.TYPE_SCROLL_TO_SELECTION: {
          return true;
        }
      }
    }
    return false;
  }

  protected void handleScoutTableEventInUi(TableEvent e) {
    if (isUiDisposed()) {
      return;
    }
    RwtScoutTableEvent uiTableEvent = null;
    /*
     * check the scout observer to filter all events that are used here
     * @see isHandleScoutTableEvent()
     */
    switch (e.getType()) {
      case TableEvent.TYPE_REQUEST_FOCUS: {
        getUiField().setFocus();
        break;
      }
      case TableEvent.TYPE_SCROLL_TO_SELECTION: {
        scrollToSelection();
        break;
      }
      case TableEvent.TYPE_ROWS_INSERTED:
      case TableEvent.TYPE_ROWS_UPDATED:
      case TableEvent.TYPE_ROWS_DELETED:
      case TableEvent.TYPE_ALL_ROWS_DELETED:
      case TableEvent.TYPE_ROW_FILTER_CHANGED:
      case TableEvent.TYPE_ROW_ORDER_CHANGED: {
        uiTableEvent = new RwtScoutTableEvent();
        break;
      }
      case TableEvent.TYPE_ROWS_SELECTED: {
        setSelectionFromScout(e.getRows());
        break;
      }
    }
    //
    if (uiTableEvent != null) {
      ((RwtScoutListModel) getUiTableViewer().getContentProvider()).consumeTableModelEvent(uiTableEvent);
      getUiTableViewer().refresh();
    }
    // refresh selection, indexes might have changed
    switch (e.getType()) {
      case TableEvent.TYPE_ROW_FILTER_CHANGED:
        setSelectionFromScout(e.getTable().getSelectedRows());
        break;
      case TableEvent.TYPE_ROWS_INSERTED:
      case TableEvent.TYPE_ROWS_UPDATED:
      case TableEvent.TYPE_ROWS_DELETED:
      case TableEvent.TYPE_ALL_ROWS_DELETED:
      case TableEvent.TYPE_ROW_ORDER_CHANGED: {
        setSelectionFromScout(e.getTable().getSelectedRows());
        break;
      }
    }
  }

  protected void setHeaderVisibleFromScout(boolean headerVisible) {
    //FIXME CGU create  header to allow sorting
//    getUiField().setHeaderVisible(headerVisible);
  }

  @Override
  public void setEnabledFromScout(boolean enabledFromScout) {
    getUiField().setEnabled(enabledFromScout);
  }

  protected void setSelectionFromScout(ITableRow[] selectedRows) {
    if (getUiField().isDisposed()) {
      return;
    }
    ITableRow[] uiSelection = getUiSelectedRows();
    if (CompareUtility.equals(uiSelection, selectedRows)) {
      // no change
      return;
    }
    else {
      if (selectedRows == null) {
        selectedRows = new ITableRow[0];
      }
      getUiTableViewer().setSelection(new StructuredSelection(selectedRows), true);
      updateScrollToSelectionFromScout();
    }
  }

  private void updateScrollToSelectionFromScout() {
    if (getScoutObject().isScrollToSelection()) {
      scrollToSelection();
    }
  }

  protected void scrollToSelection() {
    if (getUiField() != null && !getUiField().isDisposed()) {
      getUiField().showSelection();
    }
  }

  protected void setContextColumnFromUi() {
    if (getScoutObject() == null) {
      return;
    }

    // notify Scout
    final IColumn finalCol = getScoutObject().getColumnSet().getVisibleColumn(0);
    Runnable t = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getUIFacade().setContextColumnFromUI(finalCol);
      }
    };
    getUiEnvironment().invokeScoutLater(t, 0);
    // end notify
  }

  private int getVisualCellIndex(TableItem tableItem, int columnIndex) {
    int visualCellIndex = columnIndex;
    final int[] columnOrder = tableItem.getParent().getColumnOrder();
    for (int element : columnOrder) {
      if (element == columnIndex) {
        visualCellIndex = columnIndex;
      }
    }
    return visualCellIndex;
  }

  protected void setSelectionFromUi(final StructuredSelection selection) {
    if (getUpdateUiFromScoutLock().isAcquired()) {
      return;
    }
    //
    if (getScoutObject() != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          try {
            addIgnoredScoutEvent(TableEvent.class, "" + TableEvent.TYPE_ROWS_SELECTED);
            //
            getScoutObject().getUIFacade().setSelectedRowsFromUI(RwtUtility.getItemsOfSelection(ITableRow.class, selection));
          }
          finally {
            removeIgnoredScoutEvent(TableEvent.class, "" + TableEvent.TYPE_ROWS_SELECTED);
          }
        }
      };
      getUiEnvironment().invokeScoutLater(t, 0);
      // end notify
    }
  }

  protected void handleUiRowClick(final ITableRow row) {
    if (getScoutObject() != null) {
      if (row != null) {
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().fireRowClickFromUI(row);
          }
        };
        getUiEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
    }
  }

  protected void handleUiRowAction(final ITableRow row) {
    if (getScoutObject() != null) {
      if (!getScoutObject().isCheckable() && row != null) {
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().fireRowActionFromUI(row);
          }
        };
        getUiEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
    }
  }

  protected void handleUiHyperlinkAction(String urlText) {
    if (getScoutObject() != null) {
      final URL url;
      try {
        url = new URL(urlText);
      }
      catch (MalformedURLException e) {
        //nop
        return;
      }
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          ITable table = getScoutObject();
          table.getUIFacade().fireHyperlinkActionFromUI(table.getSelectedRow(), table.getContextColumn(), url);
        }
      };
      getUiEnvironment().invokeScoutLater(t, 0);
      // end notify
    }
  }

  protected void handleUiToggleAcction(Event e) {
    if (e.doit && getScoutObject().isCheckable()) {
      if (e.stateMask == 0) {
        switch (e.keyCode) {
          case ' ':
            ITableRow[] selectedRows = RwtUtility.getItemsOfSelection(ITableRow.class, (StructuredSelection) getUiTableViewer().getSelection());
            if (selectedRows != null && selectedRows.length > 0) {
              handleUiRowClick(selectedRows[0]);
            }
            e.doit = false;
            break;
        }
      }
    }
  }

  private class P_ScoutTableListener implements TableListener {
    @Override
    public void tableChanged(final TableEvent e) {
      if (isHandleScoutTableEvent(new TableEvent[]{e})) {
        if (isIgnoredScoutEvent(TableEvent.class, "" + e.getType())) {
          return;
        }
        Runnable t = new Runnable() {
          @Override
          public void run() {
            try {
              getUpdateUiFromScoutLock().acquire();
              //
              handleScoutTableEventInUi(e);
            }
            finally {
              getUpdateUiFromScoutLock().release();
            }
          }
        };
        getUiEnvironment().invokeUiLater(t);
      }
    }

    @Override
    public void tableChangedBatch(final TableEvent[] a) {
      if (isHandleScoutTableEvent(a)) {
        final ArrayList<TableEvent> filteredList = new ArrayList<TableEvent>();
        for (int i = 0; i < a.length; i++) {
          if (!isIgnoredScoutEvent(TableEvent.class, "" + a[i].getType())) {
            filteredList.add(a[i]);
          }
        }
        if (filteredList.size() == 0) {
          return;
        }
        Runnable t = new Runnable() {
          @Override
          public void run() {
            if (isUiDisposed()) {
              return;
            }
            m_redrawHandler.pushControlChanging();
            try {
              try {
                getUpdateUiFromScoutLock().acquire();
                //
                for (TableEvent element : filteredList) {
                  handleScoutTableEventInUi(element);
                }
              }
              finally {
                getUpdateUiFromScoutLock().release();
              }
            }
            finally {
              m_redrawHandler.popControlChanging();
            }
          }
        };
        getUiEnvironment().invokeUiLater(t);
      }
    }
  }

  private class P_RwtTableListener implements Listener {
    private static final long serialVersionUID = 1L;

    private Boolean m_doubleClicked = Boolean.FALSE;

    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.MouseUp: {
          setContextColumnFromUi();

          synchronized (m_doubleClicked) {
            if (m_doubleClicked == Boolean.TRUE) {
              m_doubleClicked = Boolean.FALSE;
              break;
            }
          }
          Point eventPosition = new Point(event.x, event.y);
          if (getUiField().getItem(eventPosition) == null) {
            getUiTableViewer().setSelection(null);
            setSelectionFromUi(new StructuredSelection());
          }
          else {
            StructuredSelection selection = (StructuredSelection) getUiTableViewer().getSelection();
            if (selection != null && selection.size() == 1) {
              handleUiRowClick((ITableRow) selection.getFirstElement());
            }
          }
          break;
        }
        case SWT.MouseDoubleClick: {
          synchronized (m_doubleClicked) {
            m_doubleClicked = Boolean.TRUE;
          }
          StructuredSelection selection = (StructuredSelection) getUiTableViewer().getSelection();
          if (selection != null && selection.size() == 1) {
            handleUiRowAction((ITableRow) selection.getFirstElement());
          }
          break;
        }
      }
    }
  }

  public class P_RwtSelectionListener implements ISelectionChangedListener {
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
      setSelectionFromUi((StructuredSelection) event.getSelection());
    }
  }

}
