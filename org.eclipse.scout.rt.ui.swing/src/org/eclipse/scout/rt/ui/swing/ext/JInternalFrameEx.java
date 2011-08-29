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
package org.eclipse.scout.rt.ui.swing.ext;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.beans.PropertyVetoException;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

/**
 * <ul>
 * <li>fix layout to BorderLayoutEx</li>
 * <li>fix dimensions</li>
 * <li>use container sizes and not ui sizes. support for non-y-resizing frames</li>
 * <li>set cursor to null when default to support for overall hour-glass cursor when parent frame is showing busy</li>
 * <li>support for a model tab index to be used instead of simply container order tabbing</li>
 * <li>fixed behaviour for resize-cursor display. only visible when resizing is in fact possible</li>
 * <li>fix resture sub focus: transferFocus instead of request focus and only use existing focus component iff it is
 * still showing and enabled</li>
 * </ul>
 */
public class JInternalFrameEx extends JInternalFrame implements IWaitSupport {
  private static final long serialVersionUID = 1L;
  private int m_tabIndex = 1;
  private boolean m_resizableY;
  private boolean m_waitCursor;

  public static final String CLIENT_PROP_N_RESIZE_ENABLED = "swingScout.resizeNEnabled";
  public static final String CLIENT_PROP_W_RESIZE_ENABLED = "swingScout.resizeWEnabled";
  public static final String CLIENT_PROP_E_RESIZE_ENABLED = "swingScout.resizeEEnabled";
  public static final String CLIENT_PROP_S_RESIZE_ENABLED = "swingScout.resizeSEnabled";

  public JInternalFrameEx(String title, String name, boolean closable, boolean maximizable, boolean iconifiable) {
    super(title, true, closable, maximizable, iconifiable);
    m_resizableY = true;
    JComponent contentPane = (JComponent) getContentPane();
    contentPane.setCursor(Cursor.getDefaultCursor());
    // WORKAROUND correct layout (swing is not capable of considering the maximum size
    contentPane.setLayout(new BorderLayoutEx());
    // end workaround
    setCursor(null);
    setName(name);
  }

  public int getTabIndex() {
    return m_tabIndex;
  }

  public void setTabIndex(int i) {
    m_tabIndex = i;
  }

  public boolean isResizableY() {
    return m_resizableY;
  }

  public void setResizableY(boolean b) {
    m_resizableY = b;
  }

  @Override
  public void setCursor(Cursor c) {
    /**
     * @rn imo 23.02.2007, mask out resize cursors when not between two frames
     */
    if (c != null) {
      switch (c.getType()) {
        case Cursor.DEFAULT_CURSOR: {
          c = null;
          break;
        }
        case Cursor.N_RESIZE_CURSOR: {
          if (getClientProperty(CLIENT_PROP_N_RESIZE_ENABLED) != Boolean.TRUE) {
            c = null;
          }
          break;
        }
        case Cursor.NW_RESIZE_CURSOR: {
          if (getClientProperty(CLIENT_PROP_N_RESIZE_ENABLED) != Boolean.TRUE || getClientProperty(CLIENT_PROP_W_RESIZE_ENABLED) != Boolean.TRUE) {
            c = null;
          }
          break;
        }
        case Cursor.W_RESIZE_CURSOR: {
          if (getClientProperty(CLIENT_PROP_W_RESIZE_ENABLED) != Boolean.TRUE) {
            c = null;
          }
          break;
        }
        case Cursor.SW_RESIZE_CURSOR: {
          if (getClientProperty(CLIENT_PROP_S_RESIZE_ENABLED) != Boolean.TRUE || getClientProperty(CLIENT_PROP_W_RESIZE_ENABLED) != Boolean.TRUE) {
            c = null;
          }
          break;
        }
        case Cursor.S_RESIZE_CURSOR: {
          if (getClientProperty(CLIENT_PROP_S_RESIZE_ENABLED) != Boolean.TRUE) {
            c = null;
          }
          break;
        }
        case Cursor.SE_RESIZE_CURSOR: {
          if (getClientProperty(CLIENT_PROP_S_RESIZE_ENABLED) != Boolean.TRUE || getClientProperty(CLIENT_PROP_E_RESIZE_ENABLED) != Boolean.TRUE) {
            c = null;
          }
          break;
        }
        case Cursor.E_RESIZE_CURSOR: {
          if (getClientProperty(CLIENT_PROP_E_RESIZE_ENABLED) != Boolean.TRUE) {
            c = null;
          }
          break;
        }
        case Cursor.NE_RESIZE_CURSOR: {
          if (getClientProperty(CLIENT_PROP_N_RESIZE_ENABLED) != Boolean.TRUE || getClientProperty(CLIENT_PROP_E_RESIZE_ENABLED) != Boolean.TRUE) {
            c = null;
          }
          break;
        }
      }
    }
    super.setCursor(c);
  }

