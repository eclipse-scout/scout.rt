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

import javax.swing.border.Border;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.rt.ui.swing.ext.JTextFieldEx;
import org.eclipse.scout.rt.ui.swing.ext.MouseClickedBugFix;

/**
 *
 */
public class JTextFieldWithDecorationIcons extends JTextFieldEx {

  private static final long serialVersionUID = 1L;

  public static enum Region {
    Text, Decoration
  }

  protected static final Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);

  private IDecoration m_decorationIcon;

  private final Cursor m_defaultCursor;
  private final boolean m_backgroundDecoration;
  private Insets m_cachedMargin;

  private Insets m_originalMargin;

  public JTextFieldWithDecorationIcons() {
    this(false);
  }

  public JTextFieldWithDecorationIcons(boolean backgroundDecoration) {
    m_backgroundDecoration = backgroundDecoration;
    m_defaultCursor = getCursor();
    registerMouseMotionListener();
  }

  @Override
  public void paint(Graphics g) {
    updateTextFieldMargin();
    super.paint(g);
    postPaint(g);
  }

  /**
   * @param g
   */
  protected void postPaint(Graphics g) {
    if (m_decorationIcon != null) {
      Insets borderInsets = EMPTY_INSETS;
      if (getBorder() != null) {
        borderInsets = getBorder().getBorderInsets(null);
      }
      int decoIconWidth = m_decorationIcon.getWidth();
      int x = getWidth() - decoIconWidth - borderInsets.right;
      int y = (getHeight() - m_decorationIcon.getHeight()) / 2;
      m_decorationIcon.paint(this, g, x, y);
    }
  }

  public void setDecorationIcon(IDecoration decorationIcon) {
    m_decorationIcon = decorationIcon;
    repaint();
  }

  public IDecoration getDecorationIcon() {
    return m_decorationIcon;
  }

  public boolean isBackgroundDecoration() {
    return m_backgroundDecoration;
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
        if (getRegion(e.getPoint()) == Region.Decoration) {
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

  public Region getRegion(Point position) {
    if (getDecorationIcon() != null) {
      int xStartDecoration = getWidth() - getDecorationIcon().getWidth();
      if (getBorder() != null) {
        xStartDecoration -= getBorder().getBorderInsets(null).right;
      }
      if (position.x >= xStartDecoration) {
        return Region.Decoration;
      }
    }
    return Region.Text;

  }

  @Override
  public void setBorder(Border border) {
    super.setBorder(border);
  }

  @Override
  public void setMargin(Insets m) {
    m_originalMargin = m;
    updateTextFieldMargin();
  }

  /**
   * This method may be called multiple times.
   */
  protected void updateTextFieldMargin() {
    // no margin update on background decorations
    if (!isBackgroundDecoration()) {
      // compute new insets
      if (m_originalMargin == null) {
        m_originalMargin = getMargin();
        if (m_originalMargin == null) {
          m_originalMargin = EMPTY_INSETS;
        }
      }
      Insets borderInsets = EMPTY_INSETS;
      if (getBorder() != null) {
        borderInsets = getBorder().getBorderInsets(null);

      }
      Insets marginWithDecorationIcon = new Insets(m_originalMargin.top, m_originalMargin.left, m_originalMargin.bottom, borderInsets.right + m_originalMargin.right);
      if (getDecorationIcon() != null) {
        marginWithDecorationIcon.right += getDecorationIcon().getWidth();
      }
      if (!CompareUtility.equals(marginWithDecorationIcon, m_cachedMargin)) {
        m_cachedMargin = marginWithDecorationIcon;
        super.setMargin(m_cachedMargin);
      }
    }
  }
}
