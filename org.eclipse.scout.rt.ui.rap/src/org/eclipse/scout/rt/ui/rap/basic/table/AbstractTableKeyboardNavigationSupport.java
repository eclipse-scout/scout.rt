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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rwt.lifecycle.UICallBack;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.rap.ext.table.TableEx;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

/**
 * <h3>TableKeyBoardNavigationSupport</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.7.0 June 2011
 */
public abstract class AbstractTableKeyboardNavigationSupport {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractTableKeyboardNavigationSupport.class);

  private final long m_delay;
  private long m_timeoutTimestamp;
  private String m_filterText = "";
  private Object navigationLock = new Object();
  private P_NavigationJob m_navigationJob;

  private final TableEx m_uiTable;
  private Listener m_uiTableListener;
  private int m_contextColumnIndex;

  private DisposeListener m_disposeListener = new DisposeListener() {
    private static final long serialVersionUID = 1L;

    @Override
    public void widgetDisposed(DisposeEvent e) {
      detachListeners();
    }
  };

  public AbstractTableKeyboardNavigationSupport(TableEx uiTable) {
    this(uiTable, 500L);
  }

  public AbstractTableKeyboardNavigationSupport(TableEx uiTable, long delay) {
    m_delay = delay;
    m_navigationJob = new P_NavigationJob();

    m_uiTable = uiTable;
    attachListeners();
  }

  public void dispose() {
    detachListeners();
  }

  public void resetContextColumnIndex() {
    m_contextColumnIndex = -1;
  }

  private void attachListeners() {
    if (m_uiTableListener == null) {

      m_uiTableListener = new P_TableListener();
      m_uiTable.addListener(SWT.MouseDown, m_uiTableListener);
// sle 20120110: in webui this should be made in the client. it is to costly to go to the server with every keypress
//      m_uiTable.addListener(SWT.KeyDown, m_uiTableListener);
    }
    m_uiTable.addDisposeListener(m_disposeListener);
  }

  private void detachListeners() {
    if (m_uiTableListener != null) {
      if (!m_uiTable.isDisposed()) {
        m_uiTable.removeListener(SWT.MouseDown, m_uiTableListener);
// sle 20120110: in webui this should be made in the client. it is to costly to go to the server with every keypress
//        m_uiTable.removeListener(SWT.KeyDown, m_uiTableListener);
      }
      m_uiTableListener = null;
    }
    if (!m_uiTable.isDisposed()) {
      m_uiTable.removeDisposeListener(m_disposeListener);
    }
  }

  public void addChar(char c) {
    synchronized (navigationLock) {
      if (Character.isLetter(c)) {
        if (System.currentTimeMillis() > m_timeoutTimestamp) {
          m_filterText = "";
        }
        String newText = "" + Character.toLowerCase(c);
        m_filterText += newText;
        if (m_navigationJob != null) {
          m_navigationJob.cancel();
        }
        else {
          m_navigationJob = new P_NavigationJob();
        }
        UICallBack.activate(m_uiTable.getClass().getName() + m_uiTable.hashCode());
        m_navigationJob.schedule(300L);
        m_timeoutTimestamp = System.currentTimeMillis() + m_delay;
      }
    }
  }

  void handleSearchPattern(final String regex) {
    m_uiTable.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        int column = m_contextColumnIndex;
        if (column < 0) {
          for (int i = 0; i < m_uiTable.getColumnCount(); i++) {
            if (m_uiTable.getSortColumn() == m_uiTable.getColumn(i)) {
              column = i;
              break;
            }
          }
        }
        if (column < 0 && m_uiTable.getColumnCount() > 0) {
          column = 0;
        }
        if (column < 0) {
          return;
        }
        if (LOG.isInfoEnabled()) {
          LOG.info("finding regex:" + regex + " in column " + m_uiTable.getColumn(column).getText());
        }
        // loop over values and find matching one
        int startIndex = 0;
        if (m_uiTable.getSelectionIndex() < 0) {
          startIndex = 0;
        }
        else {
          startIndex = m_uiTable.getSelectionIndex() + 1;
        }
        int itemCount = m_uiTable.getItemCount();
        for (int i = 0; i < itemCount; i++) {
          TableItem tableItem = m_uiTable.getItem((startIndex + i) % itemCount);
          String itemText = tableItem.getText(column);
          if (itemText != null && itemText.toLowerCase().matches(regex)) {
            handleKeyboardNavigation(tableItem);
            break;
          }
        }
        UICallBack.deactivate(m_uiTable.getClass().getName() + m_uiTable.hashCode());
      }
    });
  }

  private class P_NavigationJob extends Job {

    public P_NavigationJob() {
      super("");
      setSystem(true);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      String pattern;
      synchronized (navigationLock) {
        if (monitor.isCanceled() || StringUtility.isNullOrEmpty(m_filterText)) {
          return Status.CANCEL_STATUS;
        }
        pattern = StringUtility.toRegExPattern(m_filterText.toLowerCase());
        pattern = pattern + ".*";
      }
      //this call must be outside lock!
      handleSearchPattern(pattern);
      return Status.OK_STATUS;
    }
  } // end class P_NavigationJob

  abstract void handleKeyboardNavigation(TableItem tableItem);

  private class P_TableListener implements Listener {
    private static final long serialVersionUID = 1L;

    @Override
    public void handleEvent(Event event) {
      if (event.doit) {
        switch (event.type) {
// sle 20120110: in webui this should be made in the client. it is to costly to go to the server with every keypress
//          case SWT.KeyDown:
//            if ((event.stateMask == 0 || event.stateMask == SWT.SHIFT)
//                && Character.isLetter((char) event.keyCode) && (event.keyCode != SWT.KEYPAD_CR)) {//XXX RAP does not no the difference between KEYPAD_CR and CR
//              addChar(Character.toLowerCase((char) event.keyCode));
//            }
//            break;
          case SWT.MouseDown:
            Point p = new Point(event.x, event.y);
            TableItem item = m_uiTable.getItem(p);
            if (item != null) {
              for (int i = 0; i < m_uiTable.getColumnCount(); i++) {
                Rectangle rect = item.getBounds(i);
                if (rect.contains(p)) {
                  m_contextColumnIndex = i;
                  break;
                }
              }
            }
            break;
          default:
            break;
        }
      }
    }
  } // end class P_TableListener

}