  /**
   * Bug fix of swing concept (1) focus must NOT be set "to some default" when
   * frame is deselected. just leave it as is (2) avoid setting seleted true on
   * frame show when adding to pane
   */
  private boolean m_selectedEnabled = true;

  @Override
  public void setVisible(boolean aFlag) {
    try {
      m_selectedEnabled = false;
      //
      super.setVisible(aFlag);
    }
    finally {
      m_selectedEnabled = true;
    }
  }

  /**
   * Bug fix for swing do not set selected when a frame's visibility changes
   */
  @Override
  public void setSelected(boolean selected) throws PropertyVetoException {
    if (!m_selectedEnabled) {
      return;
    }
    super.setSelected(selected);
  }

  /**
   * Bug fix for swing missing null check on desktop icon
   */
  @Override
  public JDesktopPane getDesktopPane() {
    Container p;
    p = getParent();
    while (p != null && !(p instanceof JDesktopPane)) {
      p = p.getParent();
    }

    if (p == null) {
      if (getDesktopIcon() != null) {
        p = getDesktopIcon().getParent();
      }
      while (p != null && !(p instanceof JDesktopPane)) {
        p = p.getParent();
      }
    }
    return (JDesktopPane) p;
  }

  /*
   * Swing InternalFrame layout is using ui layout INSTEAD of layout manager
   */
  @Override
  public Dimension getMinimumSize() {
    Dimension d;
    if ((!isIcon()) && (getLayout() instanceof LayoutManager2)) {
      synchronized (getTreeLock()) {
        LayoutManager2 lm = (LayoutManager2) getLayout();
        d = lm.minimumLayoutSize(this);
        if (!isResizableY()) {
          Dimension pref = lm.preferredLayoutSize(this);
          d.height = pref.height;
        }
      }
    }
    else {
      d = super.getMinimumSize();
    }
    return d;
  }

  /*
   * Swing InternalFrame layout is using ui layout INSTEAD of layout manager
   */
  @Override
  public Dimension getPreferredSize() {
    Dimension d;
    if ((!isIcon()) && (getLayout() instanceof LayoutManager2)) {
      synchronized (getTreeLock()) {
        LayoutManager2 lm = (LayoutManager2) getLayout();
        d = lm.preferredLayoutSize(this);
      }
    }
    else {
      d = super.getPreferredSize();
    }
    return d;
  }

  /*
   * Swing InternalFrame layout is using ui layout INSTEAD of layout manager
   */
  @Override
  public Dimension getMaximumSize() {
    Dimension d;
    if ((!isIcon()) && (getLayout() instanceof LayoutManager2)) {
      synchronized (getTreeLock()) {
        LayoutManager2 lm = (LayoutManager2) getLayout();
        d = lm.maximumLayoutSize(this);
        if (!isResizableY()) {
          Dimension pref = lm.preferredLayoutSize(this);
          d.height = pref.height;
        }
      }
    }
    else {
      d = super.getMaximumSize();
    }
    return d;
  }

  @Override
  public void restoreSubcomponentFocus() {
    if (isIcon()) {
      super.restoreSubcomponentFocus();
    }
    else {
      Component c = getMostRecentFocusOwner();
      if (c != null && c.isShowing() && c.isEnabled()) {
        c.requestFocus();
      }
      else {
        getContentPane().transferFocus();
      }
    }
  }

  @Override
  public boolean isWaitCursor() {
    return m_waitCursor;
  }

  @Override
  public void setWaitCursor(boolean b) {
    if (b != m_waitCursor) {
      m_waitCursor = b;
      Component comp = getContentPane();
      if (m_waitCursor) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        comp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      }
      else {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        comp.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    }
  }

}
