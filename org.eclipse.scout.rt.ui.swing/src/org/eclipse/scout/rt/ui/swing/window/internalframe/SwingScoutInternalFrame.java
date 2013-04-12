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
package org.eclipse.scout.rt.ui.swing.window.internalframe;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyVetoException;
import java.util.EventListener;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.ext.BorderLayoutEx;
import org.eclipse.scout.rt.ui.swing.ext.JInternalFrameEx;
import org.eclipse.scout.rt.ui.swing.ext.JRootPaneEx;
import org.eclipse.scout.rt.ui.swing.focus.SwingScoutFocusTraversalPolicy;
import org.eclipse.scout.rt.ui.swing.window.ISwingScoutBoundsProvider;
import org.eclipse.scout.rt.ui.swing.window.ISwingScoutView;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewEvent;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewListener;

public class SwingScoutInternalFrame implements ISwingScoutView {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutInternalFrame.class);

  private ISwingEnvironment m_env;
  private boolean m_addedToDesktop;
  private EventListenerList m_listenerList;
  private JInternalFrameEx m_swingView;
  private Object m_viewConstraints;
  // cache
  private boolean m_maximized;

  private ISwingScoutBoundsProvider m_boundsProvider;

  public SwingScoutInternalFrame(ISwingEnvironment env, Object viewConstraints) {
    this(env, viewConstraints, null);
  }

  public SwingScoutInternalFrame(ISwingEnvironment env, Object viewConstraints, ISwingScoutBoundsProvider provider) {
    m_env = env;
    m_viewConstraints = viewConstraints;
    m_boundsProvider = provider;
    m_listenerList = new EventListenerList();
    //
    m_swingView = new JInternalFrameEx(" ", " ", false, false, false) {
      private static final long serialVersionUID = 1L;

      @Override
      protected JRootPane createRootPane() {
        return new JRootPaneEx() {
          private static final long serialVersionUID = 1L;

          @Override
          protected void reflow() {
            if (m_swingView.getParent() != null) {
              if (!m_swingView.isIcon()) {
                ((JComponent) m_swingView.getParent()).revalidate();
              }
            }
          }
        };
      }

      @Override
      public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        if (m_boundsProvider != null && m_addedToDesktop) {
          m_boundsProvider.storeBounds(getBounds());
        }
      }
    };
    m_swingView.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
    SwingUtility.installFocusCycleRoot(m_swingView, new SwingScoutFocusTraversalPolicy());
    JComponent contentPane = (JComponent) m_swingView.getContentPane();
    contentPane.setLayout(new BorderLayoutEx());
    contentPane.setCursor(Cursor.getDefaultCursor());
    m_swingView.addInternalFrameListener(new P_SwingWindowListener());
    m_swingView.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        try {
          if (!m_swingView.isSelected()) {
            m_swingView.setSelected(true);
          }
        }
        catch (PropertyVetoException e1) {
          //nop
        }
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (focusOwner == null || focusOwner instanceof JInternalFrame) {
              m_swingView.getContentPane().transferFocus();
            }
          }
        });
      }
    });
    m_swingView.pack();
  }

  public JInternalFrame getSwingInternalFrame() {
    return m_swingView;
  }

  @Override
  public JComponent getSwingContentPane() {
    return (JComponent) m_swingView.getContentPane();
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
    return m_addedToDesktop;
  }

  @Override
  public boolean isActive() {
    return m_swingView != null && m_swingView.isSelected();
  }

  public void setBoundsProvider(ISwingScoutBoundsProvider boundsProvider) {
    m_boundsProvider = boundsProvider;
  }

  @Override
  public void openView() {
    m_env.getRootComposite().getDesktopComposite().addView(m_swingView, m_viewConstraints);
    if (m_boundsProvider != null) {
      Rectangle c = m_boundsProvider.getBounds();
      if (c != null) {
        m_env.getRootComposite().getDesktopComposite().getSwingDesktopPane().getDesktopManager().resizeFrame(m_swingView, c.x, c.y, c.width, c.height);
      }
      m_boundsProvider.storeBounds(m_swingView.getBounds());
    }
    m_addedToDesktop = true;
  }

  @Override
  public void closeView() {
    if (m_boundsProvider != null) {
      m_boundsProvider.storeBounds(m_swingView.getBounds());
    }
    m_addedToDesktop = false;
    m_env.getRootComposite().getDesktopComposite().removeView(m_swingView);
  }

  @Override
  public void setTitle(String s) {
    if (s == null) {
      s = "";
    }
    m_swingView.setTitle(s);
  }

  @Override
  public void setCloseEnabled(boolean b) {
    m_swingView.setClosable(b);
  }

  @Override
  public void setMaximizeEnabled(boolean b) {
    m_swingView.setMaximizable(b);
  }

  @Override
  public void setMinimizeEnabled(boolean b) {
    m_swingView.setIconifiable(b);
  }

  @Override
  public void setMinimized(boolean on) {
    try {
      m_swingView.setIcon(on);
    }
    catch (PropertyVetoException e) {
      //nop
    }
  }

  @Override
  public void setMaximized(boolean on) {
    m_maximized = on;
    try {
      m_swingView.setMaximum(on);
    }
    catch (PropertyVetoException e) {
      //nop
    }
  }

  @Override
  public void setName(String name) {
    m_swingView.setName(name);
  }

  private class P_SwingWindowListener implements InternalFrameListener {
    @Override
    public void internalFrameOpened(InternalFrameEvent e) {
      fireSwingScoutViewEvent(new SwingScoutViewEvent(SwingScoutInternalFrame.this, SwingScoutViewEvent.TYPE_OPENED));
      //
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          if (m_swingView.isVisible() && !m_swingView.isIcon()) {
            Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (focusOwner == null || !SwingUtilities.isDescendingFrom(focusOwner, m_swingView.getDesktopPane().getRootPane())) {
              try {
                if (!m_swingView.isSelected()) {
                  m_swingView.setSelected(true);
                }
              }
              catch (PropertyVetoException ex) {
                //nop
              }
            }
          }
        }
      });
    }

    @Override
    public void internalFrameActivated(InternalFrameEvent e) {
      fireSwingScoutViewEvent(new SwingScoutViewEvent(SwingScoutInternalFrame.this, SwingScoutViewEvent.TYPE_ACTIVATED));
    }

    @Override
    public void internalFrameClosing(InternalFrameEvent e) {
      Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
      if (focusOwner != null && focusOwner instanceof JComponent && ((JComponent) focusOwner).getInputVerifier() != null) {
        boolean ok = ((JComponent) focusOwner).getInputVerifier().verify((JComponent) focusOwner);
        if (!ok) {
          return;
        }
      }
      fireSwingScoutViewEvent(new SwingScoutViewEvent(SwingScoutInternalFrame.this, SwingScoutViewEvent.TYPE_CLOSING));
    }

    @Override
    public void internalFrameClosed(InternalFrameEvent e) {
      Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
      if (SwingUtility.VERIFY_INPUT_ON_WINDOW_CLOSED && focusOwner != null && focusOwner instanceof JComponent && ((JComponent) focusOwner).getInputVerifier() != null) {
        ((JComponent) focusOwner).getInputVerifier().verify((JComponent) focusOwner);
      }
      fireSwingScoutViewEvent(new SwingScoutViewEvent(SwingScoutInternalFrame.this, SwingScoutViewEvent.TYPE_CLOSED));
    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent e) {
    }

    @Override
    public void internalFrameDeiconified(InternalFrameEvent e) {
    }

    @Override
    public void internalFrameIconified(InternalFrameEvent e) {
    }
  }// end private class

}
