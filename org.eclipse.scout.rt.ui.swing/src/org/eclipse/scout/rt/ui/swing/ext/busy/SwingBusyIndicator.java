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
package org.eclipse.scout.rt.ui.swing.ext.busy;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swing.SwingUtility;

/**
 * Show a busy indicator while running an (async) job
 * <p>
 * This affects only {@link RootPaneContainer} (windows) that have the {@link JRootPane}with the
 * {@link JRootPane#putClientProperty(Object, Object)} of {@value #BUSY_SUPPORTED_CLIENT_PROPERTY} set to true
 */
public class SwingBusyIndicator {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingBusyIndicator.class);
  private static final AtomicLong BUSY_SEQ = new AtomicLong();
  private static final String BUSY_SET_CLIENT_PROPERTY = "SwingBusyIndicator.busySet";
  private static SwingBusyIndicator instance = new SwingBusyIndicator();

  /**
   * {@link JComponent#getClientProperty(Object)} marker for {@link RootPaneContainer}s that support busy with glasspane
   * overlays.
   */
  public static final String BUSY_SUPPORTED_CLIENT_PROPERTY = "SwingBusyIndicator.busySupported";

  /**
   * When busy is active, this property is used to find out if a component is marked as
   * busy-clickable and therefore event retarget to it is allowed.
   * <p>
   * Type {@link Boolean}
   */
  public static final String BUSY_CLICKABLE_CLIENT_PROPERTY = "SwingBusyIndicator.busyClickable";

  public static SwingBusyIndicator getInstance() {
    return instance;
  }

  public static synchronized void setInstance(SwingBusyIndicator newIndicator) {
    if (instance != null) {
      instance.dispose();
    }
    instance = newIndicator;
  }

  private final AtomicInteger m_busyLevel = new AtomicInteger();

  /**
   * Shows a wait cursor on all windows, until the runnable has finished.
   * <p>
   * Should NOT be called on the ui thread {@link SwingUtilities#isEventDispatchThread()}
   */
  public void showWhile(Runnable runnable) {
    if (runnable == null) {
      return;
    }
    if (SwingUtilities.isEventDispatchThread()) {
      throw new IllegalStateException("must not be called on ui thread");
    }
    try {
      m_busyLevel.incrementAndGet();
      //
      /**
       * lazyHolder is filled lazy by {@link SwingUtilities#invokeLater(Runnable)}, do not use values outside of the
       * event queue thread!
       */
      ArrayList<RootPaneContainer> lazyHolder = new ArrayList<RootPaneContainer>();
      try {
        setBusy(lazyHolder);
        runnable.run();
      }
      finally {
        clearBusy(lazyHolder);
      }
    }
    finally {
      m_busyLevel.decrementAndGet();
    }
  }

  public boolean isBusy() {
    return m_busyLevel.get() > 0;
  }

  /**
   * Block all input from busy root pane containers. Only access to {@value #BLOCKING_CANCEL_COMPONENT_CLIENT_PROPERTY}
   * is accepted.
   * <p>
   * This method is normally called while a {@link #showWhile(Runnable)} is running.
   */
  public void startBlocking(IProgressMonitor monitor) {
    setBlocking(monitor);
  }

  protected void dispose() {
  }

  /**
   * @param busyId
   * @param lazyHolder
   *          is filled lazy by {@link SwingUtilities#invokeLater(Runnable)}, do not use values outside of atw event
   *          queue thread!
   */
  private void setBusy(final Collection<RootPaneContainer> lazyHolder) {
    SwingUtilities.invokeLater(
          new Runnable() {
            @Override
            public void run() {
              //windows
              Window[] windows = Window.getWindows();
              if (windows != null) {
                for (Window w : windows) {
                  RootPaneContainer r = accept(w);
                  if (r != null) {
                    if (setBusy0(r)) {
                      lazyHolder.add(r);
                    }
                  }
                }
              }
            }
          });
  }

  private void setBlocking(final IProgressMonitor monitor) {
    SwingUtilities.invokeLater(
          new Runnable() {
            @Override
            public void run() {
              //windows
              Window[] windows = Window.getWindows();
              if (windows != null) {
                for (Window w : windows) {
                  RootPaneContainer r = accept(w);
                  if (r != null) {
                    setBlocking0(r, monitor);
                  }
                }
              }
            }
          });
  }

  /**
   * @param lazyHolder
   *          is filled lazy by {@link SwingUtilities#invokeLater(Runnable)}, do not use values outside of atw event
   *          queue thread!
   */
  private void clearBusy(final Collection<RootPaneContainer> lazyHolder) {
    SwingUtilities.invokeLater(
          new Runnable() {
            @Override
            public void run() {
              if (lazyHolder.size() == 0) {
                return;
              }
              for (RootPaneContainer r : lazyHolder) {
                clearBusy0(r);
              }
            }
          });
  }

  /**
   * @return true if component is not null, {@link Component#isVisible()} and a {@link RootPaneContainer} and a
   *         container for a scout widget
   */
  private RootPaneContainer accept(Component c) {
    if (c == null) {
      return null;
    }
    if (!c.isVisible()) {
      return null;
    }
    if (!(c instanceof RootPaneContainer)) {
      return null;
    }
    JRootPane rootPane = ((RootPaneContainer) c).getRootPane();
    if (rootPane == null) {
      return null;
    }
    if (!Boolean.TRUE.equals(rootPane.getClientProperty(BUSY_SUPPORTED_CLIENT_PROPERTY))) {
      return null;
    }
    return (RootPaneContainer) c;
  }

  private boolean setBusy0(RootPaneContainer r) {
    JRootPane rootPane = r.getRootPane();
    if (rootPane == null) {
      return false;
    }
    if (rootPane.getClientProperty(BUSY_SET_CLIENT_PROPERTY) != null) {
      return false;
    }
    rootPane.putClientProperty(BUSY_SET_CLIENT_PROPERTY, true);
    BusyGlassPane glass = new BusyGlassPane();
    rootPane.setGlassPane(glass);
    glass.setVisible(true);
    return true;
  }

  private void setBlocking0(RootPaneContainer r, IProgressMonitor monitor) {
    JRootPane rootPane = r.getRootPane();
    if (rootPane == null) {
      return;
    }
    if (rootPane.getGlassPane() instanceof BusyGlassPane) {
      BusyGlassPane glass = (BusyGlassPane) rootPane.getGlassPane();
      glass.block(monitor);
    }
  }

  private void clearBusy0(RootPaneContainer r) {
    JRootPane rootPane = r.getRootPane();
    if (rootPane == null) {
      return;
    }
    rootPane.putClientProperty(BUSY_SET_CLIENT_PROPERTY, null);
    rootPane.setGlassPane(new IdleGlassPane());
    rootPane.getGlassPane().setVisible(false);
  }

  private static class BusyGlassPane extends JPanel {
    private static final long serialVersionUID = 1L;

    private IProgressMonitor m_monitor;
    private String m_blockingMessage;
    private Rectangle m_messageRect;

    public BusyGlassPane() {
      setVisible(false);
      setOpaque(false);
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          IProgressMonitor mon = m_monitor;
          if (mon == null) {
            JComponent busyClickable = getBusyClickable(e);
            if (busyClickable != null) {
              retargetEvent(e, busyClickable);
            }
          }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
          IProgressMonitor mon = m_monitor;
          if (mon == null) {
            JComponent busyClickable = getBusyClickable(e);
            if (busyClickable != null) {
              retargetEvent(e, busyClickable);
            }
          }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
          IProgressMonitor mon = m_monitor;
          JComponent busyClickable = getBusyClickable(e);
          if (mon == null) {
            if (busyClickable != null) {
              retargetEvent(e, busyClickable);
            }
          }
          else {
            Rectangle r = m_messageRect;
            if (!mon.isCanceled() && (r != null && r.contains(e.getPoint())) || busyClickable != null) {
              //blocking state
              m_blockingMessage = null;
              mon.setCanceled(true);
              repaint();
            }
          }
        }
      });
    }

    private JComponent getBusyClickable(MouseEvent e) {
      if (e.getComponent() == null) {
        return null;
      }
      Window w = SwingUtilities.getWindowAncestor(e.getComponent());
      if (!(w instanceof RootPaneContainer)) {
        return null;
      }
      Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), ((RootPaneContainer) w).getContentPane());
      Component c = SwingUtilities.getDeepestComponentAt(((RootPaneContainer) w).getContentPane(), p.x, p.y);
      if (!(c instanceof JComponent)) {
        return null;
      }
      Boolean b = (Boolean) ((JComponent) c).getClientProperty(BUSY_CLICKABLE_CLIENT_PROPERTY);
      if (b != null && b.booleanValue()) {
        return (JComponent) c;
      }
      return null;
    }

    private void retargetEvent(MouseEvent e, JComponent c) {
      MouseEvent retargetEvent = SwingUtilities.convertMouseEvent(e.getComponent(), e, c);
      c.dispatchEvent(retargetEvent);
    }

    public void block(IProgressMonitor monitor) {
      m_monitor = monitor;
      m_blockingMessage = SwingUtility.getNlsText("BusyBlockingMessage");
      repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (m_monitor != null) {
        String s = m_blockingMessage;
        Window w = SwingUtilities.getWindowAncestor(this);
        if (s != null && w != null && w.isActive()) {
          int textWidth = g.getFontMetrics().stringWidth(s);
          int textHeight = g.getFontMetrics().getAscent();
          int pad = 8;
          Rectangle r = new Rectangle(getWidth() / 2 - textWidth / 2 - pad, getHeight() * 4 / 5 - (textHeight + pad + pad), textWidth + pad + pad, textHeight + pad + pad);
          m_messageRect = r;
          g.setColor(new Color(0x66000000, true));
          g.fillRoundRect(r.x, r.y, r.width, r.height, pad + pad, pad + pad);
          g.setColor(Color.white);
          g.drawString(s, r.x + pad, r.y + pad + textHeight - 2);
        }
      }
    }

  }

  private static class IdleGlassPane extends JPanel {
    private static final long serialVersionUID = 1L;

    public IdleGlassPane() {
      setName("null.glassPane");
      setVisible(false);
      setOpaque(false);
    }
  }

}
