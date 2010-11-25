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
package org.eclipse.scout.rt.ui.swing.window.desktop.toolbar;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

/**
 * Shows a hand cursor when the cursor hovers over the adapted component.
 * 
 * @author awe
 */
public class HandCursorAdapater {

  private Cursor defaultCursor;

  private JComponent comp;

  private static final Cursor HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

  public HandCursorAdapater(final JComponent comp) {
    this.comp = comp;
    defaultCursor = comp.getCursor();
    comp.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        if (!comp.isEnabled()) return;
        showHandCursor();
      }

      @Override
      public void mouseExited(MouseEvent e) {
        if (!comp.isEnabled()) return;
        showDefaultCursor();
      }
    });
  }

  public void showDefaultCursor() {
    comp.setCursor(defaultCursor);
  }

  public void showHandCursor() {
    comp.setCursor(HAND_CURSOR);
  }
}
