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
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

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
 * <li>enable windows 7 keystrokes: WINDOWS-up/down/left/right keys for
 * maximize/minimize/left-align-screen-switch/right-align-screen-switch/</li>
 * </ul>
 */
public class JFrameEx extends JFrame {
  private static final long serialVersionUID = 1L;

  private boolean m_autoCorrectSize;
  private Windows7KeyHandler m_win7KeyHandler;
  private Rectangle m_nonMaximizedBounds;

  public JFrameEx() {
    super();
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    enableEvents(AWTEvent.COMPONENT_EVENT_MASK);
    m_autoCorrectSize = true;
  }

  @Override
  protected void setRootPane(JRootPane root) {
    if (m_win7KeyHandler == null) {
      m_win7KeyHandler = new Windows7KeyHandler();
    }
    m_win7KeyHandler.uninstall();
    super.setRootPane(root);
    m_win7KeyHandler.install();
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
   * WORKAROUND send property events; swing is not taking in account native
   * taskbars, etc.
   */
  @Override
  public synchronized void setExtendedState(int state) {
    Rectangle r = getBounds();
    if ((state & Frame.MAXIMIZED_BOTH) != 0) {
      m_nonMaximizedBounds = r;
    }
    Rectangle screen = SwingUtility.getFullScreenBoundsFor(r, false);
    screen.x = Integer.MAX_VALUE;
    screen.y = Integer.MAX_VALUE;
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

}
