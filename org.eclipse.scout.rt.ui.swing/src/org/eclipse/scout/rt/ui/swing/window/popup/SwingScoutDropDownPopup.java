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

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewEvent;

/**
 * Special popup window that is attached to an input field.
 * <p>
 * The popup is not activated when opened and is closed when mouse clicks anyhwhere outside of window.
 */
public class SwingScoutDropDownPopup extends SwingScoutPopup {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutDropDownPopup.class);

  private ComponentListener m_ownerComponentListener;
  private Component m_focusComponent;
  private FocusListener m_focusFocusListener;
  private AWTEventListener m_awtListener;

  public SwingScoutDropDownPopup(ISwingEnvironment env, Component ownerComponent, Component focusComponent) {
    super(env, ownerComponent, new Rectangle(ownerComponent.getLocationOnScreen(), ownerComponent.getSize()));
    m_focusComponent = focusComponent;
  }

  public SwingScoutDropDownPopup(ISwingEnvironment env, Component ownerComponent, Component focusComponent, int fixedWidth) {
    super(env, ownerComponent, new Rectangle(ownerComponent.getLocationOnScreen(), ownerComponent.getSize()), fixedWidth);
    m_focusComponent = focusComponent;
  }

  public void makeNonFocusable() {
    getSwingWindow().setFocusableWindowState(false);
    getSwingWindow().setFocusable(false);
    makeNonFocusableRec(getSwingWindow());
  }

  private void makeNonFocusableRec(Container parent) {
    for (Component c : parent.getComponents()) {
      if (c instanceof JComponent) {
        ((JComponent) c).setRequestFocusEnabled(false);
        ((JComponent) c).setFocusable(false);
      }
      if (c instanceof Container) {
        makeNonFocusableRec((Container) c);
      }
    }
  }

  @Override
  protected void handleSwingWindowOpened() {
    // add listener to close popup
    if (m_awtListener == null) {
      m_awtListener = new AWTEventListener() {
        public void eventDispatched(AWTEvent event) {
          if (event.getID() == MouseEvent.MOUSE_PRESSED) {
            MouseEvent me = (MouseEvent) event;
            Window w;
            if (me.getComponent() instanceof Window) {
              w = (Window) me.getComponent();
            }
            else {
              w = SwingUtilities.getWindowAncestor(me.getComponent());
            }
            if (w != getSwingWindow()) {
              Point p = SwingUtilities.convertPoint(me.getComponent(), me.getPoint(), getSwingOwnerComponent());
              if (!getSwingOwnerComponent().contains(p)) {
                // close window later (let potential field verifier run first)
                SwingUtilities.invokeLater(new Runnable() {
                  public void run() {
                    if (getSwingWindow().isVisible()) {
                      closeView();
                      fireSwingScoutViewEvent(new SwingScoutViewEvent(SwingScoutDropDownPopup.this, SwingScoutViewEvent.TYPE_CLOSED));
                    }
                  }
                });
              }
            }
          }
        }
      };
      Toolkit.getDefaultToolkit().addAWTEventListener(m_awtListener, AWTEvent.MOUSE_EVENT_MASK);
    }
    // add listener to adjust location
    if (m_ownerComponentListener == null) {
      m_ownerComponentListener = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent event) {
          autoAdjustBounds();
        }

        @Override
        public void componentMoved(ComponentEvent event) {
          autoAdjustBounds();
        }
      };
      Window w = SwingUtilities.getWindowAncestor(getSwingOwnerComponent());
      if (w != null) {
        w.addComponentListener(m_ownerComponentListener);
      }
      getSwingOwnerComponent().addComponentListener(m_ownerComponentListener);
    }
    // add listener to track focus
    if (m_focusComponent != null) {
      if (m_focusFocusListener == null) {
        m_focusFocusListener = new FocusAdapter() {
          @Override
          public void focusLost(FocusEvent event) {
            closeView();
            fireSwingScoutViewEvent(new SwingScoutViewEvent(SwingScoutDropDownPopup.this, SwingScoutViewEvent.TYPE_CLOSED));
          }
        };
        m_focusComponent.addFocusListener(m_focusFocusListener);
      }
    }
    //
    super.handleSwingWindowOpened();
    if (m_focusComponent != null && !m_focusComponent.hasFocus()) {
      closeView();
      fireSwingScoutViewEvent(new SwingScoutViewEvent(SwingScoutDropDownPopup.this, SwingScoutViewEvent.TYPE_CLOSED));
    }
  }

  @Override
  protected void handleSwingWindowClosed() {
    if (m_awtListener != null) {
      Toolkit.getDefaultToolkit().removeAWTEventListener(m_awtListener);
      m_awtListener = null;
    }
    //
    if (m_ownerComponentListener != null) {
      Window w = SwingUtilities.getWindowAncestor(getSwingOwnerComponent());
      if (w != null) {
        w.removeComponentListener(m_ownerComponentListener);
      }
      getSwingOwnerComponent().removeComponentListener(m_ownerComponentListener);
      m_ownerComponentListener = null;
    }
    //
    if (m_focusComponent != null) {
      if (m_focusFocusListener != null) {
        m_focusComponent.removeFocusListener(m_focusFocusListener);
        m_focusFocusListener = null;
      }
    }
    //
    super.handleSwingWindowClosed();
  }
}
