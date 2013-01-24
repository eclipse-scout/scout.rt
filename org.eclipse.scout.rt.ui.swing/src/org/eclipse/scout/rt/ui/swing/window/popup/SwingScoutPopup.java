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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import org.eclipse.scout.rt.ui.swing.ext.busy.SwingBusyIndicator;
import org.eclipse.scout.rt.ui.swing.form.fields.AbstractLayoutManager2;
import org.eclipse.scout.rt.ui.swing.window.DependentCloseListener;
import org.eclipse.scout.rt.ui.swing.window.ISwingScoutView;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewEvent;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewListener;

/**
 * Popup window bound to a component (ownerComponent). The popup closes when
 * there is either a click outside this window or the component loses focus
 * (focusComponent), or the component becomes invisible.
 */
public class SwingScoutPopup implements ISwingScoutView {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutPopup.class);

  private ISwingEnvironment m_env;
  private EventListenerList m_listenerList;
  private Component m_ownerComponent;
  private ComponentListener m_ownerComponentListener;
  private Rectangle m_ownerBounds;
  private JWindowEx m_swingWindow;
  private boolean m_resizable;
  private P_ResizeListener m_resizeListener;
  // cache
  private boolean m_maximized;
  private boolean m_positionBelowReferenceField;// ticket 76521
  private Rectangle m_boundsBeforeMaximize;
  private boolean m_opened;
  private boolean m_closeFired;

  private int m_fixedWidth;
  private boolean m_popupOnField;

  public SwingScoutPopup(ISwingEnvironment env, Component ownerComponent, Rectangle ownerBounds) {
    m_env = env;
    m_ownerComponent = ownerComponent;
    m_ownerBounds = ownerBounds;
    m_listenerList = new EventListenerList();
    m_positionBelowReferenceField = true;
    m_resizeListener = new P_ResizeListener();
    //
    Window w = SwingUtilities.getWindowAncestor(m_ownerComponent);
    m_swingWindow = new JWindowEx(w);
    m_swingWindow.getRootPane().putClientProperty(SwingBusyIndicator.BUSY_SUPPORTED_CLIENT_PROPERTY, true);
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
    m_swingWindow.pack();
  }

  public int getFixedWidth() {
    return m_fixedWidth;
  }

  public void setFixedWidth(int fixedWidth) {
    m_fixedWidth = fixedWidth;
  }

  public boolean isPopupOnField() {
    return m_popupOnField;
  }

  public void setPopupOnField(boolean popupOnField) {
    m_popupOnField = popupOnField;
  }

  public boolean isPopupBelow() {
    return m_positionBelowReferenceField;
  }

  public void autoAdjustBounds() {
    try {
      if (m_ownerComponent.isShowing()) {
        if (m_ownerBounds.width == 0 && m_ownerBounds.height == 0) {
          m_ownerBounds.setSize(m_ownerComponent.getWidth(), m_ownerComponent.getHeight());
        }

        m_swingWindow.validate();
        m_swingWindow.getRootPane().revalidate();
        //
        Dimension dimPref = m_swingWindow.getPreferredSize();
        Dimension dimMin = m_swingWindow.getMinimumSize();

        if (getFixedWidth() > 0) {
          dimPref.width = getFixedWidth();
          dimMin.width = getFixedWidth();
        }
        else if (!m_swingWindow.isPreferredSizeSet()) {
          // ensure minimal with because calculated by layout manager
          dimPref.width = Math.max(m_env.getFormColumnWidth() / 2, dimPref.width);
          dimMin.width = Math.max(m_env.getFormColumnWidth() / 2, dimMin.width);
        }

        // ensure preferred width to be at least as mininum width
        dimPref.width = Math.max(dimPref.width, dimMin.width);
        // ensure preferred height to be at least as mininum height
        dimPref.height = Math.max(dimPref.height, dimMin.height);

        Point p = m_ownerBounds.getLocation();
        Point above = new Point(p.x, p.y);
        if (m_popupOnField) {
          above.y += m_ownerBounds.height;
        }
        else {
          above.y -= 2;
        }
        Rectangle aboveView = SwingUtility.intersectRectangleWithScreen(new Rectangle(above.x, above.y - dimPref.height, dimPref.width, dimPref.height), false, false);
        if (aboveView.width < dimMin.width) {
          aboveView.x = aboveView.x + aboveView.width - dimMin.width;
          aboveView.width = dimMin.width;
        }
        Point below = new Point(p.x, p.y);
        if (!m_popupOnField) {
          below.y += (m_ownerBounds.height + 2);
        }
        Rectangle belowView = SwingUtility.intersectRectangleWithScreen(new Rectangle(below.x, below.y, dimPref.width, dimPref.height), false, false);
        if (belowView.width < dimMin.width) {
          belowView.x = belowView.x + belowView.width - dimMin.width;
          belowView.width = dimMin.width;
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
      LOG.error("Unexpected", t);
    }
  }

  public Window getSwingWindow() {
    return m_swingWindow;
  }

  public boolean isResizable() {
    return m_resizable;
  }

  public void setResizable(boolean resizable) {
    if (m_resizable == resizable) {
      return;
    }
    m_resizable = resizable;

    if (resizable) {
      m_swingWindow.addMouseListener(m_resizeListener);
      m_swingWindow.addMouseMotionListener(m_resizeListener);
    }
    else {
      m_swingWindow.removeMouseListener(m_resizeListener);
      m_swingWindow.removeMouseMotionListener(m_resizeListener);
    }
  }

  public Component getSwingOwnerComponent() {
    return m_ownerComponent;
  }

  @Override
  public JComponent getSwingContentPane() {
    return (JComponent) m_swingWindow.getContentPane();
  }

  @Override
  public void addSwingScoutViewListener(SwingScoutViewListener listener) {
    m_listenerList.add(SwingScoutViewListener.class, listener);
  }

  @Override
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
    registerOwnerComponentListener();
    fireSwingScoutViewEvent(new SwingScoutViewEvent(SwingScoutPopup.this, SwingScoutViewEvent.TYPE_OPENED));
  }

  protected void handleSwingWindowClosed() {
    unregisterOwnerComponentListener();
    if (getSwingWindow().isFocused()) {
      Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
      if (focusOwner != null && focusOwner instanceof JComponent && ((JComponent) focusOwner).getInputVerifier() != null) {
        ((JComponent) focusOwner).getInputVerifier().verify((JComponent) focusOwner);
      }
    }
    fireSwingScoutViewEvent(new SwingScoutViewEvent(SwingScoutPopup.this, SwingScoutViewEvent.TYPE_CLOSED));
  }

  protected void registerOwnerComponentListener() {
    if (m_ownerComponentListener == null) {
      m_ownerComponentListener = new P_OwnerComponentListener();
      getSwingWindow().getOwner().addComponentListener(m_ownerComponentListener);
    }
  }

  protected void unregisterOwnerComponentListener() {
    if (m_ownerComponentListener != null) {
      getSwingWindow().getOwner().removeComponentListener(m_ownerComponentListener);
      m_ownerComponentListener = null;
    }
  }

  @Override
  public boolean isVisible() {
    return m_swingWindow != null && m_swingWindow.isVisible();
  }

  @Override
  public boolean isActive() {
    return m_swingWindow != null && m_swingWindow.isActive();
  }

  @Override
  public void openView() {
    m_opened = true;
    autoAdjustBounds();
    if (m_opened) {
      m_swingWindow.setVisible(true);
    }
  }

  @Override
  public void closeView() {
    if (m_opened) {
      m_opened = false;
      new DependentCloseListener(m_swingWindow).close();
    }
  }

  @Override
  public void setTitle(String s) {
    // nop
  }

  @Override
  public void setCloseEnabled(boolean b) {
  }

  @Override
  public void setMaximizeEnabled(boolean b) {
  }

  @Override
  public void setMinimizeEnabled(boolean b) {
  }

  @Override
  public void setMinimized(boolean on) {
  }

  @Override
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

  @Override
  public void setName(String name) {
    m_swingWindow.setName(name);
    m_swingWindow.getRootPane().setName(name);
  }

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
        if (!SwingUtility.IS_JAVA_7_OR_GREATER && SwingUtility.DO_RESET_COMPONENT_BOUNDS) {
          SwingUtility.setZeroBounds(parent.getComponents());
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

  private class P_ResizeListener extends MouseAdapter implements MouseMotionListener {

    private static final int NO_RESIZE = 0;
    private static final int RESIZE_EAST = 1 << 1;
    private static final int RESIZE_SOUTH = 1 << 2;
    private static final int RESIZE_NORTH = 1 << 3;

    private int m_direction = NO_RESIZE;

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
      m_direction = NO_RESIZE;
      m_swingWindow.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
      m_direction = getDirection(mouseEvent);
      m_swingWindow.setCursor(getCursor(m_direction));
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
      Rectangle originalBounds = m_swingWindow.getBounds();
      Rectangle newBounds = new Rectangle(originalBounds);
      Point mousePosition = mouseEvent.getPoint();
      SwingUtilities.convertPointToScreen(mousePosition, m_swingWindow);
      if ((m_direction & RESIZE_EAST) > 0) {
        int xEast = originalBounds.x + originalBounds.width;
        int delta = mousePosition.x - xEast;
        newBounds.width = Math.max(m_swingWindow.getMinimumSize().width, originalBounds.width + delta);
      }
      if ((m_direction & RESIZE_SOUTH) > 0) {
        int yBottom = originalBounds.y + originalBounds.height;
        int delta = mousePosition.y - yBottom;
        newBounds.height = Math.max(m_swingWindow.getMinimumSize().height, originalBounds.height + delta);
      }
      if ((m_direction & RESIZE_NORTH) > 0) {
        int yTop = originalBounds.y;
        int delta = yTop - mousePosition.y;
        newBounds.height = Math.max(m_swingWindow.getMinimumSize().height, originalBounds.height + delta);
        newBounds.y = yTop + originalBounds.height - newBounds.height;
      }
      m_swingWindow.setBounds(newBounds);
      m_swingWindow.validate();
      m_swingWindow.repaint();
    }

    private int getDirection(MouseEvent event) {
      Dimension dimension = m_swingWindow.getSize();
      Insets insets = m_swingWindow.getRootPane().getInsets();

      int eastBorder = dimension.width - insets.right;
      int bottomBorder = dimension.height - insets.bottom;
      int topBorder = insets.bottom;
      Point mousePosition = event.getPoint();

      int direction = NO_RESIZE;
      if (mousePosition.x >= eastBorder) {
        direction |= RESIZE_EAST;
      }
      if (isPopupBelow()) {
        if (mousePosition.y >= bottomBorder) {
          direction |= RESIZE_SOUTH;
        }
      }
      else {
        if (mousePosition.y <= topBorder) {
          direction |= RESIZE_NORTH;
        }
      }
      return direction;
    }

    private Cursor getCursor(int direction) {
      int cursorType = Cursor.DEFAULT_CURSOR;
      switch (direction) {
        case RESIZE_EAST:
          cursorType = Cursor.E_RESIZE_CURSOR;
          break;
        case RESIZE_SOUTH:
          cursorType = Cursor.S_RESIZE_CURSOR;
          break;
        case RESIZE_NORTH:
          cursorType = Cursor.N_RESIZE_CURSOR;
          break;
        case (RESIZE_SOUTH | RESIZE_EAST):
          cursorType = Cursor.SE_RESIZE_CURSOR;
          break;
        case (RESIZE_NORTH | RESIZE_EAST):
          cursorType = Cursor.NE_RESIZE_CURSOR;
          break;
        default:
          cursorType = Cursor.DEFAULT_CURSOR;
      }

      return Cursor.getPredefinedCursor(cursorType);
    }

  }// end private class

  private class P_OwnerComponentListener extends ComponentAdapter {
    @Override
    public void componentResized(ComponentEvent e) {
      handleSwingWindowClosed();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
      handleSwingWindowClosed();
    }
  }
}
