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
import javax.swing.UIDefaults;
import javax.swing.UIManager;

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
   * When busy blocking mode is on, clicks on a component are checked if the deepest component has
   * {@link JComponent#getClientProperty(Object)} set.
   * Also if ESCAPE is typed the current {@link IProgressMonitor} is cancelled.
   */
  public static final String BUSY_SUPPORTED_CLIENT_PROPERTY = "SwingBusyIndicator.busySupported";
  /**
   * {@link UIManager#getDefaults()} . {@link UIDefaults#addPropertyChangeListener(java.beans.PropertyChangeListener)}
   * can listen for this property of type
   * boolean to detect for busy/idle
   */
  public static final String BUSY_UI_PROPERTY = "SwingBusyIndicator.busyFlag";

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
   * Should NOT be called on the ui thread {@link SwingUtilities#isEventDispatchThread()}
   * <p>
   * Blocks and shows a wait cursor on all windows, until the runnable has finished.
   */
  public void showWhile(Runnable runnable) {
    if (runnable == null) {
      return;
    }
    if (SwingUtilities.isEventDispatchThread()) {
      throw new IllegalStateException("must not be called on ui thread");
    }
    try {
      int level = m_busyLevel.incrementAndGet();
      if (level == 1L) {
        fireBusyPropertyChange(true);
      }
      //
      /**
       * lazyHolder is filled lazy by {@link SwingUtilities#invokeLater(Runnable)}, do not use values outside of atw
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
      int level = m_busyLevel.decrementAndGet();
      if (level == 0L) {
        fireBusyPropertyChange(false);
      }
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

  protected void fireBusyPropertyChange(boolean value) {
    UIManager.put(BUSY_UI_PROPERTY, value);
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
        public void mouseClicked(MouseEvent e) {
          Rectangle r = m_messageRect;
          if (r != null && r.contains(e.getPoint())) {
            IProgressMonitor mon = m_monitor;
            if (mon != null && !mon.isCanceled()) {
              m_blockingMessage = null;
              m_messageRect = null;
              mon.setCanceled(true);
              repaint();
            }
          }
        }
      });
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
