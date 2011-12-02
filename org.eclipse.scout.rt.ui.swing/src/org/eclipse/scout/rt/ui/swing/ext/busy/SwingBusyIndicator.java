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
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.AbstractAction;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
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
  private static final String BUSY_ID_CLIENT_PROPERTY = "SwingBusyIndicator.busyId";
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

  public static void setInstance(SwingBusyIndicator newIndicator) {
    instance = newIndicator;
  }

  private final AtomicLong m_busyCounter = new AtomicLong();

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
    long busyId = 0L;
    try {
      busyId = m_busyCounter.incrementAndGet();
      if (busyId == 1L) {
        fireBusyPropertyChange(true);
      }
      //
      ArrayList<RootPaneContainer> changedContainers = new ArrayList<RootPaneContainer>();
      try {
        setBusy(busyId, changedContainers);
        runnable.run();
      }
      finally {
        clearBusy(changedContainers);
      }
    }
    finally {
      m_busyCounter.decrementAndGet();
      if (busyId == 1L) {
        fireBusyPropertyChange(false);
      }
    }
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

  private void fireBusyPropertyChange(boolean value) {
    UIManager.put(BUSY_UI_PROPERTY, value);
  }

  private void setBusy(final long busyId, final Collection<RootPaneContainer> out) {
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
                    if (setBusy0(busyId, r)) {
                      out.add(r);
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

  private void clearBusy(final Collection<RootPaneContainer> in) {
    if (in.size() == 0) {
      return;
    }
    SwingUtilities.invokeLater(
          new Runnable() {
            @Override
            public void run() {
              for (RootPaneContainer r : in) {
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

  private boolean setBusy0(long busyId, RootPaneContainer r) {
    JRootPane rootPane = r.getRootPane();
    if (rootPane == null) {
      return false;
    }
    if (rootPane.getClientProperty(BUSY_ID_CLIENT_PROPERTY) != null) {
      return false;
    }
    rootPane.putClientProperty(BUSY_ID_CLIENT_PROPERTY, busyId);
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
    rootPane.putClientProperty(BUSY_ID_CLIENT_PROPERTY, null);
    rootPane.setGlassPane(new IdleGlassPane());
    rootPane.getGlassPane().setVisible(false);
  }

  private static class BusyGlassPane extends JPanel {
    private static final long serialVersionUID = 1L;

    private IProgressMonitor m_monitor;
    private String m_blockingMessage;

    public BusyGlassPane() {
      setVisible(false);
      setOpaque(false);
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          if (m_monitor == null) {
            return;
          }
        }
      });
    }

    public void block(IProgressMonitor monitor) {
      m_monitor = monitor;
      m_blockingMessage = SwingUtility.getNlsText("BusyBlockingMessage");
      setInputVerifier(new InputVerifier() {
        @Override
        public boolean verify(JComponent input) {
          return false;
        }
      });
      getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ESCAPE"), "esc");
      getActionMap().put("esc", new AbstractAction() {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          m_monitor.setCanceled(true);
          m_blockingMessage = null;
          repaint();
        }
      });
      requestFocus();
      repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (m_monitor != null) {
        g.setColor(new Color(0x44000000, true));
        g.fillRect(0, 0, getWidth(), getHeight());
        String s = m_blockingMessage;
        Window w = SwingUtilities.getWindowAncestor(this);
        if (s != null && w != null && w.isActive()) {
          int textWidth = g.getFontMetrics().stringWidth(s);
          int textHeight = g.getFontMetrics().getAscent();
          int pad = 8;
          Rectangle messageRect = new Rectangle(getWidth() / 2 - textWidth / 2 - pad, getHeight() * 2 / 3 - textHeight / 2 - pad, textWidth + pad + pad, textHeight + pad + pad);
          g.setColor(new Color(0x66000000, true));
          g.fillRoundRect(messageRect.x, messageRect.y, messageRect.width, messageRect.height, pad + pad, pad + pad);
          g.setColor(Color.white);
          g.drawString(s, messageRect.x + pad, messageRect.y + pad + textHeight - 2);
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
