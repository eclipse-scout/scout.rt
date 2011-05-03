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
package org.eclipse.scout.rt.ui.swt.services;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class SwtScoutProgressService extends AbstractService {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(SwtScoutProgressService.class);
  public static final String BUSYID_NAME = "SWT BusyIndicator";
  public static final String CURSOR_BACKUP = "CursorBackup";

  private Object m_requestorListLock = new Object();
  private HashSet<Object> m_requestorList = new HashSet<Object>();
  private int m_currentBusyId;

  public SwtScoutProgressService() {
  }

  public void setWaitingCursor(boolean busyRequest, Object requestor) {
    boolean oldValue, newValue;
    synchronized (m_requestorListLock) {
      oldValue = m_requestorList.size() > 0;
      if (busyRequest) {
        m_requestorList.add(requestor);
      }
      else {
        m_requestorList.remove(requestor);
      }
      newValue = m_requestorList.size() > 0;
    }
    if (oldValue != newValue) {
      Display display = getDisplay();
      if (display != null) {
        Cursor cursor = display.getSystemCursor(SWT.CURSOR_WAIT);
        if (newValue) {
          final Shell[] shells = display.getShells();
          final AtomicInteger busyId = new AtomicInteger(Integer.MIN_VALUE);
          BusyIndicator.showWhile(display, new Runnable() {
            @Override
            public void run() {
              int max = Integer.MIN_VALUE;
              for (Shell shell : shells) {
                Integer id = (Integer) shell.getData(BUSYID_NAME);
                if (id != null) {
                  max = Math.max(max, id);
                }
              }
              busyId.set(max);
            }
          });
          if (busyId.get() > Integer.MIN_VALUE) {
            m_currentBusyId = busyId.get();

            for (Shell shell : shells) {
              shell.setCursor(cursor);
              shell.setData(BUSYID_NAME, m_currentBusyId);
              setWaitCursorOnCustomFieldsRec(cursor, shell.getChildren());
            }
          }
        }
        else {
          Shell[] shells = display.getShells();
          for (Shell shell : shells) {
            Integer id = (Integer) shell.getData(BUSYID_NAME);
            if (id != null && id <= m_currentBusyId) {
              shell.setCursor(null);
              shell.setData(BUSYID_NAME, null);
              unsetWaitCursorOnCustomFieldsRec(shell.getChildren(), cursor);
            }
          }
        }
      }
    }
  }

  private void setWaitCursorOnCustomFieldsRec(Cursor waitCursor, Control[] items) {
    if (items == null) {
      return;
    }
    for (Control c : items) {
      if (c instanceof Browser) {
        // does not work on browser
      }
      else if (c instanceof StyledText) {
        StyledText text = (StyledText) c;
        Cursor backupCursor = text.getCursor();
        if (backupCursor != null) {
          text.setData(CURSOR_BACKUP, backupCursor);
        }
        text.setCursor(waitCursor);
      }
      // next
      if (c instanceof Composite) {
        setWaitCursorOnCustomFieldsRec(waitCursor, ((Composite) c).getChildren());
      }
    }
  }

  private void unsetWaitCursorOnCustomFieldsRec(Control[] items, Cursor waitCursor) {
    if (items == null) {
      return;
    }
    for (Control c : items) {
      if (c instanceof Browser) {
        // does not work on browser
      }
      else if (c instanceof StyledText) {
        StyledText text = (StyledText) c;
        if (text.getCursor() == waitCursor) {
          Cursor backupCursor = (Cursor) text.getData(CURSOR_BACKUP);
          text.setData(CURSOR_BACKUP, null);
          text.setCursor(backupCursor);
        }
      }
      // next
      if (c instanceof Composite) {
        unsetWaitCursorOnCustomFieldsRec(((Composite) c).getChildren(), waitCursor);
      }
    }
  }

  private Display getDisplay() {
    Display d = null;
    if (PlatformUI.isWorkbenchRunning() && PlatformUI.getWorkbench() != null) {
      d = PlatformUI.getWorkbench().getDisplay();
    }
    if (d == null) {
      d = Display.getCurrent();
    }
    if (d == null) {
      d = Display.getDefault();
    }
    return d;
  }
}
