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
package org.eclipse.scout.rt.ui.swing.window.frame;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.EventListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.ext.BorderLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.ComponentSpyAction;
import org.eclipse.scout.rt.ui.swing.ext.JFrameEx;
import org.eclipse.scout.rt.ui.swing.ext.busy.SwingBusyIndicator;
import org.eclipse.scout.rt.ui.swing.focus.SwingScoutFocusTraversalPolicy;
import org.eclipse.scout.rt.ui.swing.window.DependentCloseListener;
import org.eclipse.scout.rt.ui.swing.window.ISwingScoutBoundsProvider;
import org.eclipse.scout.rt.ui.swing.window.ISwingScoutView;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewEvent;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewListener;

public class SwingScoutFrame implements ISwingScoutView {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutFrame.class);

  private ISwingEnvironment m_env;
  private EventListenerList m_listenerList;
  private JFrameEx m_swingFrame;
  // cache
  private boolean m_maximized;
  private boolean m_opened;

  private ISwingScoutBoundsProvider m_boundsProvider;

  public SwingScoutFrame(ISwingEnvironment env) {
    this(env, null);
  }

  public SwingScoutFrame(ISwingEnvironment env, ISwingScoutBoundsProvider boundsProvider) {
    m_env = env;
    m_boundsProvider = boundsProvider;
    m_listenerList = new EventListenerList();
    //
    m_swingFrame = new JFrameEx();
    m_swingFrame.getRootPane().putClientProperty(SwingBusyIndicator.BUSY_SUPPORTED_CLIENT_PROPERTY, true);
    JComponent contentPane = (JComponent) m_swingFrame.getContentPane();
    contentPane.setLayout(new BorderLayoutEx());
    contentPane.setCursor(Cursor.getDefaultCursor());
    /**
     * WORKAROUND AWT doesn't show a dialog icon if dialog is not resizeable
     */
    m_swingFrame.setResizable(true);
    m_swingFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    m_swingFrame.addWindowListener(new P_SwingWindowListener());
    // focus handling
    SwingUtility.installFocusCycleRoot(m_swingFrame, new SwingScoutFocusTraversalPolicy());
    m_swingFrame.addWindowFocusListener(new WindowFocusListener() {
      @Override
      public void windowGainedFocus(WindowEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == null) {
              m_swingFrame.getContentPane().transferFocus();
            }
          }
        });
      }

      @Override
      public void windowLostFocus(WindowEvent e) {
      }
    });
    // add development shortcuts
    SwingUtility.installDevelopmentShortcuts((JComponent) m_swingFrame.getContentPane());
    // init layout
    m_swingFrame.pack();
    // register component spy
    if (contentPane instanceof JComponent) {
      (contentPane).getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtility.createKeystroke("shift alt F1"), "componentSpy");
      (contentPane).getActionMap().put("componentSpy", new ComponentSpyAction());
    }
  }

  @Override
  public JComponent getSwingContentPane() {
    return (JComponent) m_swingFrame.getContentPane();
  }

  @Override
  public void addSwingScoutViewListener(SwingScoutViewListener listener) {
    m_listenerList.add(SwingScoutViewListener.class, listener);
  }

  @Override
  public void removeSwingScoutViewListener(SwingScoutViewListener listener) {
    m_listenerList.remove(SwingScoutViewListener.class, listener);
  }

  private void fireSwingScoutViewEvent(SwingScoutViewEvent e) {
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

  @Override
  public boolean isVisible() {
    return m_swingFrame != null && m_swingFrame.isVisible();
  }

  @Override
  public boolean isActive() {
    return m_swingFrame != null && m_swingFrame.isActive();
  }

  public void setBoundsProvider(ISwingScoutBoundsProvider boundsProvider) {
    m_boundsProvider = boundsProvider;
  }

  @Override
  public void openView() {
    m_opened = true;
    m_swingFrame.pack();
    m_swingFrame.pack();// in case some wrapped fields were not able to respond
    // to first preferred size request.
    m_swingFrame.setLocationRelativeTo(m_env.getRootFrame());
    if (m_boundsProvider != null) {
      Rectangle c = m_boundsProvider.getBounds();
      if (c != null) {
        if (c.width == 0 || c.height == 0) {
          c.width = m_swingFrame.getWidth();
          c.height = m_swingFrame.getHeight();
        }
        m_swingFrame.setBounds(c);
      }
    }
    Rectangle a = m_swingFrame.getBounds();
    Rectangle b = SwingUtility.validateRectangleOnScreen(a, false, true);
    if (!b.equals(a)) {
      m_swingFrame.setLocation(b.getLocation());
      m_swingFrame.setSize(b.getSize());
    }
    if (m_maximized) {
      setMaximized(m_maximized);
    }
    if (m_opened) {
      m_swingFrame.setVisible(true);
    }
  }

  @Override
  public void closeView() {
    m_opened = false;
    if (m_boundsProvider != null) {
      m_boundsProvider.storeBounds(m_swingFrame.getBounds());
    }
    new DependentCloseListener(m_swingFrame).close();
  }

  @Override
  public void setTitle(String s) {
    if (s == null) {
      s = "";
    }
    m_swingFrame.setTitle(s);
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
    if (on) {
      int state = (m_swingFrame.getExtendedState() & (0xffffffff - JFrame.MAXIMIZED_BOTH)) | JFrame.ICONIFIED;
      m_swingFrame.setExtendedState(state);
    }
    else {
      m_swingFrame.setExtendedState(m_swingFrame.getExtendedState() & (0xffffffff - JFrame.ICONIFIED));
    }
  }

  @Override
  public void setMaximized(boolean on) {
    m_maximized = on;
    if (on) {
      int state = (m_swingFrame.getExtendedState() & (0xffffffff - JFrame.ICONIFIED)) | JFrame.MAXIMIZED_BOTH;
      m_swingFrame.setExtendedState(state);
    }
    else {
      m_swingFrame.setExtendedState(m_swingFrame.getExtendedState() & (0xffffffff - JFrame.MAXIMIZED_BOTH));
    }
  }

  @Override
  public void setName(String name) {
    m_swingFrame.getRootPane().setName(name);
  }

  private class P_SwingWindowListener extends WindowAdapter {
    @Override
    public void windowOpened(WindowEvent e) {
      fireSwingScoutViewEvent(new SwingScoutViewEvent(SwingScoutFrame.this, SwingScoutViewEvent.TYPE_OPENED));
    }

    @Override
    public void windowActivated(WindowEvent e) {
      fireSwingScoutViewEvent(new SwingScoutViewEvent(SwingScoutFrame.this, SwingScoutViewEvent.TYPE_ACTIVATED));
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
      fireSwingScoutViewEvent(new SwingScoutViewEvent(SwingScoutFrame.this, SwingScoutViewEvent.TYPE_CLOSING));
    }

    @Override
    public void windowClosed(WindowEvent e) {
      Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
      if (SwingUtility.VERIFY_INPUT_ON_WINDOW_CLOSED && focusOwner != null && focusOwner instanceof JComponent && ((JComponent) focusOwner).getInputVerifier() != null) {
        ((JComponent) focusOwner).getInputVerifier().verify((JComponent) focusOwner);
      }
      fireSwingScoutViewEvent(new SwingScoutViewEvent(SwingScoutFrame.this, SwingScoutViewEvent.TYPE_CLOSED));
    }
  }// end private class

}
