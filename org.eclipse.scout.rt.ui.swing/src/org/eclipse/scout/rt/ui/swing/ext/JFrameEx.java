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

import java.awt.AWTEvent;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.JRootPane;

import org.eclipse.scout.rt.ui.swing.SwingUtility;

/**
 * bug fixes:
 * <ul>
 * <li>respects native screen insets (taskbar)</li>
 * <li>iconify</li>
 * <li>using {@link JRootPaneEx} with min/max size validation</li>
 * <li>fire property "state" whenever extendedState is changed</li>
 * </ul>
 */
public class JFrameEx extends JFrame {
  private static final long serialVersionUID = 1L;

  private boolean m_autoCorrectSize;
  private Rectangle m_nonMaximizedBounds;
  private P_ComponentListener m_componentListener;

  public JFrameEx() {
    super();
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    enableEvents(AWTEvent.COMPONENT_EVENT_MASK);
    initComponentListener();
    m_autoCorrectSize = true;
  }

  private void initComponentListener() {
    if (!SwingUtility.hasScoutLookAndFeelFrameAndDialog()) {
      m_componentListener = new P_ComponentListener();
      addComponentListener(m_componentListener);
    }
  }

  public boolean isAutoCorrectSize() {
    return m_autoCorrectSize;
  }

  public void setAutoCorrectSize(boolean b) {
    m_autoCorrectSize = b;
  }

  public Rectangle getNonMaximizedBounds() {
    return m_nonMaximizedBounds;
  }

  /**
   * WORKAROUND send property events
   */
  @Override
  public synchronized void setExtendedState(int state) {
    if ((state & Frame.MAXIMIZED_BOTH) != 0) {
      m_nonMaximizedBounds = getBounds();
    }
    int oldState = getExtendedState();
    if (SwingUtility.hasScoutLookAndFeelFrameAndDialog()) {
      setExtendedStateForLookAndFeel(state);
    }
    else {
      super.setExtendedState(state);
    }
    int newState = getExtendedState();
    firePropertyChange("state", oldState, newState);
  }

  /**
   * WORKAROUND for correctly bringing a JFrame to front
   */
  @Override
  public void toFront() {
    setState(JFrame.ICONIFIED);
    setState(JFrame.NORMAL);
    super.toFront();
  }

  @Override
  protected JRootPane createRootPane() {
    return new JRootPaneEx() {
      private static final long serialVersionUID = 1L;

      @Override
      protected void reflow() {
        if (m_autoCorrectSize) {
          JFrameEx.this.pack();
        }
      }
    };
  }

  @Override
  public void dispose() {
    if (m_componentListener != null) {
      removeComponentListener(m_componentListener);
    }
    super.dispose();
  }

  private void setExtendedStateForLookAndFeel(int state) {
    if (!SwingUtility.hasScoutLookAndFeelFrameAndDialog()) {
      return;
    }

    Rectangle r = getBounds();
    Rectangle screen = SwingUtility.getFullScreenBoundsFor(r, false);
    // set correct x/y coordinate which should be relative to a single screen.
    // therefore the native windowing system insets on the frames current screen
    // should be evaluated
    GraphicsDevice screenDevice = SwingUtility.getCurrentScreen(r);
    Insets screenInsets = SwingUtility.getScreenInsets(screenDevice);
    screen.x = screenInsets.left;
    screen.y = screenInsets.top;
    setMaximizedBounds(screen);
    int oldState = getExtendedState();
    super.setExtendedState(state);

    // <bsh 2010-10-15>
    // Fix for Sun bug 6699851 ("setMaximizedbounds not working properly on dual screen environment")
    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6699851
    if ((state & Frame.MAXIMIZED_BOTH) != 0) {
      Rectangle fullscreen = SwingUtility.getFullScreenBoundsFor(getBounds(), true);
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      if (ge.getScreenDevices().length < 2 || fullscreen.x == 0) {
        // If there is only one screen or if we are on the primary screen, do _not_ apply
        // the workarround!
        //
        // (For some reason, getBounds() does not return the correct value during
        // startup. This would unintentionally cause the workarround to be applied even on
        // the primary screen. The taskbar would be hidden by the full screen window,
        // and that is not correct.)
      }
      else if (getBounds() != null && (getBounds().width != screen.width || getBounds().height != screen.height)) {
        // If the new state is "maximized" and the current bounds do not match with
        // the anticipated size, we know have bumped into the bug. To fix it, set
        // the maximized size to the size of the primary screen, then restore the
        // previous state and maximize the window again. This does not look very
        // nice, but it seems to work...
        Rectangle screen0 = ge.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        screen.width = screen0.width;
        screen.height = screen0.height;
        setMaximizedBounds(screen);
        super.setExtendedState(oldState);
        super.setExtendedState(state);
      }
    }
    // </bsh>
  }

  private class P_ComponentListener extends ComponentAdapter {
    @Override
    public void componentResized(ComponentEvent e) {
      if (getExtendedState() == Frame.NORMAL) {
        m_nonMaximizedBounds = getBounds();
      }
    }
  }
}
