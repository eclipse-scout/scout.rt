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
package org.eclipse.scout.rt.ui.rap.ext;

import java.util.ArrayList;

import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;

/**
 * <h3>ButtonEx</h3> added the context menu to the button
 * 
 * @since 3.7.0 June 2011
 */
public class ButtonEx extends Button {
  private static final long serialVersionUID = 1L;

  public static final int SELECTION_ACTION = 44;
  public static final int SELECTION_MENU = 45;

  private boolean m_hasDropDown;
  private boolean m_dropDownEnabled;

  private Object m_listenerLock = new Object();
  private Listener[] m_actionSelectionListener;
  private Listener[] m_menuSelectionListener;

  public ButtonEx(Composite parent, int style) {
    super(parent, style);
    // dropdown
    P_DelegateSelectionListener delegateListener = new P_DelegateSelectionListener();
    if ((style & SWT.DROP_DOWN) != 0) {
      m_hasDropDown = true;
      super.addListener(SWT.MouseUp, delegateListener);
    }
    super.addListener(SWT.Selection, delegateListener);
  }

  private IRwtEnvironment getUiEnvironment() {
    return (IRwtEnvironment) getDisplay().getData(IRwtEnvironment.class.getName());
  }

  @Override
  public void addListener(int eventType, Listener listener) {
    switch (eventType) {
      case SELECTION_ACTION:
        m_actionSelectionListener = addListenerToList(listener, m_actionSelectionListener);
        break;
      case SELECTION_MENU:
        m_menuSelectionListener = addListenerToList(listener, m_menuSelectionListener);
        break;
      default:
        super.addListener(eventType, listener);
        break;
    }
  }

  @Override
  public void removeListener(int eventType, Listener listener) {
    switch (eventType) {
      case SELECTION_ACTION:
        m_actionSelectionListener = removeListenerFromList(listener, m_actionSelectionListener);
        break;
      case SELECTION_MENU:
        m_menuSelectionListener = removeListenerFromList(listener, m_menuSelectionListener);
        break;
      default:
        super.removeListener(eventType, listener);
        break;
    }
  }

  private Listener[] addListenerToList(Listener l, Listener[] arr) {
    synchronized (m_listenerLock) {
      if (arr == null) {
        arr = new Listener[1];
        arr[0] = l;
      }
      else {
        Listener[] newArr = new Listener[arr.length + 1];
        System.arraycopy(arr, 0, newArr, 0, arr.length);
        arr = newArr;
        arr[arr.length - 1] = l;
      }
      return arr;
    }
  }

  private Listener[] removeListenerFromList(Listener l, Listener[] arr) {
    synchronized (m_listenerLock) {
      if (arr == null) {
        return arr;
      }
      else {
        ArrayList<Listener> newList = new ArrayList<Listener>();
        for (Listener ref : arr) {
          if (!ref.equals(l)) {
            newList.add(ref);
          }
        }
        arr = newList.toArray(new Listener[newList.size()]);
      }
      return arr;
    }
  }

  private void fireMenuSelection(Event e) {
    int backupedType = e.type;
    try {
      e.type = SELECTION_MENU;
      synchronized (m_listenerLock) {
        if (m_menuSelectionListener != null) {
          for (Listener l : m_menuSelectionListener) {
            l.handleEvent(e);
          }
        }
      }
    }
    finally {
      e.type = backupedType;
    }
  }

  private void fireActionSelection(Event e) {
    if (m_actionSelectionListener != null) {
      int backupedType = e.type;
      try {
        e.type = SELECTION_ACTION;
        synchronized (m_listenerLock) {
          for (Listener l : m_actionSelectionListener) {
            l.handleEvent(e);
          }
        }
      }
      finally {
        e.type = backupedType;
      }
    }
  }

  public void setDropDownEnabled(boolean enabled) {
    m_dropDownEnabled = enabled;
  }

  public boolean isDropDownEnabled() {
    return m_dropDownEnabled;
  }

  @Override
  protected void checkSubclass() {
    // allow subclassing
  }

  protected void handleButtonSelection(Event event) {
    fireActionSelection(event);
  }

  protected void handlePopupSelectionEvent(Event e) {
    if (m_dropDownEnabled) {
      Menu menu = getMenu();
      if (menu != null) {
        menu.setVisible(true);
      }
    }
    fireMenuSelection(e);
  }

  private class P_DelegateSelectionListener implements Listener {
    private static final long serialVersionUID = 1L;
    private long timeLastEvent = 0L;

    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.MouseUp: {
          if (timeLastEvent != 0L && timeLastEvent == event.time) {
            return;
          }
          timeLastEvent = event.time;
          switch (event.button) {
            case 1:
              // left mouse button
              if (event.x > (getBounds().width - 12)) {
                // popup
                handlePopupSelectionEvent(event);
              }
              else {
                handleButtonSelection(event);
              }
              break;
            case 3:
              // right mouse button
              // void
              break;
            default:
              break;
          }
          break;
        }
        case SWT.Selection: {
          if (timeLastEvent != 0L && timeLastEvent == event.time) {
            return;
          }
          timeLastEvent = event.time;
          handleButtonSelection(event);
          break;
        }
        default:
          break;
      }
    }
  }
}
