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
package org.eclipse.scout.rt.ui.swing.ext.decoration;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import org.eclipse.scout.rt.ui.swing.ext.JTextFieldEx;
import org.eclipse.scout.rt.ui.swing.ext.MouseClickedBugFix;

/**
 *
 */
public class JTextFieldWithDecorationIcons extends JTextFieldEx {

  private static final long serialVersionUID = 1L;

  private IDecoration m_decorationIcon;
  private int m_originalMarginRight = -1;
  private int m_insetsRight = 0;

  private Cursor m_defaultCursor;

  private Insets m_cachedInsets;

  public JTextFieldWithDecorationIcons() {
    registerMouseMotionListener();
    m_defaultCursor = getCursor();
  }

  @Override
  public void paint(Graphics g) {
    setTextFieldMargin();
    super.paint(g);
    if (m_decorationIcon != null) {
      int x = getWidth() - m_decorationIcon.getWidth() - 6/*- m_insetsRight*/;
      int y = (getHeight() - m_decorationIcon.getHeight()) / 2;
      m_decorationIcon.paint(this, g, x, y);
    }
  }

  public void setDecorationIcon(IDecoration decorationIcon) {
    m_decorationIcon = decorationIcon;
    setTextFieldMargin();
  }

  public IDecoration getDecorationIcon() {
    return m_decorationIcon;
  }

  private void registerMouseMotionListener() {
    addMouseListener(new MouseAdapter() {
      MouseClickedBugFix fix;

      @Override
      public void mouseExited(MouseEvent e) {
        handleDecorationMouseExit(e);
      }

      @Override
      public void mousePressed(MouseEvent e) {
        fix = new MouseClickedBugFix(e);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (fix != null) {
          fix.mouseReleased(this, e);
        }
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        if (fix != null && fix.mouseClicked()) {
          return;
        }
        if (isDecorationIconRegion(e.getPoint())) {
          getDecorationIcon().handleMouseChlicked(e);
        }
      }
    });
    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        handleDecorationMouseMoved(e);
      }

    });

  }

  protected void handleDecorationMouseExit(MouseEvent e) {
    if (getDecorationIcon() != null) {
      DecorationMouseEvent ex = new DecorationMouseEvent(e);
      getDecorationIcon().handleMouseExit(ex);
      processChangesFromDecoration(ex);
    }
  }

  protected void handleDecorationMouseMoved(MouseEvent e) {
    if (getDecorationIcon() != null) {
      DecorationMouseEvent ex = new DecorationMouseEvent(e);
      getDecorationIcon().handleMouseMoved(ex);
      processChangesFromDecoration(ex);
    }
  }

  protected void processChangesFromDecoration(DecorationMouseEvent ex) {
    Cursor newCursor = ex.getCursorToApply();
    if (getCursor() != newCursor) {
      if (newCursor == null) {
        newCursor = m_defaultCursor;
      }
      setCursor(newCursor);
    }
    if (ex.isRepaintNeeded()) {
      repaint();
    }

  }

  public boolean isDecorationIconRegion(Point cursorPosition) {
    if (getDecorationIcon() == null) {
      return false;
    }
    if (cursorPosition.x >= getWidth() - getDecorationIcon().getWidth() - m_insetsRight) {
      return true;
    }
    return false;
  }

  /**
   * This method may be called multiple times.
   */
  private void setTextFieldMargin() {
    Insets marginAndBorderInsets = getInsets();
    Insets marginInsets = getMargin();
    if (m_originalMarginRight == -1) {
      m_originalMarginRight = marginInsets.right;
    }
    m_insetsRight = marginAndBorderInsets.right - marginInsets.right;
    int iconWidth = 0;
    if (getDecorationIcon() != null) {
      iconWidth = getDecorationIcon().getWidth();
    }

    Insets calculatedInsets = new Insets(0, 0, 0, m_originalMarginRight + iconWidth);
    //setMargin(Insets) causes a redraw of the component. Therefore, only repaint if the values have changed.
    if (m_cachedInsets == null || !m_cachedInsets.equals(calculatedInsets)) {
      m_cachedInsets = calculatedInsets;
      setMargin(calculatedInsets);
    }
  }
}
