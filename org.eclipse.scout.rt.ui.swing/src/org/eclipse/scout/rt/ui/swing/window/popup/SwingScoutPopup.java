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
package org.eclipse.scout.rt.ui.swing.window.popup;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListener;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.ext.JWindowEx;
import org.eclipse.scout.rt.ui.swing.form.fields.AbstractLayoutManager2;
import org.eclipse.scout.rt.ui.swing.window.DependentCloseListener;
import org.eclipse.scout.rt.ui.swing.window.ISwingScoutView;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewEvent;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewListener;
import org.eclipse.scout.rt.ui.swing.window.SwingWindowManager;

/**
 * Popup window bound to a component (ownerComponent). The popup closes when
 * there is either a click outside this window or the component loses focus
 * (focusComponent), or the component becomes invisible.
 */
public class SwingScoutPopup implements ISwingScoutView {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutPopup.class);

  private ISwingEnvironment m_env;
  private int m_minWidth;
  private EventListenerList m_listenerList;
  private P_SwingScoutRootListener m_swingScoutRootListener;
  private Component m_ownerComponent;
  private Rectangle m_ownerBounds;
  private JWindowEx m_swingWindow;
  // cache
  private boolean m_maximized;
  private boolean m_positionBelowReferenceField;// ticket 76521
  private Rectangle m_boundsBeforeMaximize;
  private boolean m_opened;
  private boolean m_closeFired;

  public SwingScoutPopup(ISwingEnvironment env, Component ownerComponent, Rectangle ownerBounds) {
    this(env, ownerComponent, ownerBounds, env.getFormColumnWidth() / 2);
  }

  public SwingScoutPopup(ISwingEnvironment env, Component ownerComponent, Rectangle ownerBounds, int minWidth) {
    m_env = env;
    m_ownerComponent = ownerComponent;
    m_ownerBounds = ownerBounds;
    m_listenerList = new EventListenerList();
    m_minWidth = minWidth;
    m_positionBelowReferenceField = true;
    //
    Window w = SwingUtilities.getWindowAncestor(m_ownerComponent);
    m_swingWindow = new JWindowEx(w);
    m_swingWindow.addWindowListener(new P_SwingWindowListener());
    // border (only used by non-synth)
    if (!SwingUtility.isSynth()) {
      m_swingWindow.getRootPane().setBorder(UIManager.getBorder("SwingScoutPopup.border"));
    }
    //set synth name AFTER setting ui border
    JComponent contentPane = (JComponent) m_swingWindow.getContentPane();
    contentPane.setLayout(new P_PopupContentPaneLayout());
    contentPane.setCursor(Cursor.getDefaultCursor());
    m_swingWindow.getRootPane().setName("Synth.Popup");
    // wait cursor
    if (m_swingScoutRootListener == null) {
      m_swingScoutRootListener = new P_SwingScoutRootListener();
      m_env.addPropertyChangeListener(m_swingScoutRootListener);
    }
    m_swingWindow.pack();
  }

  public void autoAdjustBounds() {
    try {
      if (m_ownerComponent.isShowing()) {
        m_swingWindow.validate();
        m_swingWindow.getRootPane().revalidate();
        //
        Dimension d = m_swingWindow.getPreferredSize();
        d.width = Math.max(d.width, m_minWidth);
        int minWidth = Math.max(m_minWidth, m_swingWindow.getMinimumSize().width);
        Point p = m_ownerBounds.getLocation();
        Point above = new Point(p.x, p.y - 2);
        Rectangle aboveView = SwingUtility.intersectRectangleWithScreen(new Rectangle(above.x, above.y - d.height, d.width, d.height), false, false);
        if (aboveView.width < minWidth) {
          aboveView.x = aboveView.x + aboveView.width - minWidth;
          aboveView.width = minWidth;
        }
        Point below = new Point(p.x, p.y + 2 + m_ownerBounds.height);
        Rectangle belowView = SwingUtility.intersectRectangleWithScreen(new Rectangle(below.x, below.y, d.width, d.height), false, false);
        if (belowView.width < minWidth) {
          belowView.x = belowView.x + belowView.width - minWidth;
          belowView.width = minWidth;
        }
        // decide based on the preference positionBelowReferenceField
        Rectangle currentView = (m_positionBelowReferenceField ? belowView : aboveView);
        Rectangle alternateView = (m_positionBelowReferenceField ? aboveView : belowView);
        Rectangle ownerScreen = SwingUtility.getFullScreenBoundsFor(m_ownerBounds, false);
        if (currentView.height >= alternateView.height) {
          m_swingWindow.setBounds(SwingUtility.validateRectangleOnScreen(currentView, ownerScreen, true, true));
        }
        else {
          m_swingWindow.setBounds(SwingUtility.validateRectangleOnScreen(alternateView, ownerScreen, true, true));
          // toggle preference
          m_positionBelowReferenceField = !m_positionBelowReferenceField;
        }
        if (System.getProperty("java.version", "1.5").startsWith("1.5")) {
          m_swingWindow.validate();
          m_swingWindow.getRootPane().revalidate();
        }
      }
    }
    catch (Throwable t) {
      // this should not happen!
    }
  }

  public Window getSwingWindow() {
    return m_swingWindow;
  }

  public Component getSwingOwnerComponent() {
    return m_ownerComponent;
  }

  public JComponent getSwingContentPane() {
    return (JComponent) m_swingWindow.getContentPane();
  }

  public void addSwingScoutViewListener(SwingScoutViewListener listener) {
    m_listenerList.add(SwingScoutViewListener.class, listener);
  }

  public void removeSwingScoutViewListener(SwingScoutViewListener listener) {
    m_listenerList.remove(SwingScoutViewListener.class, listener);
  }

  protected void fireSwingScoutViewEvent(SwingScoutViewEvent e) {
    //avoid double close events
    switch (e.getType()) {
      case SwingScoutViewEvent.TYPE_OPENED: {
        m_closeFired = false;
        break;
      }
      case SwingScoutViewEvent.TYPE_CLOSED: {
        if (m_closeFired) {
          return;
        }
        m_closeFired = true;
        break;
      }
    }
    EventListener[] listeners = m_listenerList.getListeners(SwingScoutViewListener.class);
    if (listeners != null && listeners.length > 0) {
      for (int i = 0; i < listeners.length; i++) {
        try {
          ((SwingScoutViewListener) listeners[i]).viewChanged(e);
        }
        catch (Throwable t) {
          t.printStackTrace();
        }
      }
    }
  }

  protected void handleSwingWindowOpened() {
    SwingWindowManager.getInstance().setActiveWindow(getSwingWindow());
    fireSwingScoutViewEvent(new SwingScoutViewEvent(SwingScoutPopup.this, SwingScoutViewEvent.TYPE_OPENED));
  }

  protected void handleSwingWindowClosed() {
    if (getSwingWindow().isFocused()) {
      Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
      if (focusOwner != null && focusOwner instanceof JComponent && ((JComponent) focusOwner).getInputVerifier() != null) {
        ((JComponent) focusOwner).getInputVerifier().verify((JComponent) focusOwner);
      }
    }
    fireSwingScoutViewEvent(new SwingScoutViewEvent(SwingScoutPopup.this, SwingScoutViewEvent.TYPE_CLOSED));
  }

  public boolean isVisible() {
    return m_swingWindow != null && m_swingWindow.isVisible();
  }

  public boolean isActive() {
    return m_swingWindow != null && m_swingWindow.isActive();
  }

  public void openView() {
    m_opened = true;
    autoAdjustBounds();
    if (m_opened) {
      m_swingWindow.setVisible(true);
    }
  }

  public void closeView() {
    if (m_opened) {
      m_opened = false;
      if (m_swingScoutRootListener != null) {
        m_env.removePropertyChangeListener(m_swingScoutRootListener);
        m_swingScoutRootListener = null;
      }
      new DependentCloseListener(m_swingWindow).close();
    }
  }

  public void setTitle(String s) {
    // nop
  }

  public void setCloseEnabled(boolean b) {
  }

  public void setMaximizeEnabled(boolean b) {
  }

  public void setMinimizeEnabled(boolean b) {
  }

  public void setMinimized(boolean on) {
  }

  public void setMaximized(boolean on) {
    m_maximized = on;
    if (on) {
      if (m_boundsBeforeMaximize == null) {
        m_boundsBeforeMaximize = m_swingWindow.getBounds();
      }
      Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
      Insets in = Toolkit.getDefaultToolkit().getScreenInsets(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
      Rectangle r = new Rectangle();
      if (in != null) {
        r.x = in.left;
        r.y = in.top;
        r.width = d.width - in.left - in.right;
        r.height = d.height - in.top - in.bottom;
      }
      else {
        r.x = 0;
        r.y = 0;
        r.width = d.width;
        r.height = d.height;
      }
      m_swingWindow.setBounds(r);
    }
    else {
      if (m_boundsBeforeMaximize != null) {
        m_swingWindow.setBounds(m_boundsBeforeMaximize);
        m_boundsBeforeMaximize = null;
      }
    }
  }

  public void setName(String name) {
    m_swingWindow.setName(name);
    m_swingWindow.getRootPane().setName(name);
  }

  private class P_SwingScoutRootListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent e) {
      if (e.getPropertyName().equals(ISwingEnvironment.PROP_BUSY)) {
        boolean busy = ((Boolean) e.getNewValue()).booleanValue();
        m_swingWindow.setWaitCursor(busy);
      }
    }
  }// end private class

  private class P_PopupContentPaneLayout extends AbstractLayoutManager2 {
    @Override
    protected Dimension getLayoutSize(Container parent, int sizeflag) {
      Insets insets = parent.getInsets();
      Dimension d = new Dimension();
      // width
      d.width = 0;
      for (Component c : parent.getComponents()) {
        Dimension ps = SwingLayoutUtility.getSize(c, sizeflag);
        d.width += ps.width;
      }
      d.width += insets.left + insets.right;
      // height
      d.height = 0;
      for (Component c : parent.getComponents()) {
        Dimension ps = SwingLayoutUtility.getSize(c, sizeflag);
        d.height = Math.max(d.height, ps.height);
      }
      d.height += insets.top + insets.bottom;
      return d;
    }

    @Override
    protected void validateLayout(Container parent) {
    }

    @Override
    public void layoutContainer(Container parent) {
      verifyLayout(parent);
      synchronized (parent.getTreeLock()) {
        /*
         * necessary as workaround for awt bug: when component does not change
         * size, its reported minimumSize, preferredSize and maximumSize are
         * cached instead of beeing calculated using layout manager
         */
        for (Component c : parent.getComponents()) {
          c.setBounds(0, 0, 0, 0);
        }
        //
        Insets insets = parent.getInsets();
        Rectangle view = new Rectangle(insets.left, insets.top, parent.getWidth() - insets.left - insets.right, parent.getHeight() - insets.top - insets.bottom);
        if (parent.getComponentCount() > 0) {
          parent.getComponent(0).setBounds(view.x, view.y, view.width, view.height);
        }
      }
    }
  }

  private class P_SwingWindowListener extends WindowAdapter {
    @Override
    public void windowOpened(WindowEvent e) {
      handleSwingWindowOpened();
    }

    @Override
    public void windowActivated(WindowEvent e) {
      SwingWindowManager.getInstance().setActiveWindow(getSwingWindow());
      fireSwingScoutViewEvent(new SwingScoutViewEvent(SwingScoutPopup.this, SwingScoutViewEvent.TYPE_ACTIVATED));
    }

    @Override
    public void windowClosing(WindowEvent e) {
      Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
      if (focusOwner != null && focusOwner instanceof JComponent && ((JComponent) focusOwner).getInputVerifier() != null) {
        boolean ok = ((JComponent) focusOwner).getInputVerifier().verify((JComponent) focusOwner);
        if (!ok) {
          return;
        }
      }
      fireSwingScoutViewEvent(new SwingScoutViewEvent(SwingScoutPopup.this, SwingScoutViewEvent.TYPE_CLOSING));
    }

    @Override
    public void windowClosed(WindowEvent e) {
      handleSwingWindowClosed();
    }
  }// end private class
}
