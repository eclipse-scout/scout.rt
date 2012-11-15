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

import java.util.Date;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.basic.AbstractOpenMenuJob;
import org.eclipse.scout.rt.ui.rap.keystroke.RwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.util.BrowserInfo;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class DropDownButton extends Button implements IDropDownButtonForPatch {
  private static final long serialVersionUID = 1L;

  private Rectangle m_buttonArea = new Rectangle(1, 1, 13, 17);
  private Rectangle m_dropDownArea = new Rectangle(14, 1, 10, 17);
  private EventListenerList m_eventListeners = new EventListenerList();

  private Point m_mouseDownPosition;
  private boolean m_dropdownEnabled = true;
  private boolean m_buttonEnabled = true;

  private String m_originalVariant = "";

  public DropDownButton(Composite parent, int style) {
    super(parent, style | SWT.DOUBLE_BUFFERED);

    addListener(SWT.Traverse, new Listener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void handleEvent(Event e) {
        switch (e.detail) {
          /* Do tab group traversal */
          case SWT.TRAVERSE_ESCAPE:
          case SWT.TRAVERSE_RETURN:
          case SWT.TRAVERSE_TAB_NEXT:
          case SWT.TRAVERSE_TAB_PREVIOUS:
            e.doit = true;
            break;
        }
      }
    });
    getUiEnvironment().addKeyStroke(this, new P_KeyStroke(' '), false);
    getUiEnvironment().addKeyStroke(this, new P_KeyStroke(SWT.CR), false);
    getUiEnvironment().addKeyStroke(this, new P_KeyStroke(SWT.ARROW_DOWN), false);

    addFocusListener(new FocusAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void focusGained(FocusEvent e) {
        redraw();
      }

      @Override
      public void focusLost(FocusEvent e) {
        redraw();
      }
    });
    addMouseListener(new MouseAdapter() {
      private static final long serialVersionUID = 1L;

      private long m_mouseDownTime = 0;
      private P_OpenMenuJob m_openMenuJob = new P_OpenMenuJob(DropDownButton.this);

      @Override
      public void mouseDown(MouseEvent event) {
        m_mouseDownPosition = new Point(event.x, event.y);
        if (isDropdownEnabled()) {
          m_mouseDownTime = new Date().getTime();
          m_openMenuJob.startOpenJob(m_mouseDownPosition);
        }
        redraw();
      }

      @Override
      public void mouseUp(MouseEvent event) {
        if (event.button == 1) {
          BrowserInfo browserInfo = RwtUtility.getBrowserInfo();
          if (browserInfo.isTablet()
              || browserInfo.isMobile()) {
            long mouseUpTime = new Date().getTime();
            if (mouseUpTime - m_mouseDownTime <= 500L) {
              m_openMenuJob.stopOpenJob();
            }
          }
          handleSelectionInternal(event);
        }
        m_mouseDownPosition = null;
        redraw();
      }
    });

    addDisposeListener(new DisposeListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetDisposed(DisposeEvent e) {
        // remove key strokes
        getUiEnvironment().removeKeyStrokes(DropDownButton.this);
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

  protected void handleSelectionInternal(MouseEvent event) {
    Point pt = new Point(event.x, event.y);
    if (m_buttonArea.contains(pt)) {
      Event e = new Event();
      e.button = event.button;
      e.count = 1;
      e.data = event.data;
      e.display = event.display;
      e.stateMask = event.stateMask;
      e.time = event.time;
      e.widget = event.widget;
      e.x = event.x;
      e.y = event.y;
      fireSelectionEvent(new SelectionEvent(e));
    }
    else if (m_dropDownArea.contains(pt) && isDropdownEnabled()) {
      if (getMenu() != null) {
        getMenu().setLocation(toDisplay(event.x, event.y));
        getMenu().setVisible(true);
      }
    }
  }

  @Override
  public void fireSelectionEvent(SelectionEvent e) {
    if (isButtonEnabled()) {
      for (SelectionListener l : m_eventListeners.getListeners(SelectionListener.class)) {
        l.widgetSelected(e);
      }
    }
  }

  @Override
  public void addSelectionListener(SelectionListener listener) {
    m_eventListeners.add(SelectionListener.class, listener);
  }

  @Override
  public void removeSelectionListener(SelectionListener listener) {
    m_eventListeners.remove(SelectionListener.class, listener);
  }

  @Override
  public void setDropdownEnabled(boolean enabled) {
    m_dropdownEnabled = enabled;
    if (!StringUtility.hasText(m_originalVariant)) {
      m_originalVariant = (String) getData(WidgetUtil.CUSTOM_VARIANT);
    }
    String customVariant = m_dropdownEnabled ? m_originalVariant + "_menu" : m_originalVariant;
    setData(WidgetUtil.CUSTOM_VARIANT, customVariant);
    super.setEnabled(isButtonEnabled());
    redraw();
  }

  @Override
  public boolean isDropdownEnabled() {
    return m_dropdownEnabled;
  }

  @Override
  public void setButtonEnabled(boolean enabled) {
    m_buttonEnabled = enabled;
    super.setEnabled(isButtonEnabled());
    redraw();
  }

  @Override
  public boolean isButtonEnabled() {
    return m_buttonEnabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    m_buttonEnabled = enabled;
    m_dropdownEnabled = enabled;
    redraw();
  }

  @Override
  protected void checkSubclass() {
    // allow subclassing
  }

  private IRwtEnvironment getUiEnvironment() {
    return (IRwtEnvironment) getDisplay().getData(IRwtEnvironment.class.getName());
  }

  private class P_KeyStroke extends RwtKeyStroke {
    public P_KeyStroke(int keyCode) {
      super(keyCode);
    }

    @Override
    public void handleUiAction(Event e) {
      switch (e.keyCode) {
        case ' ':
        case SWT.CR: {
          SelectionEvent selEvent = new SelectionEvent(e);
          fireSelectionEvent(selEvent);
          break;
        }
        case SWT.ARROW_DOWN: {
          if (isDropdownEnabled() && getMenu() != null) {
            getMenu().setLocation(toDisplay(e.x, e.y));
            getMenu().setVisible(true);
          }
          break;
        }
        default:
          break;
      }
    }
  }

  private final class P_OpenMenuJob extends AbstractOpenMenuJob {

    public P_OpenMenuJob(Control UiField) {
      super(UiField);
    }

    @Override
    public void showMenu(Point pt) {
      getMenu().setLocation(pt);
      getMenu().setVisible(true);
    }

    @Override
    public boolean openMenuCheck() {
      return isDropdownEnabled()
          && !isDisposed()
          && getMenu() != null
          && !getMenu().isDisposed();
    }
  }
}
