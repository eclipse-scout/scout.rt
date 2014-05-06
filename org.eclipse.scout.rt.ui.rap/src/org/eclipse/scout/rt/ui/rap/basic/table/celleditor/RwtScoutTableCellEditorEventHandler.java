/*******************************************************************************
 * Copyright (c) 2010, 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.rap.basic.table.celleditor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.ui.rap.basic.table.RwtScoutTable;
import org.eclipse.scout.rt.ui.rap.basic.table.celleditor.RwtScoutTableCellEditor.RwtCellEditor;
import org.eclipse.scout.rt.ui.rap.form.fields.IPopupSupport;
import org.eclipse.scout.rt.ui.rap.form.fields.IPopupSupport.IPopupSupportListener;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

/**
 * This class is responsible for handling the focus lost, pop-up and traverse events occuring in
 * {@link RwtScoutTableCellEditor}.
 * 
 * @since 3.10.0-M5
 */
public class RwtScoutTableCellEditorEventHandler {

  enum TraverseKey {
    TAB_NEXT, TAB_PREVIOUS, NONE
  }

  private final RwtScoutTableCellEditor m_tableCellEditor;
  private final RwtScoutTable m_uiTableComposite;
  private final P_FocusLostListener m_focusLostListener;
  private final P_PopupListener m_popupListener;
  private final P_TraverseListener m_traverseListener;
  private RwtCellEditor m_cellEditor;

  private TraverseKey m_traverseKey = TraverseKey.NONE;

  public RwtScoutTableCellEditorEventHandler(RwtScoutTableCellEditor tableCellEditor, RwtScoutTable uiTableComposite) {
    m_tableCellEditor = tableCellEditor;
    m_uiTableComposite = uiTableComposite;
    m_focusLostListener = new P_FocusLostListener();
    m_popupListener = new P_PopupListener();
    m_traverseListener = new P_TraverseListener();
  }

  public void setupFocusAndTraverseListenerOnFocusControl(Control focusControl, RwtCellEditor cellEditor) {
    m_cellEditor = cellEditor;
    installFocusLostListenerOnFocusControl(focusControl);
    installTraverseListenerOnFocusControl(focusControl);
  }

  protected void installFocusLostListenerOnFocusControl(Control focusControl) {
    focusControl.addFocusListener(m_focusLostListener);
    m_focusLostListener.setFocusControl(focusControl);
  }

  protected void installTraverseListenerOnFocusControl(Control focusControl) {
    focusControl.addTraverseListener(m_traverseListener);
  }

  public void installPopupListenerOnPopupSupport(IPopupSupport popupSupportFormField) {
    popupSupportFormField.addPopupEventListener(m_popupListener);
  }

  public void activateFocusLostListener() {
    m_focusLostListener.install();
  }

  public void deactivateFocusLostListener() {
    m_focusLostListener.uninstall();
  }

  public void deregisterKeyStrokeFromFocusControl() {
    Control focusControl = m_focusLostListener.getFocusControl();
    if (focusControl != null && !focusControl.isDisposed()) {
      m_uiTableComposite.getUiEnvironment().removeKeyStrokes(focusControl);
    }
  }

  public void suspendFocusLostListener() {
    m_focusLostListener.suspend();
  }

  public void resumeFocusLostListener() {
    m_focusLostListener.resume();
  }

  protected void setTraverseKey(Integer traverseKey) {
    if (traverseKey == SWT.TRAVERSE_TAB_NEXT) {
      m_traverseKey = TraverseKey.TAB_NEXT;
    }
    else if (traverseKey == SWT.TRAVERSE_TAB_PREVIOUS) {
      m_traverseKey = TraverseKey.TAB_PREVIOUS;
    }
    else {
      m_traverseKey = TraverseKey.NONE;
    }
  }

  protected TraverseKey getTraverseKey() {
    return m_traverseKey;
  }

  private class P_FocusLostListener extends FocusAdapter {
    private static final long serialVersionUID = 1L;

    private final Lock m_suspendLock = new ReentrantLock();
    private AtomicInteger m_suspendCounter = new AtomicInteger();
    private Control m_focusControl;

    public void setFocusControl(Control focusControl) {
      m_focusControl = focusControl;
    }

    public Control getFocusControl() {
      return m_focusControl;
    }

    /**
     * Uninstalls this listener on the table widget
     */
    public void uninstall() {
      m_suspendCounter.set(0);
    }

    /**
     * Installs this listener on the table widget
     */
    public void install() {
      m_suspendCounter.set(0);
    }

    /**
     * <p>
     * To resume listening for focus lost events.
     * </p>
     * <p>
     * Please note that this request is put onto a stack meaning that you have to call
     * {@link P_FocusLostListener#resume()} as many times as you called {@link P_FocusLostListener#suspend()} to resume
     * listening for focus lost events.
     * </p>
     * <p>
     * <small>Counterpart of {@link P_FocusLostListener#suspend()}.</small>
     * </p>
     */
    public void resume() {
      m_suspendLock.lock();
      try {
        if (m_suspendCounter.decrementAndGet() < 0) { // negative values are not allowed
          m_suspendCounter.set(0);
        }
      }
      finally {
        m_suspendLock.unlock();
      }
    }

