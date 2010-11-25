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
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;

import org.eclipse.scout.rt.ui.swing.SwingUtility;

public class Windows7KeyHandler {
  private static final String CLIENT_PROPERTY_DEFAULT_BOUNDS = "Windows7DefaultBounds";

  private AWTEventListener m_listener;
  private boolean m_windowsKeyDown;

  public Windows7KeyHandler() {
  }

  public void install() {
    String osName = System.getProperty("os.name");
    if (osName == null || !osName.startsWith("Windows")) return;
    //
    if (m_listener == null) {
      m_listener = new AWTEventListener() {
        @Override
        public void eventDispatched(AWTEvent a) {
          switch (a.getID()) {
            case KeyEvent.KEY_PRESSED: {
              switch (((KeyEvent) a).getKeyCode()) {
                case KeyEvent.VK_WINDOWS: {
                  handleEnabled();
                  break;
                }
              }
              break;
            }
            case KeyEvent.KEY_RELEASED: {
              switch (((KeyEvent) a).getKeyCode()) {
                case KeyEvent.VK_WINDOWS: {
                  handleDisabled();
                  break;
                }
                case KeyEvent.VK_UP: {
                  handleUp();
                  break;
                }
                case KeyEvent.VK_DOWN: {
                  handleDown();
                  break;
                }
                case KeyEvent.VK_LEFT: {
                  handleLeft();
                  break;
                }
                case KeyEvent.VK_RIGHT: {
                  handleRight();
                  break;
                }
              }
              break;
            }
            case WindowEvent.WINDOW_ACTIVATED: {
              handleDisabled();
              break;
            }
          }
        }
      };
    }
    Toolkit.getDefaultToolkit().addAWTEventListener(m_listener, AWTEvent.WINDOW_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
  }

  public void uninstall() {
    if (m_listener != null) {
      Toolkit.getDefaultToolkit().removeAWTEventListener(m_listener);
      m_listener = null;
    }
  }

  private void handleEnabled() {
    m_windowsKeyDown = true;
  }

  private void handleDisabled() {
    m_windowsKeyDown = false;
  }

  private void handleUp() {
    if (m_windowsKeyDown) {
      Frame frame = getActiveFrame();
      if (frame != null) {
        frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
      }
    }
  }

  private void handleDown() {
    if (m_windowsKeyDown) {
      Frame frame = getActiveFrame();
      if (frame != null) {
        frame.setExtendedState(frame.getExtendedState() & ~Frame.MAXIMIZED_BOTH);
      }
    }
  }

  private void handleLeft() {
    if (m_windowsKeyDown) {
      Frame frame = getActiveFrame();
      if (frame != null) {
        boolean wasMaximized = (frame.getExtendedState() & Frame.MAXIMIZED_BOTH) != 0;
        frame.setExtendedState(frame.getExtendedState() & ~Frame.MAXIMIZED_BOTH);
        //
        Rectangle curView = frame.getBounds();
        Rectangle defaultView = getDefaultBounds(frame);
        Rectangle r = SwingUtility.getFullScreenBoundsFor(curView, false);
        Rectangle leftView = r.getBounds();
        Rectangle rightView = r.getBounds();
        int center = leftView.x + leftView.width / 2;
        leftView.width = center - leftView.x;
        rightView.width = rightView.x + rightView.width - center;
        rightView.x = center;
        if (!wasMaximized && leftView.equals(curView)) {
          //move on opposite screen to right
          r = SwingUtility.getOppositeFullScreenBoundsFor(frame.getBounds(), false);
          if (r != null) {
            leftView = r.getBounds();
            rightView = r.getBounds();
            center = leftView.x + leftView.width / 2;
            leftView.width = center - leftView.x;
            rightView.width = rightView.x + rightView.width - center;
            rightView.x = center;
          }
          frame.setBounds(rightView);
        }
        else if (!wasMaximized && rightView.equals(curView)) {
          //restore default
          if (defaultView != null) {
            frame.setBounds(defaultView);
          }
          else {
            frame.setBounds(leftView);
          }
        }
        else {
          //move to left and store default bounds
          if (!wasMaximized || defaultView == null) setDefaultBounds(frame, curView);
          frame.setBounds(leftView);
        }
      }
    }
  }

  private void handleRight() {
    if (m_windowsKeyDown) {
      Frame frame = getActiveFrame();
      if (frame != null) {
        boolean wasMaximized = (frame.getExtendedState() & Frame.MAXIMIZED_BOTH) != 0;
        frame.setExtendedState(frame.getExtendedState() & ~Frame.MAXIMIZED_BOTH);
        //
        Rectangle curView = frame.getBounds();
        Rectangle defaultView = getDefaultBounds(frame);
        Rectangle r = SwingUtility.getFullScreenBoundsFor(curView, false);
        Rectangle leftView = r.getBounds();
        Rectangle rightView = r.getBounds();
        int center = leftView.x + leftView.width / 2;
        leftView.width = center - leftView.x;
        rightView.width = rightView.x + rightView.width - center;
        rightView.x = center;
        if (!wasMaximized && rightView.equals(curView)) {
          //move on opposite screen to left
          r = SwingUtility.getOppositeFullScreenBoundsFor(frame.getBounds(), false);
          if (r != null) {
            leftView = r.getBounds();
            rightView = r.getBounds();
            center = leftView.x + leftView.width / 2;
            leftView.width = center - leftView.x;
            rightView.width = rightView.x + rightView.width - center;
            rightView.x = center;
          }
          frame.setBounds(leftView);
        }
        else if (!wasMaximized && leftView.equals(curView)) {
          //restore default
          if (defaultView != null) {
            frame.setBounds(defaultView);
          }
          else {
            frame.setBounds(rightView);
          }
        }
        else {
          //move to right and store default bounds
          if (!wasMaximized || defaultView == null) setDefaultBounds(frame, curView);
          frame.setBounds(rightView);
        }
      }
    }
  }

  private static Frame getActiveFrame() {
    Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
    if (w instanceof Frame) {
      return (Frame) w;
    }
    else {
      return null;
    }
  }

  private static Rectangle getDefaultBounds(Frame f) {
    if (f instanceof RootPaneContainer) {
      JRootPane root = ((RootPaneContainer) f).getRootPane();
      if (root != null) {
        return (Rectangle) root.getClientProperty(CLIENT_PROPERTY_DEFAULT_BOUNDS);
      }
    }
    return null;
  }

  private static void setDefaultBounds(Frame f, Rectangle r) {
    if (f instanceof RootPaneContainer) {
      JRootPane root = ((RootPaneContainer) f).getRootPane();
      if (root != null) {
        root.putClientProperty(CLIENT_PROPERTY_DEFAULT_BOUNDS, r);
      }
    }
  }

}
