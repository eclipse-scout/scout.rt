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
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;

/**
 * Since some l&f especially synth does not support for window resizing and window listener does not support for
 * in-window-exit events,
 * this global listener removes resize cursors on windows when they are not valid any more.
 */
public final class FixResizeCursorSupport implements AWTEventListener {

  private FixResizeCursorSupport() {
  }

  private static FixResizeCursorSupport listener;

  public static void install() {
    if (listener != null) {
      return;
    }
    listener = new FixResizeCursorSupport();
    Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.MOUSE_EVENT_MASK);
  }

  public static void uninstall() {
    if (listener == null) {
      return;
    }
    Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
    listener = null;
  }

  @Override
  public void eventDispatched(AWTEvent event) {
    if (event.getID() == MouseEvent.MOUSE_PRESSED) {
      for (Window w : Window.getWindows()) {
        if (!w.isVisible()) {
          return;
        }
        Cursor c = w.getCursor();
        if (c == null) {
          continue;
        }
        switch (c.getType()) {
          case Cursor.N_RESIZE_CURSOR:
          case Cursor.S_RESIZE_CURSOR:
          case Cursor.E_RESIZE_CURSOR:
          case Cursor.W_RESIZE_CURSOR:
          case Cursor.NE_RESIZE_CURSOR:
          case Cursor.NW_RESIZE_CURSOR:
          case Cursor.SE_RESIZE_CURSOR:
          case Cursor.SW_RESIZE_CURSOR: {
            w.setCursor(Cursor.getDefaultCursor());
            break;
          }
        }
      }
    }
  }

}
