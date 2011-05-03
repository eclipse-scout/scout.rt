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
package org.eclipse.scout.rt.ui.swt.ext;

import java.util.ArrayList;

import org.eclipse.scout.rt.ui.swt.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;

/**
 * <h3>ButtonEx</h3> added the context menu to the button
 * 
 * @since 1.0.0 11.04.2008
 */
public class ButtonEx extends Button {

  public static final int SELECTION_ACTION = 44;
  public static final int SELECTION_MENU = 45;

  private boolean m_hasDropDown;
  private boolean m_dropDownEnabled;

  private Object m_listenerLock = new Object();
  private Listener[] m_actionSelectionListener;
  private Listener[] m_menuSelectionListener;
  private Image m_dropDownIcon;

  private P_DropDownPaintListener m_dropDownPaintListener;
  private Listener m_paintListener = new Listener() {
    @Override
    public void handleEvent(Event event) {
      redraw();
    }
  };

  public ButtonEx(Composite parent, int style) {
    super(parent, style);
    m_dropDownIcon = Activator.imageDescriptorFromLocalPlugin(Activator.ICON_ARROW_DOWN).createImage();
    // dopdown
    P_DelegateSelectionListener delegateListener = new P_DelegateSelectionListener();
    if ((style & SWT.DROP_DOWN) != 0) {
      m_hasDropDown = true;
      m_dropDownPaintListener = new P_DropDownPaintListener();
      addPaintListener(m_dropDownPaintListener);
      super.addListener(SWT.MouseUp, delegateListener);
      /*
       * there is a paint event in the OS repainting all the buttons once a
       * time. to ensure the customized dropdown-menu will be redraw, the event
       * is delegated to the button
       */
      getParent().getParent().addListener(SWT.Paint, m_paintListener);
    }
    super.addListener(SWT.Selection, delegateListener);
    addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        freeResources();
      }
    });
  }

  /**
   * since tab list on parent does not work
   */
  @Override
  public boolean forceFocus() {
    if ((getStyle() & SWT.NO_FOCUS) != 0) {
      return false;
    }
    else {
      return super.forceFocus();
    }
  }

  public void freeResources() {
    getParent().getParent().removeListener(SWT.Paint, m_paintListener);
    if (m_dropDownIcon != null && !m_dropDownIcon.isDisposed()) {
      m_dropDownIcon.dispose();
      m_dropDownIcon = null;
    }
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
  public void setEnabled(boolean enabled) {
    // XXX dropdown support
    super.setEnabled(enabled);
  }

  @Override
  public Point computeSize(int hint, int hint2, boolean changed) {
    Point computedSize = super.computeSize(hint, hint2, changed);
    Point p = new Point(computedSize.x, computedSize.y);
    if (m_hasDropDown) {
      p.x += 12;
    }
    return p;
  }

  @Override
  protected void checkSubclass() {
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
    private long timeLastEvent = 0L;

    @Override
    public void handleEvent(Event event) {

      switch (event.type) {
        case SWT.MouseUp: {
          if (timeLastEvent == event.time) {
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
          if (timeLastEvent == event.time) {
            return;
          }
          timeLastEvent = event.time;
          handleButtonSelection(event);
          break;
        }
      }
    }
  }

  private class P_DropDownPaintListener implements PaintListener {
    @Override
    public void paintControl(PaintEvent e) {
      GC gc = e.gc;
      gc.setAdvanced(true);

      if (m_dropDownEnabled) {
        gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
        gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
      }
      else {
        gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
        gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
      }

      Rectangle bounds = getBounds();
      gc.drawLine(bounds.width - 12, 3, bounds.width - 12, bounds.height - 4);
      gc.drawImage(m_dropDownIcon, bounds.width - 11, (bounds.height - m_dropDownIcon.getBounds().height) / 2);
      // gc.fillPolygon(new int[] { bounds.width - 10, bounds.height / 2,
      // bounds.width - 6, bounds.height - 7, bounds.width - 3, bounds.height /
      // 2 });

    }
  } // end class P_DropDownPaintListener

}
