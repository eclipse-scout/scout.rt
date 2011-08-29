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
package org.eclipse.scout.rt.ui.swing.window;

import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import javax.swing.JDialog;

/**
 * Waits until all of the dependent (child) windows are closed, then closes the
 * parent window. Used in case that a window closes but has active owned (child)
 * windows
 */
public class DependentCloseListener {
  private Window m_parent;
  private Vector<Window> m_dependingWindows;
  private WindowListener m_windowListener;
  private ComponentListener m_componentListener;

  public DependentCloseListener(Window parent) {
    m_parent = parent;
    Window[] ownedWindows = parent.getOwnedWindows();
    if (ownedWindows != null && ownedWindows.length > 0) {
      m_windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
          handleWindowHide(e.getWindow());
        }
      };
      m_componentListener = new ComponentAdapter() {
        @Override
        public void componentHidden(ComponentEvent e) {
          handleWindowHide((Window) e.getComponent());
        }
      };
      m_dependingWindows = new Vector<Window>();
      for (Window w : ownedWindows) {
        if (w.isVisible()) {
          // only depend on JDialog, do NOT depend on Popup$HeavyWeightWindow
          // since that one is not firing decent events.
          if (w instanceof JDialog) {
            m_dependingWindows.add(w);
            w.addWindowListener(m_windowListener);
            w.addComponentListener(m_componentListener);
          }
        }
      }
    }
  }

  private void handleWindowHide(Window w) {
    if (m_windowListener != null) {
      w.removeWindowListener(m_windowListener);
    }
    if (m_componentListener != null) {
      w.removeComponentListener(m_componentListener);
    }
    m_dependingWindows.remove(w);
    checkState();
  }

  public boolean close() {
    return checkState();
  }

  private boolean checkState() {
    if (m_parent != null && m_parent.isVisible()) {
      int activeCount = 0;
      if (m_dependingWindows != null && m_dependingWindows.size() > 0) {
        for (Window w : m_dependingWindows) {
          if (w.isVisible()) {
            activeCount++;
          }
        }
      }
      if (activeCount == 0) {
        m_parent.setVisible(false);
        m_parent.dispose();
        m_parent = null;
        if (m_dependingWindows != null) {
          m_dependingWindows.clear();
        }
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return true;
    }
  }

}
