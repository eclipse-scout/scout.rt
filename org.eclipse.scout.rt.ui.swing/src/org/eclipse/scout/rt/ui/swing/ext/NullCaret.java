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

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;

public class NullCaret implements Caret, FocusListener, MouseListener {
  private static final long serialVersionUID = 1L;

  private JTextComponent component;
  private boolean visible;

  @Override
  public void addChangeListener(ChangeListener l) {
  }

  @Override
  public void deinstall(JTextComponent c) {
  }

  @Override
  public int getBlinkRate() {
    return 0;
  }

  @Override
  public int getDot() {
    return 0;
  }

  @Override
  public Point getMagicCaretPosition() {
    return null;
  }

  @Override
  public int getMark() {
    return 0;
  }

  @Override
  public void install(JTextComponent c) {
    component = c;
    c.addFocusListener(this);
    c.addMouseListener(this);
    if (c.hasFocus()) {
      focusGained(null);
    }
  }

  @Override
  public boolean isSelectionVisible() {
    return true;
  }

  @Override
  public boolean isVisible() {
    return visible;
  }

  @Override
  public void moveDot(int dot) {
  }

  @Override
  public void paint(Graphics g) {
  }

  @Override
  public void removeChangeListener(ChangeListener l) {
  }

  @Override
  public void setBlinkRate(int rate) {
  }

  @Override
  public void setDot(int dot) {
  }

  @Override
  public void setMagicCaretPosition(Point p) {
  }

  @Override
  public void setSelectionVisible(boolean v) {
  }

  @Override
  public void setVisible(boolean v) {
    visible = v;
  }

  /**
   * Called when the component containing the caret loses focus. This is
   * implemented to set the caret to visibility to false.
   * 
   * @param e
   *          the focus event
   * @see FocusListener#focusLost
   */
  public void focusLost(FocusEvent e) {
    setVisible(false);
  }

  public void focusGained(FocusEvent e) {
    if (component.isEnabled()) {
      if (component.isEditable()) {
        setVisible(true);
      }
    }
  }

  private void adjustFocus(boolean inWindow) {
    if ((component != null) && component.isEnabled() && component.isRequestFocusEnabled()) {
      if (inWindow) {
        component.requestFocusInWindow();
      }
      else {
        component.requestFocus();
      }
    }
  }

  @Override
  public void mouseClicked(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  @Override
  public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      if (e.isConsumed()) {
      }
      else {
        adjustFocus(false);
      }
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if (!e.isConsumed()) {
      adjustFocus(false);
    }
  }
}
