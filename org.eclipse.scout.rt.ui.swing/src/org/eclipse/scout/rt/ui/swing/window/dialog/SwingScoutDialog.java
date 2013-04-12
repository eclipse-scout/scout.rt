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
package org.eclipse.scout.rt.ui.swing.window.dialog;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.EventListener;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.ext.BorderLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.ComponentSpyAction;
import org.eclipse.scout.rt.ui.swing.ext.JDialogEx;
import org.eclipse.scout.rt.ui.swing.ext.busy.SwingBusyIndicator;
import org.eclipse.scout.rt.ui.swing.focus.SwingScoutFocusTraversalPolicy;
import org.eclipse.scout.rt.ui.swing.window.DependentCloseListener;
import org.eclipse.scout.rt.ui.swing.window.ISwingScoutBoundsProvider;
import org.eclipse.scout.rt.ui.swing.window.ISwingScoutView;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewEvent;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewListener;

public class SwingScoutDialog implements ISwingScoutView {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutDialog.class);

  private ISwingEnvironment m_env;
  private EventListenerList m_listenerList;
  private Window m_swingParent;
  private JDialogEx m_swingDialog;
  // cache
  private boolean m_opened;
  private boolean m_maximized;
  private Rectangle m_boundsBeforeMaximize;

  private ISwingScoutBoundsProvider m_boundsProvider;

  public SwingScoutDialog(ISwingEnvironment env, Window swingParent) {
    this(env, swingParent, null);
  }

  public SwingScoutDialog(ISwingEnvironment env, Window swingParent, ISwingScoutBoundsProvider provider) {
    this(env, swingParent, provider, true);
  }

  public SwingScoutDialog(ISwingEnvironment env, Window swingParent, ISwingScoutBoundsProvider provider, boolean modal) {
    m_env = env;
    m_boundsProvider = provider;
    while (swingParent != null && !(swingParent instanceof Dialog || swingParent instanceof Frame)) {
      swingParent = SwingUtilities.getWindowAncestor(swingParent);
    }
    m_swingParent = swingParent;
    m_listenerList = new EventListenerList();
    //
    if (m_swingParent instanceof Dialog) {
      m_swingDialog = env.createJDialogEx((Dialog) m_swingParent);
    }
    else {
      m_swingDialog = env.createJDialogEx((Frame) m_swingParent);
    }
    m_swingDialog.getRootPane().putClientProperty(SwingBusyIndicator.BUSY_SUPPORTED_CLIENT_PROPERTY, true);
    JComponent contentPane = (JComponent) m_swingDialog.getContentPane();
    contentPane.setName("SwingScoutDialog.contentPane");
    contentPane.setLayout(new BorderLayoutEx());
    contentPane.setCursor(Cursor.getDefaultCursor());
    /**
     * WORKAROUND AWT doesn't show a dialog icon if dialog is not resizeable
     */
    m_swingDialog.setResizable(true);
    m_swingDialog.setModal(modal);
    m_swingDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    m_swingDialog.addWindowListener(new P_SwingWindowListener());
    // focus handling
    SwingUtility.installFocusCycleRoot(m_swingDialog, new SwingScoutFocusTraversalPolicy());
    m_swingDialog.addWindowFocusListener(new WindowFocusListener() {
      @Override
      public void windowGainedFocus(WindowEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == null) {
              m_swingDialog.getContentPane().transferFocus();
            }
          }
        });
      }

      @Override
      public void windowLostFocus(WindowEvent e) {
      }
    });
    // add development shortcuts
    SwingUtility.installDevelopmentShortcuts((JComponent) m_swingDialog.getContentPane());
    // register component spy
    if (contentPane instanceof JComponent) {
      (contentPane).getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtility.createKeystroke("shift alt F1"), "componentSpy");
      (contentPane).getActionMap().put("componentSpy", new ComponentSpyAction());
    }
  }

  @Override
  public JComponent getSwingContentPane() {
    return (JComponent) m_swingDialog.getContentPane();
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
  public void closeView() {
    m_opened = false;
    if (m_boundsProvider != null && m_swingDialog != null && m_swingDialog.isVisible()) {
      m_boundsProvider.storeBounds(m_swingDialog.getBounds());
    }
    new DependentCloseListener(m_swingDialog).close();
  }

  @Override
  public boolean isVisible() {
    return m_swingDialog != null && m_swingDialog.isVisible();
  }

  @Override
  public boolean isActive() {
    return m_swingDialog != null && m_swingDialog.isActive();
  }

  public void setBoundsProvider(ISwingScoutBoundsProvider boundsProvider) {
    m_boundsProvider = boundsProvider;
  }

  @Override
  public void openView() {
    m_opened = true;
    // preferred size
    m_swingDialog.pack();
    m_swingDialog.pack();// in case some wrapped fields were not able to
    // respond to first preferred size request.
    m_swingDialog.setLocationRelativeTo(m_swingDialog.getOwner());
    if (m_boundsProvider != null) {
      Rectangle c = m_boundsProvider.getBounds();
      if (c != null) {
        if (c.width == 0 || c.height == 0) {
          c.width = m_swingDialog.getWidth();
          c.height = m_swingDialog.getHeight();
        }
        m_swingDialog.setBounds(c);
      }
    }
    Rectangle a = m_swingDialog.getBounds();
    Rectangle b = SwingUtility.validateRectangleOnScreen(a, false, true);
    if (!b.equals(a)) {
      m_swingDialog.setLocation(b.getLocation());
      m_swingDialog.setSize(b.getSize());
    }
    if (m_maximized) {
      setMaximized(m_maximized);
    }
    if (m_opened) {
      m_swingDialog.setVisible(true);
    }
  }

  @Override
  public void setTitle(String s) {
    if (s == null) {
      s = "";
    }
    m_swingDialog.setTitle(s);
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
        m_boundsBeforeMaximize = m_swingDialog.getBounds();
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
      m_swingDialog.setBounds(r);
    }
    else {
      if (m_boundsBeforeMaximize != null) {
        m_swingDialog.setBounds(m_boundsBeforeMaximize);
        m_boundsBeforeMaximize = null;
      }
    }
  }

  @Override
  public void setName(String name) {
    m_swingDialog.getRootPane().setName(name);
  }

  private class P_SwingWindowListener extends WindowAdapter {
    @Override
    public void windowOpened(WindowEvent e) {
      fireSwingScoutViewEvent(new SwingScoutViewEvent(SwingScoutDialog.this, SwingScoutViewEvent.TYPE_OPENED));
    }

    @Override
    public void windowActivated(WindowEvent e) {
      fireSwingScoutViewEvent(new SwingScoutViewEvent(SwingScoutDialog.this, SwingScoutViewEvent.TYPE_ACTIVATED));
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
      fireSwingScoutViewEvent(new SwingScoutViewEvent(SwingScoutDialog.this, SwingScoutViewEvent.TYPE_CLOSING));
    }

    @Override
    public void windowClosed(WindowEvent e) {
      Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
      if (SwingUtility.VERIFY_INPUT_ON_WINDOW_CLOSED && focusOwner != null && focusOwner instanceof JComponent && ((JComponent) focusOwner).getInputVerifier() != null) {
        ((JComponent) focusOwner).getInputVerifier().verify((JComponent) focusOwner);
      }
      fireSwingScoutViewEvent(new SwingScoutViewEvent(SwingScoutDialog.this, SwingScoutViewEvent.TYPE_CLOSED));
    }
  }// end private class

}