    /**
     * <p>
     * To suspend listening for focus lost events.
     * </p>
     * <p>
     * Please note that this request is put onto a stack meaning that you have to call
     * {@link P_FocusLostListener#resume()} as many times as you called {@link P_FocusLostListener#suspend()} to resume
     * listening for focus lost events.
     * </p>
     * <p>
     * <small>Counterpart of {@link P_FocusLostListener#resume()}.</small>
     * </p>
     */
    public void suspend() {
      m_suspendLock.lock();
      try {
        m_suspendCounter.incrementAndGet();
      }
      finally {
        m_suspendLock.unlock();
      }
    }

    public boolean isSuspended() {
      return m_suspendCounter.get() > 0;
    }

    @Override
    public void focusLost(FocusEvent event) {
      if (isSuspended()) {
        return;
      }

      Control currentFocus = m_cellEditor.getControl().getDisplay().getFocusControl();
      if (currentFocus == null || currentFocus.isDisposed()) {
        return;
      }

      TableViewer viewer = m_uiTableComposite.getUiTableViewer();
      if (!viewer.isCellEditorActive()) {
        return;
      }

      Control tableControl = m_uiTableComposite.getUiTableViewer().getControl();

      if (!RwtUtility.isAncestorOf(tableControl, currentFocus)) {
        for (CellEditor editor : viewer.getCellEditors()) {
          if (editor != null && editor.isActivated() && editor instanceof RwtCellEditor) {
            ((RwtCellEditor) editor).stopCellEditing();
            break;
          }
        }
      }
    }
  }

  private class P_PopupListener implements IPopupSupportListener {
    private boolean m_isPopUpOpen = false;

    public boolean isPopUpOpen() {
      return m_isPopUpOpen;
    }

    @Override
    public void handleEvent(int eventType) {
      if (eventType == IPopupSupportListener.TYPE_OPENING) {
        m_focusLostListener.suspend();
        m_isPopUpOpen = true;
      }
      else if (eventType == IPopupSupportListener.TYPE_CLOSED) {
        m_focusLostListener.resume();
        m_isPopUpOpen = false;

        handleTraverseTabKey();
      }
    }

    private void notifyFocusLostOnLastFocusControl() {
      Control focusControl = m_focusLostListener.getFocusControl();
      Event focusEvent = new Event();
      focusEvent.widget = focusControl;
      focusControl.notifyListeners(SWT.FocusOut, focusEvent);
    }

    private void handleTraverseTabKey() {
      if (getTraverseKey() == TraverseKey.NONE) {
        notifyFocusLostOnLastFocusControl();
        return;
      }

      ITableRow tableRow = m_cellEditor.getScoutTableRow();
      IColumn<?> tableColumn = m_cellEditor.getScoutTableColumn();

      notifyFocusLostOnLastFocusControl();

      if (m_cellEditor.isActivated()) {
        m_cellEditor.stopCellEditing();
      }

      if (tableRow != null && tableColumn != null) {
        m_tableCellEditor.enqueueEditNextTableCell(tableRow, tableColumn, getTraverseKey() == TraverseKey.TAB_NEXT);
      }
      setTraverseKey(SWT.TRAVERSE_NONE);
    }
  }

  private class P_TraverseListener implements TraverseListener {
    private static final long serialVersionUID = 1L;

    @Override
    public void keyTraversed(TraverseEvent e) {
      switch (e.detail) {
        case SWT.TRAVERSE_ESCAPE:
        case SWT.TRAVERSE_RETURN: {
          e.doit = false;
          break;
        }
        case SWT.TRAVERSE_TAB_NEXT: {
          handleTraverseEvent(e, SWT.TRAVERSE_TAB_NEXT, true);
          break;
        }
        case SWT.TRAVERSE_TAB_PREVIOUS: {
          handleTraverseEvent(e, SWT.TRAVERSE_TAB_PREVIOUS, false);
          break;
        }
      }
    }

    private void handleTraverseEvent(TraverseEvent event, Integer traverseKey, boolean forwardToNextCell) {
      event.doit = false;
      if (m_popupListener.isPopUpOpen()) {
        setTraverseKey(traverseKey);
      }
      else {
        verifyInputOnControlAndEditNextCell((Control) event.getSource(), forwardToNextCell);
      }
    }

    private void verifyInputOnControlAndEditNextCell(Control control, boolean forwardToNextCell) {
      RwtUtility.runUiInputVerifier(control);
      // fetch the editable row / column here because stopping the celleditor will nullify the row / column.
      ITableRow scoutTableRow = m_cellEditor.getScoutTableRow();
      IColumn<?> scoutTableColumn = m_cellEditor.getScoutTableColumn();
      m_cellEditor.stopCellEditing();
      m_tableCellEditor.enqueueEditNextTableCell(scoutTableRow, scoutTableColumn, forwardToNextCell);
    }
  }
}
